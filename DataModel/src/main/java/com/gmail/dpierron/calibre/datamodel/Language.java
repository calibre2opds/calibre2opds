package com.gmail.dpierron.calibre.datamodel;

public class Language {
  private final String id;
  private final String calibreCode;
  private ISOLANGUAGE isoLanguage;

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
    fra("français", "Français", "french", "fr"),
    eng("english", "English", "english", "en"),
    deu("deutsch", "Deutsch", "german", "de");

    final String lower;
    final String upper;
    final String english;
    final String iso;

    private ISOLANGUAGE(String lower, String upper, String english, String iso) {
      this.lower = lower;
      this.upper = upper;
      this.english = english;
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

    public String getEnglish() {
      return english;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof String) {
      String lang = (String) obj;
      return (getCalibreCode().equalsIgnoreCase(lang)) || (getIsoLanguage().getIso().equalsIgnoreCase(lang)) ||
          (getIsoLanguage().getEnglish().equalsIgnoreCase(lang)) || (getIsoLanguage().getLower().equalsIgnoreCase(lang)) ||
          (getIsoLanguage().getUpper().equalsIgnoreCase(lang));
    } else
      return super.equals(obj);

  }
}
