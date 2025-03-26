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
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.LocaleUtils;
import org.sirio5.utils.CoreRunData;

/**
 * Interfaccia multilingua per RIGEL.
 * Determina la locale richiesta dal browser dall'oggetto RunData.
 * Le stringhe di Rigel verranno localizzate nella locale richiesta.
 *
 * @author Nicola De Nisco
 */
public class RigelHtmlI18n extends RigelDefaultI18n
{
  private Locale userLocale = null;

  public RigelHtmlI18n(HttpServletRequest request)
  {
    userLocale = lsrv.getLocale(request);
  }

  public RigelHtmlI18n(CoreRunData data)
  {
    userLocale = lsrv.getLocale(data.getRequest());
  }

  public RigelHtmlI18n(String locale)
  {
    userLocale = LocaleUtils.toLocale(locale);
  }

  @Override
  public Locale getUserLocale()
  {
    return userLocale;
  }

  @Override
  public String resolveGenericMessage(String defaultMessage)
  {
    return lsrv.getString(null, userLocale, defaultMessage);
  }
}
