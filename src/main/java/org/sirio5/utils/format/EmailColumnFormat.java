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

import org.sirio5.utils.SU;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Formatta una colonna e-mail aggiungendo mailto: se serve.
 * @author Nicola De Nisco
 */
public class EmailColumnFormat extends Format
{
  public static final String MAILTO = "mailto:";

  @Override
  public Object parseObject(String source, ParsePosition status)
  {
    try
    {
      String rv = source;
      if(source.startsWith(MAILTO))
        rv = source.substring(MAILTO.length());
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
      String val = SU.okStr(obj);
      if(val.indexOf('@') != -1)
        val = "<a href=\"mailto:" + val + "\">" + val + "</a>";

      toAppendTo.append(val);
      return toAppendTo;
    }
    catch(Exception e)
    {
    }
    return null;
  }
}
