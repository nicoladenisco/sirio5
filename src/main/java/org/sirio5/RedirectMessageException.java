/*
 * Copyright (C) 2024 Nicola De Nisco
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

import java.net.URI;

/**
 * Eccezione specializzata per redirezione ad altra pagina.
 * Vedi @CoreBaseScreen per l'utilizzo.
 *
 * @author Nicola De Nisco
 */
public class RedirectMessageException extends Exception
{
  private final URI uri;

  public RedirectMessageException(String string, URI uri)
  {
    super(string);
    this.uri = uri;
  }

  public RedirectMessageException(String string, Throwable thrwbl, URI uri)
  {
    super(string, thrwbl);
    this.uri = uri;
  }

  public RedirectMessageException(Throwable thrwbl, URI uri)
  {
    super(thrwbl);
    this.uri = uri;
  }

  public URI getUri()
  {
    return uri;
  }
}
