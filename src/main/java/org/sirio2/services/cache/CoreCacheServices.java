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
package org.sirio2.services.cache;

import java.io.PrintWriter;
import java.util.Iterator;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;

/**
 * Cache globale con funzione avanzate.
 * Gli oggetti inseriti possono essere associati a delle
 * classi di appartenenza di cui è possibile fissare il
 * limite massimo di oggetti in cache.
 *
 * @author Nicola De Nisco
 */
public interface CoreCacheServices extends GlobalCacheService
{
  // ATTENZIONE il service name non e' specificato
  // perche' si sovrappone a GlobalCacheService
  public static final int UNLIMITED = -1;
  public static final String GENERIC_OBJ_CLASS = "GENERIC";

  /** semaforo attivo per aggiornamento cache */
  public static final Object semClear = new Object();
  /** semaforo attivo per notifica cancellazione elementi */
  public static final Object semDelete = new Object();
  /** semaforo attivo per notifica refresh elementi */
  public static final Object semUpdate = new Object();

  public interface testRemoveInterface
  {
    /**
     * Ritorna vero se l'entry può essere rimossa.
     * @param key chiave da testare
     * @param value valore associato alla chiave
     * @return vero se può essere rimosso
     */
    boolean testForRemove(String key, CachedObject value);
  }

  /**
   * Gets a cached object given its id (a String).
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @return A CachedObject.
   * @exception ObjectExpiredException, if the object has expired in
   * the cache.
   */
  public CachedObject getObject(String objClass, String id)
     throws ObjectExpiredException;

  /**
   * Adds an object to the cache.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   * @param o The object to add to the cache.
   */
  public void addObject(String objClass, String id, CachedObject o);

  /**
   * Removes an object from the cache.
   *
   * @param objClass classe degli oggetti
   * @param id The String id for the object.
   */
  public void removeObject(String objClass, String id);

  /**
   * Rimuove tutti gli oggetti la cui chiave inizia con una stringa.
   * @param objClass classe della cache
   * @param idLeftPart stringa iniziale degli id da rimuovere
   */
  public void removeAllObjects(String objClass, String idLeftPart);

  /**
   * Rimuove tutti gli oggetti la cui chiave inizia con una stringa.
   * @param objClass classe della cache
   * @param test funzione di test per la rimozione
   */
  public void removeAllObjects(String objClass, testRemoveInterface test);

  /**
   * Imposta un limite (numero di oggetti) per una classe di oggetti.
   * Per default le classi hanno una dimensione illimitata.
   * @param objClass classe degli oggetti
   * @param limit
   */
  public void setLimit(String objClass, int limit);

  /**
   * Ritorna il limite associato alla classe.
   * @param objClass classe degli oggetti
   * @return numero massimo di oggetti (-1 = non limitato)
   */
  public int getLimit(String objClass);

  /**
   * Imposta il comportamento della classe all'evento di flush.
   * Il comportamento influenza solo la chiamata a flushCache().
   * La chiamata esplicita a flushCache(nome classe) non è sensibile a questa impostazione.
   * @param objClass classe degli oggetti
   * @param flushPermitted se vero la classe verrà svuotata
   */
  public void setFlushPermitted(String objClass, boolean flushPermitted);

  /**
   * Ritorna il comportamento della classe all'evento di flush.
   * Il comportamento influenza solo la chiamata a flushCache().
   * La chiamata esplicita a flushCache(nome classe) non è sensibile a questa impostazione.
   * @param objClass classe degli oggetti
   * @return se vero la classe verrà svuotata
   */
  public boolean isFlushPermitted(String objClass);

  /**
   * Ritorna il numero di oggetti di una determinata classe
   * attualmente presenti nella cache.
   * @param objClass classe degli oggetti
   * @return
   */
  public int getNumberOfObjects(String objClass);

  /**
   * Ritorna un enumeratore ai nomi di classe
   * contenuti nella cache.
   * @return
   */
  public Iterator<String> classNames();

  /**
   * Enumera il contenuto della cache.
   * @param className classe di oggetti di cui si vuole l'enumerazione (null = GENERIC_OBJ_CLASS)
   * @return
   */
  public Iterator<CachedObject> cachedObjects(String className);

  /**
   * Esegue il dump del contenuto della cache sul PrintWriter indicato.
   * Per ogni ogggetto viene stampata la chiave e il risultato della funzione toString()
   * @param objClass classe degli oggetti
   * @param out
   * @throws Exception
   */
  public void dumpCache(String objClass, PrintWriter out)
     throws Exception;

  /**
   * Elimina tutti gli oggetti della cache della classe specificata.
   * @param objClass classe degli oggetti
   */
  public void flushCache(String objClass);

  /**
   * Refresh di una entry della cache.
   * Imposta la entry indicata come appena inserita.
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public boolean refreshObject(String id);

  /**
   * Refresh di una entry della cache.
   * Imposta la entry indicata come appena inserita.
   * @param objClass classe degli oggetti
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public boolean refreshObject(String objClass, String id);

  /**
   * Verifica esisitenza in cache.
   * Questa funzione non estrae l'oggetto, quindi non altera
   * il suo timestamp ne attiva altra funzione.
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public boolean containsObject(String id);

  /**
   * Verifica esisitenza in cache.
   * Questa funzione non estrae l'oggetto, quindi non altera
   * il suo timestamp ne attiva altra funzione.
   * @param objClass classe degli oggetti
   * @param id itentificatore entry
   * @return vero se la entry è presente nella cache
   */
  public boolean containsObject(String objClass, String id);
}
