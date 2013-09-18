package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.JDOM.Namespace;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.jdom.Element;
import sun.util.resources.CurrencyNames_ar_TN;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

public class FeedHelper {

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
  private final static String RELATION_NEXT = "next";

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
  private final static String LINKTYPE_NAVIGATION = "application/atom+xml;profile=opds-catalog;kind=navigation";

  /**
   * a link to an html page - external links use this type, in our catalogs
   */
  private final static String LINKTYPE_HTML = "text/html";

  /**
   * a link to a jpeg image
   */
  private final static String LINKTYPE_JPEG = "image/jpeg";

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
  public static Element getFeedRootElement(Breadcrumbs breadcrumbs, String pTitle, String urn, String urlExt) {
    Element feed = getAtomElement(true, "feed", pTitle, urn, null, LINKTYPE_NAVIGATION, null, true, null);

    // updated tag
    Element updated = getUpdatedTag();
    feed.addContent(updated);

    decorateElementWithNavigationLinks(feed, breadcrumbs, pTitle, urlExt, false);

    return feed;
  }

  /**
   * Generate a link to a catalog entry
   *
   * @param pTitle
   * @param urn
   * @param filename
   * @param pSummary
   * @param icon
   * @return
   */
  public static Element getCatalogEntry(String pTitle, String urn, String filename, String pSummary, String icon) {
    Element result = getAtomElement(false, "entry", pTitle, urn, filename, pSummary, false, icon);
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
    Element result = getAtomElement(false, "entry", pTitle, urn, null, null, null, (String) null, false, null);
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
    Element result = getAtomElement(false, "entry", title, urn, url, LINKTYPE_HTML, summary, true, icon);
    // add updated
    result.addContent(getUpdatedTag());
    return result;
  }

  public static Element getExternalLinkEntry(String title, String urn, String url, String icon) {
    String linkType;
    if (url.toUpperCase().endsWith(".XML") ||url.toUpperCase().startsWith("OPDS://")) {
      linkType = LINKTYPE_NAVIGATION;
      // Strip off the OPDS part if it precedes a HTTP type URL as this is a special case
      if (url.toUpperCase().startsWith("OPDS://HTTP"))
        url = url.substring(7);
    } else
      linkType = LINKTYPE_HTML;

    Element result = getAtomElement(false, "entry", title, urn, url, linkType, "", false, icon);
    // add updated
    result.addContent(getUpdatedTag());
    return result;
  }

  /* ---------- LINKS -----------*/

