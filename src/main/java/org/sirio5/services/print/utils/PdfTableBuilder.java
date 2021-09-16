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


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import org.sirio5.utils.SU;
import java.util.ArrayList;


/**
 * Classe di supporto per la visualizzazione di elementi tabellati
 * all'interno di una stampa PDF.
 * La tabella viene generata in un unico corpo quindi non può essere
 * intervallata con dei salti pagina. In compenso è possibile stabilire
 * dei bordi per ogni riga stampata.
 *
 * @author Nicola De Nisco
 */
public class PdfTableBuilder
{
  protected ArrayList<ColumnInfo> arCol = new ArrayList<ColumnInfo>();
  protected int irec = 0;
  protected int arrayWidthCol[] = null;

  public static class ColumnInfo
  {
    public int width = 0;
    public String caption = null;
    public int align = PdfPTable.ALIGN_LEFT;
  }

  /**
   * Aggiunge una nuova colonna.
   * ATTENZIONE: la somma delle width deve essere 100.
   * @param caption nome della colonna da inserire nell'header
   * @param width dimensioni in percentuale
   * @param align allineamento (Es. PdfPTable.ALIGN_LEFT)
   */
  public void addColumn(String caption, int width, int align)
  {
    ColumnInfo ci = new ColumnInfo();
    ci.caption = caption;
    ci.width = width;
    ci.align = align;
    arCol.add(ci);
  }

  /**
   * Restituisce una array di Object della stessa dimensione
   * del numero di colonne affinche possa essere popolato con
   * i valori e passato a stampa().
   * @return
   */
  public Object[] getNewValuesArray()
  {
    return new Object[arCol.size()];
  }

  /**
   * Restituisce la tabella pdf appositamente formattata
   * e con il layout impostato per contenere una riga di dettaglio.
   * Alla tabella vanno aggiunte le celle relative nella stessa quantita
   * del numero di colonne.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  protected PdfPTable getTabella() throws Exception
  {
    if(arrayWidthCol == null)
    {
      arrayWidthCol = new int[arCol.size()];
      for(int i = 0; i < arrayWidthCol.length; i++)
        arrayWidthCol[i] = arCol.get(i).width;
    }

    PdfPTable tbrig = new PdfPTable(arrayWidthCol.length);
    tbrig.setHeaderRows(0);
    tbrig.setWidths(arrayWidthCol);
    tbrig.setWidthPercentage(100);
    tbrig.getDefaultCell().setColspan(1);
    return tbrig;
  }

  /**
   * Stampa una riga con le intestazioni di tabella.
   * @param font
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  protected PdfPTable stampaInstestazione(PdfPTable tbrig, Font font) throws Exception
  {
    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
    tbrig.getDefaultCell().setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

    tbrig.getDefaultCell().setBorderWidthLeft(0.8f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setBorderWidthTop(0.8f);

    tbrig.addCell(new Phrase(arCol.get(0).caption, font));

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    for(int i = 1; i < (arCol.size() - 1); i++)
    {
      tbrig.getDefaultCell().setHorizontalAlignment(arCol.get(i).align);
      tbrig.addCell(new Phrase(arCol.get(i).caption, font));
    }

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.8f);
    tbrig.addCell(new Phrase(arCol.get(arCol.size() - 1).caption, font));

    return tbrig;
  }

  /**
   * Stampa una riga con tutti i campi vuoti.
   * Nel primo campo viene inserito il valore '--' per
   * occupare efficacemente la riga e darle una dimensione
   * in base al font utilizzato.
   * @param font
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  protected PdfPTable stampaVuota(PdfPTable tbrig, Font font, float borderTop, float borderBottom)
     throws Exception
  {
    Object[] values = new Object[arCol.size()];
    values[0] = "--";
    return stampa(tbrig, font, values, borderTop, borderBottom);
  }

  /**
   * Stampa una riga di valori.
   * @param font
   * @param values array con i valori (deve avere la stessa lunghezza del numero colonne)
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  protected PdfPTable stampa(PdfPTable tbrig, Font font, Object[] values, float borderTop, float borderBottom)
     throws Exception
  {
    if(values.length != arCol.size())
      throw new IllegalArgumentException("Numero di valori non corrisponde al numero colonne.");

    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
    tbrig.getDefaultCell().setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

    if(borderTop > 0)
      tbrig.getDefaultCell().setBorderWidthTop(borderTop);
    if(borderBottom > 0)
      tbrig.getDefaultCell().setBorderWidthBottom(borderBottom);

    tbrig.getDefaultCell().setBorderWidthLeft(0.8f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.addCell(new Phrase(SU.okStr(values[0]), font));

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    for(int i = 1; i < (values.length - 1); i++)
    {
      tbrig.getDefaultCell().setHorizontalAlignment(arCol.get(i).align);
      tbrig.addCell(new Phrase(SU.okStr(values[i]), font));
    }

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.8f);
    tbrig.addCell(new Phrase(SU.okStr(values[values.length - 1]), font));

    return tbrig;
  }

  /**
   * Stampa la riga di chiusura dell'area righe.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  protected PdfPTable stampaChiusura(PdfPTable tbrig, Font font) throws Exception
  {
    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

    tbrig.getDefaultCell().setBorderWidthLeft(0.8f);
    tbrig.getDefaultCell().setBorderWidthBottom(0.8f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    Phrase ph1 = new Phrase("", font);
    tbrig.addCell(ph1);

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    for(int i = 1; i < (arCol.size() - 1); i++)
    {
      tbrig.addCell(ph1);
    }

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.8f);
    tbrig.addCell(ph1);

    irec++;
    return tbrig;
  }

  /**
   * Produce la stampa generando l'intera tabella.
   * @param tbl implementazione del produttore di dati per la tabella
   * @return tabella completa compilata
   * @throws Exception
   */
  public PdfPTable stampa(TableBuilderListner tbl) throws Exception
  {
    PdfPTable tbrig = getTabella();
    int numDataRig = tbl.getNumDataRow();
    int numEmptyRig = tbl.getNumEmptyRow();

    if(tbl.haveHeader())
      stampaInstestazione(tbrig, tbl.getHeaderFont());

    for(int i=0 ; i<numDataRig ; i++)
    {
      Object[] rowData = tbl.getRowData(i, arCol.size());
      stampa(tbrig, tbl.getRowFont(i), rowData, tbl.getBorderTop(i), tbl.getBorderBottom(i));
    }

    for(int i=0 ; i<numEmptyRig; i++)
    {
      stampaChiusura(tbrig, tbl.getRowFont(numDataRig + i));
    }

    if(tbl.haveFooter())
      stampaChiusura(tbrig, tbl.getFooterFont());

    return tbrig;
  }
}
