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
package org.sirio2.services.modellixml;

import java.util.List;
import org.apache.turbine.util.RunData;
import org.jdom2.Document;
import org.rigel5.glue.WrapperBuilderInterface;
import org.rigel5.glue.WrapperCacheBase;
import org.sirio2.services.CoreServiceExtension;

/**
 * Interfaccia di un generatore di liste in collegamento
 * con Rigel per la generazione di html.
 *
 * @author Nicola De Nisco
 */
public interface modelliXML extends CoreServiceExtension, WrapperBuilderInterface
{
  public static final String SERVICE_NAME = "ModelliXML";

  /**
   * Ritorna il documento XML da cui vengono lette le liste.
   * @return
   */
  public Document getDocument();

  /**
   * Forza un ricaricamento del documento dai files relativi.
   * @throws Exception
   */
  public void forceReloadXML()
     throws Exception;

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario.
   * @param nomeCampo nome HTML del campo
   * @param nomeForm nome del form che contiene i controlli generati
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  public String getCampoData(String nomeCampo, String nomeForm,
     String valore, int size)
     throws Exception;

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario.
   * Il campo generato verra' utilizzato con il suo gemello generato da
   * 'getCampoDataIntervalloFine' che genera il campo finale dell'intervallo.
   * @param nomeCampoInizio nome HTML del campo di inizio intervallo
   * @param nomeCampoFine nome HTML del campo di fine intervallo
   * @param nomeForm
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo di inizio intervallo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  public String getCampoDataIntervalloInizio(String nomeCampoInizio, String nomeCampoFine,
     String nomeForm, String valore, int size)
     throws Exception;

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario.
   * Il campo generato verra' utilizzato con il suo gemello generato da
   * 'getCampoDataIntervalloInizio' che genera il campo iniziale dell'intervallo.
   * @param nomeCampoInizio nome HTML del campo di inizio intervallo
   * @param nomeCampoFine nome HTML del campo di fine intervallo
   * @param nomeForm
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo di fine intervallo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  public String getCampoDataIntervalloFine(String nomeCampoInizio, String nomeCampoFine,
     String nomeForm, String valore, int size)
     throws Exception;

  /**
   * Costruisce un campo di edit con la funzione di ricerca classica.
   * @param nomeCampo nome del campo generato
   * @param valore valore di default all'interno del campo
   * @param size dimensione del campo
   * @param url url per l'editing del campo (lista dei valori)
   * @param valForeign valore per il foreign (se null no descrizione foreign)
   * @param extraScript evantuale extrascript (puo' essere null)
   * @return l'HTML completo del campo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  public String getCampoForeign(String nomeCampo, String valore, int size, String url,
     String valForeign, String extraScript)
     throws Exception;

  /**
   * Recupera una implementazione di WrapperCacheBase dalla sessione.
   * L'oggetto WrapperCacheBase è una cache dei wrapper salvata in sessione.
   * @param data sessione dell'utente corrente
   * @return oggetto WrapperCacheBase
   */
  public WrapperCacheBase getWrapperCache(RunData data);

  /**
   * Rimuove cache dei wrapper dai dati di sessione.
   * @param data sessione dell'utente corrente
   */
  public void removeWrapperCache(RunData data);

  /**
   * Ritorna elenco liste sql disponibili.
   * @return lista non modificabile di stringhe
   */
  public List<String> getListeSql();

  public String getImgCancellaRecord()
     throws Exception;

  public String getImgEditData()
     throws Exception;

  public String getImgEditForeign()
     throws Exception;

  public String getImgLista()
     throws Exception;

  public String getImgEditItem()
     throws Exception;

  public String getImgEditRecord()
     throws Exception;

  public String getImgFormForeign()
     throws Exception;

  public String getImgSelect()
     throws Exception;

  public String getImgSmiley(int stato)
     throws Exception;

  public String getImgCollapse()
     throws Exception;

  public String getImgExpand()
     throws Exception;
}
