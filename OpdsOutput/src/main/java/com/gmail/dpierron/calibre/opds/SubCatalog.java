package com.gmail.dpierron.calibre.opds;

/**
 * Abstract class containing functions and variables common to all catalog types
 */

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.zip.CRC32;

public abstract class SubCatalog {
  // cache some widely used objects.
  private final static Logger logger = LogManager.getLogger(SubCatalog.class);
  // Get some non-mutable configuration options once for efffeciency that are used widely in subcatalog variants
  // TODO:  Decide if perhaps these should be moved to CatalogManager?
  protected static ConfigurationHolder currentProfile;
  protected static Integer maxBeforeSplit;
  protected static Integer maxSplitLevels;
  protected static Integer maxBeforePaginate;
  protected static Boolean useExternalIcons;
  protected static Boolean useExternalImages;
  protected static Boolean includeCoversInCatalog;
  protected static String booksURI;
  protected static Collator collator = Collator.getInstance(ConfigurationManager.getLocale());
  protected static final CRC32 crc32 = new CRC32();
  protected static CachedFile defaultCoverFile;
  protected static String defaultCoverUri;

  //  PROPERTIES

  // This variable is set to the level (if any) for a particular catalog instance.
  // It would be a null/empty string for top level catalogs.   It will be set when
  // generating any additional level  This could be a custom catalog, the Featured
  // catalog or perhaps an additional level from extra tags/custom columns.
  private String catalogLevel;

  // This identifies the particular catalog type.   It is set within the classes
  // derived from this class.  It is used in conjuction with the level to work
  // out the default catalog and basefilename for a particular catalog instance.
  private String catalogType;

  // The folder in which the files for this sub-catalog are to be placed.
  // It should always be set - for the top level it is an empty string
  // It should always be stored without level and/or security information as
  // there are methods available to get the version with these added.
  private String catalogFolder;

  // The filename on which files in this catalog are based.
  // If not set then it is assumed to be the same as the catalog folder
  private String catalogBaseFilename;

  // The full path for the folder and base filename including the security code,
  // level in the folder part and all relevant separators. It is a cached copy for
  // effeciency reasons as it is needed for each catalog entry.
  private String catalogFolderBaseFilename;

  // Set to true if the XSL file for the list entries has changed
  private static Boolean xslCatalogChanged;
  // Set to true if  the XSL for the full entries has changed.
  private static Boolean xslFullEntryChanged;

  private List<Object> stuffToFilterOut;

  private List<Book> books;

  private String optimizeUrlPrefix;       // String that is used when trying to optimize URL's

  // CONSTRUCTORS

  public SubCatalog(List<Book> books) {
    this(null, books);
    initalise();
  }

  public SubCatalog() {
    initalise();
  }

