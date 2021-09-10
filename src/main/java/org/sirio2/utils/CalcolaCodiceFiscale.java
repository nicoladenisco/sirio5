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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import org.commonlib.utils.StringOper;
import org.sirio2.services.localization.INT;

/**
 *
 * Calcolo del codice fiscale.
 * Il numero di codice fiscale delle persone fisiche e' costituito da
 * un'espressione alfanumerica di sedici caratteri. I primi quindici caratteri
 * sono indicativi dei dati anagrafici di ciascun soggetto secondo l'ordine
 * seguente:
 * <ul>
 * <li>tre caratteri alfabetici per il cognome;</li>
 * <li>tre caratteri alfabetici per il nome;</li>
 * <li>due caratteri numerici per l'anno di nascita;</li>
 * <li>un carattere alfabetico per il mese di nascita;</li>
 * <li>due caratteri numerici per il giorno di nascita ed il sesso</li>
 * <li>quattro caratteri, di cui uno alfabetico e tre (alfa)numerici per il
 * comune italiano o per lo Stato estero di nascita.</li>
 * <li>Il sedicesimo carattere, alfabetico, ha funzione di controllo.</li>
 * </ul>
 *
 * @author Luca Amoroso, Andrea Zoleo
 */
public class CalcolaCodiceFiscale
{
  private CalcolaCodiceFiscale()
  {
  }

  /**
   * Passando in input i dati anagrafici della persona, restituisce il codice
   * fiscale calcolato. Se uno dei parametri in input e' null o stringa vuota
   * restituisce una stringa vuota.
   *
   * @param cognome Il cognome della persona
   * @param nome Il nome della persona
   * @param sesso Il sesso della persona M o F
   * @param datanascita La data di nascita della persona
   * @param comune Il codice del comune di nascita della persona
   * @return String - il codice fiscale calcolato in base a parametri in
   * input. Se uno dei parametri in input e' null o stringa vuota
   * restituisce una stringa vuota.
   */
  public static String calcolaCf(String cognome, String nome, String sesso,
     Date datanascita, String comune)
  {
    if(!SU.isOkStrAll(cognome, nome, sesso, comune) || datanascita == null)
      throwEmptyArgException();

    sesso = sesso.toUpperCase().trim();
    if(!("M".equals(sesso) || "F".equals(sesso)))
      throw new IllegalArgumentException(
         "L'argomento 'sesso' deve essere la stringa 'm'o 'f'");

    nome = nome.toUpperCase().trim();
    if(!CHAR_ALLOWED.matcher(nome).matches())
      throwIllegalArgException("nome");
    else
      nome = sostituzioneChar(nome);

    cognome = cognome.toUpperCase().trim();
    if(!CHAR_ALLOWED.matcher(cognome).matches())
      throwIllegalArgException("cognome");
    else
      cognome = sostituzioneChar(cognome);

    comune = comune.toUpperCase().trim();
    if(!CODICE_COMUNE_ALLOWED.matcher(comune).matches())
      throw new IllegalArgumentException(
         "L'argomento 'comune' non sembra essere un codice valido");

    StringBuilder codfisc = new StringBuilder();
    codfisc.append(calcolaCodCognome(cognome));
    codfisc.append(calcolaCodNome(nome));
    codfisc.append(calcolaCodDt(datanascita, sesso));
    codfisc.append(comune);
    codfisc.append(calcolaCharControllo(codfisc));

    return codfisc.toString();
  }

  private static void throwEmptyArgException()
  {
    throw new IllegalArgumentException(
       "non sono permessi parametri nulli o vuoti");
  }

  private static void throwIllegalArgException(String arg_name)
  {
    throw new IllegalArgumentException(
       "L'argomento '"
       + arg_name
       + "' non puo' contenere caratteri speciali.");
  }

  /**
   * Passata una stringa in input, ritorna le sole consonanti o vocali delle
   * stringa.
   * <p>
   * Per ottenere le vocali, passare conson = false
   * <br/>
   * Per ottenere le consonanti passare conson = true
   *
   * @param stringa La stringa per la quale si vogliono ottenere le sole
   * consonanti o vocali
   * @param cod puo' essere false o true a seconda che si vogliano
   * ottenere le vocali o le consonanti della stringa
   * @return String - La stringa contente le solo vocali o consonanti della
   * stringa passata in input
   */
  private static String ottieniConsVoc(String stringa, boolean conson)
  {
    StringBuilder testo = new StringBuilder();
    int i = 0;
    char[] valChar = stringa.toCharArray();
    for(i = 0; i < valChar.length; i++)
    {
      if(isVowel(valChar[i]) ^ conson)
        testo.append(valChar[i]);
    }
    return testo.toString();
  }

