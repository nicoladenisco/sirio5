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
package org.sirio5.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonlib5.utils.SimpleTimer;
import org.sirio5.CoreConst;
import static org.sirio5.services.localization.INT.I;

/**
 * Classe base dei servizi che utilizzano thread in background per le operazioni differite.
 *
 * @author Nicola De Nisco
 */
public abstract class AbstractCoreBaseBackgroundService extends AbstractCoreBaseService
   implements CoreBackgroundService
{
  /** Logging */
  private static Log log = LogFactory.getLog(AbstractCoreBaseBackgroundService.class);
  protected Thread thRun = null;
  protected boolean mustExit = false, oneShotForce = false;
  protected long mainDelayMillis = 3 * CoreConst.ONE_MINUTE_MILLIS;

  @Override
  public void startAuto()
  {
    if(isRunningAuto())
      return;

    mustExit = false;
    thRun = new Thread(() -> runBackground());
    thRun.setName(getThreadName());
    thRun.setDaemon(true);
    thRun.start();
  }

  @Override
  public void stopAuto()
  {
    try
    {
      if(isRunningAuto())
      {
        mustExit = true;
        thRun.join(2000);
// il metodo stop() non esiste più in versioni recenti di java        
//        if(thRun.isAlive())
//          thRun.stop();
        thRun = null;
      }
    }
    catch(InterruptedException ex)
    {
      log.error(I("Errore fatale nell'interruzione del task."), ex);
    }
  }

  @Override
  public boolean isRunningAuto()
  {
    return thRun != null && thRun.isAlive();
  }

  @Override
  public void ricaricaConfigurazione()
     throws Exception
  {
  }

  protected String getThreadName()
  {
    return "SERVICE_" + getName();
  }

  @Override
  public void forzaAggiornamento()
  {
    oneShotForce = true;
  }

  /**
   * Funzione di servizio per il thread dedicato.
   * Qui viene eseguito un ciclo infinito.
   */
  protected void runBackground()
  {
    while(!mustExit)
    {
      try
      {
        SimpleTimer st = new SimpleTimer();
        while(!st.isElapsed(mainDelayMillis) && !oneShotForce)
        {
          if(mustExit)
            return;

          Thread.sleep(200);
        }

        oneShotForce = false;
        runOneShot();
        oneShotForce = false;
      }
      catch(Throwable t)
      {
        log.error(I("Errore nel loop del task."), t);
      }
    }
  }

  /**
   * Esegue l'operazione periodica oggetto del thread dedicato.
   * @throws Exception
   */
  protected abstract void runOneShot()
     throws Exception;
}
