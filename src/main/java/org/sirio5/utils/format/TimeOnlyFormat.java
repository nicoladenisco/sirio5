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

import java.util.Date;

/**
 * Formattatore della data e ora.
 * Viene utilizzato in liste.xml.
 *
 * @author Nicola De Nisco
 * @version 1.0
 */
public class TimeOnlyFormat extends AbstractDateFormat
{
  @Override
  public Date parseInternal(String source)
     throws Exception
  {
    return df.parseTime(source);
  }

  @Override
  public String formatInternal(Date value)
     throws Exception
  {
    return df.formatTime(value);
  }
}
