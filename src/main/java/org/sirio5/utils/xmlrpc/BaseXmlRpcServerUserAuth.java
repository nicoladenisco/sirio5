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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.Persistent;
import org.apache.turbine.services.TurbineServices;
import org.apache.xmlrpc.XmlRpcException;
import org.commonlib5.xmlrpc.HashtableRpc;
import org.commonlib5.xmlrpc.XmlRpcCostant;
import org.sirio5.services.localization.INT;
import org.sirio5.services.token.TokenAuthItem;
import org.sirio5.services.token.TokenAuthService;
import org.sirio5.utils.BeanWrapper;
import org.sirio5.utils.DT;
import org.sirio5.utils.HashtableStrings;
import org.sirio5.utils.SU;

/**
 * Classe base degli XmlRpc Server con autenticazione utente.
 * Definisce una serie di funzioni di base comuni a tutti
 * i server che devono supportare un concetto di autenticazione e sessione
 * associata al client che si connette al servizio.
 *
 * ATTENZIONE: non inizializzare nessun servizio nel costruttore
 * alrimenti si crea una incongruenza nello startup dei servizi
 * e l'applicazione non può procedere.
 */
abstract public class BaseXmlRpcServerUserAuth
{
  /** Logging */
  protected Log log = LogFactory.getLog(this.getClass());

  /**
   * Costruttore.
   *
   * ATTENZIONE: non collegarsi a servizi turbine nel costruttore
   * perchè il caricamento degli override di setup blocca l'inizializzazione
   * del servizio XMLRPC.
   * Vale anche per classi derivate.
   */
  public BaseXmlRpcServerUserAuth()
  {
  }

  /**
   * Inizializza un client.
   * @param param credenziali di logon
   * @return informazioni di logon
   * @throws Exception
   */
  public Map initClient(Map param)
     throws Exception
  {
    try
    {
      // crea il token agganciando una action per il logout
      TokenAuthItem item = addClient(param, (ActionEvent e) -> logoutClient(e));

      // copia gli attributi di logon nel token
      param.forEach((k, v) -> item.setAttribute(k.toString(), v));

      try
      {
        // la prima volta che si logga chiama onLogon
        if(DT.isEqu(item.getLastAccess(), item.getLogon()))
          onLogon(item);
      }
      catch(Exception e)
      {
        log.error(INT.I("Errore nel login di %s", item.getUsr().getName()), e);
        closeClient(item.getIdClient());
        throw e;
      }

      log.info(INT.I("Autenticazione utente %s avvenuta.", item.getUsr().getName()));
      return prepareLogonReturn(item);
    }
    catch(Exception ex)
    {
      log.error("initClient", ex);
      throw ex;
    }
  }

  private void logoutClient(ActionEvent e)
  {
    TokenAuthItem item1 = (TokenAuthItem) e.getSource();
    try
    {
      onLogout(item1);
    }
    catch(Throwable ex)
    {
      log.error(INT.I("Errore nel logout di %s", item1.getUsr().getName()), ex);
    }
  }

  protected HashtableRpc prepareLogonReturn(TokenAuthItem item)
     throws Exception
  {
    HashtableRpc rv = new HashtableRpc();
    rv.put(XmlRpcCostant.RV_CLIENT_ID, item.getIdClient());
    rv.put(XmlRpcCostant.RV_USER_ID, item.getUserID());
    return rv;
  }

  /**
   * Logout esplicito di un client.
   * @param clientID identificativo del token
   * @return 0=OK
   * @throws Exception
   */
  public int logout(String clientID)
     throws Exception
  {
    try
    {
      TokenAuthItem token = getClient(clientID);
      onLogout(token);
      return closeClient(clientID);
    }
    catch(Exception ex)
    {
      log.error("logout", ex);
      throw ex;
    }
  }

  /**
   * Aggiunge un client anonimo.
   * Utilizza il servizio TokenAuthService per l'autenticazione.
   * @param expireAction eventuale action da eseguire al logout o alla scadenza del token (può essere null)
   * @return token di autenticazione
   * @throws Exception
   */
  protected TokenAuthItem addClient(ActionListener expireAction)
     throws Exception
  {
    TokenAuthService tAuth = (TokenAuthService) (TurbineServices.getInstance().
       getService(TokenAuthService.SERVICE_NAME));

    return tAuth.addClient(expireAction);
  }

  /**
   * Aggiunge un client con credenziali.
   * Utilizza il servizio TokenAuthService per l'autenticazione.
   * @param htParam credenziali di autenticazione
   * @param expireAction eventuale action da eseguire al logout o alla scadenza del token (può essere null)
   * @return token di autenticazione
   * @throws Exception
   */
  protected TokenAuthItem addClient(Map htParam, ActionListener expireAction)
     throws Exception
  {
    TokenAuthService tAuth = (TokenAuthService) (TurbineServices.getInstance().
       getService(TokenAuthService.SERVICE_NAME));

    String sesid = (String) htParam.get(XmlRpcCostant.AUTH_SESSION);
    if(sesid != null)
      return tAuth.addClient(sesid, expireAction);

    String uName = (String) htParam.get(XmlRpcCostant.AUTH_USER);
    String uPass = (String) htParam.get(XmlRpcCostant.AUTH_PASS);
    if(uName != null && uPass != null)
      return tAuth.addClient(uName, uPass, expireAction);

    return tAuth.addClient(expireAction);
  }

