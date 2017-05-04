package com.gmail.dpierron.calibre.opds;
/**
 * Class providing methods and propetries for an indiviudal breadcrumb
 */
import com.gmail.dpierron.tools.Helper;
import org.jdom2.Element;

public class Breadcrumb {
  String title;
  String url;

  public Breadcrumb(String title, String url) {
    super();
    this.title = title;
    this.url = url;
  }

  public Element getXml() {
    Element link = JDOMManager.element("link");
    link.setAttribute("rel", "related");
    link.setAttribute("type", "application/atom+xml");
    if (Helper.isNotNullOrEmpty(title))
      link.setAttribute("title", title);
    link.setAttribute("href", url);
    return link;
  }

  public String toString() {
    return title;
  }

  public String getFilename() {
    String hash = Integer.toHexString(title.hashCode());
    return Helper.pad(hash, '0', 8);
  }
}
