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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.*;
import org.apache.turbine.*;
import org.apache.turbine.services.*;
import org.commonlib5.utils.ClassOper;
import org.commonlib5.utils.CommonFileUtils;
import org.commonlib5.utils.OsIdent;
import org.commonlib5.utils.StringOper;
import static org.sirio5.CoreConst.APP_PREFIX;
import org.sirio5.services.allarmi.ALLARM;

/**
 * Classe base di tutti i servizi.
 * I servizi sono configurati nel file applicazione.properties
 * come gli altri servizi di turbine.
 */
abstract public class AbstractCoreBaseService extends BaseService implements CoreServiceExtension
{
  /** Logging */
  private static final Log log = LogFactory.getLog(AbstractCoreBaseService.class);
  /** semaforo per l'inizializzazione */
  private static final Object semaforoInit = new Object();
  protected static boolean initEseguito = false, initCompletato = false, databaseModificato = false;
  /** Variabili globali per tutti i servizi */
  protected static int aziendaId = 0;
  protected static String aziendaNome = null;
  protected static String server = null, address = null;
  private static final ArrayList<InfoSetupInterface> arInfoSetup = new ArrayList<>();
  /** path di riferimento */
  protected static String pathWork = null;
  protected static String pathWorkTmp = null;
  protected static String pathWorkSpool = null;
  protected static String pathWorkCache = null;
  protected static String pathWorkDocs = null;
  protected static String pathWorkReport = null;
  protected static String pathWorkStylesheets = null;
  protected static String pathConf = null;
  protected static String pathConfSchemas = null;
  protected static String pathConfSetup = null;
  protected static String pathConfStylesheets = null;
  protected static String pathConfReports = null;
  protected static String pathConfCert = null;
  /** UUID unica di questa istanza di applicazione */
  private static String AppUUID = null;

  protected String normalizeWindowsExecutable(String sPath)
  {
    // estrae la prima stringa per aggiungere il .exe sotto windows
    if(OsIdent.checkOStype() == OsIdent.OS_WINDOWS
       && !sPath.toLowerCase().contains(".exe"))
    {
      int pos = sPath.indexOf(' ');
      if(pos == -1)
        sPath += ".exe";
      else
        sPath = sPath.substring(0, pos) + ".exe" + sPath.substring(pos);
    }

    return sPath;
  }

  public File getWorkFile(String dirConf, String path)
  {
    return new File(dirConf, path);
  }

  public String getWorkPath(String dirConf, String path)
  {
    String fPath = dirConf + "/" + path;
    return normalizePath(fPath);
  }

  public File getConfigFile(String dirConf, String path)
  {
    return new File(getConfigPath(dirConf, path));
  }

  public String getConfigPath(String dirConf, String path)
  {
    String fPath = dirConf + "/" + path;
    return getRealPath(fPath);
  }

  /**
   * Ritorna la path effettiva di un file all'interno
   * della directory dell'applicazione web.
   * La path viene ritornata in modo consono alla piattaforma
   * di funzionamento (windows o Unix).
   * ES index.jsp produce /usr/local/tdk/webapps/nomeapp/index.jsp
   * @param path una path qualsiasi all'interno dell'applicazione web
   * @return path assoluta nel file system
   */
  @Override
  public String getRealPath(String path)
  {
    String rv = Turbine.getRealPath(path.replace('\\', '/'));
    return normalizePath(rv);
  }

  @Override
  public File getRealFile(String path)
  {
    return new File(getRealPath(path));
  }

  /**
   * Converte la path di ingresso con gli
   * opportuni separatori in modo che sia coerente con
   * la piattaforma ospite (windows o unix).
   * @param path
   * @return
   */
  public String normalizePath(String path)
  {
    String rv = path.replace('\\', '/').replace("//", "/");

    if(File.separatorChar == '/')
      return rv;
    else
      return rv.replace('/', File.separatorChar);
  }
  private static String strTurbineContext = null;

  /**
   * Ritorna la context path dell'applicazione.
   * (di solito /nomeapp/).
   * @return
   */
  @Override
  public String getTurbineContextPath()
  {
    if(strTurbineContext == null)
      strTurbineContext = Turbine.getContextPath() + "/";

    return strTurbineContext;
  }

  protected CoreAppSanity getSanitizer()
  {
    String aa = Turbine.getConfiguration().getString("sanity.classname");

    if(aa != null)
    {
      if("DISABLED".equals(aa))
        return null;

      try
      {
        return (CoreAppSanity) ClassOper.createObject(aa, null, null);
      }
      catch(Exception ex)
      {
        log.error("Non riesco a creare " + aa, ex);
      }
    }

    return new CoreAppSanity();
  }

