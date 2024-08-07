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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonlib5.exec.ExecHelper;
import org.commonlib5.utils.OsIdent;
import org.sirio5.services.localization.INT;
import org.sirio5.services.print.PdfPrint.JobInfo;
import org.sirio5.utils.SU;

/**
 * Ogni istanza di questa classe rappresenta un job
 * in esecuzione per realizzare un PDF.
 *
 * @author Nicola De Nisco
 */
public class AsyncPdfJob
{
  /** Logging */
  private static final Log log = LogFactory.getLog(AsyncPdfJob.class);
  // variabili locali
  protected JobInfo info = null;
  protected Thread thRun = null;
  protected PrintContext ctx;
  protected AbstractAsyncPdfPrint service = null;
  protected String pluginName;

  public void init(AbstractAsyncPdfPrint service, int idUser, String pluginName, PrintContext ctx)
     throws Exception
  {
    this.service = service;
    this.pluginName = pluginName;
    this.ctx = ctx;

    info = new JobInfo();
    info.jobCode = generateJobCode();
    info.uri = "PLG:" + pluginName + "|RN:" + ctx.getAsString(PrintContext.REPORT_NAME_KEY);
    info.tStart = new Date();
    info.idUser = idUser;
  }

  public void start()
     throws Exception
  {
    if(info == null)
      throw new IllegalStateException(INT.I("Job non inizializzato: usare prima init()."));

    if(isRunning())
      throw new IllegalStateException(INT.I("Job %s già in elaborazione.", info.jobCode));

    thRun = new Thread(() -> runJob());
    thRun.setDaemon(true);
    thRun.setName("AsyncPdfJob_" + info.jobCode);
    thRun.start();
  }

  public boolean isRunning()
  {
    return thRun != null && thRun.isAlive();
  }

  protected void runJob()
  {
    try
    {
      AbstractReportParametersInfo pbean = (AbstractReportParametersInfo) ctx.get(PrintContext.PBEAN_KEY);
      String dataMaker = pbean == null ? null : pbean.getDataMaker();

      info.filePdf = service.makePdfInternal(info, info.idUser, pluginName, dataMaker, ctx);
      info.percCompleted = 100;

      if(info.printer != null)
        manageDirectPrint();
    }
    catch(Exception e)
    {
      info.error = e;
      log.error(INT.I("Grave errore nell'elaborazione del job %s", info.uri), e);
    }
  }

  protected String generateJobCode()
  {
    return "JOB" + System.currentTimeMillis();
  }

  public JobInfo getInfo()
  {
    return info;
  }

  public void setPrinter(String printerName)
  {
    info.printer = SU.okStrNull(printerName);
  }

  public void join(long timeout)
     throws InterruptedException
  {
    if(isRunning())
      thRun.join(timeout);
  }

  protected void manageDirectPrint()
     throws Exception
  {
    if(!OsIdent.isUnix())
      throw new Exception(INT.I("Stampa diretta a stampante di sistema richiede sistema Unix."));

    // invio diretto a stampante di sistema
    String cmd = "lp -d '" + info.printer + "' -t '" + info.saveName + "' '" + info.filePdf.getAbsolutePath() + "'";
    log.debug("Invio stampa diretta: " + cmd);
    ExecHelper.execUsingShell(cmd);
  }
}
