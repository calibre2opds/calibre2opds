package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;

public enum CompatibilityTrick {
  OPDS,
  TROOK,
  STANZA,
  ALDIKO,
  ;
  
  public String getHumanName() {
    return LocalizationHelper.INSTANCE.getEnumConstantHumanName(this);
  }
  
  public static CompatibilityTrick fromString(String value) {
    try {
      return CompatibilityTrick.valueOf(value);
    } catch (Exception e) {
      return CompatibilityTrick.OPDS;
    }
  }
}
