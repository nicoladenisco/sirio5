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
import org.apache.commons.lang.StringUtils;

/**
 * Fromattatore per stringhe per un massimo di 32 caratteri.
 *
 * @author Nicola De Nisco
 */
public class Max32StringFormatter extends Format
{
  public int getMaxLen()
  {
    return 32;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
  {
    if(obj != null)
      toAppendTo.append(StringUtils.abbreviate(obj.toString(), getMaxLen()));
    return toAppendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos)
  {
    pos.setIndex(source.length());
    return source;
  }
}
