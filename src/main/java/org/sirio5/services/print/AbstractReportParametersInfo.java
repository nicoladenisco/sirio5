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
package org.sirio5.services.print;

import com.workingdogs.village.Record;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.commonlib5.utils.CommonFileUtils;
import org.commonlib5.utils.Pair;
import org.rigel5.SetupHolder;
import org.rigel5.db.DbUtils;
import org.rigel5.db.sql.QueryBuilder;
import org.sirio5.services.localization.INT;
import org.sirio5.services.print.parametri.ParametroBuilder;
import org.sirio5.services.print.parametri.ParametroBuilderFactory;
import org.sirio5.services.security.SEC;
import org.sirio5.utils.DT;
import org.sirio5.utils.SU;
import org.sirio5.utils.SirioMacroResolver;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Oggetto report per le statistiche.
 * Mantiene i parametri di un report sotto forma
 * di un mapping fra i nomi e i tipi di parametro.
 * Implementa DefaultHandler per eseguire il
 * parsing di un report generato da JReport.
 *
 * @author Nicola De Nisco
 */
abstract public class AbstractReportParametersInfo extends DefaultHandler
{
  /** Logging */
  private static final Log log = LogFactory.getLog(AbstractReportParametersInfo.class);
  /** variabili locali */
  private int idUser;
  private PrintContext context;
  private File originalFile = null;
  private String nomeStampa = null;
  private boolean internal = false;
  private final ArrayList<ParameterInfo> filtriStatistiche = new ArrayList<>();
  private final PdfPrint pp = (PdfPrint) (TurbineServices.getInstance().
     getService(PdfPrint.SERVICE_NAME));

  public static final String LOCAL_JASPER_DTD = "jasperreport.dtd";

  public int getIdUser()
  {
    return idUser;
  }

  public PrintContext getContext()
  {
    return context;
  }

  /**
   * Costruttore per il parsing di un file report di jasper.
   * @param idUser
   * @param reportName
   * @param originalFile
   * @param context
   * @throws Exception
   */
  public void initForJasper(int idUser, String reportName, File originalFile, PrintContext context)
     throws Exception
  {
    internal = false;
    this.idUser = idUser;
    this.originalFile = originalFile;
    this.nomeStampa = reportName;
    this.context = context;
    loadJasperReport();
  }

  /**
   * Legge il file XML del report jasper per estrarre
   * i parametri con i loro tipi.
   * @throws Exception
   */
  protected void loadJasperReport()
     throws Exception
  {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    FileInputStream fis = null;

    try
    {
      SAXParser parserReport = spf.newSAXParser();
      XMLReader xmlReportReader = parserReport.getXMLReader();
      xmlReportReader.setContentHandler(this);
      xmlReportReader.setEntityResolver(new EntityResolver()
      {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
           throws SAXException, IOException
        {
          if(systemId.startsWith("http://jasperreports.sourceforge.net/dtds/"))
          {
            String nomeFile = systemId.substring(41);
            File fDTD = pp.getConfReportFile(nomeFile);
            if(fDTD.exists())
              systemId = "file:" + fDTD.getAbsolutePath();
          }

          return new InputSource(systemId);
        }
      });

      fis = new FileInputStream(originalFile);
      xmlReportReader.parse(new InputSource(fis));
    }
    catch(Exception ex)
    {
      log.error(INT.I("Report %s", originalFile.getAbsolutePath()), ex);

      // risolleva l'eccezione per il chiamante
      throw ex;
    }
    finally
    {
      CommonFileUtils.safeClose(fis);
    }
  }

  /**
   * Costruttore per altri usi che non siano quelli di jasper.
   * @param idUser
   * @param reportName
   * @param reportInfo
   * @param context
   * @throws Exception
   */
  public void initGeneric(int idUser, String reportName, String reportInfo, PrintContext context)
     throws Exception
  {
    internal = true;
    originalFile = null;
    nomeStampa = reportName;
    this.idUser = idUser;
    this.context = context;
    buildFromDescriptor(reportInfo);
  }

  @Override
  public void characters(char[] ch, int start, int length)
     throws SAXException
  {
  }

