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
package org.sirio2.services.cache;

import java.io.File;
import org.apache.fulcrum.mimetype.MimeTypeService;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.CommonFileUtils;
import org.sirio2.CoreConst;

/**
 * Oggetto della cache per memorizzazione temporanea di files.
 *
 * @author Nicola De Nisco
 */
public class FileCacheItem extends CoreCachedObject
{
  /** sezione della cache che conterrà i files */
  private static final String CACHE_FILE_SECTION = "filesCache";
  /** tempo di permanenza nella cache (5 minuti) */
  private static final long EXPIRES = 5 * CoreConst.ONE_MINUTE_MILLIS;
  /** semaforo per la sincronizzazione dell'accesso alla cache */
  private static final Object semaforo = new Object();
  /** tipo mime del file memorizzato */
  private String tipoMime = null;
  /** nome del file desiderato */
  private String fileName = null;

  private static MimeTypeService ms;

  /**
   * Costruttore privato.
   * Gli oggetti FileCacheItem vengono creati esclusivamente
   * con il metodo statico addFileToCache().
   * @param o file da memorizzare
   * @param tipoMime tipo mime del file
   * @param fileName nome del file
   */
  private FileCacheItem(File o, String tipoMime, String fileName, long expires)
  {
    super(o, expires);
    this.tipoMime = tipoMime;
    this.fileName = fileName;
  }

  @Override
  public synchronized void deletingExpired()
  {
    super.deletingExpired();
    File f = (File) getContents();
    f.delete();
  }

  /**
   * Refresh di questa entry ad ogni prelievo dalla cache.
   * Il comportamento di default sarebbe di azzerare il TTL
   * ma in questo caso il comportamento non è gradito.
   */
  @Override
  public synchronized void refreshEntry()
  {
  }

  public File getFile()
  {
    return (File) getContents();
  }

  public String getTipoMime()
  {
    return tipoMime;
  }

  public String getFileName()
  {
    return fileName;
  }

  /////////////////////////////////////////////////////////////////////////////////////
  /**
   * Recupera un oggetto file dalla cache.
   * @param ticket il codice di identificazione del file
   * @return il descrittore del file o null se inesistente o scaduto
   */
  public static FileCacheItem getFromCache(String ticket)
  {
    synchronized(semaforo)
    {
      return (FileCacheItem) CACHE.getObjectQuiet(CACHE_FILE_SECTION, ticket);
    }
  }

  /**
   * Salva un file nella cache.
   * Il file viene mantenuto nella cache in una apposita directory
   * per il tempo specificato da expires.
   * Alla scadenza il file viene automaticamente distrutto.
   * I files vengono distrutti all'uscita dell'application server.
   * @param toStore file da salvare (verrà spostato nella posizione opportuna)
   * @param mimeType tipo mime del file (se null viene ricavato dal file)
   * @param fileName nome del file (se null viene ricavato dal file)
   * @param copy se vero il file viene copiato altrimenti viene spostato
   * @param expires tempo di permanenza nella cache (millisecondi)
   * @return ticket per il recupero del file
   * @throws Exception
   */
  public static String addFileToCache(File toStore, String mimeType, String fileName, boolean copy, long expires)
     throws Exception
  {
    synchronized(semaforo)
    {
      // produce un ticket e verifica che non esista già nella cache
      String ticket = null;
      do
      {
        ticket = "TI" + System.currentTimeMillis();
      }
      while(CACHE.getObjectQuiet(CACHE_FILE_SECTION, ticket) != null);

      // crea file temporaneo in area cache
      File dest = CACHE.getWorkCacheFile(ticket);
      dest.deleteOnExit();

      // sposta o copia in area cache
      if(copy)
        CommonFileUtils.copyFile(toStore, dest);
      else
        CommonFileUtils.moveFile(toStore, dest);

      // se non specificato tenta di determinare un tipo mime per il file
      if(mimeType == null)
      {
        if(ms == null)
          ms = (MimeTypeService) TurbineServices.getInstance().getService(MimeTypeService.ROLE);

        mimeType = ms.getContentType(toStore);
      }

      // per default il nome è lo stesso del file che stiamo memorizzando ma può essere diverso
      if(fileName == null)
        fileName = toStore.getName();

      // aggiunge file alla cache
      FileCacheItem item = new FileCacheItem(dest, mimeType, fileName, expires);
      CACHE.addObject(CACHE_FILE_SECTION, ticket, item);
      return ticket;
    }
  }

  /**
   * Salva un file nella cache.
   * Il file viene mantenuto nella cache in una apposita directory
   * per il tempo di default della cache (5 minuti).
   * Alla scadenza il file viene automaticamente distrutto.
   * I files vengono distrutti all'uscita dell'application server.
   * @param toStore file da salvare (verrà spostato nella posizione opportuna)
   * @param mimeType tipo mime del file (se null viene ricavato dal file)
   * @param fileName nome del file (se null viene ricavato dal file)
   * @param copy se vero il file viene copiato altrimenti viene spostato
   * @return ticket per il recupero del file
   * @throws Exception
   */
  public static String addFileToCache(File toStore, String mimeType, String fileName, boolean copy)
     throws Exception
  {
    return addFileToCache(toStore, mimeType, fileName, copy, EXPIRES);
  }
}