  /**
   *
   * I cognomi che risultano composti da piu' parti o comunque separati od
   * interrotti, vengono considerati come se fossero scritti secondo un'unica
   * ed ininterrotta successione di caratteri. Per i soggetti coniugati di
   * sesso femminile si prende in considerazione soltanto il cognome da
   * nubile. Se il cognome contiene tre o piu' consonanti, i tre caratteri da
   * rilevare sono, nell'ordine, la prima, la seconda e la terza consonante.
   * Se il cognome contiene due consonanti, i tre caratteri da rilevare sono,
   * nell'ordine, la prima e la seconda consonante e la prima vocale. Se il
   * cognome contiene una consonante e due vocali, si rilevano, nell'ordine,
   * quella consonante e quindi la prima e la seconda vocale. Se il cognome
   * contiene una consonante e una vocale, si rilevano la consonante e la
   * vocale, nell'ordine, e si assume come terzo carattere la lettera x (ics).
   * Se il cognome e' costituito da due sole vocali, esse si rilevano,
   * nell'ordine, e si assume come terzo carattere la lettera x (ics).
   *
   * @param stringa - Il cognome della persona
   * @return StringBuilder - Parte del codice fiscale relativo al cognome
   * della persona
   */
  private static StringBuilder calcolaCodCognome(String stringa)
  {
    StringBuilder codice = new StringBuilder();
    codice.append(ottieniConsVoc(stringa, true)).append(ottieniConsVoc(stringa, false));

    if(codice.length() > 3)
      codice = new StringBuilder(codice.substring(0, 3));

    for(int i = codice.length(); i < 3; i++)
      codice.append(CARATTERE_SOSTITUTO);

    return codice;
  }

  /**
   *
   * I nomi doppi, multipli o comunque composti, vengono considerati come
   * scritti per esteso in ogni loro parte e secondo un'unica ed ininterrotta
   * successione di caratteri. Se il nome contiene quattro o piu' consonanti, i
   * tre caratteri da rilevare sono, nell'ordine, la prima, la terza e la
   * quarta consonante. Se il nome contiene tre consonanti, i tre caratteri da
   * rilevare sono, nell'ordine, la prima, la seconda e la terza consonante.
   * Se il nome contiene due consonanti, i tre caratteri da rilevare sono,
   * nell'ordine, la prima e la seconda consonante e la prima vocale. Se il
   * nome contiene una consonante e due vocali, i tre caratteri da rilevare
   * sono, nell'ordine, quella consonante e quindi la prima e la seconda
   * vocale. Se il nome contiene una consonante e una vocale, si rilevano la
   * consonante e la vocale, nell'ordine, e si assume come terzo carattere la
   * lettera x (ics). Se il nome e' costituito da due vocali, esse si rilevano
   * nell'ordine, e si assume come terzo carattere la lettera x (ics).
   *
   * @param stringa Il nome della persona
   * @return StringBuilder - Parte del codice fiscale relativo al nome della
   * persona
   */
  private static StringBuilder calcolaCodNome(String stringa)
  {
    StringBuilder codice = new StringBuilder(ottieniConsVoc(stringa, true));

    if(codice.length() >= 4)
      codice = codice.delete(1, 2);

    codice.append(ottieniConsVoc(stringa, false));

    if(codice.length() > 3)
      codice = codice.replace(0, codice.length(), codice.substring(0, 3));

    for(int i = codice.length(); i < 3; i++)
      codice.append(CARATTERE_SOSTITUTO);

    return codice;

  }

