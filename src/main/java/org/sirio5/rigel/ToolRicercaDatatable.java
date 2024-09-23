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

import org.rigel5.RigelI18nInterface;
import org.rigel5.table.BuilderRicercaGenerica;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.HtmlMascheraRicercaGenericaNoscript;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.RigelHtmlPageComponent;
import org.sirio5.services.modellixml.MDL;

/**
 * Generatore di maschere per ricerca.
 * Versione specializzata per i tool rigel.
 *
 * @author Nicola De Nisco
 */
public class ToolRicercaDatatable extends HtmlMascheraRicercaGenericaNoscript
{
  protected String unique, url;

  public ToolRicercaDatatable(BuilderRicercaGenerica brg, RigelTableModel rtm,
     RigelI18nInterface i18n, String unique, String url)
  {
    this.unique = unique;
    this.url = url;
    init(brg, rtm, i18n);
  }

  public String getUnique()
  {
    return unique;
  }

  public void setUnique(String unique)
  {
    this.unique = unique;
  }

  /**
   * Ritorna l'HTML completo della maschera per l'impostazione
   * dei parametri di filtro e di ordinamento.
   * @param nomeForm the value of nomeForm
   * @param page the value of page
   * @throws Exception
   */
  @Override
  public void buildHtmlRicerca(String nomeForm, RigelHtmlPage page)
     throws Exception
  {
    this.formName = nomeForm;
    RigelHtmlPageComponent html = new RigelHtmlPageComponent(PageComponentType.HTML, "search");

    html.append("<div class=\"rigel_simple_search\">\r\n")
       .append("<!-- BEGIN SIMPLE SEARCH -->\r\n")
       .append("&nbsp;<a href=\"#\" onclick=\"rigel.hideRicTool('").append(unique).append("');\">")
       .append(MDL.getImgCollapse()).append("</a>&nbsp;&nbsp;&nbsp;\r\n");

    html.append("<!-- MORE SIMPLE SEARCH -->\r\n")
       .append("<input type=\"button\" name=\"SimpleSearch\" value=\"")
       .append(i18n.getCaptionButtonCerca()).append("\" onclick=\"rigel.ricercaTool('")
       .append(unique).append("', '").append(url).append("');\"/>\r\n")
       .append("<input type=\"button\" name=\"publisciSimpleSearch\" value=\"")
       .append(i18n.getCaptionButtonPulisci()).append("\" onclick=\"rigel.pulisciRicercaTool('")
       .append(unique).append("', '").append(url).append("');\"/>\r\n")
       .append("<!-- END FORM SIMPLE SEARCH -->\r\n")
       .append("</div>\r\n");

    html.append("<div id=\"rigel_search_param_" + formName + "\" class=\"rigel_search_param\">\r\n")
       .append("<input type=\"hidden\" name=\"filtro\" value=\"2\">");

    buildHtmlRicercaTable(html);
    html.append("</div>\r\n");

    page.add(html);
  }

  /**
   * Ritorna l'HTML completo della ricerca semplice.
   * @param nomeForm the value of nomeForm
   * @param sizeFld the value of sizeFld
   * @param haveFilter vero se il filtro è attivo
   * @param page the value of page
   * @throws Exception
   */
  @Override
  public void buildHtmlRicercaSemplice(String nomeForm, int sizeFld, boolean haveFilter, RigelHtmlPage page)
     throws Exception
  {
  }
}
