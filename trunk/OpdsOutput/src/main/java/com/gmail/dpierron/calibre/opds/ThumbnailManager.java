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
    EBookFile file = book.getPreferredFile();
    if (file != null)
      return file.getName() + Constants.JPG_EXTENSION;
    else
      return Constants.THUMBNAIL_FILE;
  }

  /**
   *
   * @return
   */
  @Override
  String getImageHeightDat() {
    return Constants.THUMBNAILHEIGHT_DAT_FILENAME;
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
