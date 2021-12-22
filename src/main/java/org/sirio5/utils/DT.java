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
package org.sirio5.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.DateTime;
import org.commonlib5.utils.Pair;
import org.sirio5.services.formatter.DataFormatter;
import org.sirio5.services.localization.INT;

/**
 * Utility per la manipolazione di data e ora.
 *
 * @author Nicola De Nisco
 */
public class DT extends DateTime
{
  public static final DataFormatter df = (DataFormatter) TurbineServices.getInstance().
     getService(DataFormatter.SERVICE_NAME);

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa.
   *
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   */
  public static Date parseData(String s)
  {
    return parseData(s, null);
  }

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa.
   *
   * @param s stringa da interpretare
   * @param defVal valore di default
   * @return oggetto Date relativo
   */
  public static Date parseData(String s, Date defVal)
  {
    try
    {
      return df.parseData(s);
    }
    catch(Exception e)
    {
      return defVal;
    }
  }

  public static Date parseDataObj(Object toParse, Date defVal)
  {
    if(toParse == null)
      return defVal;

    if(toParse instanceof Date)
      return (Date) toParse;

    return parseData(toParse.toString(), defVal);
  }

  /**
   * Ritorna vero se la stringa e' correttamente
   * formattata per questo formattatore
   *
   * @param s stringa da analizzare
   * @return vero se ha un formato corretto
   */
  public static boolean isValidData(String s)
  {
    return df.isValidData(s);
  }

  /**
   * Formatta la sola ora senza i secondi.
   * Nel formato italiano hh:mm.
   *
   * @param d data e ora da formattare
   * @return stringa data formattata
   */
  public static String formatTime(Date d)
  {
    try
    {
      return df.formatTime(d);
    }
    catch(Exception e)
    {
      return null;
    }
  }

  /**
   * Formatta la sola ora completa di secondi.
   * Nel formato italiano hh:mm:ss.
   *
   * @param d data e ora da formattare
   * @return stringa data formattata
   */
  public static String formatTimeFull(Date d)
  {
    try
    {
      return df.formatTimeFull(d);
    }
    catch(Exception e)
    {
      return null;
    }
  }

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa hh:mm:ss.
   *
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   */
  public static Date parseDataFull(String s)
  {
    return parseDataFull(s, null);
  }

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia gg/mm/aaaa hh:mm:ss.
   *
   * @param s stringa da interpretare
   * @param defVal valore di default
   * @return oggetto Date relativo
   */
  public static Date parseDataFull(String s, Date defVal)
  {
    try
    {
      return df.parseDataFull(s);
    }
    catch(Exception e)
    {
      return defVal;
    }
  }

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia hh:mm.
   *
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   */
  public static Date parseTime(String s)
  {
    try
    {
      return df.parseTime(s);
    }
    catch(Exception e)
    {
      return null;
    }
  }

  /**
   * Effettua un parsing di una stringa
   * nel formato corretto del formattatore.
   * Per l'italia hh:mm:ss.
   *
   * @param s stringa da interpretare
   * @return oggetto Date relativo
   */
  public static Date parseTimeFull(String s)
  {
    try
    {
      return df.parseTimeFull(s);
    }
    catch(Exception e)
    {
      return null;
    }
  }

