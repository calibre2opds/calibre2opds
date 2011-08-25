package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

public interface BookFilter {
  public boolean didBookPassThroughFilter(Book book);
}
