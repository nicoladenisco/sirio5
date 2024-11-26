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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.RefreshableCachedObject;
import org.commonlib5.utils.ArrayMap;
import org.sirio5.services.bus.BUS;
import org.sirio5.services.bus.BusContext;
import org.sirio5.services.bus.BusMessages;

/**
 * Implementazione comune per CoreCacheServices.
 *
 * @author Nicola De Nisco
 */
public class CoreCacheImp implements CoreCacheServices
{
  protected HashMap<String, CacheBlock> htClasses = new HashMap<>();
  /**
   * Initial size of hash table.
   * Default = 20
   */
  public static final int DEFAULT_INITIAL_CACHE_SIZE = 20;
  /**
   * The property for the InitalCacheSize
   */
  public static final String INITIAL_CACHE_SIZE = "cache.initial.size";
  /**
   * The property for the Cache check frequency
   */
  public static final String CACHE_CHECK_FREQUENCY = "cache.check.frequency";
  /**
   * Cache check frequency in Millis (1000 Millis = 1 second).
   * Default = 15 seconds
   */
  public static final long DEFAULT_CACHE_CHECK_FREQUENCY = 15000; // 15 seconds
  /**
   * cacheCheckFrequency (default = 15 seconds)
   */
  protected int cacheInitialSize = DEFAULT_INITIAL_CACHE_SIZE;
  protected long cacheCheckFrequency = DEFAULT_CACHE_CHECK_FREQUENCY;

  protected CacheBlock getCacheBlock(String objClass)
  {
    CacheBlock cb = (CacheBlock) (htClasses.get(objClass));
    if(cb == null)
    {
      cb = new CacheBlock();
      htClasses.put(objClass, cb);
    }
    return cb;
  }

  protected Map<String, CachedObject> getCache(String objClass)
  {
    return getCacheBlock(objClass).cache;
  }

  /**
   * Returns an item from the cache. RefreshableCachedObject will be
   * refreshed if it is expired and not untouched.
   *
   * @param id The key of the stored object.
   * @return The object from the cache.
   * @exception ObjectExpiredException when either the object is
   * not in the cache or it has expired.
   */
  @Override
  public synchronized CachedObject getObject(String objClass, String id)
     throws ObjectExpiredException
  {
    Map<String, CachedObject> cache = getCache(objClass);
    CachedObject obj = cache.get(id);

    if(obj == null)
    {
      // Not in the cache.
      throw new ObjectExpiredException();
    }

    if(obj.isStale())
    {
      if(obj instanceof RefreshableCachedObject)
      {
        RefreshableCachedObject rco = (RefreshableCachedObject) obj;
        if(rco.isUntouched())
          // Do not refresh an object that has exceeded TimeToLive
          throw new ObjectExpiredException();

        // Refresh Object
        rco.refresh();

        if(rco.isStale())
          // Object is Expired.
          throw new ObjectExpiredException();
      }
      else if(obj instanceof CoreRefreshableCachedObject)
      {
        CoreRefreshableCachedObject wrco = (CoreRefreshableCachedObject) obj;
        if(wrco.isUntouched())
          // Do not refresh an object that has exceeded TimeToLive
          throw new ObjectExpiredException();

        // Refresh Object
        wrco.refresh();

        if(wrco.isStale())
          // Object is Expired.
          throw new ObjectExpiredException();
      }
      else
      {
        // Expired.
        throw new ObjectExpiredException();
      }
    }

    if(obj instanceof RefreshableCachedObject)
    {
      // notify it that it's being accessed.
      RefreshableCachedObject rco = (RefreshableCachedObject) obj;
      rco.touch();
    }
    else if(obj instanceof CoreRefreshableCachedObject)
    {
      CoreRefreshableCachedObject wrco = (CoreRefreshableCachedObject) obj;
      wrco.refreshEntry();
      wrco.touch();
    }
    else if(obj instanceof CoreCachedObject)
    {
      CoreCachedObject wco = (CoreCachedObject) obj;
      wco.refreshEntry();
    }

    return obj;
  }

