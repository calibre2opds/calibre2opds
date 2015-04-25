package com.gmail.dpierron.calibre.opds;
/**
 *
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.GenericDataObject;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom2.Element;

import java.io.IOException;
import java.util.List;

public class FeaturedBooksSubCatalog extends SubCatalog {
  private final static Logger logger = Logger.getLogger(FeaturedBooksSubCatalog.class);

  private SplitOption splitOption;

  public FeaturedBooksSubCatalog(List<Book> books) {
    this(books, SplitOption.DontSplitNorPaginate);
    setCatalogType(Constants.FEATURED_TYPE);
  }

  public FeaturedBooksSubCatalog(List<Book> books, SplitOption splitOption) {
    super(books);
    this.splitOption = splitOption;
    sortBooks();
    setCatalogType(Constants.FEATURED_TYPE);
  }

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    sortBooksByTimestamp(getBooks());
  }

  public Element getFeaturedCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String filename = getCatalogBaseFolderFileName();
    String title = currentProfile.getFeaturedCatalogTitle();
    String urn = Constants.INITIAL_URN_PREFIX + getCatalogType();

    String summary = "";
    if (getBooks().size() > 1)
      summary = Localization.Main.getText("allbooks.alphabetical", getBooks().size());
    else if (getBooks().size() == 1)
      summary = Localization.Main.getText("allbooks.alphabetical.single");

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());
/*
    return getListOfBooks(pBreadcrumbs,
                          getBooks(),
                          inSubDir,
                          0,
                          title,
                          summary,
                          urn,
                          filename,
                          splitOption,
        // #751211: Use external icons option
        useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_FEATURED : Icons.ICON_FEATURED, null);
*/
  return null;
  }

  /**
   *
   * @param pBreadcrumbs
   * @param obj
   * @param options
   * @return
   * @throws IOException
   */

  public Element getDetailedEntry(Breadcrumbs pBreadcrumbs,
      Object obj,
      Option... options) throws IOException {
    assert false : "getDetailedEntry should never be called";
    return null;
  }
}
