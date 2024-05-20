/*
 * Copyright (C) 2024 Nicola De Nisco
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sirio5.services.cache.FileCacheItem;
import org.sirio5.utils.SU;

/**
 * Servlet visualizzazione comunicazioni.
 *
 * @author Nicola De Nisco
 */
public class TeletypeServlet extends HttpServlet
{
  private int limitLines = 1000;
  private static final HashMap<String, File> localCache = new HashMap<>();

  @Override
  public void init(ServletConfig config)
     throws ServletException
  {
    super.init(config);
    limitLines = SU.parse(config.getInitParameter("limitLines"), limitLines);
  }

  public static String makeTicket()
  {
    // produce un ticket e verifica che non esista già nella cache
    String ticket = null;
    do
    {
      ticket = "TELOC" + System.currentTimeMillis();
    }
    while(localCache.containsKey(ticket));
    return ticket;
  }

  public static String addToCache(File toadd)
  {
    String ticket = makeTicket();
    localCache.put(ticket, toadd);
    return ticket;
  }

  public static File removeFromCache(String ticket)
  {
    return localCache.remove(ticket);
  }

  public static void clearCache()
  {
    localCache.clear();
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequestJson(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    if(request.getSession(false) == null)
      throw new ServletException("Sessione non valida.");

    // estrae nome della richiesta
    String sRequest = request.getPathInfo().substring(1);

    File monitorFile;
    if((monitorFile = localCache.get(sRequest)) == null)
    {
      FileCacheItem item = FileCacheItem.getFromCache(sRequest);

      if(item != null)
        monitorFile = item.getFile();
    }

    if(monitorFile == null)
      throw new ServletException("Richiesta non valida: " + sRequest + " inesistente.");

    // preleva numero di linea iniziale
    int startLine = SU.parse(request.getParameter("line"), -1);

    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Expires", "-1");
    response.setHeader("Cache-Control", "no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");

    String s;
    int countLine = 0, countLocal = 0;
    try(BufferedReader br = new BufferedReader(
       new InputStreamReader(new FileInputStream(monitorFile), "UTF-8"), 1024))
    {
      JSONObject json = new JSONObject();
      JSONArray jarr = new JSONArray();
      json.put("rows", jarr);
      json.put("completed", true);
      json.put("limit", limitLines);

      while((s = br.readLine()) != null)
      {
        if(countLine > startLine)
        {
          JSONObject jrow = new JSONObject();
          jarr.put(jrow);

          jrow.put("num", countLine);
          jrow.put("str", s);
          countLocal++;

          if(countLocal > limitLines)
          {
            json.put("completed", false);
            break;
          }
        }

        countLine++;
      }

      try(PrintWriter out = response.getWriter())
      {
        out.write(json.toString());
      }

      // rallenta volutamente il thread per non assorbire molte risorse
      if(jarr.length() == 0)
        Thread.sleep(2000);
    }
    catch(Exception ex)
    {
      throw new ServletException(ex);
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
    processRequestJson(request, response);
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
    processRequestJson(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Visualizzatore log di comunicazione.";
  }// </editor-fold>
}
