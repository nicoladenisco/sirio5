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
package org.sirio5.beans.shared;

import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.commonlib5.utils.Classificatore;
import org.sirio5.services.bus.BUS;
import org.sirio5.services.bus.BusContext;
import org.sirio5.services.bus.MessageBusListener;

/**
 * Helper per la rimozione di un attributo di sessione a seguito di un evento BUS.
 * Il metodo registerRemoveInfo() consente di legare un messaggio
 * del BUS che causerà la rimozione di un attributo di sessione.
 * Una istanza di questo helper si registrerà in sessione e si
 * rimuoverà dal bus quando viene rimosso dalla sessione.
 *
 * @author Nicola De Nisco
 */
public class SessionRemoveBusHelper implements HttpSessionBindingListener, MessageBusListener
{
  private HttpSession mysession;
  private Classificatore<Integer, String> removeMap = new Classificatore<>();
  // costanti
  public static final String BEAN_KEY = "BeanRemoveBusHelper:BEAN_KEY";

  private SessionRemoveBusHelper()
  {
  }

  /**
   * Registra rimozione attributi.
   * Registra l'evento del BUS con una serie di attributi di sessione
   * da rimuovere al verificarsi dell'evento.
   * @param session sessione di riferimento
   * @param msgID evento da monitorare
   * @param attributeNames attributi di sessione da distruggere
   */
  public static void registerRemoveInfo(HttpSession session, int msgID, String... attributeNames)
  {
    SessionRemoveBusHelper hlp = (SessionRemoveBusHelper) session.getAttribute(BEAN_KEY);
    if(hlp == null)
      session.setAttribute(BEAN_KEY, hlp = new SessionRemoveBusHelper());

    synchronized(hlp)
    {
      hlp.removeMap.aggiungiTutti(msgID, attributeNames);
    }
  }

  /**
   * Registra rimozione attributi.
   * Registra l'evento del BUS con una serie di attributi di sessione
   * da rimuovere al verificarsi dell'evento.
   * @param session sessione di riferimento
   * @param attributeName attributo di sessione da distruggere
   * @param msgIDs eventi da monitorare
   */
  public static void registerRemoveInfo(HttpSession session, String attributeName, int... msgIDs)
  {
    SessionRemoveBusHelper hlp = (SessionRemoveBusHelper) session.getAttribute(BEAN_KEY);
    if(hlp == null)
      session.setAttribute(BEAN_KEY, hlp = new SessionRemoveBusHelper());

    synchronized(hlp)
    {
      for(int i = 0; i < msgIDs.length; i++)
        hlp.removeMap.aggiungi(msgIDs[i], attributeName);
    }
  }

  @Override
  public synchronized void valueBound(HttpSessionBindingEvent hsbe)
  {
    mysession = hsbe.getSession();
    BUS.registerEventListner(BEAN_KEY, this);
  }

  @Override
  public synchronized void valueUnbound(HttpSessionBindingEvent hsbe)
  {
    BUS.removeEventListner(BEAN_KEY);
  }

  @Override
  public int message(int msgID, Object originator, BusContext context)
     throws Exception
  {
    List<String> keyRemove = removeMap.get(msgID);

    if(keyRemove != null)
    {
      // rimuove dalla sessione tutte le chiavi memorizzate
      if(mysession != null)
      {
        for(String key : keyRemove)
        {
          try
          {
            mysession.removeAttribute(key);
          }
          catch(Throwable t)
          {
            Logger.getLogger(SessionRemoveBusHelper.class.getName())
               .log(Level.SEVERE, "Error removing from session: {0}", t.getMessage());
          }
        }
      }

      // rimuove il messaggio dalla mappa
      removeMap.remove(msgID);

      // se la mappa è vuota si autorimuove dalla sessione (come conseguenza si scollega dal BUS)
      if(removeMap.isEmpty() && mysession != null)
        mysession.removeAttribute(BEAN_KEY);
    }

    return 0;
  }
}
