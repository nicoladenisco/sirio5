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
package org.sirio2.utils.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.commonlib.utils.ClassOper;
import org.commonlib.utils.Pair;
import org.sirio2.services.CoreServiceException;
import org.sirio2.services.localization.INT;
import org.sirio2.utils.SU;

/**
 * Funzioni di base di una factory di plugin.
 *
 * @author Nicola De Nisco
 * @param <T> tipo del plugin; deve implementare l'intefaccia CoreBasePlugin
 */
public abstract class CoreAbstractPluginFactory<T extends CoreBasePlugin>
{
  protected String basePath = null;
  protected String[] vPaths = null;
  protected Configuration cfg = null;
  protected String pluginCfgRadix = null;
  protected HashMap<String, Pair<String, Configuration>> cache = new HashMap<>();
  protected ArrayList<String> arPlugins = new ArrayList<String>();
  public static final Pattern patClassname = Pattern.compile("^(.+).classname");

  /**
   * Configura la factory.
   * @param cfg estratto di configurazione
   * @param pluginCfgRadix stringa radice dei plugin di interesse
   */
  public void configure(Configuration cfg, String pluginCfgRadix)
  {
    this.cfg = cfg;
    this.pluginCfgRadix = pluginCfgRadix;

    // carica path di ricerca della class del plugin
    basePath = ClassOper.getClassPackage(this.getClass());
    vPaths = cfg.getStringArray("classpath");
    String[] localPath = cfg.getStringArray(pluginCfgRadix + ".classpath");
    if(localPath.length > 0)
      vPaths = (String[]) ArrayUtils.addAll(vPaths, localPath);

    // costruisce la lista dei plugin
    Configuration subPlg = cfg.subset(pluginCfgRadix);
    Iterator<String> itrKeys = subPlg.getKeys();
    while(itrKeys.hasNext())
    {
      String key = itrKeys.next();
      Matcher m = patClassname.matcher(key);
      if(m.find())
      {
        String pluginName = m.group(1);
        String className = cfg.getString(pluginCfgRadix + "." + pluginName + ".classname");
        if(!SU.isOkStr(className))
          continue;

        arPlugins.add(pluginName);
        cache.put(pluginName, new Pair<>(className, cfg.subset(pluginCfgRadix + "." + pluginName)));
      }
    }

    Collections.sort(arPlugins);
  }

  /**
   * Ritorna lista plugin.
   * Al momento del configure è stata estratta la lista dei plugin disponibili.
   * @return lista dei plugins
   */
  public List<String> getPluginNames()
  {
    return Collections.unmodifiableList(arPlugins);
  }

  /**
   * Carica plugin.
   * @param pluginName nome del plugin
   * @return istanza del plugin
   * @throws Exception
   */
  public T getPlugin(String pluginName)
     throws Exception
  {
    Pair<String, Configuration> cname = cache.get(pluginName);
    if(cname == null)
      throw new CoreServiceException(
         INT.I("Plugin %s non dichiarato a setup (%s.%s.classname inesistente).", pluginName, pluginCfgRadix, pluginName));

    Class cl = loadClass(cname.first);
    T handler = createPlugin(cl);
    preparePlugin(pluginName, handler, cname.second);
    return handler;
  }

  /**
   * Prepara il plugin per l'esecuzione.
   * @param pluginName nome con cui è stato istanziato
   * @param handler plugin da inizializzare
   * @param cfgLocal estratto della configurazione specifico per questo plugin
   * @throws Exception
   */
  protected void preparePlugin(String pluginName, T handler, Configuration cfgLocal)
     throws Exception
  {
    handler.configure(pluginName, cfgLocal);
  }

  /**
   * Carica la classe del plugin.
   * @param className nome/classe del plugin
   * @return classe del plugin
   * @throws CoreServiceException
   */
  protected Class loadClass(String className)
     throws CoreServiceException
  {
    try
    {
      Class cl = ClassOper.loadClass(className, basePath, vPaths);
      if(cl != null)
        return cl;
    }
    catch(Throwable t)
    {
    }

    throw new CoreServiceException(INT.I("Classe %s non trovata.", className));
  }

  /**
   * Crea una istanza del plugin.
   * @param cp classe del plugin
   * @return istanza del plugin
   * @throws CoreServiceException
   */
  protected T createPlugin(Class cp)
     throws CoreServiceException
  {
    try
    {
      return (T) cp.newInstance();
    }
    catch(InstantiationException | IllegalAccessException ex)
    {
      throw new CoreServiceException(INT.I("Classe %s non istanziabile.", cp.getName()));
    }
  }

  /**
   * Ritorna elenco delle path per la ricerca della classe del plugin.
   * @return array delle path
   */
  public String[] getBasePaths()
  {
    return vPaths;
  }

  /**
   * Imposta elenco delle path per la ricerca della classe del plugin.
   * @param vPaths array delle path
   */
  public void setBasePaths(String[] vPaths)
  {
    this.vPaths = vPaths;
  }

  /**
   * Aggiunge all'elenco delle path per la ricerca della classe del plugin.
   * @param basePath path da aggiungere
   */
  public void addBasePath(String basePath)
  {
    vPaths = (String[]) ArrayUtils.add(vPaths, basePath);
  }
}