  public static String formatData(Date d)
  {
    try
    {
      return df.formatData(d);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  public static String formatData(Date d, String defVal)
  {
    try
    {
      return SU.okStr(df.formatData(d), defVal);
    }
    catch(Exception ex)
    {
      return defVal;
    }
  }

  public static String formatDataFull(Date d)
  {
    try
    {
      return df.formatDataFull(d);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  public static String formatDataFull(Date d, String defVal)
  {
    try
    {
      return SU.okStr(df.formatDataFull(d), defVal);
    }
    catch(Exception ex)
    {
      return defVal;
    }
  }

  public static String formatTimeLocale(Date d)
  {
    try
    {
      return df.formatTime(d);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  public static String formatTimeLocaleFull(Date d)
  {
    try
    {
      return df.formatTimeFull(d);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  public static Date parseLocale(String s)
  {
    try
    {
      return df.parseData(s);
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  public static String formatIso(Date d)
     throws Exception
  {
    return ISOformat.format(d);
  }

  public static Date parseIso(String s)
     throws Exception
  {
    return ISOformat.parse(s);
  }

  public static String formatIsoFull(Date d)
     throws Exception
  {
    return ISOformatFull.format(d);
  }

  public static Date parseIsoFull(String s)
     throws Exception
  {
    return ISOformatFull.parse(s);
  }

  public static String formatDateDicom(Date data)
  {
    return dfData.format(data);
  }

  public static Date parseDateDicom(String sData)
  {
    Date dData = null;

    try
    {
      dData = dfData.parse(sData);
    }
    catch(Exception ex)
    {
    }

    return dData;
  }

  public static String formatTimeDicom(Date data)
  {
    return dfOra.format(data);
  }

  public static Date parseTimeDicom(String sOra)
  {
    Date dOra = null;

    // prima il formato hhmmss
    try
    {
      dOra = dfOra.parse(sOra);
    }
    catch(Exception ex)
    {
    }

    // quindi riprova con hhmm
    if(dOra == null)
      try
    {
      dOra = dfOrans.parse(sOra);
    }
    catch(Exception ex)
    {
    }

    return dOra;
  }

  /**
   * Ritorna vero se la data test è compresa nell'intervallo
   * fra inizio e fine. Il test viene effettuato con troncamento
   * al giorno solare, ignorando l'ora, con precisione del millisecondo.
   * Gli estremi sono compresi nell'intervallo.
   *
   * @param inizio data iniziale
   * @param fine data finale
   * @param test data da provare
   * @return vero se test è nell'intervallo
   */
  public static boolean isBeetwenDaysOnly(Date inizio, Date fine, Date test)
  {
    return isBeetwen(inizioGiorno(inizio), fineGiorno(fine), test);
  }

  public static java.sql.Date convertToSql(java.util.Date d)
  {
    if(d != null && !(d instanceof java.sql.Date))
      return new java.sql.Date(d.getTime());

    return (java.sql.Date) d;
  }

  /**
   * Converte da Calendar a 0-6.
   * Converte il giorno della settimana di Calendar
   * nell'equivalente indice 0-6 utile come indice
   * di nomiBreviSettimana() e nomiLunghiSettimana().
   * @param giorno indice di Calendar
   * @return indice 0-6
   */
  public static int traslaIndiceGiorni(int giorno)
  {
    switch(giorno)
    {
      case Calendar.SUNDAY:
        return 6;
      case Calendar.MONDAY:
        return 0;
      case Calendar.TUESDAY:
        return 1;
      case Calendar.WEDNESDAY:
        return 2;
      case Calendar.THURSDAY:
        return 3;
      case Calendar.FRIDAY:
        return 4;
      case Calendar.SATURDAY:
        return 5;
    }
    return 0;
  }

  /**
   * Ritorna il primo giorno della settimana.
   * Come il suo equivalente di Calendar ritorna
   * il primo giorno della settimana per la locale
   * corrente; di solito o Calendar.MONDAY o Calendar.SUNDAY.
   * @return costante del primo giorno settimana
   */
  public static int getFirstDayOfWeek()
  {
    return df.getFirstDayOfWeek();
  }

  public static LocalDate convertToLocalDateViaMilisecond(Date dateToConvert)
  {
    return Instant.ofEpochMilli(dateToConvert.getTime())
       .atZone(ZoneId.systemDefault())
       .toLocalDate();
  }

  public static LocalDateTime convertToLocalDateTimeViaMilisecond(Date dateToConvert)
  {
    return Instant.ofEpochMilli(dateToConvert.getTime())
       .atZone(ZoneId.systemDefault())
       .toLocalDateTime();
  }

  public static Date convertToDateViaInstant(LocalDate dateToConvert)
  {
    return java.util.Date.from(dateToConvert.atStartOfDay()
       .atZone(ZoneId.systemDefault())
       .toInstant());
  }

  public static Date convertToDateViaInstant(LocalDateTime dateToConvert)
  {
    return java.util.Date
       .from(dateToConvert.atZone(ZoneId.systemDefault())
          .toInstant());
  }

  /**
   * Ritorna l'eta in formato leggibile.
   * @param dtNascita data di nascita
   * @param sanni stringa da restituire per anni
   * @param smesi stringa da restituire per mesi
   * @param sgiorni stringa da restituire per giorni
   * @return eta con descrizione
   */
  public static String getEtaDescrizione(Date dtNascita, String sanni, String smesi, String sgiorni)
  {
    int anni, mesi, giorni;
    Period timeElapsed = Period.between(convertToLocalDateViaMilisecond(dtNascita), LocalDate.now());

    if((anni = timeElapsed.getYears()) > 2)
      return Integer.toString(anni) + sanni;

    if((mesi = (timeElapsed.getMonths() + (anni * 12))) > 3)
      return Integer.toString(mesi) + smesi;

    if((giorni = (timeElapsed.getDays() + (mesi * 30))) < 0)
      giorni = 365 + giorni;

    return Integer.toString(giorni) + sgiorni;
  }

  public static String getEtaDescrizione(Date dtNascita)
  {
    return getEtaDescrizione(dtNascita, " " + INT.I("anni"), " " + INT.I("mesi"), " " + INT.I("giorni"));
  }

  public static int getEtaAnni(Date dtNascita)
  {
    Calendar calNow = new GregorianCalendar();
    Calendar calNascita = new GregorianCalendar();
    calNascita.setTime(dtNascita);

    return calNow.get(Calendar.YEAR) - calNascita.get(Calendar.YEAR);
  }

  /**
   * Ritorna una lista dei giorni della settima.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return lista come deve essere visualizzata
   */
  public static List<Pair<Integer, String>> listaGiorniSettimana()
  {
    return df.listaGiorniSettimana();
  }

  /**
   * Ritorna una lista dei giorni della settima.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return lista come deve essere visualizzata
   */
  public static List<Pair<Integer, String>> listaGiorniBreviSettimana()
  {
    return df.listaGiorniBreviSettimana();
  }

  /**
   * Ritorna una lista dei mesi dell'anno.
   * La lista è cordinata secondo la locale voluta.
   * Gli interi sono le costanti Calendar.JANUARY, Calendar.FEBRUARY, ecc.
   * @return lista come deve essere visualizzata
   */
  public static List<Pair<Integer, String>> listaMesiAnno()
  {
    return df.listaMesiAnno();
  }

  /**
   * Restiutisce il nome di un giorno della settimana.
   * @param giorno una delle costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return nome o stringa vuota
   */
  public static String getNomeGiornoSettimana(int giorno)
  {
    List<Pair<Integer, String>> lgs = DT.listaGiorniSettimana();
    for(Pair<Integer, String> lg : lgs)
    {
      if(giorno == lg.first)
        return lg.second;
    }
    return "";
  }

  /**
   * Restiutisce il nome di un giorno della settimana.
   * @param giorno una delle costanti Calendar.MONDAY, Calendar.TUESDAY, ecc.
   * @return nome o stringa vuota
   */
  public static String getNomeGiornoBreveSettimana(int giorno)
  {
    List<Pair<Integer, String>> lgs = DT.listaGiorniBreviSettimana();
    for(Pair<Integer, String> lg : lgs)
    {
      if(giorno == lg.first)
        return lg.second;
    }
    return "";
  }

  /**
   * Restiutisce il nome di un mese dell'anno.
   * @param mese una delle costanti Calendar.JANUARY, Calendar.FEBRUARY, ecc.
   * @return nome o stringa vuota
   */
  public static String getNomeMeseAnno(int mese)
  {
    List<Pair<Integer, String>> lgs = DT.listaMesiAnno();
    for(Pair<Integer, String> lg : lgs)
    {
      if(mese == lg.first)
        return lg.second;
    }
    return "";
  }

  public static final SimpleDateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  public static String formatISO8601(Date date)
  {
    dfISO8601.setTimeZone(TimeZone.getTimeZone("CET"));
    return dfISO8601.format(date);
  }

  public static String formatISO8601(Date date, String defVal)
  {
    try
    {
      return formatISO8601(date);
    }
    catch(Throwable t)
    {
      return defVal;
    }
  }

  public static Date parseISO8601(String val)
     throws ParseException
  {
    return dfISO8601.parse(val);
  }

  public static Date parseISO8601(String val, Date defVal)
  {
    try
    {
      return dfISO8601.parse(val);
    }
    catch(Throwable t)
    {
      return defVal;
    }
  }

  public static Date dataSpiazzataMillisecondi(Date origin, long offset)
  {
    if(origin == null)
      origin = new Date();

    return new Date(origin.getTime() + offset);
  }
}
