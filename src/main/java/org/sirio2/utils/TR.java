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
package org.sirio2.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.util.ServerData;
import org.apache.turbine.util.uri.URIConstants;

/**
 * Accesso alle variabili di setup generali dell'application server.
 *
 * @author Nicola De Nisco
 */
public class TR
{
  /**
   * Carica gli override delle properties.
   * @param cfg configurazione da aggiornare
   * @param override mappa di valori da sovrascrivere o aggiungere
   */
  public static void loadOverride(Configuration cfg, Map<String, String> override)
  {
    // ultima chiave per discriminare addProperty da setPropery
    String last = null;

    // ordina tutti gli override in ordine alfabetico
    ArrayList<String> keys = new ArrayList<>(override.keySet());
    keys.sort((s1, s2) -> SU.compare(s1, s2));

    // inserisce gli override in ordine alfabetico
    for(String key : keys)
    {
      String nome = SU.okStrNull(key);
      String valore = SU.okStrNull(override.get(key));

      if(nome == null || valore == null)
        continue;

      if(SU.isEqu(last, nome))
        cfg.addProperty(nome, valore);
      else
        cfg.setProperty(nome, valore);

      last = nome;
    }
  }

  /**
   * Carica gli override delle properties di setup Turbine.
   * Aggiorna la configurazione principale di Turbine.
   * @param override mappa di valori da sovrascrivere o aggiungere
   */
  public static void loadOverride(Map<String, String> override)
  {
    loadOverride(Turbine.getConfiguration(), override);
  }

  /**
   * Keep all the properties of the web server in a convenient data
   * structure
   */
  protected static ServerData serverData = null;

  /**
   * Set a property in with a key=value pair.
   *
   * @param key
   * @param value
   */
  public static void setProperty(String key, String value)
  {
    Turbine.getConfiguration().setProperty(key, value);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a boolean value.
   *
   * @param name The resource name.
   * @return The value of the named resource as a boolean.
   */
  public static boolean getBoolean(String name)
  {
    return Turbine.getConfiguration().getBoolean(name);
  }

  /**
   * The purppose of this method is to get the configuration
   * resource with the given name as a boolean value, or a default
   * value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the named resource as a boolean.
   */
  public static boolean getBoolean(String name,
     boolean def)
  {
    return Turbine.getConfiguration().getBoolean(name, def);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a double.
   *
   * @param name The resoource name.
   * @return The value of the named resource as double.
   */
  public static double getDouble(String name)
  {
    return Turbine.getConfiguration().getDouble(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a double, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the named resource as a double.
   */
  public static double getDouble(String name,
     double def)
  {
    return Turbine.getConfiguration().getDouble(name, def);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a float.
   *
   * @param name The resource name.
   * @return The value of the resource as a float.
   */
  public static float getFloat(String name)
  {
    return Turbine.getConfiguration().getFloat(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a float, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the resource as a float.
   */
  public static float getFloat(String name,
     float def)
  {
    return Turbine.getConfiguration().getFloat(name, def);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as an integer.
   *
   * @param name The resource name.
   * @return The value of the resource as an integer.
   */
  public static int getInt(String name)
  {
    return Turbine.getConfiguration().getInt(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as an integer, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the resource as an integer.
   */
  public static int getInt(String name,
     int def)
  {
    return Turbine.getConfiguration().getInt(name, def);
  }

  /**
   * Get the list of the keys contained in the configuration
   * repository.
   *
   * @return An Enumeration with all the keys.
   */
  public static Iterator getKeys()
  {
    return Turbine.getConfiguration().getKeys();
  }

  /**
   * Get the list of the keys contained in the configuration
   * repository that match the specified prefix.
   *
   * @param prefix A String prefix to test against.
   * @return An Enumeration of keys that match the prefix.
   */
  public static Iterator getKeys(String prefix)
  {
    return Turbine.getConfiguration().getKeys(prefix);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a long.
   *
   * @param name The resource name.
   * @return The value of the resource as a long.
   */
  public static long getLong(String name)
  {
    return Turbine.getConfiguration().getLong(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a long, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the resource as a long.
   */
  public static long getLong(String name,
     long def)
  {
    return Turbine.getConfiguration().getLong(name, def);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a string.
   *
   * @param name The resource name.
   * @return The value of the resource as a string.
   */
  public static String getString(String name)
  {
    return Turbine.getConfiguration().getString(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a string, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the resource as a string.
   */
  public static String getString(String name,
     String def)
  {
    return Turbine.getConfiguration().getString(name, def);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a string array.
   *
   * @param name The resource name.
   * @return The value of the resource as a string array.
   */
  public static String[] getStringArray(String name)
  {
    return Turbine.getConfiguration().getStringArray(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a vector.
   *
   * @param name The resource name.
   * @return The value of the resource as a vector.
   */
  public static List getList(String name)
  {
    return Turbine.getConfiguration().getList(name);
  }

  /**
   * The purpose of this method is to get the configuration resource
   * with the given name as a vector, or a default value.
   *
   * @param name The resource name.
   * @param def The default value of the resource.
   * @return The value of the resource as a vector.
   */
  public static List getList(String name,
     List def)
  {
    return Turbine.getConfiguration().getList(name, def);
  }

  /**
   * Get the configuration.
   *
   * @return configuration.
   */
  public static Configuration getConfiguration()
  {
    return Turbine.getConfiguration();
  }

  /**
   * The purpose of this method is to extract a subset configuration
   * sharing a common name prefix.
   *
   * @param prefix the common name prefix
   * @return A Configuration providing the subset of configuration.
   */
  public static Configuration getConfiguration(String prefix)
  {
    return Turbine.getConfiguration().subset(prefix);
  }

  /**
   * Return the server name.
   *
   * @return String server name
   */
  public static String getServerName()
  {
    return getDefaultServerData().getServerName();
  }

  /**
   * Return the server scheme.
   *
   * @return String server scheme
   */
  public static String getServerScheme()
  {
    return getDefaultServerData().getServerScheme();
  }

  /**
   * Return the server port.
   *
   * @return String server port
   */
  public static String getServerPort()
  {
    return Integer.toString(getDefaultServerData().getServerPort());
  }

  /**
   * Get the script name. This is the initial script name.
   * Actually this is probably not needed any more. I'll
   * check. jvz.
   *
   * @return String initial script name.
   */
  public static String getScriptName()
  {
    return getDefaultServerData().getScriptName();
  }

  /**
   * Return the context path.
   *
   * @return String context path
   */
  public static String getContextPath()
  {
    return getDefaultServerData().getContextPath();
  }

  /**
   * Preleva dati server dal file di configurazione.
   * Legge le informazioni del server da file di setup.
   * La servlet turbine implementa un metodo simili estraendo
   * le informazioni dalla prima richiesta.
   *
   * @return An initialized ServerData object
   */
  public static ServerData getDefaultServerData()
  {
    if(serverData == null)
    {
      Configuration configuration = getConfiguration();

      serverData = new ServerData(
         configuration.getString(TurbineConstants.DEFAULT_SERVER_NAME_KEY),
         configuration.getInt(TurbineConstants.DEFAULT_SERVER_PORT_KEY, URIConstants.HTTP_PORT),
         configuration.getString(TurbineConstants.DEFAULT_SERVER_SCHEME_KEY, URIConstants.HTTP),
         configuration.getString(TurbineConstants.DEFAULT_SCRIPT_NAME_KEY),
         configuration.getString(TurbineConstants.DEFAULT_CONTEXT_PATH_KEY));
    }

    return serverData;
  }
}
