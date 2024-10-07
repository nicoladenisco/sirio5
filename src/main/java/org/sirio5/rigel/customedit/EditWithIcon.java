/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.rigel.customedit;

import javax.swing.table.TableModel;
import org.jdom2.Element;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.CustomColumnEdit;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.sirio5.utils.LI;
import org.sirio5.utils.SU;

/**
 * Controllo di edit con una icona alla fine.
 *
 * @author Nicola De Nisco
 */
public class EditWithIcon implements CustomColumnEdit
{
  private String icon, text, link, popup;

  @Override
  public void init(Element eleXML)
     throws Exception
  {
    popup = SU.okStr(eleXML.getAttributeValue("popup"), "0");

    icon = SU.okStr(eleXML.getChildText("icon"), "fas:question");
    text = SU.okStr(eleXML.getChildText("text"), "?");
    link = SU.okStr(eleXML.getChildText("link"), "");
  }

  @Override
  public boolean haveAddHtml()
  {
    return true;
  }

  @Override
  public String addHtmlEdit(RigelColumnDescriptor cd, TableModel model,
     int row, int col, String formattedValue, String nomeCampo, String rigelHtml, RigelI18nInterface i18n)
     throws Exception
  {
    if(link.isEmpty())
      return rigelHtml + "&nbsp; " + LI.getImgIcon(icon, i18n.msg(text));

    String valore = ((RigelTableModel) model).getValueMacroInside(row, col, link, true, true);
    return generaLink(rigelHtml, valore, i18n);
  }

  public String generaLink(String rigelHtml, String valore, RigelI18nInterface i18n)
  {
    String linkUrl = LI.getLinkUrl(valore);
    String iconHtml = LI.getImgIcon(icon, i18n.msg(text));

    switch(popup)
    {
      case "0":
        return rigelHtml + "&nbsp; <a href='"
           + linkUrl + "'>"
           + iconHtml
           + "</a>";

      default:
        return rigelHtml + "&nbsp; <a href='#' onclick=\""
           + "apriPopup" + popup + "('"
           + linkUrl + "', 'pupup_edit_button')\">"
           + iconHtml
           + "</a>";
    }
  }
}
