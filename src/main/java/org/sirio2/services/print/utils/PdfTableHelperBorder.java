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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * Classe di supporto per la visualizzazione di elementi tabellati
 * all'interno di una stampa PDF.
 * La tabella viene generata una riga per volta per essere inserita
 * nel documento (metodi stampa...()) e può quindi ricevere dei salti
 * pagina. Purtroppo non è possibile inserire dei bordi all'interno
 * delle singole righe.
 *
 * @author Nicola De Nisco
 */
public class PdfTableHelperBorder extends PdfTableHelperSimple
{
  /**
   * Stampa una riga con le intestazioni di tabella.
   * @param font
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  @Override
  public PdfPTable stampaInstestazione(Font font)
     throws Exception
  {
    PdfPTable tbrig = getTabella();

    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
    tbrig.getDefaultCell().setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setBorderWidthTop(1.0f);
    tbrig.getDefaultCell().setColspan(1);
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
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell(new Phrase(arCol.get(arCol.size() - 1).caption, font));

    return tbrig;
  }

  /**
   * Stampa una riga di valori.
   * Se i valori sono inferiori alla definizione di tabella
   * l'ultima colonna riceve lo spazio delle colonne mancanti.
   * @param font
   * @param values array con i valori
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  @Override
  public PdfPTable stampa(Font font, Object[] values)
     throws Exception
  {
    if(values.length == 1)
      return stampaCellaUnica(font, values[0]);

    if(values.length < 2)
      throw new IllegalArgumentException("Numero di valori non ammesso (minimo 2).");

    PdfPTable tbrig = getTabella();
    int span = arCol.size() - values.length + 1;

    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
    tbrig.getDefaultCell().setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    addCell(tbrig, values[0], font);

    if(values.length > 2)
    {
      tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
      tbrig.getDefaultCell().setBorderWidthRight(0.1f);
      tbrig.getDefaultCell().setColspan(1);
      for(int i = 1; i < (values.length - 1); i++)
      {
        tbrig.getDefaultCell().setHorizontalAlignment(arCol.get(i).align);
        addCell(tbrig, values[i], font);
      }
    }

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    tbrig.getDefaultCell().setColspan(span);
    addCell(tbrig, values[values.length - 1], font);

    return tbrig;
  }

  /**
   * Stampa la riga di chiusura dell'area righe.
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  @Override
  public PdfPTable stampaChiusura()
     throws Exception
  {
    PdfPTable tbrig = getTabella();
    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setBackgroundColor(BaseColor.WHITE);

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthBottom(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell("");

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    for(int i = 1; i < (arCol.size() - 1); i++)
    {
      tbrig.addCell("");
    }

    tbrig.getDefaultCell().setBorderWidthLeft(0.1f);
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell("");

    irec++;
    return tbrig;
  }

  @Override
  public PdfPTable hr()
     throws Exception
  {
    PdfPTable tbrig = new PdfPTable(1);
    tbrig.setHeaderRows(0);
    tbrig.setWidths(htesFull);
    tbrig.setWidthPercentage(100);
    tbrig.getDefaultCell().setColspan(1);

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    tbrig.getDefaultCell().setBorderWidthTop(0.0f);
    tbrig.getDefaultCell().setBorderWidthBottom(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell("");

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    tbrig.getDefaultCell().setBorderWidthTop(0.1f);
    tbrig.getDefaultCell().setBorderWidthBottom(0.0f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell("");

    return tbrig;
  }

  /**
   * Stampa una unica cella che copre tutto lo spazio della tabella.
   * @param font font da utilizzare
   * @param value valore da stampare
   * @return un oggeto PdfPTable da inserire in un documento PDF.
   * @throws Exception
   */
  @Override
  public PdfPTable stampaCellaUnica(Font font, Object value)
     throws Exception
  {
    PdfPTable tbrig = new PdfPTable(1);
    tbrig.setHeaderRows(0);
    tbrig.setWidths(htesFull);
    tbrig.setWidthPercentage(100);
    tbrig.getDefaultCell().setColspan(1);

    tbrig.getDefaultCell().setBorderWidthLeft(1.0f);
    tbrig.getDefaultCell().setBorderWidthRight(1.0f);
    addCell(tbrig, value, font);

    return tbrig;
  }
}
