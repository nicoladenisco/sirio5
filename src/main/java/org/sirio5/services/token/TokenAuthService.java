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
package org.sirio5.services.token;

import java.awt.event.ActionListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;
import org.sirio5.services.CoreServiceExtension;

/**
 * Servizio per il rilascio di token di autenticazione
 * con scadenza a tempo.
 * Viene utilizzato quando occorre un sistema di autenticazione
 * dell'utente alternativo al sistema standard con un concetto
 * di tempo di validità dell'autenticazione.
 * Quando un utente si autentica viene rilasciato un token
 * identificato da un codice univoco che può essere usato
 * per le comunicazioni successive.
 * Il token ha un tempo di scadenza che lo rimuove automaticamente
 * dopo un certo periodo di non uso.
 */
public interface TokenAuthService extends CoreServiceExtension
{
  public static final String SERVICE_NAME = "TokenAuthService";

  /**
   * Registra i dati di un utente loggato.
   * Il servizio mantiene una corrispondenza
   * fra sessione e utente reale. Questo consente
   * il rilascio di un token usando come identificativo
   * l'ID di sessione.
   * @param sessionID
   * @param userBean
   * @throws Exception
   */
  public void registerUserLogon(String sessionID, TokenBean userBean)
     throws Exception;

  /**
   * Rimuove informazioni utente per la sessione indicata.
   * @param sessionID
   * @throws Exception
   */
  public void unregisterUserLogon(String sessionID)
     throws Exception;

  /**
   * Aggiunge un utente anonimo.
   * @param expireAction eventuale azione da intraprendere allo scadere del token (può essere null)
   * @return il token per l'utente
   * @throws Exception
   */
  public TokenAuthItem addClient(ActionListener expireAction)
     throws Exception;

  /**
   * Aggiunge un utente autenticandolo.
   * Solleva eccezione se l'utente non può essere autenticato.
   * @param uName nome di sitema dell'utente (lo stesso della logon)
   * @param uPass password dell'utente (la stessa della logon)
   * @param expireAction eventuale azione da intraprendere allo scadere del token (può essere null)
   * @return il token per l'utente
   * @throws Exception
   */
  public TokenAuthItem addClient(String uName, String uPass, ActionListener expireAction)
     throws Exception;

  /**
   * Aggiunge un utente attraverso l'ID di sessione.
   * @param sessionID ID della sessione HTTP
   * @param expireAction eventuale azione da intraprendere allo scadere del token (può essere null)
   * @return il token per l'utente
   * @throws Exception
   */
  public TokenAuthItem addClient(String sessionID, ActionListener expireAction)
     throws Exception;

  /**
   * Rimuove un token utene.
   * @param item il token dell'utente da rimuovere
   */
  public void removeClient(TokenAuthItem item);

  /**
   * Test validità del token.
   * Verifica per differenza di tempo rispetto al momento
   * DALLA CREAZIONE DEL TOKEN. Il token ha un expire dal
   * momento DELL'ULTIMO UTILIZZO, ma questa funzione consente
   * di controllare la differenza rispetto al momento del logon.
   * @param time tempo da verificare
   * @param item token da verificare
   * @return vero se il token è ancora valido
   */
  public boolean isExpiriedClient(long time, TokenAuthItem item);

  /**
   * Recupera token da codice univoco.
   * @param id codice univoco del token da recuperare
   * @return il token se esiste e non è scaduto, altrimenti null
   * @throws Exception
   */
  public TokenAuthItem getClient(String id)
     throws Exception;

  /**
   * Genera token OAuth2 a partire da un item.
   * Viene prodotto un JSON con i dati principali del token;
   * questo JSON viene crittografato con la chiave privata e convertito in base64.
   * @param req richiesta HTTP per la generazione del token
   * @param ti token già autenticato (vedi addClient)
   * @return token OAuth2
   * @throws Exception
   */
  public String encryptTokenOauth2(HttpServletRequest req, TokenAuthItem ti)
     throws Exception;

  /**
   * Decodifica token OAuth2.
   * Decodifica il token prodotto con encryptTokenOauth2 ritornando il JSON originario.
   * @param req richiesta HTTP per la ricostruzione del token
   * @param token token OAuth2
   * @return JSON contenuto nel token
   * @throws Exception
   */
  public JSONObject decriptTokenOauth2(HttpServletRequest req, String token)
     throws Exception;

  /**
   * Ritorna chiave RSA pubblica.
   * @return la chiave codificata in base64
   */
  public String getPublicKeyBase64();

  /**
   * Genera un token anti CSRF.
   * La durata del token viene stabilita a setup (default 1 ora).
   * @param request richiesta HTTP
   * @param sessione sessione HTTP
   * @return il token codificato in base 64
   * @throws Exception
   */
  public String getTokenAntiCSRF(HttpServletRequest request, HttpSession sessione)
     throws Exception;

  /**
   * Verifica il token indicato per la validità.
   * @param token il token codificato in base 64
   * @param remove se vero invalida il token dopo il test; il token non sarà più valido
   * @param request richiesta HTTP
   * @param sessione sessione HTTP
   * @return 0=OK, 1=token inesitente, 2=token non valido
   * @throws Exception
   */
  public int verificaTokenAntiCSRF(String token, boolean remove, HttpServletRequest request, HttpSession sessione)
     throws Exception;
}