  /**
   *
   * I due caratteri numerici indicativi dell'anno di nascita sono,
   * nell'ordine, la cifra delle decine e la cifra delle unita' dell'anno
   * stesso. Il carattere alfabetico corrispondente al mese di nascita e'
   * quello dal COD_MESE, definito in questa classe. I due caratteri numerici
   * indicativi del giorno di nascita e del sesso vengono determinati nel modo
   * seguente: per i soggetti maschili il giorno di nascita figura invariato,
   * con i numeri da uno a trentuno, facendo precedere dalla cifra zero i
   * giorni del mese dall'uno al nove; per i soggetti femminili il giorno di
   * nascita viene aumentato di quaranta unita', per cui esso figura con i
   * numeri da quarantuno a settantuno. I quatto caratteri alfanumerici
   * indicativi del comune italiano o dello Stato estero di nascita, sono
   * costituiti da un carattere alfabetico seguito da tre caratteri numerici,
   * secondo la codifica stabilita dall'Agenzia del Territorio.
   *
   * @param dtNasc La data di nascita della persona
   * @param sesso Il sesso della persona M o F
   * @return StringBuilder - Parte del codice fiscale relativo alla data di
   * nascita e al sesso della persona
   */
  private static StringBuilder calcolaCodDt(Date dtNasc, String sesso)
  {
    StringBuilder cod = new StringBuilder();
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(dtNasc);

    int giorno = cal.get(GregorianCalendar.DAY_OF_MONTH);
    int mese = cal.get(GregorianCalendar.MONTH);
    int anno = cal.get(GregorianCalendar.YEAR);

    String Anno = anno + "";
    cod.append(Anno.substring(2, 4));

    cod.append(getCodiceMese(mese));
    if(sesso.equals("M"))
    {
      //String Giorno = giorno+"";
      String giornoCorrente = StringOper.fmtZero(giorno, 2);
      cod.append(giornoCorrente);
    }
    else
    {
      giorno += 40;
      String Giorno = giorno + "";
      cod.append(Giorno);
    }

    return cod;
  }

  /**
   * Calcola carattere di controllo.
   * Il sedicesimo carattere ha funzione di controllo dell'esatta trascrizione
   * dei primi quindici caratteri e viene determinato in questo modo: ciascuno
   * degli anzidetti quindici caratteri, a seconda che occupi posizione di
   * ordine pari o posizione di ordine dispari, viene convertito in un valore
   * numerico:
   * Per i sette caratteri con posizione di ordine pari viene utilizzato
   * COD_CHAR_PARI, definito in questa classe
   * Per gli otto caratteri con posizione di ordine dispari viene utilizzato
   * COD_CHAR_DISPARI, definito in questa classe
   * I valori numerici cosi' determinati vengono addizionati e la somma si
   * divide per il numero 26. Il carattere di controllo si ottiene convertendo
   * il resto di tale divisione nel carattere alfabetico ad esso
   * corrispondente nella sotto indicata tabella:
   *
   * @param codfisc Il codice fiscale calcolato della persona
   * @return Character - L'ultimo carattere di controllo relativo al codice
   * fiscale
   */
  private static char calcolaCharControllo(StringBuilder codfisc)
  {
    int somma = 0;
    for(int i = 0; i < codfisc.length(); i++)
    {
      int k = Character.getNumericValue(codfisc.charAt(i));
      if(i % 2 == 0)
        //parita' invertita perche' stringa e' zero aligned
        somma += EVEN_ODD_CHAR_CODES[1][k];
      else
        somma += EVEN_ODD_CHAR_CODES[0][k];
    }

    int num = somma % 26;
    return carFinale[num];
  }

  /**
   * Dato un codice fiscale calcola la data di nascita corrispondente.
   * @param codiceFiscale input
   * @return data di nascita o null per errore
   * @throws java.lang.Exception
   */
  public static Date calcolaNascitaDaCf(String codiceFiscale)
     throws Exception
  {
    try
    {
      // DNSNCL66M27G902V
      // 012345678901234567890

      codiceFiscale = SU.okStr(codiceFiscale).toUpperCase();
      if(codiceFiscale.length() != 16)
        return null;

      int anno = Integer.parseInt(codiceFiscale.substring(6, 8));
      int mese = getNumMese(codiceFiscale.charAt(8));
      int giorno = Integer.parseInt(codiceFiscale.substring(9, 11));

      // le donne hanno il giorno + 40
      if(giorno > 40)
        giorno -= 40;

      if(mese == -1 || giorno <= 0 || giorno >= 31)
        return null;

      GregorianCalendar c = new GregorianCalendar();
      int year = c.get(Calendar.YEAR);
      if(year > 2000)
      {
        year = year - 2000;
        if(anno > year)
          anno += 1900;
        else
          anno += 2000;
      }
      else
        anno += 1900;

      c.set(anno, mese, giorno, 0, 0, 0);
      return c.getTime();
    }
    catch(NumberFormatException ex)
    {
      throw new Exception(INT.I("Formato codice fiscale non valido; impossibile estrarre data di nascita."));
    }
  }

