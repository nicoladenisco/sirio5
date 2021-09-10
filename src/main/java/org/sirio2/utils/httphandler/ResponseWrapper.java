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
package org.sirio2.utils.httphandler;

import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.sirio2.utils.SU;

/**
 * Implementazione di HttpServletResponse per l'uso
 * con il server http embedded di java.
 *
 * @author Nicola De Nisco
 */
public class ResponseWrapper extends HttpServletResponseWrapper
{
  final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  final ServletOutputStream servletOutputStream = new ServletOutputStream()
  {
    @Override
    public void write(int b)
       throws IOException
    {
      outputStream.write(b);
    }

    @Override
    public void setWriteListener(WriteListener wl)
    {
    }

    @Override
    public boolean isReady()
    {
      return true;
    }
  };

  private final HttpExchange ex;
  private final PrintWriter printWriter;
  private int status = HttpServletResponse.SC_OK;

  public ResponseWrapper(HttpExchange ex)
  {
    this(SU.createUnimplementAdapter(HttpServletResponse.class), ex);
  }

  public ResponseWrapper(HttpServletResponse response, HttpExchange ex)
  {
    super(response);
    this.ex = ex;
    printWriter = new PrintWriter(servletOutputStream);
  }

  @Override
  public void setContentType(String type)
  {
    ex.getResponseHeaders().add("Content-Type", type);
  }

  @Override
  public void setHeader(String name, String value)
  {
    ex.getResponseHeaders().add(name, value);
  }

  @Override
  public javax.servlet.ServletOutputStream getOutputStream()
     throws IOException
  {
    return servletOutputStream;
  }

  @Override
  public void setContentLength(int len)
  {
    ex.getResponseHeaders().add("Content-Length", len + "");
  }

  @Override
  public void setStatus(int status)
  {
    this.status = status;
  }

  @Override
  public void sendError(int sc, String msg)
     throws IOException
  {
    this.status = sc;
    if(msg != null)
    {
      printWriter.write(msg);
    }
  }

  @Override
  public void sendError(int sc)
     throws IOException
  {
    sendError(sc, null);
  }

  @Override
  public PrintWriter getWriter()
     throws IOException
  {
    return printWriter;
  }

  public void complete()
     throws IOException
  {
    try
    {
      printWriter.flush();
      ex.sendResponseHeaders(status, outputStream.size());
      if(outputStream.size() > 0)
        ex.getResponseBody().write(outputStream.toByteArray());
      ex.getResponseBody().flush();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      ex.close();
    }
  }
}
