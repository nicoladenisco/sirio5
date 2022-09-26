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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.*;
import org.apache.fulcrum.cache.CachedObject;
import org.sirio5.services.AbstractCoreBaseService;
import org.sirio5.services.cache.CACHE;
import org.sirio5.services.print.plugin.PdfGenPlugin;
import org.sirio5.services.print.plugin.PdfGeneratorFactory;

/**
 * Implementazione standard del servizio
 * di generazione PDF (ovvero stampe via PDF).
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractPdfPrint extends AbstractCoreBaseService
   implements PdfPrint
{
  /** Logging */
  private static Log log = LogFactory.getLog(AbstractPdfPrint.class);
  public static final String CACHE_CLASS_PARAM_INFO = "AbstractPdfPrint:CACHE_CLASS_PARAM_INFO";
  //
  /** variabili locali */
  protected File dirTmp = null; // directory per i temporanei
  protected String xmlbaseuri = null;
  protected boolean enableCache = false;

  @Override
  public void coreInit()
     throws Exception
  {
    String s;
    Configuration cfg = getConfiguration();

    enableCache = cfg.getBoolean("enableCache", true);

    dirTmp = getWorkTmpFile("pdfprint");
    dirTmp.mkdirs();
    ASSERT_DIR_WRITE(dirTmp);

    PdfGeneratorFactory.getInstance().configure(cfg, dirTmp);
  }

  @Override
  public AbstractReportParametersInfo getParameters(int idUser, String codiceStampa, Map params)
     throws Exception
  {
    AbstractReportParametersInfo info = null;
    if(enableCache && (info = (AbstractReportParametersInfo) CACHE.getContentQuiet(CACHE_CLASS_PARAM_INFO, codiceStampa)) != null)
      return info;

    info = createReportInfo(codiceStampa);

    PdfGenPlugin plg = PdfGeneratorFactory.getInstance().build(info.getPlugin());
    plg.getParameters(idUser, info.getNome(), info.getInfo(), params, info);

    CACHE.addContent(CACHE_CLASS_PARAM_INFO, codiceStampa, info);
    return info;
  }

  @Override
  public JobInfo generatePrintJob(int idUser, String codiceStampa, Map params, HttpSession sessione)
     throws Exception
  {
    JobInfo info = new JobInfo();
    info.idUser = idUser;
    AbstractReportParametersInfo ri = getParameters(idUser, codiceStampa, params);
    info.filePdf = makePdf(info, idUser, ri.getPlugin(), ri.getNome(), ri.getInfo(), params, ri, sessione);
    info.percCompleted = 100;
    return info;
  }

  @Override
  public JobInfo generatePrintJob(int idUser, String pluginName, String reportName, String reportInfo, Map params, HttpSession sessione)
     throws Exception
  {
    JobInfo info = new JobInfo();
    info.idUser = idUser;
    info.filePdf = makePdf(info, idUser, pluginName, reportName, reportInfo, params, null, sessione);
    info.percCompleted = 100;
    return info;
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
    return null;
  }

  protected File makePdf(JobInfo job,
     int idUser, String pluginName, String reportName, String reportInfo,
     Map params, AbstractReportParametersInfo pbean, HttpSession sessione)
     throws Exception
  {
    log.info("Avvita elaborazione job per plugin=" + pluginName + " reportName=" + reportName);
    PdfGenPlugin plg = PdfGeneratorFactory.getInstance().build(pluginName);

    if(pbean == null)
    {
      pbean = new DirectReportParametersInfo(reportName, reportInfo);
      plg.getParameters(idUser, reportName, reportInfo, params, pbean);
    }

    // imposta il tipo mime di default (PDF)
    job.tipoMime = CONTENT_TYPE_PDF;
    if(reportName != null)
      job.saveName = reportName + ".pdf";

    File pdfFile = getTmpFile();
    plg.buildPdf(job, idUser, reportName, reportInfo, params, pbean, pdfFile, sessione);
    log.info("Elaborazione job per plugin=" + pluginName + " reportName=" + reportName
       + " conclusa con successo.");

    return pdfFile;
  }

  protected File getTmpFile()
     throws Exception
  {
    File ftmp = File.createTempFile("pdfmaker", ".tmp", dirTmp);
    ftmp.deleteOnExit();
    return ftmp;
  }

  @Override
  public Iterator<CachedObject> getJobs()
     throws Exception
  {
    return null;
  }

  abstract protected AbstractReportParametersInfo createReportInfo(String codiceStampa)
     throws Exception;
}
