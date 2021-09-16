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
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import org.apache.turbine.om.security.User;
import org.rigel5.db.sql.FiltroData;
import org.rigel5.table.FiltroListe;
import org.rigel5.table.xml.*;
import org.sirio5.services.localization.INT;

/**
 * Bean per la generazione di XML a partire da una lista
 * di visualizzazione. Eventuali filtri impostati sulla vista
 * vengono applicati anche alla generazione di dati XML (e di
 * conseguenza alla stampa se i dati XML hanno questa finalita').
 *
 * @author Michele Borriello
 * @version 1.0
 */
public class stampaListeBean extends jsxmlBaseBean
{
  private String type;
  private Hashtable htListe = new Hashtable();

  private ReferenceXmlInfoStampe rInfo = null;

  public static final int RECORD_PER_PASSATA = 100;

  public stampaListeBean()
     throws Exception
  {
  }

  public String getHeader()
     throws Exception
  {
    return rInfo.getHeader();
  }

  public String getTitolo()
     throws Exception
  {
    return parseTitolo(rInfo.getTitolo());

  }

  public void getXml(Writer out)
     throws Exception
  {
    if(rInfo.getPle() != null && rInfo.getSle() == null)
      getXmlPeer(out);
    else if(rInfo.getPle() == null && rInfo.getSle() != null)
      getXmlSql(out);
    else
      throw new Exception(INT.I("Condizione non possibile."));
  }

  public void getXmlPeer(Writer out)
     throws Exception
  {
    xTable tbl = new xTable();
    org.rigel5.table.peer.xml.PeerTableModel ptm = new org.rigel5.table.peer.xml.PeerTableModel();

    ptm.setSuppEsclRicerca(true);
    ptm.initFrom(rInfo.getRtm());
    ptm.attach(tbl);

    // recupera il filtro attualmente impostato
    FiltroListe fl = rInfo.getPle().getCSelezione();

    long totalRec = rInfo.getPle().getTotalRecords();

    tbl.normalizeCols();
    out.write("<" + tbl.getTableStatement() + ">\r\n");//NOI18N

    tbl.doHeader(out);

    // genera XML a blocchi di RECORD_PER_PASSATA records
    // questo e' necessario per grosse tabelle, dove una
    // unica getRecords impegnerebbe pesantemente la memoria
    for(int rStart = 0; rStart < totalRec; rStart += RECORD_PER_PASSATA)
    {
      fl.setOffset(rStart);
      fl.setLimit(RECORD_PER_PASSATA);
//      fl.setIgnoreCase(true);
      List data = rInfo.getPle().getRecords(fl);
      ptm.rebind(data);
      tbl.doRows(out);
    }

    out.write("</" + tbl.getTableStatement() + ">\r\n");//NOI18N
  }

  public void getXmlSql(Writer out)
     throws Exception
  {
    xTable tbl = new xTable();
    org.rigel5.table.sql.xml.SqlTableModel stm = new org.rigel5.table.sql.xml.SqlTableModel();

    stm.setSuppEsclRicerca(true);
    stm.initFrom(rInfo.getRtm());
    stm.attach(tbl);

    // recupera il filtro attualmente impostato
    FiltroListe fl = rInfo.getSle().getCSelezione();

    long totalRec = rInfo.getSle().getTotalRecords();

    tbl.normalizeCols();
    out.write("<" + tbl.getTableStatement() + ">\r\n");//NOI18N

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

    out.write("</" + tbl.getTableStatement() + ">\r\n");//NOI18N
  }

  /**
   * Questa jsp viene utilizzata con dati gia' costruiti
   * quindi si presume che i test sui permessi siano gia' stati eseguiti.
   */
  @Override
  public void checkPermessi(HttpServletRequest request,
     HttpServletResponse response, ServletConfig config)
     throws Exception
  {
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public User getUser(HttpServletRequest request)
     throws Exception
  {
    us = (User) request.getSession().getAttribute(User.SESSION_KEY);
    if(us == null)
      throw new Exception(INT.I("Utente non autorizzato! Eseguire la logon con un utente valido."));

    if(type == null)
      throw new Exception(INT.I("Parametri insufficienti."));

    rInfo = ReferenceXmlInfoStampe.loadInfo(type, request.getSession());

    return us;
  }
}
