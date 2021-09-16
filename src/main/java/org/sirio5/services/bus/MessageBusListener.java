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

/**
 * Interfaccia per gli ascoltatori di eventi.
 *
 * @author Nicola De Nisco
 */
public interface MessageBusListener
{
  /**
   * Funzione principale per l'implementazione di un ascoltatore.
   * Un valore di ritorno diverso da 0 blocca la propagazione del messaggio
   * e viene ritornato dalla funzione sendMessageSync.
   *
   * @param msgID id del messaggio
   * @param originator chi ha inviato il messaggio
   * @param context pacchetto dati associato al messaggio
   * @return 0 per il flusso normale
   * @throws Exception in caso di errore
   */
  public int message(int msgID, Object originator, BusContext context)
     throws Exception;
}
