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

import javax.servlet.http.HttpSession;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.peer.html.PeerPager;
import org.rigel5.table.sql.html.SqlPager;

/**
 * Memoria per il passaggio dei dati da una lista visualizzata
 * ad una lista stampata.
 *
 * @author Nicola De Nisco
 */
public class ReferenceXmlInfoStampe
{
  private String header;
  private String titolo;
  private RigelTableModel rtm;

  private PeerPager ple;
  private SqlPager sle;

  public static final String PARAM_INFO = "stampaxml:";

  public ReferenceXmlInfoStampe()
  {
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, PeerPager p, HttpSession session)
  {
    ReferenceXmlInfoStampe ref = new ReferenceXmlInfoStampe();
    ref.rtm = r;
    ref.ple = p;
    ref.setHeader(header);
    ref.setTitolo(titolo);
    session.setAttribute(PARAM_INFO + idStampa, ref);
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, SqlPager s, HttpSession session)
  {
    ReferenceXmlInfoStampe ref = new ReferenceXmlInfoStampe();
    ref.rtm = r;
    ref.sle = s;
    ref.setHeader(header);
    ref.setTitolo(titolo);
    session.setAttribute(PARAM_INFO + idStampa, ref);
  }

  public static ReferenceXmlInfoStampe loadInfo(String idStampa, HttpSession session)
  {
    return (ReferenceXmlInfoStampe) (session.getAttribute(PARAM_INFO + idStampa));
  }

  public String getHeader()
  {
    return header;
  }

  public void setHeader(String header)
  {
    this.header = header;
  }

  public void setTitolo(String titolo)
  {
    this.titolo = titolo;
  }

  public String getTitolo()
  {
    return titolo;
  }

  public RigelTableModel getRtm()
  {
    return rtm;
  }

  public void setRtm(RigelTableModel rtm)
  {
    this.rtm = rtm;
  }

  public PeerPager getPle()
  {
    return ple;
  }

  public void setPle(PeerPager ple)
  {
    this.ple = ple;
  }

  public SqlPager getSle()
  {
    return sle;
  }

  public void setSle(SqlPager sle)
  {
    this.sle = sle;
  }
}
