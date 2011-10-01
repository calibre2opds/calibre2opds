package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

/**
 * Let all books pass
 */
public class PassthroughFilter implements BookFilter {
  public boolean didBookPassThroughFilter(Book book) {
    return true;
  }
}
