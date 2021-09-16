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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.RunData;
import org.commonlib5.utils.ClassOper;
import org.commonlib5.utils.StringOper;
import org.jdom2.Element;
import org.rigel5.HtmlUtils;
import org.sirio5.CoreConst;
import static org.sirio5.CoreConst.APP_PREFIX;
import org.sirio5.beans.CoreBaseBean;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.TR;
import org.sirio5.utils.tree.CoreMenuTreeNode;

/**
 * Bean di supporto per la visualizzazione del menu.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
abstract public class menuBean extends CoreBaseBean
{
  public static final int ID_LISTPRO_PADRE = 0;
  protected CoreMenuTreeNode padre = null;
  protected int rientra = 5;
  protected int border = 0;
  protected boolean SHOW_HELP = false;
  protected boolean USE_ANCHOR = false;
  protected String baseFormUrl, baseListUrl, baseFormPopup, baseListPopup, home;
  protected String htmlMenu = null;

  /** Creates a new instance of menuBean */
  public menuBean()
  {
    border = TR.getInt("menu.border", 0);
    SHOW_HELP = TR.getBoolean("menu.showhelp", false);
    USE_ANCHOR = TR.getBoolean("menu.useanchor", false);

    baseFormUrl = TR.getString("services.ModelliXML.baseFormUrl", "mform.vm");
    baseListUrl = TR.getString("services.ModelliXML.baseListUrl", "maint.vm");
    baseFormPopup = TR.getString("services.ModelliXML.baseFormPopup", "pform.vm");
    baseListPopup = TR.getString("services.ModelliXML.baseListPopup", "plista.vm");
    home = TR.getString("template.homepage", "Index.vm");
  }

  protected String substMacro(String s)
  {
    if(s != null)
    {
      s = StringOper.strReplace(s, "@list", baseListUrl);
      s = StringOper.strReplace(s, "@form", baseFormUrl);
      s = StringOper.strReplace(s, "@plist", baseListPopup);
      s = StringOper.strReplace(s, "@pform", baseFormPopup);
      s = StringOper.strReplace(s, "@home", home);
      s = StringOper.strReplace(s, "@action", home + "/action");
    }
    return s;
  }

  abstract public void costruisciAlbero(RunData data)
     throws Exception;

  protected String fmtFontColor(String color, String text)
  {
    return (color != null && color.trim().length() > 0)
              ? "<font color=\"" + color + "\">" + text + "</font>" : text;
  }

  protected String fmtFontColor(MenuItemBean l)
  {
    return fmtFontColor(l.getColore(), l.getDescrizione());
  }

  protected String fmtItemUrl(MenuItemBean l)
  {
    String url = substMacro(SU.okStrNull(l.getProgramma()));

    if(url == null)
      return "#";

    switch(SU.okStr(l.getFlag1()))
    {
      case "a":
      case "A":
        // absolute: interna all'applicazione ma assoluta (no template)
        return HtmlUtils.mergePath(Turbine.getContextPath(), url);

      case "i":
      case "I":
        // internal: generazione url su application server
        return getServerUrl(url);

      case "e":
      case "E":
        // external: url esterna all'applicazione; viene usata nel menu senza modifiche
        return url;
    }

    // per default viene considerata internal
    return getServerUrl(url);
  }

  protected void displayAlberoJavascript(CoreRunData data, String func,
     int livello, PrintWriter out, CoreMenuTreeNode node)
     throws Exception
  {
    if(livello >= CoreConst.MAX_LIVELLI_MENU)
      return;

    String spaces = StringOper.GetSpaces(livello + 3);

    for(int i = 0; i < node.getChildCount(); i++)
    {
      CoreMenuTreeNode child = (CoreMenuTreeNode) (node.getChildAt(i));
      if(child == null)
        continue;

      MenuItemBean l = child.getMenuItem();

      // controllo permessi
      if(l == null || !child.isEnabled())
        continue;

      if(child.isLeaf())
        out.println(spaces + "<li><a href=\"" + fmtItemUrl(l) + "\">" + data.i18n(l.getDescrizione()) + "</a></li>");
      else
      {
        if(livello == 0)
        {
          out.println(spaces + "<li class=\"dropdown\">");
          out.println(spaces
             + "<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" role=\"button\" aria-expanded=\"false\">"
             + data.i18n(l.getDescrizione()) + " <span class=\"caret\"></span></a>\n");
          out.println(spaces + "<ul class=\"dropdown-menu multi-level\" role=\"menu\">");
        }
        else
        {
          out.println(spaces + "<li class=\"dropdown-submenu\">");
          out.println(spaces
             + "<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">"
             + data.i18n(l.getDescrizione()) + " </a>\n");
          out.println(spaces + "<ul class=\"dropdown-menu\">");
        }
        displayAlberoJavascript(data, func, livello + 1, out, child);
        out.println(spaces + "</ul>");
        out.println(spaces + "</li>");
      }
    }
  }

  public void prepareToRender(RunData data)
     throws Exception
  {
    HttpServletRequest request = data.getRequest();

    if(padre == null
       || request.getParameter("rileggi") != null
       || request.getParameter("reload") != null)
    {
      htmlMenu = null;
      costruisciAlbero(data);
    }

    currJspName = request.getRequestURI();

    int pos = 0;
    if((pos = currJspName.indexOf("/action")) != -1)
      currJspName = currJspName.substring(0, pos);

    if(currJspName.endsWith(APP_PREFIX))
      currJspName += "/Index.vm";
  }

  public void printJavascriptMenu(CoreRunData data, Writer wrout)
     throws Exception
  {
    PrintWriter out = new PrintWriter(wrout);
    displayAlberoJavascript(data, "aggiornaMenuCore", 0, out, padre);
    out.flush();
  }

  public String getHtml(RunData data)
     throws Exception
  {
    prepareToRender(data);

    if(htmlMenu == null)
    {
      StringWriter swr = new StringWriter(1024);
      printJavascriptMenu((CoreRunData) data, swr);
      swr.flush();
      htmlMenu = swr.toString();
    }

    return htmlMenu;
  }

  public void creaSottoMenu(String submenugen, RunData data, int livello,
     Element elPadre, CoreMenuTreeNode node)
     throws Exception
  {
    Class subGenClass = ClassOper.loadClass(submenugen,
       ClassOper.getClassPackage(MenuGenerator.class), getMoreClasspaths());

    if(subGenClass == null)
      return;

    MenuGenerator mg = (MenuGenerator) subGenClass.newInstance();

    // aggiunge il menu del plugin
    mg.creaSottoMenu(submenugen, data, livello, elPadre, node);
  }

  public CoreMenuTreeNode getPadre()
  {
    return padre;
  }

  /**
   * Ulteriori path di ricerca per generatore menu.
   * Segnaposto per classi derivate.
   * ritorna null in questa implementazione.
   * @return path di ricerca del generatore sottomenu
   */
  public String[] getMoreClasspaths()
  {
    return null;
  }
}
