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

import java.util.Map;
import org.apache.velocity.context.Context;
import org.sirio5.beans.CoreBaseBean;
import org.sirio5.beans.NavigationStackBean;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;

/**
 * Action per la navigazione.
 *
 * @author Nicola De Nisco
 */
public class NavStackAction extends CoreBaseAction
{
  public NavStackAction()
  {
    setBeanClass(NavigationStackBean.class);
  }

  @Override
  protected void doPerform2(CoreRunData data, Context context, CoreBaseBean bean)
     throws Exception
  {
    String command = SU.okStrNull(data.getParameters().getString("command"));
    if(command != null)
      doCommand(command, data, null, bean);
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return true;
  }

  public void doCmd_push(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    NavigationStackBean bean = (NavigationStackBean) args[0];
    bean.pushUri(data);
  }

  public void doCmd_pop(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    NavigationStackBean bean = (NavigationStackBean) args[0];
    bean.popUri(data);
  }

  public void doCmd_peek(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    NavigationStackBean bean = (NavigationStackBean) args[0];
    bean.peekUri(data);
  }

  public void doCmd_return2(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    NavigationStackBean bean = (NavigationStackBean) args[0];
    bean.return2(data);
  }

  public void doCmd_clear(CoreRunData data, Map params, Object... args)
     throws Exception
  {
    removeBeanFromSession(data);
  }
}
