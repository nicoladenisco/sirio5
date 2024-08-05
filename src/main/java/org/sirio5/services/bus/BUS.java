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
package org.sirio5.services.bus;

import java.sql.Connection;
import java.util.Collection;
import org.apache.turbine.services.TurbineServices;

/**
 * Funzioni statiche per comunicazione con il bus messaggi.
 * Vedi interfaccia MessageBus per dettagli.
 *
 * @author Nicola De Nisco
 */
public class BUS
{
  private static Object __mb = null;

  public static MessageBus getService()
  {
    if(__mb == null)
      __mb = TurbineServices.getInstance().getService(MessageBus.SERVICE_NAME);
    return (MessageBus) __mb;
  }

  /**
   * Aggiunge un ascoltatore di eventi al bus.
   * @param name nome simbolico con cui un ascoltatore si registra sul bus eventi.
   * @param listener ascoltatore dei messaggi
   */
  public static void registerEventListner(String name, MessageBusListener listener)
  {
    getService().registerEventListner(name, listener);
  }

  /**
   * Aggiunge un ascoltatore di eventi al bus.
   * Il nome dell'ascoltatore è il nome della classe.
   * @param listener ascoltatore dei messaggi
   */
  public static void registerEventListner(MessageBusListener listener)
  {
    getService().registerEventListner(listener.getClass().getName(), listener);
  }

  /**
   * Rimuove un ascoltatore.
   * @param name simbolico con il quale l'ascoltatore si è registrato
   */
  public static void removeEventListner(String name)
  {
    getService().removeEventListner(name);
  }

  /**
   * Rimuove un ascoltatore.
   * Il nome dell'ascoltatore è il nome della classe.
   */
  public static void removeEventListner(MessageBusListener listener)
  {
    getService().removeEventListner(listener.getClass().getName());
  }

  /**
   * Aggiunge un ascoltatore di eventi al bus.
   * Versione specializzata per eventi di storage.
   * @param name nome simbolico dell'ascoltatore
   * @param listener interfaccia di ascolto
   */
  public static void registerEventListnerTrs(String name, MessageBusListenerTransaction listener)
  {
    getService().registerEventListnerTrs(name, listener);
  }

  /**
   * Rimuove un ascoltatore.
   * Versione specializzata per eventi di storage.
   * @param name nome simbolico dell'ascoltatore
   */
  public static void removeEventListnerTrs(String name)
  {
    getService().removeEventListnerTrs(name);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. Se uno di questi segnala ritornando
   * un valore diverso da 0 allora la propagazione del messaggio si
   * interrompe e il valore viene riportato.
   * Diversamente viene ritornato zero.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   * @return eventuale codice di ritorno
   */
  public static int sendMessageSync(int msgID, Object originator, BusContext context)
  {
    return getService().sendMessageSync(msgID, originator, context);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. Se uno di questi segnala ritornando
   * un valore diverso da 0 allora la propagazione del messaggio si
   * interrompe e il valore viene riportato.
   * Diversamente viene ritornato zero.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param con connessione SQL sotto transazione
   * @param context pacchetto dati associato al messaggio
   * @return eventuale codice di ritorno
   * @throws Exception
   */
  public static int sendMessageTransaction(int msgID, Object originator, Connection con, BusContext context)
     throws Exception
  {
    return getService().sendMessageTransaction(msgID, originator, con, context);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Una coda garantisce la sequenza corretta dei messaggi.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   */
  public static void sendMessageAsync(int msgID, Object originator, BusContext context)
  {
    getService().sendMessageAsync(msgID, originator, context);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Una coda garantisce la sequenza corretta dei messaggi.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   */
  public static void sendMessageAsync(int msgID, Object originator)
  {
    getService().sendMessageAsync(msgID, originator, null);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Il thread di invio assicura che almeno 'delay' millisecondi
   * siano trascorsi prima dell'invio del messaggio. L'attesa potrebbe essere
   * comunque superiore.
   * Una coda garantisce la sequenza corretta dei messaggi.
   * @param msgID id del messaggio
   * @param delay ritardo in millisecondi per l'invio del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   */
  public static void sendMessageAsyncDelay(int msgID, int delay, Object originator, BusContext context)
  {
    getService().sendMessageAsyncDelay(msgID, delay, originator, context);
  }

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Il thread di invio assicura che almeno 'delay' millisecondi
   * siano trascorsi prima dell'invio del messaggio.
   * Una coda garantisce la sequenza corretta dei messaggi.
   * @param qi parametri del messaggio
   */
  public static void sendMessageAsync(MessageBus.QueueInfo qi)
  {
    getService().sendMessageAsync(qi);
  }

  /**
   * Invia un pacchetto di messaggi in modalità asincrona.
   * @param clQueue collezione di messaggi
   */
  public static void sendMessagePackAsync(Collection<MessageBus.QueueInfo> clQueue)
  {
    getService().sendMessagePackAsync(clQueue);
  }

  /**
   * Ritorna helper per memorizzazione eventi da inviare in ritardo.
   * @return istanza dell'helper
   */
  public static BusMessageQueueHelper getHelper()
  {
    return new BusMessageQueueHelper();
  }
}
