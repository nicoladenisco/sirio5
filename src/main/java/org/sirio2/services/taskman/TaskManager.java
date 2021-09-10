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
package org.sirio2.services.taskman;

import java.util.List;
import javax.swing.Action;
import org.apache.turbine.services.Service;
import org.sirio2.services.taskman.tasks.Task;

/**
 * Interfaccia servizio gestione task asincroni.
 * Un Task è un lavoro asincrono avviato da un utente
 * e con un ciclo di vita indipendente. L'utente può
 * usare altre funzioni dell'applicazione e anche sloggarsi
 * mentre i suoi task continuano ad essere eseguiti.
 * L'utente può controllare lo stato di avanzamento
 * dei suoi task.
 *
 * @author Nicola De Nisco
 */
public interface TaskManager extends Service
{
  public static final String SERVICE_NAME = "TaskManager";

  /**
   * Registra un task nel servizio e lo avvia.
   * Il task deve essere stato completato
   * con i dati relativi.
   * @param toStart task da avviare e registrare
   * @param waitMillis millisecondi ti attesa per completamento task
   * oppure 0 per ritorno immediato
   * @return vero se il task è ancora in esecuzione
   * @throws Exception
   */
  public boolean registraAvviaTask(Task toStart, long waitMillis)
     throws Exception;

  /**
   * Crea un task da setup.
   * @param idUser id dell'utente che richiede la creazione
   * @param taskName nome del task
   * @param terminateAction azione di terminazione (può essere null)
   * @return il task creato
   * @throws Exception
   */
  public Task creaTaskDaSetup(int idUser, String taskName, Action terminateAction)
     throws Exception;

  /**
   * Ritorna tutti i task dell'utente indicato.
   * @param idUser identificativo dell'utente
   * @return lista dei task
   * @throws Exception
   */
  public List<Task> getListaTaskUtente(int idUser)
     throws Exception;

  /**
   * Ritorna tutti i task.
   * @return lista dei task
   * @throws Exception
   */
  public List<Task> getAllTasks()
     throws Exception;

  /**
   * Recupera tutti i task di un certo tipo.
   * @param taskName nome identificativo del task.
   * @return lista dei task
   * @throws Exception
   */
  public List<Task> findTasks(String taskName)
     throws Exception;

  /**
   * Recupera tutti i task di un certo tipo e di un determinato utente.
   * @param taskName nome identificativo del task.
   * @param idUser identificativo dell'utente
   * @return lista dei task
   * @throws Exception
   */
  public List<Task> findTasks(String taskName, int idUser)
     throws Exception;

  /**
   * Recupera un task attraverso il suo identificativo univoco.
   * @param idTask identificativo univoco del task
   * @return il task associato o null se non trovato
   * @throws Exception
   */
  public Task findTask(int idTask)
     throws Exception;

  /**
   * Verfica se un tipo di task è in esecuzione.
   * @param taskName nome identificativo del task.
   * @return vero se in esecuzione
   * @throws Exception
   */
  public boolean isRunning(String taskName)
     throws Exception;

  /**
   * Verfica se un tipo di task è in esecuzione.
   * @param taskName nome identificativo del task.
   * @param idUser identificativo dell'utente
   * @return vero se in esecuzione
   * @throws Exception
   */
  public boolean isRunning(String taskName, int idUser)
     throws Exception;

  /**
   * Rimuove il task dalla lista dei task attivi.
   * Se il task è ancora in esecuzione segnala l'interruzione
   * e attente per waitMillis. Se il task non si arresta una
   * eccezione viene sollevata. Al termine il task viene
   * rimosso dalla lista.
   * @param idTask identificativo univoco del task
   * @param waitMillis tempo di attesa interruzione task (0=attendi fino alla fine)
   * @throws Exception
   */
  public void rimuoviTask(int idTask, long waitMillis)
     throws Exception;

  /**
   * Rimuove i task completati dalla lista task.
   * @return numero task rimossi
   * @throws Exception
   */
  public int rimuoviTaskCompletati()
     throws Exception;
}
