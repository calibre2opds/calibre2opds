package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Language {
  private final String id;
  private Locale locale;

  public Language(String id, String iso3) {
    assert id != null;
    assert Helper.isNotNullOrEmpty(iso3);
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
    assert locale != null;
    return locale.getISO3Language();
  }

  public String getIso2() {
    assert locale != null;
    return locale.getLanguage();
  }

  public String getEnglishName() {
    assert locale != null;
    return locale.getDisplayLanguage().toLowerCase();
  }

  public String toString() {
    assert locale != null;
    return ""+locale.getLanguage()+"/"+locale.getISO3Language()+"/"+ locale.getDisplayLanguage();
  }

  public Locale getLocale() {
    return locale;
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
