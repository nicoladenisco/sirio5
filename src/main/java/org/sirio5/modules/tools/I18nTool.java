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

import java.util.Locale;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.RunDataApplicationTool;
import org.apache.turbine.util.RunData;

/**
 * Tool per l'accesso diretto alle stringhe di risorsa.
 *
 * @author Nicola De Nisco
 */
public class I18nTool implements RunDataApplicationTool
{
  private LocalizationService lsrv = null;
  private Locale userLocale = null;

  @Override
  public void init(Object data)
  {
    if(lsrv == null)
      lsrv = (LocalizationService) TurbineServices.getInstance().
         getService(LocalizationService.SERVICE_NAME);
  }

  @Override
  public void refresh(RunData data)
  {
    if(userLocale == null)
      userLocale = lsrv.getLocale(data.getRequest());
  }

  public String I(String key, Object param1, Object param2, Object param3)
  {
    String value = lsrv.getString(null, userLocale, key);
    return String.format(value, param1, param2, param3);
  }

  public String I(String key, Object param1, Object param2)
  {
    String value = lsrv.getString(null, userLocale, key);
    return String.format(value, param1, param2);
  }

  public String I(String key, Object param1)
  {
    String value = lsrv.getString(null, userLocale, key);
    return String.format(value, param1);
  }

  public String I(String key)
  {
    return lsrv.getString(null, userLocale, key);
  }

  public String IA(String key, Object param1, Object param2)
  {
    String value = lsrv.getString(null, userLocale, key);
    return "\"" + String.format(value, param1, param2) + "\"";
  }

  public String IA(String key, Object param1)
  {
    String value = lsrv.getString(null, userLocale, key);
    return "\"" + String.format(value, param1) + "\"";
  }

  public String IA(String key)
  {
    return "\"" + lsrv.getString(null, userLocale, key) + "\"";
  }

  public String F(String key, Object... params)
  {
    return lsrv.format(null, userLocale, key, params);
  }

  public String F(String key)
  {
    return lsrv.getString(null, userLocale, key);
  }

  public String FA(String key, Object... params)
  {
    return "\"" + lsrv.format(null, userLocale, key, params) + "\"";
  }

  public String FA(String key)
  {
    return "\"" + lsrv.getString(null, userLocale, key) + "\"";
  }
}
