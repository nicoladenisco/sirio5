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
package org.sirio2.rigel;

import java.net.URLEncoder;
import org.commonlib5.utils.StringOper;
import org.rigel5.RigelI18nInterface;
import org.rigel5.SetupHolder;
import org.rigel5.table.BuilderRicercaGenerica;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.AbstractHtmlTablePagerFilter;
import org.rigel5.table.html.HtmlMascheraRicercaGenerica;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.RigelHtmlPageComponent;
import org.sirio2.services.modellixml.MDL;

/**
 * Generatore di maschere per ricerca.
 * Versione specializzata per i tool rigel.
 *
 * @author Nicola De Nisco
 */
public class ToolRicercaListe extends HtmlMascheraRicercaGenerica
{
  protected String unique;

  public ToolRicercaListe(BuilderRicercaGenerica brg, RigelTableModel rtm, RigelI18nInterface i18n, String unique)
  {
    super(brg, rtm, i18n);
    this.unique = unique;
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
    RigelHtmlPageComponent javascript = new RigelHtmlPageComponent(PageComponentType.JAVASCRIPT, "search");

    html.append("<div class=\"rigel_simple_search\">\r\n")
       .append("<!-- BEGIN SIMPLE SEARCH -->\r\n")
       .append("&nbsp;<a href=\"#\" onclick=\"hideRic").append(unique).append("();\">")
       .append(MDL.getImgCollapse()).append("</a>&nbsp;&nbsp;&nbsp;\r\n");

    html.append("<!-- MORE SIMPLE SEARCH -->\r\n")
       .append("<input type=\"button\" name=\"SimpleSearch\" value=\"").append(i18n.getCaptionButtonCerca()).append("\" onclick=\"ricercasubmit_").append(unique).append("();\"/>\r\n")
       .append("<input type=\"button\" name=\"publisciSimpleSearch\" value=\"").append(i18n.getCaptionButtonPulisci()).append("\" onclick=\"pulisciRicerca").append(unique).append("();\"/>\r\n")
       .append("<!-- END FORM SIMPLE SEARCH -->\r\n")
       .append("</div>\r\n");

    html.append("<div id=\"rigel_search_param_" + formName + "\" class=\"rigel_search_param\">\r\n")
       .append("<input type=\"hidden\" name=\"filtro\" value=\"2\">");

    buildHtmlRicercaTable(html, javascript);
    html.append("</div>\r\n");

    page.add(html);
    page.add(javascript);
  }

