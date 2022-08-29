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
package org.sirio5.modules.actions.rigel;

import java.util.Map;
import org.apache.velocity.context.Context;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.html.PeerWrapperFormHtml;
import org.sirio5.CoreConst;
import org.sirio5.modules.actions.CoreBaseAction;
import org.sirio5.services.modellixml.MDL;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SU;

/**
 * Classe base di FormSave e ListaEditSave.
 * Funzioni comuni alle action per salvare forms rigel.
 *
 * @author Nicola De Nisco
 */
abstract public class RigelEditBaseAction extends CoreBaseAction
{
  @Override
  public void doPerform2(CoreRunData data, Context context)
     throws Exception
  {
    String type = data.getParameters().getString("type");
    if(type == null)
      throw new Exception("Manca il parametro type.");

    boolean saveDB = false, ritorna = false, nuovoDetail = false, duplica = false,
       clear = false, cancella = false, chiudi = false, nuovo = false;

    Map params = SU.getParMap(data);
    String cmd = SU.okStr(params.get("command")); // NOI18N

    if(!cmd.isEmpty())
    {
      // eventuale aggancio a comandi definiti in classi derivate
      doCommand(cmd, data, params, context);
      // un eventuale funzione potrebbe aver alterato il comando
      cmd = SU.okStr(params.get("command")); // NOI18N
    }

    switch(cmd)
    {
      case CoreConst.SAVE_ONLY:
        saveDB = true;
        break;
      case CoreConst.SAVE_AND_NEW:
        saveDB = nuovo = true;
        break;
      case CoreConst.SAVE_AND_EXIT:
        saveDB = ritorna = true;
        break;
      case CoreConst.SAVE_AND_CLOSE:
        saveDB = chiudi = true;
        break;
      case CoreConst.NEW_DETAIL:
        nuovoDetail = true;
        break;
      case CoreConst.DUP_CURRENT:
        duplica = saveDB = true;
        break;
      case CoreConst.CLEAR_FORM_DATA:
        clear = ritorna = true;
        break;
      case CoreConst.CLOSE_EDIT:
        clear = chiudi = true;
        break;
      case CoreConst.DELETE_RECORD:
        cancella = true;
        break;
    }

    if(clear)
      doClear(data, type);
    else
      doWork(data, context, params, type, saveDB, !ritorna, nuovoDetail, cancella);

    if(chiudi)
    {
      // redirezione a chiusura popup
      data.getTemplateInfo().setScreenTemplate("closeme.vm");
      return;
    }

    if(ritorna)
    {
      String jlc = SU.okStrNull(data.getParameters().getString("jlc"));
      String jvm = SU.okStrNull(data.getParameters().getString("jvm"));

      if(!verificaPaginaRitorno(data, jlc, jvm))
      {
        // variabile jlc non definita o non comprensibile: torniamo alla pagina iniziale
        gotoHome(data);
      }
    }
  }

  /**
   * Salvataggio del form.
   * Salva i campi del form nei rispettivi oggetti peer utilizzando le funzioni di rigel.
   * @param data
   * @param context
   * @param params
   * @param type
   * @param saveDB
   * @param saveTmp
   * @param nuovoDetail
   * @param cancellaDetail
   * @throws Exception
   */
  abstract protected void doWork(CoreRunData data, Context context, Map params, String type,
     boolean saveDB, boolean saveTmp, boolean nuovoDetail, boolean cancellaDetail)
     throws Exception;

  /**
   * Pulisce informazioni di sessione.
   * Rimuove dalla sessione i dati memorizzati per l'editing del form.
   * Generalmente utilizzata nel comando abbandona.
   * @param data
   * @param type
   * @throws Exception
   */
  abstract protected void doClear(CoreRunData data, String type)
     throws Exception;

  protected PeerWrapperFormHtml getForm(CoreRunData data, String type)
     throws Exception
  {
    WrapperCacheBase wpc = MDL.getWrapperCache(data);
    return wpc.getFormCache(type);
  }

  protected HtmlWrapperBase getLista(CoreRunData data, String type)
     throws Exception
  {
    WrapperCacheBase wpc = MDL.getWrapperCache(data);
    return wpc.getListaEditCache(type);
  }

  @Override
  protected boolean isAuthorized(CoreRunData data)
     throws Exception
  {
    return isAuthorizedAll(data, "modifica_dati_generale");
  }

  protected boolean isAuthorizedDelete(CoreRunData data)
     throws Exception
  {
    return isAuthorizedAll(data, "cancella_manutenzione");
  }
}
