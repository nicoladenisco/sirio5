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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ClassUtils;
import org.commonlib5.utils.ArrayMap;
import org.json.JSONObject;
import org.rigel5.RigelI18nInterface;
import org.rigel5.db.sql.FiltroData;
import org.rigel5.table.FiltroListe;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.hTable;
import org.rigel5.table.sql.SqlBuilderRicercaGenerica;
import org.rigel5.table.sql.xml.SqlTableModel;
import org.rigel5.table.sql.xml.SqlWrapperListaXml;
import org.sirio5.services.localization.INT;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;
import org.sirio5.utils.velocity.VelocityParser;

/**
 * Renderizzatore per Tool Liste.
 * Estende ListaBase (quindi uno screen) per creare un contesto
 * di rendering simile a quello delle pagine principali.
 * Verrà utilizzato dal Tool per generare html ad hoc.
 * Questa versione genera json invece che html e si adatta alla datatable in modalida server processing.
 *
 * @author Nicola De Nisco
 */
public class ToolRenderDatatableRigel
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

    JSONObject rv = renderCoreJson((CoreRunData) data, ctx);
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
    buildCtx((CoreRunData) data, type, unique, footer, ctx);

    // salva il context in sessione per le successive chiamate dalla servlet ajax
    data.getSession().setAttribute(unique, ctx);

    StringWriter writer = new StringWriter(512);
    // renderizzazione Velocity con il modello caricato da risorsa
    try (InputStream is = ClassUtils.getResourceAsStream(getClass(), "/ToolDatatable.vm"))
    {
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");

      VelocityParser vp = new VelocityParser(ctx);
      vp.parseReader(reader, writer, "ToolDatatable.vm");
    }

    return writer.toString();
  }

  private void buildCtx(CoreRunData data, String type, String unique, boolean footer, Context ctx)
     throws Exception
  {
    SqlWrapperListaXml wxml = MDL.getListaXmlSql(type);
    wxml.init();
    ctx.put("wrapper", wxml);
    ctx.put("unique", unique);
    ctx.put("counter", new AtomicInteger(1));

    String tagTabelleList = TR.getString("tag.tabelle.list", "TABLE WIDTH=\"100%\" class=\"table\""); // NOI18N
    String tableStatement = "";

    // aggiunge la classe rigel-datatable e l'idir; il valore originale viene salvato per chiamate successive
    Matcher m1 = pTableClass.matcher(tagTabelleList);
    if(m1.find())
    {
      String classes = m1.group(1) + " rigel-datatable";
      tableStatement = m1.replaceAll("class='" + classes + "' id='idtable_" + unique + "'");
      ctx.put("tableStatement", tableStatement);
    }
    else
    {
      tableStatement = tagTabelleList + " class='rigel-datatable' id='idtable_" + unique + "'";
      ctx.put("tableStatement", tableStatement);
    }

    String commonHeader = doHeaderHtml(wxml.getPtm());
    ctx.put("commonHeader", commonHeader);
    if(footer)
      ctx.put("visFooter", true);
  }

  private JSONObject renderCoreJson(CoreRunData data, Context ctx)
     throws Exception
  {
    RigelI18nInterface i18n = new RigelHtmlI18n(data);
    ParameterParser pp = data.getParameters();
    int rStart = pp.getInt("start");
    int rLimit = data.getParameters().getInt("length");
    String search = data.getParameters().getString("search[value]");

    SqlWrapperListaXml wxml = (SqlWrapperListaXml) ctx.get("wrapper");
    SqlTableModel stm = (SqlTableModel) wxml.getPtm();

    int count = 1;
    ArrayMap<Integer, Integer> mapOrder = new ArrayMap<>();
    for(int i = 0; i < stm.getColumnCount(); i++)
    {
      int col = SU.parse(pp.getObject("order[" + i + "][column]"), -1);
      String dir = SU.okStr(pp.getObject("order[" + i + "][dir]")).toUpperCase();

      if(col != -1)
      {
        int idir = 0;
        if(dir.matches("ASC"))
          idir = count++;
        if(dir.matches("DESC"))
          idir = 1000 + (count++);

        mapOrder.put(col, idir);
      }
    }

    FiltroListe cSelezione = (FiltroListe) ctx.get("cSelezione");
    if(cSelezione == null || !checkFiltroValido(stm, cSelezione, search, mapOrder))
    {
      cSelezione = creaFiltro(stm, i18n, search, mapOrder);
      ctx.put("cSelezione", cSelezione);
    }

    stm.getQuery().setOffset(rStart);
    stm.getQuery().setLimit(rLimit);
    stm.getQuery().setFiltro((FiltroData) (cSelezione.getOggFiltro()));
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

  /**
   * In base alle impostazioni utente crea il FiltroListe con
   * all'interno gli opportuni filtri necessari.
   * @param params
   * @param stm
   * @param i18n
   * @return
   * @throws java.lang.Exception
   */
  private FiltroListe creaFiltro(SqlTableModel stm, RigelI18nInterface i18n, String search, Map<Integer, Integer> mapOrder)
     throws Exception
  {
    if(!stm.isInitalized())
      throw new Exception(i18n.msg("Oggetto table model non inizializzato."));

    ToolMascheraRicercaGenericaDatatable mgr = new ToolMascheraRicercaGenericaDatatable();
    mgr.init(new SqlBuilderRicercaGenerica(stm, "dummy"), stm, i18n);
    FiltroListe fl = new FiltroListe();
    fl.setOggFiltro(mgr.buildCriteriaSafe(search, mapOrder));
    fl.salvaInfoColonne(stm);
    return fl;
  }

  private boolean checkFiltroValido(SqlTableModel stm, FiltroListe cSelezione, String search, Map<Integer, Integer> mapOrder)
     throws Exception
  {
    cSelezione.recuperaInfoColonne(stm);

    for(int i = 0; i < stm.getColumnCount(); i++)
    {
      RigelColumnDescriptor cd = stm.getColumn(i);
      Integer direzione = mapOrder.get(i);

      // verifica per colonna prima in sorting e ora non piu
      if(direzione == null && cd.getFiltroSort() != 0)
        return false;

      // verifica per colonna prima in sorting e ora con direzione diversa ASC/DESC
      if(direzione != null && direzione != cd.getFiltroSort())
        return false;

      int simpleSearchColumn = 0, simpleSearchWeight = 0;

      if(!cd.isEscludiRicerca() && ((cd.getFiltroSort() % 1000) > simpleSearchWeight))
      {
        simpleSearchColumn = cd.getFiltroSort() > 1000 ? -(i + 1) : (i + 1);
        simpleSearchWeight = cd.getFiltroSort() % 1000;
      }

      if(cd.getRicercaSemplice() == 0)
        continue;

      if(!SU.isEqu(search, cd.getFiltroValore()))
        return false;
    }

    return true;
  }
}
