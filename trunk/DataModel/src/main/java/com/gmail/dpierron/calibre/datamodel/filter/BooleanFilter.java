package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

public abstract class BooleanFilter implements BookFilter {

  private BookFilter leftFilter;
  private BookFilter rightFilter;
  private boolean isOrFilter; // if true, the filter is an OR filter, else it's an AND filter

  BooleanFilter(boolean orFilter) {
    isOrFilter = orFilter;
  }

  protected BooleanFilter(BookFilter leftFilter, BookFilter rightFilter, boolean orFilter) {
    this.leftFilter = leftFilter;
    this.rightFilter = rightFilter;
    isOrFilter = orFilter;
  }

  public BookFilter getLeftFilter() {
    return leftFilter;
  }

  public void setLeftFilter(BookFilter leftFilter) {
    this.leftFilter = leftFilter;
  }

  public BookFilter getRightFilter() {
    return rightFilter;
  }

  public void setRightFilter(BookFilter rightFilter) {
    this.rightFilter = rightFilter;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (isOrFilter)
      return getLeftFilter().didBookPassThroughFilter(book) || getRightFilter().didBookPassThroughFilter(book);
    else
      return getLeftFilter().didBookPassThroughFilter(book) && getRightFilter().didBookPassThroughFilter(book);
  }

}
