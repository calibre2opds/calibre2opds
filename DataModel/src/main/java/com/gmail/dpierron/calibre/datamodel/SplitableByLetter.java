package com.gmail.dpierron.calibre.datamodel;

/**
 * Defines the interface for implementing the Split-by-letter functionality
 *
 * The field returned should also be the field that has been used for sorting by title.
 */

public interface SplitableByLetter {
  public String getTextToSort();
}
