package com.gmail.dpierron.calibre.datamodel.filter;

class BooleanOrFilter extends BooleanFilter {
  public BooleanOrFilter(BookFilter leftFilter, BookFilter rightFilter) {
    super(leftFilter, rightFilter, true);
  }
}
