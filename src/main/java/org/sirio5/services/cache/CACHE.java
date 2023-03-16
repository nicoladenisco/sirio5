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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.lambda.FunctionTrowException;
import org.sirio5.CoreConst;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.utils.SU;

/**
 * Accesso al servizio di cache globale.
 *
 * @author Nicola De Nisco
 */
public class CACHE
{
  private static Object __mb;

  public static CoreCacheServices getService()
  {
    if(__mb == null)
      __mb = TurbineServices.getInstance().getService(GlobalCacheService.ROLE);

    return (CoreCacheServices) __mb;
  }

  /**
   * Gets a cached object given its id (a String).
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @return A CachedObject.
   * @throws org.apache.fulcrum.cache.ObjectExpiredException
   * @exception ObjectExpiredException, if the object has expired in
   * the cache or not found.
   */
  public static CachedObject getObject(String objClass, String id)
     throws ObjectExpiredException
  {
    return getService().getObject(objClass, id);
  }

  /**
   * Gets a cached object given its id (a String).
   * Like getObject() but if the object has expired in
   * the cache or not found simply return null.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @return A CachedObject or null.
   */
  public static CachedObject getObjectQuiet(String objClass, String id)
  {
    try
    {
      return getService().getObject(objClass, id);
    }
    catch(ObjectExpiredException e)
    {
      return null;
    }
  }

  /**
   * Gets the contento of a cached object given its id (a String).
   * Like getObject() but if the object has expired in
   * the cache or not found simply return null.
   *
   * @param id The String id for the object.
   * @return the content previus stored or null
   */
  public static Object getContentQuiet(String id)
  {
    try
    {
      return getService().getObject(id).getContents();
    }
    catch(Throwable e)
    {
      return null;
    }
  }

  /**
   * Gets the contento of a cached object given its id (a String).
   * Like getObject() but if the object has expired in
   * the cache or not found simply return null.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @return the content previus stored or null
   */
  public static Object getContentQuiet(String objClass, String id)
  {
    try
    {
      return getService().getObject(objClass, id).getContents();
    }
    catch(Throwable e)
    {
      return null;
    }
  }

  /**
   * Adds an object to the cache.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @param o The object to add to the cache.
   */
  public static void addObject(String objClass, String id, CachedObject o)
  {
    getService().addObject(objClass, id, o);
  }

  /**
   * Adds a content to the cache.
   * The content will be wrapped in a CachedObject instance.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @param content The content to add to the cache.
   */
  public static void addContent(String objClass, String id, Object content)
  {
    getService().addObject(objClass, id, new CachedObject(content));
  }

  /**
   * Removes an object from the cache.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   */
  public static void removeObject(String objClass, String id)
  {
    getService().removeObject(objClass, id);
  }

  /**
   * Imposta un limite (numero di oggetti) per una classe di oggetti.
   * Per default le classi hanno una dimensione illimitata.
   * @param objClass classe degli oggetti
   * @param limit
   */
  public static void addLimit(String objClass, int limit)
  {
    getService().setLimit(objClass, limit);
  }

  /**
   * Imposta un limite (numero di oggetti) per una classe di oggetti.
   * Per default le classi hanno una dimensione illimitata.
   * @param objClass classe degli oggetti
   * @param limit
   */
  public static void setLimit(String objClass, int limit)
  {
    getService().setLimit(objClass, limit);
  }

  /**
   * Ritorna il limite associato alla classe.
   * @param objClass classe degli oggetti
   * @return numero massimo di oggetti (-1 = non limitato)
   */
  public static int getLimit(String objClass)
  {
    return getService().getLimit(objClass);
  }

  /**
   * Imposta il comportamento della classe all'evento di flush.
   * Il comportamento influenza solo la chiamata a flushCache().
   * La chiamata esplicita a flushCache(nome classe) non è sensibile a questa impostazione.
   * @param objClass classe degli oggetti
   * @param flushPermitted se vero la classe verrà svuotata
   */
  public static void setFlushPermitted(String objClass, boolean flushPermitted)
  {
    getService().setFlushPermitted(objClass, flushPermitted);
  }

