package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Language;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationHolder extends PropertiesBasedConfiguration implements SetConfigurationInterface {

  final static String PROPERTY_NAME_VERSIONCHIP = "VERSIONCHIP";
  final static String PATTERN_CUSTOMCATALOG_ID = "customCatalog";
  final static String PATTERN_CUSTOMCATALOG_TITLE_ID = "title";
  final static String PATTERN_CUSTOMCATALOG_SEARCH_ID = "search";
  final static String PATTERN_CUSTOMCATALOG_ATTOP_ID = "attop";
  final static Pattern PATTERN_CUSTOMCATALOG = Pattern.compile("\\[" + PATTERN_CUSTOMCATALOG_ID + "\\](.+?)\\[/" + PATTERN_CUSTOMCATALOG_ID + "\\]");
  final static Pattern PATTERN_CUSTOMCATALOG_TITLE = Pattern.compile("\\[" + PATTERN_CUSTOMCATALOG_TITLE_ID + "\\](.+?)\\[/" + PATTERN_CUSTOMCATALOG_TITLE_ID + "\\]");
  final static Pattern PATTERN_CUSTOMCATALOG_SEARCH = Pattern.compile("\\[" + PATTERN_CUSTOMCATALOG_SEARCH_ID + "\\](.+?)\\[/" + PATTERN_CUSTOMCATALOG_SEARCH_ID + "\\]");
  final static Pattern PATTERN_CUSTOMCATALOG_ATTOP = Pattern.compile("\\[" + PATTERN_CUSTOMCATALOG_ATTOP_ID + "\\](.+?)\\[/" + PATTERN_CUSTOMCATALOG_ATTOP_ID + "\\]");

  private final static String PROPERTY_NAME_WINDOW_HEIGHT = "WindowHeight";
  private final static String PROPERTY_NAME_WINDOW_WIDTH = "WindowWidth";

  private final static String PROPERTY_NAME_FAVICON = "Favicon";
  private final static String PROPERTY_NAME_DATABASEFOLDER = "DatabaseFolder";
  private final static String PROPERTY_NAME_TARGETFOLDER = "TargetFolder";
  private final static String PROPERTY_NAME_LANGUAGE = "Language";
  private final static String PROPERTY_NAME_CATALOGFOLDERNAME = "CatalogFolderName";
  private final static String PROPERTY_NAME_ONLY_CATALOG_AT_TARGRET = "OnlyCatalogAtTarget";
  private final static String PROPERTY_NAME_CATALOGTITLE = "CatalogTitle";
  private final static String PROPERTY_NAME_MAXBEFOREPAGINATE = "MaxBeforePaginate";
  private final static String PROPERTY_NAME_MAXBEFORESPLIT = "MaxBeforeSplit";
  private final static String PROPERTY_NAME_MAXSPLITLEVELS = "MaxSplitLevels";
  private final static String PROPERTY_NAME_BOOKSINRECENTADDITIONS = "BooksInRecentAdditions";
  private final static String PROPERTY_NAME_WIKIPEDIALANGUAGE = "WikipediaLanguage";
  private final static String PROPERTY_NAME_INCLUDEDFORMATSLIST = "IncludedFormatsList";
  private final static String PROPERTY_NAME_THUMBNAILGENERATE = "ThumbnailGenerate";
  private final static String PROPERTY_NAME_THUMBNAILHEIGHT = "ThumbnailHeight";
  private final static String PROPERTY_NAME_GENERATEHTML = "GenerateHtml";
  private final static String PROPERTY_NAME_GENERATEOPDS = "GenerateOpds";
  private final static String PROPERTY_NAME_GENERATEOPDSDOWNLOADS = "GenerateOpdsDownloads";
  private final static String PROPERTY_NAME_GENERATEHTMLDOWNLOADS = "GenerateHtmlDownloads";
  private final static String PROPERTY_NAME_SUPRESSRATINGSINTITLES = "SuppressRatingsInTitles";
  private final static String PROPERTY_NAME_SPLITTAGSON = "SplitTagsOn";
  private final static String PROPERTY_NAME_NO_SPLITTAGSON = "DontSplitTagsOn";
  private final static String PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE = "IncludeBooksWithNoFile";
  private final static String PROPERTY_NAME_CRYPTFILENAMES = "CryptFilenames";
  private final static String PROPERTY_NAME_MINIMIZECHANGEDFILES = "MinimizeChangedFiles";
  private final static String PROPERTY_NAME_LANGUAGEASTAG = "LanguageAsTag";
  private final static String PROPERTY_NAME_TAGSTOIGNORE = "TagsToIgnore";
  private final static String PROPERTY_NAME_EXTERNALICONS = "ExternalIcons";
  private final static String PROPERTY_NAME_EXTERNAL_IMAGES = "ExternalImages";
  private final static String PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG = "ShowSeriesInAuthorCatalog";
  private final static String PROPERTY_NAME_CATALOGFILTER = "CatalogFilter";
  private final static String PROPERTY_NAME_TAGSTOMAKEDEEP = "TagsToMakeDeep";
  private final static String PROPERTY_NAME_MAXSUMMARYLENGTH = "MaxSummaryLength";
  private final static String PROPERTY_NAME_MAXBOOKSUMMARYLENGTH = "MaxBookSummaryLength";
  private final static String PROPERTY_NAME_GENERATEAUTHORS = "GenerateAuthors";
  private final static String PROPERTY_NAME_GENERATETAGS = "GenerateTags";
  private final static String PROPERTY_NAME_GENERATESERIES = "GenerateSeries";
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
  private final static String PROPERTY_NAME_ZIPTROOKCATALOG = "ZipTrookCatalog";
  private final static String PROPERTY_NAME_REPROCESSEPUBMETADATA = "ReprocessEpubMetadata";
  private final static String PROPERTY_NAME_ORDERALLBOOKSBYSERIES = "OrderAllBooksBySeries";
  private final static String PROPERTY_NAME_MAXMOBILERESOLUTION = "MaxMobileResolution";
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
  private final static String PROPERTY_NAME_URLBOOKS = "UrlBooks";
  private final static String PROPERTY_NAME_FEATUREDCATALOGTITLE = "FeaturedCatalogTitle";
  private final static String PROPERTY_NAME_FEATUREDCATALOGSAVEDSEARCHNAME = "FeaturedCatalogSavedSearchName";
  private final static String PROPERTY_NAME_CUSTOMCATALOGS = "CustomCatalogs";
  private final static String PROPERTY_NAME_CATALOGCUSTOMCOLUMNS = "CatalogCustomColumns";
  /* Catalog Structure */
  private final static String PROPERTY_NAME_SortUsingAuthor = "SortUsingAuthor";
  private final static String PROPERTY_NAME_SortUsingTitle = "SortUsingTitle";
  private final static String PROPERTY_NAME_SortUsingSeries = "SortUsingSeries";
  private final static String PROPERTY_NAME_SortTagsByAuthor = "SortTagsByAuthor";
  private final static String PROPERTY_NAME_TagBooksNoSplit = "TagBooksNoSplit";
  /* Book Details */
  private final static String PROPERTY_NAME_INCLUDESERIESINBOOKDETAILS = "IncludeSeriesInBookDetails";
  private final static String PROPERTY_NAME_INCLUDERATINGINBOOKDETAILS = "IncludeRatingInBookDetails";
  private final static String PROPERTY_NAME_INCLUDETAGSINBOOKDETAILS = "IncludeTagsInBookDetails";
  private final static String PROPERTY_NAME_INCLUDEPUBLISHERINBOOKDETAILS = "IncludePublisherInBookDetails";
  private final static String PROPERTY_NAME_INCLUDEPUBLISHEDINBOOKDETAILS = "IncludePublishedInBookDetails";
  private final static String PROPERTY_NAME_PUBLISHEDDATEASYEAR = "PublishDateAsYear";
  private final static String PROPERTY_NAME_IncludeAddedInBookDetailst = "IncludeAddedInBookDetailst";
  private final static String PROPERTY_NAME_IncludeModifiedInBookDetailst = "IncludeModifiedInBookDetailst";
  private final static String PROPERTY_NAME_DisplayAuthorSort = "DisplayAuthorSort";
  private final static String PROPERTY_NAME_DisplayTitleSort = "DisplayTitleSort";
  private final static String PROPERTY_NAME_DisplaySeriesSort = "DisplaySeriesSort";
  private final static String PROPERTY_NAME_BookDetailsCustomFields = "BookDetailsCustomFields";
  private final static String PROPERTY_NAME_BookDetailsCustomFieldsAlways = "BookDetailsCustomFieldsAlways";
  private final static String PROPERTY_NAME_GenerateCrossLinks = "GenerateCrossLinks";
  private final static String PROPERTY_NAME_SingleBookCrossReferences = "SingleBookCrossReferences";
  private final static String PROPERTY_NAME_IncludeAuthorCrossReferences = "IncludeAuthorCrossReferences";
  private final static String PROPERTY_NAME_IncludeSerieCrossReferences = "IncludeSerieCrossReferences";
  private final static String PROPERTY_NAME_IncludeTagCrossReferences = "IncludeTagCrossReferences";
  private final static String PROPERTY_NAME_IncludeRatingCrossReferences = "IncludeRatingCrossReferences";
  private final static String PROPERTY_NAME_GenerateExternalLinks = "GenerateExternalLinks";
  /* Advanced */
  private final static String PROPERTY_NAME_INCLUDE_COVERS_IN_CATALOG = "IncludeCoversInCatalog";
  private final static String PROPERTY_NAME_USE_THUMBNAILS_AS_COVERS = "UseThumbnsilsAsCovers";
  private final static String PROPERTY_NAME_ZIP_CATALOG = "ZipCatalog";
  private final static String PROPERTY_NAME_ZIP_OMIT_XML = "ZipOmitXml";
  private final static String PROPERTY_NAME_SecurityCode = "SecurityCode";

  final static Logger logger = Logger.getLogger(ConfigurationHolder.class);

  private DefaultConfigurationSettings defaults = new DefaultConfigurationSettings();

  // Variables used to store cached values
  // These should all be cleared on a reset
  private List<String> tokenizedCatalogCustomColumns;
  private List<String> regexTagsToIgnore;
  private List<String> tokenizedTagsToMakeDeep;
  private List<String> tokenizedBookDetailsCustomColumns;


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
    tokenizedTagsToMakeDeep = null;
    tokenizedBookDetailsCustomColumns = null;
    tokenizedCatalogCustomColumns = null;
    regexTagsToIgnore = null;

    DefaultConfigurationSettings defaults = new DefaultConfigurationSettings();
    for (Method getter : GetConfigurationInterface.class.getMethods()) {
      String getterName = getter.getName();
      String setterName = "set" + getterName.substring(3);
      //try {
      Class returnType = getter.getReturnType();
      Method setter = null;
      try {
        setter = this.getClass().getMethod(setterName, returnType);
        Object result = getter.invoke(defaults);
        setter.invoke(this, result);
      } catch (NoSuchMethodException e) {
        logger.warn("", e);
      } catch (InvocationTargetException e) {
        logger.warn("", e);
      } catch (IllegalAccessException e) {
        logger.warn("", e);
      }
    }
    setDeviceMode(getDeviceMode());
  }

  public Boolean isObsolete() {
    // check for the version chip
    String versionChip = getProperty(PROPERTY_NAME_VERSIONCHIP);
    if (versionChip == null) {
      return false;
    } else {
      return (versionChip.compareTo(Constants.CONFIGURATION_COMPATIBILITY_VERSIONCHIP) == -1) ? true : false;
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

  public Integer getWindowHeight() {
    Integer i = getInteger(PROPERTY_NAME_WINDOW_HEIGHT);
    return (i == null) ?defaults.getWindowHeight() : i;
  }
  public void setWindowHeight(Integer height) {
    setProperty(PROPERTY_NAME_WINDOW_HEIGHT, height);
  }

  public Integer getWindowWidth() {
    Integer i = getInteger(PROPERTY_NAME_WINDOW_WIDTH);
    return (i == null) ?defaults.getWindowWidth() : i;
  }
  public void setWindowWidth(Integer width) {
    setProperty(PROPERTY_NAME_WINDOW_WIDTH, width);
  }


  public DeviceMode getDeviceMode() {
    String s = getProperty(PROPERTY_NAME_DEVICEMODE);
    return (s == null) ? defaults.getDeviceMode() : DeviceMode.fromName(s);
  }
  public void setDeviceMode(DeviceMode mode) {
    setProperty(PROPERTY_NAME_DEVICEMODE, mode);
    if (mode != null) {
      resetReadOnly();
      mode.setModeSpecificOptions(this);
    } else {
      reset();
    }
  }

  public String getFavicon() {
    String s = getProperty(PROPERTY_NAME_FAVICON);
    return (Helper.isNullOrEmpty(s)) ? defaults.getFavicon() : s;
  }
  public void setFavicon(String Favicon) {
    setProperty(PROPERTY_NAME_FAVICON, Favicon);
  }

  public Boolean isDatabaseFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_DATABASEFOLDER);
  }
  public File getDatabaseFolder() {
    String s = getProperty(PROPERTY_NAME_DATABASEFOLDER);
    return (Helper.isNotNullOrEmpty(s)) ? new File(s) : null;
  }
  public void setDatabaseFolder(File databaseFolder) {
    DataModel.INSTANCE.reset(); // reset the datamodel when the database changes !
    setProperty(PROPERTY_NAME_DATABASEFOLDER, Helper.isNullOrEmpty(databaseFolder) ? "" : databaseFolder.getAbsolutePath());
  }

  public Boolean isTargetFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TARGETFOLDER);
  }
  public File getTargetFolder() {
    String s = getProperty(PROPERTY_NAME_TARGETFOLDER);
    return (Helper.isNotNullOrEmpty(s)) ? new File(s) : null;
  }
  public void setTargetFolder(File targetFolder) {
    setProperty(PROPERTY_NAME_TARGETFOLDER, Helper.isNullOrEmpty(targetFolder) ? "" : targetFolder.getAbsolutePath());
  }

  public Boolean isLanguageReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LANGUAGE);
  }
  public String getLanguage() {
    String s = getProperty(PROPERTY_NAME_LANGUAGE);
    return (Helper.isNullOrEmpty(s)) ? defaults.getLanguage() : s;
  }
  /*
  public Language getLanguage() {
    String s = getLanguage()[]
  }
  */
  public void setLanguage(String language) {
    setProperty(PROPERTY_NAME_LANGUAGE, language);
  }

  public Boolean isWikipediaLanguageReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_WIKIPEDIALANGUAGE);
  }
  public String getWikipediaLanguage() {
    String s = getProperty(PROPERTY_NAME_WIKIPEDIALANGUAGE);
    return (s == null) ? defaults.getWikipediaLanguage() : s;
  }
  public void setWikipediaLanguage(String wikipediaLanguage) {
    setProperty(PROPERTY_NAME_WIKIPEDIALANGUAGE, wikipediaLanguage);
  }

  public Boolean isCatalogFolderNameReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CATALOGFOLDERNAME);
  }
  public String getCatalogFolderName() {
    String s = getProperty(PROPERTY_NAME_CATALOGFOLDERNAME);
    return (s == null) ? defaults.getCatalogFolderName() : s;
  }
  public void setCatalogFolderName(String catalogFolderName) {
    setProperty(PROPERTY_NAME_CATALOGFOLDERNAME, catalogFolderName);
  }

  public Boolean isCatalogTitleReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CATALOGTITLE);
  }
  public String getCatalogTitle() {
    String s = getProperty(PROPERTY_NAME_CATALOGTITLE);
    return (s == null) ? defaults.getCatalogTitle() : s;
  }
  public void setCatalogTitle(String catalogTitle) {
    setProperty(PROPERTY_NAME_CATALOGTITLE, catalogTitle);
  }

  public Boolean isMaxBeforePaginateReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXBEFOREPAGINATE);
  }
  public Integer getMaxBeforePaginate() {
    Integer i = getInteger(PROPERTY_NAME_MAXBEFOREPAGINATE);
    return (i == null) ? defaults.getMaxBeforePaginate() : i;
  }
  public void setMaxBeforePaginate(Integer maxBeforePaginate) {
    setProperty(PROPERTY_NAME_MAXBEFOREPAGINATE, maxBeforePaginate);
  }

  public Boolean isMaxBeforeSplitReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXBEFORESPLIT);
  }
  public Integer getMaxBeforeSplit() {
    Integer i = getInteger(PROPERTY_NAME_MAXBEFORESPLIT);
    return (i == null) ? defaults.getMaxBeforeSplit() : ((i < 1) ? 1 : i);
  }
  public void setMaxBeforeSplit(Integer maxBeforeSplit) {
    setProperty(PROPERTY_NAME_MAXBEFORESPLIT, maxBeforeSplit);
  }

  public Boolean isMaxSplitLevelsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXSPLITLEVELS);
  }
  public Integer getMaxSplitLevels() {
    Integer i = getInteger(PROPERTY_NAME_MAXSPLITLEVELS);
    return (i == null) ?defaults.getMaxSplitLevels() : i;
  }
  public void setMaxSplitLevels(Integer maxSplitLevels) {
    setProperty(PROPERTY_NAME_MAXSPLITLEVELS, maxSplitLevels);
  }

  public Boolean isMaxMobileResolutionReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXMOBILERESOLUTION);
  }
  public Integer getMaxMobileResolution() {
    Integer i = getInteger(PROPERTY_NAME_MAXMOBILERESOLUTION);
    return (i == null) ? defaults.getMaxMobileResolution() : i;
  }
  public void setMaxMobileResolution(Integer maxMobileResolution) {
    setProperty(PROPERTY_NAME_MAXMOBILERESOLUTION, maxMobileResolution);
  }

  public Boolean isBooksInRecentAdditionsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BOOKSINRECENTADDITIONS);
  }
  public Integer getBooksInRecentAdditions() {
    Integer i = getInteger(PROPERTY_NAME_BOOKSINRECENTADDITIONS);
    return (i == null) ? defaults.getBooksInRecentAdditions() : i;
  }
  public void setBooksInRecentAdditions(Integer booksInRecentAdditions) {
    setProperty(PROPERTY_NAME_BOOKSINRECENTADDITIONS, booksInRecentAdditions);
  }

  public Boolean isIncludedFormatsListReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEDFORMATSLIST);
  }
  public String getIncludedFormatsList() {
    String s = getProperty(PROPERTY_NAME_INCLUDEDFORMATSLIST);
    return (s == null) ? defaults.getIncludedFormatsList() : s;
  }
  public void setIncludedFormatsList(String includedFormatsList) {
    setProperty(PROPERTY_NAME_INCLUDEDFORMATSLIST, includedFormatsList);
  }

  public Boolean isBookDetailsCustomFieldsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BookDetailsCustomFields);
  }
  public String getBookDetailsCustomFields() {
    String s = getProperty(PROPERTY_NAME_BookDetailsCustomFields);
    return (s == null) ? defaults.getBookDetailsCustomFields() : s;
  }
  public void setBookDetailsCustomFields(String fieldList) {
    setProperty(PROPERTY_NAME_BookDetailsCustomFields, fieldList);
  }

  public Boolean isBookDetailsCustomFieldsAlwaysReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BookDetailsCustomFieldsAlways);
  }
  public Boolean getBookDetailsCustomFieldsAlways() {
    Boolean b = getBoolean(PROPERTY_NAME_BookDetailsCustomFieldsAlways);
    return (b == null) ? defaults.getBookDetailsCustomFieldsAlways() : b;
  }
  public void setBookDetailsCustomFieldsAlways(Boolean value) {
    setProperty(PROPERTY_NAME_BookDetailsCustomFieldsAlways, value);
  }

  public Boolean getMinimizeChangedFiles() {
    Boolean b = getBoolean(PROPERTY_NAME_MINIMIZECHANGEDFILES);
    return (b == null) ? defaults.getMinimizeChangedFiles() : b;
  }
  public void setMinimizeChangedFiles(Boolean minimizeChangedFiles) {
    setProperty(PROPERTY_NAME_MINIMIZECHANGEDFILES, minimizeChangedFiles);
  }

  public Boolean isOnlyCatalogAtTargetReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ONLY_CATALOG_AT_TARGRET);
  }
  public Boolean getOnlyCatalogAtTarget() {
    Boolean b = getBoolean(PROPERTY_NAME_ONLY_CATALOG_AT_TARGRET);
    return (b == null) ? defaults.getOnlyCatalogAtTarget() : b;
  }
  public void setOnlyCatalogAtTarget(Boolean value) {
    setProperty(PROPERTY_NAME_ONLY_CATALOG_AT_TARGRET, value);
  }

  public Boolean isExternalIconsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_EXTERNALICONS);
  }
  public Boolean getExternalIcons() {
    Boolean b = getBoolean(PROPERTY_NAME_EXTERNALICONS);
    return (b == null) ? defaults.getExternalIcons() : b;
  }
  public void setExternalIcons(Boolean externalIcons) {
    setProperty(PROPERTY_NAME_EXTERNALICONS, externalIcons);
  }

  public Boolean isExternalImagesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_EXTERNAL_IMAGES);
  }
  public Boolean getExternalImages() {
    Boolean b = getBoolean(PROPERTY_NAME_EXTERNAL_IMAGES);
    return (b == null) ? defaults.getExternalImages() : b;
  }
  public void setExternalImages(Boolean b) {
    setProperty(PROPERTY_NAME_EXTERNAL_IMAGES, b);
  }

  public Boolean isGenerateOpdsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEOPDS);
  }
  public Boolean getGenerateOpds() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEOPDS);
    return (b == null) ? defaults.getGenerateOpds() : b;
  }
  public void setGenerateOpds(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATEOPDS, value);
  }

  public Boolean isGenerateHtmlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEHTML);
  }
  public Boolean getGenerateHtml() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEHTML);
    return (b == null) ? defaults.getGenerateHtml() : b;
  }
  public void setGenerateHtml(Boolean getGenerateHtml) {
    setProperty(PROPERTY_NAME_GENERATEHTML, getGenerateHtml);
  }

  public Boolean isGenerateOpdsDownloadsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEOPDSDOWNLOADS);
  }
  public Boolean getGenerateOpdsDownloads() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEOPDSDOWNLOADS);
    return (b == null) ? defaults.getGenerateOpdsDownloads() : b;
  }
  public void setGenerateOpdsDownloads(Boolean getGenerateOpdsDownloads) {
    setProperty(PROPERTY_NAME_GENERATEOPDSDOWNLOADS, getGenerateOpdsDownloads);
  }

  public Boolean isGenerateHtmlDownloadsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEHTMLDOWNLOADS);
  }
  public Boolean getGenerateHtmlDownloads() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEHTMLDOWNLOADS);
    return (b == null) ? defaults.getGenerateHtmlDownloads() : b;
  }
  public void setGenerateHtmlDownloads(Boolean getGenerateHtmlDownloads) {
    setProperty(PROPERTY_NAME_GENERATEHTMLDOWNLOADS, getGenerateHtmlDownloads);
  }

  public void setPublishedDateAsYear(Boolean value) {
    setProperty(PROPERTY_NAME_PUBLISHEDDATEASYEAR, value);
  }
  public Boolean isPublishedDateAsYearReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_PUBLISHEDDATEASYEAR);
  }
  public Boolean getPublishedDateAsYear() {
    Boolean b = getBoolean(PROPERTY_NAME_PUBLISHEDDATEASYEAR);
    return (b == null) ? defaults.getPublishedDateAsYear() : b;
  }

  public void setLanguageAsTag(Boolean value) {
    setProperty(PROPERTY_NAME_LANGUAGEASTAG, value);
  }
  public Boolean isLanguageAsTagReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LANGUAGEASTAG);
  }
  public Boolean getLanguageAsTag() {
    Boolean b = getBoolean(PROPERTY_NAME_LANGUAGEASTAG);
    return (b == null) ? defaults.getLanguageAsTag() : b;
  }

  public void setSuppressRatingsInTitles(Boolean supressRatingsInTitles) {
    setProperty(PROPERTY_NAME_SUPRESSRATINGSINTITLES, supressRatingsInTitles);
  }
  public Boolean isSupressRatingsInTitlesReadyOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SUPRESSRATINGSINTITLES);
  }
  public Boolean getSuppressRatingsInTitles() {
    Boolean b = getBoolean(PROPERTY_NAME_SUPRESSRATINGSINTITLES);
    return (b == null) ? defaults.getSuppressRatingsInTitles() : b;
  }

  public Boolean isThumbnailGenerateReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_THUMBNAILGENERATE);
  }
  public Boolean getThumbnailGenerate() {
    Boolean b = getBoolean(PROPERTY_NAME_THUMBNAILGENERATE);
    return (b == null) ? defaults.getThumbnailGenerate() : b;
  }
  public void setThumbnailGenerate(Boolean thumbnailGenerate) {
    setProperty(PROPERTY_NAME_THUMBNAILGENERATE, thumbnailGenerate);
  }

  public Boolean isThumbnailHeightReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_THUMBNAILHEIGHT);
  }
  public Integer getThumbnailHeight() {
    Integer i = getInteger(PROPERTY_NAME_THUMBNAILHEIGHT);
    return (i == null) ? defaults.getThumbnailHeight() : i;
  }
  public void setThumbnailHeight(Integer thumbnailHeight) {
    setProperty(PROPERTY_NAME_THUMBNAILHEIGHT, thumbnailHeight);
  }

  public Boolean isSplitTagsOnReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITTAGSON);
  }
  public String getSplitTagsOn() {
    String s = getProperty(PROPERTY_NAME_SPLITTAGSON);
    return (s == null) ? defaults.getSplitTagsOn() : s;
  }
  public void setSplitTagsOn(String splitTagsOn) {
    setProperty(PROPERTY_NAME_SPLITTAGSON, splitTagsOn);
  }

  public Boolean isDontSplitTagsOnReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_NO_SPLITTAGSON);
  }
  public Boolean getDontSplitTagsOn() {
    Boolean b = getBoolean(PROPERTY_NAME_NO_SPLITTAGSON);
    return (b == null) ? defaults.getDontSplitTagsOn() : b;
  }
  public void setDontSplitTagsOn(Boolean dontSplitTagsOn) {
    setProperty(PROPERTY_NAME_NO_SPLITTAGSON, dontSplitTagsOn);
  }

  public Boolean isIncludeBooksWithNoFileReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE);
  }
  public Boolean getIncludeBooksWithNoFile() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE);
    return (b == null) ? defaults.getIncludeBooksWithNoFile() : b;
  }
  public void setIncludeBooksWithNoFile(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEBOOKSWITHNOFILE, value);
  }

  public Boolean isCryptFilenamesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CRYPTFILENAMES);
  }
  public Boolean getCryptFilenames() {
    Boolean b = getBoolean(PROPERTY_NAME_CRYPTFILENAMES);
    return (b == null) ? defaults.getCryptFilenames() : b;
  }
  public void setCryptFilenames(Boolean value) {
    setProperty(PROPERTY_NAME_CRYPTFILENAMES, value);
  }

  public Boolean isShowSeriesInAuthorCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG);
  }
  public Boolean getShowSeriesInAuthorCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG);
    return (b == null) ? defaults.getShowSeriesInAuthorCatalog() : b;
  }
  public void setShowSeriesInAuthorCatalog(Boolean value) {
    setProperty(PROPERTY_NAME_SHOWSERIESINAUTHORCATALOG, value);
  }

  public Boolean isCatalogFilterReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CATALOGFILTER);
  }
  public String getCatalogFilter() {
    String s = getProperty(PROPERTY_NAME_CATALOGFILTER);
    return (s == null) ? defaults.getCatalogFilter() : s;
  }
  public void setCatalogFilter(String value) {
    setProperty(PROPERTY_NAME_CATALOGFILTER, value);
  }

  public Boolean isMaxSummaryLengthReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXSUMMARYLENGTH);
  }
  public Integer getMaxSummaryLength() {
    Integer i = getInteger(PROPERTY_NAME_MAXSUMMARYLENGTH);
    return (i == null) ? defaults.getMaxSummaryLength() : i;
  }
  public void setMaxSummaryLength(Integer value) {
    setProperty(PROPERTY_NAME_MAXSUMMARYLENGTH, value);
  }

  public void setMaxBookSummaryLength(Integer value) {
    setProperty(PROPERTY_NAME_MAXBOOKSUMMARYLENGTH, value);
  }
  public Boolean isMaxBookSummaryLengthReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXBOOKSUMMARYLENGTH);
  }
  public Integer getMaxBookSummaryLength() {
    Integer i = getInteger(PROPERTY_NAME_MAXBOOKSUMMARYLENGTH);
    return (i == null) ? defaults.getMaxBookSummaryLength() : i;
  }

  public Boolean isGenerateAuthorsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEAUTHORS);
  }
  public Boolean getGenerateAuthors() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEAUTHORS);
    return (b == null) ? defaults.getGenerateAuthors() : b;
  }
  public void setGenerateAuthors(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATEAUTHORS, value);
  }

  public Boolean isGenerateTagsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATETAGS);
  }
  public Boolean getGenerateTags() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATETAGS);
    return (b == null) ? defaults.getGenerateTags() : b;
  }
  public void setGenerateTags(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATETAGS, value);
  }

  public Boolean isGenerateSeriesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATESERIES);
  }
  public Boolean getGenerateSeries() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATESERIES);
    return (b == null) ? defaults.getGenerateSeries() : b;
  }
  public void setGenerateSeries(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATESERIES, value);
  }

  public Boolean isGenerateRecentReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATERECENT);
  }
  public Boolean getGenerateRecent() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATERECENT);
    return (b == null) ? defaults.getGenerateRecent() : b;
  }
  public void setGenerateRecent(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATERECENT, value);
  }

  public Boolean isGenerateRatingsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATERATINGS);
  }
  public Boolean getGenerateRatings() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATERATINGS);
    return (b == null) ? defaults.getGenerateRatings() : b;
  }
  public void setGenerateRatings(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATERATINGS, value);
  }

  public Boolean isGenerateAllbooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEALLBOOKS);
  }
  public Boolean getGenerateAllbooks() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEALLBOOKS);
    return (b == null) ? defaults.getGenerateAllbooks() : b;
  }
  public void setGenerateAllbooks(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATEALLBOOKS, value);
  }

  public Boolean isGenerateIndexReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GENERATEALLBOOKS);
  }
  public Boolean getGenerateIndex() {
    Boolean b = getBoolean(PROPERTY_NAME_GENERATEINDEX);
    return (b == null) ? defaults.getGenerateAllbooks() : b;
  }
  public void setGenerateIndex(Boolean value) {
    setProperty(PROPERTY_NAME_GENERATEINDEX, value);
  }

  public Boolean isCopyToDatabaseFolderReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COPYTODATABASEFOLDER);
  }

  public Boolean getCopyToDatabaseFolder() {
    Boolean b = getBoolean(PROPERTY_NAME_COPYTODATABASEFOLDER);
    return (b == null)? defaults.getCopyToDatabaseFolder() : b;
  }
  public void setCopyToDatabaseFolder(Boolean value) {
    setProperty(PROPERTY_NAME_COPYTODATABASEFOLDER, value);
  }

  public Boolean isBrowseByCoverReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BROWSEBYCOVER);
  }
  public Boolean getBrowseByCover() {
    Boolean b = getBoolean(PROPERTY_NAME_BROWSEBYCOVER);
    return (b == null) ? defaults.getBrowseByCover() : b;
  }

  public void setBrowseByCover(Boolean value) {
    setProperty(PROPERTY_NAME_BROWSEBYCOVER, value);
  }
  public Boolean isSplitByAuthorInitialGoToBooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK);
  }
  public Boolean getSplitByAuthorInitialGoToBooks() {
    Boolean b = getBoolean(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK);
    return (b == null) ? defaults.getSplitByAuthorInitialGoToBooks() : b;
  }

  public void setSplitByAuthorInitialGoToBooks(Boolean value) {
    setProperty(PROPERTY_NAME_SPLITBYAUTHORINITIALGOTOBOOK, value);
  }
  public Boolean isIncludeAboutLinkReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEABOUTLINK);
  }
  public Boolean getIncludeAboutLink() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEABOUTLINK);
    return (b == null) ? defaults.getIncludeAboutLink() : b;
  }

  public void setIncludeAboutLink(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEABOUTLINK, value);
  }
  public Boolean isTagsToIgnoreReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TAGSTOIGNORE);
  }
  public List<String> getRegExTagsToIgnore() {
    if (regexTagsToIgnore == null) {
      regexTagsToIgnore = new LinkedList<String>();
      for (String tagName : Helper.tokenize(getTagsToIgnore().toUpperCase(), ",", true)) {
        regexTagsToIgnore.add(Helper.convertGlobToRegEx(tagName));
      }
    }
    return regexTagsToIgnore;
  }

  public Boolean isTagsToMakeDeepReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TAGSTOMAKEDEEP);
  }
  public String getTagsToIgnore() {
    String s = getProperty(PROPERTY_NAME_TAGSTOIGNORE);
    return (s == null) ? defaults.getTagsToIgnore() : s;
  }
  public void setTagsToIgnore(String value) {
    setProperty(PROPERTY_NAME_TAGSTOIGNORE, value);
    regexTagsToIgnore = null;
  }

  public List<String> getTokenizedTagsToMakeDeep() {
    if (tokenizedTagsToMakeDeep == null) {
      tokenizedTagsToMakeDeep = Helper.tokenize(getTagsToMakeDeep().toUpperCase(), ",", true);
    }
    return tokenizedTagsToMakeDeep;
  }

  public List<String> getTokenizedBookDetailsCustomColumns() {
    if (tokenizedBookDetailsCustomColumns == null) {
      tokenizedBookDetailsCustomColumns = Helper.tokenize(getBookDetailsCustomFields(), ",", true);
    }
    return tokenizedBookDetailsCustomColumns;
  }

  /**
   * Get the full list of custom column names that have been asked for.
   * This can be used both for checking if they exist on the database,
   * and subsequently we can load only the ones that are required.
   *
   * @return
   */
  public List<String> getCustomColumnsWanted() {
    List<String> result = getTokenizedBookDetailsCustomColumns();
    result.addAll(getTokenizedCatalogCustomColumns());
    return result;
  }

  public String getTagsToMakeDeep() {
    String s = getProperty(PROPERTY_NAME_TAGSTOMAKEDEEP);
    return (s == null) ? defaults.getTagsToMakeDeep() : s;
  }
  public void setTagsToMakeDeep(String value) {
    setProperty(PROPERTY_NAME_TAGSTOMAKEDEEP, value);
    tokenizedTagsToMakeDeep = null;
  }

  public Boolean isMinBooksToMakeDeepLevelReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL);
  }
  public Integer getMinBooksToMakeDeepLevel() {
    Integer i = getInteger(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL);
    return (i == null) ? defaults.getMinBooksToMakeDeepLevel() : i;
  }
  public void setMinBooksToMakeDeepLevel(Integer value) {
    setProperty(PROPERTY_NAME_MINBOOKSTOMAKEDEEPLEVEL, value);
  }

  public Boolean isBrowseByCoverWithoutSplitReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT);
  }
  public Boolean getBrowseByCoverWithoutSplit() {
    Boolean b = getBoolean(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT);
    return (b == null) ? defaults.getBrowseByCoverWithoutSplit() : b;
  }
  public void setBrowseByCoverWithoutSplit(Boolean value) {
    setProperty(PROPERTY_NAME_BROWSEBYCOVERWITHOUTSPLIT, value);
  }

  public Boolean isCoverHeightReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_COVERHEIGHT);
  }
  public Integer getCoverHeight() {
    Integer i = getInteger(PROPERTY_NAME_COVERHEIGHT);
    return (i == null) ? defaults.getCoverHeight() : i;
  }

  public void setCoverHeight(Integer value) {
    setProperty(PROPERTY_NAME_COVERHEIGHT, value);
  }

  public Boolean isIncludeOnlyOneFileReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDEONLYONEFILE);
  }
  public Boolean getIncludeOnlyOneFile() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEONLYONEFILE);
    return (b == null) ? defaults.getIncludeOnlyOneFile() : b;
  }
  public void setIncludeOnlyOneFile(Boolean value) {
    setProperty(PROPERTY_NAME_ZIPTROOKCATALOG, value);
  }

  public Boolean isZipTrookCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ZIPTROOKCATALOG);
  }
  public Boolean getZipTrookCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_ZIPTROOKCATALOG);
    return (b == null) ? defaults.getZipTrookCatalog() : b;
  }
  public void setZipTrookCatalog(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEONLYONEFILE, value);
  }

  public Boolean isReprocessEpubMetadataReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_REPROCESSEPUBMETADATA);
  }
  public Boolean getReprocessEpubMetadata() {
    Boolean b = getBoolean(PROPERTY_NAME_REPROCESSEPUBMETADATA);
    return (b == null) ? defaults.getReprocessEpubMetadata() : b;
  }
  public void setReprocessEpubMetadata(Boolean value) {
    setProperty(PROPERTY_NAME_REPROCESSEPUBMETADATA, value);
  }

  public Boolean isOrderAllBooksBySeriesReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ORDERALLBOOKSBYSERIES);
  }
  public Boolean getOrderAllBooksBySeries() {
    Boolean b = getBoolean(PROPERTY_NAME_ORDERALLBOOKSBYSERIES);
    return (b == null) ? defaults.getOrderAllBooksBySeries() : b;
  }
  public void setOrderAllBooksBySeries(Boolean value) {
    setProperty(PROPERTY_NAME_ORDERALLBOOKSBYSERIES, value);
  }

  /* external links */

  public Boolean isWikipediaUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_WIKIPEDIAURL);
  }
  public String getWikipediaUrl() {
    String s = getProperty(PROPERTY_NAME_WIKIPEDIAURL);
    return (s == null) ? defaults.getWikipediaUrl() : s;
  }
  public void setWikipediaUrl(String value) {
    setProperty(PROPERTY_NAME_WIKIPEDIAURL, value);
  }

  public Boolean isAmazonAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONAUTHORURL);
  }
  public String getAmazonAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONAUTHORURL);
    return (s == null) ? defaults.getAmazonAuthorUrl() : s;
  }
  public void setAmazonAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONAUTHORURL, value);
  }

  public Boolean isAmazonIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONISBNURL);
  }
  public String getAmazonIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONISBNURL);
    return (s == null) ? defaults.getAmazonIsbnUrl() : s;
  }
  public void setAmazonIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONISBNURL, value);
  }

  public Boolean isAmazonTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_AMAZONTITLEURL);
  }
  public String getAmazonTitleUrl() {
    String s = getProperty(PROPERTY_NAME_AMAZONTITLEURL);
    return (s == null) ? defaults.getAmazonTitleUrl() : s;
  }
  public void setAmazonTitleUrl(String value) {
    setProperty(PROPERTY_NAME_AMAZONTITLEURL, value);
  }

  public Boolean isGoodreadAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADAUTHORURL);
  }
  public String getGoodreadAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADAUTHORURL);
    return (s == null) ? defaults.getGoodreadAuthorUrl() : s;
  }
  public void setGoodreadAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADAUTHORURL, value);
  }

  public Boolean isGoodreadIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADISBNURL);
  }
  public String getGoodreadIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADISBNURL);
    return (s == null) ? defaults.getGoodreadIsbnUrl() : s;
  }
  public void setGoodreadIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADISBNURL, value);
  }

  public Boolean isGoodreadTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADTITLEURL);
  }
  public String getGoodreadTitleUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADTITLEURL);
    return (s == null) ? defaults.getGoodreadTitleUrl() : s;
  }
  public void setGoodreadTitleUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADTITLEURL, value);
  }

  public Boolean isGoodreadReviewIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GOODREADREVIEWISBNURL);
  }
  public String getGoodreadReviewIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_GOODREADREVIEWISBNURL);
    return (s == null) ? defaults.getGoodreadReviewIsbnUrl() : s;
  }
  public void setGoodreadReviewIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_GOODREADREVIEWISBNURL, value);
  }


  public Boolean isIsfdbAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ISFDBAUTHORURL);
  }
  public String getIsfdbAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_ISFDBAUTHORURL);
    return (s == null) ? defaults.getIsfdbAuthorUrl() : s;
  }
  public void setIsfdbAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_ISFDBAUTHORURL, value);
  }

  public Boolean isLibrarythingAuthorUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGAUTHORURL);
  }
  public String getLibrarythingAuthorUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGAUTHORURL);
    return (s == null) ? defaults.getLibrarythingAuthorUrl() : s;
  }
  public void setLibrarythingAuthorUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGAUTHORURL, value);
  }

  public Boolean isLibrarythingIsbnUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGISBNURL);
  }
  public String getLibrarythingIsbnUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGISBNURL);
    return (s == null) ? defaults.getLibrarythingIsbnUrl() : s;
  }
  public void setLibrarythingIsbnUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGISBNURL, value);
  }

  public Boolean isLibrarythingTitleUrlReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_LIBRARYTHINGTITLEURL);
  }
  public String getLibrarythingTitleUrl() {
    String s = getProperty(PROPERTY_NAME_LIBRARYTHINGTITLEURL);
    return (s == null) ? defaults.getLibrarythingTitleUrl() : s;
  }
  public void setLibrarythingTitleUrl(String value) {
    setProperty(PROPERTY_NAME_LIBRARYTHINGTITLEURL, value);
  }

  public Boolean isIndexFilterAlgorithmReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INDEXFILTERALGORITHM);
  }
  public Index.FilterHintType getIndexFilterAlgorithm() {
    String s = getProperty(PROPERTY_NAME_INDEXFILTERALGORITHM);
    return (s == null) ? defaults.getIndexFilterAlgorithm() :Index.FilterHintType.valueOf(s);
  }
  public void setIndexFilterAlgorithm(Index.FilterHintType value) {
    setProperty(PROPERTY_NAME_INDEXFILTERALGORITHM, value);
  }

  public Boolean getIndexComments() {
    Boolean b = getBoolean(PROPERTY_NAME_INDEXCOMMENTS);
    return (b == null) ? defaults.getIndexComments() : b;
  }

  public Boolean isIndexCommentsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INDEXCOMMENTS);
  }
  public void setIndexComments(Boolean value) {
    setProperty(PROPERTY_NAME_INDEXCOMMENTS, value);
  }

  public Integer getMaxKeywords() {
    Integer i = getInteger(PROPERTY_NAME_MAXKEYWORDS);
    return (i == null) ?defaults.getMaxKeywords() : i;
  }

  public void setMaxKeywords(Integer value) {
    setProperty(PROPERTY_NAME_MAXKEYWORDS, value);
  }

  public Boolean isMaxKeywordsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_MAXKEYWORDS);
  }

  public Boolean isUrlBooksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_URLBOOKS);
  }

  /**
   * Get the Base URL for this library
   * If set it is always returned with the trailing '/' present
   * @return
   */
  public String getUrlBooks() {
    String s = getProperty(PROPERTY_NAME_URLBOOKS);
    if (s == null) {
      s = defaults.getUrlBooks();
    } else {
      if (s.length() > 0 && (! s.endsWith(Constants.FOLDER_SEPARATOR))) {
        s+= Constants.FOLDER_SEPARATOR;
      }
      // Ignore a simple / as the base Url
      if (s.equals(Constants.FOLDER_SEPARATOR)) {
        s = "";
      }
    }
    return s;
  }

  public void setUrlBooks(String value) {
    // correct the value if needed
    if (Helper.isNotNullOrEmpty(value) && value.charAt(value.length() - 1) != '/')
      value += "/";
    setProperty(PROPERTY_NAME_URLBOOKS, value);
  }

  public Boolean isFeaturedCatalogSavedSearchNameReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_FEATUREDCATALOGSAVEDSEARCHNAME);
  }

  public String getFeaturedCatalogSavedSearchName() {
    String s = getProperty(PROPERTY_NAME_FEATUREDCATALOGSAVEDSEARCHNAME);
    if (s == null)
      return defaults.getFeaturedCatalogSavedSearchName();
    else
      return s;
  }

  public void setFeaturedCatalogSavedSearchName(String value) {
    setProperty(PROPERTY_NAME_FEATUREDCATALOGSAVEDSEARCHNAME, value);
  }

  public Boolean isFeaturedCatalogTitleReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_FEATUREDCATALOGTITLE);
  }

  public String getFeaturedCatalogTitle() {
    String s = getProperty(PROPERTY_NAME_FEATUREDCATALOGTITLE);
    if (s == null)
      return defaults.getFeaturedCatalogTitle();
    else
      return s;
  }

  public void setFeaturedCatalogTitle(String value) {
    setProperty(PROPERTY_NAME_FEATUREDCATALOGTITLE, value);
  }

  public Boolean isCustomCatalogsReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_CUSTOMCATALOGS);
  }

  public List<CustomCatalogEntry> getCustomCatalogs() {
    List<CustomCatalogEntry> result = new LinkedList<CustomCatalogEntry>();
    String s = getProperty(PROPERTY_NAME_CUSTOMCATALOGS);
    try {
      if (Helper.isNotNullOrEmpty(s)) {
        Matcher matcher = PATTERN_CUSTOMCATALOG.matcher(s);
        while (matcher.find()) {
          String string = matcher.group();
          Matcher titleMatcher = PATTERN_CUSTOMCATALOG_TITLE.matcher(string);
          String title = titleMatcher.find() ? titleMatcher.group() : null;
          Matcher searchMatcher = PATTERN_CUSTOMCATALOG_SEARCH.matcher(string);
          String search = searchMatcher.find() ? searchMatcher.group() : null;
          Matcher atTopMatcher = PATTERN_CUSTOMCATALOG_ATTOP.matcher(string);
          String atTopString = atTopMatcher.find() ? atTopMatcher.group() : null;
          if (Helper.isNotNullOrEmpty(title) && Helper.isNotNullOrEmpty(search)) {
            title = title.substring(7, title.length() - 8);
            search = search.substring(8, search.length() - 9);
            Boolean atTop = false;
            if (Helper.isNotNullOrEmpty(atTopString)) {
              atTopString = atTopString.substring(7, atTopString.length() - 8);
              atTop = atTopString.equals("true") ? true : false;
            }
            result.add(new CustomCatalogEntry(title, search, atTop));
          }
        }
      }
    } catch (RuntimeException e) {
      logger.warn("error while decoding custom catalogs : " + s, e);

    }
    return result;
  }

  public void setCustomCatalogs(List<CustomCatalogEntry> values) {
    String s;
    StringBuffer mainsb = new StringBuffer();
    if (Helper.isNotNullOrEmpty(values)) {
      for ( CustomCatalogEntry value : values) {
        StringBuffer sb = new StringBuffer();
        sb.append("[" + PATTERN_CUSTOMCATALOG_ID + "]");
        sb.append("[" + PATTERN_CUSTOMCATALOG_TITLE_ID + "]");
        sb.append(value.getLabel() == null ? "" : value.getLabel());
        sb.append("[/" + PATTERN_CUSTOMCATALOG_TITLE_ID + "]");
        sb.append("[" + PATTERN_CUSTOMCATALOG_SEARCH_ID + "]");
        sb.append(value.getValue() == null ? "" : value.getValue());
        sb.append("[/" + PATTERN_CUSTOMCATALOG_SEARCH_ID + "]");
        sb.append("[" + PATTERN_CUSTOMCATALOG_ATTOP_ID + "]");
        sb.append(value.getAtTop().toString());
        sb.append("[/" + PATTERN_CUSTOMCATALOG_ATTOP_ID + "]");
        sb.append("[/" + PATTERN_CUSTOMCATALOG_ID + "]");
        mainsb.append(sb.toString());
      }
      s = mainsb.toString();
    } else s="";
    setProperty(PROPERTY_NAME_CUSTOMCATALOGS, s);
  }

  public String getCatalogCustomColumns() {
    String s = getProperty(PROPERTY_NAME_CATALOGCUSTOMCOLUMNS);
    return (s == null) ? defaults.getCatalogCustomColumns() : s;
  }
  public void setCatalogCustomColumns(String value) {
    setProperty(PROPERTY_NAME_CATALOGCUSTOMCOLUMNS, value);
  }

  public List<String> getTokenizedCatalogCustomColumns() {
    if (tokenizedCatalogCustomColumns == null) {
      tokenizedCatalogCustomColumns = Helper.tokenize(getCatalogCustomColumns().toUpperCase(), ",", true);
    }
    return tokenizedCatalogCustomColumns;
  }

  /*
    Catalog Structure
   */

  public Boolean getGenerateExternalLinks() {
    Boolean b = getBoolean(PROPERTY_NAME_GenerateExternalLinks);
    return (b == null) ? defaults.getGenerateExternalLinks() : b;
  }
  public void setGenerateExternalLinks(Boolean value) {
    setProperty(PROPERTY_NAME_GenerateExternalLinks, value);
  }

  public Boolean isGenerateCrossLinksReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_GenerateCrossLinks);
  }
  public Boolean getGenerateCrossLinks() {
    Boolean b = getBoolean(PROPERTY_NAME_GenerateCrossLinks);
    return (b == null) ? defaults.getGenerateCrossLinks() : b;
  }
  public void setGenerateCrossLinks(Boolean value) {
    setProperty(PROPERTY_NAME_GenerateCrossLinks, value);
  }

  public Boolean getSingleBookCrossReferences() {
    Boolean b = getBoolean(PROPERTY_NAME_SingleBookCrossReferences);
    return (b == null) ? defaults.getGenerateCrossLinks() : b;
  }
  public void setSingleBookCrossReferences(Boolean value) {
    setProperty(PROPERTY_NAME_SingleBookCrossReferences, value);
  }

  public Boolean getIncludeAuthorCrossReferences() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeAuthorCrossReferences);
    return (b == null) ? defaults.getGenerateCrossLinks() : b;
  }
  public void setIncludeAuthorCrossReferences(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeAuthorCrossReferences, value);
  }

  public Boolean getIncludeSerieCrossReferences() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeSerieCrossReferences);
    return (b == null) ? defaults.getGenerateCrossLinks() : b;
  }
  public void setIncludeSerieCrossReferences(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeSerieCrossReferences, value);
  }

  public Boolean getIncludeTagCrossReferences() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeTagCrossReferences);
    return (b == null) ? defaults.getIncludeTagCrossReferences() : b;
  }
  public void setIncludeTagCrossReferences(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeTagCrossReferences, value);
  }

  public Boolean getIncludeRatingCrossReferences() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeRatingCrossReferences);
    return (b == null) ? defaults.getIncludeTagCrossReferences() : b;
  }
  public void setIncludeRatingCrossReferences(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeRatingCrossReferences, value);
  }

  public Boolean getDisplayAuthorSort() {
    Boolean b = getBoolean(PROPERTY_NAME_DisplayAuthorSort);
    return (b == null) ? defaults.getDisplayAuthorSort() : b;
  }
  public void setDisplayAuthorSort(Boolean value) { setProperty(PROPERTY_NAME_DisplayAuthorSort, value); }

  public Boolean getDisplayTitleSort() {
    Boolean b = getBoolean(PROPERTY_NAME_DisplayTitleSort);
    return (b == null) ? defaults.getDisplayTitleSort() : b;
  }
  public void setDisplayTitleSort(Boolean value) {
    setProperty(PROPERTY_NAME_DisplayTitleSort, value);
  }

  public Boolean getDisplaySeriesSort() {
    Boolean b = getBoolean(PROPERTY_NAME_DisplaySeriesSort);
    return (b == null) ? defaults.getDisplaySeriesSort() : b;
  }
  public void setDisplaySeriesSort(Boolean value) {
    setProperty(PROPERTY_NAME_DisplaySeriesSort, value);
  }

  public Boolean isSortUsingAuthorReadOnly() { return isPropertyReadOnly(PROPERTY_NAME_SortUsingAuthor); }
  public Boolean getSortUsingAuthor() {
    Boolean b = getBoolean(PROPERTY_NAME_SortUsingAuthor);
    return (b == null) ? defaults.getSortUsingAuthor() : b;
  }

  public Boolean isSortTagsByAuthorReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_SortTagsByAuthor);
  }
  public Boolean getSortTagsByAuthor() {
    Boolean b = getBoolean(PROPERTY_NAME_SortTagsByAuthor);
    return (b == null) ? defaults.getSortTagsByAuthor() : b;
  }
  public void setSortUsingAuthor(Boolean value) {
    setProperty(PROPERTY_NAME_SortUsingAuthor, value);
  }

  public Boolean isTagBooksNoSplitReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_TagBooksNoSplit);
  }
  public Boolean getTagBooksNoSplit() {
    Boolean b = getBoolean(PROPERTY_NAME_TagBooksNoSplit);
    return (b == null) ? defaults.getTagBooksNoSplit() : b;
  }
  public void setTagBooksNoSplit(Boolean value) {
    setProperty(PROPERTY_NAME_TagBooksNoSplit, value);
  }

  public void setSortTagsByAuthor(Boolean value) {
    setProperty(PROPERTY_NAME_SortTagsByAuthor, value);
  }

  public Boolean getSortUsingTitle() {
    Boolean b = getBoolean(PROPERTY_NAME_SortUsingTitle);
    return (b == null) ? defaults.getSortUsingTitle() : b;
  }
  public void setSortUsingTitle(Boolean value) {
    setProperty(PROPERTY_NAME_SortUsingTitle, value);
  }

  public Boolean getSortUsingSeries() {
    Boolean b = getBoolean(PROPERTY_NAME_SortUsingSeries);
    return (b == null) ? defaults.getSortUsingSeries() : b;
  }
  public void setSortUsingTseries(Boolean value) {
    setProperty(PROPERTY_NAME_SortUsingSeries, value);
  }

  // public Boolean isSortUsingTitleReadOnly() {
  //  return isPropertyReadOnly(PROPERTY_NAME_SortUsingTitle);
  //}

  //  Book Details

  public Boolean getIncludeSeriesInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDESERIESINBOOKDETAILS);
    return (b == null) ? defaults.getIncludeSeriesInBookDetails() : b;
  }
  public void setIncludeSeriesInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDESERIESINBOOKDETAILS, value);
  }

  public Boolean getIncludeRatingInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDERATINGINBOOKDETAILS);
    return (b == null) ? defaults.getIncludeRatingInBookDetails() : b;
  }
  public void setIncludeRatingInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDERATINGINBOOKDETAILS, value);
  }

  public Boolean getIncludeTagsInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDETAGSINBOOKDETAILS);
    return (b == null) ? defaults.getIncludeTagsInBookDetails() : b;
  }
  public void setIncludeTagsInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDETAGSINBOOKDETAILS, value);
  }

  public Boolean getIncludePublisherInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEPUBLISHERINBOOKDETAILS);
    return (b == null) ? defaults.getIncludePublisherInBookDetails() : b;
  }
  public void setIncludePublisherInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEPUBLISHERINBOOKDETAILS, value);
  }

  public Boolean isIncludeCoversInCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_INCLUDE_COVERS_IN_CATALOG);
  }
  public Boolean getIncludeCoversInCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDE_COVERS_IN_CATALOG);
    return (b == null) ? defaults.getIncludeCoversInCatalog() : b;
  }
  public void setIncludeCoversInCatalog(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDE_COVERS_IN_CATALOG, value);
  }

  public Boolean isUseThumbnailsAsCoversReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_USE_THUMBNAILS_AS_COVERS);
  }
  public Boolean getUseThumbnailsAsCovers() {
    Boolean b = getBoolean(PROPERTY_NAME_USE_THUMBNAILS_AS_COVERS);
    return (b == null) ? defaults.getUseThumbnailsAsCovers() : b;
  }
  public void setUseThumbnailsAsCovers(Boolean value) {
    setProperty(PROPERTY_NAME_USE_THUMBNAILS_AS_COVERS, value);
  }

  public Boolean isZipCatalogReadOnly() {
    return isPropertyReadOnly(PROPERTY_NAME_ZIP_CATALOG);
  }
  public Boolean getZipCatalog() {
    Boolean b = getBoolean(PROPERTY_NAME_ZIP_CATALOG);
    return (b == null) ? defaults.getZipCatalog() : b;
  }
  public void setZipCatalog(Boolean value) {
    setProperty(PROPERTY_NAME_ZIP_CATALOG, value);
  }

  public Boolean getZipOmitXml() {
    Boolean b = getBoolean(PROPERTY_NAME_ZIP_OMIT_XML);
    return (b == null) ?defaults.getZipOmitXml() : b;
  }
  public void setZipOmitXml(Boolean value) {
    setProperty(PROPERTY_NAME_ZIP_OMIT_XML, value);
  }

  public Boolean getIncludePublishedInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_INCLUDEPUBLISHEDINBOOKDETAILS);
    return (b == null) ? defaults.getIncludePublishedInBookDetails() : b;
  }
  public void setIncludePublishedInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_INCLUDEPUBLISHEDINBOOKDETAILS, value);
  }

  public Boolean getIncludeAddedInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeAddedInBookDetailst);
    return (b == null) ? defaults.getIncludeAddedInBookDetails() : b;
  }
  public void setIncludeAddedInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeAddedInBookDetailst, value);
  }

  public Boolean getIncludeModifiedInBookDetails() {
    Boolean b = getBoolean(PROPERTY_NAME_IncludeModifiedInBookDetailst);
    return (b == null) ? defaults.getIncludeModifiedInBookDetails() : b;
  }
  public void setIncludeModifiedInBookDetails(Boolean value) {
    setProperty(PROPERTY_NAME_IncludeModifiedInBookDetailst, value);
  }

  public String getSecurityCode() {
    String s = getProperty(PROPERTY_NAME_SecurityCode);
    return Helper.isNullOrEmpty(s) ? defaults.getSecurityCode() : s;
  }
  public void setSecurityCode(String code) {
    setProperty(PROPERTY_NAME_SecurityCode, code);
  }

}
