package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BooleanFilter implements BookFilter {

  private List<BookFilter> filters = new LinkedList<BookFilter>();

  public void addFilter(BookFilter filter) {
    if (filters.contains(filter))
      return;
    filters.add(filter);
  }

  public boolean didBookPassThroughFilter(Book book) {
    boolean result = true;
    Iterator<BookFilter> iterator = filters.iterator();
    while (result && iterator.hasNext())
      result &= iterator.next().didBookPassThroughFilter(book);
    return result;
  }


}
