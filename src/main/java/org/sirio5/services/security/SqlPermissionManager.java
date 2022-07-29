/*
 * Copyright (C) 2022 Nicola De Nisco
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
package org.sirio5.services.security;

import com.workingdogs.village.Record;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.security.SecurityService;
import org.rigel5.db.DbUtils;
import org.rigel5.db.torque.PeerTransactAgent;

/**
 * Gestione permessi direttamente in SQL.
 *
 * @author Nicola De Nisco
 */
public class SqlPermissionManager extends PermissionManager
{
  private static final Log log = LogFactory.getLog(SqlPermissionManager.class);

  public static final String insertSQL
     = "INSERT INTO public.turbine_permission(\n"
     + "	permission_id, permission_name, objectdata)\n"
     + "	VALUES (?, ?, ?);";

  public static final String grantSQL
     = "INSERT INTO public.turbine_role_permission(\n"
     + "	role_id, permission_id)\n"
     + "	VALUES (?, ?);";

  public SqlPermissionManager(SecurityService security)
  {
    super(security);
  }

  @Override
  public void salvaPermesso(String permesso)
  {
    try
    {
      loadAdminRole();
      PeerTransactAgent.execute((con) -> salvaPermesso(permesso, con));
    }
    catch(Exception ex)
    {
      log.error("", ex);
    }
  }

  protected void salvaPermesso(String permesso, Connection con)
     throws Exception
  {
    Record rp = null;

    do
    {
      List<Record> lsRecs = DbUtils.executeQuery(
         "SELECT * FROM turbine_permission WHERE permission_name='" + permesso + "'", con);

      if(lsRecs.isEmpty())
      {
        insert(permesso, con);
        continue;
      }

      rp = lsRecs.get(0);
    }
    while(rp == null);

    grant(rp, con);
  }

  private void insert(String permesso, Connection con)
     throws Exception
  {
    try ( PreparedStatement ps = con.prepareStatement(insertSQL))
    {
      ps.setLong(1, DbUtils.getMaxField("public.turbine_permission", "permission_id", con) + 1);
      ps.setString(2, permesso);
      ps.setString(3, "");
      ps.executeUpdate();
    }
  }

  private void grant(Record permesso, Connection con)
     throws Exception
  {
    int permid = permesso.getValue("permission_id").asInt();

    List<Record> lsRecs = DbUtils.executeQuery("SELECT * "
       + "  FROM public.turbine_role_permission "
       + " WHERE role_id=" + adminRole.getId()
       + "   AND permission_id=" + permid, con);

    if(lsRecs.isEmpty())
    {
      try ( PreparedStatement ps = con.prepareStatement(grantSQL))
      {
        ps.setInt(1, (Integer) adminRole.getId());
        ps.setInt(2, permid);
        ps.executeUpdate();
      }
    }
  }
}
