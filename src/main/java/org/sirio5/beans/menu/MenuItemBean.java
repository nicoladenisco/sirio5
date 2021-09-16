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
package org.sirio5.beans.menu;

import java.io.Serializable;

/**
 * Bean per voce di menu.
 *
 * Ogni voce di menu viene descritta da una istanza di questo bean.
 */
public class MenuItemBean
   implements Serializable
{
  /**
   * whether the bean or its underlying object has changed
   * since last reading from the database
   */
  private boolean modified = true;

  /**
   * false if the underlying object has been read from the database,
   * true otherwise
   */
  private boolean isNew = true;

  /** The value for the listproId field */
  private int listproId;

  /** The value for the idPadre field */
  private int idPadre = 0;

  /** The value for the numRiga field */
  private int numRiga = 0;

  /** The value for the descrizione field */
  private String descrizione;

  /** The value for the programma field */
  private String programma;

  /** The value for the immagine field */
  private String immagine;

  /** The value for the colore field */
  private String colore;

  /** The value for the tipo field */
  private String tipo;

  /** The value for the flag1 field */
  private String flag1;

  /** The value for the flag2 field */
  private String flag2;

  /** The value for the permission field */
  private String permission;

  /** The value for the note field */
  private String note;

  /** The value for the statoRec field */
  private int statoRec = 0;

  /**
   * sets whether the bean exists in the database
   */
  public void setNew(boolean isNew)
  {
    this.isNew = isNew;
  }

  /**
   * returns whether the bean exists in the database
   */
  public boolean isNew()
  {
    return this.isNew;
  }

  /**
   * sets whether the bean or the object it was created from
   * was modified since the object was last read from the database
   */
  public void setModified(boolean isModified)
  {
    this.modified = isModified;
  }

  /**
   * returns whether the bean or the object it was created from
   * was modified since the object was last read from the database
   */
  public boolean isModified()
  {
    return this.modified;
  }

  /**
   * Get the ListproId
   *
   * @return int
   */
  public int getListproId()
  {
    return listproId;
  }

  /**
   * Set the value of ListproId
   *
   * @param v new value
   */
  public void setListproId(int v)
  {

    this.listproId = v;
    setModified(true);

  }

  /**
   * Get the IdPadre
   *
   * @return int
   */
  public int getIdPadre()
  {
    return idPadre;
  }

  /**
   * Set the value of IdPadre
   *
   * @param v new value
   */
  public void setIdPadre(int v)
  {

    this.idPadre = v;
    setModified(true);

  }

  /**
   * Get the NumRiga
   *
   * @return int
   */
  public int getNumRiga()
  {
    return numRiga;
  }

  /**
   * Set the value of NumRiga
   *
   * @param v new value
   */
  public void setNumRiga(int v)
  {

    this.numRiga = v;
    setModified(true);

  }

  /**
   * Get the Descrizione
   *
   * @return String
   */
  public String getDescrizione()
  {
    return descrizione;
  }

  /**
   * Set the value of Descrizione
   *
   * @param v new value
   */
  public void setDescrizione(String v)
  {

    this.descrizione = v;
    setModified(true);

  }

  /**
   * Get the Programma
   *
   * @return String
   */
  public String getProgramma()
  {
    return programma;
  }

  /**
   * Set the value of Programma
   *
   * @param v new value
   */
  public void setProgramma(String v)
  {

    this.programma = v;
    setModified(true);

  }

  /**
   * Get the Immagine
   *
   * @return String
   */
  public String getImmagine()
  {
    return immagine;
  }

  /**
   * Set the value of Immagine
   *
   * @param v new value
   */
  public void setImmagine(String v)
  {

    this.immagine = v;
    setModified(true);

  }

  /**
   * Get the Colore
   *
   * @return String
   */
  public String getColore()
  {
    return colore;
  }

  /**
   * Set the value of Colore
   *
   * @param v new value
   */
  public void setColore(String v)
  {

    this.colore = v;
    setModified(true);

  }

  /**
   * Get the Tipo
   *
   * @return String
   */
  public String getTipo()
  {
    return tipo;
  }

  /**
   * Set the value of Tipo
   *
   * @param v new value
   */
  public void setTipo(String v)
  {

    this.tipo = v;
    setModified(true);

  }

  /**
   * Get the Flag1
   *
   * @return String
   */
  public String getFlag1()
  {
    return flag1;
  }

  /**
   * Set the value of Flag1
   *
   * @param v new value
   */
  public void setFlag1(String v)
  {

    this.flag1 = v;
    setModified(true);

  }

  /**
   * Get the Flag2
   *
   * @return String
   */
  public String getFlag2()
  {
    return flag2;
  }

  /**
   * Set the value of Flag2
   *
   * @param v new value
   */
  public void setFlag2(String v)
  {

    this.flag2 = v;
    setModified(true);

  }

  /**
   * Get the Permission
   *
   * @return String
   */
  public String getPermission()
  {
    return permission;
  }

  /**
   * Set the value of Permission
   *
   * @param v new value
   */
  public void setPermission(String v)
  {

    this.permission = v;
    setModified(true);

  }

  /**
   * Get the Note
   *
   * @return String
   */
  public String getNote()
  {
    return note;
  }

  /**
   * Set the value of Note
   *
   * @param v new value
   */
  public void setNote(String v)
  {

    this.note = v;
    setModified(true);

  }

  /**
   * Get the StatoRec
   *
   * @return int
   */
  public int getStatoRec()
  {
    return statoRec;
  }

  /**
   * Set the value of StatoRec
   *
   * @param v new value
   */
  public void setStatoRec(int v)
  {

    this.statoRec = v;
    setModified(true);

  }
}
