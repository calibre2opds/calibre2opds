package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.tools.Helper;

public class SeriesPresenceFilter implements BookFilter {

  boolean present;

  public SeriesPresenceFilter(boolean present) {
    this.present = present;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (present)
      return Helper.isNotNullOrEmpty(book.getSeries());
    else
      return Helper.isNullOrEmpty(book.getSeries());
  }
}