  @Override
  final public void init()
     throws InitializationException
  {
    super.init();

    try
    {
      setInit(false);

      synchronized(semaforoInit)
      {
        if(!initEseguito)
        {
          if(!oneTimeInit())
            return;
        }
        else
        {
          if(!initCompletato)
          {
            log.debug("Servizio " + getName() + " non avviato a causa di un precedente errore.");
            setInit(false);
            return;
          }
        }
      }

      // inizializzazione specifica del servizio derivato
      log.debug("Avvio del servizio " + getName());
      coreInit();
      setInit(true);

      // se il servizio implementa l'interfaccia dati setup lo aggiunge al relativo array
      if(this instanceof InfoSetupInterface)
        arInfoSetup.add((InfoSetupInterface) this);

      log.debug("Servizio " + getName() + " avviato con successo.");
    }
    catch(Throwable e)
    {
      String s = "Errore nell'avvio del servizio " + getName() + ": " + e.getMessage();
      log.error(s, e);
      ALLARM.fatal(getName(), "Init", s, 0);
      setInit(false);
    }
  }

  /**
   * Esegue inizializzazione estesa servizi sirio.
   * @return vero se l'inizializzazione è avvenuta con successo
   */
  protected boolean oneTimeInit()
  {
    log.info("Attivo inizializzazione variabili statiche globali dei servizi.");
    initEseguito = true;

    try
    {
      localInit();

      CoreAppSanity san = getSanitizer();
      if(san != null)
        san.sanityApplication(this);

      initCompletato = true;
      return true;
    }
    catch(Exception e)
    {
      String s = "ERRORE FATALE INIZIALIZZAZIONE (esecuzione compromessa/non possibile): "
         + e.getMessage();
      log.error(s, e);
      ALLARM.fatal("CORE", "Init", s, 0);
      return false;
    }
  }

  /**
   * Inizializzazione servizio derivato.
   * @throws Exception
   */
  public abstract void coreInit()
     throws Exception;

  /**
   * Inizializzazione una tantum delle variabili statiche
   * e quindi generali per tutti i servizi.
   * @throws java.lang.Exception
   */
  protected void localInit()
     throws Exception
  {
    Configuration cfg = Turbine.getConfiguration();
    log.info("Inizializzazione globale con contextPath " + strTurbineContext);

    aziendaId = cfg.getInt("azienda.id", 1);
    aziendaNome = cfg.getString("azienda.nome", "Acme abstract s.r.l.");

    server = cfg.getString("server.name", "localhost");
    address = cfg.getString("server.address", "127.0.0.1");

    pathWork = TV(cfg.getString("path.work"));
    if(pathWork == null)
      throw new CoreServiceException("Definizione 'path.work' mancante nella configurazione.");

    cfg.setProperty("pathWork", pathWork);

    pathWorkTmp = TV(cfg.getString("path.work.tmp", pathWork + "/tmp"));
    pathWorkSpool = TV(cfg.getString("path.work.spool", pathWork + "/spool"));
    pathWorkCache = TV(cfg.getString("path.work.cache", pathWork + "/cache"));
    pathWorkDocs = TV(cfg.getString("path.work.docs", pathWork + "/documenti"));
    pathWorkStylesheets = TV(cfg.getString("path.work.report", pathWork + "/stylesheets"));
    pathWorkReport = TV(cfg.getString("path.work.report", pathWork + "/reports"));

    makeTestDirWrite(pathWork);
    makeTestDirWrite(pathWorkTmp);
    makeTestDirWrite(pathWorkSpool);
    makeTestDirWrite(pathWorkCache);
    makeTestDirWrite(pathWorkDocs);
    makeTestDirWrite(pathWorkStylesheets);
    makeTestDirWrite(pathWorkReport);

    pathConf = "/WEB-INF/" + TV(cfg.getString("path.conf", "/conf"));

    cfg.setProperty("pathConf", pathConf);

    pathConfSchemas = "/WEB-INF/" + TV(cfg.getString("path.conf.schemas", "/conf/schemas"));
    pathConfSetup = "/WEB-INF/" + TV(cfg.getString("path.conf.setup", "/conf/setup"));
    pathConfStylesheets = "/WEB-INF/" + TV(cfg.getString("path.conf.xls", "/conf/stylesheets"));
    pathConfReports = "/WEB-INF/" + TV(cfg.getString("path.conf.reports", "/conf/reports"));
    pathConfCert = "/WEB-INF/" + TV(cfg.getString("path.conf.certs", "/conf/cert"));

    makeTestDir(getRealPath(pathConf));
    makeTestDir(getRealPath(pathConfSchemas));
    makeTestDir(getRealPath(pathConfSetup));
    makeTestDir(getRealPath(pathConfStylesheets));
    makeTestDir(getRealPath(pathConfReports));
    makeTestDir(getRealPath(pathConfCert));

    // pulisce la directory /var/nomeapp/tmp
    // NON POSSIBILE: se applicazione ha piu moduli distrugge init dei vari moduli
    // se questa funzionalità è richiesta può essere spostata nel sanitizer personalizzato per l'applicazione.
    // CommonFileUtils.deleteDir(new File(pathWorkTmp), false);
    //
    // se non già fatto imposta un UUID univoco per questa istanza
    // il dato viene salvato in un apposito files in /var/nomeapp/documenti
    File uuidSec = getWorkDocsFile("uuid.txt");
    if((AppUUID = CommonFileUtils.readFileTxt(uuidSec, "UTF-8")) == null)
    {
      AppUUID = UUID.randomUUID().toString();

      // scrive il file di sicurezza
      CommonFileUtils.writeFileTxt(uuidSec, AppUUID, "UTF-8");
    }

    cfg.setProperty("AppUUID", AppUUID);
  }

