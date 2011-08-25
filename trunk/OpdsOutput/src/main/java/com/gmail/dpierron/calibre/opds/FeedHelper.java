package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.CompatibilityTrick;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.JDOM.Namespace;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

public enum FeedHelper {
  INSTANCE;

  /**
   * URL encode a string.
   * Any embedded slashes are NOT encoded
   * @param s
   * @return
   */
  public String urlEncode(String s) {
    return urlEncode(s, false);
  }

  /**
   * URL encode a string with control over how slashes are handled
   * @param s
   * @param doNotEncodeSlashes
   * @return
   */
  public String urlEncode(String s, boolean doNotEncodeSlashes) {
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

  /**
   * Get the icon when it is embedded binary data
   * @param icon
   * @return
   */
  private Element getIcon(String icon) {
    return getLinkElement(icon,
                          "image/png",
                          "http://opds-spec.org/thumbnail",
                          null);
  }

  private Element getFeedAuthorElement() {
    return getFeedAuthorElement(Constants.AUTHORNAME, Constants.FEED_URL, Constants.AUTHOREMAIL);
  }
  
  public Element getFeedAuthorElement(String name, String uri, String email) {
    Element author = JDOM.INSTANCE.element("author");
    if (Helper.isNotNullOrEmpty(author))
      author.addContent(JDOM.INSTANCE.element("name").addContent(name));
    if (Helper.isNotNullOrEmpty(uri))
      author.addContent(JDOM.INSTANCE.element("uri").addContent(uri));
    if (Helper.isNotNullOrEmpty(email))
      author.addContent(JDOM.INSTANCE.element("email").addContent(email));
    return author;
  }
  
  private Element getUpdatedTag() {
    Calendar c = Calendar.getInstance();
    return getUpdatedTag(c);
  }

  Element getUpdatedTag(long timeInMilli) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(timeInMilli);
    return getUpdatedTag(c);
  }

  private Element getUpdatedTag(Calendar c) {
    StringBuffer result = new StringBuffer();

    result.append(c.get(Calendar.YEAR));
    result.append('-');
    result.append(c.get(Calendar.MONTH));
    result.append('-');
    result.append(c.get(Calendar.DAY_OF_MONTH));
    result.append('T');
    result.append(c.get(Calendar.HOUR));
    result.append(':');
    result.append(c.get(Calendar.MINUTE));
    result.append(':');
    result.append(c.get(Calendar.SECOND));
    result.append('Z');

    return JDOM.INSTANCE.element("updated").addContent(result.toString());
  }

  String getLinkType(boolean forEntry)
  {
    switch (ConfigurationManager.INSTANCE.getCurrentProfile().getCompatibilityTrick())
    {
    case TROOK:
            // Trook cannot (at the moment anyway) handle the extended type attribute
            // Hopefully this restriction will be lifted at some point
            return "application/atom+xml";
    default:
            return (forEntry == true)
                   ? "application/atom+xml;type=entry;profile=opds-catalog"
                   : "application/atom+xml;type=feed;profile=opds-catalog";
    }
  }
    
  String getLinkTypeForEntry() {
    return getLinkType(true);
  }
  
  String getLinkTypeForFeed() {
    return getLinkType(false);
  }

  Element getXmlLinkElement(String url, String relation, String title) {
    return getLinkElement(url, getLinkTypeForFeed(), relation, title);
  }

  public Element getFullEntryStanzaLinkElement(String url) {
    return getLinkElement(url, getLinkTypeForEntry(), null, null);
  }

  public Element getFullEntryLinkElement(String url) {
    return getLinkElement(url, getLinkTypeForEntry(), "alternate", null);
  }
  
  Element getLinkElement(String url,
                         String urlType,
                         String urlRelation,
                         String title)
  {
    Element link = JDOM.INSTANCE.element("link");
    
    link.setAttribute("href", url);

    if (Helper.isNotNullOrEmpty(urlType))
      link.setAttribute("type", urlType);
    
    if (Helper.isNotNullOrEmpty(urlRelation))
      link.setAttribute("rel", urlRelation);
    
    if (Helper.isNotNullOrEmpty(title))
      link.setAttribute("title", title);

    return link;
  }
  
  Element getNext(String filename, String title) {
    return getXmlLinkElement(filename, "next", title);
  }
  
  Element getEntry(String pTitle,
                   String urn,
                   String filename,
                   String pSummary,
                   String icon)
  {
    return getAtomElement(false, "entry", pTitle, urn, filename, pSummary, false, icon);
  }

