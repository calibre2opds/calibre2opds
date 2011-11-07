package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.tools.Helper;

public class PublisherFilter implements BookFilter {

  /**
   * the filter string
   */
  private final String filterValue;

  /**
   * if true, all the tags that contain the filter string are valid
   */
  private final boolean contains;

  public PublisherFilter(String filterValue, boolean contains) {
    this.contains = contains;
    this.filterValue = (contains ? filterValue.toUpperCase() : filterValue); // if the "contains" flag is set, optimize by storing the filter string uppercased
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(filterValue))
      return true;

    if (Helper.isNotNullOrEmpty(book.getPublisher())) {
      if (contains) {
        if (book.getPublisher().getName().toUpperCase().contains(filterValue) || book.getPublisher().getSort().toUpperCase().contains(filterValue))
          return true;
      } else {
        if (book.getPublisher().getName().equalsIgnoreCase(filterValue) || book.getPublisher().getSort().equalsIgnoreCase(filterValue))
          return true;
      }
    }

    return false;
  }
}
