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
import javax.servlet.http.*;
import org.apache.turbine.util.*;
import org.apache.velocity.context.Context;
import org.commonlib5.utils.StringOper;
import org.rigel5.RigelCustomUrlBuilder;
import org.rigel5.SetupHolder;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.glue.custom.CustomButtonFactory;
import org.rigel5.glue.table.PeerAppMaintDispTable;
import org.rigel5.glue.table.PeerAppMaintFormTable;
import org.rigel5.table.html.hEditTable;
import org.rigel5.table.html.hTable;
import org.rigel5.table.html.wrapper.CustomButtonInfo;
import org.rigel5.table.html.wrapper.CustomButtonRuntimeInterface;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.html.*;
import org.sirio5.beans.menu.MenuItemBean;
import org.sirio5.modules.screens.CoreBaseScreen;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.LI;
import org.sirio5.utils.SU;
import org.sirio5.utils.tree.CoreMenuTreeNode;

/**
 * Classe base per l'editing dei dati.
 * Questa classe viene estesa da FormBase ovvero il papa' di
 * tutti i forms e da ListaEditBase ovvero l'editing dei dati
 * in modo tabellare.
 *
 * @author Nicola De Nisco
 */
abstract public class RigelEditBaseScreen extends CoreBaseScreen
{
  abstract public boolean isPopup();

  abstract public boolean isEditPopup();

  abstract protected String makeSelfUrl(RunData data, String type);

  protected PeerWrapperFormHtml getForm(CoreRunData data, String type)
     throws Exception
  {
    WrapperCacheBase wpc = MDL.getWrapperCache(data);
    return wpc.getFormCache(type);
  }

  protected HtmlWrapperBase getLista(CoreRunData data, String type)
     throws Exception
  {
    WrapperCacheBase wpc = MDL.getWrapperCache(data);
    return wpc.getListaEditCache(type);
  }

  public String makeImposta(PeerWrapperEditHtml wl, ListaInfo li)
     throws Exception
  {
    if(li.func == null || li.func.trim().length() == 0)
      return "";

    String campi = "";
    Enumeration enum1 = wl.getFoInfo().getForeignColumnsKeys();
    while(enum1.hasMoreElements())
    {
      String campo = (String) enum1.nextElement();
      campi += "," + campo;
    }
    campi = campi.substring(1);

    return ""
       + "  function imposta(" + campi + ")\r\n"
       + "  {\r\n"
       + "	window.opener." + li.func + "(" + campi + ");\r\n"
       + "	window.close();\r\n"
       + "  }\r\n"
       + "";
  }

  public String makeNuovo(PeerWrapperEditHtml wl)
     throws Exception
  {
    String urlNuovo = SU.okStrNull(wl.getUrlEditRiga());
    if(urlNuovo == null)
      return "";

    return "  function relCommandNuovo()\r\n"
       + "  {\r\n"
       + "    apriFinestraEdit('" + LI.mergeUrl(urlNuovo, "new", 1) + "', 'det" + SU.purge(wl.getNome()) + "');\r\n"
       + "  }\r\n"
       + "";
  }

  protected String getHtmlEdit(RunData data, Context context,
     HtmlWrapperBase pwl, Map params, HttpSession session, boolean forceNew, Map extraParams)
     throws Exception
  {
    hTable table = pwl.getTbl();
    synchronized(table)
    {
      table.setPopup(isPopup());
      table.setEditPopup(isEditPopup());
      table.setExtraParamsUrls(extraParams);
      return pwl.getHtmlForm(params, session);
    }
  }

  protected String getHtmlEdit(RunData data, Context context,
     PeerWrapperFormHtml pwl, Map params, HttpSession session, boolean forceNew, Map extraParams)
     throws Exception
  {
    if(pwl.getTbl() instanceof PeerAppMaintFormTable)
    {
      PeerAppMaintFormTable table = (PeerAppMaintFormTable) (pwl.getTbl());
      synchronized(table)
      {
        table.setPopup(isPopup());
        table.setEditPopup(isEditPopup());
        table.setExtraParamsUrls(extraParams);
        String html = table.getHtml(session, params, forceNew);
        context.put("objInEdit", table.getLastObjectInEdit());
        return html;
      }
    }
    else if(pwl.getTbl() instanceof PeerAppMaintDispTable)
    {
      PeerAppMaintDispTable table = (PeerAppMaintDispTable) (pwl.getTbl());
      synchronized(table)
      {
        table.setPopup(isPopup());
        table.setEditPopup(isEditPopup());
        table.setExtraParamsUrls(extraParams);
        String html = table.getHtml(params, session);
        return html;
      }
    }

    return "???";
  }

