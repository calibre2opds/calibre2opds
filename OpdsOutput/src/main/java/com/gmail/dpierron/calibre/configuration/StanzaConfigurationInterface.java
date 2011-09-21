package com.gmail.dpierron.calibre.configuration;

/**
 * Define all the 'set' methods that are available for setting
 * configuration settings.
 *
 * Note that the interface is not specific to Stanza mode, the
 * name is a legacy artifact
 *
 * ITIMPI:  Not sure what the purpose is of defining this interface?
 */

import java.io.File;

public interface StanzaConfigurationInterface extends ReadOnlyStanzaConfigurationInterface {

  public void setLanguage(String value);

  public void setCatalogFolderName(String value);

  public void setCatalogTitle(String value);

  public void setMaxBeforePaginate(int value);

  public void setBooksInRecentAdditions(int value);

  public void setWikipediaLanguage(String value);

  public void setIncludedFormatsList(String value);

  public void setSaveBandwidth(boolean value);

  public void setMinimizeChangedFiles(boolean value);

  public void setThumbnailGenerate(boolean value);

  public void setThumbnailHeight(int value);

  public void setGenerateOpds(boolean value);

  public void setGenerateHtml(boolean value);

  public void setGenerateOpdsDownloads(boolean value);

  public void setGenerateHtmlDownloads(boolean value);

  public void setSuppressRatingsInTitles(boolean value);

  public void setGenerateDownloads(boolean value);

  public void setMaxBeforeSplit(int value);

  public void setSplitTagsOn(String value);

  public void setIncludeBooksWithNoFile(boolean value);

  public void setBookLanguageTag(String value);

  public void setCryptFilenames(boolean value);

  public void setShowSeriesInAuthorCatalog(boolean value);

  public void setGenerateCrossLinks(boolean value);

  public void setGenerateExternalLinks(boolean value);

  public void setTagsToGenerate(String value);

  public void setTagsToExclude(String value);

  public void setMaxSummaryLength(int value);

  public void setGenerateTags(boolean value);

  public void setGenerateRecent(boolean value);

  public void setGenerateRatings(boolean value);

  public void setGenerateAllbooks(boolean value);

  public void setGenerateIndex(boolean value);

  public void setTargetFolder(File value);

  public void setDeviceMode(DeviceMode value);

  public void setCopyToDatabaseFolder(boolean value);

  public void setBrowseByCover(boolean value);

  public void setSplitByAuthorInitialGoToBooks(boolean value);

  public void setIncludeAboutLink(boolean value);

  public void setTagsToMakeDeep(String value);

  public void setBrowseByCoverWithoutSplit(boolean value);

  public void setMinBooksToMakeDeepLevel(int value);

  public void setCoverResize(boolean value);

  public void setCoverHeight(int value);

  public void setIncludeOnlyOneFile(boolean value);

  public void setCompatibilityTrick(CompatibilityTrick value);

  public void setZipTrookCatalog(boolean value);

  public void setReprocessEpubMetadata(boolean value);

  public void setMaxMobileResolution(int value);

  public void setSplitInAuthorBooks(boolean value);

  public void setSplitInSeriesBooks(boolean value);

  public void setUrlBase(String value);

  /* external links */
  public void setWikipediaUrl(String value);

  public void setAmazonAuthorUrl(String value);

  public void setAmazonIsbnUrl(String value);

  public void setAmazonTitleUrl(String value);

  public void setGoodreadAuthorUrl(String value);

  public void setGoodreadIsbnUrl(String value);

  public void setGoodreadTitleUrl(String value);

  public void setGoodreadReviewIsbnUrl(String value);

  public void setIsfdbAuthorUrl(String value);

  public void setLibrarythingAuthorUrl(String value);

  public void setLibrarythingIsbnUrl(String value);

  public void setLibrarythingTitleUrl(String value);
}
