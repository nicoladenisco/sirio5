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
package org.sirio5.services.print.plugin;

import java.io.*;
import java.util.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.*;
import org.apache.torque.Torque;
import org.commonlib5.exec.ExecHelper;
import org.commonlib5.utils.CommonFileUtils;
import org.commonlib5.utils.OsIdent;
import org.commonlib5.utils.PropertyManager;
import org.sirio5.services.CoreServiceException;
import org.sirio5.services.localization.INT;
import org.sirio5.services.print.AbstractReportParametersInfo;
import org.sirio5.services.print.PdfPrint;
import org.sirio5.services.print.PrintContext;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Plugin di stampa per la gestione dei report generati con jasper.
 *
 * Usa il renderizzatore estreno per produrre il PDF dal reporto jasper.
 * ATTENZIONE: è necessario il sorgente del reporto (.jrxml) per poter estrarre
 * le informazioni sui campi utilizzati come parametri.
 *
 * @author Nicola De Nisco
 */
public class JasperPlugin extends BasePdfPlugin
{
  public static final String PLG_NAME_CON = "jasper";
  public static final String PLG_NAME_NOCON = "jaspernc";
  /** Logging */
  private static final Log log = LogFactory.getLog(JasperPlugin.class);
  //
  // flag per attivare o meno le connessioni con il database
  protected boolean useDB = true;
  // posizione applicazione esterna jasper
  protected String jasperAppLocation = null;

  @Override
  public void configure(String pluginName, Configuration cfg)
     throws Exception
  {
    super.configure(pluginName, cfg);
    useDB = SU.isEqu(PLG_NAME_CON, pluginName);

    // legge locazione dell'applicazione jasperApp (NON DAL SERVIZIO)
    if((jasperAppLocation = TR.getString("path.app.jas")) == null)
      die(INT.I("Directory processore Jasper non dichiarata a setup (vedi aaa-generic..): stampa non disponibile."));
  }

  @Override
  public void getParameters(int idUser, PrintContext context)
     throws Exception
  {
    String reportName = context.getAsString(PrintContext.REPORT_NAME_KEY);
    String reportInfo = context.getAsString(PrintContext.REPORT_INFO_KEY);
    AbstractReportParametersInfo pbean = (AbstractReportParametersInfo) context.get(PrintContext.PBEAN_KEY);

    File reportFile = getFileReport(reportName, reportInfo);
    pbean.initForJasper(idUser, reportName, reportFile, context);
  }

  @Override
  public void buildPdf(PdfPrint.JobInfo job, int idUser, PrintContext context)
     throws Exception
  {
    String reportName = context.getAsString(PrintContext.REPORT_NAME_KEY);
    String reportInfo = context.getAsString(PrintContext.REPORT_INFO_KEY);
    AbstractReportParametersInfo pbean = (AbstractReportParametersInfo) context.get(PrintContext.PBEAN_KEY);
    File reportFile = getFileReport(reportName, reportInfo);

    // estrae i soli parametri relativi al report
    // opportunamente parserizzati rispetto al tipo
    Map reportParams = pbean.parseParameter(context);

    job.saveName = reportName + ".pdf";
    saveReportPdf(reportFile, context, reportParams);
  }

  protected File getFileReport(String reportName, String reportInfo)
     throws Exception
  {
    // determina il nome del file jasper che di solito è in reportInfo
    // in caso di sintassi diretta (/.../pdf/jasper/reportName) allora
    // il nome del file jasper potrebbe essere reportName
    String reportFileName = SU.okStrAny(reportInfo, reportName);

    if(reportFileName == null)
      die(INT.I("Il file del report (colonna info) non è specificato o non è valido."));

    // cambia o aggiunge l'estensione al file
    String riJrx = CommonFileUtils.changeFilenameExtension(reportFileName, "jrxml");

    File reportFile = print.getWorkReportFile("jrxml/" + riJrx);
    if(reportFile.canRead())
      return reportFile;

    reportFile = print.getWorkReportFile(riJrx);
    if(reportFile.canRead())
      return reportFile;

    reportFile = print.getConfReportFile("jrxml/" + riJrx);
    if(reportFile.canRead())
      return reportFile;

    reportFile = print.getConfReportFile(riJrx);
    if(reportFile.canRead())
      return reportFile;

    throw new CoreServiceException(INT.I("Il file %s non esiste o non può essere letto.", riJrx));
  }

