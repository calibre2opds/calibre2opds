package com.gmail.dpierron.calibre.opds;

/**
 * Class that provides the facilities for listing the books in a catalog
 * The type specific catalogs will extend this class to inherit its functionality
 * Inherits from:
 *   -> SubCatalog
 *
 *   This class also handles the Book Details pages for specific books
 */

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.calibre.opds.indexer.IndexManager;
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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BooksSubCatalog extends SubCatalog {
  private final static Logger logger = Logger.getLogger(BooksSubCatalog.class);

  // This is the date format used within the book details.
  // At the moment it is either a fulld ate or jsut the year
  // If users ask for more flexibility the coniguration options can be re-visited.
  private final static DateFormat PUBLICATIONDATE_FORMAT =
      currentProfile.getPublishedDateAsYear() ? new SimpleDateFormat("yyyy") : SimpleDateFormat.getDateInstance(DateFormat.LONG,new Locale(currentProfile.getLanguage()));

  // This is the date format that is to be used in the titles for the Recent Books sub-catalog section
  // It is currently a hard-coded format.   If there is user feedback suggestion that variations are
  // desireable then it could be come a configurable option
  private final static DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.LONG,new Locale(currentProfile.getLanguage()));

  /**
   * @return
   */
  public boolean isBookTheStepUnit() {
    return false;
  }

  /**
   * Create a filtered books sub-catalog item
   *
   * @param stuffToFilterOut
   * @param books
   */
  public BooksSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  /**
   * Create an un-filtered books sub-catalog item
   *
   * @param books
   */
  public BooksSubCatalog(List<Book> books) {
    super(books);
  }

  /**
   * Sort the list of books alphabetically
   * We allow the field that is to be used for sorting
   * titles to be set as a configuration parameter
   *
   * @param books
   */
  void sortBooksByTitle(List<Book> books) {
    if (currentProfile.getSortUsingTitle())
    {
      Collections.sort(books, new Comparator<Book>() {

        // ITIMPI:  I would have thought that neither o1 or o2 can be null?
        //          If so then following tests for null can be removed to improve efficiency
        public int compare(Book o1, Book o2) {
          assert (o1 != null) && (o2 != null);
          String title1 = (o1 == null ? "" : o1.getTitle_Sort());
          String title2 = (o2 == null ? "" : o2.getTitle_Sort());
          return title1.compareTo(title2);
        }
      });
    } else {
      Collections.sort(books, new Comparator<Book>() {

        public int compare(Book o1, Book o2) {
          assert (o1 != null) && (o2 != null);
          String title1 = (o1 == null ? "" : o1.getTitle());
          String title2 = (o2 == null ? "" : o2.getTitle());
          return title1.compareToIgnoreCase(title2);
        }
      });

    }
  }

  /**
   * Sort the list of books sorted with the following key sequence:
   * - Series (alphabetically)
   * - Series-Index (numerically ascending)
   * - Book Title (Alphabetically for case where series-Index is the same)
   *
   * @param books
   */
  void sortBooksBySeries(List<Book> books) {
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {

        // ITIMPI:  I would have thought that neither o1 or o2 can be null?
        //          If so then following tests for null can be removed to slightly improve efficiency
        assert (o1 != null) && (o2 != null);

        if (o1 == null) {
          if (o2 == null)
            return 0;
          else
            return 1;
        }

        if (o2 == null) {
          if (o1 == null)
            return 0;
          else
            return -1;
        }

        Series series1 = o1.getSeries();
        Series series2 = o2.getSeries();

        if (series1 == null) {
          if (series2 == null) {
            // both series are null, we need to compare the book titles (as always...)
            if (currentProfile.getSortUsingTitle()) {
              String title1 = (o1 == null ? "" : o1.getTitle());
              String title2 = (o2 == null ? "" : o2.getTitle());
              return title1.compareTo(title2);
            } else {
              String title1 = (o1 == null ? "" : o1.getTitle_Sort());
              String title2 = (o2 == null ? "" : o2.getTitle_Sort());
              return title1.compareToIgnoreCase(title2);
            }
          } else {
            // only series2 set  so assume series2 sorts greater than series1
            return 1;
          }
        } else if (series2 == null){
          // only series1 set  so assume series2 sorts less than series2
          return -1;
        }

        // Both series set if we get to here
        assert (series1 != null) || (series2 != null);
        if (series1.getId().equals(series2.getId())) {
          // same series, we need to compare the index
          if (o1.getSerieIndex() == o2.getSerieIndex())
            // series index the same, so we need to sort on the book title
            return 0;
          else if (o1.getSerieIndex() > o2.getSerieIndex())
            return 1;
          else
            return -1;
        } else {
          // different series, we need to compare the series title
          return series1.getName().toUpperCase().compareTo(series2.getName().toUpperCase());
        }
      }
    });
  }

  /**
   * Function to sort books by timestamp (last modified)
   *
   * @param books
   */
  void sortBooksByTimestamp(List<Book> books) {
    // sort the books by timestamp
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        Date ts1 = (o1 == null ? new Date() : o1.getTimestamp());
        Date ts2 = (o2 == null ? new Date() : o2.getTimestamp());
        return ts2.compareTo(ts1);
      }

    });
  }

  /**
   * @param pBreadcrumbs
   * @param books
   * @param from
   * @param pTitle
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
  Composite<Element, String> getListOfBooks(Breadcrumbs pBreadcrumbs,
      List<Book> books,
      int from,
      String pTitle,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {

    return getListOfBooks(pBreadcrumbs, books, from, pTitle, summary, urn, pFilename, splitOption, icon, null,         // No first elements
        options);
  }

  /**
   * Get a list of books starting from a specific point
   *
   * ITIMPI:  At the moment this function can call itself recursively with the 'from'
   *          parameter being incremented.   It is likely to be much more efficient
   *          in both cpu load and memory usage to flatten the loop by rewriteing the
   *          function to elimiate recursion.
   *
   * @param pBreadcrumbs
   * @param books
   * @param from
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption     This option how a list should be split if it exceeds size limits
   * @param icon
   * @param firstElements
   * @param options
   * @return
   * @throws IOException
   */
  Composite<Element, String> getListOfBooks(Breadcrumbs pBreadcrumbs,
      List<Book> books,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption,
      String icon,
      List<Element> firstElements,
      Option... options) throws IOException {
    logger.debug("getListOfBooks: START");
    int catalogSize = books.size();
    logger.debug("getListOfBooks:catalogSize=" + catalogSize);
    Map<String, List<Book>> mapOfBooksByLetter = null;
    Map<DateRange, List<Book>> mapOfBooksByDate = null;

    // Work out any split options
    // Fixes #716917 when applied to author books list
    boolean willSplitByLetter;
    boolean willSplitByDate;
    if (splitOption == null) {
      // ITIMPI: Null seems to be equivalent to SplitByLetter !
      //         Might be better to replace calls by explicit value?
      splitOption = SplitOption.SplitByLetter;
      logger.debug("getListOfBooks:splitOption=null.  Changed to SplitByLetter");
    }
    switch (splitOption) {
      case Paginate:
        logger.debug("getListOfBooks:splitOption=Paginate");
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case DontSplitNorPaginate:
        logger.debug("getListOfBooks:splitOption=DontSplitNorPaginate");
        assert from == 0 : "getListBooks: DontSplitNorPaginate, from=" + from;
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case DontSplit:
        // Bug #716917 Do not split on letter (used in Author and Series book lists)
        logger.debug("getListOfBooks:splitOption=DontSplit");
        assert from == 0 : "getListBooks: DontSplit, from=" + from;
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case SplitByDate:
        logger.debug("getListOfBooks:splitOption=SplitByDate");
        assert from == 0 : "getListBooks: splitByDate, from=" + from;
        willSplitByLetter = true;
        willSplitByDate = true;
        break;
      case SplitByLetter:
        logger.debug("getListOfBooks:splitOption=SplitByLetter");
        assert from == 0 : "getListBooks: splitByLetter, from=" + from;
        willSplitByLetter = true;
        willSplitByDate = false;
        break;
      default:
        // ITIMPI:  Not sure that this case can ever arise
        //          Just added as a safety check
        logger.debug("getListOfBooks:splitOption=" + splitOption);
        assert from == 0 : "getListBooks: unknown splitOption, from=" + from;
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
    }
    // See if SplitByLetter conditions actually apply?
    if (willSplitByLetter) {
      if ((maxSplitLevels == 0) || (catalogSize <= maxBeforeSplit)) {
        willSplitByLetter = false;
      } else if ((currentProfile.getBrowseByCover()) &&
          (currentProfile.getBrowseByCoverWithoutSplit())) {
        willSplitByLetter = false;
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("getListOfBooks:willSplitByLetter=" + willSplitByLetter);
      logger.debug("getListOfBooks:willSplitByDate=" + willSplitByDate);
      logger.debug("listing books from=" + from + ", title=" + title);
    }
    if (willSplitByDate) {
      if (logger.isDebugEnabled())
        logger.debug("splitting list of books by date");
      mapOfBooksByDate = DataModel.splitBooksByDate(books);
      catalogSize = 0;
    } else if (willSplitByLetter) {
      if (logger.isDebugEnabled())
        logger.debug("splitting list of books by letter");
      mapOfBooksByLetter = DataModel.splitBooksByLetter(books);
      catalogSize = 0;
    }

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);

    // generate the book list file
    String filename = SecureFileManager.INSTANCE.getSplitFilename(pFilename,Integer.toString(pageNumber));
    if (!filename.endsWith(".xml"))
      filename = filename + ".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);
    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
    try {
      if (logger.isTraceEnabled())
        logger.trace("getListOfBooks: fos=" + outputFile);
      try {
        fos = new FileOutputStream(outputFile);
      } catch (Exception e) {
        // ITIMPI:  This should not normally happen.   However it has been found that it can
        // if the filename we are trying to use is invalid for the file system we are using.
        // If it does occur we cannot continue with generation of the details for this book
        logger.error("Failed to create feed file " +  outputFile + "\n" + e);
        return null;
      }
      Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

      // list the books (or split them)
      List<Element> result;
      if (willSplitByDate) {
        // Split by date listing
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        result = getListOfBooksSplitByDate(breadcrumbs, mapOfBooksByDate, title, urn, pFilename,
                                (useExternalIcons && ! icon.startsWith("../") ? "../" : "")  + icon, options);
      } else if (willSplitByLetter) {
        // Split by letter listing
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        result = getListOfBooksSplitByLetter(breadcrumbs, mapOfBooksByLetter, title, urn, pFilename, SplitOption.SplitByLetter,
                                (useExternalIcons && ! icon.startsWith("../")? "../" : "") + icon, options);
      } else {
        // Paginated listing
        result = new LinkedList<Element>();
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        CatalogContext.INSTANCE.getCallback().showMessage(breadcrumbs.toString() + " (" + Summarizer.INSTANCE.getBookWord(books.size()) + ")");
        for (int i = from; i < books.size(); i++) {
          // check if we must continue
          CatalogContext.INSTANCE.getCallback().checkIfContinueGenerating();

          // See if we need to do the next page
          if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
            // ... YES - so go for next page
            if (logger.isDebugEnabled())
              logger.debug("making a nextpage link");
            Element nextLink = getListOfBooks(pBreadcrumbs, books, i, title, summary, urn, pFilename, splitOption == SplitOption.DontSplit ? SplitOption.Paginate : splitOption, icon, options).getFirstElement();
            result.add(0, nextLink);
            break;
          } else {
            // ... NO - so add book to this page
            Book book = books.get(i);
            if (logger.isTraceEnabled())
              logger.trace("getListOfBooks: adding book to the list : " + book);
            try {
              logger.trace("getListOfBooks: breadcrumbs=" + breadcrumbs + ", book=" + book + ", options=" + options);
              Element entry = getBookEntry(breadcrumbs, book, options);
              if (entry != null) {
                logger.trace("getListOfBooks: entry=" + entry);
                result.add(entry);
                TrookSpecificSearchDatabaseManager.INSTANCE.addBook(book, entry);
              }
            } catch (RuntimeException e) {
              logger.error("getListOfBooks: Exception on book: " + book.getTitle() + "[" + book.getId() + "]", e);
              throw e;
            }
          }
        }
      }

      // if needed, add the first elements to the feed
      if (Helper.isNotNullOrEmpty(firstElements))
        feed.addContent(firstElements);

      // add the book entries to the feed
      feed.addContent(result);

      // write the element to the file
      document.addContent(feed);
      JDOM.INSTANCE.getOutputter().output(document, fos);
    } finally {
      if (fos != null)
        fos.close();
    }

    // create the same file as html
    getHtmlManager().generateHtmlFromXml(document, outputFile);
    Element entry;
    String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, pBreadcrumbs.size() > 1);
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages) {titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);} else {
        titleNext = Localization.Main.getText("title.lastpage");
      }

      entry = FeedHelper.INSTANCE.getNextLink(urlExt, titleNext);
    } else {
      entry = FeedHelper.INSTANCE.getCatalogEntry(title, urn, urlInItsSubfolder, summary, icon);
    }

    return new Composite<Element, String>(entry, urlInItsSubfolder);
  }

  /**
   * Get a list of books that is paginated
   * 
   * They will all share the same base URL with just the page number part incrementing
   *
   * NOTE:  The original implementation used a recursion technique.  This seems to be
   *        relatively expensive in resource (stack) usage, so this implementation
   *        changes the algorithm to use a technique of iterating through the pages.
   *
   * @param pBreadcrumbs
   * @param books
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param icon
   * @param firstElements
   * @param options
   * @return The link that is to be added to the calling page
   * @throws IOException
   */
  /*
  Composite<Element, String> getListOfBooksPaginated(Breadcrumbs pBreadcrumbs,
      List<Book> books,
      String title,
      String summary,
      String urn,
      String pFilename,
      String icon,
      List<Element> firstElements,
      Option... options) throws IOException {

    logger.debug("getListOfBooks: START");
    int catalogSize = books.size();
    logger.debug("getListOfBooks:catalogSize=" + catalogSize);

    // TODO This routine is currently a 'work-inprogress'

    int pageNumber = 0;
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);
    List<Element> result;

    Element feed;

    // Variables


    // generate the book list file
    String filename = SecureFileManager.INSTANCE.decode(pFilename);

    for (int currentBook = 0; currentBook < catalogSize; currentBook++) {
      // See if we aer about to start a new page?
      if (currentBook % maxBeforePaginate == 0) {
        pageNumber++;     //Increment page number
        if (!filename.endsWith(".xml"))
          filename = filename + ".xml";
        if (pageNumber > 1) {
          // ITIMPI:  At the moment the page number is only added for 2nd and
          //          subsequent pages.  Should we always add it for consistency?
          //          Seems a good idea but need to check for ramifications wleswhere.
          int pos = filename.lastIndexOf(".xml");
          if (pos >= 0)

            filename = filename.substring(0, pos);
          filename = filename + "_" + pageNumber;
        }
        filename = SecureFileManager.INSTANCE.encode(filename);
        File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
        FileOutputStream fos = null;
        Document document = new Document();
        String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
        try {
          if (logger.isTraceEnabled())
            logger.trace("getListOfBooks: fos=" + outputFile);
          try {
            fos = new FileOutputStream(outputFile);
          } catch (Exception e) {
            // ITIMPI:  This should not normally happen.   However it has been found that it can
            // if the filename we are trying to use is invalid for the file system we are using.
            // If it does occur we cannot continue with generation of the details for this book
            logger.error("Failed to create feed file " +  outputFile + "\n" + e);
            return null;
          }
          Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

          // list the books (or split them)
          // Paginated listing
          result = new LinkedList<Element>();
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
          CatalogContext.INSTANCE.getCallback().showMessage(breadcrumbs.toString() + " (" + Summarizer.INSTANCE.getBookWord(books.size()) + ")");
        } finally {
          if (fos != null)
            fos.close();
        }
      }  // End of start-of page

      // check if we must continue
      CatalogContext.INSTANCE.getCallback().checkIfContinueGenerating();

      // Individual book entries

      if ((currentBook % maxBeforePaginate) != 0 ) {
        if (logger.isDebugEnabled())
          logger.debug("making a nextpage link");
        Element nextLink = getListOfBooks(pBreadcrumbs, books, i, title, summary, urn, pFilename, SplitOption.Paginate, icon, options).getFirstElement();
        result.add(0, nextLink);
        break;
      } else {
        Book book = books.get(i);
        if (logger.isTraceEnabled())
          logger.trace("getListOfBooks: adding book to the list : " + book);
        try {
          logger.trace("getListOfBooks: breadcrumbs=" + pBreadcrumbs + ", book=" + book + ", options=" + options);
          Element entry = getBookEntry(pBreadcrumbs, book, options);
          if (entry != null) {
            logger.trace("getListOfBooks: entry=" + entry);
            result.add(entry);
            TrookSpecificSearchDatabaseManager.INSTANCE.addBook(book, entry);
          }
        } catch (RuntimeException e) {
          logger.error("getListOfBooks: Exception on book: " + book.getTitle() + "[" + book.getId() + "]", e);
          throw e;
        }
      }
      // Have we completed this page?
      if (((i+1) % maxBeforePaginate == 0)
      ||  ((i+1) ==  maxBooks)) {
        // Are there further pages?
        if ((i+1) < maxBooks) {
          // If yes we need to add a next page link
        }
        // Page leadout
        if (from % maxBeforePaginate == 0) {
          // if needed, add the first elements to the feed
          if (Helper.isNotNullOrEmpty(firstElements))
            feed.addContent(firstElements);

          // add the book entries to the feed
          feed.addContent(result);

          // write the element to the file
          document.addContent(feed);
          JDOM.INSTANCE.getOutputter().output(document, fos);
        }
        // create the same file as html
        getHtmlManager().generateHtmlFromXml(document, outputFile);
        Element entry;
        String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, pBreadcrumbs.size() > 1);
        if (from > 0) {
          String titleNext;
          if (pageNumber != maxPages) {titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);} else {
            titleNext = Localization.Main.getText("title.lastpage");
          }

          entry = FeedHelper.INSTANCE.getNextLink(urlExt, titleNext);
        } else {
          entry = FeedHelper.INSTANCE.getCatalogEntry(title, urn, urlInItsSubfolder, summary, icon);
        }
      } // End of Page leadout
    }
    return new Composite<Element, String>(entry, urlInItsSubfolder);
  }
  */

  /**
   * Get a list of books split by letter
   * It is invoked when a list of books is to be further sub-divided by letter.
   *
   * @param pBreadcrumbs
   * @param mapOfBooksByLetter
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @param splitOption
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
  private List<Element> getListOfBooksSplitByLetter(Breadcrumbs pBreadcrumbs,
      Map<String,  List<Book>> mapOfBooksByLetter,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {
    if (Helper.isNullOrEmpty(mapOfBooksByLetter))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfBooksByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = SecureFileManager.INSTANCE.getSplitFilename(baseFilename, letter);
      String letterUrn = Helper.getSplitString(baseUrn, letter, ":");

      List<Book> booksInThisLetter = mapOfBooksByLetter.get(letter);
      String letterTitle;
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.book.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("bookword.title"),
                                                letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);

      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeBooks(booksInThisLetter);

      Element element = null;
      // ITIMPI:  Asert to check if the logic can ever let this be zero!
      assert (booksInThisLetter.size() > 0) : "booksInThisLetter=" + booksInThisLetter.size() + " for letter '" + letter + "'";
      if (booksInThisLetter.size() > 0) {
        if (letter.length() < maxSplitLevels) {
          element = getListOfBooks(pBreadcrumbs, booksInThisLetter, 0, letterTitle, summary, letterUrn, letterFilename,
              SplitOption.SplitByLetter , icon, options).getFirstElement();
        } else {
          element = getListOfBooks(pBreadcrumbs, booksInThisLetter, 0, letterTitle, summary, letterUrn, letterFilename,
              SplitOption.Paginate, icon, options).getFirstElement();
        }
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }

  /**
   * Get a list of books split by date
   *
   * These lists are used in the Recent Books catalog sub-section.
   *
   * @param pBreadcrumbs
   * @param mapOfBooksByDate
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
  private List<Element> getListOfBooksSplitByDate(Breadcrumbs pBreadcrumbs,
      Map<DateRange, List<Book>> mapOfBooksByDate,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      String icon,
      Option... options) throws IOException {
    if (Helper.isNullOrEmpty(mapOfBooksByDate))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<DateRange> ranges = new TreeSet<DateRange>(mapOfBooksByDate.keySet());
    for (DateRange range : ranges) {
      // generate the range file
      String rangeFilenameCleanedUp = SecureFileManager.INSTANCE.decode(baseFilename);
      int pos = rangeFilenameCleanedUp.indexOf(".xml");
      if (pos > -1)
        rangeFilenameCleanedUp = rangeFilenameCleanedUp.substring(0, pos);
      String rangeFilename = rangeFilenameCleanedUp + "_" + range + ".xml";
      rangeFilename = SecureFileManager.INSTANCE.encode(rangeFilename);

      String rangeUrn = Helper.getSplitString(baseUrn, range.toString(), ":");

      String rangeTitle = LocalizationHelper.INSTANCE.getEnumConstantHumanName(range);
      List<Book> booksInThisRange = mapOfBooksByDate.get(range);

      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeBooks(booksInThisRange);

      Element element = null;
      if (booksInThisRange.size() > 0) {
        element = getListOfBooks(pBreadcrumbs, booksInThisRange, 0, rangeTitle, summary, rangeUrn, rangeFilename, SplitOption.Paginate, icon, options)
            .getFirstElement();
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }

  /**
   * Add the aquistion links
   *
   * These are used to specify where a book can be downloaded from.
   * They will not be needed if generation of download links is suppressed.
   *
   * @param book
   * @param entry
   */
  private void addAcquisitionLinks(Book book, Element entry) {
    if (!currentProfile.getGenerateDownloads()) {
      if (logger.isTraceEnabled())
        logger.trace("addAcquisitionLinks: exit: download links suppressed");
      return;
    }

    // links to the ebook files
    if (logger.isTraceEnabled())
      logger.trace("addAcquisitionLinks: links to the ebook files");
    for (EBookFile file : book.getFiles()) {
      // prepare to copy the ebook file
      if (logger.isTraceEnabled())
        logger.trace("addAcquisitionLinks: prepare to copy the ebook file " + file.getName());
      // ITIMPI  Why is EPUB treated as a special case?
      if (file.getFormat() == EBookFormat.EPUB)
        getCatalogManager().addFileToTheMapOfFilesToCopy(file.getFile(), book);
      else
        getCatalogManager().addFileToTheMapOfFilesToCopy(file.getFile());

      entry.addContent(FeedHelper.INSTANCE.getAcquisitionLink(
          "../../" + FeedHelper.INSTANCE.urlEncode(book.getPath(), true) + "/" + FeedHelper.INSTANCE.urlEncode(file.getName() + file.getExtension(), true),
          file.getFormat().getMime(), // Mime type
          Localization.Main.getText("bookentry.download", file.getFormat())));

      // if the IncludeOnlyOneFile option is set, break to avoid publishing other files
      if (currentProfile.getIncludeOnlyOneFile()) {
        if (logger.isTraceEnabled())
          logger.trace("addAcquisitionLinks: break to avoid publishing other files");
        break;
      }
    }
  }

  /**
   * Add cover links
   *
   * Handles adding both the cover link and the thumbnail links
   *
   * Works out what images to use and whether new one optimized for calibre2opds
   * usage need to be generated and saves results of test for later use
   *
   * @param book
   * @param entry
   */
  private void addCoverLink(Book book, Element entry) {

    if (logger.isDebugEnabled())
      logger.debug("addCoverLink for " + book.getTitle());
    CachedFile coverFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), Constants.CALIBRE_COVER_FILENAME);
    if (coverFile.exists()) {
      // add the cover

      // prepare to copy the cover file

      getCatalogManager().addFileToTheMapOfFilesToCopy(coverFile);

      // Add the Cover link

      String coverUri = getCoverManager().getImageUri(book);

      // get the generated cover filename
      CachedFile resizedCoverFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), getCoverManager().getResultFilename(book));

      // prepare to copy the thumbnail if we are using them file
      if (currentProfile.getCoverResize()) {
        getCatalogManager().addFileToTheMapOfFilesToCopy(resizedCoverFile);

        if (!resizedCoverFile.exists() || getCoverManager().hasImageSizeChanged() || resizedCoverFile.lastModified() < coverFile.lastModified()) {
          if (logger.isTraceEnabled()) {
            if (!resizedCoverFile.exists())
              logger.trace("addCoverLink: resizedCover set to be generated (not already existing)");
            else if (getCoverManager().hasImageSizeChanged())
              logger.trace("addCoverLink: resizedCover set to be generated (image size changed)");
            else if (resizedCoverFile.lastModified() < coverFile.lastModified())
              logger.trace("addCoverLink: resizedCover set to be generated (new cover)");
          }
          getCoverManager().setImageToGenerate(resizedCoverFile, coverFile);
        } else {
          if (logger.isTraceEnabled())
            logger.trace("addCoverLink: resizedCover not to be generated");
        }
      } else {
        // Not using resized covers - use original cover.jpg

        if (resizedCoverFile.exists()) {
          // Safety check we never delete the Calibre cover
          if (0 == resizedCoverFile.getName().compareTo(Constants.CALIBRE_COVER_FILENAME)) {
            logger.warn("attempt to delete Calibre cover for book " + book.getTitle());
          } else {
            if (logger.isTraceEnabled())
              logger.trace("addCoverLink: coverResize=false. Delete " + resizedCoverFile.getName());
          }
          resizedCoverFile.delete();
          // Make sure it is no longer in the cache
          CachedFileManager.INSTANCE.removeCachedFile(resizedCoverFile);
        } else {
          if (logger.isTraceEnabled())
            logger.trace("addCoverLink: coverResize=false. No resizedCover file for book " + book.getTitle());
        }
        // Change URI name to user cover.jpg
        coverUri = FeedHelper.INSTANCE.urlEncode("../../" + book.getPath() + "/" + Constants.CALIBRE_COVER_FILENAME, true);
      }
      if (logger.isTraceEnabled())
        logger.trace("addCoverLink: coverUri=" + coverUri);

      entry.addContent(FeedHelper.INSTANCE.getCoverLink(coverUri));
    }

    // add the thumbnail link
    String thumbnailUri;
    if (coverFile.exists()) {
      thumbnailUri = getThumbnailManager().getImageUri(book);
      CachedFile thumbnailFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), getThumbnailManager().getResultFilename(book));
      // Take into account whether thumbnail generation suppressed
      if (currentProfile.getThumbnailGenerate()) {
        // Using generated thumbnail files

        // prepare to copy the thumbnail file
        getCatalogManager().addFileToTheMapOfFilesToCopy(thumbnailFile);

        // generate the file if does not exist or size changed
        if (!thumbnailFile.exists() || getThumbnailManager().hasImageSizeChanged() || thumbnailFile.lastModified() < coverFile.lastModified()) {
          if (logger.isTraceEnabled()) {
            if (!thumbnailFile.exists())
              logger.trace("addCoverLink: thumbnail set to be generated (not already existing)");
            else if (getThumbnailManager().hasImageSizeChanged())
              logger.trace("addCoverLink: thumbnail set to be generated (image size changed)");
            else if (thumbnailFile.lastModified() < coverFile.lastModified())
              logger.trace("addCoverLink: thumbnail set to be generated (new cover)");
          }
          getThumbnailManager().setImageToGenerate(thumbnailFile, coverFile);
        } else {
          if (logger.isTraceEnabled())
            logger.trace("addCoverLink: thumbnail not to be generated");
        }
      } else {
        // Not generating thumbnails - using existing cover.jpg
        if (thumbnailFile.exists()) {
          if (thumbnailFile.getName().compareTo("cover.jpg") == 0) {
            logger.warn("attempt to delete Calibre cover (for book " + book.getTitle());
          } else {
            if (logger.isTraceEnabled())
              logger.trace("addCoverLink: Delete existing thumbnail file " + thumbnailFile.getName());
            thumbnailFile.delete();
            // Make sure it is no longer in the cache
            CachedFileManager.INSTANCE.removeCachedFile(thumbnailFile);
          }
        }
        CachedFileManager.INSTANCE.removeCachedFile(thumbnailFile);
        // Change URI name to user cover.jpg
        thumbnailUri = FeedHelper.INSTANCE.urlEncode("../../" + book.getPath() + "/" + Constants.CALIBRE_COVER_FILENAME, true);
      }
    } else {
      // resize the default thumbnail if needed
      File resizedDefaultThumbnail = new File(getCatalogManager().getCatalogFolder(), Constants.DEFAULT_RESIZED_THUMBNAIL_FILENAME);
      File defaultThumbnail = new File(getCatalogManager().getCatalogFolder(), Constants.DEFAULT_THUMBNAIL_FILENAME);
      if (!resizedDefaultThumbnail.exists() || getThumbnailManager().hasImageSizeChanged() || resizedDefaultThumbnail.lastModified() < defaultThumbnail.lastModified()) {
        getThumbnailManager().setImageToGenerate(resizedDefaultThumbnail, defaultThumbnail);
      }

      // Change URI name to user default thumbnail
      thumbnailUri = FeedHelper.INSTANCE.urlEncode("../" + Constants.DEFAULT_RESIZED_THUMBNAIL_FILENAME, true);
    }

    if (logger.isTraceEnabled())
      logger.trace("addCoverLink: thumbNailUri=" + thumbnailUri);

    getThumbnailManager().addBook(book, thumbnailUri);
    entry.addContent(FeedHelper.INSTANCE.getThumbnailLink(thumbnailUri));
  }

  /**
   * Add book cross reference links
   *
   * Used when constructing book details entries
   *
   * @param entry
   * @param book
   */
  private void addNavigationLinks(Element entry, Book book) {
    if (currentProfile.getGenerateCrossLinks()) {
      // add the series link
      // (but only if we generate a series catalog)
      if (currentProfile.getGenerateSeries()) {
        if (book.getSeries() != null && DataModel.INSTANCE.getMapOfBooksBySeries().get(book.getSeries()).size() > 1) {
          if (logger.isTraceEnabled())
            logger.trace("addNavigationLinks: add the series link");
          entry.addContent(FeedHelper.INSTANCE.getRelatedLink(
              getCatalogManager().getCatalogFileUrlInItsSubfolder(SecureFileManager.INSTANCE.encode("series_" + book.getSeries().getId() + ".xml")),
              Localization.Main.getText("bookentry.series", book.getSerieIndex(), book.getSeries().getName())));
        }
      }

      // add the author page link(s)
      // (but only if we generate an authors catalog)
      if (currentProfile.getGenerateSeries()) {
        if (book.hasAuthor()) {
          if (logger.isTraceEnabled())
            logger.trace("addNavigationLinks: add the author page link(s)");
          for (Author author : book.getAuthors()) {
            int nbBooks = DataModel.INSTANCE.getMapOfBooksByAuthor().get(author).size();
            entry.addContent(FeedHelper.INSTANCE
                .getRelatedLink(getCatalogManager().getCatalogFileUrlInItsSubfolder(SecureFileManager.INSTANCE.encode("author_" + author.getId() + ".xml")),
                    Localization.Main.getText("bookentry.author", Summarizer.INSTANCE.getBookWord(nbBooks), author.getName())));
          }
        }
      }

      // add the tags links
      // (but only if we generate a tags catalog)
      if (currentProfile.getGenerateTags()) {
        if (Helper.isNotNullOrEmpty(book.getTags())) {
          if (logger.isTraceEnabled())
            logger.trace("addNavigationLinks: add the tags links");
          for (Tag tag : book.getTags()) {
            int nbBooks = DataModel.INSTANCE.getMapOfBooksByTag().get(tag).size();
            if (nbBooks > 1) {
              entry.addContent(FeedHelper.INSTANCE
                  .getRelatedLink(getCatalogManager().getCatalogFileUrlInItsSubfolder(SecureFileManager.INSTANCE.encode("tag_" + tag.getId() + ".xml")),
                      Localization.Main.getText("bookentry.tags", Summarizer.INSTANCE.getBookWord(nbBooks), tag.getName())));
            }
          }
        }
      }

      // add the ratings links
      if (currentProfile.getGenerateRatings() && book.getRating() != BookRating.NOTRATED) {
        if (logger.isTraceEnabled())
          logger.trace("addNavigationLinks: add the ratings links");
        int nbBooks = DataModel.INSTANCE.getMapOfBooksByRating().get(book.getRating()).size();
        if (nbBooks > 1) {
          entry.addContent(FeedHelper.INSTANCE.getRelatedLink(
              getCatalogManager().getCatalogFileUrlInItsSubfolder(SecureFileManager.INSTANCE.encode("rated_" + book.getRating().getId() + ".xml")),
              Localization.Main.getText("bookentry.ratings", Summarizer.INSTANCE.getBookWord(nbBooks),
                  LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating()))));
        }
      }
    }
  }

  /**
   * Add links for further information about a book
   *
   * Used when constructing the Book Details pages
   *
   * @param entry
   * @param book
   */
  private void addExternalLinks(Element entry, Book book) {
    if (currentProfile.getGenerateExternalLinks()) {
      String url;
      // add the GoodReads book link
      if (logger.isTraceEnabled())
        logger.trace("addExternalLinks: add the GoodReads book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getGoodreadIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.goodreads")
          ));

        url = currentProfile.getGoodreadReviewIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(
              FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.goodreads.review")));
      } else {
        url = currentProfile.getGoodreadTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.INSTANCE
              .getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(book.getTitle())), Localization.Main.getText("bookentry.goodreads")
              ));
      }

      // add the Wikipedia book link
      if (logger.isTraceEnabled())
        logger.trace("addExternalLinks: add the Wikipedia book link");
      url = currentProfile.getWikipediaUrl();
      if (Helper.isNotNullOrEmpty(url))
        entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(
            MessageFormat.format(url, currentProfile.getWikipediaLanguage(), FeedHelper.INSTANCE.urlEncode(book.getTitle()
            )),
            Localization.Main.getText("bookentry.wikipedia")));

      // Add Librarything book link
      if (logger.isTraceEnabled())
        logger.trace("addExternalLinks: Add Librarything book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getLibrarythingIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(
              FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.librarything")));
      } else if (Helper.isNotNullOrEmpty(book.getTitle())) {
        url = currentProfile.getLibrarythingTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(
              MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(book.getTitle()), FeedHelper.INSTANCE.urlEncode(book.getMainAuthor().getName())),
              Localization.Main.getText("bookentry.librarything")));
      }

      // Add Amazon book link
      if (logger.isTraceEnabled())
        logger.trace("addExternalLinks: Add Amazon book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getAmazonIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.amazon")));
      } else if (book.getMainAuthor() != null && Helper.isNotNullOrEmpty(book.getTitle())) {
        url = currentProfile.getAmazonTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(
              MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(book.getTitle()), FeedHelper.INSTANCE.urlEncode(book.getMainAuthor().getName())),
              Localization.Main.getText("bookentry.amazon")));
      }

      // Author Links
      if (book.hasAuthor()) {
        // add the GoodReads author link
        if (logger.isTraceEnabled())
          logger.trace("addExternalLinksy: add the GoodReads author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getGoodreadAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.goodreads.author", author.getName())));
        }

        // add the Wikipedia author link
        if (logger.isTraceEnabled())
          logger.trace("addExternalLinks: add the Wikipedia author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getWikipediaUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(currentProfile.getWikipediaUrl(),
                currentProfile.getWikipediaLanguage(), FeedHelper.INSTANCE.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.wikipedia.author", author.getName())));
        }

        // add the LibraryThing author link
        if (logger.isTraceEnabled())
          logger.trace("addExternalLinks: add the LibraryThing author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getLibrarythingAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(
                // LibraryThing is very peculiar on how it looks up it's authors... format is LastNameFirstName[Middle]
                MessageFormat.format(currentProfile.getLibrarythingAuthorUrl(),
                    FeedHelper.INSTANCE.urlEncode(author.getSort().replace(",", "").replace(" ", ""))),
                Localization.Main.getText("bookentry.librarything.author", author.getName())));
        }

        // add the Amazon author link
        if (logger.isTraceEnabled())
          logger.trace("addExternalLinks: add the Amazon author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getAmazonAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.amazon.author", author.getName())));
        }

        // add the ISFDB author link
        if (logger.isTraceEnabled())
          logger.trace("addExternalLinks: add the ISFDB author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getIsfdbAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.INSTANCE.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.INSTANCE.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.isfdb.author", author.getName())));
        }
      }
    }
  }

  /**
   * Generate a book enty in a catalog
   *
   * The amount of detail added depends on whether we are generating
   * a partial book entry (for a list of books) or a full entry (for book details)
   *
   * We use a common function as some of the data must be the same in both
   * the full and partial entries for a book.
   *
   * @param entry
   * @param book
   * @param isFullEntry
   */
  private void decorateBookEntry(Element entry, Book book, boolean isFullEntry) {
    if (book.hasAuthor()) {
      for (Author author : book.getAuthors()) {
        Element authorElement = JDOM.INSTANCE.element("author")
            .addContent(JDOM.INSTANCE.element("name")
            .addContent(author.getName()))
            .addContent(JDOM.INSTANCE.element("uri").addContent("author_" + author.getId() + ".xml"));
        entry.addContent(authorElement);
      }
    }

    // published element
    if (logger.isTraceEnabled()) {logger.trace("decorateBookEntry: published element");}
    Element published = FeedHelper.INSTANCE.getPublishedTag(book.getPublicationDate());
    entry.addContent(published);

    // dublin core - language
    for (Language language : book.getBookLanguages()) {
      Element dcLang = FeedHelper.INSTANCE.getDublinCoreLanguageElement(language.getIso2());
      entry.addContent(dcLang);
    }

    // dublin core - publisher
    Publisher publisher = book.getPublisher();
    if (Helper.isNotNullOrEmpty(publisher)) {
      Element dcPublisher = FeedHelper.INSTANCE.getDublinCorePublisherElement(publisher.getName());
      entry.addContent(dcPublisher);
    }

    // categories
    if (Helper.isNotNullOrEmpty(book.getTags())) {
      // tags
      for (Tag tag : book.getTags()) {
        Element categoryElement = FeedHelper.INSTANCE.getCategoryElement(tag.getName());
        entry.addContent(categoryElement);
      }
      // series
      if (currentProfile.getIncludeSeriesInBookDetails() && Helper.isNotNullOrEmpty(book.getSeries())) {
        Element categoryElement = FeedHelper.INSTANCE.getCategoryElement(book.getSeries().getName());
        entry.addContent(categoryElement);
      }
    }

    // book description
    if (isFullEntry) {
      // content element
      if (logger.isTraceEnabled())
        logger.trace("decorateBookEntry: content element");
      Element content = JDOM.INSTANCE.element("content").setAttribute("type", "text/html");
      boolean hasContent = false;
      if (logger.isTraceEnabled())
        logger.trace("decorateBookEntry: computing comments");
      // Series (if present and wanted)
      if (currentProfile.getIncludeSeriesInBookDetails() && Helper.isNotNullOrEmpty(book.getSeries())) {
        String data = Localization.Main.getText("content.series.data", book.getSerieIndex(), book.getSeries().getName());
        content.addContent(JDOM.INSTANCE.element("strong")
               .addContent(Localization.Main.getText("content.series") + " "))
               .addContent(data)
               .addContent(JDOM.INSTANCE.element("br"))
               .addContent(JDOM.INSTANCE.element("br"));
        hasContent = true;
      }
      // Tags (if present and wanted)
      // If the user has requested tags we output this section even if the list is empty.
      // The assumption is that the user in this case wants to see that no tags have been assigned
      // If we get feedback that this is not  a valid addumption then we could omit it when the list is empty
      if (currentProfile.getIncludeTagsInBookDetails()) {
        if (Helper.isNotNullOrEmpty(book.getTags())) {
          String tags = book.getTags().toString();
          if (tags != null  && tags.startsWith("["))
            // Remove braces added by java around a list
            tags = tags.substring(1, tags.length()-1);
          else
            // If no tags then we need an empty string (is this possible)
            tags = "";
          content.addContent(JDOM.INSTANCE.element("strong")
                 .addContent(Localization.Main.getText("content.tags") + " "))
                 .addContent(tags)
                 .addContent(JDOM.INSTANCE.element("br"))
                 .addContent(JDOM.INSTANCE.element("br"));
          hasContent = true;
        }
      }
      // Publisher (if present and wanted)
      if (currentProfile.getIncludePublisherInBookDetails()) {
        if (Helper.isNotNullOrEmpty(book.getPublisher())) {
          content.addContent(JDOM.INSTANCE.element("strong")
                  .addContent(Localization.Main.getText("content.publisher") + " "))
                  .addContent(book.getPublisher().getName())
                  .addContent(JDOM.INSTANCE.element("br"))
                  .addContent(JDOM.INSTANCE.element("br"));
          hasContent = true;
        }
      }
      // Published date (if present and wanted)
      if (currentProfile.getIncludePublishedInBookDetails()) {
        Date pubtmp = book.getPublicationDate();
        if (Helper.isNotNullOrEmpty(pubtmp)) {
          if (currentProfile.getPublishedDateAsYear()) {
            content.addContent(JDOM.INSTANCE.element("strong")
                .addContent(Localization.Main.getText("content.published") + " "))
                .addContent(PUBLICATIONDATE_FORMAT.format(book.getPublicationDate()))
                .addContent(JDOM.INSTANCE.element("br"))
                .addContent(JDOM.INSTANCE.element("br"));
          }
        }
      }

      // Added date (if present and wanted)
      if (currentProfile.getIncludeAddedInBookDetails()) {
        Date addtmp = book.getTimestamp();
        if (Helper.isNotNullOrEmpty(addtmp)) {
          content.addContent(JDOM.INSTANCE.element("strong")
              .addContent(Localization.Main.getText("content.added") + " "))
              .addContent(DATE_FORMAT.format(addtmp))
              .addContent(JDOM.INSTANCE.element("br"))
              .addContent(JDOM.INSTANCE.element("br"));
        }
      }


      // Modified date (if present and wanted)
      if (currentProfile.getIncludeModifiedInBookDetails()) {
        Date modtmp = book.getModified();
        if (Helper.isNotNullOrEmpty(modtmp)) {
          content.addContent(JDOM.INSTANCE.element("strong")
              .addContent(Localization.Main.getText("content.modified") + " "))
              .addContent(DATE_FORMAT.format(modtmp))
              .addContent(JDOM.INSTANCE.element("br"))
              .addContent(JDOM.INSTANCE.element("br"));
        }
      }

      List<Element> comments = JDOM.INSTANCE.convertBookCommentToXhtml(book.getComment());
      if (Helper.isNotNullOrEmpty(comments)) {
        if (logger.isTraceEnabled())
          logger.trace("decorateBookEntry: got comments");
        content.addContent(JDOM.INSTANCE.newParagraph()
               .addContent(JDOM.INSTANCE.element("strong")
               .addContent(Localization.Main.getText("content.summary"))));
        for (Element p : comments) {
          content.addContent(p.detach());
        }
        hasContent = true;
      }  else {
        if (Helper.isNotNullOrEmpty(book.getComment())) {
          logger.warn(Localization.Main.getText("warn.badComment", book.getId() , book.getTitle()));
          logger.warn(book.getComment());
          book.setComment("");
        }
      }
      if (hasContent) {
        if (logger.isTraceEnabled())
          logger.trace("decorateBookEntry: had content");
        entry.addContent(content);
      }
    } else {
      // summary element (the shortened book comment)
      if (logger.isTraceEnabled())
        logger.trace("getBookEntry: short comment");
      String summary = book.getSummary(currentProfile.getMaxBookSummaryLength());
      // If we had anything for the summary then it needs to be added to the output.
      if (Helper.isNotNullOrEmpty(summary))
        entry.addContent(JDOM.INSTANCE.element("summary").addContent(summary));
    }

    // acquisition links
    addAcquisitionLinks(book, entry);

    // cover and thumbnail links
    addCoverLink(book, entry);

    if (isFullEntry) {
      // navigation links
      addNavigationLinks(entry, book);

      // external links
      addExternalLinks(entry, book);
    }
  }

  /**
   * Control generating a book Full Details entry
   *
   * @param pBreadcrumbs
   * @param book
   * @param options
   * @return
   * @throws IOException
   */
  private Element getBookEntry(Breadcrumbs pBreadcrumbs, Book book, Option... options) throws IOException {
    if (logger.isDebugEnabled())
      logger.debug("getBookEntry: pBreadcrumbs=" + pBreadcrumbs + ", book=" + book);

    //CatalogContext.INSTANCE.getCallback().showMessage(pBreadcrumbs.toString() + "/" + book.getTitle());
    if (!isInDeepLevel() && isBookTheStepUnit())
      CatalogContext.INSTANCE.getCallback().incStepProgressIndicatorPosition();

    String filename = "book_" + book.getId() + ".xml";
    if (logger.isDebugEnabled())
      logger.debug("getBookEntry:" + book);
    if (logger.isTraceEnabled()) {
      logger.trace("getBookEntry:" + pBreadcrumbs.toString());
      logger.trace("getBookEntry: generating " + filename);
    }

    // construct the contextual title (including the date, or the series, or the rating)
    String title;
    if (Option.contains(options, Option.INCLUDE_SERIE_NUMBER))
      if (book.getSerieIndex() != 0) {
        title = book.getTitleWithSerieNumber();
      } else {
        title = book.getTitle();
      }
    else if (Option.contains(options, Option.INCLUDE_TIMESTAMP))
      title = book.getTitle() + " [" + DATE_FORMAT.format(book.getTimestamp()) + "]";
    else if (!Option.contains(options, Option.DONOTINCLUDE_RATING) && !currentProfile.getSuppressRatingsInTitles())
      title = book.getTitleWithRating(Localization.Main.getText("bookentry.rated"), LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating()));
    else
      title = book.getTitle();

    String urn = "calibre:book:" + book.getId();
    filename = SecureFileManager.INSTANCE.encode(filename);

    if (logger.isTraceEnabled())
      logger.trace("getBookEntry: checking book in the Catalog manager");
    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    String fullEntryUrl = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
    if (getCatalogManager().addBookEntryFile(outputFile)) {
      if (logger.isTraceEnabled())
        logger.trace("getBookEntry: book was not yet done");

      // generate the book full entry
      generateBookFullEntryFile(pBreadcrumbs, book, outputFile, fullEntryUrl);
    }

    Element entry = FeedHelper.INSTANCE.getBookEntry(title, urn, book.getLatestFileModifiedDate());

    // add the required data to the book entry
    decorateBookEntry(entry, book, false);

    // add a full entry link to the partial entry
    if (logger.isTraceEnabled())
      logger.trace("add a full entry link to the partial entry");
    entry.addContent(FeedHelper.INSTANCE.getFullEntryLink(fullEntryUrl));

    return entry;
  }

  /**
   * Generate the Book Details
   *
   * @param pBreadcrumbs
   * @param book
   * @param outputFile
   * @param fullEntryUrl
   * @throws IOException
   */
  private void generateBookFullEntryFile(Breadcrumbs pBreadcrumbs, Book book, File outputFile, String fullEntryUrl) throws IOException {FileOutputStream fos = null;
    if (logger.isDebugEnabled())
      logger.debug("generateBookFullEntryFile: " + book);

    Breadcrumbs breadcrumbs = pBreadcrumbs;

    // if the "all books" catalog never was generated, we'll end up with the first generated catalog's breadcrumbs ; that ain't good, I prefer linking only to main
    if (!currentProfile.getGenerateAllbooks()) {
      // remove all but the first (main) entry
      breadcrumbs = new Breadcrumbs();
      breadcrumbs.add(pBreadcrumbs.get(0));
    }

    Document document = new Document();
    try {
      fos = new FileOutputStream(outputFile);

      Element entry = JDOM.INSTANCE.rootElement("entry", JDOM.Namespace.Atom, JDOM.Namespace.DcTerms, JDOM.Namespace.Atom, JDOM.Namespace.Xhtml);

      String sTitle = book.getTitle();
      Element title = JDOM.INSTANCE.element("title").addContent(sTitle);
      entry.addContent(title);

      Element id = JDOM.INSTANCE.element("id").addContent("urn:book:" + book.getUuid());
      entry.addContent(id);

      // updated element
      if (logger.isTraceEnabled())
        logger.trace("getBookFullEntry: updated element");
      Element updated = FeedHelper.INSTANCE.getUpdatedTag(book.getLatestFileModifiedDate());
      entry.addContent(updated);

      // add the navigation links
      FeedHelper.INSTANCE.decorateElementWithNavigationLinks(entry, breadcrumbs, sTitle, fullEntryUrl, true);

      // add the required data to the book entry
      decorateBookEntry(entry, book, true);

      // write the element to the file
      document.addContent(entry);
      JDOM.INSTANCE.getOutputter().output(document, fos);

    } catch (RuntimeException e) {
      // Exceptions are not expected, but if one does occur as well
      // as logging the details, also log the book that caused it
      logger.error("... " + book.getAuthors() + ": " + book.getTitle(), e);
      throw e;
      // Increment the warning count for advising the user to look at the log after the run
    } finally {
      if (fos != null)
        fos.close();
    }

    // create the same file as html
    try {
      getHtmlManager().generateHtmlFromXml(document, outputFile, HtmlManager.FeedType.BookFullEntry);
    } catch (Exception e) {
      logger.warn("Unable to create HTML for book id=" + book.getId() + "Title=" + book.getTitle() + " \noutputFile: " + outputFile + "\nException:\n" + e);
    } catch (Throwable t) {
      logger.warn("Unexpected error trying to create HTML for book id=" + book.getId() + "Title=" + book.getTitle() + " \noutputFile: " + outputFile + "\n" + t);
    }
    if (currentProfile.getGenerateIndex()) {
      // index the book
      logger.debug("getBookEntry: indexing book");
      IndexManager.INSTANCE.indexBook(book, getHtmlManager().getHtmlFilenameFromXmlFilename(fullEntryUrl), getThumbnailManager().getThumbnailUrl(book));
    }
  }

}
