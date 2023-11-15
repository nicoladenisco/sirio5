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

import java.util.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.uri.TemplateURI;
import org.commonlib5.utils.StringOper;

/**
 * Tool per l'accesso ai dati di configurazione
 * direttamente dalle pagine Velocity.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class ConfigurationReadTool implements ApplicationTool
{
  Configuration cfg = null;

  @Override
  public void init(Object data)
  {
    cfg = Turbine.getConfiguration();
  }

  @Override
  public void refresh()
  {
  }

  public String getString(String key, String defVal)
  {
    return cfg.getString(key, defVal);
  }

  public int getInt(String key, int defVal)
  {
    return cfg.getInt(key, defVal);
  }

  public boolean getBoolean(String key, boolean defVal)
  {
    return cfg.getBoolean(key, defVal);
  }

  public List getList(String key)
  {
    return cfg.getList(key);
  }

  public String getStrings(String key, String sep)
  {
    String[] vals = cfg.getStringArray(key);
    return StringUtils.join(vals, sep);
  }

  public String getHomeLink(RunData data)
  {
    TemplateURI uri = new TemplateURI(data, cfg.getString("template.homepage", "Index.vm"));
    return uri.getRelativeLink();
  }

  public boolean isButtonEnabled(String formID, String butLabel)
  {
    String key = butLabel.replace(' ', '_');
    return cfg.getBoolean(formID + ".menu.but." + key, true);
  }

  public boolean isBoolTrue(String key)
  {
    return isBoolTrueDef(key, false);
  }

  public boolean isBoolTrueDef(String key, boolean defVal)
  {
    String val = cfg.getString(key);
    return StringOper.checkTrueFalse(val, defVal);
  }
}
