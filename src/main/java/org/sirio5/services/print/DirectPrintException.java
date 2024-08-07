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

import static org.sirio5.services.localization.INT.I;

/**
 * Questa eccezione non è un vero e proprio errore,
 * ma segnala che l'elaborazione di una stampa è in
 * corso e verrà inviata alla stampante appena pronta.
 *
 * @author Nicola De Nisco
 */
public class DirectPrintException extends Exception
{
  public PdfPrint.JobInfo job = null;
  public Throwable err = null;

  public DirectPrintException(PdfPrint.JobInfo job)
  {
    super(I("Elaborazione del job %s in corso", job.jobCode));
    this.job = job;
  }

  public DirectPrintException(PdfPrint.JobInfo job, Throwable err)
  {
    super((I("Errore durante l'elaborazione del job %s: %s", job.jobCode, err.getMessage())), err);
    this.job = job;
    this.err = err;
  }
}
