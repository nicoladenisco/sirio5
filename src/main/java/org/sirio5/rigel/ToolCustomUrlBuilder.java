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
package org.sirio5.rigel;

import java.util.Map;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.sirio5.utils.SU;

/**
 * Implementazione speciale dedicata ai Tool.
 *
 * @author Nicola De Nisco
 */
public class ToolCustomUrlBuilder extends CoreCustomUrlBuilder
{
  private String func, type;

  public String getFunc()
  {
    return func;
  }

  public void setFunc(String func)
  {
    this.func = func;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  @Override
  public String buildUrlLineSelezione(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return "javascript:impostaValori('" + func + "'," + inputUrl + ")";
  }

  @Override
  public String buildUrlSelezionaRecord(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return "javascript:impostaValori('" + func + "'," + inputUrl + ")";
  }

  @Override
  public String buildUrlCancellaRecord(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    String s = SU.purge(type);
    return "javascript:cancellaElemento_" + s + "('" + inputUrl + "')";
  }
}
