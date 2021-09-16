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
package org.sirio5.modules.tools;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.security.SecurityService;
import static org.sirio5.services.security.CoreSecurity.ADMIN_ROLE;
import org.sirio5.utils.SU;

/**
 * Tool per la verifica di permission dell'utente.
 * Consente alle .vm di testare direttamente i permessi
 * dell'utente, accedendo ai metodi di CoreSecurity.
 * Questo tool va registrato nel .properties come authorized
 * in modo che venga inzializzato dopo il login utente
 * e che ogni utente ne abbia una copia separata:
 * <code>
 * tool.authorized.perm=org.sirio2.modules.tools.PermissionTool
 * </code>
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class PermissionTool implements ApplicationTool
{
  private SecurityService security;
  private User user = null;
  private TurbineAccessControlList acl = null;

  @Override
  public void init(Object data)
  {
    if(data instanceof User)
    {
      this.user = (User) data;

      try
      {
        security = (SecurityService) TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);
        acl = security.getACL(user);
      }
      catch(Throwable ex)
      {
        Logger.getLogger(PermissionTool.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void refresh()
  {
  }

  /**
   * Ritorna vero se l'utente è l'amministratore di sistema.
   * @return vero se utente 'turbine'
   * @throws Exception
   */
  public boolean isAdmin()
     throws Exception
  {
    return SU.isEqu(0, user.getId()) || acl.hasRole(ADMIN_ROLE);
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

  /**
   * Verifica per ruolo specificato.
   * @param roleNames uno o più ruoli separati da virgola
   * @return vero se almeno un ruolo è posseduto dall'utente corrente
   * @throws Exception
   */
  public boolean haveRole(String roleNames)
     throws Exception
  {
    if(isAdmin())
      return true;

    StringTokenizer stk = new StringTokenizer(roleNames, ",");

    while(stk.hasMoreTokens())
    {
      String role = SU.okStrNull(stk.nextToken());
      if(role != null && acl.hasRole(role))
        return true;
    }

    return false;
  }
}
