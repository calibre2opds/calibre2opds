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
import com.gmail.dpierron.calibre.configuration.DeviceMode;
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
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;

public abstract class BooksSubCatalog extends SubCatalog {
  private final static Logger logger = Logger.getLogger(BooksSubCatalog.class);
  protected final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());

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

  void sortBooksByAuthorAndTitle(List<Book> books) {
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        String s1 = o1.getAuthorSort();
        String s2 = o2.getAuthorSort();
        if (! s1.equals(s2)) {
           return Helper.checkedCollatorCompareIgnoreCase(s1, s2, collator);
        }
        // If authors equal compare on title.
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
    String urlExt = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);

    Element feed;
    feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /*inSubDir */);
    // Update breadcrumbs ready for next iteration
    Breadcrumbs breadcrumbs;
    // #c2o-204 breadrumbs should already be correct if listing firt page of books for an author.
    if (from ==0 && getCatalogFolder().startsWith(Constants.AUTHOR_TYPE)) {
      breadcrumbs = pBreadcrumbs;
    } else {
      breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
    }

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
      CatalogManager.INSTANCE.callback.showMessage(progressText.toString());
      for (int i = from; i < listbooks.size(); i++) {
        // check if we must continue
        CatalogManager.INSTANCE.callback.checkIfContinueGenerating();

        // See if we need to do the next page
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          // TODO #c2o-208   Add Previous, First and Last links if needed
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

    Element entry;
    String urlInItsSubfolder = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);

    entry = createPaginateLinks(feed, filename, pageNumber, maxPages);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
    if (from == 0) {
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
   * Add the aquisition links
   *
   * These are used to specify where a book can be downloaded from.
   * They will not be needed if generation of download links is suppressed.
   *
   * @param book
   * @param entry
   */
  private void addAcquisitionLinks(Book book, Element entry) {
    if (!currentProfile.getGenerateOpdsDownloads()) {
      if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: exit: download links suppressed");
      return;
    }

    // links to the ebook files
    if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: links to the ebook files");
    for (EBookFile file : book.getFiles()) {
      // prepare to copy the ebook file
      if (logger.isTraceEnabled())  logger.trace("addAcquisitionLinks: prepare to copy the ebook file " + file.getName());
      // TODO ITIMPI  Why is EPUB treated as a special case?
      CatalogManager.INSTANCE.addFileToTheMapOfLibraryFilesToCopy(file.getFile(), (file.getFormat() == EBookFormat.EPUB) ? book : null);
      // Allow for books on specific URL (#c2o-160)
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
   * Add an image link (cover or thumbnail)
   *
   * If necessary we will generate a resized image at this point.
   *
   * If we are including the covers in the catalog we want to
   * embed the image as base64 data to reduce the number of files.
   *
   * @param book
   * @param entry
   * @param iManager
   * @param isresized
   * @param isCover
   */
  private void addImageLink (Book book, Element entry, ImageManager iManager, boolean isresized, boolean isCover) {

    String imageUri = null;         // The URI to the image at runtime
    File bookFolder = book.getBookFolder();

    CachedFile calibreCoverFile = CachedFileManager.INSTANCE.addCachedFile(bookFolder, Constants.CALIBRE_COVER_FILENAME);

    String catalogImageFilename;  // Name when stored in catalog
    CachedFile imageFile;         // The file that contains the image to be used
    CachedFile resizedImageFile;  // File to be used when resized images in use

    resizedImageFile = CachedFileManager.INSTANCE.addCachedFile(bookFolder, iManager.getResizedFilename());
    if (calibreCoverFile.exists()) {
      // Handle migration to new name standard (#c2o_???)
      FeedHelper.checkFileNameIsNewStandard(resizedImageFile,
          new File(bookFolder, iManager.getResizedFilenameOld(book)));

      if (isresized) {
        imageUri =
            // #c2o_223  Need to use image from Books URI if it is specified
                (Helper.isNullOrEmpty(booksURI) ? FeedHelper.urlEncode(Constants.LIBRARY_PATH_PREFIX)
                                                : booksURI)
                + FeedHelper.urlEncode(book.getPath()
                                      + Constants.FOLDER_SEPARATOR
                                      + iManager.getResizedFilename(), true);
        imageFile = resizedImageFile;
        catalogImageFilename = getBookFolderFilename(book)
            + Constants.TYPE_SEPARATOR
            + iManager.getResizedFilename();
        // Decide whether we need to generate the resized cover image
        if (!resizedImageFile.exists() || iManager.hasImageSizeChanged() || resizedImageFile.lastModified() < calibreCoverFile.lastModified()) {
          if (logger.isTraceEnabled()) {
            if (!resizedImageFile.exists()) {
              logger.trace("addImageLink: resizedImage set to be generated (not already existing)");
            } else if (CatalogManager.INSTANCE.coverManager.hasImageSizeChanged()) {
              logger.trace("addImageLink: resizedImage set to be generated (image size changed)");
            } else if (resizedImageFile.lastModified() < calibreCoverFile.lastModified()) {
              logger.trace("addImageLink: resizedImage set to be generated (new cover)");
            } }
          iManager.generateImage(resizedImageFile, calibreCoverFile);
        } else {
          if (logger.isTraceEnabled())  logger.trace("addImageLink: resizedCover exissts - not to be regenerated");

        }
      } else {
        // Not using resized covers - use original cover.jpg
        imageUri =
            // #c2o_223  Need to use image from Books URI if it is specified
            (Helper.isNullOrEmpty(booksURI) ? FeedHelper.urlEncode(Constants.LIBRARY_PATH_PREFIX)
                                            : booksURI)
            + FeedHelper.urlEncode(book.getPath()
                                  + Constants.FOLDER_SEPARATOR
                                  + Constants.CALIBRE_COVER_FILENAME, true);
        imageFile = calibreCoverFile;
        catalogImageFilename = getBookFolderFilename(book)
            + Constants.TYPE_SEPARATOR
            + Constants.CALIBRE_COVER_FILENAME;
        if (logger.isTraceEnabled())  logger.trace("addImageLink: imageResize=false. Use Calibre cover.jpg file for book " + book.getTitle());
        // Decide if we need to remove previously used resized cover images!
        if (resizedImageFile.exists()) {
          if (resizedImageFile.getName().compareTo("cover.jpg") == 0) {
            logger.warn("addImageLink: attempt to delete Calibre cover (for book " + book.getTitle());
          } else {
            if (logger.isTraceEnabled())  logger.trace("addImageLink: Delete existing image file " + imageFile.getName());
            resizedImageFile.delete();
            // Make sure it is no longer in the cache
            CachedFileManager.INSTANCE.removeCachedFile(resizedImageFile);
          }
        }
      }

    } else {
      // No cover from Calibre so use our default!
      // resize the default thumbnail if needed
      CachedFile resizedDefaultCover = CachedFileManager.INSTANCE.addCachedFile(new File(CatalogManager.INSTANCE.getGenerateFolder(), iManager.getDefaultResizedFilename()));
      CachedFile defaultCover = CachedFileManager.INSTANCE.addCachedFile(new File(CatalogManager.INSTANCE.getGenerateFolder(), Constants.DEFAULT_IMAGE_FILENAME));
      // Separate out resized cover case
      if (!resizedDefaultCover.exists() || iManager.hasImageSizeChanged() || resizedDefaultCover.lastModified() < defaultCover.lastModified()) {
        iManager.generateImage(resizedDefaultCover, defaultCover);
      }
      imageUri = FeedHelper.urlEncode(Constants.PARENT_PATH_PREFIX
                                  + iManager.getDefaultResizedFilename(), true);
      imageFile = resizedDefaultCover;
      catalogImageFilename = Constants.PARENT_PATH_PREFIX
          + iManager.getDefaultResizedFilename();
      CatalogManager.INSTANCE.addFileToTheMapOfFilesToCopy(imageFile);
      if (isCover) logger.warn("addImageLink: No cover.jpg file in Calibre library for book " + book);
    }

    // If we are generating a catalog for a Nook we cache the results for use later
    if (iManager.equals(CatalogManager.INSTANCE.thumbnailManager) && currentProfile.getGenerateIndex()) {
      CatalogManager.INSTANCE.thumbnailManager.addBook(book, imageUri);
    }
    // TODO   Decide here whether to embed image or not rather than where it is done at the moment.
    // TODO   We would alsom prefer to store the external URL format rather than the encoded data as
    // TODO   this would reduce over-all RAM consumption significantly on large libraries.

    // Are we storing cover images in the catalog?

    if (includeCoversInCatalog) {
      if (calibreCoverFile.exists()) {
        if (! useExternalImages && ! catalogImageFilename.equals(Constants.PARENT_PATH_PREFIX + iManager.getDefaultResizedFilename())) {
          imageUri = iManager.getFileToBase64Uri(imageFile);
        } else {
          CatalogManager.INSTANCE.addImageFileToTheMapOfCatalogImages(catalogImageFilename,imageFile);
          if (isCover) {
            imageUri=catalogImageFilename.substring(catalogImageFilename.indexOf(Constants.FOLDER_SEPARATOR)+1);
          } else {
            imageUri=Constants.PARENT_PATH_PREFIX + catalogImageFilename;
          }
        }
      }
    } else {
      // Copy images to target if not in default mode (and not images in catalog)
      if (useExternalImages) {
        if (! currentProfile.getDeviceMode().equals(DeviceMode.Default)) {
          CatalogManager.INSTANCE.addFileToTheMapOfFilesToCopy(imageFile);
        }
      }
    }
    entry.addContent(FeedHelper.getImageLink(imageUri,isCover));
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
    if ( ! currentProfile.getGenerateCrossLinks()) {
      return;
    }
    // add the series link
    // (but only if we generate a series catalog)
    if (isSeriesCrossreferences(book)) {
      Series serie = book.getSeries();
      if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the series link");
      // Series for cross-references are always held at top level
      // TODO Perhaps consider whether level should be taken into account?
      filename = SeriesSubCatalog.getSeriesFolderFilenameNoLevel(serie) + Constants.PAGE_ONE_XML;
      entry.addContent(FeedHelper.getRelatedLink(CatalogManager.INSTANCE.getCatalogFileUrl(filename, true),
          Localization.Main.getText("bookentry.series", book.getSerieIndex(), serie.getName())));
      serie.setReferenced();
    }

    String booksText = Localization.Main.getText("bookword.title");
    // add the author page link(s)
    // (but only if we generate an authors catalog)
    if (isAuthorCrossReferences(book)) {
      if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the author page link(s)");
      for (Author author : book.getAuthors()) {
        String authorName = author.getName();
        // Check for author names that do not get internal links.
        if (authorName.toUpperCase().equals("UNKNOWN")
        || authorName.toUpperCase().equals("VARIOUS")) {
          continue;
        }
        // c2o-168 - Omit Counts if MinimizeChangedFiles set
        if (! currentProfile.getMinimizeChangedFiles()) {
          booksText = Summarizer.INSTANCE.getBookWord(DataModel.INSTANCE.getMapOfBooksByAuthor().get(author).size());
        }
        // Authors for cross-references are always held at top level !
        // TODO Perhaps consider whether level should be taken into account?
        filename = AuthorsSubCatalog.getAuthorFolderFilenameNoLevel(author) + Constants.PAGE_ONE_XML;
        entry.addContent(FeedHelper.getRelatedLink(CatalogManager.INSTANCE.getCatalogFileUrl(filename, true),
            Localization.Main.getText("bookentry.author", booksText, authorName)));
        author.setReferenced();
      }
    }

    // add the tags links
    // (but only if we generate a tags catalog)
    if (isTagCrossReferences(book)) {
      if (logger.isTraceEnabled()) logger.trace("addNavigationLinks: add the tags links");
      for (final Tag tag : book.getTags()) {
        if (! CatalogManager.INSTANCE.getTagsToIgnore().contains(tag)) {           // #c2o_192
          int nbBooks = DataModel.INSTANCE.getMapOfBooksByTag().get(tag).size();
          // Tags for cross-references are held at top level
          // TODO Perhaps consider whether level should be taken into account?
          filename = TagsSubCatalog.getTagFolderFilenameNoLevel(tag) + Constants.PAGE_ONE_XML;
          if (nbBooks > 1) {
            // c2o-168 - Omit Counts if MinimizeChangedFiles set
            if (! currentProfile.getMinimizeChangedFiles()) {
              booksText = Summarizer.INSTANCE.getBookWord(nbBooks);
            }
            entry.addContent(FeedHelper.getRelatedLink(CatalogManager.INSTANCE.getCatalogFileUrl(filename, true),
                Localization.Main.getText("bookentry.tags", booksText, tag.getName())));
            tag.setReferenced();
          }
        }
      }
    }

    // add the ratings links
    if (isRatingCrossReferences(book)) {
      if (logger.isTraceEnabled())  logger.trace("addNavigationLinks: add the ratings links");
      int nbBooks = DataModel.INSTANCE.getMapOfBooksByRating().get(book.getRating()).size();
      if (nbBooks > 1) {
        BookRating rating = book.getRating();
        // c2o-168 - Omit Counts if MinimizeChangedFiles set
        if (! currentProfile.getMinimizeChangedFiles()) {
          booksText = Summarizer.INSTANCE.getBookWord(nbBooks);
        }
        // Ratings are held at level
        filename = getCatalogBaseFolderFileNameId(Constants.RATED_TYPE, rating.getId().toString()) + Constants.PAGE_ONE_XML;
        entry.addContent(FeedHelper.getRelatedLink(CatalogManager.INSTANCE.getCatalogFileUrl(filename, true),
            Localization.Main.getText("bookentry.ratings", booksText, LocalizationHelper.INSTANCE.getEnumConstantHumanName(rating))));
        rating.setReferenced();
      }
    }
  }

  /**
   * TODO  decide if this function is even necessary!
   *
   * @param book
   * @param configUrl
   * @param localizeUrl
   * @param args
   * @return
   */
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
   * TODO Allow both titles and URL's to be customisable
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
      if (Helper.isNotNullOrEmpty(url)) {
        entry.addContent(FeedHelper.getRelatedHtmlLink(
            MessageFormat.format(url, currentProfile.getWikipediaLanguage(), FeedHelper.urlEncode(book.getTitle()
            )),
            Localization.Main.getText("bookentry.wikipedia")));
      }
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
   * Get the filename that should be used for the image file
   * @param book
   * @param type
   * @return
   */
/*
  private String getImageFilename (Book book, String type) {
    String filename = getBookFolderFilename(book);
    return filename;
  }
 */
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

    // cover and thumbnail links
    if (isFullEntry) {
      // We onfly need a cover image for full entries
      if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: ADDING cover link");
      addImageLink(book,entry,currentProfile.getUseThumbnailsAsCovers()
                    ? CatalogManager.INSTANCE.thumbnailManager
                    : CatalogManager.INSTANCE.coverManager,currentProfile.getCoverResize(),true);
    }
    // We want a thumbnail for both full and partial entries.
    if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: ADDING thumbnail link");
    addImageLink(book,entry,CatalogManager.INSTANCE.thumbnailManager,currentProfile.getThumbnailGenerate(),false);


    // acquisition links
    addAcquisitionLinks(book, entry);

    if (book.hasAuthor()) {
      for (Author author : book.getAuthors()) {
        if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   author " + author);
        // #c2o-190
        String name = currentProfile.getDisplayAuthorSort() ? author.getSort() : author.getName();
        Element authorElement = JDOM.INSTANCE.element(Constants.OPDS_ELEMENT_AUTHOR)
            .addContent(JDOM.INSTANCE.element(Constants.OPDS_ELEMENT_NAME).addContent(name))
            .addContent(JDOM.INSTANCE.element(Constants.OPDS_ELEMENT_URI)
                .addContent(Constants.PARENT_PATH_PREFIX + AuthorsSubCatalog.getAuthorFolderFilenameNoLevel(author) + Constants.PAGE_ONE_XML));
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
      String seriesName = book.getSeries().getName();
      if (logger.isTraceEnabled()) logger.trace("decorateBookEntry:   series " + seriesName + "[" + book.getSerieIndex() + "]");
      Element categoryElement = FeedHelper.getCategoryElement(seriesName);
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
        content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
            .addContent(Localization.Main.getText("content.series") + ": "))
            .addContent(data)
            .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
            .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
        hasContent = true;
      }
      // Rating (if present and wanted)
      // If the user has requested tags we output this section even if the list is empty.
      // The assumption is that the user in this case wants to see that no tags have been assigned
      // If we get feedback that this is not  a valid addumption then we could omit it when the list is empty
      if (currentProfile.getIncludeRatingInBookDetails()) {
        if (Helper.isNotNullOrEmpty(book.getRating())) {
          String rating = LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating());
          content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
              .addContent(Localization.Main.getText("content.rating") + ": "))
              .addContent(rating)
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
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
          content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
              .addContent(Localization.Main.getText("content.tags") + ": "))
              .addContent(tags)
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
          hasContent = true;
        }
      }
      // Publisher (if present and wanted)
      if (currentProfile.getIncludePublisherInBookDetails()) {
        if (Helper.isNotNullOrEmpty(book.getPublisher())) {
          content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
              .addContent(Localization.Main.getText("content.publisher") + ": "))
              .addContent(book.getPublisher().getName())
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
          hasContent = true;
        }
      }
      // Published date (if present and wanted)
      if (currentProfile.getIncludePublishedInBookDetails()) {
        Date pubtmp = book.getPublicationDate();
        if (Helper.isNotNullOrEmpty(pubtmp)) {
            content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
                .addContent(Localization.Main.getText("content.published") + ": "))
                .addContent(CatalogManager.INSTANCE.bookDateFormat.format(book.getPublicationDate()))
                .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
                .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
        }
      }

      // Added date (if present and wanted)
      if (currentProfile.getIncludeAddedInBookDetails()) {
        Date addtmp = book.getTimestamp();
        if (Helper.isNotNullOrEmpty(addtmp)) {
          content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
              .addContent(Localization.Main.getText("content.added") + ": "))
              .addContent(CatalogManager.INSTANCE.titleDateFormat.format(addtmp))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
        }
      }


      // Modified date (if present and wanted)
      if (currentProfile.getIncludeModifiedInBookDetails()) {
        Date modtmp = book.getModified();
        if (Helper.isNotNullOrEmpty(modtmp)) {
          content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
              .addContent(Localization.Main.getText("content.modified") + ": "))
              .addContent(CatalogManager.INSTANCE.titleDateFormat.format(modtmp))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
              .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));
        }
      }

      // See if any Custom Column values to be included

      List<CustomColumnType>bookDetailsCustomColumnTypes = CatalogManager.INSTANCE.getBookDetailsCustomColumns();
      if (bookDetailsCustomColumnTypes != null && bookDetailsCustomColumnTypes.size() > 0) {
        List<CustomColumnValue> values = DataModel.INSTANCE.getMapOfCustomColumnValuesByBookId().get(book.getId().toString());
        for (CustomColumnType columnType : bookDetailsCustomColumnTypes) {
          String textValue = "";
          String dataType = columnType.getDatatype();
          String name = columnType.getName();
          String label = columnType.getLabel();
          if (values != null && values.size()> 0) {
            for (CustomColumnValue value : values) {
              if (value.getType().equals(columnType)) {
                textValue = value.getValueAsString();
                break;
              }
            }
          }
          if (currentProfile.getBookDetailsCustomFieldsAlways()
          || Helper.isNotNullOrEmpty(textValue)) {
                // Special processing for bool type
            // convert to localized yes/no text
            if (dataType.equals("bool")) {
              if (Helper.isNotNullOrEmpty(textValue)) {
                textValue = textValue.equals("0") ? Constants.NO : Constants.YES;
              }
            }

            // Special processing for fields that look like links
            // We convert them to a link, and use name as the description.
            if (textValue.toUpperCase().startsWith("HTTP://")
                ||  textValue.toString().startsWith("HTTPS://")) {
              name = "<U><A HREF=\"" + textValue + "\">" + name + "</A></U>";
              textValue = "";
              List<Element>namexhtml = JDOM.INSTANCE.convertHtmlTextToXhtml(name);
              for (Element p : namexhtml) {
                content.addContent(p.detach());
              }
            } else {
              name += ": ";
              content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG).addContent(name));
            }

            // Special processing for fields that contain HTML (e.g. comment)
            // We want to remove the leading <DIV> tag inserted by Calibre
            int posStart = textValue.startsWith("<div>") ? 5 : 0;
            int posEnd = textValue.endsWith("</div>") ? textValue.length() - 6 : textValue.length();
            int posPara = textValue.indexOf("<p>");

            // We want a <DIV> around the custom field inserted by Calibre to be
            // changed to a <SPAN> to avoid unecessary white space being inserted at display time
            if (posPara != -1 ) {
              textValue = "<span id=\"" + label + "\">" +  textValue.substring(posStart,posPara) + "</span>" + textValue.substring(posPara+4);
            } else {
              textValue = "<span id=\"" + label + "\">" +  textValue.substring(posStart,posEnd) + "</span>";
            }

            // Now add the text (if any)
            if (Helper.isNotNullOrEmpty(textValue)) {
              List<Element>valuexhtml = JDOM.INSTANCE.convertHtmlTextToXhtml(textValue);
              for (Element p : valuexhtml) {
                content.addContent(p.detach());
              }
            }
            // Finally some spacing elements
            content.addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK))
                .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_BREAK));

          }
        }
      }

      List<Element> comments = JDOM.INSTANCE.convertHtmlTextToXhtml(book.getComment());
      if (Helper.isNotNullOrEmpty(comments)) {
        if (logger.isTraceEnabled())  logger.trace("decorateBookEntry: got comments");
        content.addContent(JDOM.INSTANCE.newParagraph()
            .addContent(JDOM.INSTANCE.element(Constants.HTML_ELEMENT_STRONG)
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
  public static String getBookFolderFilename(Book book) {
    return getCatalogBaseFolderFileNameIdNoLevelSplit(Constants.BOOK_TYPE,book.getId(),1000);
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
    String filename = getBookFolderFilename(book);
    String fullEntryUrl = CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, true);
    File outputFile = CatalogManager.INSTANCE.storeCatalogFile(filename + Constants.XML_EXTENSION);

    if (!isInDeepLevel() && isBookTheStepUnit() && !getCatalogFolder().startsWith(Constants.AUTHOR_TYPE))
      CatalogManager.INSTANCE.callback.incStepProgressIndicatorPosition();

    if (logger.isDebugEnabled())  logger.debug("getBookEntry:" + book);
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: pBreadcrumbs " + pBreadcrumbs.toString());
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: generating " + filename);

    // construct the contextual title (including the date, or the series, or the rating)
    // #c2o_190
    String title = currentProfile.getDisplayTitleSort() ? book.getTitle_Sort() : book.getTitle();
    if (Option.contains(options, Option.INCLUDE_SERIE_NUMBER)) {
      if (book.getSerieIndex() != 0) {
        DecimalFormat df = new DecimalFormat("####.##");
        title = df.format(book.getSerieIndex()) + " - " + title;

      }
    } else if (Option.contains(options, Option.INCLUDE_TIMESTAMP)) {
      title = title + " [" + CatalogManager.INSTANCE.titleDateFormat.format(book.getTimestamp()) + "]";
    } else if (!Option.contains(options, Option.DONOTINCLUDE_RATING) && !currentProfile.getSuppressRatingsInTitles()) {
      if (book.getRating() != BookRating.NOTRATED) {
        title = MessageFormat.format(Localization.Main.getText("bookentry.rated"), title,  LocalizationHelper.INSTANCE.getEnumConstantHumanName(book.getRating()));
      }
    }
    // #c2o-212
    // Special handling for the listof books within a tag1
    if (currentProfile.getSortTagsByAuthor() && getCatalogType().equals(Constants.TAGLIST_TYPE)) {
      title = (currentProfile.getDisplayAuthorSort() ? book.getAuthorSort() : book.getListOfAuthors()) + " - " + title;
    }
    String urn = "calibre:book:" + book.getId();

    if (logger.isTraceEnabled()) logger.trace("getBookEntry: checking book in the Catalog manager");

    // We only need to actually generate the file if not done previously
    if (book.isDone()) {
      if (logger.isDebugEnabled())  logger.debug("getBookEntry: SKIPPING generation of full book entry as already done");
    } else {
      if (logger.isTraceEnabled()) logger.trace("getBookEntry: book full entry (not yet done)");
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
      FeedHelper.decorateElementWithNavigationLinks(entry, breadcrumbs, book.getTitle(), fullEntryUrl, true);
      // add the required data to the book entry
      decorateBookEntry(entry, book, true);
      // write the element to the files
      createFilesFromElement(entry, filename, HtmlManager.FeedType.BookFullEntry);

      if (currentProfile.getGenerateIndex()) {
        logger.debug("getBookEntry: indexing book");
        // index the book
        // TODO   We need to work out what should be stored for image URI's when
        // TODO   we are embedding images as hexencoded strings in the html files.
        // TODO   We probably want pointers to the actual image files (eith stored
        // TODO   in the catalog or the calibre library) instead.
        IndexManager.INSTANCE.indexBook(book, CatalogManager.INSTANCE.htmlManager.getHtmlFilename(fullEntryUrl), CatalogManager.INSTANCE.thumbnailManager.getThumbnailUrl(book));
      }
      book.setDone();
    }

    Element entry = FeedHelper.getBookEntry(title, urn, book.getLatestFileModifiedDate());

    // add the required data to the book entry
    decorateBookEntry(entry, book, false);

    // add a full entry link to the partial entry
    if (logger.isTraceEnabled())  logger.trace("getBookEntry: add a full entry link to the partial entry");
    entry.addContent(FeedHelper.getFullEntryLink(fullEntryUrl));
    book.setReferenced();

    return entry;
  }
}
