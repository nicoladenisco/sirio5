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
package org.sirio5.services;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.X509Certificate;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.commonlib5.ssl.SSLSocketInfo;
import org.commonlib5.ssl.TrustAllSecurityProvider;
import org.sirio5.services.localization.INT;

/**
 * Gestore delle connessioni TLS client per Applicazione.
 * Le connessioni server di Tomcat sono impostate in server.xml.
 *
 * @author Nicola De Nisco
 */
public class CoreTlsManager
{
  /** Logging */
  private static Log log = LogFactory.getLog(CoreTlsManager.class);
  /** Istanza del singletone */
  private static CoreTlsManager theInstance = null;
  private SSLSocketInfo info = new SSLSocketInfo();

  /**
   * path del keystore utilizzato per comunicare col server
   */
  protected String keyStore = null;
  /**
   * tipo del keystore utilizzato per comunicare col server
   */
  protected String keyStoreType = null;
  /**
   * password del keystore utilizzato per comunicare col server
   */
  protected String keyStorePassword = null;
  /**
   * path del truststore utilizzato per comunicare col server
   */
  protected String trustStore = null;
  /**
   * tipo del truststore utilizzato per comunicare col server
   */
  protected String trustStoreType = null;
  /**
   * password del truststore utilizzato per comunicare col server
   */
  protected String trustStorePassword = null;
  /**
   * Abilita il controllo del nome host nel certificato TLS.
   * Per default e' disabilitato.
   */
  protected boolean testHostName = false;
  /**
   * Abilita il controllo della validità dei certificati.
   * Per default e' disabilitato.
   */
  protected boolean testCertificate = false;

  private CoreTlsManager()
  {
  }

  public static CoreTlsManager getInstance()
  {
    if(theInstance == null)
      theInstance = new CoreTlsManager();

    return theInstance;
  }

