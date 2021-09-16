/*
 *  CaleidoLogout.java
 *  Creato il 13-mar-2013, 13.06.38
 *
 *  Copyright (C) 2013 Informatica Medica s.r.l.
 *
 *  Questo software è proprietà di Informatica Medica s.r.l.
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Informatica Medica s.r.l.
 *  Viale dei Tigli, 19
 *  Casalnuovo di Napoli (NA)
 */
package org.sirio5.modules.actions;

import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.turbine.modules.actions.LogoutUser;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.sirio5.utils.CoreRunData;

/**
 * Azione di logout.
 * Corregge un errore nella LogoutUser standard di Turbine.
 * L'oggetto RunData non è consistente per l'utente e quindi
 * l'operazione di salvataggio dello storage permanente
 * fallisce in alcuni casi.
 *
 * @author Nicola De Nisco
 */
public class CoreLogout extends LogoutUser
{
  @Override
  public void doPerform(PipelineData pd)
     throws FulcrumSecurityException
  {
    CoreRunData data = (CoreRunData) getRunData(pd);

    // questo provoca un refresh delle informazioni
    // dell'utente dai dati di sessione
    data.userExists();

    super.doPerform(data);

    data.getSession().invalidate();
  }

  public RunData getRunData(PipelineData data)
  {
    if(data instanceof RunData)
      return (RunData) data;

    throw new RuntimeException("Invalid object data.");
  }
}
