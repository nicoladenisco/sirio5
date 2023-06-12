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
package org.sirio5.modules.screens;

import java.sql.SQLException;
import java.util.Map;
import org.apache.torque.TorqueException;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.*;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.uri.TemplateURI;
import org.apache.velocity.context.Context;
import org.commonlib5.utils.ArrayMap;
import org.sirio5.ErrorMessageException;
import org.sirio5.beans.BeanFactory;
import org.sirio5.beans.CoreBaseBean;
import org.sirio5.beans.NavigationStackBean;
import org.sirio5.rigel.ConcurrentDatabaseModificationException;
import org.sirio5.rigel.UnmodificableRecordException;
import org.sirio5.services.allarmi.ALLARM;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;

/**
 * Classe base di tutti gli screen.
 *
 * FILENOI8N
 * @author Nicola De Nisco
 */
public class CoreBaseScreen extends VelocitySecureScreen
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
   * Per default tutti gli screen sono permessi purche' l'utente
   * sia loggato.
   *
   * @param pd Turbine information.
   * @return True if the user is authorized to access the screen.
   * @exception Exception, a generic exception.
   */
  @Override
  protected boolean isAuthorized(PipelineData pd)
     throws Exception
  {
    CoreRunData data = (CoreRunData) getRunData(pd);
    return isAuthorized(data);
  }

  protected boolean isValidSession(RunData data)
  {
    // il controllo sulla sessione nuova blocca l'autologon da cookie
    //if(!data.getUser().hasLoggedIn() || data.getSession().isNew())

    if(!data.getUser().hasLoggedIn())
    {
      // autorizzazione non concessa
      String loginScreen = TR.getString("template.login", "Login.vm"); // NOI18N
      data.getTemplateInfo().setScreenTemplate(loginScreen);
      return false;
    }

    return true;
  }

  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    if(!isValidSession(data))
      return false;

    return true;
  }

  protected boolean isAuthorizedAll(CoreRunData data, String permissions)
     throws Exception
  {
    if(!isValidSession(data))
      return false;

    if(SEC.checkAllPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean isAuthorizedAny(CoreRunData data, String permissions)
     throws Exception
  {
    if(!isValidSession(data))
      return false;

    if(SEC.checkAnyPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean isAuthorizedOne(CoreRunData data, String permissions)
     throws Exception
  {
    if(!isValidSession(data))
      return false;

    if(SEC.checkAnyPermission(data, permissions))
      return true;

    return redirectUnauthorized(data);
  }

  protected boolean redirectUnauthorized(CoreRunData data)
     throws Exception
  {
    User user = data.getUser();
    if(user.hasLoggedIn())
    {
      // ATTENZIONE: qui siamo già in uno screen, quindi non basta cambiare
      // il template (come sarebbe sufficiente in una action): occorre fare una redirect.
      // Diversamente il controllore di nopermessi.vm non viene interpellato e quindi il context sarà vuoto.
      redirectToTemplate(data, "nopermessi.vm"); // NOI18N
    }
    else
    {
      String loginScreen = TR.getString("template.login", "Login.vm"); // NOI18N
      data.getTemplateInfo().setScreenTemplate(loginScreen);
    }
    return false;
  }

  /**
   * Effettua una redirect dalla pagina corrente a quella indicate.
   * @param data
   * @param screenTemplate nome della vm su cui atterrare
   */
  protected void redirectToTemplate(CoreRunData data, String screenTemplate)
  {
    TemplateURI tui = new TemplateURI(data, screenTemplate);
    data.setStatusCode(302);
    data.setRedirectURI(tui.getRelativeLink());
    data.setScreenTemplate(screenTemplate);
  }

  @Override
  final protected void doBuildTemplate(PipelineData data, Context context)
     throws Exception
  {
    int numAllarmi = 0;
    CoreRunData rdata = (CoreRunData) getRunData(data);

    if((numAllarmi = ALLARM.getActiveAllarms()) != 0)
    {
      if(SEC.checkAnyPermission(rdata, "operatore_allarmi")) // NOI18N
      {
        // operatore allarmi
        context.put("allarme", rdata.i18n("Attenzione: ci sono %d allarmi che richiedono attenzione.", numAllarmi));
      }
      else
      {
        // altri utenti non autorizzati
        context.put("allarme", rdata.i18n("Avvisare un operatore per la risoluzione degli allarmi."));
      }
    }

    try
    {
      doBuildTemplate2(rdata, context);
    }
    catch(ErrorMessageException ex)
    {
      rdata.setMessage(ex.getMessage());
    }
    catch(TorqueException ex)
    {
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
  }

  protected void doBuildTemplate2(CoreRunData data, Context context)
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

    doBuildTemplate2(data, context, bean);
  }

  protected void doBuildTemplate2(CoreRunData data, Context context, CoreBaseBean bean)
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

  protected void pushUriStackBean(CoreRunData data, String uri, String descrizione)
  {
    try
    {
      NavigationStackBean bean = BeanFactory.getFromSession(data, NavigationStackBean.class);
      bean.pushUri(data, uri, descrizione);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected void pushUriStackBean(CoreRunData data, String uri, String descrizione, Map<String, String> parameters)
  {
    try
    {
      NavigationStackBean bean = BeanFactory.getFromSession(data, NavigationStackBean.class);
      bean.pushUri(uri, descrizione, parameters);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected void pushUriStackBean(CoreRunData data, String uri, String descrizione, Object... parameters)
  {
    if((parameters.length & 1) != 0)
      throw new IllegalArgumentException();

    pushUriStackBean(data, uri, descrizione, SU.pair2Map(new ArrayMap<>(), parameters));
  }

  protected void pushClearUriStackBean(CoreRunData data, String uri, String descrizione)
  {
    try
    {
      NavigationStackBean bean = BeanFactory.getFromSession(data, NavigationStackBean.class);
      bean.clear();
      bean.pushUri(data, uri, descrizione);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected void throwMessage(String message)
     throws ErrorMessageException
  {
    throw new ErrorMessageException(message);
  }

  public boolean getBooleanInContext(Context context, String key, boolean defVal)
     throws Exception
  {
    return SU.checkTrueFalse(context.get(key), defVal);
  }

  public String getStringInContext(Context context, String key, String defVal)
     throws Exception
  {
    return SU.okStr(context.get(key), defVal);
  }

  public int getIntInContext(Context context, String key, int defVal)
     throws Exception
  {
    return SU.parse(context.get(key), defVal);
  }

  public double getFloatInContext(Context context, String key, double defVal)
     throws Exception
  {
    return SU.parse(context.get(key), defVal);
  }

  public String getHomeScreen()
  {
    return TR.getString("template.homepage", "Index.vm");
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

  /**
   * Esecuzione di comandi. La forma invia un parametro speciale chiamato
   * 'command' con una stringa identificativa dell'operazione richiesta
   * dall'utente. Questa stringa diviene parte di un metodo doCmd_stringa
   * ricercato a runtime e se presente eseguito. Vedi doCmd_... per ulteriori
   * dettagli.
   *
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri request più eventuali parametri permanenti
   * @param args argomenti vari
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
}
