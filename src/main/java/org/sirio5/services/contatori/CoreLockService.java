/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.services.contatori;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.commonlib5.utils.ArraySet;
import org.sirio5.services.AbstractCoreBaseService;

/**
 * Implementazione standard del LockService.
 *
 * @author Nicola De Nisco
 */
public class CoreLockService extends AbstractCoreBaseService
   implements LockService
{
  protected final Map<String, LockResourceBlock> mapResources = new HashMap<>();

  @Override
  public void coreInit()
     throws Exception
  {
  }

  @Override
  public synchronized void createResource(String tipo, int maxLocks, boolean allowMulti)
  {
    LockResourceBlock block = new LockResourceBlock();
    block.maxLocks = maxLocks;
    block.allowMulti = allowMulti;
    mapResources.put(tipo, block);
  }

  private LockResourceItem findCreateItem(String tipo, int idRisorsa)
     throws LockException
  {
    LockResourceBlock block = mapResources.get(tipo);
    if(block == null)
      throw new LockException("Unknow '" + tipo + "' resource.");

    LockResourceItem item = block.lockMap.get(idRisorsa);
    if(item == null)
    {
      item = new LockResourceItem();
      item.sem = new Semaphore(block.maxLocks, true);
      block.lockMap.put(idRisorsa, item);
    }
    return item;
  }

  @Override
  public LockResourceItem getItem(String tipo, int idRisorsa)
     throws LockException
  {
    LockResourceBlock block = mapResources.get(tipo);
    if(block == null)
      throw new IllegalArgumentException("Unknow '" + tipo + "' resource.");

    LockResourceItem item = block.lockMap.get(idRisorsa);
    if(item == null)
      throw new ResourceUnlockedException("Resource " + tipo + "/" + idRisorsa + " is unlocked.");

    return item;
  }

  @Override
  public synchronized void lockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException
  {
    try
    {
      LockResourceItem item = findCreateItem(tipo, idRisorsa);

      item.sem.acquire();
      item.idUtenti.add(idUtente);
      item.blocker = Thread.currentThread();
      item.chiaveBloccante = idRisorsa;
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized void lockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException
  {
    for(Integer id : idRisorse)
      lockResource(tipo, id, idUtente);
  }

  @Override
  public synchronized void lockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException
  {
    try
    {
      LockResourceItem item = findCreateItem(tipo, idRisorsa);

      item.sem.acquire();
      item.idUtenti.addAll(idUtenti);
      item.blocker = Thread.currentThread();
      item.chiaveBloccante = idRisorsa;
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized void lockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException
  {
    for(Integer id : idRisorse)
      lockResourceMulti(tipo, id, idUtenti);
  }

  @Override
  public synchronized void unlockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException
  {
    LockResourceItem item = getItem(tipo, idRisorsa);

    if(item != null)
    {
      if(!item.idUtenti.contains(idUtente))
        throw new IllegalArgumentException("User " + idUtente + " not own the resource " + tipo + "/" + idRisorsa);

      item.sem.release();
      item.idUtenti.remove(idUtente);
    }
  }

  @Override
  public synchronized void unlockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException
  {
    for(Integer id : idRisorse)
      unlockResource(tipo, id, idUtente);
  }

  @Override
  public synchronized void unlockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException
  {
    LockResourceItem item = getItem(tipo, idRisorsa);

    if(item != null)
    {
      for(Integer ute : idUtenti)
      {
        if(item.idUtenti.contains(ute))
        {
          item.sem.release();
          item.idUtenti.removeAll(idUtenti);
          return;
        }
      }

      throw new IllegalArgumentException("Users " + idUtenti + " not owns the resource " + tipo + "/" + idRisorsa);
    }
  }

  @Override
  public synchronized void unlockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException
  {
    for(Integer id : idRisorse)
      unlockResourceMulti(tipo, id, idUtenti);
  }

  @Override
  public synchronized boolean tryLockResource(String tipo, int idRisorsa, int idUtente, long timeoutMillis)
     throws LockException
  {
    try
    {
      LockResourceItem item = findCreateItem(tipo, idRisorsa);

      if(item.sem.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS))
      {
        item.idUtenti.add(idUtente);
        item.blocker = Thread.currentThread();
        item.chiaveBloccante = idRisorsa;
        return true;
      }

      return false;
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized boolean tryLockResources(String tipo, Set<Integer> idRisorse, int idUtente, long timeoutMillis)
     throws LockException
  {
    Set<Integer> acquisiti = new ArraySet<>();

    for(Integer id : idRisorse)
    {
      if(!tryLockResource(tipo, id, idUtente, timeoutMillis))
        break;

      acquisiti.add(id);
    }

    if(acquisiti.size() < idRisorse.size())
    {
      for(Integer id : acquisiti)
        unlockResource(tipo, id, idUtente);
      return false;
    }

    return true;
  }

  @Override
  public synchronized boolean tryLockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException
  {
    try
    {
      LockResourceItem item = findCreateItem(tipo, idRisorsa);

      if(item.sem.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS))
      {
        item.idUtenti.addAll(idUtenti);
        item.blocker = Thread.currentThread();
        item.chiaveBloccante = idRisorsa;
        return true;
      }

      return false;
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized boolean tryLockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException
  {
    Set<Integer> acquisiti = new ArraySet<>();

    for(Integer id : idRisorse)
    {
      if(!tryLockResourceMulti(tipo, id, idUtenti, timeoutMillis))
        break;

      acquisiti.add(id);
    }

    if(acquisiti.size() < idRisorse.size())
    {
      for(Integer id : acquisiti)
        unlockResourceMulti(tipo, id, idUtenti);
      return false;
    }

    return true;
  }
}