  /**
   * Takes 4 reportParams: jdbcConnection, reportFile, reportPDF, reportParams
   * @param reportFile file di modello del report Jasper Report (.jrxml)
   * @param context parametri
   * @param reportParams parametri da utilizzare per la creazione del report.
   * @throws java.lang.Exception
   */
  protected void saveReportPdf(File reportFile, PrintContext context, Map reportParams)
     throws Exception
  {
    File reportPDF = (File) context.get(PrintContext.PDFTOGEN_KEY);

    // usiamo la versione esterna: salva i parametri in un file binario su disco
    File tmpParams = getTmpFile();

    if(log.isDebugEnabled())
    {
      PropertyManager pm = new PropertyManager();
      pm.addAll(reportParams);
      try (FileOutputStream fos = new FileOutputStream(tmpParams.getAbsolutePath() + ".debug"))
      {
        pm.save(fos);
      }
    }

    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpParams)))
    {
      oos.writeObject(reportParams);
    }

    if(!useDB)
    {
      // nessuna connessione al db: bastano 3 parametri
      String[] cmdArray =
      {
        reportFile.getAbsolutePath(), tmpParams.getAbsolutePath(),
        reportPDF.getAbsolutePath()
      };
      runExternalJasperRender(cmdArray, reportPDF);
    }
    else
    {
      // recuperiamo dal settaggio di Torque i dati per la connessione al db
      Configuration cfg = Torque.getConfiguration();
      String dbDriver = cfg.getString("defaults.connection.driver", "org.postgresql.Driver");
      String dbUri = cfg.getString("defaults.connection.url", "jdbc:localhost:sirio");
      String dbUser = cfg.getString("defaults.connection.user", "sirio");
      String dbPass = cfg.getString("defaults.connection.password", "sirio");

      // eseguiamo chiamata con 6 parametri
      String[] cmdArray =
      {
        reportFile.getAbsolutePath(), tmpParams.getAbsolutePath(),
        reportPDF.getAbsolutePath(), dbDriver, dbUri, dbUser, dbPass
      };

      runExternalJasperRender(cmdArray, reportPDF);
    }
  }

  protected void runExternalJasperRender(String args[], File reportPDF)
     throws Exception
  {
    print.ASSERT(jasperAppLocation != null, "jasperAppLocation != NULL");

    File jasDir = new File(jasperAppLocation);
    if(!jasDir.exists() || !jasDir.isDirectory())
      die(INT.I("jasperApp non installato in %s ", jasDir.getAbsolutePath()));

    String jas = null;
    switch(OsIdent.checkOStype())
    {
      case OsIdent.OS_WINDOWS:
        jas = "jas.bat";
        break;
      default:
        jas = "jas";
        break;
    }

    File jasPgm = new File(jasDir, jas);
    if(!jasPgm.exists())
      die(INT.I("Programma di lancio jasper-reports non trovato: rivedere installazione."));

    String pgm = jasPgm.getAbsolutePath();
    String[] cmd = (String[]) ArrayUtils.add(args, 0, pgm);

    log.info("Lancio " + pgm);
    ExecHelper eh = ExecHelper.exec(cmd);

    int exitValue = eh.getStatus();

    if(exitValue != 0 || !reportPDF.canRead() || reportPDF.length() == 0)
    {
      log.error("-STDOUT-------------------------------------");
      log.error(eh.getOutput());
      log.error("-STDERR-------------------------------------");
      log.error(eh.getError());
      die(INT.I("Rendering jasper del PDF non completato. Vedi log per errori."));
    }

    if(log.isDebugEnabled())
    {
      log.debug("-STDOUT-------------------------------------");
      log.debug(eh.getOutput());
      log.debug("-STDERR-------------------------------------");
      log.debug(eh.getError());
    }
  }
}
