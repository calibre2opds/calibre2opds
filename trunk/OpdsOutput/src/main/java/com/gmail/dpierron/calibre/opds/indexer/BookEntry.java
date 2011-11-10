package com.gmail.dpierron.calibre.opds.indexer;

import com.gmail.dpierron.calibre.datamodel.Book;

/**
 * a book entry, along with its title, url to the book page and url to the book cover
 */
public class BookEntry {
  Book book;
  String url;
  String thumbnailUrl;

  public BookEntry(Book book, String url, String thumbnailUrl) {
    this.book = book;
    this.thumbnailUrl = thumbnailUrl;
    this.url = url;
  }
}
