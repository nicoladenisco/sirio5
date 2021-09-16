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
package org.sirio5.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.uri.TemplateURI;
import org.sirio5.utils.TR;

/**
 * Logout dall'applicazione.
 * La sessione viene invalidata in modo da annullare tutti
 * i dati transienti legati all'utente.
 *
 * @author Nicola De Nisco
 * @version 1.0
 */
public class logout extends HttpServlet
{
  /**
   * Invalida la sessione e redirige alla servlet di logon
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    request.getSession().invalidate();

    String home = TR.getString("template.homepage");
    TemplateURI tu = new TemplateURI(Turbine.getDefaultServerData(), home);
    String url = tu.getRelativeLink();
    response.sendRedirect(url);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    doGet(request, response);
  }
}
