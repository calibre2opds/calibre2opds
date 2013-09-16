package com.gmail.dpierron.calibre.opds;
/**
 * Class that provides methods for manipulating a set of breadcrum.
 */

import com.gmail.dpierron.tools.Helper;

import java.util.Vector;

public class Breadcrumbs extends Vector<Breadcrumb> {

  public Breadcrumbs() {
    super();
  }

  public static Breadcrumbs newBreadcrumbs(String title, String url) {
    return addBreadcrumb(null, title, url);
  }

  /**
   * Take a set of breadcrumbs, and make a copy with a new one added at the end
   *
   * @param pBreadcrumbs    The existing set of breadcrumbs
   * @param title           The title for the new breadcrumb
   * @param url             The URL fro the new breadcrumb
   * @return
   */
  public static Breadcrumbs addBreadcrumb(Breadcrumbs pBreadcrumbs, String title, String url) {
    Breadcrumbs breadcrumbs = new Breadcrumbs();

    if (pBreadcrumbs != null)
      breadcrumbs.addAll(pBreadcrumbs);

    breadcrumbs.add(new Breadcrumb(title, url));

    return breadcrumbs;
  }

  /**
   * Get a unique filename using the breadcrumbs
   *
   * ITIMPI:  To keep the name as short as possible when recursing down
   *          many levels (to avoid hitting file system limits) if the text
   *          of one breadcrumb is the same as the start of the next one it
   *          is omitted.
   *
   * TODO:  Decide if this method may now be no longer needed with the new naming system.
   * @return
   */
  public String getFilename() {
    StringBuffer result = new StringBuffer();
    Breadcrumb lastElement = null;
    for (int i=0 ; i < this.size(); i++) {
      Breadcrumb thisElement = this.elementAt(i);
      if (lastElement == null) {
        lastElement = thisElement;
      } else {
        if (thisElement.toString().startsWith(lastElement.toString())) {
          lastElement = thisElement;
          continue;
        } else {
          result.append(lastElement.getFilename());
          lastElement = thisElement;
        }
      }
      if (i+1 == this.size()){
        result.append(lastElement.getFilename());
      }
    }
    return result.toString();
  }

  public String toString() {
    return Helper.concatenateList("/", this);
  }

  public static String getProgressText (Breadcrumbs breadcrumbs) {
    assert breadcrumbs.size() > 0;
    StringBuffer progressText = new StringBuffer();
    // Tidy up message removing redundant "starting with" ("splitByLetter.letter") entries
    // Not stricly necessary, but improves user experience.
    for (int i = 0 ; i < breadcrumbs.size() ; i++) {
      String thisElement = breadcrumbs.elementAt(i).toString();
      if (i != 0) {
        if (i < (breadcrumbs.size()-1)) {
          if (breadcrumbs.elementAt(i+1).toString().startsWith(thisElement)) {
            // Do not add an element if the next one starts with the same text!
            continue;
          }
        }
        progressText.append("/");
      }
      progressText.append(thisElement);

    }
    return progressText.toString();
  }
}
