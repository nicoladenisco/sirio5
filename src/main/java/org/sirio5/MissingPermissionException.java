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
package org.sirio5;

import org.sirio5.services.localization.INT;
import org.sirio5.utils.CoreRunData;

/**
 * Eccezione sollevata per mancanza dei permessi dell'utente.
 *
 * @author Nicola De Nisco
 */
public class MissingPermissionException extends RuntimeException
{
  private String permessi;
  private boolean anyOrAll;

  public MissingPermissionException(CoreRunData data, String permessi, boolean anyOrAll)
  {
    super(INT.I("Permessi non validi: %s %s", anyOrAll ? "ALL" : "ANY", permessi));
    this.permessi = permessi;
    this.anyOrAll = anyOrAll;
  }
}
