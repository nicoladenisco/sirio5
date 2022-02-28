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
package org.sirio5.rigel.table;

import com.workingdogs.village.Record;
import java.util.*;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.ColumnAccessByName;
import org.jdom2.Element;
import org.rigel5.RigelXmlSetupInterface;
import org.rigel5.db.DbUtils;
import org.rigel5.db.torque.TableMapHelper;
import org.rigel5.glue.table.PeerAppMaintFormTable;
import org.rigel5.table.RigelColumnDescriptor;
import org.sirio5.services.cache.CACHE;
import org.sirio5.services.cache.TableCache;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.SU;

/**
 * Formattatore per edit di valori multipli sotto forma di checkbox.
 *
 * @author Nicola De Nisco
 */
public class MultipleCheckboxTable extends PeerAppMaintFormTable
   implements RigelXmlSetupInterface
{
  protected static class Info
  {
    public int ncol;
    public String link, display, table, separator;
    public Element xml;
    public TableMapHelper tMap;
    public ColumnMap cmLink, cmDisplay;
    public TableCache<ColumnAccessByName> tc;
    public String jNameLink, jNameDisplay;
    public String query, cacheKey;

    public List<Record> getRecords()
       throws Exception
    {
      return CACHE.fastEntry(this, cacheKey, () -> DbUtils.executeQuery(query));
    }
  }

  protected Element xml;
  protected HashMap<String, Info> infoMap = new HashMap<>();

  public MultipleCheckboxTable()
  {
    super();
  }

  @Override
  public Element getEleXml()
  {
    return xml;
  }

  @Override
  public void setEleXml(Element xml)
  {
    this.xml = xml;

    List<Element> lsColonne = xml.getChildren("colonna");
    for(Element ecol : lsColonne)
    {
      Element echk;
      if((echk = ecol.getChild("multiple-checkbox")) == null)
        continue;

      Info info = new Info();

      info.ncol = SU.parse(echk.getChildText("numcol"), 1);
      info.separator = SU.okStr(echk.getChildText("separator"), "[,;: ]");
      info.cacheKey = "info.records_" + System.currentTimeMillis();

      if((info.query = SU.okStrNull(echk.getChildText("query"))) == null)
      {
        info.link = SU.okStrNull(echk.getChildText("link"));
        info.display = SU.okStrNull(echk.getChildText("display"));
        info.table = SU.okStrNull(echk.getChildText("table"));

        if(info.link == null || info.table == null)
          throw new RuntimeException(INT.I("Attributo field/table non specificato."));

        try
        {
          info.tMap = TableMapHelper.getByTableName(info.table);
        }
        catch(Exception ex)
        {
          throw new RuntimeException(ex);
        }

        if(info.tMap == null)
          throw new RuntimeException(INT.I("Tabella %s non trovata nel db.", info.table));
        info.cmLink = info.tMap.getCampo(info.link);
        if(info.cmLink == null)
          throw new RuntimeException(INT.I("Colonna %s non trovata nella tabella %s.", info.link, info.table));
        info.cmDisplay = info.tMap.getCampo(info.display);
        if(info.cmDisplay == null)
          throw new RuntimeException(INT.I("Colonna %s non trovata nella tabella %s.", info.display, info.table));

        info.jNameLink = info.cmLink.getJavaName();
        info.jNameDisplay = info.cmDisplay.getJavaName();
        info.tc = new TableCache<>(info.tMap.getTmap().getPeerClass());
      }

      String nomeColonna = SU.okStrNull(ecol.getAttributeValue("nome"));
      if(nomeColonna != null)
        infoMap.put(nomeColonna, info);
    }
  }

  @Override
  public void salvaDatiCella(int row, int col, Map params)
     throws Exception
  {
    Info info;
    RigelColumnDescriptor cd = getCD(col);
    if(cd != null && (info = infoMap.get(cd.getCaption())) != null)
    {
      // parse dei checkbox
      String originale = formatValoreCampo(row, col);
      String valore = (info.query == null) ? parseCheckboxPeer(originale, info, getNomeCampo(row, col), params)
                         : parseCheckboxQuery(originale, info, getNomeCampo(row, col), params);
      if(!SU.isEqu(originale, valore))
        tableModel.setValueAt(valore, row, col);
      return;
    }

    super.salvaDatiCella(row, col, params);
  }

  @Override
  protected String doInnerCell(int row, int col, String cellText, String cellHtml)
     throws Exception
  {
    Info info;
    RigelColumnDescriptor cd = getCD(col);
    if(cd != null && (info = infoMap.get(cd.getCaption())) != null)
    {
      // formatta la tabella dei checkbox
      String originale = formatValoreCampo(row, col);
      if(info.query == null)
        return formatCheckboxPeer(originale, info, getNomeCampo(row, col));
      else
        return formatCheckboxQuery(originale, info, getNomeCampo(row, col));
    }

    return super.doInnerCell(row, col, cellText, cellHtml);
  }

  protected String formatCheckboxPeer(String val, Info info, String nomeCampo)
     throws Exception
  {
    String[] values = val.split(info.separator);
    Arrays.sort(values);

    List<ColumnAccessByName> lsObject = new ArrayList<>(info.tc.getList(true));
    lsObject.sort((o1, o2) -> SU.compare(o1.getByName(info.jNameDisplay), o2.getByName(info.jNameDisplay)));

    StringBuilder sb = new StringBuilder(512);
    sb.append("<table><tr>");
    for(int i = 0; i < lsObject.size(); i++)
    {
      ColumnAccessByName bo = lsObject.get(i);

      String valLink = SU.okStr(bo.getByName(info.jNameLink));
      String valDisplay = SU.okStr(bo.getByName(info.jNameDisplay));
      boolean checked = Arrays.binarySearch(values, valLink) >= 0;

      sb.append("<td><input type='checkbox' value='1' name='chk_")
         .append(nomeCampo).append("_").append(valLink).append("' ").append(checked ? "checked" : "").append(" >")
         .append(valDisplay).append("</td>");

      if(((i + 1) % info.ncol) == 0)
        sb.append("</tr><tr>");
    }

    sb.append("</tr></table>");
    return sb.toString();
  }

  protected String formatCheckboxQuery(String val, Info info, String nomeCampo)
     throws Exception
  {
    String[] values = val.split(info.separator);
    Arrays.sort(values);

    List<Record> lsRecords = info.getRecords();
    StringBuilder sb = new StringBuilder(512);
    sb.append("<table><tr>");

    for(int i = 0; i < lsRecords.size(); i++)
    {
      Record bo = lsRecords.get(i);

      String valLink = SU.okStr(bo.getValue(1).asString());
      String valDisplay = SU.okStr(bo.getValue(2).asString());
      boolean checked = Arrays.binarySearch(values, valLink) >= 0;

      sb.append("<td><input type='checkbox' value='1' name='chk_")
         .append(nomeCampo).append("_").append(valLink).append("' ").append(checked ? "checked" : "").append(" >")
         .append(valDisplay).append("</td>");

      if(((i + 1) % info.ncol) == 0)
        sb.append("</tr><tr>");
    }

    sb.append("</tr></table>");
    return sb.toString();
  }

  protected String parseCheckboxPeer(String val, Info info, String nomeCampo, Map params)
     throws Exception
  {
    StringBuilder rv = new StringBuilder();

    List<ColumnAccessByName> lsObject = info.tc.getList(true);
    for(ColumnAccessByName bo : lsObject)
    {
      String valLink = SU.okStr(bo.getByName(info.jNameLink));
      if(params.get("chk_" + nomeCampo + "_" + valLink) != null)
      {
        rv.append(",").append(valLink);
      }
    }

    return rv.length() == 0 ? null : rv.substring(1);
  }

  protected String parseCheckboxQuery(String val, Info info, String nomeCampo, Map params)
     throws Exception
  {
    StringBuilder rv = new StringBuilder();

    List<Record> lsRecords = info.getRecords();
    for(Record bo : lsRecords)
    {
      String valLink = SU.okStr(bo.getValue(1).asString());
      if(params.get("chk_" + nomeCampo + "_" + valLink) != null)
      {
        rv.append(",").append(valLink);
      }
    }

    return rv.length() == 0 ? null : rv.substring(1);
  }
}
