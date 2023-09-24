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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.Service;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.rundata.DefaultTurbineRunData;
import org.apache.turbine.util.uri.TemplateURI;
import org.sirio5.CoreConst;
import org.sirio5.ErrorMessageException;
import org.sirio5.services.formatter.DataFormatter;
import org.sirio5.services.formatter.NumFormatter;
import org.sirio5.services.formatter.ValutaFormatter;
import org.sirio5.services.modellixml.modelliXML;
import org.sirio5.services.security.SEC;

/**
 * Estende la classe rundata di default aggiungendo una
 * serie di funzioni utilissime per la costruzione dei
 * template vm.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class CoreRunData extends DefaultTurbineRunData
{
  private LocalizationService lsrv = null;
  private String refresh = null;
  private static String homeLink = null;

  private final DataFormatter df;
  private final ValutaFormatter vf;
  private final NumFormatter nf;
  private final modelliXML modXML;

  public CoreRunData()
  {
    super();

    df = getService(DataFormatter.SERVICE_NAME);
    vf = getService(ValutaFormatter.SERVICE_NAME);
    nf = getService(NumFormatter.SERVICE_NAME);
    modXML = getService(modelliXML.SERVICE_NAME);
  }

  public <T extends Service> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  public String formatData(Date data)
     throws Exception
  {
    return data == null ? "&nbsp;" : df.formatData(data);
  }

  public String formatDataFull(Date data)
     throws Exception
  {
    return data == null ? "&nbsp;" : df.formatDataFull(data);
  }

  public String formatDataOra(Date data)
     throws Exception
  {
    if(data == null)
      return "&nbsp;";

    String s = df.formatDataFull(data);
    return s.substring(0, s.length() - 3);
  }

  public String formatDataOggi(Date data)
     throws Exception
  {
    return data == null || !(data instanceof Date)
              ? df.formatData(new Date())
              : df.formatData(data);
  }

  public String formatDataFullOggi(Date data)
     throws Exception
  {
    return data == null || !(data instanceof Date)
              ? df.formatDataFull(new Date())
              : df.formatDataFull(data);
  }

  public String formatDataEta(Date data)
     throws Exception
  {
    if(data == null)
      return "&nbsp;";

    return DT.getEtaDescrizione(data, " " + i18n("anni"), " " + i18n("mesi"), " " + i18n("giorni"));
  }

  public String formatValuta(double valuta)
     throws Exception
  {
    return vf.fmtValuta(valuta);
  }

  public String formatNumero(double numero, int nInteri, int nDecimali)
     throws Exception
  {
    return nf.format(numero, nInteri, nDecimali);
  }

  public String formatQta(double qta)
     throws Exception
  {
    return nf.format(qta, 0, 2);
  }

  public String formatDiskSpace(double qta)
     throws Exception
  {
    if(qta > CoreConst.TERABYTE)
      return formatQta(qta / CoreConst.TERABYTE) + "T";
    if(qta > CoreConst.GIGABYTE)
      return formatQta(qta / CoreConst.GIGABYTE) + "G";
    if(qta > CoreConst.MEGABYTE)
      return formatQta(qta / CoreConst.MEGABYTE) + "M";
    if(qta > CoreConst.KILOBYTE)
      return formatQta(qta / CoreConst.KILOBYTE) + "K";

    return nf.format(qta, 0, 0);
  }

  public String formatSINO(boolean value)
     throws Exception
  {
    return value ? i18n("Si") : i18n("No");
  }

  public boolean isOkStr(Object o)
  {
    if(o == null)
      return false;

    String s = o.toString().trim();
    if(s.length() == 0 || s.startsWith("java.lang.Object"))
      return false;

    return true;
  }

  public String okStr(Object o)
  {
    if(o == null)
      return "&nbsp;";

    String s = o.toString().trim();
    if(s.length() == 0 || s.startsWith("java.lang.Object"))
      return "&nbsp;";

    return s;
  }

  public String okStr2(Object o)
  {
    if(o == null)
      return "";

    String s = o.toString().trim();
    if(s.length() == 0 || s.startsWith("java.lang.Object"))
      return "";

    return s;
  }

  public String okDescrizione(Object o)
  {
    try
    {
      if(o == null)
        return "&nbsp;";

      Method m = o.getClass().getMethod("getDescrizione", (Class[]) null);
      if(m == null)
        return "&nbsp;";

      return okStr(m.invoke(o, (Object[]) null));
    }
    catch(Exception ex)
    {
      return "&nbsp;";
    }
  }

  /**
   * Ritorna informazioni sul browser dell'utente.
   *
   * @return l'oggetto BrowserDetector
   * @throws java.lang.Exception
   */
  public EnancedBrowserDetector getBrowserInfo()
     throws Exception
  {
    HttpServletRequest request = getRequest();

    // tenta di recuperare il browserInfo dalla sessione utente
    EnancedBrowserDetector browserInfo = (EnancedBrowserDetector) (request.getSession().getAttribute("browserInfo"));

    if(browserInfo == null)
    {
      browserInfo = new EnancedBrowserDetector(request.getHeader("User-Agent"));
      if(!browserInfo.isCssOK() || !browserInfo.isJavascriptOK())
        throw new Exception(i18n("Spiacente: la versione del browser che stai usando "
           + "non e' compatibile. Ti consigliamo di "
           + "aggiornare il tuo browser ad una versione piu' recente."));

      request.getSession().setAttribute("browserInfo", browserInfo);
    }

    return browserInfo;
  }

  /**
   * Ritorna vero se il browser dell'utente e' Netscape/Mozilla.
   *
   * @return vero se Mozilla
   * @throws Exception
   */
  public boolean isMozilla()
     throws Exception
  {
    return getBrowserInfo().isMozilla();
  }

  /**
   * Ritorna vero se il browser dell'utente e' Microsoft Internet Explorer.
   *
   * @return vero se Internet Explorer
   * @throws Exception
   */
  public boolean isMsie()
     throws Exception
  {
    return getBrowserInfo().isMsie();
  }

  /**
   * Ritorna vero se il browser dell'utente e' Opera.
   *
   * @return vero se Opera
   * @throws Exception
   */
  public boolean isOpera()
     throws Exception
  {
    return getBrowserInfo().isOpera();
  }

  /**
   * Ritorna vero se il browser dell'utente e' Chrome.
   *
   * @return vero se chrome
   * @throws Exception
   */
  public boolean isChrome()
     throws Exception
  {
    return getBrowserInfo().isChrome();
  }

  /**
   * Ritorna vero se il browser dell'utente e' Safari.
   *
   * @return vero se chrome
   * @throws Exception
   */
  public boolean isSafari()
     throws Exception
  {
    return getBrowserInfo().isSafari();
  }

  /**
   * Ritorna vero se il browser supporta le dialog modali (showDialog()).
   * Controlla anche che non siano state disabilitate esplicitamente da setup.
   * @return vero se le dialog modali sono supportate
   * @throws Exception
   */
  public boolean haveDialog()
     throws Exception
  {
    // controlla disabilitazione esplicita a setup
    if(!TR.getBoolean("browser.modal.dialog.enabled", true))
      return false;

    return getBrowserInfo().isModalDialog();
  }

  public String getCompleteUrl(String url)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getServerScheme()).
       append("://").
       append(getServerName()).
       append(":").
       append(getServerPort()).
       append(getContextPath()).
       append("/").
       append(url.charAt(0) == '/' ? url.substring(1) : url);
    return sb.toString();
  }

  public String getHttpCompleteUrl(String url)
  {
    int httpPort = 8080;
    if(SU.isEqu("http", getServerScheme()))
      httpPort = getServerPort();

    StringBuilder sb = new StringBuilder();
    sb.append("http://").
       append(getServerName()).
       append(":").
       append(httpPort).
       append(getContextPath()).
       append("/").
       append(url.charAt(0) == '/' ? url.substring(1) : url);

    return sb.toString();
  }

  public String getHttpsCompleteUrl(String url)
  {
    int httpPort = 8443;
    if(SU.isEqu("https", getServerScheme()))
      httpPort = getServerPort();

    StringBuilder sb = new StringBuilder();
    sb.append("https://").
       append(getServerName()).
       append(":").
       append(httpPort).
       append(getContextPath()).
       append("/").
       append(url.charAt(0) == '/' ? url.substring(1) : url);

    return sb.toString();
  }

  public String getAbsoluteUrl(String url)
  {
    return getRequest().getContextPath() + "/" + url;
  }

  public String getTemplateUrl(String url)
  {
    return getRequest().getContextPath() + "/app/template/" + url;
  }

  public String getTemplateActionUrl(String url, String action)
  {
    return getRequest().getContextPath() + "/app/template/" + url + "/action/" + action;
  }

  private static String appletCodeBase = null;

  public String getAppletCodebase()
     throws Exception
  {
    if(appletCodeBase == null)
    {
      appletCodeBase = getResource("path.applet.codebase", "AUTO");

      if(appletCodeBase == null || appletCodeBase.equals("AUTO"))
      {
        // recupera il nome del server da TurbineResource.properties
        String serverName = TR.getString("serverdata.default.serverName", "localhost");

        int httpPort = 8080;
        if(SU.isEqu("http", getServerScheme()))
          httpPort = getServerPort();

        StringBuilder sb = new StringBuilder();
        sb.append("http://").
           append(serverName).
           append(":").
           append(httpPort).
           append(getContextPath()).
           append("/applets");

        appletCodeBase = sb.toString();
      }
    }

    return appletCodeBase;
  }

  public boolean haveRefresh()
  {
    return refresh != null;
  }

  public String getRefresh()
  {
    String rv = refresh;
    refresh = null;
    return rv;
  }

  public void setRefresh(int tref)
  {
    refresh = Integer.toString(tref);
  }

  /**
   * Ritorna vero se l'utente è l'amministratore di sistema.
   * @return vero se utente 'turbine'
   * @throws Exception
   */
  public boolean isAdmin()
     throws Exception
  {
    return SEC.isAdmin(this);
  }

  /**
   * Ritorna l'ID univoco dell'utente loggato.
   * @return ID utente (intero)
   */
  public int getUserID()
  {
    return SEC.getUserID(this);
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
    return SEC.checkAllPermission(this, permissions);
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
    return SEC.checkAnyPermission(this, permissions);
  }

  public User getUsersInfo(int idUser)
     throws Exception
  {
    return (User) SEC.getUser(idUser);
  }

  public String getResource(String key, String defval)
  {
    return TR.getString(key, defval);
  }

  public String getCampoData(String nomeCampo, String nomeForm, String valore, int size)
  {
    try
    {
      return modXML.getCampoData(nomeCampo, nomeForm, valore, size);
    }
    catch(Exception ex)
    {
      return "ERRORE: " + ex.getMessage();
    }
  }

  public String getCampoDataIntervalloInizio(String nomeCampoInizio, String nomeCampoFine,
     String nomeForm, String valore, int size)
  {
    try
    {
      return modXML.getCampoDataIntervalloInizio(nomeCampoInizio, nomeCampoFine, nomeForm, valore, size);
    }
    catch(Exception ex)
    {
      return "ERRORE: " + ex.getMessage();
    }
  }

  public String getCampoDataIntervalloFine(String nomeCampoInizio, String nomeCampoFine,
     String nomeForm, String valore, int size)
  {
    try
    {
      return modXML.getCampoDataIntervalloFine(nomeCampoInizio, nomeCampoFine, nomeForm, valore, size);
    }
    catch(Exception ex)
    {
      return "ERRORE: " + ex.getMessage();
    }
  }

  /**
   * Ritorna nome dello screen della home page.
   * @return
   */
  public String getHomeScreen()
  {
    return TR.getString("template.homepage", "Index.vm");
  }

  /**
   * Ritorna il link alla home page come da setup.
   * @return
   */
  public String getHomeLink()
  {
    if(homeLink == null)
    {
      String tmplHome = TR.getString("template.homepage");
      TemplateURI tui = new TemplateURI(this, tmplHome);
      homeLink = tui.getRelativeLink();
    }
    return homeLink;
  }

  public Locale getUserLocale()
  {
    if(lsrv == null)
      lsrv = (LocalizationService) TurbineServices.getInstance().
         getService(LocalizationService.SERVICE_NAME);

    Locale userLocale = (Locale) getSession().getAttribute("userLocale");

    if(userLocale == null)
    {
      userLocale = lsrv.getLocale(getRequest());
      getSession().setAttribute("userLocale", userLocale);
    }

    return userLocale;
  }

  public String i18n(String key)
  {
    if(lsrv == null)
      lsrv = (LocalizationService) TurbineServices.getInstance().
         getService(LocalizationService.SERVICE_NAME);

    return lsrv.getString(null, getUserLocale(), key);
  }

  public String i18n(String key, Object... params)
  {
    String value = i18n(key);
    return String.format(value, params);
  }

  public void throwMessagei18n(String key)
     throws ErrorMessageException
  {
    throw new ErrorMessageException(i18n(key));
  }

  public void throwMessagei18n(String key, Object... params)
     throws ErrorMessageException
  {
    throw new ErrorMessageException(i18n(key, params));
  }

  public void setMessagei18n(String key)
  {
    setMessage(i18n(key));
  }

  public void setMessagei18n(String key, Object... params)
  {
    setMessage(i18n(key, params));
  }

  public void addMessagei18n(String key)
  {
    addMessage(i18n(key));
  }

  public void addMessagei18n(String key, Object... params)
  {
    addMessage(i18n(key, params));
  }

  public void throwMessagei18n(List<String> err)
     throws ErrorMessageException
  {
    ArrayList<String> arTrans = new ArrayList<>();
    err.forEach((s) -> arTrans.add(i18n(s)));
    ErrorMessageException.throwErrorMessageException(arTrans);
  }
}
