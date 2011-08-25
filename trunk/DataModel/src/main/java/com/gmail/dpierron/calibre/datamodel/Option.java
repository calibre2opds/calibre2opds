package com.gmail.dpierron.calibre.datamodel;

public enum Option {
  INCLUDE_SERIE_NUMBER,
  INCLUDE_TIMESTAMP,
  DONOTINCLUDE_RATING,
  SPLIT_BY_DATE,
  ;  
  
  public static boolean contains(Option[] options, Option checkedOption) {
    for (Option option : options) {
      if (option == checkedOption) return true;
    }
    return false;
  }
}
