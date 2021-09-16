/*
 *  ParametroBuilder.java
 *  Creato il Oct 7, 2020, 5:58:08 PM
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

import java.util.List;
import org.commonlib5.utils.Pair;
import org.sirio5.services.print.AbstractReportParametersInfo;
import org.sirio5.utils.factory.CoreBasePlugin;

/**
 * Plugin per la generazione di parametri per le stampe custom.
 *
 * @author Nicola De Nisco
 */
public interface ParametroBuilder extends CoreBasePlugin
{
  List<Pair<String, String>> preparaValori(AbstractReportParametersInfo pinfo, Class tipoParametro)
     throws Exception;
}
