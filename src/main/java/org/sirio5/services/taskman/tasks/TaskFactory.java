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
package org.sirio5.services.taskman.tasks;

import javax.swing.Action;
import org.apache.commons.configuration2.Configuration;
import org.sirio5.utils.factory.CoreAbstractPluginFactory;

/**
 * Costruttore dei task.
 *
 * @author Nicola De Nisco
 */
public class TaskFactory extends CoreAbstractPluginFactory<Task>
{
  private static final TaskFactory theInstance = new TaskFactory();

  private TaskFactory()
  {
  }

  public static TaskFactory getInstance()
  {
    return theInstance;
  }

  public void configure(Configuration cfg)
  {
    super.configure(cfg, "task");
  }

  public Task build(int idUser, String taskName, Action terminateAction)
     throws Exception
  {
    Task task = getPlugin(taskName);
    task.idTask = task.createTaskID();
    task.idUser = idUser;
    task.taskName = taskName;
    task.terminateAction = terminateAction;
    task.setName("Task" + taskName);
    return task;
  }
}
