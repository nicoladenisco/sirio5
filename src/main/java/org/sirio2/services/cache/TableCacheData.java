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
package org.sirio2.services.cache;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.rigel5.db.torque.PeerTransactAgent;
import org.sirio2.utils.SU;

/**
 * Dati di una tabella memorizzati nella cache.
 *
 * @author Nicola De Nisco
 */
public class TableCacheData
{
  public List lsValues = null;
  public List lsValuesUndeleted = null;
  public Map<ObjectKey, Persistent> mapValues = null;

  protected TableCacheData()
  {
  }

  /**
   * Carica i dati dalla tabella.
   * @param cls classe dell'oggetto Peer da popolare.
   * @throws Exception
   */
  protected void populateData(Class cls)
     throws Exception
  {
    lsValues = doSelect(cls);
    mapValues = new HashMap<>(lsValues.size());
    lsValuesUndeleted = new ArrayList(lsValues.size());

    for(int i = 0; i < lsValues.size(); i++)
    {
      Persistent val1 = (Persistent) lsValues.get(i);
      mapValues.put(val1.getPrimaryKey(), val1);

      ColumnAccessByName val2 = (ColumnAccessByName) lsValues.get(i);
      if(SU.parse(val2.getByName("StatoRec"), 0) < 10)
        lsValuesUndeleted.add(val1);
    }
  }

  protected List doSelect(Class cls)
     throws Exception
  {
    return PeerTransactAgent.executeReturnReadonly((con) ->
    {
      // estrae metodo doSelect dalla class del peer
      Method m = cls.getMethod("doSelect", Criteria.class, Connection.class);
      return (List) m.invoke(null, new Criteria(), con);
    });
  }

  /**
   * Iteratore sui record della tabella.
   * Se la tabella possiede STATO_REC può ritornare anche i soli record validi.
   * @param removeDeleted se vero ritorna solo quelli con STATO_REC valido.
   * @return
   */
  public Iterator getIterator(boolean removeDeleted)
  {
    return removeDeleted ? lsValuesUndeleted.iterator() : lsValues.iterator();
  }
}
