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

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import org.sirio2.utils.SU;
import org.sirio2.utils.TR;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.turbine.util.ServerData;

/**
 * Implementazione di HttpServletRequest per l'uso
 * con il server http embedded di java.
 *
 * @author Nicola De Nisco
 */
public class RequestWrapper extends HttpServletRequestWrapper
{
  private final HttpExchange ex;
  private final Map<String, String[]> postData;
  private final ServletInputStream is;
  private final Map<String, Object> attributes = new HashMap<>();
  private ServerData sd = new ServerData(TR.getDefaultServerData());
  private int contentLength = 0;

  public RequestWrapper(HttpExchange ex, Map<String, String[]> postData, ServletInputStream is)
  {
    this(SU.createUnimplementAdapter(HttpServletRequest.class), ex, postData, is);
  }

  public RequestWrapper(HttpServletRequest request, HttpExchange ex,
     Map<String, String[]> postData, ServletInputStream is)
  {
    super(request);
    this.ex = ex;
    this.postData = postData;
    this.is = is;

    HttpContext ctx = ex.getHttpContext();
    sd.setScriptName(ctx.getPath());
    sd.setContextPath("");
  }

  @Override
  public String getHeader(String name)
  {
    return ex.getRequestHeaders().getFirst(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name)
  {
    return new Vector<String>(ex.getRequestHeaders().get(name)).elements();
  }

  @Override
  public Enumeration<String> getHeaderNames()
  {
    return new Vector<String>(ex.getRequestHeaders().keySet()).elements();
  }

  @Override
  public Object getAttribute(String name)
  {
    return attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object o)
  {
    this.attributes.put(name, o);
  }

  @Override
  public Enumeration<String> getAttributeNames()
  {
    return new Vector<String>(attributes.keySet()).elements();
  }

  @Override
  public String getMethod()
  {
    return ex.getRequestMethod();
  }

  @Override
  public ServletInputStream getInputStream()
     throws IOException
  {
    return is;
  }

  @Override
  public BufferedReader getReader()
     throws IOException
  {
    return new BufferedReader(new InputStreamReader(
       getInputStream()));
  }

  @Override
  public String getPathInfo()
  {
    String serp = getServletPath();
    String urip = ex.getRequestURI().getPath();

    return urip.contains(serp) ? urip.substring(serp.length()) : urip;
  }

  @Override
  public String getParameter(String name)
  {
    String[] arr = postData.get(name);
    return arr != null ? (arr.length > 1 ? Arrays.toString(arr) : arr[0]) : null;
  }

  @Override
  public Map<String, String[]> getParameterMap()
  {
    return postData;
  }

  @Override
  public Enumeration<String> getParameterNames()
  {
    return new Vector<String>(postData.keySet()).elements();
  }

  @Override
  public String[] getParameterValues(String name)
  {
    return postData.get(name);
  }

  @Override
  public Locale getLocale()
  {
    return Locale.getDefault();
  }

  @Override
  public String getCharacterEncoding()
  {
    return "UTF-8";
  }

  public void setContentLength(int contentLength)
  {
    this.contentLength = contentLength;
  }

  @Override
  public int getContentLength()
  {
    if(contentLength == 0)
      contentLength = SU.parseInt(ex.getRequestHeaders().getFirst("Content-length"));

    return contentLength;
  }

  @Override
  public String getContentType()
  {
    return ex.getRequestHeaders().getFirst("Content-type");
  }

  @Override
  public String getServerName()
  {
    return sd.getServerName();
  }

  @Override
  public int getServerPort()
  {
    return sd.getServerPort();
  }

  @Override
  public String getScheme()
  {
    return sd.getServerScheme();
  }

  @Override
  public String getServletPath()
  {
    return sd.getScriptName();
  }

  @Override
  public String getContextPath()
  {
    return sd.getContextPath();
  }

  public ServerData getServerData()
  {
    return sd;
  }

  public void setServerData(ServerData sd)
  {
    this.sd = sd;
  }
}
