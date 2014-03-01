package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

import java.util.HashMap;
import java.util.Map;

public class ThumbnailManager extends ImageManager {
  Map<String, String> mapOfThumbnailUrlByBookId = new HashMap<String, String>();

  // CONSTRUCTOS

  public ThumbnailManager(int maxSize) {
    super(maxSize);
  }

  // METHODS and PROPERTIES

  /**
   *
   * @return
   */
  @Override
  String getResizedFilename() {
    return "c2o_thumbnail.jpg";
  }

  /**
   *
   * @param book
   * @return
   */
  String getResizedFilenameOld(Book book) {
    EBookFile file = book.getPreferredFile();
    if (file != null)
      return file.getName() + Constants.JPG_EXTENSION;
    else
      return getResizedFilename().substring(4);
  }

  public String getDefaultResizedFilename () {
    return "thumbnail.png";
  }

  /**
   *
   * @return
   */
  @Override
  String getImageHeightDat() {
    return "c2o_thumbnailHeight.dat";
  }

  /**
   *
   * @param book
   * @param url
   */
  public void addBook(Book book, String url) {
    mapOfThumbnailUrlByBookId.put(book.getId(), url);
  }

  /**
   *
   * @param book
   * @return
   */
  public String getThumbnailUrl(Book book) {
    return mapOfThumbnailUrlByBookId.get(book.getId());
  }

}
