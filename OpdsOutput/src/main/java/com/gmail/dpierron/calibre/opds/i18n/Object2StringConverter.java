package com.gmail.dpierron.calibre.opds.i18n;

public interface Object2StringConverter {
  public String getStringValue(Object value);

  public String getStringValueOrNull(Object value);
}
