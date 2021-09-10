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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.rigel2.db.torque.TableMapHelper;
import org.sirio2.utils.SU;

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
public class TableRelationCache3<T extends Persistent, O extends Persistent> extends ArrayList<T>
{
  private Map<ObjectKey, Persistent> mapValues = new HashMap<ObjectKey, Persistent>();
  private String tableName;

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param ignoreTableName tabelle da ignorare nella join (può essere null)
   * @param getLinkM metodo da applicari su lsDettails per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache3(Class cls, Collection<O> lsDettails, Collection<String> ignoreTableName, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(O obj : lsDettails)
    {
      primaryKeys.add((Integer) getLinkM.invoke(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, primaryKeys, ignoreTableName, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param ignoreTableName tabelle da ignorare nella join (può essere null)
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del detail
   * da cercare poi nel master; è la chiave esterna del detail che corrispone alla primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache3(Class cls, Collection<O> lsDettails, Collection<String> ignoreTableName, Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(O obj : lsDettails)
    {
      primaryKeys.add(fnMap.apply(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(cls, primaryKeys, ignoreTableName, con);
  }

  protected void loadDataFromDetail(Class cls, Collection<Integer> primaryKeys, Collection<String> ignoreTableName, Connection con)
     throws Exception
  {
    if(!cls.getName().endsWith("Peer"))
      throw new RuntimeException("Deve essere un oggetto Peer.");

    Method getTableMapM = cls.getMethod("getTableMap");
    TableMap tm = (TableMap) getTableMapM.invoke(null);
    tableName = tm.getName();

    TableMapHelper tmh = new TableMapHelper(tm);

    if(tmh.getNumColumnsPrimaryKeys() != 1)
      throw new RuntimeException(String.format("La tabella %s deve avere una colonna primary key.", tableName));

    Criteria c = new Criteria();
    ColumnMap colPrimary = tmh.getFirstPrimaryKey();
    c.andIn(tableName + "." + colPrimary.getColumnName(), primaryKeys.toArray());

    // recupera tutti i record collegati attraverso il metodo
    // public static List<CodSesso> doSelectJoinAllForBeans(Criteria criteria, Collection<String> ignoreTableName, Connection conn)
    Method getRecords = cls.getMethod("doSelectJoinAllForBeans", Criteria.class, Collection.class, Connection.class);
    List lsValues = (List) getRecords.invoke(null, c, ignoreTableName, con);
    loadData(lsValues);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param ignoreTableName tabelle da ignorare nella join (può essere null)
   * @param getLinkM metodo da applicari su lsMasters per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache3(Class cls, String nomeCampo, Collection<O> lsMasters, Collection<String> ignoreTableName, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(O obj : lsMasters)
    {
      primaryKeys.add((Integer) getLinkM.invoke(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, ignoreTableName, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param ignoreTableName tabelle da ignorare nella join (può essere null)
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del master
   * da cercare poi nel detail; in genere è la primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache3(Class cls, String nomeCampo, Collection<O> lsMasters, Collection<String> ignoreTableName,
     Function<O, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<Integer>();
    for(O obj : lsMasters)
    {
      primaryKeys.add(fnMap.apply(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, ignoreTableName, cls, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Questo costruttore è utile quando ho già selezionato le primary key del master
   * da cercare negli oggetti detail. Questo è conveniente se devo caricare più detail
   * da un unico insieme di master.
   * @param cls classe del peer dell'oggetto di ritorno (ES: se AnAnagrafiche sarà AnAnagrafichePeer.class)
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master (nomeTabella.nomeCampo)
   * @param primaryKeysMasters primary key del master
   * @param ignoreTableName tabelle da ignorare nella join (può essere null)
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationCache3(Class cls, String nomeCampo, Collection<Integer> primaryKeysMasters, Collection<String> ignoreTableName, Connection con)
     throws Exception
  {
    if(!primaryKeysMasters.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeysMasters, ignoreTableName, cls, con);
  }

  protected void loadDataFromMaster(String nomeCampo, Collection<Integer> primaryKeys, Collection<String> ignoreTableName, Class cls, Connection con)
     throws Exception
  {
    if(!cls.getName().endsWith("Peer"))
      throw new RuntimeException("Deve essere un oggetto Peer.");

    Criteria c = new Criteria();
    c.andIn(nomeCampo, primaryKeys.toArray());
    Method getRecords = cls.getMethod("doSelectJoinAllForBeans", Criteria.class, Collection.class, Connection.class);
    List lsValues = (List) getRecords.invoke(null, c, ignoreTableName, con);
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
