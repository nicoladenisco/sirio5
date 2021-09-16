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
package org.sirio5.utils.pdf;

import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import org.sirio5.services.localization.INT;

/**
 * Renderizzatore di PDF con immagini all'interno.
 * Consente di creare un PDF con pagine contenenti immagini
 * secondo una disposizione variabile in base al formato.
 * Se si vogliono ottenere tutte le pagine uguali
 * è sufficiente utilizzare la funzione renderAllImages()
 * altrimenti per ogni pagina si possono chiamare le
 * funzioni setPageData() e renderPaginaImmagini() con
 * parametri differenti.
 *
 * @author Nicola De Nisco
 */
public class DocumentoImmagini extends Documento
{
  public static class FormatInfo
  {
    public int nImg1 = 0, nImg2 = 0, nCol = 0, nRow = 0;
    public float dx = 0f, dy = 0f;
    public float hRef = 1f;
  }
  //
  // costanti tipo formato
  public static final int FMT_1X1 = 1;
  public static final int FMT_1X2 = 2;
  public static final int FMT_2X2 = 3;
  public static final int FMT_2X3 = 4;
  public static final int FMT_2X4 = 5;
  public static final int FMT_3X3 = 6;
  public static final int FMT_3X4 = 7;
  public static final int FMT_3X5 = 8;
  public static final int FMT_4X4 = 9;
  public static final int FMT_4X5 = 10;
  public static final int FMT_4X6 = 11;
  public static final int FMT_5X6 = 12;
  public static final int FMT_5X7 = 13;
  public static final int FMT_LAST_VALID = 13;
  //
  // conversione da stringa a formato
  public static final Map<String, Integer> strToFormat = Collections.unmodifiableMap(createStringToFormatMap());
  //
  // nomi dei formati supportati
  public static final List<String> fmtNamesList = Collections.unmodifiableList(createDescrFormati());
  //
  // numero massimo di immagini per pagina
  public static final int MAX_IMAGES_PER_PAGE = 5 * 7 * 2;
  //
  // costanti distribuzione colonne
  public static final int[] cols_1 = new int[]
  {
    100
  };
  public static final int[] cols_2 = new int[]
  {
    50, 50
  };
  public static final int[] cols_3 = new int[]
  {
    33, 34, 33
  };
  public static final int[] cols_4 = new int[]
  {
    25, 25, 25, 25
  };
  public static final int[] cols_5 = new int[]
  {
    20, 20, 20, 20, 20
  };
  //
  // formato pagina corrente
  public int formato = FMT_2X4;
  // numero di colonne immagini
  public int NUM_COLS = 2;
  // numero di righe sulla pagina di sinistra
  public int NUM_ROWS = 4;
  // numero di immagini sulla pagina di sinistra
  public int MAX_IMAGES_LEFT = 8;
  // numero massimo di immagini sulla prima pagina
  public int MAX_IMAGES_FIRST_PAGE = 10;
  // numero massimo di immagini sulla pagine diverse dalla prima
  public int MAX_IMAGES_OTHER_PAGE = 16;
  //
  // distribuzione colonne
  protected int[] cols = cols_2;
  //
  protected float cellImgWidth, cellImgHeigth, imgWidth, imgHeigth;
  protected int numImmaginiPrincipali = 0;
  protected ArrayList<File> arFiles = new ArrayList<File>();
  protected float textTablePaddingLeft = 10;
  protected float textTablePaddingRight = 10;
  //
  protected ImagePool impool = new ImagePool();

  public DocumentoImmagini()
     throws Exception
  {
  }

  /**
   * Imposta il pool di immagini da renderizzare.
   * Il pool viene cancellato e impostato al contenuto della collezione
   * @param toAdd collezione di file da inserire nel pool
   */
  public void setImagePool(Collection<File> toAdd)
  {
    impool.clear();
    impool.addAll(toAdd);
  }

  /**
   * Pulisce il pool di immagini.
   */
  public void clearPool()
  {
    impool.clear();
  }

