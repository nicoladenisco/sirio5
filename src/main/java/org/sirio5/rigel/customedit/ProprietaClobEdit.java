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
package org.sirio5.rigel.customedit;

import java.util.Map;
import javax.swing.table.TableModel;
import org.jdom2.Element;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.CustomColumnEdit;
import org.rigel5.table.RigelColumnDescriptor;
import org.sirio5.utils.SU;

/**
 * Controllo per campo CLOB di proprieta.
 * Il campo viene interpretato come una serie di chiavi key=value seperate da virgola.
 *
 * @author Nicola De Nisco
 */
public class ProprietaClobEdit implements CustomColumnEdit
{
  protected int dimensione = 10;

  @Override
  public void init(Element eleXML)
     throws Exception
  {
    dimensione = SU.parse(eleXML.getAttributeValue("size"), dimensione);
  }

  @Override
  public boolean haveCustomHtml()
  {
    return true;
  }

  @Override
  public String getHtmlEdit(RigelColumnDescriptor cd, TableModel model,
     int row, int col, String cellText, String cellHtml, String nomeCampo, RigelI18nInterface i18n)
     throws Exception
  {
    String[] arr = cellText.split("[,]");

    int ld = this.dimensione;
    if(cd.getSize() > ld)
      ld = cd.getSize();

    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < arr.length; i++)
    {
      String[] sr = arr[i].split("=");
      if(sr.length >= 2)
      {
        sb.append("<input name=\"").append(nomeCampo).append("_key_").append(i).append("\" size=\"")
           .append(ld).append("\" value=\"").append(sr[0]).append("\" class=\"aldefault\">");
        sb.append("=");
        sb.append("<input name=\"").append(nomeCampo).append("_val_").append(i).append("\" size=\"")
           .append(ld).append("\" value=\"").append(sr[1]).append("\" class=\"aldefault\">");
        sb.append("<br>");
      }
    }

    int j = arr.length;
    sb.append("<input name=\"").append(nomeCampo).append("_key_").append(j).append("\" size=\"")
       .append(ld).append("\" value=\"").append("").append("\" class=\"aldefault\">");
    sb.append("=");
    sb.append("<input name=\"").append(nomeCampo).append("_val_").append(j).append("\" size=\"")
       .append(ld).append("\" value=\"").append("").append("\" class=\"aldefault\">");

    return sb.toString();
  }

  @Override
  public boolean haveCustomParser()
  {
    return true;
  }

  @Override
  public Object parseValue(RigelColumnDescriptor cd, TableModel model,
     int row, int col, String formattedValue, String nomeCampo, String oldValue, Map params, RigelI18nInterface i18n)
     throws Exception
  {
    int minKey = 0, maxKey = 0;
    for(Object os : params.keySet())
    {
      if(os.toString().startsWith(nomeCampo))
      {
        int pos = os.toString().lastIndexOf('_');
        int num = Integer.parseInt(os.toString().substring(pos + 1));

        minKey = Math.min(minKey, num);
        maxKey = Math.max(maxKey, num);
      }
    }

    StringBuilder sb = new StringBuilder();
    for(int i = minKey; i <= maxKey; i++)
    {
      String ck = nomeCampo + "_key_" + i;
      String cv = nomeCampo + "_val_" + i;

      String key = SU.okStrNull(params.get(ck));
      String val = SU.okStrNull(params.get(cv));

      if(key != null && val != null)
        sb.append(',').append(key).append('=').append(val);
    }

    return sb.length() == 0 ? "" : sb.substring(1);
  }
}
