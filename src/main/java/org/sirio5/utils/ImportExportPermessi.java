/*
 * Copyright (C) 2021 Nicola De Nisco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either ev 2
 * of the License, or (at your option) any later ev.
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
package org.sirio5.utils;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.QueryDataSet;
import com.workingdogs.village.Record;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;
import org.commonlib5.lambda.LEU;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.rigel5.RigelI18nInterface;
import org.rigel5.db.DbUtils;
import org.sirio5.ErrorMessageException;
import org.sirio5.rigel.RigelDefaultI18n;

/**
 * Utility per l'import e l'export di permessi.
 *
 * @author Nicola De Nisco
 */
public class ImportExportPermessi
{
  protected final RigelI18nInterface i18n;
  protected final Connection dbCon;
  protected List<Record> prevUsers, prevRoles, prevPerms, prevGroup, grantUserRole, grantRolePerm;
  protected String version;

  public ImportExportPermessi(Connection dbCon)
     throws Exception
  {
    this.dbCon = dbCon;
    this.i18n = new RigelDefaultI18n();

    fetchData();
  }

  public ImportExportPermessi(Connection dbCon, RigelI18nInterface i18n)
     throws Exception
  {
    this.dbCon = dbCon;
    this.i18n = i18n;

    fetchData();
  }

  protected void fetchData()
     throws Exception
  {
    prevUsers = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_user");
    prevRoles = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_role");
    prevPerms = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_permission");
    prevGroup = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_group");

    grantUserRole = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_user_group_role");
    grantRolePerm = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_role_permission");
  }

