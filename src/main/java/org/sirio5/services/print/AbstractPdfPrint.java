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
import org.sirio5.services.print.datamaker.DatamakerGeneratorFactory;
import org.sirio5.services.print.parametri.ParametroBuilderFactory;
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
  private static final Log log = LogFactory.getLog(AbstractPdfPrint.class);
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
    Configuration cfg = getConfiguration();

    enableCache = cfg.getBoolean("enableCache", true);
    PdfGeneratorFactory.getInstance().configure(cfg);
    ParametroBuilderFactory.getInstance().configure(cfg);
    DatamakerGeneratorFactory.getInstance().configure(cfg);

    PdfGeneratorFactory.getInstance().addBasePath("org.sirio5.services.print.plugin");
    ParametroBuilderFactory.getInstance().addBasePath("org.sirio5.services.print.parametri");
    DatamakerGeneratorFactory.getInstance().addBasePath("org.sirio5.services.print.datamaker");

    dirTmp = getWorkTmpFile("print");
    ASSERT_DIR_WRITE(dirTmp);
  }

  @Override
  public AbstractReportParametersInfo getParameters(int idUser, String codiceStampa, PrintContext context)
     throws Exception
  {
    {
      AbstractReportParametersInfo info = null;
      if(enableCache && (info
         = (AbstractReportParametersInfo) CACHE.getContentQuiet(CACHE_CLASS_PARAM_INFO, codiceStampa)) != null)
        return info;
    }

    AbstractReportParametersInfo f_info = createReportInfo(codiceStampa);
    context.put(PrintContext.PBEAN_KEY, f_info);
    PdfGeneratorFactory.getInstance().runPlugin(f_info.getPlugin(), (plg) -> plg.getParameters(idUser, context));

    CACHE.addContent(CACHE_CLASS_PARAM_INFO, codiceStampa, f_info);
    return f_info;
  }

  @Override
  public JobInfo generatePrintJob(int idUser,
     String codiceStampa, PrintContext context, HttpSession sessione)
     throws Exception
  {
    AbstractReportParametersInfo ri = getParameters(idUser, codiceStampa, context);
    context.put(PrintContext.PBEAN_KEY, ri);
    context.put(PrintContext.REPORT_INFO_KEY, ri.getInfo());
    context.put(PrintContext.REPORT_NAME_KEY, ri.getNome());
    context.put(PrintContext.SESSION_KEY, sessione);

    JobInfo info = new JobInfo();
    info.idUser = idUser;
    info.filePdf = makePdfInternal(info, idUser, ri.getPlugin(), ri.getDataMaker(), context);
    info.percCompleted = 100;
    return info;
  }

  @Override
  public JobInfo generatePrintJob(int idUser,
     String pluginName, String reportName, String reportInfo, PrintContext context, HttpSession sessione)
     throws Exception
  {
    DirectReportParametersInfo pbean = new DirectReportParametersInfo(reportName, reportInfo);
    context.put(PrintContext.PBEAN_KEY, pbean);
    context.put(PrintContext.REPORT_INFO_KEY, reportInfo);
    context.put(PrintContext.REPORT_NAME_KEY, reportName);
    context.put(PrintContext.SESSION_KEY, sessione);

    JobInfo info = new JobInfo();
    info.idUser = idUser;
    info.filePdf = makePdfInternal(info, idUser, pluginName, null, context);
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

  protected File makePdfInternal(JobInfo job, int idUser, String pluginName, String dataMaker, PrintContext ctx)
     throws Exception
  {
    if(dataMaker != null)
    {
      // usa il datamaker per preparare i dati per il rendering
      Object data = DatamakerGeneratorFactory.getInstance().functionPlugin(dataMaker, (dm) -> dm.prepareData(ctx));
      if(data != null)
        ctx.put(PrintContext.PREPARED_DATA_KEY, data);
    }

    return PdfGeneratorFactory.getInstance()
       .functionPlugin(pluginName, (plg) -> makePdfWorker(plg, job, idUser, pluginName, ctx));
  }

  private File makePdfWorker(PdfGenPlugin plg, JobInfo job, int idUser, String pluginName, PrintContext context)
     throws Exception
  {
    String reportName = context.getAsString(PrintContext.REPORT_NAME_KEY);

    // imposta il tipo mime di default (PDF)
    job.tipoMime = CONTENT_TYPE_PDF;
    if(reportName != null)
      job.saveName = reportName + ".pdf";

    File pdfFile = getTmpFile();
    context.put(PrintContext.PDFTOGEN_KEY, pdfFile);
    plg.buildPdf(job, idUser, context);

    log.info("Elaborazione job per plugin=" + pluginName + " reportName=" + reportName
       + " conclusa con successo.");

    return pdfFile;
  }

  protected File getTmpFile()
     throws Exception
  {
    if(!dirTmp.isDirectory())
      if(!dirTmp.mkdirs())
        throw new IOException("Impossibile creare la directory " + dirTmp.getAbsolutePath());

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
