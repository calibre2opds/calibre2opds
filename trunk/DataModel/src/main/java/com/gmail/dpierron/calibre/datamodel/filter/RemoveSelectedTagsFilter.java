package com.gmail.dpierron.calibre.datamodel.filter;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;

public class RemoveSelectedTagsFilter implements BookFilter {

  Set<Tag> tagsToRemove;

  public RemoveSelectedTagsFilter(Set<Tag> tagsToRemove) {
    this.tagsToRemove = tagsToRemove;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    List<Tag> tags = new Vector<Tag>(book.getTags());
    for (Tag tag : tags) {
      if (tagsToRemove.contains(tag)) {
        book.getTags().remove(tag);
      }
    }

    if (book.getTags().size() == 0)
      return false;

    return true;
  }

}