  @Override
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
  {
    try
    {
      if(SU.isEquNocase("parameter", qName))
      {
        String sTmp = null;

        if((sTmp = atts.getValue("name")) == null)
          return;
        String nomeParametro = sTmp;

        boolean isForPrompting = true;
        if((sTmp = atts.getValue("isForPrompting")) != null)
          isForPrompting = SU.checkTrueFalse(sTmp);

        Class tipoPararmetro = String.class;
        if((sTmp = atts.getValue("class")) != null)
          tipoPararmetro = Class.forName(sTmp);

        if(isForPrompting)
        {
          ParameterInfo fs = buildParametro(nomeParametro, tipoPararmetro);

          if(nomeParametro.endsWith("In"))
          {
            // inizio intervallo: cerca un fine intervallo
            String gemello = nomeParametro.substring(0, nomeParametro.length() - 2) + "Fin";
            ParameterInfo fsGemello = cercaFiltro(gemello);
            if(fsGemello != null)
            {
              fs.setNomeGemello(gemello);
              fs.setIntervalloInizio(true);
              fsGemello.setIntervalloFine(true);
              fsGemello.setNomeGemello(nomeParametro);
            }
          }
          if(nomeParametro.endsWith("_i"))
          {
            // inizio intervallo: cerca un fine intervallo
            String gemello = nomeParametro.substring(0, nomeParametro.length() - 2) + "_f";
            ParameterInfo fsGemello = cercaFiltro(gemello);
            if(fsGemello != null)
            {
              fs.setNomeGemello(gemello);
              fs.setIntervalloInizio(true);
              fsGemello.setIntervalloFine(true);
              fsGemello.setNomeGemello(nomeParametro);
            }
          }

          if(nomeParametro.endsWith("Fin"))
          {
            // inizio intervallo: cerca un fine intervallo
            String gemello = nomeParametro.substring(0, nomeParametro.length() - 3) + "In";
            ParameterInfo fsGemello = cercaFiltro(gemello);
            if(fsGemello != null)
            {
              fs.setNomeGemello(gemello);
              fs.setIntervalloFine(true);
              fsGemello.setIntervalloInizio(true);
              fsGemello.setNomeGemello(nomeParametro);
            }
          }
          if(nomeParametro.endsWith("_f"))
          {
            // inizio intervallo: cerca un fine intervallo
            String gemello = nomeParametro.substring(0, nomeParametro.length() - 2) + "_i";
            ParameterInfo fsGemello = cercaFiltro(gemello);
            if(fsGemello != null)
            {
              fs.setNomeGemello(gemello);
              fs.setIntervalloFine(true);
              fsGemello.setIntervalloInizio(true);
              fsGemello.setNomeGemello(nomeParametro);
            }
          }

          filtriStatistiche.add(fs);
        }
      }
    }
    catch(Exception e)
    {
      log.error("Parsing XML: " + originalFile.getAbsolutePath(), e);
    }
  }

  /**
   * Cerca un parametro per nome.
   * @param nomeParametro
   * @return
   */
  protected ParameterInfo cercaFiltro(String nomeParametro)
  {
    for(int i = 0; i < filtriStatistiche.size(); i++)
    {
      ParameterInfo fs = (ParameterInfo) filtriStatistiche.get(i);
      if(SU.isEqu(nomeParametro, fs.getNome()))
        return fs;
    }
    return null;
  }

  public void endElement(String name)
     throws SAXException
  {
  }

  @Override
  public void endDocument()
  {
  }

  public List<ParameterInfo> getFiltriStatistiche()
  {
    return Collections.unmodifiableList(filtriStatistiche);
  }

  public void setFiltriStatistiche(Collection<ParameterInfo> filtriStatistiche)
  {
    this.filtriStatistiche.clear();
    this.filtriStatistiche.addAll(filtriStatistiche);
  }

  public File getFile()
  {
    return originalFile;
  }

  public String getNomeStampa()
  {
    return nomeStampa;
  }

  public boolean isInternal()
  {
    return internal;
  }

  /**
   * Produce la lista valori utilizzata per popolare il combo
   * quando richesta un parametro con scelta fra valori definiti
   * da una tabella esistente.
   * @param selectValPar
   * @param fromValPar
   * @param orderByValPar
   * @return
   * @throws java.lang.Exception
   */
  protected List<Record> populateComboData(String selectValPar, String fromValPar, String orderByValPar)
     throws Exception
  {
    try
    {
      // primo tentativo: usa lo stato_rec per elimiare i cancellati
      QueryBuilder qb = SetupHolder.getQueryBuilder();

      qb.setSelect(selectValPar);
      qb.setFrom(fromValPar);
      qb.setWhere("((STATO_REC IS NULL) OR (STATO_REC<10))");
      qb.setOrderby(orderByValPar);
      qb.setLimit(100);

      String queryString = qb.makeSQLstring();
      return DbUtils.executeQuery(queryString);
    }
    catch(Exception e)
    {
      // secondo tentativo: ripete la query senza lo stato_rec
      QueryBuilder qb = SetupHolder.getQueryBuilder();

      qb.setSelect(selectValPar);
      qb.setFrom(fromValPar);
      qb.setOrderby(orderByValPar);
      qb.setLimit(100);

      String queryString = qb.makeSQLstring();
      return DbUtils.executeQuery(queryString);
    }
  }

