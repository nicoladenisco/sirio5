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
package org.sirio5.utils.httphandler;

import com.sun.net.httpserver.HttpExchange;
import org.sirio5.utils.SU;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.fileupload.RequestContext;

/**
 * Contesto per il parsing dei multipart usando un HttpExchange.
 * Questa classe viene utilizzata da commons-upload per leggere
 * i dati della POST ed effettuare il parsing dei parametri.
 *
 * @author Nicola De Nisco
 */
public class HttpExchangeUploadContext implements RequestContext
{
  private HttpExchange ex;

  public HttpExchangeUploadContext(HttpExchange ex)
  {
    this.ex = ex;
  }

  @Override
  public String getCharacterEncoding()
  {
    return "UTF-8";
  }

  @Override
  public String getContentType()
  {
    return ex.getRequestHeaders().getFirst("Content-type");
  }

  @Override
  public int getContentLength()
  {
    return SU.parseInt(ex.getRequestHeaders().getFirst("Content-length"));
  }

  @Override
  public InputStream getInputStream()
     throws IOException
  {
    return ex.getRequestBody();
  }
}
