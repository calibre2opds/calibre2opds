package com.sag.baf.tools;

public class SimpleBooleanContainer implements BooleanContainer {

  boolean value;

  public SimpleBooleanContainer() {
    this(false);
  }

  public SimpleBooleanContainer(boolean value) {
    super();
    this.value = value;
  }

  public boolean getBooleanValue() {
    return value;
  }

  public void setBooleanValue(boolean value) {
    this.value = value;
  }

}
