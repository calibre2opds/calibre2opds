package com.gmail.dpierron.calibre.configuration;

/**
 * These are the default setting for the Calibre2opds configuration
 */
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.Index;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public final class DefaultConfigurationSettings extends DefaultConfiguration implements GetConfigurationInterface {

  public Integer getWindowWidth() { return 1050; }
  public Integer getWindowHeight() { return 660; }

  public String getLanguage() {
    return Locale.getDefault().getLanguage();
  }

  public String getCatalogFolderName() {
    return "_catalog";
  }

  public Boolean getOnlyCatalogAtTarget() { return false; }

  public String getCatalogTitle() {
    if (Localization.Main.isInitialized())
      return Localization.Main.getText("main.title");
    else
      return "Calibre library";
  }

  public String getFavicon() { return ""; }

  public Integer getMaxBeforePaginate() {
    return 25;
  }

  public final Integer getBooksInRecentAdditions() {
    return 500;
  }

  public String getWikipediaLanguage() {
    return "en";
  }

  public String getIncludedFormatsList() {
    return "EPUB, PDF, RTF, TXT, PRC, PDB, MOBI, LRF, LRX, FB2";
  }

  public Boolean getMinimizeChangedFiles() {
    return true;
  }

  public Boolean getExternalIcons() {
    return true;
  }

  public Boolean getExternalImages() {
    return true;
  }

  public Boolean getCoverResize() {
    return true;
  }

  public Boolean getThumbnailGenerate() {
    return true;
  }

  public Integer getThumbnailHeight() {
    return 144;
  }

  public Boolean getGenerateOpds() {
    return true;
  }

  public Boolean getGenerateHtml() {
    return true;
  }

  public Boolean getGenerateOpdsDownloads() {
    return true;
  }

  public Boolean getGenerateHtmlDownloads() {
    return true;
  }

  public Boolean getSuppressRatingsInTitles() {
    return true;
  }

  public Integer getMaxBeforeSplit() {  return 3 * getMaxBeforePaginate();  }

  public Integer getMaxSplitLevels() {
    return 1;
  }

  public String getSplitTagsOn() {
    return "";
  }

  public Boolean getDontSplitTagsOn() {
    return true;
  }

  public Boolean getIncludeBooksWithNoFile() {
    return false;
  }

  public Boolean getCryptFilenames() {
    return false;
  }

  public Boolean getShowSeriesInAuthorCatalog() {
    return true;
  }

  public Boolean getGenerateCrossLinks() {
    return true;
  }

  public Boolean getGenerateExternalLinks() {
    return true;
  }

  public String getCatalogFilter() {  return "";  }

  public Integer getMaxSummaryLength() {
    return 30;
  }

  public Integer getMaxBookSummaryLength() {
    return 250;
  }

  public Boolean getGenerateAuthors() {
    return true;
  }

  public Boolean getGenerateTags() {
    return true;
  }

  public String getTagsToIgnore() {
    return "";
  }

  public Boolean getGenerateSeries() {
    return true;
  }

  public Boolean getGenerateRecent() {
    return true;
  }

  public Boolean getGenerateRatings() {
    return true;
  }

  public Boolean getGenerateAllbooks() {
    return true;
  }

  public File getTargetFolder() {
    return new File(".");
  }

  public DeviceMode getDeviceMode() {
    return DeviceMode.Default;
  }

  public Boolean getCopyToDatabaseFolder() {
    return true;
  }

  public Boolean getBrowseByCover() {
    return false;
  }

  public Boolean getLanguageAsTag() {
    return true;
  }

  public Boolean getSplitByAuthorInitialGoToBooks() {
    return false;
  }

  public Boolean getIncludeAboutLink() {
    return true;
  }

  public Boolean getPublishedDateAsYear() {
    return false;
  }

  public String getTagsToMakeDeep() {
    return "";
  }

  public Boolean getBrowseByCoverWithoutSplit() {
    return true;
  }

  public Integer getMinBooksToMakeDeepLevel() {
    return 50;
  }

  public Integer getCoverHeight() {
    return 550;
  }

  public Integer getMaxMobileResolution() {
    return 960;
  }

  public final Boolean getIncludeOnlyOneFile() {
    return false;
  }

  public Boolean getZipTrookCatalog() {
    return false;
  }

  public Boolean getReprocessEpubMetadata() {
    return false;
  }

  public Boolean getOrderAllBooksBySeries() {
    return true;
  }

  public Boolean getIncludeCoversInCatalog() { return false; }

  public Boolean getUseThumbnailsAsCovers() {
    return false;
  }

  public Boolean getZipCatalog() {
    return false;
  }

  public Boolean getZipOmitXml() {
    return true;
  }

  public String getAmazonAuthorUrl() {
    return Localization.Main.getText("config.AmazonAuthorUrl.default") ;
  }

  public String getAmazonIsbnUrl() {
    return Localization.Main.getText("config.AmazonIsbnUrl.default");
  }

  public String getAmazonTitleUrl() {
    return Localization.Main.getText("config.AmazonTitleUrl.default");
  }

  public String getGoodreadAuthorUrl() {
    return Localization.Main.getText("config.GoodreadAuthorUrl.default");
  }

  public String getGoodreadIsbnUrl() {
    return Localization.Main.getText("config.GoodreadIsbnUrl.default");
  }

  public String getGoodreadTitleUrl() {
    return Localization.Main.getText("config.GoodreadTitleUrl.default");
  }

  public String getGoodreadReviewIsbnUrl() {
    return Localization.Main.getText("config.GoodreadReviewIsbnUrl.default");
  }

  public String getIsfdbAuthorUrl() {
    return Localization.Main.getText("config.IsfdbAuthorUrl.default");
  }

  public String getLibrarythingAuthorUrl() {
    return Localization.Main.getText("config.LibrarythingAuthorUrl.default");
  }

  public String getLibrarythingIsbnUrl() {
    return Localization.Main.getText("config.LibrarythingIsbnUrl.default");
  }

  public String getLibrarythingTitleUrl() {
    return Localization.Main.getText("config.LibrarythingTitleUrl.default");
  }

  public String getWikipediaUrl() {
    return Localization.Main.getText("config.WikipediaUrl.default");
  }

  public Boolean getGenerateIndex() {
    return false;
  }

  public Boolean getIndexComments() {
    return true;
  }

  public Integer getMaxKeywords() {
    return -1; // don't filter
  }

  public Index.FilterHintType getIndexFilterAlgorithm() {
    return Index.FilterHintType.RemoveMedian;
  }

  public String getUrlBooks() {
    return "";
  }

  public String getFeaturedCatalogTitle() {
    return "Featured books";
  }

  public String getFeaturedCatalogSavedSearchName() {
    return "";
  }

  public List<CustomCatalogEntry> getCustomCatalogs() {return new Vector<CustomCatalogEntry>();  }

  public String getCatalogCustomColumns() {
    return "";
  }

  /* Catalog Structure */

  public Boolean getDisplayAuthorSort() {
    return false;
  }

  public Boolean getDisplayTitleSort() {
    return false;
  }

  public Boolean getDisplaySeriesSort() {
    return false;
  }

  public Boolean getTagBooksNoSplit() {  return false;  }

  public Boolean getSortUsingAuthor() {
    return false;
  }

  public Boolean getSortUsingTitle() {
    return false;
  }

  public Boolean getSortUsingSeries() {
    return false;
  }

  public Boolean getSortTagsByAuthor() {return false; }

  /* Book Details */

  public Boolean getIncludeSeriesInBookDetails() {
    return true;
  }

  public Boolean getIncludeRatingInBookDetails() {
    return true;
  }

  public Boolean getIncludeTagsInBookDetails() {
    return true;
  }

  public Boolean getIncludePublisherInBookDetails() {
    return false;
  }

  public Boolean getIncludePublishedInBookDetails() {
    return false;
  }

  public Boolean getIncludeAddedInBookDetails() {
    return false;
  }

  public Boolean getIncludeModifiedInBookDetails() {
    return false;
  }

  public String getSecurityCode() {
    return null;
  }

  public String getBookDetailsCustomFields() {
    return "";
  }

  public Boolean getBookDetailsCustomFieldsAlways() {
    return false;
  }

  public Boolean getSingleBookCrossReferences () {
    return false;
  }

  public Boolean getIncludeAuthorCrossReferences () {
    return true;
  }

  public Boolean getIncludeSerieCrossReferences () {   return true;  }

  public Boolean getIncludeTagCrossReferences () {
    return false;
  }

  public Boolean getIncludeRatingCrossReferences () {
    return false;
  }

}
