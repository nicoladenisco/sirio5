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
import com.google.auth.oauth2.GoogleCredentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.commonlib5.utils.JsonHelper;
import org.commonlib5.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utilizzo della API google per la traduzione del testo.
 *
 * @author Nicola De Nisco
 */
public class GoogleTranslate
{
  private File credFile;
  private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
  private static final Logger logger = Logger.getLogger(GoogleTranslate.class.getName());
  public static final int MAX_NUM_STRINGHE = 128;

  public GoogleTranslate(File credFile)
  {
    this.credFile = credFile;
  }

  /**
   * Esegue l'autorizzazione in base al file di credenziali.
   * @return il token di accesso da utilizzare in successive chiamate
   * @throws Exception
   */
  public AccessToken autorizza()
     throws Exception
  {
    // lo stream viene chiuso da fromStream
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credFile))
       .createScoped(SCOPE);
    credentials.refreshIfExpired();

    // The typical way to use a GoogleCredentials instance is to call its getRequestMetadata(),
    // and include the metadata in your request. Since we are accessing the token directly via
    // getAccessToken(), we must first call getRequestMetadata() to ensure the token is available
    // (refreshed if necessary).
    logger.fine("METADATA: " + credentials.getRequestMetadata());

    return credentials.getAccessToken();
  }

  /**
   * Chiama il servizio API di google.
   * @param token token di autorizzazione
   * @param request richiesta json
   * @return risposta con codice http e payload json
   * @throws Exception
   */
  public Pair<Integer, JSONObject> chiama(String token, JSONObject request)
     throws Exception
  {
    //  curl -X POST \
    //  -H "Authorization: Bearer "$(gcloud auth application-default print-access-token) \
    //  -H "Content-Type: application/json; charset=utf-8" \
    //  -d @request.json \
    //  "https://translation.googleapis.com/language/translate/v2"

    URI uri = new URI("https://translation.googleapis.com/language/translate/v2");
    JsonHelper jh = new JsonHelper(uri);
    jh.addToHeader("Authorization", "Bearer " + token);
    jh.setWrapException(true);
    return jh.postAsJson(request);
  }

  /**
   * Traduzione di una lista di stringhe.
   * Il risultato della traduzione viene salvato nella coppia che contiene l'originale.
   * Esegue l'autorizzazione in base al file di credenziali e invoca il servizio API google.
   * @param testi coppia di stringhe da tradurre e tradotta (quella tradotta verrà sovrascitta)
   * @param linguaOrigine lingua di origine dei testi
   * @param linguaDestinazione lingua richiesta per la traduzione
   * @param formatoTesto formato del testo (text o html)
   * @throws Exception
   */
  public void traduci(List<Pair<String, String>> testi, String linguaOrigine, String linguaDestinazione, String formatoTesto)
     throws Exception
  {
    if(testi.size() > MAX_NUM_STRINGHE)
      throw new IOException("String list size exced max allowed (request=" + testi.size() + " max=" + MAX_NUM_STRINGHE + ").");

    AccessToken accessToken = autorizza();
    String token = accessToken.getTokenValue();
    List<String> listaStringhe = testi.stream().map((p) -> p.first).collect(Collectors.toList());

    JSONObject request = new JSONObject();
    request.put("q", listaStringhe);
    request.put("target", linguaDestinazione);
    request.put("source", linguaOrigine);
    request.put("format", formatoTesto);

    Pair<Integer, JSONObject> response = chiama(token, request);

    if(response.first != 200)
    {
      if(response.second != null)
        logger.log(Level.SEVERE, "Server returned invalid HTTP response code (" + response.first + "):\n" + response.second);

      throw new IOException("Return value " + response.first + " not valid; must be 200.");
    }

    JSONArray arTradotto = response.second
       .getJSONObject("data")
       .getJSONArray("translations");

    if(arTradotto.length() != testi.size())
      throw new IOException("Not all texts are translated.");

    for(int i = 0; i < testi.size(); i++)
    {
      Pair<String, String> p = testi.get(i);
      JSONObject jo = arTradotto.getJSONObject(i);
      p.second = jo.getString("translatedText");
    }
  }
}
