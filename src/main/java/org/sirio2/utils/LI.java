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

import java.util.Map;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.PullService;
import org.apache.turbine.services.pull.tools.UITool;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;
import org.apache.turbine.util.uri.TemplateURI;
import org.rigel2.HtmlUtils;
import static org.sirio2.CoreConst.APP_PREFIX;

/**
 * Funzioni di servizio la creazione e manipolazione di links.
 *
 * @author Nicola De Nisco
 */
public class LI extends HtmlUtils
{
  /** aggancia il settaggio a quello del tool ui (link relativi/assoluti) */
  private static final boolean wantRelative = TR.getBoolean("tool.ui.want.relative", true);

  /** aggancia il settaggio a quello del tool ui (link relativi/assoluti) */
  private static final boolean imagesFromSkin = TR.getBoolean("images.icons.fromskin", true);

  /**
   * Ritorna la uri (http://.../pgm/template/mia.vm)
   * del file di modello vm indicato.
   * @param data
   * @param templatePage
   * @return
   */
  public static String getTemplateLink(RunData data, String templatePage)
  {
    TemplateURI tui = new TemplateURI(data, templatePage);
    return wantRelative ? tui.getRelativeLink() : tui.getAbsoluteLink();
  }

  /**
   * Ritorna la path di base per le url.
   * In genere 'http://localhost:8080/nomeapp/'
   * oppure '/nomeapp/' se i link sono relativi.
   * @return la contextPath dell'applicazione web.
   */
  public static String getContextPath()
  {
    if(__myContextPath == null)
    {
      ServerData sd = Turbine.getDefaultServerData();
      String s = SU.okStrNull(System.getProperty("globalContextPath"));
      if(s == null)
        s = SU.okStrNull(sd.getContextPath());

      if(s == null)
        return null;

      __myContextPath = ("/" + s + "/").replace("//", "/");
      if(!wantRelative)
      {
        StringBuilder sb = new StringBuilder();
        sd.getHostUrl(sb);
        sb.append(__myContextPath);
        __myContextPath = sb.toString();
      }
    }

    return __myContextPath;
  }

  private static String __myContextPath = null;

  public static String getIconAwesome(String iconName, String alt)
  {
    return "<i class='fa fa-" + iconName + "' title='" + alt + "'></i>";
  }

  /**
   * Ritorna l'html compelto dell'immagine con
   * dimensioni fisse da icona (16x16).
   * @param imgFile nome del file immagine
   * @param alt stringa alternativa e di popup
   * @return html dell'immagine
   */
  public static String getIconHtml(String imgFile, String alt)
  {
    return "<img border=0 src=\"" + getImageUrl(imgFile)
       + "\" alt=\"" + alt + "\" title=\"" + alt + "\" width=16 height=16>";
  }

  public static String getIconHtml(String imgFile, String alt, int width, int height)
  {
    return "<img border=0 src=\"" + getImageUrl(imgFile)
       + "\" alt=\"" + alt + "\" title=\"" + alt + "\" width=" + width + " height=" + height + ">";
  }

  /**
   * Ritorna l'html compelto dell'immagine;
   * le dimensioni non sono specificate.
   * @param imgFile nome del file immagine
   * @param alt stringa alternativa e di popup
   * @return html dell'immagine
   */
  public static String getImageHtml(String imgFile, String alt)
  {
    return "<img border=0 src=\"" + getImageUrl(imgFile)
       + "\" alt=\"" + alt + "\" title=\"" + alt + "\">";
  }

  /**
   * Ritorna l'html compelto dell'immagine.
   * @param imgFile nome del file immagine
   * @param alt stringa alternativa e di popup
   * @param w larghezza
   * @param h altezza
   * @return html dell'immagine
   */
  public static String getImageHtml(String imgFile, String alt, int w, int h)
  {
    return "<img border=0 src=\"" + getImageUrl(imgFile)
       + "\" alt=\"" + alt + "\" title=\"" + alt + "\" width=\"" + w + "\" height=\"" + h + "\">";
  }

  /**
   * Ritorna l'html compelto dell'immagine 1.gif ovvero
   * l'immagine trucco per creare aree occupate a piacere.
   * @param w larghezza
   * @param h altezza
   * @return html dell'immagine
   */
  public static String getImageTrucco(int w, int h)
  {
    return "<img border=0 src=\"" + getImageUrl("1.gif")
       + "\" width=\"" + w + "\" height=\"" + h + "\">";
  }

