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

import org.apache.turbine.util.RunData;

/**
 * Versione potenziata del BrowserDetector.
 * Verifica anche la possibilita delle dialog modali.
 * Verifica browser mobile.
 * "Mozilla/5.0 (Linux; Android 7.0; PRA-LX1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.76 Mobile
 * Safari/537.36"
 * @author Nicola De Nisco
 */
public class EnancedBrowserDetector extends CorrectedBrowserDetector
{
  private boolean modalDialog = true;
  private boolean mobile = false;
  private float firefoxVersion = 0;

  public EnancedBrowserDetector(String userAgentString)
  {
    super(userAgentString);
    parseEn();
  }

  public EnancedBrowserDetector(RunData data)
  {
    super(data);
    parseEn();
  }

  private void parseEn()
  {
    String s = getUserAgentString();
    int pos = s.indexOf("Firefox");
    if(pos != -1)
    {
      firefoxVersion = Float.valueOf(s.substring(pos + 8));
    }

    if(s.contains("Android") || s.contains("Mobile Safari"))
      mobile = true;

    // su chrome le dialog modali sono sempre disattivate
    if(isChrome())
    {
      modalDialog = false;
      return;
    }

    // per firefox versioni superiori alla 46 sono disattivate
    if(isMozilla() && firefoxVersion >= 46.0)
    {
      modalDialog = false;
      return;
    }

    modalDialog = true;
  }

  public boolean isModalDialog()
  {
    return modalDialog;
  }

  public float getFirefoxVersion()
  {
    return firefoxVersion;
  }

  /**
   * Ritorna vero se il browser dell'utente e' Netscape/Mozilla.
   *
   * @return vero se Mozilla
   */
  public boolean isMozilla()
  {
    if(isChrome() || isSafari())
      return false;

    String s = getBrowserName().toLowerCase();
    return s.contains("mozilla") || s.contains("firefox");
  }

  /**
   * Ritorna vero se il browser dell'utente e' Microsoft Internet Explorer.
   *
   * @return vero se Internet Explorer
   */
  public boolean isMsie()
  {
    return getBrowserName().toLowerCase().contains("microsoft");
  }

  /**
   * Ritorna vero se il browser dell'utente e' Opera.
   *
   * @return vero se Opera
   */
  public boolean isOpera()
  {
    return getBrowserName().toLowerCase().contains("opera");
  }

  /**
   * Ritorna vero se il browser dell'utente e' Chrome.
   * @return
   */
  public boolean isChrome()
  {
    return getUserAgentString().toLowerCase().contains("chrome");
  }

  /**
   * Ritorna vero se il browser dell'utente e' Chrome.
   * @return
   */
  public boolean isSafari()
  {
    return getUserAgentString().toLowerCase().contains("safari");
  }

  public boolean isCssOK()
  {
    if(mobile)
      return true;

    // in classi derivate si può inserire una logica più avanzata
    return true;
  }

  public boolean isFileUploadOK()
  {
    if(mobile)
      return true;

    // in classi derivate si può inserire una logica più avanzata
    return true;
  }

  public boolean isJavascriptOK()
  {
    if(mobile)
      return true;

    // in classi derivate si può inserire una logica più avanzata
    return true;
  }

  public boolean isMobile()
  {
    return mobile;
  }
}
