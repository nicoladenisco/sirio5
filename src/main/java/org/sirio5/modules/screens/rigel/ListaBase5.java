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
package org.sirio5.modules.screens.rigel;

import java.util.*;
import javax.servlet.http.HttpSession;
import org.apache.velocity.context.*;
import org.commonlib5.utils.ClassOper;
import org.commonlib5.utils.StringOper;
import org.rigel5.*;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.table.MascheraRicercaGenerica;
import org.rigel5.table.html.AbstractHtmlTablePager;
import org.rigel5.table.html.AbstractHtmlTablePagerFilter;
import org.rigel5.table.html.HtmlMascheraRicercaGenericaNoscript;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.wrapper.*;
import org.rigel5.table.peer.html.*;
import org.rigel5.table.sql.SqlBuilderRicercaGenerica;
import org.rigel5.table.sql.html.*;
import org.sirio5.beans.xml.ReferenceXmlInfo;
import org.sirio5.rigel.RigelUtils;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.tree.CoreMenuTreeNode;

/**
 * Classe base di tutti i visualizzatori di lista XML.
 *
 * @author Nicola De Nisco
 */
abstract public class ListaBase5 extends RigelEditBaseScreen
{
  /**
   * Interroga la cache per verificare se la lista esiste
   * e di che tipo di lista sia (sql o peer)
   * @param data
   * @param type nome della lista
   * @return la lista se esiste altrimenti null
   * @throws Exception
   */
  protected HtmlWrapperBase getListaCache(CoreRunData data, String type)
     throws Exception
  {
    HtmlWrapperBase hwb = null;
    WrapperCacheBase wpc = MDL.getWrapperCache(data);

    if((hwb = wpc.getListaCache(type)) == null)
      if((hwb = wpc.getListaEditCache(type)) == null)
        return null;

    AbstractHtmlTablePagerFilter pager = (AbstractHtmlTablePagerFilter) hwb.getPager();
    if(pager.getMascheraRicerca() == null)
      pager.setMascheraRicerca(getMascheraRG(pager.getI18n(), (SqlTableModel) pager.getTableModel()));

    return hwb;
  }

  public MascheraRicercaGenerica getMascheraRG(RigelI18nInterface i18n, SqlTableModel tm)
     throws Exception
  {
    String nometab = tm.getQuery().getVista();
    HtmlMascheraRicercaGenericaNoscript rv = new HtmlMascheraRicercaGenericaNoscript();
    rv.init(new SqlBuilderRicercaGenerica(tm, nometab), tm, i18n);
    return rv;
  }

  @Override
  protected void doBuildTemplate2(CoreRunData data, Context context)
     throws Exception
  {
    ListaInfo li = ListaInfo.getFromSession(data);
    doBuildTemplateListaInfo(data, context, li);
  }

  public void doBuildTemplateListaInfo(CoreRunData data, Context context, ListaInfo li)
     throws Exception
  {
    HtmlWrapperBase lso = getListaCache(data, li.type);

    synchronized(lso)
    {
      String baseUri = makeSelfUrl(data, li.type);
      ((AbstractHtmlTablePager) lso.getPager()).setBaseSelfUrl(baseUri);

      if(!RigelUtils.checkPermessiLettura(data, lso))
      {
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }

      String cmd = data.getParameters().getString("command");
      if(cmd != null && cmd.equals("cancella"))
      {
        if(!isAuthorizedDelete(data) || !RigelUtils.checkPermessiCancellazione(data, lso))
          throw new Exception(data.i18n("Tentativo di cancellazione senza credenziali sufficienti."));

        /*
         Con queste itruzioni la cancellazione diventa fisica:
         il record viene effettivamente rimosso dal database.
         In Antigua comunque viene utilizzata la cancellazione
         logica: il campo STATO_REC del record viene posto a 1.
         pwl.getPtm().deleteByQueryKey(
         data.getParameters().getString("key"));
         */
        String sKey = data.getParameters().getString("key");
        RigelUtils.deleteRecord(data, sKey, lso);
      }

      context.put("baseUri", baseUri);
      context.put("type", li.type);

      makeContextHtml(lso, li, data, context, baseUri);
    }
  }

