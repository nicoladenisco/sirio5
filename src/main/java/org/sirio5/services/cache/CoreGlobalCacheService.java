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
package org.sirio5.services.cache;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.sirio5.services.AbstractCoreBaseService;
import static org.sirio5.services.cache.CoreCacheImp.CACHE_CHECK_FREQUENCY;
import static org.sirio5.services.cache.CoreCacheImp.INITIAL_CACHE_SIZE;

/**
 * Estensione del servizio di cache globale di Turbine.
 * Aggiugne la possibilita' di eliminare oggetti.
 * @author Nicola De Nisco
 */
public class CoreGlobalCacheService extends AbstractCoreBaseService
   implements CoreCacheServices
{
  protected CoreCacheImp ci = new CoreCacheImp();

  /**
   * Inizializzazione del servizio.
   * @throws java.lang.Exception
   */
  @Override
  public void coreInit()
     throws Exception
  {
    Configuration conf = getConfiguration();
    if(conf != null)
    {
      ci.cacheInitialSize = conf.getInt(INITIAL_CACHE_SIZE, ci.cacheInitialSize);
      if(ci.cacheInitialSize <= 0)
        throw new IllegalArgumentException(INITIAL_CACHE_SIZE + " must be >0");

      ci.cacheCheckFrequency = conf.getLong(CACHE_CHECK_FREQUENCY, ci.cacheCheckFrequency);
      if(ci.cacheCheckFrequency <= 0)
        throw new IllegalArgumentException(CACHE_CHECK_FREQUENCY + " must be >0");
    }

    // Start housekeeping thread.
    Thread housekeeping = new Thread(() -> ci.runCleaner());
    // Indicate that this is a system thread. JVM will quit only when there
    // are no more active user threads. Settings threads spawned internally
    // by Turbine as daemons allows commandline applications using Turbine
    // to terminate in an orderly manner.
    housekeeping.setName("CoreGlobalCacheService");
    housekeeping.setDaemon(true);
    housekeeping.start();

    setInit(true);
  }

  @Override
  public CachedObject getObject(String objClass, String id)
     throws ObjectExpiredException
  {
    return ci.getObject(objClass, id);
  }

  @Override
  public void addObject(String objClass, String id, CachedObject o)
  {
    ci.addObject(objClass, id, o);
  }

  @Override
  public void removeObject(String objClass, String id)
  {
    ci.removeObject(objClass, id);
  }

  @Override
  public void removeAllObjects(String objClass, String idLeftPart)
  {
    ci.removeAllObjects(objClass, idLeftPart);
  }

  @Override
  public void removeAllObjects(String objClass, testRemoveInterface test)
  {
    ci.removeAllObjects(objClass, test);
  }

  @Override
  public void setLimit(String objClass, int limit)
  {
    ci.setLimit(objClass, limit);
  }

  @Override
  public int getLimit(String objClass)
  {
    return ci.getLimit(objClass);
  }

  @Override
  public void setFlushPermitted(String objClass, boolean flushPermitted)
  {
    ci.setFlushPermitted(objClass, flushPermitted);
  }

  @Override
  public boolean isFlushPermitted(String objClass)
  {
    return ci.isFlushPermitted(objClass);
  }

  @Override
  public int getNumberOfObjects(String objClass)
  {
    return ci.getNumberOfObjects();
  }

  @Override
  public Iterator<String> classNames()
  {
    return ci.classNames();
  }

  @Override
  public Iterator<CachedObject> cachedObjects(String className)
  {
    return ci.cachedObjects(className);
  }

  @Override
  public void dumpCache(String objClass, PrintWriter out)
     throws Exception
  {
    ci.dumpCache(objClass, out);
  }

  @Override
  public void flushCache(String objClass)
  {
    ci.flushCache(objClass);
  }

  @Override
  public boolean refreshObject(String id)
  {
    return ci.refreshObject(id);
  }

  @Override
  public boolean refreshObject(String objClass, String id)
  {
    return ci.refreshObject(objClass, id);
  }

  @Override
  public boolean containsObject(String id)
  {
    return ci.containsObject(id);
  }

  @Override
  public boolean containsObject(String objClass, String id)
  {
    return ci.containsObject(objClass, id);
  }

  @Override
  public CachedObject getObject(String id)
     throws ObjectExpiredException
  {
    return ci.getObject(id);
  }

  @Override
  public void addObject(String id, CachedObject o)
  {
    ci.addObject(id, o);
  }

  @Override
  public void removeObject(String id)
  {
    ci.removeObject(id);
  }

  @Override
  public List getKeys()
  {
    return ci.getKeys();
  }

  @Override
  public List getCachedObjects()
  {
    return ci.getCachedObjects();
  }

  @Override
  public int getCacheSize()
     throws IOException
  {
    return ci.getCacheSize();
  }

  @Override
  public int getNumberOfObjects()
  {
    return ci.getNumberOfObjects();
  }

  @Override
  public void flushCache()
  {
    ci.flushCache();
  }
}
