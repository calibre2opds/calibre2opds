package com.gmail.dpierron.calibre.opds.indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * a catalog item, links a book to a keyword, has a type
 */
public class CatalogItem {
  private static int INITIAL_SIZE = 5;

  ItemType type;
  List<BookEntry> bookEntries;

  public CatalogItem(ItemType type) {
    this.type = type;
  }

  public CatalogItem(ItemType type, BookEntry bookEntry) {
    this(type);
    addBook(bookEntry);
  }

  public void addBook(BookEntry bookEntry) {
    if (bookEntries == null)
      bookEntries = new ArrayList<BookEntry>(INITIAL_SIZE);

    bookEntries.add(bookEntry);
  }

  public int size() {
    if (bookEntries != null)
      return bookEntries.size();
    else
      return 0;
  }
}
