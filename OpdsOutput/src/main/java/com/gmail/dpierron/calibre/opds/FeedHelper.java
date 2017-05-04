package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.JDOMManager.Namespace;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

public class FeedHelper {
  private final static Logger logger = LogManager.getLogger(ImageManager.class);


  /**
   * An Acquisition Feed with newly released OPDS Catalog Entries. These Acquisition Feeds typically contain a subset of the OPDS Catalog
   * Entries in an OPDS Catalog based on the publication date of the Publication
   */
  private final static String RELATION_SORT_NEW = "http://opds-spec.org/sort/new";

  /**
   * An Acquisition Feed with popular OPDS Catalog Entries. These Acquisition Feeds typically contain a subset of the OPDS Catalog
   * Entries in an OPDS Catalog based on a numerical ranking criteria.
   */
  private final static String RELATION_SORT_POPULAR = "http://opds-spec.org/sort/popular";

  /**
   * An Acquisition Feed with featured OPDS Catalog Entries. These Acquisition Feeds typically contain a subset of the OPDS Catalog
   * Entries in an OPDS Catalog that have been selected for promotion by the OPDS Catalog provider. No order is implied.
   */
  private final static String RELATION_FEATURED = "http://opds-spec.org/featured";

  /**
   * An Acquisition Feed with recommended OPDS Catalog Entries. These Acquisition Feeds typically contain a subset of the OPDS Catalog
   * Entries in an OPDS Catalog that have been selected specifically for the user.
   */
  private final static String RELATION_RECOMMENDED = "http://opds-spec.org/recommended";

  /**
   * A link to a downloadable book
   */
  private final static String RELATION_ACQUISITION = "http://opds-spec.org/acquisition";

  /**
   * The following page in a paginated Acquisition Feed
   */
  public final static String RELATION_NEXT = "next";
  /**
   * The previous page in a paginated Acquisition Feed
   */
  public final static String RELATION_PREV = "prev";
  /**
   * The first page in a paginated Acquisition Feed
   */
  public final static String RELATION_FIRST = "first";
  /**
   * The last page in a paginated Acquisition Feed
   */
  public final static String RELATION_LAST = "last";

  /**
   * Atom relation for an alternate link - only used for full entry links (see LINKTYPE_FULLENTRY)
   */
  private final static String RELATION_ALTERNATE = "alternate";

  /**
   * A related or suggested Acquisition Feed. An example would be a "related" link from the newest releases in a category to the most
   * popular in a category.
   */
  private final static String RELATION_RELATED = "related";

  /**
   * A link to the same page (self-link)
   */
  private final static String RELATION_SELF = "self";

  /**
   * A link to the start page of the catalog
   */
  private final static String RELATION_START = "start";

  /**
   * A breadcrumb link
   */
  private final static String RELATION_BREADCRUM = "breadcrumb";
  /**
   * A link to an author of the item
   */
  public final static String RELATION_AUTHOR = "author";

  /**
   * a graphical Resource associated to the OPDS Catalog Entry
   */
  private final static String RELATION_COVER = "http://opds-spec.org/image";

  /**
   * a reduced-size version of a graphical Resource associated to the OPS Catalog Entry
   */
  private final static String RELATION_THUMBNAIL = "http://opds-spec.org/image/thumbnail";

  /**
   * a link from a partial book entry in a catalog to a full book entry in a separate entry document
   */
  private final static String LINKTYPE_FULLENTRY = "application/atom+xml;type=entry;profile=opds-catalog";

  /**
   * a navigation link, i.e. to another catalog
   */
  public final static String LINKTYPE_NAVIGATION = "application/atom+xml;profile=opds-catalog;kind=navigation";

  /**
   * a link to an html page - external links use this type, in our catalogs
   */
  private final static String LINKTYPE_HTML = "text/html";

  /**
   * a link to a jpeg image
   */
  private final static String LINKTYPE_JPEG = "image/jpg";
  private final static String LINKTYPE_PNG= "image/png";

  /* ---------- ELEMENTS -----------*/

