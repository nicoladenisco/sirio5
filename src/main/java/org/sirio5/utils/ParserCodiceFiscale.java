/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.sirio5.utils.CalcolaCodiceFiscale.*;

/**
 * Parser del codice fiscale.
 * Dal codice fiscale determina comune di nascita, data di nascita, sesso.
 *
 * @author Nicola De Nisco
 */
public class ParserCodiceFiscale
{
  private final String codiceFiscale;
  private Date dataNascita;
  private String codiceBelfiore, sesso;

  public ParserCodiceFiscale(String codiceFiscale)
  {
    this.codiceFiscale = SU.okStr(codiceFiscale).toUpperCase();
    if(this.codiceFiscale.length() != 16)
      return;

    dataNascita = calcolaNascitaDaCf();
    codiceBelfiore = getCodiceComuneDaCf();
    sesso = getSessoDaCf();
  }

  /**
   * Dato un codice fiscale calcola la data di nascita corrispondente.
   * @return data di nascita o null per errore
   * @throws java.lang.Exception
   */
  private Date calcolaNascitaDaCf()
  {
    try
    {
      // DNSNCL66M27G902V
      // 012345678901234567890
      int anno = Integer.parseInt(codiceFiscale.substring(6, 8));
      int mese = getNumMese(codiceFiscale.charAt(8));
      int giorno = Integer.parseInt(codiceFiscale.substring(9, 11));

      // le donne hanno il giorno + 40
      if(giorno > 40)
        giorno -= 40;

      // 30 giorni ha Novembre con April, Giugno e Settembre
      // di 28 (0 29) ce n'è uno, tutto il resto ne ha 31
      // giorno=31 è valore accettabile
      if(mese == -1 || giorno <= 0 || giorno > 31)
        return null;

      GregorianCalendar c = new GregorianCalendar();
      int year = c.get(Calendar.YEAR);
      if(year > 2000)
      {
        year = year - 2000;
        if(anno > year)
          anno += 1900;
        else
          anno += 2000;
      }
      else
        anno += 1900;

      c.set(anno, mese, giorno, 0, 0, 0);
      return c.getTime();
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  /**
   * Dato un codice fiscale recupera il codice belfiore del comune.
   * @return codice belfiore o null per errore
   * @throws Exception
   */
  private String getCodiceComuneDaCf()
  {
    // DNSNCL66M27G902V
    // 012345678901234567890
    return codiceFiscale.substring(11, 15);
  }

  /**
   * Dato un codice fiscale recupera il sesso.
   * @param codiceFiscale input
   * @return M/F o null per errore
   * @throws Exception
   */
  private String getSessoDaCf()
  {
    try
    {
      // DNSNCL66M27G902V
      // 012345678901234567890
      int giorno = Integer.parseInt(codiceFiscale.substring(9, 11));
      return giorno > 40 ? "F" : "M";
    }
    catch(NumberFormatException ex)
    {
      return null;
    }
  }

  public String getCodiceFiscale()
  {
    return codiceFiscale;
  }

  public Date getDataNascita()
  {
    return dataNascita;
  }

  public String getCodiceBelfiore()
  {
    return codiceBelfiore;
  }

  public String getSesso()
  {
    return sesso;
  }
}
