package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.BookRating;
import com.gmail.dpierron.tools.Helper;

public class RatingFilter implements BookFilter {

  private final char rating;
  private final char comparator;

  public RatingFilter(char comparator, char rating) {
    this.rating = rating;
    this.comparator = comparator;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(rating))
      return true;

    if (Helper.isNullOrEmpty(comparator))
      return true;

    char bookRating = '0';
    BookRating rating1 = book.getRating();
    if (rating1 != null && rating1 != BookRating.NOTRATED)
      bookRating = rating1.getCharEquivalent();

    switch (comparator) {
      case '=':
        return bookRating == rating;
      case '<':
        return bookRating < rating;
      case '>':
        return bookRating > rating;
      default:
        return false;
    }

  }
}
