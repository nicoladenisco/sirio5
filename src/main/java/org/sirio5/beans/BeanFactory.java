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
package org.sirio5.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.turbine.util.RunData;
import org.commonlib5.utils.ClassOper;
import org.sirio5.beans.shared.SessionRemoveBusHelper;
import org.sirio5.services.token.TokenAuthItem;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;

/**
 * Factory per la creazione dei bean di sessione.
 * Questa factory estrae dalla sessione o eventualmente crea ex novo
 * i bean di supporto alle pagine html.
 *
 * @author Nicola De Nisco
 */
public class BeanFactory
{
  private static String[] basePaths;
  private static final HashMap<String, String> nameMap = new HashMap<>();
  private static final ArrayList<Function<String, String>> nameResolvers = new ArrayList<>();

  /**
   * Ritorna path di base per la costruzione di bean.
   * @return array di paths
   */
  public static String[] getBasePaths()
  {
    return basePaths;
  }

  /**
   * Imposta path di base per la costruzione di bean.
   * @param basePath array di paths
   */
  public static void setBasePaths(String[] basePath)
  {
    BeanFactory.basePaths = basePath;
  }

  /**
   * Aggiunge path di base per la costruzione di bean.
   * @param basePath path di base per bean
   */
  public static void addBasePath(String basePath)
  {
    basePaths = (String[]) ArrayUtils.add(basePaths, basePath);
  }

  /**
   * Estrazione/costruzione di un bean di sessione.
   * In base alla classe del bean viene generata una chiave che verrà utilizzata
   * per estrarre/memorizzare il bean in sessione. Se la sessione contiene una istanza del bean
   * viene chiamato l'opportuno metodo isValid() del bean per verificare se ancora valido;
   * se non valido viene distrutto e si prosegue come non esistesse. Altrimenti viene ritornato
   * dopo aver chiamato la funzione refreshSession() del bean.
   * Se il bean non esiste in sessione viene cercato un eventuale override di runtime della classe
   * e quindi si crea una nuova istanza. Sulla nuova istanza viene chiamato il metodo init() per
   * consentire inizializzazioni una tantum.
   * @param <T> tipo del bean da estrarre/creare
   * @param data oggetto rundata con relativa sessione
   * @param beanClass classe del bean
   * @return una istanza del bean
   * @throws Exception
   */
  public static <T extends CoreBaseBean> T getFromSession(RunData data, Class<T> beanClass)
     throws Exception
  {
    return (T) getFromSession(data, beanClass, createSessionAttributeName(beanClass));
  }

  /**
   * Estrazione (senza costruzione) di un bean di sessione.
   * Come la getFromSession() ma il bean viene solo estratto dalla sessione se esiste,
   * altrimenti viene tornato null. ATTENZIONE: questa funzione NON chiama refreshSession().
   * @param <T> tipo del bean da estrarre/creare
   * @param session sessione HTTP
   * @param beanClass classe del bean
   * @return una istanza del bean o null se non presente
   * @throws Exception
   */
  public static <T extends CoreBaseBean> T getOnlyFromSession(HttpSession session, Class<T> beanClass)
     throws Exception
  {
    return (T) session.getAttribute(createSessionAttributeName(beanClass));
  }

  /**
   * Rimozione di bean dalla sessione.
   * Se esiste in sessione il bean viene rimosso. Nessun errore viene sollevato se il bean non esiste.
   * @param data oggetto rundata con relativa sessione
   * @param beanClass classe del bean
   */
  public static void removeFromSession(RunData data, Class beanClass)
  {
    removeFromSession(data.getSession(), beanClass);
  }

  /**
   * Rimozione di bean dalla sessione.
   * Se esiste in sessione il bean viene rimosso. Nessun errore viene sollevato se il bean non esiste.
   * @param session sessione corrente
   * @param beanClass classe del bean
   */
  public static void removeFromSession(HttpSession session, Class beanClass)
  {
    session.removeAttribute(createSessionAttributeName(beanClass));
  }

  /**
   * Determina il nome dell'attributo di sessione a partire dalla classe richiesta.
   * @param beanClass classe del bean
   * @return stringa nome attributo di sessione
   */
  public static String createSessionAttributeName(Class beanClass)
  {
    return "BEAN:" + beanClass.getName();
  }

  /**
   * Estrazione/costruzione di un bean di sessione.
   * Vedi getFromSession() per una descrizione dettagliata.
   * Uso interno.
   * @param data oggetto rundata con relativa sessione
   * @param beanClass classe del bean
   * @param key stringa nome attributo di sessione
   * @return una istanza del bean
   * @throws Exception
   */
  public static CoreBaseBean getFromSession(RunData data, Class beanClass, String key)
     throws Exception
  {
    CoreBaseBean bean = (CoreBaseBean) data.getSession().getAttribute(key);

    if(bean == null || !bean.isValid((CoreRunData) data))
    {
      removeFromSession(data, key);
      bean = createBean(beanClass);
      bean.init((CoreRunData) data);
      data.getSession().setAttribute(key, bean);
    }
    else
    {
      bean.refreshSession((CoreRunData) data);
    }

    return bean;
  }

  /**
   * Rimozione di attributo dalla sessione.
   * @param data oggetto rundata con relativa sessione
   * @param key stringa nome attributo di sessione
   */
  public static void removeFromSession(RunData data, String key)
  {
    data.getSession().removeAttribute(key);
  }

  /**
   * Creazione istanza di bean con verifica di override.
   * Uso interno.
   * @param beanClass classe del bean
   * @return una istanza del bean
   * @throws Exception
   */
  public static CoreBaseBean createBean(Class beanClass)
     throws Exception
  {
    String myPackage = ClassOper.getClassPackage(beanClass);
    String className = ClassOper.getClassName(beanClass);
    Class beanClassOverride = ClassOper.loadClass(getClassnameOverride(className), myPackage, basePaths);
    if(beanClassOverride != null)
      beanClass = beanClassOverride;
    return (CoreBaseBean) beanClass.newInstance();
  }