  /**
   * Aggiunge una immagine al pool immagini.
   * @param toAdd immagine da aggiungere
   */
  public void addImage(File toAdd)
  {
    impool.add(toAdd);
  }

  /**
   * Renderizzazione di tutte le immagini del pool.
   * Se si vuole renderizzare tutte le immagini in pagine
   * uguali si può usare questa funzione che renderizza
   * in modo costante tutto il pool.
   * @param formato formato pagina (vedi costanti FMT_...)
   * @param autoFormato se vero abilita trattamento speciale pagina con una immagine
   * @param forceBW se vero abilita la conversione forzata delle immagini in scala di grigio
   * @param ruotaAuto se vero ruota automaticamente l'immagine nella cella se la dimensione lo permette
   * @param background se diverso da null imposta il background della pagina
   * @throws Exception
   */
  public void renderAllImages(int formato, boolean autoFormato, boolean forceBW, boolean ruotaAuto, Color background)
     throws Exception
  {
    int pageNo = 0;
    FormatInfo fi = getFormatInfo(formato);
    if(fi == null)
      return;

    int[] immagini = new int[fi.nImg1];

    for(int i = 0; i < impool.size(); i += fi.nImg1)
    {
      Arrays.fill(immagini, -1);
      for(int j = 0; j < immagini.length && (i + j) < impool.size(); j++)
        immagini[j] = i + j;

      setPageData(pageNo++, formato, immagini);
      renderPage(autoFormato, forceBW, ruotaAuto, background);
    }
  }

  /**
   * Imposta dati per la renderizzazione di una pagina immagini.
   *
   * @param pageNo numero della pagina
   * @param formato formato pagina (vedi costanti FMT_...)
   * @param immagini indice delle immagini da inserire riferite al pool generale
   * @throws Exception
   */
  public void setPageData(int pageNo, int formato, int[] immagini)
     throws Exception
  {
    setFormato(formato);

    cellImgWidth = (float) Math.floor(getRelativeWidth(1f / NUM_COLS)) - 1;
    cellImgHeigth = (float) Math.floor(getRelativeHeight(1f / NUM_ROWS)) - 1;
    imgWidth = cellImgWidth * 0.98f;
    imgHeigth = cellImgHeigth * 0.98f;

    arFiles.clear();
    if(immagini != null)
    {
      int maxImgPage = pageNo == 0 ? MAX_IMAGES_FIRST_PAGE : MAX_IMAGES_OTHER_PAGE;
      for(int i = 0; i < immagini.length && i < maxImgPage; i++)
      {
        int j = immagini[i];
        if(j >= 0 && j < impool.size())
          arFiles.add(impool.get(j));
      }
    }
  }

  /**
   * Disegna la pagina corrente.
   *
   * @throws Exception
   */
  public void renderPage(boolean autoFormato, boolean forceBW, boolean ruotaAuto, Color background)
     throws Exception
  {
    PdfPCell pageCell = renderPaginaImmagini(autoFormato, forceBW, ruotaAuto, background);

    if(pageCell != null)
    {
      PdfPTable tblContent = new PdfPTable(1);
      tblContent.setWidthPercentage(100); // percentage
      pageCell.setBorderWidth(0);
      pageCell.setHorizontalAlignment(Element.ALIGN_CENTER);

      tblContent.addCell(pageCell);
      document.add(tblContent);
      document.newPage();
    }
  }

