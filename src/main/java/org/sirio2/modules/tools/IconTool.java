/*
 * Copyright (C) 2021 Nicola De Nisco
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
package org.sirio2.modules.tools;

import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.ApplicationTool;
import org.rigel2.RigelI18nInterface;
import org.sirio2.rigel.RigelHtmlI18n;
import org.sirio2.services.modellixml.modelliXML;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.LI;

/**
 * Tool per semplificare l'uso delle icone.
 *
 * @author Nicola De Nisco
 */
public class IconTool implements ApplicationTool
{
  private modelliXML __mdl;
  private RigelI18nInterface i18n;

  @Override
  public void init(Object data)
  {
    if(i18n == null && data != null && data instanceof CoreRunData)
      i18n = new RigelHtmlI18n((CoreRunData) data);
  }

  @Override
  public void refresh()
  {
  }

  private modelliXML getService()
  {
    if(__mdl == null)
      __mdl = (modelliXML) TurbineServices.getInstance()
         .getService(modelliXML.SERVICE_NAME);
    return __mdl;
  }

  public String getImgIcon(String icon, String title)
  {
    return LI.getImgIcon(icon, i18(title));
  }

  public String getImgGlyphicon(String name, String title)
  {
    return LI.getImgGlyphicon(name, i18(title));
  }

  public String getImgAwesome(String name, String title)
  {
    return LI.getImgAwesome(name, i18(title));
  }

  public String getImgAwesomeFas(String name, String title)
  {
    return LI.getImgAwesomeFas(name, i18(title));
  }

  public String getImgAwesomeFar(String name, String title)
  {
    return LI.getImgAwesomeFar(name, i18(title));
  }

  public String getImgAwesomeFab(String name, String title)
  {
    return LI.getImgAwesomeFab(name, i18(title));
  }

  public String getImgAwesomeSpin(String name, String title)
  {
    return LI.getImgAwesomeSpin(name, i18(title));
  }

  public String getImgAwesomeFasSpin(String name, String title)
  {
    return LI.getImgAwesomeFasSpin(name, i18(title));
  }

  public String getImgAwesomeFarSpin(String name, String title)
  {
    return LI.getImgAwesomeFarSpin(name, i18(title));
  }

  public String getImgAwesomeFabSpin(String name, String title)
  {
    return LI.getImgAwesomeFabSpin(name, i18(title));
  }

  public String getImgSelect()
     throws Exception
  {
    return getService().getImgSelect();
  }

  public String getImgEditData()
     throws Exception
  {
    return getService().getImgEditData();
  }

  public String getImgEditForeign()
     throws Exception
  {
    return getService().getImgEditForeign();
  }

  public String getImgFormForeign()
     throws Exception
  {
    return getService().getImgFormForeign();
  }

  public String getImgLista()
     throws Exception
  {
    return getService().getImgLista();
  }

  public String getImgEditItem()
     throws Exception
  {
    return getService().getImgEditItem();
  }

  public String getImgEditRecord()
     throws Exception
  {
    return getService().getImgEditRecord();
  }

  public String getImgCancellaRecord()
     throws Exception
  {
    return getService().getImgCancellaRecord();
  }

  public String getImgExpand()
     throws Exception
  {
    return getService().getImgExpand();
  }

  public String getImgCollapse()
     throws Exception
  {
    return getService().getImgCollapse();
  }

  /**
   * Ritorna il tag HTML per l'immagine predefinita per la selezione.
   *
   * @param stato
   * @return tag HTML completo
   * @throws java.lang.Exception
   */
  public String getImgSmiley(int stato)
     throws Exception
  {
    return getService().getImgSmiley(stato);
  }

  public String getImgHtml(String imgName, String tip)
     throws Exception
  {
    tip = i18(tip);

    return "<img src=\"" + getImageUrl(imgName)
       + " \" alt=\"" + tip + "\" tip=\"" + tip + "\" title=\"" + tip + "\" border=\"0\">";
  }

  /**
   * Ritorna l'url completa dell'immagine (http://.../images/nomeima),
   * prelevandola dalla directory images dell'applicazione web.
   * Viene applicato lo skin corrente.
   * @param nomeima nome dell'immagine
   * @return la URL relativa completa
   */
  public String getImageUrl(String nomeima)
  {
    return LI.getImageUrl(nomeima);
  }

  private String i18(String title)
  {
    return i18n == null ? title : i18n.msg(title);
  }
}
