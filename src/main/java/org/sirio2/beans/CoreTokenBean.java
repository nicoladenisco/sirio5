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
package org.sirio2.beans;

import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.Service;
import org.apache.turbine.services.TurbineServices;
import org.sirio2.services.token.TokenAuthItem;
import org.sirio2.utils.DT;
import org.sirio2.utils.SU;

/**
 * Classe base di tutti i bean persistenti nei token.
 *
 * @author Nicola De Nisco
 */
public class CoreTokenBean implements CoreTokenBindingListener
{
  protected Date today = new Date();
  protected Log log = LogFactory.getLog(this.getClass());

  /**
   * Inizializzazione del bean.
   * Quando viene creato il bean viene chiamato questo metodo per la sua inizializzazione.
   * @param data dati della richiesta
   * @throws Exception
   */
  public void init(TokenAuthItem data)
     throws Exception
  {
  }

  /**
   * Verifica se questo bean è ancora valido.
   * Questa funzione viene chiamata quando
   * il bean viene recuperato dalla sessione.
   * Se nella richiesta vi sono parametri che
   * ne inficiano l'utilizzo questo metodo
   * deve ritornare false e una nuova istanza
   * di questo bean verrà creata e inizializzata.
   * @param data dati della richiesta
   * @return vero se il bean è valido
   */
  public boolean isValid(TokenAuthItem data)
  {
    return true;
  }

  /**
   * Notifica estrazione dalla sessione.
   * Ogni volta che BeanFactory estrae questo bean dalla sessione
   * viene invocato questo metodo.
   * Non viene chiamato alla creazione: vedi init().
   * @param data dati della richiesta
   * @throws java.lang.Exception
   */
  public void refreshSession(TokenAuthItem data)
     throws Exception
  {
  }

  public <T extends Service> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  public String getEsercizio()
     throws Exception
  {
    return Integer.toString(1900 + today.getYear());
  }

  public Date getToday()
  {
    return today;
  }

  public String getTodayAsString()
  {
    return DT.formatData(today);
  }

  public String getTodayAsStringFull()
  {
    return DT.formatDataFull(today);
  }

  /**
   * Esecuzione di comandi. Il form invia un parametro speciale chiamato
   * 'command' con una stringa identificativa dell'operazione richiesta
   * dall'utente. Questa stringa diviene parte di un metodo doCmd_stringa
   * ricercato a runtime e se presente eseguito. Vedi doCmd_... per ulteriori
   * dettagli.
   *
   * @param command comando da eseguire
   * @param data parametri generali della richiesta
   * @param params mappa di tutti i parametri request più eventuali parametri permanenti
   * @param args eventuali argomenti aggiuntivi a piacere
   * @throws Exception
   */
  public void doCommand(String command, TokenAuthItem data, Map params, Object... args)
     throws Exception
  {
    SU.doCommand(this, command, data, params, args);
  }

  /**
   * Notifica inserimento in sessione.
   * Viene chiamata quando questo bean viene inserito in una sessione.
   * @param token
   */
  @Override
  public void valueBound(TokenAuthItem token)
  {
  }

  /**
   * Notifica rimozione da sessione.
   * Viene chiamata quando questo bean viene rimosso da una sessione.
   * Questo include il caso di una sessione scaduta per timeout.
   * @param token
   */
  @Override
  public void valueUnbound(TokenAuthItem token)
  {
  }
}
