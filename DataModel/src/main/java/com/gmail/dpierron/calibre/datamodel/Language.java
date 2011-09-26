package com.gmail.dpierron.calibre.datamodel;

public class Language {
  String id;
  String calibreCode;
  ISOLANGUAGE isoLanguage;

  public Language(String id, String calibreCode) {
    this.id = id;
    this.calibreCode = calibreCode;
  }

  public String getId() {
    return id;
  }

  public String getCalibreCode() {
    return calibreCode;
  }

  public String toString() {
    return calibreCode;
  }

  public ISOLANGUAGE getIsoLanguage() {
    if (isoLanguage == null) {
      for (ISOLANGUAGE language_name : ISOLANGUAGE.values()) {
        if (language_name.name().equals(getCalibreCode())) {
          isoLanguage = language_name;
          break;
        }
      }
    }
    return isoLanguage;
  }

  public enum ISOLANGUAGE {
    fra("français", "Français", "fr"),
    eng("english", "English", "en"),
    deu("deutsch", "Deutsch", "de");

    String lower;
    String upper;
    String iso;

    private ISOLANGUAGE(String lower, String upper, String iso) {
      this.lower = lower;
      this.upper = upper;
      this.iso = iso;
    }

    public String getLower() {
      return lower;
    }

    public String getUpper() {
      return upper;
    }

    public String getIso() {
      return iso;
    }
  }
}
