package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

public class Publisher implements SplitableByLetter, Comparable<Publisher> {
  private final String id;
  private String name;
  private final String sort;
  private boolean done;
  // Flags
  // NOTE: Using byte plus bit settings is more memory efficient than using boolean types
  private final static byte FLAG_ALL_CLEAR = 0;
  private final static byte FLAG_DONE = 0x01;
  private final static byte FLAG_REFERENCED = 0x02;
  private byte flags = FLAG_ALL_CLEAR;


  public Publisher(String id, String name, String sort) {
    super();
    this.id = id;
    this.name = name;
    if (Helper.isNullOrEmpty(sort)) {
      this.sort = name.replaceAll(" ", "").toUpperCase();
    } else
      this.sort = sort;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSort() {
    return sort;
  }

  public String toString() {
    return getId() + " - " + getName();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Publisher) {
      return (Helper.checkedCompare(((Publisher) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public String getTextToSort() {
    return getName();
  }

  /* Comparable interface, used to sort an authors list */

  public int compareTo(Publisher o) {
    if (o == null)
      return 1;
    else {
      return Helper.trueStringCompare(getSort(), o.getSort());
    }
  }

  public void setDone() {
    done = true;
  }

  public boolean isDone() {
    return done;
  }
}
