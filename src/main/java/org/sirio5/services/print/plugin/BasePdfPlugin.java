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
package org.sirio5.services.print.plugin;

import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import java.io.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.turbine.services.TurbineServices;
import org.sirio5.services.CoreServiceException;
import org.sirio5.services.formatter.DataFormatter;
import org.sirio5.services.formatter.NumFormatter;
import org.sirio5.services.formatter.ValutaFormatter;
import org.sirio5.services.print.AbstractReportParametersInfo;
import org.sirio5.services.print.PdfPrint;
import org.sirio5.services.print.PrintContext;

/**
 * Funzioni di utlitià per i plugin.
 *
 * @author Nicola De Nisco
 */
abstract public class BasePdfPlugin implements PdfGenPlugin
{
  protected File dirTmp = null;
  protected DataFormatter df = null;
  protected ValutaFormatter vf = null;
  protected NumFormatter nf = null;
  protected PdfPrint print = null;

  @Override
  public boolean isInitialized()
  {
    return print != null;
  }

  @Override
  public void configure(String pluginName, Configuration cfg)
     throws Exception
  {
    df = (DataFormatter) (TurbineServices.getInstance().getService(DataFormatter.SERVICE_NAME));
    vf = (ValutaFormatter) (TurbineServices.getInstance().getService(ValutaFormatter.SERVICE_NAME));
    nf = (NumFormatter) (TurbineServices.getInstance().getService(NumFormatter.SERVICE_NAME));
    print = (PdfPrint) (TurbineServices.getInstance().getService(PdfPrint.SERVICE_NAME));
    dirTmp = print.getWorkTmpFile("print-plugin-" + pluginName);
  }

  @Override
  public void getParameters(int idUser, PrintContext context)
     throws Exception
  {
    String reportName = context.getAsString(PrintContext.REPORT_NAME_KEY);
    String reportInfo = context.getAsString(PrintContext.REPORT_INFO_KEY);
    AbstractReportParametersInfo rpb = (AbstractReportParametersInfo) context.get(PrintContext.PBEAN_KEY);
    rpb.initGeneric(idUser, reportName, reportInfo, context);
  }

  protected File getTmpFile()
     throws Exception
  {
    if(!dirTmp.isDirectory())
      if(!dirTmp.mkdirs())
        throw new IOException("Impossibile creare la directory " + dirTmp.getAbsolutePath());

    File ftmp = File.createTempFile("pdfplg", ".tmp", dirTmp);
    ftmp.deleteOnExit();
    return ftmp;
  }

  public void die(String cause)
     throws Exception
  {
    throw new CoreServiceException(cause);
  }

  protected String fmtDim(double dim)
     throws Exception
  {
    return nf.format(dim, 1, 2);
  }

  protected String fmtQta(double qta)
     throws Exception
  {
    return nf.format(qta, 0, 2);
  }

  protected String fmtSconto(double sconto)
     throws Exception
  {
    return nf.format(sconto, 0, 0);
  }

  protected String fmtValuta(double importo)
     throws Exception
  {
    return vf.fmtValuta(importo);
  }

  protected PdfPCell createCell(String text, Font font)
  {
    PdfPCell rv = new PdfPCell(new Phrase(text, font));
    return rv;
  }

  protected PdfPCell createCell(String text, Font font, float bleft, float bright, float btop, float bbottom)
  {
    PdfPCell rv = new PdfPCell(new Phrase(text, font));
    rv.setBorderWidthLeft(bleft);
    rv.setBorderWidthRight(bright);
    rv.setBorderWidthTop(btop);
    rv.setBorderWidthBottom(bbottom);
    return rv;
  }

  protected PdfPCell createCell(String text, Font font,
     float bleft, float bright, float btop, float bbottom,
     int hspan)
  {
    PdfPCell rv = new PdfPCell(new Phrase(text, font));
    rv.setBorderWidthLeft(bleft);
    rv.setBorderWidthRight(bright);
    rv.setBorderWidthTop(btop);
    rv.setBorderWidthBottom(bbottom);
    rv.setColspan(hspan);
    return rv;
  }

  protected PdfPCell createCell(String text, Font font,
     float bleft, float bright, float btop, float bbottom,
     int hspan, int halign)
  {
    PdfPCell rv = new PdfPCell(new Phrase(text, font));
    rv.setBorderWidthLeft(bleft);
    rv.setBorderWidthRight(bright);
    rv.setBorderWidthTop(btop);
    rv.setBorderWidthBottom(bbottom);
    rv.setColspan(hspan);
    rv.setHorizontalAlignment(halign);
    return rv;
  }

  protected PdfPCell createCell(String text, Font font,
     float bleft, float bright, float btop, float bbottom,
     int hspan, int halign, int valign)
  {
    PdfPCell rv = new PdfPCell(new Phrase(text, font));
    rv.setBorderWidthLeft(bleft);
    rv.setBorderWidthRight(bright);
    rv.setBorderWidthTop(btop);
    rv.setBorderWidthBottom(bbottom);
    rv.setColspan(hspan);
    rv.setHorizontalAlignment(halign);
    rv.setVerticalAlignment(valign);
    return rv;
  }
}