  protected void buildFromCommonInfo(CommonInfoParametro p, ParameterInfo fs, Class tipoParametro)
     throws Exception
  {
    if(p.tabellaValori != null)
    {
      buildFromTabella(p.tabellaValori, p.campoChiave, p.campoValore, fs, tipoParametro);
    }

    if(p.valori != null)
    {
      if(p.valori.indexOf(';') == -1)
      {
        // valore di default del parametro
        fs.setValoreDefault(p.valori);
      }
      else
      {
        // valori statici
        StringTokenizer stValori = new StringTokenizer(p.valori, ";");
        while(stValori.hasMoreElements())
        {
          String valore = (String) stValori.nextElement();
          // test per valore di default (incomincia con *)
          if(valore.charAt(0) == '*')
          {
            valore = valore.substring(1);
            fs.setValoreDefault(valore);
          }
          fs.addListaValori(valore, valore);
        }
      }
    }
  }

  /**
   * Carica i valori del parametro da db o da un plugin.
   * @param tabellaValori
   * @param campoChiave
   * @param campoValore
   * @param fs
   * @param tipoParametro
   * @throws Exception
   */
  protected void buildFromTabella(
     String tabellaValori, String campoChiave, String campoValore,
     ParameterInfo fs, Class tipoParametro)
     throws Exception
  {
    if(tabellaValori.startsWith("plugin:"))
    {
      String pluginName = tabellaValori.substring(7);
      ParametroBuilder plugin = ParametroBuilderFactory.getInstance().getPlugin(pluginName);
      List<Pair<String, String>> valoriPlugin = plugin.preparaValori(this, tipoParametro);

      for(Pair<String, String> v : valoriPlugin)
      {
        String valore = v.first;
        // test per valore di default (incomincia con *)
        if(valore.charAt(0) == '*')
        {
          valore = valore.substring(1);
          fs.setValoreDefault(valore);
          fs.addListaValori(valore, v.second);
        }
        else
        {
          fs.addListaValori(v.first, v.second);
        }
      }
    }
    else
    {
      String selectValPar = campoChiave + " , " + campoValore;
      String fromValPar = tabellaValori;

      List<Record> recordsValPar = populateComboData(selectValPar, fromValPar, campoValore);

      for(Record valParRec : recordsValPar)
      {
        String valCampoChiave = valParRec.getValue(1).asString();
        String valCampoDescrizione = valParRec.getValue(2).asString();
        fs.addListaValori(valCampoChiave, valCampoDescrizione);
      }
    }
  }

  /**
   * Costruisce i dati di un parametro della stampa.
   * Viene interrogata la tabella sys_parametristatistiche
   * per determinare come dovrà essere utilizzato questo
   * parametro. Se è un combo vengono caricati tutti
   * i valori possibili dalla relativa tabella oppure
   * per i self-combo dal campo valore di sys_par...
   * @param nomeParametro
   * @param tipoParametro
   * @return
   * @throws java.lang.Exception
   */
  abstract public ParameterInfo buildParametro(String nomeParametro, Class tipoParametro)
     throws Exception;

