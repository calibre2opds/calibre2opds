package com.gmail.dpierron.calibre.configuration;


import com.gmail.dpierron.calibre.opds.indexer.Index;

public class StanzaConstants {

  // options default values
  public final static int MAX_BEFORE_PAGINATE = 25;
  public final static int MAX_BEFORE_SPLIT = 3 * MAX_BEFORE_PAGINATE;
  public final static int MAX_SPLIT_LEVELS = 1;
  public final static int MAX_RECENT_ADDITIONS = 99999;
  public final static String CATALOGFOLDER = "_catalog";
  public final static boolean ONLY_CATALOG_A_TTARGET = false;
  public final static String CATALOGTITLE = "Calibre library";
  public final static String WIKIPEDIA_LANGUAGE = "en";
  public final static String INCLUDEDFORMATS = "EPUB, PDF, RTF, TXT, PRC, PDB, MOBI, LRF, LRX, FB2";
  public final static boolean GENERATE_OPDS = true;
  public final static boolean GENERATE_HTML = true;
  public final static boolean GENERATE_OPDS_DOWNLOADS = true;
  public final static boolean GENERATE_HTML_DOWNLOADS = true;
  public final static boolean GENERATE_DOWNLOADS = true;
  public final static boolean MINIMIZE_CHANGED_FILES = true;
  public final static boolean USE_EXTERNAL_ICONS = false;
  public final static int THUMBNAIL_HEIGHT = 144;
  public final static boolean THUMBNAIL_GENERATE = true;
  public final static String SPLIT_TAGS_ON = "";
  public final static boolean INCLUDEBOOKSWITHNOFILE = false;
  public final static boolean CRYPT_FILENAMES = false;
  public final static boolean SHOWSERIESINAUTHORCATALOG = true;
  public final static int MAX_SUMMARY_LENGTH = 30;
  public final static int MAX_BOOK_SUMMARY_LENGTH = 250;
  public final static String CatalogFilter = "";
  public final static boolean GENERATE_AUTHORS = true;
  public final static boolean GENERATE_TAGS = true;
  public final static boolean GENERATE_SERIES = true;
  public final static boolean GENERATE_RECENT = true;
  public final static boolean GENERATE_RATINGS = true;
  public final static boolean GENERATE_ALLBOOKS = true;
  public final static String BOOK_DETAILS_CUSTOM_FIELDS = "";
  public final static boolean suppressRatingsInTitles = false;
  public final static String targetFolder = ".";
  public final static boolean COPYTODATABASEFOLDER = true;
  public final static boolean NTOWSE_BY_COVER = false;
  public final static boolean splitByAuthorInitialGoToBooks = false;
  public final static boolean INCLUDE_ABOUT_LINK = true;
  public final static String TAGS_TO_IGNOREp = "";
  public final static String TAGS_TO_MAKE_DEEP = "";
  public final static boolean browseByCoverWithoutSplit = true;
  public static final int minBooksToMakeDeepLevel = 50;
  public final static boolean COVER_RESIZE = true;
  public static final int CoverHeight = 550;
  public final static boolean IncludeOnlyOneFile = false;
  public final static boolean ZipTrookCatalog = false;
  public final static boolean ReprocessEpubMetadata = false;
  public final static boolean OrderAllBooksBySeries = true;
  public final static int MAX_MOBILE_RESOLUTION = 960;
  public final static boolean GenerateIndex = false;
  public final static boolean IndexComments = true;
  public final static int MaxKeywords = -1; // don't filter
  public final static Index.FilterHintType IndexFilterAlgorithm = Index.FilterHintType.RemoveMedian;
  public final static String UrlBooks = null;
  public final static String FeaturedCatalogTitle = "Featured books";
  public final static String FeaturedCatalogSavedSearchName = "";
  /* Catalog Structure */
  public final static boolean DisplayAuthorSortInAuthorLists = true;
  public final static boolean DisplayTitleSortInBookLists = true;
  public final static boolean SortUsingAuthor = false;
  public final static boolean SortUsingTitle = false;
  public final static boolean languageAsTag = true;
  /* Book Details */
  public final static boolean generateExternalLinks = true;
  public final static boolean generateCrossLinks = true;
  public final static boolean includeSeriesInBookDetails = true;
  public final static boolean includeRatingInBookDetails = true;
  public final static boolean includeTagsInBookDetails = true;
  public final static boolean includePublisherInBookDetails = false;
  public final static boolean includePublishedInBookDetails = false;
  public final static boolean publishedDateAsYear = false;
  public final static boolean IncludeAddedInBookDetails = false;
  public final static boolean IncludeModifiedInBookDetails = false;
  public final static boolean DisplayAuthorSortInBookDetails = false;
  public final static boolean DisplayTitleSortInBookDetails = false;
  public final static boolean IncludeCoversInCatalog = false;
  /* Strings names for default URL's in Localization files */
  public final static String AMAZON_AUTHORS_URL_DEFAULT = "config.AmazonAuthorUrl.default";
  public final static String AMAZON_ISBN_URL_DEFAULT = "config.AmazonIsbnUrl.default";
  public final static String AMAZON_TITLE_URL_DEFAULT = "config.AmazonTitleUrl.default";
  public final static String GOODREADS_AUTHOR_URL_DEFAULT = "config.GoodreadAuthorUrl.default";
  public final static String GOODREADS_ISBN_URL_DEFAULT = "config.GoodreadIsbnUrl.default";
  public final static String GOODREADS_TITLE_URL_DEFAULT = "config.GoodreadTitleUrl.default";
  public final static String GOODREADS_REVIEW_URL_DEFAULT = "config.GoodreadReviewIsbnUrl.default";
  public final static String ISFDB_AUTHOR_URL_DEFAULT = "config.IsfdbAuthorUrl.default";
  public final static String LIBRARYTHING_AUTHOR_URL_DEFAULT = "config.LibrarythingAuthorUrl.default";
  public final static String LIBRARYTHING_ISBN_URL_DEFAULT = "config.LibrarythingIsbnUrl.default";
  public final static String LIBRARYTHING_TITLE_URL_DEFAULT = "config.LibrarythingTitleUrl.default";
  public final static String WIKIPEDIA_URL_DEFAULT = "config.WikipediaUrl.default";
}
