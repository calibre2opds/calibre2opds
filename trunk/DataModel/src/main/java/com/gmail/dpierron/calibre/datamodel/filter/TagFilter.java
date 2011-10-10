package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.Helper;

public class TagFilter implements BookFilter {

  private final String requiredTag;

  public TagFilter(String requiredTag) {
    this.requiredTag = requiredTag;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(requiredTag))
      return true;

    for (Tag tag : book.getTags()) {
      if (tag.getName().equalsIgnoreCase(requiredTag)) {
        return true;
      }
    }
    return false;
  }
}
