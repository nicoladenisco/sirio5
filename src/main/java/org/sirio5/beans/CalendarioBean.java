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
package org.sirio5.beans;

import java.util.*;
import org.apache.fulcrum.localization.LocalizationService;
import org.commonlib5.utils.Pair;
import org.sirio5.rigel.RigelHtmlI18n;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.DT;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Bean per il calendario.
 *
 * @author Nicola De Nisco
 */
final public class CalendarioBean extends CoreBaseBean
{
  public static final String SINGLE = "single";
  public static final String INTERVAL = "interval";
  //
  private int oggig = 0;
  private int oggim = 0;
  private int oggiy = 0;
  //
  private int day = 0;
  private int month = 0;
  private int year = 0;
  //
  private String func = "restart";
  private String fint = "restart";
  private String mode = "single";
  private String monthName;
  private StringBuilder htmlOpMesi, htmlOpAnni, html2;
  private Locale loc = null;
  private int bootstrapVersion = 0;
  protected RigelHtmlI18n i18n = null;
  private LocalizationService ls;

  @Override
  public void init(CoreRunData data)
     throws Exception
  {
    super.init(data);
    i18n = new RigelHtmlI18n(data);

    if(ls == null)
      ls = getService(LocalizationService.SERVICE_NAME);

    loc = ls.getLocale(data.getRequest());
    GregorianCalendar cal = new GregorianCalendar(loc);
    cal.setTime(today);
    oggig = day = cal.get(Calendar.DAY_OF_MONTH);
    oggim = month = cal.get(Calendar.MONTH);
    oggiy = year = cal.get(Calendar.YEAR);

    if(year < 1000)
      year += 1900;
    if(oggiy < 1000)
      oggiy += 1900;

    data.getParameters().setProperties(this);
    bootstrapVersion = TR.getInt("bootstrapVersion", bootstrapVersion);
  }

  @Override
  public void refreshSession(CoreRunData data)
     throws Exception
  {
    super.refreshSession(data);
    data.getParameters().setProperties(this);
  }

  public int getBootstrapVersion()
  {
    return bootstrapVersion;
  }

  public void setBootstrapVersion(int bootstrapVersion)
  {
    this.bootstrapVersion = bootstrapVersion;
  }

  /**
   * Costruisce il calendario per la data corrente.
   *
   * @throws Exception
   */
  public void buildCalendar()
     throws Exception
  {
    buildCalendar(month, year);
  }

  /**
   * Costruisce il calendario per il mese specificato.
   * @param Month mese una delle costanti Calendar.JANUARY, Calendar.FEBRUARY, ...
   * @param Year anno
   * @throws Exception
   */
  public void buildCalendar(int Month, int Year)
     throws Exception
  {
    GregorianCalendar cal = new GregorianCalendar(loc);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.MONTH, Month);
    cal.set(Calendar.YEAR, Year);
    Date firstDay = cal.getTime();
    Date lastDay = cal.getTime();

    int startDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
    int endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

    List<Pair<Integer, String>> lsMesi = DT.listaMesiAnno();
    List<Pair<Integer, String>> lsGset = DT.listaGiorniSettimana();

    // recupera il primo giorno settimana dalla lista giorni (non dalla locale)
    int primoGiornoSettimana = lsGset.get(0).first;

    // torna indietro fino alla prima domenica
    while(cal.get(Calendar.DAY_OF_WEEK) != primoGiornoSettimana)
    {
      cal.add(Calendar.DAY_OF_YEAR, -1);
      cal.getTime();
    }
    firstDay = cal.getTime();

    // imposta calendario sull'ultimo giorno del mese
    cal.set(Calendar.DAY_OF_MONTH, endDay);
    cal.set(Calendar.MONTH, Month);
    cal.set(Calendar.YEAR, Year);
    cal.getTime();
    cal.add(Calendar.DAY_OF_YEAR, 1);

    // va avanti fino al primo sabato
    while(cal.get(Calendar.DAY_OF_WEEK) != primoGiornoSettimana)
    {
      cal.add(Calendar.DAY_OF_YEAR, 1);
      cal.getTime();
    }
    lastDay = cal.getTime();

    //////////////////////////////////////////////////////////////////////
    monthName = i18n.msg(DT.getNomeMeseAnno(Month));

    htmlOpMesi = new StringBuilder(1024);
    for(Pair<Integer, String> m : lsMesi)
    {
      htmlOpMesi.append(SU.generaOptionCombo(m.first, i18n.msg(m.second), Month));
    }

    htmlOpAnni = new StringBuilder(1024);
    for(int anno = (Year - 10); anno < (Year + 10); anno++)
    {
      htmlOpAnni.append(SU.generaOptionCombo(anno, Integer.toString(anno), Year));
    }

