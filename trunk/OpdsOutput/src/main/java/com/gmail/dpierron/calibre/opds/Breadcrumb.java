package com.gmail.dpierron.calibre.opds;

import org.jdom.Element;

import com.gmail.dpierron.tools.Helper;

public class Breadcrumb {
  String title;
  String url;
  public Breadcrumb(String title, String url) {
    super();
    this.title = title;
    this.url = url;
  }
  
  public Element getXml() {
    Element link = JDOM.INSTANCE.element("link");
    link.setAttribute("rel","related");
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
