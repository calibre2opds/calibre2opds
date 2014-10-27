package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;


public class Series implements SplitableByLetter {
  private final String id;            // Calibre database ID for this series
  private final String name;          // Display name
  private final String sort;          // Sort name set in Calibre
  private boolean done = false;       // sse true when this series has been generated
  private boolean referenced = false; // Set true if referenced (which means entry needs generating

  public Series(String id, String name, String sort) {
    super();
    assert (Helper.isNotNullOrEmpty(name));
    this.id = id;
    this.name = name;
    // We try abnd optimise storage by pointing sort at the
    // name field if they areidentical
    if (Helper.isNotNullOrEmpty(sort) && sort.equalsIgnoreCase(name)) {
      this.sort = name;
    } else {
      this.sort = sort;
    }
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

  public void setReferenced() {
    referenced = true;
  }
  public boolean isReferenced() {
    return referenced;
  }
}
