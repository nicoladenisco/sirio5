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
package org.sirio5.services.cache;

import org.apache.fulcrum.cache.CachedObject;

/**
 * Oggetto wrapper per il contenuto della cache.
 *
 * @author Nicola De Nisco
 */
public class CoreCachedObject extends CachedObject
{
  protected boolean deletable = true;

  /**
   * Constructor; sets the object to expire in the default time (30
   * minutes).
   *
   * @param o The object you want to cache.
   */
  public CoreCachedObject(Object o)
  {
    super(o);
    deletable = true;
  }

  /**
   * Constructor.
   *
   * @param o The object to cache.
   * @param expires How long before the object expires, in ms,
   * e.g. 1000 = 1 second.
   */
  public CoreCachedObject(Object o, long expires)
  {
    super(o, expires);
    deletable = true;
  }

  /**
   * Constructor.
   *
   * @param o The object to cache.
   * @param expires How long before the object expires, in ms, (e.g. 1000 = 1 second.)
   * @param deletable se vero l'oggetto potra essere rimosso dalla cache
   */
  public CoreCachedObject(Object o, long expires, boolean deletable)
  {
    super(o, expires);
    this.deletable = deletable;
  }

  /**
   * @return the deletable
   */
  public synchronized boolean isDeletable()
  {
    return deletable;
  }

  /**
   * @param deletable the deletable to set
   */
  public synchronized void setDeletable(boolean deletable)
  {
    this.deletable = deletable;
  }

  /**
   * Questa funzione viene chiamata dal gestore della cache
   * quando questo oggetto sta per essere rimosso dalla cache.
   * Eventuali risorse associate possono essere distrutte qui
   * (per esempio un file collegato).
   */
  public synchronized void deletingExpired()
  {
  }

  /**
   * Rende questa entry come se fosse stata appena
   * creata, ovvero il gestore della cache ricomincia
   * a contare il suo TTL da zero.
   */
  public synchronized void refreshEntry()
  {
    this.created = System.currentTimeMillis();
  }

  /**
   * Ritorna vero se questo oggetto e' scaduto.
   * Se il flag deletable e' vero questo oggetto non scade mai.
   * @return vero se scaduto (verra' rimosso dalla cache)
   */
  @Override
  public synchronized boolean isStale()
  {
    // se non e' cancellabile non scade mai
    if(!deletable)
      return false;

    return super.isStale();
  }

  /**
   * Ritorna il tempo trascorso dalla creazione.
   * @return tempo in millisecondi
   */
  public long getElapsedTime()
  {
    return System.currentTimeMillis() - created;
  }

  /**
   * Ritorna il tempo rimanente prima della scadenza (TTL).
   * @return tempo in millisecondi
   */
  public long getExpireTime()
  {
    return getExpires() - getElapsedTime();
  }

  @Override
  public String toString()
  {
    return getContents() == null ? "NULL" : getContents().toString();
  }
}