  public static Element getNextLink(String filename, String title) {
    return getLinkElement(filename, LINKTYPE_NAVIGATION, RELATION_NEXT, title);
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

  public static Element getAcquisitionLink(String url, String mimeType, String title) {
    return getLinkElement(url, mimeType, RELATION_ACQUISITION, title);
  }

  public static Element getCoverLink(String url) {
    return getLinkElement(url, LINKTYPE_JPEG, RELATION_COVER, null);
  }

  public static Element getThumbnailLink(String url) {
    return getLinkElement(url, "image/jpeg", RELATION_THUMBNAIL, null);
  }

  public static Element getFeaturedLink(String url, String title) {
    return getLinkElement(url, LINKTYPE_NAVIGATION, RELATION_FEATURED, title);
  }

  /**
   * Decorate a root element (feed or entry, in the case of a full book entry) with the start and self links, and the breadcrumb navigation tree
   *
   * @param feed        the feed to decorate
   * @param breadcrumbs the breadcrumbs retracing steps to the root
   * @param title      the title of the page
   * @param url      the url end of the page (baseURL + url = complete url)
   * @param isEntry if true, the document is a full entry, if false, it's a catalog
   */
  public static void decorateElementWithNavigationLinks(Element feed, Breadcrumbs breadcrumbs, String title, String url, boolean isEntry) {
    if (feed == null)
      return;

    if ((! url.endsWith(Constants.XML_EXTENSION)) && (! url.endsWith(Constants.HTML_EXTENSION))) {
      int dummy = 1;
      // assert url.endsWith(Constants.XML_EXTENSION) : "Program Error: Attempt to set up URL without .xml extension ";
    }
    // add a "start" link to the catalog main page
    String baseUrl = ConfigurationManager.INSTANCE.getCurrentProfile().getUrlBooks();
    // root catalog link
    String startUrl = baseUrl;
    if (baseUrl.length() == 0) startUrl += Constants.PARENT_PATH_PREFIX;
    startUrl += CatalogManager.getInitialUr() + Constants.XML_EXTENSION;
    // c2o-87 - Title should use value from settings
    feed.addContent(getLinkElement(startUrl, LINKTYPE_NAVIGATION, RELATION_START, ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogTitle()));

    // add a navigation link to every breadcrumb in the hierarchy
    if (breadcrumbs != null) {
      // Special treatment for first breadcrumb (start URL)
      Breadcrumb breadcrumb = breadcrumbs.firstElement();
      feed.addContent(getLinkElement(startUrl, LINKTYPE_NAVIGATION, "breadcrumb", breadcrumb.title));
      // Add links for remaining breadcrumbs
      for (int i = 1 ; i < breadcrumbs.size() ; i++) {
        breadcrumb = breadcrumbs.elementAt(i);
        String breadcrumbUrl = breadcrumb.url;
        // All breadcrumb URL's (except first) need to be to parent level
        if (breadcrumbUrl.startsWith(Constants.CURRENT_PATH_PREFIX)) {
          breadcrumbUrl = Constants.PARENT_PATH_PREFIX + breadcrumbUrl.substring(Constants.CURRENT_PATH_PREFIX.length());
        }
        if (! breadcrumbUrl.startsWith(Constants.PARENT_PATH_PREFIX)) {
          breadcrumbUrl = breadcrumbUrl.substring(Constants.PARENT_PATH_PREFIX.length());
        }
        feed.addContent(getLinkElement(breadcrumbUrl, LINKTYPE_NAVIGATION, "breadcrumb", breadcrumb.title));
      }
    }

    String selfUrl = baseUrl;
    if (url != null) {
      /*
       little programming trick ; due to the way this method is called, url is also used for the breadcrumbs and
       therefore starts with ../
        */
      String s;
      if (url.startsWith("../"))
        s = url.substring(3);
      else
        s = url;
      selfUrl = baseUrl + s;
    }

    // Self URL's mean we are already in a  folder, so do need to add in "../" at start  (c2o-104)
    // ITIMPI:  How does thes the above test for removing "../" interact (at all) with this?
    if (selfUrl.startsWith(Constants.PARENT_PATH_PREFIX))
      selfUrl = Constants.CURRENT_PATH_PREFIX + selfUrl.substring(3);
    if (! selfUrl.startsWith(Constants.CURRENT_PATH_PREFIX))
      selfUrl = Constants.CURRENT_PATH_PREFIX + selfUrl;

    feed.addContent(getLinkElement(selfUrl, isEntry ? LINKTYPE_FULLENTRY : LINKTYPE_NAVIGATION, RELATION_SELF, title));
  }
  /* ---------- METADATA ----------*/

  public static Element getDublinCoreLanguageElement(String lang) {
    Element result = JDOM.INSTANCE.element("language", Namespace.DcTerms);
    result.setText(lang);
    return result;
  }

  public static Element getDublinCorePublisherElement(String publisher) {
    Element result = JDOM.INSTANCE.element("publisher", Namespace.DcTerms);
    result.setText(publisher);
    return result;
  }

  public static Element getCategoryElement(String term) {
    Element result = JDOM.INSTANCE.element("category");
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
   * URL encode a string with control over how slashes are handled
   *
   * @param s                  the string to be encoded
   * @param doNotEncodeSlashes if true, slashes will not be encoded
   */
  public static String urlEncode(String s, boolean doNotEncodeSlashes) {
    try {
      String result = s;
      if (doNotEncodeSlashes)
        result = result.replace("/", "HERELIESASLASH_ICIUNSLASH");
      result = URLEncoder.encode(result, "utf-8");
      // this dumb java converts spaces to "+" and I don't like it
      result = result.replace("+", "%20");
      if (doNotEncodeSlashes)
        result = result.replace("HERELIESASLASH_ICIUNSLASH", "/");
      return result;
    } catch (UnsupportedEncodingException e) {
      // we don't give a damn
      return null;
    }
  }

  /* ---------- PRIVATE -----------*/

  private static Element getFeedAuthorElement() {
    return getFeedAuthorElement(Constants.AUTHORNAME, Constants.CALIBRE2OPDS_COM, Constants.AUTHOREMAIL);
  }

  private static Element getFeedAuthorElement(String name, String uri, String email) {
    Element author = JDOM.INSTANCE.element("author");
    if (Helper.isNotNullOrEmpty(author))
      author.addContent(JDOM.INSTANCE.element("name").addContent(name));
    if (Helper.isNotNullOrEmpty(uri))
      author.addContent(JDOM.INSTANCE.element("uri").addContent(uri));
    if (Helper.isNotNullOrEmpty(email))
      author.addContent(JDOM.INSTANCE.element("email").addContent(email));
    return author;
  }

  private static Element getUpdatedTag() {
    if (!ConfigurationManager.INSTANCE.getCurrentProfile().getSaveBandwidth()) {
      Calendar c = Calendar.getInstance();
      return getUpdatedTag(c);
    } else {
      // DP: return fake updated time - Oh, my birthday, what a coincidence ;)
      return JDOM.INSTANCE.element("updated").addContent("1973-01-26T08:00:00Z");
    }
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
    return JDOM.INSTANCE.element("updated").addContent(getDateAsIsoDate(c));
  }

  public static Element getPublishedTag(Date d) {
    return JDOM.INSTANCE.element("published").addContent(getDateAsIsoDate(d));
  }

  private static Element getLinkElement(String url, String urlType, String urlRelation, String title) {
    Element link = JDOM.INSTANCE.element("link");
    if (urlType != null && urlRelation != null && urlType.equals(LINKTYPE_NAVIGATION) && urlRelation.equals(RELATION_NEXT)) {
      // Next URL's mean we are already in a folder, so ensure we go up a level as part of the URL (c2o-104)
      if (! url.startsWith("../"))
          url = "../" + url;
    }

    link.setAttribute("href", url);

    if (Helper.isNotNullOrEmpty(urlType))
      link.setAttribute("type", urlType);

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
      contentElement = JDOM.INSTANCE.element("content").addContent(content);
      contentElement.setAttribute("type", "text");
    }
    Element element;
    if (isRoot)
      element = JDOM.INSTANCE.rootElement(elementName, Namespace.Atom, Namespace.DcTerms, Namespace.Atom, Namespace.Xhtml, Namespace.Opds);
    else
      element = JDOM.INSTANCE.element(elementName);

    // title
    Element titleElement = JDOM.INSTANCE.element("title").addContent(title);
    element.addContent(titleElement);

    // id
    Element idElement = JDOM.INSTANCE.element("id").addContent(id);
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
}
