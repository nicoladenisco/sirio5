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

import org.rigel5.glue.table.PeerAppMaintFormTable;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.rigel5.glue.PeerObjectSaver;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.glue.table.AlternateColorTableAppBase;
import org.rigel5.glue.table.HeditTableApp;
import org.sirio5.services.modellixml.modelliXML;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.TR;

/**
 * Cache degli oggetti wrapper creati da Rigel.
 * Questa cache viene conservata in sessione.
 * Deve essere diversa per ogni utente.
 * Questa versione viene utilizzata nelle maschere
 * rigel utilizzate con Turbine.
 *
 * @author Nicola De Nisco
 */
public class CoreTurbineWrapperCache extends WrapperCacheBase
{
  // gestore modelli xml
  private modelliXML mdl = (modelliXML) (TurbineServices.getInstance().getService(modelliXML.SERVICE_NAME));

  /**
   * Inizializzazione di questa cache oggetti rigel.
   * @param data dati della richiesta
   */
  public void init(RunData data)
  {
    tagTabelleForm = TR.getString("tag.tabelle.form", "TABLE WIDTH=\"100%\""); // NOI18N
    tagTabelleList = TR.getString("tag.tabelle.list", "TABLE WIDTH=\"100%\""); // NOI18N
    i18n = new RigelHtmlI18n((CoreRunData) data);

    basePath = new String[]
    {
      "org.sirio2.rigel.table" // NOI18N
    };

    wrpBuilder = mdl;
  }

  @Override
  public PeerAppMaintFormTable buildDefaultTableForm()
  {
    return new PeerAppMaintFormTable();
  }

  @Override
  public PeerObjectSaver buildDefaultSaver()
  {
    return new CoreObjectSaver();
  }

  @Override
  public AlternateColorTableAppBase buildDefaultTableList()
  {
    return new AlternateColorTableAppBase();
  }

  @Override
  public HeditTableApp buildDefaultTableEdit()
  {
    return new HeditTableApp();
  }
}
