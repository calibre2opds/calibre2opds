package com.gmail.dpierron.calibre.datamodel;
/**
 * Defines the interface for implementing the Split-by-Date functionality
 *
 * The field returned should also be the field that has been used for sorting by date.
 */

public interface SplitableByDate {
  public String getTitleToSplitByDate();
}