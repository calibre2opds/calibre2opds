package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.tools.Helper;

public class EBookFile {
  private EBookFormat format;
  private String extension;
  private String name;
  private Book book;

  public EBookFile(String format, String name) {
    super();
    if (Helper.isNotNullOrEmpty(format)) {
      this.format = EBookFormat.fromFormat(format);
      this.extension = "." + format.toLowerCase();
    }
    this.name = name;
  }

  Book getBook() {
    return book;
  }

  public void setBook(Book value) {
    this.book = value;
  }

  public EBookFormat getFormat() {
    return format;
  }

  public String getName() {
    return name;
  }

  public String getExtension() {
    return extension;
  }

  public CachedFile getFile() {
    return CachedFileManager.INSTANCE.addCachedFile(getBook().getBookFolder(), getName() + getExtension());
  }

  public String toString() {
    return "" + getFormat() + " - " + getName();
  }
}
