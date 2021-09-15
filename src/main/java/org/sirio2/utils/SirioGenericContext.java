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
package org.sirio2.utils;

import java.io.Serializable;
import java.util.*;
import org.rigel5.RigelI18nInterface;
import org.sirio2.rigel.RigelDefaultI18n;

/**
 * Context generico.
 * Il context è una scatola flessibile dove inserire parametri.
 * Viene usato per l'implemetazione di servizi il cui numero di parametri
 * può variare in base alle implementazioni e comunque non fisso nel tempo.
 *
 * @author Nicola De Nisco
 */
public class SirioGenericContext extends HashMap<String, Object>
   implements Serializable
{
  private transient RigelI18nInterface i18n = new RigelDefaultI18n();
  private StringBuilder message = new StringBuilder();
  protected boolean mustSerializable = false;

  public SirioGenericContext()
  {
  }

  public SirioGenericContext(Map<? extends String, ? extends Object> m)
  {
    super(m);
  }

  public SirioGenericContext(SirioGenericContext ctx)
  {
    super(ctx);
    this.i18n = ctx.i18n;
    this.message.append(ctx.message);
    this.mustSerializable = ctx.mustSerializable;
  }

  public RigelI18nInterface getI18n()
  {
    return i18n;
  }

  public void setI18n(RigelI18nInterface i18n)
  {
    this.i18n = i18n;
  }

  @Override
  public Object put(String key, Object value)
  {
    if(value == null)
      return remove(key);

    if(mustSerializable)
    {
      if(!(value instanceof Serializable))
        throw new RuntimeException(i18n.msg(
           "Il parametro '%s' non implementa Serializable: non puo essere inserito nel context.", key));

      if(value instanceof Collection)
        testSerializable(key, (Collection) value);
    }

    return super.put(key, value);
  }

  public Object putAsStringNotEmpty(String key, Object value)
  {
    if(value == null)
      return remove(key);

    if((value = SU.okStrNull(value)) == null)
      return remove(key);

    return super.put(key, value);
  }

  public void testSerializable(String key, Collection collection)
  {
    for(Object value : collection)
    {
      if(!(value instanceof Serializable))
        throw new RuntimeException(i18n.msg(
           "La collezione del parametro '%s' contiene un oggetto che non implementa Serializable: non puo essere inserito nel context.", key));

      if(value instanceof Collection)
        testSerializable(key, (Collection) value);
    }
  }

  public boolean isMustSerializable()
  {
    return mustSerializable;
  }

  public void setMustSerializable(boolean mustSerializable)
  {
    this.mustSerializable = mustSerializable;
  }

  public void putAllWithoutCollection(Map params)
  {
    params.forEach((k, v) ->
    {
      if(!(v instanceof Collection))
      {
        this.put(k.toString(), v);
      }
    });
  }

  public Object getNotNull(String key)
  {
    Object rv = get(key);
    if(rv == null)
      throw new RuntimeException(i18n.msg("Il parametro '%s' non è presente nel context.", key));

    return rv;
  }

  public Object get(String key, Object defVal)
  {
    Object rv = get(key);
    if(rv == null)
      return defVal;

    return rv;
  }

  public Object getNotNullAndClear(String key)
  {
    Object rv = get(key);
    if(rv == null)
      throw new RuntimeException(i18n.msg("Il parametro '%s' non è presente nel context.", key));

    remove(key);
    return rv;
  }

  public Object getAndClear(String key, Object defVal)
  {
    Object rv = get(key);
    if(rv == null)
      return defVal;

    remove(key);
    return rv;
  }

  public Object getAndClear(String key)
  {
    Object rv = get(key);
    if(rv == null)
      return null;

    remove(key);
    return rv;
  }

  public SirioGenericContext clearMessage()
  {
    message = new StringBuilder();
    return this;
  }

  public SirioGenericContext setMessage(String msg)
  {
    message = new StringBuilder(msg);
    return this;
  }

  public SirioGenericContext addMessage(String msg)
  {
    message.append(msg);
    return this;
  }

  public SirioGenericContext addMessages(List objs, String separator, String delimiter)
  {
    message.append(SU.join(objs.iterator(), separator, delimiter));
    return this;
  }

  public String getMessage()
  {
    return message.toString();
  }

  public boolean haveMessage()
  {
    return message.length() > 0;
  }

  public SirioGenericContext append(String br)
  {
    message.append(br);
    return this;
  }

  public SirioGenericContext setMessagei18n(String key)
  {
    setMessage(i18n.msg(key));
    return this;
  }

  public SirioGenericContext setMessagei18n(String key, Object... params)
  {
    setMessage(i18n.msg(key, params));
    return this;
  }

  public SirioGenericContext addMessagei18n(String key)
  {
    addMessage(i18n.msg(key));
    return this;
  }

  public SirioGenericContext addMessagei18n(String key, Object... params)
  {
    addMessage(i18n.msg(key, params));
    return this;
  }

  /**
   * Ritorna messaggio localizzato.
   * Usa il messaggio origine come chiave
   * per cercare il messaggio nella traduzione attiva.
   * @param defaultMessage messaggio chiave
   * @return corrispondente localizzato o il messaggio chiave se non trovato
   */
  public String msg(String defaultMessage)
  {
    return i18n.msg(defaultMessage);
  }

  /**
   * Ritorna messaggio localizzato.
   * Usa il messaggio origine come chiave
   * per cercare il messaggio nella traduzione attiva.
   * @param defaultMessage messaggio chiave
   * @param args argomenti di formattazione
   * @return corrispondente localizzato o il messaggio chiave se non trovato
   */
  public String msg(String defaultMessage, Object... args)
  {
    return i18n.msg(defaultMessage, args);
  }

  public String getAsString(String key)
  {
    return SU.okStr(get(key));
  }

  public String getAsStringNull(String key)
  {
    return SU.okStrNull(get(key));
  }

  public int getAsInt(String key)
  {
    return getAsInt(key, 0);
  }

  public double getAsDouble(String key)
  {
    return getAsDouble(key, 0.0);
  }

  public boolean getAsBoolean(String key)
  {
    return getAsBoolean(key, false);
  }

  public Date getAsDate(String key)
  {
    return getAsDate(key, null);
  }

  public String getAsString(String key, String defVal)
  {
    return SU.okStr(get(key), defVal);
  }

  public int getAsInt(String key, int defVal)
  {
    Object val = get(key);
    if(val == null)
      return defVal;

    return val instanceof Number ? ((Number) val).intValue() : SU.parse(val, defVal);
  }

  public double getAsDouble(String key, double defVal)
  {
    Object val = get(key);
    if(val == null)
      return defVal;

    return val instanceof Number ? ((Number) val).doubleValue() : SU.parse(val, defVal);
  }

  public boolean getAsBoolean(String key, boolean defVal)
  {
    Object val = get(key);
    if(val == null)
      return defVal;

    return val instanceof Boolean ? ((Boolean) val) : SU.checkTrueFalse(val, defVal);
  }

  public Date getAsDate(String key, Date defVal)
  {
    Object val = get(key);
    if(val == null)
      return defVal;

    return val instanceof Date ? ((Date) val) : DT.parseIsoFull(getAsString(key), defVal);
  }

  public Collection getAsCollection(String key)
  {
    return (Collection) get(key);
  }

  public List getAsList(String key)
  {
    return (List) get(key);
  }

  public String getAsStringByList(String key)
  {
    return getAsStringByList(key, ':');
  }

  public String getAsStringByList(String key, char separator)
  {
    try
    {
      List emailList = getAsList(key);
      return SU.join(emailList.iterator(), separator);
    }
    catch(Exception ex)
    {
      return getAsString(key);
    }
  }

  /**
   * Inserisce un oggetto unico e anonimo.
   * La chiave verrà ricavata dal nome della classe.
   * Si può utilizzare getParam per il recupero.
   * @param obj
   * @return
   */
  public SirioGenericContext putParam(Object obj)
  {
    put(obj.getClass().getName(), obj);
    return this;
  }

  /**
   * Ritorna il primo elemento che corrisponde alla classe richiesta.
   * @param <T> un qualsiasi derivato di Object
   * @param type classe cercata
   * @return l'oggetto corrispondente o null
   */
  public <T> T getParam(Class<T> type)
  {
    return getParam(type, null);
  }

  /**
   * Ritorna il primo elemento che corrisponde alla classe richiesta.
   * @param <T> un qualsiasi derivato di Object
   * @param type classe cercata
   * @param defval valore di default
   * @return l'oggetto corrispondente oppure defval
   */
  public <T> T getParam(Class<T> type, T defval)
  {
    Collection<Object> valori = values();

    for(Object o : valori)
    {
      if(o != null && type.isAssignableFrom(o.getClass()))
        return type.cast(o);
    }

    return defval;
  }

  /**
   * Ritorna l'elemento richiesto convertendolo opportunamente.
   * @param <T> un qualsiasi derivato di Object
   * @param key chiave di ricerca
   * @param type classe cercata
   * @return l'oggetto corrispondente o null
   */
  public <T> T getParam(String key, Class<T> type)
  {
    return getParam(key, type, null);
  }

  /**
   * Ritorna l'elemento richiesto convertendolo opportunamente.
   * @param <T> un qualsiasi derivato di Object
   * @param key chiave di ricerca
   * @param type classe cercata
   * @param defval valore di default se oggetto non presente nell'array o tipo non corrispondente
   * @return l'oggetto corrispondente oppure defval
   */
  public <T> T getParam(String key, Class<T> type, T defval)
  {
    Object o = get(key);

    if(o != null && type.isAssignableFrom(o.getClass()))
      return type.cast(o);

    return defval;
  }

  public SirioGenericContext append(String key, Object value)
  {
    put(key, value);
    return this;
  }
}
