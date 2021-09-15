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
package org.sirio2.services.taskman.tasks;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.Action;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.services.Service;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.LongOperListenerDouble;
import org.rigel5.RigelI18nInterface;
import org.sirio2.rigel.RigelDefaultI18n;
import org.sirio2.services.allarmi.ALLARM;
import org.sirio2.services.taskman.AsyncTaskException;
import org.sirio2.services.taskman.TaskManager;
import org.sirio2.utils.factory.CoreBasePlugin;

/**
 * Classe base di un Task.
 * I task utente estendono questa classe specializzandola
 * per un compito specifico.
 *
 * @author Nicola De Nisco
 */
public abstract class Task extends Thread
   implements LongOperListenerDouble, CoreBasePlugin
{
  /** Identificatore univoco del task. */
  protected int idTask;
  /** Utente proprietario di questo task. */
  protected int idUser = 0;
  /** Permessi del proprietario del task. */
  protected TurbineAccessControlList acl = null;
  /** Nome del task. */
  protected String taskName = "anonymous";
  /** Descrizione del task. */
  protected String descrizione = "anonymous";
  /** Messaggio di elaborazione del task. */
  protected String messaggio = "";
  /** Permessi richiesti all'utente per l'esecuzione. */
  protected String permessi = null;
  /** Eventuale azione da eseguire al termina del task. */
  protected Action terminateAction = null;
  /** Memorizza eventuale errore di esecuzione. */
  protected Throwable runError = null;
  /** Flag per salvare negli allarmi eventuali errori di esecuzione. */
  protected boolean errorInAllarm = false;
  /** Contatori per avanzamento del task. */
  protected long part, total, mainPart, mainTotal;
  /** Flag per interruzione task. */
  protected boolean interrotto = false;
  /** Stato del task (vedi ST_..). */
  protected int taskState = 0;
  /** Indica se questo task è destinato a produrre files. */
  protected boolean prodFiles = false;
  /** Eventuali file da scaricare collegati. */
  protected ArrayList<String> arFileCacheTickets = new ArrayList<String>();
  /** Inizio e fine del task. */
  protected Date tStart, tEnd;
  /** Logging. */
  protected Log log = LogFactory.getLog(this.getClass());
  /** Supporto all'internazionalizzazione. */
  protected RigelI18nInterface i18n = new RigelDefaultI18n();
  //
  public static final int ST_CREATED = 0;
  public static final int ST_RUNNING = 1;
  public static final int ST_COMPLETED = 2;
  public static final int ST_ERROR = 3;
  public static final int ST_ABORT = 4;

  /**
   * Costruttore per classi derivate e
   * per TaskFactory.
   * I dati interni non sono inizializzati.
   * E' compito del chiamante popolare correttamente
   * ID, taskname, iduser, ecc.
   */
  public Task()
  {
    this.taskState = ST_CREATED;
    resetUI();
  }

  /**
   * Costruttore del task.
   * Il task viene costruito, assegnando un ID univoco
   * e popolando correttamente i valori interni.
   * @param idUser id dell'utente possessore del task
   * @param name nome del task
   * @param ta eventuale azione di terminazione (può essere null)
   */
  public Task(int idUser, String name, Action ta)
  {
    this.idUser = idUser;
    this.taskName = name;
    this.taskState = ST_CREATED;
    this.terminateAction = ta;
    this.idTask = createTaskID();
    setName("Task_" + name);
    resetUI();
  }

  /**
   * Creazione di un ID univoco per un task.
   * Genera un ID univoco usando un contatore permanente
   * utile per assegnarlo ad in ID di task.
   * @return ID univoco
   */
  protected int createTaskID()
  {
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }

  /**
   * Imposta configurazione del task.
   * Viene chiamata dalla TaskFactory per i task creati da setup.
   * @param name nome del task
   * @param cfg parametri di configurazione
   * @throws Exception
   */
  @Override
  public void configure(String name, Configuration cfg)
     throws Exception
  {
    taskName = name;
    setName("Task" + name);
    descrizione = cfg.getString("descrizione", name);
    permessi = cfg.getString("permessi");
  }

  /**
   * Funzione di servizio del thread.
   * Chiama doRun() per l'esecuzione.
   */
  @Override
  final public void run()
  {
    taskState = ST_RUNNING;
    tStart = new Date();

    try
    {
      doRun();

      if(!interrotto)
        taskState = ST_COMPLETED;
    }
    catch(Throwable e)
    {
      runError = e;
      log.error(i18n.msg("Error in running task."), e);
      taskState = ST_ERROR;

      if(errorInAllarm)
        ALLARM.error(TaskManager.SERVICE_NAME, getTaskName(), e.getMessage(), 0);
    }

    tEnd = new Date();

    try
    {
      doFinish();
    }
    catch(Exception e)
    {
      log.error(i18n.msg("Errore in doFinish(): "), e);
    }
  }

  /**
   * Esecuzione del compito specifico del task.
   * Da ridefinire in classi derivate.
   * @throws Exception
   */
  abstract public void doRun()
     throws Exception;

  /**
   * Azione eseguita alla fine del task.
   * Prima della terminazione del task, il thread
   * dedicato esegue questa funzione per eventuali
   * operazioni di chiusura.
   * Questa funzione viene invocata anche se doRun()
   * solleva eccezione.
   * @throws Exception
   */
  public void doFinish()
     throws Exception
  {
    if(terminateAction != null)
      terminateAction.actionPerformed(new ActionEvent(this, idTask, taskName));
  }

  /**
   * Ritorna il nome del task.
   * @return stringa nome
   */
  public String getTaskName()
  {
    return taskName;
  }

  /**
   * Ritorna una descrizione del task.
   * @return stringa descrizione
   */
  public String getDescrizione()
  {
    return descrizione;
  }

  /**
   * Ritorna messaggio di elaborazione del task.
   * @return stringa messaggio
   */
  public String getMessaggio()
  {
    return messaggio;
  }

  /**
   * Permessi richiesti all'utente.
   * L'utente deve possedere almeno uno dei permessi
   * specificati. Il valore null indica nessun controllo
   * sui permessi.
   * @return uno o più permessi separati da virgola.
   */
  public String getPermessi()
  {
    return permessi;
  }

  public void setPermessi(String val)
  {
    permessi = val;
  }

  public TurbineAccessControlList getAcl()
  {
    return acl;
  }

  public void setAcl(TurbineAccessControlList acl)
  {
    this.acl = acl;
  }

  public RigelI18nInterface getI18n()
  {
    return i18n;
  }

  public void setI18n(RigelI18nInterface i18n)
  {
    this.i18n = i18n;
  }

  /**
   * Flag di esclusività del task.
   * Se il task ritorna true, una sola istanza di
   * questo task può essere attiva. Un tentativo
   * di avviare una nuova istanza con lo stesso nome
   * provoca una eccezione.
   * Per default questo valore è falso: possono esistere
   * più istanze dello statesso task in esecuzione.
   * @return flag di esclusività del task
   */
  public boolean isExclusive()
  {
    return false;
  }

  /**
   * Flag ritardo avvio possibile.
   * Normalmente quando viene avviato un Task
   * il task manager attende un tempo breve prima di
   * passare all'esecuzione in background.
   * Se questo flag ritorna false questa breve attese viene evitata
   * e il task va direttamente in background.
   * @return vero per ritardare l'avvio
   */
  public boolean isDelayPossible()
  {
    return true;
  }

  /**
   * Ritorna l'identificativo univoco del task.
   * @return id del task
   */
  public long getIdTask()
  {
    return idTask;
  }

  /**
   * Ritorna l'utente possessore del task.
   * @return id dell'utente
   */
  public int getIdUser()
  {
    return idUser;
  }

  @Override
  public void completeUI(long total)
  {
    this.part = this.total = total;
  }

  @Override
  public void resetUI()
  {
    this.part = this.total = 0;
  }

  @Override
  public boolean updateUI(long part, long total)
  {
    this.part = part;
    this.total = total;
    return !interrotto;
  }

  @Override
  public void resetMainUI()
  {
    mainPart = mainTotal = 0;
  }

  @Override
  public void completeMainUI(long total)
  {
    mainPart = mainTotal = total;
  }

  @Override
  public boolean updateMainUI(long part, long total)
  {
    mainPart = part;
    mainTotal = total;
    return !interrotto;
  }

  @Override
  public void displaySubOperation(String subOperation)
  {
    messaggio = subOperation;
  }

  public void terminateTask()
  {
    interrotto = true;
    taskState = ST_ABORT;
  }

  public Action getTerminateAction()
  {
    return terminateAction;
  }

  public void setTerminateAction(Action terminateAction)
  {
    this.terminateAction = terminateAction;
  }

  public boolean isErrorInAllarm()
  {
    return errorInAllarm;
  }

  public void setErrorInAllarm(boolean errorInAllarm)
  {
    this.errorInAllarm = errorInAllarm;
  }

  public boolean isInterrotto()
  {
    return interrotto;
  }

  public Throwable getRunError()
  {
    return runError;
  }

  public long getPart()
  {
    return part;
  }

  public long getTotal()
  {
    return total;
  }

  public int getPerc()
  {
    return total == 0 ? 0 : (int) ((part * 100L) / total);
  }

  public int getTaskState()
  {
    return taskState;
  }

  public String getDescState()
  {
    switch(taskState)
    {
      case ST_CREATED:
        return i18n.msg("Creato");
      case ST_RUNNING:
        return i18n.msg("In esecuzione");
      case ST_COMPLETED:
        return i18n.msg("Completato con successo");
      case ST_ERROR:
        return i18n.msg("Completato con errori");
      case ST_ABORT:
        return i18n.msg("Interrotto dall'utente");
      default:
        return i18n.msg("Sconosciuto");
    }
  }

  public String getHtmlDesc()
  {
    if(taskState == ST_ERROR)
      return runError.getMessage();

    int perc = getPerc();

    String msg = "";

    if(mainPart != 0 && mainTotal != 0)
      msg += i18n.msg("Fase %d di %d", mainPart, mainTotal) + " - ";

    if(isAlive())
    {
      if(part == 0)
        return msg + i18n.msg("Avvio operazioni in corso ...");
      else
        return msg + i18n.msg("Eseguito: %d di %d totali (%d%%).", part, total, perc);
    }
    else
    {
      if(interrotto)
        return msg + i18n.msg("Interrotto: %d di %d elementi processati (%d%%).", part, total, perc);
      else
        return msg + i18n.msg("Completato: %d elementi processati.", total);
    }
  }

  public boolean isProdFiles()
  {
    return prodFiles;
  }

  public void setProdFiles(boolean prodFiles)
  {
    this.prodFiles = prodFiles;
  }

  public boolean haveFiles()
  {
    return !arFileCacheTickets.isEmpty();
  }

  public List<String> getFileCacheTickets()
  {
    return Collections.unmodifiableList(arFileCacheTickets);
  }

  public <T extends Service> T getService(String serviceName)
  {
    return (T) TurbineServices.getInstance().getService(serviceName);
  }

  public Date gettStart()
  {
    return tStart;
  }

  public Date gettEnd()
  {
    return tEnd;
  }

  public void die(String message)
     throws AsyncTaskException
  {
    throw new AsyncTaskException(message);
  }

  public void die(String message, Throwable cause)
     throws AsyncTaskException
  {
    throw new AsyncTaskException(message, cause);
  }

  public long getElapsed()
  {
    return tStart == null ? -1 : System.currentTimeMillis() - tStart.getTime();
  }

  public long getTotalTime()
  {
    return (tEnd == null || tStart == null) ? -1 : tEnd.getTime() - tStart.getTime();
  }
}
