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

import org.apache.commons.configuration2.Configuration;
import org.apache.fulcrum.pool.PoolService;
import org.apache.turbine.services.TurbineServices;
import org.commonlib.lambda.ConsumerThrowException;
import org.commonlib.lambda.FunctionTrowException;
import org.sirio2.services.CoreServiceException;
import org.sirio2.services.localization.INT;

/**
 * Funzioni di base di una factory di plugin.
 * I plugin sono mantenuti in un pool.
 * Dopo l'uso vanno restituiti con la funzione putPlugin() affinche
 * vengano salvati nel pool e usati successivamente.
 *
 * @author Nicola De Nisco
 * @param <T> tipo del plugin; deve implementare l'intefaccia CoreBasePoolPlugin
 */
public class CoreAbstractPoolPluginFactory<T extends CoreBasePoolPlugin> extends CoreAbstractPluginFactory<T>
{
  /** Reference to the pool service */
  private PoolService pool = null;

  @Override
  public void configure(Configuration cfg, String pluginCfgRadix)
  {
    super.configure(cfg, pluginCfgRadix);

    pool = (PoolService) TurbineServices.getInstance().getService(PoolService.ROLE);

    if(pool == null)
      throw new RuntimeException("Pull Service requires configured Pool Service!");
  }

  @Override
  protected void preparePlugin(String pluginName, T handler, Configuration cfgLocal)
     throws Exception
  {
    if(!handler.isInitialized())
      super.preparePlugin(pluginName, handler, cfgLocal);
  }

  @Override
  protected T createPlugin(Class cl)
     throws CoreServiceException
  {
    try
    {
      return (T) pool.getInstance(cl);
    }
    catch(Throwable t)
    {
    }

    throw new CoreServiceException(INT.I("Classe %s non istanziabile.", cl.getName()));
  }

  /**
   * Restituisce al pool il plugin dopo l'uso.
   * @param plg plugin da restituire
   * @return vero se il pool accetta l'istanza
   */
  public boolean putPlugin(T plg)
  {
    if(plg == null)
      return false;

    return pool.putInstance(plg);
  }

  /**
   * Esegue operazioni con il plugin.
   * Il plugin viene estratto dal pool o creato alla bisogna
   * e dopo l'operazione viene restituito al pool.
   * @param pluginName nome del plugin
   * @param consumer operazione da eseguire
   * @throws Exception
   */
  public void runPlugin(String pluginName, ConsumerThrowException<T> consumer)
     throws Exception
  {
    T plugin = null;
    try
    {
      plugin = getPlugin(pluginName);
      consumer.accept(plugin);
    }
    finally
    {
      if(plugin != null)
        putPlugin(plugin);
    }
  }

  /**
   * Chiama funzione del plugin.
   * Il plugin viene estratto dal pool o creato alla bisogna
   * e dopo l'operazione viene restituito al pool.
   * @param <R> tipo di ritorno della funzione
   * @param pluginName nome del plugin
   * @param function funzione da eseguire
   * @return valore di ritorno della funzione
   * @throws Exception
   */
  public <R> R functionPlugin(String pluginName, FunctionTrowException<T, R> function)
     throws Exception
  {
    T plugin = null;
    try
    {
      plugin = getPlugin(pluginName);
      return function.apply(plugin);
    }
    finally
    {
      if(plugin != null)
        putPlugin(plugin);
    }
  }
}