  public SubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    initalise();
    setStuffToFilterOut(stuffToFilterOut);
    setBooks(books);
    //if (crc32 == null)
    //  crc32 = new CRC32();
  }

  /**
   * Initialise the static variables to the correct values for the
   * catalog that is bing generated
   */
  private static void initalise() {
    if (currentProfile == null) {
      currentProfile = ConfigurationManager.getCurrentProfile();
      maxBeforeSplit = currentProfile.getMaxBeforeSplit();
      maxSplitLevels = currentProfile.getMaxSplitLevels();
      maxBeforePaginate = currentProfile.getMaxBeforePaginate();
      useExternalIcons = currentProfile.getExternalIcons();
      useExternalImages = currentProfile.getExternalImages();
      includeCoversInCatalog = currentProfile.getIncludeCoversInCatalog();
      booksURI = currentProfile.getUrlBooks();
      collator = Collator.getInstance(ConfigurationManager.getLocale());
      xslCatalogChanged = ! CatalogManager.isGenerateFileSameAsCatalogFile(Constants.CATALOG_XSL);
      xslFullEntryChanged = ! CatalogManager.isGenerateFileSameAsCatalogFile(Constants.FULLENTRY_XSL);
      defaultCoverFile  = CachedFileManager.addCachedFile(new File(CatalogManager.getGenerateFolder(), Constants.DEFAULT_IMAGE_FILENAME));
      defaultCoverUri = Constants.PARENT_PATH_PREFIX + Constants.DEFAULT_IMAGE_FILENAME;
    }
  }
  /**
   * Ensure all the static variables are reset to be correct for the
   * catalog that is about to be generated.
   */
  public static void reset() {
    currentProfile = null;
    initalise();
  }

  private void setOptimizUrlPrefix() {
    optimizeUrlPrefix = Constants.PARENT_PATH_PREFIX + getCatalogPrefix() + getCatalogFolder() + Constants.FOLDER_SEPARATOR;
  }
  // METHODS

  /**
   * Get the current catalog level for this catalog instance
   * If we are not in a sub-level this will be empty
   *
   * @return
   */
  public String getCatalogLevel() {
    if (catalogLevel == null) {
      catalogLevel = "";
    }
    return catalogLevel;
  }

  /**
   * Set the catalog level for this particular catalog instance
   * It is only set for catalogs that are not top-level ones
   *
   * @param newlevel
   */
  public void setCatalogLevel(String newlevel) {
    assert newlevel != null;
    catalogLevel = newlevel;
    catalogFolderBaseFilename = null;
    setOptimizUrlPrefix();
  }

  /**
   * set the Catalog Level based on the breadcrumbs to this point
   * This is used when a complete set of sub-catalogs are required.
   * TODO assumes that the breadcrumbs are unique - this assumption needs validation
   *
   * @param breadcrumbs
   */
  public void setCatalogLevel(Breadcrumbs breadcrumbs) {
    assert (breadcrumbs != null);
    setCatalogLevel(encryptString(breadcrumbs.toString()));
    setOptimizUrlPrefix();
  }

  /**
   * Routine to get CRC32 value for given string
   * Used as helper function when encrypting folder and file names
   *
   * @param data
   * @return
   */
  protected static String encryptString(String data) {
    crc32.reset();
    crc32.update(data.getBytes());
    return Long.toHexString(crc32.getValue());
  }

  /**
   * If the user has asked for encrypted filename, then an
   * encryption string is added to the start of the filename
   * that is derived from the actual filename, and then the
   * filename in the clear is added.  This should mean that
   * catalogs are easy to read while the filenames are hard to guess.
   * <p/>
   * As a special case we need to separate out the foldername
   * (if present) and only encrypt the filename part.
   *
   * @param filename
   * @return
   */
  private static String encryptFilename(String filename) {
    if (CatalogManager.getSecurityCode().length() == 0) {
      // Do nothing if encryption not active
      return filename;
    }
    int pos = filename.indexOf(Constants.SECURITY_SEPARATOR);
    if (pos != -1) {
      int dummy = 1;
    }
    return encryptString(filename) + Constants.SECURITY_SEPARATOR + filename.substring(pos + 1);
  }

  /**
   * Get the current level and if necessary add the prefix if not empty
   *
   * @return
   */
  private String getCatalogPrefix() {
    if (catalogLevel == null)
      catalogLevel = "";
    String result = (CatalogManager.getSecurityCode().length() == 0 ? "" : CatalogManager.getSecurityCodeAndSeparator()) + catalogLevel;
    if (catalogLevel.length() > 0)
      result += Constants.LEVEL_SEPARATOR;
    return result;
  }

  /**
   * Get the current catalog type.
   * If it has never been set we assume an empty string
   *
   * @return
   */
  public String getCatalogType() {
    if (catalogType == null)
      catalogType = "";
    return catalogType;
  }

  /**
   * Set the catalog type.
   * This would normally only be used for special catalog types as the main
   * ones will have set this to a final value as part of class initialisation
   *
   * @param type
   */
  public void setCatalogType(String type) {
    assert (type != null);
    catalogType = catalogFolder = catalogBaseFilename = type;
    catalogFolderBaseFilename = null;
    setOptimizUrlPrefix();
  }

  /**
   * Get the folder for this sub-catalog
   *
   * @return The foldername for this catalog
   */
  public String getCatalogFolder() {
    if (Helper.isNullOrEmpty(catalogFolder)) {
      catalogFolder = getCatalogFolder(getCatalogType());
    }

    //  Debugging asserts - could be removed if not wanted
    assert catalogFolder.indexOf(Constants.SECURITY_SEPARATOR) == -1 : "Program error: catalogFolder contains SECURITY_SEPARATOR (" + catalogFolder + ")";
    // assert catalogFolder.indexOf(Constants.LEVEL_SEPARATOR) == -1 :
    //       "Program error: catalogFolder contains LEVEL_SEPARATOR (" + catalogFolder + ")";

    return catalogFolder;
  }

  /**
   * Get the full folder name for the given folder type
   * Needs to take into account any level we may be working at
   * and also any secutiry code that might be active.
   *
   * @param foldertype // The type (ignoring level) of folder we want
   * @return // The folder name including any level and security prefix if type not empty
   */
  public String getCatalogFolder(String foldertype) {
    assert (foldertype != null);
    if (foldertype.length() == 0) {
      return foldertype;
    }
    return getCatalogPrefix() + foldertype;
  }

  /**
   * Get the catalog folder for the given type with the name derived
   * from catalog type.  It should have the security prefix the catalog name
   * but omit any level information.   This is primarily used for the
   * sub-catalog types such as 'books' and 'author' which are always
   * at the top level.
   *
   * @return The catalog name preceded with any security infomation, but no level
   */
  public String getCatalogFolderWithSecurityNoLevel() {
    assert Helper.isNotNullOrEmpty(catalogType) : "Program Error catalogType not set";
    return getCatalogFolderWithSecurityNoLevel(catalogType);
  }

  /**
   * Get the catalog folder for the given type with the name derived
   * from type.  It should have the security prefix the catalog name
   * but omit any level information.   This is primarily used for the
   * sub-catalog types such as 'books' and 'author' which are always
   * at the top level.
   *
   * @param foldertype The catalog 'type'
   * @return The catalog name preceded with any security infomation, but no level
   */
  public String getCatalogFolderWithSecurityNoLevel(String foldertype) {
    assert (Helper.isNotNullOrEmpty(foldertype)) : "Program Error: foldertype not set";
    String result = (CatalogManager.getSecurityCode().length() == 0 ? "" : CatalogManager.getSecurityCodeAndSeparator()) + foldertype;

    // Debugging asserts  - could be removed if not wanted
    int pos = result.indexOf(Constants.SECURITY_SEPARATOR);
    assert result.substring(pos + 1).indexOf(Constants.SECURITY_SEPARATOR) == -1 : "Program error: Two occurences of SECURITY_SEPARATOR (" + result + ")";
    assert result.indexOf(Constants.LEVEL_SEPARATOR) == -1 : "Program error: Unexpected LEVEL_SEPARATOR (" + result + ")";

    return result + foldertype;
  }

  /**
   * Get the catalog folder for the given type with the name derived
   * from type.  It should have the security prefix the catalog name
   * but omit any level information.   This is primarily used for the
   * sub-catalog types such as 'books' and 'author' which are always
   * at the top level.
   *
   * @param foldertype The catalog 'type'
   * @return The catalog name preceded with any security infomation, but no level
   */
  public String getCatalogFolderWithLevelAndSecurity(String foldertype) {
    assert (Helper.isNotNullOrEmpty(foldertype)) : "Program Error: foldertype not set";
    String result = (CatalogManager.getSecurityCode().length() == 0 ? "" : CatalogManager.getSecurityCodeAndSeparator()) + foldertype;

    // Debugging asserts - could be removed if not wanted
    int pos = result.indexOf(Constants.SECURITY_SEPARATOR) + 1;
    assert result.substring(pos).indexOf(Constants.SECURITY_SEPARATOR) == -1 : "Program error: Two occurences of SECURITY_SEPARATOR (" + result + ")";
    pos = result.indexOf(Constants.LEVEL_SEPARATOR);
    assert result.substring(pos).indexOf(Constants.LEVEL_SEPARATOR) == -1 : "Program error: Two occurences of LEVEL_SEPARATOR (" + result + ")";

    return result;
  }

  /**
   * Set the folder to be used
   * It is always stored decoded and without any trailing slash
   * There should also not be any security code present - if so remove it.
   * <p/>
   * NOTE: For convenience we also allow a full folder.filename path
   * to be passed in and then we extract the folder part.
   *
   * @param folder folder name to set
   */
  public void setCatalogFolder(String folder) {
    assert folder != null;

    int pos = folder.indexOf(Constants.FOLDER_SEPARATOR);
    if (pos != -1) {
      folder = folder.substring(0, pos);
      assert folder.indexOf(Constants.FOLDER_SEPARATOR) == -1 : "Program Error: Unexpected occurence of FOLDER_SEPARATOR (folder=" + folder + ")";
    }

    pos = folder.indexOf(Constants.SECURITY_SEPARATOR);
    if (pos != -1) {
      assert (folder.substring(0, pos).equals(CatalogManager.getSecurityCode())) : "Program Error:  Security Code does not seem to match expected value (folder=" + folder + ")";
      assert folder.indexOf(Constants.SECURITY_SEPARATOR, pos + 1) == -1 : "Program error: Unexpected Second Occurencs of SECURITY_SEPARATOR (folder=" +
          folder + ")";
      ;
      folder = folder.substring(pos + 1);
    }

    pos = folder.indexOf(Constants.LEVEL_SEPARATOR);
    if (pos != -1) {
      assert (folder.substring(0, pos).equals(catalogLevel)) : "Program Error:  Catalog level does not seem to match expected value  (folder=" + folder + ")";
      assert folder.indexOf(Constants.LEVEL_SEPARATOR, pos + 1) == -1 : "Program error: Unexpected second occurencs of LEVEL_SEPARATOR (folder=" + folder + ")";
      folder = folder.substring(pos + 1);
    }

    catalogFolder = folder;
    setOptimizUrlPrefix();
  }

  /**
   * Variant of setFolder when we want to split according to Id
   *
   * @param folder
   * @param id
   */
  public void setCatalogFolderSplit(String folder, String id) {
    setCatalogFolder(folder + Constants.TYPE_SEPARATOR + (int) (Long.valueOf(id) / 1000));
  }

  /**
   * Get the Current Catalog Base filename
   * <p/>
   * If both the folder name, catalog type and catalog level are not set we treat
   * this as a special case and add in the security code.
   *
   * @return
   */
  public String getCatalogBasefilename() {
    if (catalogBaseFilename == null) {
      catalogBaseFilename = getCatalogType();
    }

    // Debugging assert - could be removed if not wanted
    assert catalogBaseFilename.indexOf(Constants.FOLDER_SEPARATOR) == -1 : "Program Error:  Unexpected FOLDER_SEPARATOR (" + catalogBaseFilename + ")";

    if (catalogLevel.length() == 0 && catalogFolder.length() == 0 && catalogType.length() == 0) {
      // The special case for top level
      return CatalogManager.getSecurityCodeAndSeparator() + catalogBaseFilename;
    } else {
      // The normal case
      return catalogBaseFilename;
    }
  }

  /**
   * Set the base filename to be used for this catalog.
   * Only needed when it cannot be derived automatically from the type
   * <p/>
   * NOTE:  The name is always stored 'in the clear' so any security code
   * or level type information needs removing.
   *
   * @param name
   */
  public void setCatalogBaseFilename(String name) {
    assert Helper.isNotNullOrEmpty(name) : "Program Error: invalid name parameter (" + name + ")";
    // We want to skip over any leading folder name
    int pos = name.indexOf(Constants.FOLDER_SEPARATOR);

    // Debugging assert - could be removed if not wanted
    assert name.substring(pos + 1).indexOf(Constants.FOLDER_SEPARATOR) == -1 : "Program Error: Multiple FOLDER_SEPARATORS found (" + name + ")";
    if (pos != -1) {
      name = name.substring(pos + 1);     // Remove the folder part
    }
    // We also want to remove any leading occurrence of security code
    if (CatalogManager.getSecurityCode().length() > 0 && name.startsWith(CatalogManager.getSecurityCodeAndSeparator())) {
      name = name.substring(CatalogManager.getSecurityCodeAndSeparator().length());  // Remove the security code
    }
    // Finally we want to remove any existing encryption string
    pos = name.indexOf(Constants.SECURITY_SEPARATOR);
    name = name.substring(pos + 1);
    catalogBaseFilename = name;
    catalogFolderBaseFilename = null;
  }


  /**
   * Get the base folder/file name based on the object propertied
   * Level is added from the current object as require
   *
   * @return
   */
  public String getCatalogBaseFolderFileName() {
    if (catalogFolderBaseFilename == null) {
      // Special case of empty folder and level (as used for index files!)
      if (catalogFolder.length() == 0 & catalogLevel.length() == 0) {
        catalogFolderBaseFilename = getCatalogBasefilename();
      } else {
        catalogFolderBaseFilename = getCatalogPrefix() + getCatalogFolder();  // This will include security/level prefixes
        catalogFolderBaseFilename += Constants.FOLDER_SEPARATOR + encryptFilename(getCatalogBasefilename());
      }
    }

    //int pos = catalogFolderBaseFilename.indexOf(Constants.SECURITY_SEPARATOR);
    //pos = catalogFolderBaseFilename.indexOf(Constants.LEVEL_SEPARATOR);
    //assert catalogFolderBaseFilename.substring(pos+1).indexOf(Constants.LEVEL_SEPARATOR) == -1 :
    //    "Program error: Two occurences of LEVEL_SEPARATOR (" + catalogFolderBaseFilename + ")";

    return catalogFolderBaseFilename;
  }

  /**
   * Get the folder/file name based on the type parameter
   * Level is added from the current object as require .
   * It will have the embedded level/security information if needed.
   *
   * @return
   */
  public String getCatalogBaseFolderFileName(String type) {
    assert Helper.isNotNullOrEmpty(type);
    String folder = getCatalogFolder(type);
    return folder + ((folder.length() != 0) ? Constants.FOLDER_SEPARATOR : "") + encryptFilename(type);
  }

  public String getCatalogBaseFolderFileNameNoLevel(String type) {
    assert Helper.isNotNullOrEmpty(type);
    String result = (CatalogManager.getSecurityCode().length() == 0 ? "" : CatalogManager.getSecurityCodeAndSeparator()) + type;
    if (result.length() > 0)
      result += Constants.FOLDER_SEPARATOR;
    return result + encryptFilename(type);
  }

  /**
   * Get the full base folder/filename including the speified id.
   * It will get the level/security information from the current catalog properties.
   *
   * @param id
   * @return
   */
  public String getCatalogBaseFolderFileNameId(String id) {
    String name = getCatalogBaseFolderFileName();
    int pos = name.indexOf(Constants.FOLDER_SEPARATOR);
    String folder = "";
    if (pos != -1) {
      folder = name.substring(0, pos + 1);
      name = name.substring(pos + 1);
    }
    String result = encryptFilename(name + Constants.TYPE_SEPARATOR + id);
    return folder + result;
  }

  /**
   * Get the full base folder/filename for the given type and id
   * It will get the level information from the current catalog properties.
   * Security information will also be added as required
   *
   * @param type
   * @param id
   * @return
   */
  public String getCatalogBaseFolderFileNameId(String type, String id) {
    String folder = getCatalogFolder(type);
    return folder + ((folder.length() != 0) ? Constants.FOLDER_SEPARATOR : "") + encryptFilename(type + Constants.TYPE_SEPARATOR + id);
  }

  /**
   * Get the full base folder/filename for the given type and id
   * It will get the level information from the current catalog properties.
   * Security information will also be added as required.
   * <p/>
   * To keep the number of files in a single folder down (which can affect
   * perforance we store a maximum of 1000 book id;s in a single folder
   * (although in practise it is likely to be slightly less due to gaps
   * in the Calibre Id sequence after books have been deleted/altered/merged.
   *
   * @param type
   * @param id
   * @return
   */
  public String getCatalogBaseFolderFileNameIdSplit(String type, String id, int splitSize) {
    String filename = getCatalogBaseFolderFileNameId(type, id);
    int pos = filename.indexOf(Constants.FOLDER_SEPARATOR);
    assert pos != -1;
    filename = filename.substring(0, pos) + Constants.TYPE_SEPARATOR + ((long) (Long.parseLong(id) / splitSize)) + filename.substring(pos);
    return filename;
  }

  /**
   * Get the full base folder/filename for the given type and id
   * Security information will be added, but no level information.
   * This is intended for entry types that are always at the top level
   * (such as books)
   *
   * @param type
   * @param id
   * @return
   */
  private static String getCatalogBaseFolderFileNameIdNoLevel(String type, String id) {
    String result = (CatalogManager.getSecurityCode().length() == 0 ? "" : CatalogManager.getSecurityCodeAndSeparator()) + type;
    if (result.length() > 0)
      result += Constants.FOLDER_SEPARATOR;
    result = result + encryptFilename(type + Constants.TYPE_SEPARATOR + id);
    return result;
  }

  /**
   * Get the full base folder/filename for the given type and id
   * Security information will be added, but no level information.
   * This is intended for entry types that are always at the top level
   * (such as books)
   * <p/>
   * To keep the number of files in a single folder down (which can affect
   * perforance we store a maximum of 1000 book id;s in a single folder
   * (although in practise it is likely to be slightly less due to gaps
   * in the Calibre Id sequence after books have been deleted/altered/merged.
   *
   * @param id
   * @return
   */
  public static String getCatalogBaseFolderFileNameIdNoLevelSplit(String type, String id, int splitSize) {
    String filename = getCatalogBaseFolderFileNameIdNoLevel(type, id);
    int pos = filename.indexOf(Constants.FOLDER_SEPARATOR);
    assert pos != -1;
    filename = filename.substring(0, pos) + Constants.TYPE_SEPARATOR + ((long) (Long.parseLong(id) / splitSize)) + filename.substring(pos);
    return filename;
  }

  /**
   * Determine if the icon prefix should be for the current or parent folder
   *
   * @param inSubDir
   * @return
   */
  protected String getIconPrefix(boolean inSubDir) {
    return inSubDir ? Constants.PARENT_PATH_PREFIX : Constants.CURRENT_PATH_PREFIX;
  }

  /**
   * Optimise the URN to simplify them if pointing to files in sthe currentcatalog folder
   * <p/>
   * NOTE:  We should never optimize breadcrumb URL's as we do not know ehere they are called from
   *
   * @param url the unoptimised URL
   * @return the optimized URL
   */
  public String optimizeCatalogURL(String url) {
    assert optimizeUrlPrefix != null : "Program Error:  optimizeUrlPrefix should not be null!";
    // See if start is pointing back to current folder?
    if (url.startsWith(optimizeUrlPrefix)) {
      // If so we can strip the folder name part
      int pos = optimizeUrlPrefix.length();
      assert url.length() > pos : "Program Error: URL only has prefix!";
      //TODO  Activate the following code if trace shows would achieve expected results
      if (logger.isTraceEnabled())
        logger.trace("should be able to optimize following URL: " + url + ", (folder=" + getCatalogFolder() + ") to " + Constants.CURRENT_PATH_PREFIX +
            url.substring(pos));
      // return Constants.CURRENT_PATH_PREFIX + url.substring(pos);
    }
    return url;
  }

  /**
   * Set the list of books to be included in this (sub)catalog
   *
   * @param books
   */
  void setBooks(List<Book> books) {
    this.books = null;
    if (Helper.isNotNullOrEmpty(stuffToFilterOut)) {
      this.books = filterOutStuff(books);
    }
    if (this.books == null) {
      this.books = books;
    }
  }

  /**
   * Get the list of books associated with this sub-catalog
   *
   * @return
   */
  List<Book> getBooks() {
    return books;
  }
  /**
   * Function to sort books by timestamp (last modified)
   *
   * @param books
   */
  static void sortBooksByTimestamp(List<Book> books) {
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
   * Sort the list of books alphabetically
   * We allow the field that is to be used for sorting
   * titles to be set as a configuration parameter
   *
   * @param books
   */
  static void sortBooksByTitle(List<Book> books) {
    Collections.sort(books, new Comparator<Book>() {
      public int compare(Book o1, Book o2) {
        String title1 = o1.getTextToSort();
        String title2 = o2.getTextToSort();
        return Helper.checkedCollatorCompareIgnoreCase(title1, title2, collator);
      }
    });
  }

  /**
   * Get the list of stuff acting as a filter for this sub-catalog
   *
   * @return
   */
  List<Object> getStuffToFilterOut() {
    return stuffToFilterOut;
  }

  /**
   * Get the list of stuff to filter out extended by new values
   *
   * @param newStuff
   * @return
   */
  List<Object> getStuffToFilterOutAnd(Object newStuff) {
    List<Object> result = new ArrayList<Object>();
    if (stuffToFilterOut != null)
      result.addAll(stuffToFilterOut);
    if (newStuff != null)
      result.add(newStuff);
    return result;
  }

  /**
   * Set the list of stuff to filter out
   *
   * @param stuffToFilterOut
   * @return
   */
  SubCatalog setStuffToFilterOut(List<Object> stuffToFilterOut) {
    this.stuffToFilterOut = stuffToFilterOut;
    return this;
  }

  /**
   * Get the list of books filtered according to the filter criteria
   *
   * @param originalBooks
   * @return
   */
  List<Book> filterOutStuff(List<Book> originalBooks) {
    // by default, simply return the book list
    return originalBooks;
  }

  /**
   * Extract the folder part of the filename
   *
   * @param pCatalogFileName
   * @return
   */
  public String getFolderName(String pCatalogFileName) {
    assert (Helper.isNotNullOrEmpty(pCatalogFileName)) : "Program Error: empty filename!";
    int pos = pCatalogFileName.indexOf(Constants.FOLDER_SEPARATOR);
    return (pos == -1) ? pCatalogFileName : pCatalogFileName.substring(0, pos);
  }

  /**
   * Determine if the conditions for SplitByLetter to be active are tru
   *
   * @param splitOption
   * @param count
   * @return
   */
  public Boolean checkSplitByLetter(SplitOption splitOption, int count) {
    return (splitOption == SplitOption.SplitByLetter) && (maxSplitLevels > 0) && count > maxBeforeSplit;
  }

  /**
   * Determine if Splitoption should be changed from SplitByLetter to Paginate
   * because we have already split by the maximum number of levels requested..
   *
   * @param splitLetters
   * @return
   */
  public SplitOption checkSplitByLetter(String splitLetters) {
    return splitLetters.length() < maxSplitLevels ? SplitOption.SplitByLetter : SplitOption.Paginate;
  }

  boolean isInDeepLevel() {
    return Helper.isNotNullOrEmpty(stuffToFilterOut);
  }



  /**
   * @return a result composed of the resulting OPDS entry, and the relative url to the subcatalog
   */
  // public abstract Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException;

  /**
   * Create the XML and HTML files (as required by configuration parameters) from
   * the XML document that has just been created.
   *
   * @param feed           The feed that is to be used to generate the output files
   * @param xmlFilename    The name of the output file.
   * @param feedType       The type of file that is to be generated
   * @param isHtmlOptimiseAllowed  Set when HTML optimisation mustbe suppressed
   * @throws IOException Any exception would be unexpected, but it is always theoretically possible!
   */
  public void createFilesFromElement(Element feed,
                String xmlFilename,
                HtmlManager.FeedType feedType,
                Boolean isHtmlOptimiseAllowed)
                  throws IOException {

    // Various asserts to help with identifying logic faults in the program!
    assert feed != null : "Programerror: Unexpected attempt to create file from non-existent feed";
    assert Helper.isNotNullOrEmpty(xmlFilename) : "Program error: Attempt to create XML file for empty/null filename";
    assert !xmlFilename.startsWith(CatalogManager.getGenerateFolder().toString()) : "Program Error:  filename should not include catalog folder (" +
        xmlFilename + ")";
    // int pos = outputFilename.indexOf(Constants.SECURITY_SEPARATOR);
    // assert outputFilename.substring(pos+1).indexOf(Constants.SECURITY_SEPARATOR) == -1 :
    //    "Program error: Two occurences of SECURITY_SEPARATOR (" + outputFilename + ")";
    // pos = outputFilename.indexOf(Constants.LEVEL_SEPARATOR);
    // assert outputFilename.substring(pos+1).indexOf(Constants.LEVEL_SEPARATOR) == -1 :
    //    "Program error: Two occurences of LEVEL_SEPARATOR (" + outputFilename + ")";


    if (!xmlFilename.endsWith(Constants.XML_EXTENSION)) {
      xmlFilename += Constants.XML_EXTENSION;
    }
    CachedFile xmlFile = CachedFileManager.addCachedFile(CatalogManager.storeCatalogFile(xmlFilename));
    // Avoid creating files that already exist.
    // (if xml file exists then HTML one will as well)
    if (xmlFile.exists()) {
      logger.trace("\n\n*** Attempt to generate file already done (" + xmlFilename + ") - see if it can be optimised out! ***\n");
      //      if (logger.isTraceEnabled()) logger.trace("\n\n*** Attempt to generate file already done (" + outputFilename + ") - see if it can be optimised out! ***\n");
      return;
    }

    // Create as a DOM object
    // TODO  Check if there might be a cheaper way to do this not using DOM?
    Document document = new Document();
    document.addContent(feed);

    // write the XML file
    // (unless the user has suppressed the OPDS catalogs)
    if (currentProfile.getGenerateOpds()) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(xmlFile);
        String prettyXML = JDOMManager.getPrettyXML().outputString(document);
        String compactXML = JDOMManager.getCompactXML().outputString(document);
        String rawXML = JDOMManager.getRawXml().outputString(document);

        JDOMManager.getPrettyXML().output(document, fos);;
      } catch (RuntimeException e) {
        logger.warn("Error writing file " + xmlFilename + "(" + e.toString() + ")");
      } finally {
        if (fos != null)
          fos.close();
        xmlFile.clearCachedInformation();
      }
    }

    //  generate corresponding HTML file

    CachedFile htmlFile = CachedFileManager.addCachedFile(HtmlManager.getHtmlFilename(
        xmlFile.getAbsolutePath()));
    if (htmlFile.exists()) {
      logger.warn("Program Error?  Attempt to recreate existing HTML file '" + htmlFile + "'");
      return;
    }

    // See if we can optimise things by avoiding generating the HTML file
    // if:
    // - The flag to allow HTML optimisation is true
    //   (typically false for book lists after browse-by-cover mode changed/unknown)
    // - the target HTML file already exists in the catalog
    // - the XML file is identical to the one already in the catalog
    // - the XSL file is older than the HTML file

    CachedFile catalogXmlFile = CachedFileManager.addCachedFile(CatalogManager.getCatalogFolder() + xmlFile.getAbsolutePath().substring(CatalogManager.getGenerateFolderpathLength()));
    CachedFile catalogHtmlFile = CachedFileManager.addCachedFile(CatalogManager.getCatalogFolder() + htmlFile.getAbsolutePath().substring(CatalogManager.getGenerateFolderpathLength()));
    boolean xslChanged;

    switch (feedType) {
      case MainCatalog:
      case Catalog:
            xslChanged = xslCatalogChanged;
            break;
      case BookFullEntry:
            xslChanged = xslFullEntryChanged;
            break;
      default:
            logger.error("Program Error: unrecognised feedType for file '" + xmlFile + "'" );
            return;
    }
    if (catalogXmlFile.exists()
    && catalogXmlFile.getCrc() == xmlFile.getCrc()) {
      catalogXmlFile.setChanged(false);
      xmlFile.setChanged(false);
    }
    if (isHtmlOptimiseAllowed
    && ! xslChanged
    && catalogHtmlFile.exists()
    && xmlFile.isChanged() == false ) {
//    && CatalogManager.isSourceFileSameAsTargetFile(xmlFile, catalogXmlFile)) {
      catalogHtmlFile.setChanged(false);
      htmlFile.setChanged(false);
      CatalogManager.statsHtmlUnchanged++;
    } else {
      if (currentProfile.getGenerateHtml()) {
        CatalogManager.htmlManager.generateHtmlFromDOM(document, htmlFile.getAbsoluteFile(),
            feedType);
        htmlFile.clearCachedInformation();
        CatalogManager.statsHtmlChanged++;
//      } else {
//        CachedFileManager.removeCachedFile(htmlFile);
      }
    }
    // See if we need to keep the XML file
    if (currentProfile.getGenerateOpds()) {
      if (xmlFile.isChanged() == false) {
        xmlFile.delete();     // We do not keep the XML file in the temp folder if marked as unchanged
        CatalogManager.statsXmlUnchanged++;
      } else {
        CatalogManager.statsXmlChanged++;
      }
    } else {
      xmlFile.delete();
      CachedFileManager.removeCachedFile(xmlFile);
      CatalogManager.statsXmlDiscarded++;
    }
  }

  /*
   * Decide if a Series cross-reference should be generated for this book
   *
   * Takes into account if this is the only book and the relevant setting
   */
  protected boolean isSeriesCrossreferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks()
    ||  ! currentProfile.getIncludeSerieCrossReferences()) {
      return false;
    }
    Series series = book.getSeries();
    if (series == null) {
      return false;
    }

    if (currentProfile.getSingleBookCrossReferences()
    ||  DataModel.getMapOfBooksBySeries().get(series).size() > 1) {
      return true;
    }

    return false;
  }

  /**
   * Decide if an Author cross-reference should be generated for this book
   *
   * Does not take into account whether an author has a single book
   */
  protected boolean isAuthorCrossReferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks()
    ||  ! currentProfile.getIncludeAuthorCrossReferences()) {
      return false;
    }
    return book.hasAuthor();
  }

  /**
   * Decide if Tag cross-references should be generated for this book
   *
   * Does not take into account whether a tag has a single book
   */
  protected boolean isTagCrossReferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks()
    ||  ! currentProfile.getIncludeTagCrossReferences()) {
      return false;
    }
    List<Tag> authors = book.getTags();
    return (book.getTags() != null);
  }

  /**
   * Decide if the rating cross reference should be included.
   *
   * Takes into account if this is the only book and the relevant setting
   * Decide if Ratings cross-reference should be generated for this book
   */
  protected boolean isRatingCrossReferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks()
    ||  ! currentProfile.getIncludeRatingCrossReferences()) {
      return false;
    }
    BookRating rating = book.getRating();
    if (rating == null) {
      return false;
    }
    if (currentProfile.getSingleBookCrossReferences()
    ||  DataModel.getMapOfBooksByRating().get(rating).size() > 1) {
      return true;
    }
    return false;
  }

  /**
   * #c2o-208
   * Create additional links for a paginated set depending on
   * - the current pages
   * - the maximum page count
   *
   * A Prev link is created if currenta page > 1
   * A Last link is created if (max pages - current page) > 2
   * A First link is created if Current Page > 2
   *
   * NOTE:  This is always called on pages AFTRER the first,
   *        to provide the links to be inserted into the previous page
   *
   * @param filename    Base filename for the URL.
   * @param pageNumber  current page number
   * @param maxPages    maximum pages in the set.
   */
  public Element createPaginateLinks (Element feed, String filename, int pageNumber, int maxPages) {

    int pos = filename.lastIndexOf(Constants.XML_EXTENSION);
    if (pos > 0)  filename = filename.substring(0,pos);
    pos = filename.lastIndexOf(Constants.TYPE_SEPARATOR);
    assert pos > 0;
    // assert Integer.toString(pageNumber).equals((filename.substring(pos + 1)));
    if (! Integer.toString(pageNumber).equals((filename.substring(pos + 1)))) {
      int dummy = 1;
    }
    filename = filename.substring(0,pos + 1);

      // Prev link
    if (pageNumber > 1) {
        feed.addContent(FeedHelper.getNavigationLink(filename + (pageNumber-1) + Constants.XML_EXTENSION,
                                                     FeedHelper.RELATION_PREV,
                                                     Localization.Main.getText("title.prevpage", pageNumber-1, maxPages)));
    }
    // First link
    if (pageNumber > 2) {
      feed.addContent(FeedHelper.getNavigationLink(filename + "1" + Constants.XML_EXTENSION,
                                                   FeedHelper.RELATION_FIRST,
                                                   Localization.Main.getText("title.firstpage", 1, maxPages)));
    }
    // Last link
    if ((maxPages - pageNumber) > 1) {
      feed.addContent(FeedHelper.getNavigationLink(filename + maxPages + Constants.XML_EXTENSION,
                                                   FeedHelper.RELATION_LAST,
                                                   Localization.Main.getText("title.lastpage", maxPages, maxPages)));
    }

    // Next link
    // It is always one page out because of the way the result is used
    if (pageNumber == 1)  return null;
    return FeedHelper.getNavigationLink(filename + (pageNumber) + Constants.XML_EXTENSION,
                                        FeedHelper.RELATION_NEXT,
                                        Localization.Main.getText("title.nextpage", pageNumber, maxPages));

  }

  // TODO  GENERALISED SUB-CATALOG TYPE HANDLING

  // TODO:           WORK IN PROGRESS !

  // See if we can generalise the cration of sub-catalog pages to a
  // treatment with a series of standardised methods implemented in each specific
  // specific sub-catalog type.    This would have the huge advantage of greatly
  // reducing the chance of errors in a particular type as well as making the
  // maintenance of the types simpler.  It will also simplify adding new types.

  // The method to be used to sort objects of this type
