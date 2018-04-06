package com.gmail.dpierron.calibre.opds;
/**
 * Generate a complete sub-catalog level
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.CustomCatalogEntry;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

import java.io.IOException;
import java.util.*;

public class LevelSubCatalog extends SubCatalog {
  private final static Logger logger = LogManager.getLogger(LevelSubCatalog.class);

  private String title;

  public LevelSubCatalog(List<Book> books, String title) {
    super(books);
    this.title = title;
    setStuffToFilterOut(new Vector<Object>() {{add("dummy");}}); // needed to make SubCatalog.isInDeepLevel() know that we're a deep level
  }

  /**
   *   Generation of Custom catalogs is broken into its own routine as
   *   we may want to generate them in front of standard sections, or after
   *   them depending on configuration settings for each custom catalog.
   *
   * @param pBreadcrumbs
   * @param feed
   * @param inSubDir
   * @param atTop
   * @throws IOException
   */
  private void generateCustomCatalogs(
      Breadcrumbs pBreadcrumbs,
      Element feed,
      boolean inSubDir,
      boolean atTop)
            throws IOException {

    Element entry;

    if (logger.isDebugEnabled()) logger.debug("STARTED: Generating custom catalogs");
    List<CustomCatalogEntry> customCatalogs = currentProfile.getCustomCatalogs();
    CatalogManager.callback.startCreateCustomCatalogs(customCatalogs.size());
    if (Helper.isNullOrEmpty(customCatalogs)) {
      logger.trace("ENDED: No Custom Catalogs set");
    } else {
      int pos = 1;
      Map<String, BookFilter> customCatalogsFilters = CatalogManager.customCatalogsFilters;
      for (CustomCatalogEntry customCatalog : customCatalogs) {
        CatalogManager.callback.checkIfContinueGenerating();
        String customCatalogTitle = customCatalog.getLabel();
        String customCatalogSearch = customCatalog.getValue();
        logger.trace("Current CustomCatalog is '" + customCatalogTitle + "'");
        // check if this custom catalog is wanted in this position?
        if (customCatalog.getAtTop() != atTop) {
          logger.trace("IGNORED: AtTop setting means skip at the moment.");
          continue;
        }

        if (Helper.isNotNullOrEmpty(customCatalogTitle)
        && (!customCatalogTitle.equals(Constants.CUSTOMCATALOG_DEFAULT_TITLE))
        && (!customCatalogSearch.equals(Constants.CUSTOMCATALOG_DEFAULT_SEARCH))) {
          BookFilter customCatalogBookFilter = null;
          if (customCatalogsFilters != null) {
            customCatalogBookFilter = customCatalogsFilters.get(customCatalogTitle);
          }
          if (customCatalogBookFilter == null) {

            // external catalog

            String externalLinkUrl = customCatalog.getValue();
            logger.info("Generating custom catalog: " + customCatalogTitle);
            if (logger.isDebugEnabled()) logger.debug("Adding external link '" + customCatalogTitle + "', URLValue=" + externalLinkUrl);

            boolean opdsLink = false;
            // Check if we are linking to an external OPDS library
            if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_OPDSURL.toUpperCase())) {
              externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_OPDSURL.length());
              opdsLink = true;
            // Is this a standard HTML link?
            } else if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_HTMLURL.toUpperCase())) {
              externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_HTMLURL.length());
              opdsLink = false;
            // Assume explicit xml file are OPDS links
            } else if (externalLinkUrl.toUpperCase().endsWith(".XML")) {
                opdsLink = true;
              // Strip off the OPDS part if it precedes a HTTP type URL as this is a special case
              if (externalLinkUrl.toUpperCase().startsWith(Constants.CUSTOMCATALOG_OPDSHTTP.toUpperCase())) {
                externalLinkUrl = externalLinkUrl.substring(Constants.CUSTOMCATALOG_OPDS.length());
              }
            }
            // Remove any quotes surrounding yje URL
            if (externalLinkUrl.length() > 2) {
              if (externalLinkUrl.startsWith("\"")) {
                externalLinkUrl = externalLinkUrl.substring(1);
              }
              if (externalLinkUrl.endsWith("\"")) {
                externalLinkUrl = externalLinkUrl.substring(0, externalLinkUrl.length()-1);
              }
            }
            // Now add the external link to the feed
            entry = FeedHelper.getExternalLinkEntry(customCatalogTitle, Localization.Main.getText("content.externalLink"), opdsLink,
                "urn:calibre2opds:externalLink" + (pos++), externalLinkUrl,
                currentProfile.getExternalIcons() ? getIconPrefix(inSubDir) + Icons.ICONFILE_EXTERNAL : Icons.ICON_EXTERNAL);

            if (entry != null) {
              if (currentProfile.getNewWindowForCustomExternalLinks()) {
                List<Element> links = entry.getChildren();
                if (links != null) {
                  for (int i =0; i < links.size(); i++) {
                    Element link = links.get(i);
                    if (link.getName().equals("link")) {
                      link.setAttribute("target", "_blank");
                      break;
                    }
                  }
                }

              }
              feed.addContent(entry);
            }
          } else {

            // internal custom catalog (search based)

            List<Book> customCatalogBooks = FilterHelper.filter(customCatalogBookFilter, getBooks());
            int nb = customCatalogBooks.size();
            String s;
            switch ((int)nb) {
              case 0:
                s = Localization.Main.getText("bookword.none", nb);
                break;
              case 1:
                s = Localization.Main.getText("bookword.one", nb);
                break;
              default:
                s = Localization.Main.getText("bookword.many", nb);
                break;
            }
            logger.info("Generating custom catalog: " + customCatalogTitle + " (" + s + ")");
            if (Helper.isNullOrEmpty(customCatalogBooks)) {
              logger.warn(Localization.Main.getText("error.customCatalogEmpty",customCatalogTitle) + " (" + customCatalogSearch + ")" ); Helper.statsWarnings++;
            } else {
              LevelSubCatalog customSubCatalog = new LevelSubCatalog(customCatalogBooks, customCatalogTitle);
              customSubCatalog.setCatalogType(Constants.CUSTOM_TYPE);
              customSubCatalog.setCatalogFolder(Constants.CUSTOM_TYPE);
              // String customFilename = getCatalogBaseFolderFileName();
              // String customUrl = catalogManager.getCatalogFileUrl(customFilename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);
              // Breadcrumbs custombreadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, customUrl);
              // customSubCatalog.setCatalogLevel(custombreadcrumbs);
              customSubCatalog.setCatalogLevel(Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, null));
              customSubCatalog.setCatalogBaseFilename(CatalogManager.getInitialUr());
              String customFilename = customSubCatalog.getCatalogBaseFolderFileName();
              String customUrl = CatalogManager.getCatalogFileUrl(customFilename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1 || inSubDir);
              Breadcrumbs custombreadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, customCatalogTitle, customUrl);
              entry = customSubCatalog.getCatalog(custombreadcrumbs,
                  null,    // No further filter at this point
                  inSubDir,    // Custom catalogs always in subDir
                  Localization.Main.getText("deeplevel.summary", Summarizer.getBookWord(customCatalogBooks.size())),
                  "calibre:" + customSubCatalog.getCatalogFolder() + Constants.URN_SEPARATOR + customSubCatalog.getCatalogLevel(),
                  null,
                  // Not sure of splitOption at this point
                  useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_CUSTOM : Icons.ICON_CUSTOM);
              customSubCatalog = null;  // Maybe not necesary - but explicit object cleanup to ensure resources released
              if (entry != null) {
                feed.addContent(entry);
              }
              CatalogManager.callback.incStepProgressIndicatorPosition();
            }
            customCatalogBooks = null;
            if (logger.isDebugEnabled()) logger.debug("ENDED: Generating custom catalog " + customCatalogTitle);
          }
        }
      }
      if (logger.isDebugEnabled()) logger.debug("COMPLETED: Generating custom catalogs");
      CatalogManager.recordRamUsage("After generating Custom Catalogs");
    }
    CatalogManager.callback.endCreateCustomCatalogs();
    CatalogManager.callback.showMessage("");
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

    boolean atTopLevel = (pBreadcrumbs.size() == 0 && getCatalogLevel().length() == 0);

    String urlExt = CatalogManager.getCatalogFileUrl(getCatalogBaseFolderFileName() + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, inSubDir || pBreadcrumbs.size() > 1);
    // Breadcrumbs breadcrumbs = inSubDir ? Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt) : pBreadcrumbs;
    Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);

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
    // TODO:  Decide if this should be restricted to top level catalog - currently assuming yes?

    if (CatalogManager.featuredBooksFilter != null) {
      logger.debug("STARTED: Generating Featured catalog");
      List<Book> featuredBooks = FilterHelper.filter(CatalogManager.featuredBooksFilter, getBooks());
      if (featuredBooks.size() == 0) {
        logger.warn("No books found for Featured Books section"); Helper.statsWarnings++;
      } else {
        CatalogManager.callback.setFeaturedCount("" + featuredBooks.size() + " " + Localization.Main.getText("bookword.title"));
        FeaturedBooksSubCatalog featuredBooksSubCatalog = new FeaturedBooksSubCatalog(featuredBooks);
        if (atTopLevel)   CatalogManager.callback.startCreateFeaturedBooks(featuredBooks.size());
        featuredBooksSubCatalog.setCatalogLevel(getCatalogLevel());
        Element featuredCEntry = featuredBooksSubCatalog.getFeaturedCatalog(breadcrumbs, inSubDir);
        featuredBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
        if (featuredCEntry != null) {
          feed.addContent(featuredCEntry);
        }
      }
      logger.debug("COMPLETED: Generating Featured catalog");
      if (atTopLevel) CatalogManager.recordRamUsage("After generating Featured Catalog");
    }
    if (atTopLevel)   CatalogManager.callback.endCreateFeaturedBooks();
    CatalogManager.callback.checkIfContinueGenerating();


    // Custom catalogs when above standard entries
    if (atTopLevel) generateCustomCatalogs(breadcrumbs, feed, inSubDir, true);
    CatalogManager.callback.checkIfContinueGenerating();

    /* Authors */

    logger.debug("STARTED: Generating Authors catalog");
    if (atTopLevel)  CatalogManager.callback.startCreateAuthors(DataModel.getListOfAuthors().size());
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
                                              inSubDir,
                                              0,                            // from start,
                                              Localization.Main.getText("authors.title"),
                                              authorsSummary,
                                              Constants.INITIAL_URN_PREFIX + authorsSubCatalog.getCatalogType() + authorsSubCatalog.getCatalogLevel(),
                                              authorsSubCatalog.getCatalogBaseFolderFileName(),
                                              SplitOption.SplitByLetter);
      authorsSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null)
          feed.addContent(entry);
      if (atTopLevel) CatalogManager.recordRamUsage("After Generating Author Catalog");
      logger.debug("COMPLETED: Generating Authors catalog");
    }
    if (atTopLevel)   CatalogManager.callback.endCreateAuthors();
    CatalogManager.callback.checkIfContinueGenerating();

    /* Series */

    if (atTopLevel)   CatalogManager.callback.startCreateSeries(DataModel.getListOfSeries().size());
    if (currentProfile.getGenerateSeries()) {
      // bug c20-81  Need to allow for (perhaps unlikely) case where no books in library have a series entry set
      logger.debug("STARTED: Generating Series catalog");
      SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(stuffToFilterOut, getBooks());
      seriesSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = seriesSubCatalog.getSubCatalog(breadcrumbs,
          null,                   // let it be derived from books
          atTopLevel != true,     // inSubDir
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
      if (atTopLevel) CatalogManager.recordRamUsage("After generating Series catalog");
    }
    if (atTopLevel)   CatalogManager.callback.endCreateSeries();
    CatalogManager.callback.checkIfContinueGenerating();

    /* Tags */

    if (atTopLevel)   CatalogManager.callback.startCreateTags(DataModel.getListOfTags().size());
    if (currentProfile.getGenerateTags()) {
      logger.debug("STARTED: Generating tags catalog");
      String SplitTagsOn = currentProfile.getSplitTagsOn();
      TagsSubCatalog tagssubCatalog = (currentProfile.getDontSplitTagsOn()
                                       || Helper.isNullOrEmpty(currentProfile.getSplitTagsOn()))
                                  ? new TagListSubCatalog(stuffToFilterOut, getBooks())
                                  : new TagTreeSubCatalog(stuffToFilterOut, getBooks());
      tagssubCatalog.setCatalogLevel(getCatalogLevel());
      entry = tagssubCatalog.getCatalog(breadcrumbs,
                                        inSubDir);
      tagssubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null)
        feed.addContent(entry);
      logger.debug("COMPLETED: Generating tags catalog");
      if (atTopLevel) CatalogManager.recordRamUsage("After generating Tags catalog");
    }
    if (atTopLevel)   CatalogManager.callback.endCreateTags();
    CatalogManager.callback.checkIfContinueGenerating();

    /* Recent books */

    if (atTopLevel) {
      int nbRecentBooks = Math.min(currentProfile.getBooksInRecentAdditions(), DataModel.getListOfBooks().size());
      CatalogManager.callback.startCreateRecent(nbRecentBooks);
    }
    if (currentProfile.getGenerateRecent()) {
      logger.debug("STARTED: Generating Recent books catalog");
      RecentBooksSubCatalog recentBooksSubCatalog = new RecentBooksSubCatalog(stuffToFilterOut, getBooks());
      recentBooksSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = recentBooksSubCatalog.getCatalog(breadcrumbs, inSubDir);
      recentBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null) {
        feed.addContent(entry);
      }
      logger.debug("COMPLETED: Generating Recent books catalog");
      if (atTopLevel) CatalogManager.recordRamUsage("After generating Recent catalog");
    }
    if (atTopLevel)  CatalogManager.callback.endCreateRecent();
    CatalogManager.callback.checkIfContinueGenerating();

    /* Rated books */

    if (atTopLevel)   CatalogManager.callback.startCreateRated(DataModel.getListOfBooks().size());
    if (currentProfile.getGenerateRatings()) {
      logger.debug("STARTED: Generating Rated books catalog");
      RatingsSubCatalog ratingsSubCatalog = new RatingsSubCatalog(stuffToFilterOut,getBooks());
      ratingsSubCatalog.setCatalogLevel(getCatalogLevel());
      entry = ratingsSubCatalog.getCatalog(breadcrumbs, inSubDir);
      ratingsSubCatalog = null;  // Maybe not necesary - but explicit object cleanup
      if (entry != null) {
        feed.addContent(entry);
      }
      logger.debug("COMPLETED: Generating Rated books catalog");
      if (atTopLevel) CatalogManager.recordRamUsage("After generating Ratings catalog");
    }
    if (atTopLevel)   CatalogManager.callback.endCreateRated();
    CatalogManager.callback.checkIfContinueGenerating();

    /* All books */

    if (atTopLevel)  CatalogManager.callback.startCreateAllbooks(DataModel.getListOfBooks().size());
    if (currentProfile.getGenerateAllbooks()) {
      logger.debug("STARTED: Generating All books catalog");
      AllBooksSubCatalog allBooksSubCatalog = new AllBooksSubCatalog(stuffToFilterOut, getBooks());
      allBooksSubCatalog.setCatalogLevel(getCatalogLevel());
      String allBooksSummary = "";
      if (allBooksSubCatalog.getBooks().size() > 1)
        allBooksSummary = Localization.Main.getText("allbooks.alphabetical", getBooks().size());
      else if (getBooks().size() == 1)
        allBooksSummary = Localization.Main.getText("allbooks.alphabetical.single");
      entry = allBooksSubCatalog.getListOfBooks(breadcrumbs,
                                  getBooks(),
                                  inSubDir,
                                  0,                  // from start
                                  Localization.Main.getText("allbooks.title"),
                                  allBooksSummary, Constants.INITIAL_URN_PREFIX + allBooksSubCatalog.getCatalogType(),
                                  allBooksSubCatalog.getCatalogBaseFolderFileName(),
                                  SplitOption.SplitByLetter,
                                  useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS, null);
      allBooksSubCatalog = null;  // Maybe not necesary - but explicit object cleanup

      if (entry != null) {
        feed.addContent(entry);
      }

      logger.debug("COMPLETED: Generating All Books catalog");
      if (atTopLevel) CatalogManager.recordRamUsage("After generating All Books sub-catalog");
    }
    if (atTopLevel)  CatalogManager.callback.endCreateAllbooks();
    CatalogManager.callback.checkIfContinueGenerating();

    /* Custom Catalogs */

    if (atTopLevel) {

      // TODO:  Need to reset data model to allow all books at this point!
      generateCustomCatalogs(pBreadcrumbs, feed, inSubDir, false);
    }
    CatalogManager.callback.checkIfContinueGenerating();

    /* Level finished - end-of-level processing */

    String outputFilename = getCatalogBaseFolderFileName();
    createFilesFromElement( feed,
        outputFilename,
        (inSubDir || getCatalogLevel().length() > 0 || getCatalogFolder().length() > 0)
          ? HtmlManager.FeedType.Catalog
          : HtmlManager.FeedType.MainCatalog,
        false);           // Never optimise the index file as we want creation date to be included

    // #c2o-214
    // A check to see that all cross-reference targets have been generated
    // The only reason they would not have been is that the relevant section was suppressed

    boolean foundreference;
    do {
      foundreference = false;
      if (!atTopLevel || !currentProfile.getGenerateCrossLinks()){
        break;
      }
      for (Book book : DataModel.getListOfBooks()) {
        if (book.isDone() || !book.isReferenced()) {
          continue;
        }
        List<Book> books = Arrays.asList(book);
        BooksSubCatalog booksSubCatalog = new BooksSubCatalog(books) {};
        booksSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          booksSubCatalog.getDetailedEntry(pBreadcrumbs, book);
          foundreference = true;
          assert book.isDone();
        } catch (Exception e) {
          logger.error("Error when generating author from book cross-link"); Helper.statsErrors++;
          book.setDone();
        }
      }
      for (Author author : DataModel.getListOfAuthors()) {
        if (author.isDone() || !author.isReferenced()) {
          continue;
        }
        List<Book> authorBooks = DataModel.getMapOfBooksByAuthor().get(author);
        if (authorBooks.size() < 2 && !currentProfile.getSingleBookCrossReferences())
          continue;
        AuthorsSubCatalog authorsSubCatalog = new AuthorsSubCatalog(authorBooks);
        authorsSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          authorsSubCatalog.getDetailedEntry(pBreadcrumbs, author, DataModel.getMapOfBooksByAuthor().get(author));
          foundreference = true;
          assert author.isDone();
        } catch (Exception e) {
          logger.error("Error when generating author from book cross-link"); Helper.statsErrors++;
          author.setDone();
        }
      }
      for(Series series : DataModel.getListOfSeries()){
        if (series.isDone() || ! series.isReferenced()) {
          continue;
        }
        SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(DataModel.getMapOfBooksBySeries().get(series));
        seriesSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          seriesSubCatalog.getDetailedEntry(pBreadcrumbs, series, null, Boolean.FALSE);
          foundreference = true;
          assert series.isDone();
        } catch (Exception e) {
          logger.error("Error when generating Series from book cross-link"); Helper.statsErrors++;
          series.setDone();
        }
      }

      for (Tag tag : DataModel.getListOfTags()) {
        if (tag.isDone() || !tag.isReferenced()) {
          continue;
        }
        List<Book> tagBooks = DataModel.getMapOfBooksByTag().get(tag);
        if (tagBooks.size() < 2 && !currentProfile.getSingleBookCrossReferences()) {
          continue;
        }
        TagsSubCatalog tagsSubCatalog = new TagListSubCatalog(DataModel.getMapOfBooksByTag().get(tag));
        tagsSubCatalog.setCatalogLevel(getCatalogLevel());
        try {
          tagsSubCatalog.getDetailedEntry(pBreadcrumbs, tag, null, null);
          foundreference = true;
          assert tag.isDone();
        } catch (Exception e) {
          logger.error("Error when generating tag from book cross-link"); Helper.statsErrors++;
          logger.error("  Exception: " + e); Helper.statsErrors++;
          tag.setDone();
        }
      }

/*
        for (BookRating rating : DataModel.getlistof= book.getRating();
          if (!rating.isDone() || isRatingCrossReferences(book)) {
            if (! rating.isReferenced()) break;
            List<Book> ratingBooks = DataModel.getMapOfBooksByRating().get(rating);
            if (isRatingCrossReferences(book)) {
              break;
            }
            RatingsSubCatalog ratingSubCatalog = new RatingsSubCatalog(DataModel.getMapOfBooksByRating().get(rating));
            ratingSubCatalog.setCatalogLevel(getCatalogLevel());
            try {
              ratingSubCatalog.getRatingEntry(null, rating, null);
            } catch (Exception e) {
              logger.error("Error when generating rating '" + rating.getValue() + "' from book cross-link"); Helper.statsErrors++;
            }
          }
*/
    } while (foundreference);

    Element result = FeedHelper.getCatalogEntry(title, urn, CatalogManager.getCatalogFileUrl(outputFilename + Constants.XML_EXTENSION, inSubDir), summary, icon);
    return result;
  }


  public Element getDetailedEntry(Breadcrumbs pBreadcrumbs,
                                  Object obj,
                                  Option... options) throws IOException {
    assert false : "getDetailedEntry should never be called";
    return null;
  }

}