    //////////////////////////////////////////////////////////////////////
    html2 = new StringBuilder(1024);
    html2.append("<TR>\r\n");

    for(Pair<Integer, String> gs : lsGset)
      html2.append("<TD WIDTH=50 ALIGN=CENTER VALIGN=MIDDLE CLASS=\"cal_week\">")
         .append(i18n.msg(gs.second)).append("</TD>\r\n");

    html2.append("</TR><TR ALIGN=CENTER VALIGN=MIDDLE>\r\n");

    int column = 0;
    cal.setTime(firstDay);
    for(Date d = firstDay; !(d = cal.getTime()).equals(lastDay); cal.add(Calendar.DAY_OF_YEAR, 1))
    {
      int gg = cal.get(Calendar.DAY_OF_MONTH);
      int mm = cal.get(Calendar.MONTH);
      int yy = cal.get(Calendar.YEAR);
      String sDate = DT.formatData(d);

      boolean festa = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
         || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;

      if(mm == Month && yy == Year)
      {
        String style = "cal_mont";
        if(festa)
          style = "cal_fest";

        if(gg == oggig && mm == oggim && yy == oggiy)
          style = "cal_toda";

        switch(bootstrapVersion)
        {
          case 5: // bootstrap 5
          case 4: // bootstrap 4
          case 3: // bootstrap 3
          {
            html2.append("<TD WIDTH=50 HEIGHT=30 CLASS=\"").append(style).
               append("\" onclick=\"").append(func).append("('").append(sDate).
               append("'); chiudiCalendario();\">").append(gg).append("</TD>\r\n"); // NOI18N
            break;
          }
          case 0: // nessuno
          default:
          {
            html2.append("<TD WIDTH=50 HEIGHT=30 CLASS=\"").append(style).
               append("\" onclick=\"").append(func).append("('").append(sDate).
               append("'); chiudiCalendario();\">").append(gg).append("</TD>\r\n"); // NOI18N
            break;
          }
        }

        if(column == 6)
        {
          html2.append("</TR><TR ALIGN=CENTER VALIGN=MIDDLE>\r\n");
          column = -1;
        }
      }
      else
      {
        html2.append("<TD WIDTH=50 HEIGHT=30 CLASS=\"cal_nomn\">").append(gg).append("</TD>\r\n");
      }
      column++;
    }