  /**
   * Ritorna il comportamento della classe all'evento di flush.
   * Il comportamento influenza solo la chiamata a flushCache().
   * La chiamata esplicita a flushCache(nome classe) non è sensibile a questa impostazione.
   * @param objClass classe degli oggetti
   * @return se vero la classe verrà svuotata
   */
  public static boolean isFlushPermitted(String objClass)
  {
    return getService().isFlushPermitted(objClass);
  }

  /**
   * Ritorna il numero di oggetti di una determinata classe
   * attualmente presenti nella cache.
   * @param objClass classe degli oggetti
   * @return
   */
  public static int getNumberOfObjects(String objClass)
  {
    return getService().getNumberOfObjects(objClass);
  }

  /**
   * Ritorna un enumeratore ai nomi di classe
   * contenuti nella cache.
   * @return
   */
  public static Iterator<String> classNames()
  {
    return getService().classNames();
  }

  /**
   * Enumera il contenuto della cache.
   * @param className classe di oggetti di cui si vuole l'enumerazione (null = GENERIC_OBJ_CLASS)
   * @return
   */
  public static Iterator<CachedObject> cachedObjects(String className)
  {
    return getService().cachedObjects(className);
  }

  /**
   * Esegue il dump del contenuto della cache sul PrintWriter indicato.
   * Per ogni ogggetto viene stampata la chiave e il risultato della funzione toString()
   * @param objClass classe degli oggetti
   * @param out
   * @throws Exception
   */
  public static void dumpCache(String objClass, PrintWriter out)
     throws Exception
  {
    getService().dumpCache(objClass, out);
  }

  /**
   * Gets a cached object given its id (a String).
   * Usa la classe di default 'GENERIC'.
   *
   * @param id The String id for the object.
   * @return A CachedObject.
   * @exception ObjectExpiredException, if the object has expired in
   * the cache or not found.
   */
  public static CachedObject getObject(String id)
     throws ObjectExpiredException
  {
    return getService().getObject(id);
  }

  /**
   * Adds an object to the cache.
   * Usa la classe di default 'GENERIC'.
   *
   * @param id The String id for the object.
   * @param o The object to add to the cache.
   */
  public static void addObject(String id, CachedObject o)
  {
    getService().addObject(id, o);
  }

  /**
   * Removes an object from the cache.
   * Usa la classe di default 'GENERIC'.
   *
   * @param id The String id for the object.
   */
  public static void removeObject(String id)
  {
    getService().removeObject(id);
  }

  /**
   * Returns the number of objects in the cache.
   * @return int The current number of objects in the cache.
   */
  public static int getNumberOfObjects()
  {
    return getService().getNumberOfObjects();
  }

  /**
   * Flush the cache of all objects.
   */
  public static void flushCache()
  {
    getService().flushCache();
  }

  /**
   * Elimina tutti gli oggetti della cache della classe specificata.
   * @param objClass classe degli oggetti
   */
  public static void flushCache(String objClass)
  {
    getService().flushCache(objClass);
  }

  /**
   * Returns the current size of the cache.
   * ATTENZIONE: questa funzione è molto onerosa in termini di calcolo; utilizzare con cautela.
   *
   * @return int representing current cache size in number of bytes
   * @throws java.io.IOException
   */
  public static int getCacheSize()
     throws IOException
  {
    return getService().getCacheSize();
  }

  /**
   * Rimuove tutti gli oggetti la cui chiave inizia con una stringa.
   * @param objClass classe della cache
   * @param idLeftPart stringa iniziale degli id da rimuovere
   */
  public static void removeAllObjects(String objClass, String idLeftPart)
  {
    getService().removeAllObjects(objClass, idLeftPart);
  }

