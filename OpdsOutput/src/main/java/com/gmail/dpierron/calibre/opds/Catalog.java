package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.database.Database;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.CustomColumnType;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.EBookFile;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchNotFoundException;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.IndexManager;
import com.gmail.dpierron.calibre.opf.OpfOutput;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// import com.sun.corba.se.impl.orbutil.concurrent.Sync;


public class Catalog {

  private static final Logger logger = Logger.getLogger(Catalog.class);
  // Some Copying stats to accumulate
  private long copyExistHits;     // Count of Files that are copied because target does not exist
  private long copyLengthHits;    // Count of files that are copied because lengths differ
  private long copyDateMisses;    // Count of files that  are not copied because source older
  private long copyCrcHits;       // Count of files that are copied because CRC different
  private long copyCrcMisses;     // Count of files copied because CRC same
  private long copyCrcUnchecked;  // Count of files copied because CRC check suppressed
  private long copyToSelf;        // Count of cases where copy to self requested
  private long copyDeleted;       // Count of files/folders deleted during copy process

  // Values read once from configuration that are used repeatedly
  private ConfigurationHolder currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
  private boolean checkCRC = currentProfile.getMinimizeChangedFiles();

  private CatalogCallbackInterface callback;        // GUI handling routine

  //----------------------------------------------
  private final boolean syncFilesDetail = false; // Set to true to get more details on syncFiles process
  //----------------------------------------------     (If set false, code is optimised out by compiler)
  //-----------------------------------------
  private final boolean syncLog = true;      // Set to true to get a log of the file copy process
  //-----------------------------------------         (If set false, code is optimised out by compiler)

  private PrintWriter syncLogFile;           // File to be used for the Sync log

  // The following are used to simplify code and to avoid continually referring to the profile
  private File generateFolder = null;     // Location where catalog is generated
  private File targetFolder = null;       // Location where final catalog will be copied to (if reuired)
  // In Nook mode this should be the same as the generateFolder
  private File libraryFolder = null;      // Folder holding the Calibre library
  private String catalogFolderName = null;//Name of the catalog folder (not including path)

  /**
   * Constructor setting callback interface for GUI
   *
   * @param callback
   */
  public Catalog(CatalogCallbackInterface callback) {
    super();
    this.callback = callback;
    CatalogManager.INSTANCE.callback = callback;
  }

  /**
   * The ZIP routines were moved here from the Helper module as the
   * easiest way to give access to the callback interface for
   * providing progress information.
   *
   * @param inFolder
   * @param outZipFile
   * @throws IOException
   */
  public void recursivelyZipFiles(File inFolder, File outZipFile) throws IOException {
    recursivelyZipFiles(null, false, inFolder, outZipFile, false);
  }

  public void recursivelyZipFiles(File inFolder,
      boolean includeNameOfOriginalFolder,
      File outZipFile,
      boolean omitXmlFiles)
      throws IOException {
    recursivelyZipFiles(null, includeNameOfOriginalFolder, inFolder, outZipFile, omitXmlFiles);
  }

