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

import java.io.Writer;
import java.sql.Connection;
import java.util.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.om.security.User;
import org.jdom2.Element;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.html.wrapper.ParametroListe;
import org.rigel5.table.sql.xml.*;
import org.rigel5.table.xml.*;
import org.sirio2.services.bus.BUS;
import org.sirio2.services.bus.BusContext;
import org.sirio2.services.bus.BusMessages;
import org.sirio2.services.bus.MessageBusListener;
import org.sirio2.services.localization.INT;
import org.sirio2.services.modellixml.MDL;
import org.sirio2.utils.DT;
import org.sirio2.utils.LI;
import org.sirio2.utils.SU;

/**
 * <p>
 * Title: Newstar Jsp</p>
 * <p>
 * Description: JSP e Bean per l'applicazione WEB di Newstar</p>
 * <p>
 * Copyright: Copyright (c) 2002</p>
 * <p>
 * Company: Italsystems s.r.l.</p>
 * @author Nicola De Nisco
 * @version 1.0
 */
public class jslistaxmlSqlBean implements HttpSessionBindingListener, MessageBusListener
{
  private String type;
  private String sid;
  private Hashtable htListe = new Hashtable();
  private org.apache.turbine.om.security.User us;
  private Date today = new Date();
  private HttpServletRequest request;
  private HttpSession session;
  /** Logging */
  private static Log log = LogFactory.getLog(jslistaxmlSqlBean.class);

  public jslistaxmlSqlBean()
     throws Exception
  {
  }

  protected xTable getTableCustom(Element ele)
  {
    Element eleCustom = ele.getChild("custom-classes");//NOI18N
    if(eleCustom == null)
      return null;
    String className = eleCustom.getChildTextTrim("table");//NOI18N
    if(className == null)
      return null;

    try
    {
      return (xTable) Class.forName(className).newInstance();
    }
    catch(Exception ex)
    {
      log.error(INT.I("Impossibile istanziare la tabella custom '%s':", className), ex);//NOI18N
    }

    return null;
  }

  protected SqlWrapperListaXml creaLista()
     throws Exception
  {
    SqlWrapperListaXml wl = MDL.getListaXmlSql(type);

    wl.setXtbl(getTableCustom(wl.getEleXml()));
    if(wl.getXtbl() == null)
      wl.setXtbl(new xTable());

    wl.init();
    log.debug(INT.I("Creato nuovo SqlWrapperListaXml %s", type));
    return wl;
  }

  protected synchronized SqlWrapperListaXml getLista()
     throws Exception
  {
    SqlWrapperListaXml wl = (SqlWrapperListaXml) (htListe.get(type));
    if(wl == null)
    {
      wl = creaLista();
      htListe.put(type, wl);
    }
    return wl;
  }

  public String getHeader()
     throws Exception
  {
    return getLista().getHeader();
  }

  public String getTitolo()
     throws Exception
  {
    return getLista().getTitolo();
  }

  public void getXml(Writer out)
     throws Exception
  {
    getLista().getXml(out);
  }

  public boolean checkPermessi(HttpSession sessione)
     throws Exception
  {
// TODO: reimplemetare
//    String permessi = getLista().getPermessi();
//    return permessi == null ? true : CaleidoSecurity.checkPermission(sessione, permessi);
    return true;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public void setSid(String sid)
  {
    this.sid = sid;
  }

  public String getSid()
  {
    return sid;
  }

  public org.apache.turbine.om.security.User getUser(HttpServletRequest request)
     throws Exception
  {
    this.request = request;
    this.session = request.getSession();

    us = (User) session.getAttribute("turbine.user");//NOI18N
    if(us == null)
      throw new Exception(INT.I("Utente non autorizzato! Eseguire la logon con un utente valido."));

    if(type == null)
      throw new Exception(INT.I("Parametri insufficienti."));

    if(!checkPermessi(this.session))
      throw new Exception(INT.I("Permessi insufficienti per i dati richiesti. Contattare l'amministratore."));

    return us;
  }

  public String parseMacro(String macro)
     throws Exception
  {
    switch(macro)
    {

      case "TODAY": //NOI18N
      {
        return DT.formatData(today);
      }

      case "YEAR_FIRST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return DT.formatData(cal.getTime());
      }

      case "YEAR_LAST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_YEAR, 365);
        return DT.formatData(cal.getTime());
      }

      case "PREV_YEAR_FIRST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return DT.formatData(cal.getTime());
      }

