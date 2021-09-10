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
package org.sirio2.services;

import java.util.List;

/**
 * Interfaccia di un fornitore di informazioni di setup.
 * I servizi che implementano questa interfaccia verranno
 * interrogati su informazioni da fornire nella visualizzazione
 * dei dati di setup dell'applicazione.
 *
 * @author Nicola De Nisco
 */
public interface InfoSetupInterface
{
  /**
   * Recupera informazioni di setup.
   * Vengono inserite nell'array indicato una serie di blocchi
   * di che contengono informazioni utili circa il setup applicazione.
   * @param arInfo array da popolare
   * @param isAdmin vero se la richiesta viene dall'amministratore di sistema
   * @throws Exception
   */
  public void populateInfoSetup(List<InfoSetupBlock> arInfo, boolean isAdmin)
     throws Exception;

  /**
   * Oscura password.
   * Se l'utente è admin restituisce la password altrimenti '****'.
   * @param passwd password da oscurare
   * @param isAdmin vero se amministratore
   * @return password oppure '****'
   */
  public default String adjPassword(String passwd, boolean isAdmin)
  {
    return isAdmin ? passwd : "****";
  }
}
