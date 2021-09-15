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
package org.sirio2.rigel;

import java.util.Map;
import org.rigel5.HtmlUtils;
import org.rigel5.RigelCustomUrlBuilder;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.wrapper.CustomButtonInfo;
import org.sirio2.utils.LI;

/**
 * Implementazione di riferimento di un custom url builder.
 *
 * @author Nicola De Nisco
 */
public class CoreCustomUrlBuilder implements RigelCustomUrlBuilder
{
  private final String ctxPath, ctxPathTempl;
  private String baseMainForm, baseMainList, basePopupForm, basePopupList;

  public CoreCustomUrlBuilder()
  {
    ctxPath = LI.getContextPath();
    ctxPathTempl = ctxPath + "app/template/";
  }

  public CoreCustomUrlBuilder(String ctxp)
  {
    if(!ctxp.startsWith("/"))
      ctxp = "/" + ctxp;

    if(ctxp.endsWith("/"))
      ctxPath = ctxp;
    else
      ctxPath = ctxp + "/";

    ctxPathTempl = ctxPath + "app/template/";
  }

  /**
   * Imposta la url corretta a seconda se è una jsp o una vm.
   *
   * @param popup
   * @param url
   * @return
   */
  protected String adJustUrl(boolean popup, String url)
  {
    // genera una path assoluta
    if(HtmlUtils.isHttp(url))
      return url;

    url = parseMacro(popup, url);

    if(url.toLowerCase().startsWith("javascript:"))
      return url;

    if(!url.contains(".vm"))
    {
      // non e' un link a template
      url = ctxPath + url;
    }
    else
    {
      // e' un link a template
      url = ctxPathTempl + url;
    }

    return url;
  }

  /**
   * Sostituisce eventuali macro nella stringa input.
   *
   * @param input
   * @return
   */
  protected String parseMacro(boolean popup, String input)
  {
    if(popup)
    {
      // se la maschera è in popup deve usare il form di popup ...
      input = input.replaceAll("@form", basePopupForm);
      input = input.replaceAll("@list", basePopupList);
    }
    else
    {
      // ... altrimenti usa quelli di primo livello
      input = input.replaceAll("@form", baseMainForm);
      input = input.replaceAll("@list", baseMainList);
    }

    // queste macro sono esplicite, non soggette al flag popup
    input = input.replaceAll("@pform", basePopupForm);
    input = input.replaceAll("@plist", basePopupList);
    return input;
  }

  @Override
  public String buildUrlForeginList(boolean popup,
     RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, int col)
     throws Exception
  {
    return makeUrlAbsolute(popup, cd.getForeignEditUrl());
  }

  @Override
  public String buildUrlForeginForm(boolean popup,
     RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, int col)
     throws Exception
  {
    return makeUrlAbsolute(popup, cd.getForeignFormUrl());
  }

  @Override
  public String buildUrlCustomButton(boolean popup,
     RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, CustomButtonInfo cb)
     throws Exception
  {
    String url = cb.makeUrlRiga((RigelTableModel) (tableModel), row);
    return url == null ? null : makeUrlAbsolute(popup, url);
  }

  @Override
  public String buildUrlHeaderButton(boolean popup, RigelTableModel tableModel, CustomButtonInfo cb)
     throws Exception
  {
    String url = cb.makeUrlTestata(tableModel);
    return makeUrlAbsolute(popup, url);
  }

  @Override
  public String buildUrlHeaderButton(boolean popup,
     RigelTableModel tableModel, int row, CustomButtonInfo cb)
     throws Exception
  {
    String url = cb.makeUrlRiga((RigelTableModel) (tableModel), row);
    return makeUrlAbsolute(popup, url);
  }

  @Override
  public String buildImageCustomButton(boolean popup,
     RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, CustomButtonInfo cb)
     throws Exception
  {
    return LI.getImgIcon(cb.getIcon(), cb.getText());
  }

  @Override
  public String buildUrlEditRecord(boolean popup,
     String inputUrl, RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    String url = makeUrlAbsolute(popup, inputUrl);

    if(!url.contains("ridx=") && row != -1)
      url = HtmlUtils.mergeUrl(url, "rdix", row);

    return HtmlUtils.mergeUrlTestUnique(url, extraParams);
  }

  @Override
  public String buildUrlNewRecord(boolean popup,
     String inputUrl, RigelTableModel tableModel, Map<String, String> extraParams)
     throws Exception
  {
    String url = makeUrlAbsolute(popup, inputUrl);
    url = LI.mergeUrl(url, "new", 1);
    return HtmlUtils.mergeUrlTestUnique(url, extraParams);
  }

  @Override
  public String buildUrlLineEdit(boolean popup,
     String inputUrl, RigelTableModel tableModel, RigelColumnDescriptor cd,
     String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return buildUrlEditRecord(popup, inputUrl, tableModel, cd, fldName, row, extraParams);
  }

  @Override
  public String makeUrlAbsolute(boolean popup, String url)
     throws Exception
  {
    return adJustUrl(popup, url);
  }

  @Override
  public String buildUrlLineSelezione(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return "javascript:imposta(" + inputUrl + ")";
  }

  @Override
  public String buildUrlSelezionaRecord(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return "javascript:imposta(" + inputUrl + ")";
  }

  @Override
  public String buildUrlCancellaRecord(boolean popup, String inputUrl,
     RigelTableModel tableModel, RigelColumnDescriptor cd, String fldName, int row, Map<String, String> extraParams)
     throws Exception
  {
    return "javascript:cancellaElemento('" + inputUrl + "')";
  }

  public String getBaseMainForm()
  {
    return baseMainForm;
  }

  public void setBaseMainForm(String baseMainForm)
  {
    this.baseMainForm = baseMainForm;
  }

  public String getBaseMainList()
  {
    return baseMainList;
  }

  public void setBaseMainList(String baseMainList)
  {
    this.baseMainList = baseMainList;
  }

  public String getBasePopupForm()
  {
    return basePopupForm;
  }

  public void setBasePopupForm(String basePopupForm)
  {
    this.basePopupForm = basePopupForm;
  }

  public String getBasePopupList()
  {
    return basePopupList;
  }

  public void setBasePopupList(String basePopupList)
  {
    this.basePopupList = basePopupList;
  }
}
