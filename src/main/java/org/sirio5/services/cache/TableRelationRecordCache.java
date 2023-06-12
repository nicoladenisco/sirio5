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

import com.workingdogs.village.Column;
import com.workingdogs.village.Record;
import com.workingdogs.village.Schema;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.Persistent;
import org.commonlib5.utils.StringOper;
import org.rigel5.SetupHolder;
import org.rigel5.db.sql.QueryBuilder;
import org.sirio5.utils.SU;

/**
 * Cache per relazioni fra tabelle.
 * Se ho un array di peer (ES: List[AcAccettazioni]) consente di caricare
 * in una unica query tutti i peer collegati (ES: List[AnAnagrafiche]) e
 * quindi di recuparli attraverso le funzioni findByPrimaryKey.
 * NON utilizza la GlobalCache.
 *
 * @author Nicola De Nisco
 * @param <T> classe di oggetti Torque da usare come origine dei dati
 */
public class TableRelationRecordCache<T extends Persistent> extends ArrayList<Record>
{
  private final Map<Integer, Record> mapValues = new HashMap<>();

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param lsDetails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsObj per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName, Collection<T> lsDetails, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDetails.size());

    for(T obj : lsDetails)
      primaryKeys.add((Integer) getLinkM.invoke(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(tableName, primaryKeys, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param lsDettails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsMasters per estrarre la chiave primaria; può contentere sia il nome campo
   * Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName,
     Collection<T> lsDettails, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDettails.size());

    if(campoLink.startsWith("PEER:"))
    {
      campoLink = campoLink.substring(5);
      for(T obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByPeerName(campoLink));
      }
    }
    else
    {
      for(T obj : lsDettails)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByName(campoLink));
      }
    }

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(tableName, primaryKeys, con);
  }

  /**
   * Costruttore e caricatore dei dati da detail.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere detail dei master che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param lsDetails lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del detail
   * da cercare poi nel master; è la chiave esterna del detail che corrispone alla primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName,
     Collection<T> lsDetails, Function<T, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutte le chiavi primarie dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsDetails.size());

    for(T obj : lsDetails)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromDetail(tableName, primaryKeys, con);
  }

  protected void loadDataFromDetail(String tableName, HashSet<Integer> primaryKeys, Connection con)
     throws Exception
  {
    Schema ts = Schema.schema(con, tableName);
    List<Column> lsPks = ts.getPrimaryKeys();

    if(lsPks.size() != 1)
      throw new Exception("Target table must have one and only one primary key.");
    Column primaryKey = lsPks.get(0);
    if(!primaryKey.isNumericValue())
      throw new Exception("Target table must have one and only one primary key of numeric type.");

    String pkname = primaryKey.name();
    try ( QueryBuilder qb = SetupHolder.getQueryBuilder())
    {
      qb.setFrom(tableName);
      qb.setWhere(pkname + " IN (" + StringOper.join(primaryKeys.iterator(), ',') + ")");

      List<Record> lsRecs = qb.executeQuery(con, true);
      loadData(lsRecs, pkname);
    }
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param getLinkM metodo da applicari su lsObj per estrarre la chiave primaria
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName, String nomeCampo, Collection<T> lsMasters, Method getLinkM, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(T oo : lsMasters)
    {
      Persistent obj = (Persistent) oo;
      primaryKeys.add((Integer) getLinkM.invoke(obj));
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, tableName, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param campoLink campo degli oggetti in lsMasters per estrarre la chiave primaria; può contentere sia il nome campo
   * Torque oppure il nome del campo tabella (usando il prefisso PEER:): Idaccettazioni oppure PEER:ID_ACCETTAZIONI
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName,
     ColumnMap nomeCampo, Collection<T> lsMasters, String campoLink, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    if(campoLink.startsWith("PEER:"))
    {
      campoLink = campoLink.substring(5);
      for(T obj : lsMasters)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByPeerName(campoLink));
      }
    }
    else
    {
      for(T obj : lsMasters)
      {
        ColumnAccessByName can = (ColumnAccessByName) obj;
        primaryKeys.add((Integer) can.getByName(campoLink));
      }
    }

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo.getColumnName(), primaryKeys, tableName, con);
  }

  /**
   * Costruttore e caricatore dei dati da master.
   * Carica in memoria tutti gli oggetti collegati all'array passato come parametro.
   * Gli oggetti passati devono essere master dei detail che si stanno cercando
   * (ES: da una lista di accettazioni voglio ottenere le corrispondenti anagrafiche).
   * @param tableName nome dalla tabella obbiettivo
   * @param nomeCampo nome del campo sulla tabella detail che collega la tabella master
   * @param lsMasters lista di oggetti da ispezionare alla ricerca di chiavi primarie
   * @param fnMap funzione di collegamento (probabilmente una lambda expression) che ritorna l'id del master
   * da cercare poi nel detail; in genere è la primary key del master
   * @param con eventuale connessione al db (può essere null)
   * @throws Exception
   */
  public TableRelationRecordCache(String tableName, String nomeCampo,
     Collection<T> lsMasters, Function<T, Integer> fnMap, Connection con)
     throws Exception
  {
    // recupera tutti i valori dalla lista oggetti
    HashSet<Integer> primaryKeys = new HashSet<>(lsMasters.size());

    for(T obj : lsMasters)
      primaryKeys.add(fnMap.apply(obj));

    if(!primaryKeys.isEmpty())
      loadDataFromMaster(nomeCampo, primaryKeys, tableName, con);
  }

  protected void loadDataFromMaster(String nomeCampo, HashSet<Integer> primaryKeys, String tableName, Connection con)
     throws Exception
  {
    Schema ts = Schema.schema(con, tableName);
    List<Column> lsPks = ts.getPrimaryKeys();

    if(lsPks.size() != 1)
      throw new Exception("Target table must have one and only one primary key.");
    Column primaryKey = lsPks.get(0);
    if(!primaryKey.isNumericValue())
      throw new Exception("Target table must have one and only one primary key of numeric type.");

    String pkname = primaryKey.name();
    try ( QueryBuilder qb = SetupHolder.getQueryBuilder())
    {
      qb.setFrom(tableName);
      qb.setWhere(nomeCampo + " IN (" + StringOper.join(primaryKeys.iterator(), ',') + ")");

      List<Record> lsRecs = qb.executeQuery(con, true);
      loadData(lsRecs, pkname);
    }
  }

  protected void loadData(List<Record> lsValues, String primaryKey)
     throws Exception
  {
    addAll(lsValues);

    for(Record val : lsValues)
    {
      int pk = val.getValue(primaryKey).asInt();
      mapValues.put(pk, val);
    }
  }

  /**
   * Recupera il record richiesto.
   * @param id chiave primaria
   * @return oggetto oppure null
   * @throws Exception
   */
  public Record findByPrimaryKey(int id)
     throws Exception
  {
    return (Record) mapValues.get(id);
  }

  public List<Record> extractByFieldValue(String fieldName, Object valueFilter, boolean ingoreDeleted)
     throws Exception
  {
    ArrayList<Record> rv = new ArrayList<>();

    for(Iterator itr = iterator(); itr.hasNext();)
    {
      ColumnAccessByName val = (ColumnAccessByName) itr.next();

      if(ingoreDeleted && SU.parse(val.getByName("StatoRec"), 0) >= 10)
        continue;

      if(SU.isEqu(valueFilter, val.getByName(fieldName)))
        rv.add((Record) val);
    }

    return rv;
  }
}
