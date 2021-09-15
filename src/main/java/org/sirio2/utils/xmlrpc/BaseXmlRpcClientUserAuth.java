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
package org.sirio2.utils.xmlrpc;

import java.net.URL;
import java.util.*;
import org.apache.xmlrpc.XmlRpcException;
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

  public void init(String user, String pass, Hashtable data)
     throws Exception
  {
    if(data != null)
      initData.putAll(data);

    initData.put(XmlRpcCostant.AUTH_USER, user);
    initData.put(XmlRpcCostant.AUTH_PASS, pass);
    initClient(initData);
  }

  public void init(String sessionID, Hashtable data)
     throws Exception
  {
    if(data != null)
      initData.putAll(data);

    initData.put(XmlRpcCostant.AUTH_SESSION, sessionID);
    initClient(initData);
  }

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

  public void logout()
     throws Exception
  {
    if(idClient != null)
    {
      call("logout", idClient);
      idClient = null;
    }
  }

  public boolean hasLogged()
  {
    return idClient != null;
  }

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

  public String getIdClient()
  {
    return idClient;
  }

  public Map getInitResult()
  {
    return Collections.unmodifiableMap(initResult);
  }
}
