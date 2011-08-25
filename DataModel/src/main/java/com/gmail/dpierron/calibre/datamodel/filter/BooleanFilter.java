package com.gmail.dpierron.calibre.datamodel.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.gmail.dpierron.calibre.datamodel.Book;

public class BooleanFilter implements BookFilter {

  private List<BookFilter> filters = new Vector<BookFilter>();
  
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
