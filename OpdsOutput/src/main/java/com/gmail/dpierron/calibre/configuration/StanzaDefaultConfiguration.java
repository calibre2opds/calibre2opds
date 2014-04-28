package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.Index;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class StanzaDefaultConfiguration extends DefaultConfiguration implements GetConfigurationInterface {

  public String getLanguage() {
    return Locale.getDefault().getLanguage();
  }

  public String getCatalogFolderName() {
    return StanzaConstants.CATALOGFOLDER;
  }

  public Boolean getOnlyCatalogAtTarget() { return StanzaConstants.ONLY_CATALOG_A_TTARGET; }

  public String getCatalogTitle() {
    if (Localization.Main.isInitialized())
      return Localization.Main.getText("main.title");
    else
      return StanzaConstants.CATALOGTITLE;
  }

  public Integer getMaxBeforePaginate() {
    return StanzaConstants.MAX_BEFORE_PAGINATE;
  }

  public Integer getBooksInRecentAdditions() {
    return StanzaConstants.MAX_RECENT_ADDITIONS;
  }

  public String getWikipediaLanguage() {
    return StanzaConstants.WIKIPEDIA_LANGUAGE;
  }

  public String getIncludedFormatsList() {
    return StanzaConstants.INCLUDEDFORMATS;
  }

  public Boolean getMinimizeChangedFiles() {
    return StanzaConstants.MINIMIZE_CHANGED_FILES;
  }

  public Boolean getExternalIcons() {
    return StanzaConstants.USE_EXTERNAL_ICONS;
  }

  public Boolean getExternalImages() {
    return StanzaConstants.USE_EXTERNAL_IMAGES;
  }

  public Boolean getCoverResize() {
    return StanzaConstants.COVER_RESIZE;
  }

  public Boolean getThumbnailGenerate() {
    return StanzaConstants.THUMBNAIL_GENERATE;
  }

  public Integer getThumbnailHeight() {
    return StanzaConstants.THUMBNAIL_HEIGHT;
  }


  public Boolean getGenerateOpds() {
    return StanzaConstants.GENERATE_OPDS;
  }

  public Boolean getGenerateHtml() {
    return StanzaConstants.GENERATE_HTML;
  }

  public Boolean getGenerateOpdsDownloads() {
    return StanzaConstants.GENERATE_OPDS_DOWNLOADS;
  }

  public Boolean getGenerateHtmlDownloads() {
    return StanzaConstants.GENERATE_HTML_DOWNLOADS;
  }

  public Boolean getSuppressRatingsInTitles() {
    return StanzaConstants.suppressRatingsInTitles;
  }

  public Integer getMaxBeforeSplit() {  return StanzaConstants.MAX_BEFORE_SPLIT;  }

  public Integer getMaxSplitLevels() {
    return StanzaConstants.MAX_SPLIT_LEVELS;
  }

  public String getSplitTagsOn() {
    return StanzaConstants.SPLIT_TAGS_ON;
  }

  public Boolean getDontSplitTagsOn() {
    return StanzaConstants.DontSplitTagsOn;
  }

  public Boolean getIncludeBooksWithNoFile() {
    return StanzaConstants.INCLUDEBOOKSWITHNOFILE;
  }

  public Boolean getCryptFilenames() {
    return StanzaConstants.CRYPT_FILENAMES;
  }

  public Boolean getShowSeriesInAuthorCatalog() {
    return StanzaConstants.SHOWSERIESINAUTHORCATALOG;
  }

  public Boolean getGenerateCrossLinks() {
    return StanzaConstants.generateCrossLinks;
  }

  public Boolean getGenerateExternalLinks() {
    return StanzaConstants.generateExternalLinks;
  }

  public String getCatalogFilter() {
    return StanzaConstants.CatalogFilter;
  }

  public Integer getMaxSummaryLength() {
    return StanzaConstants.MAX_SUMMARY_LENGTH;
  }

  public Integer getMaxBookSummaryLength() {
    return StanzaConstants.MAX_BOOK_SUMMARY_LENGTH;
  }

  public Boolean getGenerateAuthors() {
    return StanzaConstants.GENERATE_AUTHORS;
  }

  public Boolean getGenerateTags() {
    return StanzaConstants.GENERATE_TAGS;
  }

  public String getTagsToIgnore() {
    return StanzaConstants.TAGS_TO_IGNOREp;
  }

  public Boolean getGenerateSeries() {
    return StanzaConstants.GENERATE_SERIES;
  }

  public Boolean getGenerateRecent() {
    return StanzaConstants.GENERATE_RECENT;
  }

  public Boolean getGenerateRatings() {
    return StanzaConstants.GENERATE_RATINGS;
  }

  public Boolean getGenerateAllbooks() {
    return StanzaConstants.GENERATE_ALLBOOKS;
  }

  public File getTargetFolder() {
    return new File(StanzaConstants.targetFolder);
  }

  public DeviceMode getDeviceMode() {
    return DeviceMode.Default;
  }

  public Boolean getCopyToDatabaseFolder() {
    return StanzaConstants.COPYTODATABASEFOLDER;
  }

  public Boolean getBrowseByCover() {
    return StanzaConstants.NTOWSE_BY_COVER;
  }

  public Boolean getLanguageAsTag() {
    return StanzaConstants.languageAsTag;
  }

  public Boolean getSplitByAuthorInitialGoToBooks() {
    return StanzaConstants.splitByAuthorInitialGoToBooks;
  }

  public Boolean getIncludeAboutLink() {
    return StanzaConstants.INCLUDE_ABOUT_LINK;
  }

  public Boolean getPublishedDateAsYear() {
    return StanzaConstants.publishedDateAsYear;
  }

  public String getTagsToMakeDeep() {
    return StanzaConstants.TAGS_TO_MAKE_DEEP;
  }

  public Boolean getBrowseByCoverWithoutSplit() {
    return StanzaConstants.browseByCoverWithoutSplit;
  }

  public Integer getMinBooksToMakeDeepLevel() {
    return StanzaConstants.minBooksToMakeDeepLevel;
  }

  public Integer getCoverHeight() {
    return StanzaConstants.CoverHeight;
  }

  public Integer getMaxMobileResolution() {
    return StanzaConstants.MAX_MOBILE_RESOLUTION;
  }

  public Boolean getIncludeOnlyOneFile() {
    return StanzaConstants.IncludeOnlyOneFile;
  }

  public Boolean getZipTrookCatalog() {
    return StanzaConstants.ZipTrookCatalog;
  }

  public Boolean getReprocessEpubMetadata() {
    return StanzaConstants.ReprocessEpubMetadata;
  }

  public Boolean getOrderAllBooksBySeries() {
    return StanzaConstants.OrderAllBooksBySeries;
  }

  public Boolean getIncludeCoversInCatalog() { return StanzaConstants.IncludeCoversInCatalog; }

  public Boolean getUseThumbnailsAsCovers() {
    return StanzaConstants.UseThumbnailsAsCovers;
  }

  public Boolean getZipCatalog() {
    return StanzaConstants.ZipCatalog;
  }

  public Boolean getZipOmitXml() {
    return StanzaConstants.ZipOmitXml;
  }

  public String getAmazonAuthorUrl() {
    return Localization.Main.getText(StanzaConstants.AMAZON_AUTHORS_URL_DEFAULT);
  }

  public String getAmazonIsbnUrl() {
    return Localization.Main.getText(StanzaConstants.AMAZON_ISBN_URL_DEFAULT);
  }

  public String getAmazonTitleUrl() {
    return Localization.Main.getText(StanzaConstants.AMAZON_TITLE_URL_DEFAULT);
  }

  public String getGoodreadAuthorUrl() {
    return Localization.Main.getText(StanzaConstants.GOODREADS_AUTHOR_URL_DEFAULT);
  }

  public String getGoodreadIsbnUrl() {
    return Localization.Main.getText(StanzaConstants.GOODREADS_ISBN_URL_DEFAULT);
  }

  public String getGoodreadTitleUrl() {
    return Localization.Main.getText(StanzaConstants.GOODREADS_TITLE_URL_DEFAULT);
  }

  public String getGoodreadReviewIsbnUrl() {
    return Localization.Main.getText(StanzaConstants.GOODREADS_REVIEW_URL_DEFAULT);
  }

  public String getIsfdbAuthorUrl() {
    return Localization.Main.getText(StanzaConstants.ISFDB_AUTHOR_URL_DEFAULT);
  }

  public String getLibrarythingAuthorUrl() {
    return Localization.Main.getText(StanzaConstants.LIBRARYTHING_AUTHOR_URL_DEFAULT);
  }

  public String getLibrarythingIsbnUrl() {
    return Localization.Main.getText(StanzaConstants.LIBRARYTHING_ISBN_URL_DEFAULT);
  }

  public String getLibrarythingTitleUrl() {
    return Localization.Main.getText(StanzaConstants.LIBRARYTHING_TITLE_URL_DEFAULT);
  }

  public String getWikipediaUrl() {
    return Localization.Main.getText(StanzaConstants.WIKIPEDIA_URL_DEFAULT);
  }

  public Boolean getGenerateIndex() {
    return StanzaConstants.GenerateIndex;
  }

  public Boolean getIndexComments() {
    return StanzaConstants.IndexComments;
  }

  public Integer getMaxKeywords() {
    return StanzaConstants.MaxKeywords;
  }

  public Index.FilterHintType getIndexFilterAlgorithm() {
    return StanzaConstants.IndexFilterAlgorithm;
  }

  public String getUrlBooks() {
    return StanzaConstants.UrlBooks;
  }

  public String getFeaturedCatalogTitle() {
    return StanzaConstants.FeaturedCatalogTitle;
  }

  public String getFeaturedCatalogSavedSearchName() {
    return StanzaConstants.FeaturedCatalogSavedSearchName;
  }

  public List<CustomCatalogEntry> getCustomCatalogs() {return new Vector<CustomCatalogEntry>();  }

  public String getCatalogCustomColumns() {
    return StanzaConstants.CatalogCustomColumns;
  }

  /* Catalog Structure */

  public Boolean getDisplayAuthorSort() {
    return StanzaConstants.DisplayAuthorSortInBookDetails;
  }

  public Boolean getDisplayTitleSort() {
    return StanzaConstants.DisplayTitleSort;
  }

  public Boolean getTagBooksNoSplit() {  return StanzaConstants.TagBooksNoSplit;  }

  public Boolean getSortUsingAuthor() {
    return StanzaConstants.SortUsingAuthor;
  }

  public Boolean getSortUsingTitle() {
    return StanzaConstants.SortUsingTitle;
  }

  public Boolean getSortTagsByAuthor() {return StanzaConstants.SortTagsByAuthor; }

  /* Book Details */

  public Boolean getIncludeSeriesInBookDetails() {
    return StanzaConstants.includeSeriesInBookDetails;
  }

  public Boolean getIncludeRatingInBookDetails() {
    return StanzaConstants.includeRatingInBookDetails;
  }

  public Boolean getIncludeTagsInBookDetails() {
    return StanzaConstants.includeTagsInBookDetails;
  }

  public Boolean getIncludePublisherInBookDetails() {
    return StanzaConstants.includePublisherInBookDetails;
  }

  public Boolean getIncludePublishedInBookDetails() {
    return StanzaConstants.includePublishedInBookDetails;
  }

  public Boolean getIncludeAddedInBookDetails() {
    return StanzaConstants.IncludeAddedInBookDetails;
  }

  public Boolean getIncludeModifiedInBookDetails() {
    return StanzaConstants.IncludeModifiedInBookDetails;
  }

  public String getSecurityCode() {
    return null;
  }

  public String getBookDetailsCustomFields() {
    return StanzaConstants.BOOK_DETAILS_CUSTOM_FIELDS;
  }

  public Boolean getBookDetailsCustomFieldsAlways() {
    return StanzaConstants.BOOK_DETAILS_CUSTOM_FIELDS_ALWAYS;
  }

  public Boolean getIncludeTagCrossReferences () {
    return StanzaConstants.INCLUDE_TAG_CROSS_REFERENCES;
  }
}
