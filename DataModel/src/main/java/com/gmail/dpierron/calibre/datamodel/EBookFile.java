package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.tools.Helper;

public class EBookFile {
  EBookFormat format;
  String extension;
  String name;
  Book book;
  
  public EBookFile(String format, String name) {
    super();
    if (Helper.isNotNullOrEmpty(format)) {
      this.format = EBookFormat.fromFormat(format);
      this.extension = "." + format.toLowerCase();
    }
    this.name = name;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book value) {
    this.book = value;
  }
  
  public EBookFormat getFormat() {
    return format;
  }
  
  void setFormat(EBookFormat format) {
    this.format = format;
  }
  
  public String getName() {
    return name;
  }
  
  public String getExtension() {
    return extension;
  }
  
  void setName(String name) {
    this.name = name;
  }
  
  public CachedFile getFile() {
    return CachedFileManager.INSTANCE.addCachedFile(getBook().getBookFolder(), getName() + getExtension());
  }
  
  public String toString() {
    return "" + getFormat() + " - " + getName();
  }
}
