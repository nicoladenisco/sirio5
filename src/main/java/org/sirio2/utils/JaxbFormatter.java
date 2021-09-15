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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.commonlib5.utils.DateTime;

/**
 * Formattatore per file JAXB.
 * Viene utilizzato per formattare correttamente l'XML quando si
 * usano le classi generate da Jaxb.
 *
 * @author Nicola De Nisco
 */
public class JaxbFormatter
{
  public static final DecimalFormat df0 = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
  public static final DecimalFormat df2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
  public static final DecimalFormat df4 = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
  public static final DecimalFormat df8 = new DecimalFormat("0.00000000", new DecimalFormatSymbols(Locale.US));

  public static String print2(BigDecimal value)
  {
    return printValue(value, df2);
  }

  public static String print4(BigDecimal value)
  {
    return printValue(value, df4);
  }

  public static String print8(BigDecimal value)
  {
    return printValue(value, df8);
  }

  public static String printPiva(BigDecimal value)
  {
    return SU.GetZeroFixedString(printValue(value, df0), 11);
  }

  public static String printPivaInt(BigInteger value)
  {
    return SU.GetZeroFixedString(value.toString(), 11);
  }

  private static String printValue(BigDecimal value, DecimalFormat df)
  {
    if(value == null)
      return "";

    try
    {
      return df.format(value.doubleValue());
    }
    catch(Exception ex)
    {
      Logger.getLogger(JaxbFormatter.class.getName()).log(Level.SEVERE, null, ex);
      return "";
    }
  }

  public static BigDecimal parseValue(String value)
  {
    return new BigDecimal(SU.parse(value, 0.0));
  }

  public static BigInteger parseValueInt(String value)
  {
    return new BigInteger(value, 10);
  }

  public static String printDateIso(XMLGregorianCalendar calendar)
  {
    if(calendar == null)
      return "";

    return DateTime.formatIso(calendar.toGregorianCalendar().getTime(), "");
  }

  public static XMLGregorianCalendar parseDateIso(String value)
  {
    try
    {
      Date tmpDate = DateTime.parseIso(value, null);
      if(tmpDate == null)
        return null;

      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(tmpDate);

      return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    }
    catch(DatatypeConfigurationException ex)
    {
      return null;
    }
  }
}