  /**
   * Controlla che la stringa in ingresso non contenga spazi.
   * @param input
   * @return
   * @throws java.lang.IllegalArgumentException
   */
  protected String TV(String input)
     throws IllegalArgumentException
  {
    if(input == null)
      throw new IllegalArgumentException("Orribile errore di setup: path di configurazione non trovata.");

    if(input.indexOf(' ') != -1)
      throw new IllegalArgumentException("La stringa '" + input + "' contiene spazi: non consentito.");

    return StringOper.strReplace(input, "//", "/");
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale di appoggio per applicazione.
   * (/var/nomeapp per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkMainFile(String subFile)
  {
    return getGenericFile(pathWork, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale dei temporanei per applicazione.
   * (/var/nomeapp/tmp per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkTmpFile(String subFile)
  {
    return getGenericFile(pathWorkTmp, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale di spool per applicazione.
   * (/var/nomeapp/spool per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkSpoolFile(String subFile)
  {
    return getGenericFile(pathWorkSpool, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale della cache per applicazione.
   * (/var/nomeapp/cache per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkCacheFile(String subFile)
  {
    return getGenericFile(pathWorkCache, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale dei documenti per applicazione.
   * (/var/nomeapp/doc per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkDocsFile(String subFile)
  {
    return getGenericFile(pathWorkDocs, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale dei logs.
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getLogFile(String subFile)
  {
    return getGenericRealFile("/logs", subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * principale di configurazione per applicazione.
   * (/nomeapp/WEB-INF/conf per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfMainFile(String subFile)
  {
    return getGenericRealFile(pathConf, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione degli schemi xml.
   * (/nomeapp/WEB-INF/conf/schemas per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfSchemasFile(String subFile)
  {
    return getGenericRealFile(pathConfSchemas, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione del setup client.
   * (/nomeapp/WEB-INF/conf/setup per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfSetupFile(String subFile)
  {
    return getGenericRealFile(pathConfSetup, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei file xls.
   * (/nomeapp/WEB-INF/conf/xls per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfXlsFile(String subFile)
  {
    return getGenericRealFile(pathConfStylesheets, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei file di jreport.
   * (/nomeapp/WEB-INF/conf/reports per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfReportFile(String subFile)
  {
    return getGenericRealFile(pathConfReports, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei certificati di sicurezza.
   * (/nomeapp/WEB-INF/conf/cert per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getConfCertFile(String subFile)
  {
    return getGenericRealFile(pathConfCert, subFile);
  }

  @Override
  public File getWorkReportFile(String subFile)
  {
    return getGenericFile(pathWorkReport, subFile);
  }

  /**
   * Ritorna un file ubicato nella directory
   * di runtime dei fogli di stile.
   * (/var/caleido/xls per UNIX).
   *
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  @Override
  public File getWorkXlsFile(String subFile)
  {
    return getGenericFile(pathWorkStylesheets, subFile);
  }

  protected File getGenericFile(String basePath, String subFile)
  {
    return subFile == null ? new File(basePath) : new File(basePath, subFile);
  }

  protected File getGenericRealFile(String basePath, String subFile)
  {
    return subFile == null ? new File(getRealPath(basePath))
              : new File(getRealPath(basePath), subFile);
  }

  protected void makeTestDir(String dir)
     throws Exception
  {
    File fDir = new File(dir);
    fDir.mkdirs();
    ASSERT_DIR(fDir);
  }

  protected void makeTestDirWrite(String dir)
     throws Exception
  {
    File fDir = new File(dir);
    fDir.mkdirs();
    ASSERT_DIR_WRITE(fDir);
  }

  /**
   * Ritorna il nome del server canonico
   * dell'istanza dell'applicazione in esecuzione.
   * @return
   */
  @Override
  public String getCanonicalServerName()
  {
    return server;
  }

  /**
   * Ritorna l'indirizzo TCP/IP principale
   * dell'istanza dell'applicazione in esecuzione.
   * @return
   */
  @Override
  public String getCanonicalServerAddress()
  {
    return address;
  }

  /**
   * Data una url relativa torna l'url completa a seconda dell'ambiente.
   * La stringa si riferisce a una risorsa JSP
   * ES url=jsmia.jsp produce http://localhost:8080/src/jsmia.jsp
   * oppure http://mio.server.it:8080/miaapp/servlet/miaapp/template/jsmia.jsp
   */
  @Override
  public String getServerUrlJSP(String url)
  {
    if(url.startsWith("/"))
      url = url.substring(1);

    return getTurbineContextPath() + APP_PREFIX + "/template/" + url;
  }

  /**
   * Data una url relativa torna l'url completa a seconda dell'ambiente.
   * La stringa si riferisce a una risorsa qualsiasi riferita alla path
   * dell'applicazione
   * ES url=img.gif produce http://localhost:8080/img.gif
   * oppure http://mio.server.it:8080/miaapp/img.gif
   */
  @Override
  public String getServerUrlGeneric(String url)
  {
    if(url.startsWith("/"))
      url = url.substring(1);

    return getTurbineContextPath() + url;
  }

  ///////////////////////////////////////////////////////////////////////////
  // funzioni di utilita'
  @Override
  public void ASSERT(boolean test, String cause)
     throws Exception
  {
    if(!test)
    {
      String mess = "ASSERT failed: " + cause;
      log.error(mess);
      throw new CoreServiceException(mess);
    }
  }

  /**
   * Verifica che il file esista.
   * @param toTest
   * @throws java.lang.Exception
   */
  @Override
  public void ASSERT_FILE(File toTest)
     throws Exception
  {
    if(!(toTest.exists() && toTest.isFile()))
    {
      String mess = "ASSERT_FILE failed: il file " + toTest.getAbsolutePath()
         + " non esiste.";
      log.debug(mess);
      throw new CoreServiceException(mess);
    }
  }

  /**
   * Verifica che la directory esista.
   * @param toTest
   * @throws java.lang.Exception
   */
  @Override
  public void ASSERT_DIR(File toTest)
     throws Exception
  {
    if(!(toTest.exists() && toTest.isDirectory()))
    {
      String mess = "ASSERT_DIR failed: la directory " + toTest.getAbsolutePath()
         + " non esiste.";
      log.debug(mess);
      throw new CoreServiceException(mess);
    }
  }

  /**
   * Verifica che la directory esista e sia
   * possibile creare files al suo interno.
   * @param toTest
   * @throws java.lang.Exception
   */
  @Override
  public void ASSERT_DIR_WRITE(File toTest)
     throws Exception
  {
    toTest.mkdirs();
    if(!(toTest.exists() && toTest.isDirectory() && CommonFileUtils.checkDirectoryWritable(toTest)))
    {
      String mess = "ASSERT_DIR_WRITE failed: la directory " + toTest.getAbsolutePath()
         + " non esiste o non e' scrivibile.";
      log.debug(mess);
      throw new CoreServiceException(mess);
    }
  }

  @Override
  public void TRACE(String mess)
  {
    log.debug(mess);
  }

  @Override
  public void die(String causa)
     throws Exception
  {
    throw new CoreServiceException(causa);
  }

  public List<InfoSetupInterface> getInfoSetup()
  {
    return arInfoSetup;
  }

  public int getAziendaId()
  {
    return aziendaId;
  }

  public String getAziendaNome()
  {
    return aziendaNome;
  }

  public static String getAppUUID()
  {
    return AppUUID;
  }

  public static boolean isInitCompletato()
  {
    return initCompletato;
  }

  public static boolean isInitEseguito()
  {
    return initEseguito;
  }

  public static boolean isDatabaseModificato()
  {
    return databaseModificato;
  }

  public static void setDatabaseModificato(boolean databaseModificato)
  {
    AbstractCoreBaseService.databaseModificato = databaseModificato;
  }
}
