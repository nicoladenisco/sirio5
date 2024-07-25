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
package org.sirio5.modules.actions;

import java.util.HashSet;
import java.util.Map;
import org.apache.velocity.context.Context;
import org.sirio5.beans.CoreBaseBean;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;

/**
 * Classe base di tutte le action.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class CoreBaseActionDoPerform extends CoreBaseAction
{
  protected final HashSet<String> ignoreCommands = new HashSet<>();

  @Override
  protected void doPerform2(CoreRunData data, Context context, CoreBaseBean bean)
     throws Exception
  {
    super.doPerform2(data, context, bean);

    if(bean == null)
      throw new RuntimeException(data.i18n(
         "La classe del bean non è stata impostata: occorre utilizzare il metodo setBeanClass()."));

    // trasferisce tutti i parametri dall'html a proprietà del bean
    bean.readParameters(data);

    String command = SU.okStrNull(data.getParameters().getString("command"));
    if(command != null)
    {
      Map params = null;
      if(!ignoreCommands.contains(command))
      {
        params = SU.getParMap(data);
        bean.readParams(params);
      }

      bean.doCommand(command, data, params, context);
      this.doCommand(command, data, params, context, bean);
    }
  }
}
