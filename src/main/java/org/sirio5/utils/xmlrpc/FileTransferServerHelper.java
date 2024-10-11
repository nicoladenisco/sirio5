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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.Pair;
import org.commonlib5.xmlrpc.FileTransfer;
import org.commonlib5.xmlrpc.HashtableRpc;
import org.sirio5.CoreConst;
import org.sirio5.beans.CoreTokenBean;
import org.sirio5.services.cache.CACHE;
import org.sirio5.services.cache.CoreCachedObject;
import org.sirio5.services.localization.INT;
import org.sirio5.services.token.TokenAuthService;

/**
 * Classe di utilità per trasferire file
 * binari attraverso XML-RPC.
 *
 * @author Nicola De Nisco
 */
public class FileTransferServerHelper extends CoreTokenBean
{
  private final String unique;
  private static final Object semaforo = new Object();
  //
  public static final String REF_TRANSFER_CACHE_KEY = "refTransferCacheKey";
  public static final int DEFAULT_BLOCK_SIZE = (int) (16 * CoreConst.KILOBYTE);
  public static final int MAX_BLOCK_SIZE = (int) (64 * CoreConst.KILOBYTE);

  public static class TransferInfo
  {
    public String id, fileName;
    public long numBlock, sizeBlock, currBlock, fileSize;
    public File toTransfer;
    public RandomAccessFile stream = null;
    public CRC32 checksum = new CRC32();
  }

  public static class TransferCachedObject extends CoreCachedObject
  {
    public TransferCachedObject(TransferInfo o)
    {
      super(o);
    }

    @Override
    public synchronized void deletingExpired()
    {
      try
      {
        super.deletingExpired();

        TransferInfo ti = (TransferInfo) getContents();
        synchronized(ti)
        {
          if(ti.stream != null)
            ti.stream.close();
          ti.stream = null;
        }
      }
      catch(IOException ex)
      {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", ex);
      }
    }
  }

  public FileTransferServerHelper()
  {
    this.unique = "uu_" + System.currentTimeMillis();
  }

  public Map preparaDownload(String fileName, File toTransfer, int suggestBlockSize)
     throws Exception
  {
    synchronized(semaforo)
    {
      TransferInfo ti = new TransferInfo();

      do
      {
        ti.id = "dw_" + System.currentTimeMillis();
      }
      while(getInfo(ti.id) != null);

      ti.toTransfer = toTransfer;
      ti.fileName = fileName;
      ti.fileSize = (int) toTransfer.length();
      ti.sizeBlock = getBlockSize(suggestBlockSize);
      ti.numBlock = (int) (ti.fileSize / ti.sizeBlock);
      if((ti.fileSize % ti.sizeBlock) != 0)
        ti.numBlock++;

      ti.stream = new RandomAccessFile(ti.toTransfer, "r");
      CACHE.addObject(REF_TRANSFER_CACHE_KEY, ti.id,
         new TransferCachedObject(ti));

      return populateTransferInfo(ti, new HashtableRpc());
    }
  }

  protected HashtableRpc populateTransferInfo(TransferInfo ti, HashtableRpc rv)
  {
    rv.put(FileTransfer.TIPAR_ID, ti.id);
    rv.put(FileTransfer.TIPAR_NUM_BLOCK, ti.numBlock);
    rv.put(FileTransfer.TIPAR_SIZE_BLOCK, ti.sizeBlock);
    rv.put(FileTransfer.TIPAR_CURR_BLOCK, ti.currBlock);
    rv.put(FileTransfer.TIPAR_FILE_SIZE, ti.fileSize);
    rv.put(FileTransfer.TIPAR_FILE_NAME, ti.toTransfer.getName());
    return rv;
  }

  /**
   * Recupera blocco per l'invio.
   * @param idFile
   * @param block
   * @return
   * @throws Exception
   * @deprecated usa getFileBlockCRC32
   */
  public byte[] getFileBlock(String idFile, int block)
     throws Exception
  {
    TransferInfo ti = getInfo(idFile);
    if(ti == null)
      throw new IOException(INT.I("IO non inizializzato o eccessivo timeout."));

    synchronized(ti)
    {
      if(ti.stream == null)
        throw new IOException(INT.I("Il trasferimento è già stato completato."));

      if(block >= ti.numBlock)
        throw new IOException(INT.I("Il blocco richiesto è eccessivo per il file indicato."));

      long seek = ti.sizeBlock * block;
      ti.stream.seek(seek);

      long left = ti.fileSize - seek;
      if(left > ti.sizeBlock)
        left = ti.sizeBlock;

      byte[] rv = new byte[(int) left];
      ti.stream.read(rv);

      ti.currBlock = block;
      return rv;
    }
  }

