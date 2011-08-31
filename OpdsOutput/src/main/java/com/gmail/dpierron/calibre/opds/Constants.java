package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.tools.Helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Constants {
  public final static String PROGNAME = "calibre2opds";
  public final static String PROGVERSION = "2.5 beta 1";
  public final static String BZR_VERSION = getBzrVersion();
  public final static String PROGTITLE = PROGNAME + " " + PROGVERSION;
  public final static String LOGFILE_FOLDER = "log";
  public final static String LOGFILE_NAME = "calibre2opds.log";
  public final static String SYNCFILE_NAME = "synclog.log";
  public final static String CONFIGURATION_COMPATIBILITY_VERSIONCHIP = "02030101";
  public final static String DSNNAME = "CALIBRE";
  public final static String METADATA = "metadata.db";
  public final static String AUTHOREMAIL = "dpierron+calibre2opds@gmail.com";
  public final static String AUTHORNAME = "David Pierron";
  public final static String FEED_URL = "http://wiki.mobileread.com/wiki/Calibre2opds";
  public final static String PAYPAL_DONATION = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LJJYRJBYCW8EU";
  public final static String HELP_URL = "http://wiki.mobileread.com/wiki/Calibre2opds";
  public final static String TROOK_FOLDER_EXTENSION = ".library";
  public final static String TROOK_SEARCH_DATABASE_FILENAME = "calibre2opds_trook_search.db";
  public final static String TROOK_CATALOG_FILENAME = "_catalog.zip";
  public final static String CALIBRE_COVER_FILENAME = "cover.jpg";


  // List of file resources that are embedded in binary, and need copying to final catalog as files
  public final static String[] FILE_RESOURCES =
  {
        ".skip",
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
        "default_thumbnail.png",
        // External image files for catalog sections
        "allbooks.png",
        "authors.png",
        "c2o.png",
        "ratings.png",
        "recent.png",
        "series.png",
        "tags.png",
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
        "_search/media/js/jquery.js",
  };

  public final static String getBzrVersion() {
    String vcsVersion = "";
    BufferedReader reader = null;
    try {
      try {
        reader = new BufferedReader(new InputStreamReader(Constants.class.getResourceAsStream("/version_OpdsOutput.properties")));
        vcsVersion = reader.readLine();
      } finally {
        if (reader != null) reader.close();
      }
    } catch (Exception e) {
      // we don't give a tiny rat's ass
    }
    if (Helper.isNotNullOrEmpty(vcsVersion)) vcsVersion = " (rev. " + vcsVersion + ")";
    return vcsVersion;
  }
}
