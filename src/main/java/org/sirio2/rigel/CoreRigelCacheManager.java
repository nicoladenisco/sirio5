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
package org.sirio2.rigel;

import java.util.List;
import org.apache.fulcrum.cache.CachedObject;
import org.rigel5.RigelCacheManager;
import org.rigel5.table.ForeignDataHolder;
import org.sirio2.CoreConst;
import org.sirio2.services.cache.CACHE;

/**
 * Gestore della cache per Rigel.
 * Memorizza i dati della cache di Rigel attraverso
 * il servizio GlobalCacheService.
 *
 * @author Nicola De Nisco
 */
public class CoreRigelCacheManager implements RigelCacheManager
{
  public static final String RIGEL_CACHE_SECTION = "RIGEL_CACHE_SECTION";
  public static final long RIGEL_CACHE_EXPIRE = 30 * CoreConst.ONE_MINUTE_MILLIS;

  @Override
  public List<ForeignDataHolder> getForeignDataList(String chiave)
  {
    return (List<ForeignDataHolder>) getGenericCachedData("l1:" + chiave);
  }

  @Override
  public void putForeignDataList(String chiave, List<ForeignDataHolder> ls)
  {
    putGenericCachedData("l1:" + chiave, ls);
  }

  @Override
  public List<ForeignDataHolder> getDataComboColonnaAttached(String chiave)
  {
    return (List<ForeignDataHolder>) getGenericCachedData("l2:" + chiave);
  }

  @Override
  public void putDataComboColonnaAttached(String chiave, List<ForeignDataHolder> ls)
  {
    putGenericCachedData("l2:" + chiave, ls);
  }

  @Override
  public List<ForeignDataHolder> getDataComboColonnaSelf(String chiave)
  {
    return (List<ForeignDataHolder>) getGenericCachedData("l3:" + chiave);
  }

  @Override
  public void putDataComboColonnaSelf(String chiave, List<ForeignDataHolder> ls)
  {
    putGenericCachedData("l3:" + chiave, ls);
  }

  @Override
  public Long getRecordCount(String chiave)
  {
    return (Long) getGenericCachedData("l4:" + chiave);
  }

  @Override
  public void putRecordCount(String chiave, long value)
  {
    putGenericCachedData("l4:" + chiave, value);
  }

  @Override
  public Object getGenericCachedData(String chiave)
  {
    return CACHE.getContentQuiet(RIGEL_CACHE_SECTION, chiave);
  }

  @Override
  public void putGenericCachedData(String chiave, Object data)
  {
    CACHE.addObject(RIGEL_CACHE_SECTION, chiave, new CachedObject(data, RIGEL_CACHE_EXPIRE));
  }

  /**
   * Rimuove dalla cache i dati che referenziano la tabella indicata.
   * @param nomeTabella nome della tabella aggiornata
   */
  @Override
  public void purgeTabella(String nomeTabella)
  {
    String upNomeTab = " FROM " + nomeTabella.toUpperCase() + " ";
    CACHE.removeAllObjects(RIGEL_CACHE_SECTION, (key, value) -> key.toUpperCase().contains(upNomeTab));
  }
}
