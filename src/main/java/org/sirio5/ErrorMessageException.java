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

import java.util.List;
import org.sirio5.utils.SU;

/**
 * Questa eccezione viene sollevata per abortire una operazione
 * e lasciare un messaggio all'utente nell'apposito riquadro rosso.
 * CoreBaseAction e CoreBaseSceen la intercettano visualizzando
 * il messaggio quando non si tratta di un errore di applicazione.
 *
 * @author Nicola De Nisco
 */
public class ErrorMessageException extends Exception
{
  public ErrorMessageException(String message)
  {
    super(message);
  }

  public ErrorMessageException(List<String> lsMsg)
  {
    super(SU.join(lsMsg.iterator(), "<br>", null));
  }

  public static void throwErrorMessageException(List<String> lsMsg)
     throws ErrorMessageException
  {
    throw new ErrorMessageException(SU.join(lsMsg.iterator(), "<br>", null));
  }
}
