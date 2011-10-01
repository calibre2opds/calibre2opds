package com.gmail.dpierron.calibre.datamodel.filter;

public class BooleanAndFilter extends BooleanFilter {
  public BooleanAndFilter() {
    super(false);
  }

  public BooleanAndFilter(BookFilter leftFilter, BookFilter rightFilter) {
    super(leftFilter, rightFilter, false);
  }
}
