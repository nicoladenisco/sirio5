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
package org.sirio2.services.contatori;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.StringOper;

/**
 * Utility per la gestione dei contatori permanenti.
 *
 * @author Nicola De Nisco
 */
public class AGC
{
  public static Object ac;

  public static AggiornaContatori getService()
  {
    if(ac == null)
      ac = TurbineServices.getInstance().getService(AggiornaContatori.SERVICE_NAME);

    return (AggiornaContatori) ac;
  }

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param esercizio codice di esercizio richiesto (puo' essere null)
   * @param chiave identificatore del tipo di contatore richiesto
   * @param minVal valore minimo del risultato
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public static int getContaInc(String esercizio, String chiave, int minVal, Connection con)
     throws Exception
  {
    return getService().getContaInc(esercizio, chiave, minVal, con);
  }

  public static int getContaInc(String esercizio, String chiave, int minVal, int maxVal, Connection con)
     throws Exception
  {
    return getService().getContaInc(esercizio, chiave, minVal, maxVal, con);
  }

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param esercizio codice di esercizio richiesto (puo' essere null)
   * @param chiave identificatore del tipo di contatore richiesto
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public static int getContaInc(String esercizio, String chiave, Connection con)
     throws Exception
  {
    return getService().getContaInc(esercizio, chiave, con);
  }

  /**
   * Ritorna un valore di contatore incrementando il contatore stesso.
   * @param chiave identificatore del tipo di contatore richiesto
   * @param con connessione SQL per transazione (puo' essere null)
   * @return il valore del contatore
   * @throws Exception
   */
  public static int getContaInc(String chiave, Connection con)
     throws Exception
  {
    return getService().getContaInc(chiave, con);
  }

  public static int contatore(String esercizio, String chiave, Connection con)
     throws Exception
  {
    return getService().getContaInc(esercizio, chiave, con);
  }

  public static int contatore(String chiave, Connection con)
     throws Exception
  {
    return getService().getContaInc(chiave, con);
  }

  public static final SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmm");

  public static String generaCodice(String chiave, int numProg, Connection con)
     throws Exception
  {
    return generaCodice(chiave, 0, numProg, con);
  }

  /**
   * Genera un codice a partire da una data e un contatore.
   * Il codice generato ha una parte di data secondo la pattern yyyyMMddHHmm
   * e una parte progressiva in base al valore del contatore.
   * @param chiave identificativo del contatore
   * @param numDate numero di caratteri della data (vedi pattern)
   * @param numProg numero di caratteri della parte contatore
   * @param con connessione db
   * @return il codice completo ES: 2012000013
   * @throws Exception
   */
  public static String generaCodice(String chiave, int numDate, int numProg, Connection con)
     throws Exception
  {
    return generaCodice(numDate == 0 ? null : new Date(), chiave, numDate, numProg, con);
  }

  /**
   * Genera un codice a partire da una data e un contatore.
   * Il codice generato ha una parte di data secondo la pattern yyyyMMddHHmm
   * e una parte progressiva in base al valore del contatore.
   * @param dtaRif data di riferimento per la parte iniziale
   * @param chiave identificativo del contatore
   * @param numDate numero di caratteri della data (vedi pattern) 0=perpetuo
   * @param numProg numero di caratteri della parte contatore
   * @param con connessione db
   * @return il codice completo ES: 2012000013
   * @throws Exception
   */
  public static String generaCodice(Date dtaRif, String chiave, int numDate, int numProg, Connection con)
     throws Exception
  {
    return generaCodice(dtaRif, chiave, numDate, numProg, 0, con);
  }

  /**
   * Genera un codice a partire da una data e un contatore.
   * Il codice generato ha una parte di data secondo la pattern yyyyMMddHHmm
   * e una parte progressiva in base al valore del contatore.
   * @param dtaRif data di riferimento per la parte iniziale
   * @param chiave identificativo del contatore
   * @param numDate numero di caratteri della data (vedi pattern) 0=perpetuo
   * @param numProg numero di caratteri della parte contatore
   * @param offset valore di spiazzamento del contatore (viene aggiunto al contatore)
   * @param con connessione db
   * @return il codice completo ES: 2012000013
   * @throws Exception
   */
  public static String generaCodice(Date dtaRif, String chiave, int numDate, int numProg, int offset, Connection con)
     throws Exception
  {
    if(numDate == 0)
    {
      int conta = getContaInc(chiave, con);
      return StringOper.fmtZero(conta, numProg);
    }

    String DataTimeAdesso = sf.format(dtaRif);
    String Esercizio = DataTimeAdesso.substring(0, numDate);
    int conta = getContaInc(Esercizio, chiave, con);
    String progressivo = StringOper.fmtZero(conta + offset, numProg);
    return Esercizio + progressivo;
  }

  /**
   * Genera un codice a partire da una data e un contatore.
   * Il codice generato ha una parte di data secondo la pattern yyyyMMddHHmm
   * e una parte progressiva in base al valore del contatore.
   * @param dtaRif data di riferimento per la parte iniziale
   * @param chiave identificativo del contatore
   * @param sCiclo periodicita' del contatore: P=perpetuo A=annuale M=mensile G=giornaliero
   * @param numProg numero di caratteri della parte contatore
   * @param con connessione db
   * @return il codice completo ES: 2012000013
   * @throws Exception
   */
  public static String generaCodice(Date dtaRif, String chiave, String sCiclo, int numProg, Connection con)
     throws Exception
  {
    int iOffset = 0;
    if("A".equalsIgnoreCase(sCiclo))
      iOffset = 4;
    else if("M".equalsIgnoreCase(sCiclo))
      iOffset = 6;
    else if("G".equalsIgnoreCase(sCiclo))
      iOffset = 8;
    else if("P".equalsIgnoreCase(sCiclo))
      iOffset = 0;

    return generaCodice(dtaRif, chiave, iOffset, numProg, con);
  }

  public static String generaCodice(String chiave, String esercizio, int numDate, int numProg, Connection con)
     throws Exception
  {
    if(numDate == 0)
    {
      int conta = getContaInc(chiave, con);
      return StringOper.fmtZero(conta, numProg);
    }

    int conta = getContaInc(esercizio, chiave, con);
    String progressivo = StringOper.fmtZero(conta, numProg);
    return esercizio + progressivo;
  }
}
