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

import org.sirio2.services.formatter.DataFormatter;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import org.apache.turbine.services.TurbineServices;

/**
 * Formattatore della data e ora con troncamento ai minuti.
 * Viene utilizzato in liste.xml.
 *
 * @author Nicola De Nisco
 */
public class DateTimeFormatMinuti extends Format
{
  private DataFormatter df = null;

  public DateTimeFormatMinuti()
  {
    df = (DataFormatter) (TurbineServices.getInstance().getService(DataFormatter.SERVICE_NAME));
  }

  @Override
  public Object parseObject(String source, ParsePosition status)
  {
    try
    {
      Object rv = df.parseDataFull(source);
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
      String s = df.formatDataFull((Date) obj);
      if(s != null && s.length() > 3)
      {
        s = s.substring(0, s.length() - 3);
        toAppendTo.append(s);
      }
      return toAppendTo;
    }
    catch(Exception e)
    {
    }
    return null;
  }
}