/*
  protected abstract void sortMethod(List<Object> obj);
*/

  /**
   * Get a page withina sub-catalog
   *
   * @param pBreadcrumbs
   * @param listobjects
   * @param inSubDir
   * @param from
   * @param title
   * @param summary
   * @param urn
   * @param pFilename
   * @param splitOption
   * @param icon
   * @param firstElements
   * @param options
   * @return
   * @throws IOException
   */
/*
  protected Element getListOfObjects(Breadcrumbs pBreadcrumbs,
      List<? extends GenericDataObject> listobjects,
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
    assert listobjects != null;
    // if (listobjects == null) listobjects = getBooks();
    if (pFilename == null)  pFilename = getCatalogBaseFolderFileName();

    //  Now some consistency checks

    // Now get on with main processing
    int catalogSize = listobjects.size();
    if (logger.isDebugEnabled()) logger.debug("getListOfBooks:catalogSize=" + catalogSize);

    if (from != 0) inSubDir = true;
    if (Helper.isNotNullOrEmpty(pBreadcrumbs) &&  pBreadcrumbs.size() > 1) inSubDir = true;
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
        willSplitByLetter = checkSplitByLetter(splitOption, listobjects.size());
        willSplitByDate = true;
        break;
      case SplitByLetter:
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=SplitByLetter");
        assert from == 0 : "getListBooks: splitByLetter, from=" + from;
        willSplitByLetter = checkSplitByLetter(splitOption, listobjects.size());
        willSplitByDate = false;
        break;
      default:
        // ITIMPI:  Not sure that this case can ever arise
        //          Just added as a safety check
        if (logger.isTraceEnabled()) logger.trace("getListOfBooks:splitOption=" + splitOption);
        assert from == 0 : "getListBooks: unknown splitOption, from=" + from;
        willSplitByLetter = checkSplitByLetter(splitOption, listobjects.size());
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

    int pageNumber = Summarizer.getPageNumber(from + 1);
    int maxPages = Summarizer.getPageNumber((willSplitByDate || willSplitByLetter) ? 0 : catalogSize);

    // generate the book list files
    String filename = pFilename + Constants.PAGE_DELIM + Integer.toString(pageNumber);
    String urlExt = CatalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, pBreadcrumbs.size() > 1  || inSubDir);

    Element feed;
    feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true);
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
      result = getListOfObjectsSplitByDate( breadcrumbs,
                                            DataModel.splitObjectsByDate(listobjects),
                                            true,   // Musy be true if splitting by date
                                            title,
                                            urn,
                                            pFilename,
                                            icon,
                                            options);
    } else if (willSplitByLetter) {
      // Split by letter listing
      result = getListOfObjectsSplitByLetter( breadcrumbs,
                                              DataModel.splitObjectsByLetter(listobjects),
                                              true, // Must be true if splitting by letter
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
      progressText += " (" + Summarizer.getBookWord(listobjects.size()) + ")";
      CatalogManager.callback.showMessage(progressText.toString());
      for (int i = from; i < listobjects.size(); i++) {
        // check if we must continue
        CatalogManager.callback.checkIfContinueGenerating();

        // See if we need to do the next page
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          // TODO #c2o-208   Add Previous, First and Last links if needed
          // ... YES - so go for next page
          if (logger.isDebugEnabled()) logger.debug("making a nextpage link");
          Element nextLink = getListOfObjects(pBreadcrumbs,
                                              listobjects,
                                              true,             // Awlays in SubDir (need to check this)
                                              i,                // Continue nfrom where we were
                                              title,
                                              summary,
                                              urn,
                                              pFilename, splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption,
                                              icon,
                                              null,             // No firstElements
                                              options);
          result.add(0, nextLink);
          break;
        } else {
          // ... NO - so add book to this page
          Object book = listobjects.get(i);
          if (logger.isTraceEnabled()) logger.trace("getListOfObjects: adding object to the list : " + book);
          try {
            logger.trace("getListOfBooks: breadcrumbs=" + breadcrumbs + ", book=" + book + ", options=" + options);
            Element entry = getDetailedEntry(breadcrumbs, book, options);
            if (entry != null) {
              if (logger.isTraceEnabled()) logger.trace("getListOfBooks: entry=" + entry);
              result.add(entry);
              TrookSpecificSearchDatabaseManager.addBook((Book) book, entry);
            }
          } catch (RuntimeException e) {
            logger.error("getListOfBooks: Exception on book: " + ((Book)book).getDisplayName() + "[" + ((Book)book).getId() + "]", e);
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
    String urlInItsSubfolder = CatalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);

    entry = createPaginateLinks(feed, filename, pageNumber, maxPages);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
    if (from == 0) {
      entry = FeedHelper.getCatalogEntry(title, urn, urlInItsSubfolder, summary, icon);
    }
    return entry;
  };
*/

  /**
   * Produce a list plit by letter
   *
   * @param pBreadcrumbs
   * @param mapOfObjectsByLetter
   * @param inSubDir
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @param splitOption
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
/*
  protected List<Element> getListOfObjectsSplitByLetter(
      Breadcrumbs pBreadcrumbs,
      Map<String,  List<? extends GenericDataObject>> mapOfObjectsByLetter,
      boolean inSubDir,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      SplitOption splitOption,
      String icon,
      Option... options) throws IOException {
    if (Helper.isNullOrEmpty(mapOfObjectsByLetter))
      return null;

    if (pBreadcrumbs.size() > 1) inSubDir = true;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle += ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfObjectsByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String letterFilename = Helper.getSplitString(baseFilename, letter, Constants.TYPE_SEPARATOR);
      String letterUrn = Helper.getSplitString(baseUrn, letter, Constants.URN_SEPARATOR);

      List<? extends GenericDataObject> objectsInThisLetter = mapOfObjectsByLetter.get(letter);
      String letterTitle;
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.book.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("bookword.title"),
            letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);

      // try and list the items to make the summary
      String summary = Summarizer.summarizeBooks(objectsInThisLetter);

      Element element = null;
      if (objectsInThisLetter.size() > 0) {
        element = getListOfObjects(pBreadcrumbs,
                                  objectsInThisLetter,
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
        assert (objectsInThisLetter.size() <= 0) : "booksInThisLetter=" + objectsInThisLetter.size() + " for letter '" + letter + "'";
      }

      if (element != null)
        result.add(element);
    }
    return result;

  };
*/
  /**
   * Produce a list split by date
   *
   * Only used as part of the 'recent' section at the moment
   * so maybe it should only be present in that calss?
   *
   * @param pBreadcrumbs
   * @param mapOfObjectsByDate
   * @param inSubDir
   * @param baseTitle
   * @param baseUrn
   * @param baseFilename
   * @param icon
   * @param options
   * @return
   * @throws IOException
   */
