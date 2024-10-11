/*
 * Copyright (C) 2024 Nicola De Nisco
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
package org.sirio5.rigel;

import org.json.JSONArray;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.xml.jsonTable;
import org.sirio5.utils.CoreRunData;

/**
 * Tabella rigel specializzata per la produzione di JSON adatto alla datatable.
 *
 * @author Nicola De Nisco
 */
public class ToolJsonDatatable extends jsonTable
{
  public void setRunData(CoreRunData data)
  {
  }

  @Override
  public void doRow(JSONArray out, int row)
     throws Exception
  {
    for(int col = 0; col < columnModel.getColumnCount(); col++)
      out.put(doCellText(row, col));
  }

  public String doCellText(int row, int col)
     throws Exception
  {
    RigelColumnDescriptor cd;
    if((cd = getCD(col)) == null)
      return "";

    if(!cd.isVisible())
      return "";

    return doFormatCellValue(row, col, cd);
  }
}
