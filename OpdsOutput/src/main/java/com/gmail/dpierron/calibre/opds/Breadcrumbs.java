package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.tools.Helper;

import java.util.Vector;

public class Breadcrumbs extends Vector<Breadcrumb> {

  public Breadcrumbs() {
    super();
  }

  public static Breadcrumbs newBreadcrumbs(String title, String url) {
    return addBreadcrumb(null, title, url);
  }

  public static Breadcrumbs addBreadcrumb(Breadcrumbs pBreadcrumbs, String title, String url) {
    Breadcrumbs breadcrumbs = new Breadcrumbs();

    if (pBreadcrumbs != null)
      breadcrumbs.addAll(pBreadcrumbs);

    breadcrumbs.add(new Breadcrumb(title, url));

    return breadcrumbs;
  }

  public String getFilename() {
    return Helper.concatenateList("", this, "getFilename");
  }

  public String toString() {
    return Helper.concatenateList("/", this);
  }
}
