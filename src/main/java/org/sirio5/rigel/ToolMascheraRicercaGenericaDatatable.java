/*
 * Copyright (C) 2024 Nicola De Nisco
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

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rigel5.RigelI18nInterface;
import org.rigel5.exceptions.InjectionDetectedException;
import org.rigel5.table.BuilderRicercaGenerica;
import org.rigel5.table.MascheraRicercaGenerica;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.RigelHtmlPage;
import org.sirio5.utils.SU;

/**
 * Generatore filtro per datatable.
 *
 * @author Nicola De Nisco
 */
public class ToolMascheraRicercaGenericaDatatable implements MascheraRicercaGenerica
{
  /** Logging */
  private static final Log log = LogFactory.getLog(ToolMascheraRicercaGenericaDatatable.class);
  protected BuilderRicercaGenerica brg = null;
  protected RigelTableModel rtm = null;
  protected String formName = "fo";
  protected RigelI18nInterface i18n = null;

  @Override
  public void init(BuilderRicercaGenerica brg, RigelTableModel rtm, RigelI18nInterface i18n)
  {
    this.brg = brg;
    this.rtm = rtm;
    this.i18n = i18n;
    formName = rtm.getFormName();
  }

  @Override
  public Object buildCriteriaSafe(Map params)
     throws Exception
  {
    return null;
  }

  @Override
  public void buildHtmlRicerca(String nomeForm, RigelHtmlPage page)
     throws Exception
  {
  }

  @Override
  public void buildHtmlRicercaSemplice(String nomeForm, int sizeFld, boolean haveFilter, RigelHtmlPage page)
     throws Exception
  {
  }

  /**
   * Costruisce il criterio di selezione in base ai parametri
   * impostati. Questa funzione in realta' e' solo un frontend
   * al metodo buildCriteria del BuilderRicercaGenerica.
   * @param search
   * @param mapOrder
   * @return un oggetto di selezione (vedi BuilderRicercaGenerica)
   * @throws Exception
   */
  protected Object buildCriteria(String search, Map<Integer, Integer> mapOrder)
     throws Exception
  {
    search = SU.okStrNull(search);

    for(int i = 0; i < rtm.getColumnCount(); i++)
    {
      RigelColumnDescriptor cd = rtm.getColumn(i);

      cd.setFiltroTipo(0);
      cd.setFiltroValore(null);
      cd.setFiltroSort(mapOrder.getOrDefault(i, 0));

      if(cd.isEscludiRicerca() || cd.getRicercaSemplice() == 0)
        continue;

      if(search != null)
      {
        cd.setFiltroTipo(1); // contiene
        cd.setFiltroValore(search);
      }
    }

    return brg.buildCriteria();
  }

  public Object buildCriteriaSafe(String search, Map<Integer, Integer> mapOrder)
     throws Exception
  {
    try
    {
      return buildCriteria(search, mapOrder);
    }
    catch(InjectionDetectedException ex)
    {
      throw ex;
    }
    catch(Exception ex)
    {
      log.error("RIGEL:", ex);
      return null;
    }
  }
}
