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
package org.sirio5.beans.xml;

import java.io.Writer;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.table.TableColumnModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.session.SessionService;
import org.jdom2.Element;
import org.rigel5.db.sql.FiltroData;
import org.rigel5.table.FiltroListe;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.xml.xTable;
import static org.sirio5.services.localization.INT.I;

/**
 * Bean per il supporto alla stampa delle liste.
 * Insieme a jsrefxml.jsp produce un xml che opportunamente
 * convertito da fop produce un pdf con la stampa della lista
 * corrente.
 *
 * @author Nicola De Nisco
 */
public class jsrefxmlBean
{
  /** Logging */
  private static final Log log = LogFactory.getLog(jsrefxmlBean.class);

  protected String type;
  protected String sid;
  protected org.apache.turbine.om.security.User us;
  protected ReferenceXmlInfo rInfo = null;
  protected HttpServletRequest request;
  protected HttpSession session;
  protected SessionService sessionService;

  public static final String PARAM_INFO = "refxml:";
  public static final int RECORD_PER_PASSATA = 100;

  protected xTable getTableCustom(Element ele)
  {
    Element eleCustom = ele.getChild("custom-classes"); // NOI18N
    if(eleCustom == null)
      return null;
    String className = eleCustom.getChildTextTrim("xml-table"); // NOI18N
    if(className == null)
      return null;

    try
    {
      return (xTable) Class.forName(className).newInstance();
    }
    catch(Throwable ex)
    {
      log.error(I("Impossibile istanziare la tabella custom '%s':", className), ex);
    }

    return null;
  }

  public String getHeader()
     throws Exception
  {
    return rInfo.getHeader();
  }

  public String getTitolo()
     throws Exception
  {
    return rInfo.getTitolo();
  }

  public void getXml(Writer out)
     throws Exception
  {
    if(rInfo.getPle() != null && rInfo.getSle() == null)
      getXmlPeer(out);
    else if(rInfo.getPle() == null && rInfo.getSle() != null)
      getXmlSql(out);
    else
      throw new Exception(I("Condizione non possibile."));
  }

  public void getXmlPeer(Writer out)
     throws Exception
  {
    xTable tbl = null;

    if(rInfo.getEleXml() != null)
      // prova con tabella personalizzata
      tbl = getTableCustom(rInfo.getEleXml());

    if(tbl == null)
      tbl = new xTable();

    org.rigel5.table.peer.xml.PeerTableModel ptm = new org.rigel5.table.peer.xml.PeerTableModel();

    ptm.setSuppEsclRicerca(true);
    ptm.initFrom(rInfo.getRtm());

    for(int i = 0; i < ptm.getColumnCount(); i++)
    {
      RigelColumnDescriptor col = ptm.getColumn(i);
      if(!col.isPrintable())
      {
        ptm.delColumn(i);
        i--;
      }
    }

    if(ptm.getColumnCount() == 0)
      throw new Exception(I("Nessuna colonna stampabile nella lista."));

    ptm.attach(tbl);

    // recupera il filtro attualmente impostato
    FiltroListe fl = rInfo.getPle().getCSelezione();

    long totalRec = rInfo.getPle().getTotalRecords();

    tbl.clearColSizes();
    tbl.normalizeCols();
    out.write("<" + tbl.getTableStatement() + ">\r\n"); // NOI18N

    tbl.doHeader(out);

    // genera XML a blocchi di RECORD_PER_PASSATA records
    // questo e' necessario per grosse tabelle, dove una
    // unica getRecords impegnerebbe pesantemente la memoria
    for(int rStart = 0; rStart < totalRec; rStart += RECORD_PER_PASSATA)
    {
      fl.setOffset(rStart);
      fl.setLimit(RECORD_PER_PASSATA);
      //se nella selezione si utilizzano campi interi, questo provoca errore
      //fl.setIgnoreCase(true);
      List data = rInfo.getPle().getRecords(fl);
      ptm.rebind(data);
      tbl.doRows(out);
    }

    // emette le dimensioni di colonna
    getColumnSizes(tbl.getArColSizes(), tbl.getColumnModel(), out);

    out.write("</" + tbl.getTableStatement() + ">\r\n"); // NOI18N
  }

