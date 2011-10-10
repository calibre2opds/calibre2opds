package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Language;
import com.gmail.dpierron.tools.Helper;

import java.util.List;

public class LanguageFilter implements BookFilter {

  private final String requiredLanguage;

  public LanguageFilter(String requiredLanguage) {
    this.requiredLanguage = requiredLanguage;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(requiredLanguage))
      return true;

    List<Language> bookLanguages = book.getBookLanguages();
    if (Helper.isNullOrEmpty(bookLanguages))
      return false;

    for (Language language : bookLanguages) {
      if (language.equals(requiredLanguage)) { // Language overrides equals()
        return true;
      }
    }
    return false;
  }
}
