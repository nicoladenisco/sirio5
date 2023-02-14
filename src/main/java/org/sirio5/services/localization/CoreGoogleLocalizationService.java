/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.services.localization;

import com.google.auth.oauth2.AccessToken;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.localization.LocaleTokenizer;
import org.apache.turbine.Turbine;
import org.commonlib5.utils.FixedSizeMap;
import org.commonlib5.utils.Pair;

/**
 * Servizio di localizazione con il supporto della API Google per la traduzione.
 *
 * @author Nicola De Nisco
 */
public class CoreGoogleLocalizationService extends CoreLocalizationService
{
  /** Logging */
  private static Log log = LogFactory.getLog(CoreGoogleLocalizationService.class);

  protected GoogleTranslate gt;
  protected String credFilePath;
  protected FixedSizeMap<String, String> cacheMsg = new FixedSizeMap<>(1024);
  protected boolean initialized;

  @Override
  public Locale getLocale(String header)
  {
    if(StringUtils.isEmpty(header))
      return displayLocale;

    initGoogle();
    if(gt == null)
      return super.getLocale(header);

    LocaleTokenizer tok = new LocaleTokenizer(header);
    if(tok.hasNext())
    {
      // identificata la lingua preferita dal browser dell'utente
      return (Locale) tok.next();
    }

    return super.getLocale(header);
  }

  @Override
  protected String subTranslation(String key, Locale locale)
  {
    initGoogle();
    if(gt == null)
      return null;

    try
    {
      // se la locale richiesta è it_IT la stringa si intende già tradotta
      if(locale.equals(originLocale))
        return key;

      String value;

      if((value = cacheMsg.get(key)) != null)
        return value;

      value = subTransSingle(key, locale);

      if(value != null)
        cacheMsg.put(key, value);

      return value;
    }
    catch(Exception ex)
    {
      log.error("", ex);
      return null;
    }
  }

  protected String subTransSingle(String key, Locale locale)
     throws Exception
  {
    String value;
    List<Pair<String, String>> testi = new ArrayList<>();
    testi.add(new Pair<>(key, null));
    gt.traduci(testi, originLocale.getLanguage(), locale.getLanguage(), "html");
    value = testi.get(0).second;
    return value;
  }

  protected final void initGoogle()
  {
    if(initialized)
      return;

    synchronized(this)
    {
      if(initialized)
        return;

      initialized = true;
      initWorker();
    }
  }

  protected void initWorker()
  {
    Configuration conf = Turbine.getConfiguration();
    credFilePath = conf.getString("locale.google.credFile", null);

    if(credFilePath == null)
    {
      log.info("File credenziali non specificato.");
      return;
    }

    File fcred = new File(credFilePath);
    if(!fcred.canRead())
    {
      log.info("File credenziali " + fcred.getAbsolutePath() + " non trovato o non leggibile.");
      return;
    }

    try
    {
      gt = new GoogleTranslate(fcred);
      AccessToken token = gt.autorizza();
      log.info("Autorizzazione API google concessa con token " + token.getTokenValue());
    }
    catch(Exception e)
    {
      gt = null;
      log.error("Autorizzazione API google fallita.", e);
    }
  }
}
