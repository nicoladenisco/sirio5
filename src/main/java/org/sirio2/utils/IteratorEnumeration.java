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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Un iteratore che wrappa una enumerazione.
 *
 * @author Nicola De Nisco
 */
public class IteratorEnumeration implements Iterator
{
  private Enumeration e = null;

  public IteratorEnumeration(Enumeration e)
  {
    this.e = e;
  }

  @Override
  public boolean hasNext()
  {
    return e.hasMoreElements();
  }

  @Override
  public Object next()
  {
    return e.nextElement();
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
