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
package org.sirio5.utils.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.List;
import java.util.Map;
import org.commonlib5.utils.ArrayMap;
import org.jdom2.Element;
import org.sirio5.utils.SU;

/**
 * Formattatore con una mappa di valori.
 * Nell'XML che lo attiva viene creata una mappa di chiave/valore fissa
 * che viene utilizzata per la formattazione di un valore generico.
 *
 * @author Nicola De Nisco
 */
public class FixedMappedValuesFormat extends Format
{
  protected ArrayMap<String, String> arOptions = new ArrayMap<>();
  protected String valueElse;
  protected boolean useDefault;

  public FixedMappedValuesFormat(Element xml)
  {
    Element elMap = xml.getChild("values-map");
    List<Element> lsOpt = elMap.getChildren("option");
    for(Element eopt : lsOpt)
    {
      String key = SU.okStrNull(eopt.getAttributeValue("value"));
      String val = SU.okStrNull(eopt.getTextTrim());

      if(key != null)
        arOptions.put(key, val);
    }
    valueElse = SU.okStrNull(elMap.getChildText("else"));
    useDefault = elMap.getChild("default") != null;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
  {
    String tmp;
    if(obj != null && (tmp = arOptions.get(obj.toString())) != null)
    {
      toAppendTo.append(tmp);
    }
    else
    {
      if(valueElse != null)
        toAppendTo.append(valueElse);
      else if(useDefault)
        toAppendTo.append(obj.toString());
    }

    return toAppendTo;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos)
  {
    String tmp = valueElse;
    for(Map.Entry<String, String> entry : arOptions.entrySet())
    {
      String key = entry.getKey();
      String value = entry.getValue();

      if(SU.isEqu(source, value))
      {
        tmp = key;
        break;
      }
    }

    pos.setIndex(source.length());
    return tmp;
  }
}
