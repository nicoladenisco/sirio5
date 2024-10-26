/*
 * Copyright (C) 2024 Nicola De Nisco
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import static org.apache.commons.lang.StringUtils.isNumeric;

/**
 * Controllo esteso del codice fiscale.
 *
 * @author Nicola De Nisco
 */
public class ControlloCodiceFiscaleEsteso
{
  protected final String cf2;
  protected boolean eni, stp;
  protected int ctrOmogodia, valoreASL, valoreProgr;
  protected final int setdisp[] =
  {
    1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20,
    11, 3, 6, 8, 12, 14, 16, 10, 22, 25, 24, 23
  };
  protected final char omogodiaCorretta[] =
  {
    'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X'
  };

  public ControlloCodiceFiscaleEsteso(String cf)
  {
    this.cf2 = SU.okStr(cf).toUpperCase();
  }

  /**
   * elencoSTP contiene l'elenco dei codici ASL d'Italia per verificare la correttezza degli
   * STP ed ENI. Tali codici sono fatti da STP/ENI + CodiceASL (Regione+ASL) + progressivo numerico.
   * Se non puoi verificare in questo modo gli STP/ENI puoi anche gestirli così:
   * se un codice fiscale ti da errore di check digit e comincia per STP/ENI, puoi verificare che
   * la substring(9) del codice fiscale sia un numero. Se lo è, allora quello che stai controllando è
   * un codice STP/ENI. In questo caso l'unica cosa che non potrai sapere è se il codice ASL è corretto.
   */
  protected final List<String> elencoSTP = new ArrayList<>();

  protected static final Pattern CHAR_ALLOWED = Pattern.compile("^[A-Z]+$");

  /**
   * Verifica il codice fornito.
   * @return eventuale errore
   */
  public String controllaCF()
  {
    // codici solo numerici assegnati dal ministero ai naufraghi non vanno controllati
    if(isNumeric(cf2))
      return null;

    if(cf2.length() != 16)
      return "codice fiscale errato: la lunghezza deve essere 16 caratteri";

    if(CHAR_ALLOWED.matcher(cf2).matches())
      return "codice fiscale errato: caratteri non validi.";

    String begin3 = cf2.substring(0, 3);
    stp = begin3.equals("STP");
    eni = begin3.equals("ENI");

    int i, s, c;

    if(eni || stp)
    {
      return verificaStpEni();
    }

    char ctr;
    boolean ok = false;
    String omogodia = cf2.substring(6, 7)
       + cf2.substring(7, 8) + cf2.substring(9, 10) + cf2.substring(10, 11)
       + cf2.substring(12, 13) + cf2.substring(13, 14) + cf2.substring(14, 15);
    try
    {
      ctrOmogodia = Integer.parseInt(omogodia);
    }
    catch(Exception e)
    {
      for(int o = 0; o < 7; o++)
      {
        ctr = omogodia.charAt(o);
        for(int u = 0; u < 21; u++)
        {
          if(ctr == omogodiaCorretta[u])
          {
            ok = true;
          }
        }
        if(!ok)
        {
          return "codice fiscale errato: omocodia non corretta.";
        }
      }
    }

    s = 0;
    for(i = 1; i <= 13; i += 2)
    {
      c = cf2.charAt(i);
      if(c >= '0' && c <= '9')
        s = s + c - '0';
      else
        s = s + c - 'A';
    }

    for(i = 0; i <= 14; i += 2)
    {
      c = cf2.charAt(i);
      if(c >= '0' && c <= '9')
        c = c - '0' + 'A';
      s = s + setdisp[c - 'A'];
    }

    if(s % 26 + 'A' != cf2.charAt(15))
    {
      return "codice fiscale errato: check digit errato. ricevuto " + cf2 + ".";
    }

    return null;
  }

  public String verificaStpEni()
  {
    String ctrSTP = cf2.substring(3, 9);

    try
    {
      // verifica che Regione + Codice ASL sia effettivamente numerico
      valoreASL = Integer.parseInt(ctrSTP);

      if(!elencoSTP.isEmpty())
      {
        if(!elencoSTP.contains(ctrSTP))
          return "codice STP errato: codice regione + codice ASL non censito nel db. ricevuto " + cf2 + ".";
      }

      try
      {
        // se non abbiamo caricato l'elenco Codice Regione + Codice ASL
        // controlliamo solo che il codice numerico progressivo sia effettivamente un numero
        valoreProgr = Integer.parseInt(cf2.substring(9).trim());
        return null;
      }
      catch(NumberFormatException e)
      {
        return "codice STP errato: progressivo non numerico. ricevuto " + cf2 + ".";
      }
    }
    catch(NumberFormatException e)
    {
      return "codice STP errato: regione+asl non numerico. ricevuto " + cf2 + ".";
    }
  }

  public void setElencoSTP(Collection<String> elenco)
  {
    elencoSTP.clear();
    elencoSTP.addAll(elenco);
  }

  public String getCodice()
  {
    return cf2;
  }

  public boolean isEni()
  {
    return eni;
  }

  public boolean isStp()
  {
    return stp;
  }
}
