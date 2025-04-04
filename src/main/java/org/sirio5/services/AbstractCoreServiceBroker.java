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
package org.sirio5.services;

import java.io.File;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.util.TorqueConnection;
import org.apache.torque.util.Transaction;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.ServiceManager;
import org.apache.turbine.services.schedule.JobEntryTorque;
import org.apache.turbine.services.schedule.JobEntryTorquePeer;
import org.apache.turbine.util.TurbineException;
import org.commonlib5.utils.CommonFileUtils;
import org.rigel5.db.DbUtils;
import static org.sirio5.services.localization.INT.I;

/**
 * Gestore servizi personalizzato.
 * La sua funzione principale (rispetto al default) è di
 * caricare gli override di setup prima dell'inizializzazione dei servizi.
 * Questo service broker attiva prima il servizio Torque, quindi carica
 * gli override di setup e solo dopo inizializza gli altri servizi.
 *
 * Per utilizzare questo service broker è necessario utilizzare la chimata
 * TurbineServices.setManager(new CaleidoServiceBroker()) prima che la
 * servlet Turbine sia inizializzata.
 * Questo avviene nella servlet speciale TurbineCaleidoServlet che di fatto
 * estende Turbine solo per settare questo service broker prima dell'inizializzazione.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
abstract public class AbstractCoreServiceBroker extends EffectiveBaseServiceBroker
   implements ServiceManager
{
  private static final Log log = LogFactory.getLog(AbstractCoreServiceBroker.class);

  /**
   * Initialize this service manager.
   * @throws org.apache.turbine.services.InitializationException
   */
  @Override
  public void init()
     throws InitializationException
  {
    try
    {
      initSirio();
    }
    catch(Exception e)
    {
      log.error("ERRORE FATALE INIZIALIZZAZIONE SERVIZI.", e);
      throw new InitializationException(e.getMessage(), e);
    }
  }

  protected void initSirio()
     throws Exception
  {
    // se la directory temporanea principale di sistema non esiste
    // oppure non è scrivibile il layer fulcrum si incazza a bestia
    // tomcat modifica questa proprietà e non sempre la directory esiste
    String tempRootDir = System.getProperty("java.io.tmpdir", ".");
    log.info("tempRootDir=" + tempRootDir);
    File tmpDir = new File(tempRootDir);
    ASSERT_DIR_WRITE(tmpDir);

    // primo caricamento mappatura servizi
    initMapping();

    // avvia torque e carica override di setup
    startTorque();
    loadOverride();

    // ricarica mappatura servizi
    mapping.clear();
    initMapping();

    // avvia modelli XML che deve salire subito dopo il db
    initService("ModelliXML");

    // assicura che lo scheduler sia valido
    sanityScheduler();

    // avvia tutti gli altri servizi di conseguenza
    initServices(false);
  }

  private void startTorque()
     throws InitializationException
  {
    try
    {
      initService("AvalonComponentService");
    }
    catch(Throwable ex)
    {
      log.error("Errore inizializzando Torque (Avalon) impossibile continuare.", ex);
      throw ex;
    }
  }

  /**
   * Carica override di setup dal database.
   * @throws InitializationException
   */
  abstract protected void loadOverride()
     throws InitializationException;

  /**
   * Verifica che i job caricati nello scheduler siano coerenti con le regole.
   * Se i job non sono coerenti questo provoca un mancato avvio del servizio.
   * @throws InitializationException
   */
  protected void sanityScheduler()
     throws InitializationException
  {
    try(TorqueConnection con = Transaction.begin())
    {
      if(!DbUtils.existTableExact(con, JobEntryTorquePeer.TABLE_NAME))
        return;

      // per sicurezza: la sanityDatabase potrebbe aver inserito il record
      deleteJob(0, con);

      List<JobEntryTorque> jobs = JobEntryTorquePeer.doSelect(new Criteria(), con);
      for(JobEntryTorque job : jobs)
      {
        try
        {
          job.calcRunTime();
        }
        catch(TurbineException e)
        {
          log.error(String.format("Job %d non ha una coerenza nella programmazione: viene eliminato.", job.getJobId()));
          deleteJob(job.getJobId(), con);
        }
      }

      Transaction.commit(con);
    }
    catch(Exception ex)
    {
      throw new InitializationException(String.format("Errore fatale aggiornando lo scheduler."), ex);
    }
  }

  protected void deleteJob(int jobId, Connection con)
     throws TorqueException
  {
    DbUtils.executeStatement(
       "DELETE FROM turbine_scheduled_job\n"
       + " WHERE job_id=" + jobId, con
    );
  }

  public void arrestaBackgroundTask()
  {
    Iterator<String> itr = getServiceNames();
    while(itr.hasNext())
    {
      String serviceName = itr.next();
      Object serviceObject = getServiceObject(serviceName);

      if(serviceObject instanceof CoreBackgroundService)
      {
        log.info(I("Arresto task background per servizio %s", serviceName));
        ((CoreBackgroundService) serviceObject).stopAuto();
      }
    }
  }

  public void avviaBackgroundTask()
  {
    Iterator<String> itr = getServiceNames();
    while(itr.hasNext())
    {
      String serviceName = itr.next();
      Object serviceObject = getServiceObject(serviceName);

      if(serviceObject instanceof CoreBackgroundService)
      {
        log.info(I("Avvio task background per servizio %s", serviceName));
        ((CoreBackgroundService) serviceObject).startAuto();
      }
    }
  }

  public void ricaricaConfigurazione()
  {
    Iterator<String> itr = getServiceNames();
    while(itr.hasNext())
    {
      String serviceName = itr.next();
      Object serviceObject = getServiceObject(serviceName);

      if(serviceObject instanceof CoreBackgroundService)
      {
        log.info(I("Ricarica configurazione per servizio %s", serviceName));
        try
        {
          ((CoreBackgroundService) serviceObject).ricaricaConfigurazione();
        }
        catch(Exception ex)
        {
          log.error(I("Errore ricaricando configurazione servizio %s", serviceName), ex);
        }
      }
    }
  }

  public void ricaricaOverrideSetup()
  {
    try
    {
      loadOverride();
    }
    catch(Exception e)
    {
      log.error("ricaricaOverrideSetup failure", e);
    }
  }

  public void ASSERT_DIR_WRITE(File toTest)
     throws Exception
  {
    toTest.mkdirs();
    if(!(toTest.exists() && toTest.isDirectory() && CommonFileUtils.checkDirectoryWritable(toTest)))
    {
      String mess = "ASSERT_DIR_WRITE failed: la directory " + toTest.getAbsolutePath()
         + " non esiste o non e' scrivibile.";
      log.debug(mess);
      throw new CoreServiceException(mess);
    }
  }
}
