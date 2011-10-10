package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.Helper;

import java.util.List;
import java.util.Locale;

public class RequiredTagsFilter implements BookFilter {

  private List<String> requiredTags;

  public RequiredTagsFilter(String requiredTagsList) {
    if (Helper.isNotNullOrEmpty(requiredTagsList))
      requiredTags = Helper.tokenize(requiredTagsList.toUpperCase(Locale.ENGLISH), ",", true);
  }

  private List<String> getRequiredTags() {
    return requiredTags;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    if (Helper.isNullOrEmpty(requiredTags))
      return true;

    for (String requiredTagName : getRequiredTags()) {
      boolean found = false;
      for (Tag tag : book.getTags()) {
        if (tag.getName().equalsIgnoreCase(requiredTagName)) {
          found = true;
          break;
        }
      }
      if (!found)
        return false;
    }
    return true;
  }

}
