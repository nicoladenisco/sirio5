/*
 * Copyright (C) 2025 Nicola De Nisco
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

import java.sql.SQLException;
import java.util.*;
import org.apache.torque.TorqueException;
import org.rigel5.SetupHolder;
import org.sirio5.ErrorMessageException;
import org.sirio5.RedirectMessageException;
import org.sirio5.rigel.ConcurrentDatabaseModificationException;
import org.sirio5.rigel.RigelHtmlI18n;
import org.sirio5.rigel.UnmodificableRecordException;
import org.sirio5.services.contatori.CounterTimeoutException;

/**
 * Gestione degli errori considerati non fatali.
 * Questa classe è utilizzata in CoreBaseScreen e CoreBaseAction
 * per decidere se l'eccezione generata dall'elaborazione
 * deve sollevare realmente eccezione (atterraggio su pagina di errore)
 * oppure deve semplicemente visualizzare un messaggio a nel banner
 * errore e riproporre lo screen corrente.
 *
 * Il comportamento è modificabile nello screen o action
 * usando i metodi addHandler removeHandler.
 *
 * @author Nicola De Nisco
 */
public class CoreFatalErrorManager
{
  private final Map<Class<? extends Throwable>, FatalErrorHandler> mapHandler = new HashMap<>();

  /**
   * Costruttore.
   * Vengono inseriti gli handler per
   * <ul>
   * <li>ErrorMessageException</li>
   * <li>RedirectMessageException</li>
   * <li>SQLException</li>
   * <li>TorqueException</li>
   * <li>ConcurrentDatabaseModificationException</li>
   * <li>UnmodificableRecordException</li>
   * <li>CounterTimeoutException</li>
   * </ul>
   */
  public CoreFatalErrorManager()
  {
    mapHandler.put(ErrorMessageException.class, (data, t) -> t.getMessage());
    mapHandler.put(RedirectMessageException.class, (data, t) -> redirect(data, (RedirectMessageException) t));
    mapHandler.put(SQLException.class, (data, t) -> reportNonFatalDatabaseError(data, (SQLException) t));
    mapHandler.put(TorqueException.class, (data, t) -> reportTorqueException(data, (TorqueException) t));
    mapHandler.put(ConcurrentDatabaseModificationException.class, (data, t) -> reportConcurrentDatabaseError(data, (ConcurrentDatabaseModificationException) t));
    mapHandler.put(UnmodificableRecordException.class, (data, t) -> reportUnmodificableRecordError(data, (UnmodificableRecordException) t));
    mapHandler.put(CounterTimeoutException.class, (data, t) -> reportCounterTimeoutException(data, (CounterTimeoutException) t));
  }

  /**
   * Aggiunge o modifica un handler per un tipo di eccezione.
   * @param cls classe dell'eccezione
   * @param handler lambda handler
   */
  public void addHandler(Class<? extends Throwable> cls, FatalErrorHandler handler)
  {
    mapHandler.put(cls, handler);
  }

  /**
   * Rimuove un handler per un tipo di eccezione.
   * @param cls classe dell'eccezione
   */
  public void removeHandler(Class<? extends Throwable> cls)
  {
    mapHandler.remove(cls);
  }

  /**
   * Funzione chiamata dallo screen o action.
   * Se esiste un handler per il tipo di eccezione lo utilizza per
   * ricevere una stringa internazionalizzata da utilizzare come messaggio.
   * Diversamente solleva l'eccezione.<br>
   * <b>ATTENZIONE: l'handler potrebbe in ogni caso decidere si sollevare l'eccezione comunque.</b>
   * @param data dati della richiesta
   * @param t eccezione catturata da analizzare
   * @param append vero per appendere il messaggio a quello già esistente in rundata
   * @throws Exception
   */
  public void handleException(CoreRunData data, Throwable t, boolean append)
     throws Exception
  {
    FatalErrorHandler handler = mapHandler.get(t.getClass());
    if(handler == null)
    {
      if(t instanceof Exception)
        throw (Exception) t;

      throw new RuntimeException(t);
    }

    String message = handler.apply(data, t);
    if(append)
      data.addMessage(message);
    else
      data.setMessage(message);
  }

