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
package org.sirio2.beans;

import org.sirio2.services.token.TokenAuthItem;

/**
 * Ascoltatore eventi inserimento e rimozione oggetti da token.
 * Ha uno scopo e un funzionamento simile a {@link HttpSessionBindingListener}
 * @author Nicola De Nisco
 */
public interface CoreTokenBindingListener
{
  /**
   * Notifica inserimento in sessione.
   * Viene chiamata quando questo bean viene inserito in una sessione.
   * @param token
   */
  public void valueBound(TokenAuthItem token);

  /**
   * Notifica rimozione da sessione.
   * Viene chiamata quando questo bean viene rimosso da una sessione.
   * Questo include il caso di una sessione scaduta per timeout.
   * @param token
   */
  public void valueUnbound(TokenAuthItem token);
}
