package com.gmail.dpierron.calibre.configuration;

/**
 * Define all the 'get' methods that are supported for accessing
 * configuration settings. Note that despite the interface name
 * it applies in all modes (the name is due to legacy)
 *
 * ITIMPI:  Main purpose of defining this interface seems to be
 *          to specify what variables are to be dumped via the
 *          ??? function.
 */

import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Composite;

import java.io.File;
import java.util.List;

public interface ReadOnlyStanzaConfigurationInterface extends ReadOnlyConfigurationInterface {

  public DeviceMode getDeviceMode();

  public String getLanguage();

  public String getCatalogFolderName();

  public String getCatalogTitle();

  public File getTargetFolder();

  public boolean getCopyToDatabaseFolder();

  public int getMaxBeforePaginate();

  public int getMaxSplitLevels();

  public int getBooksInRecentAdditions();

  public String getWikipediaLanguage();

  public String getIncludedFormatsList();

  public boolean getSaveBandwidth();

  public boolean getThumbnailGenerate();

  public int getThumbnailHeight();

  public boolean getSuppressRatingsInTitles();

  public int getMaxBeforeSplit();

  public String getSplitTagsOn();

  public boolean getIncludeBooksWithNoFile();

  public boolean getCryptFilenames();

  public boolean getShowSeriesInAuthorCatalog();

  public boolean getIncludeAboutLink();

  public String getCatalogFilter();

  public int getMaxSummaryLength();

  public int getMaxBookSummaryLength();

  public boolean getGenerateAuthors();

  public boolean getGenerateTags();

  public boolean getGenerateSeries();

  public boolean getGenerateRecent();

  public boolean getGenerateRatings();

  public boolean getGenerateAllbooks();

  public boolean getGenerateIndex();

  public boolean getGenerateOpds();

  public boolean getGenerateHtml();

  public boolean getGenerateOpdsDownloads();

  public boolean getGenerateHtmlDownloads();

  public boolean getBrowseByCover();

  public boolean getBrowseByCoverWithoutSplit();

  public boolean getMinimizeChangedFiles();

  public boolean getSplitByAuthorInitialGoToBooks();

  public boolean getLanguageAsTag();

  public String getTagsToMakeDeep();

  public int getMinBooksToMakeDeepLevel();

  public boolean getCoverResize();

  public int getCoverHeight();

  public boolean getIncludeOnlyOneFile();

  public boolean getZipTrookCatalog();

  public boolean getReprocessEpubMetadata();

  public int getMaxMobileResolution();

  public String getUrlBooks();

  /* Catalog Structure */

  public boolean getDisplayAuthorSortInAuthorLists();

  public boolean getDisplayTitleSortInBookLists();

  public boolean getSortUsingAuthor();

  public boolean getSortUsingTitle();

  public String getTagsToIgnore();

  /* Book Details */

  public boolean getGenerateCrossLinks();

  public boolean getGenerateExternalLinks();

  public boolean getIncludeSeriesInBookDetails();

  public boolean getIncludeRatingInBookDetails();

  public boolean getIncludeTagsInBookDetails();

  public boolean getIncludePublisherInBookDetails();

  public boolean getDisplayAuthorSortInBookDetails();

  public boolean getDisplayTitleSortInBookDetails();

  public boolean getIncludePublishedInBookDetails();

  public boolean getPublishedDateAsYear();

  public boolean getIncludeAddedInBookDetails();

  public boolean getIncludeModifiedInBookDetails();

  /*  Advanced */

  public boolean getIncludeCoversInCatalog();


  /* external links */

  public String getWikipediaUrl();

  public String getAmazonAuthorUrl();

  public String getAmazonIsbnUrl();

  public String getAmazonTitleUrl();

  public String getGoodreadAuthorUrl();

  public String getGoodreadIsbnUrl();

  public String getGoodreadTitleUrl();

  public String getGoodreadReviewIsbnUrl();

  public String getIsfdbAuthorUrl();

  public String getLibrarythingAuthorUrl();

  public String getLibrarythingIsbnUrl();

  public String getLibrarythingTitleUrl();

  public boolean getIndexComments();

  public int getMaxKeywords();

  public Index.FilterHintType getIndexFilterAlgorithm();

  public String getFeaturedCatalogTitle();

  public String getFeaturedCatalogSavedSearchName();

  public List<Composite<String, String>> getCustomCatalogs();

  public String getSecurityCode();
}
