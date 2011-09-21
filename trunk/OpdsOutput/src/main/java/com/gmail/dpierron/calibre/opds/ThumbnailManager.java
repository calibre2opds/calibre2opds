package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

public class ThumbnailManager extends ImageManager {


  public ThumbnailManager(int maxSize) {
    super(maxSize);
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
