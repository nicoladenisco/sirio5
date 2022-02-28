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
package org.sirio5.modules.screens.rigel;

import org.apache.velocity.context.*;
import org.rigel5.glue.table.AlternateColorTableAppBase;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.sirio5.utils.CoreRunData;

/**
 * Visualizzatore liste XML integrato nella maschera principale (non popup).
 *
 * @author Nicola De Nisco
 */
abstract public class ListaBaseMaint5 extends ListaBase5
{
  @Override
  public boolean isPopup()
  {
    return false;
  }

  @Override
  public boolean isEditPopup()
  {
    return false;
  }

  @Override
  protected void makeContextHtml(HtmlWrapperBase lso, ListaInfo li, CoreRunData data, Context context, String baseUri)
     throws Exception
  {
    AlternateColorTableAppBase act = (AlternateColorTableAppBase) (lso.getTbl());
    act.setAuthDelete(isAuthorizedDelete(data));
    act.setPopup(false);
    act.setEditPopup(false);
    act.setAuthSel(false);

    super.makeContextHtml(lso, li, data, context, baseUri);
  }
}
