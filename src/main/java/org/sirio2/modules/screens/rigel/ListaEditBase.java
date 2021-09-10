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
package org.sirio2.modules.screens.rigel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.velocity.context.Context;
import org.commonlib.utils.ClassOper;
import org.rigel2.exceptions.MissingListException;
import org.rigel2.table.html.AbstractHtmlTablePager;
import org.rigel2.table.html.wrapper.HtmlWrapperBase;
import org.rigel2.table.html.wrapper.ParametroListe;
import org.sirio2.CoreConst;
import org.sirio2.rigel.RigelUtils;
import org.sirio2.services.modellixml.MDL;
import org.sirio2.services.security.SEC;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;
import org.sirio2.utils.tree.CoreMenuTreeNode;

/**
 * Classe base di tutti i visualizzatori di liste edit XML.
 * @author Nicola De Nisco
 */
abstract public class ListaEditBase extends RigelEditBaseScreen
{
  public static final String TYPE_LISTA_EDIT = "ListaEditBase:type-lista-edit";

  @Override
  protected void doBuildTemplate2(CoreRunData data, Context context)
     throws Exception
  {
    String html = "", type = "", scTest = "";

    type = data.getParameters().getString("type");
    if(type == null)
      type = (String) (data.getSession().getAttribute(TYPE_LISTA_EDIT));

    HtmlWrapperBase eh = getLista(data, type);
    if(eh == null)
      throw new MissingListException(data.i18n("Lista %s non trovata. Controllare lista.xml.", type));

    Map params = SU.getParMap(data);
    HttpSession session = data.getSession();
    boolean forceNew = false, duplica = false;
    String baseUri = makeSelfUrl(data, type);

    HashMap<String, String> extraParams = new HashMap<>();
    extraParams.put("jlc", type);
    extraParams.put("jvm", ClassOper.getClassName(getClass()) + ".vm");

    synchronized(eh)
    {
      parseParamLista(eh, params, session);
      ((AbstractHtmlTablePager) eh.getPager()).setBaseSelfUrl(baseUri);

      String cmd = data.getParameters().getString("command"); // NOI18N
      if(cmd != null)
      {
        if(cmd.equals(CoreConst.SAVE_AND_NEW))
          forceNew = true;
        if(cmd.equals(CoreConst.DUP_CURRENT))
          forceNew = duplica = true;
      }

      if(!RigelUtils.checkPermessiLettura(data, eh))
      {
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }

      if(forceNew || SU.isOkStr(data.getParameters().getString("new"))) // NOI18N
      {
        if(!eh.isNewEnabled())
          throw new Exception(data.i18n("Creazione nuovi oggetti non consentita."));

        if(!RigelUtils.checkPermessiCreazione(data, eh))
        {
          // permessi della lista non posseduti dall'utente
          redirectUnauthorized(data);
          return;
        }
      }

      html = getHtmlEdit(data, context, eh, params, session, false, extraParams);
      scTest = getScriptTest(data, context, eh, params, session, false);

      data.getSession().setAttribute(TYPE_LISTA_EDIT, type);
    }

    String jlc = SU.okStr(params.get("jlc"));
    String jvm = SU.okStr(params.get("jvm"));

    context.put("jlc", jlc);
    context.put("jvm", jvm);
    context.put("type", type);
    context.put("baseUri", baseUri);
    context.put("phtml", html);
    context.put("edl", eh);
    context.put("scTest", "\n" + scTest);
    context.put("document", MDL.getDocument());

    if(SU.isOkStr(eh.getCustomScript()))
      context.put("cscriptm", eh.getCustomScript());

    if(eh.isHeaderButton())
    {
      List<CoreMenuTreeNode> lsMenu = makeHeaderButtons(eh, baseUri);
      if(!lsMenu.isEmpty())
      {
        StringBuilder sb = new StringBuilder(512);
        for(CoreMenuTreeNode tn : lsMenu)
          addButtonToMenu(tn, sb);

        context.put("hbuts", lsMenu);
        context.put("hbutshtml", sb.toString());
      }
    }

    if(eh.isEditEnabled())
      context.put("editEnabled", "1");
    if(eh.isSaveEnabled() && RigelUtils.checkPermessiScrittura(data, eh))
      context.put("saveEnabled", "1");
    if(eh.isNewEnabled() && RigelUtils.checkPermessiCreazione(data, eh))
      context.put("newEnabled", "1");
  }

  /**
   * Recupera eventuali parametri richiesti dalla lista.
   * @param wl wrapper con le indicazioni sui parametri da recuperare
   * @param param mappa dei parametri nella richiesta corrente
   * @param session sessione per il salvataggio permanente dei parametri
   * @return porzione della url con i parametri formattati
   * @throws Exception
   */
  public String parseParamLista(HtmlWrapperBase wl, Map param, HttpSession session)
     throws Exception
  {
    String passThroughParam = "";
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
//          pl.setValore(val.toString());
          SU.saveParam(session, type + pl.getHtmlCampo(), val);
          passThroughParam += "&" + pl.getHtmlCampo() + "=" + val;
        }
        log.debug("par=" + pl.getNome() + "(" + pl.getCampo() + ") [" + pl.getHtmlCampo() + "] val=" + pl.getValore());
      }
    }

    return passThroughParam;
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return isAuthorizedOne(data, "modifica_dati_generale");
  }

  @Override
  protected boolean isAuthorizedDelete(CoreRunData data)
     throws Exception
  {
    return SEC.checkAnyPermission(data, "cancella_dati_generale");
  }
}
