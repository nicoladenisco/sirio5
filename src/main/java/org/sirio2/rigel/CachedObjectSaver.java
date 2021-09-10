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
package org.sirio2.rigel;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import org.apache.torque.om.Persistent;
import org.apache.turbine.om.security.User;

/**
 * Salvataggio di oggetti Peer.
 * Mantiene una cache di CoreObjectSaver associata ai rispettivi oggetti Peer.
 *
 * @author Nicola De Nisco
 */
public class CachedObjectSaver
{
  public static final int WRITE_LEVEL_FORCE = -1;
  public static final String WRITE_LEVEL_PERM_STORAGE = "WRITE_LEVEL_PERM_STORAGE";

  private static final HashMap<Class, CoreObjectSaver> htCache = new HashMap<Class, CoreObjectSaver>();

  /**
   * Recupera un oggetto saver adatto al salvataggio del Persistent specificato.
   * L'oggetto viene recuperato dalla cache se esiste, altrimenti viene creato
   * e inserito nella cache.
   * @param toSave oggetto da salvare
   * @return saver adatto e inizalizzato per salvare l'oggetto specificato
   * @throws Exception
   */
  private static CoreObjectSaver getObjectSaver(Persistent toSave)
     throws Exception
  {
    return getObjectSaver(toSave.getClass());
  }

  /**
   * Recupera un oggetto saver adatto al salvataggio del Persistent specificato.
   * L'oggetto viene recuperato dalla cache se esiste, altrimenti viene creato
   * e inserito nella cache.
   * @param peerObjectClass classe dell'oggetto da salvare
   * @return saver adatto e inizalizzato per salvare l'oggetto specificato
   * @throws Exception
   */
  private static CoreObjectSaver getObjectSaver(Class peerObjectClass)
     throws Exception
  {
    CoreObjectSaver ps = htCache.get(peerObjectClass);

    if(ps != null)
      return ps;

    synchronized(htCache)
    {
      if((ps = htCache.get(peerObjectClass)) == null)
      {
        String peerClassName = peerObjectClass.getName() + "Peer";
        ps = new CoreObjectSaver(peerObjectClass, Class.forName(peerClassName));
        htCache.put(peerObjectClass, ps);
      }
      return ps;
    }
  }

  /**
   * Salva l'oggetto peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @param writeLevel livello di permessi in scrittura (0-9)
   * @throws Exception
   */
  public static void save(Connection con, Persistent toSave, int idUser, int newStatoRec, int writeLevel)
     throws Exception
  {
    // NON APPLICABILE: la modifica potrebbe riguardare il cambio di statorec; quindi è necessario procedere
    //if(!toSave.isModified())
    //  return;

    CoreObjectSaver ps = getObjectSaver(toSave);

    if(con == null)
      ps.salva(toSave, idUser, newStatoRec, writeLevel);
    else
      ps.salva(toSave, con, idUser, newStatoRec, writeLevel);
  }

  /**
   * Salva l'oggetto peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param user informazioni sull'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void save(Connection con, Persistent toSave, User user, int newStatoRec)
     throws Exception
  {
    save(con, toSave, (int) user.getId(), newStatoRec, (int) user.getPerm(WRITE_LEVEL_PERM_STORAGE, 0));
  }

  /**
   * Salva l'oggetto peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il valore di statorec viene ignorato.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveForce(Connection con, Persistent toSave, int idUser, int newStatoRec)
     throws Exception
  {
    save(con, toSave, idUser, newStatoRec, WRITE_LEVEL_FORCE);
  }

  /**
   * Salva l'oggetto peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il valore di statorec viene ignorato.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param user informazioni sull'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveForce(Connection con, Persistent toSave, User user, int newStatoRec)
     throws Exception
  {
    save(con, toSave, (int) user.getId(), newStatoRec, WRITE_LEVEL_FORCE);
  }

  /**
   * Salva array di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @param writeLevel livello di permessi in scrittura (0-9)
   * @throws Exception
   */
  public static void saveArray(Connection con, Object[] toSave, int idUser, int newStatoRec, int writeLevel)
     throws Exception
  {
    if(toSave == null || toSave.length == 0)
      return;

    saveCollection(con, Arrays.asList(toSave), idUser, newStatoRec, writeLevel);
  }

  /**
   * Salva array di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param user informazioni sull'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveArray(Connection con, Object[] toSave, User user, int newStatoRec)
     throws Exception
  {
    saveArray(con, toSave, (int) user.getId(), newStatoRec, (int) user.getPerm(WRITE_LEVEL_PERM_STORAGE, 0));
  }

  /**
   * Salva array di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il valore di statorec viene ignorato.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveForceArray(Connection con, Object[] toSave, int idUser, int newStatoRec)
     throws Exception
  {
    saveArray(con, toSave, idUser, newStatoRec, WRITE_LEVEL_FORCE);
  }

  /**
   * Salva collezioni di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @param writeLevel livello di permessi in scrittura (0-9)
   * @throws Exception
   */
  public static void saveCollection(Connection con, Collection toSave, int idUser, int newStatoRec, int writeLevel)
     throws Exception
  {
    if(toSave == null || toSave.isEmpty())
      return;

    boolean first = true;
    CoreObjectSaver ps = null;

    for(Object ob : toSave)
    {
      if(ob == null)
        continue;

      if(ob instanceof Persistent)
      {
        Persistent obj = (Persistent) ob;

        // NON APPLICABILE: la modifica potrebbe riguardare il cambio di statorec; quindi è necessario procedere
        // if(!obj.isModified())
        //   continue;
        if(first)
        {
          ps = getObjectSaver(obj);
          first = false;
        }

        if(con == null)
          ps.salva(obj, idUser, newStatoRec, writeLevel);
        else
          ps.salva(obj, con, idUser, newStatoRec, writeLevel);
      }
    }
  }

  /**
   * Salva collezioni di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il salvataggio tiene conto del valore precedente di statorec
   * che viene confrontato con writeLevel per verificare se l'operazione
   * è autorizzata.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param user informazioni sull'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveCollection(Connection con, Collection toSave, User user, int newStatoRec)
     throws Exception
  {
    saveCollection(con, toSave, (int) user.getId(), newStatoRec, (int) user.getPerm(WRITE_LEVEL_PERM_STORAGE, 0));
  }

  /**
   * Salva collezioni di oggetti peer utilizzando un PeerObjectSaver.
   * Il salvataggio diventa compatbile per la multiutenza
   * e con una serie di controlli automatici sul salvataggio.
   * I campi IdUser, IdUcrea, Creazione, UltModif e altri
   * sono aggiornati in automatico.
   * Il valore di statorec viene ignorato.
   * @param con connessione al database (può essere null)
   * @param toSave oggetto da salvare
   * @param idUser itentificativo dell'utente
   * @param newStatoRec nuovo valore per lo stato rec
   * @throws Exception
   */
  public static void saveForceCollection(Connection con, Collection toSave, int idUser, int newStatoRec)
     throws Exception
  {
    saveCollection(con, toSave, idUser, newStatoRec, WRITE_LEVEL_FORCE);
  }
}
