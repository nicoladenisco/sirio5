/*
 *  BaseXmlRpcClient.java
 *  Creato il 15-nov-2011, 15.01.34
 *
 *  Copyright (C) 2011 WinSOFT di Nicola De Nisco
 *
 *  Questo software è proprietà di Nicola De Nisco.
 *  I termini di ridistribuzione possono variare in base
 *  al tipo di contratto in essere fra Nicola De Nisco e
 *  il fruitore dello stesso.
 *
 *  Fare riferimento alla documentazione associata al contratto
 *  di committenza per ulteriori dettagli.
 */
package org.sirio5.utils.xmlrpc;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.commonlib5.utils.CommonFileUtils;
import org.commonlib5.utils.LongOperListener;
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

  protected void init(URL url)
     throws Exception
  {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setServerURL(url);
    client = new XmlRpcClient();
    client.setConfig(config);

    // set this transport factory for host-specific SSLContexts to work
    //XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
    //client.setTransportFactory(f);
    if(port == -1)
    {
      ASSERT(url.getProtocol() != null, "u.getProtocol() != null");
      switch(url.getProtocol())
      {
        case "http":
          port = 80;
          break;
        case "https":
          port = 443;
          break;
        default:
          throw new Exception("Unknow port for protocol " + url.getProtocol());
      }
    }
  }

  public BaseXmlRpcClient(String stubName, String server, int port)
     throws Exception
  {
    this.stubName = stubName;
    this.server = server;
    this.port = port;
    uri = String.format("http://%s:%d/RPC2", server, port);
    init(new URL(uri));
  }

  /**
   * Chiamata di metodo remoto.
   * Lo stubname viene aggiunto al nome del metodo se non nullo.
   * La chiamata effettiva è qualcosa del tipo stubname.metodo.
   * @param method nome del metodo
   * @param parameters parametri
   * @return valore di ritorno
   * @throws RemoteErrorException
   * @throws XmlRpcException
   * @throws IOException
   */
  protected Object call(String method, Object... parameters)
     throws RemoteErrorException, XmlRpcException, IOException
  {
    Vector params = new Vector();
    params.addAll(Arrays.asList(parameters));
    if(stubName != null)
      method = stubName + "." + method;
    Object rv = client.execute(method, params);
    if(rv instanceof XmlRpcException)
      throw new RemoteErrorException("Errore segnalato dal server remoto: "
         + ((XmlRpcException) rv).getMessage(), (Throwable) rv);
    return rv;
  }

  /**
   * Verifica asserzione.
   * @param test condizione da verificare
   * @param cause testo del messaggio eccezione
   */
  public void ASSERT(boolean test, String cause)
  {
    if(!test)
    {
      String mess = "ASSERT failed: " + cause;
      log.error(mess);
      throw new RuntimeException(mess);
    }
  }

  /**
   * Ritorana la URL utilizzata dal client.
   * @return URL di connessione
   */
  public URL getURL()
  {
    XmlRpcClientConfigImpl config = (XmlRpcClientConfigImpl) client.getConfig();
    return config.getServerURL();
  }

  /**
   * Scaricamento di un file con ticket.
   * ATTENZIONE: questa funzione può essere usata quando il client
   * è connesso alla porta standard http (ovvero quando la URL contiene il server primario).
   * @param ticket ticket rilasciato dal server
   * @param toSave file da salvare
   * @param lol stato di avanzamento (può essere null)
   * @throws Exception
   */
  public void getFileTicket(String ticket, File toSave, LongOperListener lol)
     throws Exception
  {
    // scaricamento del file zip
    URL u = getURL();
    String s = u.toString().replaceAll("RPC[0-9]+", "cache/" + ticket);
    CommonFileUtils.readUrlToFile(new URL(s), toSave, lol);
  }

  public static final int[] STANDARD_HTTP_PORTS =
  {
    80, 8080, 443, 8443, 8444
  };

  /**
   * Verifica se il client sta comunicando con una porta HTTP standard.
   * @return verso se porta standard
   */
  public boolean useHttpStandardPort()
  {
    for(int i = 0; i < STANDARD_HTTP_PORTS.length; i++)
      if(port == STANDARD_HTTP_PORTS[i])
        return true;

    return false;
  }

  /**
   * Verifica connettività con il server.
   * @return indirizzo client utilizzato nel collegamento con il server
   * @throws Exception in caso di mancanza di connettività o altro
   */
  public InetAddress testServer()
     throws Exception
  {
    // tenta di connettersi alla porta XML-RPC del master
    try (Socket s = new Socket(server, port))
    {
      return s.getLocalAddress();
    }
  }
}
