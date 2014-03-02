package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.database.Database;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.CustomColumnType;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;

import javax.print.DocFlavor;
import java.io.File;
import java.util.*;
import java.util.zip.CRC32;

public class CatalogManager {
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CatalogManager.class);
  private static File generateFolder;
  public static BookFilter featuredBooksFilter;
  public static List<Composite<String, String>> customCatalogs;
  public static Map<String, BookFilter> customCatalogsFilters;
  // TODO  Itimpi:  Does not seem to be needed any more1
  /// private static List<CachedFile> listOfFilesToCopy;
  // The list of files that need to be copied from the source
  // library to the target library.
  private static List<String> listOfFilesPathsToCopy;
  // TODO:  Itimpi:  Does not seem to be needed any more?
  // private static Map<String, Book> mapOfBookByPathToCopy;
  private static Map<String, String> mapOfCatalogFolderNames;
  // List of file in catalog that are unchanged
  private static List<CachedFile> listOfUnchangedCatalogFiles;
  // List of books that have already been generated
  // used to track if has already been done before!
  private static String securityCode;
  private static String initialUrl;


  public CatalogManager() {
    super();
    // Avoid superflous settings of static object!
    securityCode = ConfigurationManager.INSTANCE.getCurrentProfile().getSecurityCode();
    if (Helper.isNullOrEmpty(securityCode)) {
      Random generator = new Random(System.currentTimeMillis());
      securityCode = Integer.toHexString(generator.nextInt());
      ConfigurationManager.INSTANCE.getCurrentProfile().setSecurityCode(securityCode);
    }
    if (! ConfigurationManager.INSTANCE.getCurrentProfile().getCryptFilenames()) {
      securityCode = "";
    }
    initialUrl = securityCode;
    if (securityCode.length() != 0) initialUrl += Constants.SECURITY_SEPARATOR;
    initialUrl += Constants.INITIAL_URL;
  }

  public void reset() {
    generateFolder = null;
    featuredBooksFilter = null;
    customCatalogs = null;
    customCatalogsFilters = null;
    /// listOfFilesToCopy = new LinkedList<CachedFile>();
    listOfFilesPathsToCopy = new LinkedList<String>();
    // mapOfBookByPathToCopy = new HashMap<String, Book>();
    mapOfCatalogFolderNames = new HashMap<String, String>();
    // bookEntriesFiles = new LinkedList<File>();
    bookDetailsCustomColumns = null;
    listOfUnchangedCatalogFiles = new LinkedList<CachedFile>();
  }

  public static String getSecurityCode() {
    return securityCode;
  }

  public static String getInitialUr() {
    return initialUrl;
  }
  /**
   * Get the current catalog folder
   * @return
   */
  public File getGenerateFolder() {
    return generateFolder;
  }

  /**
   * Set the catalog folder given the parth to the parent
   * The name takes into account the configuration settings and mode
   * This is really just the path to where the temporary files are generated
   * @param parentfolder
   */
  public void setGenerateFolder(File parentfolder) {
//    generateFolder = new File(parentfolder, getCatalogFolderName());
    generateFolder = parentfolder;
    if (!generateFolder.exists()) {
      generateFolder.mkdirs();
    }
  }

  /**
   * Get the name of the catalog folder.
   * It will take into account the current mode if relevant
   * @return
   */
  public String getCatalogFolderName() {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode() == DeviceMode.Nook)
      return Constants.NOOK_CATALOG_FOLDERNAME;
    else
      return  ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogFolderName();
  }

  /**
   *
   * @return
   */
  public List<String> getListOfFilesPathsToCopy() {
    return listOfFilesPathsToCopy;
  }

  /**
   *
   * @param pathToCopy
   * @return
   */

 /*
  public Book getBookByPathToCopy(String pathToCopy) {
    return mapOfBookByPathToCopy.get(pathToCopy);
  }
  */

  /**
   *
   * @param file
   */
  void addFileToTheMapOfFilesToCopy(CachedFile file) {
    addFileToTheMapOfFilesToCopy(file, null);
  }

  /**
   *
   * @param file
   * @param book
   */
  void addFileToTheMapOfFilesToCopy(CachedFile file, Book book) {
    final String databasePath = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder().getAbsolutePath();
    final int databasePathLength = databasePath.length() + 1;

    if (file == null)
      return;

    // We should never try and add the same file twice (I think!)
    // assert ! listOfFilesPathsToCopy.contains(file);

    String filePath = file.getAbsolutePath();

    // TODO Might want to rework safety check into asserts!
/*
    if (!filePath.startsWith(databasePath)
    && (!filePath.endsWith(Constants.DEFAULT_RESIZED_COVER_FILENAME)))  {
      logger.trace("addFileToTheMapOfFilesToCopy: adding file not in library area!");
      return; // let's not copy files outside the database folder
    }
*/
    String relativePath = filePath.substring(databasePathLength);
    if (! listOfFilesPathsToCopy.contains(relativePath))
      listOfFilesPathsToCopy.add(relativePath);
    // mapOfBookByPathToCopy.put(relativePath, book);
    /// listOfFilesToCopy.add(file);
  }
  /**
   * Add a file to the map of image files that are to be copied
   * to the catalog (assuming this option is even set!)
   */
  /*
  void addImageFileToTheMapOfCatalogImages(Book book, CachedFile file) {

    assert file != null : "Program Error: attempt to add 'null' file to image map";
    assert (file.getName().equals(Constants.THUMBNAIL_FILENAME)
         || file.getName().equals(Constants.RESIZEDCOVER_FILENAME)
         || file.getName().equals(Constants.CALIBRE_COVER_FILENAME)):
          "Program Error: Unexpected name '" + file.getName() + "' when trying to add image to map";
    String key = book.getId().toString() + Constants.TYPE_SEPARATOR + file.getName();
    assert ! mapOfImagesToCopy.containsKey(key) : "Program Error: Already added image file " + key;

    mapOfImagesToCopy.put(key, file);
  }
  */

  /**
   * Get the URL that is used to reference a particular file.
   * If not alreaady present then added it to the map of files
   * that are currently in the catalog.
   *
   * It will have the appropriate suffix added to ensure that it
   * correctly references the current or parent folder.
   *
   * @param catalogFileName

   * @return
   */
  public String getCatalogFileUrl(String catalogFileName, Boolean inSubDir) {
    assert Helper.isNotNullOrEmpty(catalogFileName);
    int pos =  catalogFileName.indexOf(Constants.FOLDER_SEPARATOR);
    String catalogFolderName = mapOfCatalogFolderNames.get(catalogFileName);
    if (Helper.isNullOrEmpty(catalogFolderName)) {
      storeCatalogFile(catalogFileName);
      // catalogFolderName = mapOfCatalogFolderNames.get(catalogFileName);
      catalogFolderName = pos == -1 ? "" : catalogFileName.substring(0,pos);
    }

    return (inSubDir  ? Constants.PARENT_PATH_PREFIX : Constants.CURRENT_PATH_PREFIX)
                        + FeedHelper.urlEncode(catalogFolderName)
                        + (pos == - 1 ? "" : Constants.FOLDER_SEPARATOR)
                        + FeedHelper.urlEncode(catalogFileName.substring(pos + 1));
  }


  /**
   * Get the Folder that a particular catalog file belongs in
   *
   * @param pCatalogFileName
   * @return
   */
  String getFolderName(String pCatalogFileName) {
    if (Helper.isNullOrEmpty(pCatalogFileName))
      return "";

    int pos = pCatalogFileName.indexOf(Constants.FOLDER_SEPARATOR);
    return (pos == -1) ? pCatalogFileName : pCatalogFileName.substring(0,pos);
  }

  /**
   * Set up an entry for the given file in the catalog.
   * Checks to see if the file is already present and if not adds it
   *
   * @param catalogFileName   The name of the file to be stored.  Includes folde if relevant
   * @return                  File object corresponding to the given path
   */
  public File storeCatalogFile(String catalogFileName) {
    File folder = null;
    String folderName;
    int pos = catalogFileName.indexOf(Constants.FOLDER_SEPARATOR);       // Look for catalog name terminator being present
    if (pos != -1 ) {
      // truncate name supplied to use to only be folder part
      folderName = catalogFileName.substring(0, pos);
      folder = new File(getGenerateFolder(), folderName);
    } else {
      folderName = "";
      folder = new File(getGenerateFolder(), folderName);
    }
    if (!folder.exists())  {
      folder.mkdirs();
    }
    mapOfCatalogFolderNames.put(catalogFileName, folderName);
    File result = new File(getGenerateFolder(), catalogFileName);
//    File result = new File(catalogFileName);
    return result;
  }

  private static List<CustomColumnType> bookDetailsCustomColumns = null;

  /**
   * Get the list of curom columns that are to be included in Book Details.
   * If we do not recognize any of them they are ignored as an earlier
   * validation tst will have checked this with the user.
   *
   * @return
   */
  public List<CustomColumnType> getBookDetailsCustomColumns() {
    if (bookDetailsCustomColumns == null)  {
      List<CustomColumnType> types = DataModel.INSTANCE.getListOfCustomColumnTypes();
      if (types == null) {
        logger.warn("getBookDetailsCustomColumns: No custom columns read from database.");
        return null;
      }
      bookDetailsCustomColumns = new LinkedList<CustomColumnType>();
      for (String customColumnLabel : ConfigurationManager.INSTANCE.getCurrentProfile().getTokenizedBookDetailsCustomColumns()) {
        if (customColumnLabel.startsWith("#")) {
          customColumnLabel = customColumnLabel.substring(1);
        }
        for (CustomColumnType type : types) {
          if (type.getLabel().toUpperCase().equals(customColumnLabel.toUpperCase())) {
            bookDetailsCustomColumns.add(type);
          }
        }
      }
    }
    return bookDetailsCustomColumns;
  }

  /**
   * Track the list of files that are part of the catalog,
   * but are are unchanged since the last run
   * @param f
   */
  public void addUnchangedFileToList (CachedFile f) {
    if (! listOfUnchangedCatalogFiles.contains(f)) {
      listOfUnchangedCatalogFiles.add(f);
    }
  }
  /*
  Make these properties public to avoid the need for simpe get/set routines that do nothing else!

  public BookFilter getFeaturedBooksFilter() {
    return featuredBooksFilter;
  }

  public void setFeaturedBooksFilter(BookFilter featuredBooksFilter) {
    this.featuredBooksFilter = featuredBooksFilter;
  }

  public List<Composite<String, String>> getCustomCatalogs() {
    return customCatalogs;
  }

  public void setCustomCatalogs (List<Composite<String, String>> pcustomCatalogs) {
    customCatalogs = pcustomCatalogs;
  }

  public  Map<String, BookFilter> getCustomCatalogFilters () {
    return customCatalogsFilters;
  }

  public void setCustomCatalogsFilter (Map<String, BookFilter> pcustomCatalogsFilters) {
    customCatalogsFilters = pcustomCatalogsFilters;
  }
  */
}
