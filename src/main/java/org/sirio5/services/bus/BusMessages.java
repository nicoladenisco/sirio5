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
package org.sirio5.services.bus;

/**
 * Tabella statica dei messaggi trasportati dal bus.
 * @author Nicola De Nisco
 */
public class BusMessages
{
  public static final int IDLE_10_MINUTES = 10;
  public static final int IDLE_30_MINUTES = 11;
  public static final int IDLE_60_MINUTES = 12;

  public static final int USER_LOGON = 101;
  public static final int USER_LOGOUT = 102;

  public static final int GENERIC_OBJECT_SAVED = 201;
  public static final int GENERIC_OBJECTS_SAVED = 202;

  // varie
  public static final int CMD_UPDATE_PEER = 110;
  public static final int CLEAR_GLOBAL_CACHE = 120;
  public static final int CLEAR_GLOBAL_CACHE_CLASS = 121;
  public static final int RIGEL_XML_LIST_RELOADED = 122;
  public static final int ASSOCIATION_LIST_CHANGED = 130;

  // gestione allarmi
  public static final int ALLARM_ADDED = 2001;
  public static final int ALLARM_SIGNED = 2002;
}
