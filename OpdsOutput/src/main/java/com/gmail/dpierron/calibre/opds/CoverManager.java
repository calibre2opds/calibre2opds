package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;

public class CoverManager extends ImageManager {

  // CONSTRUCTORS

  public CoverManager(int maxSize) {
    super(maxSize);
  }

  // METHODS and PROPERTIES

  public void reset() {
    super.reset();
  }
  /**
   *
   * @return
   */
  @Override
  public String getResizedFilename() {
    return "c2o_resizedcover.jpg";
  }

  public String getResizedFilenameOld(Book book) {
    EBookFile file = book.getPreferredFile();
    String result;
    result = (file != null) ? file.getName() + Constants.TYPE_SEPARATOR + getResizedFilename().substring(4)
                            : getResizedFilename().substring(4);
    return result;
  }

  public String getDefaultResizedFilename() {
    return "cover.png";
  }

  /**
   *
   * @return
   */
  @Override
  public String getImageHeightDat() {
    return "c2o_coverHeight.dat";
  }

}
