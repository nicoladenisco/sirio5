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
package org.sirio5.services.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.services.security.SecurityService;
import static org.sirio5.services.security.CoreSecurity.ADMIN_ROLE;
import org.sirio5.utils.SU;

/**
 * Classe di supporto per la gestione automatica dei permessi.
 *
 * @author Nicola De Nisco
 */
public class PermissionManager
{
  private static final Log log = LogFactory.getLog(PermissionManager.class);

  protected final SecurityService security;
  protected Role adminRole;

  PermissionManager(SecurityService security)
  {
    this.security = security;
  }

  public void salvaPermesso(String permesso)
  {
    try
    {
      String pp = SU.okStrNull(permesso);
      if(pp == null)
        return;

      // controlla che la permission esista
      PermissionSet allPerm = security.getAllPermissions();
      if(allPerm.containsName(pp))
        return;

      loadAdminRole();

      // crea la permission e la assegna al ruolo amministratore
      Permission perm = security.getPermissionInstance();
      perm.setName(pp);
      security.addPermission(perm);
      security.grant(adminRole, security.getPermissionByName(pp));
    }
    catch(EntityExistsException ex)
    {
      // eccezione ignorata: il permesso già esiste
    }
    catch(Exception ex)
    {
      log.error("Errore generico:", ex);
    }
  }

  protected void loadAdminRole()
     throws Exception
  {
    // recupera o crea se non presente il ruolo amministratore
    if(adminRole == null)
    {
      try
      {
        adminRole = security.getRoleByName(ADMIN_ROLE);
      }
      catch(UnknownEntityException ee)
      {
        adminRole = security.getRoleInstance();
        adminRole.setName(ADMIN_ROLE);
        security.addRole(adminRole);
      }
    }
  }
}
