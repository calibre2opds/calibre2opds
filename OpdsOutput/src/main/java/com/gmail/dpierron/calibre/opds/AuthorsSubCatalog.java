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
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class AuthorsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(AuthorsSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.getLocale());
  private Map<Author, List<Book>> mapOfBooksByAuthor;     // Cached information for efficency
  private List<Author> authors;                           // Cached information for efficiency

  // CONSTRUCTORS

  public AuthorsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.AUTHORLIST_TYPE);
    initMapOfBooksByAuthor();          // Force initialisation
  }

  public AuthorsSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.AUTHORLIST_TYPE);
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
        String name1 = (o1 == null ? "" : o1.getTitleToSplitByLetter());
        String name2 = (o2 == null ? "" : o2.getTitleToSplitByLetter());
        return  Helper.checkedCollatorCompareIgnoreCase(name1, name2,collator);
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
  public Element getSubCatalog(Breadcrumbs pBreadcrumbs,
      List<Author> listauthors,
      boolean inSubDir,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {

    if (from != 0) inSubDir = true;
    int pageNumber = Summarizer.getPageNumber(from + 1);
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);
    String urlExt = CatalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /* inSubDir*/);

    // Check for special case of all entries being identical last name so we cannot split further regardless of split trigger value
    String lastName = listauthors.get(0).getLastName().toUpperCase();   // Get name of first entry
    boolean willSplitByLetter = false;
    for (Author author : listauthors) {                                                   // debug
      if (! author.getLastName().toUpperCase().equals(lastName)) {
        // As long as entries are not all the same, apply the split criteria
        willSplitByLetter = checkSplitByLetter(splitOption,listauthors.size());
        break;
      }
    }
    // Check for special case where the author sort name is equal to the split level.*
    while ( willSplitByLetter && listauthors.size() > 0
            && pFilename.toUpperCase().endsWith(Constants.TYPE_SEPARATOR + listauthors.get(0).getNameForSort().toUpperCase())) {
      Author author = listauthors.get(0);
      listauthors.remove(0);
      Element element;
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
      element = getAuthorEntry(breadcrumbs, author, mapOfBooksByAuthor.get(author));
      assert element != null;
      if (element != null) {
        feed.addContent(element);
      }
      willSplitByLetter = checkSplitByLetter(splitOption,listauthors.size());
    }
    Map<String, List<Author>> mapOfAuthorsByLetter = null;
    int catalogSize;
    if (willSplitByLetter) {
      mapOfAuthorsByLetter = DataModel.splitAuthorsByLetter(listauthors);
      catalogSize = 0;
    } else {
      catalogSize = listauthors.size();
    }
    int maxPages = Summarizer.getPageNumber(catalogSize);
    logger.debug("generating " + urlExt);

    // list the entries (or split them)

    List<Element> result;

    if (willSplitByLetter  /* listauthors.size() > 1*/) {
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
          // TODO #c2o-208   Add Previous, First and Last links if needed
          // Get a new page
          Element nextLink = getSubCatalog(pBreadcrumbs,
                                           listauthors,
                                           true,
                                           i,
                                           title,
                                           summary,
                                           urn,
                                           pFilename,
                                           splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption);
          result.add(0, nextLink);
          break;
        } else {
          // Get a specific author
          Author author = listauthors.get(i);
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
          logger.debug("getAuthorEntry:" + author);
          Element entry = getAuthorEntry(breadcrumbs, author, mapOfBooksByAuthor.get(author)) ;
          if (entry != null) {
            result.add(entry);
            logger.debug("adding author to the TROOK database:" + author);
            TrookSpecificSearchDatabaseManager.addAuthor(author, entry);
          }
        }
      }
    }
    feed.addContent(result);

    Element entry;
    String urlInItsSubfolder = CatalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() >1 || pageNumber != 1);
    entry  = createPaginateLinks (feed, filename, pageNumber, maxPages);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
    if (from == 0)  {
      entry = FeedHelper.getCatalogEntry(title,
                                         urn,
                                         urlInItsSubfolder,
                                         summary,
                                         // #751211: Use external icons option
                                        useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS);
    }
    return entry;
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

    if (! baseFilename.startsWith(Constants.AUTHORLIST_TYPE)) {
      int dummy = 1;
    }
    boolean inSubDir = getCatalogLevel().length() > 0 || pBreadcrumbs.size() > 1;
    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfAuthorsByLetter.keySet());
    Element element;
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = Helper.getSplitString(baseFilename, letter, Constants.TYPE_SEPARATOR);
      String letterUrn = Helper.getSplitString(baseUrn,letter,Constants.URN_SEPARATOR);
      List<Author> authorsInThisLetter = mapOfAuthorsByLetter.get(letter);
      assert (authorsInThisLetter.size() > 0) : "No authors for letter sequence '" + letter + "'";
      Collections.sort(authorsInThisLetter);

      String letterTitle;
      if (letter.equals(Constants.TYPE_SEPARATOR))
        letterTitle = Localization.Main.getText("splitByLetter.author.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("authorword.title"),
            letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);

      // try and list the items to make the summary
      String summary = Summarizer.summarizeAuthors(authorsInThisLetter);
      /*
       *  Prepare the list of authors in any case, even if it will be skipped by SplitByAuthorInitialGoToBooks.
       *  It'll be useful in cross references
       */
      logger.debug("calling getListOfBooks for the letter " + letter);
      element = getSubCatalog(pBreadcrumbs,
                              authorsInThisLetter,
                              true,
                              0,
                              letterTitle,
                              summary,
                              letterUrn,
                              letterFilename,
                              checkSplitByLetter(letter));
      assert element != null;
      if (element != null) {
        result.add(element);
      }

      if (currentProfile.getSplitByAuthorInitialGoToBooks()) {
        logger.debug("getting all books by all the authors in this letter");
        List<Book> books = new LinkedList<Book>();
        for (Author author : authorsInThisLetter) {
          books.addAll(mapOfBooksByAuthor.get(author));
        }
        if (logger.isTraceEnabled())
          logger.trace("getListOfAuthorsSplitByLetter:  Breadcrumbs=" + pBreadcrumbs.toString());

        element = getListOfBooks(pBreadcrumbs,
                                 books,
                                 true,
                                 0,                       // Starting from start
                                 letterTitle,
                                 summary,
                                 letterUrn,
                                 letterFilename,
                                 SplitOption.DontSplit,     // Bug #716917 Do not split on letter
                                  // #751211: Use external icons option
                                 useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS, null);

        assert element != null;
        if (element != null) {
          result.add(element);
        }
      }
    }
    return result;
  }

  public List<Author> getAuthors() {
    return authors;
  }

  /**
   * Get the base filename that is used to store a given author
   *
   * Since we always hold a full list of authors at the top level the
   * name can be derived purely knowing the author involved.

   * @param author
   * @return
   */
  public static String getAuthorFolderFilenameNoLevel(Author author) {
    return getCatalogBaseFolderFileNameIdNoLevelSplit(Constants.AUTHOR_TYPE,author.getId(), 1000);
  }
  /**
   *    Get the base filename that is used to store a given author
   *    This version works within the given level
   * @param author
   * @return
   */
  public String getAuthorFolderFilenameWithLevel (Author author) {
    return getCatalogBaseFolderFileNameIdSplit(Constants.AUTHOR_TYPE, author.getId(), 1000);
  }
  /**
   *
   * @param pBreadcrumbs
   * @param author
   * @param authorbooks
   * @return
   */
  public Element getAuthorEntry(Breadcrumbs pBreadcrumbs, Author author,  List<Book> authorbooks) throws IOException  {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + author);

    CatalogManager.callback.showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogManager.callback.incStepProgressIndicatorPosition();

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

    String filename = getAuthorFolderFilenameWithLevel(author);
    logger.debug("getAuthorEntry:generating " + filename);
    String title = currentProfile.getDisplayAuthorSort() ? author.getSort(): author.getName();
    String urn = Constants.INITIAL_URN_PREFIX + Constants.AUTHOR_TYPE + Constants.URN_SEPARATOR + author.getId();
    Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, CatalogManager.getCatalogFileUrl(filename + Constants.PAGE_ONE_XML, true));

    // try and list the items to make the summary
    String summary = Summarizer.summarizeBooks(authorbooks);

    // We like to list series if we can before books not in series
    // (unless the user has suppressed series generation).

    if (listOfBooksInSeries.size() > 0 && currentProfile.getShowSeriesInAuthorCatalog()) {
      logger.debug("processing the series by " + author);

      // make a link to the series by this author catalog
      logger.debug("make a link to the series by this author catalog");
      SeriesSubCatalog seriesSubCatalog = new SeriesSubCatalog(listOfBooksInSeries);
      seriesSubCatalog.setCatalogLevel(getCatalogLevel());
      seriesSubCatalog.setCatalogFolderSplit(Constants.AUTHOR_TYPE, author.getId());
      seriesSubCatalog.setCatalogBaseFilename(Constants.AUTHOR_TYPE + Constants.TYPE_SEPARATOR + author.getId()
                                              + Constants.TYPE_SEPARATOR + Constants.SERIES_TYPE);
      firstElements = seriesSubCatalog.getListOfSeries(breadcrumbs,
                                                       null,      // series derived from catalog books
                                                       true,
                                                       0,         // from start
                                                       title,
                                                       null,      // summary not needed as only single series?
                                                       urn,
                                                       null,      // filename derived from catalog properties
                                                       SplitOption.Paginate,
                                                       true);

      seriesSubCatalog = null;    // May not be necessary, but allow earlier release of resources

      // Make a link to the "allbooks entry" for this author
      AllBooksSubCatalog allbooksSubcatalog = new AllBooksSubCatalog(authorbooks);
      sortBooksByTitle(authorbooks);
      allbooksSubcatalog.setCatalogLevel(getCatalogLevel());
      allbooksSubcatalog.setCatalogFolder(filename);
      allbooksSubcatalog.setCatalogBaseFilename(filename + Constants.TYPE_SEPARATOR + Constants.ALLBOOKS_TYPE);
      Element entry = allbooksSubcatalog.getListOfBooks(breadcrumbs,
                                                      null,           // derived from catalog properties
                                                      true,
                                                      0,              // from start
                                                      Localization.Main.getText("bookentry.author", Localization.Main.getText("allbooks.title"), author.getName()),
                                                      allbooksSubcatalog.getSummary(),
                                                      allbooksSubcatalog.getUrn(),
                                                      allbooksSubcatalog.getCatalogBaseFolderFileName(),
                                                      SplitOption.Paginate, useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS, null);
      allbooksSubcatalog = null;     // May not be necessary - but allowe earlier release of resources
      firstElements.add(0,entry); // Add at start (in front of Series list)

      // Reset books to list non-series books
      morebooks = listOfBooksNotInSeries;
      logger.debug("processing the other " + morebooks.size() + " books by " + author);
    } else {
      // No series (or we do not want them in author - simply take all te books to list them
      assert authorbooks != null;
      morebooks = authorbooks;
      logger.debug("there are no series by " + author + ", processing all his " + morebooks.size() + " books");
      // try and list the items to make the summary
      logger.debug("try and list the items to make the summary");
      summary = Summarizer.summarizeBooks(morebooks);
    }

    // sort 'morebooks' by title
    logger.debug("sort books by title");
    sortBooksByTitle(morebooks);

    logger.debug("calling getListOfBooks with " + morebooks.size() + " books");
    logger.trace("getAuthorEntry  Breadcrumbs=" + pBreadcrumbs.toString());

    author.setDone();
    return getListOfBooks(pBreadcrumbs,
                          morebooks,
                          true,           // Always in subDir
                          0,              // from
                          title,
                          summary,
                          urn,
                          filename,
                          SplitOption.DontSplit,        // Bug #716917 Do not split on letter
                          // #751211: Use external icons option
                          useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS,
                          firstElements);
  }
}