  /**
   * Riporta errore di db all'utente se non fatale.
   * @param pdata dati della richiesta
   * @param ex eccezione catturata da analizzare
   * @return messaggio da visualizzare già internazionalizzato
   * @throws SQLException eccezione risollevata se fatale
   */
  public String reportNonFatalDatabaseError(CoreRunData pdata, SQLException ex)
     throws SQLException
  {
    try
    {
      StringBuilder sb = new StringBuilder();
      sb.append(SetupHolder.getQueryBuilder().formatNonFatalError(ex, new RigelHtmlI18n(pdata)))
         .append("<br><span class=\"txt-white-regular-09\">")
         .append(pdata.i18n("Messaggio originale: %s", ex.getLocalizedMessage()))
         .append("</span>");
      return sb.toString();
    }
    catch(Exception ex1)
    {
      throw ex;
    }
  }

  /**
   * Riporta errore di modifica concorrente all'utente.
   * @param pdata dati della richiesta
   * @param ex eccezione catturata da analizzare
   * @return messaggio da visualizzare già internazionalizzato
   * @throws Exception
   */
  public String reportConcurrentDatabaseError(CoreRunData pdata, ConcurrentDatabaseModificationException ex)
     throws Exception
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div style=\"background-color: red;\"><span class=\"txt-white-bold-12-nul\">")
       .append(pdata.i18n("Spiacente!"))
       .append("</span><br><span class=\"txt-white-regular-11-nul\">")
       .append(pdata.i18n("Un altro utente ha modificato il record che stai salvando."))
       .append("<br>")
       .append(pdata.i18n("Per evitare conflitti le tue modifiche non possono essere accettate."))
       .append("</span><br><span class=\"txt-white-regular-09\">")
       .append(ex.getLocalizedMessage())
       .append("</span></div>");
    return sb.toString();
  }

  /**
   * Riporta errore di dato non modificabile all'utente.
   * @param pdata dati della richiesta
   * @param ex eccezione catturata da analizzare
   * @return messaggio da visualizzare già internazionalizzato
   * @throws Exception
   */
  public String reportUnmodificableRecordError(CoreRunData pdata, UnmodificableRecordException ex)
     throws Exception
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div style=\"background-color: red;\"><span class=\"txt-white-bold-12-nul\">")
       .append(pdata.i18n("Spiacente!"))
       .append("</span><br><span class=\"txt-white-regular-11-nul\">")
       .append(pdata.i18n("Non hai i permessi per modificare il record indicato."))
       .append("</span><br><span class=\"txt-white-regular-09\">")
       .append(ex.getLocalizedMessage())
       .append("</span></div>");
    return sb.toString();
  }

  /**
   * Riporta errore di contatori congestionati all'utente.
   * @param pdata dati della richiesta
   * @param ex eccezione catturata da analizzare
   * @return messaggio da visualizzare già internazionalizzato
   * @throws Exception
   */
  public String reportCounterTimeoutException(CoreRunData pdata, CounterTimeoutException ex)
     throws Exception
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div style=\"background-color: red;\"><span class=\"txt-white-bold-12-nul\">")
       .append(pdata.i18n("Sistema congestionato!"))
       .append("</span><br><span class=\"txt-white-regular-11-nul\">")
       .append(pdata.i18n("Non è stato possibile completare l'operazione di salvataggio a causa di un sovraccarico temporaneo."))
       .append("</span><br><span class=\"txt-white-regular-09\">")
       .append(ex.getLocalizedMessage())
       .append("</span><br><br><span class=\"txt-white-bold-12-nul\">")
       .append(pdata.i18n("RIPETERE ULTIMA OPERAZIONE DI SALVATAGGIO."))
       .append("</span></div>");
    return sb.toString();
  }

  public String redirect(CoreRunData data, RedirectMessageException ex)
     throws Exception
  {
    data.setRedirectURI(ex.getUri().toString());
    data.setStatusCode(302);
    return ex.getMessage();
  }

  public String reportTorqueException(CoreRunData data, TorqueException ex)
     throws Exception
  {
    // ispeziona la causa per verificare se gestirla come sqlexception
    if(ex.getCause() != null && ex.getCause() instanceof SQLException)
    {
      SQLException sqe = (SQLException) ex.getCause();
      return reportNonFatalDatabaseError(data, sqe);
    }
    else
      return reportNonFatalDatabaseError(data, ex);
  }

  @FunctionalInterface
  public static interface FatalErrorHandler
  {
    public String apply(CoreRunData data, Throwable t)
       throws Exception;
  }
}
