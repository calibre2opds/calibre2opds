package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RecentBooksSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(RecentBooksSubCatalog.class);

  public RecentBooksSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    sortBooks();
  }

  public RecentBooksSubCatalog(List<Book> books) {
    super(books);
    sortBooks();
  }

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    // sort the books by timestamp
    Collections.sort(getBooks(), new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        Date ts1 = (o1 == null ? new Date() : o1.getTimestamp());
        Date ts2 = (o2 == null ? new Date() : o2.getTimestamp());
        return ts2.compareTo(ts1);
      }

    });

    setBooks(
        new Helper.ListCopier<Book>().copyList(getBooks(), ConfigurationManager.INSTANCE.getCurrentProfile().getBooksInRecentAdditions()));
  }

  public Element getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_recent.xml");
    String title = Localization.Main.getText("recent.title");
    String urn = "calibre:recent";

    String summary = "";
    if (getBooks().size() > 1)
      summary = Localization.Main.getText("recent.list", getBooks().size());
    else if (getBooks().size() == 1)
      summary = Localization.Main.getText("recent.list.single");

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    Element result = getListOfBooks(pBreadcrumbs, getBooks(), 0, title, summary, urn, filename, SplitOption.SplitByDate,
        // #751211: Use external icons option
        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
            getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + StanzaConstants.ICONFILE_RECENT :
            StanzaConstants.ICON_RECENT, Option.INCLUDE_TIMESTAMP);
    return result;
  }

}