  /**
   * create the root of an OPDS feed
   *
   * @param breadcrumbs the navigation elements
   * @param pTitle      the title of the feed
   * @param urn         the identifier of the feed
   * @param urlExt      the URL of the feed (relative to the base URL)
   * @return a 'feed' element
   */
  public static Element getFeedRootElement(Breadcrumbs breadcrumbs, String pTitle, String urn, String urlExt, boolean inSubDir) {
    Element feed = getAtomElement(true,Constants.OPDS_ELEMENT_FEED, pTitle, urn, null, LINKTYPE_NAVIGATION, null, true, null);

    // updated tag
    Element updated = getUpdatedTag();
    feed.addContent(updated);

    decorateElementWithNavigationLinks(feed, breadcrumbs, pTitle, urlExt, false);

    return feed;
  }

  /**
   * Generate a link to a catalog entry adding an updated element
   *
   * @param pTitle
   * @param urn
   * @param filename
   * @param pSummary
   * @param icon
   * @return
   */
  public static Element getCatalogEntry(
      String pTitle,
      String urn,
      String filename,
      String pSummary,
      String icon) {
    Element result = getAtomElement(false, Constants.OPDS_ELEMENT_ENTRY, pTitle, urn, filename, pSummary, false, icon);
    // add updated
    result.addContent(getUpdatedTag());
    return result;
  }

  /**
   * Generate a link to a book details entry
   *
   * @param pTitle
   * @param urn
   * @param timestamp
   * @return
   */
  public static Element getBookEntry(String pTitle, String urn, long timestamp) {
    Element result = getAtomElement(false, Constants.OPDS_ELEMENT_ENTRY, pTitle, urn, null, null, null, (String) null, false, null);
    // add updated
    result.addContent(getUpdatedTag(timestamp));
    return result;
  }

  /**
   * Generate a link to the 'About entry'
   *
   * @param title
   * @param urn
   * @param url
   * @param summary
   * @param icon
   * @return
   */
  public static Element getAboutEntry(String title, String urn, String url, String summary, String icon) {
    Element result = getAtomElement(false, Constants.OPDS_ELEMENT_ENTRY, title, urn, url, LINKTYPE_HTML, summary, true, icon);
    // add updated
    result.addContent(getUpdatedTag());
    return result;
  }

  public static Element getExternalLinkEntry(String title, String summary, boolean opdsLink, String urn, String url, String icon) {
    Element result = getAtomElement(false, Constants.OPDS_ELEMENT_ENTRY, title, urn, url,
                                    opdsLink ? LINKTYPE_NAVIGATION : LINKTYPE_HTML, summary, false, icon);
    // add updated
    result.addContent(getUpdatedTag());
    return result;
  }

  /* ---------- LINKS -----------*/

  public static Element getNavigationLink(String url, String navType, String title)  { return getLinkElement(url, LINKTYPE_NAVIGATION, navType, title); }

  public static Element getNextLink(String url, String title) {
    return getLinkElement(url, LINKTYPE_NAVIGATION, RELATION_NEXT, title);
  }

  public static Element getFullEntryLink(String url) {
    return getLinkElement(url, LINKTYPE_FULLENTRY, RELATION_ALTERNATE, null);
  }

  public static Element getRelatedLink(String url, String title) {
    return getLinkElement(url, LINKTYPE_NAVIGATION, RELATION_RELATED, title);
  }

  public static Element getRelatedHtmlLink(String url, String title) {
    return getLinkElement(url, LINKTYPE_HTML, RELATION_RELATED, title);
  }

  public static Element getAcquisitionLink(String url, String mimeType, String title, String size) {
    Element link = getLinkElement(url, mimeType, RELATION_ACQUISITION, title);
    if (Helper.isNotNullOrEmpty(size)) {
      link.setAttribute("displaysize", size);
    }
    return link;
  }

  /**
   * Add an image link (cover or thumbnail)
   *
   * @param url
   * @param isCover
   * @return
   */
  public static Element getImageLink(String url, boolean isCover) {
    return getLinkElement(url,
                          url.toUpperCase().endsWith(".PNG") ? LINKTYPE_PNG : LINKTYPE_JPEG,
                          isCover ? RELATION_COVER : RELATION_THUMBNAIL, null);
  }

  public static Element getFeaturedLink(String url, String title) {
    return getLinkElement(url, LINKTYPE_NAVIGATION, RELATION_FEATURED, title);
  }

