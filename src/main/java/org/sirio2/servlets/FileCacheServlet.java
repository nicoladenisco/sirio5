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
package org.sirio2.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.mimetype.MimeTypeService;
import org.apache.fulcrum.parser.DefaultParameterParser;
import org.apache.fulcrum.upload.UploadService;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.Pair;
import org.sirio2.CoreConst;
import org.sirio2.services.cache.FileCacheItem;
import org.sirio2.services.localization.INT;
import org.sirio2.utils.FU;
import org.sirio2.utils.SU;

/**
 * Servlet per servire i files nella cache.
 *
 * @author Nicola De Nisco
 */
public class FileCacheServlet extends HttpServlet
{
  /** Logging */
  private static final Log pgmlog = LogFactory.getLog(FileCacheServlet.class);

  private UploadService us = null;
  private MimeTypeService mts = null;

  // Initialize global variables
  @Override
  public void init()
     throws ServletException
  {
    us = (UploadService) TurbineServices.getInstance().
       getService(UploadService.ROLE);

    mts = (MimeTypeService) TurbineServices.getInstance().
       getService(MimeTypeService.ROLE);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws ServletException, IOException
  {
    try
    {
      DefaultParameterParser params = new DefaultParameterParser("UTF-8");
      List<FileItem> fis = us.parseRequest(req);

      String nome = SU.okStrNull(params.getString("nome", null));
      String tipoMime = SU.okStrNull(params.getString("tipoMime", null));

      if(fis == null || fis.isEmpty())
        throw new Exception(INT.I("Nessun dato da salvare!"));

      ArrayList<Pair<String, String>> arTickets = new ArrayList<>(fis.size());
      for(FileItem fi : fis)
      {
        if(!SU.isOkStr(tipoMime) || SU.isEqu(CoreConst.AUTO_MIME, tipoMime))
          if((tipoMime = fi.getContentType()) == null)
            // nessuna definizione esplicita di tipo mime:
            // cerchiamo di determinarlo automaticamente
            tipoMime = mts.getContentType(fi.getName().toLowerCase());

        File tmpFile = File.createTempFile("upload", ".tmp");
        fi.write(tmpFile);

        String fileName = SU.okStr(fi.getName(), nome);
        String ticket = FileCacheItem.addFileToCache(tmpFile, tipoMime, fileName, false);
        arTickets.add(new Pair<>(fileName, ticket));
      }

      resp.setContentType(CoreConst.MIME_TXT);
      PrintWriter out = resp.getWriter();
      arTickets.forEach((pa) -> out.printf("%s=%s\n", pa.first, pa.second));
      out.flush();
    }
    catch(ServletException ex)
    {
      pgmlog.error("", ex);
      throw ex;
    }
    catch(Exception ex)
    {
      pgmlog.error("", ex);
      throw new ServletException(ex);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException
  {
    try
    {
      // estrae ticket della richiesta
      String sTicket = request.getPathInfo().substring(1);
      FileCacheItem fi = FileCacheItem.getFromCache(sTicket);
      if(fi == null)
        throw new ServletException(INT.I(
           "Richiesta per il ticket %s non possibile: inesitente o scaduto", sTicket));

      pgmlog.info("FileCacheServlet: OK " + fi.getFile().getAbsolutePath());

      // invio del file come risposta
      FU.sendFile(request, response, fi.getTipoMime(), fi.getFile(), fi.getFileName(), false);
    }
    catch(ServletException ex)
    {
      pgmlog.error("", ex);
      throw ex;
    }
    catch(Exception ex)
    {
      pgmlog.error("", ex);
      throw new ServletException(ex);
    }
  }
}
