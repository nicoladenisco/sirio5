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

import org.sirio5.services.CoreServiceExtension;

/**
 * Interfaccia per la definizione di servizi di formattazione della valuta.
 */
public interface ValutaFormatter extends CoreServiceExtension
{
    public static final String SERVICE_NAME = "ValutaFormatter";

    /**
     * Ritorna la stringa formattata per il valore specificato.
     * NON viene aggiunto il simbolo della divisa.
     * @param value valore da formattare
     * @return la stringa rappresentazione del valore
     * @throws Exception
     */
    public String fmtValuta(double value) throws Exception;

    /**
     * Ritorna la stringa formattata per il valore specificato.
     * VIENE aggiunto il simbolo della divisa.
     * @param value valore da formattare
     * @return la stringa rappresentazione del valore
     * @throws Exception
     */
    public String fmtValutaDivisa(double value) throws Exception;

    /**
     * Data una stringa rappresentazione della valuta
     * ne restituisce il valore in doppia precisione.
     * NON deve essere presente l'indicatore di divisa.
     * @param value la stringa
     * @return il valore
     * @throws Exception
     */
    public double parseValuta(String value) throws Exception;

    /**
     * Arrotonda il valore indicato secondo le regole in uso
     * per la divisa. (ES: Euro arrotonda alle due cifre decimali).
     * @param value
     * @return
     * @throws Exception
     */
    public double round(double value) throws Exception;

    /**
     * Ritorna la Stringa rappresentazione della divisa in formato testo.
     * @return stringa divisa
     * @throws Exception
     */
    public String getDivisaText() throws Exception;

    /**
     * Ritorna la Stringa rappresentazione della divisa in formato HTML.
     * @return stringa divisa per HTML.
     * @throws Exception
     */
    public String getDivisaHtml() throws Exception;
}

