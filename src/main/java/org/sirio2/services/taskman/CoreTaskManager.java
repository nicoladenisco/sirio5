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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import org.apache.commons.configuration2.Configuration;
import org.sirio2.services.CoreServiceException;
import org.sirio2.services.AbstractCoreBaseService;
import org.sirio2.services.localization.INT;
import org.sirio2.services.taskman.tasks.Task;
import org.sirio2.services.taskman.tasks.TaskFactory;
import org.sirio2.utils.SU;

/**
 * Implementazione standard del servizio TaskManager.
 *
 * @author Nicola De Nisco
 */
public class CoreTaskManager extends AbstractCoreBaseService
   implements TaskManager
{
  protected ArrayList<Task> arTasks = new ArrayList<Task>();

  @Override
  public void coreInit()
     throws Exception
  {
    Configuration cfg = getConfiguration();
    TaskFactory.getInstance().configure(cfg);
  }

  @Override
  public synchronized boolean registraAvviaTask(Task toStart, long waitMillis)
     throws Exception
  {
    if(!SU.isOkStr(toStart.getTaskName()))
      throw new IllegalStateException(
         INT.I("Nessun nome task registrato."));

    if(toStart.isAlive())
      throw new IllegalStateException(
         INT.I("Task %s già avviato.", toStart.getTaskName()));

    if(toStart.getIdTask() == 0)
      throw new IllegalStateException(
         INT.I("Task %s non correttamente configurato: non può essere inserito nello scheduler.",
            toStart.getTaskName()));

    if(toStart.isExclusive())
    {
      List<Task> tsnames = findTasks(toStart.getTaskName());
      for(Task t : tsnames)
      {
        if(t.isAlive())
          throw new IllegalStateException(
             INT.I("Task %s esclusivo: un'altra istanza (%d) è già in esecuzione.",
                toStart.getTaskName(), t.getIdTask()));
      }
    }

    toStart.setDaemon(true);
    toStart.start();

    // se richiesto attende completamento per il tempo indicato
    if(waitMillis != 0 && toStart.isDelayPossible())
      toStart.join(waitMillis);

    // se il task è ancora attivo lo aggiunge alla lista task
    if(toStart.isAlive())
    {
      arTasks.add(toStart);
      return true;
    }

    // se si è verificato un errore lo aggiunge alla lista task
    // questo consente di leggere l'errore dalla lista task
    if(toStart.getRunError() != null)
      arTasks.add(toStart);

    return false;
  }

  @Override
  public synchronized Task creaTaskDaSetup(int idUser, String taskName, Action terminateAction)
     throws Exception
  {
    return TaskFactory.getInstance().build(idUser, taskName, terminateAction);
  }

  @Override
  public synchronized List<Task> getListaTaskUtente(int idUser)
     throws Exception
  {
    ArrayList<Task> arUser = new ArrayList<Task>();

    for(Task task : arTasks)
    {
      if(idUser == task.getIdUser())
        arUser.add(task);
    }

    return arUser;
  }

  @Override
  public synchronized List<Task> getAllTasks()
     throws Exception
  {
    return Collections.unmodifiableList(arTasks);
  }

  @Override
  public synchronized List<Task> findTasks(String taskName)
     throws Exception
  {
    ArrayList<Task> rv = new ArrayList<Task>();

    for(Task t : arTasks)
    {
      if(SU.isEqu(taskName, t.getTaskName()))
        rv.add(t);
    }

    return rv;
  }

  @Override
  public synchronized List<Task> findTasks(String taskName, int idUser)
     throws Exception
  {
    ArrayList<Task> rv = new ArrayList<Task>();

    for(Task t : arTasks)
    {
      if(idUser == t.getIdUser() && SU.isEqu(taskName, t.getTaskName()))
        rv.add(t);
    }

    return rv;
  }

  @Override
  public synchronized boolean isRunning(String taskName)
     throws Exception
  {
    for(Task t : arTasks)
    {
      if(!t.isAlive())
        continue;

      if(SU.isEqu(taskName, t.getTaskName()))
        return true;
    }

    return false;
  }

  @Override
  public synchronized boolean isRunning(String taskName, int idUser)
     throws Exception
  {
    for(Task t : arTasks)
    {
      if(!t.isAlive())
        continue;

      if(idUser == t.getIdUser() && SU.isEqu(taskName, t.getTaskName()))
        return true;
    }

    return false;
  }

  @Override
  public synchronized Task findTask(int idTask)
     throws Exception
  {
    for(Task t : arTasks)
    {
      if(idTask == t.getIdTask())
        return t;
    }
    return null;
  }

  @Override
  public synchronized void rimuoviTask(int idTask, long waitMillis)
     throws Exception
  {
    Task task = findTask(idTask);
    if(task == null)
      throw new CoreServiceException(INT.I("Task con id %d non trovato.", idTask));

    if(task.isAlive())
    {
      if(waitMillis == 0)
        task.join();
      else
        task.join(waitMillis);

      if(task.isAlive())
        throw new IllegalStateException(INT.I("Il task %d %s non si arresta.", idTask, task.getTaskName()));
    }

    arTasks.remove(task);
  }

  @Override
  public synchronized int rimuoviTaskCompletati()
     throws Exception
  {
    int num = 0;
    Iterator<Task> itrTasks = arTasks.iterator();
    while(itrTasks.hasNext())
    {
      Task t = itrTasks.next();
      if(!t.isAlive())
      {
        itrTasks.remove();
        num++;
      }
    }
    return num;
  }
}