  /**
   * Adds an object to the cache.
   *
   * @param id The key to store the object by.
   * @param o The object to cache.
   */
  @Override
  public synchronized void addObject(String objClass, String id, CachedObject o)
  {
    CacheBlock cb = getCacheBlock(objClass);
    HashMap cache = cb.cache;

    // If the cache already contains the key, remove it and add
    // the fresh one.
    CachedObject co = (CachedObject) cache.get(id);
    if(co != null)
    {
      if(!notifyRemoveObject(co))
        return;

      cache.remove(id);
      cache.put(id, o);
    }
    else
    {
      cache.put(id, o);
    }
  }

  /**
   * Removes an object from the cache.
   *
   * @param id The String id for the object.
   */
  @Override
  public synchronized void removeObject(String objClass, String id)
  {
    Map<String, CachedObject> cache = getCache(objClass);
    CachedObject co = (CachedObject) cache.get(id);
    if(co != null)
    {
      if(!notifyRemoveObject(co))
        return;
      cache.remove(id);
    }
  }

  /**
   * Rimuove tutti gli oggetti la cui chiave inizia con una stringa.
   * @param objClass classe della cache
   * @param idLeftPart stringa iniziale degli id da rimuovere
   */
  @Override
  public synchronized void removeAllObjects(String objClass, String idLeftPart)
  {
    removeAllObjects(objClass, (String key, CachedObject value) -> key.startsWith(idLeftPart));
  }

  @Override
  public synchronized void removeAllObjects(String objClass, testRemoveInterface test)
  {
    // acquisisce semaforo cancellazione in corso
    synchronized(semClear)
    {
      ArrayList<String> arKeysDelete = new ArrayList<>();
      Map<String, CachedObject> cache = getCache(objClass);

      for(Map.Entry<String, CachedObject> entrySet : cache.entrySet())
      {
        String key = entrySet.getKey();
        CachedObject value = entrySet.getValue();

        if(test != null && !test.testForRemove(key, value))
          continue;

        if(notifyRemoveObject(value))
          arKeysDelete.add(key);
      }

      for(String key : arKeysDelete)
        cache.remove(key);
    }
  }

  /**
   * Prima di rimuovere l'oggetto dalla cache avvia
   * le operazioni di cancellazione dello stesso.
   * @param co
   * @return
   */
  protected boolean notifyRemoveObject(CachedObject co)
  {
    if(co instanceof CoreCachedObject)
    {
      CoreCachedObject wco = (CoreCachedObject) co;

      // un oggetto non cancellabile non puo' essere rimosso dalla cache
      if(!wco.isDeletable())
        return false;

      // segnala che sta per essere cancellato
      wco.deletingExpired();
    }
    return true;
  }

  /**
   * Circle through the cache and remove stale objects. Frequency
   * is determined by the cacheCheckFrequency property.
   */
  protected void runCleaner()
  {
    while(true)
    {
      try
      {
        // Sleep for amount of time set in cacheCheckFrequency - default = 5 seconds.
        Thread.sleep(cacheCheckFrequency);

        // rimuove oggetti scaduti dalla cache
        clearCache();
      }
      catch(InterruptedException e)
      {
      }
      catch(Throwable t)
      {
        Logger.getLogger(CoreCacheImp.class.getName()).log(Level.SEVERE, "Error in cache cleaner:", t);
      }
    }
  }

