package com.gmail.dpierron.calibre.opds;
/**
 * Constants used internally within the Calibre2opds program
 * Assembled here to help with making them visible and easy to change.
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.tools.Helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

public class Constants {
  public final static String PROGNAME = "calibre2opds";
  public final static String PROGVERSION = "3.3 beta";
  public final static String BZR_VERSION = getBzrVersion();
  public final static String CALIBRE_METADATA_DB_= "metadata.db";
  public final static String PROGTITLE = PROGNAME + " " + PROGVERSION;
  public final static String LOGFILE_FOLDER = "log";
  public final static String LOGFILE_NAME = "calibre2opds.log";
  public final static String SYNCFILE_NAME = "synclog.log";
  public final static String CONFIGURATION_COMPATIBILITY_VERSIONCHIP = "02030101";
  public final static String LIBRARY_PATH_PREFIX = "../../";
  public final static String PARENT_PATH_PREFIX = "../";
  public final static String CURRENT_PATH_PREFIX = "./";
  public final static String FOLDER_SEPARATOR = "/";
  public final static String LEVEL_SEPARATOR = "!";
  public final static String TYPE_SEPARATOR = "_";
  public final static String URN_SEPARATOR = ":";
  public final static String SECURITY_SEPARATOR = "@";
  public final static String ALLBOOKS_TYPE = "allbooks";
  public final static String BOOK_TYPE ="book";
  public final static String AUTHOR_TYPE = "author";
  public final static String AUTHORS_TYPE = "authors";
  public final static String SERIE_TYPE = "serie";
  public final static String SERIES_TYPE = "series";
  public final static String TAG_TYPE = "tag";
  public final static String TAGS_TYPE = "tags";
  public final static String RATED_TYPE = "rated";
  public final static String RECENT_TYPE = "recent";
  public final static String PUBLISHER_TYPE = "publisher";
  public final static String PUBLISHERS_TYPE = "publishers";
  public final static String FEATURED_TYPE = "featured";
  public final static String CUSTOM_TYPE = "custom";
  public final static String IMAGES_FOLDER = "images";
  public final static String RESIZEDCOVER_FILENAME = "c2o_resizedcover.jpg";
  public final static String THUMBNAIL_FILENAME = "c2o_thumbnail.jpg";
  public final static String XML_EXTENSION = ".xml";
  public final static String HTML_EXTENSION = ".html";
  public final static String JPG_EXTENSION = ".jpg";
  public final static String AUTHOREMAIL = "dpierron+calibre2opds@gmail.com";
  public final static String AUTHORNAME = "David Pierron";
  public final static String CALIBRE2OPDS_COM = "http://calibre2opds.com";
  public final static String PAYPAL_DONATION = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LJJYRJBYCW8EU";
  public final static String HOME_URL = "http://calibre2opds.com/";
  public final static String USERGUIDE_URL = "http://wiki.mobileread.com/wiki/Calibre2Opds_Index";
  public final static String DEVELOPERGUIDE_URL = "http://wiki.mobileread.com/wiki/Calibre2OpdsDevelop";
  public final static String LOCALIZE_URL = "http://wiki.mobileread.com/wiki/Calibre2OpdsLocalize";
  public final static String CUSTOMIZE_URL = "http://wiki.mobileread.com/wiki/Calibre2OpdsCustomize";
  public final static String ISSUES_URL = "http://calibre2opds.myjetbrains.com/youtrack/";
  public final static String FORUM_URL = "https://getsatisfaction.com/calibre2opds/";
  public final static String PAGE_DELIM = "_Page_";
  public final static String PAGE_ONE_XML = "_Page_1.xml";
  public final static String INITIAL_URL = "index";
  public final static String INITIAL_URN_PREFIX = "Calibre2opds:";
  public final static String TROOK_FOLDER_EXTENSION = ".library";
  public final static String TROOK_SEARCH_DATABASE_FILENAME = "calibre2opds_trook_search.db";
  public final static String TROOK_CATALOG_FILENAME = "_catalog.zip";
  public final static String NOOK_CATALOG_FOLDERNAME = "_catalog";
  public final static String CALIBRE_COVER_FILENAME = "cover.jpg";
  public final static String DEFAULT_RESIZED_COVER_FILENAME = "default_cover.png";
  public final static String DEFAULT_RESIZED_THUMBNAIL_FILENAME = "thumbnail.png";
  public final static String DEFAULT_THUMBNAIL_FILENAME = "default_thumbnail.png";
  public final static String HELP_URL_MAIN_OPTIONS = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Main_Options_Tab";
  public final static String HELP_URL_CATALOGSTRUCTURE = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Catalog_Structure_Tab";
  public final static String HELP_URL_BOOKDETAILS = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Book_Details_Tab";
  public final static String HELP_URL_ADVANCED = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Advanced_Tab";
  public final static String HELP_URL_EXTERNALLINKS = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Book_Links_tab";
  public final static String HELP_URL_CUSTOMCATALOGS = "http://wiki.mobileread.com/wiki/Calibre2OpdsRunning#Custom_Catalogs_Tab";
  public final static Collection<String> CUSTOM_COLUMN_TYPES_SUPPORTED = Arrays.asList("text", "comments", "float", "int", "bool", "rating", "datetime", "enumeration");
  public final static Collection<String> CUSTOM_COLUMN_TYPES_UNSUPPORTED = Arrays.asList("series");

  // List of file resources that are embedded in binary, and need copying to final catalog as files
  public final static String[] FILE_RESOURCES = {
      ".skip",                // Special dummy file for Nook catalogs
      "desktop.css",
      "mobile.css",
      "homeIwebKit.png",
      "navleft.png",
      "functions.js",
      "oval-red-left.gif",
      "oval-red-right.gif",
      "bg_button_a.gif",
      "bg_button_span.gif",
      "bc_bg.png",
      "bc_separator.png",
      "home.png",
      DEFAULT_THUMBNAIL_FILENAME,
      // External image files for catalog sections
      Icons.ICONFILE_RECENT,
      Icons.ICONFILE_AUTHORS,
      Icons.ICONFILE_TAGS,
      Icons.ICONFILE_SERIES,
      Icons.ICONFILE_BOOKS,
      Icons.ICONFILE_ABOUT,
      Icons.ICONFILE_RATING,
      Icons.ICONFILE_FEATURED,
      Icons.ICONFILE_CUSTOM,
      Icons.ICONFILE_EXTERNAL,
      Icons.ICONFILE_SEARCH,
      "_search/search.html",
      "_search/css/desktop.css",
      "_search/database/database.js",
      "_search/media/license-bsd.txt",
      "_search/media/css/demo_page.css",
      "_search/media/css/demo_table.css",
      "_search/media/css/demo_table_jui.css",
      "_search/media/images/back_disabled.jpg",
      "_search/media/images/back_enabled.jpg",
      "_search/media/images/favicon.ico",
      "_search/media/images/forward_disabled.jpg",
      "_search/media/images/forward_enabled.jpg",
      "_search/media/images/Sorting icons.psd",
      "_search/media/images/sort_asc.png",
      "_search/media/images/sort_asc_disabled.png",
      "_search/media/images/sort_both.png",
      "_search/media/images/sort_desc.png",
      "_search/media/images/sort_desc_disabled.png",
      "_search/media/js/jquery.dataTables.min.js",
      "_search/media/js/jquery.js",};

  public static String getBzrVersion() {
    String vcsVersion = "";
    BufferedReader reader = null;
    try {
      try {
        reader = new BufferedReader(new InputStreamReader(Constants.class.getResourceAsStream("/version_OpdsOutput.properties")));
        vcsVersion = reader.readLine();
      } finally {
        if (reader != null)
          reader.close();
      }
    } catch (Exception e) {
      // we don't give a tiny rat's ass
    }
    if (Helper.isNotNullOrEmpty(vcsVersion))
      vcsVersion = " (rev. " + vcsVersion + ")";
    return vcsVersion;
  }
}