  Element getFeed(Breadcrumbs breadcrumbs,
                  String pTitle,
                  String urn,
                  String pSummary)
  {
    Element result = getAtomElement(true, "feed", pTitle, urn, null, getLinkTypeForFeed(), null, pSummary, true, null);
    decorateFeed(result, breadcrumbs);
    return result;
  }

  Element getFeed(Breadcrumbs breadcrumbs,
                  String pTitle,
                  String urn,
                  Element pSummary)
  {
    Element result = getAtomElement(true, "feed", pTitle, urn, null, getLinkTypeForFeed(), null, pSummary, true, null);
    decorateFeed(result, breadcrumbs);
    return result;
  }

  void decorateFeed(Element feed, Breadcrumbs breadcrumbs) {
    if (breadcrumbs != null) {
      for (Breadcrumb breadcrumb : breadcrumbs) { 
        feed.addContent(getXmlLinkElement(breadcrumb.url, "breadcrumb", breadcrumb.title));
      }
    }

    // updated tag
    if (!ConfigurationManager.INSTANCE.getCurrentProfile().getSaveBandwidth()) {
      Element updated = getUpdatedTag();
      feed.addContent(updated);
    }
    
    // root catalog link
    // feed.addContent(getXmlLinkElement("../catalog.xml", "start", Localization.Main.getText("home.title")));
    // ITIMPI:  Should this be an absolute link to the catalog folder index. file rather than a relative one?
    feed.addContent(getXmlLinkElement("../index.xml", "start", Localization.Main.getText("home.title")));
  }
  
  private Element getAtomElement(boolean isRoot,
                                 String pElement,
                                 String pTitle,
                                 String urn,
                                 String filename,
                                 String pSummary,
                                 boolean includeAuthor,
                                 String icon)
  {
      return getAtomElement(isRoot,
                            pElement,
                            pTitle,
                            urn,
                            filename,
                            getLinkTypeForFeed(),
                            pSummary,
                            includeAuthor,
                            icon);
  }
  
  public Element getAtomElement(boolean isRoot,
                                String pElement,
                                String pTitle,
                                String urn,
                                String url,
                                String urlType,
                                String pSummary,
                                boolean includeAuthor,
                                String icon)
  {
      return getAtomElement(isRoot,
                            pElement,
                            pTitle,
                            urn,
                            url,
                            urlType,
                            null,       // Relation not required
                            pSummary,
                            includeAuthor,
                            icon);
  }
  
  public Element getAtomElement(boolean isRoot,
                                String elementName,
                                String title,
                                String id,
                                String url,
                                String urlType,
                                String urlRelation,
                                String content,
                                boolean includeAuthor,
                                String icon)
  {
    Element contentElement = null;
    if (Helper.isNotNullOrEmpty(content)) {
      contentElement = JDOM.INSTANCE.element("content").addContent(content);
      contentElement.setAttribute("type", "text");
    }
    return getAtomElement(isRoot,
                          elementName,
                          title,
                          id,
                          url,
                          urlType,
                          urlRelation,
                          contentElement,
                          includeAuthor,
                          icon);
  }

  public Element getAtomElement(boolean isRoot,
                                String elementName,
                                String title,
                                String id,
                                String url,
                                String urlType,
                                String urlRelation,
                                Element contentElement,
                                boolean includeAuthor,
                                String icon) {
    Element element;
    if (isRoot)
      element = JDOM.INSTANCE.rootElement(elementName,
                                          Namespace.Atom,   // Namepaces to be added ...
                                          Namespace.Atom,   // ITIMPI - not sure why this is repeated
                                          Namespace.Xhtml,
                                          Namespace.Opds);
    else
      element = JDOM.INSTANCE.element(elementName);
    
    // title
    Element titleElement = JDOM.INSTANCE.element("title").addContent(title);
    element.addContent(titleElement);
    
    // id
    Element idElement = JDOM.INSTANCE.element("id").addContent(id);
    element.addContent(idElement);

    // updated tag
    if (!ConfigurationManager.INSTANCE.getCurrentProfile().getSaveBandwidth()) {
      Element updated = getUpdatedTag();
      element.addContent(updated);
    }

    // content
    if (contentElement != null) {
      element.addContent(contentElement);
    }    

    // link
    if (Helper.isNotNullOrEmpty(url)) {
      element.addContent(getLinkElement(url, urlType, urlRelation, null));
    }
    
    // icon link
    if (Helper.isNotNullOrEmpty(icon))
      element.addContent(getIcon(icon));
    
    // add the feed author
    if (includeAuthor)
      element.addContent(getFeedAuthorElement());
    return element;
  }

}
