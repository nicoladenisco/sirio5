/*
 *  SirioMacroResolver.java
 *
 *  Creato il 9 Luglio 2017
 *
 *  Copyright (C) 2017 RAD-IMAGE s.r.l.
 *
 *  RAD-IMAGE s.r.l.
 *  Via San Giovanni, 1 - Contrada Belvedere
 *  San Nicola Manfredi (BN)
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

import java.util.*;
import org.apache.turbine.om.security.User;
import org.commonlib5.utils.MacroResolver;

/**
 * Risolutore generico di macro.
 * In una stringa possono apparire costrutti del tipo ${macro}
 * dove 'macro' viene risolto attraverso una map di parametri.<br>
 * Una serie di macro predefinite vengono sempre risolte:
 * <ul>
 * <li>YEAR_FIRST - primo giorno dell'anno corrente</li>
 * <li>YEAR_LAST - ultimo giorno dell'anno corrente</li>
 * <li>PREV_YEAR_FIRST - primo giorno dell'anno precedente</li>
 * <li>PREV_YEAR_LAST - ultimo giorno dell'anno precedente</li>
 * <li>MONTH_FIRST - primo giorno del mese corrente</li>
 * <li>MONTH_LAST - ultimo giorno del mese corrente</li>
 * <li>GFIRST - data del primo giorno dell'anno</li>
 * <li>GLAST - data dell'ultimo giorno dell'anno</li>
 * <li>TODAY - data odierna (solo data)</li>
 * <li>TODAYM30 - data odierna meno 30 giorni (solo data)</li>
 * <li>TODAYM60 - data odierna meno 60 giorni (solo data)</li>
 * <li>YESTERDAY - data giorno precedente (solo data)</li>
 * <li>TOTIME - data odierna (data e ora)</li>
 * <li>TOTIMEM30 - data odierna meno 30 giorni (data e ora)</li>
 * <li>TOTIMEM60 - data odierna meno 60 giorni (data e ora)</li>
 * <li>YESTERDAY_TIME - data giorno precedente (data e ora)</li>
 * <li>IYEAR - intervallo primo e ultimo giorno dell'anno</li>
 * <li>IMOUNTH - intervallo primo e ultimo giorno del mese</li>
 *
 * <li>ISO_YEAR_FIRST - primo giorno dell'anno corrente in formato ISO</li>
 * <li>ISO_YEAR_LAST - ultimo giorno dell'anno corrente in formato ISO</li>
 * <li>ISO_PREV_YEAR_FIRST - primo giorno dell'anno precedente in formato ISO</li>
 * <li>ISO_PREV_YEAR_LAST - ultimo giorno dell'anno precedente in formato ISO</li>
 * <li>ISO_MONTH_FIRST - primo giorno del mese corrente in formato ISO</li>
 * <li>ISO_MONTH_LAST - ultimo giorno del mese corrente in formato ISO</li>
 * <li>ISO_GFIRST - data del primo giorno dell'anno in formato ISO</li>
 * <li>ISO_GLAST - data dell'ultimo giorno dell'anno in formato ISO</li>
 * <li>ISO_TODAY - data odierna (solo data) in formato ISO</li>
 * <li>ISO_TODAYM30 - data odierna meno 30 giorni (solo data) in formato ISO</li>
 * <li>ISO_TODAYM60 - data odierna meno 60 giorni (solo data) in formato ISO</li>
 * <li>ISO_TODAY_OFFSET(numGiorni) - data odierna con spiazzamento (solo data) in formato ISO</li>
 * <li>ISO_YESTERDAY - data giorno precedente (solo data) in formato ISO</li>
 * <li>ISO_TOTIME - data odierna (data e ora) in formato ISO</li>
 * <li>ISO_TOTIMEM30 - data odierna meno 30 giorni (data e ora) in formato ISO</li>
 * <li>ISO_TOTIMEM60 - data odierna meno 60 giorni (data e ora) in formato ISO</li>
 * <li>ISO_TOTIME_OFFSET(numGiorni) - data odierna con spiazzamento (data e ora) in formato ISO</li>
 * <li>ISO_YESTERDAY_TIME - data giorno precedente (data e ora) in formato ISO</li>
 * <li>ISO_IYEAR - intervallo primo e ultimo giorno dell'anno in formato ISO</li>
 * <li>ISO_IMOUNTH - intervallo primo e ultimo giorno del mese in formato ISO</li>
 *
 * <li>USER - nome e cognome dell'utente loggato</li>
 * <li>USER_FIRST - nome dell'utente loggato</li>
 * <li>USER_LAST - cognome dell'utente loggato</li>
 * <li>AZIENDAID - ID dell'azienda (da setup)</li>
 * <li>AZIENDA - nome dell'azienda (da setup)</li>
 * <li>ESERCIZIO - anno corrente in quattro cifre (2017)</li>
 * </ul>
 * @author Nicola De Nisco
 */
public class SirioMacroResolver extends MacroResolver
{
  protected User u = null;

  public SirioMacroResolver()
  {
    initFunctions();
  }

  public SirioMacroResolver(User u)
  {
    this.u = u;
    initFunctions();
  }

  protected void initFunctions()
  {
    mapFunction.put("YEAR_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      return formatData(cal.getTime());
    });

