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
import java.util.stream.Collectors;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.SimpleKey;
import org.commonlib5.lambda.PredicateThrowException;
import org.sirio5.utils.SU;

/**
 * Cache per relazioni fra tabelle.
 * Se ho un array di peer (ES: List[AcAccettazioni]) consente di caricare
 * in una unica query tutti i peer collegati (ES: List[AnAnagrafiche]) e
 * quindi di recuparli attraverso le funzioni findByPrimaryKey.
 * NON utilizza la GlobalCache.
 *
 * @author Nicola De Nisco
 * @param <T> Tipo di oggetti recuperati
 * @param <O> Tipo di oggetti origine
 */
public class TableRelationCache4<T extends Persistent, O extends Persistent> extends ArrayList<T>
{
  private final Class targetPeerClass;
  private final Method getRecords;
  private final Method doSelect;
  private final Map<ObjectKey, Persistent> mapValues = new HashMap<>();

  public TableRelationCache4(Class cls)
  {
    try
    {
      if(!cls.getName().endsWith("Peer"))
        throw new Exception("Deve essere un oggetto Peer.");

      targetPeerClass = cls;
      getRecords = targetPeerClass.getMethod("retrieveByPKs", Collection.class, Connection.class);
      doSelect = targetPeerClass.getMethod("doSelect", Criteria.class, Connection.class);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsDettails per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromDetail(Collection<O> lsDettails, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add((Integer) getLinkM.invoke(obj));

    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!pks.isEmpty())
      loadDataFromPrimaryKeys(pks, con);
  }

  /**
   * Caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsDettails per estrarre la chiave primaria; può contentere sia il nome
   * campo Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure
   * PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromDetail(Collection<O> lsDettails, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());
    getKeysFromNomeCampo(lsDettails, campoLink, primaryKeys);
    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!primaryKeys.isEmpty())
      loadDataFromPrimaryKeys(pks, con);
  }

  /**
   * Legge un set di chiavi primarie da un campo di oggetti collegati.
   * @param campoLink
   * @param lsDettails
   * @param primaryKeys
   */
  protected void getKeysFromNomeCampo(Collection<O> lsDettails, String campoLink, HashSet<Integer> primaryKeys)
  {
    if(campoLink.startsWith("PEER:"))
    {
      campoLink = campoLink.substring(5);
      for(O obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByPeerName(campoLink));
      }
    }
    else
    {
      for(O obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByName(campoLink));
      }
    }
  }

  /**
   * Caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del detail
   * da cercare poi nel master; è la chiave esterna del detail che corrispone alla primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromDetail(Collection<O> lsDettails, Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add(fnMap.apply(obj));

    if(primaryKeys.isEmpty())
      return;

    List<ObjectKey> pks = primaryKeys.stream()
       .map((i) -> SimpleKey.keyFor(i))
       .collect(Collectors.toList());

    if(!pks.isEmpty())
      loadDataFromPrimaryKeys(pks, con);
  }

  public void loadDataFromDetail(Function<O, ObjectKey> fnMap, Collection<O> lsDettails, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<ObjectKey> primaryKeys = new HashSet<>(lsDettails.size());

    for(O obj : lsDettails)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromPrimaryKeys(primaryKeys, con);
  }

  public void loadDataFromPrimaryKeys(Collection<ObjectKey> primaryKeys, Connection con)
     throws Exception
  {
    // recupera tutti i record collegati attraverso il metodo
    List lsValues = (List) getRecords.invoke(null, primaryKeys, con);
    loadData(lsValues);
  }

  /**
   * Caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsMasters per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromMaster(ColumnMap nomeCampo, Collection<O> lsMasters, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(O obj : lsMasters)
      primaryKeys.add((Integer) getLinkM.invoke(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromForeignKey(nomeCampo, primaryKeys, con);
  }

  /**
   * Caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsMasters per estrarre la chiave primaria; può contentere sia il nome campo
   * Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromMaster(ColumnMap nomeCampo, Collection<O> lsMasters, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());
    getKeysFromNomeCampo(lsMasters, campoLink, primaryKeys);
    if(!primaryKeys.isEmpty())
      loadDataFromForeignKey(nomeCampo, primaryKeys, con);
  }

  /**
   * Caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del master
   * da cercare poi nel detail; in genere è la primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public void loadDataFromMaster(ColumnMap nomeCampo, Collection<O> lsMasters,
     Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(O obj : lsMasters)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromForeignKey(nomeCampo, primaryKeys, con);
  }

  /**
   * Caricatore dei dati da master.
   * Questo costruttore è utile quando ho già selezionato le primary key del master
   * da cercare negli oggetti detail. Questo è conveniente se devo caricare più detail
   * da un unico insieme di master.
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master (nomeTabella.nomeCampo)
   * @param primaryKeysMasters primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  protected void loadDataFromForeignKey(ColumnMap nomeCampo, Collection<Integer> primaryKeysMasters, Connection con)
     throws Exception
  {
    if(primaryKeysMasters.isEmpty())
      return;

    Criteria c = new Criteria();
    c.whereIn(nomeCampo, primaryKeysMasters);
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
   */
  public T findByPrimaryKey(int id)
  {
    return findByPrimaryKey(new NumberKey(id));
  }

  /**
   * Recupera il record richiesto.
   * @param toSearch chiave primaria
   * @return oggetto oppure null
   */
  public T findByPrimaryKey(ObjectKey toSearch)
  {
    return (T) mapValues.get(toSearch);
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (StatoRec ge 10)
   * @return lista di oggetti
   */
  public List<T> extractByFieldValue(String fieldName, Object valueFilter, boolean ignoreDeleted)
  {
    ArrayList<T> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ignoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Estrae tutti gli oggetti con un campo pari al valore richiesto.
   * @param fieldName nome del campo
   * @param valueFilter valore del viltro
   * @param ignoreDeleted se vero ignora cancellati (STATO_REC ge 10)
   * @return lista di oggetti
   */
  public List<T> extractByFieldValuePeerName(String fieldName, Object valueFilter, boolean ignoreDeleted)
  {
    ArrayList<T> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ignoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByPeerName(fieldName)))
        rv.add((T) val);
    }

    return rv;
  }

  /**
   * Ritorna il primo oggetto che soddisfa il filtro.
   * @param fn espressione lambda del filtro
   * @return oggetto o null
   * @throws Exception
   */
  public T findFirst(PredicateThrowException<T> fn)
     throws Exception
  {
    for(T t : this)
    {
      if(fn.test(t))
        return t;
    }
    return null;
  }

  /**
   * Ritorna l'ultimo oggetto che soddisfa il filtro.
   * @param fn espressione lambda del filtro
   * @return oggetto o null
   * @throws Exception
   */
  public T findLast(PredicateThrowException<T> fn)
     throws Exception
  {
    T rv = null;
    for(T t : this)
    {
      if(fn.test(t))
        rv = t;
    }
    return rv;
  }
}
