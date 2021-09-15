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
package org.sirio2.utils;

import java.util.*;
import org.apache.commons.lang.ArrayUtils;
import org.commonlib5.utils.Pair;

/**
 * Generatore HTML per tabella si/no.
 *
 * @author Nicola De Nisco
 */
public class TabellaSino
{
  protected List<Pair<Integer, String>> righe = new ArrayList<>();
  protected List<Pair<Integer, String>> colonne = new ArrayList<>();
  protected String capButSi = "SI", capButNo = "NO",
     baseUrlRiga, baseUrlColonna,
     scriptRigheSi = "tutteColonneSI", scriptRigheNo = "tutteColonneNO",
     scriptColonneSi = "tutteRigheSI", scriptColonneNo = "tutteRigheNO";
  protected String tagTabelleForm = null, captionColonne, captionRighe;
  protected int[] idRowSelected = ArrayUtils.EMPTY_INT_ARRAY;
  protected int[] idColSelected = ArrayUtils.EMPTY_INT_ARRAY;
  // costanti
  public static final String sRowHeader = "TR class=\"rigel_table_header_row\"";
  public static final String sColHeader = "TD class=\"rigel_table_header_cell\"";

  @FunctionalInterface
  public interface FunctionSino<T1, T2, R>
  {
    R apply(T1 t1, T2 t2)
       throws Exception;
  }

  @FunctionalInterface
  public interface ReturnSino<T1, T2, T3>
  {
    void apply(T1 t1, T2 t2, T3 t3)
       throws Exception;
  }

  public void setRighe(String caption, Collection<Pair<Integer, String>> righe)
  {
    this.captionRighe = caption;
    this.righe.clear();
    this.righe.addAll(righe);

    // per default seleziona tutte le righe come visibili
    selezionaTutteRighe();
  }

  public void setColonne(String caption, Collection<Pair<Integer, String>> colonne)
  {
    this.captionColonne = caption;
    this.colonne.clear();
    this.colonne.addAll(colonne);

    // per default seleziona tutte le colonne come visibili
    selezionaTutteColonne();
  }

  public void selezionaTutteRighe()
  {
    idRowSelected = new int[righe.size()];
    for(int i = 0; i < righe.size(); i++)
      idRowSelected[i] = righe.get(i).first;
  }

  public void selezionaTutteColonne()
  {
    idColSelected = new int[colonne.size()];
    for(int i = 0; i < colonne.size(); i++)
      idColSelected[i] = colonne.get(i).first;
  }

  public void addRiga(Pair<Integer, String> riga)
  {
    righe.add(riga);
    idRowSelected = Arrays.copyOf(idRowSelected, colonne.size());
    int i = idRowSelected.length - 1;
    idRowSelected[i] = this.righe.get(i).first;
  }

