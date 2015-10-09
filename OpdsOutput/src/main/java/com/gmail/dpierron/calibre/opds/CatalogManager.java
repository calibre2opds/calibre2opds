package com.gmail.dpierron.calibre.opds;

/**
 *              CatalogManager
 *              ~~~~~~~~~~~~~~
 * Class to store context about the current Catalog that is being generated,
 * and to provide methods for manipulating Catalog generic information.
 *
 * NOTE:  As there should only ever be one instance of this class all global
 *        variables and methods are declared static
 */
import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.gui.CatalogCallbackInterface;
import com.gmail.dpierron.tools.Helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CatalogManager {
  private final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(CatalogManager.class);

  //-----------------------------------------
  private static final boolean syncLog = true;      // Set to true to get a log of the file copy process
  //-----------------------------------------         (If set false, code is optimised out by compiler)

  public static BookFilter featuredBooksFilter;

  // TODO Does not seem to be used any more - remove it?
  // private static List<CachedFile> listOfFilesToCopy;

  // The list of non-image files that need to be copied from the
  // source library to the target library
  private static List<String> listOfLibraryFilesToCopy;

  // The list of image files that need to ce copied from the source library
  private static Map<String, CachedFile> mapOfImagesToCopy;

  // TODO:  Itimpi:  Does not seem to be needed any more?
  // private static Map<String, Book> mapOfBookByPathToCopy;
  private static Map<String, String> mapOfCatalogFolderNames;

  // List of file in catalog that are unchanged
  // TODO - Not yet used - intended to help with optimisation
  private static List<CachedFile> listOfUnchangedCatalogFiles;

  private static String securityCode;
  private static String securityCodeAndSeparator;

  private static String initialUrl;

  private static CachedFile libraryFolder; // Folder holding the Calibre library
  private static int libraryFolderPathLength;
  private static File generateFolder;      // Location where catalog is generated
  private static int generateFolderPathLength;
  private static File catalogFolder;       // Location where catalog is to be placed
  private static int catalogFolderPathLength;
  private static File targetFolder;        // Location where final catalog will be copied to (if required)
  private static int targetFolderPathLength;
  private static PrintWriter syncLogFile;  // File to be used for the Sync log


  public static HtmlManager htmlManager;
  public static ThumbnailManager thumbnailManager;
  public static ImageManager coverManager;
  public static CatalogCallbackInterface callback;
  public static SecurityManager securityManager;
  public static ConfigurationHolder currentProfile;
  // This is the date format used within the book details.
  // At the moment it is either a full date or jsut the year
  // If users ask for more flexibility the coniguration options can be re-visited.
  public static DateFormat titleDateFormat;
  // This is the date format that is to be used in the titles for the Recent Books sub-catalog section
  // It is currently a hard-coded format.   If there is user feedback suggestion that variations are
  // desireable then it could be come a configurable option
  public static DateFormat bookDateFormat;
  // Tags that the user has specified should not be included
  private static List<Tag> tagsToIgnore;
  public static Map<String, BookFilter> customCatalogsFilters;

  // Some Stats to accumulate
  // NOTE.  We make them public to avoid needing getters
  public static long statsCopyExistHits;     // Count of Files that are copied because target does not exist
  public static long statsCopyLengthHits;    // Count of files that are copied because lengths differ
  public static long statsCopyDateMisses;    // Count of files that  are not copied because source older
  public static long statsCopyCrcHits;       // Count of files that are copied because CRC different
  public static long statsCopyCrcMisses;     // Count of files copied because CRC same
  public static long statsCopyToSelf;        // Count of cases where copy to self requested
  public static long statsCopyUnchanged;     // Count of cases where copy skipped because file was not even generated
  public static long statsCopyDeleted;       // Count of files/folders deleted during copy process
  public static long statsBookUnchanged;     // We detected that the book was unchanged since last run
  public static long statsBookChanged;       // We detected that the book was changed since last run
  public static long statsCoverUnchanged;    // We detected that the cover was unchanged since last run
  public static long statsCoverChanged;      // We detected that the cover was changed since last run


  // public CatalogManager() {
  public static void initialize() {
    // super();
    // Avoid superflous settings of static object!

    securityCode = ConfigurationManager.getCurrentProfile().getSecurityCode();
    if (Helper.isNullOrEmpty(securityCode)) {
      Random generator = new Random(System.currentTimeMillis());
      securityCode = Integer.toHexString(generator.nextInt());
      ConfigurationManager.getCurrentProfile().setSecurityCode(securityCode);
    }
    if (! ConfigurationManager.getCurrentProfile().getCryptFilenames()) {
      securityCode = "";
    }
    initialUrl = securityCode;
    if (securityCode.length() != 0) initialUrl += Constants.SECURITY_SEPARATOR;
    initialUrl += Constants.INITIAL_URL;
    resetStats();

    // TODO  Decide if these should be conditional or just done every time!
    if (htmlManager == null)      htmlManager = new HtmlManager();
    if (thumbnailManager == null) thumbnailManager = ImageManager.newThumbnailManager();
    if (coverManager==null)       coverManager = ImageManager.newCoverManager();
    if (securityManager==null)    securityManager = new SecurityManager();
    if (currentProfile==null)     currentProfile = ConfigurationManager.getCurrentProfile();
    if (bookDateFormat==null)     bookDateFormat = currentProfile.getPublishedDateAsYear() ? new SimpleDateFormat("yyyy") : SimpleDateFormat.getDateInstance(DateFormat.LONG,currentProfile.getLanguage());
    if (titleDateFormat==null)    titleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, currentProfile.getLanguage());
    if (customCatalogsFilters==null) customCatalogsFilters = new HashMap<String, BookFilter>();

  }


  public static void reset() {
    libraryFolder = null;
    generateFolder = null;
    targetFolder = null;
    catalogFolder = null;
    syncLogFile = null;
    featuredBooksFilter = null;
    // listOfFilesToCopy = new LinkedList<CachedFile>();
    listOfLibraryFilesToCopy = new LinkedList<String>();
    // mapOfBookByPathToCopy = new HashMap<String, Book>();
    mapOfCatalogFolderNames = new HashMap<String, String>();
    // bookEntriesFiles = new LinkedList<File>();
    bookDetailsCustomColumns = null;
    listOfUnchangedCatalogFiles = new LinkedList<CachedFile>();
    mapOfImagesToCopy = new HashMap<String, CachedFile>();
    htmlManager = null;
    thumbnailManager = null;
    coverManager = null;
    securityManager = null;
    currentProfile = null;
    titleDateFormat = null;
    bookDateFormat = null;
    tagsToIgnore = null;
    customCatalogsFilters = null;
    JDOMManager.reset();
    securityCode = "";
    securityCodeAndSeparator = null;
    resetStats();
  }

  private static void resetStats() {
    statsCopyExistHits = statsCopyLengthHits
        = statsCopyCrcHits
        = statsCopyCrcMisses
        = statsCopyDateMisses
        = statsCopyUnchanged
        = statsBookUnchanged
        = statsBookChanged
        = statsCoverUnchanged
        = statsCoverChanged = 0;

  }
  public static String getSecurityCode() {
    if (securityCode == null) {
      securityCode = CatalogManager.getSecurityCode();
    }
    return securityCode;
  }

  public static String getSecurityCodeAndSeparator() {
    if (securityCodeAndSeparator == null) {
      securityCodeAndSeparator = securityCode + (securityCode.length() == 0 ? "" : Constants.SECURITY_SEPARATOR);
    }
    return securityCodeAndSeparator;
  }

  public static String getInitialUr() {
    return initialUrl;
  }
  /**
   * Get the current catalog folder
   * @return
   */

  /**
   * We always generate into a temporary folder
   *  We need to create one if we do not already have one setup
   *
   * @return
   */
  public static File getGenerateFolder() {

    if (generateFolder == null) try {
      //  Initialise area for generating the catalog files
      File temp = File.createTempFile("calibre2opds", "");
      String tempPath = temp.getAbsolutePath();
      temp.delete();  // Remove file just created as we are going to create a folder there instead
      // See if user has specified a specific location for the TEMP folder
      String tempDirectory = System.getenv("CALIBRE2OPDS_TEMP");
      if (Helper.isNotNullOrEmpty(tempDirectory)) {
        tempPath = tempDirectory + Constants.FOLDER_SEPARATOR + temp.getName();
      }
      generateFolder = new File(tempPath);
      if (logger.isTraceEnabled())
        logger.trace("generateFolder set to " + generateFolder);
      generateFolder.mkdir();
      generateFolder.deleteOnExit();
      generateFolderPathLength = getGenerateFolder().getAbsolutePath().length();
      logger.info("Temporary Files folder: " + generateFolder.getAbsolutePath());
    } catch (IOException e) {
      // Do not believe this is possible
      // If it happens we need to abort the run!
      logger.error("Unable to create temp folder");
      System.exit(-8);
    }
    return generateFolder;
  }

  public static int getGenerateFolderpathLength() {
    assert generateFolderPathLength != 0;
    return generateFolderPathLength;
  }

  /**
   * Get the library folder associated with the current catalog generation
   *
   * @return
   */
  public static CachedFile getLibraryFolder() {
  if (libraryFolder == null) {
    libraryFolder = CachedFileManager.addCachedFile(currentProfile.getDatabaseFolder());
    libraryFolderPathLength = libraryFolder.getAbsolutePath().length();
  }
  return libraryFolder;
  }

  public static int getLibraryFolderPathLength() {
    assert libraryFolderPathLength != 0;
    return libraryFolderPathLength;
  }

  /**
   * Get the target folder for the current catalog generation
   *
   * @return
   */
  public static File getTargetFolder() {
    if (targetFolder == null) {
      targetFolder = currentProfile.getTargetFolder();
      targetFolderPathLength =  targetFolder == null ? 0 : targetFolder.getAbsolutePath().length();
    }
    return targetFolder;
  }

  public static int getTargetFolderPathLength() {
    assert targetFolderPathLength != 0;
    return targetFolderPathLength;
  }
  /**
   * Only used in Nook mode!
   */
  public static void setTargetFolder(File f) {
    assert currentProfile.getDeviceMode().equals(DeviceMode.Nook);
    targetFolder = f;
    targetFolderPathLength = targetFolder.getAbsolutePath().length();
  }

  public static boolean getSyncLog() {
    return syncLog;
  }

  private static PrintWriter getSyncLogFile() {
    if (! syncLog) return null;
    if (syncLogFile == null) try {
      syncLogFile = new PrintWriter(ConfigurationManager.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
    } catch (IOException e) {
      // This should not happen
      logger.error("Unable to create SyncLog File");
      System.exit(-7);
    }
    return syncLogFile;
  }

  /**
   * Print a line if sync loggining is active
   * Accepts formatting parameters like a printf method
   *
   * @param s     formatting string/fixed text
   * @param args  (optionla) arguments to use with formatting
   */
  public static void syncLogPrintln(String s, Object ... args ) {
    if (syncLog) {
      getSyncLogFile().print(String.format(s,args));
      getSyncLogFile().println();
    }
    return;
  }

  public static void syncLogClose() {
    if (syncLogFile != null) {
      syncLogFile.close();
    }
  }
  /**
   * Get the name of the catalog folder.
   * It will take into account the current mode if relevant
   * @return
   */
  public static String getCatalogFolderName() {
    if (ConfigurationManager.getCurrentProfile().getDeviceMode() == DeviceMode.Nook)
      return Constants.NOOK_CATALOG_FOLDERNAME;
    else
      return  ConfigurationManager.getCurrentProfile().getCatalogFolderName();
  }

  /**
   * Get the location where the generated catalog must be copied to
   *
   * @return
   */
  public static File getCatalogFolder() {
    return catalogFolder;
  }
  /**
   * Set the location where the generated catalog must be copied to
   *
   * @param folder
   */
  public static void setCatalogFolder(File folder)
  {
    catalogFolder = folder;
    catalogFolderPathLength = catalogFolder.getAbsolutePath().length();
  }

  public static int getCatalogFolderPathLength() {
    assert catalogFolderPathLength != 0;
    return catalogFolderPathLength;
  }
  /**
   *
   * @return
   */
  public static List<String> getListOfFilesPathsToCopy() {
    return listOfLibraryFilesToCopy;
  }

  /**
   *
   * @param pathToCopy
   * @return
   */

 /*
  public static Book getBookByPathToCopy(String pathToCopy) {
    return mapOfBookByPathToCopy.get(pathToCopy);
  }
  */

  /**
   *
   * @param file
   */
  public static void addFileToTheMapOfFilesToCopy(CachedFile file) {
    addFileToTheMapOfLibraryFilesToCopy(file, null);
  }

  /**
   *
   * @param file
   * @param book
   */
  public static void addFileToTheMapOfLibraryFilesToCopy(CachedFile file, Book book) {
    final String databasePath = ConfigurationManager.getCurrentProfile().getDatabaseFolder().getAbsolutePath();
    final int databasePathLength = databasePath.length() + 1;

    if (file == null)
      return;

    String filePath = file.getAbsolutePath();

    // Lets not copy files outside the database folder
    if (!filePath.startsWith(databasePath) ) {
      if (filePath.endsWith(Constants.DEFAULT_THUMBNAIL_FILENAME)
      ||  filePath.endsWith(Constants.CALIBRE_COVER_FILENAME)) {
        // Expected to happen when cover image missing
        return;
      }
      logger.warn("addFileToTheMapOfLibraryFilesToCopy: adding file not in library area! (" + filePath + ")");
      return;
    }
    String relativePath = filePath.substring(databasePathLength);
    if (! listOfLibraryFilesToCopy.contains(relativePath)) {
      listOfLibraryFilesToCopy.add(relativePath);
    }
    // TODO  Work out if this following line is ever needed
    // TODO  If not we can eliminate the version that passed in book as a parameter
    // if (book != null) {
    //   assert ! mapOfBooksByPathToCopy.contains(relativePath);
    //   mapOfBookByPathToCopy.put(relativePath, book);
    // }
    // TODO It appear we no longer need the following list either.
    // TODO Perhaps it is necessary to validate what is was intended for?
    // if (listOfFilesToCopy.contains(file)) {
    //   logger.trace("addFileToTheMapOfLibraryFilesToCopy: listOfFilesToCopy alread contains file " + file);
    // } else {
    //    listOfFilesToCopy.add(file);
    // }
  }

  /**
   * Add a file to the map of image files that are to be copied
   * to the catalog (assuming this option is even set!)
   */
  public static void addImageFileToTheMapOfCatalogImages(String key, CachedFile file) {

    assert file != null : "Program Error: attempt to add 'null' file to image map";
    assert (file.getName().equals("c2o_thumbnail.jpg")
         || file.getName().equals("c2o_resizedcover.jpg")
         || file.getName().equals(Constants.CALIBRE_COVER_FILENAME)):
          "Program Error: Unexpected name '" + file.getName() + "' when trying to add image to map";
    if (! mapOfImagesToCopy.containsKey(key)) {
      mapOfImagesToCopy.put(key, file);
    }
  }

  /**
   *
   * @return
   */
  public static Map<String,CachedFile> getMapOfCatalogImages() {
    return mapOfImagesToCopy;
  }

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
  public static String getCatalogFileUrl(String catalogFileName, Boolean inSubDir) {
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
   public static String getFolderName(String pCatalogFileName) {
    if (Helper.isNullOrEmpty(pCatalogFileName))
      return "";

    int pos = pCatalogFileName.indexOf(Constants.FOLDER_SEPARATOR);
    return (pos == -1) ? pCatalogFileName : pCatalogFileName.substring(0,pos);
  }

  /**
   * Set up an entry for the given file in the catalog.
   * Checks to see if the file is already present and if not adds it
   *
   * @param catalogFileName   The name of the file to be stored.  Includes folder if relevant
   * @return                  File object corresponding to the given path
   */
  public static File storeCatalogFile(String catalogFileName) {
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
  public static List<CustomColumnType> getBookDetailsCustomColumns() {
    if (bookDetailsCustomColumns == null)  {
      List<CustomColumnType> types = DataModel.getListOfCustomColumnTypes();
      if (types == null) {
        logger.warn("getBookDetailsCustomColumns: No custom columns read from database.");
        return null;
      }
      bookDetailsCustomColumns = new LinkedList<CustomColumnType>();
      for (String customColumnLabel : ConfigurationManager.getCurrentProfile().getTokenizedBookDetailsCustomColumns()) {
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
   * TODO  Not yet used - planned for optimisation
   * Track the list of files that are part of the catalog,
   * but are are unchanged since the last run
   * @param f
   */
  public static void addUnchangedFileToList (CachedFile f) {
    if (! listOfUnchangedCatalogFiles.contains(f)) {
      listOfUnchangedCatalogFiles.add(f);
    }
  }
  /*
  Make these properties public to avoid the need for simpe get/set routines that do nothing else!

  public static BookFilter getFeaturedBooksFilter() {
    return featuredBooksFilter;
  }

  public static void setFeaturedBooksFilter(BookFilter featuredBooksFilter) {
    this.featuredBooksFilter = featuredBooksFilter;
  }

  public static List<Composite<String, String>> getCustomCatalogs() {
    return customCatalogs;
  }

  public static void setCustomCatalogs (List<Composite<String, String>> pcustomCatalogs) {
    customCatalogs = pcustomCatalogs;
  }

  public static Map<String, BookFilter> getCustomCatalogFilters () {
    return customCatalogsFilters;
  }

  public  static void setCustomCatalogsFilter (Map<String, BookFilter> pcustomCatalogsFilters) {
    customCatalogsFilters = pcustomCatalogsFilters;
  }
  */
  /**
   * Get the list of tags to ignore. If it has not been done,
   * convert the list of tags to ignore from the string
   * representation to the appropriate object representation
   * as this is more effecient in later processing.
   *
   * @return
   */
  public static List<Tag>  getTagsToIgnore () {
    if (tagsToIgnore == null) {
      tagsToIgnore = new LinkedList<Tag>();
      for (Tag tag : DataModel.getListOfTags()) {
        List<String> regextagsToIgnore = currentProfile.getRegExTagsToIgnore();
        for (String regexTag : regextagsToIgnore) {
          if (tag.getName().toUpperCase().matches("^" + regexTag)) {
            if (! tagsToIgnore.contains(tag)) {
              tagsToIgnore.add(tag);
            }
          }
        }
      }
    }
    return tagsToIgnore;
  }

  /**
   * Determine if images need to be resized
   *
   * @param filename
   * @param TargetSize
   * @return
   */
  private static boolean isImagesResized(String filename, int TargetSize) {
    File sizeFile = new File(filename);
    return false;
  }

  /**
   * Check if specified file different in generate and target folders.
   *
   * Ensures that there are entries in the cache for these files.
   *
   * The following checks are made as part of determining if the
   * files are iendtical:
   * - The file must exist in both locations
   * = The sizes must be the same
   * - The CRC's must match
   *
   * If these criteria are saitisfied then the 'Changed' attribute is
   * cleared on the cache entry for both sourcc and target.
   *
   * @param sourcefile
   * @return
   */
  public static boolean isSourceFileSameAsTargetFile(CachedFile sourcefile, CachedFile targetfile) {

    // Assumptions we should be able to make
    assert sourcefile != null;
    assert sourcefile.exists() == true;
    assert targetfile != null;

    // If target does not exist then they cannot be the same
    // (even if the generate file appears unchanged
    if (! targetfile.exists()) {
      assert targetfile.isChanged() == true;
      return false;
    }

    // See if we already know they match
    if (! sourcefile.isChanged()) {
      targetfile.setChanged(false);
    }
    if (! targetfile.isChanged()) {
      return true;
    }

    // Must be different if length changed
    if (sourcefile.length() != targetfile.length()) {
      assert targetfile.isChanged() == true;
      return false;
    }

    // If length appears identical need to check CRC
    targetfile.setChanged(sourcefile.getCrc() != targetfile.getCrc());
    return (targetfile.isChanged() == false);
  }

  /**
   *
   * Check if specified file different in generate and catalog folders.
   * The filename rovided should be the path relative to the folders
   * in question and not include the path itself.
   *
   * Uses isGenerateFileSameAsTargetFile() supplying catalog folder as target.
   *
   * @param filename
   * @return
   */
  public static boolean isGenerateFileSameAsCatalogFile(String filename) {
    // Create absolute path entries and ensure theya re in the cache
    CachedFile generateFile = CachedFileManager.addCachedFile(CatalogManager.getGenerateFolder().getAbsolutePath() + File.separator + filename);
    CachedFile catalogFile = CachedFileManager.addCachedFile(CatalogManager.getCatalogFolder().getAbsolutePath() + File.separator + filename);
    assert generateFile != null;
    assert catalogFile != null;
    return isSourceFileSameAsTargetFile(generateFile, catalogFile);
  }

    // Collect some information about the RAM usage while running
  private static String ramPoolMeasurement[] = new String[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static String ramPoolName[] = new String[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static String ramPoolType[] = new String[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static long ramPoolCommitted[] = new long[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static long ramPoolInit[] = new long[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static long ramPoolMax[] = new long[ManagementFactory.getMemoryPoolMXBeans().size()];
  private static long ramPoolUsed[] = new long[ManagementFactory.getMemoryPoolMXBeans().size()];

  /**
   * Provide an end-of-run summary of RAM usage
   */
  public static void reportRamUsage(String measurementPoint) {
    logger.info("");
    logger.info("Java VM RAM Usage " + measurementPoint);
    logger.info(String.format("   %-20s %-15s%10s%10s%10s%10s",
        "NAME",
        "TYPE",
        "COMMITTED",
        "INIT",
        "MAX",
        "USED"));
    for (int i = 0 ; i < ramPoolType.length; i++) {
      logger.info(String.format("   %-20s %-15s%10d MB%7d MB%7d MB%7d MB",
          ramPoolName[i],
          ramPoolType[i],
          ramPoolCommitted[i] / (2<<20),
          ramPoolInit[i] / (2<<20),
          ramPoolMax[i] / (2<<20),
          ramPoolUsed[i] / (2<<20)));
    }
    logger.info("");
  }
  /**
   * Record the RAM usage at the specified measuring point.
   *
   * If in a debug mode we also output to log file, otherwise
   * just accumlate results.
   */
  public static void recordRamUsage(String measurementPoint) {
    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    if (ramPoolName.length != pools.size()) {
      //  This is safety check - not sure it can really happen
      logger.error("Unexpected change in number of RAM areas (was " + ramPoolName.length + ", now " + pools.size());
    } else {
      for (int i = 0; i < pools.size() ; i++) {
        MemoryPoolMXBean pool = pools.get(i);
        if (ramPoolName[i] == null)
          ramPoolName[i] = pool.getName();
        if (ramPoolType[i] == null)
          ramPoolType[i] = pool.getType().toString();
        if (! ramPoolName[i].equals(pool.getName())) {
          logger.error("Mismatch on RAM Pool " + i + " name (expected " + ramPoolName[i] + ", got " + pool.getName() + "0" );
        } else {
          MemoryUsage usage = pool.getUsage();
          if (usage.getCommitted() > ramPoolCommitted[i])
            ramPoolCommitted[i] = usage.getCommitted();
          if (usage.getInit() > ramPoolInit[i])
            ramPoolInit[i] = usage.getInit();
          if (usage.getMax() > ramPoolMax[i])
            ramPoolMax[i] = usage.getMax();
          if (usage.getUsed() > ramPoolUsed[i])
            ramPoolUsed[i] = usage.getUsed();
          if (logger.isDebugEnabled()) {
            if (i == 0) {
              logger.info("");
              logger.info("Java VM RAM Usage " + measurementPoint);
              logger.info(String.format("   %-20s %-15s%10s%10s%10s%10s", "NAME", "TYPE", "COMMITTED", "INIT", "MAX", "USED"));
            }
            logger.info(String.format("   %-20s %-15s%10d MB%7d MB%7d MB%7d MB",
                                      pool.getName(),
                                      pool.getType().toString(),
                                      usage.getCommitted() / (2 << 20),
                                      usage.getInit() / (2 << 20),
                                      usage.getMax() / (2 << 20),
                                      usage.getUsed() / (2 << 20)));
            logger.info("");
          }
        }
      }
    }
  }
}
