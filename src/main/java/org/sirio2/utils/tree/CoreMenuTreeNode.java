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
package org.sirio2.utils.tree;

import org.sirio2.beans.menu.MenuItemBean;

/**
 * Nodo dell'albero di menu.
 *
 * @author Nicola De Nisco
 */
public class CoreMenuTreeNode extends CoreTreeNodeImpl<MenuItemBean>
{
  private boolean enabled = false;

  public CoreMenuTreeNode(MenuItemBean l)
  {
    super(l);
  }

  public MenuItemBean getMenuItem()
  {
    return getValue();
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public CoreMenuTreeNode findNodeByID(int idSysmenu)
  {
    if(idSysmenu == getValue().getListproId())
      return this;

    if(!isLeaf())
    {
      CoreMenuTreeNode rv;
      for(CoreTreeNodeImpl<MenuItemBean> child : this)
      {
        if((rv = ((CoreMenuTreeNode) child).findNodeByID(idSysmenu)) != null)
          return rv;
      }
    }

    return null;
  }
}