  /**
   * Renderizza una pagina immagini.
   * Costruisce una cella da inserire in una pagina con tutte le immagini sendo il formato impostato.
   * @param autoFormato se vero abilita trattamento speciale pagina con una immagine
   * @param forceBW se vero abilita la conversione forzata delle immagini in scala di grigio
   * @param ruotaAuto se vero ruota automaticamente l'immagine nella cella se la dimensione lo permette
   * @param background se diverso da null imposta il background della pagina
   * @return la cella da inserire nel pdf.
   * @throws Exception
   */
  protected PdfPCell renderPaginaImmagini(boolean autoFormato, boolean forceBW, boolean ruotaAuto, Color background)
     throws Exception
  {
    // nessuna immagine
    if(arFiles.isEmpty())
    {
      return new PdfPCell(new Phrase(INT.I("Nessuna immagine."), fontH16B_N));
    }

    PdfPCell cellImages = null;

    // una sola immagine
    if(arFiles.size() == 1 && autoFormato)
      cellImages = createImageCellSingle(arFiles.get(0), forceBW, ruotaAuto);
    else
    {
      PdfPTable tblImages = createImageTable(0, MAX_IMAGES_LEFT, forceBW, ruotaAuto);
      cellImages = new PdfPCell(tblImages);
    }

    if(background != null && !background.equals(Color.WHITE))
      cellImages.setBackgroundColor(cvtColor(background));

    return cellImages;
  }

  /**
   * Carica una griglia di immagini per una mezza pagina.
   *
   * @param startPage indice di partenza delle immagini
   * @param numInPage numero immagini da inserire nella pagina
   * @param forceBW flag per forzare la conversione in bianco e nero delle immagni
   * @param autoRuota flag per la rotazione automatica in caso di lun maggiore alt
   * @return la tabella immagini creata
   * @throws Exception
   */
  protected PdfPTable createImageTable(int startPage, int numInPage, boolean forceBW, boolean autoRuota)
     throws Exception
  {
    // fino a 8 immagini disposte su due colonne
    PdfPTable tblImages = new PdfPTable(NUM_COLS);
    tblImages.setWidths(cols);
    tblImages.setWidthPercentage(100); // percentage
    tblImages.getDefaultCell().setBorderWidth(0);
    tblImages.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
    tblImages.getDefaultCell().setFixedHeight(cellImgHeigth);

    for(int i = 0; i < numInPage; i++)
    {
      int img = i + startPage;
      if(img < arFiles.size())
        tblImages.addCell(createImageCell(arFiles.get(img), forceBW, autoRuota));
      else
        tblImages.addCell("");
    }

    return tblImages;
  }

  /**
   * Carica una immagine diagnostica e restituisce la
   * cella appositamente riempita con l'immagine scalata
   * correttamente.
   *
   * @param fileImage file immagine da inserire nella cella
   * @param forceBW flag per forzare la conversione in bianco e nero delle immagni
   * @param autoRuota flag per la rotazione automatica in caso di lun maggiore alt
   * @return la cella PDF creata
   * @throws Exception
   */
  protected PdfPCell createImageCell(File fileImage, boolean forceBW, boolean autoRuota)
     throws Exception
  {
    Image img = readDiskImage(fileImage, forceBW);

    // nel formato 1x1 ruota di 90 gradi l'immagine
    // se questa ha il lato lungo per X
    if(autoRuota && formato == FMT_1X1)
      if(img.getWidth() > img.getHeight())
        img.setRotationDegrees(90f);

    img.scaleToFit(imgWidth, imgHeigth);

    PdfPCell c = new PdfPCell(img, false);
    c.setFixedHeight(cellImgHeigth);
    c.setBorderWidth(0);
    c.setHorizontalAlignment(Element.ALIGN_CENTER);
    c.setVerticalAlignment(Element.ALIGN_MIDDLE);

    return c;
  }

  /**
   * Carica una immagine diagnostica e restituisce la
   * cella appositamente riempita con l'immagine scalata
   * correttamente. La cella restituita occupa l'intera
   * pagina ed è automaticamente ruotata per occupare la
   * maggiore area possibile sulla pagina.
   *
   * @param fileImage file immagine da inserire nella cella
   * @param forceBW flag per forzare la conversione in bianco e nero delle immagni
   * @param autoRuota flag per la rotazione automatica in caso di lun maggiore alt
   * @return la cella PDF creata
   * @throws java.lang.Exception
   */
  protected PdfPCell createImageCellSingle(File fileImage, boolean forceBW, boolean autoRuota)
     throws Exception
  {
    cellImgWidth = getRelativeWidth(1f / 1 /* NUM_COLS */) * 0.9f;
    cellImgHeigth = getRelativeHeight(1f / 1 /* NUM_ROWS */) * 0.9f;
    imgWidth = cellImgWidth * 0.98f;
    imgHeigth = cellImgHeigth * 0.98f;

    Image img = readDiskImage(fileImage, forceBW);

    if(autoRuota && img.getWidth() > img.getHeight())
      img.setRotationDegrees(90f);

    img.scaleToFit(imgWidth, imgHeigth);

    PdfPCell c = new PdfPCell(img, false);
    c.setFixedHeight(cellImgHeigth);
    c.setBorderWidth(0);
    c.setHorizontalAlignment(Element.ALIGN_CENTER);
    c.setVerticalAlignment(Element.ALIGN_MIDDLE);

    return c;
  }

