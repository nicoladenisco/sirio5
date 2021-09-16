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

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.collections.IteratorUtils;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.StringKey;
import org.sirio5.utils.SU;

/**
 * Cache generica di oggetti Peer.
 * Carica una intera tabella nella cache offrendo
 * i metodi per accedere ai singoli record.
 * Da utilizzare solo per tabelle con pochi record (COD_..., SYS_...).
 * Utilizza la GlobalCache per memorizzare i dati.
 *
 * @author Nicola De Nisco
 * @param <T>
 */
public class TableCache<T extends ColumnAccessByName> implements Iterable<T>
{
  private Class cls;
  private String tableName;
  private static final String TABLE_CACHE_CLASS = "TableCache";

  /**
   * Costruisce selettore dei dati.
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   */
  public TableCache(Class cls)
  {
    this.cls = cls;
  }

  public String getTableName()
     throws Exception
  {
    if(tableName == null)
    {
      if(!cls.getName().endsWith("Peer"))
        throw new Exception("Deve essere un oggetto Peer.");

      Method getTableMapM = cls.getMethod("getTableMap");
      TableMap tm = (TableMap) getTableMapM.invoke(null);
      if(tm == null)
        throw new Exception("Layer torque non inizializzato (definizione tabella non trovata).");

      tableName = tm.getName();
    }
    return tableName;
  }

  /**
   * Recupera il record richiesto.
   * @param id chiave primaria
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByPrimaryKey(int id)
     throws Exception
  {
    return findByPrimaryKey(new NumberKey(id));
  }

  /**
   * Recupera il record richiesto.
   * @param id chiave primaria
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByPrimaryKey(String id)
     throws Exception
  {
    return findByPrimaryKey(new StringKey(id));
  }

  /**
   * Recupera il record richiesto.
   * @param toSearch chiave primaria
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByPrimaryKey(ObjectKey toSearch)
     throws Exception
  {
    TableCacheData tc = getFromCache();
    return (T) tc.mapValues.get(toSearch);
  }

  /**
   * Recupera il record per codice.
   * Il valore è valido solo se esiste un campo 'CODICE' sulla tabella.
   * @param codice codice associato all'oggetto
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByCodice(String codice)
     throws Exception
  {
    return findByField("Codice", codice, true);
  }

  /**
   * Recupera record usando un campo qualsiasi come filtro.
   * Se la tabella contiene il campo indicato ritorna il primo record con
   * il valore del campo match del filtro (vedi getByName dell'oggetto Peer).
   * @param fieldName nome campo per il filtro
   * @param valueFilter valore del filtro
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByField(String fieldName, Object valueFilter, boolean ignoreDeleted)
     throws Exception
  {
    TableCacheData tc = getFromCache();
    for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();
      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        return (T) val;
    }

    return null;
  }

  /**
   * Recupera record usando un campo qualsiasi come filtro.
   * Se la tabella contiene il campo indicato ritorna il primo record con
   * il valore del campo match del filtro (vedi getByName dell'oggetto Peer).
   * @param peerName nome campo peer (nometabella.nomecampo) per il filtro
   * @param valueFilter valore del filtro
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByPeername(String peerName, Object valueFilter, boolean ignoreDeleted)
     throws Exception
  {
    TableCacheData tc = getFromCache();
    for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();
      if(SU.isEqu(valueFilter, val.getByPeerName(peerName)))
        return (T) val;
    }

    return null;
  }

  /**
   * Recupera iteratore sui dati in cache.
   * @return iteratore non modificabile
   */
  @Override
  public Iterator<T> iterator()
  {
    return getIterator(false);
  }

