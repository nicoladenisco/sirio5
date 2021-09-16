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
package org.sirio5.utils.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.Predicate;
import javax.swing.tree.TreeNode;
import org.apache.commons.collections.IteratorUtils;
import org.commonlib5.lambda.PredicateThrowException;

/**
 * Nodo di un albero.
 * Implementa TreeNode e quindi gestisce
 * il contenimento di oggetti figli.
 * @author Nicola De Nisco
 * @param <T> oggetto contenuto nel nodo
 */
public class CoreTreeNodeImpl<T> extends ArrayList<CoreTreeNodeImpl<T>>
   implements CoreTreeNode
{
  protected boolean opened = false;
  protected CoreTreeNode parent = null;
  protected T value = null;

  public CoreTreeNodeImpl()
  {
  }

  public CoreTreeNodeImpl(T value)
  {
    this.value = value;
  }

  public T getValue()
  {
    return value;
  }

  public void setValue(T value)
  {
    this.value = value;
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

  public void addChild(CoreTreeNodeImpl<T> child)
  {
    child.parent = this;
    add(child);
  }

  @Override
  public TreeNode getChildAt(int childIndex)
  {
    return get(childIndex);
  }

  @Override
  public int getChildCount()
  {
    return size();
  }

  public void setParent(CoreTreeNode parent)
  {
    this.parent = parent;
  }

  @Override
  public TreeNode getParent()
  {
    return parent;
  }

  @Override
  public int getIndex(TreeNode node)
  {
    return indexOf(node);
  }

  @Override
  public boolean getAllowsChildren()
  {
    return true;
  }

  @Override
  public boolean isLeaf()
  {
    return isEmpty();
  }

  @Override
  public Enumeration children()
  {
    return IteratorUtils.asEnumeration(iterator());
  }

  @Override
  public void clear()
  {
    parent = null;
    opened = false;
    value = null;
    super.clear();
  }

  public void removeChild()
  {
    super.clear();
  }

  public void moveUp(CoreTreeNodeImpl<T> child)
  {
    int idx = indexOf(child);
    if(idx > 0)
    {
      remove(child);
      add(idx - 1, child);
    }
  }

  public void moveDown(CoreTreeNodeImpl<T> child)
  {
    int idx = indexOf(child);
    if(idx > -1 && idx < size() - 1)
    {
      remove(child);
      add(idx + 1, child);
    }
  }

  public boolean isRoot()
  {
    return parent == null;
  }

  public T findDown(Predicate<T> p)
  {
    if(value != null && p.test(value))
      return value;

    T t;
    for(CoreTreeNodeImpl<T> n : this)
    {
      if((t = n.findDown(p)) != null)
        return t;
    }

    return null;
  }

  public T findDown2(PredicateThrowException<T> p)
     throws Exception
  {
    if(value != null && p.test(value))
      return value;

    T t;
    for(CoreTreeNodeImpl<T> n : this)
    {
      if((t = n.findDown2(p)) != null)
        return t;
    }

    return null;
  }

  public T findUp(Predicate<T> p)
  {
    if(value != null && p.test(value))
      return value;

    if(getParent() == null || !(getParent() instanceof CoreTreeNodeImpl))
      return null;

    CoreTreeNodeImpl<T> pp = (CoreTreeNodeImpl<T>) getParent();
    return pp.findUp(p);
  }

  public T findUp2(PredicateThrowException<T> p)
     throws Exception
  {
    if(value != null && p.test(value))
      return value;

    if(getParent() == null || !(getParent() instanceof CoreTreeNodeImpl))
      return null;

    CoreTreeNodeImpl<T> pp = (CoreTreeNodeImpl<T>) getParent();
    return pp.findUp2(p);
  }

  public void getAncestor(Collection<T> l)
  {
    if(value != null)
      l.add(value);

    if(getParent() == null || !(getParent() instanceof CoreTreeNodeImpl))
      return;

    CoreTreeNodeImpl<T> pp = (CoreTreeNodeImpl<T>) getParent();
    pp.getAncestor(l);
  }
}
