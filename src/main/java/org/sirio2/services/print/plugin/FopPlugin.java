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
package org.sirio2.services.print.plugin;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.*;
import org.apache.turbine.Turbine;
import org.commonlib.exec.ExecHelper;
import org.commonlib.utils.CommonFileUtils;
import org.commonlib.utils.OsIdent;
import org.sirio2.services.print.AbstractReportParametersInfo;
import org.sirio2.services.print.PdfPrint;
import org.sirio2.services.print.XmlGenerationError;
import org.sirio2.utils.LI;
import org.sirio2.utils.SU;
import org.sirio2.utils.TR;

/**
 * Generatore di pdf via fop.
 *
 * @author Nicola De Nisco
 */
public class FopPlugin extends BasePdfPlugin
{
  /** Logging */
  private static Log log = LogFactory.getLog(FopPlugin.class);
  protected String xmlbaseuri = null;
  // posizione dell'applicazione fop
  protected String fopAppLocation = null;

  @Override
  public void configure(String pluginName, Configuration cfg, File dirTmp)
     throws Exception
  {
    super.configure(pluginName, cfg, dirTmp);
    xmlbaseuri = cfg.getString("xmlbaseuri", null);

    // legge locazione dell'applicazione fopApp (NON DAL SERVIZIO)
    if((fopAppLocation = TR.getString("path.app.fop")) == null)
      die("Directory processore Fop non dichiarata a setup (vedi aaa-generic..): stampa non disponibile.");
  }

  @Override
  public void buildPdf(PdfPrint.JobInfo job, int idUser,
     String reportName, String reportInfo, Map params,
     AbstractReportParametersInfo pbean, File pdfToGen)
     throws Exception
  {
    // estrae i soli parametri relativi al report sotto forma di stringa
    Map reportParams = null;
    if(pbean != null)
      reportParams = pbean.parseParameterString(params);

    doGetXmlReport(reportName, params, pdfToGen, reportParams);
  }

  //Process the HTTP Get request
  protected File doGetXmlReport(String sJsp, Map params, File fPdf, Map reportParams)
     throws Exception
  {
    File fopSheetDir = print.getConfXlsFile("fop");
    if(!fopSheetDir.isDirectory())
      die("La directory " + fopSheetDir + " non esiste o non è leggibile.");

    // esrae nome della JSP per la generazione dell'XML
    print.ASSERT(sJsp != null, "sJsp != null");
    if(!sJsp.endsWith(".jsp"))
      sJsp += ".jsp";

    // estrae nome del file XSL per conversione dati XML
    File xslFile = null;
    {
      String xslParam = (String) params.get(PdfPrint.XSL_REQUEST_PARAM);

      // se non e' stato specificato il foglio di stile
      // cerca di individuarne uno con nomejsp.xsl oppure nomejsp2fop.xsl
      if(xslParam == null)
      {
        String sNomeJsp = sJsp.substring(0, sJsp.indexOf(".jsp"));

        xslFile = new File(fopSheetDir, sNomeJsp + ".xsl");
        if(!xslFile.exists())
          xslFile = new File(fopSheetDir, sNomeJsp + "2fop.xsl");

        if(!xslFile.canRead())
          xslFile = null;
      }
      else
      {
        // protezione contro hacking della path: prende il solo nome del file
        String fileName = FilenameUtils.getName(xslParam);
        xslFile = new File(fopSheetDir, fileName);
        print.ASSERT_FILE(xslFile);
      }
    }

    String sessionid = (String) params.get(PdfPrint.SESSION_ID);
    print.ASSERT(sessionid != null, "sessionid != null");

    // costruisce url per chiamare la JSP per generare l'XML
    // qualcosa del tipo 'http://localhost:8080/nomeapp/xml/mia.jsp;jsessionid=identificativosessione?param1=val1...'
    // NOTA: indipendentemente da come e' chiamata questa servlet
    // la chiamata alla JSP viene effettuata comunque su localhost:8080
    // ovvero direttametne all'istanza di Tomcat su cui viene eseguita questa servlet
    String sUrl = null;
    if(xmlbaseuri == null || SU.isEqu("AUTO", xmlbaseuri))
      sUrl = LI.mergePath("http://localhost:" + getTomcatHttpPort(),
         Turbine.getContextPath());
    else
      sUrl = xmlbaseuri;

    sUrl = LI.mergePath(sUrl, "xml/" + sJsp + ";jsessionid=" + sessionid);

    if(reportParams == null || reportParams.isEmpty())
    {
      String query = (String) params.get(PdfPrint.QUERY_STRING);
      if(query != null)
        sUrl += "?" + query;
    }
    else
    {
      // aggiunge eventuali parametri dal form del report
      sUrl = LI.mergeUrl(sUrl, reportParams);
    }

    log.info("sURL=" + sUrl);

    // costruisce file temporaneo con l'XML della stampa
    File fXml = getTmpFile();
    fXml.deleteOnExit();
    log.info("sXmlFile=" + fXml.getAbsolutePath());

    // invoca la jsp e il risultato viene salvato in fXml
    CommonFileUtils.readUrlToFile(sUrl, fXml);

    if(fXml.length() < 4096 && CommonFileUtils.findStringInFile(
       PdfPrint.ERROR_PAGE_MARKER, fXml, "UTF-8") != -1)
    {
      // visualizza pagina d'errore
      throw new XmlGenerationError("Generazione XML non avvenuta.", fXml);
    }
    else
    {
      if(xslFile == null)
      {
        // rendering jas diretto
        runExternalFopRender(fPdf, fXml, null);
      }
      else
      {
        // rendering con trasformazione
        runExternalFopRender(fPdf, fXml, xslFile);
      }

      // rimozione temporaneo
      fXml.delete();
    }

    log.info("Pdfmaker: OK " + fPdf.getAbsolutePath());
    return fPdf;
  }