  public Iterator getIterator(boolean removeDeleted)
  {
    try
    {
      TableCacheData tc = getFromCache();
      return IteratorUtils.unmodifiableIterator(tc.getIterator(removeDeleted));
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public Stream<T> stream()
     throws Exception
  {
    TableCacheData tc = getFromCache();
    return tc.lsValues.stream();
  }

  public Stream<T> parallelStream()
     throws Exception
  {
    TableCacheData tc = getFromCache();
    return tc.lsValues.parallelStream();
  }

  /**
   * Recupera contenuto della cache.
   * @return lista non modificabile
   * @throws Exception
   */
  public List<T> getList()
     throws Exception
  {
    return getList(false);
  }

  /**
   * Recupera contenuto della cache.
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @throws Exception
   * @return lista non modificabile
   */
  public List<T> getList(boolean ignoreDeleted)
     throws Exception
  {
    TableCacheData tc = getFromCache();

    if(ignoreDeleted)
      return Collections.unmodifiableList(tc.lsValuesUndeleted);

    return Collections.unmodifiableList(tc.lsValues);
  }

  /**
   * Lista filtrata per valore della chiave primaria.
   * @param filtro elenco delle chiavi primarie ammesse
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @return lista di elementi
   * @throws Exception
   */
  public List<T> getFilteredListInt(Collection<Integer> filtro, boolean ignoreDeleted)
     throws Exception
  {
    if(filtro == null || filtro.isEmpty())
    {
      return getList(ignoreDeleted);
    }
    else
    {
      ArrayList<T> rv = new ArrayList<>();
      TableCacheData tc = getFromCache();

      for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
      {
        Persistent val = (Persistent) itr.next();

        int key = ((NumberKey) val.getPrimaryKey()).intValue();

        if(filtro.contains(key))
          rv.add((T) val);
      }

      return rv;
    }
  }

  /**
   * Lista filtrata per valore della chiave primaria.
   * @param filtro elenco delle chiavi primarie ammesse
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @return lista di elementi
   * @throws Exception
   */
  public List<T> getFilteredList(Collection<ObjectKey> filtro, boolean ignoreDeleted)
     throws Exception
  {
    if(filtro == null || filtro.isEmpty())
    {
      return getList(ignoreDeleted);
    }
    else
    {
      ArrayList<T> rv = new ArrayList<>();
      TableCacheData tc = getFromCache();

      for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
      {
        Persistent val = (Persistent) itr.next();

        if(filtro.contains(val.getPrimaryKey()))
          rv.add((T) val);
      }

      return rv;
    }
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo 'Codice', 'Descrizione', ecc.
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (StatoRec ge 10)
   * @return lista di oggetti
   * @throws java.lang.Exception
   */
  public List<T> extractByFieldValue(String fieldName, Object valueFilter, boolean ignoreDeleted)
     throws Exception
  {
    ArrayList<T> rv = new ArrayList<>();
    TableCacheData tc = getFromCache();

    for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo 'CODICE', 'DESCRIZIONE', 'ID_VALORE'
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (STATO_REC ge 10)
   * @return lista di oggetti
   * @throws java.lang.Exception
   */
  public List<T> extractByFieldValuePeerName(String fieldName, Object valueFilter, boolean ignoreDeleted)
     throws Exception
  {
    ArrayList<T> rv = new ArrayList<>();
    TableCacheData tc = getFromCache();

    for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(SU.isEqu(valueFilter, val.getByPeerName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Ritorna numero di records nella tabella.
   * @return numero di records (elementi)
   * @throws Exception
   */
  public int size()
     throws Exception
  {
    TableCacheData tc = getFromCache();
    return tc.lsValues.size();
  }

  /**
   * Ritorna il conteggio degli elementi non cancellati (stato_rec).
   * @return numero di records attivi
   * @throws Exception
   */
  public int sizeNoDeleted()
     throws Exception
  {
    TableCacheData tc = getFromCache();
    return tc.lsValuesUndeleted.size();
  }

  /**
   * Ritorna una lista con tutti i codici.
   * Ignora cancellati logicamente (stato_rec).
   * @return lista di codici
   * @throws Exception
   */
  public List<String> getAllCodici()
     throws Exception
  {
    return getAllCodici(true);
  }

  /**
   * Ritorna una lista con tutti i codici.
   * @param ignoreDeleted se vero ignora cancellati logicamente (stato_rec)
   * @return lista di codici
   * @throws Exception
   */
  public List<String> getAllCodici(boolean ignoreDeleted)
     throws Exception
  {
    ArrayList<String> rv = new ArrayList<>();
    TableCacheData tc = getFromCache();

    for(Iterator itr = tc.getIterator(ignoreDeleted); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      String codice = SU.okStrNull(val.getByName("Codice"));

      if(codice != null)
        rv.add(codice);
    }

    return rv;
  }

  public TableCacheData reload()
     throws Exception
  {
    getTableName();
    TableCacheData data = new TableCacheData();
    data.populateData(cls);
    CACHE.addObject(TABLE_CACHE_CLASS, getTableName(), new CachedObject(data));
    return data;
  }

  private static final Object semaforo = new Object();

  /**
   * Recupera dalla GlobalCache il blocco dati relativo.
   * Se non è presente in cache, oppure l'istanza è scaduta,
   * viene creato un nuovo blocco dati leggendo dal db.
   * @return l'istanza in cache del blocco dati.
   * @throws Exception
   */
  private TableCacheData getFromCache()
     throws Exception
  {
    try
    {
      return (TableCacheData) CACHE.getObject(TABLE_CACHE_CLASS, getTableName()).getContents();
    }
    catch(ObjectExpiredException ei)
    {
      synchronized(semaforo)
      {
        // ritenta un prelievo dalla cache: l'attesa del semaforo poteva consentire ad altri di passare
        TableCacheData rv = (TableCacheData) CACHE.getContentQuiet(TABLE_CACHE_CLASS, getTableName());
        if(rv != null)
          return rv;

        return reload();
      }
    }
  }

  /**
   * Rimuove i record di questa tabella dalla cache.
   * @throws Exception
   */
  public void remove()
     throws Exception
  {
    CACHE.removeObject(TABLE_CACHE_CLASS, getTableName());
  }

  /**
   * Rimuove dalla cache i records delle tabelle indicate.
   * I nomi delle tabelle devono essere identici a quelli ritornati da ...Peer.TABLE_NAME; il case è significativo.
   * @param tableNames elenco di nomi tabelle
   */
  public static void removeByTableName(String... tableNames)
  {
    for(String tm : tableNames)
    {
      CACHE.removeObject(TABLE_CACHE_CLASS, tm);
    }
  }
}
