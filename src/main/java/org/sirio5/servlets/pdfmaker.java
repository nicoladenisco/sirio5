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
package org.sirio5.servlets;

// Java
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.*;
import org.apache.turbine.services.TurbineServices;
import org.apache.velocity.context.Context;
import org.sirio5.rigel.RigelHtmlI18n;
import org.sirio5.services.localization.INT;
import org.sirio5.services.print.AsyncPdfRunningException;
import org.sirio5.services.print.DirectPrintException;
import org.sirio5.services.print.MessagePrintException;
import org.sirio5.services.print.PdfPrint;
import org.sirio5.services.print.PrintContext;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.FU;
import org.sirio5.utils.LI;
import org.sirio5.utils.SU;
import org.sirio5.utils.pdf.PDFutils;
import org.sirio5.utils.velocity.VelocityParser;

/**
 * <p>
 * Title: Servlet di supporto per la generazione di PDF al volo.</p>
 * <p>
 * Description: questa servlet ha il compito di assistere la generazione
 * la volo di PDF utilizzati per le stampe on line di Newstar.</p>
 * <p>
 * La generazione dei PDF avviene utilizzando per ogni stampa una
 * corrispettiva JSP che genera i dati XML necessari. Questi uniti ad
 * un opportuno foglio di stile XSL vengono elaborati dall'applicazione
 * FOP (vedi www.apache.org) per generare il relativo PDF.</p>
 * <p>
 * Nella configurazione dell'application server viene settato un alias
 * in modo che qualsiasi url che cominci con /pdf/ viene reindirizzato
 * a questa servlet.<br>
 * L'url tipica sara' qualcosa del tipo:<br>
 * <pre>
 * http://server/newstar/pdf/mia.jsp?xsl=mia2fop.xsl&parm1=val1...
 * </pre>
 * questa servlet chiama la JSP indicata salvando i dati XML
 * su un file temporano, quindi utilizza il motore fop per trasformare
 * questo XML in un PDF.
 * </p>
 * @author Nicola De Nisco
 * @version 1.1
 */
public class pdfmaker extends HttpServlet
{
  /** Logging */
  private static final Log pgmlog = LogFactory.getLog(pdfmaker.class);
  //
  // costanti
  public static final String DEFAULT_PLUGIN = "fop";
  public static final int TIME_EXPIRIES = 10000; // 10 secondi
  public static final PdfPrint.JobInfo NULL_JOB = new PdfPrint.JobInfo();
  public static final String CACHE_RICHIESTE_SESSIONE = "CACHE_RICHIESTE_SESSIONE";
  //
  /** variabili locali */
  protected boolean enableGzip = false; // abilita invio compresso
  //
  /** Servizi collegati */
  protected PdfPrint pp = null;

  // Initialize global variables
  @Override
  public void init()
     throws ServletException
  {
    try
    {
      String s;
      if((s = getInitParameter("enableGzip")) != null && s.equals("true"))
        enableGzip = true;
    }
    catch(Exception ex)
    {
      pgmlog.error(ex);
      throw new ServletException(ex);
    }
  }

