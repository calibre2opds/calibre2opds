package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

public class NotFilter implements BookFilter {
  private BookFilter negatedFilter;

  public NotFilter(BookFilter negatedFilter) {
    this.negatedFilter = negatedFilter;
  }

  public boolean didBookPassThroughFilter(Book book) {
    return !negatedFilter.didBookPassThroughFilter(book);
  }
}
