/*
 * Copyright (C) 2022 nicola
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
package org.sirio5.rigel;

import com.workingdogs.village.Column;
import com.workingdogs.village.Record;
import com.workingdogs.village.Schema;
import com.workingdogs.village.TableDataSet;
import com.workingdogs.village.Value;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.apache.turbine.util.RunData;
import org.rigel5.db.torque.PeerTransactAgent;
import org.rigel5.exceptions.InvalidObjectException;
import org.rigel5.glue.RecordObjectSaver;
import org.sirio5.CoreConst;
import org.sirio5.services.security.SEC;

/**
 * Classe di supporto per il salvataggio di oggetti Record.
 * Viene verificata l'esistenza di una serie di campi
 * che vengono opportunamente riempiti con le corrette informazioni.
 * Se l'oggetto che si sta salvando continene il campo ULT_MODIF
 * effettua automaticamente un controllo di sicurezza per eventuali
 * salvataggi concorrenti fra piu' utenti.
 *
 * @author Nicola De Nisco
 * @version 1.0
 */
public class CoreRecordObjectSaver implements RecordObjectSaver
{
  protected int idAzienda, idApplicativo;
  protected Column colIdAzienda, colIdApplicativi, colIdUser, colIdUcrea,
     colStatoRec, colUltmodif, colCreazione,
     colUuid;
  protected Schema sc;
  protected int idUser = 0;
  protected boolean isAdmin = false;
  protected boolean strict = false;
  protected boolean ignoreWl = false;

  /**
   * Costruttore per classi derivate.
   */
  public CoreRecordObjectSaver()
  {
  }

  /**
   * Costruttore.
   * @param tableName nome della tabella
   * @param con connessione al db (sola lettura)
   * @throws Exception
   */
  public CoreRecordObjectSaver(String tableName, Connection con)
     throws Exception
  {
    init(tableName, con);
  }

  /**
   * Costruttore.
   * @param tableName nome della tabella
   * @param con connessione al db (sola lettura)
   * @param data riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreRecordObjectSaver(String tableName, Connection con, RunData data)
     throws Exception
  {
    init(tableName, con);

    idUser = SEC.getUserID(data);
    isAdmin = SEC.isAdmin(data);
    ignoreWl = SEC.checkAllPermission(data, "COS_ignoreWriteLevel");
  }

  /**
   * Costruttore.
   * @param tableName nome della tabella
   * @param con connessione al db (sola lettura)
   * @param session riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreRecordObjectSaver(String tableName, Connection con, HttpSession session)
     throws Exception
  {
    init(tableName, con);

    idUser = SEC.getUserID(session);
    isAdmin = SEC.isAdmin(session);
    ignoreWl = SEC.checkAllPermission(session, "COS_ignoreWriteLevel");
  }

  /**
   * Costruttore.
   * @param data riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreRecordObjectSaver(RunData data)
     throws Exception
  {
    idUser = SEC.getUserID(data);
    isAdmin = SEC.isAdmin(data);
    ignoreWl = SEC.checkAllPermission(data, "COS_ignoreWriteLevel");
  }

  @Override
  public void init(String tableName, Connection con)
     throws Exception
  {
    sc = Schema.schema(con, tableName);
    colIdAzienda = sc.findInSchemaIgnoreCaseQuiet("Id_Azienda");
    colIdApplicativi = sc.findInSchemaIgnoreCaseQuiet("Id_Applicativi");
    colIdUser = sc.findInSchemaIgnoreCaseQuiet("Id_User");
    colIdUcrea = sc.findInSchemaIgnoreCaseQuiet("Id_Ucrea");
    colStatoRec = sc.findInSchemaIgnoreCaseQuiet("Stato_Rec");
    colUltmodif = sc.findInSchemaIgnoreCaseQuiet("Ult_modif");
    colCreazione = sc.findInSchemaIgnoreCaseQuiet("Creazione");
    colUuid = sc.findInSchemaIgnoreCaseQuiet("Uuid");
  }

  /**
   * Costruttore.
   * @param session riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreRecordObjectSaver(HttpSession session)
     throws Exception
  {
    idUser = SEC.getUserID(session);
    isAdmin = SEC.isAdmin(session);
    ignoreWl = SEC.checkAllPermission(session, "COS_ignoreWriteLevel");
  }

  /**
   * Imposta le credenziali dell'utente.
   * @param idUser
   * @param isAdmin
   * @throws Exception
   * @deprecated usare le funzioni salva con utente specificato: sono rientranti
   */
  @Override
  public synchronized void setUserInfo(int idUser, boolean isAdmin)
     throws Exception
  {
    this.idUser = idUser;
    this.isAdmin = isAdmin;
  }

