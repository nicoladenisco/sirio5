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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.rigel5.glue.PeerObjectSaver;
import org.rigel5.glue.RecordObjectSaver;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.glue.table.AlternateColorTableAppBase;
import org.rigel5.glue.table.HeditTableApp;
import org.rigel5.glue.table.PeerAppMaintFormTable;
import org.rigel5.glue.table.SqlAppMaintFormTable;
import org.rigel5.table.RigelTableModel;
import org.sirio5.services.modellixml.modelliXML;
import org.sirio5.utils.CoreRunData;
import org.sirio5.utils.SirioMacroResolver;
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
  /** gestore modelli xml */
  protected final modelliXML mdl = (modelliXML) (TurbineServices.getInstance().getService(modelliXML.SERVICE_NAME));
  /** proprieta da passare nei table model */
  protected final Map<String, String> properties = new HashMap<>();
  protected User tuser;

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
      "org.sirio5.rigel.table" // NOI18N
    };

    wrpBuilder = mdl;
    tuser = data.getUser();
    properties.put("userid", tuser.getId().toString());
  }

  @Override
  public PeerAppMaintFormTable buildDefaultPeerTableForm()
  {
    return new PeerAppMaintFormTable();
  }

  @Override
  public SqlAppMaintFormTable buildDefaultSqlTableForm()
  {
    return new SqlAppMaintFormTable();
  }

  @Override
  public PeerObjectSaver buildDefaultPeerSaver()
  {
    return new CoreObjectSaver();
  }

  @Override
  public RecordObjectSaver buildDefaultRecordSaver()
  {
    return new CoreRecordObjectSaver();
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

  public Map<String, String> getProperties()
  {
    return Collections.unmodifiableMap(properties);
  }

  public void setProperties(Map<String, String> properties)
  {
    this.properties.clear();
    this.properties.putAll(properties);
  }

  public void addProperty(String chiave, String valore)
  {
    properties.put(chiave, valore);
  }

  public String getProperty(String chiave)
  {
    return properties.get(chiave);
  }

  public void clearProperty(String chiave)
  {
    properties.remove(chiave);
  }

  @Override
  public void populateTableModelProperties(RigelTableModel tm)
  {
    tm.setProperties(properties);
    SirioMacroResolver mr = new SirioMacroResolver(tuser);
    mr.putAll(properties);
    tm.getQuery().setMacroResolver(mr);
  }
}
