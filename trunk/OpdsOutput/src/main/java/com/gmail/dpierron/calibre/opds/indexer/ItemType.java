package com.gmail.dpierron.calibre.opds.indexer;

/**
 * the type of catalog item
 */
public enum ItemType {
  /* don't change the codes without changing the javascript in the search page ! */
  BookTitle("T"),
  BookComment("C"),
  Author("A"),
  Tag("t"),
  Series("S");

  String code;

  private ItemType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