  // Clean up resources
  @Override
  public void destroy()
  {
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
  {
    doGet(req, resp);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    try
    {
      if(pp == null)
        pp = (PdfPrint) (TurbineServices.getInstance().getService(PdfPrint.SERVICE_NAME));

      doWork(request, response);
    }
    catch(AsyncPdfRunningException ex)
    {
      if(ex.err != null)
        throw new ServletException(INT.I("Errore nell'engine della stampa."), ex.err);

      // elaborazione pdf in corso: notifichiamo l'attesa all'utente
      String url = LI.getLinkUrl("pdfwait.vm") + "?codice=" + ex.job.jobCode;

      // Redirect call to wait page
      response.sendRedirect(url);
    }
    catch(DirectPrintException ex)
    {
      if(ex.err != null)
        throw new ServletException(INT.I("Errore nell'engine della stampa."), ex.err);

      String url;
      if(ex.job.percCompleted == 100)
      {
        // elaborazione pdf completata: redirezione a chiusura popup
        url = LI.getLinkUrl("closeme.vm");
      }
      else
      {
        // elaborazione pdf in corso: notifichiamo l'attesa all'utente
        url = LI.getLinkUrl("pdfdirect.vm") + "?codice=" + ex.job.jobCode;
      }

      // Redirect call to wait page
      response.sendRedirect(url);
    }
    catch(MessagePrintException ex)
    {
      request.getSession().setAttribute("MessagePrintException", ex);

      if(ex.getRedirect() != null)
      {
        response.sendRedirect(ex.getRedirect());
        response.setStatus(502);
        return;
      }

      if(ex.getTemplate() != null)
      {
        elaboraRispostaTemplate(ex, request, response);
        return;
      }

      throw new ServletException(ex);
    }
    catch(ServletException ex)
    {
      pgmlog.error(ex);
      throw ex;
    }
    catch(Exception ex)
    {
      pgmlog.error(ex);
      throw new ServletException(ex);
    }
  }

  protected void doWork(HttpServletRequest request, HttpServletResponse response)
     throws Exception
  {
    // estrae nome della richiesta
    String sRequest = request.getPathInfo().substring(1);

    // estrae query string per match della cache
    String query = request.getQueryString();

    // costruisce chiave della cache PDF
    String cacheKey = query == null ? sRequest : sRequest + "?" + query;

    /*
     * PROTEZIONE CONTRO LA SOLITA MERDA DI SOFTWARE MICROSOFT
     * IN QUESTO CASO INTERNET EXPLORER.
     * La suddetta cacca invia richieste multiple al server per la stessa url
     * attivando quindi piu' istanze di trasformazione e di rendering.
     * Quindi qui testiamo che non sia gia' in corso una richiesta
     * per soddisfare il suddetto cesso di browser.
     * Il file PDF ottenuto viene inserito in una cache con tempo
     * di scadenza fissato da TIME_EXPIRIES per cui richieste multiple
     * dallo stesso browser per gli stessi risultati vengono soddisfatti
     * con la cache.
     */
    boolean force = SU.checkTrueFalse(request.getParameter("force"), false);
    Hashtable<String, PdfPrint.JobInfo> htReq = getJobCache(request);
    PdfPrint.JobInfo job = force ? null : htReq.get(cacheKey);

    // se il job è in elaborazione ritorna immediatamente
    if(job != null && job == NULL_JOB)
      return;

    if(job != null && job.filePdf != null)
    {
      // verifica se la cache e' ancora valida
      if(!job.filePdf.exists()
         || (System.currentTimeMillis() - job.filePdf.lastModified()) > TIME_EXPIRIES)
      {
        job.filePdf.delete();
        job = null;
        htReq.remove(cacheKey);
      }
    }

    // controlla per job asincrono in esecuzione
    if(job == null && SU.isEqu("job", sRequest))
    {
      String jobCode = request.getParameter("codice");
      job = checkJobCompleted(jobCode);
    }

    // test per job in elaborazione
    if(job != null && job.filePdf == null)
    {
      String jobCode = job.jobCode;
      job = checkJobCompleted(jobCode);
    }

    if(job == null)
    {
      try
      {
        // inserisce nella cache un marcatore per job in elaborazione
        htReq.put(cacheKey, NULL_JOB);

        // costruisce il PDF in base alla richiesta
        job = makePdf(sRequest, request, response);

        // inserimento nella cache del file PDF
        htReq.put(cacheKey, job);
      }
      catch(AsyncPdfRunningException ex)
      {
        htReq.put(cacheKey, ex.job);
        throw ex;
      }
      catch(Exception e)
      {
        htReq.remove(cacheKey);
        throw e;
      }
    }

    // verifica per job in elaborazione: ritorna immediatamente
    if(job == null || job.filePdf == null)
      return;

    pgmlog.info("Pdfmaker: OK " + job.filePdf.getAbsolutePath());

    // invio del file pdf come risposta
    if(SU.isOkStr(job.tipoMime))
      PDFutils.sendFile(request, response, job.tipoMime, job.filePdf, job.saveName, enableGzip);
    else
      PDFutils.sendFileAsPDF(request, response, job.filePdf, job.saveName, enableGzip);
  }

  /**
   * Recupera dalla sessione la cache dei job di stampa per l'utente.
   * Se non presente viene creata e inserita nei dati di sessione.
   * @param request request http per recuperare la sessione
   * @return chache job
   */
  protected Hashtable<String, PdfPrint.JobInfo> getJobCache(HttpServletRequest request)
  {
    Hashtable<String, PdfPrint.JobInfo> htReq
       = (Hashtable<String, PdfPrint.JobInfo>) request.getSession().getAttribute(CACHE_RICHIESTE_SESSIONE);

    if(htReq == null)
    {
      htReq = new Hashtable<>();
      request.getSession().setAttribute(CACHE_RICHIESTE_SESSIONE, htReq);
    }

    return htReq;
  }

  /**
   * Costruisce il PDF in base al tipo di richiesta.
   *
   * @param sRequest richiesta da processare
   * @param request oggetto request HTTP
   * @param response oggetto response HTTP
   * @return descrittore del job elaborato
   * @throws Exception
   */
  protected PdfPrint.JobInfo makePdf(String sRequest,
     HttpServletRequest request, HttpServletResponse response)
     throws Exception
  {
    PdfPrint.JobInfo info = null;
    PrintContext context = new PrintContext();
    context.setI18n(new RigelHtmlI18n(request));
    context.putAll(SU.getParMap(request));
    int idUser = authRequest(request);

    String mappaParametri = null;
    if((mappaParametri = request.getParameter("special_map")) == null)
      mappaParametri = PdfPrint.PRINT_PARAM;

    // preleva la mappa parametri dalla sessione e la salva in params
    Map parameters = (Map) request.getSession().getAttribute(mappaParametri);
    if(parameters != null)
    {
      context.put(mappaParametri, parameters);
      context.putAll(parameters);
      if(SU.checkTrueFalse(parameters.get("autoremove"), true))
        request.getSession().removeAttribute(mappaParametri);
    }

    context.put(PdfPrint.PATH_INFO, request.getPathInfo());
    context.put(PdfPrint.SESSION_ID, request.getSession().getId());
    context.put(PdfPrint.QUERY_STRING, request.getQueryString());

    // aggiunge al context eventuali informazioni
    info = completaPrintContext(context, sRequest, request, response);

    // estrae il nome del plugin e quello del report dalla richiesta
    // la richiesta è http://server/pdf/plugin/report?param1=val1&...
    // oppure http://server/pdf/codiceStampa?param1=val1&...
    int pos = 0;
    String pluginName = null, reportName = null;
    if((pos = sRequest.indexOf('/')) == -1)
    {
      info = pp.generatePrintJob(idUser, sRequest, context, request.getSession());
    }
    else
    {
      pluginName = sRequest.substring(0, pos);
      reportName = sRequest.substring(pos + 1);
      info = pp.generatePrintJob(idUser, pluginName, reportName, null, context, request.getSession());
    }

    if(info == null)
      throw new Exception(INT.I("Generazione del print job non riuscita."));

    if(info.filePdf == null)
    {
      // elaborazione asincrona del job attivata
      if(info.error == null)
        throw new AsyncPdfRunningException(info);
      else
        throw new AsyncPdfRunningException(info, info.error);
    }

    return info;
  }

  /**
   * Completa o corregge contenuto del context.
   * @param context
   * @param sRequest
   * @param request
   * @param response
   * @return eventuale job (di solito null)
   * @throws Exception
   */
  protected PdfPrint.JobInfo completaPrintContext(PrintContext context, String sRequest,
     HttpServletRequest request, HttpServletResponse response)
     throws Exception
  {
    return null;
  }

  /**
   * Autentica la richiesta.
   * Determina l'utente che ha generato la richiesta.
   * Ridefinibile in classi derivate.
   * @param request parametri della richiesta
   * @return id utente
   * @throws java.lang.Exception
   */
  protected int authRequest(HttpServletRequest request)
     throws Exception
  {
    int idUser = SEC.getUserID(request.getSession());
    return idUser == -1 ? 0 : idUser;
  }

  private PdfPrint.JobInfo checkJobCompleted(String jobCode)
     throws Exception
  {
    PdfPrint.JobInfo info = pp.refreshInfo(jobCode);

    if(info != null)
      return checkJobCompleted(info);

    return info;
  }

  private PdfPrint.JobInfo checkJobCompleted(PdfPrint.JobInfo info)
     throws Exception
  {
    if(info == null)
      return null;

    if(info.error != null && info.error instanceof MessagePrintException)
      throw (MessagePrintException) info.error;

    if(info.filePdf == null && info.printer == null)
    {
      // elaborazione asincrona del job attivata
      if(info.error == null)
        throw new AsyncPdfRunningException(info);
      else
        throw new AsyncPdfRunningException(info, info.error);
    }

    if(info.printer != null)
    {
      // elaborazione asincrona con invio diretto alla stampante
      if(info.error == null)
        throw new DirectPrintException(info);
      else
        throw new DirectPrintException(info, info.error);
    }

    if(info.percCompleted == 100 && info.filePdf != null && info.filePdf.length() == 0)
    {
      if(info.error == null)
        throw new DirectPrintException(info);
      else
        throw new DirectPrintException(info, info.error);
    }

    return info;
  }

  protected void elaboraRispostaTemplate(MessagePrintException emp,
     HttpServletRequest request, HttpServletResponse response)
     throws ServletException
  {
    try
    {
      String template = emp.getTemplate();
      Context ctx = VelocityParser.createNewContext();
      String sRequest = request.getPathInfo().substring(1);

      for(Map.Entry<String, Object> entry : emp.getOptions().entrySet())
      {
        String key = entry.getKey();
        Object value = entry.getValue();
        ctx.put(key, value);
      }

      File tmp = File.createTempFile("risposta", ".html");
      VelocityParser vp = new VelocityParser(ctx);
      vp.parseFileToFile(template, tmp);
      pgmlog.info("Risposta template in " + tmp.getAbsolutePath());
      FU.sendFileAsHTML(request, response, tmp, false);
      tmp.delete();
    }
    catch(Exception ex)
    {
      throw new ServletException(ex);
    }
  }
}
