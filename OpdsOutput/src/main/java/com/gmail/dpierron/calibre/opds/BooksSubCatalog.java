package com.gmail.dpierron.calibre.opds;

/**
 * Class that provides the facilities for listing the books in a catalog
 * The type specific catalogs will extend this class to inherit its functionality
 *
 * Inherits from:
 *   -> SubCatalog
 * Note that this is an abstract class so cannot be instantiated directly.
 */

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.calibre.opds.indexer.IndexManager;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BooksSubCatalog extends SubCatalog {
  private final static Logger logger = Logger.getLogger(BooksSubCatalog.class);
  protected final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());

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
    Collections.sort(books, new Comparator<Book>() {
      public int compare(Book o1, Book o2) {
        if (currentProfile.getSortUsingTitle())  {
          return Helper.checkedCollatorCompareIgnoreCase(o1.getTitle_Sort(), o2.getTitle_Sort(), collator);
        } else {
          return Helper.checkedCollatorCompareIgnoreCase(o1.getTitle(), o2.getTitle(), collator);
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
   * Get a list of books starting from a specific point
   *
   * This function is the control routine and is called once
   * for each page at the same level, or each time a split occurs.
   * It is called recursivel - and thus will be called once per file.
   *
   * ITIMPI:  At the moment this function can call itself recursively with the 'from'
   *          parameter being incremented.   It is likely to be much more efficient
   *          in both cpu load and memory usage to flatten the loop by rewriteing the
   *          function to elimiate recursion.
   *
   * @param pBreadcrumbs
   * @param listbooks
   * @param inSubDir
   * @param from
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption     This option how a list should be split if it exceeds size limits
   * @param icon
   * @param firstElements   Passed as null if not known
   * @param options
   * @return
   * @throws IOException
   */
  Element getListOfBooks(Breadcrumbs pBreadcrumbs,
      List<Book> listbooks,
      boolean inSubDir,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption,
      String icon,
      List<Element> firstElements,
      Option... options) throws IOException {
    if (logger.isDebugEnabled()) logger.debug("getListOfBooks: START");

    // Special case of first time through when not all values set
    if (listbooks == null) listbooks = getBooks();
    if (pFilename == null)  pFilename = getCatalogBaseFolderFileName();

    //  Now some consistency checks

    // Now get on with main processing
    int catalogSize = listbooks.size();
    if (logger.isDebugEnabled()) logger.debug("getListOfBooks:catalogSize=" + catalogSize);

    if (from != 0) inSubDir = true;
    if (pBreadcrumbs.size() > 1) inSubDir = true;
    if (inSubDir && icon.startsWith(Constants.CURRENT_PATH_PREFIX))
      icon = Constants.PARENT_PATH_PREFIX + icon.substring(2);

    // Work out any split options
    // Fixes #716917 when applied to author books list
    boolean willSplitByLetter;
    boolean willSplitByDate;
    if (splitOption == null) {
      // ITIMPI: Null seems to be equivalent to SplitByLetter !
      //         Might be better to replace calls by explicit value?
      splitOption = SplitOption.SplitByLetter;
      if (logger.isDebugEnabled()) logger.debug("getListOfBooks:splitOption=null.  Changed to SplitByLetter");
    }
    switch (splitOption) {
      case Paginate:
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=Paginate");
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case DontSplitNorPaginate:
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=DontSplitNorPaginate");
        assert from == 0 : "getListBooks: DontSplitNorPaginate, from=" + from;
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case DontSplit:
        // Bug #716917 Do not split on letter (used in Author and Series book lists)
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=DontSplit");
        assert from == 0 : "getListBooks: DontSplit, from=" + from;
        willSplitByLetter = false;
        willSplitByDate = false;
        break;
      case SplitByDate:
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=SplitByDate");
        assert from == 0 : "getListBooks: splitByDate, from=" + from;
        willSplitByLetter = checkSplitByLetter(splitOption, listbooks.size());
        willSplitByDate = true;
        break;
      case SplitByLetter:
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=SplitByLetter");
        assert from == 0 : "getListBooks: splitByLetter, from=" + from;
        willSplitByLetter = checkSplitByLetter(splitOption, listbooks.size());
        willSplitByDate = false;
        break;
      default:
        // ITIMPI:  Not sure that this case can ever arise
        //          Just added as a safety check
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=" + splitOption);
        assert from == 0 : "getListBooks: unknown splitOption, from=" + from;
        willSplitByLetter = checkSplitByLetter(splitOption, listbooks.size());
        willSplitByDate = false;
        break;
    }
    // See if SplitByLetter conditions actually apply?
    if ((currentProfile.getBrowseByCover())
    &&  (currentProfile.getBrowseByCoverWithoutSplit())) {
        willSplitByLetter = false;
    }
    if (logger.isTraceEnabled()) logger.trace("getListOfBooks:willSplitByLetter=" + willSplitByLetter);
    if (logger.isTraceEnabled()) logger.trace("getListOfBooks:willSplitByDate=" + willSplitByDate);
    if (logger.isTraceEnabled()) logger.trace("listing books from=" + from + ", title=" + title);

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber((willSplitByDate || willSplitByLetter) ? 0 : catalogSize);

    // generate the book list files
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);
    String urlExt = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);

    Element feed;
    feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /*inSubDir */);
    // Update breadcrumbs ready for next iteration
    Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);

    // list the books (or split them)
    List<Element> result;
    if (willSplitByDate) {
      // Split by date listing
      result = getListOfBooksSplitByDate(breadcrumbs,
                                         DataModel.splitBooksByDate(listbooks),
                                         true /*inSubDir*/,   // Musy be true if splitting by date
                                         title,
                                         urn,
                                         pFilename,
                                         icon,
                                         options);
    } else if (willSplitByLetter) {
      // Split by letter listing
      result = getListOfBooksSplitByLetter(breadcrumbs,
                                           DataModel.splitBooksByLetter(listbooks),
                                           true   /*inSubDir*/, // Must be true if splitting by letter
                                           title,
                                           urn,
                                           pFilename,
                                           SplitOption.SplitByLetter,
                                           icon,
                                           options);
    } else {
      // Paginated listing
      result = new LinkedList<Element>();
      String progressText = Breadcrumbs.getProgressText(breadcrumbs);
      progressText += " (" + Summarizer.INSTANCE.getBookWord(listbooks.size()) + ")";
      CatalogContext.INSTANCE.callback.showMessage(progressText.toString());
      for (int i = from; i < listbooks.size(); i++) {
        // check if we must continue
        CatalogContext.INSTANCE.callback.checkIfContinueGenerating();

        // See if we need to do the next page
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          // ... YES - so go for next page
          if (logger.isDebugEnabled()) logger.debug("making a nextpage link");
          Element nextLink = getListOfBooks(pBreadcrumbs,
                                            listbooks,
                                            true,             // Awlays in SubDir (need to check this)
                                            i,                // Continue nfrom where we were
                                            title,
                                            summary,
                                            urn,
                                            pFilename,
                                            splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption,
                                            icon,
                                            null,              // No firstElements
                                            options);
          result.add(0, nextLink);
          break;
        } else {
          // ... NO - so add book to this page
          Book book = listbooks.get(i);
          if (logger.isTraceEnabled()) logger.trace("getListOfBooks: adding book to the list : " + book);
          try {
            logger.trace("getListOfBooks: breadcrumbs=" + breadcrumbs + ", book=" + book + ", options=" + options);
            Element entry = getBookEntry(breadcrumbs, book, options);
            if (entry != null) {
              if (logger.isTraceEnabled()) logger.trace("getListOfBooks: entry=" + entry);
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

    // create the output files
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);

    Element entry;
    String urlInItsSubfolder = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages) {titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);} else {
        titleNext = Localization.Main.getText("title.lastpage");
      }

      entry = FeedHelper.getNextLink(urlExt, titleNext);
    } else {
      entry = FeedHelper.getCatalogEntry(title, urn, urlInItsSubfolder, summary, icon);
    }

    return entry;
  }


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
  private List<Element> getListOfBooksSplitByLetter(
      Breadcrumbs pBreadcrumbs,
      Map<String,  List<Book>> mapOfBooksByLetter,
      boolean inSubDir,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {
    if (Helper.isNullOrEmpty(mapOfBooksByLetter))
      return null;

    if (pBreadcrumbs.size() > 1) inSubDir = true;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle += ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfBooksByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = Helper.getSplitString(baseFilename, letter, Constants.TYPE_SEPARATOR);
      String letterUrn = Helper.getSplitString(baseUrn, letter, Constants.URN_SEPARATOR);

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
      if (booksInThisLetter.size() > 0) {
        element = getListOfBooks(pBreadcrumbs,
                                 booksInThisLetter,
                                 true,              // Always inSubDir if in letter
                                 0,                 // start at first page
                                 letterTitle,
                                 summary,
                                 letterUrn,
                                 letterFilename,
                                 checkSplitByLetter(letter),
                                 icon,
                                 null,              // No firstElements
                                 options);
      }
      else
      {
        // ITIMPI:  Assert to check if the logic can ever let this be zero!
        assert (booksInThisLetter.size() <= 0) : "booksInThisLetter=" + booksInThisLetter.size() + " for letter '" + letter + "'";
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
  private List<Element> getListOfBooksSplitByDate(
      Breadcrumbs pBreadcrumbs,
      Map<DateRange, List<Book>> mapOfBooksByDate,
      boolean inSubDir,
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

    if (pBreadcrumbs.size() > 1) inSubDir = true;

    List<Element> result = new LinkedList<Element>();
    SortedSet<DateRange> ranges = new TreeSet<DateRange>(mapOfBooksByDate.keySet());
    for (DateRange range : ranges) {
      // generate the range file
      String rangeFilename = baseFilename + Constants.TYPE_SEPARATOR + range;

      String rangeUrn = Helper.getSplitString(baseUrn, range.toString(), Constants.URN_SEPARATOR);

      String rangeTitle = LocalizationHelper.INSTANCE.getEnumConstantHumanName(range);
      List<Book> booksInThisRange = mapOfBooksByDate.get(range);

      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeBooks(booksInThisRange);

      Element element = null;
      if (booksInThisRange.size() > 0) {
        element = getListOfBooks(pBreadcrumbs,
                                 booksInThisRange,
                                 true,         // Always inSubDir
                                 0,            // Start at first page
                                 rangeTitle,
                                 summary,
                                 rangeUrn,
                                 rangeFilename,
                                 SplitOption.Paginate,
                                 icon,
                                 null,
                                 options);
      }

      if (element != null)
        result.add(element);
    } // end of for
    return result;
  }

  // ----------------
  //    BOOK ENTRY
  // ----------------

  //  The remainder of the methods are specific to creating an entry for a specific book

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
      if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: exit: download links suppressed");
      return;
    }

    // links to the ebook files
    if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: links to the ebook files");
    for (EBookFile file : book.getFiles()) {
      // prepare to copy the ebook file
      if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: prepare to copy the ebook file " + file.getName());
      // TODO ITIMPI  Why is EPUB treated as a special case?
      if (file.getFormat() == EBookFormat.EPUB)
        catalogManager.addFileToTheMapOfFilesToCopy(file.getFile(), book);
      else
        catalogManager.addFileToTheMapOfFilesToCopy(file.getFile());
      // Allowm for books on specific URL (#c2o-160)
      String prefix = currentProfile.getUrlBooks();
      if (Helper.isNullOrEmpty(prefix)) {
        prefix = Constants.PARENT_PATH_PREFIX + Constants.PARENT_PATH_PREFIX ;
      }
      entry.addContent(FeedHelper.getAcquisitionLink(prefix + FeedHelper.urlEncode(book.getPath(), true)
          + Constants.FOLDER_SEPARATOR + FeedHelper.urlEncode(file.getName() + file.getExtension(), true),
          file.getFormat().getMime(), // Mime type
          Localization.Main.getText("bookentry.download", file.getFormat())));

      // if the IncludeOnlyOneFile option is set, break to avoid publishing other files
      if (currentProfile.getIncludeOnlyOneFile()) {
        if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: break to avoid publishing other files");
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

    if (logger.isDebugEnabled())  logger.debug("addCoverLink for " + book.getTitle());
    CachedFile coverFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), Constants.CALIBRE_COVER_FILENAME);
    if (coverFile.exists()) {
      // add the cover

      // prepare to copy the cover file

      catalogManager.addFileToTheMapOfFilesToCopy(coverFile);

      // Add the Cover link

      String coverUri = coverManager.getImageUri(book);

      // get the generated cover filename
      CachedFile resizedCoverFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), coverManager.getResultFilename(book));

      // prepare to copy the thumbnail if we are using them file
      if (currentProfile.getCoverResize()) {
        catalogManager.addFileToTheMapOfFilesToCopy(resizedCoverFile);

        if (!resizedCoverFile.exists() || coverManager.hasImageSizeChanged() || resizedCoverFile.lastModified() < coverFile.lastModified()) {
          if (logger.isTraceEnabled()) {
            if (!resizedCoverFile.exists())
              logger.trace("addCoverLink: resizedCover set to be generated (not already existing)");
            else if (coverManager.hasImageSizeChanged())
              logger.trace("addCoverLink: resizedCover set to be generated (image size changed)");
            else if (resizedCoverFile.lastModified() < coverFile.lastModified())
              logger.trace("addCoverLink: resizedCover set to be generated (new cover)");
          }
          coverManager.setImageToGenerate(resizedCoverFile, coverFile);
        } else {
          if (logger.isTraceEnabled())  logger.trace("addCoverLink: resizedCover not to be generated");
        }
      } else {
        // Not using resized covers - use original cover.jpg

        if (resizedCoverFile.exists()) {
          // Safety check we never delete the Calibre cover
          if (0 == resizedCoverFile.getName().compareTo(Constants.CALIBRE_COVER_FILENAME)) {
            logger.warn("attempt to delete Calibre cover for book " + book.getTitle());
          } else {
            if (logger.isTraceEnabled())  logger.trace("addCoverLink: coverResize=false. Delete " + resizedCoverFile.getName());
          }
          resizedCoverFile.delete();
          // Make sure it is no longer in the cache
          CachedFileManager.INSTANCE.removeCachedFile(resizedCoverFile);
        } else {
          if (logger.isTraceEnabled())  logger.trace("addCoverLink: coverResize=false. No resizedCover file for book " + book.getTitle());
        }
        // Change URI name to user cover.jpg
        coverUri = FeedHelper.urlEncode(Constants.LIBRARY_PATH_PREFIX + book.getPath() + Constants.FOLDER_SEPARATOR + Constants.CALIBRE_COVER_FILENAME, true);
      }
      if (logger.isTraceEnabled())  logger.trace("addCoverLink: coverUri=" + coverUri);

      entry.addContent(FeedHelper.getCoverLink(coverUri));
    }

    // add the thumbnail link
    String thumbnailUri;
    if (coverFile.exists()) {
      thumbnailUri = thumbnailManager.getImageUri(book);
      CachedFile thumbnailFile = CachedFileManager.INSTANCE.addCachedFile(book.getBookFolder(), thumbnailManager.getResultFilename(book));
      // Take into account whether thumbnail generation suppressed
      if (currentProfile.getThumbnailGenerate()) {
        // Using generated thumbnail files

        // prepare to copy the thumbnail file
        catalogManager.addFileToTheMapOfFilesToCopy(thumbnailFile);

        // generate the file if does not exist or size changed
        if (!thumbnailFile.exists() || thumbnailManager.hasImageSizeChanged() || thumbnailFile.lastModified() < coverFile.lastModified()) {
          if (logger.isTraceEnabled()) {
            if (!thumbnailFile.exists())
              logger.trace("addCoverLink: thumbnail set to be generated (not already existing)");
            else if (thumbnailManager.hasImageSizeChanged())
              logger.trace("addCoverLink: thumbnail set to be generated (image size changed)");
          } else if (thumbnailFile.lastModified() < coverFile.lastModified()) {
            logger.trace("addCoverLink: thumbnail set to be generated (new cover)");
          }
          thumbnailManager.setImageToGenerate(thumbnailFile, coverFile);
        } else {
          if (logger.isTraceEnabled())  logger.trace("addCoverLink: thumbnail not to be generated");
        }
      } else {
        // Not generating thumbnails - using existing cover.jpg
        if (thumbnailFile.exists()) {
          if (thumbnailFile.getName().compareTo("cover.jpg") == 0) {
            logger.warn("attempt to delete Calibre cover (for book " + book.getTitle());
          } else {
            if (logger.isTraceEnabled())  logger.trace("addCoverLink: Delete existing thumbnail file " + thumbnailFile.getName());
            thumbnailFile.delete();
            // Make sure it is no longer in the cache
            CachedFileManager.INSTANCE.removeCachedFile(thumbnailFile);
          }
        }
        CachedFileManager.INSTANCE.removeCachedFile(thumbnailFile);
        // Change URI name to user cover.jpg
        thumbnailUri = FeedHelper.urlEncode(Constants.LIBRARY_PATH_PREFIX + book.getPath() + Constants.FOLDER_SEPARATOR + Constants.CALIBRE_COVER_FILENAME, true);
      }
    } else {
      // resize the default thumbnail if needed
      File resizedDefaultThumbnail = new File(catalogManager.getGenerateFolder(), Constants.DEFAULT_RESIZED_THUMBNAIL_FILENAME);
      File defaultThumbnail = new File(catalogManager.getGenerateFolder(), Constants.DEFAULT_THUMBNAIL_FILENAME);
      if (!resizedDefaultThumbnail.exists() || thumbnailManager.hasImageSizeChanged() || resizedDefaultThumbnail.lastModified() < defaultThumbnail.lastModified()) {
        thumbnailManager.setImageToGenerate(resizedDefaultThumbnail, defaultThumbnail);
      }

      // Change URI name to user default thumbnail
      thumbnailUri = FeedHelper.urlEncode(Constants.PARENT_PATH_PREFIX + Constants.DEFAULT_RESIZED_THUMBNAIL_FILENAME, true);
    }

    if (logger.isTraceEnabled())  logger.trace("addCoverLink: thumbNailUri=" + thumbnailUri);

    thumbnailManager.addBook(book, thumbnailUri);
    entry.addContent(FeedHelper.getThumbnailLink(thumbnailUri));
  }

  /**
   * Add book cross reference links
   *
   * Used when constructing book details entries
   *
   * NOTE:  At the moment we do not constrict these to the current level
   *        We might want to revisit this assumption?
   *
   * @param entry
   * @param book
   */
  private void addNavigationLinks(Element entry, Book book) {
    String filename;
    if (currentProfile.getGenerateCrossLinks()) {
      // add the series link
      // (but only if we generate a series catalog)
      if (currentProfile.getGenerateSeries()) {
        if (book.getSeries() != null && DataModel.INSTANCE.getMapOfBooksBySeries().get(book.getSeries()).size() > 1) {
          if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the series link");
          // Series are always held at top level
          filename = SeriesSubCatalog.getSeriesFolderFilename(book.getSeries()) + Constants.PAGE_ONE_XML;
          entry.addContent(FeedHelper.getRelatedLink(catalogManager.getCatalogFileUrl(filename, true),
              Localization.Main.getText("bookentry.series", book.getSerieIndex(), book.getSeries().getName())));
        }
      }

      String booksText = Localization.Main.getText("bookword.title");
      // add the author page link(s)
      // (but only if we generate an authors catalog)
      if (currentProfile.getGenerateSeries()) {
        if (book.hasAuthor()) {
          if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the author page link(s)");
          for (Author author : book.getAuthors()) {
            // c2o-168 - Omit Counts if MinimizeChangedFiles set
            if (! currentProfile.getMinimizeChangedFiles()) {
              booksText = Summarizer.INSTANCE.getBookWord(DataModel.INSTANCE.getMapOfBooksByAuthor().get(author).size());
            }
            // Authors are always held at top level !
            filename = AuthorsSubCatalog.getAuthorFolderFilename(author) + Constants.PAGE_ONE_XML;
            entry.addContent(FeedHelper.getRelatedLink(catalogManager.getCatalogFileUrl(filename, true),
                Localization.Main.getText("bookentry.author", booksText, author.getName())));
          }
        }
      }

      // add the tags links
      // (but only if we generate a tags catalog)
      if (currentProfile.getGenerateTags() && currentProfile.getIncludeTagCrossReferences()) {
        if (Helper.isNotNullOrEmpty(book.getTags())) {
          if (logger.isTraceEnabled()) logger.trace("addNavigationLinks: add the tags links");
          for (Tag tag : book.getTags()) {
            int nbBooks = DataModel.INSTANCE.getMapOfBooksByTag().get(tag).size();
            // Tags are held at level
            filename = getCatalogBaseFolderFileNameId(Constants.TAG_TYPE, tag.getId()) + Constants.PAGE_ONE_XML;
            if (nbBooks > 1) {
              // c2o-168 - Omit Counts if MinimizeChangedFiles set
              if (! currentProfile.getMinimizeChangedFiles()) {
                booksText = Summarizer.INSTANCE.getBookWord(nbBooks);
              }
              entry.addContent(FeedHelper.getRelatedLink(catalogManager.getCatalogFileUrl(filename, true),
                  Localization.Main.getText("bookentry.tags", booksText, tag.getName())));
            }
          }
        }
      }

      // add the ratings links
      if (currentProfile.getGenerateRatings() && book.getRating() != BookRating.NOTRATED) {
        if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the ratings links");
        int nbBooks = DataModel.INSTANCE.getMapOfBooksByRating().get(book.getRating()).size();
        if (nbBooks > 1) {
          // c2o-168 - Omit Counts if MinimizeChangedFiles set
          if (! currentProfile.getMinimizeChangedFiles()) {
            booksText = Summarizer.INSTANCE.getBookWord(nbBooks);
          }
          // Ratings are held at level
          filename = getCatalogBaseFolderFileNameId(Constants.RATED_TYPE, book.getRating().getId().toString()) + Constants.PAGE_ONE_XML;
          entry.addContent(FeedHelper.getRelatedLink(catalogManager.getCatalogFileUrl(filename, true),
              Localization.Main.getText("bookentry.ratings", booksText, LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating()))));
        }
      }
    }
  }

  private String getLocalizedUrl(Book book, String configUrl, String localizeUrl, String... args) {
    String guiLanguage = currentProfile.getLanguage();
    Language bookLanguage =  book.getBookLanguage();

    String languageCode = bookLanguage.getIso2();
    if (Helper.isNullOrEmpty(languageCode)){
      languageCode = currentProfile.getLanguage();
    }

    String url = "";
    return Localization.Main.getText(url, (java.lang.Object[])args);
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
      if (logger.isTraceEnabled())  logger.trace("addExternalLinks: ADDING external links to book " + book);
      String url;
      // add the GoodReads book link
      if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the GoodReads book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getGoodreadIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.goodreads")
          ));

        url = currentProfile.getGoodreadReviewIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(
              FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.goodreads.review")));
      } else {
        url = currentProfile.getGoodreadTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper
              .getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.urlEncode(book.getTitle())), Localization.Main.getText("bookentry.goodreads")
              ));
      }

      // add the Wikipedia book link
      if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the Wikipedia book link");
      url = currentProfile.getWikipediaUrl();
      if (Helper.isNotNullOrEmpty(url))
        entry.addContent(FeedHelper.getRelatedHtmlLink(
            MessageFormat.format(url, currentProfile.getWikipediaLanguage(), FeedHelper.urlEncode(book.getTitle()
            )),
            Localization.Main.getText("bookentry.wikipedia")));

      // Add Librarything book link
      if (logger.isTraceEnabled())  logger.trace("addExternalLinks: Add Librarything book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getLibrarythingIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(
              FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.librarything")));
      } else if (Helper.isNotNullOrEmpty(book.getTitle())) {
        url = currentProfile.getLibrarythingTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.getRelatedHtmlLink(
              MessageFormat.format(url, FeedHelper.urlEncode(book.getTitle()), FeedHelper.urlEncode(book.getMainAuthor().getName())),
              Localization.Main.getText("bookentry.librarything")));
      }

      // Add Amazon book link
      if (logger.isTraceEnabled())  logger.trace("addExternalLinks: Add Amazon book link");
      if (Helper.isNotNullOrEmpty(book.getIsbn())) {
        url = currentProfile.getAmazonIsbnUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, book.getIsbn()), Localization.Main.getText("bookentry.amazon")));
      } else if (book.getMainAuthor() != null && Helper.isNotNullOrEmpty(book.getTitle())) {
        url = currentProfile.getAmazonTitleUrl();
        if (Helper.isNotNullOrEmpty(url))
          entry.addContent(FeedHelper.getRelatedHtmlLink(
              MessageFormat.format(url, FeedHelper.urlEncode(book.getTitle()), FeedHelper.urlEncode(book.getMainAuthor().getName())),
              Localization.Main.getText("bookentry.amazon")));
      }

      // Author Links
      if (book.hasAuthor()) {
        // add the GoodReads author link
        if (logger.isTraceEnabled())  logger.trace("addExternalLinksy: add the GoodReads author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getGoodreadAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.goodreads.author", author.getName())));
        }

        // add the Wikipedia author link
        if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the Wikipedia author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getWikipediaUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(currentProfile.getWikipediaUrl(),
                currentProfile.getWikipediaLanguage(), FeedHelper.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.wikipedia.author", author.getName())));
        }

        // add the LibraryThing author link
        if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the LibraryThing author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getLibrarythingAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.getRelatedHtmlLink(
                // LibraryThing is very peculiar on how it looks up it's authors... format is LastNameFirstName[Middle]
                MessageFormat.format(currentProfile.getLibrarythingAuthorUrl(),
                    FeedHelper.urlEncode(author.getSort().replace(",", "").replace(" ", ""))),
                Localization.Main.getText("bookentry.librarything.author", author.getName())));
        }

        // add the Amazon author link
        if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the Amazon author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getAmazonAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.amazon.author", author.getName())));
        }

        // add the ISFDB author link
        if (logger.isTraceEnabled())  logger.trace("addExternalLinks: add the ISFDB author link");
        for (Author author : book.getAuthors()) {
          url = currentProfile.getIsfdbAuthorUrl();
          if (Helper.isNotNullOrEmpty(url))
            entry.addContent(FeedHelper.getRelatedHtmlLink(MessageFormat.format(url, FeedHelper.urlEncode(author.getName())),
                Localization.Main.getText("bookentry.isfdb.author", author.getName())));
        }
      }
    }
  }

  /**
   * Generate a book entry in a catalog
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
    if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: ADDING book decoration to book " + book);
    if (book.hasAuthor()) {
      for (Author author : book.getAuthors()) {
        if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   author " + author);
        Element authorElement = JDOM.INSTANCE.element("author")
            .addContent(JDOM.INSTANCE.element("name").addContent(author.getName()))
            .addContent(JDOM.INSTANCE.element("uri")
                .addContent(Constants.PARENT_PATH_PREFIX + AuthorsSubCatalog.getAuthorFolderFilename(author) + Constants.PAGE_ONE_XML));
        entry.addContent(authorElement);
      }
    }

    // published element
    if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   published " + book.getPublicationDate());
    Element published = FeedHelper.getPublishedTag(book.getPublicationDate());
    entry.addContent(published);

    // dublin core - language
    for (Language language : book.getBookLanguages()) {
      if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   language " + language.getIso2());
      Element dcLang = FeedHelper.getDublinCoreLanguageElement(language.getIso2());
      entry.addContent(dcLang);
    }

    // dublin core - publisher
    Publisher publisher = book.getPublisher();
    if (Helper.isNotNullOrEmpty(publisher)) {
      if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   publisher " + publisher.getName());
      Element dcPublisher = FeedHelper.getDublinCorePublisherElement(publisher.getName());
      entry.addContent(dcPublisher);
    }

    // categories
    if (Helper.isNotNullOrEmpty(book.getTags())) {
      // tags
      for (Tag tag : book.getTags()) {
        if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   tag " + tag.getName());
        Element categoryElement = FeedHelper.getCategoryElement(tag.getName());
        entry.addContent(categoryElement);
      }
    }
    // series
    if (currentProfile.getIncludeSeriesInBookDetails() && Helper.isNotNullOrEmpty(book.getSeries())) {
      if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   series " + book.getSeries().getName() + "[" + book.getSerieIndex() + "]");
      Element categoryElement = FeedHelper.getCategoryElement(book.getSeries().getName());
      entry.addContent(categoryElement);
    }

    // book description
    if (isFullEntry) {
      if (logger.isTraceEnabled()) logger.trace("decorateBookEntry: FULL ENTRY");
      // content element
      if (logger.isTraceEnabled())  logger.trace("decorateBookEntry:   content element");
      Element content = JDOM.INSTANCE.element("content").setAttribute("type", "text/html");
      boolean hasContent = false;
      if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: computing comments");
      // Series (if present and wanted)
      if (currentProfile.getIncludeSeriesInBookDetails() && Helper.isNotNullOrEmpty(book.getSeries())) {
        String data = Localization.Main.getText("content.series.data", book.getSerieIndex(), book.getSeries().getName());
        content.addContent(JDOM.INSTANCE.element("strong")
            .addContent(Localization.Main.getText("content.series") + ": "))
            .addContent(data)
            .addContent(JDOM.INSTANCE.element("br"))
            .addContent(JDOM.INSTANCE.element("br"));
        hasContent = true;
      }
      // Rating (if present and wanted)
      // If the user has requested tags we output this section even if the list is empty.
      // The assumption is that the user in this case wants to see that no tags have been assigned
      // If we get feedback that this is not  a valid addumption then we could omit it when the list is empty
      if (currentProfile.getIncludeRatingInBookDetails()) {
        if (Helper.isNotNullOrEmpty(book.getRating())) {
          String rating = LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating());
          content.addContent(JDOM.INSTANCE.element("strong")
              .addContent(Localization.Main.getText("content.rating") + ": "))
              .addContent(rating)
              .addContent(JDOM.INSTANCE.element("br"))
              .addContent(JDOM.INSTANCE.element("br"));
          hasContent = true;
        }
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
              .addContent(Localization.Main.getText("content.tags") + ": "))
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
              .addContent(Localization.Main.getText("content.publisher") + ": "))
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
            content.addContent(JDOM.INSTANCE.element("strong")
                .addContent(Localization.Main.getText("content.published") + ": "))
                .addContent(PUBLICATIONDATE_FORMAT.format(book.getPublicationDate()))
                .addContent(JDOM.INSTANCE.element("br"))
                .addContent(JDOM.INSTANCE.element("br"));
        }
      }

      // Added date (if present and wanted)
      if (currentProfile.getIncludeAddedInBookDetails()) {
        Date addtmp = book.getTimestamp();
        if (Helper.isNotNullOrEmpty(addtmp)) {
          content.addContent(JDOM.INSTANCE.element("strong")
              .addContent(Localization.Main.getText("content.added") + ": "))
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
              .addContent(Localization.Main.getText("content.modified") + ": "))
              .addContent(DATE_FORMAT.format(modtmp))
              .addContent(JDOM.INSTANCE.element("br"))
              .addContent(JDOM.INSTANCE.element("br"));
        }
      }

      // See if any Custom Column values to be included

      List<CustomColumnType>bookDetailsCustomColumnTypes = CatalogContext.INSTANCE.catalogManager.getBookDetailsCustomColumns();
      if (bookDetailsCustomColumnTypes != null && bookDetailsCustomColumnTypes.size() > 0) {
        List<CustomColumnValue> values = DataModel.INSTANCE.getMapOfCustomColumnValuesByBookId().get(book.getId().toString());
        if (values != null && values.size()> 0) {
          for (CustomColumnValue value : values) {
            // We only do values the user has asked for
            if (bookDetailsCustomColumnTypes.contains(value.getType())) {
              String textValue = value.getValue();
              if (value.getType().getDatatype().equals("bool")) {
                textValue = textValue.equals("0") ? Localization.Main.getText("boolean.no")
                                              : Localization.Main.getText("boolean.yes");
              }
              int posStart = textValue.startsWith("<div>") ? 5 : 0;
              int posEnd = textValue.endsWith("</div>") ? textValue.length() - 6 : textValue.length();
              int posPara = textValue.indexOf("<p>");
              if (posPara != -1 ) {
                textValue = "<span id=\"" + value.getType().getLabel() + "\">" +  textValue.substring(posStart,posPara) + "</span>" + textValue.substring(posPara+4);
              } else {
                textValue = "<span id=\"" + value.getType().getLabel() + "\">" +  textValue.substring(posStart,posEnd) + "</span>";
              }

              List<Element>valuexhtml = JDOM.INSTANCE.convertHtmlTextToXhtml(textValue);
              content.addContent(JDOM.INSTANCE.element("strong")
                  .addContent(value.getType().getName() + ": "));
              for (Element p : valuexhtml) {
                content.addContent(p.detach());
              }
              content.addContent(JDOM.INSTANCE.element("br"))
                  .addContent(JDOM.INSTANCE.element("br"));
            }
          }
        }
      }

      List<Element> comments = JDOM.INSTANCE.convertHtmlTextToXhtml(book.getComment());
      if (Helper.isNotNullOrEmpty(comments)) {
        if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: got comments");
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
        if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: had content");
        entry.addContent(content);
      }
    } else {
      // summary element (the shortened book comment)
      if (logger.isTraceEnabled())  logger.trace("getBookEntry: short comment");
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
   * Get the base filename that is used to store a given book
   *
   * Since we always hold books at the top level the name can be
   * derived purely knowing the book involved.
   *
   * @param book
   * @return
   */
  public static String getBoookFolderFilename (Book book) {
    return getCatalogBaseFolderFileNameIdNoLevelSplit(Constants.BOOK_TYPE,book.getId());
  }
  /**
   * Control generating a book Full Details entry
   *
   * The partial details are always generated as these are
   * required by the catalog entry that points to the book.
   *
   * The full details are only generated if it does not appear
   * that we have done these previosuly.
   *
   * @param pBreadcrumbs
   * @param book
   * @param options
   * @return
   * @throws java.io.IOException
   */
  public Element getBookEntry(Breadcrumbs pBreadcrumbs,
      Book book,
      Option... options) throws IOException {

    if (logger.isDebugEnabled())  logger.debug("getBookEntry: pBreadcrumbs=" + pBreadcrumbs + ", book=" + book);
    // Book files are always a top level (we might revisit this assumption one day)
    String filename = getBoookFolderFilename(book);
    String fullEntryUrl = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, true);
    File outputFile = catalogManager.storeCatalogFile(filename + Constants.XML_EXTENSION);

    if (!isInDeepLevel() && isBookTheStepUnit() && !getCatalogFolder().equals(Constants.AUTHOR_TYPE))
      CatalogContext.INSTANCE.callback.incStepProgressIndicatorPosition();

    if (logger.isDebugEnabled())  logger.debug("getBookEntry:" + book);
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: pBreadcrumbs " + pBreadcrumbs.toString());
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: generating " + filename);

    // construct the contextual title (including the date, or the series, or the rating)
    String title;
    if (Option.contains(options, Option.INCLUDE_SERIE_NUMBER)) {
      if (book.getSerieIndex() != 0) {
        title = book.getTitleWithSerieNumber();
      } else {
        title = book.getTitle();
      }
    } else if (Option.contains(options, Option.INCLUDE_TIMESTAMP)) {
      title = book.getTitle() + " [" + DATE_FORMAT.format(book.getTimestamp()) + "]";
    }else if (!Option.contains(options, Option.DONOTINCLUDE_RATING) && !currentProfile.getSuppressRatingsInTitles()) {
      title = book.getTitleWithRating(Localization.Main.getText("bookentry.rated"), LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating()));
    } else {
      title = book.getTitle();
    }
    String urn = "calibre:book:" + book.getId();

    if (logger.isTraceEnabled()) logger.trace("getBookEntry: checking book in the Catalog manager");

    // We only need to actually generate the file if not done previously
    if (! catalogManager.addBookEntryFile(outputFile)) {
      if (logger.isDebugEnabled())  logger.debug("getBookEntry: SKIPPING generation of full book entry as already done");
    } else {
      if (logger.isTraceEnabled()) logger.trace("getBookEntry: book full entry (not yet done)");
      // generate the book full entry
      // generateBookFullEntryFile(pBreadcrumbs, book, filename, fullEntryUrl);

      // if the "all books" catalog never was generated, we'll end up with the first generated catalog's breadcrumbs ; that ain't good, I prefer linking only to main
      Breadcrumbs breadcrumbs = pBreadcrumbs;
      if (!currentProfile.getGenerateAllbooks()) {
        // remove all but the first (main) entry
        breadcrumbs = new Breadcrumbs();
        breadcrumbs.add(pBreadcrumbs.get(0));
      }
      Element entry = JDOM.INSTANCE.rootElement("entry", JDOM.Namespace.Atom, JDOM.Namespace.DcTerms, JDOM.Namespace.Atom, JDOM.Namespace.Xhtml);
      entry.addContent (JDOM.INSTANCE.element("title").addContent(book.getTitle()));
      entry.addContent(JDOM.INSTANCE.element("id").addContent("urn:book:" + book.getUuid()));
      entry.addContent(FeedHelper.getUpdatedTag(book.getLatestFileModifiedDate()));
      // add the navigation links
      FeedHelper.decorateElementWithNavigationLinks(entry, breadcrumbs, book.getTitle(), fullEntryUrl, true, true);
      // add the required data to the book entry
      decorateBookEntry(entry, book, true);
      // write the element to the files
      createFilesFromElement(entry, filename, HtmlManager.FeedType.BookFullEntry);

      if (currentProfile.getGenerateIndex()) {
        logger.debug("getBookEntry: indexing book");
        // index the book
        IndexManager.INSTANCE.indexBook(book, htmlManager.getHtmlFilename(fullEntryUrl), thumbnailManager.getThumbnailUrl(book));
      }
    }

    Element entry = FeedHelper.getBookEntry(title, urn, book.getLatestFileModifiedDate());

    // add the required data to the book entry
    decorateBookEntry(entry, book, false);

    // add a full entry link to the partial entry
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: add a full entry link to the partial entry");
    entry.addContent(FeedHelper.getFullEntryLink(fullEntryUrl));

    return entry;
  }
}
