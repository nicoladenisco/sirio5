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

import java.util.*;
import java.text.*;
import org.apache.commons.configuration2.Configuration;
import org.commonlib5.gui.validator.ItalianParser;
import org.commonlib5.utils.Pair;
import org.sirio5.services.AbstractCoreBaseService;

public class ItalianDataFormatter extends AbstractCoreBaseService
   implements DataFormatter
{
  protected ItalianParser itParser = new ItalianParser();
  protected boolean timeDefaultToCurrentTime = false;
  protected boolean dateDefaultToCurrentDate = false;
  protected List<Pair<Integer, String>> lsGiorniSettimana = new ArrayList<>();
  protected List<Pair<Integer, String>> lsGiorniBreviSettimana = new ArrayList<>();
  protected List<Pair<Integer, String>> lsMesiAnno = new ArrayList<>();
  public static final DateFormat dfITshort = new SimpleDateFormat("dd/MM/yyyy");
  public static final DateFormat dfoITshort = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  public static final DateFormat doITshort = new SimpleDateFormat("HH:mm:ss");
  public static final DateFormat dosITshort = new SimpleDateFormat("HH:mm");
  public static final DateFormat dfYear = new SimpleDateFormat("yyyy");

  @Override
  public void coreInit()
     throws Exception
  {
    Configuration cfg = getConfiguration();
    timeDefaultToCurrentTime = cfg.getBoolean("timeDefaultToCurrentTime", true);
    dateDefaultToCurrentDate = cfg.getBoolean("dateDefaultToCurrentDate", true);

    lsGiorniSettimana.add(new Pair<>(Calendar.MONDAY, "Lunedì"));
    lsGiorniSettimana.add(new Pair<>(Calendar.TUESDAY, "Martedì"));
    lsGiorniSettimana.add(new Pair<>(Calendar.WEDNESDAY, "Mercoledì"));
    lsGiorniSettimana.add(new Pair<>(Calendar.THURSDAY, "Giovedì"));
    lsGiorniSettimana.add(new Pair<>(Calendar.FRIDAY, "Venerdì"));
    lsGiorniSettimana.add(new Pair<>(Calendar.SATURDAY, "Sabato"));
    lsGiorniSettimana.add(new Pair<>(Calendar.SUNDAY, "Domenica"));

    lsGiorniBreviSettimana.add(new Pair<>(Calendar.MONDAY, "Lun"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.TUESDAY, "Mar"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.WEDNESDAY, "Mer"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.THURSDAY, "Gio"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.FRIDAY, "Ven"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.SATURDAY, "Sab"));
    lsGiorniBreviSettimana.add(new Pair<>(Calendar.SUNDAY, "Dom"));

    lsMesiAnno.add(new Pair<>(Calendar.JANUARY, "Gennaio"));
    lsMesiAnno.add(new Pair<>(Calendar.FEBRUARY, "Febbraio"));
    lsMesiAnno.add(new Pair<>(Calendar.MARCH, "Marzo"));
    lsMesiAnno.add(new Pair<>(Calendar.APRIL, "Aprile"));
    lsMesiAnno.add(new Pair<>(Calendar.MAY, "Maggio"));
    lsMesiAnno.add(new Pair<>(Calendar.JUNE, "Giugno"));
    lsMesiAnno.add(new Pair<>(Calendar.JULY, "Luglio"));
    lsMesiAnno.add(new Pair<>(Calendar.AUGUST, "Agosto"));
    lsMesiAnno.add(new Pair<>(Calendar.SEPTEMBER, "Settembre"));
    lsMesiAnno.add(new Pair<>(Calendar.OCTOBER, "Ottobre"));
    lsMesiAnno.add(new Pair<>(Calendar.NOVEMBER, "Novembre"));
    lsMesiAnno.add(new Pair<>(Calendar.DECEMBER, "Dicembre"));
  }

  public String formatYear(Date d)
     throws Exception
  {
    return d == null ? null : dfYear.format(d);
  }

  @Override
  public String formatData(Date d)
     throws Exception
  {
    return d == null ? null : dfITshort.format(d);
  }

  @Override
  public String formatDataFull(Date d)
     throws Exception
  {
    return d == null ? null : dfoITshort.format(d);
  }

  @Override
  public String formatTime(Date d)
     throws Exception
  {
    return d == null ? null : dosITshort.format(d);
  }

  @Override
  public String formatTimeFull(Date d)
     throws Exception
  {
    return d == null ? null : doITshort.format(d);
  }

  @Override
  public Date parseTimeFull(String s)
     throws Exception
  {
    return parseTime(s);
  }

  @Override
  public boolean isValidData(String s)
  {
    try
    {
      Date dummy = parseData(s);
      return true;
    }
    catch(Exception e)
    {
    }
    return false;
  }

  @Override
  public Date parseData(String s)
     throws Exception
  {
    return parseDataFull(s);
  }

  @Override
  public Date parseDataFull(String s)
     throws Exception
  {
    Date d = itParser.parseDate(s, null);
    if(d == null)
      throw new ParseException("Non riesco ad interpretare la data/ora.", 0);

    return d;
  }

  @Override
  public Date parseTime(String s)
     throws Exception
  {
    Date d = itParser.parseTime(s, null);
    if(d == null)
      throw new ParseException("Non riesco ad interpretare l'ora.", 0);

    return d;
  }

  @Override
  public int getFirstDayOfWeek()
  {
    return Calendar.MONDAY;
  }

  @Override
  public List<Pair<Integer, String>> listaGiorniSettimana()
  {
    return lsGiorniSettimana;
  }

  @Override
  public List<Pair<Integer, String>> listaGiorniBreviSettimana()
  {
    return lsGiorniBreviSettimana;
  }

  @Override
  public List<Pair<Integer, String>> listaMesiAnno()
  {
    return lsMesiAnno;
  }
}
