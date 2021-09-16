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
import org.rigel5.table.html.AbstractHtmlTablePager;
import org.rigel5.table.html.PageComponentType;
import org.rigel5.table.html.RigelHtmlPage;
import org.rigel5.table.html.RigelHtmlPageComponent;
import org.sirio5.utils.LI;

/**
 * Gestore dell'interfaccia rigel.
 *
 * @author Nicola De Nisco
 */
public class CoreRigelUIManager extends DefaultUIManager
{
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
    RigelHtmlPageComponent html = new RigelHtmlPageComponent(PageComponentType.HTML, "nav");
    RigelHtmlPageComponent javascript = new RigelHtmlPageComponent(PageComponentType.JAVASCRIPT, "nav");

    String funcGoto = "gotoPage_" + tp.getFormName();
    String funcTest = "testInvioGoto_" + tp.getFormName();
    String funTestEvent = "onkeypress='return " + funcTest + "(event);'";

    String[] aimg = getArrayImmagini();
    String imgFirst = aimg[0];
    String imgLast = aimg[1];
    String imgPrev = aimg[2];
    String imgNext = aimg[3];

    if(pagCurr > 0)
    {
      sLeft = "<a href=\"" + getJumpUrl(tp, sessione, 0) + "\">"
         + "<img src=\"" + imgFirst + "\" border=0>"
         + "</a><a href=\"" + getJumpUrl(tp, sessione, (pagCurr - 1) * limit) + "\">"
         + "<img src=\"" + imgPrev + "\" border=0>"
         + "</a>";
    }
    else
    {
      sLeft = "<img src=\"" + imgFirst + "\" border=0>"
         + "<img src=\"" + imgPrev + "\" border=0>";
    }

    sCenter
       = "Pag. <input class=\"little\" type=\"text\""
       + " value=\"" + (pagCurr + 1) + "\""
       + " name=\"in_" + funcGoto + "\""
       + " size='5' " + funTestEvent + ">"
       + " di " + numPagine + " <input class=\"little\" type=\"button\""
       + " value=\"Go\" onClick=\"" + funcGoto + "();\">";

    if(pagCurr < (numPagine - 1))
    {
      sRight = "<a href=\"" + getJumpUrl(tp, sessione, (pagCurr + 1) * limit) + "\">"
         + "<img src=\"" + imgNext + "\" border=0>"
         + "</a><a href=\"" + getJumpUrl(tp, sessione, (numPagine - 1) * limit) + "\">"
         + "<img src=\"" + imgLast + "\" border=0>"
         + "</a>";
    }
    else
    {
      sRight = "<img src=\"" + imgNext + "\" border=0>"
         + "<img src=\"" + imgLast + "\" border=0>";
    }

    String tmp = getJumpUrl(tp, sessione, 9999);
    tmp = StringOper.strReplace(tmp, "9999", "'+rStart+'");

    generateFuncGoto(javascript, funcGoto, tp, numPagine, limit, tmp);

    javascript.append(""
       + "function " + funcTest + "(e)\n"
       + "{\n"
       + "  if(e == null) e=event;\n"
       + "  if(e.keyCode == 13){\n"
       + "  " + funcGoto + "();\n"
       + "   return false;\n"
       + "  }\n"
       + "  return true;\n"
       + "}\n"
    );

    html.append("<div class=\"rigel_navbar\">"
       + "<table width=100% border=0 cellspacing=0 cellpadding=1><TR>\r\n"
       + "<TD width=33% class=\"little-black\" align=left>" + sLeft + "</td>\r\n"
       + "<TD width=33% class=\"little-black\" align=center>" + sCenter + "</td>\r\n"
       + "<TD width=33% class=\"little-black\" align=right>" + sRight + "</td>\r\n"
       + "</tr></table>\r\n"
       + "</div>"
    );

    page.add(html);
    page.add(javascript);
  }

  protected void generateFuncGoto(RigelHtmlPageComponent javascript,
     String funcGoto, AbstractHtmlTablePager tp, int numPagine, int limit, String tmp)
  {
    javascript.append(""
       + "function " + funcGoto + "()\n"
       + "{\n"
       + "  var nPage = document." + tp.getFormName() + ".in_" + funcGoto + ".value;\n"
       + "  if(nPage <= 0 || nPage > " + numPagine + ") {\n"
       + "    alert('Valore di pagina non consentito.');\n"
       + "  } else {\n"
       + "    rStart = (nPage-1)*" + limit + ";\n"
       + "    window.location.href = '" + tmp + "'\n"
       + "  }\n"
       + "}\n"
       + "\n"
    );
  }

  /**
   * Costruzione delle url per le quattro immagini di navigazione.
   * @return array di 4 url: primo, ultimo, precedente, successivo
   */
  protected String[] getArrayImmagini()
  {
    String[] rv = new String[4];
    rv[0] = LI.getImageUrl("pagination_first.gif");
    rv[1] = LI.getImageUrl("pagination_last.gif");
    rv[2] = LI.getImageUrl("pagination_prev.gif");
    rv[3] = LI.getImageUrl("pagination_next.gif");
    return rv;
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