  /**
   * Ritorna l'HTML completo della ricerca semplice.
   * @param nomeForm the value of nomeForm
   * @param sizeFld the value of sizeFld
   * @param page the value of page
   * @throws Exception
   */
  @Override
  public void buildHtmlRicercaSemplice(String nomeForm, int sizeFld, RigelHtmlPage page)
     throws Exception
  {
    this.formName = nomeForm;
    boolean valid = false;
    String firstControl = null;
    int simpleSearchColumn = 0;
    int simpleSearchWeight = 0;
    int numSiSeColumn = 0;
    String clearForm = "";

    RigelHtmlPageComponent html = new RigelHtmlPageComponent(PageComponentType.HTML, "simplesearch");
    RigelHtmlPageComponent javascript = new RigelHtmlPageComponent(PageComponentType.JAVASCRIPT, "simplesearch");
    html.append("<div class=\"rigel_simple_search\">\r\n")
       .append("<!-- BEGIN SIMPLE SEARCH -->\r\n")
       .append("&nbsp;<a href=\"#\" onclick=\"showRic").append(unique).append("();\">")
       .append(MDL.getImgExpand()).append("</a>&nbsp;&nbsp;&nbsp;\r\n");

    for(int i = 0; i < rtm.getColumnCount(); i++)
    {
      RigelColumnDescriptor cd = rtm.getColumn(i);

      if(!cd.isEscludiRicerca() && ((cd.getFiltroSort() % 1000) > simpleSearchWeight))
      {
        simpleSearchColumn = cd.getFiltroSort() > 1000 ? -(i + 1) : (i + 1);
        simpleSearchWeight = cd.getFiltroSort() % 1000;
      }

      if(cd.getRicercaSemplice() == 0)
        continue;

      if(numSiSeColumn >= SetupHolder.getMaxSiSeColumn())
        continue;

      String fieldName = getFieldName(cd);
      String caption = i18n.localizeTableCaption(null, rtm, cd, i, cd.getCaption());
      String defval = cd.getFiltroValore();
      int idx = cd.getFiltroTipo();
      if(idx == 0)
        defval = "";

      clearForm += "document." + formName + ".VL" + fieldName + ".value='';\r\n";

      if(cd.isComboRicerca())
      {
        html.append(caption).append("&nbsp;");
        html.append("<input type=\"hidden\" name=\"OP").append(fieldName).append("\" value=\"")
           .append(BuilderRicercaGenerica.IDX_CRITERIA_EQUAL).append("\">")
           .append(brg.getHtmlComboColonnaRicSemplice(formName, fieldName, cd, defval, i18n))
           .append("&nbsp;&nbsp;\r\n");
      }
      else
      {
        int opIdx = cd.getRicercaSemplice();

        html.append(caption).append("&nbsp;");

        if(cd.isDate())
        {
          if(opIdx == BuilderRicercaGenerica.IDX_CRITERIA_BETWEEN)
          {
            // l'uso di IDX_CRITERIA_BETWEEN su un campo date produce un combo box con gli intervalli più utilizzati
            html.append("<input type=\"hidden\" name=\"OP")
               .append(fieldName).append("\" value=\"").append(opIdx).append("\">");
            getComboBoxAnnoCompleto(html.getContent(), "VL" + fieldName, StringOper.parse(defval, 0));
          }
          else if(SetupHolder.getImgEditData() != null)
          {
            // campo per input data forzato a EQUAL
            html
               .append("<input type=\"hidden\" name=\"OP").append(fieldName).append("\" value=\"")
               .append(BuilderRicercaGenerica.IDX_CRITERIA_EQUAL)
               .append("\"><input type=\"text\" name=\"VL").append(fieldName).append("\" value=\"")
               .append(defval == null ? "" : defval).append("\" size=\"").append(sizeFld).append("\">");

            // aggiunge calendario per i campi data
            String sds = URLEncoder.encode("restartd_VL" + cd.getName(), "UTF-8");

            html.append("&nbsp;<a href=\"javascript:apriCalendarioForm('")
               .append(formName).append("','").append(sds).append("')\">")
               .append(SetupHolder.getImgEditData())
               .append("</a>");

            javascript.append(getScriptData(cd, fieldName));
          }
        }
        else
        {
          // solo i campi alfanumerici possono avere un filtro
          // di ricerca rapida diverso da uguale
          if(!cd.isAlpha())
            opIdx = BuilderRicercaGenerica.IDX_CRITERIA_EQUAL;

          // in tutti gli altri casi il campo per il valore di ricerca
          html
             .append("<input type=\"hidden\" name=\"OP").append(fieldName).append("\" value=\"").append(opIdx)
             .append("\"><input type=\"text\" name=\"VL").append(fieldName).append("\" value=\"")
             .append(defval == null ? "" : defval).append("\" size=\"").append(sizeFld).append("\">");
        }

        html.append("&nbsp;&nbsp;\r\n");
      }

      if(firstControl == null)
        firstControl = "VL" + fieldName;

      valid = true;
      numSiSeColumn++;
    }

    if(!valid)
      return;

    html.append("<input type=\"hidden\" name=\"filtro\" value=\"").append(AbstractHtmlTablePagerFilter.FILTRO_APPLICA).append("\"/>\r\n")
       .append("<input type=\"hidden\" name=\"SSORT\" value=\"").append(simpleSearchColumn).append("\"/>\r\n");

    javascript.append("document.").append(formName).append(".").append(firstControl).append(".focus();\r\n")
       .append("\r\n")
       .append("function SimpleSort_").append(formName).append("(idx)\r\n")
       .append("{\r\n")
       .append("   val=document.").append(formName).append(".SSORT.value;\r\n")
       .append("   if(idx == Math.abs(val))\r\n")
       .append("   {\r\n")
       .append("     document.").append(formName).append(".SSORT.value=-val;\r\n")
       .append("   }\r\n")
       .append("   else\r\n")
       .append("   {\r\n")
       .append("     document.").append(formName).append(".SSORT.value=idx;\r\n")
       .append("   }\r\n")
       .append("   document.").append(formName).append(".submit();\r\n")
       .append("}\r\n")
       .append("\r\n")
       .append("function pulisciRicercaSemplice_").append(formName).append("()\r\n")
       .append("{\r\n")
       .append("   ").append(clearForm)
       .append("   document.").append(formName).append(".SSORT.value=0;\r\n")
       .append("   pulisciRicerca").append(unique).append("();\r\n")
       .append("}\r\n")
       .append("");

    html
       .append("<!-- MORE SIMPLE SEARCH -->\r\n")
       .append("<input type=\"button\" name=\"SimpleSearch\" value=\"").append(i18n.getCaptionButtonCerca()).append("\" onclick=\"submit_").append(unique).append("();\"/>\r\n")
       .append("<input type=\"button\" name=\"publisciSimpleSearch\" value=\"").append(i18n.getCaptionButtonPulisci()).append("\" onclick=\"pulisciRicercaSemplice_").append(formName).append("();\"/>\r\n")
       .append("<!-- END FORM SIMPLE SEARCH -->\r\n")
       .append("</div>\r\n");

    page.add(html);
    page.add(javascript);
  }
}
