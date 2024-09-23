/*
 * Copyright (C) 2020 Nicola De Nisco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.sirio5.rigel;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.commonlib5.utils.ArrayMap;
import org.json.JSONObject;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.hTable;
import org.rigel5.table.sql.xml.SqlTableModel;
import org.rigel5.table.sql.xml.SqlWrapperListaXml;
import org.sirio5.services.localization.INT;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Renderizzatore per Tool Liste.
 * Estende ListaBase (quindi uno screen) per creare un contesto
 * di rendering simile a quello delle pagine principali.
 * Verrà utilizzato dal Tool per generare html ad hoc.
 * Questa versione genera json invece che html e si adatta alla datatable in modalida server processing.
 *
 * @author Nicola De Nisco
 */
public class ToolRenderDatatableRigel2
{
  protected final ToolRigelUIManagerDatatable uim = new ToolRigelUIManagerDatatable();
  protected final ToolCustomUrlBuilder urb = new ToolCustomUrlBuilder();
  protected static final Pattern pTableClass = Pattern.compile("class=[\'|\"](.+?)[\'|\"]");

  /**
   * Produce JSON per il Tool delle liste.
   * Questa funzione viene chiamata dalla servlet ajax ToolDirectHtml.
   *
   * <pre>
   * {
   * "draw": 1,
   * "recordsTotal": 57,
   * "recordsFiltered": 57,
   * "data": [
   * [
   * "Airi",
   * "Satou",
   * "Accountant",
   * "Tokyo",
   * "28th Nov 08",
   * "$162,700"
   * ],
   * ...
   * </pre>
   *
   * @param data dati di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderJson(RunData data)
     throws Exception
  {
    String ctxUnique = data.getParameters().getString("unique");
    Context ctx = (Context) data.getSession().getAttribute(ctxUnique);
    if(ctx == null)
      throw new Exception(INT.I("Context non presente in sessione; tool non disponibile."));

    JSONObject rv = renderCoreJson(data, ctx);
    return rv.toString();
  }

  /**
   * Produce HTML per il Tool delle liste.
   * @param data dati di chiamata
   * @param ctx context di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public synchronized String renderHtml(RunData data, Context ctx)
     throws Exception
  {
    int counter = (int) ctx.get("count");
    String type = data.getParameters().getString("type");
    String unique = "LISTA_" + SU.purge(type) + "_" + counter;
    boolean footer = false;

    // recupera parametri del tool e li passa in RunData
    Map<String, String> mp = (Map<String, String>) ctx.get("paramsMap");
    if(mp != null)
    {
      for(Map.Entry<String, String> entry : mp.entrySet())
      {
        String key = entry.getKey();
        String value = entry.getValue();
        data.getParameters().setString(key, value);
      }

      footer = SU.checkTrueFalse(mp.get("footer"), footer);
    }

    // costruisce tutti i componenti di pagina
    buildCtx(data, type, unique, footer, ctx);

    // salva il context in sessione per le successive chiamate dalla servlet ajax
    data.getSession().setAttribute(unique, ctx);

    String js
       = "\n"
       + "<SCRIPT>\n"
       + "    // attivazione datatable per rigel\n"
       + "    $(\"#idtable_" + unique + "\").DataTable({\n"
       + "      ajax: \"" + data.getContextPath() + "/rigeltool/datatable/unique/" + unique + "\",\n"
       + "      processing: true,\n"
       + "      serverSide: true\n"
       + "    });\n"
       + "</SCRIPT>\n"
       + "\n";

    String html = SU.okStr(ctx.get("html"));
    return html + js;
  }

  private void buildCtx(RunData data, String type, String unique, boolean footer, Context ctx)
     throws Exception
  {
    SqlWrapperListaXml wxml = MDL.getListaXmlSql(type);
    wxml.init();
    ctx.put("wrapper", wxml);
    ctx.put("unique", unique);
    ctx.put("counter", new AtomicInteger(1));

    String tagTabelleList = TR.getString("tag.tabelle.list", "TABLE WIDTH=\"100%\" class=\"table\""); // NOI18N
    String tableStatement = "";

    // aggiunge la classe rigel-datatable e l'id; il valore originale viene salvato per chiamate successive
    Matcher m1 = pTableClass.matcher(tagTabelleList);
    if(m1.find())
    {
      String classes = m1.group(1) + " rigel-datatable";
      tableStatement = m1.replaceAll("class='" + classes + "' id='idtable_" + unique + "'");
      ctx.put("tableStatement", tableStatement);
    }
    else
    {
      throw new Exception("il tag table deve avere una definizione di classe CSS");
    }

    StringBuilder html = new StringBuilder();
    String commonHeader = doHeaderHtml(wxml.getPtm());

    html.append("<").append(tableStatement).append(">\n"
       + "    <thead>\n"
       + "        <tr>\n"
    );
    html.append(commonHeader).append(
       "\n"
       + "        </tr>\n"
       + "    </thead>\n"
    );

    if(footer)
    {
      html.append(""
         + "    <tfoot>\n"
         + "        <tr>\n"
      );
      html.append(commonHeader).append(
         "\n"
         + "        </tr>\n"
         + "    </tfoot>\n"
      );
    }

    html.append(""
       + "</table>"
    );

    ctx.put("html", html.toString());
  }

  private JSONObject renderCoreJson(RunData data, Context ctx)
     throws Exception
  {
    ParameterParser pp = data.getParameters();
    int rStart = pp.getInt("start");
    int rLimit = data.getParameters().getInt("length");
    String search = data.getParameters().getString("search");

    SqlWrapperListaXml wxml = (SqlWrapperListaXml) ctx.get("wrapper");
    SqlTableModel stm = (SqlTableModel) wxml.getPtm();

    ArrayMap mapOrder = new ArrayMap();
    for(int i = 0; i < stm.getColumnCount(); i++)
    {
      Object col = pp.getObject("order[" + i + "][column]");
      Object dir = pp.getObject("order[" + i + "][dir]");
      mapOrder.put(col, dir);
    }

    // TODO: converte confronta search e mapOrder con valori precedentemente salvati
    // se serve salva nei descrittori di colonna e applica filtro
    //
    stm.getQuery().setOffset(rStart);
    stm.getQuery().setLimit(rLimit);
    stm.rebind();

    AtomicInteger counter = (AtomicInteger) ctx.get("counter");
    JSONObject out = new JSONObject();
    out.put("draw", counter.getAndIncrement());
    out.put("recordsTotal", stm.getTotalRecords());
    out.put("recordsFiltered", stm.getTotalRecords());

    ToolJsonDatatable table = new ToolJsonDatatable();
    table.setModel(stm);
    table.setColumnModel(stm.getColumnModel());
    table.doRows(out);

    return out;
  }

  /**
   * Produce l'header della tabella
   * @throws java.lang.Exception
   */
  private String doHeaderHtml(RigelTableModel tableModel)
     throws Exception
  {
    hTable table = new hTable();
    table.setModel(tableModel);
    table.setColumnModel(tableModel.getColumnModel());
    table.setHeaderStatement("tr");
    table.setColheadStatement("th");

    StringBuilder html = new StringBuilder();

    for(int i = 0; i < tableModel.getColumnCount(); i++)
    {
      html.append(table.doCellHeader(i));
    }

    return html.toString().replace("/TD", "/th");
  }
}
