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

import org.sirio5.services.print.PdfPrint;
import org.sirio5.services.print.AbstractReportParametersInfo;
import java.io.File;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;

/**
 * Interfaccia di un plugin per la generazione di pdf.
 *
 * @author Nicola De Nisco
 */
public interface PdfGenPlugin
{
  /**
   * Configurazione e inizializzazione del plugin.
   * Il plugin puo' usare parametri dal servizio PdfPrint.
   * @param pluginName
   * @param cfg
   * @param dirTmp
   * @throws Exception
   */
  public void configure(String pluginName, Configuration cfg, File dirTmp)
     throws Exception;

  /**
   * Restituisce un bean con i parametri per la stampa richiesta.
   * @param idUser the value of idUser
   * @param reportName the value of reportName
   * @param reportInfo the value of reportInfo
   * @param params parametri accessori del plugin
   * @param info report info da popolare
   * @throws Exception
   */
  public void getParameters(int idUser, String reportName, String reportInfo, Map params, AbstractReportParametersInfo info)
     throws Exception;

  /**
   * Funzione per la generazione del pdf.
   * @param job
   * @param idUser
   * @param reportName
   * @param reportInfo
   * @param params
   * @param pbean
   * @param pdfToGen
   * @throws Exception
   */
  public void buildPdf(PdfPrint.JobInfo job, int idUser,
     String reportName, String reportInfo, Map params,
     AbstractReportParametersInfo pbean, File pdfToGen)
     throws Exception;
}
