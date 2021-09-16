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
import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.sirio5.utils.SU;

/**
 * Cache per relazioni fra tabelle.
 * Se ho un array di peer (ES: List[AcAccettazioni]) consente di caricare
 * in una unica query tutti i peer collegati (ES: List[AnAnagrafiche]) e
 * quindi di recuparli attraverso le funzioni findByPrimaryKey.
 * NON utilizza la GlobalCache.
 *
 * @author Nicola De Nisco
 * @param <T>
 */
public class TableRelationCache<T extends Persistent> extends ArrayList<T>
{
  private Map<ObjectKey, Persistent> mapValues = new HashMap<ObjectKey, Persistent>();

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsObj lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsObj per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache(Class cls, List lsObj, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(int i = 0; i < lsObj.size(); i++)
    {
      Persistent obj = (Persistent) lsObj.get(i);
      primaryKeys.add((Integer) getLinkM.invoke(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, primaryKeys, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsObj lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del detail
   * da cercare poi nel master; è la chiave esterna del detail che corrispone alla primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache(Class cls, List lsObj, Function<Persistent, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(int i = 0; i < lsObj.size(); i++)
    {
      Persistent obj = (Persistent) lsObj.get(i);
      primaryKeys.add(fnMap.apply(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, primaryKeys, con);
  }

  protected void loadDataFromDetail(Class cls, HashSet<Integer> primaryKeys, Connection con)
     throws Exception
  {
    // recupera tutti i record collegati attraverso il metodo
    Method getRecords = cls.getMethod("retrieveByPKs", Collection.class, Connection.class);
    List lsValues = (List) getRecords.invoke(null, primaryKeys, con);
    loadData(lsValues);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsObj lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsObj per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache(Class cls, String nomeCampo, List lsObj, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(int i = 0; i < lsObj.size(); i++)
    {
      Persistent obj = (Persistent) lsObj.get(i);
      primaryKeys.add((Integer) getLinkM.invoke(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsObj lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del master
   * da cercare poi nel detail; in genere è la primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache(Class cls, String nomeCampo, List lsObj,
     Function<Persistent, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(int i = 0; i < lsObj.size(); i++)
    {
      Persistent obj = (Persistent) lsObj.get(i);
      primaryKeys.add(fnMap.apply(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, cls, con);
  }

  protected void loadDataFromMaster(String nomeCampo, HashSet<Integer> primaryKeys, Class cls, Connection con)
     throws Exception
  {
    Criteria c = new Criteria();
    c.andIn(nomeCampo, primaryKeys);
    Method doSelect = cls.getMethod("doSelect", Criteria.class, Connection.class);
    List lsValues = (List) doSelect.invoke(null, c, con);
    loadData(lsValues);
  }

  protected void loadData(List lsValues)
     throws Exception
  {
    addAll(lsValues);

    for(int i = 0; i < lsValues.size(); i++)
    {
      Persistent val = (Persistent) lsValues.get(i);
      mapValues.put(val.getPrimaryKey(), val);
    }
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
   * @param toSearch chiave primaria
   * @return oggetto oppure null
   * @throws Exception
   */
  public T findByPrimaryKey(ObjectKey toSearch)
     throws Exception
  {
    return (T) mapValues.get(toSearch);
  }

  public List<T> extractByFieldValue(String fieldName, Object valueFilter, boolean ingoreDeleted)
     throws Exception
  {
    ArrayList<T> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ingoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }
}
