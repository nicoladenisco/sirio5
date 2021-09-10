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
package org.sirio2.services.allarmi;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;
import org.apache.turbine.services.BaseService;

/**
 * Implementazione standard del servizio di gestion degli allarmi.
 * @author Nicola De Nisco
 */
abstract public class AbstractCoreAllarmiService extends BaseService
   implements ServAllarmi
{
  /** Logging */
  private static Log log = LogFactory.getLog(AbstractCoreAllarmiService.class);
  // parametri
  protected int maxDaysAllarmi, maxDaysComm;
  //
  protected int numActive = 0;
  protected long lastUpdNum = 0;
  protected static final long DELAY_UPD_NUM = 30 * 1000; // 30 secondi

  @Override
  public void init()
  {
    Configuration cfg = getConfiguration();

    maxDaysAllarmi = cfg.getInt("maxDaysAllarmi", 60);
    maxDaysComm = cfg.getInt("maxDaysComm", 60);

    // servizio inizializzato correttamente
    setInit(true);
  }

  @Override
  public void shutdown()
  {
  }

  @Override
  public void commLog(String severity, String servizio, String componente, String messaggio, int visibilita)
  {
    allarmLog(severity, servizio, componente, messaggio, visibilita);
  }

  @Override
  public void commLog(String severity, String servizio, String componente, String messaggio, int visibilita, String note, String... info)
  {
    allarmLog(severity, servizio, componente, messaggio, visibilita);
  }

  @Override
  public int getActiveAllarms()
     throws Exception
  {
    if((System.currentTimeMillis() - lastUpdNum) > DELAY_UPD_NUM)
    {
      deleteAllarmi();
      deleteCommlog();

      numActive = contaAllarmi();
      lastUpdNum = System.currentTimeMillis();
    }

    return numActive;
  }

  protected void resetActiveAllarms()
     throws Exception
  {
    lastUpdNum = 0;
  }

  /**
   * Attiva cancellazione allarmi vecchi.
   * @throws TorqueException
   */
  abstract protected void deleteAllarmi()
     throws Exception;

  /**
   * Attiva cancellazione allarmi vecchi.
   * @throws TorqueException
   */
  abstract protected void deleteCommlog()
     throws Exception;

  /**
   * Conta gli allarmi attivi nel db.
   * @return
   * @throws Exception
   */
  abstract protected int contaAllarmi()
     throws Exception;
}
