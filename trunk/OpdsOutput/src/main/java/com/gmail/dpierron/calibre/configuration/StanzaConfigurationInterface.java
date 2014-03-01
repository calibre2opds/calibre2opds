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

import com.gmail.dpierron.tools.Composite;

import java.io.File;
import java.util.List;

public interface StanzaConfigurationInterface extends ReadOnlyStanzaConfigurationInterface {

  public void setLanguage(String value);

  public void setCatalogFolderName(String value);

  public void setOnlyCatalogAtTarget(boolean value);

  public void setCatalogTitle(String value);

  public void setMaxBeforePaginate(int value);

  public void setBooksInRecentAdditions(int value);

  public void setWikipediaLanguage(String value);

  public void setIncludedFormatsList(String value);

  public void setMinimizeChangedFiles(boolean value);

  public void setBookDetailsCustomFields(String value);

  public void setBookDetailsCustomFieldsAlways(boolean b);

  public void setIncludeTagCrossReferences(boolean value);

  public void setThumbnailGenerate(boolean value);

  public void setThumbnailHeight(int value);

  public void setGenerateOpds(boolean value);

  public void setGenerateHtml(boolean value);

  public void setGenerateOpdsDownloads(boolean value);

  public void setGenerateHtmlDownloads(boolean value);

  public void setSuppressRatingsInTitles(boolean value);

  public void setGenerateDownloads(boolean value);

  public void setMaxBeforeSplit(int value);

  public void setMaxSplitLevels(int value);

  public void setSplitTagsOn(String value);

  public void setIncludeBooksWithNoFile(boolean value);

  public void setCryptFilenames(boolean value);

  public void setShowSeriesInAuthorCatalog(boolean value);

  public void setGenerateCrossLinks(boolean value);

  public void setGenerateExternalLinks(boolean value);

  public void setCatalogFilter(String value);

  public void setMaxSummaryLength(int value);

  public void setGenerateAuthors(boolean value);

  public void setGenerateTags(boolean value);

  public void setGenerateSeries(boolean value);

  public void setGenerateRecent(boolean value);

  public void setGenerateRatings(boolean value);

  public void setGenerateAllbooks(boolean value);

  public void setLanguageAsTag(boolean value);

  public void setTagsToIgnore(String value);

  public void setIncludeSeriesInBookDetails(boolean value);

  public void setIncludeRatingInBookDetails(boolean value);

  public void setIncludeTagsInBookDetails(boolean value);

  public void setIncludePublisherInBookDetails(boolean value);

  public void setIncludePublishedInBookDetails(boolean value);

  public void setIncludeAddedInBookDetails(boolean value);

  public void setIncludeModifiedInBookDetails(boolean value);

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

  public void setZipTrookCatalog(boolean value);

  public void setReprocessEpubMetadata(boolean value);

  public void setMaxMobileResolution(int value);

  public void setUrlBooks(String value);

  public void setIncludeCoversInCatalog(boolean value);

  public void setUseThumbnailsAsCovers(boolean value);

  public void setZipCatalog(boolean value);

  public void setZipOmitXml(boolean value);

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

  public void setFeaturedCatalogTitle(String value);

  public void setFeaturedCatalogSavedSearchName(String value);

  public void setCustomCatalogs(List<Composite<String, String>> value);

  public void setCatalogCustomColumns(String value);

  public void setSecurityCode (String code);
}
