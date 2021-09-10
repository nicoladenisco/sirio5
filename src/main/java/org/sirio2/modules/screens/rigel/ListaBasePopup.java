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

import java.util.*;

import org.rigel2.HtmlUtils;
import org.rigel2.RigelCustomUrlBuilder;
import org.rigel2.SetupHolder;
import org.rigel2.table.html.wrapper.HtmlWrapperBase;
import org.rigel2.glue.table.AlternateColorTableAppBase;
import org.apache.velocity.context.*;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Visualizzazione di dati tabellari come da lista.xml.
 * Specializzata per una visualizzazione popup.
 *
 * @author Nicola De Nisco
 */
abstract public class ListaBasePopup extends ListaBase
{
  @Override
  public boolean isPopup()
  {
    return true;
  }

  @Override
  public boolean isEditPopup()
  {
    return true;
  }

  @Override
  protected void makeContextHtml(HtmlWrapperBase lso, ListaInfo li,
     CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    AlternateColorTableAppBase act = (AlternateColorTableAppBase) (lso.getTbl());
    act.setAuthDelete(isAuthorizedDelete(data));
    act.setPopup(true);
    act.setEditPopup(true);
    act.setAuthSel(true);

    super.makeContextHtml(lso, li, data, context, baseUri);

    String scImposta = makeJavascriptImposta(data, lso, li);
    String scNuovo = makeJavascriptNuovo(lso, li);
    boolean haveEdit = lso.haveEditRiga();

    context.put("scImposta", "\n" + scImposta);
    context.put("scNuovo", "\n" + scNuovo);
    context.put("haveEdit", haveEdit);
  }

  public String makeJavascriptImposta(CoreRunData data, HtmlWrapperBase wl, ListaInfo li)
     throws Exception
  {
    if(li.func == null || li.func.trim().length() == 0)
      return "";

    if(wl.getFoInfo() == null)
      throwMessage(data.i18n(
         "E' stata definita una funzione 'imposta' ma manca la direttiva foreign-server nella lista '%s'.",
         li.type));

    String campi = "";
    Enumeration enumCol = wl.getFoInfo().getForeignColumnsKeys();
    while(enumCol.hasMoreElements())
    {
      String campo = (String) enumCol.nextElement();
      campi += "," + campo;
    }
    campi = campi.substring(1);

    if(data.isMsie())
    {
      return "\r\n"
         + "  function imposta(" + campi + ")\r\n"
         + "  {\r\n"
         + "	window.opener.parent." + li.func.trim() + "(" + campi + ");\r\n"
         + (li.chiudisel.equals("1") ? "	window.close();\r\n" : "")
         + "  }\r\n"
         + "\r\n";
    }
    else
    {
      return "\r\n"
         + "  function imposta(" + campi + ")\r\n"
         + "  {\r\n"
         + "	window.opener." + li.func.trim() + "(" + campi + ");\r\n"
         + (li.chiudisel.equals("1") ? "	window.close();\r\n" : "")
         + "  }\r\n"
         + "\r\n";
    }
  }

  public String makeJavascriptNuovo(HtmlWrapperBase wl, ListaInfo li)
     throws Exception
  {
    String urlNuovo = SU.okStr(wl.getEdInfo() == null ? null : wl.getEdInfo().getUrlEditRiga());
    if(!SU.isOkStr(urlNuovo))
      return "";

    if(li.passThroughParam != null && !li.passThroughParam.isEmpty())
      urlNuovo = HtmlUtils.mergeUrl(urlNuovo, li.passThroughParam);

    RigelCustomUrlBuilder urlBuilder = SetupHolder.getUrlBuilder();
    String url = urlBuilder.buildUrlNewRecord(true, urlNuovo, wl.getPtm(), null);

    if(HtmlUtils.isJavascriptBegin(url))
    {
      // ritorna direttamente la funzione javascript
      return url.substring(HtmlUtils.JAVASCRIPT_BEGIN.length());
    }

    return "  function relCommandNuovo()\r\n"
       + "  {\r\n"
       + "    apriFinestraEdit('" + url + "', 'det" + SU.purge(wl.getNome()) + "');\r\n"
       + "  }\r\n"
       + "";
  }
}
