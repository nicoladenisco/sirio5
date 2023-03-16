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
package org.sirio5.services.token;

import java.util.*;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.om.security.User;
import org.sirio5.beans.CoreTokenBindingListener;
import static org.sirio5.services.security.CoreSecurity.ADMIN_ROLE;
import org.sirio5.utils.SU;

/**
 * Token di autenticazione per utente.
 * E' un po' il concetto della sessione http.
 * Ogni TokenAuthItem puo' mantenere memorizzati dei dati
 * associati a chiave alfanumerica attraverso le funzioni
 * setAttribute getAttribute removeAttribute.
 */
public class TokenAuthItem
{
  private final HashMap<String, Object> htAttr = new HashMap<>();
  private int userID = 0;
  private User usr = null;
  private String idClient, session;
  private java.util.Date logon = new java.util.Date();
  private java.util.Date lastAccess = new java.util.Date();
  private TurbineAccessControlList acl = null;

  public TokenAuthItem()
  {
  }

  public void clear()
  {
    removeAllAttributes();
  }

  @Override
  protected void finalize()
     throws Throwable
  {
    super.finalize();
    clear();
  }

  public String getIdClient()
  {
    return idClient;
  }

  public void setIdClient(String newIdClient)
  {
    idClient = newIdClient;
  }

  public void setLogon(java.util.Date newLogon)
  {
    logon = newLogon;
  }

  public java.util.Date getLogon()
  {
    return logon;
  }

  /**
   * Imposta attributo token.
   * @param nome nome dell'attributo
   * @param attr valore (può essere null)
   * @return eventuale valore precedente per l'attributo
   */
  public synchronized Object setAttribute(String nome, Object attr)
  {
    Object prev = removeAttribute(nome);
    if(attr == null)
      return prev;

    htAttr.put(nome, attr);

    if(attr instanceof CoreTokenBindingListener)
    {
      ((CoreTokenBindingListener) attr).valueBound(this);
    }

    return prev;
  }

  /**
   * Ritorna attributo token.
   * @param nome nome dell'attributo
   * @return valore o null se non presente
   */
  public synchronized Object getAttribute(String nome)
  {
    return htAttr.get(nome);
  }

  /**
   * Rimuove attributo da token.
   * @param nome nome dell'attributo
   * @return valore rimosso o null se non presente
   */
  public synchronized Object removeAttribute(String nome)
  {
    Object attr = htAttr.get(nome);
    if(attr == null)
      return null;

    if(attr instanceof CoreTokenBindingListener)
    {
      ((CoreTokenBindingListener) attr).valueUnbound(this);
    }

    return htAttr.remove(nome);
  }

  public synchronized void removeAllAttributes()
  {
    for(Map.Entry<String, Object> entry : htAttr.entrySet())
    {
      String key = entry.getKey();
      Object attr = entry.getValue();

      if(attr != null && (attr instanceof CoreTokenBindingListener))
      {
        ((CoreTokenBindingListener) attr).valueUnbound(this);
      }
    }

    htAttr.clear();
  }

  public Iterator<String> getAttributeNames()
  {
    return htAttr.keySet().iterator();
  }

  public void setLastAccess(java.util.Date newLastAccess)
  {
    lastAccess = newLastAccess;
  }

  public java.util.Date getLastAccess()
  {
    return lastAccess;
  }

  public User getUsr()
  {
    return usr;
  }

  public void setUsr(User usr)
  {
    this.usr = usr;
  }

  public int getUserID()
  {
    return userID;
  }

  public void setUserID(int userID)
  {
    this.userID = userID;
  }

  public String getSession()
  {
    return session;
  }

  public void setSession(String session)
  {
    this.session = session;
  }

  public TurbineAccessControlList getAcl()
  {
    return acl;
  }

  public void setAcl(TurbineAccessControlList acl)
  {
    this.acl = acl;
  }

  /**
   * Ritorna vero se l'utente è l'amministratore di sistema.
   * @return vero se utente 'turbine'
   * @throws Exception
   */
  public boolean isAdmin()
     throws Exception
  {
    return userID == 0 || acl.hasRole(ADMIN_ROLE);
  }

  /**
   * Verifica una singola permission.
   * @param permissions una o più permission da verificare
   * @return vero se TUTTE le permission sono verificate per l'utente corrente
   * @throws Exception
   */
  public boolean isAuthorized(String permissions)
     throws Exception
  {
    return isAdmin() || acl.hasPermission(permissions);
  }

  /**
   * Verifica tutte le permission specificate.
   * Verifica una o un gruppo di permission separate da virgola.
   * @param permissions una o più permission da verificare
   * @return vero se TUTTE le permission sono verificate per l'utente corrente
   * @throws Exception
   */
  public boolean isAuthorizedAll(String permissions)
     throws Exception
  {
    if(isAdmin())
      return true;

    StringTokenizer stk = new StringTokenizer(permissions, ",");

    while(stk.hasMoreTokens())
    {
      String perm = SU.okStrNull(stk.nextToken());
      if(perm != null && !acl.hasPermission(perm))
        return false;
    }

    return true;
  }

  /**
   * Verifica una delle permission specificate.
   * Verifica una o un gruppo di permission separate da virgola.
   * @param permissions una o più permission da verificare
   * @return vero se ALMENO UNA delle permission sono verificate per l'utente corrente
   * @throws Exception
   */
  public boolean isAuthorizedAny(String permissions)
     throws Exception
  {
    if(isAdmin())
      return true;

    StringTokenizer stk = new StringTokenizer(permissions, ",");

    while(stk.hasMoreTokens())
    {
      String perm = SU.okStrNull(stk.nextToken());
      if(perm != null && acl.hasPermission(perm))
        return true;
    }

    return false;
  }
}
