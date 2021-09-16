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
package org.sirio5.beans.menu;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.turbine.util.RunData;
import org.sirio5.CoreConst;
import org.sirio5.services.modellixml.modelliXML;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;
import org.sirio5.utils.tree.CoreMenuTreeNode;

/**
 * Nuova gestione del menù principale su file XML.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class xmlMenuBean extends menuBean
{
  protected Document doc = null;
  protected modelliXML mdl = null;

  @Override
  public void init(CoreRunData data)
     throws Exception
  {
    super.init(data);
    mdl = (modelliXML) getService(modelliXML.SERVICE_NAME);
  }

  /**
   * Nuova versione con lettura del file XML.
   * Il menu viene gestito con un file XML invece
   * che con una tabella di database come nella
   * versione precedente.
   * @param data oggetto RunData al momento dell'invocazione
   * @throws Exception
   */
  @Override
  public void costruisciAlbero(RunData data)
     throws Exception
  {
    if(doc == null)
      doc = buildDocument();

    Element el = doc.getRootElement();
    padre = new CoreMenuTreeNode(new MenuItemBean());
    cercaFigli(data, 0, el, padre);
  }

  public void cercaFigli(RunData data, int livello, Element elPadre, CoreMenuTreeNode node)
     throws Exception
  {
    node.setEnabled(true);
    String permission = node.getMenuItem().getPermission();

    // controllo permessi
    if(SU.isOkStr(permission))
    {
      if(!SEC.checkAnyPermission(data, permission))
        node.setEnabled(false);
    }

    node.removeChild();
    if(livello >= CoreConst.MAX_LIVELLI_MENU)
      return;

    List<Element> lsFigli = elPadre.getChildren("menu");
    for(Element el : lsFigli)
    {
      CoreMenuTreeNode child = createNodeFromXml(el);
      node.addChild(child);
      cercaFigli(data, livello + 1, el, child);

      String submenugen = el.getAttributeValue("submenugen");
      if(SU.isOkStr(submenugen))
        creaSottoMenu(submenugen, data, livello + 1, el, child);
    }
  }

  protected CoreMenuTreeNode createNodeFromXml(Element el)
     throws Exception
  {
    MenuItemBean b = new MenuItemBean();
    b.setDescrizione(el.getAttributeValue("descrizione"));
    b.setProgramma(el.getAttributeValue("programma"));
    b.setImmagine(el.getAttributeValue("immagine"));
    b.setColore(el.getAttributeValue("colore"));
    b.setTipo(el.getAttributeValue("tipo"));
    b.setFlag1(el.getAttributeValue("flag1"));
    b.setFlag2(el.getAttributeValue("flag2"));
    b.setPermission(el.getAttributeValue("permission"));
    b.setNote(el.getAttributeValue("note"));
    return new CoreMenuTreeNode(b);
  }

  public Document buildDocument()
     throws Exception
  {
    String sFileMenu = TR.getString("menu.file", "menu.xml");
    File fxml = mdl.getConfMainFile(sFileMenu);
    return buildDocument(fxml);
  }

  public Document buildDocument(File fxml)
     throws JDOMException, IOException
  {
    log.info("Leggo " + fxml.getAbsolutePath());
    SAXBuilder builder = new SAXBuilder();
    return builder.build(fxml);
  }
}
