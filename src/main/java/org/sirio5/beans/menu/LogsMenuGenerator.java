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
package org.sirio5.beans.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.RunData;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import static org.sirio5.services.localization.INT.I;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;
import org.sirio5.utils.tree.CoreMenuTreeNode;

/**
 * Generatore di sottomenu per i files di log.
 * Per ogni file di log dichiarato in Log.properties
 * genera una voce nel menu per consentire l'accesso
 * immediato del relativo file di log.
 *
 * @author Nicola De Nisco
 */
public class LogsMenuGenerator implements MenuGenerator
{
  /** Logging */
  private static final Log log = LogFactory.getLog(LogsMenuGenerator.class);
  public static final String MENU_LOGS_SESSION_KEY = "MENU_LOGS_SESSION_KEY";

  /**
   * Crea il sottomenu per i log.
   * @param submenugen
   * @param data
   * @param livello
   * @param elPadre
   * @param node
   * @throws Exception
   */
  @Override
  public void creaSottoMenu(String submenugen,
     RunData data, int livello, Element elPadre, CoreMenuTreeNode node)
     throws Exception
  {
    List<MenuItemBean> arBeans = parseFileLog(data);
    if(arBeans == null)
      return;

    for(MenuItemBean b : arBeans)
    {
      CoreMenuTreeNode mt = new CoreMenuTreeNode(b);
      mt.setEnabled(true);
      node.addChild(mt);
    }
  }

  public List<MenuItemBean> parseFileLog(RunData data)
  {
    List<MenuItemBean> arBeans;
    if((arBeans = (List<MenuItemBean>) data.getSession().getAttribute(MENU_LOGS_SESSION_KEY)) != null)
      return arBeans;

    arBeans = parse();

    // salva l'array dei beans in sessione per recuperarli dalla servlet di visualizzazione dei logs
    data.getSession().setAttribute(MENU_LOGS_SESSION_KEY, arBeans);

    return arBeans;
  }

  private List<MenuItemBean> parse()
  {
    List<MenuItemBean> arBeans;

    // carica il file di configurazione di Log4j leggendo
    // le relative impostazioni dal TurbineResources.properties
    String sFile = TR.getString("log4j.file"); // NOI18N
    if(SU.isOkStr(sFile))
    {
      File fileLog4j = new File(Turbine.getRealPath(sFile));
      if(!fileLog4j.canRead())
        return Collections.EMPTY_LIST;

      if((arBeans = loadConfiguration(fileLog4j)) == null)
        return Collections.EMPTY_LIST;

      return arBeans;
    }

    sFile = TR.getString("log4j2.file"); // NOI18N
    if(SU.isOkStr(sFile))
    {
      File fileLog4j = new File(Turbine.getRealPath("WEB-INF/conf/" + sFile));
      if(!fileLog4j.canRead())
        return Collections.EMPTY_LIST;

      if((arBeans = loadConfiguration2(fileLog4j)) == null)
        return Collections.EMPTY_LIST;

      return arBeans;
    }

    return Collections.EMPTY_LIST;
  }

  /**
   * Caricamento per versione classica.
   * @param fileLog4j
   * @return
   */
  public List<MenuItemBean> loadConfiguration(File fileLog4j)
  {
    try
    {
      PropertiesConfiguration cfg = new PropertiesConfiguration(fileLog4j);
      cfg.addProperty("applicationRoot", Turbine.getRealPath(""));
      ArrayList<MenuItemBean> arFiles = new ArrayList<>();

      HashSet<String> unique = new HashSet<>();
      Iterator itKeys = cfg.getKeys("log4j.appender");
      while(itKeys.hasNext())
      {
        String key = (String) itKeys.next();
        if(key.endsWith(".file"))
        {
          String val = cfg.getString(key);
          unique.add(val);
        }
      }

      unique.stream().sorted().forEach((val) ->
      {
        int pos;
        MenuItemBean b = new MenuItemBean();
        b.setProgramma("/viewlogs/" + val);
        b.setNote(val);
        b.setDescrizione(val);
        b.setFlag1("A");

        if((pos = val.lastIndexOf('/')) != -1)
        {
          b.setDescrizione(val.substring(pos + 1));
          b.setProgramma("/viewlogs/" + b.getDescrizione());
        }

        arFiles.add(b);
      });

      return arFiles;
    }
    catch(Exception ex)
    {
      log.error(I("Errore leggendo il file %s:", fileLog4j.getAbsolutePath()), ex);
      return null;
    }
  }

  /**
   * Caricamento nuova versione con file XML.
   * @param fileLog4j
   * @return
   */
  public List<MenuItemBean> loadConfiguration2(File fileLog4j)
  {
    try
    {
      ArrayList<MenuItemBean> arFiles = new ArrayList<>();
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(fileLog4j);

      List<Element> lsFileAppenders = doc.getRootElement().getChild("Appenders").getChildren("File");

      String realPath = Turbine.getRealPath("");
      HashSet<String> unique = new HashSet<>();
      for(Element efa : lsFileAppenders)
      {
        String attrFile = efa.getAttributeValue("fileName").replace("${web:rootDir}", realPath);
        unique.add(attrFile);
      }

      unique.stream().sorted().forEach((val) ->
      {
        int pos;
        MenuItemBean b = new MenuItemBean();
        b.setProgramma("/viewlogs/" + val);
        b.setNote(val);
        b.setDescrizione(val);
        b.setFlag1("A");

        if((pos = val.lastIndexOf('/')) != -1)
        {
          b.setDescrizione(val.substring(pos + 1));
          b.setProgramma("/viewlogs/" + b.getDescrizione());
        }

        arFiles.add(b);
      });

      return arFiles;
    }
    catch(Exception ex)
    {
      log.error(I("Errore leggendo il file %s:", fileLog4j.getAbsolutePath()), ex);
      return null;
    }
  }
}
