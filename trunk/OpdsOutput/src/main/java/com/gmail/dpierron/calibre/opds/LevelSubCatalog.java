package com.gmail.dpierron.calibre.opds;
/**
 * Generate a complete sub-catalog level
 *
 * TODO:  Make the  necessary changes to use this class from the Catalog class
 *        That would have the benefit of keeping all understanding about generating
 *        a level in a single place.   However it would need to take into account
 *        how progress is reported unless this can be generalised better!
 */

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class LevelSubCatalog extends SubCatalog {
  private final static Logger logger = Logger.getLogger(LevelSubCatalog.class);

  private String title;
  private long now;

  public LevelSubCatalog(List<Book> books, String title) {
    super(books);
    this.title = title;
    setStuffToFilterOut(new Vector<Object>() {{add("dummy");}}); // needed to make SubCatalog.isInDeepLevel() know that we're a deep level
  }

  /**
   * Create a custom sub-catalog
   * TODO:  ITIMPI perhaps this should be in its own class?  Is it necessary?
   *
   * @param pBreadcrumbs
   * @return
   * @throws IOException
   */

 /*
  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs,
                                                       boolean inSubDir) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    if (logger.isDebugEnabled())
      logger.debug("creating level " + title);
    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());

    String filename = getCatalogBaseFolderFileName();
    String urlExt = catalogManager.getCatalogFileUrl(filename, true);
    String urn = "calibre:" + getGenerateFolder() + Constants.URN_SEPARATOR + getCatalogLevel();

    // specify that this is a deep level
    String summary = Localization.Main.getText("deeplevel.summary", Summarizer.INSTANCE.getBookWord(getBooks().size()));
    Element entry = getListOfBooks(pBreadcrumbs, getStuffToFilterOut(), inSubDir, summary, urn, null,
        useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_CUSTOM : Icons.ICON_CUSTOM);
    return new Composite<Element, String>(entry, urlExt);
  }
 */
  /**
   * Generate the standard set of catalog entries
   *
   * NOTE.  If at top level we need to be updating the progress dialog.
   *        In other caes we do not (maybe this needs revisiting?)
   *
   * @param pBreadcrumbs
   * @param stuffToFilterOut
   * @param summary
   * @param urn
   * @param splitOption
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
  public Element getCatalog(Breadcrumbs pBreadcrumbs,
      List<Object> stuffToFilterOut,
      boolean inSubDir,
      String summary,
      String urn,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {

    boolean atTopLevel = (pBreadcrumbs.size() == 1 && getCatalogLevel().length() == 0);
    CatalogCallbackInterface callback = CatalogContext.INSTANCE.callback; // Cache for efficiency

    String urlExt = catalogManager.getCatalogFileUrl(getCatalogBaseFolderFileName() + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);
    Breadcrumbs breadcrumbs = inSubDir ? Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt)  : pBreadcrumbs;

    Composite<Element, String> subCatalogEntry;
    Element entry;


    /* About entry */

    if (atTopLevel) {
      if (currentProfile.getIncludeAboutLink()) {
        logger.debug("Generating About entry");
        entry = FeedHelper.getAboutEntry(Localization.Main.getText("about.title", Constants.PROGTITLE), "urn:calibre2opds:about", Constants.HOME_URL,
                Localization.Main.getText("about.summary"), currentProfile.getExternalIcons() ? Icons.ICONFILE_ABOUT : Icons.ICON_ABOUT);
        if (entry != null)
          feed.addContent(entry);
      }
    }

    /* All books */

    if (atTopLevel)  callback.startCreateAllbooks(DataModel.INSTANCE.getListOfBooks().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateAllbooks()) {
      logger.debug("STARTED: Generating All books catalog");
      AllBooksSubCatalog allBooksSubCatalog = new AllBooksSubCatalog(stuffToFilterOut, getBooks());
      allBooksSubCatalog.setCatalogLevel(getCatalogLevel());
      String allBooksSummary = "";
      if (allBooksSubCatalog.getBooks().size() > 1)
        allBooksSummary = Localization.Main.getText("allbooks.alphabetical", getBooks().size());
      else if (getBooks().size() == 1)
        allBooksSummary = Localization.Main.getText("allbooks.alphabetical.single");
      entry = allBooksSubCatalog.getListOfBooks(breadcrumbs, getBooks(), getCatalogLevel().length() > 0, 0,          // from start
          Localization.Main.getText("allbooks.title"), allBooksSummary, Constants.INITIAL_URN_PREFIX + allBooksSubCatalog.getCatalogType(),
          allBooksSubCatalog.getCatalogBaseFolderFileName(), SplitOption.SplitByLetter,
          useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS, null);
      allBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup

      if (entry != null) {
        feed.addContent(entry);
      }

      logger.debug("COMPLETED: Generating All Books catalog");
    }
    if (atTopLevel) callback.endCreateAllbooks(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Authors */

    logger.debug("STARTED: Generating Authors catalog");
    if (atTopLevel)  callback.startCreateAuthors(DataModel.INSTANCE.getListOfAuthors().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateAuthors()) {
      AuthorsSubCatalog authorsSubCatalog = new AuthorsSubCatalog(stuffToFilterOut, getBooks());
      authorsSubCatalog.setCatalogLevel(getCatalogLevel());
      String authorsSummary = "";
      if (authorsSubCatalog.getAuthors().size() > 1)
        authorsSummary = Localization.Main.getText("authors.alphabetical", authorsSubCatalog.getAuthors().size());
      else if (authorsSubCatalog.getAuthors().size() == 1)
        authorsSummary = Localization.Main.getText("authors.alphabetical.single");
      entry = authorsSubCatalog.getSubCatalog(breadcrumbs,
                                              authorsSubCatalog.getAuthors(),// sTART WITH ALL AUTHORS
                                              getCatalogFolder().length() > 0,
                                              0,        // from start,
                                              Localization.Main.getText("authors.title"),
                                              authorsSummary,
                                              Constants.INITIAL_URN_PREFIX + authorsSubCatalog.getCatalogType() + authorsSubCatalog.getCatalogLevel(),
                                              authorsSubCatalog.getCatalogBaseFolderFileName(),
                                              SplitOption.SplitByLetter);
      authorsSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null)
          feed.addContent(entry);
      logger.debug("COMPLETED: Generating Authors catalog");
    }
    if (atTopLevel)  callback.endCreateAuthors(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Tags */

    if (atTopLevel)  callback.startCreateTags(DataModel.INSTANCE.getListOfTags().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateTags()) {
      logger.debug("STARTED: Generating tags catalog");
      TagsSubCatalog tagssubCatalog = (Helper.isNotNullOrEmpty(currentProfile.getSplitTagsOn()))
                                  ? new TagTreeSubCatalog(stuffToFilterOut, getBooks())
                                  : new TagListSubCatalog(stuffToFilterOut, getBooks());
      tagssubCatalog.setCatalogLevel(getCatalogLevel());
      entry = tagssubCatalog.getCatalog(breadcrumbs, pBreadcrumbs.size() > 1 || getCatalogLevel().length() > 0 /*inSubDir*/);
      tagssubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null)
        feed.addContent(entry);
      logger.debug("COMPLETED: Generating tags catalog");
    }
    if (atTopLevel)  callback.endCreateTags(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Series */

    if (atTopLevel)  callback.startCreateSeries(DataModel.INSTANCE.getListOfSeries().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateSeries()) {
      // bug c20-81  Need to allow for (perhaps unlikely) case where no books in library have a series entry set
      logger.debug("STARTED: Generating Series catalog");
      SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(stuffToFilterOut, getBooks());
      seriesSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = seriesSubCatalog.getSubCatalog(breadcrumbs, null,     // let it be derived from books
          getCatalogLevel().length() > 0, 0, Localization.Main.getText("series.title"), seriesSubCatalog.getSeries().size() > 1 ?
          Localization.Main.getText("series.alphabetical", seriesSubCatalog.getSeries().size()) :
          (seriesSubCatalog.getSeries().size() == 1 ? Localization.Main.getText("series.alphabetical.single") : ""),
          Constants.INITIAL_URN_PREFIX + Constants.URN_SEPARATOR + Constants.SERIES_TYPE + seriesSubCatalog.getCatalogLevel(), null,
          // let it be derived from catalog properties
          SplitOption.Paginate, true);
      seriesSubCatalog = null;  // Maybe not necesary - but explicit object cleanup for earlier resource release
      if (entry != null)
        feed.addContent(entry);
      logger.debug("COMPLETED: Generating Series catalog");
    }
    if (atTopLevel)  callback.endCreateSeries(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Recent books */

    if (atTopLevel) {
      int nbRecentBooks = Math.min(currentProfile.getBooksInRecentAdditions(), DataModel.INSTANCE.getListOfBooks().size());
      callback.startCreateRecent(nbRecentBooks);
    }
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateRecent()) {
      logger.debug("STARTED: Generating Recent books catalog");
      RecentBooksSubCatalog recentBooksSubCatalog = new RecentBooksSubCatalog(stuffToFilterOut, getBooks());
      recentBooksSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = recentBooksSubCatalog.getCatalog(breadcrumbs, getCatalogLevel().length() > 0);
      recentBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null) {
        feed.addContent(entry);
      }
      logger.debug("COMPLETED: Generating Recent books catalog");
    }
    if (atTopLevel)  callback.endCreateRecent(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Rated books */

    if (atTopLevel)  callback.startCreateRated(DataModel.INSTANCE.getListOfBooks().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateRatings()) {
      logger.debug("STARTED: Generating Rated books catalog");
      RatingsSubCatalog ratingsSubCatalog = new RatingsSubCatalog(stuffToFilterOut,getBooks());
      ratingsSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = ratingsSubCatalog.getCatalog(breadcrumbs,
                                           getCatalogLevel().length() > 0);
                                           ratingsSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null) {
        feed.addContent(entry);
      }
      logger.debug("COMPLETED: Generating Rated books catalog");
    }
    if (atTopLevel)  callback.endCreateRated(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* Featured catalog */
    // TODO:  Decide if this should be restricted to top level catalog - currentl assuming yes?

    now = System.currentTimeMillis();
    if (CatalogContext.INSTANCE.catalogManager.featuredBooksFilter != null) {
      logger.debug("STARTED: Generating Featured catalog");
      List<Book> featuredBooks = FilterHelper.filter(CatalogContext.INSTANCE.catalogManager.featuredBooksFilter, getBooks());
      if (featuredBooks.size() == 0) {
        logger.warn("No books found for Featured Books section");
      } else {
        callback.setFeaturedCount("" + featuredBooks.size() + " " + Localization.Main.getText("bookword.title"));
        FeaturedBooksSubCatalog featuredBooksSubCatalog = new FeaturedBooksSubCatalog(featuredBooks);
        if (atTopLevel)  callback.startCreateFeaturedBooks(featuredBooks.size());
        featuredBooksSubCatalog.setCatalogLevel(getCatalogLevel());
        Element featuredCEntry = featuredBooksSubCatalog.getFeaturedCatalog(breadcrumbs, inSubDir);
        featuredBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
        if (featuredCEntry != null) {
          feed.addContent(featuredCEntry);
        }
      }
      logger.debug("COMPLETED: Generating Featured catalog");
    }
    if (atTopLevel)  callback.endCreateFeaturedBooks(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();


    /* Custom catalogs */

    if (atTopLevel) {                          /* (only at top level) */
      now = System.currentTimeMillis();
      List<Composite<String, String>> customCatalogs = currentProfile.getCustomCatalogs();
      if (Helper.isNotNullOrEmpty(customCatalogs)) {
        logger.debug("STARTED: Generating custom catalogs");
        int pos = 1;
        callback.startCreateCustomCatalogs(customCatalogs.size());
        for (Composite<String, String> customCatalog : customCatalogs) {
          callback.checkIfContinueGenerating();
          String customCatalogTitle = customCatalog.getFirstElement();
          String customCatalogSearch = customCatalog.getSecondElement();
          if (Helper.isNotNullOrEmpty(customCatalogTitle)) {
            BookFilter customCatalogBookFilter = CatalogContext.INSTANCE.catalogManager.customCatalogsFilters.get(customCatalogTitle);
            if (customCatalogBookFilter != null) {
              // custom catalog
              if (logger.isDebugEnabled())
                logger.debug("STARTED: Generating custom catalog " + title);
              List<Book> customCatalogBooks = FilterHelper.filter(customCatalogBookFilter, getBooks());
              if (customCatalogBooks.size() == 0) {
                logger.warn("No books found for custom catalog " + customCatalogTitle);
              } else {
                if (Helper.isNotNullOrEmpty(customCatalogBooks)) {
                  String customFilename = getCatalogBaseFolderFileName();
                  String customUrl = catalogManager.getCatalogFileUrl(customFilename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);
                  Breadcrumbs custombreadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, customUrl);
                  LevelSubCatalog customSubCatalog = new LevelSubCatalog(customCatalogBooks, customCatalogTitle);
                  customSubCatalog.setCatalogType(Constants.CUSTOM_TYPE);
                  customSubCatalog.setCatalogFolder(Constants.CUSTOM_TYPE);
                  customSubCatalog.setCatalogLevel(custombreadcrumbs);
                  customSubCatalog.setCatalogBaseFilename(catalogManager.getInitialUr());
                  entry = customSubCatalog.getCatalog(custombreadcrumbs, null,    // No further filter at this point
                      inSubDir,    // Custom catalogs always in subDir
                      Localization.Main.getText("deeplevel.summary", Summarizer.INSTANCE.getBookWord(customCatalogBooks.size())),
                      "calibre:" + customSubCatalog.getCatalogFolder() + Constants.URN_SEPARATOR + customSubCatalog.getCatalogLevel(), null,
                      // Not sure of splitOption at this point
                      useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_CUSTOM : Icons.ICON_CUSTOM);
                  customSubCatalog = null;  // Maybe not necesary - but explicit object cleanup to ensure resources released
                  if (entry != null) {
                    feed.addContent(entry);
                  }
                }
              }
            } else {
              // external catalog
              if (logger.isDebugEnabled())
                logger.debug("STARTED: Adding external link " + title);

              String externalLinkUrl = customCatalog.getSecondElement();
              entry = FeedHelper.getExternalLinkEntry(customCatalogTitle, "urn:calibre2opds:externalLink" + (pos++), externalLinkUrl,
                  currentProfile.getExternalIcons() ? getIconPrefix(inSubDir) + Icons.ICONFILE_EXTERNAL : Icons.ICON_EXTERNAL);
              if (entry != null)
                feed.addContent(entry);
            }
          }
          callback.incStepProgressIndicatorPosition();

          callback.checkIfContinueGenerating();
        }
        logger.debug("COMPLETED: Generating custom catalogs");
      }
      callback.endCreateCustomCatalogs(System.currentTimeMillis() - now);
    }
    callback.checkIfContinueGenerating();

    String outputFilename = getCatalogBaseFolderFileName();
    if (inSubDir || getCatalogLevel().length() > 0 || getCatalogFolder().length() > 0){
      createFilesFromElement(feed, outputFilename, HtmlManager.FeedType.Catalog);
    } else {
      createFilesFromElement(feed, outputFilename, HtmlManager.FeedType.MainCatalog);
    }
    return FeedHelper.getCatalogEntry(title, urn, catalogManager.getCatalogFileUrl(outputFilename + Constants.XML_EXTENSION, inSubDir), summary, icon);

  }
}
