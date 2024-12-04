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
package org.sirio5.services.cache;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collection;
import org.apache.torque.om.Persistent;

/**
 * Caricamento ottimizzato di tabelle collegate.
 *
 * @author Nicola De Nisco
 * @param <T> Tipo di oggetti recuperati
 * @param <O> Tipo di oggetti origine
 */
public interface TableRelationLink<T extends Persistent, O extends Persistent>
{
  /**
   * Recupera metodo per chiave primaria.
   * A causa del cambio nome in Torque 5.1 e successivi
   * qui vengono cercati entrambi i nomi utilizzati nelle varie versioni di Torque.
   * @param targetPeerClass
   * @return
   */
  public default Method getMetodPrimary(Class targetPeerClass)
  {
    Method m;

    if((m = getMetodPrimary("retrieveByPKs", targetPeerClass)) == null)
      if((m = getMetodPrimary("retrieveByObjectKeys", targetPeerClass)) == null)
        throw new RuntimeException("L'oggetto non implementa il recupero da chiavi primarie.");

    return m;
  }

  public default Method getMetodPrimary(String name, Class targetPeerClass)
  {

    return getMetodQuiet(name, targetPeerClass, Collection.class, Connection.class);
  }

  public default Method getMetodQuiet(String name, Class targetPeerClass, Class... parametri)
  {
    try
    {
      return targetPeerClass.getMethod(name, parametri);
    }
    catch(NoSuchMethodException noSuchMethodException)
    {
      return null;
    }
    catch(SecurityException securityException)
    {
      throw securityException;
    }
  }
}
