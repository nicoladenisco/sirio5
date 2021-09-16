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

import java.io.File;

/**
 * Wrapper l'inserimento di cache di un generico file.
 * Quando questo oggetto viene cancellato dalla cache
 * il rispettivo file associato viene egualmente
 * cancellato dal disco.
 * Tutti i files inseriti sono marcati con deleteonexit
 * in modo da cancellarli in caso di uscita dalla jvm.
 *
 * @author Nicola De Nisco
 */
public class FileCacheObject extends CoreCachedObject
{
  public FileCacheObject(File o, long expires, boolean deletable)
  {
    super(o, expires, deletable);
  }

  public FileCacheObject(File o, long expires)
  {
    super(o, expires);
  }

  public FileCacheObject(File o)
  {
    super(o);
  }

  @Override
  public synchronized void deletingExpired()
  {
    ((File)getContents()).delete();
  }
}