  protected void makeContextHtml(HtmlWrapperBase lso, ListaInfo li, CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    Map param = SU.getParMap(data);

    HashMap<String, String> extraParams = new HashMap<>();
    extraParams.put("jlc", li.type);
    extraParams.put("jvm", ClassOper.getClassName(getClass()) + ".vm");

    String titolo = data.i18n(lso.getTitolo());
    String header = data.i18n(lso.getHeader());

    if(lso instanceof SqlWrapperListaHtml)
    {
      li.passThroughParam = parseParamLista((SqlWrapperListaHtml) lso, param, data.getSession());
      ReferenceXmlInfo.saveInfo(
         li.type, header, titolo,
         lso.getPtm(), (SqlPager) lso.getPager(), data.getSession(), lso.getEleXml());
    }
    else
    {
      li.passThroughParam = null;
      ReferenceXmlInfo.saveInfo(
         li.type, header, titolo,
         lso.getPtm(), (PeerPager) lso.getPager(), data.getSession(), lso.getEleXml());
    }

    // costruisce la url per il pulsante 'new'
    li.urlNuovo = makeUrlNuovo(lso, li, extraParams);

    // costruisce componenti pagina
    lso.getTbl().setExtraParamsUrls(extraParams);
    RigelHtmlPage page = new RigelHtmlPage();
    lso.getHtml(param, data.getSession(), page);

    // inserisce nel context i componeti pagina generati
    int filtro = StringOper.parse(param.get("filtro"), 0);
    formatHtmlLista(filtro, page, context);

    context.put("urlNuovo", li.urlNuovo);
    context.put("header", header);
    context.put("titolo", titolo);
    context.put("document", MDL.getDocument());
    context.put("prstampe", data.getContextPath() + "/pdf/fop/jsrefxml");
    context.put("numrows", lso.getPtm().getRowCount());

    if(SU.isOkStr(lso.getCustomScript()))
      context.put("cscript", lso.getCustomScript());

    if(lso.isHeaderButton())
    {
      List<CoreMenuTreeNode> lsMenu = makeHeaderButtons(data, lso, baseUri);
      if(!lsMenu.isEmpty())
      {
        StringBuilder sb = new StringBuilder(512);
        for(CoreMenuTreeNode tn : lsMenu)
          addButtonToMenu(tn, sb);

        context.put("hbuts", lsMenu);
        context.put("hbutshtml", sb.toString());
      }
    }

    if(lso.isEditEnabled())
      context.put("editEnabled", "1");
    if(lso.isSaveEnabled() && RigelUtils.checkPermessiScrittura(data, lso))
      context.put("saveEnabled", "1");
    if(lso.isNewEnabled() && RigelUtils.checkPermessiCreazione(data, lso))
      context.put("newEnabled", "1");
  }

  protected void makeContextHtmlSimplified(HtmlWrapperBase lso, ListaInfo li, CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    Map param = SU.getParMap(data);

    String titolo = data.i18n(lso.getTitolo());
    String header = data.i18n(lso.getHeader());

    // costruisce componenti pagina
    RigelHtmlPage page = new RigelHtmlPage();
    lso.getHtml(param, data.getSession(), page);

    // inserisce nel context i componeti pagina generati
    int filtro = StringOper.parse(param.get("filtro"), 0);
    formatHtmlLista(filtro, page, context);

    context.put("header", header);
    context.put("titolo", titolo);
    context.put("document", MDL.getDocument());
    context.put("numrows", lso.getPtm().getRowCount());

    if(SU.isOkStr(lso.getCustomScript()))
      context.put("cscript", lso.getCustomScript());

    if(lso.isEditEnabled())
      context.put("editEnabled", "1");
    if(lso.isSaveEnabled() && RigelUtils.checkPermessiScrittura(data, lso))
      context.put("saveEnabled", "1");
    if(lso.isNewEnabled() && RigelUtils.checkPermessiCreazione(data, lso))
      context.put("newEnabled", "1");
  }