  public void recursivelyZipFiles(final String extension,
      boolean includeNameOfOriginalFolder,
      File inFolder,
      File outZipFile,
      boolean omitXmlFiles)
      throws IOException {
    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outZipFile)));
    String relativePath = "";
    if (includeNameOfOriginalFolder)
      relativePath = inFolder.getName();
    try {
      recursivelyZipFiles(extension, relativePath, inFolder, zipOutputStream, omitXmlFiles);
    } finally {
      zipOutputStream.close();
    }
  }

  private void recursivelyZipFiles(final String extension,
      String currentRelativePath,
      File currentDir,
      ZipOutputStream zipOutputStream,
      final boolean omitXmlFiles)
      throws IOException {
    String[] files = currentDir.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        if (extension == null
            && (f.isFile() && omitXmlFiles && (! name.toUpperCase().endsWith(".XML")))) {
          return true;
        } else {
          if (f.isDirectory()
              || (extension != null && name.toUpperCase().endsWith(extension.toUpperCase()))
              || (!omitXmlFiles)
              // We bonly accept XML files if omitXmlFiles setting is not set
              || (omitXmlFiles && (!name.toUpperCase().endsWith(".XML")))) {
            return true;
          }
          // We need to increment progress for XML files we are ignoring
          if (f.isFile() && (name.toUpperCase().endsWith(".XML"))) {
            callback.incStepProgressIndicatorPosition();
          }
          return false;
        }
      }

    });

    for (String filename : files) {
      File f = new File(currentDir, filename);
      String fileRelativePath = currentRelativePath + (Helper.isNullOrEmpty(currentRelativePath) ? "" : File.separator) + filename;
      if (f.isDirectory()) {
        callback.showMessage("Folder: " + f.getName());
        recursivelyZipFiles(extension, fileRelativePath, f, zipOutputStream, omitXmlFiles);
      } else {
        BufferedInputStream in = null;
        byte[] data = new byte[1024];
        in = new BufferedInputStream(new FileInputStream(f), 1000);
        zipOutputStream.putNextEntry(new ZipEntry(fileRelativePath));
        int count;
        while ((count = in.read(data, 0, data.length)) != -1) {
          zipOutputStream.write(data, 0, count);
        }
        zipOutputStream.closeEntry();
        callback.incStepProgressIndicatorPosition();
      }
    }
  }

  /**
   * Sync Files between source and target
   * <p/>
   * Routine that handles synchronisation of files between source and target
   * It also handles deleting unwanted files/folders at the target location
   *
   * @param src
   * @param dst
   * @throws IOException
   */
  private void syncFiles(CachedFile src, CachedFile dst) throws IOException {


    if (logger.isTraceEnabled())
      logger.trace("syncFiles (" + src + "," + dst + ")");

    callback.incStepProgressIndicatorPosition();

    // Sanity check on parameters
    assert (src != null) & (dst != null) : "Unexpected parameter to copy: src=" + src + ", dst=" +dst;

    // Sanity check - we cannot copy a non-existent file
    // ITIMPI:  Would it better to throw an exception to ensure we fix this?
    //          However maybe it a valid check against file system having changed during run
    if (!src.exists()) {
      // ITIMPI:
      // The following code is to get around the fact that if a user renames a book in
      // Calibre while a generate is running then the book will be missing when we get
      // around to trying to copy it.   We will silently ignore such cases although a wrning
      // message is added to the log file.
      //
      // If the file that is missing is a .html or a .xml file then this is more serious as
      // it suggest a file has gone missing that we created earlier in the generation process.
      //
      // There have also been some suggestions tha tthere might be a mismatch between the real
      // state of the file and the cached state.  If so this is likely to be a program logic
      // fault so we want to get details to help with diagnostics

      File f = new File(src.getAbsolutePath());
      if (f.exists() != false) {
        logger.error("syncFiles: Incorrect caching of exists()=false status for file: " + src.getAbsolutePath());
        return;
      }
      // If we get here at least the cached state agrees with the real state!
      // If it is missing .xml or .html file then this is still a significant issue
      if (src.getName().endsWith(Constants.XML_EXTENSION)
      || src.getName().endsWith(Constants.HTML_EXTENSION)) {
        logger.error("syncFiles: Missing catalog file " + src.getAbsolutePath());
        return;
      }
      // If we get here then we assume it is the case where the user managed to rename a book
      // while calibre2opds was running, so we simply log it has happened and otherwise ignore it.
      logger.warn("syncFiles: Unexpected missing file: " + src.getAbsolutePath());
      return;
    }
    // Sanity check - we cannot copy a file to itself
    // ITIMPI:  Easier to silently ignore such copies than include lots of
    //          logic according to mode to decide if a file is a copy candidate.
    if (src.getAbsolutePath().equalsIgnoreCase(dst.getAbsolutePath())) {
      // Lets add them to stats so we know it happens!
      copyToSelf++;
      if (syncFilesDetail && logger.isTraceEnabled())
        logger.trace("syncFiles: attempting to copy file to itself: " + src.getAbsolutePath());
      return;
    }

    //-----------------------------------------------------------------------------
    // Directory Handling
    //-----------------------------------------------------------------------------

    if (src.isDirectory()) {
      if (logger.isTraceEnabled())
        logger.trace("Directory " + src.getName() + " Processing Started");
      String displayText = src.getParentFile().getName() + File.separator + src.getName();
      // Improve message by removing name of TEMP folder from start
      if (displayText.startsWith(generateFolder.getName())) {
         displayText = displayText.substring(generateFolder.getName().length()+1);
      }
      callback.showMessage(displayText);

      // Create any missing target directories
      if (!dst.exists()) {
        if (logger.isTraceEnabled())
          logger.trace("Directory " + dst.getName() + " Create missing target");
        if (syncLog)
          syncLogFile.printf("CREATED: %s\n", dst.getName());
        if (dst.getName().endsWith("_Page"))
          assert false;
        dst.mkdirs();
      }

      //  Sanity check - target should be a directory
      if (!dst.isDirectory()) {
        logger.warn("Directory " + src.getName() + " Unexpected file with name expected for directory");
        return;
      }

      // Create current list of files that are in source locations
      File sourceFiles[] = src.listFiles();
      // Create current list of files that are in target location
      File destfiles[] = dst.listFiles();
      List<File> targetNotInSourceFiles;
      if (destfiles != null) {
        targetNotInSourceFiles = new LinkedList<File>(Arrays.asList(dst.listFiles()));
      } else {
        logger.debug("***** Possible Program Error: unexpected null from dst.listFiles() when dst=" + dst);
        targetNotInSourceFiles = new LinkedList<File>();  // Assign empty list
      }

      // Now we want to:
      // - Remove any that are in the source list as they will not need to be deleted.
      // - If we aer adding images to catalog we also need not to delete these if they exist!
      // - Copy across files from source list as we go
      for (int i = 0; i < sourceFiles.length; i++) {
        CachedFile sourceFile = CachedFileManager.INSTANCE.addCachedFile(sourceFiles[i]);
        String fileName = sourceFile.getName();
        CachedFile destFile = CachedFileManager.INSTANCE.addCachedFile(dst, fileName);

        // ITIMPI:  Need to decide if the exists() check is redundant
        //          as it may cause an unneeded file access
        if (destFile.exists()) {
          // TODO It is possible we can use an assert here instead!
          if ((src.getName().endsWith(Constants.XML_EXTENSION))
          && (currentProfile.getGenerateOpds() == true)) {
            // XML files never needed if not generating OPDS catalog
            if (logger.isTraceEnabled())
              logger.trace("No OPDS catalog so delete " + src.getAbsolutePath());
          } else {
            // remove entry from list of deletion candidates
            // as we are going to over-write it
             targetNotInSourceFiles.remove(destFile);
            if (CachedFileManager.INSTANCE.inCache(destFile) == null)
              destFile = CachedFileManager.INSTANCE.addCachedFile(destFile);
          }
        } else {
          // If the target does not exist, then we need to do
          // nothing about deleting the file.   However not sure
          // what this means in terms of application logic if we
          // actually get to this point!
          if (logger.isTraceEnabled())
            logger.trace("Directory " + src.getAbsolutePath() + " Unexpected missing target" + dst.getName());
          CachedFileManager.INSTANCE.removeCachedFile(destFile);
        }
        // copy across the file
        syncFiles(sourceFile, destFile);
      }
      // Now actually remove the files that are still in the list of removal candidates
      for (File file : targetNotInSourceFiles) {
        Helper.delete(file, true);
        if (syncLog)
          syncLogFile.printf("DELETED: %s\n", file.getName());
        copyDeleted++;
        CachedFileManager.INSTANCE.removeCachedFile(file);
      }
      if (logger.isTraceEnabled())
        logger.trace("Directory " + src.getName() + " Processing completed");
    } // End of Directory section

    //-----------------------------------------------------------------------------------
    // File level Copying
    // Try to optimise out any copying that is not required.
    //-----------------------------------------------------------------------------------
    else {
      boolean copyflag;

      // Ignore XML files if no OPDS catalog wanted
      // IS this the best place to do this?
      // TODO.  Suspect this section is now redundant - need to check this!
      if (!currentProfile.getGenerateOpds()) {
        if (src.getName().endsWith(Constants.XML_EXTENSION)) {
          int dummy = 1;
          if (dst.exists()) {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + dst.getAbsolutePath() + ": Deleted as XML file and no OPDS catalog required");
          } else {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + src.getAbsolutePath() + ": Ignored as XML file and no OPDS catalog required");
          }
          CachedFileManager.INSTANCE.removeCachedFile(src);
          CachedFileManager.INSTANCE.removeCachedFile(dst);
          return;
        }
      }

      if (syncFilesDetail && logger.isTraceEnabled())
        logger.trace("File " + src.getName() + ": Checking to see if should be copied");

      // Files that do not exist on target always need copying
      // ... so we only need to check other cases
      if (!dst.exists()) {
        if (syncFilesDetail && logger.isTraceEnabled())
          logger.trace("File " + src.getName() + ": Copy as target is missing");
        copyExistHits++;
        copyflag = true;
        if (syncLog)
          syncLogFile.printf("COPIED (New file): %s\n", dst.getName());
        dst.clearCachedInformation();
      } else {

        if (syncFilesDetail && logger.isTraceEnabled())
          logger.trace("File " + src.getName() + ": .. exists on target");
        // Target present, so check lengths
        if (src.length() != dst.length()) {
          if (logger.isTraceEnabled())
            logger.trace("File " + src.getName() + ": Copy as size changed");
          copyLengthHits++;
          copyflag = true;
          if (syncLog)
            syncLogFile.printf("COPIED (length changed): %s\n", src.getName());
        } else {
          if (syncFilesDetail && logger.isTraceEnabled())
            logger.trace("File " + src.getName() + ": .. size same on source and target");

          // Size unchanged, so check dates
          // TODO  There could be some issues if the date/time on the target
          //       is different to the machine running calibre2opds.  It might
          //       be worth adding some code to calculate the difference and
          //       use the results in the date comparisons. However for the
          //       time being we are assuming this is not an issue.
          if (src.lastModified() <= dst.lastModified()) {
            // Target newer than source
            if (logger.isTraceEnabled())
              logger.trace("File " + src.getName() + ": Skip Copy as source is not newer");
            copyDateMisses++;
            copyflag = false;
            if (syncLog)
              syncLogFile.printf("NOT COPIED (Source not newer): %s\n", dst.getName());
          } else {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + src.getName() + ": .. source is newer");
            // Source newer, but same size so see if CRC check to be done
            if (!checkCRC) {
              if (logger.isTraceEnabled())
                logger.trace("File " + src.getName() + ": Copy as CRC check not active");
              if (dst.isCrc())
                if (logger.isTraceEnabled())
                  logger.trace("File " + src.getName() + "CRC entry invalidated");
              dst.clearCrc();
              copyCrcUnchecked++;
              copyflag = true;
              if (syncLog)
                syncLogFile.printf("COPIED (CRC check not active): %s\n", src.getName());
            } else {
              if (src.getCrc() != dst.getCrc()) {
                if (logger.isTraceEnabled())
                  logger.trace("File " + src.getName() + ": Copy as CRC's different");
                copyCrcHits++;
                copyflag = true;
                if (syncLog)
                  syncLogFile.printf("COPIED (CRC changed): %s\n", src.getName());
              } else {
                if (logger.isTraceEnabled())
                  logger.trace("File " + src.getName() + ": Skip copy as CRC's match");
                copyCrcMisses++;
                copyflag = false;
                if (syncLog)
                  syncLogFile.printf("NOT COPIED (CRC same): %s\n", src.getName());
              }
            }
          }
        }
      }
      // Copy the file if we have decided that we need to do so
      if (copyflag) {
        // TODO:  It might be faster and more efficient to use a rename/move if it can
        //        be determined that source and target are on the same file system
        //        (which will be the case if generating files locally)
        //        N.B.  This also assumes the file is not needed again!

        callback.showMessage(src.getParentFile().getName() + File.separator + src.getName());
        if (syncFilesDetail && logger.isDebugEnabled())
          logger.debug("Copying file " + src.getName() + " to " + dst.getAbsolutePath());
        try {
          Helper.copy(src, dst);
          dst.exists();        // Dummy call to check cache value as used!
          dst.setCrc(src.getCrc());
        } catch (java.io.FileNotFoundException e) {
          // We ignore failed attempts to copy a file, although we log them
          // This allows for the user to have made changes to the library while
          // Calibre2opds is generating a library without the whole run failing.
          logger.warn("Unable to to copy file " + src);
        }
      }
    }  // End of File Handling section
  }

  /**
   * @param book
   * @return
   */
  private boolean shouldReprocessEpubMetadata(Book book) {
    EBookFile epubFile = book.getEpubFile();
    if (epubFile == null)
      return false;
    File opfFile = new File(book.getBookFolder(), "metadata.opf");
    if (!opfFile.exists())
      return true;          // ITIMPI:   Should this perhaps return false?
    long opfDate = opfFile.lastModified();
    long epubDate = epubFile.getFile().lastModified();
    return (opfDate > epubDate);
  }

  /**
   * TODO:  Not sure what this routine is intended for (it is not used)
   * @param books
   * @return
   */
  private Element computeSummary(List<Book> books) {
    Element contentElement = JDOM.INSTANCE.element("content");

    File calibreLibraryFolder = currentProfile.getDatabaseFolder();
    File summaryFile = new File(calibreLibraryFolder, "calibre2opds_summary.html");
    if (summaryFile.exists()) {
      // load the summary file and insert its content
      contentElement.setAttribute("type", "text/html");
      try {
        FileInputStream is = new FileInputStream(summaryFile);
        String text = Helper.readTextFile(is);
        List<Element> htmlElements = JDOM.INSTANCE.convertHtmlTextToXhtml(text);
        if (htmlElements != null)
          for (Element htmlElement : htmlElements) {
            contentElement.addContent(htmlElement.detach());
          }
      } catch (FileNotFoundException e) {
        logger.error(Localization.Main.getText("error.summary.cannotFindFile", summaryFile.getAbsolutePath()), e);
      } catch (IOException e) {
        logger.error(Localization.Main.getText("error.summary.errorParsingFile"), e);
      }
    } else {
      // create a simple content element with a text summary
      contentElement.setAttribute("type", "text");
      String summary = Localization.Main.getText("main.summary", Constants.PROGTITLE, Summarizer.INSTANCE.getBookWord(books.size()));
      contentElement.addContent(summary);
    }

    return contentElement;
  }

  /**
    * Sync a set of image files across to the specified target folder
    * The images are segregated into folders according to the bookid
    * Used when images are stored within catalog
    *
    * @param targetFolder
    */
  private void syncImages(CachedFile targetFolder) {
    Map<String,CachedFile> mapOfImagesToCopy = CatalogManager.INSTANCE.getMapOfCatalogImages();
    for (Map.Entry<String,CachedFile> entry : mapOfImagesToCopy.entrySet()) {
      CachedFile targetFile = CachedFileManager.INSTANCE.addCachedFile(targetFolder, entry.getKey());
      try {
        syncFiles(entry.getValue(), targetFile);
      } catch (IOException e) {
        logger.warn("syncImages: Failure copy file '" + entry.getKey() + "' to catalog");
      }
    }
  }

  /**
   * -----------------------------------------------
   * Control the overall catalog generation process
   * -----------------------------------------------
   *
   * @throws java.io.IOException
   */
  public void createMainCatalog() throws IOException {
    long countMetadata;     // Count of files for which ePub metadata is updated

    CatalogManager.reportInitialRamUsage();

    // reinitialize caches (in case of multiple calls in the same session)
    // CatalogManager.INSTANCE.catalogManager.reset();
    CatalogManager.INSTANCE.reset();
    CatalogManager.INSTANCE.initialize();
    CatalogManager.INSTANCE.callback = callback;

    Localization.Main.reloadLocalizations();
    Localization.Enum.reloadLocalizations();

    String textYES = Localization.Main.getText("boolean.yes");
    String textNO = Localization.Main.getText("boolean.no");
    // Ensure cached values are current for this generate run.
    currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
    checkCRC = currentProfile.getMinimizeChangedFiles();

    if (!currentProfile.getGenerateAllbooks())        callback.disableCreateAllBooks();
    if (!currentProfile.getGenerateAuthors())         callback.disableCreateAuthors();
    if (!currentProfile.getGenerateSeries())          callback.disableCreateSeries();
    if (!currentProfile.getGenerateTags())            callback.disableCreateTags();
    if (!currentProfile.getGenerateRatings())         callback.disableCreateRated();
    if (!currentProfile.getGenerateRecent())          callback.disableCreateRecent();
    if (Helper.isNullOrEmpty(currentProfile.getFeaturedCatalogSavedSearchName())) callback.disableCreateFeaturedBooks();
    if (Helper.isNullOrEmpty(currentProfile.getCustomCatalogs())) callback.disableCreateCustomCatalogs();
    if (! currentProfile.getReprocessEpubMetadata())  callback.disableReprocessingEpubMetadata();
    if (! currentProfile.getGenerateIndex())          callback.disableCreateJavascriptDatabase();
    if ((currentProfile.getDeviceMode() == DeviceMode.Default)
    ||  (currentProfile.getOnlyCatalogAtTarget()))    callback.disableCopyLibToTarget();
    if (! currentProfile.getZipCatalog())             callback.disableZipCatalog();
    /** where the catalog is eventually located */
    String where = null;

    /** if true, generation has been stopped by the user */
    boolean generationStopped = false;
    /** if true, then generation crashed unexpectedly; */
    boolean generationCrashed = false;

    logger.info(Localization.Main.getText("config.profile.label", ConfigurationManager.INSTANCE.getCurrentProfileName()));
    callback.dumpOptions();

    //                      PARAMETER VALIDATION PHASE

    // Do some sanity checks on the settings before starting the generation
    if (logger.isTraceEnabled())
      logger.trace("Start sanity checks against user errors that might cause data loss");

    // PARAMETER SETTING CHECKS

    // Make sure that at least one of OPDS and HTML catalog types is activated
    if ((!currentProfile.getGenerateOpds()) && (!currentProfile.getGenerateHtml())) {
      callback.errorOccured(Localization.Main.getText("error.nogeneratetype"), null);
      return;
    }

    // We are not allowed to suppress generation of all sub-catalogs
    // (unless we have at least one external catalog specified)
    if (!currentProfile.getGenerateAuthors()
        &&  !currentProfile.getGenerateTags()
        &&  !currentProfile.getGenerateSeries()
        &&  !currentProfile.getGenerateRecent()
        &&  !currentProfile.getGenerateRatings()
        &&  !currentProfile.getGenerateAllbooks()) {
      callback.errorOccured(Localization.Main.getText("error.noSubcatalog"), null);
      return;
    }

    // Check that folder specified as library folder actually contains a calibre database
    libraryFolder = currentProfile.getDatabaseFolder();
    if (Helper.isNullOrEmpty(libraryFolder)) {
      callback.errorOccured(Localization.Main.getText("error.databasenotset"), null);
      return;
    }
    assert libraryFolder != null : "libraryFolder must be set to continue with generation";
    if (!DatabaseManager.INSTANCE.databaseExists()) {
      callback.errorOccured(Localization.Main.getText("error.nodatabase", libraryFolder), null);
      return;
    }

    // Check that the catalog folder is actually set to something and not an empty string
    catalogFolderName = currentProfile.getCatalogFolderName();
    if (Helper.isNullOrEmpty(catalogFolderName)) {
      callback.errorOccured(Localization.Main.getText("error.nocatalog"), null);
      return;
    }
    // THere we aso add some checks against unusual values in the catalog value  )c2o-91)
    if (catalogFolderName.startsWith("/")
    ||  catalogFolderName.startsWith("\\")
    ||  catalogFolderName.startsWith("../")
    ||  catalogFolderName.startsWith("..\\")) {
      callback.errorOccured(Localization.Main.getText("error.badcatalog"), null);
      return;
    }

    // Check for cases where target folder must be specified
    targetFolder = currentProfile.getTargetFolder();
    if (Helper.isNullOrEmpty(targetFolder)) {
      switch (currentProfile.getDeviceMode()) {
      case Nook:
        callback.errorOccured(Localization.Main.getText("error.nooktargetnotset"), null);
        return;
      case Nas:
        callback.errorOccured(Localization.Main.getText("error.targetnotset"), null);
        return;
      case Default:
        assert currentProfile.getCopyToDatabaseFolder(): "Copy to database folder MUST be set in Default mode";
        break;
      default:
        assert false : "Unknown DeviceMode " + currentProfile.getDeviceMode();
      }
    } else {
      switch (currentProfile.getDeviceMode()) {
        case Nook:
            // As a saftey check we insist that the Nook target already exists
            if (! targetFolder.exists()) {
              callback.errorOccured(Localization.Main.getText("error.nooktargetdoesnotexist"), null);
              return;
            }
            targetFolder = new File(targetFolder.getAbsolutePath() + "/" + currentProfile.getCatalogFolderName()  + Constants.TROOK_FOLDER_EXTENSION);
            break;
        case Nas:
          if (! targetFolder.exists()) {
            callback.errorOccured(Localization.Main.getText("error.targetdoesnotexist"), null);
            return;
          }
          break;
        case Default:
          assert false : "Setting Target folder should be disabled in Default mode";
        default:
          assert false : "Unknown DeviceMode " + currentProfile.getDeviceMode();
      }
    }
    logger.trace("targetFolder set to " + targetFolder);

    // Check any custom columns specified actually exist
    boolean errors = false;
    List<String> customColumnsWanted = currentProfile.getCustomColumnsWanted();
    if (customColumnsWanted != null && customColumnsWanted.size() > 0) {
      testcol: for (String customLabel : customColumnsWanted) {
        if (customLabel.startsWith("#")){
          customLabel = customLabel.substring(1);
        }
        for (CustomColumnType type : DataModel.INSTANCE.getListOfCustomColumnTypes()) {
          if (type.getLabel().toUpperCase().equals(customLabel.toUpperCase())) {
            if (Constants.CUSTOM_COLUMN_TYPES_SUPPORTED.contains(type.getDatatype())) {
              continue testcol;
            }
            if (Constants.CUSTOM_COLUMN_TYPES_UNSUPPORTED.contains(type.getDatatype())) {
              callback.errorOccured(Localization.Main.getText("gui.error.customColumnNotSupported", customLabel), null);
              errors = true;
              continue testcol;
            }
            callback.errorOccured(Localization.Main.getText("gui.error.customColumnNotRecognized", customLabel), null);
            errors = true;
            continue testcol;
          }
        }
        // If we get here we did not find the relevant custom column
        callback.errorOccured(Localization.Main.getText("gui.error.customColumnNotFound", customLabel), null);
        errors = true;
      }
      if (errors == true) {
        if (1 == callback.askUser(Localization.Main.getText("gui.confirm.continueGenerating", targetFolder), textYES, textNO)) {
          return;
        }
      }
    }

    // FILE PLACEMENT CHECKS

    assert Helper.isNotNullOrEmpty(libraryFolder);
    if (targetFolder != null) {
      if (currentProfile.getOnlyCatalogAtTarget()) {
        File f = new File(targetFolder, Constants.CALIBRE_METADATA_DB_);
        if (f.exists()) {
          callback.errorOccured(Localization.Main.getText("error.targetislibrary"), null);
          return;
        }
      }
      // Check that target folder (if set) is not set to be the same as the library folder
      if (libraryFolder.getAbsolutePath().equals(targetFolder.getAbsolutePath())) {
        callback.errorOccured(Localization.Main.getText("error.targetsame"), null);
        return;
      }
      // Check that target folder (if set) is not set to be a higher level than the library folder
      // (which would have unfortunate consequences when deleting during sync operation)
      if (libraryFolder.getAbsolutePath().startsWith(targetFolder.getAbsolutePath())) {
        callback.errorOccured(Localization.Main.getText("error.targetparent"), null);
        return;
      }
      // If not already a catalog at target, give overwrite warning
      if (!checkCatalogExistence(targetFolder, false)) {
        if (1 == callback.askUser(Localization.Main.getText("gui.confirm.clear", targetFolder), textYES, textNO)) {
          return;
        }
      }
    }
    // If catalog folder exists, then see if it looks like it already contains a catalog
    // and if not warn the user that existing contents will be lost and get confirmation OK
    File catalogParentFolder = targetFolder;
    // N.B.  Whether calibreTargetFolder was set to be different to the Library folder is mode dependent
    if (catalogParentFolder == null || catalogParentFolder.getName().length() == 0) {
      if (!checkCatalogExistence(libraryFolder, true)) {
        if (! libraryFolder.equals(targetFolder)) {     // Avoid two prompts for same folder
          if (1 == callback.askUser(Localization.Main.getText("gui.confirm.clear", libraryFolder + File.separator + currentProfile.getCatalogFolderName()), textYES, textNO)) {
            return;
          }
        }
      }
      catalogParentFolder = libraryFolder;
    }
    logger.trace("catalogParentFolder set to " + catalogParentFolder);
    // Set catalog folder (remembering to add TROOK extension if in Nook mode)
    File catalogFolder = new File(catalogParentFolder, CatalogManager.INSTANCE.getCatalogFolderName());
//                                + ((currentProfile.getDeviceMode() == DeviceMode.Nook ? Constants.TROOK_FOLDER_EXTENSION + "/" + Constants.NOOK_CATALOG_FOLDERNAME : "")));
    if (logger.isTraceEnabled())
      logger.trace("New catalog to be generated at " + catalogFolder.getPath());

    // If copying catalog back to database folder check it is safe to overwrite
    if (true == currentProfile.getCopyToDatabaseFolder()) {
      File databaseFolder = currentProfile.getDatabaseFolder();
      if ( !checkCatalogExistence(databaseFolder, true)) {
        if (! databaseFolder.equals(catalogParentFolder)
        &&  ! databaseFolder.equals(libraryFolder)) {     // Avoid two prompts for same folder
          if (1 == callback.askUser(Localization.Main.getText("gui.confirm.clear", databaseFolder + File.separator + currentProfile.getCatalogFolderName()), textYES, textNO)) {
            return;
          }
        }
      }
      // catalogParentFolder = null;    // We are finished with this, so clear for future reference as still in scope
    }

    logger.trace("Passed sanity checks, so proceed with generation");

    //                  GENERATION PHASE


    // Ensure objects are set to clean state (in case resused in same run)

    CachedFileManager.INSTANCE.reset();
    CatalogManager.INSTANCE.thumbnailManager.reset();
    CatalogManager.INSTANCE.coverManager.reset();

    CatalogManager.reportRamUsage();

    try {
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
      if (logger.isTraceEnabled())logger.trace("generateFolder set to " + generateFolder);
      generateFolder.mkdir();
      generateFolder.deleteOnExit();
      logger.info("Temporary Files folder: " + generateFolder.getAbsolutePath());

      // Save the location of the Catalog folder for any other component that needs to knw it
      CatalogManager.INSTANCE.setGenerateFolder(generateFolder);
      callback.startCreateMainCatalog();

      callback.startReadDatabase();
      Long now = System.currentTimeMillis();
      DataModel.INSTANCE.reset();
      DataModel.INSTANCE.setUseLanguageAsTags(ConfigurationManager.INSTANCE.getCurrentProfile().getLanguageAsTag());
      DataModel.INSTANCE.preloadDataModel();    // Get mandatory database fields
      logger.info("COMPLETED preloading Datamodel");
      CatalogManager.reportRamUsage();

      // Database read optimizations
      // (ony read in optional databitems if weneed them later)

      // TODO Tags
      // TODO Series
      // TODO Published
      // TODO Publisher
      // Custom Columns - remove any custom columns that are not on wanted list
      List<CustomColumnType>customColumns = DataModel.INSTANCE.getListOfCustomColumnTypes();
      customColumnsWanted = currentProfile.getCustomColumnsWanted();
      checktype: for (int i=0; i < customColumns.size() ; i++) {
        CustomColumnType type = customColumns.get(i);
        if (customColumnsWanted == null || customColumnsWanted.size() == 0) {
          customColumns.remove(type);
          i--;  // Decrement as we have removed current node
        } else {
          for (String label : customColumnsWanted) {
            if (label.startsWith("#")) {
              label = label.substring(1);
            }
            if (type.getLabel().toUpperCase().equals(label.toUpperCase())) {
              continue checktype;
            }
          }
          customColumns.remove(type);
          i--;    // Decrement as we have removed current node
        }
      }
      DataModel.INSTANCE.getMapOfCustomColumnValuesByBookId();
      callback.checkIfContinueGenerating();     // check if we must continue


      // Prepare the feature books search query
      BookFilter featuredBookFilter = null;
      String featuredCatalogTitle = ConfigurationManager.INSTANCE.getCurrentProfile().getFeaturedCatalogTitle();
      String featuredCatalogSearch = ConfigurationManager.INSTANCE.getCurrentProfile().getFeaturedCatalogSavedSearchName();
      if (Helper.isNotNullOrEmpty(featuredCatalogSearch)) {
        try {
          featuredBookFilter = CalibreQueryInterpreter.interpret(featuredCatalogSearch);
        } catch (CalibreSavedSearchInterpretException e) {
          // callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
          callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", featuredCatalogTitle,featuredBookFilter), e);
        } catch (CalibreSavedSearchNotFoundException e) {
          // callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
          callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", featuredCatalogTitle, featuredCatalogSearch), null);
        }
        if (featuredBookFilter == null) {
          // an error occured, let's ask the user if he wants to abort
          if (1 == callback.askUser(Localization.Main.getText("gui.confirm.continueGenerating"), textYES, textNO)) {
            callback.endCreateMainCatalog(null, CatalogManager.INSTANCE.htmlManager.getTimeInHtml());
            return;
          }
        }
      }
      CatalogManager.INSTANCE.featuredBooksFilter = featuredBookFilter;
      callback.checkIfContinueGenerating();      // check if we must continue

      // Prepare the Custom catalogs search query
      List<Composite<String, String>> customCatalogs = ConfigurationManager.INSTANCE.getCurrentProfile().getCustomCatalogs();
      if (Helper.isNotNullOrEmpty(customCatalogs)) {
nextCC: for (Composite<String, String> customCatalog : customCatalogs) {
          callback.checkIfContinueGenerating();
          String customCatalogTitle = customCatalog.getFirstElement();
          String customCatalogSearch = customCatalog.getSecondElement();
          if (Helper.isNotNullOrEmpty(customCatalogTitle) && Helper.isNotNullOrEmpty(customCatalogSearch)) {
            // skip http external catalogs (c2o-13)
            for (String urlPrefix : Constants.CUSTOMCATALOG_SEARCH_FIELD_URLS) {
              if (customCatalogSearch.toUpperCase().startsWith(urlPrefix.toUpperCase())) {
                continue nextCC;
              }
            }
            BookFilter customCatalogFilter = null;
            try {
              customCatalogFilter = CalibreQueryInterpreter.interpret(customCatalogSearch);
            } catch (CalibreSavedSearchInterpretException e) {
              // callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
              callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", customCatalogTitle, customCatalogSearch), e);
            } catch (CalibreSavedSearchNotFoundException e) {
              // callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
              callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", customCatalogTitle, customCatalogSearch), null);
            }
            if (customCatalogFilter == null) {
              // an error occured, let's ask the user if he wants to abort
              if (1 == callback.askUser(Localization.Main.getText("gui.confirm.continueGenerating"), textYES, textNO)) {
                callback.endCreateMainCatalog(null, CatalogManager.INSTANCE.htmlManager.getTimeInHtml());
                return;
              }
              // TODO Set something to suppress this custom catalog entry at generate stage!
              //      Currently an entry is generated to a none-existent URL
            } else {
              CatalogManager.INSTANCE.customCatalogsFilters.put(customCatalogTitle, customCatalogFilter);
            }
          }
        }
      }

      callback.checkIfContinueGenerating();

      // filter the datamodel

      try {
        RemoveFilteredOutBooks.INSTANCE.runOnDataModel();
      } catch (CalibreSavedSearchInterpretException e) {
        callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
      } catch (CalibreSavedSearchNotFoundException e) {
        callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
      }

      List<Book> books = DataModel.INSTANCE.getListOfBooks();
      if (Helper.isNullOrEmpty(books)) {
        if (Database.INSTANCE.wasSqlEsception() == 0 ) {
          callback.errorOccured(Localization.Main.getText("error.nobooks"), null);
        } else
          callback.errorOccured("Error accessing database: code=" + Database.INSTANCE.wasSqlEsception(), null);
        return;
      } else {
        logger.info("Database loaded: " + books.size() + " books");
      }
      callback.checkIfContinueGenerating();

      CatalogManager.reportRamUsage();
      callback.endReadDatabase(System.currentTimeMillis() - now, Summarizer.INSTANCE.getBookWord(books.size()));
      callback.setAuthorCount("" + DataModel.INSTANCE.getListOfAuthors().size() + " " + Localization.Main.getText("authorword.title"));
      callback.setTagCount("" + DataModel.INSTANCE.getListOfTags().size() + " " + Localization.Main.getText("tagword.title"));
      callback.setSeriesCount("" + DataModel.INSTANCE.getListOfSeries().size() + " " + Localization.Main.getText("seriesword.title"));

      // Load up the File Cache if it exists

      now = System.currentTimeMillis();
      switch (currentProfile.getDeviceMode()) {
        case Nook:
          CachedFileManager.INSTANCE.setCacheFolder(targetFolder);
          break;
        default:
          CachedFileManager.INSTANCE.setCacheFolder(catalogFolder);
          break;
      }
      if (checkCRC) {
        if (logger.isTraceEnabled())
          logger.trace("Loading Cache");
        callback.showMessage(Localization.Main.getText("info.step.loadingcache"));
        CachedFileManager.INSTANCE.loadCache();
        logger.info(Localization.Main.getText("info.step.loadedcache", CachedFileManager.INSTANCE.getCacheSize()));

      } else {
        if (logger.isTraceEnabled())
          logger.trace("Deleting Cache");
        CachedFileManager.INSTANCE.deleteCache();
      }
      logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));
      CatalogManager.reportRamUsage();

      callback.checkIfContinueGenerating();


      // prepare the Trook specific search database

      if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
        TrookSpecificSearchDatabaseManager.INSTANCE.setDatabaseFile(new File(generateFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME));
        TrookSpecificSearchDatabaseManager.INSTANCE.getConnection();
      }

      // copy the resource files to the catalog folder
      // We check in the following order:
      //  - Configuration folder
      //  - Install folder
      //  - built-in resource

      logger.debug("STARTED: Copying Resource files");
      for (String resource : Constants.FILE_RESOURCES) {
        callback.checkIfContinueGenerating();
        InputStream resourceStream = ConfigurationManager.INSTANCE.getResourceAsStream(resource);
//        File resourceFile = new File(generateFolder, CatalogContext.INSTANCE.catalogManager.getCatalogFolderName() + "/" + resource);
        File resourceFile = new File(generateFolder, resource);
        Helper.copy(resourceStream, resourceFile);
        logger.trace("Copying Resource " + resource);
      }
      logger.debug("COMPLETED: Copying Resource files");
      callback.checkIfContinueGenerating();

      //      Standard sub-catalogs for a folder level

      //      Element main = FeedHelper.getFeedRootElement(null, title, Constants.INITIAL_URN, urlExt);
      logger.debug("Starter generating top level catalog");
      LevelSubCatalog levelSubCatalog = new LevelSubCatalog(books,currentProfile.getCatalogTitle());
      levelSubCatalog.setCatalogLevel("");      // Empty level for top level sub-catalogs
      levelSubCatalog.setCatalogType("");       // No type for top level sub-catalog!
      levelSubCatalog.setCatalogFolder("");         // Force to top level!
      levelSubCatalog.setCatalogBaseFilename(CatalogManager.INSTANCE.getInitialUr());
      Breadcrumbs breadcrumbs = Breadcrumbs.newBreadcrumbs(currentProfile.getCatalogTitle(),
                                "dummy.xml");
      levelSubCatalog.getCatalog(
          breadcrumbs,
          null,           // StufftoFilterOut
          false,          // at top level
          "",             // Summary
          "",             // urn
          null,           // Splitoption
          "");            // icon
      levelSubCatalog = null; // Maybe not necessary - but forced free may help release resources earlier

      /* Javascript search database */

      logger.debug("STARTING: Generating Javascript database");
      long nbKeywords = IndexManager.INSTANCE.size();
      callback.startCreateJavascriptDatabase(nbKeywords);
      now = System.currentTimeMillis();
      if (currentProfile.getGenerateIndex())
        IndexManager.INSTANCE.exportToJavascriptArrays();
      callback.endCreateJavascriptDatabase(System.currentTimeMillis() - now);
      logger.debug("COMPLETED: Generating Javascript database");
      callback.checkIfContinueGenerating();

      /* Epub metadata reprocessing */

      logger.debug("STARTING: Processing ePub Metadata");
      callback.startReprocessingEpubMetadata(DataModel.INSTANCE.getListOfBooks().size());
      now = System.currentTimeMillis();
      countMetadata = 0;
      if (currentProfile.getReprocessEpubMetadata()) {
        for (Book book : DataModel.INSTANCE.getListOfBooks()) {
          callback.checkIfContinueGenerating();
          callback.incStepProgressIndicatorPosition();
          if (shouldReprocessEpubMetadata(book)) {
            try {
              callback.showMessage(book.getAuthors() + ": " + book.getTitle());
              new OpfOutput(book).processEPubFile();
            } catch (IOException e) {
              String message = Localization.Main.getText("gui.error.tools.processEpubMetadataOfAllBooks", book.getTitle(), e.getMessage());
              logger.error(message, e);
            }
            countMetadata++;
          }
        }
      }
      callback.endReprocessingEpubMetadata(System.currentTimeMillis() - now);
      logger.debug("COMPLETED: Processing ePub Metadata");
      callback.checkIfContinueGenerating();

      if (syncLog)
        syncLogFile =
            new PrintWriter(ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);

      // reset stats fields for this run

      copyExistHits = copyLengthHits
                    = copyCrcUnchecked
                    = copyCrcHits
                    = copyCrcMisses
                    = copyDateMisses
                    = copyCrcUnchecked = 0;

      // copy the catalogs
      // (and books, if the target folder is set) to the destination folder

      // if the target folder is set, copy/sync Files from the library there
      int nbFilesToCopyToTarget = CatalogManager.INSTANCE.getListOfFilesPathsToCopy().size();
      callback.startCopyLibToTarget(nbFilesToCopyToTarget);
      now = System.currentTimeMillis();
      // In modes other than default mode we make a copy of all the ebook
      // files referenced by the catalog in the target lcoation
      if ((currentProfile.getDeviceMode() != DeviceMode.Default)
      && (!currentProfile.getOnlyCatalogAtTarget())) {
        logger.debug("STARTING: syncFiles eBook files to target");
        now = System.currentTimeMillis();
        for (String pathToCopy : CatalogManager.INSTANCE.getListOfFilesPathsToCopy()) {
          callback.checkIfContinueGenerating();
          CachedFile sourceFile = CachedFileManager.INSTANCE.addCachedFile(currentProfile.getDatabaseFolder(), pathToCopy);
          CachedFile targetFile = CachedFileManager.INSTANCE.addCachedFile(targetFolder, pathToCopy);
          syncFiles(sourceFile, targetFile);
        }
        logger.debug("COMPLETED: syncFiles eBook files to target");
        callback.checkIfContinueGenerating();

        callback.showMessage(Localization.Main.getText("info.step.tidyingtarget"));
        // delete the target folders that were not in the source list (minus the catalog folder, of course)
        logger.debug("STARTING: Build list of files to delete from target");
        Set<File> usefulTargetFiles = new TreeSet<File>();
        List<String> sourceFiles = new LinkedList<String>(CatalogManager.INSTANCE.getListOfFilesPathsToCopy());
        for (String sourceFile : sourceFiles) {
          callback.checkIfContinueGenerating();
          File targetFile = new File(targetFolder, sourceFile);
          while (targetFile != null) {
            usefulTargetFiles.add(targetFile);
            targetFile = targetFile.getParentFile();
          }
        }
        logger.debug("COMPLETED: Build list of files to delete from target");
        callback.checkIfContinueGenerating();

        logger.debug("STARTED: Creating list of files on target");
        List<File> existingTargetFiles = Helper.listFilesIn(targetFolder);
        logger.debug("COMPLETED: Creating list of files on target");
        String targetCatalogFolderPath = new File(targetFolder, CatalogManager.INSTANCE.getCatalogFolderName()).getAbsolutePath();
        String calibreFolderPath = currentProfile.getDatabaseFolder().getAbsolutePath();

        // TODO    Look if this can be done more effeciently?  Perhaps piecemeal during sync?
        logger.debug("STARTING: Delete superfluous files from target");
        String catalogfolder = currentProfile.getCatalogFolderName();
        for (File existingTargetFile : existingTargetFiles) {
          callback.checkIfContinueGenerating();
          // Never delete catalog folder if present
          if (! existingTargetFile.getName().endsWith(catalogFolderName)) {
            if (!usefulTargetFiles.contains(existingTargetFile)) {
              if (!existingTargetFile.getAbsolutePath().startsWith(targetCatalogFolderPath)) // don't delete the catalog files
              {
                if (!existingTargetFile.getAbsolutePath()
                    .startsWith(calibreFolderPath)) // as an additional security, don't delete anything in the Calibre library
                {
                  if (logger.isTraceEnabled())
                    logger.trace("deleting " + existingTargetFile.getPath());
                  callback.showMessage(Localization.Main.getText("info.deleting") + " " + existingTargetFile);
                  Helper.delete(existingTargetFile, true);

                  if (syncLog)
                    syncLogFile.printf("DELETED: %s\n", existingTargetFile);
                  // Ensure no longer in cache
                  CachedFileManager.INSTANCE.removeCachedFile(existingTargetFile);
                }
              }
            }
          }
        }
        logger.debug("COMPLETED: Delete superfluous files from target");
      }
      callback.endCopyLibToTarget(System.currentTimeMillis() - now);
      callback.checkIfContinueGenerating();

      long nbCatalogFilesToCopyToTarget = Helper.count(CatalogManager.INSTANCE.getGenerateFolder());
      // If we are copying to two locations need to double count
      if (! currentProfile.getDeviceMode().equals(DeviceMode.Default)
      &&  currentProfile.getCopyToDatabaseFolder()) {
        nbCatalogFilesToCopyToTarget += nbCatalogFilesToCopyToTarget;
      }
      nbCatalogFilesToCopyToTarget += CatalogManager.INSTANCE.getMapOfCatalogImages().size();
      callback.startCopyCatToTarget(nbCatalogFilesToCopyToTarget);
      now = System.currentTimeMillis();
      // Now need to decide about the catalog and associated files
      // In particular there are some Nook mode specific files
      // In Nook mode we do not need to copy the catalog files if we have a ZIP'ed copy
      logger.debug("STARTING: syncFiles Catalog Folder");
      switch (currentProfile.getDeviceMode()) {
      case Nook:
        // when publishing to the Nook, don't forget to copy the search database (if it exists)
        if (TrookSpecificSearchDatabaseManager.INSTANCE.getDatabaseFile() != null) {
          TrookSpecificSearchDatabaseManager.INSTANCE.closeConnection();
          CachedFile destinationFile = CachedFileManager.INSTANCE.addCachedFile(targetFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME);
          CachedFile trookDatabaseFile = CachedFileManager.INSTANCE.addCachedFile(TrookSpecificSearchDatabaseManager.INSTANCE.getDatabaseFile());
          syncFiles(trookDatabaseFile, destinationFile);
        }
        // Also need to make sure catalog.xml exists for Trook use
        // Use index.xml already generated
        File indexFile = new File(generateFolder, "/" + CatalogManager.INSTANCE.getCatalogFolderName() + "/index.xml");
        // replicate it to catalog.xml
        File catalogFile = new File(generateFolder, "/" + CatalogManager.INSTANCE.getCatalogFolderName() + "/catalog.xml");
        if (logger.isTraceEnabled())
          logger.trace("copy '" + indexFile + "' to '" + catalogFile + "'");
        syncFiles(new CachedFile(indexFile.getAbsolutePath()), new CachedFile(catalogFile.getAbsolutePath()));
        File targetCatalogZipFile = new File(targetFolder, Constants.TROOK_CATALOG_FILENAME);
        // Start by deleting any existing ZIP file
        if (targetCatalogZipFile.exists()) {
          targetCatalogZipFile.delete();
        }
        if (currentProfile.getZipTrookCatalog()) {
          // when publishing to the Nook, archive the catalog into a big zip file (easier to transfer, and Trook knows how to read it!)
          recursivelyZipFiles(CatalogManager.INSTANCE.getGenerateFolder(), true, targetCatalogZipFile, false);
          // Now ensure that there is no unzipped catalog left behind!
          File targetCatalogFolder = new File(targetFolder, CatalogManager.INSTANCE.getCatalogFolderName());
          callback.showMessage(Localization.Main.getText("info.deleting") + " " + targetCatalogFolder.getName());
          Helper.delete(targetCatalogFolder, true);
          break;
        }
        // FALLTHRU Sync catalog files if not using ZIP mode
      case Nas:
        File targetCatalogFolder;
        if (currentProfile.getOnlyCatalogAtTarget()) {
          targetCatalogFolder = targetFolder;
        } else {
          targetCatalogFolder = new File(targetFolder, CatalogManager.INSTANCE.getCatalogFolderName());
        }
        syncFiles(new CachedFile(generateFolder.getAbsolutePath()), new CachedFile(targetCatalogFolder.getAbsolutePath()));
        logger.debug("START: Copy images to Destination catalog folder");
        syncImages(new CachedFile(targetCatalogFolder.getAbsolutePath()));
        logger.debug("COMPLETED: Copy images to Destination catalog folder");
        break;
      case Default:
        // Do nothing.   In this mode we sync the catalog using the code for copying back to the library
        break;
      }
      logger.debug("COMPLETED: syncFiles Catalog Folder");
      callback.checkIfContinueGenerating();

      // NOTE.   This is how we sync the catalog in Default mode
      if (currentProfile.getCopyToDatabaseFolder()) {
        logger.debug("STARTING: Copy Catalog Folder to Database Folder");
        File libraryCatalogFolder = new File(libraryFolder, CatalogManager.INSTANCE.getCatalogFolderName());
        syncFiles(new CachedFile(generateFolder.getAbsolutePath()) , new CachedFile(libraryCatalogFolder.getAbsolutePath()));
        logger.debug("COMPLETED: Copy Catalog Folder to Database Folder");
        logger.debug("START: Copy images to Database catalog folder");
        syncImages(new CachedFile(libraryCatalogFolder.getAbsolutePath()));
        logger.debug("COMPLETED: Copy images to Database catalog folder");
      }
      CatalogManager.thumbnailManager.writeImageHeightFile();
      CatalogManager.coverManager.writeImageHeightFile();
      callback.endCopyCatToTarget(System.currentTimeMillis() - now);
      callback.checkIfContinueGenerating();

      callback.startZipCatalog(nbCatalogFilesToCopyToTarget);
      now = System.currentTimeMillis();
      if (currentProfile.getZipCatalog()) {
        logger.debug("STARTING: ZIP Catalog");
        String zipFilename = ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogTitle() + ".zip";
        File zipFolder = (targetFolder == null) ? currentProfile.getDatabaseFolder() : targetFolder;
        File zipFile = new File(zipFolder, zipFilename);
        zipFile.delete();     // Remove any existing ZIP file
        recursivelyZipFiles(CatalogManager.INSTANCE.getGenerateFolder(), false, zipFile, currentProfile.getZipOmitXml());
        if (targetFolder != null  && currentProfile.getCopyToDatabaseFolder()) {
          Helper.copy(zipFile,new File(currentProfile.getDatabaseFolder(),zipFilename));
        }
        logger.debug("COMPLETED: ZIP Catalog");
      }
      callback.endZipCatalog(System.currentTimeMillis() - now);
      callback.checkIfContinueGenerating();

      if (syncLog) {
        logger.info("Sync Log: " + ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
      }
      // Save the CRC cache to the catalog folder
      // We always do this even if CRC Checking not enabled
      now = System.currentTimeMillis();
      logger.info(Localization.Main.getText("info.step.savingcache") + " " + CachedFileManager.INSTANCE.getCacheSize());
      callback.showMessage(Localization.Main.getText("info.step.savingcache"));
      CachedFileManager.INSTANCE.saveCache(generateFolder.getPath());
      logger.info(Localization.Main.getText("info.step.savedcache", CachedFileManager.INSTANCE.getSaveCount(), CachedFileManager.INSTANCE.getIgnoredCount()));
      logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));

      callback.checkIfContinueGenerating();

      // Produce run statistics

      if (syncLog) {
        syncLogFile.println();
        syncLogFile.println(Localization.Main.getText("stats.copy.header"));
        syncLogFile.println(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
        syncLogFile.println(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
        syncLogFile.println(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
        syncLogFile.println(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
        syncLogFile.println(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
        syncLogFile.println(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
        // syncLogFile.println(String.format("%8d  ", copyDeleted) + Localization.Main.getText("stats.copy.deleted"));
        syncLogFile.close();
      }

      logger.info("");
      logger.info(Localization.Main.getText("stats.library.header"));
      logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfBooks().size()) + Localization.Main.getText("bookword.title"));
      logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfAuthors().size()) + Localization.Main.getText("authorword.title"));
      logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfSeries().size()) + Localization.Main.getText("seriesword.title"));
      logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfTags().size()) + Localization.Main.getText("tagword.title"));
      logger.info("");
      logger.info(Localization.Main.getText("stats.run.header"));
      logger.info(String.format("%8d  ", countMetadata) + Localization.Main.getText("stats.run.metadata"));
      logger.info(String.format("%8d  ", CatalogManager.INSTANCE.thumbnailManager.getCountOfImagesGenerated()) + Localization.Main.getText("stats.run.thumbnails"));
      logger.info(String.format("%8d  ", CatalogManager.INSTANCE.coverManager.getCountOfImagesGenerated()) + Localization.Main.getText("stats.run.covers"));
      logger.info("");
      logger.info(Localization.Main.getText("stats.copy.header"));
      logger.info(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
      logger.info(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
      logger.info(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
      logger.info(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
      logger.info(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
      logger.info(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
      logger.info(String.format("%8d  ", copyDeleted) + Localization.Main.getText("stats.copy.deleted"));
      logger.info("");
      if (copyToSelf != 0)
        logger.warn(String.format("%8d  ", copyToSelf) + Localization.Main.getText("stats.copy.toself"));

      // Now work put where to tell user result has been placed

      if (logger.isTraceEnabled())
        logger.trace("try to determine where the results have been put");
      switch (currentProfile.getDeviceMode()) {
        case Nook:
          where = Localization.Main.getText("info.step.done.nook");
          break;
        case Nas:
          where = currentProfile.getTargetFolder().getPath();
          break;
        case Default:
          File libraryCatalogFolder = new File(libraryFolder, currentProfile.getCatalogFolderName());
          where = libraryCatalogFolder.getPath();
          break;
      }
      if (targetFolder != null &&  currentProfile.getCopyToDatabaseFolder()) {
        where = where + " " + Localization.Main.getText("info.step.done.andYourDb");
      }
      if (logger.isTraceEnabled())
        logger.trace("where=" + where);
    } catch (GenerationStoppedException gse) {
      generationStopped = true;
    } catch (Throwable t) {
      // error = t;
      generationCrashed = true;
      logger.error(" ");
      logger.error("*************************************************");
      logger.error(Localization.Main.getText("error.unexpectedFatal").toUpperCase());
      logger.error(Localization.Main.getText("error.cause").toUpperCase() + ": " + t + ": " + t.getCause());
      logger.error(Localization.Main.getText("error.message").toUpperCase() + ": " + t.getMessage());
      logger.error(Localization.Main.getText("error.stackTrace").toUpperCase() + ":");
      for (StackTraceElement element : t.getStackTrace())
        logger.error(element.toString());
      logger.error("*************************************************");
      logger.error(" ");
    } finally {
      // make sure the temp files are deleted whatever happens
      long now = System.currentTimeMillis();
      logger.info(Localization.Main.getText("info.step.deletingfiles"));
      if (generateFolder != null ) {
        callback.showMessage(Localization.Main.getText("info.step.deletingfiles"));
        callback.setStopGenerating();
        Helper.delete(generateFolder, false);
      }
      logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));
      if (generationStopped)
        callback.errorOccured(Localization.Main.getText("error.userAbort"), null);
      else if (generationCrashed)
        callback.errorOccured(Localization.Main.getText("error.unexpectedFatal"), null);
      else
        callback.endCreateMainCatalog(where, CatalogManager.INSTANCE.htmlManager.getTimeInHtml());
      CatalogManager.reportRamUsage();
    }
  }

  /**
   * Check to see if there appears to already be an existing calibre2opds catalog
   * at the specified location (by checking for specific files).  Note that a false
   * is always definitive, while a true could return a false (although unlikely) positive.
   *
   * @param catalogParentFolder    Path that contains the catalog folder
   * @param checkCatalogFolderOnly Set to true if it is OK if parent exists and catalog does not
   * @return true if cataog appears to be present
   *         false if catalog definitely not there.
   */
  private boolean checkCatalogExistence(File catalogParentFolder, boolean checkCatalogFolderOnly) {
    // We treat Parent folder as not existing as being equivalent to
    // catalog existing as there is no problem with over-writing.
    if (!catalogParentFolder.exists()) {
      if (logger.isTraceEnabled())
        logger.trace("checkCatalogExistence: true (parent does not exist");
      return true;
    }
    // In Nook mode the only thing we check for is the presentce of the
    // Trook database file as we deem this sufficient to allow overwrite
    switch (currentProfile.getDeviceMode()) {
      case Nook:
        File trookFile = new File(catalogParentFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME);
        if (! trookFile.exists()) {
          if (logger.isTraceEnabled())
            logger.trace("checkCatalogExistence: false (trook database file does not exist");
          return false;
        }
        break;
    
      default:
        File catalogFolder;
        if (currentProfile.getOnlyCatalogAtTarget()) {
          // If this option set, then catalog going to be at supplied level
          catalogFolder = catalogParentFolder;
        } else {
          catalogFolder = new File(catalogParentFolder, CatalogManager.INSTANCE.getCatalogFolderName());
        }
        // We treat catalog folder as not existing as being equivalent to
        // catalog existing as there is no problem with over-writing.
        if ((false == catalogFolder.exists()) && (true == checkCatalogFolderOnly)) {
          if (logger.isTraceEnabled())
            logger.trace("checkCatalogExistence: true (catalog folder does not exist");
          return true;
        }
    
        if (logger.isTraceEnabled())
          logger.trace("checkCatalogExistence: Check for catalog at " + catalogFolder.    getPath());
        if (!catalogFolder.exists()) {
          if (logger.isTraceEnabled())
            logger.trace("checkCatalogExistence: false (catalog folder does not exist)");
          return false;
        }
        File desktopFile = new File(catalogFolder, "desktop.css");
        if (!desktopFile.exists()) {
          if (logger.isTraceEnabled())
            logger.trace("checkCatalogExistence: false (desktop.css file does not exist)");
          return false;
        }
        File mobileFile = new File(catalogFolder, "mobile.css");
        if (!mobileFile.exists()) {
          if (logger.isTraceEnabled())
            logger.trace("checkCatalogExistence: false (desktop.css file does not exist)");
          return false;
        }
        break;
    }
    if (logger.isTraceEnabled())
      logger.trace("checkCatalogExistence: true");
    return true;
  }
}
