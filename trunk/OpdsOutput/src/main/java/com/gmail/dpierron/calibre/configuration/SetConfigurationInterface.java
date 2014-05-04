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
import java.util.List;

public interface SetConfigurationInterface extends GetConfigurationInterface {

  public void setLanguage(String value);

  public void setCatalogFolderName(String value);

  public void setOnlyCatalogAtTarget(Boolean value);

  public void setCatalogTitle(String value);

  public void setMaxBeforePaginate(Integer value);

  public void setBooksInRecentAdditions(Integer value);

  public void setWikipediaLanguage(String value);

  public void setIncludedFormatsList(String value);

  public void setMinimizeChangedFiles(Boolean value);

  public void setBookDetailsCustomFields(String value);

  public void setBookDetailsCustomFieldsAlways(Boolean b);

  public void setThumbnailGenerate(Boolean value);

  public void setThumbnailHeight(Integer value);

  public void setGenerateOpds(Boolean value);

  public void setGenerateHtml(Boolean value);

  public void setGenerateOpdsDownloads(Boolean value);

  public void setGenerateHtmlDownloads(Boolean value);

  public void setSuppressRatingsInTitles(Boolean value);

  public void setMaxBeforeSplit(Integer value);

  public void setMaxSplitLevels(Integer value);

  public void setSplitTagsOn(String value);

  public void setDontSplitTagsOn(Boolean value);

  public void setIncludeBooksWithNoFile(Boolean value);

  public void setCryptFilenames(Boolean value);

  public void setShowSeriesInAuthorCatalog(Boolean value);

  public void setGenerateCrossLinks(Boolean value);

  public void setGenerateExternalLinks(Boolean value);

  public void setCatalogFilter(String value);

  public void setMaxSummaryLength(Integer value);

  public void setGenerateAuthors(Boolean value);

  public void setGenerateTags(Boolean value);

  public void setGenerateSeries(Boolean value);

  public void setGenerateRecent(Boolean value);

  public void setGenerateRatings(Boolean value);

  public void setGenerateAllbooks(Boolean value);

  public void setLanguageAsTag(Boolean value);

  public void setSortTagsByAuthor(Boolean value);

  public void setTagBooksNoSplit(Boolean value);

  public void setTagsToIgnore(String value);

  public void setIncludeSeriesInBookDetails(Boolean value);

  public void setIncludeRatingInBookDetails(Boolean value);

  public void setIncludeTagsInBookDetails(Boolean value);

  public void setIncludePublisherInBookDetails(Boolean value);

  public void setIncludePublishedInBookDetails(Boolean value);

  public void setIncludeAddedInBookDetails(Boolean value);

  public void setIncludeModifiedInBookDetails(Boolean value);

  public void setSingleBookCrossReferences(Boolean value);

  public void setIncludeAuthorCrossReferences(Boolean value);

  public void setIncludeSerieCrossReferences(Boolean value);

  public void setIncludeTagCrossReferences(Boolean value);

  public void setIncludeRatingCrossReferences(Boolean value);

  public void setGenerateIndex(Boolean value);

  public void setTargetFolder(File value);

  public void setDeviceMode(DeviceMode value);

  public void setCopyToDatabaseFolder(Boolean value);

  public void setBrowseByCover(Boolean value);

  public void setSplitByAuthorInitialGoToBooks(Boolean value);

  public void setIncludeAboutLink(Boolean value);

  public void setTagsToMakeDeep(String value);

  public void setBrowseByCoverWithoutSplit(Boolean value);

  public void setMinBooksToMakeDeepLevel(Integer value);

  public void setCoverResize(Boolean value);

  public void setCoverHeight(Integer value);

  public void setIncludeOnlyOneFile(Boolean value);

  public void setZipTrookCatalog(Boolean value);

  public void setReprocessEpubMetadata(Boolean value);

  public void setMaxMobileResolution(Integer value);

  public void setUrlBooks(String value);

  public void setIncludeCoversInCatalog(Boolean value);

  public void setUseThumbnailsAsCovers(Boolean value);

  public void setZipCatalog(Boolean value);

  public void setZipOmitXml(Boolean value);

  public void setExternalImages(Boolean value);

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

  public void setCustomCatalogs(List<CustomCatalogEntry> value);

  public void setCatalogCustomColumns(String value);

  public void setSecurityCode (String code);
}
