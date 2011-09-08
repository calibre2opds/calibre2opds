package com.gmail.dpierron.calibre.configuration;

public enum CompatibilityTrick {
  OPDS,
  TROOK,
  STANZA,
  ;
  
  public static CompatibilityTrick fromString(String value) {
    try {
      return CompatibilityTrick.valueOf(value);
    } catch (Exception e) {
      return CompatibilityTrick.OPDS;
    }
  }
}
