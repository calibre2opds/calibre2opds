package com.gmail.dpierron.calibre.opds.indexer;

import java.util.Map;
import java.util.TreeMap;

/**
 * a keyword, with all the catalog items it links to
 */
public class Keyword implements Comparable<Keyword> {
  long id;
  String word;
  Map<ItemType, CatalogItem> catalogItems;

  public Keyword(long id, String word) {
    this.id = id;
    this.word = word;
  }

  public void addCatalogItem(ItemType type, BookEntry bookEntry) {
    if (catalogItems == null) {
      catalogItems = new TreeMap<ItemType, CatalogItem>();
      catalogItems.put(type, new CatalogItem(type, bookEntry));
    } else {
      CatalogItem item = catalogItems.get(type);
      if (item == null) {
        item = new CatalogItem(type);
        catalogItems.put(type, item);
      }
      item.addBook(bookEntry);
    }
  }

  public int compareTo(Keyword o) {
    return word.compareTo(o.word);
  }

  public String toString() {
    return "" + word + (catalogItems != null ? ":" + catalogItems : "");
  }

  public int size() {
    if (catalogItems == null)
      return 0;

    int size = 0;
    for (CatalogItem catalogItem : catalogItems.values()) {
      size += catalogItem.size();
    }
    return size;
  }

}
