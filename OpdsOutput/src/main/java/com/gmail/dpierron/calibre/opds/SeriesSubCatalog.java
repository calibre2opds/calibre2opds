package com.gmail.dpierron.calibre.opds;

/**
 * Class for implementing the Series type sub-catalogs
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SeriesSubCatalog extends BooksSubCatalog {
  private Logger logger = Logger.getLogger(SeriesSubCatalog.class);
  private List<Series> series;
  private Map<Series, List<Book>> mapOfBooksBySerie;

  public SeriesSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  /**
    * Construct a series object from a list of books
    *
    * @param books
    */
  public SeriesSubCatalog(List<Book> books) {
    super(books);
  }

  /**
   *    Get the series list for the books associated with this sub-catalog
   *    If it is not populated, then do so in alphabetical order
   *    (taking into account leading noise words).
   */
  private List<Series> getSeries() {
    if (series == null) {
      series = new Vector<Series>();
      for (Book book : getBooks()) {
        if (book.getSeries() != null && !series.contains(book.getSeries()))
          series.add(book.getSeries());
      }
      
      final String bookLang = getBooks().get(0).getBookLanguage(ConfigurationManager.INSTANCE.getCurrentProfile().getBookLanguageTag());
      
      // sort the series alphabetically
      Collections.sort(series, new Comparator<Series>() {
    
        public int compare(Series o1, Series o2) {
          String title1 = (o1 == null ? "" : NoiseWord.fromLanguage(bookLang).removeLeadingNoiseWords(o1.getName()));
          String title2 = (o2 == null ? "" : NoiseWord.fromLanguage(bookLang).removeLeadingNoiseWords(o2.getName()));
          return title1.compareTo(title2);
        }
      });
    
    }
    return series;
  }

  /**
    *
    * @return
    */
  private Map<Series, List<Book>> getMapOfBooksBySerie() {
    if (mapOfBooksBySerie == null) {
      mapOfBooksBySerie = new HashMap<Series, List<Book>>();
      for (Book book : getBooks()) {
        List<Book> books = mapOfBooksBySerie.get(book.getSeries());
        if (books == null) {
          books = new Vector<Book>();
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
    *
    * @param pBreadcrumbs
    * @param title
    * @param summary
    * @param urn
    * @param pFilename
    * @return
    * @throws IOException
    */
  public List<Element> getContentOfListOfSeries(Breadcrumbs pBreadcrumbs,
                                                String title,
                                                String summary,
                                                String urn,
                                                String pFilename)
                                                        throws IOException {
    if (Helper.isNullOrEmpty(getSeries()))
      return null;
    return getContentOfListOfSeries(pBreadcrumbs,
                                    getSeries(),
                                    0,
                                    title,
                                    summary,
                                    urn,
                                    pFilename,
                                    null,
                                    true);
  }

  /**
    *
    * @param pBreadcrumbs
    * @param series
    * @param from
    * @param title
    * @param summary
    * @param urn
    * @param pFilename
    * @param splitOption
    * @param addTheSeriesWordToTheTitle
    * @return
    * @throws IOException
    */

  private List<Element> getContentOfListOfSeries(Breadcrumbs pBreadcrumbs,
                                                 List<Series> series,
                                                 int from,
                                                 String title,
                                                 String summary,
                                                 String urn,
                                                 String pFilename,
                                                 SplitOption splitOption,
                                                 boolean addTheSeriesWordToTheTitle)
                                                        throws IOException {
    Map<String, List<Series>> mapOfSeriesByLetter = null;
    List<Element> result;

    boolean willSplitByLetter = (splitOption != SplitOption.Paginate);
    logger.trace("getContentOfListOfSeries: title=" + title);
    logger.trace("getContentOfListOfSeries: willSplitByLetter=" + willSplitByLetter
            + " (series.size() > ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit())="
            + (series.size() > ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit()));
    willSplitByLetter = willSplitByLetter && (series.size() > ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit());

    if (willSplitByLetter) {
      mapOfSeriesByLetter = DataModel.splitSeriesByLetter(series);
    } 

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from+1);
  
    String filename = SecureFileManager.INSTANCE.decode(pFilename);
    if (from > 0) {
      int pos = filename.lastIndexOf(".xml");
      if (pos >= 0) filename = filename.substring(0, pos);
      filename = filename + "_" + pageNumber;
    }
    if (!filename.endsWith(".xml"))
      filename = filename + ".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);
  
    // list the entries (or split them)
    if (willSplitByLetter)
    {
      // split the series list by letter
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs,
                                                          title,
                                                          getCatalogManager().getCatalogFileUrlInItsSubfolder(filename));
      result = getListOfSeriesSplitByLetter(breadcrumbs,
                                            mapOfSeriesByLetter,
                                            title,
                                            urn,
                                            pFilename,
                                            addTheSeriesWordToTheTitle);
    }
    else
    {
      // list the series list
      result = new Vector<Element>();
      for (int i=from; i<series.size(); i++)
      {
        if ((i-from) >= ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforePaginate())
        {
          Element nextLink = getListOfSeries(pBreadcrumbs,
                                            series,
                                            i,
                                            title,
                                            summary,
                                            urn,
                                            pFilename,
                                            splitOption,
                                            addTheSeriesWordToTheTitle);
          result.add(nextLink);
          break;
        }
        else
        {
          Series serie = series.get(i);
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs,
                                                              title,
                                                              getCatalogManager().getCatalogFileUrlInItsSubfolder(filename));
          Element entry = getSeries(breadcrumbs,
                                    serie,
                                    urn,
                                    addTheSeriesWordToTheTitle);
          if (entry != null)
          {
            result.add(entry);
            TrookSpecificSearchDatabaseManager.INSTANCE.addSeries(serie, entry);
          }
        }
      }
    }
    
    return result;
  }

  /**
    *
    * @param pBreadcrumbs
    * @param series
    * @param from
    * @param title
    * @param summary
    * @param urn
    * @param pFilename
    * @param splitOption
    * @param addTheSeriesWordToTheTitle
    * @return
    * @throws IOException
    */
  private Element getListOfSeries(Breadcrumbs pBreadcrumbs,
                                  List<Series> series,
                                  int from,
                                  String title,
                                  String summary,
                                  String urn,
                                  String pFilename,
                                  SplitOption splitOption,
                                  boolean addTheSeriesWordToTheTitle)
                                            throws IOException {
    int catalogSize;
    boolean willSplit = splitOption != SplitOption.Paginate && (series.size() > ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit());
    if (willSplit) {
      catalogSize = 0;
    } else
      catalogSize = series.size();

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from+1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);
  
    String filename = SecureFileManager.INSTANCE.decode(pFilename);
    if (from > 0) {
      int pos = filename.lastIndexOf(".xml");
      if (pos >= 0) filename = filename.substring(0, pos);
      filename = filename + "_" + pageNumber;
    }
    if (!filename.endsWith(".xml"))
      filename = filename + ".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);
  
    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    try {
      fos = new FileOutputStream(outputFile);
    
      Element feed = FeedHelper.INSTANCE.getFeed(pBreadcrumbs, title, urn, summary);
      
      // list the entries (or split them)
      List<Element> result = getContentOfListOfSeries(pBreadcrumbs,
                                                      series,
                                                      from,
                                                      title,
                                                      summary,
                                                      urn,
                                                      pFilename,
                                                      splitOption,
                                                      addTheSeriesWordToTheTitle);
    
      // add the entries to the feed
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
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages)
        titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);
      else
        titleNext = Localization.Main.getText("title.lastpage");
      
      entry = FeedHelper.INSTANCE.getNext(getCatalogManager().getCatalogFileUrlInItsSubfolder(filename), titleNext);
    }
    else
    {
      boolean weAreAlsoInSubFolder = pBreadcrumbs.size()>1;
      entry = FeedHelper.INSTANCE.getEntry(title,
                                           urn,
                                           getCatalogManager().getCatalogFileUrlInItsSubfolder(filename,weAreAlsoInSubFolder ),
                                           summary,
                                           // #751211: Use external icons option
                                           ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons()
                                               ? getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + StanzaConstants.ICONFILE_SERIES
                                               : StanzaConstants.ICON_SERIES);
    }
    return entry;
  }

  /**
    *
    * @param pBreadcrumbs
    * @param mapOfSeriesByLetter
    * @param baseTitle
    * @param baseUrn
    * @param baseFilename
    * @param addTheSeriesWordToTheTitle
    * @return
    * @throws IOException
    */
  private List<Element> getListOfSeriesSplitByLetter(Breadcrumbs pBreadcrumbs,
                                                     Map<String, List<Series>> mapOfSeriesByLetter,
                                                     String baseTitle,
                                                     String baseUrn,
                                                     String baseFilename,
                                                     boolean addTheSeriesWordToTheTitle)
                                                            throws IOException {
    if (Helper.isNullOrEmpty(mapOfSeriesByLetter)) 
      return null;
    
    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";
    
    List<Element> result = new Vector<Element>(mapOfSeriesByLetter.keySet().size());
    SortedSet<String> letters = new TreeSet<String>(mapOfSeriesByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String baseFilenameCleanedUp = SecureFileManager.INSTANCE.decode(baseFilename);
      int pos = baseFilenameCleanedUp.indexOf(".xml");
      if (pos > -1)
        baseFilenameCleanedUp = baseFilenameCleanedUp.substring(0, pos);
      String letterFilename = baseFilenameCleanedUp + "_"+letter+ ".xml";
      letterFilename = SecureFileManager.INSTANCE.encode(letterFilename);
      
      String letterUrn = baseUrn + ":" + letter;
      List<Series> seriesInThisLetter = mapOfSeriesByLetter.get(letter);
      String letterTitle;
      int itemsCount = seriesInThisLetter.size();
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.series.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("seriesword.title"), letter);
      Element element = null;
      if (itemsCount > 0) {
        // try and list the items to make the summary
        String summary = Summarizer.INSTANCE.summarizeSeries(seriesInThisLetter);

        element = getListOfSeries(pBreadcrumbs,
                                  seriesInThisLetter,
                                  0,
                                  letterTitle,
                                  summary,
                                  letterUrn,
                                  letterFilename,
                                  SplitOption.Paginate,
                                  addTheSeriesWordToTheTitle);
      } 
      
      if (element != null)
        result.add(element);
    }
    return result;
  }

   /**
    *
    * @param pBreadcrumbs
    * @param series
    * @param baseurn
    * @param addTheSeriesWordToTheTitle
    * @return
    * @throws IOException
    */
  private Element getSeries(Breadcrumbs pBreadcrumbs,
                            Series series,
                            String baseurn,
                            boolean addTheSeriesWordToTheTitle)
                                    throws IOException {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs+"/"+series);

    CatalogContext.INSTANCE.getCallback().showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogContext.INSTANCE.getCallback().incStepProgressIndicatorPosition();

    List<Book> books = getMapOfBooksBySerie().get(series);
    if (Helper.isNullOrEmpty(books)) return null;

    // sort the books by series index
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        Float index1 = (o1 == null ? Float.MIN_VALUE : o1.getSerieIndex());
        Float index2 = (o2 == null ? Float.MIN_VALUE : o2.getSerieIndex());
        return index1.compareTo(index2);
      }
    });

    String basename = "series_";
    String filename = getFilenamePrefix(pBreadcrumbs)+basename+series.getId()+".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);

    String title = series.getName();
    if (addTheSeriesWordToTheTitle)
      title = Localization.Main.getText("content.series") +" "+ title;
    String urn = baseurn+":"+series.getId();
    
    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(books);
    
    Element result = getListOfBooks(pBreadcrumbs,
                                    books,
                                    0,              // Starting at 0
                                    title,
                                    summary,
                                    urn,
                                    filename,
                                    // Bug #716917 Split on letter in series according to user option
                                    ConfigurationManager.INSTANCE.getCurrentProfile().getSplitInSeriesBooks()
                                            ? SplitOption.SplitByLetter
                                            : SplitOption.SplitOptionNone,
                                    // #751211: Use external icons option
                                    ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons()
                                            ? getCatalogManager().getPathToCatalogRoot(filename) + StanzaConstants.ICONFILE_SERIES
                                            : StanzaConstants.ICON_SERIES,
                                    Option.INCLUDE_SERIE_NUMBER);
    
    return result;
  }

  /**
    *
    * @param pBreadcrumbs
    * @return
    * @throws IOException
    */
  public Element getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getSeries())) return null;
    
    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename()+"_series.xml");
    String title = Localization.Main.getText("series.title");
    String urn = "calibre:series";
    
    String summary = "";
    if (getSeries().size() > 1)
      summary = Localization.Main.getText("series.alphabetical", series.size());
    else if (getSeries().size() == 1)
      summary = Localization.Main.getText("series.alphabetical.single");
  
    return getListOfSeries(pBreadcrumbs,
                           getSeries(),
                           0,           // Start at 0
                           title,
                           summary,
                           urn,
                           filename,
                           null,        // SplitOption
                           false);
  }

}
