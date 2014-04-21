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

  public boolean getOnlyCatalogAtTarget();

  public boolean getCopyToDatabaseFolder();

  public Integer getMaxBeforePaginate();

  public Integer getMaxSplitLevels();

  public Integer getBooksInRecentAdditions();

  public String getWikipediaLanguage();

  public String getIncludedFormatsList();

  public boolean getThumbnailGenerate();

  public Integer getThumbnailHeight();

  public boolean getSuppressRatingsInTitles();

  public String getBookDetailsCustomFields();

  public boolean getIncludeTagCrossReferences();

  public Integer getMaxBeforeSplit();

  public String getSplitTagsOn();

  public boolean getDontSplitTagsOn();

  public boolean getIncludeBooksWithNoFile();

  public boolean getCryptFilenames();

  public boolean getShowSeriesInAuthorCatalog();

  public boolean getIncludeAboutLink();

  public String getCatalogFilter();

  public Integer getMaxSummaryLength();

  public Integer getMaxBookSummaryLength();

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

  public Integer getMinBooksToMakeDeepLevel();

  public boolean getCoverResize();

  public Integer getCoverHeight();

  public boolean getIncludeOnlyOneFile();

  public boolean getZipTrookCatalog();

  public boolean getReprocessEpubMetadata();

  public Integer getMaxMobileResolution();

  public String getUrlBooks();

  /* Catalog Structure */

  public boolean getDisplayAuthorSort();

  public boolean getDisplayTitleSort();

  public boolean getSortUsingAuthor();

  public boolean getSortUsingTitle();

  public String getTagsToIgnore();

  public boolean getSortTagsByAuthor();

  public boolean getTagBooksNoSplit();

  /* Book Details */

  public boolean getGenerateCrossLinks();

  public boolean getGenerateExternalLinks();

  public boolean getIncludeSeriesInBookDetails();

  public boolean getIncludeRatingInBookDetails();

  public boolean getIncludeTagsInBookDetails();

  public boolean getIncludePublisherInBookDetails();

  public boolean getIncludePublishedInBookDetails();

  public boolean getPublishedDateAsYear();

  public boolean getIncludeAddedInBookDetails();

  public boolean getIncludeModifiedInBookDetails();

  /*  Advanced */

  public boolean getIncludeCoversInCatalog();

  public boolean getUseThumbnailsAsCovers();

  public boolean getZipCatalog();

  public boolean getZipOmitXml();

  public boolean getExternalImages();

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

  public Integer getMaxKeywords();

  public Index.FilterHintType getIndexFilterAlgorithm();

  public String getFeaturedCatalogTitle();

  public String getFeaturedCatalogSavedSearchName();

  public List<Composite<String, String>> getCustomCatalogs();

  public String getCatalogCustomColumns();

  public String getSecurityCode();
}