  /**
   * Decorate a root element
   * (feed or entry, in the case of a full book entry) with the start and self links, and the breadcrumb navigation tree
   *
   * @param feed        the feed to decorate
   * @param breadcrumbs the breadcrumbs retracing steps to the root
   * @param title       the title of the page
   * @param catalogFilename         the url (filename) of the page being decorated
   * @param isEntry     if true, the document is a full entry, if false, it's a catalog
   */
  public static void decorateElementWithNavigationLinks(Element feed, Breadcrumbs breadcrumbs, String title, String catalogFilename, boolean isEntry) {
    if (feed == null)
      return;
    assert breadcrumbs != null;
    assert catalogFilename!= null;
    assert catalogFilename.endsWith(Constants.XML_EXTENSION) || catalogFilename.endsWith(Constants.HTML_EXTENSION)
             : "Program Error: url should end with .xml extension";

    if (catalogFilename.contains("custom")) {
      int dummy = 1;
    }

    // We want to get past any folder separators to get to the base filename;
    int pos = 0;
    while (catalogFilename.substring(pos).contains(Constants.FOLDER_SEPARATOR))
      pos = catalogFilename.indexOf(Constants.FOLDER_SEPARATOR,pos) + 1;
    String filename = catalogFilename.substring(pos);
    String folder = catalogFilename.substring(0,pos);
    pos = folder.indexOf(Constants.CURRENT_PATH_PREFIX);   // Also handles parent case!
    if (pos != -1)
      folder = folder.substring(pos+Constants.CURRENT_PATH_PREFIX.length());
    feed.addContent(getLinkElement(Constants.CURRENT_PATH_PREFIX + filename, isEntry ? LINKTYPE_FULLENTRY : LINKTYPE_NAVIGATION, RELATION_SELF, title));

    // add a "start" link to the catalog main page

    String startUrl = (folder.length() == 0 ? Constants.CURRENT_PATH_PREFIX : Constants.PARENT_PATH_PREFIX)
                      + CatalogManager.getInitialUr() + Constants.XML_EXTENSION;
    // c2o-87 - Title should use value from settings
    feed.addContent(getLinkElement(startUrl,
                                   LINKTYPE_NAVIGATION,
                                   RELATION_START,
                                   ConfigurationManager.getCurrentProfile().getCatalogTitle()));

    // add a navigation link to every breadcrumb in the hierarchy

    // Special treatment for first breadcrumb (start URL)

    if (breadcrumbs.size() > 0) {
      // Add breadcrumb links
      for (int i = 0 ; i < breadcrumbs.size() ; i++) {
        Breadcrumb breadcrumb = breadcrumbs.elementAt(i);
        String breadcrumbUrl = breadcrumb.url;
        while (breadcrumbUrl.substring(pos).contains(Constants.FOLDER_SEPARATOR))
          pos = breadcrumbUrl.indexOf(Constants.FOLDER_SEPARATOR,pos) + 1;
        String breadcrumbFilename = breadcrumbUrl.substring(pos);
        String breadcrumbFolder = breadcrumbUrl.substring(0,pos);
        pos = breadcrumbFolder.indexOf(Constants.CURRENT_PATH_PREFIX);   // Also handles parent case!
        if (pos != -1)
          breadcrumbFolder = breadcrumbFolder.substring(pos+Constants.CURRENT_PATH_PREFIX.length());
        feed.addContent(getLinkElement((breadcrumbFolder.equals(folder)
                                       ? Constants.CURRENT_PATH_PREFIX
                                       : Constants.PARENT_PATH_PREFIX + breadcrumbFolder)
                                       + breadcrumbFilename,
                                      LINKTYPE_NAVIGATION,
                                      RELATION_BREADCRUM,
                                      breadcrumb.title));
      }
    }
  }

  /* ---------- METADATA ----------*/

  public static Element getDublinCoreLanguageElement(String lang) {
    Element result = JDOMManager.element("language", Namespace.DcTerms);
    result.setText(lang);
    return result;
  }

  public static Element getDublinCorePublisherElement(String publisher) {
    Element result = JDOMManager.element("publisher", Namespace.DcTerms);
    result.setText(publisher);
    return result;
  }

  public static Element getCategoryElement(String term) {
    Element result = JDOMManager.element("category");
    result.setAttribute("term", term);
    return result;
  }