  /**
   * Chiama la trasformazione come eseguibile esterno
   * in modo da evitare memory leaks e sfruttare tutta
   * la memoria del sistema.
   * @param fpdf file da generare
   * @param fxml file xml con i dati da renderizzare
   * @param fxsl eventuale foglio di stile (puo' essere null)
   * @throws java.lang.Exception
   */
  protected void runExternalFopRender(File fpdf, File fxml, File fxsl)
     throws Exception
  {
    String fop = null;
    switch(OsIdent.checkOStype())
    {
      case OsIdent.OS_WINDOWS:
        fop = "fop.bat";
        break;
      default:
        fop = "fop";
        break;
    }

    File fopDir = new File(fopAppLocation);
    if(!fopDir.exists() || !fopDir.isDirectory())
      die("Fop non installato in " + fopDir.getAbsolutePath());

    File fopPgm = new File(fopDir, fop);
    if(!fopPgm.exists())
      die("Programma di lancio fop non trovato: rivedere installazione fop.");

    ArrayList<String> arStr = new ArrayList<String>();
    arStr.add(fopPgm.getAbsolutePath());

    // aggiunge i parametri opportuni
    if(fxsl == null)
    {
      arStr.add("-fo");
      arStr.add(fxml.getAbsolutePath());
      arStr.add("-pdf");
      arStr.add(fpdf.getAbsolutePath());
    }
    else
    {
      arStr.add("-xml");
      arStr.add(fxml.getAbsolutePath());
      arStr.add("-xsl");
      arStr.add(fxsl.getAbsolutePath());
      arStr.add("-pdf");
      arStr.add(fpdf.getAbsolutePath());
    }

    String[] pgm = new String[arStr.size()];
    arStr.toArray(pgm);

    log.info("Lancio " + SU.join(pgm, ' '));
    ExecHelper eh = ExecHelper.exec(pgm);

    log.debug("-STDOUT-------------------------------------");
    log.debug(eh.getOutput());
    log.debug("-STDERR-------------------------------------");
    log.debug(eh.getError());

    int exitValue = eh.getStatus();

    if(exitValue != 0)
      die("Rendering FOP del PDF non completato. Vedi log per errori.");
  }

  private String getTomcatHttpPort()
  {
    try
    {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
         Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));

      for(Iterator<ObjectName> i = objs.iterator(); i.hasNext();)
      {
        ObjectName obj = i.next();
        String scheme = mbs.getAttribute(obj, "scheme").toString();
        String port = obj.getKeyProperty("port");

        if("http".equalsIgnoreCase(scheme))
          return port;
      }
    }
    catch(Exception ex)
    {
      log.error("", ex);
    }

    return Turbine.getServerPort();
  }
}
