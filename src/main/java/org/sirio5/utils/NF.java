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
package org.sirio5.utils;

import java.text.DecimalFormatSymbols;
import org.apache.turbine.services.TurbineServices;
import org.sirio5.services.formatter.NumFormatter;

/**
 * Accesso al servizio di formattazione numerica.
 *
 * @author Nicola De Nisco
 */
public class NF
{
  public static final NumFormatter nf = (NumFormatter) TurbineServices.getInstance().
     getService(NumFormatter.SERVICE_NAME);

  public static DecimalFormatSymbols getSymbols()
  {
    return nf.getSymbols();
  }

  public static String format(double val, int nInt, int nDec) throws Exception
  {
    return nf.format(val, nInt, nDec);
  }

  public static double parseDouble(String s, int nInt, int nDec) throws Exception
  {
    return nf.parseDouble(s, nInt, nDec);
  }
}
