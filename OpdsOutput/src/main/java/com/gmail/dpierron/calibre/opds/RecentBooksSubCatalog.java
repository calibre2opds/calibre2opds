package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
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
    setCatalogType(Constants.RECENT_TYPE);
  }

  public RecentBooksSubCatalog(List<Book> books) {
    super(books);
    sortBooks();
    setCatalogType(Constants.RECENT_TYPE);
  }

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    sortBooksByTimestamp(getBooks());
    setBooks(new Helper.ListCopier<Book>().copyList(getBooks(), currentProfile.getBooksInRecentAdditions()));
  }

  public Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String filename = getCatalogBaseFolderFileName();
    String title = Localization.Main.getText("recent.title");
    String urn = Constants.INITIAL_URN_PREFIX + getCatalogType();

    String summary = "";
    if (getBooks().size() > 1)
      summary = Localization.Main.getText("recent.list", getBooks().size());
    else if (getBooks().size() == 1)
      summary = Localization.Main.getText("recent.list.single");

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());
    // String urlInItsSubfolder = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1);
    Element result = getListOfBooks(pBreadcrumbs,
                                    getBooks(),
                                    inSubDir,
                                    0,
                                    title,
                                    summary,
                                    urn,
                                    filename,
                                    SplitOption.SplitByDate,
                                    // #751211: Use external icons option
                                    useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_RECENT : Icons.ICON_RECENT, null,     // No first element
                                    Option.INCLUDE_TIMESTAMP);

    return result;
  }

}
