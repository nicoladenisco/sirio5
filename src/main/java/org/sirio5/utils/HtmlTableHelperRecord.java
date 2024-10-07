/*
 * Copyright (C) 2023 Nicola De Nisco
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
package org.sirio5.utils;

import com.workingdogs.village.Record;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.commonlib5.lambda.FunctionTrowException;
import org.commonlib5.utils.HtmlTableHelper;
import org.commonlib5.utils.Pair;

/**
 * Classe di utilità per formattare dati in una tabella html.
 * Versione specializzata per oggetti di Torque.
 * Estrae html da una lista di oggetti con interfaccia ColumnAccessByName.
 *
 * @author Nicola De Nisco
 */
public class HtmlTableHelperRecord extends HtmlTableHelper
{
  protected final List<Pair<String, FunctionTrowException<Record, String>>> campi = new ArrayList<>();

  public int addField(String caption, FunctionTrowException<Record, String> fun)
  {
    campi.add(new Pair<>(caption, fun));
    return campi.size();
  }

  public void addRowRecord(Record rec)
  {
    addRow(campi.stream().map((p) -> prepareHtml(p.second, rec)).collect(Collectors.toList()));
  }

  protected String prepareHtml(FunctionTrowException<Record, String> fun, Record rec)
  {
    try
    {
      return fun.apply(rec);
    }
    catch(Exception e)
    {
      return "ERROR: " + e.getMessage();
    }
  }

  public void createHeader()
  {
    setHeader(campi.stream().map((p) -> p.first).collect(Collectors.toList()));
  }

  public void buildFromRecords(Collection<? extends Record> lsRecs)
  {
    createHeader();
    for(Record r : lsRecs)
      addRowRecord(r);
  }

  @Override
  public void clear()
  {
    campi.clear();
    super.clear();
  }

  public List<Pair<String, FunctionTrowException<Record, String>>> getCampi()
  {
    return campi;
  }
}

/*
  public String getHtmlDati()
     throws Exception
  {
    List<Record> lsRecs = BasePeer.executeQuery(sSQL, false, con);
    if(lsRecs.isEmpty())
      return;

    HtmlTableHelperRecord tr = new HtmlTableHelperRecord();
    tr.addField("CODICE GRUPPO", (r) -> r.getValue("CODGRUPPO").asString());
    tr.addField("DESCRIZIONE", (r) -> r.getValue("GRUPPO").asString());
    tr.buildFromRecords(lsRecs);

    StringBuilder sb = new StringBuilder(512);
    ht.formatHtmlContent(sb);
    return sb.toString();
  }
 */
