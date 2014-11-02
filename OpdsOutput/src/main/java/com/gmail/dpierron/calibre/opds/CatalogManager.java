package com.gmail.dpierron.calibre.opds;

/**
 * Class to store context about the current Catalog that is being generated,
 * and to provide methods for manipulating Catalog information.
 */
import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.CustomColumnType;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.gui.CatalogCallbackInterface;
import com.gmail.dpierron.tools.Helper;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public enum CatalogManager {
  INSTANCE;
  private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CatalogManager.class);
  private static File generateFolder;
  public static BookFilter featuredBooksFilter;
  // TODO Does not seem to be used any more - remove it?
  // private static List<CachedFile> listOfFilesToCopy;
  // The list of non-image files that need to be copied from the
  // source library to the target library
  private static List<String> listOfLibraryFilesToCopy;
  // The lsit of image files that need to ce copied from the source library
  private static Map<String, CachedFile> mapOfImagesToCopy;
  // TODO:  Itimpi:  Does not seem to be needed any more?
  // private static Map<String, Book> mapOfBookByPathToCopy;
  private static Map<String, String> mapOfCatalogFolderNames;
  // List of file in catalog that are unchanged
  // TODO - Not yet used - intended to help with optimisation
  private static List<CachedFile> listOfUnchangedCatalogFiles;
  private String securityCode;
  private String initialUrl;

  public static HtmlManager htmlManager;
  public static ThumbnailManager thumbnailManager;
  public static ImageManager coverManager;
  public static CatalogCallbackInterface callback;
  public static SecurityManager securityManager;
  public static ConfigurationManager configurationManager;
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

  // public CatalogManager() {
  public void initialize() {
    // super();
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
  // }

  // public void initialize() {
    if (htmlManager == null)      htmlManager = new HtmlManager();
    if (thumbnailManager == null) thumbnailManager = ImageManager.newThumbnailManager();
    if (coverManager==null)       coverManager = ImageManager.newCoverManager();
    if (securityManager==null)    securityManager = new SecurityManager();
    if (currentProfile==null)     currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
    if (bookDateFormat==null)     bookDateFormat = currentProfile.getPublishedDateAsYear() ? new SimpleDateFormat("yyyy") : SimpleDateFormat.getDateInstance(DateFormat.LONG,new Locale(currentProfile.getLanguage()));
    if (titleDateFormat==null)    titleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, new Locale(currentProfile.getLanguage()));
    if (customCatalogsFilters==null) customCatalogsFilters = new HashMap<String, BookFilter>();
    getTagsToIgnore();
  }


  public void reset() {
    generateFolder = null;
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
    JDOM.INSTANCE.reset();
  }

  public String getSecurityCode() {
    return securityCode;
  }

  public String getInitialUr() {
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
    return listOfLibraryFilesToCopy;
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
    addFileToTheMapOfLibraryFilesToCopy(file, null);
  }

  /**
   *
   * @param file
   * @param book
   */
  void addFileToTheMapOfLibraryFilesToCopy(CachedFile file, Book book) {
    final String databasePath = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder().getAbsolutePath();
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
  void addImageFileToTheMapOfCatalogImages(String key, CachedFile file) {

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
  public Map<String,CachedFile> getMapOfCatalogImages() {
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
   * TODO  Not yet used - planned for optimisation
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
  /**
   * Get the list of tags to ignore. If it has not been done,
   * convert the list of tags to ignore from the string
   * representation to the appropriate object representation
   * as this is more effecient in later processing.
   *
   * @return
   */
  public List<Tag>  getTagsToIgnore () {
    if (tagsToIgnore == null) {
      tagsToIgnore = new LinkedList<Tag>();
      for (Tag tag : DataModel.INSTANCE.getListOfTags()) {
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