  public void formatHtmlLista(int filtro, RigelHtmlPage page, Context context)
     throws Exception
  {
    StringBuilder sb = new StringBuilder(4096);

    // prima emette tutti i commenti
    page.buildPart(sb, PageComponentType.COMMENT, "<!--", "-->");

    if(filtro == AbstractHtmlTablePagerFilter.FILTRO_MACHERA)
    {
      // html ricerca avanzata
      page.buildPart(sb, PageComponentType.HTML, "search", "", "");
    }
    else
    {
      // html ricerca semplice
      page.buildPart(sb, PageComponentType.HTML, "simplesearch", "", "");

      // html principale
      StringBuilder sbBody = new StringBuilder(4096);
      sbBody.append("<div class=\"rigel_body\">");
      page.buildPart(sbBody, PageComponentType.HTML, "body", "", "");
      sbBody.append("</div>");
      sb.append(sbBody);
      context.put("bodyhtml", sbBody.toString());

      // html navigazione
      page.buildPart(sb, PageComponentType.HTML, "nav", "", "");
    }

    context.put("phtml", sb.toString());
  }

  /**
   * Costruisce url per pulsante 'Nuovo'.
   * @param wl wrapper con le informazioni sulla lista
   * @param li dati correnti della lista
   * @param extraParams parametri addizionali per la crezione dell'url
   * @return la url per il pulsante
   * @throws Exception
   */
  public String makeUrlNuovo(HtmlWrapperBase wl, ListaInfo li, HashMap<String, String> extraParams)
     throws Exception
  {
    String urlNuovo = SU.okStrNull(wl.getUrlEditRiga());
    if(urlNuovo == null)
      return "";

    if(li.passThroughParam != null && !li.passThroughParam.isEmpty())
      urlNuovo = HtmlUtils.mergeUrl(urlNuovo, li.passThroughParam);

    RigelCustomUrlBuilder urlBuilder = SetupHolder.getUrlBuilder();
    return urlBuilder.buildUrlNewRecord(isPopup(), urlNuovo, wl.getPtm(), extraParams);
  }

  /**
   * Recupera eventuali parametri richiesti dalla lista.
   * @param wl wrapper con le indicazioni sui parametri da recuperare
   * @param param mappa dei parametri nella richiesta corrente
   * @param session sessione per il salvataggio permanente dei parametri
   * @return porzione della url con i parametri formattati
   * @throws Exception
   */
  public Map parseParamLista(SqlWrapperListaHtml wl, Map param, HttpSession session)
     throws Exception
  {
    Map<String, String> passThroughParam = new HashMap();
    String type = wl.getNome();

    synchronized(wl)
    {
      for(ParametroListe pl : wl.getFiltro().getParametri())
      {
        Object val = param.get(pl.getHtmlCampo());
        if(val == null)
          val = param.get(type + pl.getHtmlCampo());

        if(val != null)
        {
          SU.saveParam(session, type + pl.getHtmlCampo(), val);
          passThroughParam.put(pl.getHtmlCampo(), SU.okStr(val));
        }

        log.debug("par=" + pl.getNome() + "(" + pl.getCampo() + ") [" + pl.getHtmlCampo() + "] val=" + val);
      }

      if(wl.ssp.getGroupby() != null)
      {
        for(ParametroListe pl : wl.ssp.getGroupby().filtro.getParametri())
        {
          Object val = param.get(pl.getHtmlCampo());
          SU.saveParam(session, pl.getHtmlCampo(), val);
          log.debug("par=" + pl.getNome() + "(" + pl.getCampo() + ") [" + pl.getHtmlCampo() + "] val=" + val);
        }
      }
    }

    return passThroughParam;
  }
}
