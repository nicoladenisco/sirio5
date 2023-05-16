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
import org.apache.turbine.services.TurbineServices;

/**
 * Utility per il lock.
 *
 * @author Nicola De Nisco
 */
public class LOCK
{
  private static LockService __lk = null;

  public static LockService getService()
  {
    if(__lk == null)
      __lk = (LockService) TurbineServices.getInstance().
         getService(LockService.SERVICE_NAME);
    return __lk;
  }

  /**
   * Ritorna pachetto di informazioni di sincronizzazione.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @return
   * @throws LockException
   */
  public static LockResourceItem getItem(String tipo, int idRisorsa)
     throws LockException
  {
    return getService().getItem(tipo, idRisorsa);
  }

  /**
   * Crea una risorsa del tipo indicato.
   * @param tipo tipo univoco della risorsa
   * @param maxLocks numero massimo di lock possibili prima del blocco (di solito 1)
   * @param allowMulti consentito il multi (acquisizione contemporanea da parte di più utenti)
   */
  public static void createResource(String tipo, int maxLocks, boolean allowMulti)
  {
    getService().createResource(tipo, maxLocks, allowMulti);
  }

  /**
   * Richiede il blocco di una risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public static void lockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException
  {
    getService().lockResource(tipo, idRisorsa, idUtente);
  }

  /**
   * Richiede il blocco di più risorse contemporaneamente.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public static void lockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException
  {
    getService().lockResources(tipo, idRisorse, idUtente);
  }

  /**
   * Richiede il blocco di una risorsa da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public static void lockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException
  {
    getService().lockResourceMulti(tipo, idRisorsa, idUtenti);
  }

  /**
   * Richiede il blocco di più risorse contemporaneamente da parte di più utenti.
   * Uno qualsiasi degli utenti indicati potrà eseguire lo sblocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public static void lockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException
  {
    getService().lockResourcesMulti(tipo, idRisorse, idUtenti);
  }

  /**
   * Richiede lo sblocco di una risorsa.
   * L'utente deve essere lo stesso che ha eseguito il blocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public static void unlockResource(String tipo, int idRisorsa, int idUtente)
     throws LockException
  {
    getService().unlockResource(tipo, idRisorsa, idUtente);
  }

  /**
   * Richiede lo sblocco di più risorse contemporaneamente.
   * Il pacchetto di risorse deve essere lo stesso sottoposto al momento del blocco.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @throws LockException
   */
  public static void unlockResources(String tipo, Set<Integer> idRisorse, int idUtente)
     throws LockException
  {
    getService().unlockResources(tipo, idRisorse, idUtente);
  }

  /**
   * Richiede lo sblocco di una risorsa da parte di più utenti.
   * Uno qualsiasi degli utenti indicati deve comparire
   * nell'elenco di quelli che hanno bloccato la risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtenti utenti che voigliono eseguire il blocco
   * @throws LockException
   */
  public static void unlockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti)
     throws LockException
  {
    getService().unlockResourceMulti(tipo, idRisorsa, idUtenti);
  }

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
  public static void unlockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti)
     throws LockException
  {
    getService().unlockResourcesMulti(tipo, idRisorse, idUtenti);
  }

  /**
   * Tenta il blocco di una risorsa.
   * @param tipo tipo univoco della risorsa
   * @param idRisorsa identificativo univoco della risorsa
   * @param idUtente utente che vuole eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public static boolean tryLockResource(String tipo, int idRisorsa, int idUtente, long timeoutMillis)
     throws LockException
  {
    return getService().tryLockResource(tipo, idRisorsa, idUtente, timeoutMillis);
  }

  /**
   * Tenta il blocco di più risorse contemporaneamente.
   * @param tipo tipo univoco della risorsa
   * @param idRisorse identificativi univoci della risorse
   * @param idUtente utente che vuole eseguire il blocco
   * @param timeoutMillis tempo di attesa per il blocco in millisecondi
   * @return vero se il blocco ha avuto successo
   * @throws LockException
   */
  public static boolean tryLockResources(String tipo, Set<Integer> idRisorse, int idUtente, long timeoutMillis)
     throws LockException
  {
    return getService().tryLockResources(tipo, idRisorse, idUtente, timeoutMillis);
  }

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
  public static boolean tryLockResourceMulti(String tipo, int idRisorsa, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException
  {
    return getService().tryLockResourceMulti(tipo, idRisorsa, idUtenti, timeoutMillis);
  }

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
  public static boolean tryLockResourcesMulti(String tipo, Set<Integer> idRisorse, Set<Integer> idUtenti, long timeoutMillis)
     throws LockException
  {
    return getService().tryLockResourcesMulti(tipo, idRisorse, idUtenti, timeoutMillis);
  }
}
