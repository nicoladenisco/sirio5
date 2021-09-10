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
package org.sirio2.beans;

import org.sirio2.utils.CoreRunData;

/**
 * ESEMPIO DI BEAN.
 * Questa classe è un esempio di come devono essere
 * implementati i bean. Quando viene creato un nuovo
 * bean copiare questa classe e incollarla nel nuovo
 * bean. Eseguire quindi un cerca e cambia di
 * AAAskelBean nel nuovo nome della classe bean.
 *
 * TODO: inserire due linee nell'action che usa questo bean
 * // trasferisce tutti i parametri dall'html a proprietà del bean
 * data.getParameters().setProperties(bean);
 *
 * Per creare una istanza del bean persistente in sessione
 * utilizzare la BeanFactory.
 *
 * @author Nicola De Nisco
 */
final public class AAAskelBean extends CoreBaseBean
{
// <editor-fold defaultstate="collapsed" desc="Getter/Setter">
  // TODO: inserire qui i getter e setter
  // di eventuali proprietà del bean
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Metodi di servizio per il bean">
  @Override
  public void init(CoreRunData data)
     throws Exception
  {
    super.init(data);

    // TODO: inserire qui il codice di inizializzazione del bean
  }

  /**
   * Verifica se questo bean è ancora valido.
   * Questa funzione viene chiamata quando
   * il bean viene recuperato dalla sessione.
   * Se nella richiesta vi sono parametri che
   * ne inficiano l'utilizzo questo metodo
   * deve ritornare false e una nuova istanza
   * di questo bean verrà creata e inizializzata.
   * @param data dati della richiesta HTML
   * @return vero se il bean è valido
   */
  @Override
  public boolean isValid(CoreRunData data)
  {
    // TODO: inserire qui eventuali controlli
    // per determinare se questo bean è valido
    // rispetto ai parametri della richiesta HTML

    return super.isValid(data);
  }
// </editor-fold>
}
