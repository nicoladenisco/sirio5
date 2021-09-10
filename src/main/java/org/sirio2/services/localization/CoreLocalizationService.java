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
package org.sirio2.services.localization;

import java.io.InputStream;
import java.util.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.localization.DefaultLocalizationService;
import org.apache.fulcrum.localization.LocaleTokenizer;
import org.apache.turbine.Turbine;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.sirio2.utils.SU;

/**
 * Servizio di localizzazione specializzato.
 * Estende le funzioni del serivzio localizzazione di Turbine,
 * aggiungendo anche la gestione dei files translation.xml.
 *
 * Le stringhe non presenti in translation.xml sono inviate alla log
 * con 'Unknow key [stringa non presente]'. Questo consente di
 * recuperarle velocemente con un codice tipo:
 *
 * <code>
 * cat services.log | perl -n -e'/Unknow key \[(.+)\]/ && print $1 . "\n"' | sort | uniq
 * </code>
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class CoreLocalizationService extends DefaultLocalizationService
{
  /** Logging */
  private static Log log = LogFactory.getLog(CoreLocalizationService.class);
  protected String xmlPath = null;
  protected Locale displayLocale = null;
  protected Set<String> defaultSet = new HashSet<>();
  protected Set<Locale> availableXmlLocale = new HashSet<>();
  protected Map<Locale, Map<String, String>> allXmlMessageMap = new HashMap<>();
  protected Map<String, Locale> cacheMatch = new HashMap<>();
  protected Locale originLocale = Locale.ITALY;
  protected boolean outputUnknowKey = false;
  protected Map<String, Map<Locale, ResourceBundle>> bundles = new HashMap<>();

  public CoreLocalizationService()
  {
    Configuration conf = Turbine.getConfiguration();
    xmlPath = conf.getString("locale.xmlFile", null);

    // carica tutti i messaggi di default
    if(xmlPath != null)
      caricaDefaultXML();

    // imposta locale da visualizzare di default
    Locale jvmDefault = Locale.getDefault();
    String sShowLang = conf.getString("locale.to.display.language", jvmDefault.getLanguage()).trim();
    String sShowCoun = conf.getString("locale.to.display.country", jvmDefault.getCountry()).trim();
    displayLocale = new Locale(sShowLang, sShowCoun);

    // imposta localizzatore nella INT di om
    INT.setService(this);

    // imposta emissione log delle stringe non localizzate
    outputUnknowKey = conf.getBoolean("outputUnknowKey", outputUnknowKey);
  }

  /**
   * Carica messaggi DEFAULT dal file XML.
   * Verranno utilizzati per determinare le traduzioni da completare.
   */
  protected void caricaDefaultXML()
  {
    try (InputStream is = this.getClass().getResourceAsStream(xmlPath))
    {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(is);

      Element root = doc.getRootElement();
      Element languages = root.getChild("LANGUAGES");
      List<Element> langList = languages.getChildren("LANG");

      availableXmlLocale.clear();
      for(Element e : langList)
      {
        String language = e.getAttributeValue("language");
        String country = e.getAttributeValue("country");
        Locale l = new Locale(language, country);
        availableXmlLocale.add(l);

        String key = e.getAttributeValue("key");
        if(key.equals("DEFAULT"))
          originLocale = l;
      }

      defaultSet.clear();
      Element messages = root.getChild("MESSAGES");
      List<Element> msgList = messages.getChildren("MSG");
      for(Element e : msgList)
      {
        String defmsg = e.getChildTextTrim("DEFAULT");
        if(!StringUtils.isEmpty(defmsg))
          if(!defaultSet.add(StringUtils.deleteWhitespace(defmsg)))
            log.warn("key [" + defmsg + "] duplicated");
      }

      log.debug("Caricati " + defaultSet.size() + " messaggi.");
    }
    catch(Exception e)
    {
      log.error("Error loading localized XML string.", e);
    }
  }

  /**
   * Carica mappa messaggi/traduzioni per la locale indicata dal file XML.
   * @param l locale richiesta
   * @return mappa relativa (può essere vuota ma non è mai nulla)
   */
  protected Map<String, String> caricaMessaggiXML(Locale l)
  {
    Map<String, String> msgMap;

    if((msgMap = allXmlMessageMap.get(l)) != null)
      return msgMap;

    msgMap = new HashMap<String, String>();

    try (InputStream is = this.getClass().getResourceAsStream(xmlPath))
    {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(is);

      Element root = doc.getRootElement();
      Element languages = root.getChild("LANGUAGES");
      List<Element> langList = languages.getChildren("LANG");
      Element choosed = null;

      for(Element e : langList)
      {
        String language = e.getAttributeValue("language");
        String country = e.getAttributeValue("country");

        // se la lingua corrisponde è un candidato valido (il primo nell'ordine)
        if(choosed == null && SU.isEquNocase(language, l.getLanguage()))
          choosed = e;

        // se anche la nazione corrisponde allora è perfetto
        if(SU.isEquNocase(language, l.getLanguage()) && SU.isEquNocase(country, l.getCountry()))
        {
          choosed = e;
          break;
        }
      }

      // carica i messaggi per la locale individuata
      if(choosed != null)
      {
        String key = choosed.getAttributeValue("key");
        String alt = choosed.getAttributeValue("alternate");
        boolean isOkAlt = SU.isOkStr(alt);

        if(!key.equals("DEFAULT"))
        {
          Element messages = root.getChild("MESSAGES");
          List<Element> msgList = messages.getChildren("MSG");
          for(Element e : msgList)
          {
            String defmsg = e.getChildTextTrim("DEFAULT");
            String locmsg = e.getChildTextTrim(key);

            if(isOkAlt && locmsg.isEmpty())
              locmsg = e.getChildTextTrim(alt);

            // nota: la chiave è senza spazi per evitare incoerenze di formattazione
            if(!StringUtils.isEmpty(locmsg))
              msgMap.put(StringUtils.deleteWhitespace(defmsg), locmsg);
          }
        }
      }
    }
    catch(Exception e)
    {
      log.error("Error loading localized XML string.", e);
    }

    allXmlMessageMap.put(l, msgMap);
    return msgMap;
  }

  @Override
  public String getString(String bundleName, Locale locale, String key)
  {
    String value = null;

    if((key = SU.okStrNull(key)) == null)
      return null;

    if(locale == null)
    {
      // se locale è nullo si intende quello di default
      locale = displayLocale;
    }

    if(xmlPath != null)
    {
      // nota: la chiave è senza spazi per evitare incoerenze di formattazione
      String mapKey = StringUtils.deleteWhitespace(key);

      if(defaultSet.contains(mapKey))
      {
        // se la locale richiesta è it_IT la stringa si intende già tradotta
        if(locale.equals(originLocale))
          return key;

        // cerca prima nelle traduzioni xml
        Map<String, String> msgMap = caricaMessaggiXML(locale);
        if((value = msgMap.get(mapKey)) != null)
          return value;
      }
      else
      {
        if(outputUnknowKey)
          log.info("Unknow key [" + key + "]");
      }
    }

    // aggiusta eventuale bundle name al default se null
    bundleName = SU.okStr(bundleName, getDefaultBundleName());

    // Look for text in requested bundle.
    ResourceBundle rb = getBundle(bundleName, locale);
    if(rb != null)
      if((value = getStringOrNull(rb, key)) != null)
        return value;

    // Look for text in list of default bundles.
    String[] bNames = getBundleNames();
    if(bNames.length > 1)
    {
      for(int i = 0; i < bNames.length; i++)
      {
        String bn = bNames[i];
        if(!bn.equals(bundleName))
        {
          if((rb = getBundle(bn, locale)) != null)
            if((value = getStringOrNull(rb, key)) != null)
              return value;
        }
      }
    }

    if(outputUnknowKey)
      log.debug("Missing localization for [" + key + "] " + locale);

    return key;
  }

  /**
   * Ritorna la locale in base all'header del browser.
   * Verifica fra le locali supportate, ovvero quelle indicate nel file XML,
   * e ritorna quella più attinente. Nella stringa header sono indicate le locale
   * richieste dal browser in ordine di priorità.
   * @param header header del browser
   * @return la locale adatta all'utente
   */
  @Override
  public Locale getLocale(String header)
  {
    if(header == null)
      return displayLocale;

    Locale rv = cacheMatch.get(header);
    if(rv == null)
    {
      rv = getLocaleInternal(header);
      cacheMatch.put(header, rv);
    }

    return rv;
  }

  private Locale getLocaleInternal(String header)
  {
    if(!StringUtils.isEmpty(header))
    {
      LocaleTokenizer tok = new LocaleTokenizer(header);
      while(tok.hasNext())
      {
        Locale l = (Locale) tok.next();

        // match language e country: ottimo ritorniamo subito
        if(availableXmlLocale.contains(l))
          return l;

        // verifica del solo language: risultato accettabile
        for(Locale al : availableXmlLocale)
        {
          if(SU.isEqu(l.getLanguage(), al.getLanguage()))
            return al;
        }
      }
    }

    // nessuna delle lingue richieste è supportata: ritorna quella impostata a setup
    return displayLocale;
  }

  /**
   * This method returns a ResourceBundle for the given bundle name
   * and the given Locale.
   *
   * @param bundleName Name of bundle (or <code>null</code> for the
   * default bundle).
   * @param locale The locale (or <code>null</code> for the locale
   * indicated by the default language and country).
   * @return A localized ResourceBundle.
   */
  @Override
  public ResourceBundle getBundle(String bundleName, Locale locale)
  {
    try
    {
      // aggiusta eventuale bundle name al default se null
      bundleName = SU.okStr(bundleName, getDefaultBundleName());

      if(locale == null)
      {
        locale = getLocale((String) null);
      }

      // Find/retrieve/cache bundle.
      ResourceBundle rb = null;
      Map<Locale, ResourceBundle> bundlesByLocale = bundles.get(bundleName);
      if(bundlesByLocale != null)
      {
        // Cache of bundles by locale for the named bundle exists.
        // Check the cache for a bundle corresponding to locale.
        rb = bundlesByLocale.get(locale);

        if(rb == null)
        {
          // Not yet cached.
          rb = cacheBundle(bundleName, locale);
        }
      }
      else
      {
        rb = cacheBundle(bundleName, locale);
      }

      return rb;
    }
    catch(Throwable t)
    {
      log.error("", t);
      return null;
    }
  }

  /**
   * Caches the named bundle for fast lookups. This operation is
   * relatively expesive in terms of memory use, but is optimized
   * for run-time speed in the usual case.
   *
   * @exception MissingResourceException Bundle not found.
   */
  private synchronized ResourceBundle cacheBundle(String bundleName, Locale locale)
     throws MissingResourceException
  {
    Map<Locale, ResourceBundle> bundlesByLocale = bundles.get(bundleName);
    if(bundlesByLocale == null)
    {
      bundlesByLocale = new HashMap<>();
      bundles.put(bundleName, bundlesByLocale);
    }

    ResourceBundle rb = bundlesByLocale.get(locale);
    if(rb != null)
      return rb;

    try
    {
      rb = ResourceBundle.getBundle(bundleName, locale);
    }
    catch(MissingResourceException e)
    {
      rb = findBundleByLocale(bundleName, locale, bundlesByLocale);
      if(rb == null)
      {
        throw (MissingResourceException) e.fillInStackTrace();
      }
    }

    if(rb != null)
    {
      // Cache bundle.
      bundlesByLocale.put(locale, rb);
      bundles.put(bundleName, bundlesByLocale);
    }

    return rb;
  }

  /**
   * <p>
   * Retrieves the bundle most closely matching first against the
   * supplied inputs, then against the defaults.</p>
   *
   * <p>
   * Use case: some clients send a HTTP Accept-Language header
   * with a value of only the language to use
   * (i.e. "Accept-Language: en"), and neglect to include a country.
   * When there is no bundle for the requested language, this method
   * can be called to try the default country (checking internally
   * to assure the requested criteria matches the default to avoid
   * disconnects between language and country).</p>
   *
   * <p>
   * Since we're really just guessing at possible bundles to use,
   * we don't ever throw <code>MissingResourceException</code>.</p>
   */
  private ResourceBundle findBundleByLocale(String bundleName, Locale locale, Map bundlesByLocale)
  {
    String defaultLanguage = getDefaultLanguage();
    String defaultCountry = getDefaultCountry();
    Locale defaultLocale = new Locale(defaultLanguage, defaultCountry);

    ResourceBundle rb = null;
    if(SU.isEqu(defaultLanguage, locale.getLanguage()))
    {
      /*
             *              log.debug("Requested language '" + locale.getLanguage() +
             *              "' matches default: Attempting to guess bundle " +
             *              "using default country '" + defaultCountry + '\'');
       */
      Locale withDefaultCountry = new Locale(locale.getLanguage(), defaultCountry);
      rb = (ResourceBundle) bundlesByLocale.get(withDefaultCountry);
      if(rb == null)
      {
        rb = getBundleIgnoreException(bundleName, withDefaultCountry);
      }
    }
    else if(SU.isEqu(defaultCountry, locale.getCountry()))
    {
      Locale withDefaultLanguage = new Locale(defaultLanguage, locale.getCountry());
      rb = (ResourceBundle) bundlesByLocale.get(withDefaultLanguage);
      if(rb == null)
      {
        rb = getBundleIgnoreException(bundleName, withDefaultLanguage);
      }
    }

    if(rb == null && !defaultLocale.equals(locale))
    {
      rb = getBundleIgnoreException(bundleName, defaultLocale);
    }

    return rb;
  }

  /**
   * Retrieves the bundle using the
   * <code>ResourceBundle.getBundle(String, Locale)</code> method,
   * returning <code>null</code> instead of throwing
   * <code>MissingResourceException</code>.
   * @param bundleName
   * @param locale
   * @return the bundle
   */
  public ResourceBundle getBundleIgnoreException(String bundleName, Locale locale)
  {
    try
    {
      return ResourceBundle.getBundle(bundleName, locale);
    }
    catch(MissingResourceException ignored)
    {
      return null;
    }
  }
}
