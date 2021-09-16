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
package org.sirio5.utils.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import org.sirio5.CoreConst;

/**
 * Formattatore che restituisce il numero di giorni trascorsi
 * rispetto a una data.
 *
 * @author Nicola De Nisco
 */
public class NumDaysColumnFormat extends Format
{
  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
  {
    try
    {
      long numDays = (System.currentTimeMillis() - ((Date)obj).getTime()) / CoreConst.ONE_DAY_MILLIS;
      toAppendTo.append(numDays);
      return toAppendTo;
    }
    catch(Exception e)
    {
      return null;
    }
  }

  @Override
  public Object parseObject(String source, ParsePosition pos)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
