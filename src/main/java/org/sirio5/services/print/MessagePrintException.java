/*
 *  MessagePrintException.java
 *  Creato il Apr 9, 2021, 6:21:47 PM
 *
 *  Copyright (C) 2021 Informatica Medica s.r.l.
 *
 *  Questo software è proprietà di Informatica Medica s.r.l.
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Informatica Medica s.r.l.
 *  Viale dei Tigli, 19
 *  Casalnuovo di Napoli (NA)
 */
package org.sirio5.services.print;

import java.util.Map;
import org.commonlib5.utils.ArrayMap;
import org.sirio5.utils.SU;

/**
 * Eccezione per visualizzazione messaggio utente.
 * Questa eccezione viene sollevata quando un plugin di stampa
 * vuole restituire una pagina html all'utente piuttosto che il
 * PDF della stampa.
 *
 * @author Nicola De Nisco
 */
public class MessagePrintException extends Exception
{
  private final String redirect;
  private final String template;
  private final Map<String, Object> options = new ArrayMap<>();

  /**
   * Costruttore eccezione messaggio.
   * Solo uno dei due redirect o template può essere utilizzato.
   * @param redirect uri della pagina di redirezione
   * @param template template da cui ricavare la risposta HTML
   * @param message messaggio opzionale
   * @param optionPair opzioni (in coppia chiave1=valore1, chiave2=valore2) da ritornare al gestore eccezione
   */
  public MessagePrintException(String redirect, String template, String message, Object... optionPair)
  {
    super(message);
    this.redirect = redirect;
    this.template = template;

    for(int i = 0; i < optionPair.length; i++)
    {
      String key = SU.okStr(optionPair[i]);
      Object value = "";

      if(++i < optionPair.length)
        value = optionPair[i];

      options.put(key, value);
    }
  }

  public String getRedirect()
  {
    return redirect;
  }

  public String getTemplate()
  {
    return template;
  }

  public Map<String, Object> getOptions()
  {
    return options;
  }
}
