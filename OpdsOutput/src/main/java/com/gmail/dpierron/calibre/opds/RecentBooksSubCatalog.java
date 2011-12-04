package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
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
    sortBooksByTimestamp(getBooks());
    setBooks(new Helper.ListCopier<Book>().copyList(getBooks(), ConfigurationManager.INSTANCE.getCurrentProfile().getBooksInRecentAdditions()));
  }

  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
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
    String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, weAreAlsoInSubFolder);
    Element result = getListOfBooks(pBreadcrumbs, getBooks(), 0, title, summary, urn, filename, SplitOption.SplitByDate,
        // #751211: Use external icons option
        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
            (weAreAlsoInSubFolder ? "../" : "./") + Icons.ICONFILE_RECENT :
            Icons.ICON_RECENT, Option.INCLUDE_TIMESTAMP).getFirstElement();
    return new Composite<Element, String>(result, urlInItsSubfolder);
  }

}
