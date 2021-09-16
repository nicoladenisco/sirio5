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
package org.sirio5.services.allarmi;

import java.util.List;
import org.apache.turbine.services.Service;

/**
 * Servizio gestione degli allarmi.
 *
 * @author Nicola De Nisco
 */
public interface ServAllarmi extends Service
{
  public static final String SERVICE_NAME = "ServAllarmi";

  // stato degli allarmi
  public static final String ALLARME_ATTIVO = "A";
  public static final String ALLARME_DISATTIVATO = "D";

  // tipi di allarme
  public static final String TIPALLARME_INFO = "I";
  public static final String TIPALLARME_WARNING = "W";
  public static final String TIPALLARME_ERROR = "E";
  public static final String TIPALLARME_FATAL = "F";

  /**
   * Ritorna numero allarmi attivi.
   * @return allarmi attivi
   * @throws Exception
   */
  public int getActiveAllarms()
     throws Exception;

  /**
   * Recupera allarmi attivi per il servizio specificato.
   * @param serv servizio di interesse
   * @return lista allarmi
   */
  public List checkAllarmi(String serv);

  /**
   * Recupera allarmi attivi per il servizio specificato.
   * @param serv servizio di interesse
   * @param comp componente di interesse
   * @return lista allarmi
   */
  public List checkAllarmi(String serv, String comp);

  /**
   * Disattiva un allarme.
   * @param idAllarme identificatore dell'allarme
   * @param idUser utente che esegue l'operazione
   * @param messaggio messaggio da utilizzare come note di disattivazione
   * @throws Exception
   */
  public void disattivaAllarme(int idAllarme, int idUser, String messaggio)
     throws Exception;

  /**
   * Disattiva tutti gli allammi attivi.
   * @param idUser utente che esegue l'operazione
   * @param messaggio messaggio da utilizzare come note di disattivazione
   * @throws Exception
   */
  void disattivaAllarmiAttivi(int idUser, String messaggio)
     throws Exception;

  /**
   * Invia un messaggio nella log allarmi.
   * @param severity severità (una delle costanti TIPALLARME_...)
   * @param servizio servizio che segnala l'allarme
   * @param componente componente responsabile per l'allarme
   * @param messaggio messaggio descrittivo della natura dell'allarme
   * @param visibilita visibilità dell'allarme
   */
  public void allarmLog(String severity, String servizio, String componente, String messaggio, int visibilita);

  /**
   * Invia un messaggio nella log comunicazioni.
   * @param severity severità (una delle costanti TIPALLARME_...)
   * @param servizio servizio che segnala l'allarme
   * @param componente componente responsabile per l'allarme
   * @param messaggio messaggio descrittivo della natura dell'allarme
   * @param visibilita visibilità dell'allarme
   */
  public void commLog(String severity, String servizio, String componente, String messaggio, int visibilita);

  /**
   * Invia un messaggio nella log comunicazioni.
   * @param severity severità (una delle costanti TIPALLARME_...)
   * @param servizio servizio che segnala l'allarme
   * @param componente componente responsabile per l'allarme
   * @param messaggio messaggio descrittivo della natura dell'allarme
   * @param visibilita visibilità dell'allarme
   * @param note note generiche da aggiungere alla log
   * @param info informazioni ulteriori (max 4 elementi)
   */
  public void commLog(String severity, String servizio, String componente, String messaggio, int visibilita, String note, String... info);
}