  /**
   * Ritorna oggetto di autenticazione.
   * @param id identificato del token
   * @return token di autenticazione
   * @throws Exception
   */
  protected TokenAuthItem getClient(String id)
     throws Exception
  {
    TokenAuthService tAuth = (TokenAuthService) (TurbineServices.getInstance().
       getService(TokenAuthService.SERVICE_NAME));

    return tAuth.getClient(id);
  }

  /**
   * Invalida esplicitamente un token.
   * @param id identificato del token
   * @return 0=OK
   * @throws Exception
   */
  protected int closeClient(String id)
     throws Exception
  {
    TokenAuthService tAuth = (TokenAuthService) (TurbineServices.getInstance().
       getService(TokenAuthService.SERVICE_NAME));

    tAuth.removeClient(tAuth.getClient(id));
    return 0;
  }

  /**
   * Ritorna vero se la connessione è ancora valida.
   * @param id identificato del token
   * @return vero se il token è ancora valido
   */
  public boolean isValidConnection(String id)
  {
    try
    {
      return getClient(id) != null;
    }
    catch(Throwable e)
    {
      return false;
    }
  }

  /**
   * Notifica avvenuto logon.
   * Questa funzione è un segnaposto ridefinibile in classi
   * derivate per aggiungere informazioni a item dopo il logon.
   * Attenzione: viene chiamata solo la prima volta che avviene il logon;
   * se il token è attivo il logon non viene ripetuto.
   * @param item oggetto di autenticazione
   * @throws java.lang.Exception
   */
  protected void onLogon(TokenAuthItem item)
     throws Exception
  {
  }

  /**
   * Notifica avvenuto logout.
   * Questa funzione è un segnaposto ridefinibile in classi
   * derivate per effettuare operazioni di chiusura al logout.
   * Il logout può avvenire o esplicitamente oppure per timeout
   * del token di autenticazione.
   * @param item
   * @throws Exception
   */
  protected void onLogout(TokenAuthItem item)
     throws Exception
  {
  }

  protected HashtableRpc exportPeerObject(ColumnAccessByName ref, HashtableRpc ht, String prefix, String... fieldList)
     throws Exception
  {
    for(String fldName : fieldList)
      ht.put(prefix + fldName, ref.getByName(fldName));
    return ht;
  }

  /**
   * Esporta lista di oggetti.
   * Viene creato un List di hashtable (una per ogni oggetto).
   * @param lsObj lista di oggetti figli di Persistent
   * @param prefix prefisso per i campi esportati
   * @return vettore di hashtable
   * @throws Exception
   */
  protected List exportListObject(Collection lsObj, String prefix)
     throws Exception
  {
    List vObj = new ArrayList();
    BeanWrapper bw = new BeanWrapper();
    for(Object bobj : lsObj)
    {
      bw.setObject(bobj);
      vObj.add(bw.objExportPure(new HashtableStrings(), prefix));
    }
    return vObj;
  }

  /**
   * Esporta un oggetto.
   * Viene creata una hashtable con i campi dell'oggetto.
   * @param bo oggetto figlio di Persistent
   * @param prefix prefisso per i campi esportati
   * @return hashtable con i campi
   * @throws Exception
   */
  protected Map<String, String> exportObject(Persistent bo, String prefix)
     throws Exception
  {
    BeanWrapper bw = new BeanWrapper(bo);
    return bw.objExportPure(new HashtableStrings(), prefix);
  }

  protected int parseInt(Object value)
  {
    return SU.parse(value, 0);
  }

  protected boolean preparaKeyUppercase = false;

  protected List preparaList(List params)
  {
    List rv = new ArrayList();
    for(Object value : (List) params)
    {
      if(value != null)
      {
        if(value instanceof Map)
          value = preparaMap((Map) value);
        else if(value instanceof List)
          value = preparaList((List) value);
        else
          value = SU.okStr(value);
      }
      rv.add(value);
    }
    return rv;
  }

  protected Map preparaMap(Map params)
  {
    Map rv = new HashMap();
    Set<Map.Entry<Object, Object>> entrySet = params.entrySet();
    for(Map.Entry<Object, Object> entry : entrySet)
    {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if(key != null && value != null)
      {
        if(value instanceof Map)
          value = preparaMap((Map) value);
        else if(value instanceof List)
          value = preparaList((List) value);
        else
          value = SU.okStr(value);

        if(preparaKeyUppercase)
          rv.put(key.toString().toUpperCase(), value);
        else
          rv.put(key.toString(), value);
      }
    }
    return rv;
  }

  public List getAsList(Object rv)
     throws XmlRpcException
  {
    if(rv instanceof List)
      return (List) rv;

    if(rv instanceof Object[])
      return Arrays.asList((Object[]) rv);

    throw new XmlRpcException("The return type is not compatible with List.");
  }

  public Map getAsMap(Object rv)
     throws XmlRpcException
  {
    if(rv instanceof Map)
      return (Map) rv;

    throw new XmlRpcException("The return type is not compatible with List.");
  }
}
