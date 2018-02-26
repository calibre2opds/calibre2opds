package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.List;


public class Tag extends GenericDataObject implements SplitableByLetter, Comparable<Tag> {
  private final String id;
  private final String name;
  private String[] partsOfTag;
  // Flags
  // NOTE: Using byte plus bit settings is more memory efficient than using boolean types
  private final static byte FLAG_ALL_CLEAR = 0;
  private final static byte FLAG_DONE = 0x01;
  private final static byte FLAG_REFERENCED = 0x02;
  private byte flags = FLAG_ALL_CLEAR;

  //            CONSTRUCTOR

  public Tag(String id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  // METHODS and PROPERTIES implementing Abstract ones from Base class)

  public ColumType getColumnType() {
    return ColumType.COLUMN_TAGS;
  }
  public String getColumnName() {
    return "tags";
  }
  public String getDisplayName() {
    return name;
  }
  public String getSortName() {
    return name;
  }
  public String getTextToSort() {
    return name;
  }
  public String getTextToDisplay() {
    return name;
  }

  //                            METHODS and PROPERTIES

  public String getId() {
    return id;
  }



  public String toString() {
    return getTextToSort();
  }


  public String[] getPartsOfTag(String splitTagsOn) {
    if (partsOfTag == null) {
      List<String> parts = Helper.tokenize(getTextToSort(), splitTagsOn);
      partsOfTag = new String[parts.size()];
      int[] partsOfTagHash = new int[partsOfTag.length];
      for (int i = 0; i < parts.size(); i++) {
        String part = parts.get(i);
        partsOfTag[i] = part;
        partsOfTagHash[i] = (part == null ? -1 : part.hashCode());
      }
    }
    return partsOfTag;
  }

  public int compareTo(Tag o) {
    if (o == null)
      return 1;
    return Helper.trueStringCompare(getId(), o.getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Tag) {
      return (Helper.checkedCompare(((Tag) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public void setDone() {
    flags |= FLAG_DONE;
  }
  public boolean isDone () {
    return ((flags & FLAG_DONE) != 0);
  }

  public void setReferenced() {
    flags |= FLAG_REFERENCED;
  }
  public boolean isReferenced () {
    return ((flags & FLAG_REFERENCED) != 0);
  }
}
