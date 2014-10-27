package com.gmail.dpierron.calibre.datamodel.filter;
/**
 * Created by WalkerDJ on 04/10/2014.
 */

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.tools.Helper;

public class CustomColumnPresenceFilter implements BookFilter {


  boolean present;

  public CustomColumnPresenceFilter(boolean present) {
    this.present = present;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;
    if (present)
      return Helper.isNotNullOrEmpty(book.getCustomColumnValues());
    else
      return Helper.isNullOrEmpty(book.getCustomColumnValues());
  }
}
