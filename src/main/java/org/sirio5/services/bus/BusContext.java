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
package org.sirio5.services.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.commonlib5.utils.ArrayOper;
import org.rigel5.RigelI18nInterface;
import org.sirio5.rigel.RigelHtmlI18n;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SirioGenericContext;

/**
 * Dati trasportati sul bus insieme al messaggio.
 *
 * @author Nicola De Nisco
 */
public class BusContext extends SirioGenericContext
{
  protected final List<BusPostActionListener> postListener = new ArrayList<>();

  public BusContext()
  {
  }

  public BusContext(Map<? extends String, ? extends Object> m)
  {
    super(m);
  }

  public BusContext(SirioGenericContext ctx)
  {
    super(ctx);
  }

  public BusContext(CoreRunData data)
  {
    super();
    setI18n(new RigelHtmlI18n(data));
  }

  public BusContext(RigelI18nInterface i18n)
  {
    super();
    setI18n(i18n);
  }

  public BusContext(Object... params)
  {
    super();
    appendPair(params);
  }

  public BusContext(CoreRunData data, Object... params)
  {
    super();
    setI18n(new RigelHtmlI18n(data));
    appendPair(params);
  }

  public BusContext(RigelI18nInterface i18n, Object... params)
  {
    super();
    setI18n(i18n);
    appendPair(params);
  }

  public BusContext appendPair(Object... params)
  {
    Map m = ArrayOper.asMapFromPair(params);
    putAll(m);
    return this;
  }

  @Override
  public BusContext append(String key, Object value)
  {
    put(key, value);
    return this;
  }

  public BusContext addPostActionListener(BusPostActionListener listener)
  {
    postListener.add(listener);
    return this;
  }
}