  /* ---------- UTILITIES  -----------*/

  /**
   * URL encode a string. Any embedded slashes are NOT encoded
   */
  public static String urlEncode(String s) {
    return urlEncode(s, false);
  }

  /**
   * URL encode a string with control over how special characters are handled
   *
   * @param s                       the string to be encoded
   * @param doNotEncodeSlashOrColon if true, slashes and colons will not be encoded
   *                                This ,eams sequences like http:// stay intact
   */
  public static String urlEncode(String s, boolean doNotEncodeSlashOrColon) {
    try {
      String result = s;
      if (doNotEncodeSlashOrColon) {
        result = result.replace("/", "HERELIESASLASH_ICIUNSLASH");
        result = result.replace(":", "HERELIESACOLON_ICIUNSLASH");
      }
      result = URLEncoder.encode(result, "utf-8");
      // this dumb java converts spaces to "+" and I don't like it
      result = result.replace("+", "%20");
      if (doNotEncodeSlashOrColon) {
        result = result.replace("HERELIESASLASH_ICIUNSLASH", "/");
        result = result.replace("HERELIESACOLON_ICIUNSLASH", ":");
      }
      return result;
    } catch (UnsupportedEncodingException e) {
      // we don't give a damn
      return null;
    }
  }

  /* ---------- PRIVATE -----------*/

  private static Element getFeedAuthorElement() {
    return getFeedAuthorElement(Constants.AUTHORNAME, Constants.HOME_URL, Constants.AUTHOREMAIL);
  }

  private static Element getFeedAuthorElement(String name, String uri, String email) {
    Element author = JDOMManager.element(Constants.OPDS_ELEMENT_AUTHOR);
    if (Helper.isNotNullOrEmpty(author))
      author.addContent(JDOMManager.element(Constants.OPDS_ELEMENT_NAME).addContent(name));
    if (Helper.isNotNullOrEmpty(uri))
      author.addContent(JDOMManager.element( Constants.OPDS_ELEMENT_URI).addContent(uri));
    if (Helper.isNotNullOrEmpty(email))
      author.addContent(JDOMManager.element(Constants.OPDS_ELEMENT_EMAIL).addContent(email));
    return author;
  }

  private static Element getUpdatedTag() {
    // TODO:  Revisit sinc minimizChangedFiles removed as an option
    // TODO:  prbably best to act as if it was set?
//    if (!ConfigurationManager.getCurrentProfile().getMinimizeChangedFiles()) {
//      Calendar c = Calendar.getInstance();
//      return getUpdatedTag(c);
//    } else {
      // DP: return fake updated time - Oh, my birthday, what a coincidence ;)
      return JDOMManager.element("updated").addContent("1973-01-26T08:00:00Z");
//    }
  }

  private static String getDateAsIsoDate(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    return getDateAsIsoDate(c);

  }

  private static String getDateAsIsoDate(Calendar c) {
    StringBuffer result = new StringBuffer();

    result.append(Helper.leftPad("" + c.get(Calendar.YEAR), '0', 4));
    result.append('-');
    result.append(Helper.leftPad("" + (c.get(Calendar.MONTH) + 1), '0', 2));
    result.append('-');
    result.append(Helper.leftPad("" + c.get(Calendar.DAY_OF_MONTH), '0', 2));
    result.append('T');
    result.append(Helper.leftPad("" + c.get(Calendar.HOUR), '0', 2));
    result.append(':');
    result.append(Helper.leftPad("" + c.get(Calendar.MINUTE), '0', 2));
    result.append(':');
    result.append(Helper.leftPad("" + c.get(Calendar.SECOND), '0', 2));
    result.append('Z');

    return result.toString();
  }

