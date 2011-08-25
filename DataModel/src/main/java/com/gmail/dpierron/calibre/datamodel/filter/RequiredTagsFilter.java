package com.gmail.dpierron.calibre.datamodel.filter;

import java.util.List;
import java.util.Locale;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.Helper;

public class RequiredTagsFilter implements BookFilter {

  List<String> requiredTags;
  boolean includeBooksWithNoTag;
  
  public RequiredTagsFilter(String requiredTagsList, boolean includeBooksWithNoTag) {
    if (Helper.isNotNullOrEmpty(requiredTagsList)) 
      requiredTags = Helper.tokenize(requiredTagsList.toUpperCase(Locale.ENGLISH), ",", true);
    this.includeBooksWithNoTag= includeBooksWithNoTag;
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
