package com.gmail.dpierron.calibre.opds;
/**
 * Class for implementing the All Books type sub-catalogs
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.List;

public class AllBooksSubCatalog extends BooksSubCatalog {
  private final static org.apache.log4j.Logger logger = Logger.getLogger(AllBooksSubCatalog.class);

  public AllBooksSubCatalog(List<Book> books) {
    super(books);
    sortBooks();
  }

  /**
   * Version of catalog constructor where we can force sort by Title if wanted
   * @param stuffToFilterOut
   * @param books
   * @param sortByTitle
   */
  public AllBooksSubCatalog(List<Object> stuffToFilterOut, List<Book> books, Boolean sortByTitle) {
    super(stuffToFilterOut, books);
    if (sortByTitle)
      sortBooksByTitle(getBooks());
    else
      sortBooks();
 }

  /**
   * Version of catalog constructor when we may want items filetered out
   * @param stuffToFilterOut
   * @param books
   */
  public AllBooksSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    sortBooks();
  }

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getOrderAllBooksBySeries()) {
      // sort the books by series
      sortBooksBySeries(getBooks());
    } else {
      // sort the books by title
      sortBooksByTitle(getBooks());
    }
  }

  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
   return getSubCatalogEntry(pBreadcrumbs, SplitOption.SplitByLetter);
  }

  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs, SplitOption splitOption) throws IOException {
      if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_books.xml");
    String title = Localization.Main.getText("allbooks.title");
    String urn = "calibre:books";

    String summary = "";
    if (getBooks().size() > 1)
      summary = Localization.Main.getText("allbooks.alphabetical", getBooks().size());
    else if (getBooks().size() == 1)
      summary = Localization.Main.getText("allbooks.alphabetical.single");

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    // return getListOfBooks(pBreadcrumbs, getBooks(), 0, title, summary, urn, filename, splitOption,
    return getListOfBooks(pBreadcrumbs, getBooks(), 0, title, summary, urn, filename, maxSplitLevels > 0 ? splitOption : SplitOption.DontSplit,
        // #751211: Use external icons option
        useExternalIcons ?
            (weAreAlsoInSubFolder ? "../" : "./") + Icons.ICONFILE_BOOKS :
            Icons.ICON_BOOKS);
  }
}
