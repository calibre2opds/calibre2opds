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
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class AuthorsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(AuthorsSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());
  private List<Author> authors;
  private Map<Author, List<Book>> mapOfBooksByAuthor;
  private Map<Author, List<Series>> mapOfSeriesByAuthor;
  private Map<Author, List<Book>> mapOfBooksNotInSerieByAuthor;

  public AuthorsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  public AuthorsSubCatalog(List<Book> books) {
    super(books);
  }

  List<Author> getAuthors() {
    if (authors == null) {
      authors = new LinkedList<Author>();
      for (Book book : getBooks()) {
        for (Author author : book.getAuthors()) {
          if (!authors.contains(author))
            authors.add(author);
        }
      }
      // sort the authors by name
      // We can use configuration parameters to sort by either auth_sort or author
      if (currentProfile.getSortUsingAuthor()) {
      Collections.sort(authors, new Comparator<Author>() {

        public int compare(Author o1, Author o2) {
          String name1 = (o1 == null ? "" : o1.getName().toUpperCase());
          String name2 = (o2 == null ? "" : o2.getName().toUpperCase());
          return collator.compare(name1,name2);
        }
      });
      } else {
        Collections.sort(authors, new Comparator<Author>() {

          public int compare(Author o1, Author o2) {
            String name1 = (o1 == null ? "" : o1.getNameForSort().toUpperCase());
            String name2 = (o2 == null ? "" : o2.getNameForSort().toUpperCase());
            return collator.compare(name1, name2);
          }
        });
      }

    }
    return authors;
  }

  public Map<Author, List<Book>> getMapOfBooksByAuthor() {
    if (mapOfBooksByAuthor == null) {
      mapOfBooksByAuthor = new HashMap<Author, List<Book>>();
      for (Book book : getBooks()) {
        for (Author author : book.getAuthors()) {
          List<Book> books = mapOfBooksByAuthor.get(author);
          if (books == null) {
            books = new LinkedList<Book>();
            mapOfBooksByAuthor.put(author, books);
          }
          books.add(book);
        }
      }
    }
    return mapOfBooksByAuthor;
  }

  public Map<Author, List<Series>> getMapOfSeriesByAuthor() {
    if (mapOfSeriesByAuthor == null)
      computeMapOfSeriesByAuthor();
    return mapOfSeriesByAuthor;
  }

  public Map<Author, List<Book>> getMapOfBooksNotInSeriesByAuthor() {
    if (mapOfBooksNotInSerieByAuthor == null)
      computeMapOfSeriesByAuthor();
    return mapOfBooksNotInSerieByAuthor;
  }

  private void computeMapOfSeriesByAuthor() {
    mapOfSeriesByAuthor = new HashMap<Author, List<Series>>();
    mapOfBooksNotInSerieByAuthor = new HashMap<Author, List<Book>>();
    for (Book book : getBooks()) {
      for (Author author : book.getAuthors()) {
        Series serie = book.getSeries();
        if (serie != null) {
          List<Series> series = mapOfSeriesByAuthor.get(author);
          if (series == null) {
            series = new LinkedList<Series>();
            mapOfSeriesByAuthor.put(author, series);
          }
          if (!series.contains(serie))
            series.add(serie);
        } else {
          List<Book> books = mapOfBooksNotInSerieByAuthor.get(author);
          if (books == null) {
            books = new LinkedList<Book>();
            mapOfBooksNotInSerieByAuthor.put(author, books);
          }
          if (!books.contains(book))
            books.add(book);
        }
      }
    }
  }

  /**
   * Produce a list of books.
   * This function can be used recursively to handle a set of pages
   * @param pBreadcrumbs    The point we have reached so far
   * @param authors         The list of authors that need listing
   * @param from            The point reached in the list.  Will be 0 first time through
   * @param title           The title for this page
   * @param summary         THe summary
   * @param urn             The URN to link back to the calling point
   * @param pFilename       The filename to be used as the bawe for this set of pages
   * @param splitOption     The current preferred split option.
   * @return                Link to the page just generated to insert into parent
   * @throws IOException
   */
  private Composite<Element, String> getListOfAuthors(Breadcrumbs pBreadcrumbs,
      List<Author> authors,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {
    int catalogSize;
    Map<String, List<Author>> mapOfAuthorsByLetter = null;
    // Check for special case of all entries being identical last name so we cannot split further regardless of split trigger value
    boolean willSplitByLetter = false;
    String lastName = authors.get(0).getLastName().toUpperCase();   // Get name of first entry
    for (Author author : authors) {                                                   // debug
      if (! author.getLastName().toUpperCase().equals(lastName)) {
        // As long as entries are not all the same, apply the split criteria
        willSplitByLetter = (splitOption != SplitOption.Paginate)
                            && (maxSplitLevels != 0)
                            && (authors.size() > maxBeforeSplit);
        break;
      }
    }
    if (willSplitByLetter) {
      mapOfAuthorsByLetter = DataModel.splitAuthorsByLetter(authors);
      catalogSize = 0;
    } else
      catalogSize = authors.size();

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);
    String filename = SecureFileManager.INSTANCE.getSplitFilename(pFilename, Integer.toString(pageNumber));
    logger.debug("generating " + filename);
    filename = SecureFileManager.INSTANCE.encode(filename);

    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
    try {
      fos = new FileOutputStream(outputFile);
      Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

      // list the entries (or split them)
      List<Element> result;
      if (willSplitByLetter) {
        logger.debug("splitting by letter");
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        result = getListOfAuthorsSplitByLetter(breadcrumbs, mapOfAuthorsByLetter, title, urn, pFilename);
      } else {
        logger.debug("NOT splitting by letter");
        result = new LinkedList<Element>();
        for (int i = from; i < authors.size(); i++) {
          if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
            Element nextLink = getListOfAuthors(pBreadcrumbs, authors, i, title, summary, urn, pFilename,
                                                splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption).getFirstElement();
            result.add(0, nextLink);
            break;
          } else {
            Author author = authors.get(i);
            Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
            logger.debug("getAuthor:" + author);
            Element entry = getAuthor(breadcrumbs, author, urn);
            if (entry != null) {
              result.add(entry);
              logger.debug("adding author to the TROOK database:" + author);
              TrookSpecificSearchDatabaseManager.INSTANCE.addAuthor(author, entry);
            }
          }
        }
      }

      // add the entries to the feed
      feed.addContent(result);

      // write the element to the file
      document.addContent(feed);
      JDOM.INSTANCE.getOutputter().output(document, fos);
    } catch (RuntimeException e) {
      // ITIMPI:  Should we log something when NOT in trace mode?
    } finally {
      if (fos != null)
        fos.close();
    }

    // create the same file as html
    getHtmlManager().generateHtmlFromXml(document, outputFile);

    Element entry;
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, weAreAlsoInSubFolder);
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages) {titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);} else {
        titleNext = Localization.Main.getText("title.lastpage");
      }

      entry = FeedHelper.INSTANCE.getNextLink(urlExt, titleNext);
    } else {
      entry = FeedHelper.INSTANCE.getCatalogEntry(title, urn, urlInItsSubfolder, summary,
          // #751211: Use external icons option
          useExternalIcons ? (pBreadcrumbs.size() > 1 ? "../" : "./") + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS);
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
  private List<Element> getListOfAuthorsSplitByLetter(Breadcrumbs pBreadcrumbs,
      Map<String, List<Author>> mapOfAuthorsByLetter,
      String baseTitle,
      String baseUrn,
      String baseFilename) throws IOException {

    if (Helper.isNullOrEmpty(mapOfAuthorsByLetter))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfAuthorsByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = SecureFileManager.INSTANCE.getSplitFilename(baseFilename,letter);
      // check we are not recursing so deep we may have an issue with pathlength!
      // This should be MUCH less likely with reworked spliLevel algorithm
      if (letterFilename.length() > 200) {
        assert true: "letterFilename.length() = " + letterFilename.length() + " (" + letterFilename + ")";
      }
      String letterUrn = Helper.getSplitString(baseUrn,letter,":");
      List<Author> authorsInThisLetter = mapOfAuthorsByLetter.get(letter);

      // sort the authors list
      Collections.sort(authorsInThisLetter);

      String letterTitle;
      if (letter.equals("_"))
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
        logger.debug("calling getListOfAuthors for the letter " + letter);

        element = getListOfAuthors(pBreadcrumbs, authorsInThisLetter, 0, letterTitle, summary, letterUrn, letterFilename,
//                (letter.length() < maxSplitLevels) ? SplitOption.SplitByLetter : SplitOption.Paginate).getFirstElement();
                  (letter.length() < maxSplitLevels) ? SplitOption.SplitByLetter : SplitOption.Paginate).getFirstElement();

        if (currentProfile.getSplitByAuthorInitialGoToBooks()) {
          logger.debug("getting all books by all the authors in this letter");
          List<Book> books = new LinkedList<Book>();
          for (Author author : authorsInThisLetter) {
            books.addAll(getMapOfBooksByAuthor().get(author));
          }
          if (logger.isTraceEnabled())
            logger.trace("getListOfAuthorsSplitByLetter:  Breadcrumbs=" + pBreadcrumbs.toString());
          boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;

          element = getListOfBooks(pBreadcrumbs, books, 0,                       // Starting from start
              letterTitle, summary, letterUrn, letterFilename, SplitOption.DontSplit,     // Bug #716917 Do not split on letter
              // #751211: Use external icons option
              useExternalIcons ? (weAreAlsoInSubFolder ? "../" : "./") + Icons.ICONFILE_BOOKS : Icons.ICON_BOOKS).getFirstElement();
        }
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }

  /**
   * Get the details of a single author
   *
   * @param pBreadcrumbs          The point we have reached
   * @param author                The author to be listed
   * @param baseurn               The base URN for this author
   * @return                      Yhe link to be inserted into the parent
   * @throws IOException
   */
  private Element getAuthor(Breadcrumbs pBreadcrumbs, Author author, String baseurn) throws IOException {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + author);

    CatalogContext.INSTANCE.getCallback().showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogContext.INSTANCE.getCallback().incStepProgressIndicatorPosition();

    List<Element> firstElements = null;
    List<Book> books = null;
    List<Book> booksByThisAuthor = getMapOfBooksByAuthor().get(author);
    // sort  by title
    logger.debug("sort 'booksByThisAuthor' by title");
    sortBooksByTitle(booksByThisAuthor);

    if (Helper.isNullOrEmpty(booksByThisAuthor))
      return null;

    String basename = "author_";
    String filename = getFilenamePrefix(pBreadcrumbs) + basename + author.getId() + ".xml";
    logger.debug("getAuthor:generating " + filename);
    filename = SecureFileManager.INSTANCE.encode(filename);

    String title = author.getSort();
    String urn = baseurn + ":" + author.getId();

    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(booksByThisAuthor);
    boolean areThereSeries = false;
    for (Book book : booksByThisAuthor) {
      if (book.getSeries() != null) {
        areThereSeries = true;
        logger.debug("there are series");
        break;
      }
    }

    List<Object> stuffToFilterOutPlusAuthor = new ArrayList<Object>();
    if (stuffToFilterOut != null)
      stuffToFilterOutPlusAuthor.addAll(stuffToFilterOut);
    stuffToFilterOutPlusAuthor.add(author);

    // We like to list series if we can before books not in series
    // (unless the user has suppressed series generation).
    if (areThereSeries
    && currentProfile.getGenerateSeries()
    && currentProfile.getShowSeriesInAuthorCatalog()) {
      logger.debug("processing the series by " + author);

      // make a link to the series by this author catalog
      logger.debug("make a link to the series by this author catalog");
      firstElements = new SeriesSubCatalog(stuffToFilterOutPlusAuthor, booksByThisAuthor)
          .getContentOfListOfSeries(pBreadcrumbs, title, summary, urn, filename, SplitOption.Paginate);

      books = getMapOfBooksNotInSeriesByAuthor().get(author);
      if (books == null)
        books = new LinkedList<Book>();

      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, getCatalogManager().getCatalogFileUrlInItsSubfolder(filename));
      logger.debug("processing the other books by " + author);
      Element entry = new AllBooksSubCatalog(stuffToFilterOutPlusAuthor, booksByThisAuthor, true).getSubCatalogEntry(breadcrumbs, SplitOption.Paginate).getFirstElement();
      if (entry != null)
        firstElements.add(0, entry);

    } else {
      logger.debug("there are no series by " + author + ", processing all his books");
      books = booksByThisAuthor;
      if (Helper.isNullOrEmpty(books))
        return null;

      // try and list the items to make the summary
      logger.debug("try and list the items to make the summary");
      summary = Summarizer.INSTANCE.summarizeBooks(books);
    }

    // sort 'books' by title
    logger.debug("sort books by title");
    sortBooksByTitle(books);

    logger.debug("calling getListOfBooks with " + books.size() + " books");
    logger.trace("getAuthor  Breadcrumbs=" + pBreadcrumbs.toString());

    Element result = getListOfBooks(pBreadcrumbs, books, 0,              // from
        title, summary, urn, filename, SplitOption.DontSplit,        // Bug #716917 Do not split on letter
        // #751211: Use external icons option
        useExternalIcons ? (pBreadcrumbs.size() > 1 ? "../" : "./") + Icons.ICONFILE_AUTHORS : Icons.ICON_AUTHORS, firstElements).getFirstElement();
    return result;
  }

  /**
   * Used to generate a sub-catalog
   *
   * @param pBreadcrumbs        The point reached so far
   * @return                    The link to insert into the parent
   * @throws IOException
   */
  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getAuthors()))
      return null;

    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_authors.xml");
    String title = Localization.Main.getText("authors.title");
    String urn = "calibre:authors";

    String summary = "";
    if (getAuthors().size() > 1)
      summary = Localization.Main.getText("authors.alphabetical", authors.size());
    else if (getAuthors().size() == 1)
      summary = Localization.Main.getText("authors.alphabetical.single");

    logger.debug("getListOfAuthors:" + pBreadcrumbs.toString());
    return getListOfAuthors(pBreadcrumbs, getAuthors(), 0, title, summary, urn, filename, null);
  }

}
