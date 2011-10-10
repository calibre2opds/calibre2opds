package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.Helper;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ForbiddenTagsFilter implements BookFilter {

  private List<String> forbiddenTags;

  public ForbiddenTagsFilter(String forbiddenTagsList) {
    if (Helper.isNotNullOrEmpty(forbiddenTagsList))
      forbiddenTags = Helper.tokenize(forbiddenTagsList.toUpperCase(Locale.ENGLISH), ",", true);
  }

  private List<String> getForbiddenTags() {
    return forbiddenTags;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(forbiddenTags))
      return true;

    List<Tag> tags = new LinkedList<Tag>(book.getTags());
    for (Tag tag : tags) {
      if (getForbiddenTags().contains(tag.getName().toUpperCase()))
        return false;
    }

    return true;
  }

}