    mapFunction.put("YEAR_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 365);
      return formatData(cal.getTime());
    });

    mapFunction.put("PREV_YEAR_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.add(Calendar.YEAR, -1);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      return formatData(cal.getTime());
    });

    mapFunction.put("PREV_YEAR_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.add(Calendar.YEAR, -1);
      cal.set(Calendar.DAY_OF_YEAR, 365);
      return formatData(cal.getTime());
    });

    mapFunction.put("MONTH_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      return formatData(cal.getTime());
    });

    mapFunction.put("MONTH_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      cal.set(Calendar.DAY_OF_MONTH, lastDay);
      return formatData(cal.getTime());
    });

    mapFunction.put("GFIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
      return formatData(cal.getTime());
    });

    mapFunction.put("GLAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 11, 31, 23, 59, 59);
      return formatData(cal.getTime());
    });

    mapFunction.put("IYEAR", (seg) ->
    {
      String rv = "";
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
      rv += formatData(cal.getTime());
      cal.set(cal.get(Calendar.YEAR), 11, 31, 23, 59, 59);
      rv += "|" + formatData(cal.getTime());
      return rv;
    });

    mapFunction.put("IMONTH", (seg) ->
    {
      String rv = "";
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
      rv += formatData(cal.getTime());
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
      rv += "|" + formatData(cal.getTime());
      return rv;
    });

    mapFunction.put("TODAY", (seg) -> formatData(today));
    mapFunction.put("TOTIME", (seg) -> formatDataFull(today));

    mapFunction.put("YESTERDAY", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -1);
      return formatData(cal.getTime());
    });
    mapFunction.put("YESTERDAY_TIME", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -1);
      return formatDataFull(cal.getTime());
    });

    mapFunction.put("TODAYM30", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -30);
      return formatData(cal.getTime());
    });

    mapFunction.put("TODAYM60", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -60);
      return formatData(cal.getTime());
    });

    mapFunction.put("TOTIMEM30", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -30);
      return formatDataFull(cal.getTime());
    });

    mapFunction.put("TOTIMEM60", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -60);
      return formatDataFull(cal.getTime());
    });

    mapFunction.put("ISO_YEAR_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_YEAR_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 365);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_PREV_YEAR_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.add(Calendar.YEAR, -1);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_PREV_YEAR_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.add(Calendar.YEAR, -1);
      cal.set(Calendar.DAY_OF_YEAR, 365);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_MONTH_FIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_MONTH_LAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(today);
      int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      cal.set(Calendar.DAY_OF_MONTH, lastDay);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_GFIRST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_GLAST", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 11, 31, 23, 59, 59);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_IYEAR", (seg) ->
    {
      String rv = "";
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
      rv += formatIso(cal.getTime());
      cal.set(cal.get(Calendar.YEAR), 11, 31, 23, 59, 59);
      rv += "|" + formatIso(cal.getTime());
      return rv;
    });

    mapFunction.put("ISO_IMONTH", (seg) ->
    {
      String rv = "";
      GregorianCalendar cal = new GregorianCalendar();
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
      rv += formatIso(cal.getTime());
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
      rv += "|" + formatIso(cal.getTime());
      return rv;
    });

    mapFunction.put("ISO_TODAY", (seg) -> formatIso(today));
    mapFunction.put("ISO_TOTIME", (seg) -> formatIsoFull(today));

    mapFunction.put("ISO_YESTERDAY", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -1);
      return formatIso(cal.getTime());
    });
    mapFunction.put("ISO_YESTERDAY_TIME", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -1);
      return formatIsoFull(cal.getTime());
    });

    mapFunction.put("ISO_TODAYM30", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -30);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_TODAYM60", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -60);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_TODAY_OFFSET", (seg) ->
    {
      int giorni = SU.parseInt(seg);
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, giorni);
      return formatIso(cal.getTime());
    });

    mapFunction.put("ISO_TOTIMEM30", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -30);
      return formatIsoFull(cal.getTime());
    });

    mapFunction.put("ISO_TOTIMEM60", (seg) ->
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, -60);
      return formatIsoFull(cal.getTime());
    });

    mapFunction.put("ISO_TOTIME_OFFSET", (seg) ->
    {
      int giorni = SU.parseInt(seg);
      GregorianCalendar cal = new GregorianCalendar();
      cal.add(Calendar.DAY_OF_YEAR, giorni);
      return formatIsoFull(cal.getTime());
    });

    mapFunction.put("USER", (seg) -> (u == null) ? "" : u.getFirstName() + " " + u.getLastName());
    mapFunction.put("USER_FIRST", (seg) -> (u == null) ? "" : u.getFirstName());
    mapFunction.put("USER_LAST", (seg) -> (u == null) ? "" : u.getLastName());
    mapFunction.put("AZIENDAID", (seg) -> TR.getString("azienda.id", ""));
    mapFunction.put("AZIENDA", (seg) -> TR.getString("azienda.nome", ""));
    mapFunction.put("ESERCIZIO", (seg) -> Integer.toString(1900 + today.getYear()));
  }

  public String formatData(Date d)
  {
    return DT.formatData(d);
  }

  public String formatDataFull(Date d)
  {
    return DT.formatDataFull(d);
  }

  public String formatIso(Date d)
     throws Exception
  {
    return DT.formatIso(d);
  }

  public String formatIsoFull(Date d)
     throws Exception
  {
    return DT.formatIsoFull(d);
  }
}