  /**
   * Recupera blocco per l'invio.
   * @param idFile
   * @param block
   * @return
   * @throws Exception
   */
  public Pair<Long, byte[]> getFileBlockCRC32(String idFile, int block)
     throws Exception
  {
    TransferInfo ti = getInfo(idFile);
    if(ti == null)
      throw new IOException(INT.I("IO non inizializzato o eccessivo timeout."));

    synchronized(ti)
    {
      if(ti.stream == null)
        throw new IOException(INT.I("Il trasferimento è già stato completato."));

      if(block >= ti.numBlock)
        throw new IOException(INT.I("Il blocco richiesto è eccessivo per il file indicato."));

      long seek = ti.sizeBlock * block;
      ti.stream.seek(seek);

      long left = ti.fileSize - seek;
      if(left > ti.sizeBlock)
        left = ti.sizeBlock;

      byte[] rv = new byte[(int) left];
      ti.stream.read(rv);

      ti.currBlock = block;
      ti.checksum.reset();
      ti.checksum.update(rv);
      return new Pair<>(ti.checksum.getValue(), rv);
    }
  }

  public Map preparaUpload(String fileName, int fileSize, int suggestBlockSize)
     throws Exception
  {
    synchronized(semaforo)
    {
      TransferInfo ti = new TransferInfo();

      // recupera una directory per il file temporaneo
      TokenAuthService ta = (TokenAuthService) TurbineServices
         .getInstance().getService(TokenAuthService.SERVICE_NAME);
      File dirTmp = ta.getWorkTmpFile("FileTransferServer");
      dirTmp.mkdirs();

      do
      {
        ti.id = "up_" + System.currentTimeMillis();
      }
      while(getInfo(ti.id) != null);

      ti.fileName = fileName;
      ti.fileSize = fileSize;
      ti.toTransfer = File.createTempFile(unique, ".tmp", dirTmp);
      ti.toTransfer.deleteOnExit();
      ti.sizeBlock = getBlockSize(suggestBlockSize);
      ti.numBlock = (int) (ti.fileSize / ti.sizeBlock);
      if((ti.fileSize % ti.sizeBlock) != 0)
        ti.numBlock++;

      ti.stream = new RandomAccessFile(ti.toTransfer, "rw");
      CACHE.addObject(REF_TRANSFER_CACHE_KEY, ti.id,
         new TransferCachedObject(ti));

      return populateTransferInfo(ti, new HashtableRpc());
    }
  }

  /**
   * Invia un blocco.
   * @param idFile
   * @param block
   * @param data
   * @return 0=success
   * @throws Exception
   * @deprecated usa putFileBlockCRC32
   */
  public int putFileBlock(String idFile, int block, byte[] data)
     throws Exception
  {
    TransferInfo ti = getInfo(idFile);
    if(ti == null)
      throw new IOException(INT.I("IO non inizializzato o eccessivo timeout."));

    synchronized(ti)
    {
      if(ti.stream == null)
        throw new IOException(INT.I("Il trasferimento è già stato completato."));

      if(block >= ti.numBlock)
        throw new IOException(INT.I("Il blocco richiesto è eccessivo per il file indicato."));

      long seek = ti.sizeBlock * block;
      ti.stream.seek(seek);
      ti.stream.write(data);
      ti.currBlock = block;
      return 0;
    }
  }

  /**
   * Invia un blocco.
   * @param idFile
   * @param block
   * @param data
   * @return checksum CRC32 bit del blocco
   * @throws Exception
   */
  public long putFileBlockCRC32(String idFile, int block, byte[] data)
     throws Exception
  {
    TransferInfo ti = getInfo(idFile);
    if(ti == null)
      throw new IOException(INT.I("IO non inizializzato o eccessivo timeout."));

    synchronized(ti)
    {
      if(ti.stream == null)
        throw new IOException(INT.I("Il trasferimento è già stato completato."));

      if(block >= ti.numBlock)
        throw new IOException(INT.I("Il blocco richiesto è eccessivo per il file indicato."));

      long seek = ti.sizeBlock * block;
      ti.stream.seek(seek);
      ti.stream.write(data);
      ti.currBlock = block;

      ti.checksum.reset();
      ti.checksum.update(data);
      return ti.checksum.getValue();
    }
  }

  public int trasferimentoCompletato(String idFile)
     throws Exception
  {
    TransferInfo ti = getInfo(idFile);
    if(ti == null)
      throw new IOException(INT.I("IO non inizializzato o eccessivo timeout."));

    synchronized(ti)
    {
      ti.stream.close();
      ti.stream = null;
      return 0;
    }
  }

  public TransferInfo getInfo(String idFile)
  {
    return (TransferInfo) CACHE.getContentQuiet(REF_TRANSFER_CACHE_KEY, idFile);
  }

  public int getBlockSize(int suggestBlockSize)
  {
    if(suggestBlockSize == 0)
      return DEFAULT_BLOCK_SIZE;

    if(suggestBlockSize < 1024)
      return 1024;

    if(suggestBlockSize > MAX_BLOCK_SIZE)
      return MAX_BLOCK_SIZE;

    return suggestBlockSize;
  }
}
