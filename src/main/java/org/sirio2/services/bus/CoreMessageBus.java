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
package org.sirio2.services.bus;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.BaseService;
import org.commonlib5.utils.SimpleTimer;
import org.sirio2.CoreConst;
import org.sirio2.services.localization.INT;

/**
 * Implementazione del message bus.
 * @author Nicola De Nisco
 */
public class CoreMessageBus extends BaseService
   implements MessageBus
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreMessageBus.class);
  /** Mappa degli ascoltatori. */
  protected ConcurrentSkipListMap<String, MessageBusListener> busMap = new ConcurrentSkipListMap<>();
  /** Mappa degli ascoltatori SQL. */
  protected ConcurrentSkipListMap<String, MessageBusListenerTransaction> busMapTrs = new ConcurrentSkipListMap<>();
  /** Thread per la spedizione asincrona. */
  protected Thread tAsync;
  /** Coda messaggi asincroni. */
  protected LinkedBlockingQueue<QueueInfo> queue = new LinkedBlockingQueue<>();
  /** Soglia per il warning velocità di risposta. */
  protected int delayWarning = 50;
  /** Ritardo di default per i messaggi asincroni. */
  protected long delayDefaultAsyncMillis = 300;
  /** Timer per il segnale IDLE a 10 minuti. */
  private final SimpleTimer idle10Timer = new SimpleTimer();
  /** Timer per il segnale IDLE a 30 minuti. */
  private final SimpleTimer idle30Timer = new SimpleTimer();
  /** Timer per il segnale IDLE a 60 minuti. */
  private final SimpleTimer idle60Timer = new SimpleTimer();

  @Override
  public void init()
  {
    Configuration cfg = getConfiguration();
    delayWarning = cfg.getInt("delayWarningMillis", 50);

    tAsync = new Thread(() -> runAsync());
    tAsync.setName("busasync");
    tAsync.setDaemon(true);
    tAsync.start();

    // servizio inizializzato correttamentea
    setInit(true);
  }

  @Override
  public int sendMessageSync(int msgID, Object originator, BusContext context)
  {
    if(busMap.isEmpty())
      return 0;

    int val = 0;
    SimpleTimer st = new SimpleTimer();
    for(Map.Entry<String, MessageBusListener> entry : busMap.entrySet())
    {
      String nome = entry.getKey();
      MessageBusListener listener = entry.getValue();

      try
      {
        st.reset();

        if((val = listener.message(msgID, originator, context)) != 0)
          return val;

        if(st.getElapsed() > delayWarning)
          log.warn(INT.I("%s [%s] ha elaborato in %d millisecondi", nome, listener.getClass().getName(), st.getElapsed()));
      }
      catch(Throwable ex)
      {
        log.error(INT.I("Errore BUS messaggio %d in %s ", msgID, listener.getClass().getName()), ex);
      }
    }

    // eventuale azione di post action
    if(context != null && !context.postListener.isEmpty())
      context.postListener.forEach((l) -> l.actionPerformed(msgID, originator, context));

    return 0;
  }

  @Override
  public int sendMessageTransaction(int msgID, Object originator, Connection con, BusContext context)
     throws Exception
  {
    if(busMapTrs.isEmpty())
      return 0;

    int val = 0;
    SimpleTimer st = new SimpleTimer();
    for(Map.Entry<String, MessageBusListenerTransaction> entry : busMapTrs.entrySet())
    {
      String nome = entry.getKey();
      MessageBusListenerTransaction listener = entry.getValue();
      st.reset();

      if((val = listener.message(msgID, originator, con, context)) != 0)
        return val;

      if(st.getElapsed() > delayWarning)
        log.warn(INT.I("%s [%s] ha elaborato in  %d  millisecondi", nome, listener.getClass().getName(), st.getElapsed()));
    }

    return 0;
  }

  @Override
  public void sendMessageAsync(int msgID, Object originator, BusContext context)
  {
    if(busMap.isEmpty())
      return;

    QueueInfo qi = new QueueInfo();
    qi.msgID = msgID;
    qi.delay = delayDefaultAsyncMillis;
    qi.originator = originator;
    qi.context = context;
    qi.originTime = System.currentTimeMillis();

    sendMessageAsync(qi);
  }

  @Override
  public void sendMessageAsyncDelay(int msgID, int delay, Object originator, BusContext context)
  {
    if(busMap.isEmpty())
      return;

    QueueInfo qi = new QueueInfo();
    qi.msgID = msgID;
    qi.delay = delay;
    qi.originator = originator;
    qi.context = context;
    qi.originTime = System.currentTimeMillis();

    sendMessageAsync(qi);
  }

  @Override
  public void sendMessagePackAsync(Collection<QueueInfo> clQueue)
  {
    for(QueueInfo qi : clQueue)
    {
      if(qi.delay == 0)
        qi.delay = delayDefaultAsyncMillis;

      if(qi.originTime == 0)
        qi.originTime = System.currentTimeMillis();

      sendMessageAsync(qi);
    }
  }

  @Override
  public void sendMessageAsync(QueueInfo qi)
  {
    try
    {
      if(busMap.isEmpty())
        return;

      queue.put(qi);
    }
    catch(InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Funzione di servizio del thread di spedizione differita.
   * Estrae dalla coda eventi l'evento da inviare sul bus e
   * lo invia ai destinatari.
   */
  protected void runAsync()
  {
    while(true)
    {
      try
      {
        QueueInfo qi;

        while((qi = queue.poll(3, TimeUnit.SECONDS)) != null)
        {
          // in caso di evento ritardato attende per i millisecondi indicati
          // il tempo di attesa viene misurato dal momento di inserimento nella coda
          if(qi.delay > 0)
          {
            long elapsed, twait;
            while((elapsed = (System.currentTimeMillis() - qi.originTime)) < qi.delay)
            {
              if((twait = (qi.delay - elapsed) >> 1) < 10)
                twait = 10;

              Thread.sleep(twait);
            }
          }

          sendMessageSync(qi.msgID, qi.originator, qi.context);
        }

        idle();
      }
      catch(Throwable t)
      {
        log.error(INT.I("Errore di BUS:"), t);
      }
    }
  }

  @Override
  public void registerEventListner(String name, MessageBusListener listener)
  {
    busMap.put(name, listener);
  }

  @Override
  public void removeEventListner(String name)
  {
    busMap.remove(name);
  }

  @Override
  public void registerEventListnerTrs(String name, MessageBusListenerTransaction listener)
  {
    busMapTrs.put(name, listener);
  }

  @Override
  public void removeEventListnerTrs(String name)
  {
    busMapTrs.remove(name);
  }

  protected void idle()
  {
    if(idle10Timer.isElapsed(10 * CoreConst.ONE_MINUTE_MILLIS))
    {
      log.debug("Emetto segnale IDLE 10 minuti.");
      sendMessageSync(BusMessages.IDLE_10_MINUTES, this, null);
      idle10Timer.reset();
    }

    if(idle30Timer.isElapsed(30 * CoreConst.ONE_MINUTE_MILLIS))
    {
      log.debug("Emetto segnale IDLE 30 minuti.");
      sendMessageSync(BusMessages.IDLE_30_MINUTES, this, null);
      idle30Timer.reset();
    }

    if(idle60Timer.isElapsed(60 * CoreConst.ONE_MINUTE_MILLIS))
    {
      log.debug("Emetto segnale IDLE 60 minuti.");
      sendMessageSync(BusMessages.IDLE_60_MINUTES, this, null);
      idle60Timer.reset();
    }
  }
}