/*
  protected  List<Element> getListOfObjectsSplitByDate(
      Breadcrumbs pBreadcrumbs,
      Map<DateRange, List<GenericDataObject>> mapOfObjectsByDate,
      boolean inSubDir,
      String baseTitle,
      String baseUrn,
      String baseFilename,
      String icon,
      Option... options) throws IOException {
    if (Helper.isNullOrEmpty(mapOfObjectsByDate))
      return null;
    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    if (pBreadcrumbs.size() > 1) inSubDir = true;

    List<Element> result = new LinkedList<Element>();
    SortedSet<DateRange> ranges = new TreeSet<DateRange>(mapOfObjectsByDate.keySet());
    for (DateRange range : ranges) {
      // generate the range file
      String rangeFilename = baseFilename + Constants.TYPE_SEPARATOR + range;

      String rangeUrn = Helper.getSplitString(baseUrn, range.toString(), Constants.URN_SEPARATOR);

      String rangeTitle = LocalizationHelper.getEnumConstantHumanName(range);
      List<? extends GenericDataObject> booksInThisRange = mapOfObjectsByDate.get(range);

      // try and list the items to make the summary
      String summary = Summarizer.summarizeBooks(booksInThisRange);

      Element element = null;
      if (booksInThisRange.size() > 0) {
        element = getListOfObjects(pBreadcrumbs,
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
*/
//  public abstract <T extends GenericDataObject> List<T> getObjectList() ;

  /**
   * Get the detailed entry for this object type
   * We need to over-ride this method in each subcatalog type
   * as the details are going to be very type dependent.
   *
   * @param pBreadcrumbs
   * @param obj
   * @param options
   * @return
   * @throws IOException
   */
  public abstract Element getDetailedEntry(Breadcrumbs pBreadcrumbs,
                                          Object  obj,
                                          Option... options) throws IOException;

}
