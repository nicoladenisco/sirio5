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

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sirio5.CoreConst;
import org.sirio5.beans.menu.LogsMenuGenerator;
import org.sirio5.beans.menu.MenuItemBean;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.FU;

/**
 * Servlet per la visualizzazione dei logs.
 *
 * @author Nicola De Nisco
 */
public class LogsViewerServlet extends HttpServlet
{
  private final static Log log = LogFactory.getLog(LogsViewerServlet.class);

  private String forcePath;

  @Override
  public void init(ServletConfig config)
     throws ServletException
  {
    super.init(config);

    forcePath = config.getInitParameter("forcePath");
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    try
    {
      List<MenuItemBean> arBeans = (List<MenuItemBean>) request.getSession()
         .getAttribute(LogsMenuGenerator.MENU_LOGS_SESSION_KEY);

      String sInfo = request.getPathInfo();
      if(sInfo == null || sInfo.length() < 5)
        throw new Exception(INT.I("Richiesta non valida."));

      String ssInfo = sInfo.substring(1);
      MenuItemBean b = arBeans.stream()
         .filter((s) -> ssInfo.equalsIgnoreCase(s.getDescrizione()))
         .findFirst()
         .orElse(null);

      if(b == null)
        throw new Exception(INT.I("Richiesta non valida: log '%s' inesistente.", ssInfo));

      File logFile = new File(b.getNote());
      if(forcePath != null)
        logFile = new File(forcePath, sInfo.substring(1));

      FU.sendFileResponse(request, response, logFile, CoreConst.MIME_TXT + ";charset=utf-8", ssInfo, false);
    }
    catch(Exception ex)
    {
      log.error(ex.getMessage(), ex);
      throw new ServletException(ex.getMessage(), ex);
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Visualizzatore log applicazione";
  }// </editor-fold>

}
