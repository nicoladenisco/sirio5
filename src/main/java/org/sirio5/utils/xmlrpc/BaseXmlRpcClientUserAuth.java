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
package org.sirio5.utils.xmlrpc;

import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import org.apache.xmlrpc.XmlRpcException;
import org.commonlib5.utils.CommonFileUtils;
import org.commonlib5.utils.CommonNetUtils;
import org.commonlib5.xmlrpc.RemoteErrorException;
import org.commonlib5.xmlrpc.XmlRpcCostant;

/**
 * Classe base dei client XML-RPC che implementano l'autenticazione utente.
 *
 * @author Nicola De Nisco
 */
public class BaseXmlRpcClientUserAuth extends BaseXmlRpcClient
{
  protected String idClient = null;
  protected Hashtable initResult = null;
  protected Hashtable initData = new Hashtable();
  //
  public static final String TOKEN_MAGIG = "MDQOWHF!IWEQRGHUYRWVQNCWOEQ$DKPOQKEPO.QJEFWQE;UFNCLWRHV:OIWUERHFUISXJKLMql"; // NOI18N

  public BaseXmlRpcClientUserAuth(String stubName, URL url)
     throws Exception
  {
    super(stubName, url);
  }

  public BaseXmlRpcClientUserAuth(String stubName, String server, int port)
     throws Exception
  {
    super(stubName, server, port);
  }

  /**
   * Inizializzazione del client tramite credenziali.
   * @param data parametri di inizializzazione.
   * @param user utente che chiede la connessione
   * @param pass password dell'utente
   * @throws Exception
   */
  public void init(String user, String pass, Hashtable data)
     throws Exception
  {
    if(data != null)
      initData.putAll(data);

    InetAddress iadr = testServer();
    initData.put(XmlRpcCostant.AUTH_USER, user);
    initData.put(XmlRpcCostant.AUTH_PASS, pass);
    initData.put(XmlRpcCostant.AUTH_ADDRESS, CommonNetUtils.toString(iadr));
    initClient(initData);
  }

  public void initMagic(String user, String serverUUID, Hashtable data)
     throws Exception
  {
    String shHash = calcolaHashMagic(user, serverUUID);
    init(user, shHash, data);
  }

  public String calcolaHashMagic(String user, String serverUUID)
     throws Exception
  {
    return CommonFileUtils.calcolaHashStringa(
       user + "_" + TOKEN_MAGIG + "_" + serverUUID, "SHA1");
  }

  /**
   * Inizializzazione del client tramite identificativo di sessione HTTP.
   * La connessione verrà agganciata all'utente al momento autenticato
   * nell'applicazion server con l'id di sessione specificato.
   * @param data parametri di inizializzazione.
   * @param sessionID identificativo della sessione HTTP (rilasciato dall'application server)
   * @throws Exception
   */
  public void init(String sessionID, Hashtable data)
     throws Exception
  {
    if(data != null)
      initData.putAll(data);

    InetAddress iadr = testServer();
    initData.put(XmlRpcCostant.AUTH_SESSION, sessionID);
    initData.put(XmlRpcCostant.AUTH_ADDRESS, CommonNetUtils.toString(iadr));
    initClient(initData);
  }

  /**
   * Inizializzazione del client.
   * @param data parametri di inizializzazione.
   * @return parametri di risposta dal server
   * @throws Exception
   */
  public Hashtable initClient(Hashtable data)
     throws Exception
  {
    Vector params = new Vector();
    params.add(data);
    Object rv = client.execute(stubName + ".initClient", params);
    if(rv instanceof XmlRpcException)
      throw new RemoteErrorException("Errore segnalato dal server remoto:\n"
         + ((Throwable) rv).getMessage(), (Throwable) rv);

    if(!(rv instanceof Hashtable))
      throw new RemoteErrorException("Tipo inaspettato nel valore di ritorno: era attesa una Hashtable.");

    initResult = (Hashtable) rv;
    String errorMessage = (String) initResult.get(XmlRpcCostant.RV_ERROR);
    if(errorMessage != null)
      throw new RemoteErrorException("Errore di comunicazione (lato server): " + errorMessage);

    if((idClient = (String) initResult.get(XmlRpcCostant.RV_CLIENT_ID)) == null)
      throw new RemoteErrorException("Autenticazione fallita: nessun codice utente ritornato.");

    return (Hashtable) rv;
  }

