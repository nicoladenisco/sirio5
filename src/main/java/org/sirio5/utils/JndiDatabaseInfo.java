/*
 * Copyright (C) 2025 Nicola De Nisco
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
import java.util.List;
import org.apache.commons.configuration2.Configuration;
import org.apache.torque.Torque;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * Recupera le informazioni di accesso al db in caso di risorsa jndi.
 * Legge il file di tomcat context.xml per reperire la risorsa jndi
 * e se presente recupera i dati di accesso al db.
 *
 * @author Nicola De Nisco
 */
public class JndiDatabaseInfo
{
  private boolean haveJndi;
  private String dbDriver, dbUri, dbUser, dbPass;

  public JndiDatabaseInfo()
  {
    this("jdbc/");
  }

  public JndiDatabaseInfo(String prefix)
  {
    String catalinaHome = System.getProperty("catalina.home");
    File confDir = new File(catalinaHome + "/conf");

    if(!confDir.isDirectory())
      throw new RuntimeException("Non riesco a rintracciare la directory conf di Tomcat.");

    File contextXml = new File(confDir, "context.xml");
    SAXBuilder builder = new SAXBuilder();

    Document d;
    try
    {
      d = builder.build(contextXml);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }

    List<Element> lsResch = d.getRootElement().getChildren("Resource");
    for(Element er : lsResch)
    {
      String nome = er.getAttributeValue("name");
      if(nome == null || !nome.startsWith(prefix))
        continue;

      dbDriver = er.getAttributeValue("driverClassName");
      dbUri = er.getAttributeValue("url");
      dbUser = er.getAttributeValue("username");
      dbPass = er.getAttributeValue("password");
      haveJndi = true;
      return;
    }
  }

  public String getDbDriver()
  {
    return dbDriver;
  }

  public String getDbUri()
  {
    return dbUri;
  }

  public String getDbUser()
  {
    return dbUser;
  }

  public String getDbPass()
  {
    return dbPass;
  }

  public boolean haveJndi()
  {
    return haveJndi;
  }

  /**
   * Ritorna vero sel la configurazione di torque è agganciata ad una risorsa JNDI.
   * @return vero se torque usa JNDI
   */
  public static boolean haveTorqueJndi()
  {
    Configuration cfg = Torque.getConfiguration();
    String factory = cfg.getString("defaults.jndifactory");
    return (SU.isOkStr(factory));
  }
}
