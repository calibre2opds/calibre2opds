package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.Index;

import java.io.File;
import java.util.Locale;




public class StanzaDefaultConfiguration extends DefaultConfiguration implements ReadOnlyStanzaConfigurationInterface {

  public String getLanguage() {
    return Locale.getDefault().getLanguage();
  }
  
  public String getCatalogFolderName() {
    return StanzaConstants.CATALOGFOLDER;
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
    return StanzaConstants.MINIMIZECHANGEDFILES;
  }

  public boolean getExternalIcons() {
    return StanzaConstants.EXTERNALICONS;
  }

  public boolean getSaveBandwidth() {
    return StanzaConstants.SAVEBANDWIDTH;
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
    return StanzaConstants.GENERATEOPDS;
  }

  public boolean getGenerateHtml() {
    return StanzaConstants.GENERATEHTML;
  }

  public boolean getGenerateOpdsDownloads() {
    return StanzaConstants.GENERATEOPDSDOWNLOADS;
  }

  public boolean getGenerateHtmlDownloads() {
    return StanzaConstants.GENERATEHTMLDOWNLOADS;
  }

  public boolean getSuppressRatingsInTitles() {
      return StanzaConstants.suppressRatingsInTitles;
  }

  public boolean getGenerateDownloads() {
    return StanzaConstants.GENERATEDOWNLOADS;
  }

  public int getMaxBeforeSplit() {
    return StanzaConstants.MAX_BEFORE_SPLIT;
  }

  public String getSplitTagsOn() {
    return StanzaConstants.SPLITTAGSON;
  }

  public boolean getIncludeBooksWithNoFile() {
	  return StanzaConstants.INCLUDEBOOKSWITHNOFILE;
  }

  public String getBookLanguageTag() {
    return StanzaConstants.DEFAULTBOOKLANGUAGETAG;
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

  public String getTagsToGenerate() {
    return StanzaConstants.tagsToGenerate;
  }

  public String getTagsToExclude() {
    return StanzaConstants.tagsToExclude;
  }

  public int getMaxSummaryLength() {
    return StanzaConstants.MAX_SUMMARY_LENGTH;
  }

  public boolean getGenerateTags() {
    return StanzaConstants.generateTags;
  }

  public boolean getGenerateRatings() {
    return StanzaConstants.generateRatings;
  }

  public boolean getGenerateAllbooks() {
    return StanzaConstants.generateAllbooks;
  }

  public File getTargetFolder() {
    return new File(StanzaConstants.targetFolder);
  }

  public DeviceMode getDeviceMode() {
    return DeviceMode.Dropbox;
  }

  public boolean getCopyToDatabaseFolder() {
    return StanzaConstants.COPYTODATABASEFOLDER;
  }

  public boolean getBrowseByCover() {
    return StanzaConstants.browseByCover;
  }

  public boolean getSplitByAuthorInitialGoToBooks() {
    return StanzaConstants.splitByAuthorInitialGoToBooks;
  }

  public boolean getIncludeAboutLink() {
    return StanzaConstants.includeAboutLink;
  }

  public boolean getPublishedDateAsYear() {
    return StanzaConstants.publishedDateAsYear;
  }

  public String getTagsToMakeDeep() {
    return StanzaConstants.tagsToMakeDeep;
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
  
  public CompatibilityTrick getCompatibilityTrick() {
    return StanzaConstants.COMPATIBILITYTRICK;
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

  public boolean getSplitInAuthorBooks() {
    return StanzaConstants.splitInAuthorBooks;
  }

  public boolean getSplitInSeriesBooks() {
    return StanzaConstants.splitInSeriesBooks;
  }

  public String getAmazonAuthorUrl() {
    return StanzaConstants.AMAZON_AUTHOR_URL;
  }

  public String getWikipediaUrl() {
    return StanzaConstants.WIKIPEDIA_URL;
  }

  public String getAmazonIsbnUrl() {
    return StanzaConstants.AMAZON_ISBN_URL;
  }

  public String getAmazonTitleUrl() {
    return StanzaConstants.AMAZON_TITLE_URL;
  }

  public String getGoodreadAuthorUrl() {
    return StanzaConstants.GOODREAD_AUTHOR_URL;
  }

  public String getGoodreadIsbnUrl() {
    return StanzaConstants.GOODREAD_ISBN_URL;
  }

  public String getGoodreadTitleUrl() {
    return StanzaConstants.GOODREAD_TITLE_URL;
  }

  public String getGoodreadReviewIsbnUrl() {
    return StanzaConstants.GOODREAD_REVIEW_ISBN_URL;
  }

  public String getIsfdbAuthorUrl() {
    return StanzaConstants.ISFDB_AUTHOR_URL;
  }

  public String getLibrarythingAuthorUrl() {
    return StanzaConstants.LIBRARYTHING_AUTHOR_URL;
  }

  public String getLibrarythingIsbnUrl() {
    return StanzaConstants.LIBRARYTHING_ISBN_URL;
  }

  public String getLibrarythingTitleUrl() {
    return StanzaConstants.LIBRARYTHING_TITLE_URL;
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

}
