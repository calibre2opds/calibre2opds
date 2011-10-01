package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ConfigurationHolder extends PropertiesBasedConfiguration implements StanzaConfigurationInterface {

  final static String PROPERTY_NAME_VERSIONCHIP = "VERSIONCHIP";
  private final static String PROPERTY_NAME_DATABASEFOLDER = "DatabaseFolder";
  private final static String PROPERTY_NAME_TARGETFOLDER = "TargetFolder";
  private final static String PROPERTY_NAME_LANGUAGE = "Language";
  private final static String PROPERTY_NAME_CATALOGFOLDERNAME = "CatalogFolderName";
  private final static String PROPERTY_NAME_CATALOGTITLE = "CatalogTitle";
  private final static String PROPERTY_NAME_MAXBEFOREPAGINATE = "MaxBeforePaginate";
  private final static String PROPERTY_NAME_MAXBEFORESPLIT = "MaxBeforeSplit";
  private final static String PROPERTY_NAME_BOOKSINRECENTADDITIONS = "BooksInRecentAdditions";
  private final static String PROPERTY_NAME_WIKIPEDIALANGUAGE = "WikipediaLanguage";
  private final static String PROPERTY_NAME_INCLUDEDFORMATSLIST = "IncludedFormatsList";
  private final static String PROPERTY_NAME_SAVEBANDWIDTH = "SaveBandwidth";
  private final static String PROPERTY_NAME_THUMBNAILGENERATE = "ThumbnailGenerate";
  private final static String PROPERTY_NAME_THUMBNAILHEIGHT = "ThumbnailHeight";
  private final static String PROPERTY_NAME_GENERATEHTML = "GenerateHtml";
  private final static String PROPERTY_NAME_GENERATEOPDS = "GenerateOpds";
  private final static String PROPERTY_NAME_GENERATEOPDSDOWNLOADS = "GenerateOpdsDownloads";
  private final static String PROPERTY_NAME_GENERATEHTMLDOWNLOADS = "GenerateHtmlDownloads";
  private final static String PROPERTY_NAME_SUPRESSRATINGSINTITLES = "SuppressRatingsInTitles";
  private final static String PROPERTY_NAME_GENERATEDOWNLOADS = "GenerateDownloads";
  private final static String PROPERTY_NAME_PUBLISHEDDATEASYEAR = "PublishDateAsYear";
  private final static String PROPERTY_NAME_SPLITTAGSON = "SplitTagsOn";
  private final static String PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE = "IncludeBooksWithNoFile";
  private final static String PROPERTY_NAME_CRYPTFILENAMES = "CryptFilenames";
  private final static String PROPERTY_NAME_MINIMIZECHANGEDFILES = "MinimizeChangedFiles";
  private final static String PROPERTY_NAME_EXTERNALICONS = "ExternalIcons";
  private final static String PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG = "ShowSeriesInAuthorCatalog";
  private final static String PROPERTY_NAME_TAGSTOGENERATE = "TagsToGenerate";
  private final static String PROPERTY_NAME_TAGSTOMAKEDEEP = "TagsToMakeDeep";
  private final static String PROPERTY_NAME_TAGSTOEXCLUDE = "TagsToExclude";
  private final static String PROPERTY_NAME_GENERATEEXTERNALLINKS = "GenerateExternalLinks";
  private final static String PROPERTY_NAME_GENERATECROSSLINKS = "GenerateCrossLinks";
  private final static String PROPERTY_NAME_MAXSUMMARYLENGTH = "MaxSummaryLength";
  private final static String PROPERTY_NAME_GENERATETAGS = "GenerateTags";
  private final static String PROPERTY_NAME_GENERATERECENT = "GenerateRecent";
  private final static String PROPERTY_NAME_GENERATERATINGS = "GenerateRatings";
  private final static String PROPERTY_NAME_GENERATEALLBOOKS = "GenerateAllbooks";
  private final static String PROPERTY_NAME_GENERATEINDEX = "GenerateIndex";
  private final static String PROPERTY_NAME_DEVICEMODE = "DeviceMode";
  private final static String PROPERTY_NAME_COPYTODATABASEFOLDER = "CopyToDatabaseFolder";
  private final static String PROPERTY_NAME_BROWSEBYCOVER = "BrowseByCover";
  private final static String PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK = "SplitByAuthorInitialGoToBooks";
  private final static String PROPERTY_NAME_INCLUDEABOUTLINK = "IncludeAboutLink";
  private final static String PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT = "BrowseByCoverWithoutSplit";
  private final static String PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL = "MinBooksToMakeDeepLevel";
  private final static String PROPERTY_NAME_COVERRESIZE = "CoverResize";
  private final static String PROPERTY_NAME_COVERHEIGHT = "CoverHeight";
  private final static String PROPERTY_NAME_INCLUDEONLYONEFILE = "IncludeOnlyOneFile";
  private final static String PROPERTY_NAME_COMPATIBILITYTRICK = "CompatibilityTrick";
  private final static String PROPERTY_NAME_ZIPTROOKCATALOG = "ZipTrookCatalog";
  private final static String PROPERTY_NAME_REPROCESSEPUBMETADATA = "ReprocessEpubMetadata";
  private final static String PROPERTY_NAME_ORDERALLBOOKSBYSERIES = "OrderAllBooksBySeries";
  private final static String PROPERTY_NAME_MAXMOBILERESOLUTION = "MaxMobileResolution";
  private final static String PROPERTY_NAME_SPLITINAUTHORBOOKS = "SplitInAuthorBooks";
  private final static String PROPERTY_NAME_SPLITINSERIESBOOKS = "SplitInSeriesBooks";
  private final static String PROPERTY_NAME_WIKIPEDIAURL = "WikipediaUrl";
  private final static String PROPERTY_NAME_AMAZONAUTHORURL = "AmazonAuthorUrl";
  private final static String PROPERTY_NAME_AMAZONISBNURL = "AmazonIsbnUrl";
  private final static String PROPERTY_NAME_AMAZONTITLEURL = "AmazonTitleUrl";
  private final static String PROPERTY_NAME_GOODREADAUTHORURL = "GoodreadAuthorUrl";
  private final static String PROPERTY_NAME_GOODREADISBNURL = "GoodreadIsbnUrl";
  private final static String PROPERTY_NAME_GOODREADTITLEURL = "GoodreadTitleUrl";
  private final static String PROPERTY_NAME_GOODREADREVIEWISBNURL = "GoodreadReviewIsbnUrl";
  private final static String PROPERTY_NAME_ISFDBAUTHORURL = "IsfdbAuthorUrl";
  private final static String PROPERTY_NAME_LIBRARYTHINGAUTHORURL = "LibrarythingAuthorUrl";
  private final static String PROPERTY_NAME_LIBRARYTHINGISBNURL = "LibrarythingIsbnUrl";
  private final static String PROPERTY_NAME_LIBRARYTHINGTITLEURL = "LibrarythingTitleUrl";
  private final static String PROPERTY_NAME_INDEXCOMMENTS = "IndexComments";
  private final static String PROPERTY_NAME_MAXKEYWORDS = "MaxKeywords";
  private final static String PROPERTY_NAME_INDEXFILTERALGORITHM = "IndexFilterAlgorithm";
  private final static String PROPERTY_NAME_URLBASE = "UrlBase";
  private final static String PROPERTY_NAME_CUSTOMCATALOGTITLE = "CustomCatalogTitle";
  private final static String PROPERTY_NAME_CUSTOMCATALOGSAVEDSEARCHNAME = "CustomCatalogSavedSearchName";

  final static Logger logger = Logger.getLogger(ConfigurationHolder.class);

  private StanzaDefaultConfiguration defaults = new StanzaDefaultConfiguration();

  ConfigurationHolder(File propertiesFile) {
    super(propertiesFile);
    try {
      load();
    } catch (IOException e) {
      reset();
    }
  }

  public void resetReadOnly() {
    Set<Object> keys = new TreeSet<Object>(properties.keySet());
    for (Object key : keys) {
      String name = (String) key;
      if (name.endsWith("_ReadOnly"))
        properties.remove(name);
    }
  }

  public void reset() {
    StanzaDefaultConfiguration defaults = new StanzaDefaultConfiguration();
    for (Method getter : ReadOnlyStanzaConfigurationInterface.class.getMethods()) {
      String getterName = getter.getName();
      String setterName = "set" + getterName.substring(3);
      try {
        Class returnType = getter.getReturnType();
        Method setter = this.getClass().getMethod(setterName, returnType);
        Object result = getter.invoke(defaults);
        setter.invoke(this, result);
      } catch (Exception e) {
        logger.warn(e);
      }
    }
    setDeviceMode(getDeviceMode());
  }

  public boolean isObsolete() {
    // check for the version chip
    String versionChip = getProperty(PROPERTY_NAME_VERSIONCHIP);
    if (versionChip == null) {
      return false;
    } else {
      if (versionChip.compareTo(Constants.CONFIGURATION_COMPATIBILITY_VERSIONCHIP) == -1) {
        return true;
      } else
        return false;
    }
  }

  @Override
  public void save() {
    // check for the version chip
    properties.setProperty(PROPERTY_NAME_VERSIONCHIP, Constants.CONFIGURATION_COMPATIBILITY_VERSIONCHIP);
    super.save();
  }

  /**
   * * SPECIFIC CONFIGURATION GETTERS AND SETTERS ARE BELOW THIS LINE ***
   */

  public DeviceMode getDeviceMode() {
    String s = getProperty(PROPERTY_NAME_DEVICEMODE);
    if (s == null)
      return defaults.getDeviceMode();
    else
      return DeviceMode.fromName(s);
  }

  public void setDeviceMode(DeviceMode mode) {
    setProperty(PROPERTY_NAME_DEVICEMODE, mode);
    if (mode != null) {
      resetReadOnly();
      mode.setSpecificOptionsReadOnly(this);
      mode.setSpecificOptionsValues(this);
    } else {
      reset();
    }
  }

  public boolean isDatabaseFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_DATABASEFOLDER);
  }

  public File getDatabaseFolder() {
    String s = getProperty(PROPERTY_NAME_DATABASEFOLDER);
    if (Helper.isNotNullOrEmpty(s))
      return new File(s);
    else
      return null;
  }

  public void setDatabaseFolder(File databaseFolder) {
    setProperty(PROPERTY_NAME_DATABASEFOLDER, getUniversalPath(databaseFolder));
  }

  private String getUniversalPath(File file) {
    if (file == null)
      return null;
    String universalPath = file.getAbsolutePath();
    if (ConfigurationManager.INSTANCE.isHacksEnabled()) {
      universalPath = file.toURI().getPath();
      int pos = universalPath.indexOf(':');
      if (pos >= 0 && pos + 1 < universalPath.length())
        universalPath = universalPath.substring(pos + 1);
    }
    return universalPath;
  }

  public boolean isTargetFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TARGETFOLDER);
  }

  public File getTargetFolder() {
    String s = getProperty(PROPERTY_NAME_TARGETFOLDER);
    if (Helper.isNotNullOrEmpty(s))
      return new File(s);
    else
      return null;
  }

  public void setTargetFolder(File targetFolder) {
    setProperty(PROPERTY_NAME_TARGETFOLDER, getUniversalPath(targetFolder));
  }

  public boolean isLanguageReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LANGUAGE);
  }

  public String getLanguage() {
    String s = getProperty(PROPERTY_NAME_LANGUAGE);
    if (Helper.isNullOrEmpty(s))
      return defaults.getLanguage();
    else
      return s;
  }

  public void setLanguage(String language) {
    setProperty(PROPERTY_NAME_LANGUAGE, language);
  }

  public boolean isWikipediaLanguageReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_WIKIPEDIALANGUAGE);
  }

  public String getWikipediaLanguage() {
    String s = getProperty(PROPERTY_NAME_WIKIPEDIALANGUAGE);
    if (s == null)
      return defaults.getWikipediaLanguage();
    else
      return s;
  }

  public void setWikipediaLanguage(String wikipediaLanguage) {
    setProperty(PROPERTY_NAME_WIKIPEDIALANGUAGE, wikipediaLanguage);
  }

  public boolean isCatalogFolderNameReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CATALOGFOLDERNAME);
  }

  public String getCatalogFolderName() {
    String s = getProperty(PROPERTY_NAME_CATALOGFOLDERNAME);
    if (s == null)
      return defaults.getCatalogFolderName();
    else
      return s;
  }

  public void setCatalogFolderName(String catalogFolderName) {
    setProperty(PROPERTY_NAME_CATALOGFOLDERNAME, catalogFolderName);
  }

  public boolean isCatalogTitleReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CATALOGTITLE);
  }

  public String getCatalogTitle() {
    String s = getProperty(PROPERTY_NAME_CATALOGTITLE);
    if (s == null)
      return defaults.getCatalogTitle();
    else
      return s;
  }

  public void setCatalogTitle(String catalogTitle) {
    setProperty(PROPERTY_NAME_CATALOGTITLE, catalogTitle);
  }

  public boolean isMaxBeforePaginateReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXBEFOREPAGINATE);
  }

  public int getMaxBeforePaginate() {
    Integer i = getInteger(PROPERTY_NAME_MAXBEFOREPAGINATE);
    if (i == null)
      return defaults.getMaxBeforePaginate();
    else
      return i.intValue();
  }

  public void setMaxBeforePaginate(int maxBeforePaginate) {
    setProperty(PROPERTY_NAME_MAXBEFOREPAGINATE, maxBeforePaginate);
  }

  public boolean isMaxBeforeSplitReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXBEFORESPLIT);
  }

  public int getMaxBeforeSplit() {
    Integer i = getInteger(PROPERTY_NAME_MAXBEFORESPLIT);
    if (i == null)
      return defaults.getMaxBeforeSplit();
    else
      return i.intValue();
  }

  public void setMaxBeforeSplit(int maxBeforeSplit) {
    setProperty(PROPERTY_NAME_MAXBEFORESPLIT, maxBeforeSplit);
  }

  public boolean isMaxMobileResolutionReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXMOBILERESOLUTION);
  }

  public int getMaxMobileResolution() {
    Integer i = getInteger(PROPERTY_NAME_MAXMOBILERESOLUTION);
    if (i == null)
      return defaults.getMaxMobileResolution();
    else
      return i.intValue();
  }

  public void setMaxMobileResolution(int maxMobileResolution) {
    setProperty(PROPERTY_NAME_MAXMOBILERESOLUTION, maxMobileResolution);
  }

  public boolean isBooksInRecentAdditionsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BOOKSINRECENTADDITIONS);
  }

  public int getBooksInRecentAdditions() {
    Integer i = getInteger(PROPERTY_NAME_BOOKSINRECENTADDITIONS);
    if (i == null)
      return defaults.getBooksInRecentAdditions();
    else
      return i.intValue();
  }

  public void setBooksInRecentAdditions(int booksInRecentAdditions) {
    setProperty(PROPERTY_NAME_BOOKSINRECENTADDITIONS, booksInRecentAdditions);
  }

  public boolean isIncludedFormatsListReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEDFORMATSLIST);
  }

  public String getIncludedFormatsList() {
    String s = getProperty(PROPERTY_NAME_INCLUDEDFORMATSLIST);
    if (s == null)
      return defaults.getIncludedFormatsList();
    else
      return s;
  }

  public void setIncludedFormatsList(String includedFormatsList) {
    setProperty(PROPERTY_NAME_INCLUDEDFORMATSLIST, includedFormatsList);
  }

  public boolean isMinimizeChangedFilesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MINIMIZECHANGEDFILES);
  }

  public boolean getMinimizeChangedFiles() {
    Boolean b = getBoolean(PROPERTY_NAME_MINIMIZECHANGEDFILES);
    if (b == null)
      return defaults.getMinimizeChangedFiles();
    else
      return b.booleanValue();
  }

  public void setMinimizeChangedFiles(boolean minimizeChangedFiles) {
    setProperty(PROPERTY_NAME_MINIMIZECHANGEDFILES, minimizeChangedFiles);
  }

  public boolean isExternalIconsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_EXTERNALICONS);
  }

  public boolean getExternalIcons() {
    Boolean b = getBoolean(PROPERTY_NAME_EXTERNALICONS);
    if (b == null)
      return defaults.getExternalIcons();
    else
      return b.booleanValue();
  }

  public void setExternalIcons(boolean externalIcons) {
    setProperty(PROPERTY_NAME_EXTERNALICONS, externalIcons);
  }

  public boolean isSaveBandwidthReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SAVEBANDWIDTH);
  }

  public boolean getSaveBandwidth() {
    Boolean b = getBoolean(PROPERTY_NAME_SAVEBANDWIDTH);
    if (b == null)
      return defaults.getSaveBandwidth();
    else
      return b.booleanValue();
  }

  public void setSaveBandwidth(boolean saveBandwidth) {
    setProperty(PROPERTY_NAME_SAVEBANDWIDTH, saveBandwidth);
  }

  public boolean isGenerateOpdsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEOPDS);
  }

  public boolean getGenerateOpds() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEOPDS);
    if (b == null)
      return defaults.getGenerateOpds();
    else
      return b.booleanValue();
  }

  public void setGenerateOpds(boolean value) {
    setProperty(PROPERTY_NAME_GENERATEOPDS, value);
  }

  public boolean isGenerateHtmlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEHTML);
  }

  public boolean getGenerateHtml() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEHTML);
    if (b == null)
      return defaults.getGenerateHtml();
    else
      return b.booleanValue();
  }

  public void setGenerateHtml(boolean getGenerateHtml) {
    setProperty(PROPERTY_NAME_GENERATEHTML, getGenerateHtml);
  }

  public boolean isGenerateOpdsDownloadsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEOPDSDOWNLOADS);
  }

  public boolean getGenerateOpdsDownloads() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEOPDSDOWNLOADS);
    if (b == null)
      return defaults.getGenerateOpdsDownloads();
    else
      return b.booleanValue();
  }

  public void setGenerateOpdsDownloads(boolean getGenerateOpdsDownloads) {
    setProperty(PROPERTY_NAME_GENERATEOPDSDOWNLOADS, getGenerateOpdsDownloads);
  }

  public boolean isGenerateHtmlDownloadsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEHTMLDOWNLOADS);
  }

  public boolean getGenerateHtmlDownloads() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEHTMLDOWNLOADS);
    if (b == null)
      return defaults.getGenerateHtmlDownloads();
    else
      return b.booleanValue();
  }

  public void setGenerateHtmlDownloads(boolean getGenerateHtmlDownloads) {
    setProperty(PROPERTY_NAME_GENERATEHTMLDOWNLOADS, getGenerateHtmlDownloads);
  }

  public void setPublishedDateAsYear(boolean value) {
    setProperty(PROPERTY_NAME_PUBLISHEDDATEASYEAR, value);
  }

  public boolean isPublishedDateAsYearReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_PUBLISHEDDATEASYEAR);
  }

  public boolean getPublishedDateAsYear() {
    Boolean b = getBoolean(PROPERTY_NAME_PUBLISHEDDATEASYEAR);
    if (b == null)
      return defaults.getSuppressRatingsInTitles();
    else
      return b.booleanValue();
  }

  public void setSuppressRatingsInTitles(boolean supressRatingsInTitles) {
    setProperty(PROPERTY_NAME_SUPRESSRATINGSINTITLES, supressRatingsInTitles);
  }

  public boolean isSupressRatingsInTitlesReadyOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SUPRESSRATINGSINTITLES);
  }

  public boolean getSuppressRatingsInTitles() {
    Boolean b = getBoolean(PROPERTY_NAME_SUPRESSRATINGSINTITLES);
    if (b == null)
      return defaults.getSuppressRatingsInTitles();
    else
      return b.booleanValue();
  }

  public boolean isGenerateDownloadsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEDOWNLOADS);
  }

  public boolean getGenerateDownloads() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEDOWNLOADS);
    if (b == null)
      return defaults.getGenerateDownloads();
    else
      return b.booleanValue();
  }

  public void setGenerateDownloads(boolean generateDownloads) {
    setProperty(PROPERTY_NAME_GENERATEDOWNLOADS, generateDownloads);
  }

  public boolean isThumbnailGenerateReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_THUMBNAILGENERATE);
  }

  public boolean getThumbnailGenerate() {
    Boolean b = getBoolean(PROPERTY_NAME_THUMBNAILGENERATE);
    if (b == null)
      return defaults.getThumbnailGenerate();
    else
      return b.booleanValue();
  }

  public void setThumbnailGenerate(boolean thumbnailGenerate) {
    setProperty(PROPERTY_NAME_THUMBNAILGENERATE, thumbnailGenerate);
  }

  public boolean isThumbnailHeightReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_THUMBNAILHEIGHT);
  }

  public int getThumbnailHeight() {
    Integer i = getInteger(PROPERTY_NAME_THUMBNAILHEIGHT);
    if (i == null)
      return defaults.getThumbnailHeight();
    else
      return i.intValue();
  }

  public void setThumbnailHeight(int thumbnailHeight) {
    setProperty(PROPERTY_NAME_THUMBNAILHEIGHT, thumbnailHeight);
  }

  public boolean isSplitTagsOnReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITTAGSON);
  }

  public String getSplitTagsOn() {
    String s = getProperty(PROPERTY_NAME_SPLITTAGSON);
    if (s == null)
      return defaults.getSplitTagsOn();
    else
      return s;
  }

  public void setSplitTagsOn(String splitTagsOn) {
    setProperty(PROPERTY_NAME_SPLITTAGSON, splitTagsOn);
  }

  public boolean isIncludeBooksWithNoFileReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE);
  }

  public boolean getIncludeBooksWithNoFile() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE);
    if (b == null)
      return defaults.getIncludeBooksWithNoFile();
    else
      return b.booleanValue();
  }

  public void setIncludeBooksWithNoFile(boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE, value);
  }

  public boolean isCryptFilenamesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CRYPTFILENAMES);
  }

  public boolean getCryptFilenames() {
    Boolean b = getBoolean(PROPERTY_NAME_CRYPTFILENAMES);
    if (b == null)
      return defaults.getCryptFilenames();
    else
      return b.booleanValue();
  }

  public void setCryptFilenames(boolean value) {
    setProperty(PROPERTY_NAME_CRYPTFILENAMES, value);
  }

  public boolean isShowSeriesInAuthorCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG);
  }

  public boolean getShowSeriesInAuthorCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG);
    if (b == null)
      return defaults.getShowSeriesInAuthorCatalog();
    else
      return b.booleanValue();
  }

  public void setShowSeriesInAuthorCatalog(boolean value) {
    setProperty(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG, value);
  }

  public boolean isGenerateExternalLinksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEEXTERNALLINKS);
  }

  public boolean getGenerateExternalLinks() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEEXTERNALLINKS);
    if (b == null)
      return defaults.getGenerateExternalLinks();
    else
      return b.booleanValue();
  }

  public void setGenerateExternalLinks(boolean value) {
    setProperty(PROPERTY_NAME_GENERATEEXTERNALLINKS, value);
  }

  public boolean isGenerateCrossLinksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATECROSSLINKS);
  }

  public boolean getGenerateCrossLinks() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATECROSSLINKS);
    if (b == null)
      return defaults.getGenerateCrossLinks();
    else
      return b.booleanValue();
  }

  public void setGenerateCrossLinks(boolean value) {
    setProperty(PROPERTY_NAME_GENERATECROSSLINKS, value);
  }

  public boolean isTagsToGenerateReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TAGSTOGENERATE);
  }

  public String getTagsToGenerate() {
    String s = getProperty(PROPERTY_NAME_TAGSTOGENERATE);
    if (s == null)
      return defaults.getTagsToGenerate();
    else
      return s;
  }

  public void setTagsToGenerate(String value) {
    setProperty(PROPERTY_NAME_TAGSTOGENERATE, value);
  }

  public boolean isTagsToExcludeReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TAGSTOEXCLUDE);
  }

  public String getTagsToExclude() {
    String s = getProperty(PROPERTY_NAME_TAGSTOEXCLUDE);
    if (s == null)
      return defaults.getTagsToExclude();
    else
      return s;
  }

  public void setTagsToExclude(String value) {
    setProperty(PROPERTY_NAME_TAGSTOEXCLUDE, value);
  }

  public boolean isMaxSummaryLengthReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXSUMMARYLENGTH);
  }

  public int getMaxSummaryLength() {
    Integer i = getInteger(PROPERTY_NAME_MAXSUMMARYLENGTH);
    if (i == null)
      return defaults.getMaxSummaryLength();
    else
      return i.intValue();
  }

  public void setMaxSummaryLength(int value) {
    setProperty(PROPERTY_NAME_MAXSUMMARYLENGTH, value);
  }

  public boolean isGenerateTagsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATETAGS);
  }

  public boolean getGenerateTags() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATETAGS);
    if (b == null)
      return defaults.getGenerateTags();
    else
      return b.booleanValue();
  }

  public void setGenerateTags(boolean value) {
    setProperty(PROPERTY_NAME_GENERATETAGS, value);
  }

  public boolean isGenerateRecentReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATERECENT);
  }

  public boolean getGenerateRecent() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATERECENT);
    if (b == null)
      return defaults.getGenerateRecent();
    else
      return b.booleanValue();
  }

  public void setGenerateRecent(boolean value) {
    setProperty(PROPERTY_NAME_GENERATERECENT, value);
  }

  public boolean isGenerateRatingsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATERATINGS);
  }

  public boolean getGenerateRatings() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATERATINGS);
    if (b == null)
      return defaults.getGenerateRatings();
    else
      return b.booleanValue();
  }

  public void setGenerateRatings(boolean value) {
    setProperty(PROPERTY_NAME_GENERATERATINGS, value);
  }

  public boolean isGenerateAllbooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEALLBOOKS);
  }

  public boolean getGenerateAllbooks() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEALLBOOKS);
    if (b == null)
      return defaults.getGenerateAllbooks();
    else
      return b.booleanValue();
  }

  public void setGenerateAllbooks(boolean value) {
    setProperty(PROPERTY_NAME_GENERATEALLBOOKS, value);
  }

  public boolean isGenerateIndexReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEALLBOOKS);
  }

  public boolean getGenerateIndex() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEINDEX);
    if (b == null)
      return defaults.getGenerateAllbooks();
    else
      return b.booleanValue();
  }

  public void setGenerateIndex(boolean value) {
    setProperty(PROPERTY_NAME_GENERATEINDEX, value);
  }


  public boolean isCopyToDatabaseFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COPYTODATABASEFOLDER);
  }

  public boolean getCopyToDatabaseFolder() {
    Boolean b = getBoolean(PROPERTY_NAME_COPYTODATABASEFOLDER);
    if (b == null)
      return defaults.getCopyToDatabaseFolder();
    else
      return b.booleanValue();
  }

  public void setCopyToDatabaseFolder(boolean value) {
    setProperty(PROPERTY_NAME_COPYTODATABASEFOLDER, value);
  }

  public boolean isBrowseByCoverReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BROWSEBYCOVER);
  }

  public boolean getBrowseByCover() {
    Boolean b = getBoolean(PROPERTY_NAME_BROWSEBYCOVER);
    if (b == null)
      return defaults.getBrowseByCover();
    else
      return b.booleanValue();
  }

  public void setBrowseByCover(boolean value) {
    setProperty(PROPERTY_NAME_BROWSEBYCOVER, value);
  }

  public boolean isSplitByAuthorInitialGoToBooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK);
  }

  public boolean getSplitByAuthorInitialGoToBooks() {
    Boolean b = getBoolean(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK);
    if (b == null)
      return defaults.getSplitByAuthorInitialGoToBooks();
    else
      return b.booleanValue();
  }

  public void setSplitByAuthorInitialGoToBooks(boolean value) {
    setProperty(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK, value);
  }

  public boolean isIncludeAboutLinkReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEABOUTLINK);
  }

  public boolean getIncludeAboutLink() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEABOUTLINK);
    if (b == null)
      return defaults.getIncludeAboutLink();
    else
      return b.booleanValue();
  }

  public void setIncludeAboutLink(boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEABOUTLINK, value);
  }

  public boolean isTagsToMakeDeepReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TAGSTOMAKEDEEP);
  }

  private List<String> tokenizedTagsToMakeDeep;

  public List<String> getTokenizedTagsToMakeDeep() {
    if (tokenizedTagsToMakeDeep == null) {
      tokenizedTagsToMakeDeep = Helper.tokenize(getTagsToMakeDeep().toUpperCase(), ",", true);
    }
    return tokenizedTagsToMakeDeep;
  }

  public String getTagsToMakeDeep() {
    String s = getProperty(PROPERTY_NAME_TAGSTOMAKEDEEP);
    if (s == null)
      return defaults.getTagsToMakeDeep();
    else
      return s;
  }

  public void setTagsToMakeDeep(String value) {
    setProperty(PROPERTY_NAME_TAGSTOMAKEDEEP, value);
    tokenizedTagsToMakeDeep = null;
  }

  public boolean isMinBooksToMakeDeepLevelReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL);
  }

  public int getMinBooksToMakeDeepLevel() {
    Integer i = getInteger(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL);
    if (i == null)
      return defaults.getMinBooksToMakeDeepLevel();
    else
      return i.intValue();
  }

  public void setMinBooksToMakeDeepLevel(int value) {
    setProperty(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL, value);
  }

  public boolean isBrowseByCoverWithoutSplitReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT);
  }

  public boolean getBrowseByCoverWithoutSplit() {
    Boolean b = getBoolean(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT);
    if (b == null)
      return defaults.getBrowseByCoverWithoutSplit();
    else
      return b.booleanValue();
  }

  public void setBrowseByCoverWithoutSplit(boolean value) {
    setProperty(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT, value);
  }


  public boolean isCoverResizeReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COVERRESIZE);
  }

  public boolean getCoverResize() {
    Boolean b = getBoolean(PROPERTY_NAME_COVERRESIZE);
    if (b == null)
      return defaults.getThumbnailGenerate();
    else
      return b.booleanValue();
  }

  public void setCoverResize(boolean coverResize) {
    setProperty(PROPERTY_NAME_COVERRESIZE, coverResize);
  }

  public boolean isCoverHeightReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COVERHEIGHT);
  }

  public int getCoverHeight() {
    Integer i = getInteger(PROPERTY_NAME_COVERHEIGHT);
    if (i == null)
      return defaults.getCoverHeight();
    else
      return i.intValue();
  }

  public void setCoverHeight(int value) {
    setProperty(PROPERTY_NAME_COVERHEIGHT, value);
  }

  public boolean isIncludeOnlyOneFileReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEONLYONEFILE);
  }

  public boolean getIncludeOnlyOneFile() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEONLYONEFILE);
    if (b == null)
      return defaults.getIncludeOnlyOneFile();
    else
      return b.booleanValue();
  }

  public void setIncludeOnlyOneFile(boolean value) {
    setProperty(PROPERTY_NAME_ZIPTROOKCATALOG, value);
  }

  public boolean isZipTrookCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ZIPTROOKCATALOG);
  }

  public boolean getZipTrookCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_ZIPTROOKCATALOG);
    if (b == null)
      return defaults.getZipTrookCatalog();
    else
      return b.booleanValue();
  }

  public void setZipTrookCatalog(boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEONLYONEFILE, value);
  }

  public boolean isReprocessEpubMetadataReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_REPROCESSEPUBMETADATA);
  }

  public boolean getReprocessEpubMetadata() {
    Boolean b = getBoolean(PROPERTY_NAME_REPROCESSEPUBMETADATA);
    if (b == null)
      return defaults.getReprocessEpubMetadata();
    else
      return b.booleanValue();
  }

  public void setReprocessEpubMetadata(boolean value) {
    setProperty(PROPERTY_NAME_REPROCESSEPUBMETADATA, value);
  }

  public boolean isOrderAllBooksBySeriesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ORDERALLBOOKSBYSERIES);
  }

  public boolean getOrderAllBooksBySeries() {
    Boolean b = getBoolean(PROPERTY_NAME_ORDERALLBOOKSBYSERIES);
    if (b == null)
      return defaults.getOrderAllBooksBySeries();
    else
      return b.booleanValue();
  }

  public void setOrderAllBooksBySeries(boolean value) {
    setProperty(PROPERTY_NAME_ORDERALLBOOKSBYSERIES, value);
  }

  public boolean isCompatibilityTrickReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COMPATIBILITYTRICK);
  }

  public CompatibilityTrick getCompatibilityTrick() {
    String s = getProperty(PROPERTY_NAME_COMPATIBILITYTRICK);
    if (s == null)
      return defaults.getCompatibilityTrick();
    else
      return CompatibilityTrick.fromString(s);
  }

  public void setCompatibilityTrick(CompatibilityTrick value) {
    setProperty(PROPERTY_NAME_COMPATIBILITYTRICK, value.toString());
  }

  public boolean isSplitInAuthorBooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITINAUTHORBOOKS);
  }

  public boolean getSplitInAuthorBooks() {
    Boolean b = getBoolean(PROPERTY_NAME_SPLITINAUTHORBOOKS);
    if (b == null)
      return defaults.getSplitInAuthorBooks();
    else
      return b.booleanValue();
  }

  public void setSplitInAuthorBooks(boolean value) {
    setProperty(PROPERTY_NAME_SPLITINAUTHORBOOKS, value);
  }

  public boolean isSplitInSeriesBooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITINSERIESBOOKS);
  }

  public boolean getSplitInSeriesBooks() {
    Boolean b = getBoolean(PROPERTY_NAME_SPLITINSERIESBOOKS);
    if (b == null)
      return defaults.getSplitInSeriesBooks();
    else
      return b.booleanValue();
  }

  public void setSplitInSeriesBooks(boolean value) {
    setProperty(PROPERTY_NAME_SPLITINSERIESBOOKS, value);
  }

  /* external links */
  public String getWikipediaUrl() {
    String s = getProperty(PROPERTY_NAME_WIKIPEDIAURL);
    if (s == null)
      return defaults.getWikipediaUrl();
    else
      return s;
  }

  public void setWikipediaUrl(String value) {
    setProperty(PROPERTY_NAME_WIKIPEDIAURL, value);
  }

  public boolean isWikipediaUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_WIKIPEDIAURL);
  }

  public String getAmazonAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONAUTHORURL);
    if (s == null)
      return defaults.getAmazonAuthorUrl();
    else
      return s;
  }

  public void setAmazonAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONAUTHORURL, value);
  }

  public boolean isAmazonAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONAUTHORURL);
  }

  public String getAmazonIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONISBNURL);
    if (s == null)
      return defaults.getAmazonIsbnUrl();
    else
      return s;
  }

  public void setAmazonIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONISBNURL, value);
  }

  public boolean isAmazonIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONISBNURL);
  }

  public String getAmazonTitleUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONTITLEURL);
    if (s == null)
      return defaults.getAmazonTitleUrl();
    else
      return s;
  }

  public void setAmazonTitleUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONTITLEURL, value);
  }

  public boolean isAmazonTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONTITLEURL);
  }

  public String getGoodreadAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADAUTHORURL);
    if (s == null)
      return defaults.getGoodreadAuthorUrl();
    else
      return s;
  }

  public void setGoodreadAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADAUTHORURL, value);
  }

  public boolean isGoodreadAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADAUTHORURL);
  }

  public String getGoodreadIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADISBNURL);
    if (s == null)
      return defaults.getGoodreadIsbnUrl();
    else
      return s;
  }

  public void setGoodreadIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADISBNURL, value);
  }

  public boolean isGoodreadIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADISBNURL);
  }

  public String getGoodreadTitleUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADTITLEURL);
    if (s == null)
      return defaults.getGoodreadTitleUrl();
    else
      return s;
  }

  public void setGoodreadTitleUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADTITLEURL, value);
  }

  public boolean isGoodreadTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADTITLEURL);
  }

  public String getGoodreadReviewIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADREVIEWISBNURL);
    if (s == null)
      return defaults.getGoodreadReviewIsbnUrl();
    else
      return s;
  }

  public void setGoodreadReviewIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADREVIEWISBNURL, value);
  }

  public boolean isGoodreadReviewIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADREVIEWISBNURL);
  }

  public String getIsfdbAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_ISFDBAUTHORURL);
    if (s == null)
      return defaults.getIsfdbAuthorUrl();
    else
      return s;
  }

  public void setIsfdbAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_ISFDBAUTHORURL, value);
  }

  public boolean isIsfdbAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ISFDBAUTHORURL);
  }

  public String getLibrarythingAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGAUTHORURL);
    if (s == null)
      return defaults.getLibrarythingAuthorUrl();
    else
      return s;
  }

  public void setLibrarythingAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGAUTHORURL, value);
  }

  public boolean isLibrarythingAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGAUTHORURL);
  }

  public String getLibrarythingIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGISBNURL);
    if (s == null)
      return defaults.getLibrarythingIsbnUrl();
    else
      return s;
  }

  public void setLibrarythingIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGISBNURL, value);
  }

  public boolean isLibrarythingIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGISBNURL);
  }

  public String getLibrarythingTitleUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGTITLEURL);
    if (s == null)
      return defaults.getLibrarythingTitleUrl();
    else
      return s;
  }

  public void setLibrarythingTitleUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGTITLEURL, value);
  }

  public boolean isLibrarythingTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGTITLEURL);
  }

  public Index.FilterHintType getIndexFilterAlgorithm() {
    String s = getProperty(PROPERTY_NAME_INDEXFILTERALGORITHM);
    if (s == null)
      return defaults.getIndexFilterAlgorithm();
    else
      return Index.FilterHintType.valueOf(s);
  }

  public void setIndexFilterAlgorithm(Index.FilterHintType value) {
    setProperty(PROPERTY_NAME_INDEXFILTERALGORITHM, value);
  }

  public boolean isIndexFilterAlgorithmReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INDEXFILTERALGORITHM);
  }

  public boolean getIndexComments() {
    Boolean b = getBoolean(PROPERTY_NAME_INDEXCOMMENTS);
    if (b == null)
      return defaults.getIndexComments();
    else
      return b.booleanValue();
  }

  public void setIndexComments(boolean value) {
    setProperty(PROPERTY_NAME_INDEXCOMMENTS, value);
  }

  public boolean isIndexCommentsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INDEXCOMMENTS);
  }

  public int getMaxKeywords() {
    Integer i = getInteger(PROPERTY_NAME_MAXKEYWORDS);
    if (i == null)
      return defaults.getMaxKeywords();
    else
      return i.intValue();
  }

  public void setMaxKeywords(int value) {
    setProperty(PROPERTY_NAME_MAXKEYWORDS, value);
  }

  public boolean isMaxKeywordsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXKEYWORDS);
  }

  public boolean isUrlBaseReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_URLBASE);
  }

  public String getUrlBase() {
    String s = getProperty(PROPERTY_NAME_URLBASE);
    if (s == null)
      return defaults.getUrlBase();
    else
      return s;
  }

  public void setUrlBase(String value) {
    // correct the value if needed
    if (value != null && value.charAt(value.length() - 1) != '/')
      value += "/";
    setProperty(PROPERTY_NAME_URLBASE, value);
  }

  public boolean isCustomCatalogSavedSearchNameReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CUSTOMCATALOGSAVEDSEARCHNAME);
  }

  public String getCustomCatalogSavedSearchName() {
    String s = getProperty(PROPERTY_NAME_CUSTOMCATALOGSAVEDSEARCHNAME);
    if (s == null)
      return defaults.getCustomCatalogSavedSearchName();
    else
      return s;
  }

  public void setCustomCatalogSavedSearchName(String value) {
    // correct the value if needed
    if (value != null && value.charAt(value.length() - 1) != '/')
      value += "/";
    setProperty(PROPERTY_NAME_CUSTOMCATALOGSAVEDSEARCHNAME, value);
  }

  public boolean isCustomCatalogTitleReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CUSTOMCATALOGTITLE);
  }

  public String getCustomCatalogTitle() {
    String s = getProperty(PROPERTY_NAME_CUSTOMCATALOGTITLE);
    if (s == null)
      return defaults.getCustomCatalogTitle();
    else
      return s;
  }

  public void setCustomCatalogTitle(String value) {
    // correct the value if needed
    if (value != null && value.charAt(value.length() - 1) != '/')
      value += "/";
    setProperty(PROPERTY_NAME_CUSTOMCATALOGTITLE, value);
  }
}