  /**
   * Dato un codice fiscale recupera il codice belfiore del comune.
   * @param codiceFiscale input
   * @return codice belfiore o null per errore
   * @throws Exception
   */
  public static String getCodiceComuneDaCf(String codiceFiscale)
     throws Exception
  {
    // DNSNCL66M27G902V
    // 012345678901234567890

    codiceFiscale = SU.okStr(codiceFiscale).toUpperCase();
    if(codiceFiscale.length() != 16)
      return null;

    return codiceFiscale.substring(11, 15);
  }

  /**
   * Dato un codice fiscale recupera il sesso.
   * @param codiceFiscale input
   * @return M/F o null per errore
   * @throws Exception
   */
  public static String getSessoDaCf(String codiceFiscale)
     throws Exception
  {
    try
    {
      // DNSNCL66M27G902V
      // 012345678901234567890

      codiceFiscale = SU.okStr(codiceFiscale).toUpperCase();
      if(codiceFiscale.length() != 16)
        return null;

      int giorno = Integer.parseInt(codiceFiscale.substring(9, 11));
      return giorno > 40 ? "F" : "M";
    }
    catch(NumberFormatException ex)
    {
      throw new Exception(INT.I("Formato codice fiscale non valido; impossibile determinare il sesso."));
    }
  }
  //
  //
  //
  public static final char[] carFinale =
  {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
    'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  };

  /**
   * Prende la stringa in input (nome o cognome) e sostituisce tutti i
   * caratteri non ammessi con dei caratteri ammessi o stringa vuota.
   * @param value La stringa per la quale si desidera effettuare le sostituzioni
   * @return String - La stringa con i caratteri non ammessi sostituiti da
   * caratteri ammessi
   */
  private static String sostituzioneChar(String value)
  {
    for(int i = 0; i < CHAR_SOSTITUZIONE[1].length; i++)
    {
      value = value.replaceAll(CHAR_SOSTITUZIONE[ROW_REGEX][i],
         CHAR_SOSTITUZIONE[ROW_SOST][i]);
    }

    return value;
  }
  private static final String CARATTERE_SOSTITUTO = "X";
  private static char[] codici_mesi =
  {
    'A', 'B', 'C', 'D', 'E', 'H',
    'L', 'M', 'P', 'R', 'S', 'T'
  };

  private static char getCodiceMese(int mese)
  {
    return codici_mesi[mese];
  }

  private static int getNumMese(char mese)
  {
    mese = Character.toUpperCase(mese);

    for(int i = 0; i < codici_mesi.length; i++)
    {
      if(mese == codici_mesi[i])
        return i;
    }

    return -1;
  }
  private static int[][] EVEN_ODD_CHAR_CODES =
  {
    {
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
      24, 25
    },
    {
      1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16, 10,
      22, 25, 24, 23
    }
  };

  private static boolean isVowel(char c)
  {
    return VOCALE_ALLOWED.matcher(String.valueOf(c)).matches();
  }
  /**
   * Array utilizzato per ottenere la regex di sostituzione e la relativa
   * stringa di sostituzione. Si usa per modificare nome e cognome e renderli
   * sintatticamente corretti.
   * @see sostituzioneChar()
   */
  private static final String[][] CHAR_SOSTITUZIONE =
  {
    {
      "[\u00c0]", "[\u00c8]", "[\u00c9]", "[\u00cc]", "[\u00d2]", "[\u00d9]", "[\\s]", "[']"
    },
    //    {{"[A']","[E']","[I']","['I]","[O']","[U']","[\\s]","[']"},
    {
      "A", "E", "E", "I", "O", "U", "", ""
    }
  };
  private static final int ROW_REGEX = 0;
  private static final int ROW_SOST = 1;
  /**
   * Utilizzata per verificare la sintassi delle stringhe nome e cognome
   */
  private static final Pattern CHAR_ALLOWED = Pattern.compile("[A-Z\u00c0\u00c8\u00c9\u00cc\u00d2\u00d9' ]+");
  /**
   * Utilizzata per il controllo della sintassi del codice dei comuni
   */
  private static final Pattern CODICE_COMUNE_ALLOWED = Pattern.compile("[A-Z][0-9]{3}");
  /**
   * Utilizzato per verificare se una lettera e' una consonante
   */
  private static final Pattern VOCALE_ALLOWED = Pattern.compile("[AEIOU]");
}
