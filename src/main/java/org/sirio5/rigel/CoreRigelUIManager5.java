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
import org.rigel5.DefaultUIManager;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.html.AbstractHtmlTablePager;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.RigelHtmlPageComponent;
import org.sirio5.services.modellixml.MDL;

/**
 * Gestore dell'interfaccia rigel.
 * Nuova versione senza javascript; usa il nuovo rigel.js unificato.
 *
 * @author Nicola De Nisco
 */
public class CoreRigelUIManager5 extends DefaultUIManager
{
  /**
   * Restituisce la barra inferiore di navigazione.
   * Nella barra inferiore viene indicato sulla sinistra il navigatore
   * per numeri di pagina, al centro l'indicazione con la pagina corrente
   * e le pagine totali, sulla sinistra un navigatore del tipo precedente
   * - successivo.
   * @param pagCurr the value of pagCurr
   * @param numPagine the value of numPagine
   * @param numPerPagina the value of numPerPagina
   * @param tp the value of tp
   * @param sessione the value of sessione
   * @param page the value of page
   * @throws java.lang.Exception
   */
  @Override
  public void addHtmlNavRecord(int pagCurr, int numPagine, int numPerPagina,
     AbstractHtmlTablePager tp, HttpSession sessione, RigelHtmlPage page)
     throws Exception
  {
    String sLeft, sCenter, sRight;
    RigelI18nInterface i18n = tp.getI18n();
    RigelHtmlPageComponent html = new RigelHtmlPageComponent(PageComponentType.HTML, "nav");
    String formName = tp.getFormName();
    String baseUri = tp.getBaseSelfUrl();

    //goto(baseUri, numPerPage, numPagine, formname)
    String funcGoto = "rigel.gotoNav("
       + "'" + baseUri + "', " + numPerPagina + ", " + numPagine + ", '" + formName
       + "');";

    //testInvio(baseUri, numPerPage, numPagine, formname, e)
    String funTestEvent = "onkeypress=\"return rigel.testInvioNav("
       + "'" + baseUri + "', " + numPerPagina + ", " + numPagine + ", '" + formName
       + "', event);\"";

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
         + "<a href=\"" + getJumpUrl(tp, sessione, (pagCurr - 1) * numPerPagina) + "\">" + imgPrev + "</a>";
    }
    else
    {
      sLeft = imgFirst + "&nbsp;&nbsp;&nbsp;" + imgPrev;
    }

    sCenter
       = i18n.msg("Pagina")
       + " <input class=\"little\" type=\"text\""
       + " value=\"" + (pagCurr + 1) + "\""
       + " name=\"in_" + formName + "\""
       + " id=\"id_in_" + formName + "\""
       + " size='5' " + funTestEvent + "> "
       + i18n.msg("di")
       + " " + numPagine + " <input class=\"little\" type=\"button\""
       + " value=\"Go\" onClick=\"" + funcGoto + "\">";

    if(pagCurr < (numPagine - 1))
    {
      sRight
         = "<a href=\"" + getJumpUrl(tp, sessione, (pagCurr + 1) * numPerPagina) + "\">" + imgNext + "</a>"
         + "&nbsp;&nbsp;&nbsp;"
         + "<a href=\"" + getJumpUrl(tp, sessione, (numPagine - 1) * numPerPagina) + "\">" + imgLast + "</a>";
    }
    else
    {
      sRight = imgNext + "&nbsp;&nbsp;&nbsp;" + imgLast;
    }

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

  /**
   * Costruisce url di salto.
   * Ridefinibile in classi derivate.
   * @param tp pager di riferimento
   * @param sessione sessione corrente
   * @param rec record di salto
   * @return url di salto
   * @throws Exception
   */
  protected String getJumpUrl(AbstractHtmlTablePager tp, HttpSession sessione, int rec)
     throws Exception
  {
    return tp.getSelfUrl(rec, sessione);
  }
}
