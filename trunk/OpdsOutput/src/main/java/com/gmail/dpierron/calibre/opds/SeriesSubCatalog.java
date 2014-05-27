package com.gmail.dpierron.calibre.opds;

/**
 * Class for implementing the Series type sub-catalogs
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Helper;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class SeriesSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(SeriesSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());
  private List<Series> series;
  private Map<Series, List<Book>> mapOfBooksBySerie;


  public SeriesSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.SERIESLIST_TYPE);
  }

  /**
   * Construct a series object from a list of books
   *
   * @param books
   */
  public SeriesSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.SERIESLIST_TYPE);
  }

  /**
   * Get the series list for the books associated with this sub-catalog
   * If it is not populated, then do so in alphabetical order
   * (taking into account leading noise words).
   */
  public List<Series> getSeries() {
    if (series == null) {
      series = new LinkedList<Series>();
      for (Book book : getBooks()) {
        if (book.getSeries() != null && !series.contains(book.getSeries()))
          series.add(book.getSeries());
      }

      final Language bookLang = getBooks().get(0).getBookLanguage();

      // sort the series alphabetically
      Collections.sort(series, new Comparator<Series>() {

        public int compare(Series o1, Series o2) {
          String title1 = (o1 == null ? "" : NoiseWord.fromLanguage(bookLang).removeLeadingNoiseWords(o1.getName().toUpperCase()));
          String title2 = (o2 == null ? "" : NoiseWord.fromLanguage(bookLang).removeLeadingNoiseWords(o2.getName().toUpperCase()));
          return collator.compare(title1, title2);
        }
      });

    }
    return series;
  }

  /**
   * @return
   */
  private Map<Series, List<Book>> getMapOfBooksBySerie() {
    if (mapOfBooksBySerie == null) {
      mapOfBooksBySerie = new HashMap<Series, List<Book>>();
      for (Book book : getBooks()) {
        List<Book> books = mapOfBooksBySerie.get(book.getSeries());
        if (books == null) {
          books = new LinkedList<Book>();
          Series serie = book.getSeries();
          if (serie != null)
            mapOfBooksBySerie.put(serie, books);
        }
        books.add(book);
      }
    }
    return mapOfBooksBySerie;
  }

  /**
   * Get the list of series for embedding at the current level.
   *
   * This is used when you want to add a list of series to an existing
   * page without creating a link to a new page.
   *
   * This reoutine is called recursively to generate each file (page or split level)
   *
   * @param pBreadcrumbs
   * @param listSeries                      // Series to process.  Set to null if not known to take from catalog properties
   * @param from                        // Start point - set to 0 if not known
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption
   * @param addTheSeriesWordToTheTitle
   * @return
   * @throws IOException
   */

  public List<Element> getListOfSeries(
      Breadcrumbs pBreadcrumbs,
      List<Series> listSeries,        //  The list of series to be processed.
      boolean inSubDir,
      int from,                   // Start point - set to 0 if not known
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption,
      boolean addTheSeriesWordToTheTitle) throws IOException {

    // Set if not specified from catalog properties
    if (listSeries == null) listSeries = getSeries();
    if (pFilename == null) pFilename = getCatalogBaseFolderFileName();

    Map<String, List<Series>> mapOfSeriesByLetter = null;
    List<Element> result;

    if (logger.isTraceEnabled()) logger.trace("getListOfSeries: title=" + title);
    boolean willSplitByLetter;

    if (null == splitOption)
      splitOption = SplitOption.SplitByLetter;
    switch (splitOption) {
      // case DontSplit:
      case Paginate:
      case DontSplit:
        if (logger.isTraceEnabled())
          logger.trace("splitOption=" + splitOption);
        willSplitByLetter = false;
        break;

      default:
        if (logger.isTraceEnabled())
          logger.trace("getListOfSeries: splitOption=" + splitOption + ", series.size()=" + listSeries.size() + ", MaxBeforeSplit==" +
              maxBeforeSplit);
        willSplitByLetter = (maxSplitLevels != 0) &&  (listSeries.size() > maxBeforeSplit);
        break;
    }
    if (logger.isTraceEnabled())
      logger.trace("getListOfSeries:  willSplitByLetter=" + willSplitByLetter);

    if (willSplitByLetter) {
      mapOfSeriesByLetter = DataModel.splitSeriesByLetter(listSeries);
    }

    if (from > 0) inSubDir = true;
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);

    // list the entries (or split them)

    if (willSplitByLetter) {
      // split the series list by letter
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
      result = getListOfSeriesSplitByLetter(breadcrumbs,
                                            mapOfSeriesByLetter,
                                            inSubDir,
                                            title,
                                            urn,
                                            pFilename,
                                            addTheSeriesWordToTheTitle);
    } else {
      // list the series list
      result = new LinkedList<Element>();
      for (int i = from; i < listSeries.size(); i++) {
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          // TODO #c2o-208   Add Previous, First and Last links if needed
          Element nextLink =
              getSubCatalog(pBreadcrumbs,
                            listSeries,
                            true /*inSubDir*/,      // Must be in subDir if paginating!
                            i,
                            title,
                            summary,
                            urn,
                            pFilename,
                            splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption, addTheSeriesWordToTheTitle);
          result.add(0, nextLink);
          int maxPages = Summarizer.INSTANCE.getPageNumber(listSeries.size());
          break;
        } else {
          Series serie = listSeries.get(i);
          Breadcrumbs breadcrumbs;
          // #c2o-204:  If we are getting the series for an author then the breadcrumbs are already correct.
          if (this.getCatalogFolder().startsWith(Constants.AUTHOR_TYPE)) {
            breadcrumbs = pBreadcrumbs;
          } else {
            breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
          }
          Element entry = getSeriesEntry(breadcrumbs, serie, urn, addTheSeriesWordToTheTitle);
          if (entry != null) {
            result.add(entry);
            TrookSpecificSearchDatabaseManager.INSTANCE.addSeries(serie, entry);
          }
        }
      }
    }

    return result;
  }

  /**
   * Get a new Series sub-catalog.
   *
   * @param pBreadcrumbs
   * @param listSeries              series, or nuil to derive series from books
   * @param inSubDir
   * @param from
   * @param title
   * @param summary
   * @param urn
   * @param pFilename            if null then derived from getCatalogBaseFolderFileName()
   * @param splitOption
   * @param addTheSeriesWordToTheTitle
   * @return
   * @throws IOException
   */
    public Element getSubCatalog(Breadcrumbs pBreadcrumbs,
          List<Series> listSeries,
          boolean inSubDir,
          int from,
          String title,
          String summary,
          String urn,
          String pFilename,
          SplitOption splitOption,
          boolean addTheSeriesWordToTheTitle) throws IOException {

    if (listSeries == null) listSeries = getSeries();
    if (pFilename == null) pFilename = getCatalogBaseFolderFileName();
    if (from > 0) inSubDir = true;

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);
    String urlExt = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /* inSubDir*/);

    // Check for special case where the series name is equal to the split level.
    if (splitOption == SplitOption.SplitByLetter) {
      while (listSeries.size() > 0
          && pFilename.toUpperCase().endsWith(Constants.TYPE_SEPARATOR + listSeries.get(0).getName().toUpperCase())) {
        Series series = listSeries.get(0);
        listSeries.remove(0);
        Element element;
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        element = getSeriesEntry(breadcrumbs, series, urn, addTheSeriesWordToTheTitle);
        assert element != null;
        if (element != null) {
          feed.addContent(element);
        }
      }
    }

    int catalogSize = listSeries.size();
    if (summary == null) summary = catalogSize > 1 ? Localization.Main.getText("series.alphabetical", catalogSize)
                                                   : (catalogSize == 1 ? Localization.Main.getText("series.alphabetical.single") : "");
    if (urn == null)  urn = Constants.INITIAL_URN_PREFIX + Constants.URN_SEPARATOR + Constants.SERIESLIST_TYPE + getCatalogLevel();

    boolean willSplitByLetter = checkSplitByLetter(splitOption, catalogSize);
    if (willSplitByLetter) {
      catalogSize = 0;
    } else
      catalogSize = listSeries.size();

    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);


    // list the entries (or split them)
    List<Element> result = getListOfSeries(pBreadcrumbs,
                                           listSeries,
                                           inSubDir,
                                           from,
                                           title,
                                           summary,
                                           urn,
                                           pFilename,
                                           splitOption,
                                           addTheSeriesWordToTheTitle);

    // add the entries to the feed
    feed.addContent(result);


    Element entry;
    String urlInItsSubfolder = optimizeCatalogURL(CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
    entry = createPaginateLinks(feed, urlInItsSubfolder, pageNumber, maxPages);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
    if (from == 0) {
      entry = FeedHelper.getCatalogEntry(title, urn, urlInItsSubfolder, summary,
          // #751211: Use external icons option
          useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_SERIES : Icons.ICON_SERIES);
    }
    return entry;
  }

  /**
   * @param pBreadcrumbs
   * @param mapOfSeriesByLetter
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @param addTheSeriesWordToTheTitle
   * @return
   * @throws IOException
   */
  private List<Element> getListOfSeriesSplitByLetter(
      Breadcrumbs pBreadcrumbs,
      Map<String, List<Series>> mapOfSeriesByLetter,
      boolean inSubDir,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      boolean addTheSeriesWordToTheTitle) throws IOException {

    if (Helper.isNullOrEmpty(mapOfSeriesByLetter))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfSeriesByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = Helper.getSplitString(baseFilename,letter, Constants.TYPE_SEPARATOR);

      String letterUrn = Helper.getSplitString(baseUrn, letter, Constants.URN_SEPARATOR);
      List<Series> seriesInThisLetter = mapOfSeriesByLetter.get(letter);
      String letterTitle;
      int itemsCount = seriesInThisLetter.size();
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.series.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("seriesword.title"),
                                                letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);
      Element element = null;
      if (itemsCount > 0) {
        int maxBeforeSplit=0;
        // try and list the items to make the summary
        String summary = Summarizer.INSTANCE.summarizeSeries(seriesInThisLetter);

        element = getSubCatalog(pBreadcrumbs,
                                seriesInThisLetter,
                                true,        // inSubDir must be true if splitting by letter
                                0,
                                letterTitle,
                                summary,
                                letterUrn,
                                letterFilename,
                                checkSplitByLetter(letter),
                                addTheSeriesWordToTheTitle);
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }

  /**
   *  Get the base filename that is used to store a given series
   *
   * Since we always hold a complete set of series at the top level
   * the name can be derived purely knowing the series involved.
   *
   * NOTE.  This is NOT used in the case where we are doing the
   *        sub-set of a series for a given author.
   *
   * @param serie
   * @return
   */
  public static String getSeriesFolderFilenameNoLevel(Series serie) {
    return getCatalogBaseFolderFileNameIdNoLevelSplit(Constants.SERIES_TYPE, serie.getId(), 1000);
  }

  /**
   *  Get the base filename that is used to store a given series
   *  taking into account any level information
   *
   * @param serie
   * @return
   */
  public String getSeriesFolderFilenameWithLevel(Series serie) {
    return getCatalogBaseFolderFileNameIdSplit(Constants.SERIES_TYPE, serie.getId(), 1000);
  }
  /**
   * List the books that belong to the given series
   *
   * @param pBreadcrumbs
   * @param serie
   * @param baseurn
   * @param addTheSeriesWordToTheTitle
   * @return
   * @throws IOException
   */
  public Element getSeriesEntry(Breadcrumbs pBreadcrumbs, Series serie, String baseurn, boolean addTheSeriesWordToTheTitle) throws IOException {

    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + serie);

    CatalogManager.INSTANCE.callback.showMessage(pBreadcrumbs.toString());
    // We want to avoid incrementing the progress bar if we are not doing
    // the top level series sub-catalog
    if ( !(isInDeepLevel() || (getCatalogFolder().startsWith(Constants.AUTHOR_TYPE))))
      CatalogManager.INSTANCE.callback.incStepProgressIndicatorPosition();

    List<Book> books = getMapOfBooksBySerie().get(serie);
    if (Helper.isNullOrEmpty(books))
      return null;

    // sort the books by series index
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        Float index1 = (o1 == null ? Float.MIN_VALUE : o1.getSerieIndex());
        Float index2 = (o2 == null ? Float.MIN_VALUE : o2.getSerieIndex());
        return index1.compareTo(index2);
      }
    });

    String title = serie.getName();
    if (addTheSeriesWordToTheTitle)
      title = Localization.Main.getText("content.series") + ": " + title;
    String urn = baseurn + Constants.SERIES_TYPE + Constants.URN_SEPARATOR + serie.getId();
    // We need to determine if we are generating a serie within an author?
    // If we are we want the file to be in the author folder
    // if we are not then we want it at the top level
    String filename;
    if (getCatalogFolder().startsWith(getCatalogType())) {
      filename = getSeriesFolderFilenameWithLevel(serie);
    } else {
      filename = getCatalogBaseFolderFileNameId(serie.getId());
    }
    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(books);

    Element result = getListOfBooks(pBreadcrumbs,
                                    books,
                                    true,
                                    0,   // Starting at 0
                                    title,
                                    summary,
                                    urn,
                                    filename,
                                    SplitOption.Paginate,   // Do not split on letter in Series - it does not really make sense
                                    useExternalIcons      // #751211: Use external icons option
                                        ? getIconPrefix(true) + Icons.ICONFILE_SERIES : Icons.ICON_SERIES,
                                    null,
                                    Option.INCLUDE_SERIE_NUMBER);
    serie.setDone();
    return result;
  }
}
