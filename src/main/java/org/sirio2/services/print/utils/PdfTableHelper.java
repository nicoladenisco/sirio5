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
package org.sirio2.services.print.utils;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * Interfaccia di un helper per generare tabelle PDF.
 *
 * @author Nicola De Nisco
 */
public interface PdfTableHelper
{
  public static class ColumnInfo
  {
    public int width = 0;
    public String caption = null;
    public int align = PdfPTable.ALIGN_LEFT;
  }

  public static final int[] htesFull =
  {
    100
  };

  /**
   * Aggiunge una nuova colonna.
   * ATTENZIONE: la somma delle width deve essere 100.
   * @param caption nome della colonna da inserire nell'header
   * @param width dimensioni in percentuale
   * @param align allineamento (Es. PdfPTable.ALIGN_LEFT)
   */
  void addColumn(String caption, int width, int align);

  /**
   * Pulisce i dati accumulati.
   */
  void clear();

  /**
   * Restituisce una array di Object della stessa dimensione
   * del numero di colonne affinche possa essere popolato con
   * i valori e passato a stampa().
   * @return
   */
  Object[] getNewValuesArray();

  /**
   * Restituisce la tabella pdf appositamente formattata
   * e con il layout impostato per contenere una riga di dettaglio.
   * Alla tabella vanno aggiunte le celle relative nella stessa quantita
   * del numero di colonne.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable getTabella()
     throws Exception;

  /**
   * Restituisce la tabella pdf con una linea orizontale.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable hr()
     throws Exception;

  /**
   * Stampa una riga di valori.
   * @param font
   * @param values array con i valori
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable stampa(Font font, Object[] values)
     throws Exception;

  /**
   * Stampa la riga di chiusura dell'area righe.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable stampaChiusura()
     throws Exception;

  /**
   * Stampa una riga con le intestazioni di tabella.
   * @param font
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable stampaInstestazione(Font font)
     throws Exception;

  /**
   * Stampa una riga con tutti i campi vuoti.
   * Nel primo campo viene inserito il valore '--' per
   * occupare efficacemente la riga e darle una dimensione
   * in base al font utilizzato.
   * @param font
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  PdfPTable stampaVuota(Font font)
     throws Exception;

  /**
   * Stampa una riga con tutti i campi vuoti.
   * Nel primo campo viene inserito un valore per
   * occupare efficacemente la riga e darle una dimensione
   * in base al font utilizzato.
   * @param font
   * @param dummyValue valore da inserire nella prima cella
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  public PdfPTable stampaVuota(Font font, String dummyValue)
     throws Exception;

  /**
   * Stampa una unica cella che copre tutto lo spazio della tabella.
   * @param font font da utilizzare
   * @param value valore da stampare
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  public PdfPTable stampaCellaUnica(Font font, Object value)
     throws Exception;
}