  /**
   * Rimuove tutti gli oggetti applicando funzione di test.
   * @param objClass classe della cache
   * @param test funzione di test per la rimozione
   */
  public static void removeAllObjects(String objClass, CoreCacheServices.testRemoveInterface test)
  {
    getService().removeAllObjects(objClass, test);
  }

  /**
   * Rimuove tutti gli oggetti della classe specificata.
   * @param objClass classe della cache
   */
  public static void removeAllObjects(String objClass)
  {
    getService().removeAllObjects(objClass, (key, value) -> Boolean.TRUE);
  }

  /**
   * Ritorna un file nell'area di cache.
   * @param ticket identificatore del file
   * @return file temporaneo nell'area cache
   */
  public static File getWorkCacheFile(String ticket)
  {
    return MDL.getWorkCacheFile(ticket);
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * @param <R> tipo di valore ritornato
   * @param key chiave da utilizzare per recupero/memorizzazione
   * @param fun funzione per produrre il risultato da inserire in cache
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <R> R fastEntry(String key, Callable<R> fun)
     throws Exception
  {
    try
    {
      return (R) getService().getObject(key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.call();
      getService().addObject(key, new CachedObject(toStore));
      return toStore;
    }
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * La chiave viene ottenuta da keyPart+'/'+value.
   * @param <T> tipo di parametro per ottenere il valore
   * @param <R> tipo di valore ritornato
   * @param keyPart chiave da utilizzare per recupero/memorizzazione (parte della chiave)
   * @param value valore passato alla funzione lambda (può essere null) (altra parte della chiave)
   * @param fun funzione per produrre il risultato da inserire in cache
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <T, R> R fastEntry(String keyPart, T value, FunctionTrowException<T, R> fun)
     throws Exception
  {
    String key = keyPart + "/" + SU.okStr(value);

    try
    {
      return (R) getService().getObject(key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.apply(value);
      getService().addObject(key, new CachedObject(toStore));
      return toStore;
    }
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * La chiave viene ottenuta da keyPart+'/'+value.
   * @param <T> tipo di parametro per ottenere il valore
   * @param <R> tipo di valore ritornato
   * @param objClass classe degli oggetti
   * @param keyPart chiave da utilizzare per recupero/memorizzazione (parte della chiave)
   * @param value valore passato alla funzione lambda (può essere null) (altra parte della chiave)
   * @param fun funzione per produrre il risultato da inserire in cache
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <T, R> R fastEntry(String objClass, String keyPart, T value, FunctionTrowException<T, R> fun)
     throws Exception
  {
    String key = keyPart + "/" + SU.okStr(value);

    try
    {
      return (R) getService().getObject(objClass, key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.apply(value);
      getService().addObject(objClass, key, new CachedObject(toStore));
      return toStore;
    }
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * @param <R> tipo di valore ritornato
   * @param owner proprietario dell'oggetto (viene utilizzato per generare la chiave)
   * @param simpleKey chiave da utilizzare per recupero/memorizzazione (viene aggiunta a owner)
   * @param fun funzione per produrre il risultato da inserire in cache
   * @param args eventuali argomenti che influiscono sulla determinazione della chiave
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <R> R fastEntry(Object owner, String simpleKey, Callable<R> fun, Object... args)
     throws Exception
  {
    String key = createKey(owner, simpleKey, args);

    try
    {
      return (R) getService().getObject(key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.call();
      getService().addObject(key, new CachedObject(toStore));
      return toStore;
    }
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * @param <R> tipo di valore ritornato
   * @param owner proprietario dell'oggetto (viene utilizzato per generare la chiave)
   * @param objClass classe degli oggetti
   * @param simpleKey chiave da utilizzare per recupero/memorizzazione (viene aggiunta a owner)
   * @param fun funzione per produrre il risultato da inserire in cache
   * @param args eventuali argomenti che influiscono sulla determinazione della chiave
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <R> R fastEntry(Object owner, String objClass, String simpleKey, Callable<R> fun, Object... args)
     throws Exception
  {
    String key = createKey(owner, simpleKey, args);

    try
    {
      return (R) getService().getObject(objClass, key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.call();
      getService().addObject(objClass, key, new CachedObject(toStore));
      return toStore;
    }
  }

  /**
   * Genera chiave per la cache.
   * La chiave contiene il nome della classe dell'owner, la simplekey,
   * tutti parametri di args diversi da null rappresentati come stringa.
   * @param owner proprietario dell'oggetto
   * @param simpleKey chiave da utilizzare per recupero/memorizzazione
   * @param args eventuali argomenti che influiscono sulla determinazione della chiave
   * @return stringa da utilizzare come chiave per la cache
   */
  public static String createKey(Object owner, String simpleKey, Object[] args)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(owner.getClass().getName()).append('/');
    sb.append(simpleKey).append('/');

    if(args != null && args.length > 0)
    {
      for(Object arg : args)
      {
        if(arg != null)
          sb.append(arg.toString());

        sb.append('/');
      }
    }

    sb.append("compositeKey");
    return sb.toString();
  }

  /**
   * Refresh di una entry della cache.
   * Imposta la entry indicata come appena inserita.
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public static boolean refreshObject(String id)
  {
    return getService().refreshObject(id);
  }

  /**
   * Refresh di una entry della cache.
   * Imposta la entry indicata come appena inserita.
   * @param objClass classe degli oggetti
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public static boolean refreshObject(String objClass, String id)
  {
    return getService().refreshObject(objClass, id);
  }

  /**
   * Verifica esisitenza in cache.
   * Questa funzione non estrae l'oggetto, quindi non altera
   * il suo timestamp ne attiva altra funzione.
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public static boolean containsObject(String id)
  {
    return getService().containsObject(id);
  }

  /**
   * Verifica esisitenza in cache.
   * Questa funzione non estrae l'oggetto, quindi non altera
   * il suo timestamp ne attiva altra funzione.
   * @param objClass classe degli oggetti
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public static boolean containsObject(String objClass, String id)
  {
    return getService().containsObject(objClass, id);
  }

  /**
   * Abbassamento messaggi di log/allarmi.
   * Verifica se il codice indicato è già stato segnalato in log.
   * @param codice codice da verificare
   * @return vero se già loggato o generato allarme
   */
  public static boolean isLogSignaled(String codice)
  {
    return getSignaled().contains(codice);
  }

  /**
   * Abbassamento messaggi di log/allarmi.
   * Segnala il codice come inviato a log/allarmi.
   * @param codice codice da segnalare
   */
  public static void addLogSignaled(String codice)
  {
    getSignaled().add(codice);
  }

  public static final String CACHE_SIGNALED = "CACHE_SIGNALED_ENTRY";

  public static Set<String> getSignaled()
  {
    try
    {
      return (Set<String>) CACHE.getObject(CACHE_SIGNALED).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      HashSet<String> set = new HashSet<>();
      CACHE.addObject(CACHE_SIGNALED, new CachedObject(set, CoreConst.ONE_DAY_MILLIS));
      return set;
    }
  }

  /**
   * Aggiunta e recupero veloce di una entry di cache.
   * La chiave viene utilizzata per recupere dalla cache il risultato.
   * Se non presente la funzione lambda viene chiamata per produrre il risultato da inserire.
   * Il risultato ottenuto viene inserito nella cache prima di essere ritornato.
   * Il tempo di permanenza nella cache è quello di default.
   * @param <R> tipo di valore ritornato
   * @param key chiave da utilizzare per recupero/memorizzazione
   * @param expires tempo di permanenza in cache (millisecondi)
   * @param fun funzione per produrre il risultato da inserire in cache
   * @return risultato prodotto o recuperato dalla cache
   * @throws Exception
   */
  public static <R> R fastEntryExpires(String key, long expires, Callable<R> fun)
     throws Exception
  {
    try
    {
      return (R) getService().getObject(key).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      R toStore = fun.call();
      getService().addObject(key, new CachedObject(toStore, expires));
      return toStore;
    }
  }
}