  /**
   * Verifica per override di setup del bean indicato.
   * @param nomeBean nome del bean di cui si chiede override
   * @return eventuale override se presente altrimenti nomeBean
   */
  public static String getClassnameOverride(String nomeBean)
  {
    String tmp;

    if((tmp = SU.okStrNull(nameMap.get(nomeBean))) != null)
      return tmp;

    for(Function<String, String> fr : nameResolvers)
    {
      if((tmp = SU.okStrNull(fr.apply(nomeBean))) != null)
      {
        nameMap.put(nomeBean, tmp);
        return tmp;
      }
    }

    return nomeBean;
  }

  /**
   * Aggiunge override esplicito.
   * @param originalName nome del bean originale
   * @param overrideName nome del bean sostituzione (deve estendere originalName)
   */
  public static void addBeanOverride(String originalName, String overrideName)
  {
    nameMap.put(originalName, overrideName);
  }

  /**
   * Aggiunge un risolutore di override.
   * Il risolutore verrà utilizzato da getClassnameOverride() per determinare
   * se il bean che si sta per costruire ha un override di classe.
   * @param fr risolutore di override
   */
  public static void addOverrideResolver(Function<String, String> fr)
  {
    nameResolvers.add(fr);
  }

  /**
   * Registra helper per rimozione bean in risposta eventi di bus.
   * Questa funzione registra un helper per la rimozione automatica di un bean
   * dalla sessione quando sul bus messaggi viene captato uno qualunque degli eventi indicati.
   * Internamente usa {@link #SessionRemoveBusHelper}.
   * @param session sessione di riferimento per la registrazione dell'helper
   * @param beanClass classe del bean da rimuovere (la stessa utilizzata per getFromSession())
   * @param msgIDs eventi che causano la rimozione dalla sessione
   */
  public static void registerRemoveInfo(HttpSession session, Class beanClass, int... msgIDs)
  {
    SessionRemoveBusHelper.registerRemoveInfo(session, createSessionAttributeName(beanClass), msgIDs);
  }

  /**
   * Estrazione/costruzione di un bean di sessione token.
   * Esegue le stesse funzioni di getFromSession() ma utilizza un token di autenticazione
   * come sessione. I due oggetti sono molto equivalenti.
   * @param <T> tipo del bean da estrarre/creare
   * @param data oggetto rundata con relativa sessione
   * @param beanClass classe del bean
   * @return una istanza del bean
   * @throws Exception
   */
  public static <T extends CoreTokenBean> T getFromToken(TokenAuthItem data, Class<T> beanClass)
     throws Exception
  {
    return (T) getFromToken(data, beanClass, createSessionAttributeName(beanClass));
  }

  /**
   * Estrazione (senza costruzione) di un bean di sessione token.
   * Come la getFromToken() ma il bean viene solo estratto dalla sessione se esiste,
   * altrimenti viene tornato null. ATTENZIONE: questa funzione NON chiama refreshSession().
   * @param <T> tipo del bean da estrarre/creare
   * @param data dati del token
   * @param beanClass classe del bean
   * @return una istanza del bean o null se non presente
   * @throws Exception
   */
  public static <T extends CoreTokenBean> T getOnlyFromToken(TokenAuthItem data, Class<T> beanClass)
     throws Exception
  {
    return (T) data.getAttribute(createSessionAttributeName(beanClass));
  }

  /**
   * Rimozione di bean dalla sessione token.
   * Se esiste in sessione il bean viene rimosso. Nessun errore viene sollevato se il bean non esiste.
   * @param data oggetto rundata con relativa sessione
   * @param beanClass classe del bean
   */
  public static void removeFromToken(TokenAuthItem data, Class beanClass)
  {
    data.removeAttribute(createSessionAttributeName(beanClass));
  }

  /**
   * Estrazione/costruzione di un bean di token.
   * Vedi getFromSession() per una descrizione dettagliata.
   * Uso interno.
   * @param data pacchetto dati associati al token
   * @param beanClass classe del bean
   * @param key stringa nome attributo di sessione
   * @return una istanza del bean
   * @throws Exception
   */
  public static CoreTokenBean getFromToken(TokenAuthItem data, Class beanClass, String key)
     throws Exception
  {
    CoreTokenBean bean = (CoreTokenBean) data.getAttribute(key);

    if(bean == null || !bean.isValid(data))
    {
      removeFromToken(data, key);
      bean = createTokenBean(beanClass);
      bean.init(data);
      data.setAttribute(key, bean);
    }
    else
    {
      bean.refreshSession(data);
    }

    return bean;
  }

  /**
   * Rimozione di attributo dalla sessione.
   * @param data oggetto rundata con relativa sessione
   * @param key stringa nome attributo di sessione
   */
  public static void removeFromToken(TokenAuthItem data, String key)
  {
    data.removeAttribute(key);
  }

  /**
   * Creazione istanza di bean con verifica di override.
   * Uso interno.
   * @param beanClass classe del bean
   * @return una istanza del bean
   * @throws Exception
   */
  public static CoreTokenBean createTokenBean(Class beanClass)
     throws Exception
  {
    String myPackage = ClassOper.getClassPackage(beanClass);
    String className = ClassOper.getClassName(beanClass);
    Class beanClassOverride = ClassOper.loadClass(getClassnameOverride(className), myPackage, basePaths);
    if(beanClassOverride != null)
      beanClass = beanClassOverride;
    return (CoreTokenBean) beanClass.newInstance();
  }
}
