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
package org.sirio2.modules.actions;

import java.util.*;
import org.apache.torque.om.Persistent;
import org.apache.velocity.context.*;
import org.rigel2.RigelCacheManager;
import org.rigel2.RigelI18nInterface;
import org.rigel2.SetupHolder;
import org.rigel2.exceptions.MissingListException;
import org.rigel2.glue.pager.PeerTablePagerEditApp;
import org.rigel2.glue.table.PeerAppMaintFormTable;
import org.rigel2.glue.validators.Validator;
import org.rigel2.table.html.hEditTable;
import org.rigel2.table.peer.html.*;
import org.sirio2.modules.actions.rigel.RigelEditBaseAction;
import org.sirio2.rigel.RigelHtmlI18n;
import org.sirio2.rigel.RigelUtils;
import org.sirio2.services.bus.BUS;
import org.sirio2.services.bus.BusContext;
import org.sirio2.services.bus.BusMessages;
import org.sirio2.services.security.SEC;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Salvataggio dei dati per i forms XML.
 *
 * @author Nicola De Nisco
 */
public class FormSave extends RigelEditBaseAction
{
  @Override
  public void doWork(CoreRunData data, Context context, Map params, String type,
     boolean saveDB, boolean saveTmp, boolean nuovoDetail, boolean cancellaDetail)
     throws Exception
  {
    PeerWrapperFormHtml pwl = getForm(data, type);
    if(pwl == null)
      throw new Exception(data.i18n("Inizializzazione non corretta."));

    if(!RigelUtils.checkPermessiLettura(data, pwl))
    {
      // permessi della lista non posseduti dall'utente
      redirectUnauthorized(data);
      return;
    }

    if(saveDB)
    {
      if(!pwl.isSaveEnabled())
        throw new Exception(data.i18n("Salvataggio non consentito."));

      if(!RigelUtils.checkPermessiScrittura(data, pwl))
      {
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(SU.isOkStr(data.getParameters().getString("new")))
    {
      if(!pwl.isNewEnabled())
        throw new Exception(data.i18n("Creazione nuovi oggetti non consentita."));

      if(!RigelUtils.checkPermessiCreazione(data, pwl))
      {
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    PeerAppMaintFormTable pfe = (PeerAppMaintFormTable) (pwl.getTbl());
    boolean isNewObject = pfe.isNewObject();

    // imposta credenziali e aggiorna dati
    pfe.setUserInfo(SEC.getUserID(data), SEC.isAdmin(data));
    pfe.aggiornaDati(data.getSession(), params, saveDB, saveTmp, data);
    Persistent objInEdit = pfe.getLastObjectInEdit();
    RigelI18nInterface i18n = new RigelHtmlI18n(data);

    // verifica per master/detail
    if(!isNewObject && pwl.getMdInfo() != null)
    {
      String dettType = pwl.getMdInfo().getEditList();
      if(dettType == null)
        throw new Exception(data.i18n("Manca dettaglio nella definizione master-detail. Controllare lista.xml."));

      doWorkDetail(data, context, params, type, dettType, pwl, objInEdit, i18n,
         saveDB, saveTmp, nuovoDetail, cancellaDetail);

      // attiva le azioni di post parsing eventualmente presenti nel blocco <master-detail/> del master
      Validator.postParseValidate(pwl.getMdInfo().getEleXml(),
         objInEdit, pwl.getPtm(), null, 0,
         data.getSession(), params, i18n, null, data);
    }

    // aggancio per classi derivate
    doPostSave(data, context, params, type, saveDB, objInEdit);

    // Attiva le azioni di post save
    params.put("SAVED_ON_DATABASE", saveDB);
    Validator.postSaveAction(pwl.getEleXml(), objInEdit, pwl.getPtm(), pfe, 0, data.getSession(), params, i18n, data);

    if(saveDB)
    {
      // invalida le cache di Rigel interessate dalla tabella modificata
      RigelCacheManager cm = SetupHolder.getCacheManager();
      cm.purgeTabella(pwl.getNomeTabella());

      BusContext bc = new BusContext(params);
      bc.setI18n(i18n);
      bc.put("obj", objInEdit);
      bc.put("isNewObject", isNewObject);

      BUS.sendMessageAsync(BusMessages.GENERIC_OBJECT_SAVED, this, bc);
    }

    context.put("obj", objInEdit);
  }

  /**
   * Salva eventuale detail del form.
   * @param data
   * @param context
   * @param params
   * @param type
   * @param dettType
   * @param pwl
   * @param objInEdit the value of objInEdit
   * @param i18n
   * @param saveDB
   * @param saveTmp
   * @param nuovoDetail
   * @param cancellaDetail the value of cancellaDetail
   * @throws Exception
   */
  protected void doWorkDetail(CoreRunData data, Context context,
     Map params, String type, String dettType, PeerWrapperFormHtml pwl, Persistent objInEdit,
     RigelI18nInterface i18n,
     boolean saveDB, boolean saveTmp, boolean nuovoDetail, boolean cancellaDetail)
     throws Exception
  {
    PeerWrapperEditHtml eh = (PeerWrapperEditHtml) getLista(data, dettType);
    if(eh == null)
      throw new MissingListException(data.i18n("Lista dettaglio %s non trovata. Controllare lista.xml.", dettType));

    if(!RigelUtils.checkPermessiLettura(data, eh))
    {
      // permessi della lista non posseduti dall'utente
      redirectUnauthorized(data);
      return;
    }

    if(saveDB)
    {
      if(!eh.isSaveEnabled())
        throw new Exception(data.i18n("Salvataggio non consentito."));

      if(!RigelUtils.checkPermessiScrittura(data, eh))
      {
        // permessi salvataggio lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(nuovoDetail)
    {
      if(!eh.isNewEnabled())
        throw new Exception(data.i18n("Creazione nuovi oggetti non consentita."));

      if(!RigelUtils.checkPermessiCreazione(data, eh))
      {
        // permessi nuovo elemento lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(cancellaDetail)
    {
      if(!eh.isSaveEnabled())
        throw new Exception(data.i18n("Cancellazione non consentita."));

      if(!RigelUtils.checkPermessiCancellazione(data, eh))
      {
        // permessi cancellazione lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }

      String sKey = data.getParameters().getString("key");
      RigelUtils.deleteRecord(data, sKey, eh);
      doClear(data, type);
    }

    // carica eventuale filtro sul detail
    eh.populateParametri(params);

    // estae i parametri di collegamento dal master
    Map linkParams = pwl.makeMapMasterDetail(0);

    // aggiorna e salva i dati sul db
    PeerTablePagerEditApp peh = (PeerTablePagerEditApp) (eh.getPager());
    peh.aggiornaDati(data.getSession(), params, nuovoDetail, saveDB, data, linkParams);

    // oggetti modificati
    List<Persistent> objectsDetail = ((PeerTableModel) eh.getPtm()).getVBuf();
    if(objectsDetail != null)
    {
      context.put("objDet", objectsDetail);

      if(saveDB && !objectsDetail.isEmpty())
      {
        // invalida le cache di Rigel interessate dalla tabella modificata
        RigelCacheManager cm = SetupHolder.getCacheManager();
        cm.purgeTabella(eh.getNomeTabella());
      }
    }

    // attiva le azioni di post parsing eventualmente presenti
    Validator.postSaveMasterDetail(pwl.getMdInfo().getEleXml(),
       objInEdit, pwl.getPtm(), (hEditTable) pwl.getTbl(), 0,
       objectsDetail, eh.getPtm(), (hEditTable) eh.getTbl(),
       data.getSession(), params, i18n, null, data);
  }

  /**
   * Pulizia dati in sessione.
   * @param data
   * @param type
   * @throws Exception
   */
  @Override
  protected void doClear(CoreRunData data, String type)
     throws Exception
  {
  }

  /**
   * Operazioni di post salvataggio oggetto.
   * Questa funzione è un segnaposto per classi derivate
   * che vogliono salvare altri dati a seguito del salvataggio
   * dell'oggetto principale. In questa classe l'implementazione è vuota.
   * @param data
   * @param context
   * @param params
   * @param type
   * @param saveDB
   * @param objInEdit oggetto sottoposto ad edit
   * @throws Exception
   */
  protected void doPostSave(CoreRunData data, Context context,
     Map params, String type, boolean saveDB, Persistent objInEdit)
     throws Exception
  {
  }
}
