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

import java.util.*;
import javax.swing.tree.TreeNode;

/**
 * Classe base per Referenced...
 * Implementa TreeNode e quindi gestisce
 * il contenimento di oggetti figli.
 *
 * @author Nicola De Nisco
 * @deprecated usa la nuova versione generic CoreTreeNodeImpl
 * @see CoreTreeNodeImpl
 */
public abstract class CoreTreeNodeBaseImpl
   implements CoreTreeNode, Iterable, Collection
{
  protected CoreTreeNodeBaseImpl parent = null;
  protected Vector vChild = new Vector();
  protected boolean opened = false;

  public void addChild(CoreTreeNodeBaseImpl child)
  {
    child.parent = this;
    vChild.add(child);
  }

  @Override
  public TreeNode getChildAt(int childIndex)
  {
    return (TreeNode) vChild.get(childIndex);
  }

  @Override
  public int getChildCount()
  {
    return vChild.size();
  }

  @Override
  public TreeNode getParent()
  {
    return parent;
  }

  @Override
  public int getIndex(TreeNode node)
  {
    return vChild.indexOf(node);
  }

  @Override
  public boolean getAllowsChildren()
  {
    return true;
  }

  @Override
  public boolean isLeaf()
  {
    return vChild.isEmpty();
  }

  @Override
  public Enumeration children()
  {
    return vChild.elements();
  }

  @Override
  public boolean isOpened()
  {
    return opened;
  }

  @Override
  public void setOpened(boolean opened)
  {
    this.opened = opened;
  }

  @Override
  public Iterator iterator()
  {
    return vChild.iterator();
  }

  @Override
  public int size()
  {
    return vChild.size();
  }

  @Override
  public boolean isEmpty()
  {
    return vChild.isEmpty();
  }

  @Override
  public boolean contains(Object o)
  {
    return vChild.contains(o);
  }

  @Override
  public Object[] toArray()
  {
    return vChild.toArray();
  }

  @Override
  public Object[] toArray(Object[] a)
  {
    return vChild.toArray(a);
  }

  @Override
  public boolean add(Object o)
  {
    return vChild.add(o);
  }

  @Override
  public boolean remove(Object o)
  {
    return vChild.remove(o);
  }

  @Override
  public boolean containsAll(Collection c)
  {
    return vChild.containsAll(c);
  }

  @Override
  public boolean addAll(Collection c)
  {
    return vChild.addAll(c);
  }

  @Override
  public boolean removeAll(Collection c)
  {
    return vChild.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection c)
  {
    return vChild.retainAll(c);
  }

  @Override
  public void clear()
  {
    parent = null;
    opened = false;
    vChild.clear();
  }

  public void removeChild()
  {
    vChild.clear();
  }

  public void moveUp(TreeNode child)
  {
    int idx = vChild.indexOf(child);
    if(idx > 0)
    {
      vChild.remove(child);
      vChild.add(idx-1, child);
    }
  }

  public void moveDown(TreeNode child)
  {
    int idx = vChild.indexOf(child);
    if(idx > -1 && idx < vChild.size()-1)
    {
      vChild.remove(child);
      vChild.add(idx+1, child);
    }
  }

  public boolean isRoot()
  {
    return parent == null;
  }
}
