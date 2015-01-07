package com.gmail.dpierron.calibre.datamodel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Language {
  private final String id;
  private Locale locale;

  public Language(String id, String iso3) {
    for (Locale l : Locale.getAvailableLocales()) {
      if (l.getISO3Language().equalsIgnoreCase(iso3)) {
        this.locale = l;
        break;
      }
    }
    // Force generic ENGLISH locale for awkward cases
    if (this.locale == null || this.locale.getLanguage().equalsIgnoreCase("en")) {
      this.locale = Locale.ENGLISH;
    }
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getIso3() {
    return locale.getISO3Language();
  }

  public String getIso2() {
    return locale.getLanguage();
  }

  public String getEnglishName() {
    return locale.getDisplayLanguage().toLowerCase();
  }

  public String toString() {
    return ""+locale.getLanguage()+"/"+locale.getISO3Language()+"/"+ locale.getDisplayLanguage();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof String) {
      String lang = (String) obj;
      return (getIso3().equalsIgnoreCase(lang)) || (getIso2().equalsIgnoreCase(lang)) || (getEnglishName().equalsIgnoreCase(lang));
    } else
      return super.equals(obj);

  }
}
