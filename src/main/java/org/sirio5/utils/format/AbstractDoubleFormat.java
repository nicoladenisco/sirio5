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

/**
 * Classe base dei formattatori di numeri in doppia precisione.
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractDoubleFormat extends Format
{
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
      if(obj instanceof Number)
      {
        double value = ((Number) obj).doubleValue();
        String sFmt = formatInternal(value);
        toAppendTo.append(sFmt);
      }
      else
      {
        toAppendTo.append("Formato non valido: non è un numero.");
      }

      return toAppendTo;
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  abstract public Number parseInternal(String source)
     throws Exception;

  abstract public String formatInternal(double value)
     throws Exception;
}