  public void getXmlSql(Writer out)
     throws Exception
  {
    xTable tbl = null;

    if(rInfo.getEleXml() != null)
      // prova con tabella personalizzata
      tbl = getTableCustom(rInfo.getEleXml());

    if(tbl == null)
      tbl = new xTable();

    org.rigel5.table.sql.xml.SqlTableModel stm = new org.rigel5.table.sql.xml.SqlTableModel();

    stm.setSuppEsclRicerca(true);
    stm.initFrom(rInfo.getRtm());

    for(int i = 0; i < stm.getColumnCount(); i++)
    {
      RigelColumnDescriptor col = stm.getColumn(i);
      if(!col.isPrintable())
      {
        stm.delColumn(i);
        i--;
      }
    }

    if(stm.getColumnCount() == 0)
      throw new Exception(I("Nessuna colonna stampabile nella lista."));

    stm.attach(tbl);

    // recupera il filtro attualmente impostato
    FiltroListe fl = rInfo.getSle().getCSelezione();

    long totalRec = rInfo.getSle().getTotalRecords();

    tbl.clearColSizes();
    tbl.normalizeCols();
    out.write("<" + tbl.getTableStatement() + ">\r\n"); // NOI18N

    tbl.doHeader(out);

    // genera XML a blocchi di RECORD_PER_PASSATA records
    // questo e' necessario per grosse tabelle, dove una
    // unica getRecords impegnerebbe pesantemente la memoria
    for(int rStart = 0; rStart < totalRec; rStart += RECORD_PER_PASSATA)
    {
      stm.getQuery().setOffset(rStart);
      stm.getQuery().setLimit(RECORD_PER_PASSATA);
      stm.getQuery().setIgnoreCase(true);
      stm.getQuery().setFiltro((FiltroData) (fl.getOggFiltro()));

      stm.rebind();
      tbl.doRows(out);
    }

    // emette le dimensioni di colonna
    getColumnSizes(tbl.getArColSizes(), tbl.getColumnModel(), out);

    out.write("</" + tbl.getTableStatement() + ">\r\n"); // NOI18N
  }

  protected void getColumnSizes(int[] arSizes, TableColumnModel tm, Writer out)
     throws Exception
  {
    double total = 0;
    if(arSizes != null)
    {
      out.write("<column-sizes>\r\n"); // NOI18N
      for(int i = 0; i < tm.getColumnCount(); i++)
        total += arSizes[i];

      for(int i = 0; i < tm.getColumnCount(); i++)
      {
        RigelColumnDescriptor cd = (RigelColumnDescriptor) tm.getColumn(i);
        float size = (float) (arSizes[i] / total);
        out.write("<column name=\"" + cd.getCaption() + "\" size=\"" + size + "\"/>\r\n"); // NOI18N
      }
      out.write("</column-sizes>\r\n"); // NOI18N
    }
  }

  /**
   * Questa jsp viene utilizzata con dati gia' costruiti
   * quindi si presume che i test sui permessi siano gia' stati eseguiti.
   *
   * @param sessione
   * @return
   * @throws Exception
   */
  public boolean ckeckPermessi(HttpSession sessione)
     throws Exception
  {
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

    String sesid = request.getParameter("sesid");
    if(sesid != null)
    {
      session = getSessionService().getSession(sesid);
    }

    us = (User) session.getAttribute("turbine.user"); // NOI18N
    if(us == null)
      throw new Exception(I("Utente non autorizzato! Eseguire la logon con un utente valido."));

    // recupera l'oggetto di riferimento
    rInfo = (ReferenceXmlInfo) (session.getAttribute(PARAM_INFO + type));
    if(rInfo == null)
      throw new Exception(I("Nessun oggetto di riferimento per il tipo %s", type));

    if(!ckeckPermessi(session))
      throw new Exception(I("Permessi insufficienti per i dati richiesti. Contattare l'amministratore."));

    return us;
  }

  public SessionService getSessionService()
  {
    // don't care about synchronization, lookup is cheap
    if(sessionService == null)
    {
      sessionService = (SessionService) TurbineServices.getInstance().getService(SessionService.SERVICE_NAME);
    }

    return sessionService;
  }
}