      case "PREV_YEAR_LAST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 365);
        return DT.formatData(cal.getTime());
      }

      case "MONTH_FIRST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return DT.formatData(cal.getTime());
      }

      case "MONTH_LAST": //NOI18N
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(today);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return DT.formatData(cal.getTime());
      }

      default:
        break;
    }

    return macro;
  }

  public String getHtmlParametro(ParametroListe pl)
     throws Exception
  {
    String sOut = "<tr>"//NOI18N
       + "<td>" + pl.getNome() + "</td>"//NOI18N
       + "<td>" + pl.getOperazione() + "</td>";//NOI18N

    String val = parseMacro(SU.okStr(pl.getDefval()));

    if(pl.getTipo() == RigelColumnDescriptor.PDT_DATE)
      sOut += "<td>" + MDL.getCampoData(pl.getHtmlCampo(), "fo", val, 20) + "</td>";//NOI18N
    else if(pl.getForeignMode() != RigelColumnDescriptor.DISP_FLD_ONLY)
      sOut += "<td>" + MDL.getCampoForeign(pl.getHtmlCampo(), val, 20,
         "javascript:apriFinestra('" + pl.getForeignEditUrl() + "&@@@')",//NOI18N
         (pl.getForeignMode() == RigelColumnDescriptor.DISP_DESCR_EDIT) ? "" : null,
         null) + "</td>";//NOI18N
    else
      sOut += "<td><input type=text size=20 value=\"" + val + "\" name=\"" + pl.getHtmlCampo() + "\"></td>";//NOI18N

    sOut += "<td>" + pl.getDescrizione() + "</td>"//NOI18N
       + "</tr>";//NOI18N

    return sOut;
  }

  public String getHtmlParamStampa()
     throws Exception
  {
    SqlWrapperListaXml wl = getLista();

    synchronized(wl)
    {
      String sOut = "";//NOI18N
      for(ParametroListe pl : wl.getFiltro().getParametri())
        sOut += getHtmlParametro(pl);

      if(wl.ssp.getGroupby() != null)
        for(ParametroListe pl : wl.ssp.getGroupby().filtro.getParametri())
          sOut += getHtmlParametro(pl);

      return sOut;
    }
  }

  public String getParametriFissiStampa()
     throws Exception
  {
    SqlWrapperListaXml wl = getLista();
    return wl.makeHiddenPrintParametri(0);
  }

  public String getUrlStampa()
     throws Exception
  {
    SqlWrapperListaXml wl = getLista();
    return LI.getLinkUrl("/pdf/" + wl.getPrInfo().getUrlEditRiga());//NOI18N
  }

  public void parseParamStampa(HttpServletRequest request)
     throws Exception
  {
    SqlWrapperListaXml wl = getLista();

    synchronized(wl)
    {
      for(ParametroListe pl : wl.getFiltro().getParametri())
      {
        pl.setValore(request.getParameter(pl.getHtmlCampo()));
        log.debug("par=" + pl.getNome() + "(" + pl.getCampo() + ") [" + pl.getHtmlCampo() + "] val=" + pl.getValore());//NOI18N
      }

      if(wl.ssp.getGroupby() != null)
      {
        for(ParametroListe pl : wl.ssp.getGroupby().filtro.getParametri())
        {
          pl.setValore(request.getParameter(pl.getHtmlCampo()));
          log.debug("par=" + pl.getNome() + "(" + pl.getCampo() + ") [" + pl.getHtmlCampo() + "] val=" + pl.getValore());//NOI18N
        }
      }
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent hsbe)
  {
    BUS.registerEventListner(this);
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent hsbe)
  {
    BUS.removeEventListner(this);
  }

  @Override
  public int message(int msgID, Object originator, BusContext context)
     throws Exception
  {
    switch(msgID)
    {
      case BusMessages.RIGEL_XML_LIST_RELOADED:
        synchronized(this)
        {
          htListe.clear();
        }
        break;
    }

    return 0;
  }
}
