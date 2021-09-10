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
package org.sirio2.modules.tools;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.velocity.VelocityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.sirio2.rigel.ToolRenderFormRigel;
import org.sirio2.utils.SU;

/**
 * Tool per l'accesso ai form xml di Rigel.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class RigelFormTool
   implements ApplicationTool
{
  public static final String MODELLO = "ToolForm.vm";
  //
  private final ToolRenderFormRigel render = new ToolRenderFormRigel();
  private static final AtomicInteger counter = new AtomicInteger();
  private VelocityService velocity;

  @Override
  public void init(Object data)
  {
    velocity = (VelocityService) TurbineServices.getInstance().getService(VelocityService.SERVICE_NAME);
  }

  @Override
  public void refresh()
  {
  }

  /**
   * Elabora HTML per il form rigel richiesto.
   * Questa funzione è deprecata. La selfUrlVM serviva nella versione precedente.
   * Questa versione utilizza ajax per aggiornare i dati visualizzati.
   * @param data dati di richiesta
   * @param form form rigel richiesto
   * @param selfUrlVM url della pagina che contiene il tool
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   * @deprecated
   */
  public String getHtml(RunData data, String form, String selfUrlVM, String params)
     throws Exception
  {
    return html(data, form, params);
  }

  /**
   * Elabora HTML per il form rigel richiesto.
   * @param data dati di richiesta
   * @param form form rigel richiesto
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   */
  public String html(RunData data, String form, String params)
     throws Exception
  {
    data.getParameters().setString("type", form);
    Context ctx = velocity.getContext(data);
    ctx.put("count", counter.getAndIncrement());

    // aggiunge i parametri specificati in params
    if(params != null)
    {
      Map<String, String> mp = SU.string2Map(params, ",", true);
      ctx.put("paramsMap", mp);
    }

    return render.renderHtml(data, ctx);
  }

  /**
   * Elabora HTML per il form rigel richiesto.
   * Ritorna l'html da inserire all'interno di un form già
   * esistente che probabilmente contiene altri valori da modificare.
   * Il salvataggio sarà a carico del form ospite (una action che estende FormSave).
   * @param data dati di richiesta
   * @param form form rigel richiesta
   * @param selfUrlVM url della pagina che contiene il tool
   * @param formName nome del form ospite
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   * @deprecated
   */
  public String getHtmlNoForm(RunData data, String form, String selfUrlVM, String formName, String params)
     throws Exception
  {
    return htmlNoForm(data, form, formName, params);
  }

  /**
   * Elabora HTML per il form rigel richiesto.
   * Ritorna l'html da inserire all'interno di un form già
   * esistente che probabilmente contiene altri valori da modificare.
   * Il salvataggio sarà a carico del form ospite (una action che estende FormSave).
   * @param data dati di richiesta
   * @param form form rigel richiesta
   * @param formName nome del form ospite
   * @param params parametri nella forma 'chiave=valore, chiave=valore'
   * @return html completo
   * @throws Exception
   */
  public String htmlNoForm(RunData data, String form, String formName, String params)
     throws Exception
  {
    data.getParameters().setString("type", form);
    Context ctx = velocity.getContext(data);
    ctx.put("count", counter.getAndIncrement());

    // aggiunge i parametri specificati in params
    if(params != null)
    {
      Map<String, String> mp = SU.string2Map(params, ",", true);
      ctx.put("paramsMap", mp);
    }

    return render.renderHtmlNoForm(data, ctx, formName);
  }
}