  /**
   * Legge un file immagine da disco.
   *
   * Il file viene letto e convertito in un oggetto immagine.
   * Una eventuale forzatura a monocromatico può essere applicata.
   * @param fileImage file da leggere
   * @param forceBW flag per forzatura in bianco e nero
   * @return oggetto immagine da inserire nel PDF
   * @throws Exception
   */
  protected Image readDiskImage(File fileImage, boolean forceBW)
     throws Exception
  {
    if(forceBW)
    {
      // legge l'immagine e la converte in scala di grigi
      BufferedImage tmpImg = ImageIO.read(fileImage);
      ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      BufferedImage tmpGray = op.filter(tmpImg, null);
      return Image.getInstance(tmpGray, Color.white, false);
    }

    return Image.getInstance(fileImage.getAbsolutePath());
  }

  /**
   * Ritorna informazioni dettagliate per il formato specificato.
   * @param formato formato della pagina (vedi costanti FMT_...)
   * @return informazioni o null se formato non previsto
   */
  public static FormatInfo getFormatInfo(int formato)
  {
    FormatInfo rv = new FormatInfo();

    switch(formato)
    {
      case FMT_1X1:
        rv.nImg1 = 1;
        rv.nImg2 = 0;
        rv.dx = 1f;
        rv.dy = 1f;
        rv.nCol = 1;
        rv.nRow = 1;
        rv.hRef = 1f;
        break;
      case FMT_1X2:
        rv.nImg1 = 2;
        rv.nImg2 = 1;
        rv.dx = 1f;
        rv.dy = 0.5f;
        rv.nCol = 1;
        rv.nRow = 2;
        rv.hRef = 0.5f;
        break;
      case FMT_2X2:
        rv.nImg1 = 4;
        rv.nImg2 = 2;
        rv.dx = 0.5f;
        rv.dy = 0.5f;
        rv.nCol = 2;
        rv.nRow = 2;
        rv.hRef = 0.5f;
        break;
      case FMT_2X3:
        rv.nImg1 = 6;
        rv.nImg2 = 2;
        rv.dx = 0.5f;
        rv.dy = 0.3333f;
        rv.nCol = 2;
        rv.nRow = 3;
        rv.hRef = 0.6666f;
        break;
      case FMT_2X4:
        rv.nImg1 = 8;
        rv.nImg2 = 2;
        rv.dx = 0.5f;
        rv.dy = 0.25f;
        rv.nCol = 2;
        rv.nRow = 4;
        rv.hRef = 0.75f;
        break;
      case FMT_3X3:
        rv.nImg1 = 9;
        rv.nImg2 = 3;
        rv.dx = 0.3333f;
        rv.dy = 0.3333f;
        rv.nCol = 3;
        rv.nRow = 3;
        rv.hRef = 0.6666f;
        break;
      case FMT_3X4:
        rv.nImg1 = 12;
        rv.nImg2 = 3;
        rv.dx = 0.3333f;
        rv.dy = 0.25f;
        rv.nCol = 3;
        rv.nRow = 4;
        rv.hRef = 0.75f;
        break;
      case FMT_3X5:
        rv.nImg1 = 15;
        rv.nImg2 = 3;
        rv.dx = 0.3333f;
        rv.dy = 0.20f;
        rv.nCol = 3;
        rv.nRow = 5;
        rv.hRef = 0.80f;
        break;
      case FMT_4X4:
        rv.nImg1 = 16;
        rv.nImg2 = 4;
        rv.dx = 0.25f;
        rv.dy = 0.25f;
        rv.nCol = 4;
        rv.nRow = 4;
        rv.hRef = 0.75f;
        break;
      case FMT_4X5:
        rv.nImg1 = 20;
        rv.nImg2 = 4;
        rv.dx = 0.25f;
        rv.dy = 0.20f;
        rv.nCol = 4;
        rv.nRow = 5;
        rv.hRef = 0.80f;
        break;
      case FMT_4X6:
        rv.nImg1 = 24;
        rv.nImg2 = 4;
        rv.dx = 0.25f;
        rv.dy = 0.1666f;
        rv.nCol = 4;
        rv.nRow = 6;
        rv.hRef = 0.8333f;
        break;
      case FMT_5X6:
        rv.nImg1 = 30;
        rv.nImg2 = 5;
        rv.dx = 0.20f;
        rv.dy = 0.1666f;
        rv.nCol = 5;
        rv.nRow = 6;
        rv.hRef = 0.8333f;
        break;
      case FMT_5X7:
        rv.nImg1 = 35;
        rv.nImg2 = 5;
        rv.dx = 0.20f;
        rv.dy = 0.1428f;
        rv.nCol = 5;
        rv.nRow = 7;
        rv.hRef = 0.8571f;
        break;
      default:
        return null;
    }

    return rv;
  }

