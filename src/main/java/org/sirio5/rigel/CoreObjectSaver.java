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
package org.sirio5.rigel;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.turbine.util.RunData;
import org.rigel5.db.torque.PeerTransactAgent;
import org.rigel5.glue.PeerObjectSaver;
import org.sirio5.CoreConst;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Classe di supporto per il salvataggio di oggetti Peer.
 * Viene verificata l'esistenza di una serie di campi
 * che vengono opportunamente riempiti con le corrette informazioni.
 * Se l'oggetto che si sta salvando continene il campo ULT_MODIF
 * effettua automaticamente un controllo di sicurezza per eventuali
 * salvataggi concorrenti fra piu' utenti.
 *
 * @author Nicola De Nisco
 * @version 1.0
 */
public class CoreObjectSaver implements PeerObjectSaver
{
  protected Class objectClass, peerClass;
  protected Method setIdAziendaM, setIdApplicativiM, getIdUserM, setIdUserM, setIdUcreaM,
     getStatoRecM, setStatoRecM, getUltModifM, setUltModifM, retrieveByPKM, setCreazioneM,
     getUuidM, setUuidM;
  protected int idAzienda, idApplicativo;
  protected int idUser = 0;
  protected boolean isAdmin = false;
  protected boolean strict = false;
  protected boolean ignoreWl = false;

  /**
   * Costruttore per classi derivate.
   */
  public CoreObjectSaver()
  {
  }

  /**
   * Costruttore.
   * @param objectClass class dell'oggetto peer da salvare
   * @param peerClass class dell'oggetto xxxxxPeer del peer da salvare
   * @throws Exception
   */
  public CoreObjectSaver(Class objectClass, Class peerClass)
     throws Exception
  {
    init(objectClass, peerClass);
  }

  /**
   * Costruttore.
   * @param objectClass class dell'oggetto peer da salvare
   * @param peerClass class dell'oggetto xxxxxPeer del peer da salvare
   * @param data riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreObjectSaver(Class objectClass, Class peerClass, RunData data)
     throws Exception
  {
    init(objectClass, peerClass);

    idUser = SEC.getUserID(data);
    isAdmin = SEC.isAdmin(data);
    ignoreWl = SEC.checkAllPermission(data, "COS_ignoreWriteLevel");
  }

  /**
   * Costruttore.
   * @param objectClass class dell'oggetto peer da salvare
   * @param peerClass class dell'oggetto xxxxxPeer del peer da salvare
   * @param session riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreObjectSaver(Class objectClass, Class peerClass, HttpSession session)
     throws Exception
  {
    init(objectClass, peerClass);

    idUser = SEC.getUserID(session);
    isAdmin = SEC.isAdmin(session);
    ignoreWl = SEC.checkAllPermission(session, "COS_ignoreWriteLevel");
  }

  /**
   * Costruttore.
   * @param data riferimento alla sessione (per i dati sull'utente)
   * @throws Exception
   */
  public CoreObjectSaver(RunData data)
     throws Exception
  {
    idUser = SEC.getUserID(data);
    isAdmin = SEC.isAdmin(data);
    ignoreWl = SEC.checkAllPermission(data, "COS_ignoreWriteLevel");
  }

