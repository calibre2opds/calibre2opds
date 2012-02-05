package com.gmail.dpierron.calibre.opds;

/**
 * Class for implementing the Author sub-catalogs.
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
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
import java.util.*;

public class AuthorsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(AuthorsSubCatalog.class);
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
      Collections.sort(authors, new Comparator<Author>() {

        public int compare(Author o1, Author o2) {
          String name1 = (o1 == null ? "" : o1.getNameForSort());
          String name2 = (o2 == null ? "" : o2.getNameForSort());
          return name1.compareTo(name2);
        }
      });

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
   * @param pBreadcrumbs
   * @param authors
   * @param from
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption
   * @return
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
    boolean willSplit = (splitOption != SplitOption.Paginate) && (maxSplitLevels != 0) && (authors.size() > maxBeforeSplit);
    if (willSplit) {
      mapOfAuthorsByLetter = DataModel.splitAuthorsByLetter(authors);
      catalogSize = 0;
    } else
      catalogSize = authors.size();

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);

    String filename = SecureFileManager.INSTANCE.decode(pFilename);
    if (from > 0) {
      int pos = filename.lastIndexOf(".xml");
      if (pos >= 0)
        filename = filename.substring(0, pos);
      filename = filename + "_" + pageNumber;
    }
    if (!filename.endsWith(".xml"))
      filename = filename + ".xml";

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
      if (willSplit) {
        logger.debug("splitting by letter");
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        result = getListOfAuthorsSplitByLetter(breadcrumbs, mapOfAuthorsByLetter, title, urn, pFilename);
      } else {
        logger.debug("NOT splitting by letter");
        result = new LinkedList<Element>();
        for (int i = from; i < authors.size(); i++) {
          if ((i - from) >= maxBeforePaginate) {
            Element nextLink = getListOfAuthors(pBreadcrumbs, authors, i, title, summary, urn, pFilename, splitOption).getFirstElement();
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
      if (logger.isTraceEnabled())
        logger.trace("Exception generating: " + outputFile.getName() + "\n" + e);
      throw e;
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
   * @param pBreadcrumbs
   * @param mapOfAuthorsByLetter
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @return
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
      String baseFilenameCleanedUp = SecureFileManager.INSTANCE.decode(baseFilename);
      int pos = baseFilenameCleanedUp.indexOf(".xml");
      if (pos > -1)
        baseFilenameCleanedUp = baseFilenameCleanedUp.substring(0, pos);
      String letterFilename = baseFilenameCleanedUp + "_" + Helper.convertToHex(letter) + ".xml";
      letterFilename = SecureFileManager.INSTANCE.encode(letterFilename);

      String letterUrn = baseUrn + ":" + letter;
      List<Author> authorsInThisLetter = mapOfAuthorsByLetter.get(letter);

      // sort the authors list
      Collections.sort(authorsInThisLetter);

      String letterTitle;
      int itemsCount = authorsInThisLetter.size();
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.author.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("authorword.title"),
                                                letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);
      Element element = null;
      if (itemsCount > 0) {
        // try and list the items to make the summary
        String summary = Summarizer.INSTANCE.summarizeAuthors(authorsInThisLetter);

        /*
         *  Prepare the list of authors in any case, even if it will be skipped by SplitByAuthorInitialGoToBooks.
         *  It'll be useful in cross references
         */
        logger.debug("calling getListOfAuthors for the letter " + letter);

        element =
            getListOfAuthors(pBreadcrumbs, authorsInThisLetter, 0, letterTitle, summary, letterUrn, letterFilename,
                (letter.length() < maxSplitLevels) ? SplitOption.SplitByLetter : SplitOption.Paginate).getFirstElement();

        if (ConfigurationManager.INSTANCE.getCurrentProfile().getSplitByAuthorInitialGoToBooks()) {
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
   * @param pBreadcrumbs
   * @param author
   * @param baseurn
   * @return
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

    if (Helper.isNullOrEmpty(booksByThisAuthor))
      return null;

    String basename = "author_";
    String filename = getFilenamePrefix(pBreadcrumbs) + basename + author.getId() + ".xml";
    logger.debug("getAuthor:generating " + filename);
    filename = SecureFileManager.INSTANCE.encode(filename);

    String title = author.getSort();
    String urn = baseurn + ":" + author.getId();

    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(getMapOfBooksByAuthor().get(author));
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
    && ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateSeries()
    && ConfigurationManager.INSTANCE.getCurrentProfile().getShowSeriesInAuthorCatalog()) {
      logger.debug("processing the series by " + author);

      // make a link to the series by this author catalog
      logger.debug("make a link to the series by this author catalog");
      firstElements = new SeriesSubCatalog(stuffToFilterOutPlusAuthor, getMapOfBooksByAuthor().get(author))
          .getContentOfListOfSeries(pBreadcrumbs, title, summary, urn, filename);

      books = getMapOfBooksNotInSeriesByAuthor().get(author);
      if (books == null)
        books = new LinkedList<Book>();

      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, getCatalogManager().getCatalogFileUrlInItsSubfolder(filename));
      logger.debug("processing the other books by " + author);
      // Do we need a way to suppress split by letter on next call?
      Element entry = new AllBooksSubCatalog(stuffToFilterOutPlusAuthor, getMapOfBooksByAuthor().get(author)).getSubCatalogEntry(breadcrumbs).getFirstElement
          ();
      if (entry != null)
        firstElements.add(0, entry);

    } else {
      logger.debug("there are no series by " + author + ", processing all his books");
      books = getMapOfBooksByAuthor().get(author);
      if (Helper.isNullOrEmpty(books))
        return null;

      // try and list the items to make the summary
      logger.debug("try and list the items to make the summary");
      summary = Summarizer.INSTANCE.summarizeBooks(books);
    }

    // sort books by title
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
   * @param pBreadcrumbs
   * @return
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
