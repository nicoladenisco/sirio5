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
package org.sirio2.modules.screens.rigel;

import org.apache.velocity.context.Context;
import org.sirio2.utils.CoreRunData;

/**
 * Edit dei form XML (versione popup).
 * @author Nicola De Nisco
 */
public abstract class FormBasePopup extends FormBase
{
  @Override
  protected void doBuildTemplate2(CoreRunData data, Context context)
     throws Exception
  {
    data.getTemplateInfo().setLayoutTemplate("/Null.vm");
    super.doBuildTemplate2(data, context);
  }

  @Override
  public boolean isPopup()
  {
    return true;
  }

  @Override
  public boolean isEditPopup()
  {
    return true;
  }
}
