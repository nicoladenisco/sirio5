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
package org.sirio5.utils;

import java.io.Closeable;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.util.TurbineException;

/**
 * Helper per la generazione di un oggetto CoreRunData in posti strani (tipo JSP).
 * Implementa Closeable per l'uso in un try with resource.
 * L'oggetto CoreRunData viene inizializzato con i dati utente che sono quindi disponibili.
 * <pre>
 * <code>
 *
 * try(CoreRunDataHelper rh = new CoreRunDataHelper(request, response, config))
 * {
 *   CoreRunData data = rh.getCoreRunData()
 *   ...
 *   ...
 *   LocalizationService ls = rh.getService(LocalizationService.ROLE);
 *   ...
 *   ...
 * }
 *
 * </code>
 * </pre>
 * @author Nicola De Nisco
 */
public class CoreRunDataHelper implements Closeable
{
  // costruisce oggetto RunData estraendolo dal pool
  private RunDataService rundataService = null;
  private CoreRunData data;

  public CoreRunDataHelper(HttpServletRequest req, HttpServletResponse res, ServletConfig config)
     throws ServletException
  {

    if((rundataService = getService(RunDataService.SERVICE_NAME)) == null)
      throw new ServletException("RunData Service is not configured!");
    open(req, res, config);
  }

  /**
   * Ricostruisce un oggetto CoreRunData.
   * Viene chiamata dal costruttore e va usata solo se per qualche motivo
   * è stata chiamata close() in modo esplicito e si vuole ricreare l'oggetto CoreRunData.
   * @param req
   * @param res
   * @param config
   * @return
   * @throws ServletException
   */
  public CoreRunData open(HttpServletRequest req, HttpServletResponse res, ServletConfig config)
     throws ServletException
  {
    if(data != null)
      return data;

    try
    {
      data = (CoreRunData) rundataService.getRunData(req, res, config);

      // Pull user information from session.
      data.populate();
      return data;
    }
    catch(TurbineException ex)
    {
      throw new ServletException(ex);
    }
  }

  @Override
  public void close()
     throws IOException
  {
    // restituisce RunData al pool
    rundataService.putRunData(data);
    data = null;
  }

  public CoreRunData getCoreRunData()
  {
    return data;
  }

  final public <T> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }
}