  /**
   * Parsing di un descrittore per stampa interna.
   * @param descriptor descrittore nella forma
   * Dpar1|Ipar2
   * @throws java.lang.Exception
   */
  public void buildFromDescriptor(String descriptor)
     throws Exception
  {
    String[] descparams = SU.split(descriptor, '|');
    if(descparams == null || descparams.length == 0)
      return;

    User u = SEC.getUser(idUser);
    SirioMacroResolver mres = new SirioMacroResolver(u);
    if(context != null && !context.isEmpty())
      mres.putAll(context);

    int pos;
    Class tiClass = null;
    String nome, valDef;

    for(int i = 0; i < descparams.length; i++)
    {
      String campo = SU.okStrNull(descparams[i]);
      if(campo == null)
        continue;

      char tipo = campo.charAt(0);

      if((pos = campo.indexOf('=')) != -1)
      {
        nome = campo.substring(1, pos);
        valDef = mres.resolveMacro(campo.substring(pos + 1));
      }
      else
      {
        nome = campo.substring(1);
        valDef = "";
      }

      switch(tipo)
      {
        case 'E':
          tiClass = java.util.Date.class;
          ParameterInfo fsInizio = buildParametro(nome + "_i", tiClass);
          ParameterInfo fsFine = buildParametro(nome + "_f", tiClass);
          fsInizio.setIntervalloInizio(true);
          fsInizio.setNomeGemello(nome + "_f");
          fsFine.setIntervalloFine(true);
          fsFine.setNomeGemello(nome + "_i");
          filtriStatistiche.add(fsInizio);
          filtriStatistiche.add(fsFine);
          continue;
        case 'H':
        {
          // valore da non inserire nel form ma necessario per l'elaborazione
          tiClass = java.lang.String.class;
          ParameterInfo fsHidden = buildParametro(nome, tiClass);
          fsHidden.setHidden(true);
          fsHidden.setValoreDefault(valDef);
          filtriStatistiche.add(fsHidden);
          continue;
        }
        case 'D':
          tiClass = java.util.Date.class;
          break;
        case 'T':
          tiClass = java.sql.Timestamp.class;
          break;
        case 'N':
          tiClass = java.lang.Number.class;
          break;
        case 'S':
          tiClass = java.lang.String.class;
          break;
        case 'I':
          tiClass = java.lang.Integer.class;
          break;
        case 'B':
          tiClass = java.lang.Boolean.class;
          break;
        default:
        {
          // valore da non inserire nel form ma necessario per l'elaborazione
          nome = pos == -1 ? campo : campo.substring(0, pos);
          tiClass = java.lang.String.class;
          ParameterInfo fsHidden = buildParametro(nome, tiClass);
          fsHidden.setHidden(true);
          fsHidden.setValoreDefault(valDef);
          filtriStatistiche.add(fsHidden);
          continue;
        }
      }

      ParameterInfo fs = buildParametro(nome, tiClass);
      fs.setValoreDefault(valDef);
      filtriStatistiche.add(fs);
    }
  }

  /**
   * Legge la map dei parametri HTML (quella generata da un POST)
   * e ritorna una map con i parametri del report e il relativo
   * tipo java opportunamente parsato dal valore string contenuto
   * nella map di ingresso.
   * @param params parametri della POST (solo stringhe)
   * @return map parametro form abbinato al tipo java corrispondente
   * @throws Exception
   */
  public Map parseParameter(Map params)
     throws Exception
  {
    Map rvParams = new HashMap();

    for(ParameterInfo fs : filtriStatistiche)
    {
      Object obj = params.get(fs.getNome());
      if(obj == null)
        continue;

      if((obj = parseOne(fs, obj)) == null)
        continue;

      rvParams.put(fs.getNome(), obj);
    }

    return rvParams;
  }

  public Map parseParameterString(Map params)
     throws Exception
  {
    Map<String, String> rvParams = new HashMap<>();

    for(ParameterInfo fs : filtriStatistiche)
    {
      Object obj = params.get(fs.getNome());
      if(obj == null)
        continue;

      if((obj = parseOne(fs, obj)) == null)
        continue;

      String val;
      if(fs.isDate())
        val = DT.formatIso((Date) obj, null);
      else if(fs.isTimeStamp())
        val = DT.formatIsoFull((Date) obj, null);
      else
        val = obj.toString();

      if(val != null)
        rvParams.put(fs.getNome(), val);
    }

    return rvParams;
  }

  private Object parseOne(ParameterInfo fs, Object obj)
     throws Exception
  {
    String val = obj.toString();
    if(val == null)
      return null;

    if(fs.isDate() || fs.isTimeStamp())
    {
      if(!(obj instanceof Date))
        obj = DT.parseData(val);

      if(fs.isIntervalloInizio())
      {
        obj = DT.inizioGiorno((Date) obj);
      }
      else if(fs.isIntervalloFine())
      {
        obj = DT.fineGiorno((Date) obj);
      }

      if(fs.isTimeStamp())
      {
        long time = ((Date) (obj)).getTime();
        obj = new Timestamp(time);
      }
    }
    else if(fs.isInteger())
    {
      if(!(obj instanceof Integer))
        obj = SU.parse(val, 0);
    }
    else if(fs.isBoolean())
    {
      if(!(obj instanceof Boolean))
        obj = SU.checkTrueFalse(obj);
    }
    else
    {
      obj = val;
    }

    return obj;
  }

  @Override
  public String toString()
  {
    return filtriStatistiche.toString();
  }

  abstract public String getPlugin();

  abstract public String getNome();

  abstract public String getInfo();

  abstract public String getDataMaker();

  public static class CommonInfoParametro
  {
    public String tabellaValori, campoChiave, campoValore, valori;
  }
}
