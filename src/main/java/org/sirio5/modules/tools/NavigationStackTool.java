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
package org.sirio5.modules.tools;

import org.apache.turbine.services.pull.RunDataApplicationTool;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.uri.TemplateURI;
import org.sirio5.beans.BeanFactory;
import org.sirio5.beans.NavigationStackBean;
import org.sirio5.utils.CoreRunData;

/**
 * Tool per l'accesso allo stack di navigazione.
 *
 * @author Nicola De Nisco
 */
public class NavigationStackTool implements RunDataApplicationTool
{
  private NavigationStackBean nsb = null;

  @Override
  public void init(Object data)
  {
  }

  @Override
  public void refresh(RunData data)
  {
    try
    {
      if(data != null && nsb == null)
        nsb = BeanFactory.getFromSession(data, NavigationStackBean.class);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public void pushUri(RunData data)
     throws Exception
  {
    nsb.pushUri((CoreRunData) data);
  }

  public void pushUriTemplate(RunData data, String template)
     throws Exception
  {
    TemplateURI tu = new TemplateURI(data, template);
    nsb.pushUri((CoreRunData) data, tu.getRelativeLink());
  }

  public void popUri(RunData data)
     throws Exception
  {
    nsb.popUri((CoreRunData) data);
  }

  public void peekUri(RunData data)
     throws Exception
  {
    nsb.peekUri((CoreRunData) data);
  }

  public void return2(RunData data)
     throws Exception
  {
    nsb.return2((CoreRunData) data);
  }

  public void clear(RunData data)
     throws Exception
  {
    nsb.clear();
  }
}
