package com.gmail.dpierron.calibre.configuration;


import com.gmail.dpierron.calibre.opds.indexer.Index;

public class StanzaConstants {

  // options default values
  public final static int MAX_BEFORE_PAGINATE = 25;
  public final static int MAX_BEFORE_SPLIT = 3 * MAX_BEFORE_PAGINATE;
  public final static int MAX_RECENT_ADDITIONS = 99999;
  public final static String CATALOGFOLDER = "_catalog";
  public final static String CATALOGTITLE = "Calibre library";
  public final static String WIKIPEDIA_LANGUAGE = "en";
  public final static String INCLUDEDFORMATS = "EPUB, PDF, RTF, TXT, PRC, PDB, MOBI, LRF, LRX, FB2";
  public final static boolean GENERATEOPDS = true;
  public final static boolean GENERATEHTML = true;
  public final static boolean GENERATEOPDSDOWNLOADS = true;
  public final static boolean GENERATEHTMLDOWNLOADS = true;
  public final static boolean GENERATEDOWNLOADS = true;
  public final static boolean SAVEBANDWIDTH = true;
  public final static boolean MINIMIZECHANGEDFILES = true;
  public final static boolean EXTERNALICONS = false;
  public final static int THUMBNAIL_HEIGHT = 144;
  public final static boolean THUMBNAIL_GENERATE = true;
  public final static String SPLITTAGSON = "";
  public final static boolean INCLUDEBOOKSWITHNOFILE = false;
  public final static boolean CRYPT_FILENAMES = false;
  public final static boolean SHOWSERIESINAUTHORCATALOG = true;
  public final static int MAX_SUMMARY_LENGTH = 30;
  public final static int MAX_BOOK_SUMMARY_LENGTH = 250;
  public final static String CatalogFilter = "";
  public final static boolean generateExternalLinks = true;
  public final static boolean generateCrossLinks = true;
  public final static boolean generateTags = true;
  public final static boolean generateRecent = true;
  public final static boolean generateRatings = true;
  public final static boolean generateAllbooks = true;
  public final static boolean suppressRatingsInTitles = false;
  public final static String targetFolder = ".";
  public final static boolean COPYTODATABASEFOLDER = true;
  public final static boolean browseByCover = false;
  public final static boolean publishedDateAsYear = false;
  public final static boolean splitByAuthorInitialGoToBooks = false;
  public final static boolean includeAboutLink = true;
  public final static String tagsToMakeDeep = "";
  public final static boolean browseByCoverWithoutSplit = true;
  public static final int minBooksToMakeDeepLevel = 50;
  public final static boolean COVER_RESIZE = true;
  public static final int CoverHeight = 550;
  public final static boolean IncludeOnlyOneFile = false;
  public final static CompatibilityTrick COMPATIBILITYTRICK = CompatibilityTrick.OPDS;
  public final static boolean ZipTrookCatalog = false;
  public final static boolean ReprocessEpubMetadata = false;
  public final static boolean OrderAllBooksBySeries = true;
  public final static int MAX_MOBILE_RESOLUTION = 960;
  public final static boolean splitInSeriesBooks = false;
  public final static boolean splitInAuthorBooks = false;
  public final static String GOODREAD_ISBN_URL = "http://www.goodreads.com/book/isbn/{0}";
  public final static String GOODREAD_REVIEW_ISBN_URL = "http://www.goodreads.com/review/isbn/{0}";
  public final static String GOODREAD_TITLE_URL = "http://www.goodreads.com/book/title/{0}";
  public final static String GOODREAD_AUTHOR_URL = "http://www.goodreads.com/book/author/{0}";
  public final static String LIBRARYTHING_ISBN_URL = "http://www.librarything.com/isbn/{0}";
  public final static String LIBRARYTHING_TITLE_URL = "http://www.librarything.com/title/{0}%20{1}";
  public final static String LIBRARYTHING_AUTHOR_URL = "http://www.librarything.com/author/{0}";
  public final static String AMAZON_ISBN_URL =
      "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-isbn={0}";
  public final static String AMAZON_TITLE_URL =
      "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-title={0}&field-author={1}";
  public final static String AMAZON_AUTHOR_URL =
      "http://www.amazon.com/gp/search/ref=sr_adv_b/?search-alias=stripbooks&unfiltered=1&sort=relevanceexprank&field-author={0}";
  public final static String ISFDB_AUTHOR_URL = "http://www.isfdb.org/cgi-bin/ea.cgi?{0}";
  public final static String WIKIPEDIA_URL = "http://{0}.wikipedia.org/wiki/{1}";
  public final static boolean GenerateIndex = false;
  public final static boolean IndexComments = true;
  public final static int MaxKeywords = -1; // don't filter
  public final static Index.FilterHintType IndexFilterAlgorithm = Index.FilterHintType.RemoveMedian;
  public final static String UrlBase = null;
  public final static String CustomCatalogTitle = "Featured books";
  public final static String CustomCatalogSavedSearchName = "";
}