  /**
   * Ritorna l'html compelto dell'immagine.
   * @param imgFile nome del file immagine
   * @param alt stringa alternativa e di popup
   * @param title stringa in sovrimpressione
   * @param w larghezza
   * @param h altezza
   * @return html dell'immagine
   */
  public static String getImageHtml(String imgFile, String alt, String title, int w, int h)
  {
    return "<img border=0 src=\"" + getImageUrl(imgFile)
       + "\" alt=\"" + alt + "\" title=\"" + title + "\" width=\""
       + w + "\" height=\"" + h + "\">";
  }

  private static PullService ps;
  private static UITool ui = null;

  /**
   * Ritorna l'url completa dell'immagine (http://.../images/nomeima),
   * prelevandola dalla directory images dell'applicazione web.
   * @param nomeima
   * @return
   */
  public static String getImageUrl(String nomeima)
  {
    if(imagesFromSkin)
    {
      if(ps == null)
        ps = (PullService) TurbineServices.getInstance().getService(PullService.SERVICE_NAME);

      if(ui == null)
        ui = (UITool) ps.getGlobalContext().get("ui");

      return ui.image(nomeima);
    }
    else
    {
      return getContextPath() + "images/" + nomeima;
    }
  }

  /**
   * Ritorna l'url assoluta di una url relativa.
   * Se l'url e' il nome di una pagina vm ritorna
   * il corretto link all'area template.
   * @param url
   * @return
   */
  public static String getLinkUrl(String url)
  {
    if(!url.startsWith("http:"))
      if(url.startsWith("@action/"))
        return getContextPath() + APP_PREFIX + "/action/" + url.substring(8);
      else if(url.contains(".vm"))
        return getContextPath() + APP_PREFIX + "/template/" + url;
      else
        return mergePath(getContextPath(), url);
    return url;
  }

  /**
   * Ritorna l'url assoluta di una url relativa.
   * Se l'url e' il nome di una pagina vm ritorna
   * il corretto link all'area template.
   * @param url
   * @param params
   * @return
   */
  public static String getLinkUrlwParams(String url, Map params)
  {
    return mergeUrl(getLinkUrl(url), params);
  }

  /**
   * Ritorna l'url per la funzione abbandona e salva ed esci
   * ovvero l'url di ritorno di un form interpretando il contenuto
   * della string a jlc.
   * @param jlc
   * @return
   */
  public static String getFormAbbandonaUrl(String jlc)
  {
    if(jlc.contains(".vm") || jlc.contains(".jsp"))
    {
      return getLinkUrl(jlc);
    }
    else
    {
      return getLinkUrl("maint.vm/type/" + jlc); // NOI18N
    }
  }

  public static String getImgIcon(String icon, String text)
  {
    // sintassi per icone build int in Bootstrap 3
    if(icon.startsWith("glyphicon:"))
      return getImgGlyphicon(SU.okStr(icon.substring(10)), text);

    // vecchia sintassi font awesome 4
    if(icon.startsWith("awesome:"))
      return getImgAwesome(SU.okStr(icon.substring(8)), text);
    // vecchia sintassi font awesome 4
    if(icon.startsWith("awesome-spin:"))
      return getImgAwesomeSpin(SU.okStr(icon.substring(13)), text);

    // nuova sintassi solid/regular font awesome 5
    if(icon.startsWith("fas:"))
      return getImgAwesomeFas(SU.okStr(icon.substring(4)), text);
    if(icon.startsWith("far:"))
      return getImgAwesomeFar(SU.okStr(icon.substring(4)), text);
    if(icon.startsWith("fas-spin:"))
      return getImgAwesomeFasSpin(SU.okStr(icon.substring(9)), text);
    if(icon.startsWith("far-spin:"))
      return getImgAwesomeFarSpin(SU.okStr(icon.substring(9)), text);

    return LI.getIconHtml(icon, text);
  }

  public static String getImgGlyphicon(String name, String title)
  {
    return "<span class=\"glyphicon glyphicon-" + name + "\" aria-hidden=\"true\" title=\"" + title + "\"></span>";
  }

  public static String getImgAwesome(String name, String title)
  {
    return "<i class=\"fa fa-" + name + "\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFas(String name, String title)
  {
    return "<i class=\"fas fa-" + name + "\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFar(String name, String title)
  {
    return "<i class=\"far fa-" + name + "\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFab(String name, String title)
  {
    return "<i class=\"fab fa-" + name + "\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeSpin(String name, String title)
  {
    return "<i class=\"fa fa-" + name + " fa-spin\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFasSpin(String name, String title)
  {
    return "<i class=\"fas fa-" + name + " fa-spin\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFarSpin(String name, String title)
  {
    return "<i class=\"far fa-" + name + " fa-spin\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }

  public static String getImgAwesomeFabSpin(String name, String title)
  {
    return "<i class=\"fab fa-" + name + " fa-spin\" aria-hidden=\"true\" title=\"" + title + "\"></i>";
  }
}
