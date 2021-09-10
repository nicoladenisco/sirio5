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
package org.sirio2.utils.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.*;

import java.util.zip.GZIPOutputStream;
import javax.servlet.http.*;

import java.util.List;
import org.commonlib.utils.CommonFileUtils;

/**
 * Funzioni di utilita' per la produzione e manipolazione
 * di file in formato PDF.
 *
 * @author Nicola De Nisco
 */
public class PDFutils
{
  public static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
  public static final String CONTENT_TYPE_PDF = "application/pdf";
  public static final int BUFFER_GZIP = 4096; //4KBytes

  /**
   * Converte un file HTML in un file PDF.
   * Gli eventuali link simbolici
   * vengono mantenuti nel PDF di output.
   */
  public static void convertHtlm2Pdf(File inHtml, File outPdf)
     throws Exception
  {
    try (FileReader in = new FileReader(inHtml))
    {
      convertHtlm2Pdf(in, outPdf);
    }
  }

  /**
   * Converte un file HTML fornito dal reader
   * in un file PDF. Gli eventuali link simbolici
   * vengono mantenuti nel PDF di output.
   */
  public static void convertHtlm2Pdf(Reader inHtml, File outPdf)
     throws Exception
  {
    Document document = new Document();
    List<Element> elementlist = HTMLWorker.parseToList(inHtml, null);

    FileOutputStream out = new FileOutputStream(outPdf);
    PdfWriter.getInstance(document, out);
    document.open();
    for(int i = 0; i < elementlist.size(); i++)
    {
      Element element = (Element) elementlist.get(i);
      document.add(element);
    }
    document.close();
  }

  /**
   * Invia un file PDF come risposta in una servlet.
   * @param request
   * @param response
   * @param fPdf
   * @param saveFileName
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsPDF(HttpServletRequest request, HttpServletResponse response,
     File fPdf, String saveFileName, boolean enableGzip)
     throws Exception
  {
    sendFile(request, response, CONTENT_TYPE_PDF, fPdf, saveFileName, enableGzip);
  }

  public static void sendFileAsHTML(HttpServletRequest request,
     HttpServletResponse response, File fHtml, boolean enableGzip)
     throws Exception
  {
    sendFile(request, response, CONTENT_TYPE_HTML, fHtml, null, enableGzip);
  }

  public static void sendFile(HttpServletRequest request, HttpServletResponse response,
     String mimeType, File fPdf, String saveFileName, boolean enableGzip)
     throws Exception
  {
    // invio del file pdf come risposta
    response.setContentType(mimeType);
    response.setContentLength((int) (fPdf.length()));
    if(saveFileName != null)
      response.setHeader("Content-Disposition", "inline; filename=" + saveFileName);
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Pragma", "No-cache");

    OutputStream output = response.getOutputStream();

    // se e' abilitata la compressione dell'output ...
    if(enableGzip)
    {
      // ... verifica se il client accetta gzip encoding compress output stream ...
      String acceptEncoding = request.getHeader("Accept-Encoding");
      if(acceptEncoding != null)
      {
        if(acceptEncoding.toLowerCase().contains("gzip"))
        {
          output = new GZIPOutputStream(output, BUFFER_GZIP);
          response.setHeader("Content-Encoding", "gzip");
        }
      }
    }

    try (FileInputStream fis = new FileInputStream(fPdf))
    {
      CommonFileUtils.copyStream(fis, output);

      // cattura eccezione se il file e' stato gia' chiuso
      try
      {
        output.flush();
      }
      catch(Exception ex)
      {
      }
    }
  }

  public static void mergePDF(File[] pdfInput, File pdfOutput)
     throws Exception
  {
    try (FileOutputStream fos = new FileOutputStream(pdfOutput))
    {
      mergePDF(pdfInput, fos);
      fos.flush();
    }
  }

  /**
   * Concatena una serie di PDF in un PDF unico di output.
   * @param pdfInput
   * @param pdfOutput
   * @throws java.lang.Exception
   */
  public static void mergePDF(File[] pdfInput, OutputStream pdfOutput)
     throws Exception
  {
    PdfCopy writer = null;

    // in document metto il documento concatenato
    Document document = null;

    for(int i = 0; i < pdfInput.length; i++)
    {
      File pdfDoc = pdfInput[i];

      try (InputStream is = new FileInputStream(pdfDoc))
      {
        PdfReader reader = new PdfReader(is);
        if(document == null)
        {
          // Se document e' null sono al primo passaggio e lo lego al PdfReader e PdfWriter
          document = new Document(reader.getPageSizeWithRotation(1));
          writer = new PdfCopy(document, pdfOutput);
          document.open();
        }

        // copio pagina per pagina i doc da concatenare nel doc finale
        int n = reader.getNumberOfPages();
        for(int j = 0; j < n; j++)
        {
          PdfImportedPage page = writer.getImportedPage(reader, j + 1);
          writer.addPage(page);
        }
      }
    }

    if(document != null)
      document.close();
  }