  /**
   * Iterate through the cache and remove or refresh stale objects.
   */
  protected void clearCache()
  {
    ArrayList<CachedObject> refreshThese = new ArrayList<>();
    ArrayList<CoreCachedObject> deleteThese = new ArrayList<>();

    // acquisisce semaforo cancellazione in corso
    synchronized(semClear)
    {
      // Sync on this object so that other threads do not
      // change the HashMap while enumerating over it.
      synchronized(this)
      {
        for(Map.Entry<String, CacheBlock> entryClasses : htClasses.entrySet())
        {
          String objClass = entryClasses.getKey();
          CacheBlock cb = entryClasses.getValue();
          HashMap<String, CachedObject> cache = cb.cache;
          ArrayList<String> toRemoveKeys = new ArrayList<>();

          for(Map.Entry<String, CachedObject> entryItem : cache.entrySet())
          {
            String key = entryItem.getKey();
            CachedObject co = entryItem.getValue();

            if(co instanceof RefreshableCachedObject)
            {
              RefreshableCachedObject rco = (RefreshableCachedObject) co;
              if(rco.isUntouched())
              {
                toRemoveKeys.add(key);
              }
              else if(rco.isStale())
              {
                // Do refreshing outside of sync block so as not
                // to prolong holding the lock on this object
                refreshThese.add(rco);
              }
            }
            else if(co instanceof CoreRefreshableCachedObject)
            {
              CoreRefreshableCachedObject wrco = (CoreRefreshableCachedObject) co;

              if(wrco.isUntouched())
              {
                // un oggetto non cancellabile non puo' essere rimosso dalla cache
                if(!wrco.isDeletable())
                  continue;

                // segnala che sta per essere cancellato
                deleteThese.add(wrco);
                toRemoveKeys.add(key);
              }
              else if(wrco.isStale())
              {
                // Do refreshing outside of sync block so as not
                // to prolong holding the lock on this object
                refreshThese.add(wrco);
              }
            }
            else if(co instanceof CoreCachedObject)
            {
              CoreCachedObject wco = (CoreCachedObject) co;

              if(co.isStale())
              {
                // un oggetto non cancellabile non puo' essere rimosso dalla cache
                if(!wco.isDeletable())
                  continue;

                // segnala che sta per essere cancellato
                deleteThese.add(wco);
                toRemoveKeys.add(key);
              }
            }
            else
            {
              if(co.isStale())
                toRemoveKeys.add(key);
            }
          }

          // rimuove tutti gli oggetti scaduti dalla cache
          for(String key : toRemoveKeys)
            cache.remove(key);

          // verifica se la cache ha un limite ed eventualmente cancella
          int limit = cb.limit;
          if(limit != UNLIMITED && cache.size() >= limit)
            checkLimit(cache, limit, deleteThese);
        }
      }

      // notifica agli oggetti la loro rimozione dalla cache
      if(!deleteThese.isEmpty())
      {
        synchronized(semDelete)
        {
          for(CachedObject o : deleteThese)
          {
            try
            {
              CoreCachedObject co = (CoreCachedObject) o;
              co.deletingExpired();
            }
            catch(Throwable ex)
            {
            }
          }
        }
      }

      // chiama il metodo refresh per gli elementi che lo richiedono
      if(!refreshThese.isEmpty())
      {
        synchronized(semUpdate)
        {
          for(CachedObject co : refreshThese)
          {
            try
            {
              if(co instanceof RefreshableCachedObject)
              {
                RefreshableCachedObject rco = (RefreshableCachedObject) co;
                rco.refresh();
              }
              else if(co instanceof CoreRefreshableCachedObject)
              {
                CoreRefreshableCachedObject wrco = (CoreRefreshableCachedObject) co;
                wrco.refresh();
              }
            }
            catch(Throwable ex)
            {
            }
          }
        }
      }
    }
  }

  /**
   * Returns the number of objects currently stored in the cache
   *
   * @return int number of object in the cache
   */
  @Override
  public synchronized int getNumberOfObjects()
  {
    int numItem = 0;
    for(Map.Entry<String, CacheBlock> entryClasses : htClasses.entrySet())
    {
      String objClass = entryClasses.getKey();
      CacheBlock cb = entryClasses.getValue();
      numItem += cb.cache.size();
    }

    return numItem;
  }

  /**
   * Returns the number of objects currently stored in the cache
   *
   * @return int number of object in the cache
   */
  @Override
  public synchronized int getNumberOfObjects(String objClass)
  {
    Map<String, CachedObject> cache = getCache(objClass);
    return cache.size();
  }

  /**
   * Returns the current size of the cache.
   *
   * @return int representing current cache size in number of bytes
   * @throws java.io.IOException
   */
  @Override
  public synchronized int getCacheSize()
     throws IOException
  {
    int objectsize = 0;

    for(Map.Entry<String, CacheBlock> entryClasses : htClasses.entrySet())
    {
      String objClass = entryClasses.getKey();
      CacheBlock cb = entryClasses.getValue();
      HashMap cache = cb.cache;

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(baos);
      out.writeObject(cache);
      out.flush();
      //
      // Subtract 4 bytes from the length, because the serialization
      // magic number (2 bytes) and version number (2 bytes) are
      // both written to the stream before the object
      //
      objectsize += baos.toByteArray().length - 4;
    }

    return objectsize;
  }

