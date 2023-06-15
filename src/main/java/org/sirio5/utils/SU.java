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
package org.sirio5.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.PullService;
import org.apache.turbine.services.pull.tools.UITool;
import org.apache.turbine.util.RunData;
import org.commonlib5.exec.ExecHelper;
import org.commonlib5.utils.*;
import org.rigel5.SetupHolder;
import org.sirio5.CoreConst;
import org.sirio5.rigel.ConcurrentDatabaseModificationException;
import org.sirio5.rigel.RigelHtmlI18n;
import org.sirio5.rigel.UnmodificableRecordException;
import org.sirio5.services.cache.CACHE;
import org.sirio5.services.localization.INT;
import org.sirio5.services.token.TokenAuthItem;

/**
 * Semplici Utility.
 * Questa classe contiene semplici utility sotto forma di funzioni static.
 */
public class SU extends StringOper
{
  private static final Log log = LogFactory.getLog(SU.class);

  public static final String PERM_PAR_KEY = "PermanentParameterMap";
  public static final String SESSION_ID = "sessionId";
  public static final String QUERY_STRING = "queryString";
  public static final String PATH_INFO = "pathInfo";
  public static final Pattern pw2c = Pattern.compile("^(.):\\\\(.+)$", Pattern.CASE_INSENSITIVE);
  public static final Pattern pc2w = Pattern.compile("^/cygdrive/(.)/(.+)$", Pattern.CASE_INSENSITIVE);
  public static final String LISTA_STAMPANTI_SISTEMA = "LISTA_STAMPANTI_SISTEMA";
  public static final SimpleDateFormat fmtDate2Directory = new SimpleDateFormat("yyyy/MM/dd");
  public static final SimpleDateFormat fmtDate2FileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");

  /**
   * Generazione di una Map per i parametri di una richiesta.
   * Questa funzione ha una importante funzionalita':
   * estrae i parametri anche dai dati di sessione memorizzati
   * con il metodo saveParam. In questo modo
   * si possono creare dei parametri semi permanenti all'interno
   * della sessione e soprattutto possono essere passati da una
   * pagina e l'altra senza farli comparire nell'url.
   * A parita' di nome quelli della richiesta sono prevalenti
   * su quelli memorizzati nella sessione.
   * @param request
   * @return
   */
  public static Map getParMap(HttpServletRequest request)
  {
    HashMap htParam = new HashMap();

    // estrae i parametri permanenti salvati in sessione
    HttpSession session = request.getSession();
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved != null)
      htParam.putAll(saved);

    // estrae i parametri della richiesta (anche i campi di input con nome della form)
    Map<String, String[]> parameterMap = request.getParameterMap();
    for(Map.Entry<String, String[]> entry : parameterMap.entrySet())
    {
      String name = entry.getKey();
      String[] value = entry.getValue();

      if(value == null || value.length == 0)
        continue;

      // se contiene un solo valore lo passa come tale, altrimenti passa l'array dei valori
      if(value.length == 1)
        htParam.put(name, value[0]);
      else
        htParam.put(name, value);
    }

    // carica i parametri fissi
    htParam.put(SESSION_ID, request.getSession().getId());
    htParam.put(QUERY_STRING, okStr(request.getQueryString()));
    htParam.put(PATH_INFO, okStr(request.getPathInfo()));

