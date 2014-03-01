package com.gmail.dpierron.calibre.opds;


public interface CatalogCallbackInterface {

  // main catalog
  public void startCreateMainCatalog();

  public void endCreateMainCatalog(String where, long timeInHtml);

  // show options
  public void dumpOptions();

  // database

  public void startReadDatabase();

  public void endReadDatabase(long milliseconds, String summary);

  // Tags

  public void setTagCount(String summary);

  public void startCreateTags(long nb);

  public void endCreateTags(long milliseconds);

  public void disableCreateTags();

  // Authors

  public void setAuthorCount(String summary);

  public void startCreateAuthors(long nb);

  public void endCreateAuthors(long milliseconds);

  public void disableCreateAuthors();

  // Series

  public void setSeriesCount(String summary);

  public void startCreateSeries(long nb);

  public void endCreateSeries(long milliseconds);

  public void disableCreateSeries();

  // Recent books

  public void startCreateRecent(long nb);

  public void endCreateRecent(long milliseconds);

  public void disableCreateRecent();

  // Rated books

  public void startCreateRated(long nb);

  public void endCreateRated(long milliseconds);

  public void disableCreateRated();

  // All books

  public void startCreateAllbooks(long nb);

  public void endCreateAllbooks(long milliseconds);

  public void disableCreateAllBooks();

  // Featured books

  public void setFeaturedCount(String summary);

  public void startCreateFeaturedBooks(long nb);

  public void endCreateFeaturedBooks(long milliseconds);

  public void disableCreateFeaturedBooks();

  // Custom catalogs

  public void startCreateCustomCatalogs(long nb);

  public void endCreateCustomCatalogs(long milliseconds);

  public void disableCreateCustomCatalogs();

  // ePub metadata

  public void startReprocessingEpubMetadata(long nb);

  public void endReprocessingEpubMetadata(long milliseconds);

  public void disableReprocessingEpubMetadata();

  public void incStepProgressIndicatorPosition();

  public void showMessage(String message);

  // Javascript database

  public void startCreateJavascriptDatabase(long nb);

  public void endCreateJavascriptDatabase(long milliseconds);

  public void disableCreateJavascriptDatabase();

  // Copy to target

  public void setCopyLibCount(String summary);

  public void setCopyCatCount(String summary);

  public void startCopyLibToTarget(long nb);

  public void startCopyCatToTarget(long nb);

  public void endCopyLibToTarget(long milliseconds);

  public void endCopyCatToTarget(long milliseconds);

  public void startZipCatalog(long nb);

  public void endZipCatalog(long milliseconds);

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
   * @throws GenerationStoppedException if the user has specified that he wants to stop the generation
   */
  public void checkIfContinueGenerating() throws GenerationStoppedException;

  public void resetWarnCount();
  public int getWarnCount();
  public void incrementWarnCount();

  public void setStopGenerating ();
}
