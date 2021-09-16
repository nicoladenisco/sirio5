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
package org.sirio5.beans.xml;

import java.io.Writer;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.rigel5.table.peer.xml.*;
import org.rigel5.table.xml.*;
import org.sirio5.services.bus.BUS;
import org.sirio5.services.bus.BusContext;
import org.sirio5.services.bus.BusMessages;
import org.sirio5.services.bus.MessageBusListener;
import org.sirio5.services.localization.INT;
import org.sirio5.services.modellixml.modelliXML;

public class jslistaxmlBean implements HttpSessionBindingListener, MessageBusListener
{
  private static final Log log = LogFactory.getLog(jslistaxmlBean.class);

  private String type;
  private String sid;
  private final Hashtable htListe = new Hashtable();
  private org.apache.turbine.om.security.User us;
  private HttpServletRequest request;
  private HttpSession session;

  public jslistaxmlBean()
     throws Exception
  {
  }

  protected PeerWrapperListaXml creaLista()
     throws Exception
  {
    PeerWrapperListaXml wl = ((modelliXML) (TurbineServices.getInstance().
       getService(modelliXML.SERVICE_NAME))).getListaXmlPeer(type);
    wl.setXtbl(new xTable());
    wl.init();
    log.debug((INT.I("Creato nuovo PeerWrapperListaXml ")) + type);
    return wl;
  }

  protected PeerWrapperListaXml getLista()
     throws Exception
  {
    PeerWrapperListaXml wl = (PeerWrapperListaXml) (htListe.get(type));
    if(wl == null)
    {
      wl = creaLista();
      htListe.put(type, wl);
    }
    return wl;
  }

  public String getHeader()
     throws Exception
  {
    return getLista().getHeader();
  }

  public String getTitolo()
     throws Exception
  {
    return getLista().getTitolo();
  }

  public void getXml(Writer out)
     throws Exception
  {
    // annulla limite affinche' vengano
    // restituiti tutti i records
    getLista().setLimit(0);

    getLista().getXml(out);
  }

  public boolean checkPermessi(HttpSession sessione)
     throws Exception
  {
// TODO: implementare
//    String permessi = getLista().getPermessi();
//    return permessi == null ? true : CaleidoSecurity.checkPermission(sessione, permessi);
    return true;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public void setSid(String sid)
  {
    this.sid = sid;
  }

  public String getSid()
  {
    return sid;
  }

  public org.apache.turbine.om.security.User getUser(HttpServletRequest request)
     throws Exception
  {
    this.request = request;
    this.session = request.getSession();

    us = (User) session.getAttribute("turbine.user");//NOI18N
    if(us == null)
      throw new Exception((INT.I("Utente non autorizzato! Eseguire la logon con un utente valido.")));

    if(type == null)
      throw new Exception(INT.I("Parametri insufficienti."));

    if(!checkPermessi(this.session))
      throw new Exception(INT.I("Permessi insufficienti per i dati richiesti. Contattare l'amministratore."));

    return us;
  }

  @Override
  public void valueBound(HttpSessionBindingEvent hsbe)
  {
    BUS.registerEventListner(this);
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent hsbe)
  {
    BUS.removeEventListner(this);
  }

  @Override
  public int message(int msgID, Object originator, BusContext context)
     throws Exception
  {
    switch(msgID)
    {
      case BusMessages.RIGEL_XML_LIST_RELOADED:
        synchronized(this)
        {
          htListe.clear();
        }
        break;
    }

    return 0;
  }
}