  /**
   * Imposta le credenziali dell'utente. Se idUser è uguale a 0
   * viene considerato utente amministratore.
   * @param idUser
   * @throws Exception
   * @deprecated usare le funzioni salva con utente specificato: sono rientranti
   */
  public void setUserInfo(int idUser)
     throws Exception
  {
    setUserInfo(idUser, idUser == 0);
  }

  /**
   * Usa i metodi runtime per recuperare un oggetto record
   * del tipo richiesto in base alla usa chiave primaria.
   * @param pKey chiave primaria dell'oggetto da recuperare
   * @param con connessione al db
   * @return l'oggetto se esiste altrimenti null
   */
  protected Record caricaElem(Map<Column, Value> pKey, Connection con)
  {
    try
    {
      if(pKey == null || pKey.isEmpty())
        return null;

      // il valore 0 viene considerato come nullo
      // altrimenti prende i record usati per integrita' referenziale
      if(pKey.size() == 1)
      {
        for(Map.Entry<Column, Value> entry : pKey.entrySet())
        {
          Column col = entry.getKey();
          Value val = entry.getValue();
          if(col.isNumericValue() && val.asLong() == 0)
            return null;
        }
      }

      try(TableDataSet td = new TableDataSet(con, sc.getFullTableName()))
      {
        td.fetchByPrimaryKeysValues(pKey);
        return td.size() == 0 ? null : td.getRecord(0);
      }
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Recupera ultimo utente che ha modificato il record.
   * @param obj l'oggetto di interesse
   * @return il valore di ID_USER sul record (altrimenti 0)
   */
  protected int getIdUser(Record obj)
  {
    try
    {
      return colIdUser == null ? 0 : obj.getValue(colIdUser.name()).asInt();
    }
    catch(Exception ex)
    {
      return 0;
    }
  }

  /**
   * Recupera il valore dello stato rec per un oggetto record.
   * @param obj l'oggetto di interesse
   * @return il valore di STATO_REC (altrimenti 0)
   */
  protected int getStatoRec(Record obj)
  {
    try
    {
      return colStatoRec == null ? 0 : obj.getValue(colStatoRec.name()).asInt();
    }
    catch(Exception ex)
    {
      return 0;
    }
  }

  /**
   * Recupera il valore di ultima modifica per un oggetto record.
   * @param obj l'oggetto di interesse
   * @return il valore di ULT_MODIF (altrimenti null)
   */
  protected Date getUltModif(Record obj)
  {
    try
    {
      return colUltmodif == null ? null : obj.getValue(colUltmodif.name()).asDate();
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Recupera il valore di UUID per un oggetto record.
   *
   * @param obj l'oggetto di interesse
   * @return il valore di UUID (altrimenti null)
   */
  protected String getUuid(Record obj)
  {
    try
    {
      return colUuid == null ? null : obj.getValue(colUuid.name()).asOkString();
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Salva un oggetto sul database applicando una serie di controlli
   * se stiamo modificando un record gia' esistente:
   * lo stato_rec deve essere 0, altrimenti il record viene considerato
   * non modificabile, la timestamp dell'ultima modifica non deve essere
   * cambiata altrimenti vuol dire che un altro utente ha gia' modificato
   * il record.
   * @param obj l'oggetto record da salvare
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Record obj)
     throws Exception
  {
    salva(obj, 0);
  }

  /**
   * Salva un oggetto sul database.
   * Vengono applicati una serie di controlli se stiamo modificando un record gia' esistente:
   * lo stato_rec deve essere 0, altrimenti il record viene considerato
   * non modificabile, la timestamp dell'ultima modifica non deve essere
   * cambiata altrimenti vuol dire che un altro utente ha gia' modificato
   * il record.
   * @param obj l'oggetto record da salvare
   * @param statoRecNew lo stato del record da impostare
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Record obj, int statoRecNew)
     throws Exception
  {
    salva(obj, statoRecNew, 0);
  }

  /**
   * Salva oggetto sul database.
   * Vengono applicati una serie di controlli se stiamo modificando un record gia' esistente:
   * lo stato_rec deve inferiore a writeLevel, altrimenti il record viene considerato
   * non modificabile, la timestamp dell'ultima modifica non deve essere
   * cambiata altrimenti vuol dire che un altro utente ha gia' modificato
   * il record.
   * Se write level è -1 la scrittura viene forzata ignorando il valore di stato_rec e di ultmodif.
   * @param obj l'oggetto record da salvare
   * @param statoRecNew lo stato del record da impostare
   * @param writeLevel livello di scrittura posseduto dall'utente
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  public synchronized void salva(Record obj, int statoRecNew, int writeLevel)
     throws Exception
  {
    salva(obj, idUser, statoRecNew, writeLevel);
  }

  @Override
  public void salva(Record obj, int userID, int statoRecNew, int writeLevel)
     throws Exception
  {
    PeerTransactAgent.execute((con) -> salva(obj, con, userID, statoRecNew, writeLevel));
  }

  /**
   * Salva un oggetto sul database.
   * Come salva() ma con possibilità di specificare la connesione (transazione) SQL.
   * @param obj l'oggetto record da salvare
   * @param dbCon connessione SQL
   * @param statoRecNew nuovo stato rec da impostare nel record
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Record obj, Connection dbCon, int statoRecNew)
     throws Exception
  {
    salva(obj, dbCon, statoRecNew, 0);
  }

  /**
   * Salva un oggetto sul database.
   * Come salva() ma con possibilità di specificare la connesione (transazione) SQL.
   * @param obj l'oggetto record da salvare
   * @param dbCon connessione SQL
   * @param statoRecNew lo stato del record da impostare
   * @param writeLevel livello di scrittura posseduto dall'utente
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  public synchronized void salva(Record obj, Connection dbCon, int statoRecNew, int writeLevel)
     throws Exception
  {
    salva(obj, dbCon, idUser, statoRecNew, writeLevel);
  }

  @Override
  public void salva(Record obj, Connection dbCon, int userID, int statoRecNew, int writeLevel)
     throws Exception
  {
    boolean adminFlag = userID == 0;
    Schema s = obj.schema();
    if(!s.isSingleTable())
      throw new InvalidObjectException("The record must refer a single table.");

    if(!s.getTableName().equals(sc.getTableName()))
      throw new InvalidObjectException(String.format(
         "The record refer to table '%s' but the saver is initialized for table '%s'.",
         s.getTableName(), sc.getTableName()
      ));

    if(!obj.toBeSavedWithInsert())
    {
      List<Column> lsColPrimary = s.getPrimaryKeys();

      if(!((colStatoRec == null && colUltmodif == null) || lsColPrimary.isEmpty()))
      {
        // recupero del record dal database
        // attraverso la sua chiave primaria
        Map<Column, Value> pKey = obj.getPrimaryKeyValues();

        if(pKey != null && !pKey.isEmpty())
        {
          Record prev = caricaElem(pKey, dbCon);

          // controlli sullo stato precedente del record
          if(prev != null && writeLevel != -1)
          {
            if(!ignoreWl && colStatoRec != null)
            {
              int statoRec = getStatoRec(prev) % 10;
              if(statoRec > writeLevel && !adminFlag)
                throw new UnmodificableRecordException(
                   "Table:" + s.getFullTableName() + " Key:" + pKey + " WL:" + statoRec + " UL:" + writeLevel); // NOI18N
            }

            if(colUltmodif != null)
            {
              Date objUM = getUltModif(obj);
              Date prevUM = getUltModif(prev);

              if(objUM != null && prevUM != null && !isEquals(objUM, prevUM))
              {
                // il record è stato modificato sul db rispetto a quello che si sta salvando!!

                // se la modalità strict è attiva questo è sufficiente a sollevare l'errore ...
                if(strict || colIdUser == null)
                  throw new ConcurrentDatabaseModificationException(
                     "Table:" + s.getFullTableName() + " Key:" + pKey); // NOI18N

                // ... altrimenti controlla che l'utente sia diverso per sollevare l'errore
                int userIDprev = getIdUser(prev);
                if(userID != userIDprev)
                  throw new ConcurrentDatabaseModificationException(
                     "Table:" + s.getFullTableName() + " Key:" + pKey + " User:" + userIDprev); // NOI18N
              }
            }
          }
        }
      }
    }

    saveObject(obj, dbCon, userID, statoRecNew);
  }

  /**
   * Funzione interna di salvataggio dell'oggetto.
   * Se l'oggetto possiede id_azienda, id_user, ult_modif
   * allora li aggiorna di conseguenza.
   * @param obj
   * @param dbCon
   * @param userID
   * @param statoRecNew
   * @throws Exception
   */
  protected void saveObject(Record obj, Connection dbCon, int userID, int statoRecNew)
     throws Exception
  {
    Date now = new Date();

    if(obj.toBeSavedWithInsert())
    {
      // questi valgono solo per un nuovo record (insert) ...
      if(colIdAzienda != null)
        obj.setValue(colIdAzienda.name(), idAzienda);
      if(colIdApplicativi != null)
        obj.setValue(colIdApplicativi.name(), idApplicativo);
      if(colCreazione != null)
        obj.setValueQuiet(colCreazione.name(), now);
      if(colIdUcrea != null)
        obj.setValueQuiet(colIdUcrea.name(), userID);
    }

    // ... per tutti i altri casi (insert/update)
    if(colStatoRec != null)
      obj.setValueQuiet(colStatoRec.name(), statoRecNew);
    if(colIdUser != null)
      obj.setValueQuiet(colIdUser.name(), userID);
    if(colUltmodif != null)
      obj.setValueQuiet(colUltmodif.name(), now);

    // crea un UUID se necessario
    if(colUuid != null && getUuid(obj) == null)
      obj.setValue(colUuid.name(), UUID.randomUUID().toString().toUpperCase());

    obj.save(dbCon);
  }

  /**
   * Pulizia per nuovo oggetto.
   * Se l'oggetto possiede id_azienda, id_user, ult_modif, ecc.
   * questi campi vengono azzerati o posti a null.
   * Al successivo salvataggio verranno impostati correttamente da saveObject().
   * @param obj
   * @throws Exception
   */
  @Override
  public void clearNewObject(Record obj)
     throws Exception
  {
    if(colIdAzienda != null)
      obj.setValue(colIdAzienda.name(), 0);
    if(colIdApplicativi != null)
      obj.setValue(colIdApplicativi.name(), 0);
    if(colCreazione != null)
      obj.setValueNull(colCreazione.name());
    if(colIdUcrea != null)
      obj.setValueQuiet(colIdUcrea.name(), 0);

    if(colStatoRec != null)
      obj.setValueQuiet(colStatoRec.name(), 0);
    if(colIdUser != null)
      obj.setValueQuiet(colIdUser.name(), 0);
    if(colUltmodif != null)
      obj.setValueNull(colUltmodif.name());
  }

  protected boolean isEquals(Date d1, Date d2)
  {
    return Math.abs(d1.getTime() - d2.getTime()) < CoreConst.EPSI_DATE;
  }

  //////////////////////////////////////////////////////////////////////
  public int getIdUser()
  {
    return idUser;
  }

  public boolean isIsAdmin()
  {
    return isAdmin;
  }

  public synchronized void setIdUser(int idUser)
  {
    this.idUser = idUser;
  }

  public synchronized void setIsAdmin(boolean isAdmin)
  {
    this.isAdmin = isAdmin;
  }

  public boolean isStrict()
  {
    return strict;
  }

  public void setStrict(boolean strict)
  {
    this.strict = strict;
  }

}
