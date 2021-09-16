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
package org.sirio5.services.print;

/**
 * Descrittore dei parametri minimale.
 * Viene utilizzato nella stampa diretta, quando non si fa riferimento
 * a descrittori di stampe e parametri del database.
 *
 * @author Nicola De Nisco
 */
public class DirectReportParametersInfo extends AbstractReportParametersInfo
{
  private String reportName, reportInfo;

  public DirectReportParametersInfo(String reportName, String reportInfo)
  {
    this.reportName = reportName;
    this.reportInfo = reportInfo;
  }

  @Override
  public ParameterInfo buildParametro(String nomeParametro, Class tipoParametro)
     throws Exception
  {
    ParameterInfo rv = new ParameterInfo();
    rv.setNome(nomeParametro);
    rv.setDescrizione(nomeParametro);
    rv.setTipo(tipoParametro);
    return rv;
  }

  @Override
  public String getPlugin()
  {
    return "Jasper";
  }

  @Override
  public String getNome()
  {
    return reportName;
  }

  @Override
  public String getInfo()
  {
    return reportInfo;
  }
}
