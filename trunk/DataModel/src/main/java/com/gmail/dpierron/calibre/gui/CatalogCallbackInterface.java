package com.gmail.dpierron.calibre.gui;

/**
 * Method that provides ways of interacting with the GUI
 *
 * This interface is defined in the DataModel package so that classes
 * in this package can interact with the GUI components that are
 * instantiated within the OpdsOuputput pckage.    It is oone this
 * way to avoid circular references.
 */

import com.gmail.dpierron.calibre.gui.GenerationStoppedException;

public interface CatalogCallbackInterface {

  //  Progrees Indicator

  public void setProgressMax(long max);

  public void incStepProgressIndicatorPosition();

  // show options
  public void dumpOptions();

  // main catalog
  public void startInitializeMainCatalog();

  public void endInitializeMainCatalog();

  public void startFinalizeMainCatalog();

  public void endFinalizeMainCatalog(String where, long timeInHtml);

  // database

  public void startReadDatabase();

  public void endReadDatabase(String summary);

  // Tags

  public void setTagCount(String summary);

  public void startCreateTags(long nb);

  public void endCreateTags();

  public void disableCreateTags();

  // Authors

  public void setAuthorCount(String summary);

  public void startCreateAuthors(long nb);

  public void endCreateAuthors();

  public void disableCreateAuthors();

  // Series

  public void setSeriesCount(String summary);

  public void startCreateSeries(long nb);

  public void endCreateSeries();

  public void disableCreateSeries();

  // Recent books

  public void setRecentCount(String summary);

  public void startCreateRecent(long nb);

  public void endCreateRecent();

  public void disableCreateRecent();

  // Rated books

  public void startCreateRated(long nb);

  public void endCreateRated();

  public void disableCreateRated();

  // All books

  public void startCreateAllbooks(long nb);

  public void endCreateAllbooks();

  public void disableCreateAllBooks();

  // Featured books

  public void setFeaturedCount(String summary);

  public void startCreateFeaturedBooks(long nb);

  public void endCreateFeaturedBooks();

  public void disableCreateFeaturedBooks();

  // Custom catalogs

  public void startCreateCustomCatalogs(long nb);

  public void endCreateCustomCatalogs();

  public void disableCreateCustomCatalogs();

  // ePub metadata

  public void startReprocessingEpubMetadata(long nb);

  public void endReprocessingEpubMetadata();

  public void disableReprocessingEpubMetadata();

  public void showMessage(String message);

  // Javascript database

  public void startCreateJavascriptDatabase(long nb);

  public void endCreateJavascriptDatabase();

  public void disableCreateJavascriptDatabase();

  // Copy to target

  public void setCopyLibCount(String summary);

  public void setCopyCatCount(String summary);

  public void startCopyLibToTarget(long nb);

  public void startCopyCatToTarget(long nb);

  public void endCopyLibToTarget();

  public void disableCopyLibToTarget();

  public void endCopyCatToTarget();

  public void startZipCatalog(long nb);

  public void endZipCatalog();

  public void disableZipCatalog();



  // Error
  public void errorOccured(String message, Throwable error);

  /**
   * ask the user a question
   *
   * @param message         the question
   * @param possibleAnswers the possible answers (3 max)
   * @return the number of the choosen answer, -1 if error or cancel
   */
  public int askUser(String message, String... possibleAnswers);

  /**
   * @throws com.gmail.dpierron.calibre.gui.GenerationStoppedException if the user has specified that he wants to stop the generation
   */
  public void checkIfContinueGenerating() throws GenerationStoppedException;

  public void resetWarnCount();
  public int getWarnCount();
  public void incrementWarnCount();

  public void setStopGenerating();
  public void clearStopGenerating();
}
