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
package org.sirio5.modules.actions;

import java.sql.SQLException;
import java.util.Map;
import org.apache.torque.TorqueException;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.*;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.uri.TemplateURI;
import org.apache.velocity.context.Context;
import org.sirio5.CsrfProtectionException;
import org.sirio5.ErrorMessageException;
import org.sirio5.RedirectMessageException;
import org.sirio5.beans.BeanFactory;
import org.sirio5.beans.CoreBaseBean;
import org.sirio5.beans.NavigationStackBean;
import org.sirio5.rigel.ConcurrentDatabaseModificationException;
import org.sirio5.rigel.UnmodificableRecordException;
import org.sirio5.services.modellixml.modelliXML;
import org.sirio5.services.security.SEC;
import org.sirio5.services.token.TokenAuthService;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.LI;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Classe base di tutte le action.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class CoreBaseAction extends VelocitySecureAction
{
  private Class beanClass = null;

  public Class getBeanClass()
  {
    return beanClass;
  }

  public void setBeanClass(Class beanClass)
  {
    this.beanClass = beanClass;
  }

  public <T> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  public RunData getRunData(PipelineData data)
  {
    return data.getRunData();
  }

  /**
   * Overide this method to perform the security check needed.
   * Per default tutti gli screen sono permessi purche' l'utente sia loggato.
   *
   * @param pd Turbine information.
   * @return True if the user is authorized to access the screen.
   * @throws java.lang.Exception
   */
  @Override
  final protected boolean isAuthorized(PipelineData pd)
     throws Exception
  {
    CoreRunData data = (CoreRunData) getRunData(pd);

    if(!isValidSession(data))
      return false;

    return isAuthorized(data);
  }

  protected boolean isValidSession(RunData data)
  {
    // il controllo sulla sessione nuova blocca l'autologon da cookie
    //if(!data.getUser().hasLoggedIn() || data.getSession().isNew())

    if(!data.getUser().hasLoggedIn())
    {
      // autorizzazione non concessa
      ((CoreRunData) data).setMessagei18n("E' necessario effettuare la logon!");
      String loginScreen = TR.getString("template.login", "Login.vm"); // NOI18N
      data.getTemplateInfo().setScreenTemplate(loginScreen);
      return false;
    }

    return true;
  }

  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return true;
  }

  protected boolean isAuthorizedAll(CoreRunData data, String permissions)
     throws Exception
  {
    if(SEC.checkAllPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean isAuthorizedAny(CoreRunData data, String permissions)
     throws Exception
  {
    if(SEC.checkAnyPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean isAuthorizedOne(CoreRunData data, String permissions)
     throws Exception
  {
    if(SEC.checkAnyPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean isAbbandona(RunData data, String... abbandona)
     throws Exception
  {
    // se l'utente non è loggato è da decidere
    if(!data.getUser().hasLoggedIn())
      return false;

    String command = SU.okStrNull(data.getParameters().getString("command"));
    if(command == null)
      return false;

    // il comando abbandona viene sempre considerato valido; diversamente non si esce dalle maschere
    for(String ab : abbandona)
    {
      if(command.equalsIgnoreCase(ab))
        return true;
    }

    // negli altri casi è da decidere
    return false;
  }

  protected boolean redirectUnauthorized(CoreRunData data)
     throws Exception
  {
    if(data.getUser().hasLoggedIn())
    {
      data.getTemplateInfo().setScreenTemplate("nopermessi.vm"); // NOI18N
    }
    else
    {
      String loginScreen = TR.getString("template.login", "Login.vm"); // NOI18N
      data.getTemplateInfo().setScreenTemplate(loginScreen);
    }
    return false;
  }

  public static boolean getBooleanInContext(Context context, String key, boolean defVal)
     throws Exception
  {
    return SU.checkTrueFalse(context.get(key), defVal);
  }

  public static String getStringInContext(Context context, String key, String defVal)
     throws Exception
  {
    return SU.okStr(context.get(key), defVal);
  }

  public static int getIntInContext(Context context, String key, int defVal)
     throws Exception
  {
    return SU.parse(context.get(key), defVal);
  }

  public static double getFloatInContext(Context context, String key, double defVal)
     throws Exception
  {
    return SU.parse(context.get(key), defVal);
  }

  public String getHomeScreen()
  {
    return TR.getString("template.homepage", "Index.vm"); // NOI18N
  }

  /**
   * Ritorna il link alla home page come da setup.
   * @param data
   * @return
   */
  public String getHomeLink(RunData data)
  {
    TemplateURI tui = new TemplateURI(data, getHomeScreen());
    return tui.getRelativeLink();
  }

  public void gotoHome(RunData data)
  {
    data.getTemplateInfo().setLayoutTemplate("Default.vm"); // NOI18N
    data.getTemplateInfo().setScreenTemplate(getHomeScreen());
  }

  @Override
  final public void perform(PipelineData data)
     throws Exception
  {
    CoreRunData rdata = (CoreRunData) getRunData(data);

    try
    {
      super.perform(data);
    }
    catch(ErrorMessageException ex)
    {
      rdata.setMessage(ex.getMessage());
    }
    catch(TorqueException ex)
    {
      // ispeziona la causa per verificare se gestirla come sqlexception
      if(ex.getCause() != null && ex.getCause() instanceof SQLException)
      {
        SQLException sqe = (SQLException) ex.getCause();
        SU.reportNonFatalDatabaseError(rdata, sqe);
      }
      else
        SU.reportNonFatalDatabaseError(rdata, ex);
    }
    catch(SQLException ex)
    {
      SU.reportNonFatalDatabaseError(rdata, ex);
    }
    catch(ConcurrentDatabaseModificationException ex)
    {
      SU.reportConcurrentDatabaseError(rdata, ex);
    }
    catch(UnmodificableRecordException ex)
    {
      SU.reportUnmodificableRecordError(rdata, ex);
    }
    catch(RedirectMessageException ex)
    {
      rdata.setMessage(ex.getMessage());
      rdata.setRedirectURI(ex.getUri().toString());
      rdata.setStatusCode(302);
    }
  }

  @Override
  final public void doPerform(PipelineData data, Context context)
     throws Exception
  {
    CoreRunData rdata = (CoreRunData) getRunData(data);

    if(!rdata.getUser().hasLoggedIn())
    {
      // autorizzazione non concessa
      rdata.setMessagei18n("E' necessario effettuare la logon!");
      String loginScreen = TR.getString("template.login", "Login.vm"); // NOI18N
      rdata.setScreenTemplate(loginScreen);
      return;
    }

    doPerform2(rdata, context);
  }

  protected void doPerform2(CoreRunData data, Context context)
     throws Exception
  {
    CoreBaseBean bean = null;
    if(beanClass != null)
    {
      synchronized(this)
      {
        bean = BeanFactory.getFromSession(data, beanClass);
        context.put("bean", bean);
      }
    }

    doPerform2(data, context, bean);
  }

  protected void doPerform2(CoreRunData data, Context context, CoreBaseBean bean)
     throws Exception
  {
  }

  protected void removeBeanFromSession(CoreRunData data)
  {
    if(beanClass == null)
      throw new RuntimeException(data.i18n(
         "La classe del bean non è stata impostata: occorre utilizzare il metodo setBeanClass()."));

    BeanFactory.removeFromSession(data, beanClass);
  }

  protected void throwMessage(String message)
     throws ErrorMessageException
  {
    throw new ErrorMessageException(message);
  }

  protected boolean verificaPaginaRitorno(CoreRunData data, CoreBaseBean bean)
     throws Exception
  {
    return verificaPaginaRitorno(data, SU.okStrNull(bean.getJlc()), SU.okStrNull(bean.getJvm()));
  }

  protected boolean verificaPaginaRitorno(CoreRunData data, String jlc, String jvm)
     throws Exception
  {
    if(TR.getBoolean("navigation.stack.usefirst", false))
    {
      // verifica lo stack di navigazione per eventuale indirizzo di ritorno
      if(NavigationStackBean.ret2Session(data))
        return true;
    }

    if(jlc != null && jlc.contains(".vm"))
    {
      // si ritorna ad una generica pagina velocity come specificato in jlc
      data.getTemplateInfo().setScreenTemplate(jlc);
      return true;
    }
    else if(jlc != null && jlc.contains("maint-"))
    {
      // se jlc inizia per maint- sicuramente si ritorna a maint.vm
      if(jvm == null)
        jvm = "maint.vm";

      data.getTemplateInfo().setScreenTemplate(jvm);
      data.getParameters().setString("type", jlc);
      return true;
    }
    else if(jlc != null && !jlc.contains("."))
    {
      // se jlc non contiene punti (.html, .vm, ecc.) probabilmente è il nome di una lista
      if(jvm == null)
        jvm = "maint.vm";

      data.getTemplateInfo().setScreenTemplate(jvm);
      data.getParameters().setString("type", jlc);
      return true;
    }
    else if(jlc != null && jlc.contains(".") && jvm == null)
    {
      // se jlc contiene punti (.html, .vm, ecc.) è una pagina di atterraggio
      data.setStatusCode(302);
      data.setRedirectURI(LI.getLinkUrl(jlc));
      return true;
    }
    else if(jvm != null && jvm.contains("."))
    {
      // se jvm contiene punti (.html, .vm, ecc.) è una pagina di atterraggio
      data.setStatusCode(302);
      String retUri = LI.getLinkUrl(jvm);

      // aggiunge eventuali parametri passati in jlc
      if(jlc != null)
      {
        Map<String, String> argomenti = SU.string2Map(jlc, ",", true);
        retUri = LI.mergeUrl(retUri, argomenti);
      }

      data.setRedirectURI(retUri);
      return true;
    }
    else if(jlc == null && jvm != null)
    {
      data.getTemplateInfo().setScreenTemplate(jvm);
      return true;
    }
    else
    {
      // verifica lo stack di navigazione per eventuale indirizzo di ritorno
      if(NavigationStackBean.ret2Session(data))
        return true;
    }

    return false;
  }

  /**
   * Esecuzione di comandi.
   * La forma invia un parametro speciale chiamato
   * 'command' con una stringa identificativa dell'operazione richiesta
   * dall'utente. Questa stringa diviene parte di un metodo doCmd_stringa
   * ricercato a runtime e se presente eseguito. Vedi doCmd_... per ulteriori
   * dettagli.
   *
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri request più eventuali parametri
   * permanenti
   * @param args
   * @throws Exception
   */
  public void doCommand(String command, CoreRunData data, Map params, Object... args)
     throws Exception
  {
    SU.doCommand(this, command, data, params, args);
  }

  public void ASSERT(boolean test, String cause)
  {
    if(!test)
    {
      String mess = "ASSERT failed: " + cause;
      log.error(mess);
      throw new RuntimeException(mess);
    }
  }

  protected void checkTokenCSRF(CoreRunData data, boolean obbligatorio)
     throws Exception
  {
    if("GET".equals(data.getRequest().getMethod()) && data.getParameters().getKeys().length < 10)
      return;

    String token = data.getParameters().getString(modelliXML.CSRF_TOKEN_FIELD_NAME);

    if(obbligatorio && token == null)
      throw new CsrfProtectionException("Missing token in request.");

    if(token == null)
      return;

    TokenAuthService tas = getService(TokenAuthService.SERVICE_NAME);
    int verifica = tas.verificaTokenAntiCSRF(token, true, data.getRequest(), data.getSession());

    switch(verifica)
    {
      case 0:
        return;

      case 1:
        throw new CsrfProtectionException("Unknow token in request.");
      case 2:
        throw new CsrfProtectionException("Invalid token in request.");
    }
  }
}
