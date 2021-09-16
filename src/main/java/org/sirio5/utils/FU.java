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
package org.sirio5.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.commonlib5.utils.CommonFileUtils;

/**
 * File Utils for Servlets.
 *
 * @author Nicola De Nisco
 */
public class FU
{
  public static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";
  public static final String CONTENT_TYPE_PDF = "application/pdf";
  public static final String CONTENT_TYPE_DICOM = "application/dicom";
  public static final String CONTENT_TYPE_JPEG = "image/jpeg";
  public static final String CONTENT_TYPE_ZIP = "application/zip";
  public static final int BUFFER_GZIP = 4096; //4KBytes

  /**
   * Invia un file di risposta come PDF.
   * In una servlet consente di inviare un file PDF come risposta.
   * @param request
   * @param response
   * @param fPdf
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsPDF(HttpServletRequest request, HttpServletResponse response,
     File fPdf, boolean enableGzip)
     throws Exception
  {
    sendFileResponse(request, response, fPdf, CONTENT_TYPE_PDF, fPdf.getName(), enableGzip);
  }

  /**
   * Invia un file di risposta come ZIP.
   * In una servlet consente di inviare un file PDF come risposta.
   * @param request
   * @param response
   * @param fzip
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsZIP(HttpServletRequest request, HttpServletResponse response,
     File fzip, boolean enableGzip)
     throws Exception
  {
    sendFileResponse(request, response, fzip, CONTENT_TYPE_ZIP, fzip.getName(), enableGzip);
  }

  /**
   * Invia un file di risposta HTML.
   * In una servlet consente di inviare un file HTML come risposta.
   * @param request
   * @param response
   * @param fHtml
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsHTML(HttpServletRequest request,
     HttpServletResponse response, File fHtml, boolean enableGzip)
     throws Exception
  {
    sendFileResponse(request, response, fHtml, CONTENT_TYPE_HTML, fHtml.getName(), enableGzip);
  }

  /**
   * Invia un file di risposta HTML.
   * In una servlet consente di inviare un file HTML come risposta.
   * @param request
   * @param response
   * @param fDicom
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsDicom(HttpServletRequest request,
     HttpServletResponse response, File fDicom, boolean enableGzip)
     throws Exception
  {
    sendFileResponse(request, response, fDicom, CONTENT_TYPE_DICOM, fDicom.getName(), enableGzip);
  }

  /**
   * Invia un file di risposta HTML.
   * In una servlet consente di inviare un file HTML come risposta.
   * @param request
   * @param response
   * @param fjpeg
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileAsJpeg(HttpServletRequest request,
     HttpServletResponse response, File fjpeg, boolean enableGzip)
     throws Exception
  {
    sendFileResponse(request, response, fjpeg, CONTENT_TYPE_JPEG, fjpeg.getName(), enableGzip);
  }

  /**
   * Invio file come risposta servlet.
   * Invia un file con "Content-Disposition", "inline;", ovvero il browser
   * se può cerca di renderizzarlo direttamente nella pagina.
   * @param request
   * @param response
   * @param fPdf
   * @param tipoMime
   * @param saveFileName
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFileResponse(HttpServletRequest request, HttpServletResponse response,
     File fPdf, String tipoMime, String saveFileName, boolean enableGzip)
     throws Exception
  {
    // invio del file pdf come risposta
    response.setContentType(tipoMime);
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
        if(acceptEncoding.toLowerCase().contains("gzip"))
        {
          output = new GZIPOutputStream(output, BUFFER_GZIP);
          response.setHeader("Content-Encoding", "gzip");
        }
    }

    copyFileOnStream(fPdf, output);
  }

  /**
   * Invio file come risposta servlet.
   * Simile a sendFileResponse() ma con "Content-Disposition", "attachment;"
   * in modo da sollecitare il browser a chiedere il salvataggio del
   * file piuttosto che cercare di aprirlo all'interno della finestra.
   * @param request
   * @param response
   * @param mimeType
   * @param fPdf
   * @param saveFileName
   * @param enableGzip
   * @throws Exception
   */
  public static void sendFile(HttpServletRequest request, HttpServletResponse response,
     String mimeType, File fPdf, String saveFileName, boolean enableGzip)
     throws Exception
  {
    // invio del file pdf come risposta
    response.setContentType(mimeType);
    response.setContentLength((int) (fPdf.length()));
    if(saveFileName != null)
      response.setHeader("Content-Disposition", "attachment; filename=" + saveFileName);
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
        if(acceptEncoding.toLowerCase().contains("gzip"))
        {
          output = new GZIPOutputStream(output, BUFFER_GZIP);
          response.setHeader("Content-Encoding", "gzip");
        }
    }

    copyFileOnStream(fPdf, output);
  }

  public static void copyFileOnStream(File fPdf, OutputStream output)
     throws Exception
  {
    try (FileInputStream fis = new FileInputStream(fPdf))
    {
      CommonFileUtils.copyStream(fis, output);
      safeFlush(output);
    }
  }

  /**
   * Flush dello stream.
   * Cattura eccezione se il file e' stato gia' chiuso.
   * @param output
   */
  public static void safeFlush(OutputStream output)
  {
    try
    {
      output.flush();
    }
    catch(Throwable t)
    {
    }
  }
}
