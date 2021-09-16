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
package org.sirio5.utils.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;
import org.commonlib5.utils.Pair;
import org.commonlib5.xmlrpc.FileTransfer;
import org.sirio5.beans.BeanFactory;
import org.sirio5.services.token.TokenAuthItem;
import org.sirio5.utils.SU;

/**
 * Classe base degli XmlRpc Server con autenticazione utente
 * e funzioni integrate di trasferimento file.
 * Definisce una serie di funzioni di base comuni a tutti
 * i server che devono supportare un concetto di autenticazione e sessione
 * associata al client che si connette al servizio.
 *
 * ATTENZIONE: non inizializzare nessun servizio nel costruttore
 * alrimenti si crea una incongruenza nello startup dei servizi
 * e l'applicazione non può procedere.
 *
 * @author Nicola De Nisco
 */
public class BaseXmlRpcServerFileTransfer extends BaseXmlRpcServerUserAuth
   implements FileTransfer
{
  public FileTransferServerHelper getHelper(TokenAuthItem token)
     throws Exception
  {
    return BeanFactory.getFromToken(token, FileTransferServerHelper.class);
  }

  @Override
  final public byte[] getFileBlock(String clientID, String idFile, int block)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    return getHelper(token).getFileBlock(idFile, block);
  }

  @Override
  final public Vector getFileBlockCRC32(String clientID, String idFile, int block)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    Pair<Long, byte[]> dt = getHelper(token).getFileBlockCRC32(idFile, block);
    Vector rv = new Vector();
    rv.add(new Double(dt.first));
    rv.add(dt.second);
    return rv;
  }

  @Override
  final public int putFileBlock(String clientID, String idFile, int block, byte[] data)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    return getHelper(token).putFileBlock(idFile, block, data);
  }

  @Override
  final public double putFileBlockCRC32(String clientID, String idFile, int block, byte[] data)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    return getHelper(token).putFileBlockCRC32(idFile, block, data);
  }

  @Override
  final public int trasferimentoCompletato(String clientID, String idFile)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    return getHelper(token).trasferimentoCompletato(idFile);
  }

  @Override
  public Hashtable preparaDownload(String clientID, Hashtable dati, int suggestBlockSize)
     throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  final public Hashtable preparaUpload(String clientID, Hashtable dati, int suggestBlockSize)
     throws Exception
  {
    TokenAuthItem token = getClient(clientID);
    String fileName = SU.okStrNull(dati.get(TIPAR_FILE_NAME));
    int fileSize = SU.parse(dati.get(TIPAR_FILE_SIZE), 0);
    return getHelper(token).preparaUpload(fileName, fileSize, suggestBlockSize);
  }
}
