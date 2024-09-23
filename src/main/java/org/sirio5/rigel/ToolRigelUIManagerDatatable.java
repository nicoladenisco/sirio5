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
import org.rigel5.RigelUIManager;
import org.rigel5.table.html.AbstractHtmlTablePager;
import org.rigel5.table.html.RigelHtmlPage;

/**
 * Gestore dell'interfaccia rigel per i Tool.
 *
 * @author Nicola De Nisco
 */
public class ToolRigelUIManagerDatatable implements RigelUIManager
{
  public int pagCurr, numPagine, limit;

  @Override
  public String formatHtmlLista(int filtro, RigelHtmlPage page)
     throws Exception
  {
    return "";
  }

  @Override
  public String formatHtmlForm(RigelHtmlPage page)
     throws Exception
  {
    return "";
  }

  @Override
  public String formatSimpleSearch(int filtro, RigelHtmlPage page)
     throws Exception
  {
    return "";
  }

  @Override
  public String formatSimpleSearchPalmare(int filtro, RigelHtmlPage page)
     throws Exception
  {
    return "";
  }

  @Override
  public void addHtmlNavRecord(int pagCurr, int numPagine, int limit,
     AbstractHtmlTablePager tp, HttpSession sessione, RigelHtmlPage page)
     throws Exception
  {
    this.pagCurr = pagCurr;
    this.numPagine = numPagine;
    this.limit = limit;
  }
}
