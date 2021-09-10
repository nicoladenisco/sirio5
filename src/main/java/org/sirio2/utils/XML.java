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
package org.sirio2.utils;

import java.io.PrintWriter;
import java.io.StringReader;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Utilità per manipolazione XML.
 *
 * @author Nicola De Nisco
 */
public class XML
{
  /**
   * Formatta un documento XML.
   * Effettua la formattazione nel formato canonico e leggibile
   * di un documento XML con newline e identazione.
   * @param xmlContent documento XML da formattare
   * @return xml formattato
   * @throws Exception
   */
  public static String formatXML(String xmlContent)
     throws Exception
  {
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(new StringReader(xmlContent));

    XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
    return xout.outputString(doc);
  }

  /**
   * Formatta un documento XML.
   * Effettua la formattazione nel formato canonico e leggibile
   * di un documento XML con newline e identazione.
   * @param xmlContent documento XML da formattare
   * @param out dove inviare l'output
   * @throws Exception
   */
  public static void formatXML(String xmlContent, PrintWriter out)
     throws Exception
  {
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(new StringReader(xmlContent));

    XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
    xout.output(doc, out);
  }
}
