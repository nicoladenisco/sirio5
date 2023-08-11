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
package org.sirio5.rigel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ClassUtils;
import org.rigel5.SetupHolder;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.sirio5.modules.screens.rigel.FormBase;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;
import org.sirio5.utils.velocity.VelocityParser;

/**
 * Renderizzatore specializzato.
 *
 * @author Nicola De Nisco
 */
public class ToolRenderFormRigel extends FormBase
{
  protected ToolRigelUIManager uim = new ToolRigelUIManager();
  protected ToolCustomUrlBuilder urb = new ToolCustomUrlBuilder();
  protected String unique = null, funcNameEdit, funcNameSubmit, funcNameSplit, formName, bodyName;
  protected int counter;

  @Override
  public boolean isPopup()
  {
    return true;
  }

  @Override
  public boolean isEditPopup()
  {
    return true;
  }

  @Override
  protected String makeSelfUrl(RunData data, String type)
  {
    return data.getContextPath() + "/rigeltool/form/type/" + type + "/unique/" + unique;
  }

  @Override
  protected void makeContextHtml(boolean forceNew, boolean duplica, boolean nuovoDetail,
     Map params, CoreRunData data, Context context, HtmlWrapperBase pwl, String type, String baseUri)
     throws Exception
  {
    CoreCustomUrlBuilder ub = (CoreCustomUrlBuilder) SetupHolder.getUrlBuilder();
    urb.setBaseMainForm(ub.getBaseMainForm());
    urb.setBaseMainList(ub.getBaseMainList());
    urb.setBasePopupForm(ub.getBasePopupForm());
    urb.setBasePopupList(ub.getBasePopupList());
    urb.setType(type);

    context.put("unique", unique);
    context.put("formName", formName);

    funcNameEdit = "apriFinestraEdit_" + unique;
    context.put("funcNameEdit", funcNameEdit);
    funcNameSubmit = "submit_" + unique;
    context.put("funcNameSubmit", funcNameSubmit);
    bodyName = "body_" + unique;
    context.put("bodyName", bodyName);

    pwl.setUim(uim);

    RigelTableModel rtm = pwl.getPtm();
    rtm.setFormName(formName);

    super.makeContextHtml(forceNew, duplica, nuovoDetail, params, data, context, pwl, type, baseUri);
  }

  public void buildCtx(RunData data, Context ctx)
     throws Exception
  {
    doBuildTemplate2((CoreRunData) data, ctx);
  }

  /**
   * Produce HTML per il Tool delle liste.
   * Questa funzione viene chiamata dalla servlet ajax ToolDirectHtml.
   * @param data dati di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderHtml(RunData data)
     throws Exception
  {
    String ctxUnique = data.getParameters().getString("unique");
    Context ctx = (Context) data.getSession().getAttribute(ctxUnique);
    if(ctx == null)
      throw new Exception(INT.I("Context non presente in sessione; tool non disponibile."));

    String html = renderHtml(data, ctx);

    // da tutto l'html estrae solo la parte racchiusa da <form></form>
    // il resto non si può toccare
    int pos1, pos2;
    if((pos1 = html.indexOf("<!-- __START_CUT__ -->")) != -1)
      if((pos2 = html.indexOf("<!-- __END_CUT__ -->", pos1)) != -1)
        return html.substring(pos1, pos2);

    return html;
  }

  /**
   * Produce HTML per il Tool delle liste.
   * @param data dati di chiamata
   * @param ctx context di chiamata
   * @return HTML della lista
   * @throws Exception
   */
  public String renderHtml(RunData data, Context ctx)
     throws Exception
  {
    String type = data.getParameters().getString("type");
    if(type == null)
      throw new Exception("Errore interno: parametro type non definito; rivedere flusso.");

    unique = "FORM_" + SU.purge(type) + "_" + counter;
    formName = "fo_" + unique;

    return renderHtml(data, ctx, "ToolForm.vm");
  }

  /**
   * Produce HTML per il Tool delle liste.
   * @param data dati di chiamata
   * @param ctx context di chiamata
   * @param formName nome del form ospite
   * @return HTML della lista
   * @throws Exception
   */
  public String renderHtmlNoForm(RunData data, Context ctx, String formName)
     throws Exception
  {
    String type = data.getParameters().getString("type");
    if(type == null)
      throw new Exception("Errore interno: parametro type non definito; rivedere flusso.");

    unique = "FORM_" + SU.purge(type) + "_" + counter;
    this.formName = formName;

    return renderHtml(data, ctx, "ToolFormNoform.vm");
  }

  private synchronized String renderHtml(RunData data, Context ctx, String modello)
     throws Exception
  {
    boolean suppressEmpty = false;
    String suppressEmptyMessage = "";
    counter = (int) ctx.get("count");

    // recupera parametri del tool e li passa in RunData
    Map<String, String> mp = (Map<String, String>) ctx.get("paramsMap");
    if(mp != null)
    {
      suppressEmpty = SU.checkTrueFalse(mp.get("suppressEmpty"));
      suppressEmptyMessage = SU.okStr(mp.get("suppressEmptyMessage"), suppressEmptyMessage);

      for(Map.Entry<String, String> entry : mp.entrySet())
      {
        String key = entry.getKey();
        String value = entry.getValue();
        data.getParameters().setString(key, value);
      }
    }

    // costruisce tutti i componenti di pagina
    buildCtx(data, ctx);
    ctx.put("htpageForm", uim.getLastPageForm());
    ctx.put("htpageLista", uim.getLastPageLista());

    // salva il context in sessione per le successive chiamate dalla servlet ajax
    data.getSession().setAttribute(unique, ctx);

    if(suppressEmpty && SU.parse(ctx.get("numrows"), -1) == 0)
      return suppressEmptyMessage;

    StringWriter writer = new StringWriter(512);
    // renderizzazione Velocity con il modello caricato da risorsa
    try (InputStream is = ClassUtils.getResourceAsStream(getClass(), "/" + modello))
    {
      InputStreamReader reader = new InputStreamReader(is, "UTF-8");

      VelocityParser vp = new VelocityParser(ctx);
      vp.parseReader(reader, writer, modello);
    }

    // rimaneggia javascript sostituendo submit con funzione specifica
    return SU.strReplace(writer.toString(), "document." + formName + ".submit();", funcNameSubmit + "();");
  }
}
