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
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.util.ArrayList;
import org.sirio2.utils.SU;

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
public class PdfTableHelperSimple implements PdfTableHelper
{
  protected ArrayList<ColumnInfo> arCol = new ArrayList<ColumnInfo>();
  protected int irec = 0;
  protected int arrayWidthCol[] = null;

  @Override
  public void clear()
  {
    arCol.clear();
  }

  /**
   * Aggiunge una nuova colonna.
   * ATTENZIONE: la somma delle width deve essere 100.
   * @param caption nome della colonna da inserire nell'header
   * @param width dimensioni in percentuale
   * @param align allineamento (Es. PdfPTable.ALIGN_LEFT)
   */
  @Override
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
  @Override
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
  @Override
  public PdfPTable getTabella()
     throws Exception
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
  @Override
  public PdfPTable stampaInstestazione(Font font)
     throws Exception
  {
    PdfPTable tbrig = getTabella();

    tbrig.getDefaultCell().setBorder(0);
    tbrig.getDefaultCell().setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
    tbrig.getDefaultCell().setBorderWidthBottom(1.0f);

    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell(new Phrase(arCol.get(0).caption, font));

    tbrig.getDefaultCell().setColspan(1);
    for(int i = 1; i < (arCol.size() - 1); i++)
    {
      tbrig.getDefaultCell().setHorizontalAlignment(arCol.get(i).align);
      tbrig.addCell(new Phrase(arCol.get(i).caption, font));
    }

    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell(new Phrase(arCol.get(arCol.size() - 1).caption, font));

    return tbrig;
  }

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
  @Override
  public PdfPTable stampaVuota(Font font, String dummyValue)
     throws Exception
  {
    Object[] values = new Object[arCol.size()];
    values[0] = dummyValue;
    return stampa(font, values);
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
  @Override
  public PdfPTable stampaVuota(Font font)
     throws Exception
  {
    return stampaVuota(font, "--");
  }

  /**
   * Come stampa() ma con un numero variabile di argomenti.
   * @param font
   * @param values
   * @return
   * @throws Exception
   */
  public PdfPTable stampaVar(Font font, Object... values)
     throws Exception
  {
    return stampa(font, values);
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

    tbrig.getDefaultCell().setColspan(1);
    addCell(tbrig, values[0], font);

    if(values.length > 2)
    {
      tbrig.getDefaultCell().setColspan(1);
      for(int i = 1; i < (values.length - 1); i++)
      {
        tbrig.getDefaultCell().setHorizontalAlignment(arCol.get(i).align);
        addCell(tbrig, values[i], font);
      }
    }

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

    tbrig.getDefaultCell().setBorderWidthBottom(1.0f);
    tbrig.getDefaultCell().setColspan(1);
    for(int i = 1; i < arCol.size(); i++)
    {
      tbrig.addCell("");
    }

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

    tbrig.getDefaultCell().setBorderWidthTop(0.0f);
    tbrig.getDefaultCell().setBorderWidthBottom(0.1f);
    tbrig.getDefaultCell().setColspan(1);
    tbrig.addCell("");

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
    addCell(tbrig, value, font);
    return tbrig;
  }

  public void addCell(PdfPTable tbrig, Object toadd, Font font)
  {
    if(toadd == null)
    {
      tbrig.addCell(new Phrase("", font));
      return;
    }

    if(toadd instanceof PdfPTable)
    {
      tbrig.addCell((PdfPTable) toadd);
    }
    else if(toadd instanceof Image)
    {
      tbrig.addCell((Image) toadd);
    }
    else if(toadd instanceof Phrase)
    {
      tbrig.addCell((Phrase) toadd);
    }
    else if(toadd instanceof PdfPCell)
    {
      tbrig.addCell((PdfPCell) toadd);
    }
    else
      tbrig.addCell(new Phrase(SU.okStr(toadd), font));
  }
}
