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
package org.sirio5.services.modellixml;

import java.io.File;
import java.net.URLEncoder;
import java.util.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.pull.PullService;
import org.apache.turbine.services.pull.tools.UITool;
import org.apache.turbine.util.RunData;
import org.commonlib5.utils.StringOper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.rigel5.SetupHolder;
import org.rigel5.db.torque.TQUtils;
import org.rigel5.db.torque.TurbineConnectionProducer;
import org.rigel5.exceptions.MissingListException;
import org.rigel5.exceptions.MissingSectionException;
import org.rigel5.glue.WrapperCacheBase;
import org.rigel5.glue.custom.CustomButtonFactory;
import org.rigel5.glue.custom.CustomEditFactory;
import org.rigel5.glue.custom.CustomFormatterFactory;
import org.rigel5.glue.validators.ValidatorsFactory;
import org.rigel5.table.peer.PeerWrapperTmapMaker;
import org.rigel5.table.peer.PeerWrapperXmlMaker;
import org.rigel5.table.peer.TorqueObjectManager;
import org.rigel5.table.peer.html.PeerWrapperEditHtml;
import org.rigel5.table.peer.html.PeerWrapperFormHtml;
import org.rigel5.table.peer.html.PeerWrapperListaHtml;
import org.rigel5.table.peer.xml.PeerWrapperListaXml;
import org.rigel5.table.sql.SqlWrapperXmlMaker;
import org.rigel5.table.sql.html.SqlWrapperFormHtml;
import org.rigel5.table.sql.html.SqlWrapperListaHtml;
import org.rigel5.table.sql.xml.SqlWrapperListaXml;
import org.sirio5.rigel.CoreCustomUrlBuilder;
import org.sirio5.rigel.CoreRigelCacheManager;
import org.sirio5.rigel.CoreRigelUIManager;
import org.sirio5.rigel.CoreTurbineWrapperCache;
import org.sirio5.rigel.RigelDefaultI18n;
import org.sirio5.services.AbstractCoreBaseService;
import org.sirio5.services.CoreServiceException;
import org.sirio5.services.localization.INT;
import org.sirio5.utils.LI;
import org.sirio5.utils.SU;
import org.sirio5.utils.format.DateOnlyFormat;
import org.sirio5.utils.format.DateTimeFormat;
import org.sirio5.utils.format.NumeroServiceFormat;
import org.sirio5.utils.format.TimeOnlyFormat;
import org.sirio5.utils.format.ValutaServiceFormat;

/**
 * Implementazione standard del gestore modelli XML.
 *
 * @author Nicola De Nisco
 */
