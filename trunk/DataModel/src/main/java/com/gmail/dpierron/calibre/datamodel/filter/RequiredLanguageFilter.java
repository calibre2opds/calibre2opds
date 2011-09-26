package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Language;
import com.gmail.dpierron.tools.Helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class RequiredLanguageFilter implements BookFilter {

  List<Language> requiredLanguages;
  boolean includeBooksWithNoLanguage;

  public RequiredLanguageFilter(String requiredLanguageList, boolean includeBooksWithNoLanguage) {
    if (Helper.isNotNullOrEmpty(requiredLanguageList)) {
      requiredLanguages = new LinkedList<Language>();
      List<String> isoCodes = Helper.tokenize(requiredLanguageList.toLowerCase(Locale.ENGLISH), ",", true);
      for (String isoCode : isoCodes) {
        Language lang = DataModel.INSTANCE.getMapOfLanguagesByIsoCode().get(isoCode);
        if (lang != null)
          requiredLanguages.add(lang);
      }
    }
    this.includeBooksWithNoLanguage = includeBooksWithNoLanguage;
  }

  private List<Language> getRequiredLanguages() {
    return requiredLanguages;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(requiredLanguages))
      return true;

    for (Language requiredLanguage : getRequiredLanguages()) {
      boolean found = false;
      for (Language bookLanguage : book.getBookLanguages()) {
        if (bookLanguage.equals(requiredLanguage)) {
          found = true;
          break;
        }
      }
      if (!found)
        return false;
    }
    return true;
  }

}
