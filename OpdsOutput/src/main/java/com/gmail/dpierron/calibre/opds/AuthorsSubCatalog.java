package com.gmail.dpierron.calibre.opds;

/**
 * Class for implementing the Author sub-catalogs.
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Author;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Series;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class AuthorsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(AuthorsSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());
  private Map<Author, List<Book>> mapOfBooksByAuthor;     // Cached information for efficency
  private List<Author> authors;                           // Cached information for efficiency


  public AuthorsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.AUTHORS_TYPE);
    initMapOfBooksByAuthor();          // Force initialisation
  }

  public AuthorsSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.AUTHORS_TYPE);
    initMapOfBooksByAuthor();          // Force initialisation
  }

  /**
   * Build up the list of book/author relationships
   * We cache the results for improved efficiency on indivisual authors.
   *
   * @return
   */

  private void initMapOfBooksByAuthor() {
    mapOfBooksByAuthor = new HashMap<Author, List<Book>>();
    authors = new LinkedList<Author>();
    for (Book book : getBooks()) {
      for (Author author : book.getAuthors()) {
        List<Book> currentbooks = mapOfBooksByAuthor.get(author);
        if (currentbooks == null) {
          currentbooks = new LinkedList<Book>();
          mapOfBooksByAuthor.put(author, currentbooks);
        }
        currentbooks.add(book);
        if (!authors.contains(author))
          authors.add(author);
      }
    }
    // sort the authors by name
    // We can use configuration parameters to sort by either auth_sort or author
    Collections.sort(authors, new Comparator<Author>() {
      public int compare(Author o1, Author o2) {
        String name1;
        String name2;
        if (currentProfile.getSortUsingAuthor()) {
          name1 = (o1 == null ? "" : o1.getName().toUpperCase());
          name2 = (o2 == null ? "" : o2.getName().toUpperCase());
          return collator.compare(name1,name2);
        } else {
          name1 = (o1 == null ? "" : o1.getNameForSort().toUpperCase());
          name2 = (o2 == null ? "" : o2.getNameForSort().toUpperCase());
        }
        return collator.compare(name1, name2);
      }
    });
  }

  /**
   * Produce a list of authors.
   * This function is used recursively to handle a set of pages
   *
   * @param pBreadcrumbs    The point we have reached so far
   * @param listauthors     The list of authors that need listing
   * @param inSubDir
   * @param from            The point reached in the list.  Will be 0 first time through
   * @param title           The title for this page
   * @param summary         THe summary
   * @param urn             The URN to link back to the calling point
   * @param pFilename       The filename to be used as the bawe for this set of pages
   * @param splitOption     The current preferred split option.
   * @return                Link to the page just generated to insert into parent
   * @throws IOException
   */
  public Composite<Element, String> getCatalog(
      Breadcrumbs pBreadcrumbs,
      List<Author> listauthors,
      boolean inSubDir,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {


    int catalogSize;
    int pos;
    Map<String, List<Author>> mapOfAuthorsByLetter = null;
    // Check for special case of all entries being identical last name so we cannot split further regardless of split trigger value
    boolean willSplitByLetter = false;
    String lastName = listauthors.get(0).getLastName().toUpperCase();   // Get name of first entry
    for (Author author : listauthors) {                                                   // debug
      if (! author.getLastName().toUpperCase().equals(lastName)) {
        // As long as entries are not all the same, apply the split criteria
        willSplitByLetter = checkSplitByLetter(splitOption,authors.size());
        break;
      }
    }
    if (willSplitByLetter) {
      mapOfAuthorsByLetter = DataModel.splitAuthorsByLetter(listauthors);
      catalogSize = 0;
    } else
      catalogSize = listauthors.size();

    if (from != 0) inSubDir = true;
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);
    String urlExt = optimizeCatalogURL(catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
    Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);
    logger.debug("generating " + urlExt);

    // list the entries (or split them)
    List<Element> result;
    if (willSplitByLetter) {
      logger.debug("splitting by letter");
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
      result = getListOfAuthorsSplitByLetter(breadcrumbs,
                                             mapOfAuthorsByLetter,
                                             title,
                                             urn,
                                             pFilename);
    } else {
      logger.debug("NOT splitting by letter");
      result = new LinkedList<Element>();
      for (int i = from; i < listauthors.size(); i++) {
        if ((splitOption != SplitOption.DontSplitNorPaginate)
        && ((i - from) >= maxBeforePaginate)) {
          // Get a new page
          Element nextLink = getCatalog(pBreadcrumbs,
                                        listauthors,
                                        true,
                                        i,
                                        title,
                                        summary,
                                        urn,
                                        pFilename,
                                        splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption).getFirstElement();
          result.add(0, nextLink);
          break;
        } else {
          // Get a specific author
          Author author = listauthors.get(i);
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
          logger.debug("getAuthor:" + author);
          // AuthorEntry authorEntry = new AuthorEntry (mapOfBooksByAuthor.get(author), author);
          // Element entry = authorEntry.getSubCatalogEntry(breadcrumbs, true).getFirstElement();

          Element entry = getAuthor(breadcrumbs,
                                    mapOfBooksByAuthor.get(author),
                                    author) ;
          if (entry != null) {
            result.add(entry);
            logger.debug("adding author to the TROOK database:" + author);
            TrookSpecificSearchDatabaseManager.INSTANCE.addAuthor(author, entry);
          }
        }
      }
    }
    feed.addContent(result);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);

    Element entry;
    String urlInItsSubfolder = optimizeCatalogURL(catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() >1 || pageNumber != 1));
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages) {
        titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);
      } else {
        titleNext = Localization.Main.getText("title.lastpage");
      }
      entry = FeedHelper.INSTANCE.getNextLink(urlExt, titleNext);
    } else {
      entry = FeedHelper.INSTANCE.getCatalogEntry(title, urn, urlInItsSubfolder, summary,
          // #751211: Use external icons option
          useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS);
    }
    return new Composite<Element, String>(entry, urlInItsSubfolder);
  }

  /**
   * Get a list of author that needs to be split by letter
   * It might be necessary to recurse to further levels if this
   * is allowed by the maximum split level setting
   *
   * @param pBreadcrumbs               The point we have currently reached
   * @param mapOfAuthorsByLetter       The list of authors to list
   * @param baseTitle                  The base URL for this level
   * @param baseUrn                    The base Filename for this level
   * @param baseFilename               The base filename form this level
   * @return                           The link to this page for the parent
   * @throws IOException
   */
  private List<Element> getListOfAuthorsSplitByLetter(
      Breadcrumbs pBreadcrumbs,
      Map<String, List<Author>> mapOfAuthorsByLetter,
      String baseTitle,
      String baseUrn,
      String baseFilename) throws IOException {

    if (Helper.isNullOrEmpty(mapOfAuthorsByLetter))
      return null;

    boolean inSubDir = getCatalogLevel().length() > 0 || pBreadcrumbs.size() > 1;
    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfAuthorsByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = Helper.getSplitString(baseFilename, letter, Constants.TYPE_SEPARATOR);
      String letterUrn = Helper.getSplitString(baseUrn,letter,Constants.URN_SEPARATOR);
      List<Author> authorsInThisLetter = mapOfAuthorsByLetter.get(letter);

      // sort the authors list
      Collections.sort(authorsInThisLetter);

      String letterTitle;
      if (letter.equals(Constants.TYPE_SEPARATOR))
        letterTitle = Localization.Main.getText("splitByLetter.author.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("authorword.title"),
                                                letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);
      Element element = null;
      // ITIMPI:  Not sure that the following check is needed if there cannot be a case of 0, so assert added to find out!
      assert (authorsInThisLetter.size() > 0) : "No authors for letter sequence '" + letter + "'";
      if (authorsInThisLetter.size() > 0) {
        // try and list the items to make the summary
        String summary = Summarizer.INSTANCE.summarizeAuthors(authorsInThisLetter);

        /*
         *  Prepare the list of authors in any case, even if it will be skipped by SplitByAuthorInitialGoToBooks.
         *  It'll be useful in cross references
         */
        logger.debug("calling getCatalog for the letter " + letter);

        element = getCatalog(pBreadcrumbs,
                             authorsInThisLetter,
                             true,
                             0,
                             letterTitle,
                              summary,
                             letterUrn,
                             letterFilename,
                             (letter.length() < maxSplitLevels) ? SplitOption.SplitByLetter : SplitOption.Paginate).getFirstElement();

        if (currentProfile.getSplitByAuthorInitialGoToBooks()) {
          logger.debug("getting all books by all the authors in this letter");
          List<Book> books = new LinkedList<Book>();
          for (Author author : authorsInThisLetter) {
            books.addAll(mapOfBooksByAuthor.get(author));
          }
          if (logger.isTraceEnabled())
            logger.trace("getListOfAuthorsSplitByLetter:  Breadcrumbs=" + pBreadcrumbs.toString());

          element = getCatalog(pBreadcrumbs, books, true, 0,                       // Starting from start
              letterTitle, summary, letterUrn, letterFilename, SplitOption.DontSplit,     // Bug #716917 Do not split on letter
              // #751211: Use external icons option
              useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS, null).getFirstElement();
        }
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }

  public List<Author> getAuthors() {
    return authors;
  }

  /**
   *
   * @param pBreadcrumbs
   * @param authorbooks
   * @param author
   * @return
   */
  private Element getAuthor(Breadcrumbs pBreadcrumbs,
                                  List<Book> authorbooks,
                                  Author author) throws IOException  {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + author);

    CatalogContext.INSTANCE.callback.showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogContext.INSTANCE.callback.incStepProgressIndicatorPosition();

    List listOfBooksInSeries = new LinkedList<Book>();
    List listOfBooksNotInSeries = new LinkedList<Book>();
    // We only need to worry about series if they are being listed under the author.
    if (currentProfile.getShowSeriesInAuthorCatalog()) {
      for (Book book : authorbooks) {
        Series serie = book.getSeries();
        if (serie != null) {
          listOfBooksInSeries.add(book);
        } else {
          listOfBooksNotInSeries.add(book);
        }
      }
    }


    List<Element> firstElements = null;
    List<Book> morebooks = null;

    // sort  by title
    logger.debug("sort 'booksByThisAuthor' by title");
    sortBooksByTitle(authorbooks);

    if (Helper.isNullOrEmpty(author))  {
      return null;
    }

    String filename = getCatalogBaseFolderFileNameIdNoLevel(Constants.AUTHOR_TYPE, author.getId());
    logger.debug("getAuthor:generating " + filename);

    String title = author.getSort();
    String urn = Constants.INITIAL_URN_PREFIX + Constants.AUTHOR_TYPE + Constants.URN_SEPARATOR + author.getId();
    Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, true));

    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(getBooks());

    // We like to list series if we can before books not in series
    // (unless the user has suppressed series generation).

    if (listOfBooksInSeries.size() > 0
        //          && currentProfile.getGenerateSeries()
        && listOfBooksInSeries.size() != 0
        && currentProfile.getShowSeriesInAuthorCatalog()) {
      logger.debug("processing the series by " + author);

      // make a link to the series by this author catalog
      logger.debug("make a link to the series by this author catalog");
      //        SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(stuffToFilterOutPlusAuthor, booksByThisAuthor);
      SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(listOfBooksInSeries);
      seriesSubCatalog.setCatalogLevel(getCatalogLevel());
      seriesSubCatalog.setCatalogFolder(Constants.AUTHOR_TYPE);
      seriesSubCatalog.setCatalogBaseFilename(Constants.AUTHOR_TYPE + Constants.TYPE_SEPARATOR + author.getId() + Constants.TYPE_SEPARATOR + Constants.SERIES_TYPE);
      firstElements = seriesSubCatalog.getContentOfListOfSeries(pBreadcrumbs,
                                                               null,
                                                               true,
                                                               0, title, summary, urn,
                                                               seriesSubCatalog.getCatalogBaseFolderFileName(),
                                                               SplitOption.Paginate, true);

      // Make a link to the "allbooks entry" for this author
      AllBooksSubCatalog booksSubcatalog = new AllBooksSubCatalog(authorbooks);
      booksSubcatalog.setCatalogLevel(getCatalogLevel());
      booksSubcatalog.setCatalogFolder(Constants.AUTHOR_TYPE);
      booksSubcatalog.setCatalogBaseFilename(Constants.AUTHOR_TYPE + Constants.TYPE_SEPARATOR + author.getId() + Constants.TYPE_SEPARATOR + Constants.ALLBOOKS_TYPE);
      Element entry = booksSubcatalog.getCatalog(breadcrumbs,
                                                 booksSubcatalog.getBooks(),
                                                 true,
                                                 0,       // from start
                                                 Localization.Main.getText("allbooks.title"),
                                                 booksSubcatalog.getSummary(),
                                                 booksSubcatalog.getUrn(),
                                                 booksSubcatalog.getCatalogBaseFolderFileName(),
                                                 SplitOption.Paginate,
                                                 useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS,
                                                 null).getFirstElement();
      if (entry != null)
        firstElements.add(0, entry);

      // Reset books to list non-series books
      morebooks = listOfBooksNotInSeries;
      logger.debug("processing the other " + morebooks.size() + " books by " + author);
    } else {
      assert authorbooks != null;
      morebooks = authorbooks;
      logger.debug("there are no series by " + author + ", processing all his " + morebooks.size() + " books");
      // try and list the items to make the summary
      logger.debug("try and list the items to make the summary");
      summary = Summarizer.INSTANCE.summarizeBooks(getBooks());
    }

    // sort 'morebooks' by title
    logger.debug("sort books by title");
    sortBooksByTitle(morebooks);

    logger.debug("calling getCatalog with " + morebooks.size() + " books");
    logger.trace("getAuthor  Breadcrumbs=" + pBreadcrumbs.toString());

    return getCatalog(pBreadcrumbs, morebooks, true,           // Always in subDir
        0,              // from
        title, summary, urn, filename, SplitOption.DontSplit,        // Bug #716917 Do not split on letter
        // #751211: Use external icons option
        useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS, firstElements).getFirstElement();
  }


}
