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
package org.sirio5.rigel.validators;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.torque.criteria.Criteria;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.map.TableMap;
import org.apache.torque.om.ColumnAccessByName;
import org.apache.torque.om.Persistent;
import org.jdom2.Element;
import org.rigel5.RigelI18nInterface;
import org.rigel5.db.torque.TableMapHelper;
import org.rigel5.glue.validators.PostParseValidator;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.html.hEditTable;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.SU;

/**
 * Validatore che esegue una undo di cancellazione logica.
 *
 * @author Nicola De Nisco
 */
public class UndoLogicalDeleteValidator implements PostParseValidator
{
  private List<Element> elKeys = null;
  private List<Element> elUnique = null;

  @Override
  public void init(Element eleXML)
     throws Exception
  {
    elKeys = eleXML.getChildren("alternate-key");
    elUnique = eleXML.getChildren("unique");
  }

  @Override
  public boolean validate(Object __obj,
     RigelTableModel tableModel, hEditTable table,
     int row, HttpSession session, Map param,
     RigelI18nInterface i18n, Connection dbCon, Map custom)
     throws Exception
  {
    if(__obj instanceof Persistent)
    {
      Persistent obj = (Persistent) __obj;

      if(obj.isNew()
         && ((elKeys != null && !elKeys.isEmpty()) || (elUnique != null && !elUnique.isEmpty())))
      {
        Class peerClass = getClassByObject(obj);
        Method msel = peerClass.getMethod("doSelect", Criteria.class, Connection.class);
        Method mtmp = peerClass.getMethod("getTableMap");

        TableMap tm = (TableMap) mtmp.invoke(null);
        TableMapHelper tmTo = new TableMapHelper(tm);

        for(Element eKey : elKeys)
        {
          if(testForUndo1(eKey, obj, msel, tmTo, dbCon))
            break;
        }

        for(Element eKey : elUnique)
        {
          if(testForUndo2(eKey, obj, msel, tmTo, dbCon))
            break;
        }
      }
    }

    return true;
  }

  /**
   * Verifica per undo.
   * Sintassi alternate-key/field.
   * @param eKey
   * @param obj
   * @param m
   * @param dbCon
   * @return
   * @throws Exception
   */
  private boolean testForUndo1(Element eKey, Object obj, Method m, TableMapHelper tmTo, Connection dbCon)
     throws Exception
  {
    List<Element> elFields = eKey.getChildren("field");
    if(elFields == null || elFields.isEmpty())
      throw new Exception(INT.I("Tag alternate-key/field non trovato; rivedere liste.xml."));

    ArrayList<String> fields = new ArrayList<>();

    for(Element eField : elFields)
    {
      String nomeCampo = eField.getTextTrim();
      if(!nomeCampo.isEmpty())
        fields.add(nomeCampo);
    }

    if(fields.isEmpty())
      return false;

    return testForUndo(fields, obj, m, tmTo, dbCon);
  }

  /**
   * Verifica per undo.
   * Sintassi unique/unique-column (come da schema).
   * @param eKey
   * @param obj
   * @param m
   * @param dbCon
   * @return
   * @throws Exception
   */
  private boolean testForUndo2(Element eKey, Object obj, Method m, TableMapHelper tmTo, Connection dbCon)
     throws Exception
  {
    List<Element> elFields = eKey.getChildren("unique-column");
    if(elFields == null || elFields.isEmpty())
      throw new Exception(INT.I("Tag unique/unique-column non trovato; rivedere liste.xml."));

    ArrayList<String> fields = new ArrayList<>();

    for(Element eField : elFields)
    {
      String nomeCampo = eField.getAttributeValue("name");
      if(!nomeCampo.isEmpty())
        fields.add(nomeCampo);
    }

    if(fields.isEmpty())
      return false;

    return testForUndo(fields, obj, m, tmTo, dbCon);
  }

  private boolean testForUndo(List<String> uniqueCol, Object obj, Method m, TableMapHelper tmTo, Connection dbCon)
     throws Exception
  {
    Criteria c = new Criteria();
    ColumnAccessByName cab = (ColumnAccessByName) obj;

    for(String nomeCampo : uniqueCol)
    {
      ColumnMap cmap = tmTo.getCampo(nomeCampo);
      if(cmap == null)
        throw new Exception(INT.I("Campo %s non trovato in alternate-key/field; rivedere liste.xml.", nomeCampo));

      c.and(cmap.getFullyQualifiedName(), cab.getByName(cmap.getJavaName()));
    }

    List lsFound = (List) m.invoke(null, c, dbCon);
    if(lsFound.size() != 1)
      return false;

    return mergeObject(obj, lsFound.get(0));
  }

  private Class getClassByObject(Object obj)
     throws ClassNotFoundException
  {
    String className = obj.getClass().getName() + "Peer";
    return Class.forName(className);
  }

  private boolean mergeObject(Object obj, Object slave)
     throws Exception
  {
    ColumnAccessByName cabObj = (ColumnAccessByName) obj;
    ColumnAccessByName cabSlave = (ColumnAccessByName) slave;

    if(SU.parse(cabSlave.getByName("StatoRec"), 0) < 10)
      return false;

    cabObj.setByName("Creazione", cabSlave.getByName("Creazione"));
    cabObj.setByName("IdUser", cabSlave.getByName("IdUser"));
    cabObj.setByName("IdUcrea", cabSlave.getByName("IdUcrea"));
    cabObj.setByName("UltModif", cabSlave.getByName("UltModif"));

    Persistent perObj = (Persistent) obj;
    Persistent perSlave = (Persistent) slave;

    perObj.setPrimaryKey(perSlave.getPrimaryKey());
    perObj.setNew(false);
    return true;
  }
}
