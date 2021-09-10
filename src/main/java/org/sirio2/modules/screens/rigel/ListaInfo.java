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
package org.sirio2.modules.screens.rigel;

import java.util.Map;
import org.sirio2.utils.CoreRunData;

/**
 * Informazioni di lista salvate in sessione per diminuire
 * il numero di parametri richiesti nelle uri.
 *
 * @author Nicola De Nisco
 */
public class ListaInfo
{
  public String type;
  public String func;
  public String rigarif;
  public String chiudisel = "1";
  public String extraWhere = "";
  public Map<String, String> passThroughParam;
  public String urlNuovo;

  /**
   * Imposta i parametri passati.
   * @throws Exception
   */
  public static ListaInfo getFromSession(CoreRunData data)
     throws Exception
  {
    String stype = data.getParameters().getString("type",
       (String) data.getSession().getAttribute("li:type"));

    if(stype == null)
      throw new IllegalStateException(data.i18n("Il tipo lista non è rintracciabile; rivedere flusso."));

    ListaInfo li = (ListaInfo) data.getSession().getAttribute("li:" + stype);
    if(li == null)
      li = new ListaInfo();

    if(stype != null && (li.type == null || !stype.equals(li.type)))
    {
      // reset dei parametri memorizzati
      li.func = null;
      li.rigarif = null;
      li.chiudisel = "1";
      li.extraWhere = "";
      li.type = stype;
    }

    String tmp = null;
    if((tmp = data.getParameters().getString("func")) != null)
      li.func = tmp;
    if((tmp = data.getParameters().getString("rigarif")) != null)
      li.rigarif = tmp;
    if((tmp = data.getParameters().getString("chiudisel")) != null)
      li.chiudisel = tmp;
    if((tmp = data.getParameters().getString("extraw")) != null)
      li.extraWhere = tmp;

    data.getSession().setAttribute("li:type", stype);
    data.getSession().setAttribute("li:" + stype, li);
    return li;
  }
}