  public static Element getUpdatedTag(long timeInMilli) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(timeInMilli);
    return getUpdatedTag(c);
  }

  private static Element getUpdatedTag(Calendar c) {
    return JDOMManager.element("updated").addContent(getDateAsIsoDate(c));
  }

  public static Element getPublishedTag(Date d) {
    return JDOMManager.element("published").addContent(getDateAsIsoDate(d));
  }

  /**
   *
   * @param url
   * @param urlType
   * @param urlRelation
   * @param title
   * @return
   */
  public static Element getLinkElement(String url, String urlType, String urlRelation, String title) {
    Element link = JDOMManager.element(Constants.OPDS_ELEMENT_LINK);
    if (urlType != null && urlRelation != null
    && urlType.equals(LINKTYPE_NAVIGATION)
    && (urlRelation.equals(RELATION_NEXT) || urlRelation.equals(RELATION_PREV) || urlRelation.equals(RELATION_FIRST) || urlRelation.equals(RELATION_LAST))) {
      // Next URL's mean we are already in a folder, so ensure we go up a level as part of the URL (c2o-104)
      if (! url.startsWith("../"))
          url = "../" + url;
    }

    link.setAttribute("href", url);

    if (Helper.isNotNullOrEmpty(urlType)) {
      link.setAttribute("type", urlType);
      // #c20-277  Set download attribute for acquisition links
      // #c2o-280  undo previ/us chande as download attiibute now added via XRL
      // if (Helper.isNotNullOrEmpty(urlRelation) && urlRelation.equals(RELATION_ACQUISITION)) {
      //   String filename = url.substring(url.lastIndexOf('/') + 1);
      //   link.setAttribute("download", filename);
      // }
    }


    if (Helper.isNotNullOrEmpty(urlRelation))
      link.setAttribute("rel", urlRelation);

    if (Helper.isNotNullOrEmpty(title))
      link.setAttribute("title", title);

    return link;
  }

  private static Element getAtomElement(boolean isRoot,
      String pElement,
      String pTitle,
      String urn,
      String filename,
      String pSummary,
      boolean includeAuthor,
      String icon) {
    return getAtomElement(isRoot, pElement, pTitle, urn, filename, LINKTYPE_NAVIGATION, pSummary, includeAuthor, icon);
  }

  private static Element getAtomElement(boolean isRoot,
      String pElement,
      String pTitle,
      String urn,
      String url,
      String urlType,
      String pSummary,
      boolean includeAuthor,
      String icon) {
    return getAtomElement(isRoot, pElement, pTitle, urn, url, urlType, null,       // Relation not required
        pSummary, includeAuthor, icon);
  }

  private static Element getAtomElement(boolean isRoot,
      String elementName,
      String title,
      String id,
      String url,
      String urlType,
      String urlRelation,
      String content,
      boolean includeAuthor,
      String icon) {
    Element contentElement = null;
    if (Helper.isNotNullOrEmpty(content)) {
      contentElement = JDOMManager.element("content").addContent(content);
      contentElement.setAttribute("type", "text");
    }
    Element element;
    if (isRoot)
      element = JDOMManager.rootElement(elementName, Namespace.Atom, Namespace.DcTerms, Namespace.Atom, Namespace.Xhtml, Namespace.Opds);
    else
      element = JDOMManager.element(elementName);

    // title
    Element titleElement = JDOMManager.element("title").addContent(title);
    element.addContent(titleElement);

    // id
    Element idElement = JDOMManager.element("id").addContent(id);
    element.addContent(idElement);

    // content
    if (contentElement != null) {
      element.addContent(contentElement);
    }

    // link
    if (Helper.isNotNullOrEmpty(url)) {
      element.addContent(getLinkElement(url, urlType, urlRelation, null));
    }

    // icon link
    if (Helper.isNotNullOrEmpty(icon)) {
      Element iconElt = getLinkElement(icon, "image/png", "http://opds-spec.org/image/thumbnail", null);
      element.addContent(iconElt);
    }

    // add the feed author
    if (includeAuthor)
      element.addContent(getFeedAuthorElement());
    return element;
  }

  /**
   * We changed the standard for naming files.
   * This carries out the check if a file exists with the old naming
   * standard and and if necessary renames it to the new standard.
   * It use the CachedFile class to try and minimise any I/O from repeated checks
   *
   * @param newfile
   * @param oldfile
   */
  public static void checkFileNameIsNewStandard (CachedFile newfile, File oldfile) {
    if (! newfile.exists() && oldfile.exists()) {
      oldfile.renameTo(newfile);
      newfile.clearCachedInformation();                 // Clear cached information
      CachedFileManager.removeCachedFile(oldfile);
      logger.info("File " + oldfile.getName() + " renamed to " + newfile.getName());
    }
  }
}
