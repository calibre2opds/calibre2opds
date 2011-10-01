package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;

import java.util.LinkedList;
import java.util.List;

public class FilterHelper {
  public static List<Book> filter(BookFilter filter, List<Book> books) {
    List<Book> result = new LinkedList<Book>();
    for (Book book : books) {
      if (filter.didBookPassThroughFilter(book)) {
        result.add(book);
      }
    }
    return result;
  }
}
