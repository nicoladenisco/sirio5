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
import org.apache.fulcrum.security.util.PermissionSet;
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
  private static Object __turbineSecurity;
  private static Object __sirioSecurity;

  public static SecurityService getTurbineSecurity()
  {
    if(__turbineSecurity == null)
      __turbineSecurity = TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);

    return (SecurityService) __turbineSecurity;
  }

  public static CoreSecurity getSirioSecurity()
  {
    if(__sirioSecurity == null)
      __sirioSecurity = TurbineServices.getInstance().getService(CoreSecurity.SERVICE_NAME);

    return (CoreSecurity) __sirioSecurity;
  }

  public static User loginUser(String uName, String uPasw)
     throws Exception
  {
    return loginUser(uName, uPasw, null);
  }

  public static User loginUser(String uName, String uPasw, MutableInt logonMode)
     throws Exception
  {
    return getSirioSecurity().loginUser(uName, uPasw, logonMode);
  }

  /**
   * Recupera la TurbineAccessControlList relativa all'utente indicato
   */
  public static TurbineAccessControlList getUserACL(User us)
     throws Exception
  {
    return getTurbineSecurity().getACL(us);
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili
   */
  public static boolean checkPermission(TurbineAccessControlList acl, String permname)
     throws Exception
  {
    return acl == null ? false : acl.hasPermission(permname, getTurbineSecurity().getAllGroups())
       || acl.hasRole(CoreSecurity.ADMIN_ROLE);
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili
   */
  public static boolean checkRole(TurbineAccessControlList acl, String rolename)
     throws Exception
  {
    return acl == null ? false : acl.hasRole(rolename, getTurbineSecurity().getAllGroups())
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
    return getSirioSecurity().getUserID(us);
  }

  public static User getUser(HttpSession session)
  {
    return getSirioSecurity().getUser(session);
  }

  public static User getUser(int idUser)
  {
    return getSirioSecurity().getUser(idUser);
  }

  public static User getUser(String username)
     throws DataBackendException, UnknownEntityException
  {
    return getTurbineSecurity().getUser(username);
  }

  public static User getAnonymousUser()
     throws UnknownEntityException
  {
    return getTurbineSecurity().getAnonymousUser();
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
    return getSirioSecurity().getUserID(session);
  }

  /**
   * Verifica per utente amministratore
   * @param session sessione con i dati dell'utente
   * @return vero se amministratore
   */
  public static boolean isAdmin(HttpSession session)
  {
    return getSirioSecurity().isAdmin(session);
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
    return getSirioSecurity().getACL(session);
  }

  /**
   * Recupera lista permessi dal database.
   * @param user utente
   * @return lista permessi o null
   * @throws DataBackendException
   * @throws UnknownEntityException
   */
  public static TurbineAccessControlList getACL(User user)
     throws DataBackendException, UnknownEntityException
  {
    return getTurbineSecurity().getACL(user);
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
    return getSirioSecurity().checkAnyPermission(data.getSession(), permessi);
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
    return getSirioSecurity().checkAllPermission(data.getSession(), permessi);
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
    return getSirioSecurity().checkAnyPermission(session, permessi);
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
    return getSirioSecurity().checkAllPermission(session, permessi);
  }

  public static boolean loginUser(HttpSession session, String uName, String uPasw)
     throws Exception
  {
    return getSirioSecurity().loginUser(session, uName, uPasw, null) != null;
  }

  /**
   * Controlla l'acl correntemente salvata nella sessione e verifica
   * se l'utente ha il permesso indicato in tutti i gruppi possibili.
   */
  public static boolean checkPermission(HttpSession session, String permname)
     throws Exception
  {
    return getSirioSecurity().checkAnyPermission(session, permname);
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
    return getSirioSecurity().loginUser(session, username, password, mmode);
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
    return getSirioSecurity().autoLogonTestByUserName(time, userName, key, requestType, session);
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
    return getSirioSecurity().makeAutoLogonKey(tClient, u, requestType);
  }

  public static boolean isAnonymousUser(User user)
  {
    return getTurbineSecurity().isAnonymousUser(user);
  }

  public static void saveUser(User user)
     throws UnknownEntityException, DataBackendException
  {
    getTurbineSecurity().saveUser(user);
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
    User u = getTurbineSecurity().getUser(user);
    Group g = getTurbineSecurity().getGroupByName(group);
    Role r = getTurbineSecurity().getRoleByName(role);
    getTurbineSecurity().grant(u, g, r);
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
    User u = getTurbineSecurity().getUser(user);
    Group g = getTurbineSecurity().getGlobalGroup();
    Role r = getTurbineSecurity().getRoleByName(role);
    getTurbineSecurity().grant(u, g, r);
  }

  /**
   * Verifica se la password dell'utente è scaduta.
   * @param us descrittore utente
   * @return ritorna vero se la password è scaduta
   */
  public static boolean checkScadenzaPassword(User us)
  {
    return getSirioSecurity().checkScadenzaPassword(us);
  }

  public static boolean checkValiditaPassword(User us)
     throws Exception
  {
    switch(checkPassword(us.getName(), us.getPassword()))
    {
      case CoreSecurity.PASS_CHECK_WEAK:
      case CoreSecurity.PASS_CHECK_SHORT:
      case CoreSecurity.PASS_CHECK_INVALID:
        return false;
    }

    return true;
  }

  /**
   * Verifica nuova password.
   * Applica un check sulla password che si vuole salvare
   * e ritorna una delle costanti PASS_...
   * @param userName nome utente
   * @param password password da testare
   * @return livello password
   * @throws Exception
   */
  public static int checkPassword(String userName, String password)
     throws Exception
  {
    return getSirioSecurity().checkPassword(userName, password);
  }

  /**
   * Salvataggio automatico permessi non presenti.
   * @param permessi lista di permessi separati da ',;' o spazio
   */
  public static void salvaPermessi(String permessi)
  {
    getSirioSecurity().salvaPermessi(permessi);
  }

  /**
   * Salvataggio automatico permesso non presente.
   * @param permesso permesso da salvare
   */
  public static void salvaPermesso(String permesso)
  {
    getSirioSecurity().salvaPermesso(permesso);
  }

  /**
   * Recupera tutti i permessi (bufferata).
   * @return tutti i permessi
   * @throws Exception
   */
  public static PermissionSet getAllPermissions()
     throws Exception
  {
    return getSirioSecurity().getAllPermissions();
  }

  /**
   * Ritorna vero se attiva autenticazione via LDAP.
   * @return vero per ldap (Active Directory)
   */
  public static boolean haveLdapAuth()
  {
    return getSirioSecurity().haveLdapAuth();
  }

  /**
   * Cambia la password per l'utente specificato.
   * @param u utente a cui cambiare password
   * @param oldPass vecchia password
   * @param newPass nuova password
   * @param mode tipo di logon
   * @throws Exception
   */
  public static void cambiaPassword(User u, String oldPass, String newPass, int mode)
     throws Exception
  {
    getSirioSecurity().cambiaPassword(u, oldPass, newPass, mode);
  }

  /**
   * Genera una password casuale.
   * Utilità per generare una password casuale.
   * @param len lunghezza password (0=default da setup)
   * @return password generata secondo le regole a setup.
   */
  public static String generaPassword(int len)
  {
    return getSirioSecurity().generaPassword(len);
  }
}
