package com.gmail.dpierron.calibre.opds;

/**
 * Abstract class containing functions common to all catalog types
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SubCatalog {

  private final static Logger logger = Logger.getLogger(SubCatalog.class);

  private List<Book> books;
  List<Object> stuffToFilterOut;
  boolean isMainStep = true;

  public SubCatalog(List<Book> books) {
    this(null, books);
  }

  public SubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    setStuffToFilterOut(stuffToFilterOut);
    setBooks(books);
  }

  /**
   * Set the list of books to be included in this aa(sub) catalog
   *
   * @param books
   */
  void setBooks(List<Book> books) {
    this.books = null;
    if (Helper.isNotNullOrEmpty(stuffToFilterOut)) {
      this.books = filterOutStuff(books);
    }
    if (this.books == null)
      this.books = books;
  }

  List<Book> getBooks() {
    return books;
  }

  List<Object> getStuffToFilterOut() {
    return stuffToFilterOut;
  }

  List<Object> getStuffToFilterOutAnd(Object newStuff) {
    List<Object> result = new ArrayList<Object>();
    if (stuffToFilterOut != null)
      result.addAll(stuffToFilterOut);
    if (newStuff != null)
      result.add(newStuff);
    return result;
  }

  SubCatalog setStuffToFilterOut(List<Object> stuffToFilterOut) {
    this.stuffToFilterOut = stuffToFilterOut;
    return this;
  }

  List<Book> filterOutStuff(List<Book> originalBooks) {
    // by default, simply return the book list
    return originalBooks;
  }

  boolean isInDeepLevel() {
    return Helper.isNotNullOrEmpty(stuffToFilterOut);
  }

  HtmlManager getHtmlManager() {
    return CatalogContext.INSTANCE.getHtmlManager();
  }

  void setHtmlManager(HtmlManager htmlManager) {
    CatalogContext.INSTANCE.setHtmlManager(htmlManager);
  }

  CatalogManager getCatalogManager() {
    return CatalogContext.INSTANCE.getCatalogManager();
  }

  void setCatalogManager(CatalogManager catalogManager) {
    CatalogContext.INSTANCE.setCatalogManager(catalogManager);
  }

  public ImageManager getThumbnailManager() {
    return CatalogContext.INSTANCE.getThumbnailManager();
  }

  public void setThumbnailManager(ImageManager thumbnailManager) {
    CatalogContext.INSTANCE.setThumbnailManager(thumbnailManager);
  }

  public ImageManager getCoverManager() {
    return CatalogContext.INSTANCE.getCoverManager();
  }

  public void setCoverManager(ImageManager coverManager) {
    CatalogContext.INSTANCE.setCoverManager(coverManager);
  }

  /**
   * @return a result composed of the resulting OPDS entry, and the relative url to the subcatalog
   */
  public abstract Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException;

  public Element getSubCatalogLevel(Breadcrumbs pBreadcrumbs,
      List<Book> books,
      List<Object> stuffToFilterOut,
      String title,
      String summary,
      String urn,
      String filename,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {
    // generate an additional level
    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    try {
      fos = new FileOutputStream(outputFile);

      String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
      Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);

      Composite<Element, String> subCatalogEntry;
      Element entry;

      /* Tags */
      subCatalogEntry = TagSubCatalog.getTagSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
      if (subCatalogEntry != null) {
        entry = subCatalogEntry.getFirstElement();
        if (entry != null)
          feed.addContent(entry);
      }

      /* Authors */
      subCatalogEntry = new AuthorsSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
      if (subCatalogEntry != null) {
        entry = subCatalogEntry.getFirstElement();
        if (entry != null)
          feed.addContent(entry);
      }

      /* Series */
      subCatalogEntry = new SeriesSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
      if (subCatalogEntry != null) {
        entry = subCatalogEntry.getFirstElement();
        if (entry != null)
          feed.addContent(entry);
      }

      /* Recent books */
      if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateRecent()) {
        if (subCatalogEntry != null) {
          subCatalogEntry = new RecentBooksSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
          entry = subCatalogEntry.getFirstElement();
          if (entry != null)
            feed.addContent(entry);
        }
      }

      /* Rated books */
      if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateRatings()) {
        if (subCatalogEntry != null) {
          subCatalogEntry = new RatingsSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
          entry = subCatalogEntry.getFirstElement();
          if (entry != null)
            feed.addContent(entry);
        }
      }

      /* All books */
      if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateAllbooks()) {
        if (subCatalogEntry != null) {
          subCatalogEntry = new AllBooksSubCatalog(stuffToFilterOut, books).getSubCatalogEntry(breadcrumbs);
          entry = subCatalogEntry.getFirstElement();
          if (entry != null)
            feed.addContent(entry);
        }
      }

      // write the element to the file
      document.addContent(feed);
      JDOM.INSTANCE.getOutputter().output(document, fos);

    } finally {
      if (fos != null)
        fos.close();
    }

    // create the same file as html
    getHtmlManager().generateHtmlFromXml(document, outputFile);
    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogLevel  Breadcrumbs=" + pBreadcrumbs.toString());
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    return FeedHelper.INSTANCE.getCatalogEntry(title, urn, getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, weAreAlsoInSubFolder), summary,
        // #751211: Use external icons option
        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
            getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + Icons.ICONFILE_TAGS :
            Icons.ICON_TAGS);

  }

  String getFilenamePrefix(Breadcrumbs pBreadcrumbs) {
    if (isInDeepLevel())
      return pBreadcrumbs.getFilename() + "_";
    else
      return "";
  }

  public boolean isMainStep() {
    return isMainStep;
  }

  public void setMainStep(boolean mainStep) {
    isMainStep = mainStep;
  }
}
