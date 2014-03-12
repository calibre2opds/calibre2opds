package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Composite;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class StanzaDefaultConfiguration extends DefaultConfiguration implements ReadOnlyStanzaConfigurationInterface {

  public String getLanguage() {
    return Locale.getDefault().getLanguage();
  }

  public String getCatalogFolderName() {
    return StanzaConstants.CATALOGFOLDER;
  }

  public boolean getOnlyCatalogAtTarget() {
    return StanzaConstants.ONLY_CATALOG_A_TTARGET;
  }

  public String getCatalogTitle() {
    if (Localization.Main.isInitialized())
      return Localization.Main.getText("main.title");
    else
      return StanzaConstants.CATALOGTITLE;
  }

  public int getMaxBeforePaginate() {
    return StanzaConstants.MAX_BEFORE_PAGINATE;
  }

  public int getBooksInRecentAdditions() {
    return StanzaConstants.MAX_RECENT_ADDITIONS;
  }

  public String getWikipediaLanguage() {
    return StanzaConstants.WIKIPEDIA_LANGUAGE;
  }

  public String getIncludedFormatsList() {
    return StanzaConstants.INCLUDEDFORMATS;
  }

  public boolean getMinimizeChangedFiles() {
    return StanzaConstants.MINIMIZE_CHANGED_FILES;
  }

  public boolean getExternalIcons() {
    return StanzaConstants.USE_EXTERNAL_ICONS;
  }

  public boolean getExternalImages() {
    return StanzaConstants.USE_EXTERNAL_IMAGES;
  }

  public boolean getCoverResize() {
    return StanzaConstants.COVER_RESIZE;
  }

  public boolean getThumbnailGenerate() {
    return StanzaConstants.THUMBNAIL_GENERATE;
  }

  public int getThumbnailHeight() {
    return StanzaConstants.THUMBNAIL_HEIGHT;
  }


  public boolean getGenerateOpds() {
    return StanzaConstants.GENERATE_OPDS;
  }

  public boolean getGenerateHtml() {
    return StanzaConstants.GENERATE_HTML;
  }

  public boolean getGenerateOpdsDownloads() {
    return StanzaConstants.GENERATE_OPDS_DOWNLOADS;
  }

  public boolean getGenerateHtmlDownloads() {
    return StanzaConstants.GENERATE_HTML_DOWNLOADS;
  }

  public boolean getSuppressRatingsInTitles() {
    return StanzaConstants.suppressRatingsInTitles;
  }

  public boolean getGenerateDownloads() {
    return StanzaConstants.GENERATE_DOWNLOADS;
  }

  public int getMaxBeforeSplit() {
    return StanzaConstants.MAX_BEFORE_SPLIT;
  }

  public int getMaxSplitLevels() {
    return StanzaConstants.MAX_SPLIT_LEVELS;
  }

  public String getSplitTagsOn() {
    return StanzaConstants.SPLIT_TAGS_ON;
  }

  public boolean getIncludeBooksWithNoFile() {
    return StanzaConstants.INCLUDEBOOKSWITHNOFILE;
  }

  public boolean getCryptFilenames() {
    return StanzaConstants.CRYPT_FILENAMES;
  }

  public boolean getShowSeriesInAuthorCatalog() {
    return StanzaConstants.SHOWSERIESINAUTHORCATALOG;
  }

  public boolean getGenerateCrossLinks() {
    return StanzaConstants.generateCrossLinks;
  }

  public boolean getGenerateExternalLinks() {
    return StanzaConstants.generateExternalLinks;
  }

  public String getCatalogFilter() {
    return StanzaConstants.CatalogFilter;
  }

  public int getMaxSummaryLength() {
    return StanzaConstants.MAX_SUMMARY_LENGTH;
  }

  public int getMaxBookSummaryLength() {
    return StanzaConstants.MAX_BOOK_SUMMARY_LENGTH;
  }

  public boolean getGenerateAuthors() {
    return StanzaConstants.GENERATE_AUTHORS;
  }

  public boolean getGenerateTags() {
    return StanzaConstants.GENERATE_TAGS;
  }

  public String getTagsToIgnore() {
    return StanzaConstants.TAGS_TO_IGNOREp;
  }

  public boolean getGenerateSeries() {
    return StanzaConstants.GENERATE_SERIES;
  }

  public boolean getGenerateRecent() {
    return StanzaConstants.GENERATE_RECENT;
  }

  public boolean getGenerateRatings() {
    return StanzaConstants.GENERATE_RATINGS;
  }

  public boolean getGenerateAllbooks() {
    return StanzaConstants.GENERATE_ALLBOOKS;
  }

  public File getTargetFolder() {
    return new File(StanzaConstants.targetFolder);
  }

  public DeviceMode getDeviceMode() {
    return DeviceMode.Default;
  }

  public boolean getCopyToDatabaseFolder() {
    return StanzaConstants.COPYTODATABASEFOLDER;
  }

  public boolean getBrowseByCover() {
    return StanzaConstants.NTOWSE_BY_COVER;
  }

  public boolean getLanguageAsTag() {
    return StanzaConstants.languageAsTag;
  }

  public boolean getSplitByAuthorInitialGoToBooks() {
    return StanzaConstants.splitByAuthorInitialGoToBooks;
  }

  public boolean getIncludeAboutLink() {
    return StanzaConstants.INCLUDE_ABOUT_LINK;
  }

  public boolean getPublishedDateAsYear() {
    return StanzaConstants.publishedDateAsYear;
  }

  public String getTagsToMakeDeep() {
    return StanzaConstants.TAGS_TO_MAKE_DEEP;
  }

  public boolean getBrowseByCoverWithoutSplit() {
    return StanzaConstants.browseByCoverWithoutSplit;
  }

  public int getMinBooksToMakeDeepLevel() {
    return StanzaConstants.minBooksToMakeDeepLevel;
  }

  public int getCoverHeight() {
    return StanzaConstants.CoverHeight;
  }

  public int getMaxMobileResolution() {
    return StanzaConstants.MAX_MOBILE_RESOLUTION;
  }

  public boolean getIncludeOnlyOneFile() {
    return StanzaConstants.IncludeOnlyOneFile;
  }

  public boolean getZipTrookCatalog() {
    return StanzaConstants.ZipTrookCatalog;
  }

  public boolean getReprocessEpubMetadata() {
    return StanzaConstants.ReprocessEpubMetadata;
  }

  public boolean getOrderAllBooksBySeries() {
    return StanzaConstants.OrderAllBooksBySeries;
  }

  public boolean getIncludeCoversInCatalog() {


    return StanzaConstants.IncludeCoversInCatalog;
  }

  public boolean getUseThumbnailsAsCovers() {
    return StanzaConstants.UseThumbnailsAsCovers;
  }
  public boolean getZipCatalog() {
    return StanzaConstants.ZipCatalog;
  }

  public boolean getZipOmitXml() {
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

  public boolean getGenerateIndex() {
    return StanzaConstants.GenerateIndex;
  }

  public boolean getIndexComments() {
    return StanzaConstants.IndexComments;
  }

  public int getMaxKeywords() {
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

  public List<Composite<String, String>> getCustomCatalogs() {
    return new Vector<Composite<String, String>>();
  }

  public String getCatalogCustomColumns() {
    return StanzaConstants.CatalogCustomColumns;
  }

  /* Catalog Structure */

  public boolean getDisplayAuthorSortInAuthorLists() {
    return StanzaConstants.DisplayAuthorSortInBookDetails;
  }

  public boolean getDisplayTitleSortInBookLists() {
    return StanzaConstants.DisplayTitleSortInBookLists;
  }

  public boolean getSortUsingAuthor() {
    return StanzaConstants.SortUsingAuthor;
  }

  public boolean getSortUsingTitle() {
    return StanzaConstants.SortUsingTitle;
  }

  /* Book Details */

  public boolean getIncludeSeriesInBookDetails() {
    return StanzaConstants.includeSeriesInBookDetails;
  }

  public boolean getIncludeRatingInBookDetails() {
    return StanzaConstants.includeRatingInBookDetails;
  }

  public boolean getIncludeTagsInBookDetails() {
    return StanzaConstants.includeTagsInBookDetails;
  }

  public boolean getIncludePublisherInBookDetails() {
    return StanzaConstants.includePublisherInBookDetails;
  }

  public boolean getIncludePublishedInBookDetails() {
    return StanzaConstants.includePublishedInBookDetails;
  }

  public boolean getIncludeAddedInBookDetails() {
    return StanzaConstants.IncludeAddedInBookDetails;
  }

  public boolean getIncludeModifiedInBookDetails() {
    return StanzaConstants.IncludeModifiedInBookDetails;
  }

  public boolean getDisplayAuthorSortInBookDetails() {
    return StanzaConstants.DisplayAuthorSortInBookDetails;
  }

  public boolean getDisplayTitleSortInBookDetails() {
    return StanzaConstants.DisplayTitleSortInBookDetails;
  }

  public String getSecurityCode() {
    return null;
  }

  public String getBookDetailsCustomFields() {
    return StanzaConstants.BOOK_DETAILS_CUSTOM_FIELDS;
  }

  public boolean getBookDetailsCustomFieldsAlways() {
    return StanzaConstants.BOOK_DETAILS_CUSTOM_FIELDS_ALWAYS;
  }

  public boolean getIncludeTagCrossReferences () {
    return StanzaConstants.INCLUDE_TAG_CROSS_REFERENCES;
  }
}
