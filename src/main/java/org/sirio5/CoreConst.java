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
package org.sirio5;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Pattern;
import org.sirio5.services.token.TokenAuthItem;
import org.sirio5.utils.CoreRunData;

/**
 * In questa classe sono archiviate le costanti ad uso generale.
 *
 * FILENOI18N
 * @author Nicola De Nisco
 */
public class CoreConst
{
  // prefisso per l'applicazione turbine nelle path
  public static final String APP_PREFIX = "app";

  // modalita' di logon
  public static final int LOGON_UNDEFINED = 0;
  public static final int LOGON_NORMAL = 1;
  public static final int LOGON_AUTO = 2;
  public static final int LOGON_SPECIAL = 3;
  public static final int LOGON_CERTIFICATE = 4;
  public static final int LOGON_CERTIFICATE_ROOT = 5;
  public static final int LOGON_LDAP = 6;
  public static final int LOGON_SSO = 7;

  // chiave nel profilo utente per abilitazione login con password
  public static final String ENABLED_PASSWORD_LOGON = "ENABLED_PASSWORD_LOGON";

  public static final String AUTO = "AUTOMATICO";
  public static final String IMPEGNO = "impegno";

  public static final int MAX_LIVELLI_MENU = 10;

  public static final double EPSI_VALUTA = 0.001;
  public static final double EPSI_QTA = 0.00001;
  public static final double EPSI_DIM = 0.0001;
  public static final double EPSI_GENERIC = 0.001;

  public static final long KILOBYTE = 1024L;
  public static final long MEGABYTE = 1024L * 1024L;
  public static final long GIGABYTE = 1024L * 1024L * 1024L;
  public static final long TERABYTE = 1024L * 1024L * 1024L * 1024L;

  // un secondo e minuto espressi in millisecondi
  public static final long ONE_SECOND_MILLIS = 1000L;
  public static final long ONE_MINUTE_MILLIS = 60 * 1000L;
  public static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
  public static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;
  public static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;
  public static final long ONE_MONTH_MILLIS = 30 * 24 * 60 * 60 * 1000L;
  public static final long ONE_YEAR_MILLIS = 365 * 24 * 60 * 60 * 1000L;

  // array dei mesi in inglese
  public static final String[] MONTH =
  {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };

  public static final int LEN_TICKET = 16;
  public static final int LEN_TICKET_SIZE = 16;
  public static final int TIMEOUT_TICKET_MILLIS = 10000;

  // parametri di sessione estratti al logon
  public static final String LOGIN_MODE = "LOGIN_MODE";
  public static final String LOGIN_JOURNAL = "LOGIN_JOURNAL";
  public static final String USER_DIMENSION = "USER_DIMENSION";

  // un pollice espresso in millimetri
  public static final double ONE_INCH_MILL = 25.4;

  // stato degli allarmi
  public static final String ALLARME_ATTIVO = "A";
  public static final String ALLARME_DISATTIVATO = "D";

  // tipi di allarme
  public static final String TIPALLARME_INFO = "I";
  public static final String TIPALLARME_WARNING = "W";
  public static final String TIPALLARME_ERROR = "E";
  public static final String TIPALLARME_FATAL = "F";

  // millisecondi di tolleranza per considerare due date uguali
  public static final long EPSI_DATE = 100;

  // tipi mime ed estensini file
  public static final String AUTO_MIME = "AUTO";
  public static final String MIME_DICOM = "application/dicom";
  public static final String EXT_DICOM = "dcm";
  public static final String MIME_BIN = "application/binary";
  public static final String EXT_BIN = "bin";
  public static final String MIME_PDF = "application/pdf";
  public static final String EXT_PDF = "pdf";
  public static final String MIME_RTF = "text/rtf";
  public static final String EXT_RTF = "rtf";
  public static final String MIME_TXT = "text/plain";
  public static final String EXT_TXT = "txt";
  public static final String MIME_HTML = "text/html";
  public static final String EXT_HTML = "html";
  public static final String MIME_ZIP = "application/zip";
  public static final String EXT_ZIP = "zip";

