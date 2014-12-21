package com.gmail.dpierron.tools.i18n;

public interface Object2StringConverter {
  public String getStringValue(Object value);

  public String getStringValueOrNull(Object value);
}
