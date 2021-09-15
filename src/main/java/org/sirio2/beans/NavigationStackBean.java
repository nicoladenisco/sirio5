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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.commonlib5.utils.ArrayMap;
import org.rigel5.HtmlUtils;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Stack per la navigazione fra pagine.
 *
 * @author Nicola De Nisco
 */
final public class NavigationStackBean extends CoreBaseBean
{
  // costanti
  public static final String BEAN_KEY = "NavigationStackBean:BEAN_KEY";
  private LinkedList<NavInfo> uriStack = new LinkedList<>();

  public static class NavInfo
  {
    public String uri, descrizione;
    public Map<String, String> queryParameters;

    @Override
    public boolean equals(Object obj)
    {
      if(!(obj instanceof NavInfo))
        return false;
      return SU.isEqu(uri, ((NavInfo) obj).uri);
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 59 * hash + Objects.hashCode(this.uri);
      return hash;
    }
  }

// <editor-fold defaultstate="collapsed" desc="Getter/Setter">
  // TODO: inserire qui i getter e setter
  // di eventuali proprietà del bean
// </editor-fold>
  /**
   * Recupera dalla request la url di richiesta.
   * Le variabili 'action' e 'command' vengono rimosse.
   * @param request richiesta http
   * @return la ni completa della richiesta
   */
  public NavInfo getFullURL(HttpServletRequest request)
  {
    NavInfo rv = new NavInfo();

    rv.uri = request.getRequestURL().toString();
    String queryString = request.getQueryString();

    if(queryString != null)
    {
      rv.queryParameters = new ArrayMap<>();
      Map<String, String> m = SU.string2Map(queryString, "&", true);
      for(Map.Entry<String, String> entrySet : m.entrySet())
      {
        String key = entrySet.getKey();
        String value = entrySet.getValue();
        if("action".equals(key))
          continue;
        if("command".equals(key))
          continue;

        rv.queryParameters.put(key, value);
      }
    }

    return rv;
  }

  public boolean pushUri(CoreRunData data)
  {
    NavInfo ni = getFullURL(data.getRequest());
    if(ni == null)
      return false;

    ni.descrizione = "";

    push(ni);
    return true;
  }

  public boolean pushUri(CoreRunData data, String uri)
  {
    NavInfo ni = getFullURL(data.getRequest());
    if(ni == null)
      return false;

    ni.uri = uri;
    ni.descrizione = "";

    push(ni);
    return true;
  }

  public boolean pushUri(CoreRunData data, String uri, String descrizione)
  {
    NavInfo ni = getFullURL(data.getRequest());
    if(ni == null)
      return false;

    ni.uri = uri;
    ni.descrizione = descrizione;

    push(ni);
    return true;
  }

  public boolean pushUri(String uri, String descrizione, Map<String, String> parameters)
  {
    NavInfo ni = new NavInfo();
    ni.uri = uri;
    ni.descrizione = descrizione;
    ni.queryParameters = parameters;

    push(ni);
    return true;
  }

  public boolean popUri(CoreRunData data)
  {
    if(uriStack.isEmpty())
      return false;

    NavInfo ni = uriStack.pop();
    String uri = HtmlUtils.mergeUrl(ni.uri, ni.queryParameters);
    data.setStatusCode(302);
    data.setRedirectURI(uri);
    return true;
  }

  public boolean peekUri(CoreRunData data)
  {
    if(uriStack.isEmpty())
      return false;

    NavInfo ni = uriStack.peek();
    String uri = HtmlUtils.mergeUrl(ni.uri, ni.queryParameters);
    data.setStatusCode(302);
    data.setRedirectURI(uri);
    return true;
  }

  public Iterator<NavInfo> uriIterator()
  {
    return uriStack.descendingIterator();
  }

  public List<NavInfo> getList()
  {
    return new ArrayList<>(uriStack);
  }

  public boolean isEmpty()
  {
    return uriStack.isEmpty();
  }

  public int size()
  {
    return uriStack.size();
  }

  public void push(NavInfo ni)
  {
    int pos = uriStack.indexOf(ni);

    if(pos != -1)
    {
      while(pos-- >= 0)
        uriStack.remove();
    }

    uriStack.push(ni);
  }

  public NavInfo pop()
  {
    return uriStack.pop();
  }

  public NavInfo peek()
  {
    return uriStack.peek();
  }

  public void clear()
  {
    uriStack.clear();
  }

  /**
   * Ritorna a url precedente.
   * Scarta la cima dello stack (pagina attualmente visualizzata)
   * e recupera senza rimuoverla la entry precedente che è la
   * pagina di ritorno. Effettua un redirect impostando i relativi
   * campi dell'oggetto rundata.
   * @param data
   * @return vero se il ritorno è stato attivato
   */
  public boolean return2(CoreRunData data)
  {
    if(uriStack.size() <= 1)
      return false;

    // scarta la cima dello stack che è la ni attualmente visualizzata
    if(!uriStack.isEmpty())
      uriStack.pop();

    if(peekUri(data))
      return true;

    return false;
  }

  /**
   * Estrae il bean dalla sessione e ritorna a url precedete.
   * @param data
   * @return vero se il ritorno è stato attivato
   * @throws Exception
   */
  public static boolean ret2Session(CoreRunData data)
     throws Exception
  {
    return BeanFactory.getFromSession(data, NavigationStackBean.class).return2(data);
  }
}
