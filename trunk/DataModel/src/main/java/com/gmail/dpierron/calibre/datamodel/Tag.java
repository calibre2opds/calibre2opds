package com.gmail.dpierron.calibre.datamodel;

import java.util.List;

import com.gmail.dpierron.tools.Helper;



public class Tag implements SplitableByLetter, Comparable<Tag> {
  private String id;
  private String name;
  private String[] partsOfTag;
  private int[] partsOfTagHash;
  
  public Tag(String id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return getName();
  }
  
  public String getTitleToSplitByLetter(Object options) {
    return getName();
  }

  public String[] getPartsOfTag(String splitTagsOn) {
    if (partsOfTag == null) {
      List<String> parts = Helper.tokenize(getName(), splitTagsOn);
      partsOfTag = new String[parts.size()];
      partsOfTagHash = new int[partsOfTag.length];
      for (int i = 0; i < parts.size(); i++) {
        String part = parts.get(i);
        partsOfTag[i] = part;
        partsOfTagHash[i] = (part == null ? -1 : part.hashCode());
      }
    }
    return partsOfTag;
  }
  
  public int[] getPartsOfTagHash(String splitTagsOn) {
    if (partsOfTag == null) 
      getPartsOfTag(splitTagsOn);
    return partsOfTagHash;
  }

  public int compareTo(Tag o) {
    if (o == null) return 1;
    return Helper.trueStringCompare(getId(), o.getId());
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tag) {
      if (obj == null) return false;
      return (Helper.checkedCompare(((Tag) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  
}
