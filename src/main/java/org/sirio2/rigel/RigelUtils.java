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
package org.sirio2.rigel;

import javax.servlet.http.HttpSession;
import org.apache.torque.om.Persistent;
import org.apache.turbine.util.RunData;
import org.rigel5.table.RigelTableModel;
import org.rigel5.table.WrapperBase;
import org.rigel5.table.html.wrapper.HtmlWrapperBase;
import org.rigel5.table.peer.html.PeerTableModel;
import org.rigel5.table.sql.html.SqlTableModel;
import org.sirio2.services.security.SEC;
import org.sirio2.utils.CoreRunData;

/**
 * Funzioni di utilità generale utilizzate per le videate gestite da rigel.
 *
 * @author Nicola De Nisco
 */
public class RigelUtils
{
  public static void deleteRecord(RunData data, String sKey, HtmlWrapperBase hwb)
     throws Exception
  {
    int idUser = SEC.getUserID(data);
    RigelTableModel tm = hwb.getPtm();

    if(tm instanceof PeerTableModel)
    {
      deleteRecord(data, sKey, (PeerTableModel) tm, idUser);
    }
    else if(tm instanceof SqlTableModel)
    {
      deleteRecord(data, sKey, (SqlTableModel) tm, idUser);
    }
  }

  /**
   * Cancella (logicamente ovvero stato_rec=10) il record identificato
   * dalla stringa rappresentazione della chiave primaria passata.
   * @param sKey stringa identificativa del record
   * @param pwl getstore dati associato alla vista
   * @throws Exception
   */
  public static void deleteRecord(RunData data, String sKey, PeerTableModel ptm, int idUser)
     throws Exception
  {
    for(int i = 0; i < ptm.getRowCount(); i++)
    {
      Persistent obj = (Persistent) (ptm.getRowRecord(i));
      if(obj.getPrimaryKey().toString().compareTo(sKey) == 0)
      {
        CachedObjectSaver.save(null, obj, idUser, 10, 0);
        break;
      }
    }

    ptm.clearTotalRecords();
  }

  /**
   * Cancella (logicamente ovvero stato_rec=10) il record identificato
   * dalla stringa rappresentazione della chiave primaria passata.
   * @param sKey stringa identificativa del record
   * @param pwl getstore dati associato alla vista
   * @throws Exception
   */
  public static void deleteRecord(RunData data, String sKey, SqlTableModel ptm, int idUser)
     throws Exception
  {
    String tableName = ptm.getQuery().getDeleteFrom();

    if(tableName == null)
    {
      ((CoreRunData) data).setMessagei18n(
         "Cancellazione non possibile: mancata definizione tabella di riferimento in lista-sql.");
      return;
    }

    ptm.deleteLogicalByQueryKey(sKey, tableName, idUser, 10);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di cancellazione per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la cancellazione
   * @throws Exception
   */
  public static boolean checkPermessiCancellazione(RunData data, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getCancellazione();
    return permessi == null ? true : SEC.checkAllPermission(data, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di creazione per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la creazione di nuovo record
   * @throws Exception
   */
  public static boolean checkPermessiCreazione(RunData data, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getCreazione();
    return permessi == null ? true : SEC.checkAllPermission(data, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di lettura per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la lettura
   * @throws Exception
   */
  public static boolean checkPermessiLettura(RunData data, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getLettura();
    return permessi == null ? true : SEC.checkAllPermission(data, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di scrittura per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la scrittura
   * @throws Exception
   */
  public static boolean checkPermessiScrittura(RunData data, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getScrittura();
    return permessi == null ? true : SEC.checkAllPermission(data, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di cancellazione per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la cancellazione
   * @throws Exception
   */
  public static boolean checkPermessiCancellazione(HttpSession session, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getCancellazione();
    return permessi == null ? true : SEC.checkAllPermission(session, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di creazione per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la creazione di nuovo record
   * @throws Exception
   */
  public static boolean checkPermessiCreazione(HttpSession session, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getCreazione();
    return permessi == null ? true : SEC.checkAllPermission(session, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di lettura per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la lettura
   * @throws Exception
   */
  public static boolean checkPermessiLettura(HttpSession session, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getLettura();
    return permessi == null ? true : SEC.checkAllPermission(session, permessi);
  }

  /**
   * Verifica se l'utente loggato ha il permesso di scrittura per il form indicato.
   * @param data descrittore di sessione
   * @param wb wrapper del form
   * @return vero se è consentita la scrittura
   * @throws Exception
   */
  public static boolean checkPermessiScrittura(HttpSession session, WrapperBase wb)
     throws Exception
  {
    if(wb.getPermessi() == null)
      return true;
    String permessi = wb.getPermessi().getScrittura();
    return permessi == null ? true : SEC.checkAllPermission(session, permessi);
  }
}