  /**
   * Chiude la connessione logica invalidando il token.
   * Il server scarica tutte le informazioni legati a questa connessione.
   * @throws Exception
   */
  public void logout()
     throws Exception
  {
    if(idClient != null)
    {
      call("logout", idClient);
      idClient = null;
    }
  }

  /**
   * Stato di logon.
   * @return vero se il token è stato rilasciato dal server
   */
  public boolean hasLogged()
  {
    return idClient != null;
  }

  /**
   * Controlla stato della connessione.
   * Il token deve essere rilasciato (hasLogged()),
   * le comunicaizoni sono attive, il server conferma
   * che il token è collegato a una connessione attiva.
   * @return vero se la connessione è attiva
   */
  public boolean isValidConnection()
  {
    try
    {
      if(idClient == null)
        return false;

      return (Boolean) call("isValidConnection", idClient);
    }
    catch(Throwable e)
    {
      return false;
    }
  }

  /**
   * Controlla stato del link.
   * @return vero se il server è raggiungibile e attivo per questo stub
   */
  public boolean ping()
  {
    try
    {
      call("isValidConnection", idClient);
      return true;
    }
    catch(Throwable e)
    {
      return false;
    }
  }

  /**
   * Token della connessione.
   * @return stringa token della connessione
   */
  public String getIdClient()
  {
    return idClient;
  }

  /**
   * Risultati di inizializzazione.
   * @return valori restituiti dal server al momento della init
   */
  public Map getInitResult()
  {
    return Collections.unmodifiableMap(initResult);
  }

  /**
   * Parametri di inizializzazione.
   * @return valori inviati al server al momento della init
   */
  public Map getInitData()
  {
    return Collections.unmodifiableMap(initData);
  }

  /**
   * Ritorna la lista dei profili dell'utente autenticato.
   * @param clientID identificativo del token
   * @return vettore di hashtable con i dati dei profili dell'utente
   * @deprecated usa i dati restituiti al logon (getInitResult)
   * @throws Exception
   */
  public Vector getListaProfiliUtente(String clientID)
     throws Exception
  {
    return (Vector) call("getListaProfiliUtente", clientID);
  }

  /**
   * Cambia il profilo attivo dell'utente autenticato.
   * @param clientID identificativo del token
   * @param idNuovoProfilo nuovo id profilo richiesto
   * @return il profilo richiesto se possibile per l'utente
   * @throws Exception
   */
  public int cambiaProfiloUtente(String clientID, int idNuovoProfilo)
     throws Exception
  {
    return (Integer) call("cambiaProfiloUtente", clientID, idNuovoProfilo);
  }

  /**
   * Verifica permessi per l'utente corrente.
   * @param clientID id del ticket rilasciato da initClient
   * @param permessi lista di permessi separati da virgola
   * @return diverso da zero se tutti i permessi indicati sono verificati
   * @throws Exception
   */
  public int checkAllPermission(String clientID, String permessi)
     throws Exception
  {
    return (Integer) call("checkAllPermission", clientID, permessi);
  }

  /**
   * Verifica permessi per l'utente corrente.
   * @param clientID id del ticket rilasciato da initClient
   * @param permessi lista di permessi separati da virgola
   * @return diverso da zero se almeno uno dei permessi indicati è verificato
   * @throws Exception
   */
  public int checkAnyPermission(String clientID, String permessi)
     throws Exception
  {
    return (Integer) call("checkAnyPermission", clientID, permessi);
  }
}
