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
package org.sirio5.services;

import com.workingdogs.village.Record;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Role;
import org.apache.torque.util.Transaction;
import org.commonlib5.lambda.LEU;
import org.commonlib5.utils.JavaLoggingToCommonLoggingRedirector;
import org.rigel5.db.DbUtils;
import org.sirio5.utils.SU;

/**
 * Controllo dei parametri fondamentali per il corretto funzionamento dell'applicazione.
 *
 * @author Nicola De Nisco
 */
public class CoreAppSanity
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreAppSanity.class);

  protected void sanityApplication(AbstractCoreBaseService service)
     throws Exception
  {
    sanitySystem(service);
    sanityJava(service);
    sanityDatabase(service);
    sanitySecurity(service);
    sanityScheduler(service);
  }

  /**
   * Effettua controlli sul sistema per evidenziare
   * situazioni non conformi al funzionamento dell'applicazione.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanitySystem(AbstractCoreBaseService service)
     throws Exception
  {
    // redirige il logger standard di Java all'interno di Log4j
    JavaLoggingToCommonLoggingRedirector.activate();

    // imposta il truststore di default
    CoreTlsManager.getInstance().initTLScomunication();

    log.info("sanitySystem superato");
  }

  /**
   * Effettua controlli sull'ambiente java per evidenziare
   * situazioni non conformi al funzionamento dell'applicazione.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityJava(AbstractCoreBaseService service)
     throws Exception
  {
    // controlla la presenza di java e javac nella path
    // di sistema: vengono utilizzati da sottoprogrammi
    // lanciati esternamente (tipo Jasper)

    boolean found = false;
    String path = System.getenv("PATH");
    String[] dirs = SU.split(path, File.pathSeparatorChar);
    for(int i = 0; i < dirs.length; i++)
    {
      log.debug("Test " + dirs[i] + " for java/javac");
      File fDir = new File(dirs[i]);
      if(!fDir.isDirectory())
        continue;

      File testJava = new File(fDir, "java");
      File testJavac = new File(fDir, "javac");
      if(testJava.exists() && testJavac.exists())
      {
        found = true;
        break;
      }

      File testJavaw = new File(fDir, "java.exe");
      File testJavacw = new File(fDir, "javac.exe");
      if(testJavaw.exists() && testJavacw.exists())
      {
        found = true;
        break;
      }
    }

    if(!found)
      throw new CoreServiceException("I programmi java e/o javac non sono presenti nella path.");

    log.info("sanityJava superato");
  }

  /**
   * Verifica e regolarizza il database.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityDatabase(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Verifica le impostazioni di sicurezza di base.
   * L'utente turbine deve esistere ed avere id=0.
   * Il ruolo turbine_root deve esistere ad avere id=1.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanitySecurity(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Verifica presenza e setup dei job dello scheduler.
   * @param service servizio che ha richiesto l'operazione
   * @throws Exception
   */
  protected void sanityScheduler(AbstractCoreBaseService service)
     throws Exception
  {
  }

  /**
   * Assegna tutti i permessi al ruolo indicato.
   * @param role ruolo amministratore
   * @throws Exception
   */
  protected void grantAllPermission(Role role)
     throws Exception
  {
    Connection connection = null;
    try
    {
      connection = Transaction.begin();

      if(grantAllPermission(role, connection))
        return;

      Transaction.commit(connection);
      connection = null;
    }
    finally
    {
      if(connection != null)
      {
        Transaction.safeRollback(connection);
      }
    }
  }

  private boolean grantAllPermission(Role role, Connection connection)
     throws Exception
  {
    String sSQL
       = "select permission_id\n"
       + "  from turbine_permission\n"
       + " where permission_id NOT IN ("
       + "    select permission_id from turbine_role_permission where role_id=" + role.getId() + ")";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, connection);
    if(lsRecs.isEmpty())
      return true;

    int[] permissionToGrant = lsRecs.stream()
       .mapToInt(LEU.rethrowFunctionInt((r) -> r.getValue(1).asInt()))
       .distinct().sorted().toArray();
    if(permissionToGrant.length == 0)
      return true;

    String sINS
       = "INSERT INTO turbine_role_permission(\n"
       + "	role_id, permission_id)\n"
       + "	VALUES (?, ?);";
    try ( PreparedStatement ps = connection.prepareStatement(sINS))
    {
      int roleid = (Integer) role.getId();
      for(int i = 0; i < permissionToGrant.length; i++)
      {
        int permid = permissionToGrant[i];

        ps.clearParameters();
        ps.setInt(1, roleid);
        ps.setInt(2, permid);
        ps.executeUpdate();
      }
    }

    return false;
  }

  /**
   * Crea ruoli se non esistono.
   * @param roleNames elenco di nomi di ruolo
   * @throws Exception
   */
  protected void createRoles(String... roleNames)
     throws Exception
  {
    Connection connection = null;
    try
    {
      connection = Transaction.begin();

      if(createRoles(roleNames, connection))
        return;

      Transaction.commit(connection);
      connection = null;
    }
    finally
    {
      if(connection != null)
      {
        Transaction.safeRollback(connection);
      }
    }
  }

  private boolean createRoles(String[] roleNames, Connection con)
     throws Exception
  {
    String sSQL
       = "SELECT *"
       + "  FROM turbine_role";
    List<Record> lsRecs = DbUtils.executeQuery(sSQL, con);

    int roleid = lsRecs.stream().mapToInt(
       LEU.rethrowFunctionInt((r) -> r.getValue("role_id").asInt())).max().orElse(0) + 1;

    String sINS
       = "INSERT INTO turbine_role(\n"
       + "	role_id, role_name, objectdata)\n"
       + "	VALUES (?, ?, NULL);";
    try ( PreparedStatement ps = con.prepareStatement(sINS))
    {
      for(int i = 0; i < roleNames.length; i++)
      {
        String roleName = roleNames[i];
        if(lsRecs.stream()
           .anyMatch(LEU.rethrowPredicate((r) -> SU.isEqu(roleName, r.getValue("role_name").asString()))))
          continue;

        ps.clearParameters();
        ps.setInt(1, roleid++);
        ps.setString(2, roleName);
        ps.executeUpdate();
      }
    }

    return false;
  }
}