  /**
   * Importa da un file XML ruoli, permessi e relative associazioni.
   * Il file deve essere stato generato con la funzione esportaSuFileXML.
   * Nel file può apparire un tag [ev]0.0.0[/ev] che può essere
   * confrontato con testVersion per verificare se effettuare o meno l'import;
   * l'import avverrà solo se la versione del file è superiore a quella indicata
   * in testVersion.
   *
   * @param inputStream stream del file
   * @param removeOlds se vero rimuove vecchi ruoli, permessi e relative associazioni.
   * @param testVersion confronta versione prima dell'import (può essere null)
   * @return vero se l'importazione è stata eseguita
   * @throws Exception
   */
  public boolean importDaFileXML(InputStream inputStream, boolean removeOlds, String testVersion)
     throws Exception
  {
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(inputStream);
    Element root = doc.getRootElement();
    if(root == null)
      throw new ErrorMessageException(i18n.msg("File XML non valido: nessun root document."));

    Element roles = root.getChild("roles");
    Element permissions = root.getChild("permissions");
    Element grants = root.getChild("grants");

    if(roles == null || permissions == null || grants == null)
      throw new ErrorMessageException(i18n.msg("File XML non valido: sezioni roles, permissions, grants sono obbligatorie."));

    Element ev = root.getChild("version");
    if(ev != null)
    {
      version = SU.okStrNull(ev.getText());
      if(testVersion != null && SU.compareVersion(version, testVersion, '.') <= 0)
        return false;
    }

    // mappa degli utenti, gruppi e ruoli
    List<Record> prev = null;

    if(removeOlds)
    {
      prev = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_user_group_role_view");

      // rimuove ruoli e permessi esistenti
      DbUtils.executeStatement("DELETE FROM turbine_user_group_role", dbCon);
      DbUtils.executeStatement("DELETE FROM turbine_role_permission", dbCon);
      DbUtils.executeStatement("DELETE FROM turbine_role", dbCon);
      DbUtils.executeStatement("DELETE FROM turbine_permission", dbCon);

      prevRoles.clear();
      prevPerms.clear();
      grantRolePerm.clear();
      grantUserRole.clear();
    }

    String sSQL
       = "INSERT INTO turbine_role(\n"
       + "	role_id, role_name)\n"
       + "	VALUES (?, ?)";

    // carica tutti i ruoli
    try(PreparedStatement ps = dbCon.prepareStatement(sSQL))
    {
      List lsRole = roles.getChildren("role");
      for(Iterator itRole = lsRole.iterator(); itRole.hasNext();)
      {
        try
        {
          Element eRole = (Element) itRole.next();
          String roleName = SU.okStrNull(eRole.getAttributeValue("name"));

          if(roleName == null)
            continue;

          if(findRole(roleName) != null)
            continue;

          ps.setLong(1, DbUtils.getMaxField("turbine_role", "role_id", dbCon) + 1);
          ps.setString(2, roleName);
          ps.executeUpdate();
        }
        catch(Exception e1)
        {
          e1.printStackTrace();
        }
      }
    }

    sSQL
       = "INSERT INTO turbine_permission(\n"
       + "	permission_id, permission_name)\n"
       + "	VALUES (?, ?)";

    // carica tutti i permessi
    try(PreparedStatement ps = dbCon.prepareStatement(sSQL))
    {
      List lsPermission = permissions.getChildren("permission");
      for(Iterator itPermission = lsPermission.iterator(); itPermission.hasNext();)
      {
        try
        {
          Element ePermission = (Element) itPermission.next();
          String permissionName = SU.okStrNull(ePermission.getAttributeValue("name"));

          if(permissionName == null)
            continue;

          if(findPermission(permissionName) != null)
            continue;

          ps.setLong(1, DbUtils.getMaxField("turbine_permission", "permission_id", dbCon) + 1);
          ps.setString(2, permissionName);
          ps.executeUpdate();
        }
        catch(Exception e1)
        {
          e1.printStackTrace();
        }
      }
    }

    // aggiorna con i nuovi valori inseriti
    prevRoles = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_role");
    prevPerms = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_permission");

    sSQL
       = "INSERT INTO turbine_role_permission(\n"
       + "	role_id, permission_id)\n"
       + "	VALUES (?, ?)";

    // associa ruoli e permessi
    try(PreparedStatement ps = dbCon.prepareStatement(sSQL))
    {
      List lsGrant = grants.getChildren("grant");
      for(Iterator itGrant = lsGrant.iterator(); itGrant.hasNext();)
      {
        Element eGrant = (Element) itGrant.next();
        String roleName = eGrant.getAttributeValue("role");
        String permName = eGrant.getAttributeValue("permission");
        Record tr = findRole(roleName);
        Record tp = findPermission(permName);
        if(tr != null && tp != null)
        {
          int idRole = tr.getValue("role_id").asInt();
          int idPerm = tp.getValue("permission_id").asInt();

          if(!checkRolePermission(idRole, idPerm))
          {
            ps.setInt(1, idRole);
            ps.setInt(2, idPerm);
            ps.executeUpdate();
          }
        }
      }
    }

    // aggiorna con i nuovi valori inseriti
    grantUserRole = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_user_group_role");
    grantRolePerm = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_role_permission");

    if(removeOlds)
    {
      sSQL
         = "INSERT INTO turbine_user_group_role(\n"
         + "	user_id, group_id, role_id)\n"
         + "	VALUES (?, ?, ?)";

      try(PreparedStatement ps = dbCon.prepareStatement(sSQL))
      {
        for(Record r : prev)
        {
          String userName = r.getValue("j0_login_name").asString();
          String gropName = r.getValue("j1_group_name").asString();
          String roleName = r.getValue("j2_role_name").asString();

          Record user = findUser(userName);
          Record grop = findGroup(gropName);
          Record role = findRole(roleName);

          if(user == null || grop == null || role == null)
            continue;

          ps.setInt(1, user.getValue("user_id").asInt());
          ps.setInt(2, grop.getValue("group_id").asInt());
          ps.setInt(3, role.getValue("role_id").asInt());
          ps.executeUpdate();
        }
      }

      grantUserRole = QueryDataSet.fetchAllRecords(dbCon, "SELECT * FROM turbine_user_group_role");
    }

    return true;
  }

  /**
   * Esporta permessi in un file XML.
   * Nel file può apparire un tag [ev]0.0.0[/ev] che verrà
   * popolato con il contenuto di signVersion.
   *
   * @param os stream per l'output del file
   * @param signVersion eventuale indicatore di versione (può essere null)
   * @throws Exception
   */
  public void esportaSuFileXML(OutputStream os, String signVersion)
     throws Exception
  {
    // creazione documento XML di output
    Document docOutput = new Document(new Element("data"));

    if(signVersion != null)
      docOutput.getRootElement().addContent(new Element("version").addContent(signVersion));

    docOutput.getRootElement().addContent(genRoles());
    docOutput.getRootElement().addContent(genPermissions());
    docOutput.getRootElement().addContent(genGrants());

    // output del documento
    XMLOutputter xout = new XMLOutputter();
    xout.setFormat(Format.getPrettyFormat());

    xout.output(docOutput, os);
  }

