package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Container object for linking many object types used within the program
 *
 * NOTES:
 *   1.  We use public variables when getter/setters would have no additional logic!
 *       (this tends to lead to slightly more efficient Java code).
 */

public enum CatalogContext {
  INSTANCE;

  public static CatalogManager catalogManager;
  public static HtmlManager htmlManager;
  public static ThumbnailManager thumbnailManager;
  public static ImageManager coverManager;
  public static CatalogCallbackInterface callback;
  public static SecurityManager securityManager;
  public static ConfigurationManager configurationManager;
  public static ConfigurationHolder currentProfile;
  // This is the date format used within the book details.
  // At the moment it is either a full date or jsut the year
  // If users ask for more flexibility the coniguration options can be re-visited.
  public static DateFormat titleDateFormat;
  // This is the date format that is to be used in the titles for the Recent Books sub-catalog section
  // It is currently a hard-coded format.   If there is user feedback suggestion that variations are
  // desireable then it could be come a configurable option
  public static DateFormat bookDateFormat;
  // Tags that the user has specified should not be included
  private static List<Tag> tagsToIgnore;


  private CatalogContext() {
    initialize();
  }

  public void initialize() {
    if (catalogManager == null)   catalogManager = new CatalogManager();
    if (htmlManager == null)      htmlManager = new HtmlManager();
    if (thumbnailManager == null) thumbnailManager = ImageManager.newThumbnailManager();
    if (coverManager==null)       coverManager = ImageManager.newCoverManager();
    if (securityManager==null)    securityManager = new SecurityManager();
    if (currentProfile==null)     currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
    if (bookDateFormat==null)     bookDateFormat = currentProfile.getPublishedDateAsYear() ? new SimpleDateFormat("yyyy") : SimpleDateFormat.getDateInstance(DateFormat.LONG,new Locale(currentProfile.getLanguage()));
    if (titleDateFormat==null)    titleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, new Locale(currentProfile.getLanguage()));
    getTagsToIgnore();
  }

  /**
   * Clear any cached information
   * Should always be called at the start of a generate run
   */
  public void reset() {
    catalogManager = null;
    htmlManager = null;
    thumbnailManager = null;
    coverManager = null;
    securityManager = null;
    currentProfile = null;
    titleDateFormat = null;
    bookDateFormat = null;
    tagsToIgnore = null;
  }

  /**
   * Get the list of tags to ignore. If it has not been done,
   * convert the list of tags to ignore from the string
   * representation to the appropriate object representation
   * as this is more effecient in later processing.
   *
   * @return
   */
  public List<Tag>  getTagsToIgnore () {
    if (tagsToIgnore == null) {
      tagsToIgnore = new LinkedList<Tag>();
      for (Tag tag : DataModel.INSTANCE.getListOfTags()) {
        List<String> regextagsToIgnore = currentProfile.getRegExTagsToIgnore();
        for (String regexTag : regextagsToIgnore) {
          if (tag.getName().toUpperCase().matches("^" + regexTag)) {
            if (! tagsToIgnore.contains(tag)) {
              tagsToIgnore.add(tag);
            }
          }
        }
      }
    }
    return tagsToIgnore;
  }


  //  The following methods have been deprecated in favour of making
  //  some global variables available.  This decision might need revisiting.
 /*
  public CatalogCallbackInterface getCallback() {
    if (callback == null)
      callback = new Log4jCatalogCallback();
    return callback;
  }

  public void setCallback(CatalogCallbackInterface callback) {
    this.callback = callback;
  }

  public CatalogManager getCatalogManager() {
    return catalogManager;
  }

  public void setCatalogManager(CatalogManager catalogManager) {
    catalogManager = catalogManager;
  }

  public HtmlManager getHtmlManager() {
    return htmlManager;
  }

  public void setHtmlManager(HtmlManager htmlManager) {
    htmlManager = htmlManager;
  }

  public ThumbnailManager getThumbnailManager() {
    return thumbnailManager;
  }

  public void setThumbnailManager(ThumbnailManager thumbnailManager) {
    thumbnailManager = thumbnailManager;
  }

  public ImageManager getCoverManager() {
    return coverManager;
  }

  public void setCoverManager(ImageManager coverManager) {
    this.coverManager = coverManager;
  }
  */

}