  public static void sendFileAsPDFDoubleA3(HttpServletRequest request,
     HttpServletResponse response, File fPdf)
     throws Exception
  {
    // invio del file pdf come risposta
    response.setContentType(CONTENT_TYPE_PDF);
    //response.setContentLength((int) (fPdf.length()));
    OutputStream output = response.getOutputStream();

    FileInputStream fis = new FileInputStream(fPdf);
    int pow2 = 1;
    // we create a reader for a certain document
    PdfReader reader = new PdfReader(fis);
    // we retrieve the total number of pages and the page size
    int total = reader.getNumberOfPages();
    //   Rectangle pageSize = reader.getPageSize(1);
    //   Rectangle newSize = (pow2 % 2) == 0 ? new Rectangle(pageSize.width()-18, pageSize.height()-18) : new Rectangle(pageSize.height()-18, pageSize.width()-18);
    Rectangle pageSize = PageSize.A4;
    Rectangle newSize = new Rectangle(pageSize.getHeight(), pageSize.getWidth());
    // unit size : il primo parametro è la larghezza del documeto A5 , (-18 riesce a fare in modo
    // da per lasciare un po di margine sopra e sotto e non tagliare il footer)
    Rectangle unitSize = new Rectangle(pageSize.getHeight() / 2 - 18, pageSize.getWidth());

    Rectangle currentSize;
    int n = 2;
    //                       PipedInputStream pdf_in = new PipedInputStream();
    //                       PipedOutputStream pdf_out = new PipedOutputStream();
//                        pdf_in.connect(pdf_out);
    // step 1: creation of a document-object
    Document document = new Document(newSize, 0, 0, 0, 0);
    // step 2: we create a writer that listens to the document
    PdfWriter writer = PdfWriter.getInstance(document, output);
    // step 3: we open the document
    document.open();
    // step 4: adding the content
    PdfContentByte cb = writer.getDirectContent();
    PdfImportedPage page;
    float offsetX, offsetY, factor;
    int p;
    for(int i = 0; i < total; i++)
    {
      document.newPage();
      p = i + 1;
      offsetX = 0;
      offsetY = 0;
      currentSize = reader.getPageSize(p);
      page = writer.getImportedPage(reader, p);
      factor = Math.min(unitSize.getWidth() / currentSize.getWidth(), unitSize.getHeight() / currentSize.getHeight());
      offsetX += (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
      offsetY += (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
      cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
      // pagina di copia
      offsetX += pageSize.getHeight() / 2;
      cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
    }
    // step 5: we close the document
    document.close();

    // cattura eccezione se il file e' stato gia' chiuso
    try
    {
      output.flush();
      fis.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public static Phrase getHeaderFooter(Reader in)
  {
    java.util.List elist = null;
    Phrase ret = new Phrase();

    try
    {
      elist = HTMLWorker.parseToList(in, null);
      for(int i = 0; i < elist.size(); i++)
      {
        ret.add((Phrase) elist.get(i));
      }
    }
    catch(Exception e)
    {
    }
    return ret;
  }

  public static void addDocZone(Reader in, Document document)
  {
    java.util.List elist = null;
    try
    {
      elist = HTMLWorker.parseToList(in, null);
      for(int i = 0; i < elist.size(); i++)
      {
        Element element = (Element) elist.get(i);
        document.add(element);
      }
    }
    catch(Exception e)
    {
    }
  }

  /**
   * Verifica se il file indicato è un PDF e se è leggibile.
   * @param fpdf file da verificare
   * @return vero se tutto OK
   */
  public static boolean isValidPDF(File fpdf)
  {
    PdfReader pdfReader = null;
    try
    {
      pdfReader = new PdfReader(fpdf.getAbsolutePath());

      for(int i = 1; i < pdfReader.getNumberOfPages(); i++)
      {
        String textFromPdfFilePageOne = PdfTextExtractor.getTextFromPage(pdfReader, i);
      }

      return true;
    }
    catch(Exception e)
    {
      if(pdfReader != null)
        pdfReader.close();
    }

    return false;
  }
}
