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
import org.apache.velocity.context.*;
import org.rigel2.RigelCacheManager;
import org.rigel2.SetupHolder;
import org.rigel2.exceptions.MissingListException;
import org.rigel2.glue.pager.PeerTablePagerEditApp;
import org.rigel2.table.AbstractTablePager;
import org.rigel2.table.html.AbstractHtmlTablePager;
import org.rigel2.table.html.wrapper.HtmlWrapperBase;
import org.rigel2.table.peer.html.PeerTableModel;
import org.sirio2.modules.actions.rigel.RigelEditBaseAction;
import org.sirio2.rigel.RigelHtmlI18n;
import org.sirio2.rigel.RigelUtils;
import org.sirio2.services.bus.BUS;
import org.sirio2.services.bus.BusContext;
import org.sirio2.services.bus.BusMessages;
import org.sirio2.utils.CoreRunData;
import org.sirio2.utils.SU;

/**
 * Salvataggio dei dati per i forms XML.
 *
 * @author Nicola De Nisco
 */
public class ListaEditSave extends RigelEditBaseAction
{
  @Override
  protected void doWork(CoreRunData data, Context context, Map params, String type,
     boolean saveDB, boolean saveTmp, boolean nuovoDetail, boolean cancellaDetail)
     throws Exception
  {
    HtmlWrapperBase eh = getLista(data, type);
    if(eh == null)
      throw new MissingListException(data.i18n("Lista %s non trovata. Controllare lista.xml.", type));

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
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(SU.isOkStr(data.getParameters().getString("new"))) // NOI18N
    {
      if(!eh.isNewEnabled())
        throw new Exception(data.i18n("Creazione nuovi oggetti non consentita."));

      if(!RigelUtils.checkPermessiCreazione(data, eh))
      {
        // permessi della lista non posseduti dall'utente
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
        // permessi della lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }
    }

    if(cancellaDetail)
    {
      if(!eh.isSaveEnabled())
        throw new Exception("Salvataggio non consentito.");

      if(!RigelUtils.checkPermessiCancellazione(data, eh))
      {
        // permessi cancellazione lista non posseduti dall'utente
        redirectUnauthorized(data);
        return;
      }

      String sKey = data.getParameters().getString("key");
      RigelUtils.deleteRecord(data, sKey, eh);
      doClear(data, type);

      AbstractTablePager pager = eh.getPager();
      if(pager instanceof PeerTablePagerEditApp)
        ((PeerTablePagerEditApp) pager).reloadAllRecords();

      return;
    }

    AbstractHtmlTablePager peh = (AbstractHtmlTablePager) (eh.getPager());

    // aggiorna e salva i dati sul db
    if(peh instanceof PeerTablePagerEditApp)
    {
      ((PeerTablePagerEditApp) peh).aggiornaDati(data.getSession(),
         params, nuovoDetail, saveDB, data, null);

      if(saveDB)
      {
        // invalida le cache di Rigel interessate dalla tabella modificata
        RigelCacheManager cm = SetupHolder.getCacheManager();
        cm.purgeTabella(eh.getNomeTabella());

        PeerTableModel ptm = (PeerTableModel) eh.getPtm();
        BusContext bc = new BusContext(params);
        bc.setI18n(new RigelHtmlI18n(data));
        bc.put("objsInEdit", ptm.getVBuf());

        BUS.sendMessageAsync(BusMessages.GENERIC_OBJECTS_SAVED, this, bc);
      }
    }
  }

  @Override
  protected void doClear(CoreRunData data, String type)
     throws Exception
  {
  }
}
