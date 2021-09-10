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
package org.sirio2.services.formatter;

import java.text.*;
import java.util.*;

import org.sirio2.services.AbstractCoreBaseService;
import org.apache.commons.logging.*;
import org.sirio2.utils.SU;

public class ItalianNumFormatter extends AbstractCoreBaseService
   implements NumFormatter
{
  /** Logging */
  private static Log log = LogFactory.getLog(ItalianNumFormatter.class);
  private DecimalFormatSymbols dfs = new DecimalFormatSymbols();
  private Hashtable htFormats = new Hashtable();

  @Override
  public void coreInit()
     throws Exception
  {
    dfs.setDecimalSeparator(',');
    dfs.setGroupingSeparator('.');
  }

  /**
   * Crea un decimal format con qualcosa del tipo
   * ###.###.###.###.###.#00,00000 il numero di
   * zeri a sinistra della virgola e' ni mentre
   * quello a destra e' nd.
   * Il formato viene bufferato in una hashtable
   * per ottimizzare le prestazioni in caso di recupero.
   * @param ni numero di interi fissi
   * @param nd numero di decimali fissi
   * @return
   */
  protected DecimalFormat createFormat(int ni, int nd)
  {
    String key = ni + "_" + nd;
    DecimalFormat formato = (DecimalFormat) htFormats.get(key);
    if(formato != null)
      return formato;

    // costruisce rf sostituendo ai # tanti
    // zeri a cominciare da destra secondo ni
    String rf = "###.###.###.###.###.##0";
    if(ni > 0)
    {
      char arRf[] = rf.toCharArray();
      for(int i = arRf.length - 1; i >= 0 && ni > 0; i--)
      {
        if(arRf[i] == '#')
        {
          arRf[i] = '0';
          ni--;
        }
      }
      rf = new String(arRf);
    }

    // aggiunge virgola e parte decimale
    if(nd > 0)
    {
      rf += ',' + SU.GetZeroes(nd);
    }

    // crea e salva nella cache il formato
    formato = new DecimalFormat();
    formato.setDecimalFormatSymbols(dfs);
    formato.applyLocalizedPattern(rf);
    htFormats.put(key, formato);

    return formato;
  }

  @Override
  public DecimalFormatSymbols getSymbols()
  {
    return dfs;
  }

  @Override
  public String format(double val, int nInt, int nDec)
     throws Exception
  {
    if(nDec == 0)
    {
      // il significato nDec == 0 non è senza virgola,
      // ma con virgola se serve e numero di cifre decimali
      // libero all'occorrenza
      DecimalFormat df = createFormat(nInt, 6);

      int i;
      char ac[] = df.format(val).toCharArray();
      for(i = ac.length - 1; i > 0; i--)
      {
        if(ac[i] == ',')
        {
          i--;
          break;
        }

        if(ac[i] != '0')
          break;
      }

      return new String(ac, 0, i + 1);
    }
    else
    {
      DecimalFormat df = createFormat(nInt, nDec);
      return df.format(val);
    }
  }

  @Override
  public double parseDouble(String source, int nInt, int nDec)
     throws Exception
  {
    DecimalFormat df = createFormat(nInt, 6);
    return Utils.parseItalianStyle(source, df);
  }
}
