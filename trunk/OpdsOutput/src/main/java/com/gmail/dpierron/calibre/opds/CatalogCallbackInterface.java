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

  // Authors

  public void setAuthorCount(String summary);

  public void startCreateAuthors(long nb);

  public void endCreateAuthors(long milliseconds);

  // Series

  public void setSeriesCount(String summary);

  public void startCreateSeries(long nb);

  public void endCreateSeries(long milliseconds);

  // Recent books

  public void startCreateRecent(long nb);

  public void endCreateRecent(long milliseconds);

  // Rated books

  public void startCreateRated(long nb);

  public void endCreateRated(long milliseconds);

  // All books

  public void startCreateAllbooks(long nb);

  public void endCreateAllbooks(long milliseconds);

  // Featured books

  public void setFeaturedCount(String summary);

  public void startCreateFeaturedBooks(long nb);

  public void endCreateFeaturedBooks(long milliseconds);

  // Custom catalogs

  public void startCreateCustomCatalogs(long nb);

  public void endCreateCustomCatalogs(long milliseconds);

  // Thumbnails

  public void startCreateThumbnails(long nb);

  public void endCreateThumbnails(long milliseconds);

  // Covers

  public void startCreateCovers(long nb);

  public void endCreateCovers(long milliseconds);

  // ePub metadata

  public void startReprocessingEpubMetadata(long nb);

  public void endReprocessingEpubMetadata(long milliseconds);

  public void incStepProgressIndicatorPosition();

  public void showMessage(String message);

  // Javascript database

  public void startCreateJavascriptDatabase(long nb);

  public void endCreateJavascriptDatabase(long milliseconds);

  // Copy to target

  public void setCopyLibCount(String summary);

  public void setCopyCatCount(String summary);

  public void startCopyLibToTarget(long nb);

  public void startCopyCatToTarget(long nb);

  public void endCopyLibToTarget(long milliseconds);

  public void endCopyCatToTarget(long milliseconds);

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
}
