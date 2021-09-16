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

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.TR;

/**
 * Accesso XML-RPC diretto da Tomcat.
 * Consente di utilizzare direttamente Tomcat
 * come server XML-RPC dell'applicazione, oltre al tradizionale
 * server dedicato.
 *
 * @author Nicola De Nisco
 */
public class EmbeddedXmlRpc extends HttpServlet
{
  /** Logging */
  private static final Log log = LogFactory.getLog(EmbeddedXmlRpc.class);
  /** Gestore connessioni xmlrpc */
  protected XmlRpcServletServer server = new XmlRpcServletServer();

  @Override
  public void init()
     throws ServletException
  {
    super.init();

    PropertyHandlerMapping phm = new PropertyHandlerMapping();
    Configuration conf = TR.getConfiguration("services.XmlRpcService.embedded");

    // Check if there are any handlers to register at startup
    for(Iterator keys = conf.getKeys("handler"); keys.hasNext();)
    {
      String handler = (String) keys.next();
      String handlerName = handler.substring(handler.indexOf('.') + 1);
      String handlerClass = conf.getString(handler);

      log.debug(INT.I("Found Handler %s as %s / %s", handler, handlerName, handlerClass));

      registerHandler(phm, handlerName, handlerClass);
    }

    server.setHandlerMapping(phm);

    // some boilerplate stuff
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
    serverConfig.setEnabledForExtensions(true);
    serverConfig.setContentLengthOptional(false);
  }

  /**
   * A helper method that tries to initialize a handler and register it.
   * The purpose is to check for all the exceptions that may occur in
   * dynamic class loading and throw an InitializationException on
   * error.
   *
   * @param phm
   * @param handlerName The name the handler is registered under.
   * @param handlerClass The name of the class to use as a handler.
   * @throws javax.servlet.ServletException
   */
  public void registerHandler(PropertyHandlerMapping phm, String handlerName, String handlerClass)
     throws ServletException
  {
    try
    {
      Class clHandler = Class.forName(handlerClass);
      phm.addHandler(handlerName, clHandler);
    }
    catch(ClassNotFoundException ex)
    {
      log.error(INT.I("Class %s not found: the handler %s is not available.", handlerClass, handlerName), ex);
    }
    catch(ThreadDeath | OutOfMemoryError t)
    {
      throw t;
    }
    catch(Throwable t)
    {
      String msg = INT.I("Failed to instantiate %s", handlerClass);
      log.error(msg, t);
      throw new ServletException(msg, t);
    }
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
    server.execute(request, response);
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
    return "Short description";
  }// </editor-fold>

}
