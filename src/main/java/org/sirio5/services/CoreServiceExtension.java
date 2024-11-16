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

import java.io.File;

/**
 * Interfaccia base di tutti i servizi.
 * @author Nicola De Nisco
 */
public interface CoreServiceExtension
{
  public void ASSERT(boolean test, String cause)
     throws Exception;

  public void ASSERT_FILE(File toTest)
     throws Exception;

  public void ASSERT_DIR(File toTest)
     throws Exception;

  public void ASSERT_DIR_WRITE(File toTest)
     throws Exception;

  public void TRACE(String mess);

  public void die(String causa)
     throws Exception;

  /**
   * Ritorna l'indirizzo TCP/IP principale
   * dell'istanza di applicazione in esecuzione.
   * @return
   */
  public String getCanonicalServerAddress();

  /**
   * Ritorna il nome del server canonico
   * dell'istanza di applicazione in esecuzione.
   * @return
   */
  public String getCanonicalServerName();

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei certificati di sicurezza.
   * (/nomeapp/WEB-INF/conf/cert per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfCertFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale di configurazione del sistema applicazione.
   * (/nomeapp/WEB-INF/conf per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfMainFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei file di jreport.
   * (/nomeapp/WEB-INF/conf/reports per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfReportFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione degli schemi xml.
   * (/nomeapp/WEB-INF/conf/schemas per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfSchemasFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione del setup client.
   * (/nomeapp/WEB-INF/conf/setup per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfSetupFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * di configurazione dei file xls.
   * (/nomeapp/WEB-INF/conf/xls per UNIX a partire dalla
   * directory di installazione dell'applicazione web).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getConfXlsFile(String subFile);

  /**
   * Ritorna la path effettiva di un file all'interno
   * della directory dell'applicazione web.
   * La path viene ritornata in modo consono alla piattaforma
   * di funzionamento (windows o Unix).
   * ES index.jsp produce /usr/local/tdk/webapps/nomeapp/index.jsp
   * @param path una path qualsiasi all'interno dell'applicazione web
   * @return path assoluta nel file system
   */
  public String getRealPath(String path);

  /**
   * Ritorna la path effettiva di un file all'interno
   * della directory dell'applicazione web.
   * La path viene ritornata in modo consono alla piattaforma
   * di funzionamento (windows o Unix).
   * ES index.jsp produce /usr/local/tdk/webapps/nomeapp/index.jsp
   * @param path una path qualsiasi all'interno dell'applicazione web
   * @return File assoluto nel file system
   */
  public File getRealFile(String path);

  /**
   * Data una url relativa torna l'url completa a seconda dell'ambiente.
   * La stringa si riferisce a una risorsa qualsiasi riferita alla path
   * dell'applicazione
   * ES url=img.gif produce http://localhost:8080/img.gif
   * oppure http://mio.server.it:8080/miaapp/img.gif
   * @param url
   * @return
   */
  public String getServerUrlGeneric(String url);

  /**
   * Data una url relativa torna l'url completa a seconda dell'ambiente.
   * La stringa si riferisce a una risorsa JSP
   * ES url=jsmia.jsp produce http://localhost:8080/src/jsmia.jsp
   * oppure http://mio.server.it:8080/miaapp/servlet/miaapp/template/jsmia.jsp
   * @param url
   * @return
   */
  public String getServerUrlJSP(String url);

  /**
   * Ritorna la context path dell'applicazione.
   * (di solito /nomeapp/).
   * @return
   */
  public String getTurbineContextPath();

  /**
   * Ritorna un file ubicato nella directory
   * principale della cache del sistema applicazione.
   * (/var/nomeapp/cache per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkCacheFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale dei documenti del sistema applicazione.
   * (/var/nomeapp/doc per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkDocsFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale dei logs.
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getLogFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale di appoggio del sistema applicazione.
   * (/var/nomeapp per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkMainFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale di spool del sistema applicazione.
   * (/var/nomeapp/spool per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkSpoolFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale dei temporanei del sistema applicazione.
   * (/var/nomeapp/tmp per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkTmpFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * principale dei report di newstar.
   * (/var/newstar/report per UNIX).
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkReportFile(String subFile);

  /**
   * Ritorna un file ubicato nella directory
   * di runtime dei fogli di stile.
   * (/var/caleido/xls per UNIX).
   *
   * @param subFile nome del file SENZA path
   * @return collocazione completa nel file system
   */
  public File getWorkXlsFile(String subFile);
}
