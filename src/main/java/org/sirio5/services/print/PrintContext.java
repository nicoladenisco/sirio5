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
package org.sirio5.services.print;

import java.util.Map;
import org.sirio5.utils.SirioGenericContext;

/**
 * Context per il generatore di PDF
 * @author Nicola De Nisco
 */
public class PrintContext extends SirioGenericContext
{
  public static final String REPORT_NAME_KEY = "REPORT_NAME_KEY",
     REPORT_INFO_KEY = "REPORT_INFO_KEY",
     PBEAN_KEY = "PBEAN_KEY",
     PDFTOGEN_KEY = "PDFTOGEN_KEY",
     PREPARED_DATA_KEY = "PREPARED_DATA_KEY",
     SESSION_KEY = "SESSION_KEY",
     PATH_INFO_KEY = "PATH_INFO",
     SESSION_ID_KEY = "SESSION_ID",
     QUERY_STRING_KEY = "QUERY_STRING";

  public PrintContext()
  {
  }

  public PrintContext(Map<? extends String, ? extends Object> m)
  {
    super(m);
  }

  public PrintContext(SirioGenericContext ctx)
  {
    super(ctx);
  }
}