  public void initTLScomunication()
     throws Exception
  {
    Configuration cfg = Turbine.getConfiguration();

    // inizializzazione dei parametri per le connessioni TLS
    keyStore = cfg.getString("ssl.keyStore", "keystore.jks");
    keyStoreType = cfg.getString("ssl.keyStoreType", "JKS");
    keyStorePassword = cfg.getString("ssl.keyStorePassword", "PASSWORD");
    trustStore = cfg.getString("ssl.trustStore", "cacerts.jks");
    trustStoreType = cfg.getString("ssl.trustStoreType", "JKS");
    trustStorePassword = cfg.getString("ssl.trustStorePassword", "PASSWORD");

    testHostName = cfg.getBoolean("ssl.testHostName", false);
    testCertificate = cfg.getBoolean("ssl.testCertificate", false);

    if(keyStore == null || keyStoreType == null || keyStorePassword == null
       || trustStore == null || trustStoreType == null || trustStorePassword == null)
      throw new CoreServiceException(INT.I("Mancano parametri indispensabili per il keystore."));

    // identifica sul sistema la directory /usr/local/tdk/conf/ o simile
    String sCatalinaHome = System.getProperty("catalina.base", "/usr/local/tdk");
    File dirCatalinaConf = new File(sCatalinaHome, "conf");

    // localizza i files nel file system
    File fKeyStore = new File(dirCatalinaConf, keyStore);
    File fTrustStore = new File(dirCatalinaConf, trustStore);

    log.debug(INT.I("Uso %s come file keyStore.", fKeyStore.getCanonicalPath()));
    log.debug(INT.I("Uso %s come file trustStore.", fTrustStore.getCanonicalPath()));

    info.setKeyStore(fKeyStore);
    info.setKeyStoreType(keyStoreType);
    info.setKeyStorePassword(keyStorePassword);

    info.setTrustStore(fTrustStore);
    info.setTrustStoreType(trustStoreType);
    info.setTrustStorePassword(trustStorePassword);

    if(fKeyStore.canRead() && fTrustStore.canRead())
    {
      System.setProperty("javax.net.ssl.keyStore", fKeyStore.getAbsolutePath());
      System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
      System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
      System.setProperty("javax.net.ssl.trustStore", fTrustStore.getAbsolutePath());
      System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
      System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
    else
    {
      log.error(INT.I("I files del keystore e/o del truststore non sono presenti o leggibili; uso settaggi di default della JVM."));
    }

    if(!testCertificate)
      TrustAllSecurityProvider.register();

    if(!testHostName)
      TrustAllSecurityProvider.registerHttpsURL();

    SSLServerSocketFactory factory = ((SSLServerSocketFactory) SSLServerSocketFactory.getDefault());
    for(int i = 0; i < 3; i++)
    {
      int port = (int) (65000 + ((Math.random() * 500)));
      try(ServerSocket server = factory.createServerSocket(port))
      {
        log.debug("SSL environment is sane.");
        break;
      }
      catch(Throwable t)
      {
        log.error(INT.I("Fallito tentativo %d di 3", (i + 1)), t);
      }
    }

    log.debug(INT.I("Layer SSL/TLS inizializzato."));
  }

  public SSLSocketInfo getInfo()
  {
    return info;
  }

  /**
   * Setta l'ambiente TLS nel caso in cui
   * il registry necessiti di autenticazione tramite certificato.
   *
   * @throws java.lang.Exception
   */
  public void setEnvTLS(String uri)
     throws Exception
  {
    URL url = new URL(uri);
    String host = url.getHost();

    try
    {
      // apro la connessione e verifico il certificato
      SSLSocket sslsocket = getTLSsocket(url);
      sslsocket.close();
    }
    catch(Exception ex)
    {
      throw new CoreServiceException(
         INT.I("Il certificato fornito per %s non e' valido: %s.", host, ex.getMessage()));
    }
  }

  public SSLSocket getTLSsocket(URL url)
     throws Exception
  {
    String host = url.getHost();
    int port = url.getPort();

    if(host == null || port == -1)
      throw new CoreServiceException(
         INT.I("L'url deve essere nella forma https://host:port/other ; altre forme non sono ammesse!"));

    return getTLSsocket(host, port);
  }

  public SSLSocket getTLSsocket(String host, int port)
     throws IOException, CoreServiceException
  {
    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, port);
    sslsocket.setUseClientMode(true);
    sslsocket.setEnabledProtocols(new String[]
    {
      "TLSv1", "SSLv3"
    });

    // apro la connessione e verifico il certificato
    sslsocket.startHandshake();

    if(testCertificate)
    {
      boolean valid = true;
      log.debug(INT.I("Inizio verifica certificati per %s:%d", host, port));
      X509Certificate[] cert = sslsocket.getSession().getPeerCertificateChain();

      for(int i = 0; i < cert.length; i++)
      {
        try
        {
          X509Certificate ce = cert[i];
          ce.checkValidity();
        }
        catch(CertificateExpiredException e1)
        {
          log.debug(INT.I("Certificato scaduto: %s", e1.getMessage()));
          valid = false;
        }
        catch(CertificateNotYetValidException e2)
        {
          log.debug(INT.I("Certificato non valido: %s", e2.getMessage()));
          valid = false;
        }
      }
      log.debug(INT.I("Fine verifica certificati per %s:%d", host, port));

      if(!valid)
        throw new CoreServiceException(
           INT.I("Certificati forniti dal server non validi o scaduti (consultare log per i dettagli)."));
    }

    return sslsocket;
  }

  public SSLServerSocket getTLSserverSocket(int port)
     throws IOException
  {
    SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    SSLServerSocket sslsocket = (SSLServerSocket) factory.createServerSocket(port);
    sslsocket.setUseClientMode(false);
    sslsocket.setEnabledProtocols(new String[]
    {
      "TLSv1", "SSLv3"
    });

    return sslsocket;
  }
}
