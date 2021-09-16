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
package org.sirio5.services.contatori;

import java.sql.Connection;
import org.sirio5.services.CoreServiceExtension;

public interface AggiornaContatori extends CoreServiceExtension
{
  public static final String SERVICE_NAME = "AggiornaContatori";

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param esercizio codice di esercizio richiesto (puo' essere null)
   * @param chiave identificatore del tipo di contatore richiesto
   * @param minVal valore minimo del risultato
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public int getContaInc(String esercizio, String chiave, int minVal, Connection con)
     throws Exception;

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param esercizio codice di esercizio richiesto (puo' essere null)
   * @param chiave identificatore del tipo di contatore richiesto
   * @param minVal valore minimo del risultato
   * @param maxVal valore massimo del risultato
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public int getContaInc(String esercizio, String chiave, int minVal, int maxVal, Connection con)
     throws Exception;

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param esercizio codice di esercizio richiesto (puo' essere null)
   * @param chiave identificatore del tipo di contatore richiesto
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public int getContaInc(String esercizio, String chiave, Connection con)
     throws Exception;

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param chiave identificatore del tipo di contatore richiesto
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public int getContaInc(String chiave, Connection con)
     throws Exception;
}
