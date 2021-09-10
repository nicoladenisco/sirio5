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

import java.text.DecimalFormatSymbols;
import org.sirio2.services.CoreServiceExtension;

public interface NumFormatter extends CoreServiceExtension
{
    public static final String SERVICE_NAME = "NumFormatter";

    public DecimalFormatSymbols getSymbols();

    /**
     * Ritorna una stringa formattata
     * rappresentazione locale del valore indicato.
     * @param val valore da formattare
     * @param nInt numeri di interi fissi da visualizzare
     * @param nDec numero di decimali fissi da visuallizzare
     * @return stringa
     */
    public String format(double val, int nInt, int nDec) throws Exception;

    /**
     * Effettua il parsing di una stringa correttemente formattata.
     * @param s stringa da interpretare
     * @param nInt numeri di interi fissi da visualizzare
     * @param nDec numero di decimali fissi da visuallizzare
     * @return valore relativo
     */
    public double parseDouble(String s, int nInt, int nDec) throws Exception;
}

