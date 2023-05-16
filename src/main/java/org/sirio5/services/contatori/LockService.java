/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.services.contatori;

import java.util.Set;
import org.apache.turbine.services.Service;

/**
 * Servizio per il locking di risorse.
 * Consente di definire dei lock da acquisire per l'accesso
 * a risorse condivise fra più utenti.
 * Ogni utente può acquisire un accesso esclusivo a una risorsa.
 *
 * @author Nicola De Nisco
 */
public interface LockService extends Service
{
  public static final String SERVICE_NAME = "LockService";

  /**
   * Ritorna pachetto di informazioni di sincronizzazione.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @return
   * @throws LockException
   */
  public LockResourceItem getItem(String tipo, int idRisorsa)
     throws LockException;

  /**
   * Crea una risorsa del tipo indicato.
   * @param tipo tipo univoco della risorsa
   * @param maxLocks numero massimo di lock possibili prima del blocco (di solito 1)
   * @param allowMulti consentito il multi (acquisizione contemporanea da parte di più utenti)
   */
  public void createResource(String tipo, int maxLocks, boolean allowMulti);

  /**
   * Richiede il blocco di una risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public void lockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException;

  /**
   * Richiede il blocco di più risorse contemporaneamente.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public void lockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException;

  /**
   * Richiede il blocco di una risorsa da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public void lockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException;

  /**
   * Richiede il blocco di più risorse contemporaneamente da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public void lockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException;

  /**
   * Richiede lo sblocco di una risorsa.
   * L'utente deve essere lo stesso che ha eseguito il blocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public void unlockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException;

  /**
   * Richiede lo sblocco di più risorse contemporaneamente.
   * Il pacchetto di risorse deve essere lo stesso sottoposto al momento del blocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public void unlockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException;

  /**
   * Richiede lo sblocco di una risorsa da parte di più utenti.
   * Uno qualsiasi degli utenti indicati deve comparire
   * nell'elenco di quelli che hanno bloccato la risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public void unlockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException;

  /**
   * Richiede lo sblocco di più risorse contemporaneamente da parte di più utenti.
   * Il pacchetto di risorse deve essere lo stesso sottoposto al momento del blocco.
   * Uno qualsiasi degli utenti indicati deve comparire
   * nell'elenco di quelli che hanno bloccato la risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public void unlockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException;

  /**
   * Tenta il blocco di una risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public boolean tryLockResource(String tipo, int idRisorsa, int idUtente, long timeoutMillis)
     throws LockException;

  /**
   * Tenta il blocco di più risorse contemporaneamente.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public boolean tryLockResources(String tipo, Set<Integer> idRisorse, int idUtente, long timeoutMillis)
     throws LockException;

  /**
   * Tenta il blocco di una risorsa da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public boolean tryLockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException;

  /**
   * Tenta il blocco di più risorse contemporaneamente da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public boolean tryLockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException;

}
