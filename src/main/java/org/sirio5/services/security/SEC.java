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

import javax.servlet.http.HttpSession;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.torque.om.TorqueTurbineRole;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;

/**
 * Gestione della sicurezza.
 *
 * @author Nicola De Nisco
 */
public class SEC
{
  private static Object __security;
  private static Object __sec;

  public static SecurityService getSecurity()
  {
    if(__security == null)
      __security = TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);

    return (SecurityService) __security;
  }

  public static CoreSecurity getSec()
  {
    if(__sec == null)
      __sec = TurbineServices.getInstance().getService(CoreSecurity.SERVICE_NAME);

    return (CoreSecurity) __sec;
  }

  public static User loginUser(String uName, String uPasw)
     throws Exception
  {
    return loginUser(uName, uPasw, null);
  }

  public static User loginUser(String uName, String uPasw, MutableInt logonMode)
     throws Exception
  {
    return getSec().loginUser(uName, uPasw, logonMode);
  }

  /**
   * Recupera la TurbineAccessControlList relativa all'utente indicato
   */
  public static TurbineAccessControlList getUserACL(User us)
     throws Exception
  {
    return getSecurity().getACL(us);
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili
   */
  public static boolean checkPermission(TurbineAccessControlList acl, String permname)
     throws Exception
  {
    return acl == null ? false : acl.hasPermission(permname, getSecurity().getAllGroups())
       || acl.hasRole(CoreSecurity.ADMIN_ROLE);
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili
   */
  public static boolean checkRole(TurbineAccessControlList acl, String rolename)
     throws Exception
  {
    return acl == null ? false : acl.hasRole(rolename, getSecurity().getAllGroups())
       || acl.hasRole(CoreSecurity.ADMIN_ROLE);
  }

  /**
   * Ritorna lo userid (identificativo univoco dell'utente)
   * riconducibile all'utente indicato.
   * @param us struttura con i dati dell'utente
   * @return userid oppure -1 in caso d'errore
   */
  public static int getUserID(User us)
  {
    return getSec().getUserID(us);
  }

  public static User getUser(HttpSession session)
  {
    return getSec().getUser(session);
  }

  public static User getUser(int idUser)
  {
    return getSec().getUser(idUser);
  }

  public static User getUser(String username)
     throws DataBackendException, UnknownEntityException
  {
    return getSecurity().getUser(username);
  }

  public static User getAnonymousUser()
     throws UnknownEntityException
  {
    return getSecurity().getAnonymousUser();
  }

  public static int getRoleID(Role rl)
  {
    try
    {
      return ((TorqueTurbineRole) (rl)).getEntityId();
    }
    catch(Exception ex)
    {
      return -1;
    }
  }

  public static int getUserID(RunData data)
  {
    return getUserID(data.getSession());
  }

  public static int getUserID(HttpSession session)
  {
    return getSec().getUserID(session);
  }

  /**
   * Verifica per utente amministratore
   * @param session sessione con i dati dell'utente
   * @return vero se amministratore
   */
  public static boolean isAdmin(HttpSession session)
  {
    return getSec().isAdmin(session);
  }

  public static boolean isAdmin(RunData data)
  {
    return isAdmin(data.getSession());
  }

  /**
   * Recupera lista permessi dalla sessione.
   * @param session sessione con i dati dell'utente
   * @return lista permessi o null
   */
  public static TurbineAccessControlList getACL(HttpSession session)
  {
    return getSec().getACL(session);
  }

  /**
   * Controlla che l'utente loggato possieda almeno uno dei permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param data dati di sessione
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente possiede uno dei permessi
   */
  public static boolean checkAnyPermission(RunData data, String permessi)
     throws Exception
  {
    return getSec().checkAnyPermission(data.getSession(), permessi);
  }

  /**
   * Controlla che l'utente loggato possieda tutti i permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param data dati di sessione
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente posside tutti i permessi
   */
  public static boolean checkAllPermission(RunData data, String permessi)
     throws Exception
  {
    return getSec().checkAllPermission(data.getSession(), permessi);
  }

  /**
   * Controlla che l'utente loggato possieda almeno uno dei permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param data dati di sessione
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente possiede uno dei permessi
   */
  public static boolean checkAnyPermission(HttpSession session, String permessi)
     throws Exception
  {
    return getSec().checkAnyPermission(session, permessi);
  }

  /**
   * Controlla che l'utente loggato possieda tutti i permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param data dati di sessione
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente posside tutti i permessi
   */
  public static boolean checkAllPermission(HttpSession session, String permessi)
     throws Exception
  {
    return getSec().checkAllPermission(session, permessi);
  }

  public static boolean loginUser(HttpSession session, String uName, String uPasw)
     throws Exception
  {
    return getSec().loginUser(session, uName, uPasw, null) != null;
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili.
   */
  public static boolean checkPermission(HttpSession session, String permname)
     throws Exception
  {
    return getSec().checkAnyPermission(session, permname);
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili.
   */
  public static boolean checkRole(HttpSession session, String rolename)
     throws Exception
  {
    TurbineAccessControlList acl = getACL(session);
    return checkRole(acl, rolename);
  }

  public static User loginUser(HttpSession session, String username, String password, MutableInt mmode)
     throws Exception
  {
    return getSec().loginUser(session, username, password, mmode);
  }

  /**
   * Applica algoritmo per l'autologon.
   * E' possibile in alcune situazione effettuare autologon al sistema
   * fornendo dei parametri letti dalla richiesta. Per ottenere i valori
   * occorre utilizzare una funzione di scramble presente in commonlib.
   * Se viene fornita una sessione vengono salvari nella sessione il descrittore
   * dell'utente e la relativa ACL in modo compatibile come un logon tradizionale.
   * @param time tempo in millisecondi al momento della compilazione della richiesta
   * @param userName nome dell'utente che intende loggarsi
   * @param key chiave di scramble ottenuta da kCalc (rientra anche la password dell'utente)
   * @param requestType tipo di richiesta (può essere null) per aumentare lo scramble
   * @param session sessione per memorizzare utente e acl (può essere null)
   * @return vero se l'utente può considerarsi loggato
   * @throws Exception
   */
  public static boolean autoLogonTestByUserName(String time, String userName, String key, String requestType, HttpSession session)
     throws Exception
  {
    return getSec().autoLogonTestByUserName(time, userName, key, requestType, session);
  }

  /**
   * Calcola il valore della chiave di scramble.
   * Potrà essere utilizzata come parametro key per autoLogonTestByUserName.
   * @param tClient tempo in millisecondi al momento della compilazione della richiesta
   * @param u utente che esegue la pseudo logon
   * @param requestType tipo di richiesta (può essere null) per aumentare lo scramble
   * @return la chiave di scramble
   * @throws Exception
   */
  public static String makeAutoLogonKey(long tClient, User u, String requestType)
     throws Exception
  {
    return getSec().makeAutoLogonKey(tClient, u, requestType);
  }

  public static boolean isAnonymousUser(User user)
  {
    return getSecurity().isAnonymousUser(user);
  }

  public static void saveUser(User user)
     throws UnknownEntityException, DataBackendException
  {
    getSecurity().saveUser(user);
  }

  /**
   * Grant an User a Role in a Group.
   *
   * @param user the user.
   * @param group the group.
   * @param role the role.
   * @throws DataBackendException if there was an error accessing the data
   * backend.
   * @throws UnknownEntityException if user account, group or role is not
   * present.
   */
  public static void grant(String user, String group, String role)
     throws DataBackendException, UnknownEntityException
  {
    User u = getSecurity().getUser(user);
    Group g = getSecurity().getGroupByName(group);
    Role r = getSecurity().getRoleByName(role);
    getSecurity().grant(u, g, r);
  }

  /**
   * Grant an User a Role in Global Group.
   *
   * @param user the user.
   * @param role the role.
   * @throws DataBackendException if there was an error accessing the data
   * backend.
   * @throws UnknownEntityException if user account, group or role is not
   * present.
   */
  public static void grant(String user, String role)
     throws DataBackendException, UnknownEntityException
  {
    User u = getSecurity().getUser(user);
    Group g = getSecurity().getGlobalGroup();
    Role r = getSecurity().getRoleByName(role);
    getSecurity().grant(u, g, r);
  }
}
