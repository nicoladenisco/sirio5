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

import javax.servlet.http.HttpSession;
import org.jdom2.Element;
import org.rigel2.table.RigelTableModel;
import org.rigel2.table.peer.html.PeerPager;
import org.rigel2.table.sql.html.SqlPager;

/**
 * Salva nella sessione le impostazioni di una lista affinchè
 * possa essere stampata in seguito.
 * Lo stato dei filtri e l'impostazione del tablemodel vengono
 * salvati. Una apposita jsp riprende questi dati generando
 * un opportuno XML con le stesse colonne e gli stessi filtri
 * impostati. FOP trasformerà questo XML in PDF o PostScript.
 *
 * @author Nicola De Nisco
 */
public class ReferenceXmlInfo
{
  private String header;
  private String titolo;
  private RigelTableModel rtm;
  private PeerPager ple;
  private SqlPager sle;
  private Element eleXml;

  public ReferenceXmlInfo()
  {
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, PeerPager p, HttpSession session)
  {
    saveInfo(idStampa, header, titolo, r, p, session, null);
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, PeerPager p, HttpSession session, Element eleXml)
  {
    ReferenceXmlInfo ref = new ReferenceXmlInfo();
    ref.rtm = r;
    ref.ple = p;
    ref.eleXml = eleXml;
    ref.setHeader(header);
    ref.setTitolo(titolo);
    session.setAttribute(jsrefxmlBean.PARAM_INFO + idStampa, ref);
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, SqlPager s, HttpSession session)
  {
    saveInfo(idStampa, header, titolo, r, s, session, null);
  }

  public static void saveInfo(
     String idStampa, String header, String titolo,
     RigelTableModel r, SqlPager s, HttpSession session, Element eleXml)
  {
    ReferenceXmlInfo ref = new ReferenceXmlInfo();
    ref.rtm = r;
    ref.sle = s;
    ref.eleXml = eleXml;
    ref.setHeader(header);
    ref.setTitolo(titolo);
    session.setAttribute(jsrefxmlBean.PARAM_INFO + idStampa, ref);
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

  public Element getEleXml()
  {
    return eleXml;
  }

  public void setEleXml(Element eleXml)
  {
    this.eleXml = eleXml;
  }
}
