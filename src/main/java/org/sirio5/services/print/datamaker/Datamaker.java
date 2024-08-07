/*
 * Datamaker.java
 *
 * Created on 26-set-2012, 17.08.01
 *
 * Copyright (C) 2012 Informatica Medica s.r.l.
 *
 * Questo software è proprietà di Informatica Medica s.r.l.
 * Tutti gli usi non esplicitimante autorizzati sono da
 * considerarsi tutelati ai sensi di legge.
 *
 * Informatica Medica s.r.l.
 * Viale dei Tigli, 19
 * Casalnuovo di Napoli (NA)
 *
 * Creato il 26-set-2012, 17.08.01
 */
package org.sirio5.services.print.datamaker;

import org.sirio5.services.print.PrintContext;
import org.sirio5.utils.factory.CoreBasePoolPlugin;

/**
 * Interfaccia di un pre-generatore di dati.
 * I pre-generatori possono essere utilizzati per generare
 * di dati temporanei da utilizzare nelle stampe.
 *
 * @author Nicola De Nisco
 */
public interface Datamaker extends CoreBasePoolPlugin
{
  /**
   * Preparazione dati intermedi per la stampa.
   * @param context parametri inviati alla stampa.
   * @return oggetto generico di pre-elaborazione (di solito null)
   * @throws Exception interrompe elaborazione della stampa
   */
  public Object prepareData(PrintContext context)
     throws Exception;
}
