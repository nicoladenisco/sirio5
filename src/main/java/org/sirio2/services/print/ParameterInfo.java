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
package org.sirio2.services.print;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.commonlib5.utils.Pair;
import org.sirio2.utils.DT;
import org.sirio2.utils.SU;

/**
 * Entry di filtro stampe.
 *
 * @author Nicola De Nisco.
 */
public class ParameterInfo
{
  private String nome = "";
  private String nomeGemello = "";
  private String descrizione = "";
  private List<Pair<String, String>> listaValori = new ArrayList<>();
  private Object valoreDefault = "";
  private Class tipo = null;
  private boolean intervalloInizio = false;
  private boolean intervalloFine = false;
  private boolean hidden = false;

  /**
   * Creates a new instance of ParameterInfo
   */
  public ParameterInfo()
  {
  }

  public String getNome()
  {
    return nome;
  }

  public void setNome(String nome)
  {
    this.nome = nome;
  }

  public String getDescrizione()
  {
    return descrizione;
  }

  public void setDescrizione(String descrizione)
  {
    this.descrizione = descrizione;
  }

  public List<Pair<String, String>> getListaValori()
  {
    return listaValori;
  }

  public void addListaValori(String chiave, String descrizione)
  {
    listaValori.add(new Pair<>(chiave, descrizione));
  }

  public Object getValoreDefault()
  {
    return valoreDefault;
  }

  public void setValoreDefault(Object valore)
  {
    this.valoreDefault = valore;
  }

  public Class getTipo()
  {
    return tipo;
  }

  public void setTipo(Class tipo)
  {
    this.tipo = tipo;
  }

  public boolean isDate()
  {
    return tipo.equals(java.util.Date.class)
       || tipo.equals(java.sql.Date.class);
  }

  public boolean isTimeStamp()
  {
    return tipo.equals(java.sql.Timestamp.class);
  }

  public boolean isInteger()
  {
    return tipo.equals(java.lang.Integer.class);
  }

  public boolean isNumber()
  {
    return tipo.equals(java.lang.Number.class);
  }

  public boolean isBoolean()
  {
    return tipo.equals(java.lang.Boolean.class);
  }

  public boolean isIntervalloInizio()
  {
    return intervalloInizio;
  }

  public void setIntervalloInizio(boolean intervalloInizio)
  {
    this.intervalloInizio = intervalloInizio;
  }

  public boolean isIntervalloFine()
  {
    return intervalloFine;
  }

  public void setIntervalloFine(boolean intervalloFine)
  {
    this.intervalloFine = intervalloFine;
  }

  public boolean isHidden()
  {
    return hidden;
  }

  public void setHidden(boolean hidden)
  {
    this.hidden = hidden;
  }

  public String getNomeGemello()
  {
    return nomeGemello;
  }

  public void setNomeGemello(String nomeGemello)
  {
    this.nomeGemello = nomeGemello;
  }

  @Override
  public String toString()
  {
    return nome;
  }

  public Object parse(String val)
     throws Exception
  {
    Object obj = null;

    if(isDate() || isTimeStamp())
    {
      Date d = DT.parseData(val);
      obj = d;

      if(isIntervalloInizio())
      {
        obj = DT.inizioGiorno(d);
      }
      else if(isIntervalloFine())
      {
        obj = DT.fineGiorno(d);
      }

      if(isTimeStamp())
      {
        long time = ((Date) (obj)).getTime();
        obj = new Timestamp(time);
      }
    }
    else if(isInteger())
    {
      obj = Integer.valueOf(val);
    }
    else if(isBoolean())
    {
      obj = SU.checkTrueFalse(val);
    }
    else
    {
      obj = val;
    }

    return obj;
  }

  public String format(Object valDef)
  {
    String value = "";

    if(isDate() || isTimeStamp())
    {
      if(valDef != null)
      {
        if(valDef instanceof String)
        {
          value = (String) valDef;
        }
        else if(valDef instanceof Timestamp)
        {
          long ms = ((Timestamp) valDef).getTime();
          value = DT.formatData(new Date(ms));
        }
        else if(valDef instanceof Date)
        {
          Date d = (Date) valDef;
          value = DT.formatData(d);
        }
        else
        {
          value = DT.formatData(new Date());
        }
      }
      else
      {
        value = DT.formatData(new Date());
      }
    }
    else
      value = valDef.toString();

    return value;
  }
}
