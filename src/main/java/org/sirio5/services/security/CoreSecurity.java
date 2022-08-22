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
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.Service;

/**
 * Servizio gestione sicurezza.
 *
 * @author Nicola De Nisco
 */
public interface CoreSecurity extends Service
{
  public static final String SERVICE_NAME = "Security";
  //
  public static final String PERM_KEY = "PERM_KEY";
  public static final String LAST_PERM_KEY = "LAST_PERM_KEY";
  public static final int TURBINE_ROOT_ROLE_ID = 1;
  public static final String ADMIN_NAME = "turbine";
  public static final String ADMIN_ROLE = "turbine_root";
  public static final String SVILUPPO_NAME = "sviluppo";
  public static final String INFOMED_NAME = "infomed";
  public static final String SVILUPPO_GROUP_NAME = "Sviluppo";
  public static final String ASSISTENZA_GROUP_NAME = "Assistenza";
  public static final String ALL_PERMISSION_CACHE_KEY = "ALL_PERMISSION_CACHE_KEY";
  //
  public static final String CREAZIONE_PASSWORD = "creazionePassword";
  public static final int PASS_CHECK_OK = 0;
  public static final int PASS_CHECK_STRONG = 1;
  public static final int PASS_CHECK_SHORT = 2;
  public static final int PASS_CHECK_WEAK = 3;
  public static final int PASS_CHECK_INVALID = 4;

  // chiave nel profilo utente per abilitazione login con password (vedi CaleidoLogin.java)
  public static final String ENABLED_PASSWORD_LOGON = "ENABLED_PASSWORD_LOGON";

  // chiave nel profilo utente per selezionare il profilo di accesso al logon
  public static final String USER_PROFILE_LOGON = "USER_PROFILE_LOGON";

  // usata per forzare una logout se l'operazione fallisce (viene salvata in parmap)
  public static final String LOGOUT_ON_FAIL = "LOGOUT_ON_FAIL";

  /**
   * Autentica l'utente come da credenziali.
   * Esegue tutti i test possibili sulle credenziali.
   * @param uName nome utente
   * @param uPasw relativa password
   * @param logonMode un intero che indica il tipo di logon (può essere null)
   * @return l'utente o null se non loggabile
   * @throws Exception
   */
  public User loginUser(String uName, String uPasw, MutableInt logonMode)
     throws Exception;

  /**
   * Autentica l'utente come da credenziali.
   * Le acl e l'utente sono salvati in sessione.
   * Utile per logon fuori da turbine (esempio da una servlet).
   * @param session sessione http
   * @param uName nome utente
   * @param uPasw relativa password
   * @param logonMode
   * @return l'utente o null se non loggabile
   * @throws Exception
   */
  public User loginUser(HttpSession session, String uName, String uPasw, MutableInt logonMode)
     throws Exception;

  /**
   * Ritorna lo userid (identificativo univoco dell'utente)
   * riconducibile all'utente indicato.
   * @param us struttura con i dati dell'utente
   * @return userid oppure -1 in caso d'errore
   */
  public int getUserID(User us);

  /**
   * Estrae l'utente dai dati di sessione.
   * @param session
   * @return
   */
  public User getUser(HttpSession session);

  /**
   * Recupera utente dal suo ID.
   * @param idUser
   * @return
   */
  public User getUser(int idUser);

  /**
   * Estrae permessi utente dalla sessione.
   * @param session
   * @return permessi o null
   */
  public TurbineAccessControlList getACL(HttpSession session);

  /**
   * Ritorna lo userid (identificativo univoco dell'utente).
   * @param session dati della sessione corrente
   * @return userid oppure -1 in caso d'errore
   */
  public int getUserID(HttpSession session);

  /**
   * Verifica se utente corrente è amministratore.
   * @param session dati utente corrente
   * @return vero se amministratore
   */
  public boolean isAdmin(HttpSession session);

  /**
   * Salvataggio automatico permessi non presenti.
   * @param permessi lista di permessi separati da ',;' o spazio
   */
  public void salvaPermessi(String permessi);

  /**
   * Salvataggio automatico permesso non presente.
   * @param permesso permesso da salvare
   */
  public void salvaPermesso(String permesso);

  /**
   * Recupera tutti i permessi (bufferata).
   * @return tutti i permessi
   * @throws Exception
   */
  public PermissionSet getAllPermissions()
     throws Exception;

  /**
   * Controlla che l'utente loggato possieda tutti i permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param session
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente posside tutti i permessi
   * @throws java.lang.Exception
   */
  public boolean checkAllPermission(HttpSession session, String permessi)
     throws Exception;

  /**
   * Controlla che l'utente loggato possieda almeno uno dei permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param session
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente possiede uno dei permessi
   * @throws java.lang.Exception
   */
  public boolean checkAnyPermission(HttpSession session, String permessi)
     throws Exception;

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
  public boolean autoLogonTestByUserName(String time, String userName, String key, String requestType, HttpSession session)
     throws Exception;

  /**
   * Calcola il valore della chiave di scramble.
   * Potrà essere utilizzata come parametro key per autoLogonTestByUserName.
   * @param tClient tempo in millisecondi al momento della compilazione della richiesta
   * @param u utente che esegue la pseudo logon
   * @param requestType tipo di richiesta (può essere null) per aumentare lo scramble
   * @return la chiave di scramble
   * @throws Exception
   */
  public String makeAutoLogonKey(long tClient, User u, String requestType)
     throws Exception;

  /**
   * Verifica se la password dell'utente è scaduta.
   * @param us descrittore utente
   * @return ritorna vero se la password è scaduta
   */
  public boolean checkScadenzaPassword(User us);

  /**
   * Verifica nuova password.
   * Applica un check sulla password che si vuole salvare
   * e ritorna una delle costanti PASS_...
   * @param userName nome utente
   * @param password password da testare
   * @return livello password
   * @throws Exception
   */
  public int checkPassword(String userName, String password)
     throws Exception;

  /**
   * Ritorna vero se attiva autenticazione via LDAP.
   * @return vero per ldap (Active Directory)
   */
  public boolean haveLdapAuth();

  /**
   * Cambia la password per l'utente specificato.
   * @param u utente a cui cambiare password
   * @param oldPass vecchia password
   * @param newPass nuova password
   * @param mode tipo di logon
   * @throws Exception
   */
  public void cambiaPassword(User u, String oldPass, String newPass, int mode)
     throws Exception;
}
