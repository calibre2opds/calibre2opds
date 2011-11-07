package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.tools.Helper;

public class TagPresenceFilter implements BookFilter {

  boolean present;

  public TagPresenceFilter(boolean present) {
    this.present = present;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (present)
      return Helper.isNotNullOrEmpty(book.getTags());
    else
      return Helper.isNullOrEmpty(book.getTags());
  }
}
