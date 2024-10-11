/*
 *  VelocityParser.java
 *
 *  Creato il 9 Luglio 2017
 *
 *  Copyright (C) 2017 RAD-IMAGE s.r.l.
 *
 *  RAD-IMAGE s.r.l.
 *  Via San Giovanni, 1 - Contrada Belvedere
 *  San Nicola Manfredi (BN)
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
package org.sirio5.utils.velocity;

import java.io.*;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.velocity.VelocityService;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * Parsing di un file (tipo vm) secondo la sintassi
 * del velocity engine con sostituzione delle macro
 * secondo il relativo context.
 * Utilizza il Velocity Engine standard di Turbine.
 * Viene utilizzata per generare file temporanei a
 * partire da modelli generici.
 * @author Nicola De Nisco
 */
public class VelocityParser
{
  protected final Context ctx;

  /**
   * Costruttore.
   * La directory dei modelli sarà webapp/templates/app
   * @param ctx contesto di risoluzione velocity
   */
  public VelocityParser(Context ctx)
  {
    this(ctx, Turbine.getRealPath("templates/app"));
  }

  /**
   * Costruttore.
   * @param ctx contesto di risoluzione velocity
   * @param templatePath la directory dove cercare i modelli
   */
  public VelocityParser(Context ctx, String templatePath)
  {
    this.ctx = ctx;
    Velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
  }

  /**
   * Parsing di un modello con output nel writer specificato.
   * @param fileModello il file modello con sintassi Velocity
   * @param writer il ricevitore dell'output
   * @throws java.lang.Exception
   */
  public void parseFile(String fileModello, Writer writer)
     throws Exception
  {
    Template t = Velocity.getTemplate(fileModello);
    mergeTemplate(t, writer);
  }

  protected void mergeTemplate(Template t, Writer writer)
     throws Exception
  {
    synchronized(ctx)
    {
      EventCartridge oldEventCartridge = null;

      try
      {
        oldEventCartridge = ((InternalEventContext) ctx).getEventCartridge();
        EventCartridge ec = new EventCartridge();
        ec.attachToContext(ctx);

        t.merge(ctx, writer);
      }
      finally
      {
        if(oldEventCartridge != null)
          oldEventCartridge.attachToContext(ctx);
      }
    }
  }

  /**
   * Parsing di un modello con output in una stringa.
   * @param fileModello il file modello con sintassi Velocity
   * @return la stringa con le modifiche apportate
   * @throws java.lang.Exception
   */
  public String parseFileToString(String fileModello)
     throws Exception
  {
    StringWriter sw = new StringWriter();
    parseFile(fileModello, sw);
    return sw.toString();
  }

  /**
   * Parsing di un modello con output nel file specificato.
   * @param fileModello il file modello con sintassi Velocity
   * @param output il file che riceve l'output
   * @throws java.lang.Exception
   */
  public void parseFileToFile(String fileModello, File output)
     throws Exception
  {
    try(FileWriter fw = new FileWriter(output))
    {
      parseFile(fileModello, fw);
      fw.flush();
    }
  }

  /**
   * Crea un oggetto Template a partire da un reader generico.
   * L'oggetto Tenmplate potrà poi essere trasformato con il
   * suo metodo merge(context, writer).
   * @param reader sorgente dati
   * @param templateName nome del file template
   * @return oggetto Template
   * @throws Exception
   */
  public Template createTemplateFromReader(Reader reader, String templateName)
     throws Exception
  {
    RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
    Template template = new Template();
    SimpleNode node = runtimeServices.parse(reader, template);
    template.setRuntimeServices(runtimeServices);
    template.setData(node);
    template.initDocument();
    return template;
  }

  /**
   * Parsing di una stringa contenente macro velocity.
   * La stringa viene utilizzata come sorgente per il
   * parsing e il risultato viene restituitso sotto
   * forma di stringa.
   * @param inputstring stringa con macro velocity
   * @return stringa convertita
   * @throws Exception
   */
  public String parseString(String inputstring)
     throws Exception
  {
    StringReader reader = new StringReader(inputstring);
    StringWriter writer = new StringWriter(512);
    parseReader(reader, writer, "VelocityParser.parseString");
    return writer.toString();
  }

  /**
   * Parsing di un generico input verso un generico output.
   * @param reader template da interpretare
   * @param writer output dell'elaborazione
   * @param templateName nome del template (per messaggi d'errore)
   * @throws Exception
   */
  public void parseReader(Reader reader, Writer writer, String templateName)
     throws Exception
  {
    Template template = createTemplateFromReader(reader, templateName);
    mergeTemplate(template, writer);
  }

  public static Context createNewContext()
  {
    VelocityService velocity = (VelocityService) TurbineServices
       .getInstance().getService(VelocityService.SERVICE_NAME);
    return velocity.getNewContext();
  }
}
