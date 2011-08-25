package com.gmail.dpierron.calibre.datamodel;

public enum BookRating {
  ONE(2),
  TWO(4),
  THREE(6),
  FOUR(8),
  FIVE(10),
  NOTRATED(0);
  
  private int value;
  
  private BookRating(int value) {
    this.value = value;
  }
  
  public static BookRating[] sortedRatings() {
    return new BookRating[] {FIVE, FOUR, THREE, TWO, ONE, NOTRATED};
  }
  
  public String getId() {
    return "RATED_"+name();
  }
  
  public int getValue() {
    return value;
  }

  public static BookRating fromValue(int value) {
    for (BookRating rating : BookRating.values()) {
      if (rating.getValue() == value) return rating;
    }
    return NOTRATED;
  }
}
