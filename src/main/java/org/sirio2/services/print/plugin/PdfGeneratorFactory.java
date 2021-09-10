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
package org.sirio2.services.print.plugin;

import org.sirio2.utils.SU;
import java.io.File;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.commonlib.utils.ClassOper;
import org.sirio2.services.CoreServiceException;

/**
 * Costruttore dei generatori di pdf.
 * @author Nicola De Nisco
 */
public class PdfGeneratorFactory
{
  private String[] vPaths = null;
  private String myPackage = null;
  private Configuration cfg = null;
  private File dirTmp = null;
  private static PdfGeneratorFactory theInstance = new PdfGeneratorFactory();

  private PdfGeneratorFactory()
  {
  }

  public static PdfGeneratorFactory getInstance()
  {
    return theInstance;
  }

  public void configure(Configuration cfg, File dirTmp)
  {
    this.cfg = cfg;
    this.dirTmp = dirTmp;
    vPaths = cfg.getStringArray("classpath");

    myPackage = ClassOper.getClassPackage(this.getClass());
  }

  public PdfGenPlugin build(String nome)
     throws Exception
  {
    if(!SU.isOkStr(nome))
      throw new CoreServiceException("Nome del plugin non valido.");

    String className = cfg.getString("plugin." + nome);
    if(className == null)
      throw new CoreServiceException("Plugin " + nome + " sconosciuto.");

    PdfGenPlugin sp = loadClass(className);
    sp.configure(nome, cfg.subset("plugin." + nome), dirTmp);
    return sp;
  }

  private PdfGenPlugin loadClass(String className)
     throws Exception
  {
    Class c = ClassOper.loadClass(className, myPackage, vPaths);

    if(c == null)
      throw new CoreServiceException("Classe " + className + " non trovata o non istanziabile.");

    return (PdfGenPlugin) c.newInstance();
  }

  public String[] getBasePaths()
  {
    return vPaths;
  }

  public void setBasePaths(String[] vPaths)
  {
    this.vPaths = vPaths;
  }

  public void addBasePath(String basePath)
  {
    vPaths = (String[]) ArrayUtils.add(vPaths, basePath);
  }
}
