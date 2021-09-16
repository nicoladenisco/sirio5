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
import java.io.*;

/**
 *
 * @author andy
 */
public class MultipartFormParser
{
  public static final String CONTENT_TYPE = "Content-type";
  public static final String WWW_MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String DEFAULT_ENCODING = "ISO-8859-1";

  private String boundary = null;
  private String encoding = DEFAULT_ENCODING;
  private BufferedReader br = null;

  /**
   * Cannot create a class without initialization!!!
   */
  private MultipartFormParser()
  {
  }

  /**
   *
   * @param msg
   */
  public MultipartFormParser(HttpExchange msg)
     throws IOException
  {
    this(msg, msg.getRequestBody());
  }

  public MultipartFormParser(HttpExchange msg, InputStream is)
     throws IOException
  {
    // First check if it's a multipart form data request
    // Otherwise give back an empty param list
    String contentType = msg.getRequestHeaders().getFirst(CONTENT_TYPE);
    String line;

    if(contentType != null && contentType.startsWith(WWW_MULTIPART_FORM_DATA))
    {
      // OK now it's time to parse the Multipart Request
      // Get the token string; it's included in the content type.
      // Should look something like "------------------------12012133613061"
      boundary = extractBoundary(contentType);

      if(boundary != null)
      {
        br = new BufferedReader(new InputStreamReader(is));
        // Read until we hit the token
        // Some clients send a preamble (per RFC 2046), so ignore that
        // Thanks to Ben Johnson, ben.johnson@merrillcorp.com, for pointing out
        // the need for preamble support.
        do
        {
          line = br.readLine();
          if(line == null)
          {
            throw new IOException("Corrupt form data: premature ending");
          }
          // See if this line is the token, and if so break
          if(line.startsWith(boundary))
          {
            break;  // success
          }

        }
        while(true);

      }
      else
      {
        throw new IOException("No boundary defined in the Content-type header");
      }

    }
    else
    {
      throw new IOException("The request is not a " + WWW_MULTIPART_FORM_DATA + " content");
    }
  }

  /**
   *
   * @return
   */
  public String getBoundary()
  {
    return boundary;
  }

  /**
   *
   */
  public MultipartParam getNextParam()
     throws IOException
  {
    MultipartParam param;
    String line;

    // Read the headers; they look like this (not all may be present):
    // Content-Disposition: form-data; name="field1"; filename="file1.txt"
    // Content-Type: type/subtype
    // Content-Transfer-Encoding: binary
    line = br.readLine();
    if(line == null)
    {
      // No parts left, we're done
      return null;

    }
    else if(line.length() == 0)
    {
      // IE4 on Mac sends an empty line at the end; treat that as the end.
      // Thanks to Daniel Lemire and Henri Tourigny for this fix.
      return null;
    }

    param = new MultipartParam();

    // Read the following header lines we hit an empty line
    // A line starting with whitespace is considered a continuation;
    // that requires a little special logic.  Thanks to Nic Ferrier for
    // identifying a good fix.
    while(line != null && line.length() > 0)
    {
      String nextLine = null;
      boolean getNextLine = true;
      while(getNextLine)
      {
        nextLine = br.readLine();
        if((nextLine != null) && (nextLine.startsWith(" ") || nextLine.startsWith("\t")))
        {
          line = line + nextLine;

        }
        else
        {
          getNextLine = false;
        }
      }

      // Add the line to the header list
      param.addHeader(line);
      line = nextLine;
    }

    // If we got a null above, it's the end
    if(line == null)
    {
      return null;
    }

    // Now, finally, we read the content (end after reading the token)
    line = br.readLine();
    StringBuilder value = new StringBuilder();
    while(!line.startsWith(boundary))
    {
      value.append(line);
      line = br.readLine();
    }

    param.setContent(value.toString());
    return param;
  }

  /**
   * Extracts and returns the token token from a line.
   *
   * @return the token token.
   */
  private String extractBoundary(String line)
  {
    // Use lastIndexOf() because IE 4.01 on Win98 has been known to send the
    // "token=" string multiple times.  Thanks to David Wall for this fix.
    int index = line.lastIndexOf("boundary=");
    if(index == -1)
    {
      return null;
    }
    String token = line.substring(index + 9);  // 9 for "token="
    if(token.charAt(0) == '"')
    {
      // The token is enclosed in quotes, strip them
      index = token.lastIndexOf('"');
      token = token.substring(1, index);
    }

    // The real token is always preceeded by an extra "--"
    token = "--" + token;

    return token;
  }
}
