package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

public class CoverManager extends ImageManager {


  public CoverManager(int maxSize) {
    super(maxSize);
  }

  String getResultFilename(Book book) {
    EBookFile file = book.getPreferredFile();
    String result;
    result = (file != null) ? file.getName() + "_resizedcover.jpg" : "resizedcover.jpg";
    return result;
  }

  @Override
  String getImageHeightDat() {
    return "coverHeight.dat";
  }

}
