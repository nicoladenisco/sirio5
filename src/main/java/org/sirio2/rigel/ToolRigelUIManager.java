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

import javax.servlet.http.HttpSession;
import org.rigel2.table.html.AbstractHtmlTablePager;
import org.rigel2.table.html.RigelHtmlPage;
import org.rigel2.table.html.RigelHtmlPageComponent;

/**
 * Gestore dell'interfaccia rigel per i Tool.
 *
 * @author Nicola De Nisco
 */
public class ToolRigelUIManager extends CoreRigelUIManager
{
  private RigelHtmlPage lastPageLista = null, lastPageForm = null;

  @Override
  protected String getJumpUrl(AbstractHtmlTablePager tp, HttpSession sessione, int rec)
     throws Exception
  {
    String uri = tp.getSelfUrl(rec, sessione);
    String formName = tp.getFormName();
    return "javascript:jump" + formName + "('" + uri + "')";
  }

  @Override
  protected void generateFuncGoto(RigelHtmlPageComponent javascript,
     String funcGoto, AbstractHtmlTablePager tp, int numPagine, int limit, String tmp)
  {
    tmp = tmp.replace("javascript:", "");

    javascript.append(""
       + "function " + funcGoto + "()\n"
       + "{\n"
       + "  var nPage = document." + tp.getFormName() + ".in_" + funcGoto + ".value;\n"
       + "  if(nPage <= 0 || nPage > " + numPagine + ") {\n"
       + "    alert('Valore di pagina non consentito.');\n"
       + "  } else {\n"
       + "    rStart = (nPage-1)*" + limit + ";\n"
       + "    " + tmp + "\n"
       + "  }\n"
       + "}\n"
       + "\n"
    );
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
}
