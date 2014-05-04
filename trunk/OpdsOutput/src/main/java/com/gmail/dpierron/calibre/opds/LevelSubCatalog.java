package com.gmail.dpierron.calibre.opds;
/**
 * Generate a complete sub-catalog level
 *
 * TODO:  Make the  necessary changes to use this class from the Catalog class
 *        That would have the benefit of keeping all understanding about generating
 *        a level in a single place.   However it would need to take into account
 *        how progress is reported unless this can be generalised better!
 */

import com.gmail.dpierron.calibre.configuration.CustomCatalogEntry;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;

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
   *   Generation of Custom catalogs is broken into its own routine as
   *   we may want to generate them in front of standar sections, or after
   *   them depending on configuration settings for each custom catalog.
   */

  private void generateCustomCatalogs(
      Breadcrumbs pBreadcrumbs,
      Element feed,
      boolean atTopLevel,
      boolean inSubDir,
      boolean atTop)
            throws IOException {

    Element entry;

    logger.debug("STARTED: Generating custom catalogs");
    /* (only at top level) */
    if (! atTopLevel) {
      logger.trace("EXIT:  Not at top level");
      return;
    }
    now = System.currentTimeMillis();
    List<CustomCatalogEntry> customCatalogs = currentProfile.getCustomCatalogs();
    if (Helper.isNullOrEmpty(customCatalogs)) {
      logger.trace("ENDED: No Custom Catalogs set");
    } else {
      int pos = 1;
      CatalogManager.INSTANCE.callback.startCreateCustomCatalogs(customCatalogs.size());
      Map<String, BookFilter> customCatalogsFilters = CatalogManager.INSTANCE.customCatalogsFilters;
      for (CustomCatalogEntry customCatalog : customCatalogs) {
        CatalogManager.INSTANCE.callback.checkIfContinueGenerating();
        String customCatalogTitle = customCatalog.getLabel();
        String customCatalogSearch = customCatalog.getValue();
        logger.trace("Current CustomCatalog is '" + customCatalogTitle + "'");
        // check if this custom catalog is wanted in this position?
        if (customCatalog.getAtTop() != atTop) {
          logger.trace("IGNORED: AtTop setting means skip at the moment.");
          continue;
        }

        if (Helper.isNotNullOrEmpty(customCatalogTitle) && (!customCatalogTitle.equals(Constants.CUSTOMCATALOG_DEFAULT_TITLE))
            // && (Helper.isNotNullOrEmpty(customCatalogsFilters))
            && (!customCatalogTitle.equals(Constants.CUSTOMCATALOG_DEFAULT_SEARCH))) {
          BookFilter customCatalogBookFilter = null;
          if (customCatalogsFilters != null) {
            customCatalogBookFilter = customCatalogsFilters.get(customCatalogTitle);
          }
          if (customCatalogBookFilter == null) {

            // external catalog

            String externalLinkUrl = customCatalog.getValue();
            if (logger.isDebugEnabled()) {
              logger.debug("Adding external link '" + customCatalogTitle + "', URLValue=" + externalLinkUrl);
            }
            boolean opdsLink = false;
            if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_OPDSURL.toUpperCase())) {
              externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_OPDSURL.length());
              opdsLink = true;
              ;
            } else if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_HTMLURL.toUpperCase())) {
              externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_HTMLURL.length());
              opdsLink = false;
            } else if (externalLinkUrl.toUpperCase().endsWith(".XML") || externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_OPDSURL.toUpperCase())) {
              opdsLink = true;
              // Strip off the OPDS part if it precedes a HTTP type URL as this is a special case
              if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_OPDSHTTP.toUpperCase())) {
                externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_OPDS.length());
              }
            }
            // Remove any quotes surrounding yje URL
            if ((externalLinkUrl.length() > 2) && externalLinkUrl.startsWith("\"") && externalLinkUrl.endsWith("\"")) {
              externalLinkUrl = externalLinkUrl.substring(1, externalLinkUrl.length() - 2);
            }
            // Now add the external link to the feed
            entry = FeedHelper.getExternalLinkEntry(customCatalogTitle, Localization.Main.getText("content.externalLink"), opdsLink,
                "urn:calibre2opds:externalLink" + (pos++), externalLinkUrl,
                currentProfile.getExternalIcons() ? getIconPrefix(inSubDir) + Icons.ICONFILE_EXTERNAL : Icons.ICON_EXTERNAL);
            if (entry != null) {
              feed.addContent(entry);
            }
          } else {

            // internal custom catalog (search based)

            if (logger.isDebugEnabled())
              logger.debug("STARTED: Generating custom catalog " + customCatalogTitle);
            List<Book> customCatalogBooks = FilterHelper.filter(customCatalogBookFilter, getBooks());
            if (customCatalogBooks.size() == 0) {
              logger.warn("No books found for custom catalog " + customCatalogTitle);
            } else {
              if (Helper.isNullOrEmpty(customCatalogBooks)) {
                logger.warn("Custom Catalog '" + customCatalogTitle + "' not generated as 0 bo0oks match criteria");
              } else {
                LevelSubCatalog customSubCatalog = new LevelSubCatalog(customCatalogBooks, customCatalogTitle);
                customSubCatalog.setCatalogType(Constants.CUSTOM_TYPE);
                customSubCatalog.setCatalogFolder(Constants.CUSTOM_TYPE);
                // String customFilename = getCatalogBaseFolderFileName();
                // String customUrl = catalogManager.getCatalogFileUrl(customFilename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);
                // Breadcrumbs custombreadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, customUrl);
                // customSubCatalog.setCatalogLevel(custombreadcrumbs);
                customSubCatalog.setCatalogLevel(Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, null));
                customSubCatalog.setCatalogBaseFilename(CatalogManager.INSTANCE.getInitialUr());
                String customFilename = customSubCatalog.getCatalogBaseFolderFileName();
                String customUrl = CatalogManager.INSTANCE.getCatalogFileUrl(customFilename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1 || inSubDir);
                Breadcrumbs custombreadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, customUrl);
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
            customCatalogBooks = null;
            if (logger.isDebugEnabled())
              logger.debug("ENDED: Generating custom catalog " + customCatalogTitle);
          }
        }
        CatalogManager.INSTANCE.callback.incStepProgressIndicatorPosition();
      }
      logger.debug("COMPLETED: Generating custom catalogs");
      if (atTopLevel)
        CatalogManager.reportRamUsage();
    }
    CatalogManager.INSTANCE.callback.endCreateCustomCatalogs(System.currentTimeMillis() - now);
  }

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

    CatalogCallbackInterface callback = CatalogManager.INSTANCE.callback; // Cache for efficiency

    String urlExt = CatalogManager.INSTANCE.getCatalogFileUrl(getCatalogBaseFolderFileName() + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, inSubDir || pBreadcrumbs.size() > 1);
    Breadcrumbs breadcrumbs = inSubDir ? Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt) : pBreadcrumbs;

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

    /* Featured catalog */
    // TODO:  Decide if this should be restricted to top level catalog - currentl assuming yes?

    now = System.currentTimeMillis();
    if (CatalogManager.INSTANCE.featuredBooksFilter != null) {
      logger.debug("STARTED: Generating Featured catalog");
      List<Book> featuredBooks = FilterHelper.filter(CatalogManager.INSTANCE.featuredBooksFilter, getBooks());
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
      if (atTopLevel) CatalogManager.reportRamUsage();
    }
    if (atTopLevel)  callback.endCreateFeaturedBooks(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();


    // Custom catalogs when above standard entries
    generateCustomCatalogs(pBreadcrumbs, feed, atTopLevel, inSubDir, true);
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
      if (atTopLevel) CatalogManager.reportRamUsage();
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
      if (atTopLevel) CatalogManager.reportRamUsage();
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
      entry = seriesSubCatalog.getSubCatalog(breadcrumbs,
                                             null,     // let it be derived from books
                                             getCatalogLevel().length() > 0,
                                             0,
                                             Localization.Main.getText("series.title"),
                                             seriesSubCatalog.getSeries().size() > 1
                                                ? Localization.Main.getText("series.alphabetical", seriesSubCatalog.getSeries().size())
                                                : (seriesSubCatalog.getSeries().size() == 1 ? Localization.Main.getText("series.alphabetical.single") : ""),
                                            null,       // urn:      let it be derived from catalog properties,
                                            null,       // filename: let it be derived from catalog properties
                                            SplitOption.SplitByLetter,
                                            false);     // seriesWod: Do NOT add to series title
      seriesSubCatalog = null;  // Maybe not necesary - but explicit object cleanup for earlier resource release
      if (entry != null)
        feed.addContent(entry);
      logger.debug("COMPLETED: Generating Series catalog");
      if (atTopLevel) CatalogManager.reportRamUsage();
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
      if (atTopLevel) CatalogManager.reportRamUsage();
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
      if (atTopLevel) CatalogManager.reportRamUsage();
    }
    if (atTopLevel)  callback.endCreateRated(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    /* All books */

    if (atTopLevel) {
      callback.startCreateAllbooks(DataModel.INSTANCE.getListOfBooks().size());
    }
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
      if (atTopLevel) CatalogManager.reportRamUsage();
    }
    if (atTopLevel) callback.endCreateAllbooks(System.currentTimeMillis() - now);
    callback.checkIfContinueGenerating();

    generateCustomCatalogs(pBreadcrumbs, feed, atTopLevel, inSubDir, false);
    callback.checkIfContinueGenerating();

    String outputFilename = getCatalogBaseFolderFileName();
    if (inSubDir || getCatalogLevel().length() > 0 || getCatalogFolder().length() > 0){
      createFilesFromElement(feed, outputFilename, HtmlManager.FeedType.Catalog);
    } else {
      createFilesFromElement(feed, outputFilename, HtmlManager.FeedType.MainCatalog);
    }

    // #c2o-214
    // A check to see that all cross-refrence targets have been generated
    // The only reason they would not have been is that the relevant section was suppressed

    boolean foundreference;
    do {
      foundreference = false;
      if (!atTopLevel || !currentProfile.getGenerateCrossLinks()){
        break;
      }
      for (Book book : DataModel.INSTANCE.getListOfBooks()) {
        if (book.isDone() || !book.isReferenced()) {
          continue;
        }
        List<Book> books = Arrays.asList(book);
        BooksSubCatalog booksSubCatalog = new BooksSubCatalog(books) {};
        booksSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          booksSubCatalog.getBookEntry(pBreadcrumbs, book);
          foundreference = true;
          assert book.isDone();
        } catch (Exception e) {
          logger.error("Error when generating author from book cross-link");
          book.setDone();
        }
      }
      for (Author author : DataModel.INSTANCE.getListOfAuthors()) {
        if (author.isDone() || !author.isReferenced()) {
          continue;
        }
        List<Book> authorBooks = DataModel.INSTANCE.getMapOfBooksByAuthor().get(author);
        if (authorBooks.size() < 2 && !currentProfile.getSingleBookCrossReferences())
          continue;
        AuthorsSubCatalog authorsSubCatalog = new AuthorsSubCatalog(authorBooks);
        authorsSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          authorsSubCatalog.getAuthorEntry(pBreadcrumbs, author, DataModel.INSTANCE.getMapOfBooksByAuthor().get(author));
          foundreference = true;
          assert author.isDone();
        } catch (Exception e) {
          logger.error("Error when generating author from book cross-link");
          author.setDone();
        }
      }
      for(Series series : DataModel.INSTANCE.getListOfSeries()){
        if (series.isDone() || ! series.isReferenced()) {
          continue;
        }
        SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(DataModel.INSTANCE.getMapOfBooksBySeries().get(series));
        seriesSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          seriesSubCatalog.getSeriesEntry(pBreadcrumbs, series, null, false);
          foundreference = true;
          assert series.isDone();
        } catch (Exception e) {
          logger.error("Error when generating Series from book cross-link");
          series.setDone();
        }
      }

      for (Tag tag : DataModel.INSTANCE.getListOfTags()) {
        if (tag.isDone() || !tag.isReferenced()) {
          continue;
        }
        List<Book> tagBooks = DataModel.INSTANCE.getMapOfBooksByTag().get(tag);
        if (tagBooks.size() < 2 && !currentProfile.getSingleBookCrossReferences()) {
          continue;
        }
        TagsSubCatalog tagsSubCatalog = new TagListSubCatalog(DataModel.INSTANCE.getMapOfBooksByTag().get(tag));
        tagsSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          tagsSubCatalog.getTagEntry(null, tag, null, null);
          foundreference = true;
          assert tag.isDone();
        } catch (Exception e) {
          logger.error("Error when generating tag from book cross-link");
          tag.setDone();
        }
      }

/*
        for (BookRating rating : DataModel.INSTANCE.getlistof= book.getRating();
          if (!rating.isDone() || isRatingCrossReferences(book)) {
            if (! rating.isReferenced()) break;
            List<Book> ratingBooks = DataModel.INSTANCE.getMapOfBooksByRating().get(rating);
            if (isRatingCrossReferences(book)) {
              break;
            }
            RatingsSubCatalog ratingSubCatalog = new RatingsSubCatalog(DataModel.INSTANCE.getMapOfBooksByRating().get(rating));
            ratingSubCatalog.setCatalogLevel(getCatalogLevel());
            try {
              ratingSubCatalog.getRatingEntry(null, rating, null);
            } catch (Exception e) {
              logger.error("Error when generating rating '" + rating.getValue() + "' from book cross-link");
            }
          }
*/
    } while (foundreference);

    return FeedHelper.getCatalogEntry(title, urn, CatalogManager.INSTANCE.getCatalogFileUrl(outputFilename + Constants.XML_EXTENSION, inSubDir), summary, icon);

  }
}
