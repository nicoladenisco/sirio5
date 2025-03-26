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

import java.util.Locale;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.turbine.services.TurbineServices;
import org.rigel5.RigelI18nInterface;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.hTable;
import org.sirio5.services.localization.CoreLocalizationService;

/**
 * Interfaccia multilingua per RIGEL.
 * Le stringhe vengono tradotte secondo la locale di default.
 *
 * @author Nicola De Nisco
 */
public class RigelDefaultI18n implements RigelI18nInterface
{
  protected LocalizationService lsrv = null;

  public RigelDefaultI18n()
  {
    lsrv = (LocalizationService) TurbineServices.getInstance().
       getService(LocalizationService.SERVICE_NAME);
  }

  @Override
  public String localizeTableCaption(hTable table,
     RigelTableModel model, RigelColumnDescriptor column,
     int numCol, String caption)
  {
    return resolveGenericMessage(caption);
  }

  @Override
  public String getCaptionButtonCerca()
  {
    return resolveGenericMessage("Cerca");
  }

  @Override
  public String getCaptionButtonPulisci()
  {
    return resolveGenericMessage("Pulisci");
  }

  @Override
  public String resolveGenericMessage(String defaultMessage)
  {
    return lsrv.getString(null, null, defaultMessage);
  }

  @Override
  public String msg(String defaultMessage)
  {
    return resolveGenericMessage(defaultMessage);
  }

  @Override
  public String msg(String defaultMessage, Object... args)
  {
    String value = resolveGenericMessage(defaultMessage);
    return String.format(value, args);
  }

  @Override
  public Locale getUserLocale()
  {
    if(lsrv instanceof CoreLocalizationService)
    {
      return ((CoreLocalizationService) lsrv).getDisplayLocale();
    }

    return Locale.getDefault();
  }
}
