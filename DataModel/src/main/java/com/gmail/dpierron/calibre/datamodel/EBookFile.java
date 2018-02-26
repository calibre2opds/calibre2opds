package com.gmail.dpierron.calibre.datamodel;

/**
 *
 */

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.tools.Helper;

public class EBookFile {
  private EBookFormat format;
  private String name;
  private Book book;

  /*               CONSTRUCTORS                  */
  public EBookFile(String format, String name) {
    super();
    if (Helper.isNotNullOrEmpty(format)) {
      this.format = EBookFormat.fromFormat(format);
    }
    this.name = name;
  }

  /*            METHODS and PROPERTIES             */

  /**
   * Set the book assoicated with this particular file instance
   * @param value
   */
  public void setBook(Book value) {
    this.book = value;
  }

  /**
   *
   * @return
   */
  public EBookFormat getFormat() {
    return format;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  public String getExtension() {
    return "." + format.toString().toLowerCase();
  }

  /**
   *
   * @return
   */
  public CachedFile getFile() {
    return CachedFileManager.addCachedFile(book.getBookFolder(), getName() + getExtension());
  }

  /**
   *
   * @return
   */
  public String toString() {
    return "" + getFormat() + " - " + getName();
  }
}
