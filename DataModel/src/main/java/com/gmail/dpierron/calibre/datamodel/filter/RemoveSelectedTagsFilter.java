package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RemoveSelectedTagsFilter implements BookFilter {

  private final Set<Tag> tagsToRemove;

  public RemoveSelectedTagsFilter(Set<Tag> tagsToRemove) {
    this.tagsToRemove = tagsToRemove;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    List<Tag> tags = new LinkedList<Tag>(book.getTags());
    for (Tag tag : tags) {
      if (tagsToRemove.contains(tag)) {
        book.getTags().remove(tag);
      }
    }

    return book.getTags().size() != 0;

  }

}
