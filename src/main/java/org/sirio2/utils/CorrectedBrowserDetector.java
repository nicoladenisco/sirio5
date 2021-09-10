/*
 * Copyright (C) 2021 Nicola De Nisco
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
package org.sirio2.utils;

import java.util.HashMap;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.turbine.util.RunData;

/**
 * This class parses the user agent string and provides getters for
 * its parts. It uses YAUAA (https://yauaa.basjes.nl/)
 *
 * The initialization step for a full UserAgentAnalyzer
 * (i.e. all fields) usually takes something in the range of 2-5 seconds.
 *
 * BUG BUG BUG
 * Questa classe è una riedizione di org.apache.turbine.util.BrowserDetector
 * che purtroppo non può essere aggirato in una classe derivata.
 * In crome il numero di versione ha il formato xxx.xxxx.xxxx.xxx che non
 * è previsto (nell'implemetazione originale viene utilizzato un float).
 * Questa classe è compatibile con chrome.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:leon@clearink.com">Leon Atkisnon</a>
 * @author <a href="mailto:mospaw@polk-county.com">Chris Mospaw</a>
 * @author <a href="mailto:bgriffin@cddb.com">Benjamin Elijah Griffin</a>
 * @author <a href="mailto:tv@apache.org">Thomas Vandahl</a>
 */
public class CorrectedBrowserDetector
{
  /** The user agent string. */
  private String userAgentString = "";

  /** The user agent parser */
  private static UserAgentAnalyzer uaa = UserAgentAnalyzer
     .newBuilder()
     .withFields(UserAgent.AGENT_NAME,
        UserAgent.AGENT_VERSION,
        UserAgent.OPERATING_SYSTEM_NAME)
     .hideMatcherLoadStats()
     .build();

  /** The user agent cache. */
  private static volatile HashMap<String, UserAgent> userAgentCache = new HashMap<>();

  /** The browser name specified in the user agent string. */
  private String browserName = "";

  /**
   * The browser version specified in the user agent string. If we
   * can't parse the version just assume an old browser.
   */
  private String browserVersion = "1.0";

  /**
   * The browser platform specified in the user agent string.
   */
  private String browserPlatform = "unknown";

  /**
   * Constructor used to initialize this class.
   *
   * @param userAgentString A String with the user agent field.
   */
  public CorrectedBrowserDetector(String userAgentString)
  {
    this.userAgentString = userAgentString;
    UserAgent userAgent = getUserAgent();

    // Get the browser name and version.
    browserName = userAgent.getValue(UserAgent.AGENT_NAME);
    browserVersion = userAgent.getValue(UserAgent.AGENT_VERSION);

    // Try to figure out what platform.
    browserPlatform = userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME);
  }

  /**
   * Constructor used to initialize this class.
   *
   * @param data The Turbine RunData object.
   */
  public CorrectedBrowserDetector(RunData data)
  {
    this(data.getUserAgent());
  }

  /**
   * The browser name specified in the user agent string.
   *
   * @return A String with the browser name.
   */
  public String getBrowserName()
  {
    return browserName;
  }

  /**
   * The browser platform specified in the user agent string.
   *
   * @return A String with the browser platform.
   */
  public String getBrowserPlatform()
  {
    return browserPlatform;
  }

  /**
   * The browser version specified in the user agent string.
   *
   * @return A String with the browser version.
   */
  public String getBrowserVersionAsString()
  {
    return browserVersion;
  }

  /**
   * Confronta la versione del browser con la stringa fornita.
   * La stringa come la versione del broser possono avere la forma x.x.x.x
   *
   * @param toCompare versione da testare
   * @return 0=sono uguali 1=browser &gt; toCompare -1=browser &lt; toCompare;
   */
  public int compareBrowserVersion(String toCompare)
  {
    return SU.compareVersion(browserVersion, toCompare, '.');
  }

  /**
   * The browser version specified in the user agent string.
   *
   * @return A String with the browser version.
   */
  public float getBrowserVersion()
  {
    return (float) SU.parse(browserVersion, 0.0);
  }

  /**
   * The user agent string for this class.
   *
   * @return A String with the user agent.
   */
  public String getUserAgentString()
  {
    return userAgentString;
  }

  /**
   * The user agent for this class.
   *
   * @return A user agent.
   */
  public UserAgent getUserAgent()
  {
    return parse(userAgentString);
  }

  /**
   * Helper method to initialize this class.
   *
   * @param userAgentString the user agent string
   */
  private static UserAgent parse(String userAgentString)
  {
    UserAgent rv = null;
    if((rv = userAgentCache.get(userAgentString)) != null)
      return rv;

    synchronized(userAgentCache)
    {
      if((rv = userAgentCache.get(userAgentString)) != null)
        return rv;

      rv = uaa.parse(userAgentString);
      userAgentCache.put(userAgentString, rv);
    }

    return rv;

    //return userAgentCache.computeIfAbsent(userAgentString, uaa::parse);
  }

  /**
   * Helper method to convert String to a float.
   *
   * @param s A String.
   * @return The String converted to float.
   */
  private static final float toFloat(String s)
  {
    return Float.parseFloat(s);
  }
}
