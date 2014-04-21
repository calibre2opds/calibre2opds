package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;


public class Series implements SplitableByLetter {
  private final String id;
  private final String name;
  private final String sort;
  boolean done = false;

  public Series(String id, String name, String sort) {
    super();
    this.id = id;
    this.name = name;
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
    if (obj instanceof Series) {
      return (Helper.checkedCompare(((Series) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public String getTitleToSplitByLetter() {
    return getName();
  }

  public void setDone() {
    done = true;
  }

  public boolean isDone() {
    return done;
  }
}
