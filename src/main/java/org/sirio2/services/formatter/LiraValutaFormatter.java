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
package org.sirio2.services.formatter;

import java.text.*;
import org.sirio2.services.AbstractCoreBaseService;
import org.apache.commons.logging.*;

/**
 * Implementazione di formattatore di valuta per la Lira italiana.
 */
public class LiraValutaFormatter extends AbstractCoreBaseService
   implements ValutaFormatter
{
  /** Logging */
  private static Log log = LogFactory.getLog(LiraValutaFormatter.class);
  private DecimalFormat formLira;

  /**
   * Inzializzazione del formattatore.
   */
  @Override
  public void coreInit()
     throws Exception
  {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(',');
    dfs.setGroupingSeparator('.');

    formLira = new DecimalFormat();
    formLira.setDecimalFormatSymbols(dfs);
    formLira.applyLocalizedPattern("###.###.###");
  }

  /**
   * Ritorna la stringa formattata per il valore specificato.
   * NON viene aggiunto il simbolo della divisa.
   * @param value valore da formattare
   * @return la stringa rappresentazione del valore
   * @throws Exception
   */
  @Override
  public String fmtValuta(double value)
     throws Exception
  {
    return formLira.format(value);
  }

  /**
   * Ritorna la stringa formattata per il valore specificato.
   * VIENE aggiunto il simbolo della divisa.
   * @param value valore da formattare
   * @return la stringa rappresentazione del valore
   * @throws Exception
   */
  @Override
  public String fmtValutaDivisa(double value)
     throws Exception
  {
    return "L. " + formLira.format(value);
  }

  /**
   * Data una stringa rappresentazione della valuta
   * ne restituisce il valore in doppia precisione.
   * NON deve essere presente l'indicatore di divisa.
   * @param value la stringa
   * @return il valore
   * @throws Exception
   */
  @Override
  public double parseValuta(String value)
     throws Exception
  {
    return Utils.parseItalianStyle(value, formLira);
  }

  /**
   * Ritorna il formattatore di valuta
   * @return oggetto Format
   * @throws Exception
   */
  public Format getFormat()
     throws Exception
  {
    return formLira;
  }

  /**
   * Arrotondamento della valuta.
   * La lira non richiede operazionioni di arrotondamento.
   * @param value valore da arrotondare
   * @return valore arrotondato in base alla valuta
   * @throws Exception
   */
  @Override
  public double round(double value)
     throws Exception
  {
    return value;
  }

  /**
   * Ritorna la Stringa rappresentazione della divisa in formato testo.
   * @return stringa divisa
   * @throws Exception
   */
  @Override
  public String getDivisaText()
     throws Exception
  {
    return "L.";
  }

  /**
   * Ritorna la Stringa rappresentazione della divisa in formato HTML.
   * @return stringa divisa per HTML.
   * @throws Exception
   */
  @Override
  public String getDivisaHtml()
     throws Exception
  {
    return "L.";
  }
}
