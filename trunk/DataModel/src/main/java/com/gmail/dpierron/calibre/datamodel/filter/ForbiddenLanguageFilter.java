package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Language;
import com.gmail.dpierron.tools.Helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ForbiddenLanguageFilter implements BookFilter {

  List<Language> forbiddenLanguages;
  boolean includeBooksWithNoLanguage;

  public ForbiddenLanguageFilter(String forbiddenLanguageList, boolean includeBooksWithNoLanguage) {
    if (Helper.isNotNullOrEmpty(forbiddenLanguageList)) {
      forbiddenLanguages = new LinkedList<Language>();
      List<String> isoCodes = Helper.tokenize(forbiddenLanguageList.toLowerCase(Locale.ENGLISH), ",", true);
      for (String isoCode : isoCodes) {
        Language lang = DataModel.INSTANCE.getMapOfLanguagesByIsoCode().get(isoCode);
        if (lang != null)
          forbiddenLanguages.add(lang);
      }
    }
    this.includeBooksWithNoLanguage = includeBooksWithNoLanguage;
  }

  private List<Language> getForbiddenLanguages() {
    return forbiddenLanguages;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(forbiddenLanguages))
      return true;

    if (Helper.isNullOrEmpty(book.getBookLanguages()))
      return includeBooksWithNoLanguage;

    for (Language requiredLanguage : getForbiddenLanguages()) {
      boolean found = false;

      for (Language bookLanguage : book.getBookLanguages()) {
        if (bookLanguage.equals(requiredLanguage)) {
          found = true;
          break;
        }
      }
      if (found)
        return false;
    }
    return true;
  }

}
