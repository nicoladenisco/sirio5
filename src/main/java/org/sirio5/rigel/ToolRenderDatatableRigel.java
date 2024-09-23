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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ClassUtils;
import org.commonlib5.utils.HtmlTableJsonConverter;
import org.json.JSONObject;
import org.rigel5.SetupHolder;
import org.rigel5.glue.table.AlternateColorTableAppBase;
import org.rigel5.table.html.AbstractHtmlTablePagerFilter;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.PeerBuilderRicercaGenerica;
import org.rigel5.table.peer.html.PeerTableModel;
import org.rigel5.table.sql.SqlBuilderRicercaGenerica;
import org.rigel5.table.sql.html.SqlTableModel;
import static org.sirio5.CoreConst.*;
import org.sirio5.modules.screens.rigel.ListaBase5;
import org.sirio5.modules.screens.rigel.ListaInfo;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
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
public class ToolRenderDatatableRigel extends ListaBase5
{
  protected final ToolRigelUIManagerDatatable uim = new ToolRigelUIManagerDatatable();
  protected final ToolCustomUrlBuilder urb = new ToolCustomUrlBuilder();
  protected String unique = null, funcNameEdit, funcNameSubmit, funcNameSplit, formName, bodyName;
  protected int counter;
  protected static final Pattern pTableClass = Pattern.compile("class=[\'|\"](.+?)[\'|\"]");

  @Override
  public boolean isPopup()
  {
    return false;
  }

  @Override
  public boolean isEditPopup()
  {
    return true;
  }

  @Override
  protected String makeSelfUrl(RunData data, String type)
  {
    if(unique == null)
      unique = "LISTA_" + SU.purge(type) + "_" + counter;

    return data.getContextPath() + "/rigeltool/datatable/type/" + type + "/unique/" + unique;
  }

  @Override
  protected void makeContextHtml(HtmlWrapperBase lso, ListaInfo li, CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    CoreCustomUrlBuilder ub = (CoreCustomUrlBuilder) SetupHolder.getUrlBuilder();
    urb.setBaseMainForm(ub.getBaseMainForm());
    urb.setBaseMainList(ub.getBaseMainList());
    urb.setBasePopupForm(ub.getBasePopupForm());
    urb.setBasePopupList(ub.getBasePopupList());

    context.put("unique", unique);
    funcNameEdit = "rigel.apriEditTool";
    context.put("funcNameEdit", funcNameEdit);
    funcNameSubmit = "submit_" + unique;
    context.put("funcNameSubmit", funcNameSubmit);
    formName = "fo_" + unique;
    context.put("formName", formName);
    bodyName = "body_" + unique;
    context.put("bodyName", bodyName);

    lso.setUim(uim);

    ParameterParser pp = data.getParameters();
    AlternateColorTableAppBase act = (AlternateColorTableAppBase) (lso.getTbl());
    act.setDatatable(true);
    act.setAuthDelete(isAuthorizedDelete(data));
    act.setPopup(SU.checkTrueFalse(pp.getString("popup"), true));
    act.setEditPopup(SU.checkTrueFalse(pp.getString("editPopup"), true));
    act.setAuthSel(SU.checkTrueFalse(pp.getString("authSel"), true));
    act.setPopupEditFunction(funcNameEdit);
    act.setUrlBuilder(urb);
    urb.setFunc(li.func);
    urb.setType(li.type);

    // aggiunge la classe rigel-datatable e l'id; il valore originale viene salvato per chiamate successive
    String tableStatement = SU.okStr(context.get("tableStatement"), act.getTableStatement());
    Matcher m1 = pTableClass.matcher(tableStatement);
    if(m1.find())
    {
      String classes = m1.group(1) + " rigel-datatable";
      String newtblsta = m1.replaceAll("class='" + classes + "' id='idtable_" + unique + "'");
      act.setTableStatement(newtblsta);
      context.put("tableStatement", tableStatement);
    }
    else
    {
      throw new Exception("il tag table deve avere una definizione di classe CSS");
    }

    AbstractHtmlTablePagerFilter flt = (AbstractHtmlTablePagerFilter) lso.getPager();
    flt.setFormName(formName);
    //flt.setUim(uim);
    flt.setI18n(new RigelHtmlI18n(data));
    String baseurl = flt.getBaseSelfUrl();
    context.put("selfurl", baseurl);

    if(lso.getPtm() instanceof SqlTableModel)
    {
      SqlTableModel tm = (SqlTableModel) lso.getPtm();
      String nometab = tm.getQuery().getVista();
      flt.setMascheraRicerca(new ToolRicercaDatatable(new SqlBuilderRicercaGenerica(tm, nometab),
         tm, act.getI18n(), unique, baseurl));
    }
    else if(lso.getPtm() instanceof PeerTableModel)
    {
      PeerTableModel tm = (PeerTableModel) lso.getPtm();
      flt.setMascheraRicerca(new ToolRicercaDatatable(new PeerBuilderRicercaGenerica(tm, tm.getTableMap()),
         tm, act.getI18n(), unique, baseurl));
    }

    // la prima volta si presuppone non ci siano filtri, quindi il numero di record totali
    long numRecords = flt.getTotalRecords();
    if(!context.containsKey("recordsTotal"))
      context.put("recordsTotal", numRecords);

    // numero di record secondo il filtro applicato
    context.put("recordsFiltered", numRecords);

    super.makeContextHtml(lso, li, data, context, baseUri);
  }

