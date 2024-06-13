/*
 *  BaseXmlRpcClientUserAuthFileTransfer.java
 *  Creato il Jan 21, 2019, 5:47:09 PM
 *
 *  Copyright (C) 2019 Informatica Medica s.r.l.
 *
 *  Questo software è proprietà di Informatica Medica s.r.l.
 *  Tutti gli usi non esplicitimante autorizzati sono da
 *  considerarsi tutelati ai sensi di legge.
 *
 *  Informatica Medica s.r.l.
 *  Viale dei Tigli, 19
 *  Casalnuovo di Napoli (NA)
 */
package org.sirio5.utils.xmlrpc;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.commonlib5.utils.Pair;
import org.commonlib5.xmlrpc.FileTransfer;

/**
 * Classe base dei client con autenticazione e trasferimento files.
 *
 * @author Nicola De Nisco
 */
public class BaseXmlRpcClientUserAuthFileTransfer extends BaseXmlRpcClientUserAuth
   implements FileTransfer
{
  public BaseXmlRpcClientUserAuthFileTransfer(String stubName, URL url)
     throws Exception
  {
    super(stubName, url);
  }

  public BaseXmlRpcClientUserAuthFileTransfer(String stubName, String server, int port)
     throws Exception
  {
    super(stubName, server, port);
  }

  @Override
  public Map preparaDownload(String clientID, Map dati, int suggestBlockSize)
     throws Exception
  {
    return (Map) call("preparaDownload", clientID, dati, suggestBlockSize);
  }

  @Override
  public Map preparaUpload(String clientID, Map dati, int suggestBlockSize)
     throws Exception
  {
    return (Map) call("preparaUpload", clientID, dati, suggestBlockSize);
  }

  /**
   * Trasferisce un blocco di file da caleido verso l'applicazione esterna.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaDownload() con anche il numero di blocchi da
   * trasferire.
   * @param clientID id del ticket rilasciato da initClient
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @return array di bytes del blocco
   * @throws Exception
   * @deprecated usa getFileBlockCRC32
   */
  @Override
  public byte[] getFileBlock(String clientID, String idFile, int block)
     throws Exception
  {
    ASSERT(idFile != null, "idFile != null");
    return (byte[]) call("getFileBlock", clientID, idFile, block);
  }

  /**
   * Trasferisce un blocco di file da caleido verso l'applicazione esterna.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaDownload() con anche il numero di blocchi da
   * trasferire.
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @return array di bytes del blocco
   * @throws Exception
   * @deprecated usa getFileBlockCRC32
   */
  public byte[] getFileBlock(String idFile, int block)
     throws Exception
  {
    return getFileBlock(idClient, idFile, block);
  }

  @Override
  public List getFileBlockCRC32(String clientID, String idFile, int block)
     throws Exception
  {
    ASSERT(idFile != null, "idFile != null");
    return (List) call("getFileBlockCRC32", clientID, idFile, block);
  }

  /**
   * Trasferisce un blocco di file da caleido verso l'applicazione esterna.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaDownload() con anche il numero di blocchi da
   * trasferire.
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @return una coppia CRC32 e array di bytes del blocco
   * @throws Exception
   */
  public Pair<Long, byte[]> getFileBlockCRC32(String idFile, int block)
     throws Exception
  {
    List vget = getFileBlockCRC32(idClient, idFile, block);
    Double checksum = (Double) vget.get(0);
    return new Pair<>(checksum.longValue(), (byte[]) vget.get(1));
  }

  /**
   * Trasferisce un blocco di file dall'applicazione esterna a caleido.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaUpload() con anche il numero di blocchi da
   * trasferire.
   * @param clientID id del ticket rilasciato da initClient
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @param data dati binari del file
   * @return 0 tutto ok
   * @throws Exception
   * @deprecated usa putFileBlockCRC32
   */
  @Override
  public int putFileBlock(String clientID, String idFile, int block, byte[] data)
     throws Exception
  {
    ASSERT(idFile != null, "idFile != null");
    ASSERT(data != null, "data != null");
    return (Integer) call("putFileBlock", clientID, idFile, block, data);
  }

  /**
   * Trasferisce un blocco di file dall'applicazione esterna a caleido.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaUpload() con anche il numero di blocchi da
   * trasferire.
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @param data dati binari del file
   * @return 0 tutto ok
   * @throws Exception
   * @deprecated usa putFileBlockCRC32
   */
  public int putFileBlock(String idFile, int block, byte[] data)
     throws Exception
  {
    return putFileBlock(idClient, idFile, block, data);
  }

  /**
   * Trasferisce un blocco di file dall'applicazione esterna a caleido.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaUpload() con anche il numero di blocchi da
   * trasferire.
   * @param clientID id del ticket rilasciato da initClient
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @param data dati binari del file
   * @return CRC a 32 bit del blocco trasferito
   * @throws Exception
   */
  @Override
  public double putFileBlockCRC32(String clientID, String idFile, int block, byte[] data)
     throws Exception
  {
    ASSERT(idFile != null, "idFile != null");
    ASSERT(data != null, "data != null");
    return (Double) call("putFileBlockCRC32", clientID, idFile, block, data);
  }

  /**
   * Trasferisce un blocco di file dall'applicazione esterna a caleido.
   * L'id del file è stato trasferito nei parametri di ritorno
   * di preparaUpload() con anche il numero di blocchi da
   * trasferire.
   * @param idFile identificatore del file
   * @param block numero del blocco richiesto
   * @param data dati binari del file
   * @return CRC a 32 bit del blocco trasferito
   * @throws Exception
   */
  public long putFileBlockCRC32(String idFile, int block, byte[] data)
     throws Exception
  {
    return (long) putFileBlockCRC32(idClient, idFile, block, data);
  }

  @Override
  public int trasferimentoCompletato(String clientID, String idFile)
     throws Exception
  {
    ASSERT(idFile != null, "idFile != null");
    return (Integer) call("trasferimentoCompletato", clientID, idFile);
  }

  public int trasferimentoCompletato(String idFile)
     throws Exception
  {
    return trasferimentoCompletato(idClient, idFile);
  }

  /**
   * Loop di upload generico per qualsiasi tipo di file.
   * @param pup informazioni ricevute da una precedente chiamata a preparaUpload()
   * @param tmpFile file temporaneo da inviare al server
   * @return id dell'operazione da utilizzare in chiamate successive
   * @throws Exception
   */
  public String uploadLoopGenerico(Map pup, File tmpFile)
     throws Exception
  {
    return uploadFileStandardLoop(idClient, pup, tmpFile, null);
  }

  /**
   * Loop di download generico per qualsiasi tipo di file.
   * @param pup informazioni ricevute da una precedente chiamata a preparaUpload()
   * @param tmpFile file temporaneo da inviare al server
   * @return id dell'operazione da utilizzare in chiamate successive
   * @throws Exception
   */
  public String downloadLoopGenerico(Map pup, File tmpFile)
     throws Exception
  {
    return downloadFileStandardLoop(idClient, pup, tmpFile, null);
  }
}
