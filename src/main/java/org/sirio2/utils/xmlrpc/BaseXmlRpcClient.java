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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.commonlib5.xmlrpc.RemoteErrorException;

/**
 * Implementazione di base dei client XML-RPC.
 *
 * @author Nicola De Nisco
 */
public class BaseXmlRpcClient
{
  protected Log log = LogFactory.getLog(this.getClass());
  protected String stubName = null, server = null, uri = null;
  protected int port = 0;
  protected XmlRpcClient client = null;

  public BaseXmlRpcClient(String stubName, URL url)
     throws Exception
  {
    this.stubName = stubName;
    this.server = url.getHost();
    this.port = url.getPort();
    init(url);
  }

  public BaseXmlRpcClient(String stubName, String server, int port)
     throws Exception
  {
    this.stubName = stubName;
    this.server = server;
    this.port = port;
    init(new URL(uri));
  }

  protected void init(URL url)
  {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);
    client = new XmlRpcClient();
    client.setConfig(config);

    // set this transport factory for host-specific SSLContexts to work
    //XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
    //client.setTransportFactory(f);
  }

  protected Object call(String method, Object... parameters)
     throws RemoteErrorException, XmlRpcException, IOException
  {
    Vector params = new Vector();
    params.addAll(Arrays.asList(parameters));
    if(stubName != null)
      method = stubName + "." + method;
    Object rv = client.execute(method, params);
    if(rv instanceof XmlRpcException)
      throw new RemoteErrorException("Errore segnalato dal server remoto:", (Throwable) rv);
    return rv;
  }

  public void ASSERT(boolean test, String cause)
     throws Exception
  {
    if(!test)
    {
      String mess = "ASSERT failed: " + cause;
      log.debug(mess);
      throw new Exception(mess);
    }
  }
}
