package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;


public class Series implements SplitableByLetter {
  private String id;
  private String name;
  private String sort;

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
    if (obj instanceof Series) {
      if (obj == null)
        return false;
      return (Helper.checkedCompare(((Series) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public String getTitleToSplitByLetter() {
    return getName();
  }
}
