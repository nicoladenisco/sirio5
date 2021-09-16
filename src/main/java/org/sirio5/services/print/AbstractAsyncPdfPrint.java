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

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.cache.CachedObject;
import org.sirio5.services.cache.CACHE;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.SU;

/**
 * Servizio di stampa via PDF.
 * Il servizio sovrintende alla generazione al
 * volo di PDF da utilizzare come strumento di stampa.
 * Questa versione del servizio elabora ogni PDF all'interno
 * di un thread dedicato, consentendo di tornare se l'elaborazione
 * diventa troppo lunga.
 * Quando in JobInfo il campo filePdf è a null vuol dire
 * che l'elaborazione è in corso. La servlet che usa il servizio
 * può notificare l'utente e invitarlo a riprovare la richiesta.
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractAsyncPdfPrint extends AbstractPdfPrint
{
  /** Logging */
  private static Log log = LogFactory.getLog(AbstractAsyncPdfPrint.class);
  //
  // costanti
  public static final String CACHE_CLASS = "PDFJOBS";
  //
  /** variabili locali */
  protected int tWaitSeconds = 10; // tempo di attesa prima che il thread ritorni
  protected int tExpiresSeconds = 600; // tempo di eliminazione dalla cache

  @Override
  public void coreInit()
     throws Exception
  {
    super.coreInit();

    Configuration cfg = getConfiguration();
    tWaitSeconds = cfg.getInt("tWaitSeconds", tWaitSeconds);
    tExpiresSeconds = cfg.getInt("tExpiresSeconds", tExpiresSeconds);
  }

  /**
   * Ritorna un gestore job asincroni.
   * Ridefinibile in classi derivate per generare istanze più specifiche.
   * @return istanza del job
   */
  protected AsyncPdfJob createJob()
  {
    return new AsyncPdfJob();
  }

  /**
   * Avvia il processo di stampa di un job.
   * @param idUser
   * @param codiceStampa codice dalla stampa richiesta
   * @param params parametri per la stampa
   * @return il descrittore informazioni sul job
   * @throws java.lang.Exception
   */
  @Override
  public JobInfo generatePrintJob(int idUser, String codiceStampa, Map params)
     throws Exception
  {
    if(SU.checkTrueFalse(params.get("SYNC_REQUEST"), false))
      return super.generatePrintJob(idUser, codiceStampa, params);

    AsyncPdfJob job = createJob();
    AbstractReportParametersInfo ri = getParameters(idUser, codiceStampa, params);

    job.init(this, idUser, ri.getPlugin(), ri.getNome(), ri.getInfo(), params, ri);
    job.start();
    job.join(tWaitSeconds * 1000);

    if(job.isRunning())
      addJobInCache(job);

    return job.getInfo();
  }

  /**
   * Avvia il processo di stampa di un job.
   * @param idUser
   * @param pluginName tipo del plugin richiesto
   * @param reportName nome del report richiesto
   * @param reportInfo
   * @param params parametri accessori del plugin
   * @return il descrittore informazioni sul job
   * @throws java.lang.Exception
   */
  @Override
  public JobInfo generatePrintJob(int idUser, String pluginName,
     String reportName, String reportInfo, Map params)
     throws Exception
  {
    if(SU.checkTrueFalse(params.get("SYNC_REQUEST"), false))
      return super.generatePrintJob(idUser, pluginName, reportName, reportInfo, params);

    AsyncPdfJob job = createJob();

    job.init(this, idUser, pluginName, reportName, reportInfo, params, null);
    job.start();
    job.join(tWaitSeconds * 1000);

    if(job.isRunning())
      addJobInCache(job);

    return job.getInfo();
  }

  /**
   * Reperisce informazioni aggiornate sul job in avanzamento.
   * @param jobCode
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public JobInfo refreshInfo(String jobCode)
     throws Exception
  {
    try
    {
      CachedObject obj = CACHE.getObject(CACHE_CLASS, jobCode);
      if(obj == null || obj.isStale())
        return null;

      AsyncPdfJob job = (AsyncPdfJob) obj.getContents();
      return job.getInfo();
    }
    catch(Exception e)
    {
    }

    return null;
  }

  @Override
  public Iterator<CachedObject> getJobs()
     throws Exception
  {
    return CACHE.cachedObjects(CACHE_CLASS);
  }

  static class AsyncPdfJobCacheItem extends CachedObject
  {
    public AsyncPdfJobCacheItem(AsyncPdfJob job, long expireSeconds)
    {
      super(job, expireSeconds * 1000);
    }

    @Override
    public synchronized boolean isStale()
    {
      AsyncPdfJob job = (AsyncPdfJob) getContents();
      if(job.isRunning())
      {
        refresh();
        return false;
      }

      return super.isStale();
    }

    public void refresh()
    {
      this.created = System.currentTimeMillis();
    }
  }

  protected void addJobInCache(AsyncPdfJob job)
     throws Exception
  {
    String jobCode = job.getInfo().jobCode;
    CACHE.addObject(CACHE_CLASS, jobCode,
       new AsyncPdfJobCacheItem(job, tExpiresSeconds));
    log.debug(INT.I("Aggiunto job %s alla cache.", jobCode));
  }
}
