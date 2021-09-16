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
package org.sirio5.services.localization;

/**
 * Gestione dei messaggi per internazionalizzazione.
 *
 * @author Nicola De Nisco
 */
public class INT
{
  private static CoreLocalizationService service = null;

  protected static void setService(CoreLocalizationService srv)
  {
    service = srv;
  }

  public static String resolve(String defaultMessage)
  {
    return service == null ? defaultMessage : service.getString(null, null, defaultMessage);
  }

  public static String I(String msg)
  {
    return resolve(msg);
  }

  public static String I(String formatString, Object... objects)
  {
    return String.format(resolve(formatString), objects);
  }
}
