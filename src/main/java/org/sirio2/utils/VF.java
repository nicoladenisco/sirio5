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
package org.sirio2.utils;

import org.apache.turbine.services.TurbineServices;
import org.sirio2.services.formatter.ValutaFormatter;

/**
 * Accesso al servizio ValutaFormatter.
 *
 * @author Nicola De Nisco
 */
public class VF
{
  public static final ValutaFormatter vf = (ValutaFormatter) TurbineServices.getInstance().
     getService(ValutaFormatter.SERVICE_NAME);

  public static String fmtValuta(double value) throws Exception
  {
    return vf.fmtValuta(value);
  }

  public static String fmtValutaDivisa(double value) throws Exception
  {
    return vf.fmtValutaDivisa(value);
  }

  public static double parseValuta(String value) throws Exception
  {
    return vf.parseValuta(value);
  }

  public static double round(double value) throws Exception
  {
    return vf.round(value);
  }

  public String getDivisaText() throws Exception
  {
    return vf.getDivisaText();
  }

  public String getDivisaHtml() throws Exception
  {
    return vf.getDivisaHtml();
  }
}
