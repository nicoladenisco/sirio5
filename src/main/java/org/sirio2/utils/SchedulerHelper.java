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
package org.sirio2.utils;

import java.util.List;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.schedule.JobEntry;
import org.apache.turbine.services.schedule.ScheduleService;
import org.apache.turbine.util.TurbineException;
import org.commonlib5.utils.ClassOper;

/**
 * Utility per la gestione semplificata dello scheduler.
 *
 * @author Nicola De Nisco
 */
public class SchedulerHelper
{
  public ScheduleService schedulerService = (ScheduleService) TurbineServices
     .getInstance().getService(ScheduleService.SERVICE_NAME);

  protected List<? extends JobEntry> jobs;

  public SchedulerHelper()
  {
    jobs = schedulerService.listJobs();
  }

  public List<? extends JobEntry> getJobs()
  {
    return jobs;
  }

  /**
   * Cerca un job nello scheduler di Turbine.
   * @param task nome del job
   * @return il job oppure null
   */
  public JobEntry findJob(String task)
  {
    return findJob(task, jobs);
  }

  /**
   * Cerca un job nell'elenco indicato.
   * @param task nome del job
   * @param lsJobs elenco dei jobs
   * @return il job oppure null
   */
  protected JobEntry findJob(String task, List lsJobs)
  {
    if(lsJobs != null)
    {
      for(int i = 0; i < lsJobs.size(); i++)
      {
        JobEntry job = (JobEntry) lsJobs.get(i);
        if(SU.isEqu(task, job.getTask()))
          return job;
      }
    }
    return null;
  }

  /**
   * Cerca o crea una entry nello scheduler.
   * La entry viene creata solo se non esiste
   * altrimenti i parametri sono ignorati.
   * @param sec Value for entry "seconds".
   * @param min Value for entry "minutes".
   * @param hour Value for entry "hours".
   * @param wd Value for entry "week days".
   * @param day_mo Value for entry "month days".
   * @param clazz classe del job da eseguire.
   * @throws TurbineException
   */
  public void findOrCreateJob(int sec, int min, int hour, int wd, int day_mo, Class clazz)
     throws TurbineException
  {
    String jobName = ClassOper.getClassName(clazz);
    if(findJob(jobName) == null)
      schedulerService.addJob(schedulerService.newJob(sec, min, hour, wd, day_mo, jobName));
  }

  /**
   * Distrugge e ricrea una entry nello scheduler.
   * @param sec Value for entry "seconds".
   * @param min Value for entry "minutes".
   * @param hour Value for entry "hours".
   * @param wd Value for entry "week days".
   * @param day_mo Value for entry "month days".
   * @param clazz classe del job da eseguire.
   * @throws TurbineException
   */
  public void removeAndCreateJob(int sec, int min, int hour, int wd, int day_mo, Class clazz)
     throws TurbineException
  {
    JobEntry jeTmp;
    String jobName = ClassOper.getClassName(clazz);

    if((jeTmp = findJob(jobName)) != null)
      schedulerService.removeJob(jeTmp);

    schedulerService.addJob(schedulerService.newJob(sec, min, hour, wd, day_mo, jobName));
  }
}
