/*
 * Copyright (C) 2021 Nicola De Nisco
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

import java.io.Serializable;
import java.util.EventListener;

/**
 * Interfaccia per la notifica di avvenuto dispatch del messaggio.
 *
 * @author Nicola De Nisco
 */
public interface BusPostActionListener extends EventListener, Serializable
{
  /**
   * Notifica l'avvenuto invio del messaggio sul bus.
   * @param msgID id del messaggio
   * @param originator l'oggetto che invia il messaggio
   * @param context pacchetto dati associato al messaggio
   */
  public void actionPerformed(int msgID, Object originator, BusContext context);
}
