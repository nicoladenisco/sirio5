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
package org.sirio5.services;

import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.LinkedMap;

/**
 * Blocco informazioni di setup.
 *
 * @author Nicola De Nisco
 */
public class InfoSetupBlock
{
  protected String name, priority;
  protected LinkedMap info = new LinkedMap();

  public InfoSetupBlock()
  {
  }

  public InfoSetupBlock(String name, String priority)
  {
    this.name = name;
    this.priority = priority;
  }

  public InfoSetupBlock add(String key, String value)
  {
    info.put(key, value);
    return this;
  }

  public Set<Map.Entry<String, String>> entrySet()
  {
    return info.entrySet();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getPriority()
  {
    return priority;
  }

  public void setPriority(String priority)
  {
    this.priority = priority;
  }
}
