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
package org.sirio2.beans.xml;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.turbine.services.Service;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.util.RunData;
import org.commonlib.utils.StringOper;
import org.sirio2.services.modellixml.MDL;
import org.sirio2.services.security.SEC;
import org.sirio2.utils.DT;
import org.sirio2.utils.SU;
import org.sirio2.utils.TR;

/**
 * Classe base dei bean generatori di xml.
 * @author Nicola De Nisco
 */
abstract public class jsxmlBaseBean
{
  protected org.apache.turbine.om.security.User us;
  protected Date today = new Date();

  public static final String DEFAULT_TITOLO = "Utente: %n Data: %df";//NOI18N
  public static final String DEFAULT_PIEDE = "Utente: %n Data: %df";//NOI18N
  public static final String TITOLO_STANDARD = "@standard";

  /** Creates a new instance of jsxmlBaseBean */
  public jsxmlBaseBean()
  {
  }

  protected String formatData(Date dformat)
     throws Exception
  {
    return DT.formatData(dformat);
  }

  public String getCampoData(String nomeCampo, String valore, int size)
     throws Exception
  {
    return MDL.getCampoData(nomeCampo, "fo", valore, size);
  }

  public String getCampoForeign(String nomeCampo, String valore, int size,
     String url, String valForeign, String extraScript)
     throws Exception
  {
    return MDL.getCampoForeign(nomeCampo, valore, size, url, valForeign, extraScript);
  }

  public Service getService(String serviceName)
  {
    return (Service) TurbineServices.getInstance().getService(serviceName);
  }

  ////////////////////////////////////////////////////////////////////////
  public String parseTitolo(String s)
  {
    if(s == null || SU.isEqu(TITOLO_STANDARD, s))
      s = TR.getString("stampe.titolo", DEFAULT_TITOLO);//NOI18N

    return parseMacro(s);
  }

  public String parsePiede(String s)
  {
    if(s == null || SU.isEqu(TITOLO_STANDARD, s))
      s = TR.getString("stampe.piede", DEFAULT_PIEDE);//NOI18N

    return parseMacro(s);
  }

  /**
   * Sostituisce all'interno della stringa i valori
   * di una serie di macro di uso comune nelle stampe.
   * @param s stringa in ingresso
   * @return la stringa con le macro sostituite con i valori
   */
  public String parseMacro(String s)
  {
    try
    {
      if(s == null)
        return null;

      s = StringOper.strReplace(s, "%f", us.getFirstName());//NOI18N
      s = StringOper.strReplace(s, "%l", us.getLastName());//NOI18N
      s = StringOper.strReplace(s, "%n", us.getFirstName() + " " + us.getLastName());//NOI18N

      if(s.indexOf("%df") != -1)//NOI18N
        s = StringOper.strReplace(s, "%df", DT.formatDataFull(today));//NOI18N
      if(s.indexOf("%d") != -1)//NOI18N
        s = StringOper.strReplace(s, "%d", DT.formatData(today));//NOI18N
      if(s.indexOf("%tf") != -1)//NOI18N
        s = StringOper.strReplace(s, "%tf", DT.formatTimeFull(today));//NOI18N
      if(s.indexOf("%t") != -1)//NOI18N
        s = StringOper.strReplace(s, "%t", DT.formatTime(today));//NOI18N

      if(s.indexOf("TODAY") != -1)//NOI18N
        s = StringOper.strReplace(s, "TODAY", formatData(today));//NOI18N

      if(s.indexOf("YEAR_FIRST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        s = StringOper.strReplace(s, "YEAR_FIRST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("YEAR_LAST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_YEAR, 365);
        s = StringOper.strReplace(s, "YEAR_LAST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("PREV_YEAR_FIRST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        s = StringOper.strReplace(s, "PREV_YEAR_FIRST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("PREV_YEAR_LAST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 365);
        s = StringOper.strReplace(s, "PREV_YEAR_LAST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("MONTH_FIRST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        s = StringOper.strReplace(s, "MONTH_FIRST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("MONTH_LAST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        s = StringOper.strReplace(s, "MONTH_LAST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("PREV_MONTH_FIRST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        s = StringOper.strReplace(s, "PREV_MONTH_FIRST", formatData(cal.getTime()));//NOI18N
      }

      if(s.indexOf("PREV_MONTH_LAST") != -1)//NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.MONTH, -1);
        cal.getTime();
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        s = StringOper.strReplace(s, "PREV_MONTH_LAST", formatData(cal.getTime()));//NOI18N
      }

      return s;
    }
    catch(Exception e)
    {
      return "Error: " + e.getMessage();//NOI18N
    }
  }

  /*
  public String getStandardTitolo()
  {
    String standardTitolo = TurbineResources.getString("stampe.titolo", DEFAULT_TITOLO);//NOI18N
    return parseMacro(standardTitolo);
  }
   */
  protected boolean checkPermessi(HttpServletRequest request,
     HttpServletResponse response, ServletConfig config, String permessi)
     throws Exception
  {
    if(!SU.isOkStr(permessi))
      return true;

    boolean rv = false;
    RunDataService trs = (RunDataService) getService(RunDataService.SERVICE_NAME);
    RunData data = trs.getRunData(request, response, config);
    try
    {
      // Pull user from session.
      data.populate();

      rv = SEC.checkAllPermission(data, permessi);
    }
    finally
    {
      trs.putRunData(data);
    }
    return rv;
  }

  abstract public void checkPermessi(HttpServletRequest request,
     HttpServletResponse response, ServletConfig config)
     throws Exception;
}
