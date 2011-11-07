package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.BookRating;

public class RatingPresenceFilter implements BookFilter {

  boolean present;

  public RatingPresenceFilter(boolean present) {
    this.present = present;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (present)
      return book.getRating() != BookRating.NOTRATED;
    else
      return book.getRating() == BookRating.NOTRATED;
  }
}
