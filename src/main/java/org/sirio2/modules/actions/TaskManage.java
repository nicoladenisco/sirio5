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
package org.sirio2.modules.actions;

import java.util.Map;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.sirio2.rigel.RigelHtmlI18n;
import org.sirio2.services.security.SEC;
import org.sirio2.services.taskman.TaskManager;
import org.sirio2.services.taskman.tasks.Task;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Action per la manipolazione dei Task.
 *
 * @author Nicola De Nisco
 */
public class TaskManage extends CoreBaseAction
{
  public static final long WAIT_MILLIS = 3000;
  public TaskManager tm = (TaskManager) getService(TaskManager.SERVICE_NAME);

  @Override
  public void doPerform2(CoreRunData data, Context context)
     throws Exception
  {
    super.doPerform2(data, context);

    String command = SU.okStrNull(data.getParameters().getString("command"));

    if(command != null)
      doCommand(command, data, SU.getParMap(data), context);
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return super.isAuthorizedAll(data, "TaskManage");
  }

  public void doCmd_start(CoreRunData data, Map params, Object... others)
     throws Exception
  {
    String taskName = SU.okStrNull(data.getParameters().getString("nome"));

    if(taskName == null)
      data.throwMessagei18n("Specificare il nome del task da avviare.");

    int idUser = SEC.getUserID(data);
    Task task = tm.creaTaskDaSetup(idUser, taskName, null);
    task.setAcl(SEC.getACL(data.getSession()));
    task.setI18n(new RigelHtmlI18n(data));

    // controlla permessi: se l'utente non ha i permessi viene rediretto alla maschera permessi
    String permessi = SU.okStrNull(task.getPermessi());
    if(permessi != null && !isAuthorizedAny(data, permessi))
      return;

    // avvia il task aspettando 3 secondi
    if(tm.registraAvviaTask(task, WAIT_MILLIS))
    {
      // se in 3 secondi non ha completato passa a visualizzazione task
      data.setScreenTemplate("TaskList.vm");
      return;
    }

    data.setMessagei18n("Task <i>%s</i> concluso.", task.getDescrizione());
  }

  public void doCmd_interrompi(CoreRunData data, Map params, Object... others)
     throws Exception
  {
    int idTask = data.getParameters().getInt("id", 0);

    if(idTask == 0)
      data.throwMessagei18n("Errore interno: identificatore task non specificato.");

    Task task = tm.findTask(idTask);
    if(task == null)
      data.throwMessagei18n("Errore interno: il task specificato non esiste.");

    if(!task.isAlive())
    {
      data.setMessagei18n("Il task %s ha già completato la sua esecuzione.", task.getName());
      data.setScreenTemplate("TaskList.vm");
      return;
    }

    task.terminateTask();
    data.setMessagei18n("Il task %s è stato segnalato per interruzione.", task.getName());
    data.setScreenTemplate("TaskList.vm");
  }

  public void doCmd_rimuovi(CoreRunData data, Map params, Object... others)
     throws Exception
  {
    int idTask = data.getParameters().getInt("id", 0);

    if(idTask == 0)
      data.throwMessagei18n("Errore interno: identificatore task non specificato.");

    Task task = tm.findTask(idTask);
    if(task == null)
      data.throwMessagei18n("Errore interno: il task specificato non esiste.");

    tm.rimuoviTask(idTask, WAIT_MILLIS);
    data.setMessagei18n("Il task %s è stato rimosso dalla lista task.", task.getName());
    data.setScreenTemplate("TaskList.vm");
  }

  public void doCmd_rimuovitutti(CoreRunData data, Map params, Object... others)
     throws Exception
  {
    int num = tm.rimuoviTaskCompletati();
    data.setMessagei18n("Rimossi %d task completati.", num);
    data.setScreenTemplate("TaskList.vm");
  }
}
