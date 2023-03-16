/*
 * Copyright (C) 2022 Nicola De Nisco
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
package org.sirio5.utils.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import org.apache.turbine.services.TurbineServices;
import org.sirio5.services.formatter.DataFormatter;

/**
 * Classe base dei formattatori di date.
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractDateFormat extends Format
{
  // non strettamente necessario, ma la maggior parte delle classi derivate lo usa
  protected DataFormatter df = (DataFormatter) (TurbineServices.getInstance()
     .getService(DataFormatter.SERVICE_NAME));

  @Override
  final public Object parseObject(String source, ParsePosition status)
  {
    try
    {
      Object rv = parseInternal(source);
      status.setIndex(source.length());
      return rv;
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  final public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
  {
    try
    {
      if(obj instanceof Date)
      {
        Date value = (Date) obj;
        String sFmt = formatInternal(value);
        toAppendTo.append(sFmt);
      }
      else
      {
        toAppendTo.append("Formato non valido: non è una data.");
      }

      return toAppendTo;
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  abstract public Date parseInternal(String source)
     throws Exception;

  abstract public String formatInternal(Date value)
     throws Exception;
}