  public void addColonna(Pair<Integer, String> colonna)
  {
    colonne.add(colonna);
    idColSelected = Arrays.copyOf(idColSelected, colonne.size());
    int i = idColSelected.length - 1;
    idColSelected[i] = this.colonne.get(i).first;
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  public String getHtmlMatrice(FunctionSino<Integer, Integer, Boolean> testRigaColonna)
     throws Exception
  {
    StringBuilder sOut = new StringBuilder(8192);
    sOut.append("<table width='100%'>\n<thead>\n");
    sOut.append("<tr>"
       + "<th colspan=2 rowspan=2 align=center>" + captionRighe + "</td>"
       + "<th colspan=" + (idColSelected.length) + " align=center>" + captionColonne + "</td>"
       + "</tr>\n");

    sOut.append("<tr>");
    for(int i = 0; i < idColSelected.length; i++)
    {
      int idColonna = idColSelected[i];
      Pair<Integer, String> colonna = colonne.stream().filter((pa) -> idColonna == pa.first).findFirst().orElse(null);
      sOut.append("<th>");
      if(baseUrlColonna != null)
        sOut.append("<a href=\"").append(baseUrlColonna).append(idColonna).append("\">").append(colonna.second).append("</a>");
      else
        sOut.append(colonna.second);
      sOut.append("</th>");
    }
    sOut.append("</tr>\n</thead>\n<tbody>\n");

    for(int i = 0; i < idRowSelected.length; i++)
    {
      int idRiga = idRowSelected[i];
      Pair<Integer, String> riga = righe.stream().filter((pa) -> idRiga == pa.first).findFirst().orElse(null);
      sOut.append("<tr>");
      sOut.append("<td align='left' nowrap>");
      if(baseUrlRiga != null)
        sOut.append("<a href=\"").append(baseUrlRiga).append(idRiga).append("\">").append(riga.second).append("</a>");
      else
        sOut.append(riga.second);
      sOut.append("</td>\n");
      sOut.append("<td align='right' nowrap>").append(getButSiNo(scriptRigheSi, scriptRigheNo, idRiga)).append("</td>");

      for(int j = 0; j < idColSelected.length; j++)
      {
        int idColonna = idColSelected[j];
        String cbName = "RUGR_" + idRiga + "_" + idColonna;
        String hidName = "HUGR_" + idRiga + "_" + idColonna;
        sOut.append("<td>").append(getCheckBox(cbName, hidName, testRigaColonna.apply(idRiga, idColonna))).append("</td>");
      }
      sOut.append("</tr>\n");
    }

    sOut.append("<tr><td colspan=2>&nbsp;</td>");
    for(int i = 0; i < idColSelected.length; i++)
    {
      int idColonna = idColSelected[i];
      sOut.append("<td align='left' nowrap>").append(getButSiNo(scriptColonneSi, scriptColonneNo, idColonna)).append("</td>");
    }
    sOut.append("</tr>\n");

    sOut.append("</tbody></table>\n");
    return sOut.toString();
  }

  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  public String getHtmlMatriceInversa(FunctionSino<Integer, Integer, Boolean> testRigaColonna)
     throws Exception
  {
    StringBuilder sOut = new StringBuilder(8192);
    sOut.append("<table width='100%'>\n<thead>\n");
    sOut.append("<tr>"
       + "<th colspan=2 rowspan=2 align=center>" + captionColonne + "</td>"
       + "<th colspan=" + (idRowSelected.length) + " align=center>" + captionRighe + "</td>"
       + "</tr>\n");

    sOut.append("<tr>");
    for(int i = 0; i < idRowSelected.length; i++)
    {
      int idRigaInv = idRowSelected[i];
      Pair<Integer, String> rigainv = righe.stream().filter((pa) -> idRigaInv == pa.first).findFirst().orElse(null);
      sOut.append("<th>");
      if(baseUrlRiga != null)
        sOut.append("<a href=\"").append(baseUrlRiga).append(idRigaInv).append("\">").append(rigainv.second).append("</a>");
      else
        sOut.append(rigainv.second);
      sOut.append("</th>");
    }
    sOut.append("</tr>\n</thead>\n<tbody>\n");

    for(int i = 0; i < idColSelected.length; i++)
    {
      int idColonnaInv = idColSelected[i];
      Pair<Integer, String> colonnainv
         = colonne.stream().filter((pa) -> idColonnaInv == pa.first).findFirst().orElse(null);
      sOut.append("<tr>");
      sOut.append("<td align='left' nowrap>");
      if(baseUrlColonna != null)
        sOut.append("<a href=\"").append(baseUrlColonna).append(idColonnaInv).append("\">").append(colonnainv.second).append("</a>");
      else
        sOut.append(colonnainv.second);
      sOut.append("</td>\n");
      sOut.append("<td align='right' nowrap>").append(getButSiNo(scriptColonneSi, scriptColonneNo, idColonnaInv)).append("</td>");

      for(int j = 0; j < idRowSelected.length; j++)
      {
        int idRigaInv = idRowSelected[j];
        String cbName = "RUGR_" + idRigaInv + "_" + idColonnaInv;
        String hidName = "HUGR_" + idRigaInv + "_" + idColonnaInv;
        sOut.append("<td>").append(getCheckBox(cbName, hidName, testRigaColonna.apply(idRigaInv, idColonnaInv))).append("</td>");
      }
      sOut.append("</tr>\n");
    }

    sOut.append("<tr><td colspan=2>&nbsp;</td>");
    for(int i = 0; i < idRowSelected.length; i++)
    {
      int idRigaInv = idRowSelected[i];
      sOut.append("<td align='left' nowrap>").append(getButSiNo(scriptRigheSi, scriptRigheNo, idRigaInv)).append("</td>");
    }
    sOut.append("</tr>\n");

    sOut.append("</tbody></table>\n");
    return sOut.toString();
  }

  public String getButSiNo(String scriptSI, String scriptNO, int idValue)
  {
    return "<input type=\"button\" value=\"" + capButSi + "\" onClick=\"" + scriptSI + "(" + idValue + ")\">&nbsp;"
       + "<input type=\"button\" value=\"" + capButNo + "\" onClick=\"" + scriptNO + "(" + idValue + ")\">";
  }

  public String getCheckBox(String cbName, String hidName, boolean checked)
  {
    return "<input type=\"checkbox\" name=\"" + cbName + "\" value=\"1\" "
       + (checked ? "checked" : "") + ">"
       + "<input type=\"hidden\" name=\"" + hidName + "\" value=\"1\">";
  }

  public void storeMatrice(Map params, ReturnSino<Integer, Integer, Integer> ritornaValore)
     throws Exception
  {
    for(int i = 0; i < idRowSelected.length; i++)
    {
      int idRiga = idRowSelected[i];
      for(int j = 0; j < idColSelected.length; j++)
      {
        int idColonna = idColSelected[j];
        String cbName = "RUGR_" + idRiga + "_" + idColonna;
        String hidName = "HUGR_" + idRiga + "_" + idColonna;

        if(params.get(hidName) != null)
        {
          int value = SU.parseInt(params.get(cbName));

          // valore valido: può essere memorizzato
          ritornaValore.apply(idRiga, idColonna, value);
        }
      }
    }
  }

  public static final String script = ""
     + "  function tutteColonneSI(idRiga)\n"
     + "  {\n"
     + "    var src = \"RUGR_\" + idRiga + \"_\";\n"
     + "\n"
     + "    // Iterate each checkbox\n"
     + "    jQuery(':checkbox').each(function () {\n"
     + "      if (this.name.startsWith(src))\n"
     + "        this.checked = true;\n"
     + "    });\n"
     + "  }\n"
     + "\n"
     + "  function tutteColonneNO(idRiga)\n"
     + "  {\n"
     + "    var src = \"RUGR_\" + idRiga + \"_\";\n"
     + "\n"
     + "    // Iterate each checkbox\n"
     + "    jQuery(':checkbox').each(function () {\n"
     + "      if (this.name.startsWith(src))\n"
     + "        this.checked = false;\n"
     + "    });\n"
     + "  }"
     + "\n"
     + "  function tutteRigheSI(idColonna)\n"
     + "  {\n"
     + "    var src = \"_\" + idColonna;\n"
     + "\n"
     + "    // Iterate each checkbox\n"
     + "    jQuery(':checkbox').each(function () {\n"
     + "      if (this.name.startsWith(\"RUGR_\") && this.name.endsWith(src))\n"
     + "        this.checked = true;\n"
     + "    });\n"
     + "  }\n"
     + "\n"
     + "  function tutteRigheNO(idColonna)\n"
     + "  {\n"
     + "    var src = \"_\" + idColonna;\n"
     + "\n"
     + "    // Iterate each checkbox\n"
     + "    jQuery(':checkbox').each(function () {\n"
     + "      if (this.name.startsWith(\"RUGR_\") && this.name.endsWith(src))\n"
     + "        this.checked = false;\n"
     + "    });\n"
     + "  }";

// <editor-fold defaultstate="collapsed" desc="Getter/Setter">
  public String getCapButSi()
  {
    return capButSi;
  }

  public void setCapButSi(String capButSi)
  {
    this.capButSi = capButSi;
  }

  public String getCapButNo()
  {
    return capButNo;
  }

  public void setCapButNo(String capButNo)
  {
    this.capButNo = capButNo;
  }

  public String getTagTabelleForm()
  {
    return tagTabelleForm;
  }

  public void setTagTabelleForm(String tagTabelleForm)
  {
    this.tagTabelleForm = tagTabelleForm;
  }

  public int[] getIdColSelected()
  {
    return idColSelected;
  }

  public void setIdColSelected(int[] idColSelected)
  {
    this.idColSelected = idColSelected;
  }

  public String getBaseUrlRiga()
  {
    return baseUrlRiga;
  }

  public void setBaseUrlRiga(String baseUrlRiga)
  {
    this.baseUrlRiga = baseUrlRiga;
  }

  public String getBaseUrlColonna()
  {
    return baseUrlColonna;
  }

  public void setBaseUrlColonna(String baseUrlColonna)
  {
    this.baseUrlColonna = baseUrlColonna;
  }

  public String getScriptRigheSi()
  {
    return scriptRigheSi;
  }

  public void setScriptRigheSi(String scriptRigheSi)
  {
    this.scriptRigheSi = scriptRigheSi;
  }

  public String getScriptRigheNo()
  {
    return scriptRigheNo;
  }

  public void setScriptRigheNo(String scriptRigheNo)
  {
    this.scriptRigheNo = scriptRigheNo;
  }

  public String getScriptColonneSi()
  {
    return scriptColonneSi;
  }

  public void setScriptColonneSi(String scriptColonneSi)
  {
    this.scriptColonneSi = scriptColonneSi;
  }

  public String getScriptColonneNo()
  {
    return scriptColonneNo;
  }

  public void setScriptColonneNo(String scriptColonneNo)
  {
    this.scriptColonneNo = scriptColonneNo;
  }

  public String getScript()
  {
    return script;
  }

  public String getCaptionColonne()
  {
    return captionColonne;
  }

  public void setCaptionColonne(String captionColonne)
  {
    this.captionColonne = captionColonne;
  }

  public String getCaptionRighe()
  {
    return captionRighe;
  }

  public void setCaptionRighe(String captionRighe)
  {
    this.captionRighe = captionRighe;
  }

  public int[] getIdRowSelected()
  {
    return idRowSelected;
  }

  public void setIdRowSelected(int[] idRowSelected)
  {
    this.idRowSelected = idRowSelected;
  }
// </editor-fold>
}
