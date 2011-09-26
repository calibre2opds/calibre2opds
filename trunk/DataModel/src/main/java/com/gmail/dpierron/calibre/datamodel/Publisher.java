package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

public class Publisher implements SplitableByLetter, Comparable<Publisher> {
  private String id;
  private String name;
  private String sort;

  public Publisher(String id, String name, String sort) {
    super();
    this.id = id;
    this.name = name;
    this.sort = sort;
  }

  public String getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;
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
    if (obj instanceof Publisher) {
      if (obj == null)
        return false;
      return (Helper.checkedCompare(((Publisher) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public String getTitleToSplitByLetter() {
    return getName();
  }

  /* Comparable interface, used to sort an authors list */

  public int compareTo(Publisher o) {
    if (o == null)
      return 1;
    else {
      int i = Helper.trueStringCompare(getSort(), o.getSort());
      return i;
    }
  }


}
