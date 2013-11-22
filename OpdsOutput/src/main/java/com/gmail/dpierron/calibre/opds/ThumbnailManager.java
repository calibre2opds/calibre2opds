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
   * @param book
   * @return
   */
  @Override
  String getResultFilename(Book book) {
    return Constants.THUMBNAIL_FILENAME;
  }
  String getResultFilenameOld(Book book) {
    EBookFile file = book.getPreferredFile();
    if (file != null)
      return file.getName() + Constants.JPG_EXTENSION;
    else
      return Constants.THUMBNAIL_FILENAME.substring(4);
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
   * @param bookId
   * @return
   */
  String getThumbnailUrl(String bookId) {
    return mapOfThumbnailUrlByBookId.get(bookId);
  }

  /**
   *
   * @param book
   * @return
   */
  public String getThumbnailUrl(Book book) {
    return getThumbnailUrl(book.getId());
  }

}
