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

import javax.servlet.http.HttpSession;
import org.commonlib5.utils.StringOper;
import org.rigel5.DefaultUIManager;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.html.AbstractHtmlTablePager;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.RigelHtmlPageComponent;
import org.sirio5.services.modellixml.MDL;

/**
 * Gestore dell'interfaccia rigel per i Tool.
 *
 * @author Nicola De Nisco
 */
public class ToolRigelUIManager2 extends DefaultUIManager
{
  private String unique;
  private RigelHtmlPage lastPageLista = null, lastPageForm = null;

  /**
   * Restituisce la barra inferiore di navigazione.
   * Nella barra inferiore viene indicato sulla sinistra il navigatore
   * per numeri di pagina, al centro l'indicazione con la pagina corrente
   * e le pagine totali, sulla sinistra un navigatore del tipo precedente
   * - successivo.
   * @param pagCurr the value of pagCurr
   * @param numPagine the value of numPagine
   * @param limit the value of limit
   * @param tp the value of tp
   * @param sessione the value of sessione
   * @param page the value of page
   * @throws java.lang.Exception
   */
  @Override
  public void addHtmlNavRecord(int pagCurr, int numPagine, int limit,
     AbstractHtmlTablePager tp, HttpSession sessione, RigelHtmlPage page)
     throws Exception
  {
    String sLeft, sCenter, sRight;
    RigelI18nInterface i18n = tp.getI18n();
    RigelHtmlPageComponent html = new RigelHtmlPageComponent(PageComponentType.HTML, "nav");

    String uri = tp.getBaseSelfUrl();
    String funcGoto = "rigel.gotoForTool('" + uri + "'," + limit + "," + numPagine + ", '" + unique + "')";
    String funcTest = "rigel.testInvioTool('" + uri + "'," + limit + "," + numPagine + ", '" + unique + "', event)";
    String funTestEvent = "onkeypress=\"return " + funcTest + "\"";

    String[] aimg = MDL.getImgsNav();
    String imgFirst = aimg[0];
    String imgLast = aimg[1];
    String imgPrev = aimg[2];
    String imgNext = aimg[3];

    if(pagCurr > 0)
    {
      sLeft
         = "<a href=\"" + getJumpUrl(tp, sessione, 0) + "\">" + imgFirst + "</a>"
         + "&nbsp;&nbsp;&nbsp;"
         + "<a href=\"" + getJumpUrl(tp, sessione, (pagCurr - 1) * limit) + "\">" + imgPrev + "</a>";
    }
    else
    {
      sLeft = imgFirst + "&nbsp;&nbsp;&nbsp;" + imgPrev;
    }

    sCenter
       = "Pag. <input class=\"little\" type=\"text\""
       + " value=\"" + (pagCurr + 1) + "\""
       + " name=\"in_" + unique + "\""
       + " id=\"id_in_" + unique + "\""
       + " size='5' " + funTestEvent + ">"
       + " di " + numPagine + " <input class=\"little\" type=\"button\""
       + " value=\"Go\" onClick=\"" + funcGoto + "();\">";

    if(pagCurr < (numPagine - 1))
    {
      sRight
         = "<a href=\"" + getJumpUrl(tp, sessione, (pagCurr + 1) * limit) + "\">" + imgNext + "</a>"
         + "&nbsp;&nbsp;&nbsp;"
         + "<a href=\"" + getJumpUrl(tp, sessione, (numPagine - 1) * limit) + "\">" + imgLast + "</a>";
    }
    else
    {
      sRight = imgNext + "&nbsp;&nbsp;&nbsp;" + imgLast;
    }

    String tmp = getJumpUrl(tp, sessione, 9999);
    tmp = StringOper.strReplace(tmp, "9999", "'+rStart+'");

    html.append("<div class=\"rigel_navbar\">"
       + "<table width=100% border=0 cellspacing=0 cellpadding=1><TR>\r\n"
       + "<TD width=33% class=\"rigel_navbar_left\" align=left>" + sLeft + "</td>\r\n"
       + "<TD width=33% class=\"rigel_navbar_center\" align=center>" + sCenter + "</td>\r\n"
       + "<TD width=33% class=\"rigel_navbar_right\" align=right>" + sRight + "</td>\r\n"
       + "</tr></table>\r\n"
       + "</div>"
    );

    page.add(html);
  }

  protected String getJumpUrl(AbstractHtmlTablePager tp, HttpSession sessione, int rec)
     throws Exception
  {
    String uri = tp.getSelfUrl(rec, sessione);
    return "javascript:rigel.jumpTool('" + unique + "', '" + uri + "')";
  }

  /**
   * Formattazione nulla per Tool.
   * Il tool renderizza le componenti direttamente nella vm.
   * @param filtro
   * @param page
   * @return sempre stringa vuota
   * @throws Exception
   */
  @Override
  public String formatHtmlLista(int filtro, RigelHtmlPage page)
     throws Exception
  {
    lastPageLista = page;
    return "";
  }

  /**
   * Formattazione nulla per Tool.
   * Il tool renderizza le componenti direttamente nella vm.
   * @param page
   * @return sempre stringa vuota
   * @throws Exception
   */
  @Override
  public String formatHtmlForm(RigelHtmlPage page)
     throws Exception
  {
    lastPageForm = page;
    return "";
  }

  public RigelHtmlPage getLastPageLista()
  {
    return lastPageLista;
  }

  public RigelHtmlPage getLastPageForm()
  {
    return lastPageForm;
  }

  public String getUnique()
  {
    return unique;
  }

  public void setUnique(String unique)
  {
    this.unique = unique;
  }
}
