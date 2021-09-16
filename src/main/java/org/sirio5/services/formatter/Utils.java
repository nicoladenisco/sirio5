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
package org.sirio5.services.formatter;

import java.text.NumberFormat;
import java.text.ParseException;
import org.apache.commons.lang.StringUtils;
import org.sirio5.utils.SU;

/**
 * Utility per i formattatori.
 *
 * @author Nicola De Nisco
 */
public class Utils
{
  /**
   * Parse di stringa in un formato compatibile con l'italia.
   * Oltre al formato canonico fa il parsing anche di valori tipo 1200.34 e 1200,34
   * considerandoli come numero con virgola in ogni caso (1.200,34).
   * Una stringa vuota o null produce 0.0.
   * @param value valore da convertire
   * @param numFormat formato canonico aspettato
   * @return valore dopo il parsing
   * @throws ParseException nel caso la stringa non possa essere convertita
   */
  protected static double parseItalianStyle(String value, NumberFormat numFormat)
     throws ParseException
  {
    String s = SU.okStrNull(value);
    if(s == null)
      return 0.0;

    // verifichiamo numero di virgole e numero di punti
    int nv = StringUtils.countMatches(s, ",");
    int np = StringUtils.countMatches(s, ".");

    if(nv == 0 && np == 1)
    {
      // qualcosa del tipo 1200.34 inteso come 1.200,34
      return Double.parseDouble(s);
    }

    if(nv == 1 && np == 0)
    {
      // qualcosa del tipo 1200,34 inteso come 1.200,34
      return Double.parseDouble(s.replace(',', '.'));
    }

    return numFormat.parse(s).doubleValue();
  }
}
