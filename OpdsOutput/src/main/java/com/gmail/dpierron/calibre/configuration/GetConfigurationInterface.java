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

public interface GetConfigurationInterface extends ReadOnlyConfigurationInterface {

  public DeviceMode getDeviceMode();

  public String getLanguage();

  public String getCatalogFolderName();

  public String getCatalogTitle();

  public File getTargetFolder();

  public Boolean getOnlyCatalogAtTarget();

  public Boolean getCopyToDatabaseFolder();

  public Integer getMaxBeforePaginate();

  public Integer getMaxSplitLevels();

  public Integer getBooksInRecentAdditions();

  public String getWikipediaLanguage();

  public String getIncludedFormatsList();

  public Boolean getThumbnailGenerate();

  public Integer getThumbnailHeight();

  public Boolean getSuppressRatingsInTitles();

  public String getBookDetailsCustomFields();

  public Boolean getSingleBookCrossReferences();

  public Boolean getIncludeAuthorCrossReferences();

  public Boolean getIncludeSerieCrossReferences();

  public Boolean getIncludeTagCrossReferences();

  public Boolean getIncludeRatingCrossReferences();

  public Integer getMaxBeforeSplit();

  public String getSplitTagsOn();

  public Boolean getDontSplitTagsOn();

  public Boolean getIncludeBooksWithNoFile();

  public Boolean getCryptFilenames();

  public Boolean getShowSeriesInAuthorCatalog();

  public Boolean getIncludeAboutLink();

  public String getCatalogFilter();

  public Integer getMaxSummaryLength();

  public Integer getMaxBookSummaryLength();

  public Boolean getGenerateAuthors();

  public Boolean getGenerateTags();

  public Boolean getGenerateSeries();

  public Boolean getGenerateRecent();

  public Boolean getGenerateRatings();

  public Boolean getGenerateAllbooks();

  public Boolean getGenerateIndex();

  public Boolean getGenerateOpds();

  public Boolean getGenerateHtml();

  public Boolean getGenerateOpdsDownloads();

  public Boolean getGenerateHtmlDownloads();

  public Boolean getBrowseByCover();

  public Boolean getBrowseByCoverWithoutSplit();

  public Boolean getMinimizeChangedFiles();

  public Boolean getSplitByAuthorInitialGoToBooks();

  public Boolean getLanguageAsTag();

  public String getTagsToMakeDeep();

  public Integer getMinBooksToMakeDeepLevel();

  public Boolean getCoverResize();

  public Integer getCoverHeight();

  public Boolean getIncludeOnlyOneFile();

  public Boolean getZipTrookCatalog();

  public Boolean getReprocessEpubMetadata();

  public Integer getMaxMobileResolution();

  public String getUrlBooks();

  /* Catalog Structure */

  public Boolean getDisplayAuthorSort();

  public Boolean getDisplayTitleSort();

  public Boolean getSortUsingAuthor();

  public Boolean getSortUsingTitle();

  public String getTagsToIgnore();

  public Boolean getSortTagsByAuthor();

  public Boolean getTagBooksNoSplit();

  /* Book Details */

  public Boolean getGenerateCrossLinks();

  public Boolean getGenerateExternalLinks();

  public Boolean getIncludeSeriesInBookDetails();

  public Boolean getIncludeRatingInBookDetails();

  public Boolean getIncludeTagsInBookDetails();

  public Boolean getIncludePublisherInBookDetails();

  public Boolean getIncludePublishedInBookDetails();

  public Boolean getPublishedDateAsYear();

  public Boolean getIncludeAddedInBookDetails();

  public Boolean getIncludeModifiedInBookDetails();

  /*  Advanced */

  public Boolean getIncludeCoversInCatalog();

  public Boolean getUseThumbnailsAsCovers();

  public Boolean getZipCatalog();

  public Boolean getZipOmitXml();

  public Boolean getExternalImages();

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

  public Boolean getIndexComments();

  public Integer getMaxKeywords();

  public Index.FilterHintType getIndexFilterAlgorithm();

  public String getFeaturedCatalogTitle();

  public String getFeaturedCatalogSavedSearchName();

  public List<CustomCatalogEntry> getCustomCatalogs();

  public String getCatalogCustomColumns();

  public String getSecurityCode();
}
