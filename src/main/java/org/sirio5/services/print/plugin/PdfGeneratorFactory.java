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

import org.apache.commons.configuration2.Configuration;
import org.sirio5.utils.factory.CoreAbstractPoolPluginFactory;

/**
 * Costruttore dei generatori di pdf.
 * @author Nicola De Nisco
 */
public class PdfGeneratorFactory extends CoreAbstractPoolPluginFactory<PdfGenPlugin>
{
  private static final PdfGeneratorFactory theInstance = new PdfGeneratorFactory();

  private PdfGeneratorFactory()
  {
  }

  public static PdfGeneratorFactory getInstance()
  {
    return theInstance;
  }

  public void configure(Configuration cfg)
  {
    super.configure(cfg, "plugin");
  }
}
