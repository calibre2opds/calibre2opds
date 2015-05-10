package com.gmail.dpierron.calibre.opds;
/**
 * Class that provides methods for manipulating a set of breadcrumbs.
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
   * Convert the breadcrumbs ro a string with / between elements
   *
   * @return
   */
  public String toString() {
    return Helper.concatenateList("/", this);
  }

  /**
   * Create the string to be displayed as the progress message
   * Tries to some optimisation to keep message length down.
   *
   * @param breadcrumbs
   * @return
   */
  public static String getProgressText (Breadcrumbs breadcrumbs) {
    assert breadcrumbs.size() > 0;
    StringBuffer progressText = new StringBuffer();
    // Tidy up message removing redundant "starting with" ("splitByLetter.letter") entries
    // Not stricly necessary, but improves user experience.
    for (int i = 1 ; i < breadcrumbs.size() ; i++) {
      String thisElement = breadcrumbs.elementAt(i).toString();
      if (i != 1) {
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