  public void buildCtx(RunData data, Context ctx)
     throws Exception
  {
    doBuildTemplate2((CoreRunData) data, ctx);
  }

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

    String html = renderHtml(data);

    // converte html in json
    // TODO: da rifare: conversione non possibile
    HtmlTableJsonConverter cvt = new HtmlTableJsonConverter();
    String jdata = cvt.convertHtml2Json(html);

    JSONObject rv = new JSONObject();
    rv.put("draw", "0");
    rv.put("recordsTotal", ctx.get("recordsTotal"));
    rv.put("recordsFiltered", ctx.get("recordsFiltered"));
    rv.put("data", jdata);

    return rv.toString();
  }

  /**
   * Produce HTML per il Tool delle liste.
   * Questa funzione viene chiamata dalla servlet ajax ToolDirectHtml.
   * @param data dati di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderHtml(RunData data)
     throws Exception
  {
    String ctxUnique = data.getParameters().getString("unique");
    Context ctx = (Context) data.getSession().getAttribute(ctxUnique);
    if(ctx == null)
      throw new Exception(INT.I("Context non presente in sessione; tool non disponibile."));

    unique = ctxUnique;
    String html = renderHtml(data, ctx);
    return cutHtml(html);
  }

  protected String cutHtml(String html)
  {
    // da tutto l'html estrae solo la parte racchiusa da <form></form>
    // il resto non si può toccare

    int pos1, pos2;
    if((pos1 = html.indexOf(HTML_START_CUT)) != -1)
      if((pos2 = html.indexOf(HTML_END_CUT, pos1)) != -1)
        return html.substring(pos1, pos2);

    return html;
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
    boolean suppressEmpty = false;
    String suppressEmptyMessage = "";
    counter = (int) ctx.get("count");
    unique = null;

    // recupera parametri del tool e li passa in RunData
    Map<String, String> mp = (Map<String, String>) ctx.get("paramsMap");
    if(mp != null)
    {
      suppressEmpty = SU.checkTrueFalse(mp.get("suppressEmpty"));
      suppressEmptyMessage = SU.okStr(mp.get("suppressEmptyMessage"), suppressEmptyMessage);

      for(Map.Entry<String, String> entry : mp.entrySet())
      {
        String key = entry.getKey();
        String value = entry.getValue();
        data.getParameters().setString(key, value);
      }
    }

    // costruisce tutti i componenti di pagina
    buildCtx(data, ctx);

    // salva il context in sessione per le successive chiamate dalla servlet ajax
    data.getSession().setAttribute(unique, ctx);

    if(suppressEmpty && SU.parse(ctx.get("numrows"), -1) == 0)
      return suppressEmptyMessage;

    StringWriter writer = new StringWriter(512);
    // renderizzazione Velocity con il modello caricato da risorsa
    try (InputStream is = ClassUtils.getResourceAsStream(getClass(), "/ToolDatatable.vm"))
    {
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");

      VelocityParser vp = new VelocityParser(ctx);
      vp.parseReader(reader, writer, "ToolDatatable.vm");
    }

    String html = cutHtml(writer.toString());

    String js
       = "\n"
       + "<SCRIPT>\n"
       + "    // attivazione datatable per rigel\n"
       + "    $(\"#idtable_" + unique + "\").DataTable({\n"
       + "      ajax: \"" + data.getContextPath() + "/rigeltool/datatable/unique/" + unique + "\",\n"
       + "      deferLoading: " + ctx.get("recordsTotal") + ",\n"
       + "      processing: true,\n"
       + "      serverSide: true\n"
       + "    });\n"
       + "</SCRIPT>\n"
       + "\n";

    // rimaneggia javascript sostituendo submit con funzione specifica
    String url = (String) ctx.get("selfurl");
    return SU.strReplace(html + js,
       "document." + formName + ".submit();",
       "rigel.submitTool('" + unique + "', '" + url + "')");
  }

  @Override
  public void formatHtmlLista(int filtro, RigelHtmlPage page, Context context)
     throws Exception
  {
    context.put("filtro", filtro);
    context.put("htpage", page);
  }
}
