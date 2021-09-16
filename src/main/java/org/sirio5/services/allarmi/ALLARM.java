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
package org.sirio5.services.allarmi;

import java.util.List;
import org.apache.turbine.services.TurbineServices;

/**
 * Stub per il servizio allarmi.
 *
 * @author Nicola De Nisco
 */
public class ALLARM
{
  private static Object all = null;

  public static ServAllarmi getService()
  {
    if(all == null)
      all = TurbineServices.getInstance().getService(ServAllarmi.SERVICE_NAME);
    return (ServAllarmi) all;
  }

  public static void info(String serv, String comp, String msg, int vis)
  {
    allarmLog(ServAllarmi.TIPALLARME_INFO, serv, comp, msg, vis);
  }

  public static void warning(String serv, String comp, String msg, int vis)
  {
    allarmLog(ServAllarmi.TIPALLARME_WARNING, serv, comp, msg, vis);
  }

  public static void error(String serv, String comp, String msg, int vis)
  {
    allarmLog(ServAllarmi.TIPALLARME_ERROR, serv, comp, msg, vis);
  }

  public static void fatal(String serv, String comp, String msg, int vis)
  {
    allarmLog(ServAllarmi.TIPALLARME_FATAL, serv, comp, msg, vis);
  }

  public static void allarmLog(String severity, String servizio, String componente, String messaggio, int visibilita)
  {
    getService().allarmLog(severity, servizio, componente, messaggio, visibilita);
  }

  public static int getActiveAllarms()
     throws Exception
  {
    return getService().getActiveAllarms();
  }

  public static List checkAllarmi(String serv)
  {
    return getService().checkAllarmi(serv);
  }

  public static List checkAllarmi(String serv, String comp)
  {
    return getService().checkAllarmi(serv, comp);
  }
}
