/*
 * Copyright (C) 2024 Nicola De Nisco
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;

/**
 * Helper per leggere la configurazione di log4j a runtime.
 *
 * @author Nicola De Nisco
 */
public class ReadLog4j2ConfigHelper
{
  private final LoggerContext ctx;
  private final Configuration config;

  public ReadLog4j2ConfigHelper()
  {
    ctx = LoggerContext.getContext(true);
    //ctx = (LoggerContext) LogManager.getContext();
    config = ctx.getConfiguration();
  }

  public List<String> getFileAppenderPath()
  {
    List<String> rv = new ArrayList<>();
    Map<String, Appender> allAppenders = config.getAppenders();

    for(Map.Entry<String, Appender> entry : allAppenders.entrySet())
    {
      String nome = entry.getKey();
      Appender ap = entry.getValue();

      if(ap instanceof FileAppender)
      {
        FileAppender fp = (FileAppender) ap;
        String fileName = fp.getFileName();
        if(SU.isOkStr(fileName))
          rv.add(fileName);
      }
    }

    return rv;
  }
}
