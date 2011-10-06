package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.List;

public class FeaturedBooksSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(FeaturedBooksSubCatalog.class);

  private SplitOption splitOption;

  public FeaturedBooksSubCatalog(List<Book> books) {
    this(books, SplitOption.DontSplitNorPaginate);
  }

  public FeaturedBooksSubCatalog(List<Book> books, SplitOption splitOption) {
    super(books);
    this.splitOption = splitOption;
    sortBooks();
  }

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    sortBooksByTimestamp(getBooks());
  }

  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_featuredbooks.xml");
    String title = ConfigurationManager.INSTANCE.getCurrentProfile().getCustomCatalogTitle();
    String urn = "calibre:featuredbooks";

    String summary = "";
    if (getBooks().size() > 1)
      summary = Localization.Main.getText("allbooks.alphabetical", getBooks().size());
    else if (getBooks().size() == 1)
      summary = Localization.Main.getText("allbooks.alphabetical.single");

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    return getListOfBooks(pBreadcrumbs, getBooks(), 0, title, summary, urn, filename, splitOption,
        // #751211: Use external icons option
        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
            getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + StanzaConstants.ICONFILE_FEATURED :
            StanzaConstants.ICON_BOOKS);
  }
}
