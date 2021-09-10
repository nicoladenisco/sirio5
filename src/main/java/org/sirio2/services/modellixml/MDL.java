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
package org.sirio2.services.modellixml;

import java.io.File;
import java.util.List;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.jdom2.Document;
import org.jdom2.Element;
import org.rigel2.glue.WrapperCacheBase;
import org.rigel2.table.html.AbstractHtmlTablePager;
import org.rigel2.table.html.hTable;
import org.rigel2.table.peer.html.PeerWrapperEditHtml;
import org.rigel2.table.peer.html.PeerWrapperFormHtml;
import org.rigel2.table.peer.html.PeerWrapperListaHtml;
import org.rigel2.table.peer.xml.PeerWrapperListaXml;
import org.rigel2.table.sql.html.SqlWrapperFormHtml;
import org.rigel2.table.sql.html.SqlWrapperListaHtml;
import org.rigel2.table.sql.xml.SqlWrapperListaXml;

/**
 * Accesso al servizio modelliXML.
 *
 * @author Nicola De Nisco
 */
public class MDL
{
  private static Object mb = null;

  public static modelliXML getService()
  {
    if(mb == null)
      mb = TurbineServices.getInstance().getService(modelliXML.SERVICE_NAME);
    return (modelliXML) mb;
  }

  public static Document getDocument()
  {
    return getService().getDocument();
  }

  public static void forceReloadXML()
     throws Exception
  {
    getService().forceReloadXML();
  }

  public static PeerWrapperListaHtml getListaPeer(String nomeLista)
     throws Exception
  {
    return getService().getListaPeer(nomeLista);
  }

  public static PeerWrapperEditHtml getListaEditPeer(String nomeLista)
     throws Exception
  {
    return getService().getListaEditPeer(nomeLista);
  }

  public static PeerWrapperFormHtml getFormPeer(String nomeForm)
     throws Exception
  {
    return getService().getFormPeer(nomeForm);
  }

  public static PeerWrapperListaXml getListaXmlPeer(String nomeLista)
     throws Exception
  {
    return getService().getListaXmlPeer(nomeLista);
  }

  public static SqlWrapperListaHtml getListaSql(String nomeLista)
     throws Exception
  {
    return getService().getListaSql(nomeLista);
  }

  public static SqlWrapperFormHtml getFormSql(String nomeForm)
     throws Exception
  {
    return getService().getFormSql(nomeForm);
  }

  public static SqlWrapperListaXml getListaXmlSql(String nomeLista)
     throws Exception
  {
    return getService().getListaXmlSql(nomeLista);
  }

  public static PeerWrapperListaHtml getListaTmap(String nomeTabella)
     throws Exception
  {
    return getService().getListaTmap(nomeTabella);
  }

  public static PeerWrapperEditHtml getListaEditTmap(String nomeTabella)
     throws Exception
  {
    return getService().getListaEditTmap(nomeTabella);
  }

  public static PeerWrapperFormHtml getFormTmap(String nomeTabella)
     throws Exception
  {
    return getService().getFormTmap(nomeTabella);
  }

  public static String getCampoData(String nomeCampo, String nomeForm, String valore, int size)
     throws Exception
  {
    return getService().getCampoData(nomeCampo, nomeForm, valore, size);
  }

  public static String getCampoDataIntervalloInizio(String nomeCampoInizio, String nomeCampoFine, String nomeForm, String valore, int size)
     throws Exception
  {
    return getService().getCampoDataIntervalloInizio(nomeCampoInizio, nomeCampoFine, nomeForm, valore, size);
  }

  public static String getCampoDataIntervalloFine(String nomeCampoInizio, String nomeCampoFine, String nomeForm, String valore, int size)
     throws Exception
  {
    return getService().getCampoDataIntervalloFine(nomeCampoInizio, nomeCampoFine, nomeForm, valore, size);
  }

  public static String getCampoForeign(String nomeCampo, String valore, int size, String url, String valForeign, String extraScript)
     throws Exception
  {
    return getService().getCampoForeign(nomeCampo, valore, size, url, valForeign, extraScript);
  }

  public static hTable getTableCustom(Element ele)
     throws Exception
  {
    Element eleCustom = ele.getChild("custom-classes");
    if(eleCustom == null)
      return null;
    String className = eleCustom.getChildTextTrim("table");
    if(className == null)
      return null;

    return (hTable) Class.forName(className).newInstance();
  }

  public static AbstractHtmlTablePager getPagerCustom(Element ele)
     throws Exception
  {
    Element eleCustom = ele.getChild("custom-classes");
    if(eleCustom == null)
      return null;
    String className = eleCustom.getChildTextTrim("pager");
    if(className == null)
      return null;

    return (AbstractHtmlTablePager) Class.forName(className).newInstance();
  }

  public static WrapperCacheBase getWrapperCache(RunData data)
  {
    return getService().getWrapperCache(data);
  }

  public static void removeWrapperCache(RunData data)
  {
    getService().removeWrapperCache(data);
  }

  public static String getImgCancellaRecord()
     throws Exception
  {
    return getService().getImgCancellaRecord();
  }

  public static String getImgEditData()
     throws Exception
  {
    return getService().getImgEditData();
  }

  public static String getImgEditForeign()
     throws Exception
  {
    return getService().getImgEditForeign();
  }

  public static String getImgEditItem()
     throws Exception
  {
    return getService().getImgEditItem();
  }

  public static String getImgEditRecord()
     throws Exception
  {
    return getService().getImgEditRecord();
  }

  public static String getImgFormForeign()
     throws Exception
  {
    return getService().getImgFormForeign();
  }

  public static String getImgSelect()
     throws Exception
  {
    return getService().getImgSelect();
  }

  public static String getImgSmiley(int stato)
     throws Exception
  {
    return getService().getImgSmiley(stato);
  }

  public static String getImgCollapse()
     throws Exception
  {
    return getService().getImgCollapse();
  }

  public static String getImgExpand()
     throws Exception
  {
    return getService().getImgExpand();
  }

  public static File getWorkCacheFile(String ticket)
  {
    return getService().getWorkCacheFile(ticket);
  }

  public static List<String> getListeSql()
  {
    return getService().getListeSql();
  }
}