  /**
   * Flush the cache of all objects.
   */
  @Override
  public synchronized void flushCache()
  {
    for(Map.Entry<String, CacheBlock> entryClasses : htClasses.entrySet())
    {
      String objClass = entryClasses.getKey();
      CacheBlock cb = entryClasses.getValue();
      if(cb.flushPermitted)
        flushCache(objClass);
    }

    // notifica gli interessati l'avvenuto svuotamento della global cache
    BUS.sendMessageAsync(BusMessages.CLEAR_GLOBAL_CACHE, this);
  }

  /**
   * Elimina tutti gli oggetti della cache della classe specificata.
   * @param objClass classe deglli oggetti
   */
  @Override
  public synchronized void flushCache(String objClass)
  {
    try
    {
      Map<String, CachedObject> cache = getCache(objClass);
      ArrayMap<String, CachedObject> toRemove = new ArrayMap<>();
      for(Map.Entry<String, CachedObject> entry : cache.entrySet())
      {
        String key = entry.getKey();
        CachedObject co = entry.getValue();

        if(co != null)
        {
          if(notifyRemoveObject(co))
            toRemove.put(key, co);
        }
      }

      // rimuove dalla cache tutte le entry
      toRemove.forEachKey((s) -> cache.remove(s));

      BusContext bc = new BusContext(
         "class", objClass,
         "removed", toRemove
      );
      BUS.sendMessageAsync(BusMessages.CLEAR_GLOBAL_CACHE_CLASS, this, bc);
    }
    catch(Exception ex)
    {
      Logger.getLogger(CoreCacheImp.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Imposta limite per una classe di oggetti.
   * @param objClass classe degli oggetti
   * @param limit numero massimo oggetti possibili per la classe specificata
   */
  @Override
  public void setLimit(String objClass, int limit)
  {
    CacheBlock cb = getCacheBlock(objClass);
    cb.limit = limit;
  }

  /**
   * Recuper limite i oggetti per la classe indicata.
   * @param objClass classe degli oggetti
   * @return numero massimo oggetti possibili per la classe specificata
   */
  @Override
  public int getLimit(String objClass)
  {
    CacheBlock cb = getCacheBlock(objClass);
    return cb.limit;
  }

  @Override
  public void setFlushPermitted(String objClass, boolean flushPermitted)
  {
    CacheBlock cb = getCacheBlock(objClass);
    cb.flushPermitted = flushPermitted;
  }

  @Override
  public boolean isFlushPermitted(String objClass)
  {
    CacheBlock cb = getCacheBlock(objClass);
    return cb.flushPermitted;
  }

  /**
   * Verifica il superamento del limite massimo.
   * Per una determinata sottoarea della cache verifica il raggiungimento
   * del limite massimo di oggetti previsto per la classe di oggetti.
   * Al superamento del limite gli oggetti eccedenti vengono rimossi
   * in ordina cronologico.
   * @param cache sottoarea della cache (classe di oggetti)
   * @param limit limite per la sottoarea (classe)
   * @param deleteThese vettore per l'accodamento degli oggetti da cancellare
   */
  protected void checkLimit(Map<String, CachedObject> cache, int limit, List<CoreCachedObject> deleteThese)
  {
    // prepara un array con gli oggetti della cache
    Collection<CachedObject> c = cache.values();
    CachedObject[] coArr = new CachedObject[c.size()];
    c.toArray(coArr);

    // ordina l'array dal più vecchio
    Arrays.sort(coArr, (CachedObject o1, CachedObject o2) -> Long.compare(o1.getCreated(), o2.getCreated()));

    // rimuove dalla cache gli elementi più vecchi fino a rientrare nel limite imposto
    for(int i = 0; i < coArr.length && cache.size() >= limit; i++)
    {
      CachedObject co = coArr[i];

      if(co instanceof CoreCachedObject)
      {
        CoreCachedObject wco = (CoreCachedObject) co;

        // un oggetto non cancellabile non puo' essere rimosso dalla cache
        if(!wco.isDeletable())
          continue;

        // carica il vettore con gli elementi da segnalare cancellazione
        deleteThese.add(wco);
      }

      c.remove(co);
    }
  }

  /**
   * Gets a cached object given its id (a String).
   *
   * @param id The String id for the object.
   * @return A CachedObject.
   * @throws ObjectExpiredException
   */
  @Override
  public CachedObject getObject(String id)
     throws ObjectExpiredException
  {
    return getObject(GENERIC_OBJ_CLASS, id);
  }

  /**
   * Adds an object to the cache.
   *
   * @param id The String id for the object.
   * @param o The object to add to the cache.
   */
  @Override
  public void addObject(String id, CachedObject o)
  {
    addObject(GENERIC_OBJ_CLASS, id, o);
  }

  /**
   * Removes an object from the cache.
   *
   * @param id The String id for the object.
   */
  @Override
  public void removeObject(String id)
  {
    removeObject(GENERIC_OBJ_CLASS, id);
  }

  /**
   * Ritorna un enumeratore ai nomi di classe
   * contenuti nella cache.
   * @return
   */
  @Override
  public synchronized Iterator<String> classNames()
  {
    return htClasses.keySet().iterator();
  }

  @Override
  public synchronized Iterator<CachedObject> cachedObjects(String className)
  {
    Map<String, CachedObject> ht = getCache(className);
    return ht.values().iterator();
  }

  /**
   * Esegue il dump del contenuto della cache sul PrintWriter indicato.
   * Per ogni ogggetto viene stampata la chiave e il risultato della funzione toString()
   * @param objClass
   * @param out
   * @throws Exception
   */
  @Override
  public void dumpCache(String objClass, PrintWriter out)
     throws Exception
  {
    Map<String, CachedObject> cache = getCache(objClass);
    for(Map.Entry<String, CachedObject> entry : cache.entrySet())
    {
      String key = entry.getKey();
      CachedObject obj = entry.getValue();

      if(obj == null)
        continue;

      Object content = obj.getContents();
      if(content == null)
        continue;

      if(obj.isStale())
        out.println(key + "[expired]=" + content);
      else
        out.println(key + "=" + content);
    }
  }

  @Override
  public synchronized boolean refreshObject(String id)
  {
    return refreshObject(GENERIC_OBJ_CLASS, id);
  }

  @Override
  public synchronized boolean refreshObject(String objClass, String id)
  {
    Map<String, CachedObject> cache = getCache(objClass);
    CachedObject obj = cache.get(id);

    if(obj == null)
      return false;

    if(obj instanceof RefreshableCachedObject)
    {
      // notify it that it's being accessed.
      RefreshableCachedObject rco = (RefreshableCachedObject) obj;
      rco.refresh();
    }
    else if(obj instanceof CoreRefreshableCachedObject)
    {
      CoreRefreshableCachedObject wrco = (CoreRefreshableCachedObject) obj;
      wrco.refreshEntry();
    }
    else if(obj instanceof CoreCachedObject)
    {
      CoreCachedObject wco = (CoreCachedObject) obj;
      wco.refreshEntry();
    }
    else
    {
      cache.put(id, new CachedObject(obj.getContents(), obj.getExpires()));
    }

    return true;
  }

  @Override
  public List<String> getKeys()
  {
    Map<String, CachedObject> cache = getCache(GENERIC_OBJ_CLASS);
    return new ArrayList(cache.keySet());
  }

  @Override
  public List<CachedObject<?>> getCachedObjects()
  {
    Map<String, CachedObject> cache = getCache(GENERIC_OBJ_CLASS);
    return new ArrayList(cache.values());
  }

  @Override
  public boolean containsObject(String id)
  {
    Map<String, CachedObject> cache = getCache(GENERIC_OBJ_CLASS);
    return cache.containsKey(id);
  }

  @Override
  public boolean containsObject(String objClass, String id)
  {
    Map<String, CachedObject> cache = getCache(objClass);
    return cache.containsKey(id);
  }
}
