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
import java.util.Collection;
import org.apache.turbine.services.Service;

/**
 * Servizio per la distribuzione dei messaggi (eventi)
 * all'interno dell'applicazione applicazione.
 *
 * @author Nicola De Nisco
 */
public interface MessageBus extends Service
{
  public static final String SERVICE_NAME = "MessageBus";

  public static class QueueInfo
  {
    public int msgID;
    public Object originator;
    public BusContext context;
    public long delay = 0, originTime = 0;
  }

  /**
   * Aggiunge un ascoltatore di eventi al bus.
   * @param name nome simbolico dell'ascoltatore
   * @param listener interfaccia di ascolto
   */
  public void registerEventListner(String name, MessageBusListener listener);

  /**
   * Rimuove un ascoltatore.
   * @param name nome simbolico dell'ascoltatore
   */
  public void removeEventListner(String name);

  /**
   * Aggiunge un ascoltatore di eventi al bus.
   * Versione specializzata per eventi di storage.
   * @param name nome simbolico dell'ascoltatore
   * @param listener interfaccia di ascolto
   */
  public void registerEventListnerTrs(String name, MessageBusListenerTransaction listener);

  /**
   * Rimuove un ascoltatore.
   * Versione specializzata per eventi di storage.
   * @param name nome simbolico dell'ascoltatore
   */
  public void removeEventListnerTrs(String name);

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. Se uno di questi segnala ritornando
   * un valore diverso da 0 allora la propagazione del messaggio si
   * interrompe e il valore viene riportato.
   * ATTENZIONE: se non strettamente necessario è sempre preferibile
   * un invio asincrono (vedi sendMessageAsync).
   * Diversamente viene ritornato zero.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   * @return eventuale codice di ritorno
   */
  public int sendMessageSync(int msgID, Object originator, BusContext context);

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
  public int sendMessageTransaction(int msgID, Object originator, Connection con, BusContext context)
     throws Exception;

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Una coda garantisce la sequenza corretta dei messaggi.
   * Il messaggio verrà ritardato di un minimo di 300 millisecondi (varibile a setup).
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   */
  public void sendMessageAsync(int msgID, Object originator, BusContext context);

  /**
   * Funzione per l'invio di un messaggio a tutti gli ascoltatori.
   * Gli ascoltatori registrati la riceveranno in ordine alfabetico
   * del nome di registrazione. L'invio è asincrono: la funzione ritorna
   * immediatamente dopo aver segnalato ad un thread dedicato di inivare
   * il messaggio. Il thread di invio assicura che almeno 'delay' millisecondi
   * siano trascorsi prima dell'invio del messaggio.
   * Una coda garantisce la sequenza corretta dei messaggi.
   * @param msgID id del messaggio
   * @param delay ritardo in millisecondi per l'invio del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   */
  public void sendMessageAsyncDelay(int msgID, int delay, Object originator, BusContext context);

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
  public void sendMessageAsync(QueueInfo qi);

  /**
   * Invia un pacchetto di messaggi in modalità asincrona.
   * @param clQueue collezione di messaggi
   */
  public void sendMessagePackAsync(Collection<QueueInfo> clQueue);
}