  /**
   * Creazione di una mappa di stringhe descrittive dei formati.
   * @return mappa descrizione/formato (vedi costanti FMT_...)
   */
  private static Map<String, Integer> createStringToFormatMap()
  {
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    map.put("1,1", FMT_1X1);
    map.put("1,2", FMT_1X2);
    //map.put("2,1", FMT_2X1);
    map.put("2,2", FMT_2X2);
    map.put("2,3", FMT_2X3);
    map.put("2,4", FMT_2X4);
    map.put("3,3", FMT_3X3);
    map.put("3,4", FMT_3X4);
    map.put("3,5", FMT_3X5);
    map.put("4,4", FMT_4X4);
    map.put("4,5", FMT_4X5);
    map.put("4,6", FMT_4X6);
    map.put("5,6", FMT_5X6);
    map.put("5,7", FMT_5X7);
    return map;
  }

  /**
   * Ritorna una lista dei formati possibili.
   * @return lista descrittiva dei formati
   */
  public static List<String> createDescrFormati()
  {
    ArrayList<String> rv = new ArrayList<String>();
    Set<String> formati = strToFormat.keySet();
    for(String s : formati)
      rv.add(s.replace(',', 'x'));

    Collections.sort(rv);
    return rv;
  }

  /**
   * Imposta il formato corrente della pagina.
   * @param formato formato della pagina (vedi costanti FMT_...)
   * @throws Exception in caso di formato non consentito
   */
  public void setFormato(int formato)
     throws Exception
  {
    FormatInfo fi = getFormatInfo(formato);

    if(fi == null)
      throw new IllegalArgumentException(INT.I("Valore non consentito per tipo formato."));

    MAX_IMAGES_LEFT = fi.nImg1;
    MAX_IMAGES_FIRST_PAGE = fi.nImg1 + fi.nImg2;
    NUM_COLS = fi.nCol;
    NUM_ROWS = fi.nRow;

    switch(fi.nCol)
    {
      case 1:
        cols = cols_1;
        break;
      case 2:
        cols = cols_2;
        break;
      case 3:
        cols = cols_3;
        break;
      case 4:
        cols = cols_4;
        break;
      case 5:
        cols = cols_5;
        break;
      default:
        throw new IllegalArgumentException(INT.I("Valore non consentito per tipo formato."));
    }

    this.formato = formato;
    MAX_IMAGES_OTHER_PAGE = MAX_IMAGES_LEFT * 2;
  }
}
