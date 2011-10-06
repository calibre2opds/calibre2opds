package com.sag.baf.tools;

// used to pass a mutable String to a anonymous inner class (the StringContainer can be final, and the String can still be set)
public class SimpleStringContainer {

  String value;

  public SimpleStringContainer() {
    this("");
  }

  public SimpleStringContainer(String value) {
    super();
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