  public static final String MIME_JPG = "image/jpeg";
  public static final String EXT_JPG = "jpg";
  public static final String MIME_PNG = "image/png";
  public static final String EXT_PNG = "png";
  public static final String MIME_GIF = "image/gif";
  public static final String EXT_GIF = "gif";

  public static final String MIME_P7M = "application/x-pkcs7-mime";
  public static final String EXT_P7M = "p7m";

  public static final String MIME_XML = "text/xml";
  public final static String MIME_CDA = "text/x-cda-r2+xml"; //TipoMime CDA Tabella 2.10-1 AffinityDomain

  public final static String[] MIME_PDFs =
  {
    MIME_PDF, EXT_PDF
  };

  public static final String[] MIME_IMAGES =
  {
    MIME_JPG, MIME_GIF, MIME_PNG
  };

  // costanti per il funzionamento dei forms
  public static final String SAVE_ONLY = "salva";
  public static final String DUP_CURRENT = "duplica";
  public static final String SAVE_AND_NEW = "salvanew";
  public static final String SAVE_AND_EXIT = "salvaesci";
  public static final String SAVE_AND_CLOSE = "salvachiudi";
  public static final String NEW_DETAIL = "nuovodett";
  public static final String CLEAR_FORM_DATA = "abbandona";
  public static final String CLOSE_EDIT = "chiudi";
  public static final String DELETE_RECORD = "cancella";

  public static final String M_DEF_LIST = "clifor";

  // minuti di tolleranza per autologon (+/-10 minuti)
  public static final long TOLL_TIME_LOGIN = 10L * ONE_MINUTE_MILLIS;

  // tipi di socket
  public static final int TSOCK_CLIENT = 0;
  public static final int TSOCK_SERVER = 1;

  public static final Class[] cmdParamTypes1 = new Class[]
  {
    CoreRunData.class, Map.class
  };
  public static final Class[] cmdParamTypes2 = new Class[]
  {
    CoreRunData.class, Map.class, Object[].class
  };

  public static final Class[] cmdParamTypes3 = new Class[]
  {
    TokenAuthItem.class, Map.class
  };
  public static final Class[] cmdParamTypes4 = new Class[]
  {
    TokenAuthItem.class, Map.class, Object[].class
  };

  // espressioni regolari di uso frequente
  public static final Pattern codFis
     = Pattern.compile("[A-Za-z]{6}[0-9]{2}[A-Za-z]{1}[0-9]{2}[A-Za-z]{1}[0-9]{3}[A-Za-z]{1}");
  public static final Pattern parIva = Pattern.compile("[0-9]{11}");
  public static final Pattern eMail
     = Pattern.compile("[\\w\\-\\.]*[\\w\\.]\\@[\\w\\.]*[\\w\\-\\.]+[\\w\\-]+[\\w]\\.+[\\w]+[\\w]");

  public static final String eMail2
     = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  // numero di millisecondi di tolleranza per eguaglianza date
  public static final int TIME_MILLIS_EQUAL = 1000; // 1 sec.

  public static final DateFormat dfITshort = new SimpleDateFormat("dd/MM/yyyy");
  public static final DateFormat dfUSshort = new SimpleDateFormat("yyyy-MM-dd");

  public static final java.util.Date prvDate = new java.util.Date(1980 - 1900, 0, 01);
  public static final java.util.Date farDate = new java.util.Date(2070 - 1900, 11, 31);

  public static final long MSEC_MINUTO = 60 * 1000; // millisecondi in un minuto
  public static final long MSEC_ORA = MSEC_MINUTO * 60; // millisecondi in un'ora
  public static final long MSEC_GIORNO = MSEC_ORA * 24; // millisecondi in un giorno
  public static final long MSEC_MESE = MSEC_GIORNO * 30; // millisecondi in un mese standard
  public static final long MSEC_ANNO = MSEC_GIORNO * 365; // millisecondi in un anno standard

  /** Chiave di setup per path alternativa dove reperire i modelli per i tools */
  public static final String TOOL_RENDER_MODEL_PATH = "toolRender.model.path";
}
