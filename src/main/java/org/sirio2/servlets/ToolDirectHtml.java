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
package org.sirio2.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.services.velocity.VelocityService;
import org.commonlib.utils.SimpleTimer;
import org.sirio2.rigel.ToolRenderFormRigel;
import org.sirio2.rigel.ToolRenderListeRigel;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Servlet per la restituzione dell'html per i tool.
 *
 * @author Nicola De Nisco
 */
public class ToolDirectHtml extends HttpServlet
{
  /** Logging */
  private static final Log log = LogFactory.getLog(ToolDirectHtml.class);
  private final ToolRenderListeRigel renderListe = new ToolRenderListeRigel();
  private final ToolRenderFormRigel renderForm = new ToolRenderFormRigel();
  private RunDataService rundataService = null;
  private VelocityService velocityService = null;

  @Override
  public void init()
     throws ServletException
  {
    super.init();

    if((rundataService = (RunDataService) TurbineServices.getInstance()
       .getService(RunDataService.SERVICE_NAME)) == null)
      throw new ServletException("No RunData Service configured!");

    if((velocityService = (VelocityService) TurbineServices.getInstance()
       .getService(VelocityService.SERVICE_NAME)) == null)
      throw new ServletException("No Velocity Service configured!");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    processRequest(request, response);
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
    // Placeholder for the RunData object.
    CoreRunData data = null;

    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    SimpleTimer st = new SimpleTimer();
    try
    {
      // estrae nome della richiesta
      String sRequest = request.getPathInfo().substring(1);

      // Get general RunData here...
      // Perform turbine specific initialization below.
      data = (CoreRunData) rundataService.getRunData(request, response, getServletConfig());
      ParameterParser pp = data.getParameterParser();
      pp.setRequest(request);

      // Pull user from session.
      data.populate();

      int pos = sRequest.indexOf('/');
      if(pos != -1)
      {
        String params = sRequest.substring(pos);
        if(!params.isEmpty())
        {
          for(StringTokenizer stk = new StringTokenizer(params, "/"); stk.hasMoreTokens();)
          {
            String key = stk.nextToken();
            if(stk.hasMoreTokens())
            {
              String value = stk.nextToken();
              pp.add(key, value);
            }
          }
        }

        sRequest = sRequest.substring(0, pos);
      }

      switch(SU.okStr(sRequest))
      {
        case "lista":
          runLista(data, out);
          break;
        case "form":
          runForm(data, out);
          break;
        default:
          throw new ServletException(data.i18n("Richiesta non elaborabile: %s", sRequest));
      }

      out.flush();
      st.waitElapsed(300);
    }
    catch(Exception ex)
    {
      log.error("Rigel direct rendering error", ex); // NOI18N
      throw new ServletException(ex);
    }
    finally
    {
      // Return the used RunData to the factory for recycling.
      rundataService.putRunData(data);

      out.close();
    }
  }

  private void runLista(CoreRunData data, PrintWriter out)
     throws Exception
  {
    String html = renderListe.renderHtml(data);
    out.print(html);
  }

  private void runForm(CoreRunData data, PrintWriter out)
     throws Exception
  {
    String html = renderForm.renderHtml(data);
    out.print(html);
  }
}
