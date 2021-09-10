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
package org.sirio2.services.print;

import java.util.Iterator;
import java.util.Map;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.turbine.services.TurbineServices;
import org.sirio2.services.print.PdfPrint.JobInfo;

/**
 * Accesso al servizio PdfPrint.
 *
 * @author Nicola De Nisco
 */
public class PRINT
{
  private static Object mb = null;

  public static PdfPrint getService()
  {
    if(mb == null)
      mb = TurbineServices.getInstance().getService(PdfPrint.SERVICE_NAME);
    return (PdfPrint) mb;
  }

  public static AbstractReportParametersInfo getParameters(int idUser, String codiceStampa, Map params)
     throws Exception, IllegalAccessException
  {
    return getService().getParameters(idUser, codiceStampa, params);
  }

  public static JobInfo generatePrintJob(int idUser, String codiceStampa, Map params)
     throws Exception, IllegalAccessException
  {
    return getService().generatePrintJob(idUser, codiceStampa, params);
  }

  public static JobInfo generatePrintJob(int idUser, String pluginName, String reportName, String reportInfo, Map params)
     throws Exception, IllegalAccessException
  {
    return getService().generatePrintJob(idUser, pluginName, reportName, reportInfo, params);
  }

  public static JobInfo refreshInfo(String jobCode)
     throws Exception
  {
    return getService().refreshInfo(jobCode);
  }

  public static Iterator<CachedObject> getJobs()
     throws Exception
  {
    return getService().getJobs();
  }
}