public class CoreModelliXML extends AbstractCoreBaseService
   implements modelliXML
{
  /** Logging */
  private static final Log log = LogFactory.getLog(CoreModelliXML.class);
  protected PeerWrapperXmlMaker pwm = new PeerWrapperXmlMaker();
  protected SqlWrapperXmlMaker swm = new SqlWrapperXmlMaker();
  protected PeerWrapperTmapMaker twm = new PeerWrapperTmapMaker();
  protected Document doc = null;
  protected List<File> vFilesXml = new ArrayList<>();
  protected boolean rigelInitOK = false;
  protected String baseFormUrl, baseListUrl, baseFormPopup, baseListPopup;
  protected String context;
  protected PullService ps;
  protected UITool ui = null;
  protected boolean attivaProtezioneCSRF = true;
  //
  public static final String WrapperCacheBaseKey = "WrapperCacheBaseKey";
  protected final ArrayList<String> arListeSql = new ArrayList<>();

  @Override
  public void coreInit()
     throws Exception
  {
    log.info("Inizializzazione StandardModelliXML");
    ps = (PullService) getServiceBroker().getService(PullService.SERVICE_NAME);
    ui = (UITool) ps.getGlobalContext().get("ui");

    // estrae dati di setup
    Configuration cfg = getConfiguration();
    baseFormUrl = cfg.getString("baseFormUrl", "mform.vm");
    baseListUrl = cfg.getString("baseListUrl", "maint.vm");
    baseFormPopup = cfg.getString("baseFormPopup", "pform.vm");
    baseListPopup = cfg.getString("baseListPopup", "plista.vm");

    attivaProtezioneCSRF = cfg.getBoolean("attivaProtezioneCSRF", attivaProtezioneCSRF);

    // carica prima i files personalizzati (hanno precedenza)
    String[] fXmlPers = cfg.getStringArray("file.azienda." + aziendaId);
    if(fXmlPers.length != 0)
      addFilesToParse(fXmlPers);

    // cerca i files standard
    String[] fXml = cfg.getStringArray("file");
    if(fXml.length != 0)
      addFilesToParse(fXml);

    // cerca i files base
    String[] fXmlBase = cfg.getStringArray("file.base");
    if(fXmlBase.length != 0)
      addFilesToParse(fXmlBase);

    if(vFilesXml.isEmpty())
      die("Nessun file XML specificato.");

    doc = buildDocument();

    TorqueObjectManager tom = new TorqueObjectManager();
    tom.setBasePeerArray(cfg.getStringArray("basePeer"));
    tom.setBaseObjectArray(cfg.getStringArray("baseObject"));

    pwm.setTObjMan(tom);
    pwm.setDocument(doc);
    swm.setDocument(doc);
    twm.setTObjMan(tom);

    twm.setBaseFormUrl(baseFormUrl);
    twm.setBaseListUrl(baseListUrl);

    String excludeFields = cfg.getString("excludeFields", "statorec");
    if(excludeFields != null)
      twm.setExcludeFields(excludeFields);

    String readOnlyFields = cfg.getString("readOnlyFields", "iduser,idcrea,ultmodif,creazione");
    if(readOnlyFields != null)
      twm.setReadOnlyFields(readOnlyFields);

    ValidatorsFactory.getInstance().setClassRadixs(new String[]
    {
      "org.sirio5.rigel.validators"
    });

    CustomEditFactory.getInstance().setClassRadixs(new String[]
    {
      "org.sirio5.rigel.customedit"
    });

    CustomButtonFactory.getInstance().setClassRadixs(new String[]
    {
      "org.sirio5.rigel.custombuttons"
    });

    CustomFormatterFactory.getInstance().setClassRadixs(new String[]
    {
      "org.sirio5.rigel.customformatter",
      "org.sirio5.utils.format"
    });

    tryInitRigelPath();
    caricaListaSql();
  }

  protected void addFilesToParse(String[] fileNames)
     throws Exception
  {
    for(int i = 0; i < fileNames.length; i++)
      addFileToParse(fileNames[i]);
  }

  protected void addFileToParse(String fileName)
     throws Exception
  {
    File f = getConfMainFile(fileName);
    if(f.canRead())
      vFilesXml.add(f);
    else
      log.warn("Il file " + f.getAbsolutePath() + " non può essere letto: ignorato.");
  }

  @Override
  public void shutdown()
  {
  }

  public void tryInitRigelPath()
     throws Exception
  {
    if(rigelInitOK)
      return;

    String contextPath = null;

    if(contextPath == null)
      contextPath = SU.okStrNull(System.getProperty("globalContextPath"));

    if(contextPath == null)
      contextPath = StringOper.okStrNull(Turbine.getContextPath());

    if(contextPath == null)
      return;

    context = ("/" + contextPath + "/").replace("//", "/");

    initRigelPath();
    rigelInitOK = true;
  }

  /**
   * Vero se stiamo usando bootstrap 3.
   * Da ridefinire in classi derivate se non usiamo bootstrap.
   * @return vero per bootstrap
   */
  public boolean useBootstrap()
  {
    return false;
  }

  /**
   * Vero se stiamo usando Awesome per le icone.
   * Da ridefinire in classi derivate se non usiamo awesome.
   * @return vero per bootstrap
   */
  public boolean useAwesome()
  {
    return false;
  }

  /**
   * Vero se stiamo usando Awesome per le icone.
   * Da ridefinire in classi derivate se non usiamo awesome.
   * @return vero per bootstrap
   */
  public boolean useAwesome5()
  {
    return true;
  }

  public void initRigelPath()
     throws Exception
  {
    String qbName = getSC("querybuilder", "AUTO");

    if("AUTO".equalsIgnoreCase(qbName))
    {
      String adapterName = SU.okStrNull(TQUtils.getAdapterName());
      if(adapterName == null)
        throw new CoreServiceException("Non riesco a determinare l'adpter del db utilizzato; probabilmente db non supportato.");

      if((qbName = TQUtils.torqueRigelAdapterMap.get(adapterName)) == null)
        throw new CoreServiceException("Database non supportato.");
    }

    SetupHolder.setAttivaProtezioneCSRF(attivaProtezioneCSRF);
    SetupHolder.setImgEditData(getImgEditData());
    SetupHolder.setImgEditForeign(getImgEditForeign());
    SetupHolder.setImgLista(getImgLista());
    SetupHolder.setImgEditItem(getImgEditItem());
    SetupHolder.setImgFormForeign(getImgFormForeign());
    SetupHolder.setImgSelItem(getImgSelect());
    SetupHolder.setImgDeleteItem(getImgCancellaRecord());

    SetupHolder.setDateFormat(new DateOnlyFormat());
    SetupHolder.setTimeFormat(new TimeOnlyFormat());
    SetupHolder.setDateTimeFormat(new DateTimeFormat());
    SetupHolder.setNumberFormat(new NumeroServiceFormat());
    SetupHolder.setValutaFormat(new ValutaServiceFormat());
    SetupHolder.setConProd(new TurbineConnectionProducer());
    SetupHolder.setQueryBuilderClassName(qbName);
    SetupHolder.setCacheManager(new CoreRigelCacheManager());

    SetupHolder.setAutoComboLimit(100);
    SetupHolder.setAutoForeingColumns("descrizione, cognome, rag_soc, nome");

    CoreCustomUrlBuilder ub = new CoreCustomUrlBuilder(context);
    ub.setBaseMainForm(baseFormUrl);
    ub.setBaseMainList(baseListUrl);
    ub.setBasePopupForm(baseFormPopup);
    ub.setBasePopupList(baseListPopup);
    SetupHolder.setUrlBuilder(ub);

    SetupHolder.setUiManager(new CoreRigelUIManager());
    SetupHolder.setRi18n(new RigelDefaultI18n());
  }

  public String getImgWithDefaults(String key,
     String defUI, String defAwe, String defAwe5, String defBoot, String testo)
     throws Exception
  {
    if(useAwesome5())
    {
      if(defAwe5.isEmpty())
        defAwe5 = defAwe.replace("awesome:", "fas:");

      String nomeIcona = getSC(key, defAwe5);
      return LI.getImgIcon(nomeIcona, testo);
    }

    if(useAwesome())
    {
      String nomeIcona = getSC(key, defAwe);
      return LI.getImgIcon(nomeIcona, testo);
    }

    if(useBootstrap())
    {
      String nomeIcona = getSC(key, defBoot);
      return LI.getImgIcon(nomeIcona, testo);
    }

    String nomeIcona = getSC(key, defUI);
    return getImgHtml(nomeIcona, testo);
  }

  @Override
  public String getImgSelect()
     throws Exception
  {
    return getImgWithDefaults("ImgSelect", "select.gif", "awesome:check", "", "glyphicon:download", INT.I("Conferma"));
  }

  @Override
  public String getImgEditData()
     throws Exception
  {
    return getImgWithDefaults("ImgEditData", "calendario.gif", "awesome:calendar", "", "glyphicon:calendar", INT.I("Modifica data"));
  }

  @Override
  public String getImgEditForeign()
     throws Exception
  {
    return getImgWithDefaults("ImgEditForeign", "editForeign.gif", "awesome:search", "", "glyphicon:search", INT.I("Ricerca valore"));
  }

  @Override
  public String getImgFormForeign()
     throws Exception
  {
    return getImgWithDefaults("ImgFormForeign", "formForeign.gif", "awesome:file-code", "", "glyphicon:list-alt", INT.I("Visualizza dettaglio"));
  }

  @Override
  public String getImgLista()
     throws Exception
  {
    return getImgWithDefaults("ImgLista", "ricerca.gif", "awesome:list", "", "glyphicon:list", INT.I("Seleziona lista"));
  }

  @Override
  public String getImgEditItem()
     throws Exception
  {
    return getImgWithDefaults("ImgEditItem", "editItem.gif", "awesome:edit", "", "glyphicon:edit", INT.I("Modifica"));
  }

  @Override
  public String getImgEditRecord()
     throws Exception
  {
    return getImgWithDefaults("ImgEditRecord", "editRecord.gif", "awesome:edit", "", "glyphicon:edit", INT.I("Modifica record"));
  }

  @Override
  public String getImgCancellaRecord()
     throws Exception
  {
    return getImgWithDefaults("ImgCancellaRecord", "cancella.gif", "awesome:remove", "fas:trash", "glyphicon:remove", INT.I("Cancella record"));
  }

  @Override
  public String getImgExpand()
     throws Exception
  {
    return getImgWithDefaults("ImgEspandi", "ricerca.gif", "awesome:expand", "", "glyphicon:expand", INT.I("Altre opzioni"));
  }

  @Override
  public String getImgCollapse()
     throws Exception
  {
    return getImgWithDefaults("ImgContrai", "cancella.gif", "awesome:compress", "", "glyphicon:collapse-up", INT.I("Chiudi opzioni"));
  }

  /**
   * Ritorna il tag HTML per l'immagine predefinita per la selezione.
   *
   * @param stato
   * @return tag HTML completo
   * @throws java.lang.Exception
   */
  @Override
  public String getImgSmiley(int stato)
     throws Exception
  {
    String simg;

    switch(stato)
    {
      case 1:
        simg = getImgHtml("nwsmileygreen.gif", "Stato cliente");
        break;
      case 2:
        simg = getImgHtml("nwsmileyred.gif", "Stato cliente");
        break;
      case 3:
        simg = getImgHtml("nwsmileyyellow.gif", "Stato cliente");
        break;
      case 4:
        simg = getImgHtml("nwsmileywhite.gif", "Stato cliente");
        break;
      case 5:
        simg = getImgHtml("nwsmileyorange.gif", "Stato cliente");
        break;
      case 6:
        simg = getImgHtml("nwsmileyblue.gif", "Stato cliente");
        break;
      case 7:
        simg = getImgHtml("nwsmileymaroon.gif", "Stato cliente");
        break;
      case 8:
        simg = getImgHtml("nwsmileyblack.gif", "Stato cliente");
        break;
      default:
        simg = getImgHtml("nwsmileygray.gif", "Stato cliente");
        break;
    }

    return simg;
  }

  /**
   * Costruzione delle url per le quattro immagini di navigazione.
   * @return array di 4 url: primo, ultimo, precedente, successivo
   * @throws java.lang.Exception
   */
  @Override
  public String[] getImgsNav()
     throws Exception
  {
    String[] rv = new String[4];
    rv[0] = getImgWithDefaults("ImgFirst", "pagination_first.gif",
       "awesome:fast-backward", "", "glyphicon:collapse-up", INT.I("Prima pagina"));
    rv[1] = getImgWithDefaults("ImgLast", "pagination_last.gif",
       "awesome:fast-forward", "", "glyphicon:collapse-up", INT.I("Ultima pagina"));
    rv[2] = getImgWithDefaults("ImgPrev", "pagination_prev.gif",
       "awesome:step-backward", "", "glyphicon:collapse-up", INT.I("Pagina precedente"));
    rv[3] = getImgWithDefaults("ImgNext", "pagination_next.gif",
       "awesome:step-forward", "", "glyphicon:collapse-up", INT.I("Pagina successiva"));
    return rv;
  }

  protected String getSC(String key, String defVal)
     throws Exception
  {
    return getConfiguration().getString(key, defVal);
  }

  protected String getImgHtml(String imgName, String tip)
     throws Exception
  {
    return "<img src=\"" + getImageUrl(imgName)
       + " \" alt=\"" + tip + "\" tip=\"" + tip + "\" title=\"" + tip + "\" border=\"0\">";
  }

  /**
   * Ritorna l'url completa dell'immagine (http://.../images/nomeima),
   * prelevandola dalla directory images dell'applicazione web.
   * Viene applicato lo skin corrente.
   * @param nomeima nome dell'immagine
   * @return la URL relativa completa
   */
  protected String getImageUrl(String nomeima)
  {
    return ui.image(nomeima);
  }

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario
   * @param nomeCampo nome HTML del campo
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  @Override
  public String getCampoData(String nomeCampo, String nomeForm, String valore, int size)
     throws Exception
  {
    String sds = URLEncoder.encode("restartd_" + nomeCampo, "UTF-8");

    return "<input name=\"" + nomeCampo + "\" size=\"" + size + "\" value=\"" + StringOper.okStr(valore) + "\">&nbsp;"
       + "<a href=\"javascript:apriCalendario('" + sds + "')\">"
       + getImgEditData() + "</a>\r\n"
       + "<script LANGUAGE=\"JavaScript\">\r\n"
       + "function restartd_" + nomeCampo + "(strdate) {\r\n"
       + "      document." + nomeForm + "." + nomeCampo + ".value = strdate;\r\n"
       + "      chiudiCalendario();\r\n"
       + "}\r\n"
       + "</SCRIPT>\r\n";
  }

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario.
   * Il campo generato verra' utilizzato con il suo gemello generato da
   * 'getCampoDataIntervalloFine' che genera il campo finale dell'intervallo.
   * @param nomeCampoInizio nome HTML del campo di inizio intervallo
   * @param nomeCampoFine nome HTML del campo di fine intervallo
   * @param nomeForm nome del form
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo di inizio intervallo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  @Override
  public String getCampoDataIntervalloInizio(String nomeCampoInizio, String nomeCampoFine, String nomeForm, String valore, int size)
     throws Exception
  {
    String sds = URLEncoder.encode("restartd_" + nomeCampoInizio, "UTF-8");
    String sdf = URLEncoder.encode("restartd_" + nomeCampoInizio + "_" + nomeCampoFine, "UTF-8");

    return "<input id=\"" + nomeCampoInizio + "\" name=\"" + nomeCampoInizio + "\" size=\"" + size + "\" value=\"" + StringOper.okStr(valore) + "\">&nbsp;"
       + "<a href=\"javascript:apriCalendarioIntervallo('" + sds + "','" + sdf + "')\">"
       + getImgEditData() + "</a>\r\n"
       + "<script LANGUAGE=\"JavaScript\">\r\n"
       + "function restartd_" + nomeCampoInizio + "(strdate) {\r\n"
       + "      document." + nomeForm + "." + nomeCampoInizio + ".value = strdate;\r\n"
       + "      chiudiCalendario();\r\n"
       + "}\r\n"
       + "function restartd_" + nomeCampoInizio + "_" + nomeCampoFine + "(ss) {\r\n"
       + "      var idx = ss.indexOf(\"|\");\r\n"
       + "      var s1  = ss.substring( 0, idx);\r\n"
       + "      var s2  = ss.substring(idx+1);\r\n"
       + "      document." + nomeForm + "." + nomeCampoInizio + ".value = s1;\r\n"
       + "      document." + nomeForm + "." + nomeCampoFine + ".value = s2;\r\n"
       + "      chiudiCalendario();\r\n"
       + "}\r\n"
       + "</SCRIPT>\r\n";
  }

  /**
   * Ritorna i tags HTML necessari per un campo data con l'ausilio del calendario
   * Il campo generato verra' utilizzato con il suo gemello generato da
   * 'getCampoDataIntervalloInizio' che genera il campo iniziale dell'intervallo.
   * @param nomeCampoInizio nome HTML del campo di inizio intervallo
   * @param nomeCampoFine nome HTML del campo di fine intervallo
   * @param nomeForm nome del form
   * @param valore eventuale valore di default (puo' essere null)
   * @param size dimensione richiesta
   * @return l'HTML completo del campo di fine intervallo e del javascript per l'editing
   * @throws java.lang.Exception
   */
  @Override
  public String getCampoDataIntervalloFine(String nomeCampoInizio, String nomeCampoFine, String nomeForm, String valore, int size)
     throws Exception
  {
    String sds = URLEncoder.encode("restartd_" + nomeCampoFine, "UTF-8");
    String sdf = URLEncoder.encode("restartd_" + nomeCampoFine + "_" + nomeCampoInizio, "UTF-8");

    return "<input id=\"" + nomeCampoFine + "\" name=\"" + nomeCampoFine + "\" size=\"" + size + "\" value=\"" + StringOper.okStr(valore) + "\">&nbsp;"
       + "<a href=\"javascript:apriCalendarioIntervallo('" + sds + "','" + sdf + "')\">"
       + getImgEditData() + "</a>\r\n"
       + "<script LANGUAGE=\"JavaScript\">\r\n"
       + "function restartd_" + nomeCampoFine + "(strdate) {\r\n"
       + "      document." + nomeForm + "." + nomeCampoFine + ".value = strdate;\r\n"
       + "      chiudiCalendario();\r\n"
       + "}\r\n"
       + "function restartd_" + nomeCampoFine + "_" + nomeCampoInizio + "(ss) {\r\n"
       + "      var idx = ss.indexOf(\"|\");\r\n"
       + "      var s1  = ss.substring( 0, idx);\r\n"
       + "      var s2  = ss.substring(idx+1);\r\n"
       + "      document." + nomeForm + "." + nomeCampoInizio + ".value = s1;\r\n"
       + "      document." + nomeForm + "." + nomeCampoFine + ".value = s2;\r\n"
       + "      chiudiCalendario();\r\n"
       + "}\r\n"
       + "</SCRIPT>\r\n";
  }

  /**
   * Costruisce un campo di edit con la funzione di ricerca classica.
   * @param nomeCampo nome del campo generato
   * @param valore valore di default all'interno del campo
   * @param size dimensione del campo
   * @param url url per l'editing del campo (lista dei valori)
   * @param valForeign valore per il foreign (se null no descrizione foreign)
   * @param extraScript evantuale extrascript (puo' essere null)
   * @return l'HTML completo del campo e del javascript per l'editing
   */
  @Override
  public String getCampoForeign(String nomeCampo, String valore, int size, String url,
     String valForeign, String extraScript)
     throws Exception
  {
    if(valForeign == null)
      return getForeignEditFld(nomeCampo,
         "<input type=\"text\" name=\"" + nomeCampo + "\" value=\"" + valore + "\" size=\"" + size + "\">\r\n",
         url, extraScript);
    else
      return getForeignEditDescr(nomeCampo,
         "<input type=\"text\" name=\"" + nomeCampo + "\" value=\"" + valore + "\" size=\"" + size + "\">\r\n",
         valForeign,
         url, extraScript);
  }

  /**
   * Ritorna la porzione di javascript necessaria a consentire l'editing
   * di dati esterni.
   * @param fldName nome del campo
   * @return il codice javascript
   */
  protected String getScriptEdit(String fldName, boolean conDescrizione, String extraScript)
  {
    if(conDescrizione)
    {
      return "\r\n"
         + "<SCRIPT LANGUAGE=\"JavaScript\">\r\n"
         + "  function imposta_" + fldName + "(codice, descri)\r\n"
         + "  {\r\n"
         + "    document.fo." + fldName + ".value=codice;\r\n"
         + "    document.getElementById('" + fldName + "_ED').childNodes[0].nodeValue=descri;\r\n"
         + (extraScript == null ? "" : extraScript)
         + "  }\r\n"
         + "</SCRIPT>\r\n"
         + "\r\n";
    }
    else
    {
      return "\r\n"
         + "<SCRIPT LANGUAGE=\"JavaScript\">\r\n"
         + "  function imposta_" + fldName + "(codice, descri)\r\n"
         + "  {\r\n"
         + "    document.fo." + fldName + ".value=codice;\r\n"
         + (extraScript == null ? "" : extraScript)
         + "  }\r\n"
         + "</SCRIPT>\r\n"
         + "\r\n";
    }
  }

  protected String getForeignEditFld(String fldName, String valore,
     String url, String extraScript)
     throws Exception
  {
    return "\r\n"
       + "<table border=0 cellspacing=0 cellpadding=0><tr>\r\n"
       + "<td>" + valore + "</td>\r\n"
       + "<td>&nbsp;<a href=\"" + buildUrl(url, fldName) + "\">" + getImgEditForeign() + "</a>"
       + "&nbsp;</td>\r\n"
       + "</tr></table>\r\n"
       + getScriptEdit(fldName, false, extraScript);
  }

  protected String getForeignEditDescr(String fldName, String valore, String valForeign,
     String url, String extraScript)
     throws Exception
  {
    return "\r\n"
       + "<table border=0 cellspacing=0 cellpadding=0><tr>\r\n"
       + "<td>" + valore + "</td>\r\n"
       + "<td>&nbsp;<a href=\"" + buildUrl(url, fldName) + "\">" + getImgEditForeign() + "</a>"
       + "&nbsp;</td>\r\n"
       + "<td><div id=\"" + fldName + "_ED\" style=\"font-weight: bold;\">"
       + valForeign + "&nbsp;</div></td>\r\n"
       + "</tr></table>\r\n"
       + getScriptEdit(fldName, true, extraScript);
  }

  protected String buildUrl(String url, String fldName)
     throws Exception
  {
    String sds = "func=" + URLEncoder.encode("imposta_" + fldName, "UTF-8");

    // verfica per url con macro
    int pos;
    if((pos = url.indexOf("@@@")) != -1)
    {
      return url.substring(0, pos) + sds + url.substring(pos + 3);
    }

    return url + sds;
  }

  @Override
  public void forceReloadXML()
     throws Exception
  {
    doc = buildDocument();
    pwm.setDocument(doc);
    swm.setDocument(doc);
  }

  public Document buildDocument()
     throws Exception
  {
    if(vFilesXml.size() == 1)
    {
      File fxml = (File) vFilesXml.get(0);

      log.info("Leggo " + fxml.getAbsolutePath());
      SAXBuilder builder = new SAXBuilder();
      return builder.build(fxml);
    }

    // crea una hastable per il livello principale
    HashMap<String, Map<String, Element>> htMainLevel = new HashMap<>();

    // scorre i files per attivare la fusione
    for(int i = 0; i < vFilesXml.size(); i++)
    {
      File fxml = (File) vFilesXml.get(i);

      log.info("Merge multipli XML dal file " + fxml.getAbsolutePath());
      SAXBuilder builder = new SAXBuilder();
      Document d = builder.build(fxml);

      List tipiListe = d.getRootElement().getChildren();
      for(Iterator itTls = tipiListe.iterator(); itTls.hasNext();)
      {
        // a questo livello siamo a <liste></liste> ... <forms></forms>
        Element e = (Element) itTls.next();
        String ename = e.getName();

        // recupera o crea una hash table per il livello elementi
        Map<String, Element> htItemLevel = htMainLevel.get(ename);
        if(htItemLevel == null)
        {
          htItemLevel = new HashMap<>();
          htMainLevel.put(ename, htItemLevel);
        }

        // aggiunge tutte le liste contenute <clienti></clienti> <indirizzi></indirizzi>
        List liste = e.getChildren();
        for(Iterator itLst = liste.iterator(); itLst.hasNext();)
        {
          Element l = (Element) itLst.next();
          String iname = l.getName();

          // scarta i duplicati
          if(htItemLevel.get(iname) == null)
            htItemLevel.put(iname, l.clone());
        }
      }
    }

    // nuovo elemento root
    Element root = new Element("root");

    // aggiunge gli elementi memorizzati nelle hashtable
    for(Map.Entry<String, Map<String, Element>> mmain : htMainLevel.entrySet())
    {
      String ename = mmain.getKey();
      Map<String, Element> htItemLevel = mmain.getValue();

      Element elista = new Element(ename);
      root.addContent(elista);

      for(Map.Entry<String, Element> mlista : htItemLevel.entrySet())
      {
        String iname = mlista.getKey();
        Element eitem = mlista.getValue();
        elista.addContent(eitem);
      }
    }

    // crea il documento fusione di tutti i files xml passati
    Document mergeDoc = new Document();
    mergeDoc.setRootElement(root);
    return mergeDoc;
  }

  @Override
  public Document getDocument()
  {
    return doc;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  // liste basate sui Peer (Torque)
  //
  @Override
  public PeerWrapperListaHtml getListaPeer(String nomeLista)
     throws Exception
  {
    tryInitRigelPath();
    return pwm.getLista(nomeLista);
  }

  @Override
  public PeerWrapperEditHtml getListaEditPeer(String nomeLista)
     throws Exception
  {
    tryInitRigelPath();
    return pwm.getListaEdit(nomeLista);
  }

  @Override
  public PeerWrapperFormHtml getFormPeer(String nomeForm)
     throws Exception
  {
    tryInitRigelPath();
    return pwm.getForm(nomeForm);
  }

  @Override
  public PeerWrapperListaXml getListaXmlPeer(String nomeLista)
     throws Exception
  {
    tryInitRigelPath();
    try
    {
      return pwm.getListaXml(nomeLista, true);
    }
    catch(MissingListException | MissingSectionException ex1)
    {
      return pwm.getListaXmlFromListe(nomeLista, true);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  // liste basate su query libere (SQL)
  //
  @Override
  public SqlWrapperListaHtml getListaSql(String nomeLista)
     throws Exception
  {
    tryInitRigelPath();
    return swm.getLista(nomeLista);
  }

  @Override
  public SqlWrapperFormHtml getFormSql(String nomeForm)
     throws Exception
  {
    tryInitRigelPath();
    return swm.getForm(nomeForm);
  }

  @Override
  public SqlWrapperListaXml getListaXmlSql(String nomeLista)
     throws Exception
  {
    tryInitRigelPath();
    try
    {
      return swm.getListaXml(nomeLista, true);
    }
    catch(MissingListException | MissingSectionException ex1)
    {
      return swm.getListaXmlFromListe(nomeLista, true);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  // liste basate sulle tablemap (Torque)
  //
  @Override
  public PeerWrapperListaHtml getListaTmap(String nomeTabella)
     throws Exception
  {
    tryInitRigelPath();
    return twm.getLista(nomeTabella);
  }

  @Override
  public PeerWrapperEditHtml getListaEditTmap(String nomeTabella)
     throws Exception
  {
    tryInitRigelPath();
    return twm.getListaEdit(nomeTabella);
  }

  @Override
  public PeerWrapperFormHtml getFormTmap(String nomeTabella)
     throws Exception
  {
    tryInitRigelPath();
    return twm.getForm(nomeTabella);
  }

  @Override
  public WrapperCacheBase getWrapperCache(RunData data)
  {
    CoreTurbineWrapperCache rv = (CoreTurbineWrapperCache) data.getSession().getAttribute(WrapperCacheBaseKey);

    if(rv == null)
    {
      rv = new CoreTurbineWrapperCache();
      rv.init(data);
      data.getSession().setAttribute(WrapperCacheBaseKey, rv);
    }

    return rv;
  }

  @Override
  public void removeWrapperCache(RunData data)
  {
    data.getSession().removeAttribute(WrapperCacheBaseKey);
  }

  protected void caricaListaSql()
     throws Exception
  {
    Element listeSql = doc.getRootElement().getChild("liste-sql");
    if(listeSql == null)
      die("Nessuna lista SQL caricata.");
    for(Element e : listeSql.getChildren())
    {
      String nomeLista = e.getName();
      if(nomeLista.startsWith("Ls"))
        arListeSql.add(nomeLista);
    }
  }

  @Override
  public List<String> getListeSql()
  {
    return Collections.unmodifiableList(arListeSql);
  }
}
