package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.tools.Helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Constants {
  public final static String PROGNAME = "calibre2opds";
  public final static String PROGVERSION = "3.2 beta";
  public final static String BZR_VERSION = getBzrVersion();
  public final static String PROGTITLE = PROGNAME + " " + PROGVERSION;
  public final static String LOGFILE_FOLDER = "log";
  public final static String LOGFILE_NAME = "calibre2opds.log";
  public final static String SYNCFILE_NAME = "synclog.log";
  public final static String CONFIGURATION_COMPATIBILITY_VERSIONCHIP = "02030101";
  public final static String DSNNAME = "CALIBRE";
  public final static String AUTHOREMAIL = "dpierron+calibre2opds@gmail.com";
  public final static String AUTHORNAME = "David Pierron";
  public final static String CALIBRE2OPDS_COM = "http://calibre2opds.com";
  public final static String PAYPAL_DONATION = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LJJYRJBYCW8EU";
  public final static String HOME_URL = "http://calibre2opds.com/";
  public final static String USERGUIDE_URL = "http://calibre2opds.com/read-the-documentation/development/overview/";
  public final static String DEVELOPERGUIDE_URL = "http://calibre2opds.com/read-the-documentation/development/";
  public final static String ISSUES_URL = "http://calibre2opds.myjetbrains.com/youtrack/";
  public final static String FORUM_URL = "https://getsatisfaction.com/calibre2opds/";
  public final static String TROOK_FOLDER_EXTENSION = ".library";
  public final static String TROOK_SEARCH_DATABASE_FILENAME = "calibre2opds_trook_search.db";
  public final static String TROOK_CATALOG_FILENAME = "_catalog.zip";
  public final static String NOOK_CATALOG_FOLDERNAME = "_catalog";
  public final static String CALIBRE_COVER_FILENAME = "cover.jpg";
  public final static String DEFAULT_RESIZED_THUMBNAIL_FILENAME = "thumbnail.png";
  public final static String DEFAULT_THUMBNAIL_FILENAME = "default_thumbnail.png";


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

  public final static String getBzrVersion() {
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
