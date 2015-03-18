package com.gmail.dpierron.calibre.opds;

/**
 * Abstract class containing functions and variables common to all catalog types
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.CRC32;

public abstract class SubCatalog {
  // cache some widely used objects.
  private final static Logger logger = Logger.getLogger(SubCatalog.class);
  protected ConfigurationHolder currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
  // Get some non-mutable configuration options once for efffeciency that are used widely in subcatalog variants
  protected int maxBeforeSplit = currentProfile.getMaxBeforeSplit();
  protected int maxSplitLevels = currentProfile.getMaxSplitLevels();
  protected int maxBeforePaginate = currentProfile.getMaxBeforePaginate();
  protected boolean useExternalIcons = currentProfile.getExternalIcons();
  protected boolean useExternalImages = currentProfile.getExternalImages();
  protected boolean includeCoversInCatalog = currentProfile.getIncludeCoversInCatalog();
  protected static String booksURI = ConfigurationManager.INSTANCE.getCurrentProfile().getUrlBooks();
  private static String securityCode = CatalogManager.INSTANCE.getSecurityCode();
  private static String securityCodeAndSeparator = securityCode + (securityCode.length() == 0 ? "" : Constants.SECURITY_SEPARATOR);
  private static CRC32 crc32;

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

  private List<Object> stuffToFilterOut;

  private List<Book> books;

  private String optimizeUrlPrefix;       // String that is used when trying to optimize URL's

  // CONSTRUCTORS

  public SubCatalog(List<Book> books) {
    this(null, books);
    if (crc32 == null)
      crc32 = new CRC32();
  }

  public SubCatalog() {
    // Do nothing special!
    if (crc32 == null)
      crc32 = new CRC32();
  }

  public SubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    setStuffToFilterOut(stuffToFilterOut);
    setBooks(books);
    if (crc32 == null)
      crc32 = new CRC32();
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
    if (securityCode.length() == 0)
      return filename;  // Do nothing if encryption not active
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
    String result = (securityCode.length() == 0 ? "" : securityCodeAndSeparator) + catalogLevel;
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
    String result = (securityCode.length() == 0 ? "" : securityCodeAndSeparator) + foldertype;

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
    String result = (securityCode.length() == 0 ? "" : securityCodeAndSeparator) + foldertype;

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
      assert (folder.substring(0, pos).equals(securityCode)) : "Program Error:  Security Code does not seem to match expected value (folder=" + folder + ")";
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
      return securityCodeAndSeparator + catalogBaseFilename;
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
    if (securityCode.length() > 0 && name.startsWith(securityCodeAndSeparator)) {
      name = name.substring(securityCodeAndSeparator.length());  // Remove the security code
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
    String result = (securityCode.length() == 0 ? "" : securityCodeAndSeparator) + type;
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
    String result = (securityCode.length() == 0 ? "" : securityCodeAndSeparator) + type;
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
   * @param outputFilename The name of the output file.
   * @param feedType       The type of file that is to be generated
   * @throws IOException Any exception would be unexpected, but it is always theoretically possible!
   */
  public void createFilesFromElement(Element feed, String outputFilename, HtmlManager.FeedType feedType) throws IOException {

    // Various asserts to help with identifying logic faults in the program!
    assert feed != null : "Programerror: Unexpected attempt to create file from non-existent feed";
    assert Helper.isNotNullOrEmpty(outputFilename) : "Program error: Attempt to create XML file for empty/null filename";
    assert !outputFilename.startsWith(CatalogManager.INSTANCE.getGenerateFolder().toString()) : "Program Error:  filename should not include catalog folder (" +
        outputFilename + ")";
    // int pos = outputFilename.indexOf(Constants.SECURITY_SEPARATOR);
    // assert outputFilename.substring(pos+1).indexOf(Constants.SECURITY_SEPARATOR) == -1 :
    //    "Program error: Two occurences of SECURITY_SEPARATOR (" + outputFilename + ")";
    // pos = outputFilename.indexOf(Constants.LEVEL_SEPARATOR);
    // assert outputFilename.substring(pos+1).indexOf(Constants.LEVEL_SEPARATOR) == -1 :
    //    "Program error: Two occurences of LEVEL_SEPARATOR (" + outputFilename + ")";


    String xmlfilename = outputFilename;
    if (!xmlfilename.endsWith(Constants.XML_EXTENSION)) {
      xmlfilename += Constants.XML_EXTENSION;
    }
    File outputFile = CatalogManager.INSTANCE.storeCatalogFile(xmlfilename);
    // Avoid creating files that already exist.
    // (if xml file exists then HTML one will as well)
    if (outputFile.exists()) {
      logger.trace("\n\n*** Attempt to generate file already done (" + outputFilename + ") - see if it can be optimised out! ***\n");
      //      if (logger.isTraceEnabled()) logger.trace("\n\n*** Attempt to generate file already done (" + outputFilename + ") - see if it can be optimised out! ***\n");
      return;
    }

    // Create as a DOM object
    // TODO  Check if there might be a cheaper way to do this not using DOM?
    String test = feed.toString();
    Document document = new Document();
    document.addContent(feed);

    // write the XML file
    // (unless the user has suppressed the OPDS catalogs)
    if (currentProfile.getGenerateOpds()) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(outputFile);
        JDOM.INSTANCE.getOutputter().output(document, fos);
      } catch (RuntimeException e) {
        logger.warn("Error writing file " + xmlfilename + "(" + e.toString() + ")");
      } finally {
        if (fos != null)
          fos.close();
      }
    }

    //  generate corresponding HTML file

    // TODO:   See if we can optimise things by avoiding generating the HTML file
    // TODO:   if the target already exists and the XML file is unchanged in this run?
    // TODO:   This would have an implication on the syn process so not a trivial change

    if (! ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtml()) {
      return;
    }
    File htmlFile = new File(HtmlManager. getHtmlFilename(outputFile.toString()));
    if (htmlFile.exists()) {
      logger.warn("Program Error?  Attempt to recreate existing HTML file '" + htmlFile + "'");
      return;
    }
    CatalogManager.INSTANCE.htmlManager.generateHtmlFromDOM(document, htmlFile, feedType);
  }

  /*
   * Decide if a Series cross-reference should be generated for this book
   *
   * Takes into account if this is the only book and the relevant setting
   */
  protected boolean isSeriesCrossreferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks() || ! currentProfile.getIncludeSerieCrossReferences()) {
      return false;
    }
    Series series = book.getSeries();
    if (series == null) {
      return false;
    }

    if (currentProfile.getSingleBookCrossReferences()
        ||  DataModel.INSTANCE.getMapOfBooksBySeries().get(series).size() > 1) {
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
    if (! currentProfile.getGenerateCrossLinks() || ! currentProfile.getIncludeAuthorCrossReferences()) {
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
    if (! currentProfile.getGenerateCrossLinks() || ! currentProfile.getIncludeTagCrossReferences()) {
      return false;
    }
    return (book.getTags() != null);
  }

  /**
   *
   * Takes into account if this is the only book and the relevant setting
   * Decide if Ratings cross-reference should be generated for this book
   */
  protected boolean isRatingCrossReferences(Book book) {
    if (! currentProfile.getGenerateCrossLinks() || ! currentProfile.getIncludeRatingCrossReferences()) {
      return false;
    }
    BookRating rating = book.getRating();
    if (rating == null) {
      return false;
    }
    if (currentProfile.getSingleBookCrossReferences()
        ||  DataModel.INSTANCE.getMapOfBooksByRating().get(rating).size() > 1) {
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
}
