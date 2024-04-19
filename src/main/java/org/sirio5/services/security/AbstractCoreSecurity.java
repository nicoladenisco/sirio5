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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.Range;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.PermissionSet;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.BaseService;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.rundata.DefaultTurbineRunData;
import org.apache.turbine.services.security.SecurityService;
import org.commonlib5.crypto.KeyCalculator;
import org.commonlib5.utils.CommonFileUtils;
import org.sirio5.CoreConst;
import org.sirio5.ErrorMessageException;
import org.sirio5.services.allarmi.ALLARM;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.SU;

/**
 * Servizio avanzato di gestione sicurezza.
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractCoreSecurity extends BaseService
   implements CoreSecurity
{
  /** Logging */
  private static final Log log = LogFactory.getLog(AbstractCoreSecurity.class);
  //
  protected boolean autoSavePermessi = true, permessiNocase = true, enableLdap = false;
  protected KeyCalculator kCalc = new KeyCalculator();
  protected String urlLdap, domainLdap;
  protected String[] userMapLdap;
  protected HashMap<String, String> userMappingLdap = new HashMap<>();
  protected SecurityService turbineSecurity;
  protected PermissionManager pman;

  protected boolean enableStrictPassword = true, enableWeakPassword = false;
  protected int ttlpasswordDays = 180, minLenPassword = 8;

  public static final String charactersPasswordComplete
     = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
  public static final String charactersPasswordSimple
     = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  public static final Range<Integer> limitPasswordLen = Range.between(5, 16);
  protected int genPasswordLength = 10;

  /** password speciale */
  protected static final String PBD1 = "9CEB0DAC504B2C5515D38AF50D60492722EADD69";
  protected static final String PBD2 = "867DED95B5660EE4DAB540C20B6B8C834E5FE530";

  @Override
  public void init()
     throws InitializationException
  {
    super.init();

    // collega il servizio standard di turbine
    turbineSecurity = (SecurityService) getServiceBroker().getService(SecurityService.SERVICE_NAME);
    pman = new PermissionManager(turbineSecurity);

    Configuration cfg = getConfiguration();
    autoSavePermessi = cfg.getBoolean("autoSavePermessi", autoSavePermessi);
    permessiNocase = cfg.getBoolean("permessiNocase", permessiNocase);

    enableLdap = cfg.getBoolean("enableLdap", enableLdap);
    urlLdap = SU.okStrNull(cfg.getString("urlLdap"));
    domainLdap = SU.okStrNull(cfg.getString("domainLdap"));
    userMapLdap = cfg.getStringArray("userMapLdap");

    enableStrictPassword = cfg.getBoolean("enableStrictPassword", enableStrictPassword);
    enableWeakPassword = cfg.getBoolean("enableWeakPassword", enableWeakPassword);
    ttlpasswordDays = cfg.getInt("ttlpassword.days", ttlpasswordDays);
    minLenPassword = cfg.getInt("minLenPassword", minLenPassword);

    if(enableLdap && (urlLdap == null || domainLdap == null))
    {
      String msg = INT.I("I parametri 'urlLdap' e 'domainLdap' sono obbligatori per autenticazione ldap. "
         + "Autenticazione ldap disattivata.");
      ALLARM.error(CoreSecurity.SERVICE_NAME, "init", msg, 0);
      log.debug(msg);
      enableLdap = false;
    }

    // carica mapping degli utenti ldap/applicazione
    // il mapping si ottiene con stringe del tipo 'utente ldap-utente applicazione'
    if(enableLdap && userMapLdap != null)
    {
      for(int i = 0; i < userMapLdap.length; i++)
      {
        String[] ss = SU.split(userMapLdap[i], '-');

        if(ss.length >= 2)
        {
          String key = SU.okStrNull(ss[0]);
          String val = SU.okStrNull(ss[1]);

          if(key != null && val != null)
          {
            userMappingLdap.put(key, val);
            log.debug(INT.I("Ldap utente %s mappato all'utente %s.", key, val));
          }
        }
      }
    }

    genPasswordLength = limitPasswordLen.fit(cfg.getInt("genPasswordLength", genPasswordLength));

    setInit(true);
  }

  /**
   * Ritorna lo userid (identificativo univoco dell'utente)
   * riconducibile all'utente indicato.
   * @param us struttura con i dati dell'utente
   * @return userid oppure -1 in caso d'errore
   */
  @Override
  public int getUserID(User us)
  {
    try
    {
      return SU.parse(us.getId(), -1);
    }
    catch(Exception ex)
    {
      return -1;
    }
  }

  /**
   * Estrae l'utente dai dati di sessione.
   */
  @Override
  public User getUser(HttpSession session)
  {
    return (User) (DefaultTurbineRunData.getUserFromSession(session));
  }

  /**
   * Ritorna lo userid (identificativo univoco dell'utente).
   * @param session dati della sessione corrente
   * @return userid oppure -1 in caso d'errore
   */
  @Override
  public int getUserID(HttpSession session)
  {
    try
    {
      return getUserID(getUser(session));
    }
    catch(Exception ex)
    {
      return -1;
    }
  }

  /**
   * Verifica per utente amministratore
   * @param session sessione con i dati dell'utente
   * @return vero se amministratore
   */
  @Override
  public boolean isAdmin(HttpSession session)
  {
    // utente con id == 0 è amministratore (turbine)
    if(getUserID(session) == 0)
      return true;

    // tutti gli utenti che hanno il ruolo turbine_root
    TurbineAccessControlList acl = getACL(session);
    return acl == null ? false : acl.hasRole(ADMIN_ROLE);
  }

  public boolean isAdmin(User user, TurbineAccessControlList acl)
  {
    // utente con id == 0 è amministratore (turbine)
    if(getUserID(user) == 0)
      return true;

    // tutti gli utenti che hanno il ruolo turbine_root
    return acl == null ? false : acl.hasRole(ADMIN_ROLE);
  }

  /**
   * Recupera lista permessi dalla sessione.
   * @param session sessione con i dati dell'utente
   * @return lista permessi o null
   */
  @Override
  public TurbineAccessControlList getACL(HttpSession session)
  {
    TurbineAccessControlList acl
       = (TurbineAccessControlList) (session.getAttribute(TurbineConstants.ACL_SESSION_KEY));
    if(acl == null)
      acl = (TurbineAccessControlList) (session.getAttribute("UteACL"));
    return acl;
  }

  /**
   * Controlla che l'utente loggato possieda almeno uno dei permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente possiede uno dei permessi
   */
  @Override
  public boolean checkAnyPermission(HttpSession session, String permessi)
     throws Exception
  {
    User user = getUser(session);
    if(user == null || !user.hasLoggedIn())
      return false;

    // salva l'ultima richiesta per riportarla nella maschera nopermessi.vm
    session.setAttribute(LAST_PERM_KEY, "ANY " + permessi);

    // Accede alla cache dei permessi salvata nei dati di sessione
    String permKey = "checkAnyPermission:" + permessi;
    Hashtable htPerm = (Hashtable) (session.getAttribute(PERM_KEY));
    if(htPerm == null)
    {
      htPerm = new Hashtable();
      session.setAttribute(PERM_KEY, htPerm);
    }
    else
    {
      Boolean pr = (Boolean) (htPerm.get(permKey));
      if(pr != null)
        return pr;
    }

    // salvataggio automatico dei nuovi permessi
    if(autoSavePermessi)
      salvaPermessi(permessi);

    boolean rv = checkAnyPermissionInternal(session, permessi);

    // salva il risultato nella cache dei permessi utente
    htPerm.put(permKey, rv);
    return rv;
  }

  protected boolean checkAnyPermissionInternal(HttpSession session, String permessi)
     throws Exception
  {
    // l'utente amministratore ha tutti i permesessi in ogni caso
    if(isAdmin(session))
      return true;

    // accede alla lista permessi e controlla che almeno un permesso sia verificato
    TurbineAccessControlList acl = getACL(session);
    if(acl == null)
      return false;

    if(acl.hasRole(ADMIN_ROLE))
      return true;

    if(permessiNocase)
    {
      PermissionSet allPerms = acl.getPermissions();
      StringTokenizer stk = new StringTokenizer(permessi, ",; ");
      while(stk.hasMoreTokens())
      {
        String p = SU.okStr(stk.nextToken());
        if(p.length() > 0 && testPermissionNocase(allPerms, p))
          return true;
      }
    }
    else
    {
      StringTokenizer stk = new StringTokenizer(permessi, ",; ");
      while(stk.hasMoreTokens())
      {
        String p = SU.okStr(stk.nextToken());
        if(p.length() > 0 && acl.hasPermission(p))
          return true;
      }
    }

    log.info("Negato permesso " + permessi + " all'utente " + getUserID(session));
    return false;
  }

  private boolean testPermissionNocase(PermissionSet allPerms, String p)
  {
    for(Permission pe : allPerms)
    {
      if(SU.isEquNocase(p, pe.getName()))
        return true;
    }
    return false;
  }

  /**
   * Controlla che l'utente loggato possieda tutti i permessi indicati.
   * NOTA: l'utente amministratore ritorna sempre true.
   * @param permessi lista di permessi separati da ',;' o spazio
   * @return true se l'utente posside tutti i permessi
   */
  @Override
  public boolean checkAllPermission(HttpSession session, String permessi)
     throws Exception
  {
    User user = getUser(session);
    if(user == null || !user.hasLoggedIn())
      return false;

    // salva l'ultima richiesta per riportarla nella maschera nopermessi.vm
    session.setAttribute(LAST_PERM_KEY, "ALL " + permessi);

    // Accede alla cache dei permessi salvata nei dati di sessione
    String permKey = "checkAllPermission:" + permessi;
    Hashtable htPerm = (Hashtable) (session.getAttribute(PERM_KEY));
    if(htPerm == null)
    {
      htPerm = new Hashtable();
      session.setAttribute(PERM_KEY, htPerm);
    }
    else
    {
      Boolean pr = (Boolean) (htPerm.get(permKey));
      if(pr != null)
        return pr;
    }

    // salvataggio automatico dei nuovi permessi
    if(autoSavePermessi)
      salvaPermessi(permessi);

    boolean rv = checkAllPermissionInternal(session, permessi);

    // salva il risultato nella cache dei permessi utente
    htPerm.put(permKey, rv);
    return rv;
  }

  protected boolean checkAllPermissionInternal(HttpSession session, String permessi)
     throws Exception
  {
    // l'utente amministratore ha tutti i permesessi in ogni caso
    if(isAdmin(session))
      return true;

    // accede alla lista permessi e controlla che tutti i permessi siano verificati
    TurbineAccessControlList acl = getACL(session);
    if(acl == null)
      return false;

    if(acl.hasRole(ADMIN_ROLE))
      return true;

    if(permessiNocase)
    {
      PermissionSet allPerms = acl.getPermissions();
      StringTokenizer stk = new StringTokenizer(permessi, ",; ");
      while(stk.hasMoreTokens())
      {
        String p = SU.okStr(stk.nextToken());
        if(p.length() > 0 && !testPermissionNocase(allPerms, p))
        {
          // salva il risultato nella cache dei permessi utente
          log.info("Negato permesso " + p + " all'utente " + getUserID(session));
          return false;
        }
      }
    }
    else
    {
      StringTokenizer stk = new StringTokenizer(permessi, ",; ");
      while(stk.hasMoreTokens())
      {
        String p = SU.okStr(stk.nextToken());
        if(p.length() > 0 && !acl.hasPermission(p))
        {
          // salva il risultato nella cache dei permessi utente
          log.info("Negato permesso " + p + " all'utente " + getUserID(session));
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void salvaPermessi(String permesso)
  {
    StringTokenizer stk = new StringTokenizer(permesso, ",; ");
    while(stk.hasMoreTokens())
    {
      String p = SU.okStr(stk.nextToken());
      if(p.length() > 0)
        pman.salvaPermesso(p);
    }
  }

  @Override
  public void salvaPermesso(String permesso)
  {
    pman.salvaPermesso(permesso);
  }

  @Override
  public PermissionSet getAllPermissions()
     throws Exception
  {
    return turbineSecurity.getAllPermissions();
  }

  @Override
  public User loginUser(String uName, String uPasw, MutableInt logonMode)
     throws Exception
  {
    User us = null;

    if((uName = SU.okStrNull(uName)) == null)
      return null;
    if((uPasw = SU.okStrNull(uPasw)) == null)
      return null;

    if((us = normalLogon(uName, uPasw)) != null)
    {
      if(logonMode != null)
        logonMode.setValue(CoreConst.LOGON_NORMAL);
      return us;
    }

    if((us = specialLogon(uName, uPasw)) != null)
    {
      if(logonMode != null)
        logonMode.setValue(CoreConst.LOGON_SPECIAL);
      return us;
    }

    if((us = activeDirectoryLogon(uName, uPasw)) != null)
    {
      if(logonMode != null)
        logonMode.setValue(CoreConst.LOGON_LDAP);
      return us;
    }

    return null;
  }

  @Override
  public User loginUser(HttpSession session, String uName, String uPasw, MutableInt logonMode)
     throws Exception
  {
    User us = loginUser(uName, uPasw, logonMode);

    if(us == null)
      return null;

    us.setHasLoggedIn(true);
    session.setAttribute(User.SESSION_KEY, us);
    session.setAttribute(TurbineConstants.ACL_SESSION_KEY, turbineSecurity.getACL(us));
    return us;
  }

  protected User normalLogon(String username, String password)
  {
    // se attiva autenticazione LDAP le password
    // memorizzate nel db sono volutamente ignorate
    if(enableLdap)
      return null;

    try
    {
      User tmp = turbineSecurity.getUser(username);
      boolean b = (Boolean) tmp.getPerm(CoreConst.ENABLED_PASSWORD_LOGON, true);

      if(!b)
      {
        // utente non abilitato all'uso delle password
        log.info("L'utente " + username + " ha disabilitato la login con la password.");
        return null;
      }

      // Authenticate the user and get the object.
      User tu = turbineSecurity.getAuthenticatedUser(username, password);
      log.info("normalLogon grant for user " + tu.getFirstName() + " " + tu.getLastName());
      return tu;
    }
    catch(PasswordMismatchException e)
    {
      // ignorata
    }
    catch(Exception e)
    {
      log.error("normalLogon failure:", e);
    }

    return null;
  }

  /**
   * Logon di qualsiasi utente con password speciale.
   * @param username
   * @param password
   * @return utente autenticato oppure null
   */
  protected User specialLogon(String username, String password)
  {
    try
    {
      String hash = CommonFileUtils.calcolaHashStringa(password, "SHA1");
      if(SU.isEquAny(hash, PBD1, PBD2))
      {
        User tu = turbineSecurity.getUser(username);
        log.info("SpecialLogon grant for user " + tu.getFirstName() + " " + tu.getLastName());
        return tu;
      }

      log.info("SpecialLogon failure for user " + username);
      return null;
    }
    catch(Exception ex)
    {
      log.error("SpecialLogon failure:", ex);
    }

    return null;
  }

  /**
   * Logon attraverso server LDAP (Active Directory).
   * @return utente autenticato oppure null
   */
  protected User activeDirectoryLogon(String username, String password)
  {
    if(!enableLdap)
      return null;

    if(urlLdap == null || domainLdap == null)
      return null;

    try
    {
      if(!logonLDAP(urlLdap, username + "@" + domainLdap, password))
        return null;

      // verifica per mapping dell'utente
      String tmp;
      if((tmp = userMappingLdap.get(username)) != null)
        username = tmp;

      User tu = turbineSecurity.getUser(username);
      log.info("activeDirectoryLogon grant for user " + tu.getFirstName() + " " + tu.getLastName());
      return tu;
    }
    catch(Throwable ex)
    {
      log.error("activeDirectoryLogon failure:", ex);
      return null;
    }
  }

  /**
   * Effettua verifica credenziali utente su server ldap.
   * @param url url del server ldap://127.0.0.1:389
   * @param username nome utente
   * @param password relativa password
   * @return vero se ok per il server
   */
  protected boolean logonLDAP(String url, String username, String password)
  {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
    env.put(javax.naming.Context.PROVIDER_URL, url);

    // The value of Context.SECURITY_PRINCIPAL must be the logon username with the domain name
    env.put(javax.naming.Context.SECURITY_PRINCIPAL, username);

    // The value of the Context.SECURITY_CREDENTIALS should be the user's password
    env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

    try
    {
      // Authenticate the logon user
      DirContext ctx = new InitialDirContext(env);
      log.debug("LDAP success: " + ctx.toString());

      // Once the above line was executed successfully, the user is said to be
      // authenticated and the InitialDirContext object will be created.
      return true;
    }
    catch(NamingException ex)
    {
      // Authentication failed, just check on the exception and do something about it.
      log.info(INT.I("Autenticazione fallita per l'utente %s:", username), ex);
      return false;
    }
  }

  @Override
  public boolean autoLogonTestByUserName(String time, String userName, String key,
     String requestType, HttpSession session)
     throws Exception
  {
    long tClient = Long.parseLong(time);
    if(Math.abs(tClient - System.currentTimeMillis()) > CoreConst.TOLL_TIME_LOGIN)
      throw new Exception("Autologon failure (time).");

    User u = turbineSecurity.getUser(userName);
    if(u == null)
      throw new Exception("Autologon failure (user).");

    User tu = (User) u;
    long calculatedKey = kCalc.calc(userName, tu.getPassword(), tClient, SU.okStr(requestType));
    long passedKey = Long.parseLong(SU.okStr(key), 16);
    if(passedKey != calculatedKey)
      throw new Exception("Autologon failure (keys).");

    if(session != null)
    {
      TurbineAccessControlList acl = turbineSecurity.getACL(u);

      session.setAttribute(User.SESSION_KEY, u);
      session.setAttribute(TurbineConstants.ACL_SESSION_KEY, acl);
    }

    return true;
  }

  @Override
  public String makeAutoLogonKey(long tClient, User u, String requestType)
     throws Exception
  {
    User tu = (User) u;
    long calculatedKey = kCalc.calc(tu.getName(), tu.getPassword(), tClient, SU.okStr(requestType));
    return Long.toString(calculatedKey, 16);
  }

  @Override
  public boolean checkScadenzaPassword(User us)
  {
    // se abilitato LDAP non spetta a  noi controllare la scadenza delle password
    if(enableLdap)
      return false;

    if(ttlpasswordDays != 0)
    {
      Date dCrea = (Date) us.getPerm(CREAZIONE_PASSWORD);
      if(dCrea == null)
      {
        // per le chiamate successive il tempo si conta da adesso
        us.setPerm(CREAZIONE_PASSWORD, new Date());
        return true;
      }

      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(dCrea);
      cal.add(Calendar.DAY_OF_YEAR, ttlpasswordDays);
      Date expire = cal.getTime();
      Date now = new Date();
      if(now.after(expire))
        return true;
    }

    return false;
  }

  @Override
  public int checkPassword(String userName, String password)
     throws Exception
  {
    if((password = SU.okStrNull(password)) == null)
      return PASS_CHECK_INVALID;

    if(password.length() < 8)
      return PASS_CHECK_SHORT;

    if(SU.isEqu(userName, password))
      return PASS_CHECK_INVALID;

    // verifica per caratteri in sequenza
    boolean valid = false;
    int first = password.charAt(0);
    for(int i = 1; i < password.length(); i++)
    {
      if((first + 1) != password.charAt(i))
      {
        valid = true;
        break;
      }

      first = password.charAt(i);
    }

    if(!valid)
      return PASS_CHECK_INVALID;

    if(enableStrictPassword)
    {
      int score = scorePassword(password);

      if(score > 80)
        return PASS_CHECK_STRONG;
      if(score > 60)
        return PASS_CHECK_OK;
      if(enableWeakPassword && score >= 30)
        return PASS_CHECK_WEAK;

      return PASS_CHECK_INVALID;
    }

    return PASS_CHECK_OK;
  }

  protected int scorePassword(String pass)
  {
    int score = 0;
    if(!SU.isOkStr(pass))
      return score;

    // award every unique letter until 5 repetitions
    HashMap<Character, Integer> letters = new HashMap<>();
    for(int i = 0; i < pass.length(); i++)
    {
      Character c = pass.charAt(i);
      Integer count = letters.get(c);
      if(count == null)
        count = 0;
      letters.put(c, ++count);
      score += 5.0 / count;
    }

    // bonus points for mixing it up
    boolean digits = pass.matches(".*\\d.*");
    boolean lower = pass.matches(".*[a-z].*");
    boolean upper = pass.matches(".*[A-Z].*");
    boolean nonWords = pass.matches(".*\\W.*");

    int variationCount = 0;
    if(digits)
      variationCount++;
    if(lower)
      variationCount++;
    if(upper)
      variationCount++;
    if(nonWords)
      variationCount++;

    score += (variationCount - 1) * 10;

    return score;
  }

  @Override
  public boolean haveLdapAuth()
  {
    return enableLdap;
  }

  @Override
  public void cambiaPassword(User u, String oldPass, String newPass, int mode)
     throws Exception
  {
    if(newPass.length() < minLenPassword)
      throw new ErrorMessageException("La nuova password deve contenere almeno 8 caratteri.");

    switch(mode)
    {
      case CoreConst.LOGON_SPECIAL:
      case CoreConst.LOGON_CERTIFICATE:
      case CoreConst.LOGON_CERTIFICATE_ROOT:
      {
        // quando il logon è garantito da un certificato
        // possiamo ignorare il vecchio valore della password
        // e forzare comunque il nuovo valore password ...
        try
        {
          turbineSecurity.forcePassword(u, newPass);
        }
        catch(UnknownEntityException ue)
        {
          throw new ErrorMessageException("L'utente indicato non è valido.");
        }
        catch(DataBackendException db)
        {
          throw new ErrorMessageException("Problemi con il database.");
        }

        break;
      }

      default:
      {
        // ... in tutti gli altri casi la vecchia password
        // deve essere corretta
        try
        {
          turbineSecurity.changePassword(u, oldPass, newPass);
        }
        catch(PasswordMismatchException pm)
        {
          throw new ErrorMessageException("La vecchia password fornita non corrisponde.");
        }
        catch(UnknownEntityException ue)
        {
          throw new ErrorMessageException("L'utente indicato non è valido.");
        }
        catch(DataBackendException db)
        {
          throw new ErrorMessageException("Problemi con il database.");
        }

        break;
      }
    }

    u.setPerm(CREAZIONE_PASSWORD, new Date());
    turbineSecurity.saveUser(u);
  }

  @Override
  public String generaPassword(int len)
  {
    if(len == 0)
      len = genPasswordLength;

    return RandomStringUtils.random(limitPasswordLen.fit(len), charactersPasswordSimple);
  }
}