    html2.append("</TR>\r\n");
  }

  public String computeIntervals()
     throws Exception
  {
    StringBuilder rv = new StringBuilder(1024);
    Calendar cal = new GregorianCalendar(loc);

    if(true)
    {
      String spg = DT.formatData(today);
      String sug = DT.formatData(today);
      rv.append(fmtOption(spg, sug, i18n.msg("Oggi")));
    }

    if(true)
    {
      cal.setTime(today);
      cal.add(Calendar.DAY_OF_YEAR, -1);
      String spg = DT.formatData(cal.getTime());
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Ieri")));
    }

    if(true)
    {
      cal.setTime(today);
      cal.add(Calendar.DAY_OF_YEAR, -2);
      String spg = DT.formatData(cal.getTime());
      String sug = DT.formatData(today);
      rv.append(fmtOption(spg, sug, i18n.msg("Ultimi due giorni")));
    }

    if(true)
    {
      cal.setTime(today);
      cal.add(Calendar.WEEK_OF_YEAR, -1);
      String spg = DT.formatData(cal.getTime());
      String sug = DT.formatData(today);
      rv.append(fmtOption(spg, sug, i18n.msg("Ultima settimana")));
    }

    if(true)
    {
      cal.setTime(today);
      int pg = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
      int ug = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      cal.set(Calendar.DAY_OF_MONTH, pg);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.DAY_OF_MONTH, ug);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Mese corrente")));
    }

    if(oggim > Calendar.JANUARY)
    {
      cal.setTime(today);
      for(int mese = oggim - 1; mese >= Calendar.JANUARY; mese--)
      {
        cal.set(Calendar.MONTH, mese);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String spg = DT.formatData(cal.getTime());
        int ug = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, ug);
        String sug = DT.formatData(cal.getTime());
        rv.append(fmtOption(spg, sug, DT.getNomeMeseAnno(mese)));
      }
    }

    if(true)
    {
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.MARCH);
      cal.set(Calendar.DAY_OF_MONTH, 31);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Primo trimestre")));
    }

    if(oggim > Calendar.MARCH)
    {
      cal.setTime(today);
      cal.set(Calendar.MONTH, Calendar.APRIL);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.JUNE);
      cal.set(Calendar.DAY_OF_MONTH, 30);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Secondo trimestre")));
    }

    if(oggim > Calendar.JUNE)
    {
      cal.setTime(today);
      cal.set(Calendar.MONTH, Calendar.JULY);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
      cal.set(Calendar.DAY_OF_MONTH, 30);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Terzo trimestre")));
    }

    if(oggim > Calendar.SEPTEMBER)
    {
      cal.setTime(today);
      cal.set(Calendar.MONTH, Calendar.OCTOBER);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH, 31);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Quarto trimestre")));
    }

    if(true)
    {
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.JUNE);
      cal.set(Calendar.DAY_OF_MONTH, 30);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Primo semestre")));
    }

    if(oggim > Calendar.JUNE)
    {
      cal.setTime(today);
      cal.set(Calendar.MONTH, Calendar.JULY);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH, 31);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, i18n.msg("Secondo semestre")));
    }

    if(true)
    {
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      String spg = DT.formatData(cal.getTime());
      String sug = DT.formatData(today);
      rv.append(fmtOption(spg, sug, i18n.msg("Dall'inizio dell'anno")));
    }

    for(int i = 1; i <= 5; i++)
    {
      // i cinque anni precedenti
      cal.setTime(today);
      cal.set(Calendar.DAY_OF_YEAR, 1);
      cal.set(Calendar.YEAR, oggiy - i);
      String spg = DT.formatData(cal.getTime());
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH, 31);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(spg, sug, Integer.toString(oggiy - i)));
    }

    return rv.toString();
  }

  /**
   * Costruisce il contenuto combo box per le età.
   * @return options del combo box
   * @throws Exception
   */
  public String computeYears()
     throws Exception
  {
    StringBuilder rv = new StringBuilder(1024);
    Calendar cal = new GregorianCalendar(loc);

    for(int olds = 0; olds < 100; olds += 10)
    {
      cal.setTime(today);
      cal.add(Calendar.YEAR, -olds);
      String spg = DT.formatData(cal.getTime());
      cal.add(Calendar.YEAR, -10);
      String sug = DT.formatData(cal.getTime());
      rv.append(fmtOption(sug, spg, String.format(i18n.msg("Da %d a %d anni", olds, olds + 10))));
    }

    return rv.toString();
  }

  protected String fmtOption(String spg, String sug, String descrizione)
  {
    switch(bootstrapVersion)
    {
      case 5: // bootstrap 5
      case 4: // bootstrap 4
      {
        return "<li><a class=\"dropdown-item\" href=\"#\" onclick=\""
           + fint + "('" + spg + "|" + sug + "'); chiudiCalendario();\">" + descrizione + "</a></li>\n"; // NOI18N
      }
      case 3: // bootstrap 3
      {
        return "<li role=\"presentation\"><a role=\"menuitem\" tabindex=\"-1\" href=\"#\" onclick=\""
           + fint + "('" + spg + "|" + sug + "'); chiudiCalendario();\">" + descrizione + "</a></li>\n"; // NOI18N
      }
      case 0: // nessuno
      default:
      {
        return "<option value='" + spg + "|" + sug + "'>" + descrizione + "</option>\r\n";
      }
    }
  }

  public String getHtmlOpAnni()
  {
    return htmlOpAnni == null ? "" : htmlOpAnni.toString();
  }

  public String getHtmlOpMesi()
  {
    return htmlOpMesi == null ? "" : htmlOpMesi.toString();
  }

  public Date getCurrentDate()
  {
    GregorianCalendar cal = new GregorianCalendar(loc);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.YEAR, year);
    return cal.getTime();
  }

  public void setDay(int day)
  {
    this.day = day;
  }

  public int getDay()
  {
    return day;
  }

  public void setMonth(int month)
  {
    this.month = month;
  }

  public int getMonth()
  {
    return month;
  }

  public String getMonthName()
  {
    return monthName;
  }

  public void setYear(int year)
  {
    this.year = year;
  }

  public int getYear()
  {
    return year;
  }

  public void setFunc(String func)
  {
    this.func = func;
  }

  public String getFunc()
  {
    return func;
  }

  public String getHtml2()
  {
    return html2 == null ? "" : html2.toString();
  }

  public int getOggig()
  {
    return oggig;
  }

  public void setOggig(int oggig)
  {
    this.oggig = oggig;
  }

  public int getOggim()
  {
    return oggim;
  }

  public void setOggim(int oggim)
  {
    this.oggim = oggim;
  }

  public int getOggiy()
  {
    return oggiy;
  }

  public void setOggiy(int oggiy)
  {
    this.oggiy = oggiy;
  }

  public String getFint()
  {
    return fint;
  }

  public void setFint(String fint)
  {
    this.fint = fint;
  }

  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  public boolean isInterval()
  {
    return SU.isEquNocase(mode, INTERVAL);
  }
}
