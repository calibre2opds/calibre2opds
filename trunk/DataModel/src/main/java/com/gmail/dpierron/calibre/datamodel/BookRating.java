package com.gmail.dpierron.calibre.datamodel;

public enum BookRating {
  ONE(2, '1'),
  TWO(4, '2'),
  THREE(6, '3'),
  FOUR(8, '4'),
  FIVE(10, '5'),
  NOTRATED(0, '0');

  private final int value;
  private final char charEquivalent;
  private boolean done = false;

  private BookRating(int value, char charEquivalent) {
    this.value = value;
    this.charEquivalent = charEquivalent;
  }

  public static BookRating[] sortedRatings() {
    return new BookRating[]{FIVE, FOUR, THREE, TWO, ONE, NOTRATED};
  }

  public String getId() {
    // return "RATED_" + name();
    return name();
  }

  public int getValue() {
    return value;
  }

  public char getCharEquivalent() {
    return charEquivalent;
  }

  public static BookRating fromValue(int value) {
    for (BookRating rating : BookRating.values()) {
      if (rating.getValue() == value)
        return rating;
    }
    return NOTRATED;
  }

  public void setDone() { done = true;}
  public boolean isDone() { return done; }
}