  protected Element genRoles()
     throws Exception
  {
    Element rv = new Element("roles");
    for(Record r : prevRoles)
    {
      Element e = new Element("role");
      e.setAttribute("name", r.getValue("role_name").asString());
      rv.addContent(e);
    }
    return rv;
  }

  protected Element genPermissions()
     throws Exception
  {
    Element rv = new Element("permissions");
    for(Record r : prevPerms)
    {
      Element e = new Element("permission");
      e.setAttribute("name", r.getValue("permission_name").asString());
      rv.addContent(e);
    }
    return rv;
  }

  protected Element genGrants()
     throws Exception
  {
    Element rv = new Element("grants");
    for(Record r : grantRolePerm)
    {
      int idRole = r.getValue("role_id").asInt();
      int idPerm = r.getValue("permission_id").asInt();

      Record role = getRole(idRole);
      Record perm = getPermission(idPerm);

      Element e = new Element("grant");
      e.setAttribute("role", role.getValue("role_name").asString());
      e.setAttribute("permission", perm.getValue("permission_name").asString());
      rv.addContent(e);
    }
    return rv;
  }

  public Record getGroup(int idGroup)
     throws DataSetException
  {
    return prevGroup.stream()
       .filter(LEU.rethrowPredicate((g) -> idGroup == g.getValue("group_id").asInt()))
       .findFirst().orElse(null);
  }

  public Record findGroup(String groupName)
     throws DataSetException
  {
    return prevGroup.stream()
       .filter(LEU.rethrowPredicate((g) -> SU.isEqu(groupName, g.getValue("group_name").asString())))
       .findFirst().orElse(null);
  }

  public Record getUser(int idUser)
     throws DataSetException
  {
    return prevUsers.stream()
       .filter(LEU.rethrowPredicate((g) -> idUser == g.getValue("user_id").asInt()))
       .findFirst().orElse(null);
  }

  public Record findUser(String loginName)
     throws DataSetException
  {
    return prevUsers.stream()
       .filter(LEU.rethrowPredicate((g) -> SU.isEqu(loginName, g.getValue("login_name").asString())))
       .findFirst().orElse(null);
  }

  public Record getRole(int idRole)
     throws DataSetException
  {
    return prevRoles.stream()
       .filter(LEU.rethrowPredicate((g) -> idRole == g.getValue("role_id").asInt()))
       .findFirst().orElse(null);
  }

  public Record findRole(String roleName)
     throws DataSetException
  {
    return prevRoles.stream()
       .filter(LEU.rethrowPredicate((g) -> SU.isEqu(roleName, g.getValue("role_name").asString())))
       .findFirst().orElse(null);
  }

  public Record getPermission(int idPermission)
     throws DataSetException
  {
    return prevPerms.stream()
       .filter(LEU.rethrowPredicate((g) -> idPermission == g.getValue("permission_id").asInt()))
       .findFirst().orElse(null);
  }

  public Record findPermission(String permissionName)
     throws DataSetException
  {
    return prevPerms.stream()
       .filter(LEU.rethrowPredicate((g) -> SU.isEqu(permissionName, g.getValue("permission_name").asString())))
       .findFirst().orElse(null);
  }

  public boolean checkRolePermission(int idRole, int idPermission)
     throws DataSetException
  {
    return grantRolePerm.stream()
       .anyMatch(LEU.rethrowPredicate((g)
          -> (idRole == g.getValue("role_id").asInt()
       && idPermission == g.getValue("permission_id").asInt())));
  }

  public boolean checkUserGroupRole(int idUser, int idGroup, int idRole)
     throws DataSetException
  {
    return grantUserRole.stream()
       .anyMatch(LEU.rethrowPredicate((g)
          -> (idUser == g.getValue("user_id").asInt()
       && idGroup == g.getValue("group_id").asInt()
       && idRole == g.getValue("role_id").asInt())));
  }

  public String getVersion()
  {
    return version;
  }
}
