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

import java.util.Date;
import java.util.List;
import org.commonlib5.utils.Pair;
import org.sirio5.services.CoreServiceExtension;

/**
 * Interfaccia per la definizione di servizi di formattazione della data.
 * @author Nicola De Nisco
 */
public interface DataFormatter extends CoreServiceExtension
{
  public static final String SERVICE_NAME = "DataFormatter";

  public static final int DF_DATE = 1;
  public static final int DF_DATE_TIME = 2;
  public static final int DF_TIME = 3;
  public static final int DF_TIME_FULL = 4;

  /**
   * Formatta una data senza ora.
   * Nel formato italiano gg/mm/aaaa.
   * @param d data da formattare
   * @return stringa data formattata
   * @throws java.lang.Exception
   */
  public String formatData(Date d) throws Exception;

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa.
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   * @throws java.lang.Exception
   */
  public Date parseData(String s) throws Exception;

  /**
   * Ritorna vero se la stringa e' correttamente
   * formattata per questo formattatore
   * @param s stringa da analizzare
   * @return vero se ha un formato corretto
   */
  public boolean isValidData(String s);

  /**
   * Formatta una data e ora.
   * Nel formato italiano gg/mm/aaaa hh:mm:ss
   * @param d data e ora da formattare
   * @return stringa data formattata
   * @throws java.lang.Exception
   */
  public String formatDataFull(Date d) throws Exception;

  /**
   * Formatta la sola ora senza i secondi.
   * Nel formato italiano hh:mm
   * @param d data e ora da formattare
   * @return stringa data formattata
   * @throws java.lang.Exception
   */
  public String formatTime(Date d) throws Exception;

  /**
   * Formatta la sola ora completa di secondi.
   * Nel formato italiano hh:mm
   * @param d data e ora da formattare
   * @return stringa data formattata
   * @throws java.lang.Exception
   */
  public String formatTimeFull(Date d) throws Exception;

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa hh:mm:ss.
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   * @throws java.lang.Exception
   */
  public Date parseDataFull(String s) throws Exception;

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia hh:mm.
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   * @throws java.lang.Exception
   */
  public Date parseTime(String s) throws Exception;

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia hh:mm:ss.
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   * @throws java.lang.Exception
   */
  public Date parseTimeFull(String s) throws Exception;

  /**
   * Ritorna il primo giorno della settimana.
   * Come il suo equivalente di Calendar ritorna
   * il primo giorno della settimana per la locale
   * corrente; di solito o Calendar.MONDAY o Calendar.SUNDAY.
   * @return costante del primo giorno settimana
   */
  public int getFirstDayOfWeek();

  /**
   * Ritorna una lista dei giorni della settima.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return lista come deve essere visualizzata
   */
  public List<Pair<Integer, String>> listaGiorniSettimana();

  /**
   * Ritorna una lista dei giorni della settima.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return lista come deve essere visualizzata
   */
  public List<Pair<Integer, String>> listaGiorniBreviSettimana();

  /**
   * Ritorna una lista dei mesi dell'anno.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.JANUARY, Calendar.FEBRUARY, ecc.
   * @return lista come deve essere visualizzata
   */
  public List<Pair<Integer, String>> listaMesiAnno();
}