    return htParam;
  }

  public static void saveParam(HttpSession session, String key, Object val)
  {
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved == null)
    {
      saved = new HashMap();
      session.setAttribute(PERM_PAR_KEY, saved);
    }

    if(val == null)
      saved.remove(key);
    else
      saved.put(key, val);
  }

  public static void saveParam(HttpSession session, Map<String, Object> params)
  {
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved == null)
    {
      saved = new HashMap();
      session.setAttribute(PERM_PAR_KEY, saved);
    }

    saved.putAll(params);
  }

  public static void saveParam(HttpSession session, String... params)
  {
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved == null)
    {
      saved = new HashMap();
      session.setAttribute(PERM_PAR_KEY, saved);
    }

    if(params.length == 0)
      return;

    if((params.length & 1) == 1)
      throw new RuntimeException("Il parametro 'params' deve essere di lunghezza pari.");

    for(int i = 0; i < params.length; i += 2)
    {
      String key = params[i];
      String val = params[i + 1];

      if(val == null)
        saved.remove(key);
      else
        saved.put(key, val);
    }
  }

  public static Object readParam(HttpSession session, String key)
  {
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved == null)
      return null;

    return saved.get(key);
  }

  public static Object getParam(HttpSession session, String key, Object defval)
  {
    Object rv = readParam(session, key);
    return rv == null ? defval : rv;
  }

  public static Map getParMap(RunData data)
  {
    HashMap htParam = (HashMap) getParMap(data.getRequest());

    ParameterParser pp = data.getParameters();
    Object[] keys = pp.getKeys();
    for(int i = 0; i < keys.length; i++)
    {
      String name = (String) keys[i];
      String[] value = pp.getStrings(name);

      if(value == null || value.length == 0)
        continue;

      // se contiene un solo valore lo passa come tale, altrimenti passa l'array dei valori
      if(value.length == 1)
        htParam.put(name, value[0]);
      else
        htParam.put(name, value);

      // NOTA: nelle versioni precedenti qui venivano passati i file items;
      // in Turbine 5 la gestione dei files allegati è molto diversa
      // vedi data.getParameters().getParts()
    }
    return htParam;
  }

  public static Object removeParam(HttpSession session, String key)
  {
    HashMap saved = (HashMap) session.getAttribute(PERM_PAR_KEY);
    if(saved == null)
      return null;

    return saved.remove(key);
  }

  /**
   * Dump dei parametri di richiesta su stdout
   * @param request
   * @throws java.lang.Exception
   */
  public static void dumpRequest(HttpServletRequest request)
     throws Exception
  {
    Enumeration enum1 = request.getParameterNames();
    while(enum1.hasMoreElements())
    {
      String item = (String) enum1.nextElement();
      log.debug(item + "->" + request.getParameter(item)); // NOI18N
    }

    Enumeration enum2 = request.getAttributeNames();
    while(enum2.hasMoreElements())
    {
      String item = (String) enum2.nextElement();
      log.debug(item + "->" + request.getAttribute(item)); // NOI18N
    }

    log.debug("header=" + request.getHeader("Content-type")); // NOI18N
    /*
     int cl = pp.getContentLength();
     if(cl > 0)
     {
     log.debug("content type="+pp.getContentType());
     log.debug("content length="+cl);

     InputStream is = pp.getInputStream();
     byte b[] = new byte[cl];
     is.read(b);
     System.out.write(b);
     is.close();
     }
     */
  }

  /**
   * Dump dei parametri di sessione su stdout.
   * Usata per il debugging.
   * @param session
   */
  public static void dumpSession(HttpSession session)
  {
    Enumeration itr = session.getAttributeNames();
    while(itr.hasMoreElements())
    {
      String name = (String) (itr.nextElement());
      String valu = session.getAttribute(name).toString();
      log.debug(name + "=" + valu); // NOI18N
    }
  }

  /**
   * Funzione per il controllo del codice fiscale.
   * @param cf
   * @return Stringa vuota se OK altrimenti errore riscontrato.
   */
  public static String ControllaCF(String cf)
  {
    int i, s, c;
    String cf2;
    int setdisp[] =
    {
      1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
      11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
    };
    if(cf.length() == 0)
      return "";
    if(cf.length() != 16)
      return "La lunghezza del codice fiscale non &egrave;\n" + "corretta: il codice fiscale dovrebbe essere lungo\n" + "esattamente 16 caratteri.";
    cf2 = cf.toUpperCase();
    for(i = 0; i < 16; i++)
    {
      c = cf2.charAt(i);
      if(!(c >= '0' && c <= '9' || c >= 'A' && c <= 'Z'))
        return "Il codice fiscale contiene dei caratteri non validi:\n" + "i soli caratteri validi sono le lettere e le cifre.";
    }
    s = 0;
    for(i = 1; i <= 13; i += 2)
    {
      c = cf2.charAt(i);
      if(c >= '0' && c <= '9')
        s = s + c - '0';
      else
        s = s + c - 'A';
    }
    for(i = 0; i <= 14; i += 2)
    {
      c = cf2.charAt(i);
      if(c >= '0' && c <= '9')
        c = c - '0' + 'A';
      s = s + setdisp[c - 'A'];
    }
    if(s % 26 + 'A' != cf2.charAt(15))
      return "Il codice fiscale non &egrave; corretto:\n" + "il codice di controllo non corrisponde.";
    return "";
  }

  /**
   * Funzione per il controllo della partita IVA.
   * @param pi
   * @return Stringa vuota se OK altrimenti errore riscontrato.
   */
  public static String ControllaPIVA(String pi)
  {
    int i, c, s;
    if(pi.length() == 0)
      return "";
    if(pi.length() != 11)
      return "La lunghezza della partita IVA non &egrave;\n" + "corretta: la partita IVA dovrebbe essere lunga\n" + "esattamente 11 caratteri.\n";
    for(i = 0; i < 11; i++)
    {
      if(pi.charAt(i) < '0' || pi.charAt(i) > '9')
        return "La partita IVA contiene dei caratteri non ammessi:\n" + "la partita IVA dovrebbe contenere solo cifre.\n";
    }
    s = 0;
    for(i = 0; i <= 9; i += 2)
    {
      s += pi.charAt(i) - '0';
    }
    for(i = 1; i <= 9; i += 2)
    {
      c = 2 * (pi.charAt(i) - '0');
      if(c > 9)
        c = c - 9;
      s += c;
    }
    if((10 - s % 10) % 10 != pi.charAt(10) - '0')
      return "La partita IVA non &egrave; valida:\n" + "il codice di controllo non corrisponde.";
    return "";
  }

  public static String join(Collection lsObj)
     throws Exception
  {
    return join(lsObj.iterator(), ',');
  }

  public static int parseInt(Object val)
  {
    return val == null ? 0 : StringOper.parse(val, (int) 0);
  }

  public static boolean checkTrueFalse(Object oBool)
  {
    return checkTrueFalse(oBool, false);
  }

  public static boolean sleep(int millis)
  {
    try
    {
      Thread.sleep(millis);
      return true;
    }
    catch(Throwable t)
    {
      return false;
    }
  }

  public static boolean isOlderThan(File toTest, long millisTimeout)
  {
    return toTest.exists() && (System.currentTimeMillis() - toTest.lastModified()) > millisTimeout;
  }

  /**
   * Generazione di una voce di combobox.
   * @param valore valore da inserire
   * @param defVal default da selezionare
   * @return stringa HTML
   */
  public static String generaOptionCombo(String valore, String defVal)
  {
    return generaOptionCombo(valore, valore, defVal);
  }

  /**
   * Generazione di una voce di combobox.
   * @param codice valore restituito nel post
   * @param descrizione descrizione visualizzata
   * @param defVal default (codice) da selezionare
   * @return stringa HTML
   */
  public static String generaOptionCombo(String codice, String descrizione, String defVal)
  {
    if(isEqu(codice, defVal))
      return "<option value=\"" + codice + "\" selected>" + okStr(descrizione, "&nbsp;") + "</option>";
    else
      return "<option value=\"" + codice + "\">" + okStr(descrizione, "&nbsp;") + "</option>";
  }

  /**
   * Generazione di una voce di combobox.
   * @param codice valore restituito nel post
   * @param descrizione descrizione visualizzata
   * @param defVal default (codice) da selezionare
   * @return stringa HTML
   */
  public static String generaOptionCombo(int codice, String descrizione, int defVal)
  {
    if(codice == defVal)
      return "<option value=\"" + codice + "\" selected>" + okStr(descrizione, "&nbsp;") + "</option>";
    else
      return "<option value=\"" + codice + "\">" + okStr(descrizione, "&nbsp;") + "</option>";
  }

  /**
   * Converte una path windows nel formato cygwin.
   * La path deve essere assoluta (iniziare per X:\)
   * e non deve essere una path UNC (\\server\share\ecc).
   * @param path path originale windows
   * @return path equivalente cygwin
   * @throws java.lang.Exception
   */
  public static String convertPathToCygwin(String path)
     throws Exception
  {
    Matcher m = pw2c.matcher(path.trim());

    if(!m.matches() || m.groupCount() != 2)
      throw new IllegalArgumentException(
         "La path non è una path assoluta windows (deve iniziare per x:\\...).");

    String sDrive = m.group(1);
    String sPath = m.group(2);

    return "/cygdrive/" + sDrive.toLowerCase() + "/" + sPath.replace('\\', '/');
  }

  /**
   * Convere una path cygwin nel formato windows.
   * La path deve iniziare per /cygdrive/x/... e
   * @param path in stile cygwin
   * @return path originale windows
   * @throws Exception
   */
  public static String convertPathFromCygwin(String path)
     throws Exception
  {
    Matcher m = pc2w.matcher(path.trim());

    if(!m.matches() || m.groupCount() != 2)
      throw new IllegalArgumentException(
         "La path non è una path assoluta cygwin (deve iniziare per /cygwin/x/...).");

    String sDrive = m.group(1);
    String sPath = m.group(2);

    return sDrive + ":\\" + sPath.replace('/', '\\');
  }

  /**
   * Esecuzione di comandi. Il form invia un parametro speciale chiamato
   * 'command' con una stringa identificativa dell'operazione richiesta
   * dall'utente. Questa stringa diviene parte di un metodo doCmd_stringa
   * ricercato a runtime e se presente eseguito. Vedi doCmd_... per ulteriori
   * dettagli.
   * Esempio se command 'salva' o 'Salva' o 'SALVA' viene cercato
   * un metodo doCmd_salva e invocato con i parametri passati.
   * Questa funzione statica viene chiamata dalle sue sorelle in
   * CoreBaseScreen, CoreBaseAction e CoreBaseBean.
   *
   * @param caller oggetto chiamante
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri pp più eventuali parametri permanenti
   * @param args
   * @return
   * @throws Exception
   */
  public static boolean doCommand(Object caller, String command, RunData data, Map params, Object... args)
     throws Exception
  {
    Class clazz = caller.getClass();
    String mName = "doCmd_" + command.toLowerCase();
    Method cmd = null;

    try
    {
      // cerca prima un metodo che supporta i parametri args
      // ES: doCmd_salva(RunData data, Map params, Object... args)
      try
      {
        cmd = clazz.getMethod(mName, CoreConst.cmdParamTypes2);
        cmd.invoke(caller, data, params, args);
        return true;
      }
      catch(NoSuchMethodException e1)
      {
      }

      // quindi riprova senza args
      // ES: doCmd_salva(RunData data, Map params)
      try
      {
        cmd = clazz.getMethod(mName, CoreConst.cmdParamTypes1);
        cmd.invoke(caller, data, params);
        return true;
      }
      catch(NoSuchMethodException e1)
      {
      }
    }
    catch(InvocationTargetException ex)
    {
      Throwable t = ex.getCause();
      if(t == null || !(t instanceof Exception))
        throw ex;

      throw (Exception) t;
    }

    log.info("Comando " + command + " non implementato; ignorato.");
    return false;
  }

  /**
   * Esecuzione di comandi.
   * Simile alla sua gemella ma lavora con i token.
   *
   * @param caller oggetto chiamante
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri pp più eventuali parametri permanenti
   * @param args
   * @return
   * @throws Exception
   */
  public static boolean doCommand(Object caller, String command, TokenAuthItem data, Map params, Object... args)
     throws Exception
  {
    Class clazz = caller.getClass();
    String mName = "doCmd_" + command.toLowerCase();
    Method cmd = null;

    try
    {
      // cerca prima un metodo che supporta i parametri args
      // ES: doCmd_salva(RunData data, Map params, Object... args)
      try
      {
        cmd = clazz.getMethod(mName, CoreConst.cmdParamTypes4);
        cmd.invoke(caller, data, params, args);
        return true;
      }
      catch(NoSuchMethodException e1)
      {
      }

      // quindi riprova senza args
      // ES: doCmd_salva(RunData data, Map params)
      try
      {
        cmd = clazz.getMethod(mName, CoreConst.cmdParamTypes3);
        cmd.invoke(caller, data, params);
        return true;
      }
      catch(NoSuchMethodException e1)
      {
      }
    }
    catch(InvocationTargetException ex)
    {
      Throwable t = ex.getCause();
      if(t == null || !(t instanceof Exception))
        throw ex;

      throw (Exception) t;
    }

    log.info("Comando " + command + " non implementato; ignorato.");
    return false;
  }

  /**
   * Converte stringa in intero.
   * Ritorna il valore intero della stringa in base 10 senza
   * sollevare alcuna eccezione.
   * @param val un qualsiasi oggetto java
   * @param valout oggetto per memorizzare il risultato della conversione
   * @return vero se val è convertibile in un numero
   */
  public static boolean parse(Object val, MutableInt valout)
  {
    try
    {
      valout.setValue(Integer.parseInt(okStrNull(val)));
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  /**
   * Converte stringa in doppia precisione.
   * Ritorna il valore doppia precisione della stringa in base 10 senza
   * sollevare alcuna eccezione.
   * @param val un qualsiasi oggetto java
   * @param valout oggetto per memorizzare il risultato della conversione
   * @return vero se val è convertibile in un numero
   */
  public static boolean parse(Object val, MutableDouble valout)
  {
    try
    {
      valout.setValue(Double.parseDouble(okStrNull(val)));
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  /**
   * Creazione di oggetto proxy per interfaccia.
   * Data una interfaccia restituisce una implementazione con tutti i metodi
   * che sollevano una eccezione UnsupportedOperationException specificando
   * il metodo chiamato e i relativi argomenti.
   * @param interfaccia interfaccia da implementare
   * @return oggetto proxy che implementa l'interfaccia
   */
  @SuppressWarnings("unchecked")
  public static <T> T createUnimplementAdapter(Class<T> interfaccia)
  {
    class UnimplementedHandler implements InvocationHandler
    {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
      {
        throw new UnsupportedOperationException("Not implemented: "
           + method + ", args=" + Arrays.toString(args)); // NOI18N
      }
    }

    return (T) Proxy.newProxyInstance(UnimplementedHandler.class.getClassLoader(),
       new Class<?>[]
       {
         interfaccia
       },
       new UnimplementedHandler());
  }

  /**
   * Legge il contenuto di uno stream in memoria.
   * @param in stream da leggere
   * @return array di bytes del contenuto
   * @throws IOException
   */
  public static byte[] getBytes(InputStream in)
     throws Exception
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    CommonFileUtils.copyStream(in, out);
    return out.toByteArray();
  }

  public static final Pattern pBodyHtml = Pattern.compile(".*?<body.*?>(.*?)</body>.*?",
     Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

  /**
   * Recupera solo 'body' da html.
   * Dato un blocco di HTML recupera solo il contenuto del tag BODY.
   * @param html blocco HTML
   * @return contenuto del tag BODY o html se non trovato
   */
  public static String getOnlyBody(String html)
  {
    Matcher m = pBodyHtml.matcher(html);
    if(m.find())
      return m.group(1);

    return html;
  }

  /**
   * Controllo validità codice.
   * Verifica che codice contenga solo caratteri alfanumerici.
   * @param codice da verificare
   * @return vero se contiene solo caratteri consentiti
   */
  public static boolean checkCodiceValido(String codice)
  {
    return pTestCodice.matcher(codice).matches();
  }
  public static final Pattern pTestCodice = Pattern.compile("^[a-z|A-Z|0-9|_]+$");

  /**
   * Riporta errore di db all'utente se non fatale.
   * @param pdata
   * @param ex eccezione catturata da analizzare
   * @throws SQLException eccezione risollevata se fatale
   */
  public static void reportNonFatalDatabaseError(CoreRunData pdata, SQLException ex)
     throws SQLException
  {
    try
    {
      pdata.setMessage(
         SetupHolder.getQueryBuilder().formatNonFatalError(ex, new RigelHtmlI18n(pdata)) + "<br>"
         + "<span class=\"txt-white-regular-09\">"
         + pdata.i18n("Messaggio originale: %s", ex.getLocalizedMessage())
         + "</span>");
    }
    catch(Exception ex1)
    {
      throw ex;
    }
  }

  /**
   * Riporta errore di modifica concorrente all'utente.
   * @param pdata
   * @param ex
   */
  public static void reportConcurrentDatabaseError(CoreRunData pdata, ConcurrentDatabaseModificationException ex)
  {
    pdata.setMessage(
       "<span class=\"txt-white-bold-12-nul\">" + pdata.i18n("Spiacente!") + "</span><br>"
       + "<span class=\"txt-white-regular-11-nul\">"
       + pdata.i18n("Un altro utente ha modificato il record che stai salvando.") + "<br>"
       + pdata.i18n("Per evitare conflitti le tue modifiche non possono essere accettate.")
       + "</span><br>"
       + "<span class=\"txt-white-regular-09\">"
       + ex.getLocalizedMessage()
       + "</span>");
  }

  /**
   * Riporta errore di dato non modificabile all'utente.
   * @param pdata
   * @param ex
   * @throws Exception
   */
  public static void reportUnmodificableRecordError(CoreRunData pdata, UnmodificableRecordException ex)
     throws Exception
  {
    pdata.setMessage(
       "<span class=\"txt-white-bold-12-nul\">" + pdata.i18n("Spiacente!") + "</span><br>"
       + "<span class=\"txt-white-regular-11-nul\">"
       + pdata.i18n("Non hai i permessi per modificare il record indicato.")
       + "</span><br>"
       + "<span class=\"txt-white-regular-09\">"
       + ex.getLocalizedMessage()
       + "</span>");
  }

  /**
   * Ritorna il primo elemento di params che corrisponde alla classe richiesta.
   * @param <T> un qualsiasi derivato di Object
   * @param params array di oggetti
   * @param type classe cercata
   * @return l'oggetto corrispondente o null
   */
  public static <T> T getParam(Object[] params, Class<T> type)
  {
    return getParam(params, type, null);
  }

  /**
   * Ritorna il primo elemento di params che corrisponde alla classe richiesta.
   * @param <T> un qualsiasi derivato di Object
   * @param params array di oggetti
   * @param type classe cercata
   * @param defval valore di default in caso di non trovato
   * @return l'oggetto corrispondente o null
   */
  public static <T> T getParam(Object[] params, Class<T> type, T defval)
  {
    if(params != null && params.length > 0)
    {
      for(Object o : params)
      {
        if(o != null && type.isAssignableFrom(o.getClass()))
          return type.cast(o);
      }
    }
    return defval;
  }

  /**
   * Ritorna l'elemento richiesto convertendolo opportunamente.
   * @param <T> un qualsiasi derivato di Object
   * @param pos l'indice all'interno di params
   * @param params array di oggetti
   * @param type classe cercata
   * @return l'oggetto corrispondente o null
   */
  public static <T> T getParam(int pos, Object[] params, Class<T> type)
  {
    return getParam(pos, params, type, null);
  }

  /**
   * Ritorna l'elemento richiesto convertendolo opportunamente.
   * @param <T> un qualsiasi derivato di Object
   * @param pos l'indice all'interno di params
   * @param params array di oggetti
   * @param type classe cercata
   * @param defval valore di default se oggetto non presente nell'array o tipo non corrispondente
   * @return l'oggetto corrispondente oppure defval
   */
  public static <T> T getParam(int pos, Object[] params, Class<T> type, T defval)
  {
    if(params != null && params.length > pos)
    {
      Object o = params[pos];
      if(o != null && type.isAssignableFrom(o.getClass()))
        return type.cast(o);
    }
    return defval;
  }

  /**
   * Ritorna il primo valore diverso da zero.
   * I valori passati devono essere numeri interi.
   * @param values lista interi
   * @return il primo diverso da zero altrimenti zero
   */
  public static int getFirstNonZeroInteger(Number... values)
  {
    for(Number val : values)
    {
      if(val instanceof Number && ((Number) val).intValue() != 0)
        return ((Number) val).intValue();
    }
    return 0;
  }

  /**
   * Trasforma lista di parametri in mappa chiave/valore.
   * I parametri devono essere in numero pari; quelli pari saranno le chiavi,
   * quelli dispari diventeranno i valori (0/1, 2/3, 4/5).
   * @param map mappa da popolare
   * @param parameters elenco di parametri
   * @return
   */
  public static Map<String, String> pair2Map(Map<String, String> map, Object... parameters)
  {
    if((parameters.length & 1) != 0)
      throw new IllegalArgumentException();

    for(int i = 0; i < parameters.length; i += 2)
    {
      String key = okStr(parameters[i]);
      String val = okStr(parameters[i + 1]);
      map.put(key, val);
    }

    return map;
  }

  private static boolean testedDebugEnv = false, debugEnv = false;

  public static boolean isDebugEnvironment()
  {
    if(!testedDebugEnv)
    {
      debugEnv = isDebugEnvironmentInternal();
      testedDebugEnv = true;
    }

    return debugEnv;
  }

  private static boolean isDebugEnvironmentInternal()
  {
    String s;
    if((s = System.getProperty("debugEnvironment")) != null)
      return checkTrueFalse(s);

    String userHome = System.getProperty("user.home");
    if(userHome != null)
    {
      File test = new File(userHome, "cvsinfomedica");
      if(test.isDirectory())
        return true;
    }

    if(OsIdent.isUnix())
    {
      File home = new File("/home");
      if(!home.isDirectory())
        return false;

      File[] utenti = home.listFiles();
      for(File dirUtente : utenti)
      {
        if(dirUtente.isDirectory())
        {
          File test = new File(dirUtente, "cvsinfomedica");
          if(test.isDirectory())
            return true;
        }
      }
    }

    return false;
  }

  public static List<String> getListaStampanti()
  {
    try
    {
      return (List<String>) CACHE.getObject(LISTA_STAMPANTI_SISTEMA).getContents();
    }
    catch(ObjectExpiredException ex)
    {
      List<String> lsPrt = getListaStampantiUnix();
      CACHE.addObject(LISTA_STAMPANTI_SISTEMA, new CachedObject(lsPrt));
      return lsPrt;
    }
  }

  protected static List<String> getListaStampantiUnix()
  {
    ArrayList<String> rv = new ArrayList<>();

    try
    {
      ExecHelper eh = ExecHelper.execUsingShell("lpstat -a");
      List<String> lsStr = SU.string2List(eh.getOutput(), "\n", true);
      for(String s : lsStr)
      {
        String ss[] = s.split("\\s");
        if(ss.length > 0)
          rv.add(ss[0]);
      }
    }
    catch(IOException ex)
    {
      log.error("", ex);
    }

    return rv;
  }

  /**
   * Da una data genera la directory path/anno/mese/giorno verificando esistenza e coerenza.
   * @param directory directory iniziale
   * @param d data
   * @return coppia path relativa/oggetto file
   * @throws Exception
   */
  public static Pair<String, File> makeDirs(File directory, Date d)
     throws Exception
  {
    // crea una directory con anno/mese/giorno
    String dirs = fmtDate2Directory.format(d).replace('/', File.separatorChar);
    File dir = new File(directory, dirs);

    if(dir.exists())
    {
      if(!dir.isDirectory())
      {
        log.error(INT.I("%s esiste ma non e' una directory.", dir.getAbsolutePath()));
        return null;
      }
    }
    else if(!dir.mkdirs())
    {
      log.error(INT.I("Impossibile creare la directory %s ", dir.getAbsolutePath()));
      return null;
    }

    return new Pair<>(dirs, dir);
  }

  /**
   * Rimuove i commenti da un testo HTML.
   * @param html input
   * @return html senza i commenti
   */
  public static String removeHtmlComment(String html)
  {
    int pos1 = html.indexOf("<!--");

    if(pos1 != -1)
      html = html.substring(0, pos1) + removeHtmlComment(html.substring(pos1 + 4, html.length()));

    int pos2 = html.indexOf("-->");
    return pos2 == -1 ? html : html.substring(pos2 + 3, html.length());
  }

  public static final String permittedInHtml = " .;!?:\r\n()[]";

  /**
   * Elimina caratteri non consentiti in pagine HTML.
   * Utilizzata per prevenire attacchi di script injection.
   * Elimina tutti i tag sostituendoli con'_'.
   * @param s html da sanitizzare
   * @return html pulito
   */
  public static String purgeForHtml(String s)
  {
    int len = s.length();
    char[] arChar = s.toCharArray();
    StringBuilder sb = new StringBuilder(len + 5);

    for(int i = 0; i < len; i++)
    {
      int c = arChar[i];

      if(permittedInHtml.indexOf(c) != -1
         || (c >= 'a' && c <= 'z')
         || (c >= 'A' && c <= 'Z')
         || (c >= '0' && c <= '9'))
        sb.append(arChar[i]);
      else
        sb.append('_');
    }

    return sb.toString();
  }

  /**
   * Converte stringa in doppia precisione.
   * Ritorna il valore doppia precisione della stringa in base 10 senza
   * sollevare alcuna eccezione. Effettua un ulteriore tentativo sostituendo ',' con '.'.
   * @param val un qualsiasi oggetto java
   * @param valout oggetto per memorizzare il risultato della conversione
   * @return vero se val è convertibile in un numero
   */
  public static boolean parseComma(Object val, MutableDouble valout)
  {
    String s = SU.okStrNull(val);
    if(s == null)
      return false;

    try
    {
      valout.setValue(Double.parseDouble(s));
      return true;
    }
    catch(Exception e)
    {
    }

    try
    {
      valout.setValue(Double.parseDouble(s.replace(',', '.')));
      return true;
    }
    catch(Exception e)
    {
    }

    return false;
  }

  public static UITool getUITool()
  {
    PullService ps = (PullService) TurbineServices.getInstance()
       .getService(PullService.SERVICE_NAME);
    return (UITool) ps.getGlobalContext().get("ui");
  }
}