  protected String getScriptTest(RunData data, Context context,
     HtmlWrapperBase pwl, Map params, HttpSession session, boolean forceNew)
     throws Exception
  {
    if(pwl.getTbl() instanceof hEditTable)
      return SU.okStr(((hEditTable) pwl.getTbl()).getScriptTest());

    return "";
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return isAuthorizedOne(data, "visualizza_manutenzione");
  }

  protected boolean isAuthorizedDelete(CoreRunData data)
     throws Exception
  {
    return SEC.checkAllPermission(data, "cancella_manutenzione");
  }

  protected List<CoreMenuTreeNode> makeHeaderButtons(RunData data, HtmlWrapperBase lso, String baseUri)
     throws Exception
  {
    ArrayList<CoreMenuTreeNode> arBut = new ArrayList<>();
    RigelCustomUrlBuilder urlBuilder = SetupHolder.getUrlBuilder();

    for(int i = 0; i < lso.getNumHeaderButtons(); i++)
    {
      CustomButtonInfo cb = lso.getHeaderButton(i);
      cb.addRuntimeParam("user", data.getUser());

      MenuItemBean mb = new MenuItemBean();
      boolean popup = isPopup() || cb.getPopup() > 0;
      String script = null, url = null;

      if(cb.haveClassName() && cb.getCbri() == null)
      {
        // istanzia oggetto custom di controllo del bottone (solo la prima volta)
        cb.setCbri(CustomButtonFactory.getInstance().getCustomButton(cb.getClassName()));
      }

      CustomButtonRuntimeInterface cbri;
      if((cbri = cb.getCbri()) != null)
      {
        cb = cbri.setHeaderData(cb, lso);

        // se non visibile per questa riga salta a successivo
        if(!cbri.isVisible())
          continue;
      }

      if(cbri == null || cbri.isEnabled())
      {
        if(cb.haveJavascript())
        {
          script = cb.makeJavascript(lso.getPtm(), 0);
        }
        else
        {
          // se url == null vuol dire che questo custom button non è applicabile
          if((url = urlBuilder.buildUrlHeaderButton(popup, lso.getPtm(), cb)) == null)
            continue;

          url = StringOper.strReplace(url, LI.getContextPath() + "@self", baseUri);
        }

        // modifica url in base alle opzioni del custom button
        if(url != null)
        {
          if(cb.getPopup() > 0)
          {
            script = "apriPopup" + cb.getPopup() + "('" + url + "', '" + StringOper.purge(cb.getText()) + "')";
          }
          else
          {
            // url semplice: in questo caso possiamo chiedere conferma all'utente se previsto
            if(cb.haveConfirm())
            {
              String confirm = cb.makeConfirmMessage(lso.getPtm(), 0, 0);

              // se confirm == null vuol dire che questo custom button non è applicabile
              if(confirm == null)
                continue;

              script = "confermaCB('" + confirm + "', '" + url + "')";
            }
          }
        }

        if(script == null && url == null)
          continue;

        mb.setProgramma(script == null ? "goLink('" + url + "')" : script);
        mb.setDescrizione(cb.getText());
        arBut.add(new CoreMenuTreeNode(mb));
      }
    }

    return arBut;
  }

  public void addButtonToMenu(CoreMenuTreeNode tn, StringBuilder sb)
  {
    MenuItemBean mb = tn.getMenuItem();
    sb.append("<li><a href=\"javascript:").append(mb.getProgramma()).append("\">")
       .append(mb.getDescrizione()).append("</a>");

    if(!tn.isLeaf())
    {
      sb.append("<ul>");
      for(int i = 0; i < tn.getChildCount(); i++)
        addButtonToMenu((CoreMenuTreeNode) tn.getChildAt(i), sb);
      sb.append("</ul>");
    }

    sb.append("</li>");
  }
}
