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
package org.sirio5.services.print.utils;

import com.itextpdf.text.Font;

/**
 * Definizione di un produttore di dati per PdfTableBuilder.
 *
 * @author Nicola De Nisco
 */
public interface TableBuilderListner
{
  /**
   * Numero di righe da stampare con contenuto dati.
   * @return numero di righe
   */
  public int getNumDataRow();

  /**
   * Un eventuale numero di righe vuote da aggiungere
   * alla fine della zona dati.
   * @return numero di righe
   */
  public int getNumEmptyRow();

  /**
   * Recupera un array con i dati da inserire in una riga.
   * Nell'array sono consentiti i null; il dato da stampare
   * viene ottenuto con il metodo toString().
   * @param row riga da stampare
   * @param numCol numero di colonne (e lunghezza dell'array ritornato)
   * @return
   */
  public Object[] getRowData(int row, int numCol);

  /**
   * Ritorna lo spessore richiesto per il bordo superiore della riga.
   * @param row numero della riga in tabella
   * @return spessore bordo (0=nessuno)
   */
  public float getBorderTop(int row);

  /**
   * Ritorna lo spessore richiesto per il bordo inferiore della riga.
   * @param row numero della riga in tabella
   * @return spessore bordo (0=nessuno)
   */
  public float getBorderBottom(int row);

  /**
   * Recupera il font desiderato per la riga indicata.
   * Con questo font verranno stampate tutte le celle della riga indicata.
   * @param row la riga
   * @return font da utilizzare
   */
  public Font getRowFont(int row);

  /**
   * Recupera il font desiderato per l'intestazione della tabella.
   * Con questo font verranno stampate tutte le celle dell'intestazione
   * @return font da utilizzare
   */
  public Font getHeaderFont();

  /**
   * Recupera il font desiderato per la chiusura della tabella.
   * La chiusura è vuota (non contiene alcun dato) ma il font ne determina l'altezza.
   * @return font da utilizzare
   */
  public Font getFooterFont();

  /**
   * Ritorna vero se è richiesta la stampa dell'header.
   * @return
   */
  public boolean haveHeader();

  /**
   * Ritorna vero se è richiesta la stampa della riga di chiusura.
   * @return
   */
  public boolean haveFooter();
}
