/*
 *  ParametroBuilderFactory.java
 *  Creato il Oct 7, 2020, 6:00:23 PM
 *
 *  Copyright (C) 2020 Informatica Medica s.r.l.
 *
 *  Questo software è proprietà di Informatica Medica s.r.l.
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Informatica Medica s.r.l.
 *  Viale dei Tigli, 19
 *  Casalnuovo di Napoli (NA)
 */
package org.sirio5.services.print.parametri;

import org.apache.commons.configuration2.Configuration;
import org.sirio5.utils.factory.CoreAbstractPluginFactory;

/**
 * Factory per i plugin dei parametri custom.
 *
 * @author Nicola De Nisco
 */
public class ParametroBuilderFactory extends CoreAbstractPluginFactory<ParametroBuilder>
{
  private static ParametroBuilderFactory theInstance = new ParametroBuilderFactory();

  private ParametroBuilderFactory()
  {
  }

  public static ParametroBuilderFactory getInstance()
  {
    return theInstance;
  }

  public void configure(Configuration cfg)
  {
    super.configure(cfg, "parametri");
  }
}
