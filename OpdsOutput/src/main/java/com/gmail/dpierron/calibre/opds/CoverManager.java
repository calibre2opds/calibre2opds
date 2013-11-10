package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

public class CoverManager extends ImageManager {

  // CONSTRUCTORS

  public CoverManager(int maxSize) {
    super(maxSize);
  }

  // METHODS and PROPERTIES

  /**
   *
   * @param book
   * @return
   */
  @Override
  public String getResultFilename(Book book) {
    EBookFile file = book.getPreferredFile();
    String result;
    result = (file != null) ? file.getName() + Constants.TYPE_SEPARATOR + Constants.RESIZED_COVER_FILENAME
                            : Constants.RESIZED_COVER_FILENAME;
    return result;
  }

  /**
   *
   * @return
   */
  @Override
  public String getImageHeightDat() {
    return Constants.COVERHEIGHT_DAT_FILENAME;
  }

}
