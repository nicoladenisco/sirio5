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
package org.sirio5.services.print;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpSession;
import org.apache.fulcrum.cache.CachedObject;
import org.sirio5.services.CoreServiceExtension;

/**
 * Servizio di stampa via PDF.
 * Il servizio sovrintende alla generazione al
 * volo di PDF da utilizzare come strumento di stampa.
 *
 * @author Nicola De Nisco
 */
public interface PdfPrint extends CoreServiceExtension
{
  public static final String SERVICE_NAME = "PdfPrint";
  //
  /** Costanti */
  public static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
  public static final String CONTENT_TYPE_PDF = "application/pdf";
  public static final String FO_REQUEST_PARAM = "fo";
  public static final String XML_REQUEST_PARAM = "xml";
  public static final String XSL_REQUEST_PARAM = "xsl";
  public static final String ERROR_PAGE_MARKER = "ERROR_PAGE";
  public static final String PRINT_PARAM = "print_param";
  public static final String MODEL = "model";
  public static final String IDSTAT = "idstat";
  public static final String PLUGIN_NAME_FOP = "fop";
  public static final String PLUGIN_NAME_JASPER = "jasper";
  public static final String PATH_INFO = "PATH_INFO";
  public static final String SESSION_ID = "SESSION_ID";
  public static final String QUERY_STRING = "QUERY_STRING";

  /**
   * Descrittore di un job di stampa.
   */
  public static class JobInfo
  {
    public String jobCode = null;     // codice del job
    public int percCompleted = 0;     // percentuale di completamento
    public File filePdf = null;       // relativo file PDF quando completato
    public String saveName = null;    // eventuale nome del PDF da utilizzare nell'header risposta
    public String uri = null;         // uri invocata al momento della richiesta
    public Throwable error = null;    // descrizione di un eventuale errore di elaborazione
    public Date tStart = null;        // istante di avvio elaborazione
    public int idUser = 0;            // utente che ha richiesto la stampa
    public String tipoMime;           // tipo mime del file prodotto
    public String printer;            // eventuale stampante per invio diretto
  }

  /**
   * Restituisce un bean con i parametri per la stampa richiesta.
   * @param idUser
   * @param codiceStampa codice dalla stampa richiesta
   * @param context parametri accessori
   * @return eventuali campi per il report
   * @throws java.lang.Exception errore durante la stampa
   * @throws IllegalAccessException utente non autorizzato
   */
  public AbstractReportParametersInfo getParameters(int idUser, String codiceStampa, PrintContext context)
     throws Exception, IllegalAccessException;

  /**
   * Avvia il processo di stampa di un job.
   * @param idUser
   * @param codiceStampa codice dalla stampa richiesta
   * @param context parametri accessori
   * @param sessione
   * @return il descrittore informazioni sul job
   * @throws java.lang.Exception errore durante la stampa
   * @throws IllegalAccessException utente non autorizzato
   */
  public JobInfo generatePrintJob(int idUser,
     String codiceStampa, PrintContext context, HttpSession sessione)
     throws Exception, IllegalAccessException;

  /**
   * Avvia il processo di stampa di un job.
   * @param idUser utente che richiede la stampa
   * @param pluginName tipo del plugin richiesto
   * @param reportName nome del report richiesto
   * @param reportInfo informazioni supplementari per il plugin
   * @param context parametri accessori
   * @param sessione
   * @return il descrittore informazioni sul job
   * @throws java.lang.Exception errore durante la stampa
   * @throws IllegalAccessException utente non autorizzato
   */
  public JobInfo generatePrintJob(int idUser,
     String pluginName, String reportName, String reportInfo, PrintContext context, HttpSession sessione)
     throws Exception, IllegalAccessException;

  /**
   * Reperisce informazioni aggiornate sul job in avanzamento.
   * @param jobCode
   * @return
   * @throws java.lang.Exception
   */
  public JobInfo refreshInfo(String jobCode)
     throws Exception;

  /**
   * Ritorna un iteratore sui job in elaborazione.
   * @return
   * @throws Exception
   */
  public Iterator<CachedObject> getJobs()
     throws Exception;
}
