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

/**
 * Interfaccia di servizio con task di elaborazione in background.
 * Viene ereditata dai servizi che utilizzano task in background
 * per eseguire operazioni asincrone.
 * Consente di arrestare e avviare le operzioni in background.
 *
 * @author Nicola De Nisco
 */
public interface CoreBackgroundService extends CoreServiceExtension
{
  /**
   * Avvia l'elaborazione automatica.
   * Un thread dedicato viene attivato per consentire
   * una elaborazione in backgroud delle accettazioni
   * mentre arrivano.
   */
  public void startAuto();

  /**
   * Arresta l'elaborazione automatica.
   */
  public void stopAuto();

  /**
   * Ritorna lo stato dell'elaborazione automatica.
   * @return vero se attiva
   */
  public boolean isRunningAuto();

  /**
   * Ricarica la configurazione del dicom server dalle tabelle di setup.
   * @throws Exception
   */
  public void ricaricaConfigurazione()
     throws Exception;

  /**
   * Forza un impulso del thread in background.
   */
  public void forzaAggiornamento();
}
