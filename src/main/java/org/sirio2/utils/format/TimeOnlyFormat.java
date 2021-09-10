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
package org.sirio2.utils.format;

import java.text.*;
import java.util.Date;
import org.sirio2.services.formatter.DataFormatter;
import org.apache.turbine.services.TurbineServices;

/**
 * Formattatore della data e ora.
 * Viene utilizzato in liste.xml.
 *
 * @author Nicola De Nisco
 * @version 1.0
 */
public class TimeOnlyFormat extends Format
{
  private DataFormatter df = null;

  public TimeOnlyFormat()
  {
    df = (DataFormatter) (TurbineServices.getInstance().getService(DataFormatter.SERVICE_NAME));
  }

  @Override
  public Object parseObject(String source, ParsePosition status)
  {
    try
    {
      Object rv = df.parseTime(source);
      status.setIndex(source.length());
      return rv;
    }
    catch(Exception e)
    {
    }
    return null;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
  {
    try
    {
      toAppendTo.append(df.formatTime((Date) obj));
      return toAppendTo;
    }
    catch(Exception e)
    {
    }
    return null;
  }
}
