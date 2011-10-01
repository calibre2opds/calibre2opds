package com.gmail.dpierron.calibre.datamodel.filter;

public class BooleanOrFilter extends BooleanFilter {
  public BooleanOrFilter() {
    super(true);
  }

  public BooleanOrFilter(BookFilter leftFilter, BookFilter rightFilter) {
    super(leftFilter, rightFilter, true);
  }
}
