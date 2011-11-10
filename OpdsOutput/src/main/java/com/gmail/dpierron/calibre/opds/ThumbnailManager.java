package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

import java.util.HashMap;
import java.util.Map;

public class ThumbnailManager extends ImageManager {
  Map<String, String> mapOfThumbnailUrlByBookId = new HashMap<String, String>();

  public ThumbnailManager(int maxSize) {
    super(maxSize);
  }

  public void addBook(Book book, String url) {
    mapOfThumbnailUrlByBookId.put(book.getId(), url);
  }

  String getThumbnailUrl(String bookId) {
    return mapOfThumbnailUrlByBookId.get(bookId);
  }

  public String getThumbnailUrl(Book book) {
    return getThumbnailUrl(book.getId());
  }

  @Override
  String getResultFilename(Book book) {
    EBookFile file = book.getPreferredFile();
    if (file != null)
      return file.getName() + ".jpg";
    else
      return "thumbnail.jpg";
  }

  @Override
  String getImageHeightDat() {
    return "thumbnailHeight.dat";
  }

}
