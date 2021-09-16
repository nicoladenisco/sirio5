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

import org.apache.fulcrum.cache.Refreshable;

/**
 * Come per RefreshableCachedObject con un concetto di referesh all'estrazione.
 * Attenzione: questo oggetto va in refresh interno (chimata del metodo refresh)
 * solo se non viene estratto dalla cache per un tempo oltre il suo expire perchè
 * ad ogni estrazione dalla cache viene azzerato il tempo di permanenza.
 * Usare RefreshableCachedObject se questa caratteristica non è voluta.
 *
 * @author Nicola De Nisco
 */
public class CoreRefreshableCachedObject extends CoreCachedObject
{
  /** Serial Version UID */
  private static final long serialVersionUID = 727229797378897180L;
  /**
   * How long to wait before removing an untouched object from the cache.
   * Negative numbers mean never remove (the default).
   */
  private long timeToLive = -1;
  /**
   * The last time the Object was accessed from the cache.
   */
  private long lastAccess;

  /**
   * Constructor; sets the object to expire in the default time (30
   * minutes).
   *
   * @param o The object you want to cache.
   */
  public CoreRefreshableCachedObject(Object o)
  {
    super(o);
    lastAccess = System.currentTimeMillis();
  }

  /**
   * Constructor.
   *
   * @param o The object to cache.
   * @param expires How long before the object expires, in ms,
   * e.g. 1000 = 1 second.
   */
  public CoreRefreshableCachedObject(Object o, long expires)
  {
    super(o, expires);
    lastAccess = System.currentTimeMillis();
  }

  /**
   * Constructor.
   *
   * @param o The object to cache.
   * @param expires How long before the object expires, in ms e.g. 1000 = 1 second.
   * @param deletable se vero l
   */
  public CoreRefreshableCachedObject(Object o, long expires, boolean deletable)
  {
    super(o, expires, deletable);
    lastAccess = System.currentTimeMillis();
  }

  /**
   * Sets the timeToLive value
   *
   * @param timeToLive the new Value in milliseconds
   */
  public synchronized void setTTL(long timeToLive)
  {
    this.timeToLive = timeToLive;
  }

  /**
   * Gets the timeToLive value.
   *
   * @return The current timeToLive value (in milliseconds)
   */
  public synchronized long getTTL()
  {
    return timeToLive;
  }

  /**
   * Sets the last acccess time to the current time.
   * Viene chiamato ad ogni estrazione dalla cache.
   * L'oggetto diventa 'touched'.
   */
  public synchronized void touch()
  {
    lastAccess = System.currentTimeMillis();
  }

  /**
   * Returns true if the object hasn't been touched
   * in the previous TTL period.
   * L'oggetto diventa 'touched' se viene acceduto ameno
   * una volta dopo l'inserimento nella cache.
   * @return true if untouched
   */
  public synchronized boolean isUntouched()
  {
    if(timeToLive < 0)
      return false;

    if(lastAccess + timeToLive < System.currentTimeMillis())
      return true;
    else
      return false;
  }

  /**
   * Refresh the object and the created time.
   * Questo metodo viene chiamato se l'oggetto è scaduto (isStale() ritorna true)
   * al momento dell'estrazione oppure periodicamente durante il test di permanenza
   * nella cache.
   */
  public void refresh()
  {
    if(getContents() instanceof Refreshable)
    {
      Refreshable r = (Refreshable) getContents();
      synchronized(this)
      {
        created = System.currentTimeMillis();
        r.refresh();
      }
    }
  }
}
