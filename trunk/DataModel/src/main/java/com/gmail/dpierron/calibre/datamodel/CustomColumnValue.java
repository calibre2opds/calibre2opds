package com.gmail.dpierron.calibre.datamodel;

/**
 * User: WalkerDJ
 *
 * Class that is used to store a custom column value
 */
public class CustomColumnValue {

  private CustomColumnType type;
  private String value;

  //  CONSTRUCTORS

  public CustomColumnValue (CustomColumnType type, String value) {
    this.type = type;
    this.value = value;
  }

  // METHODS and PROPERTIES

}
