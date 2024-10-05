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
package org.sirio5.rigel.customedit;

import java.util.Map;
import javax.swing.table.TableModel;
import org.apache.torque.map.ColumnMap;
import org.jdom2.Element;
import org.rigel5.RigelI18nInterface;
import org.rigel5.db.torque.TableMapHelper;
import org.rigel5.exceptions.XmlSyntaxException;
import org.rigel5.table.CustomColumnEdit;
import org.rigel5.table.RigelColumnDescriptor;
import org.rigel5.table.peer.PeerAbstractTableModel;
import org.sirio5.ErrorMessageException;
import org.sirio5.utils.SU;

/**
 * Custom edit per limitare il numero di caratteri immessi.
 * Viene di solito utilizzato per i campi stringa per imporre
 * un limite massimo di caratteri.
 *
 * @author Nicola De Nisco
 */
public class LimitaDimensioniEdit implements CustomColumnEdit
{
  private int len = 0;
  private String maxLen, tipo, msg;

  @Override
  public void init(Element eleXML)
     throws Exception
  {
    maxLen = SU.okStr(eleXML.getAttributeValue("max"), "AUTO");
    tipo = SU.okStr(eleXML.getAttributeValue("tipo"), "TRONCA");
    msg = SU.okStr(eleXML.getAttributeValue("msg"), "");
  }

  @Override
  public boolean haveCustomHtml()
  {
    return false;
  }

  @Override
  public String getHtmlEdit(RigelColumnDescriptor cd, TableModel model, int row, int col, String cellText, String cellHtml, String nomeCampo, RigelI18nInterface i18n)
     throws Exception
  {
    return cellText;
  }

  @Override
  public boolean haveCustomParser()
  {
    return true;
  }

  @Override
  public Object parseValue(RigelColumnDescriptor cd, TableModel model, int row, int col,
     String formattedValue, String nomeCampo, String oldValue, Map params, RigelI18nInterface i18n)
     throws Exception
  {
    if(len == 0)
    {
      TableMapHelper tm = new TableMapHelper(((PeerAbstractTableModel) model).getTableMap());
      ColumnMap cm = tm.getCampo(cd.getName());
      if(cm == null)
        throw new Exception(i18n.msg("La colonna %s non esiste nel table model.", cd.getName()));

      if(!(cm.getType() instanceof String))
        throw new XmlSyntaxException(i18n.msg("La colonna %s non è un tipo riconducibile a stringa.", cd.getName()));

      switch(maxLen.toUpperCase())
      {
        case "AUTO":
          if(!(model instanceof PeerAbstractTableModel))
            throw new XmlSyntaxException(i18n.msg("Valore %s per attributo 'max' è consentito solo per forms (PeerAbstractTableModel).", maxLen));

          // recupera l'informazione sulla dimensione del campo attraverso il tablemap
          if((len = cm.getSize()) == 0)
            throw new XmlSyntaxException(i18n.msg("La dimensione della colonna %s non può essere zero.", cd.getName()));
          break;

        default:
          if((len = SU.parseInt(maxLen)) == 0)
            throw new XmlSyntaxException(i18n.msg("Valore %s per attributo 'max' non è consentito.", maxLen));
      }

      if(msg.length() > len)
        throw new XmlSyntaxException(i18n.msg("Valore %s per attributo 'msg' eccede la dimensione %d richiesta.",
           msg, len));
    }

    switch(tipo.toUpperCase())
    {
      case "TRUNK":
      case "TRONCA":
        if(formattedValue.length() > len)
          formattedValue = formattedValue.substring(0, len - msg.length()) + msg;
        break;

      case "ERROR":
      case "ERRORE":
      case "EXCEPTION":
        if(formattedValue.length() > len)
          throw new ErrorMessageException(
             i18n.msg("Il valore %s eccede la dimensione massima consentita di %d caratteri per il campo %s.",
                formattedValue, len, cd.getCaption()));
        break;

      default:
        throw new XmlSyntaxException(i18n.msg("Valore %s per attributo 'tipo' non è consentito.", tipo));
    }

    return formattedValue;
  }
}