  @Override
  public synchronized void init(Class objectClass, Class peerClass)
     throws Exception
  {
    this.objectClass = objectClass;
    this.peerClass = peerClass;

    idAzienda = TR.getInt("azienda.id", -1);
    if(idAzienda == -1)
      throw new Exception("Parametro di setup azienda.id non trovato. Modificare TurbineResource.properties.");

    idApplicativo = TR.getInt("applicativo.id", 0);

    setIdAziendaM = getObjectMethod("setIdAzienda", Integer.TYPE);
    setIdApplicativiM = getObjectMethod("setIdapplicativi", Integer.TYPE);
    getIdUserM = getObjectMethod("getIdUser");
    setIdUserM = getObjectMethod("setIdUser", Integer.TYPE);
    setIdUcreaM = getObjectMethod("setIdUcrea", Integer.TYPE);
    getStatoRecM = getObjectMethod("getStatoRec");
    setStatoRecM = getObjectMethod("setStatoRec", Integer.TYPE);
    getUltModifM = getObjectMethod("getUltModif");
    setUltModifM = getObjectMethod("setUltModif", java.util.Date.class);
    setCreazioneM = getObjectMethod("setCreazione", java.util.Date.class);
    retrieveByPKM = getPeerMethod("retrieveByPK", org.apache.torque.om.ObjectKey.class, java.sql.Connection.class);

    getUuidM = getObjectMethod("getUuid");
    setUuidM = getObjectMethod("setUuid", String.class);
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
   * Recupera un metodo dall'oggetto peer usando l'analisi a runtime.
   * @param name nome del metodo da recuperare
   * @param parType parametri accettati dal metodo
   * @return il metodo se esiste altrimenti null
   */
  protected Method getObjectMethod(String name, Class... parType)
  {
    try
    {
      return objectClass.getMethod(name, parType);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Recupera un metodo dall'oggetto xxxxxxPeer usando l'analisi a runtime.
   * @param name nome del metodo da recuperare
   * @param parType parametri accettati dal metodo
   * @return il metodo se esiste altrimenti null
   */
  protected Method getPeerMethod(String name, Class... parType)
  {
    try
    {
      return peerClass.getMethod(name, parType);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Usa i metodi runtime per recuperare un oggetto peer
   * del tipo richiesto in base alla usa chiave primaria.
   * @param pKey chiave primaria dell'oggetto da recuperare
   * @param con connessione al db
   * @return l'oggetto se esiste altrimenti null
   */
  protected Persistent caricaElem(ObjectKey pKey, Connection con)
  {
    try
    {
      if(pKey == null)
        return null;

      // il valore 0 viene considerato come nullo
      // altrimenti prende i record usati per integrita' referenziale
      if(pKey instanceof NumberKey
         && ((NumberKey) (pKey)).longValue() == 0)
        return null;

      return (Persistent) retrieveByPKM.invoke(null, pKey, con);
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
  protected int getIdUser(Persistent obj)
  {
    try
    {
      return ((Integer) (getIdUserM.invoke(obj)));
    }
    catch(Exception ex)
    {
      return 0;
    }
  }

  /**
   * Recupera il valore dello stato rec per un oggetto peer.
   * @param obj l'oggetto di interesse
   * @return il valore di stato_rec (altrimenti 0)
   */
  protected int getStatoRec(Persistent obj)
  {
    try
    {
      return ((Integer) (getStatoRecM.invoke(obj)));
    }
    catch(Exception ex)
    {
      return 0;
    }
  }

  /**
   * Recupera il valore di ultima modifica per un oggetto peer.
   * @param obj l'oggetto di interesse
   * @return il valore di ult_modif (altrimenti null)
   */
  protected Date getUltModif(Persistent obj)
  {
    try
    {
      return (Date) (getUltModifM.invoke(obj));
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Recupera il valore di UUID per un oggetto peer.
   *
   * @param obj l'oggetto di interesse
   * @return il valore di ult_modif (altrimenti null)
   */
  protected String getUuid(Persistent obj)
  {
    try
    {
      return SU.okStrNull(getUuidM.invoke(obj));
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
   * @param obj l'oggetto peer da salvare
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Persistent obj)
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
   * @param obj l'oggetto peer da salvare
   * @param statoRecNew lo stato del record da impostare
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Persistent obj, int statoRecNew)
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
   * @param obj l'oggetto peer da salvare
   * @param statoRecNew lo stato del record da impostare
   * @param writeLevel livello di scrittura posseduto dall'utente
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  public synchronized void salva(Persistent obj, int statoRecNew, int writeLevel)
     throws Exception
  {
    salva(obj, idUser, statoRecNew, writeLevel);
  }

  @Override
  public void salva(Persistent obj, int userID, int statoRecNew, int writeLevel)
     throws Exception
  {
    PeerTransactAgent.execute((con) -> salva(obj, con, userID, statoRecNew, writeLevel));
  }

  /**
   * Salva un oggetto sul database.
   * Come salva() ma con possibilità di specificare la connesione (transazione) SQL.
   * @param obj l'oggetto peer da salvare
   * @param dbCon connessione SQL
   * @param statoRecNew nuovo stato rec da impostare nel record
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  @Override
  public void salva(Persistent obj, Connection dbCon, int statoRecNew)
     throws Exception
  {
    salva(obj, dbCon, statoRecNew, 0);
  }

  /**
   * Salva un oggetto sul database.
   * Come salva() ma con possibilità di specificare la connesione (transazione) SQL.
   * @param obj l'oggetto peer da salvare
   * @param dbCon connessione SQL
   * @param statoRecNew lo stato del record da impostare
   * @param writeLevel livello di scrittura posseduto dall'utente
   * @throws Exception
   * @deprecated usare la funzione salva(..,userID,...) perchè rientrante
   */
  public synchronized void salva(Persistent obj, Connection dbCon, int statoRecNew, int writeLevel)
     throws Exception
  {
    salva(obj, dbCon, idUser, statoRecNew, writeLevel);
  }

  @Override
  public void salva(Persistent obj, Connection dbCon, int userID, int statoRecNew, int writeLevel)
     throws Exception
  {
    boolean adminFlag = userID == 0;

    if(!((getStatoRecM == null && getUltModifM == null) || retrieveByPKM == null))
    {
      // recupero del record dal database
      // attraverso la sua chiave primaria
      ObjectKey pKey = obj.getPrimaryKey();

      if(pKey != null)
      {
        Persistent prev = caricaElem(pKey, dbCon);

        // controlli sullo stato precedente del record
        if(prev != null && writeLevel != -1)
        {
          if(!ignoreWl && getStatoRecM != null)
          {
            int statoRec = getStatoRec(prev) % 10;
            if(statoRec > writeLevel && !adminFlag)
              throw new UnmodificableRecordException(
                 "Table:" + objectClass.getName() + " Key:" + pKey + " WL:" + statoRec + " UL:" + writeLevel); // NOI18N
          }

          if(getUltModifM != null)
          {
            Date objUM = getUltModif(obj);
            Date prevUM = getUltModif(prev);

            if(objUM != null && prevUM != null && !isEquals(objUM, prevUM))
            {
              // il record è stato modificato sul db rispetto a quello che si sta salvando!!

              // se la modalità strict è attiva questo è sufficiente a sollevare l'errore ...
              if(strict || getIdUserM == null)
                throw new ConcurrentDatabaseModificationException(
                   "Table:" + objectClass.getName() + " Key:" + pKey); // NOI18N

              // ... altrimenti controlla che l'utente sia diverso per sollevare l'errore
              int userIDprev = getIdUser(prev);
              if(userID != userIDprev)
                throw new ConcurrentDatabaseModificationException(
                   "Table:" + objectClass.getName() + " Key:" + pKey + " User:" + userIDprev); // NOI18N
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
  protected void saveObject(Persistent obj, Connection dbCon, int userID, int statoRecNew)
     throws Exception
  {
    // se il peer lo richiede imposta il codice azienda
    if(setIdAziendaM != null && obj.isNew())
      setIdAziendaM.invoke(obj, idAzienda);

    // se il peer lo richiede imposta il codice applicativo
    if(setIdApplicativiM != null && obj.isNew())
      setIdApplicativiM.invoke(obj, idApplicativo);

    // se il peer lo richiede imposta statorec
    if(setStatoRecM != null)
      setStatoRecM.invoke(obj, statoRecNew);

    // se il peer lo richiede imposta utente modifica
    if(setIdUserM != null)
      setIdUserM.invoke(obj, userID);

    // se il peer lo richiede imposta ultima modifica
    if(setUltModifM != null)
      setUltModifM.invoke(obj, new Date());

    // se il peer lo richiede imposta data di creazione
    if(setCreazioneM != null && obj.isNew())
      setCreazioneM.invoke(obj, new Date());

    // se il peer lo richiede imposta utente creazione
    if(setIdUcreaM != null && obj.isNew())
      setIdUcreaM.invoke(obj, userID);

    // crea un UUID se necessario
    if(getUuidM != null && setUuidM != null && getUuid(obj) == null)
      setUuidM.invoke(obj, UUID.randomUUID().toString().toUpperCase());

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
  public void clearNewObject(Persistent obj)
     throws Exception
  {
    // se il peer lo richiede imposta il codice azienda
    if(setIdAziendaM != null)
      setIdAziendaM.invoke(obj, 0);

    // se il peer lo richiede imposta il codice applicativo
    if(setIdApplicativiM != null)
      setIdApplicativiM.invoke(obj, 0);

    // se il peer lo richiede imposta statorec
    if(setStatoRecM != null)
      setStatoRecM.invoke(obj, 0);

    // se il peer lo richiede imposta utente modifica
    if(setIdUserM != null)
      setIdUserM.invoke(obj, 0);

    // se il peer lo richiede imposta ultima modifica
    if(setUltModifM != null)
      clearField(obj, setUltModifM);

    // se il peer lo richiede imposta data di creazione
    if(setCreazioneM != null)
      clearField(obj, setCreazioneM);

    // se il peer lo richiede imposta utente creazione
    if(setIdUcreaM != null)
      setIdUcreaM.invoke(obj, 0);

    // crea un UUID se necessario
    if(setUuidM != null)
      clearField(obj, setUuidM);
  }

  protected void clearField(Persistent obj, Method setMet)
     throws Exception
  {
    Object[] args =
    {
      null
    };

    setMet.invoke(obj, args);
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
