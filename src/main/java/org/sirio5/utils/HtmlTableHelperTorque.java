/*
 * Copyright (C) 2023 Nicola De Nisco
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.ColumnAccessByName;
import org.commonlib5.lambda.FunctionTrowException;
import org.commonlib5.utils.HtmlTableHelper;

/**
 * Classe di utilità per formattare dati in una tabella html.
 * Versione specializzata per oggetti di Torque.
 * Estrae html da una lista di oggetti con interfaccia ColumnAccessByName.
 *
 * @author Nicola De Nisco
 */
public class HtmlTableHelperTorque extends HtmlTableHelper
{
  public static class Holder
  {
    String caption, fieldName, peerName;
    FunctionTrowException<Object, String> convert;
  }

  protected final List<Holder> lsHolder = new ArrayList<>();

  public int addFieldByName(String caption, String fieldName)
  {
    return addFieldByName(caption, fieldName, null);
  }

  public int addFieldByName(String caption, ColumnMap column)
  {
    return addFieldByName(caption, column, null);
  }

  public int addFieldByPeerName(String caption, String fieldName)
  {
    return addFieldByPeerName(caption, fieldName, null);
  }

  public int addFieldByPeerName(String caption, ColumnMap column)
  {
    return addFieldByPeerName(caption, column, null);
  }

  public int addFieldByName(String caption, String fieldName, FunctionTrowException<Object, String> convert)
  {
    Holder h = new Holder();
    h.caption = caption;
    h.fieldName = fieldName;
    h.convert = convert;
    lsHolder.add(h);
    return lsHolder.size();
  }

  public int addFieldByName(String caption, ColumnMap column, FunctionTrowException<Object, String> convert)
  {
    return addFieldByName(caption, column.getJavaName(), convert);
  }

  public int addFieldByPeerName(String caption, String peerName, FunctionTrowException<Object, String> convert)
  {
    Holder h = new Holder();
    h.caption = caption;
    h.peerName = peerName;
    h.convert = convert;
    lsHolder.add(h);
    return lsHolder.size();
  }

  public int addFieldByPeerName(String caption, ColumnMap column, FunctionTrowException<Object, String> convert)
  {
    return addFieldByPeerName(caption, column.getColumnName(), convert);
  }

  public void addRowTorque(ColumnAccessByName object)
  {
    addRow(lsHolder.stream().map((p) -> prepareHtml(p, object)).collect(Collectors.toList()));
  }

  protected String prepareHtml(Holder h, ColumnAccessByName object)
  {
    try
    {
      Object value = null;
      if(h.fieldName != null)
        value = object.getByName(h.fieldName);
      if(h.peerName != null)
        value = object.getByPeerName(h.peerName);

      if(value == null)
        value = "";

      return h.convert == null ? value.toString() : h.convert.apply(value);
    }
    catch(Exception e)
    {
      return "ERROR: " + e.getMessage();
    }
  }

  public void createHeader()
  {
    setHeader(lsHolder.stream().map((p) -> p.caption).collect(Collectors.toList()));
  }

  public void buildFromTorque(Collection<? extends ColumnAccessByName> torqueObjects)
  {
    createHeader();
    for(ColumnAccessByName to : torqueObjects)
      addRowTorque(to);
  }

  @Override
  public void clear()
  {
    lsHolder.clear();
    super.clear();
  }

  public List<Holder> getHolderList()
  {
    return lsHolder;
  }
}

/*
  public String getHtmlNodi()
     throws Exception
  {
    HtmlTableHelperTorque ht = new HtmlTableHelperTorque();
    ht.addFieldByName(i18n.msg("ID"), "NodiTopolinoId");
    ht.addFieldByName(i18n.msg("DESCRIZIONE"), "Descrizione");
    ht.addFieldByName(i18n.msg("UUID"), "Uniqueid");
    ht.addFieldByName(i18n.msg("FUNZIONI"), "NodiTopolinoId", (v) -> formattaFunzioniNodo((Integer) v));
    ht.buildFromTorque(lsNodi);

    StringBuilder sb = new StringBuilder(512);
    ht.formatHtmlContent(sb);
    return sb.toString();
  }
 */
