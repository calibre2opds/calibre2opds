package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.EBookFile;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.IndexManager;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.calibre.opf.OpfOutput;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import javax.swing.*;
import java.io.*;
import java.util.*;

// import com.sun.corba.se.impl.orbutil.concurrent.Sync;


public class Catalog {

  private static final Logger logger = Logger.getLogger(Catalog.class);
  // Some Copying stats to accumulate
  private static long copyExistHits;     // Count of Files that are copied because target does not exist
  private static long copyLengthHits;    // Count of files that are copied because lengths differ
  private static long copyDateMisses;    // Count of files that  are not copied because source older
  private static long copyCrcHits;       // Count of files that are copied because CRC different
  private static long copyCrcMisses;     // Count of files copied because CRC same
  private static long copyCrcUnchecked;  // Count of files copied because CRC check suppressed
  private static long copyToSelf;        // Count of cases where copy to self requested
  private static long copyDeleted;       // Count of files/folders deleted during copy process

  // Values read once from configuration that are used repeatedly
  private static final ConfigurationHolder currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
  private static final boolean checkCRC = currentProfile.getMinimizeChangedFiles();
  private static final String catalogFolderName = currentProfile.getCatalogFolderName();

  private File destinationFolder;

  private CatalogCallbackInterface callback;        // GUI handling routine

  //----------------------------------------------
  private static final boolean syncFilesDetail = false; // Set to true to get more details on syncFiles process
  //----------------------------------------------     (If set false, code is optimised out by compiler)
  //-----------------------------------------
  private static final boolean syncLog = true;      // Set to true to get a log of the file copy process
  //-----------------------------------------         (If set false, code is optimised out by compiler)
  private static PrintWriter syncLogFile;           // File to be used for the Sync log

  /**
   * Default Constructor
   */
  private Catalog() {
    super();
  }

  /**
   * Constructor setting callback interface for GUI
   *
   * @param callback
   */
  public Catalog(CatalogCallbackInterface callback) {
    this();
    this.callback = callback;
    CatalogContext.INSTANCE.setCallback(callback);
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
  private void syncFiles(File src, File dst) throws IOException {


    callback.incStepProgressIndicatorPosition();

    // Sanity check on parameters
    // ITIMPI:  Would it better to throw an exception to ensure we fix this
    //          as I would have thought it indicates application logic fault?
    if ((src == null) || (dst == null)) {
      if (src == null)
        logger.warn("syncFiles: Unexpected 'src' null parameter");
      else
        logger.warn("syncFiles: Unexpected 'dst' null parameter");
      return;
    }

    // Sanity check - we cannot copy a non-existent file
    // ITIMPI:  Would it better to throw an exception to ensure we fix this?
    //          However maybe it a valid check against file system having changed during run
    if (!src.exists()) {
      // ITIMPI:
      // The following code is to get around the fact that under certain conditions
      // (as yet unclear) an exists()=false can incorrectly be cached for some
      // image files generated during this run.  When the root cause is identified
      // then this workaround can be removed.
      File f = new File(src.getAbsolutePath());
      if (f.exists() == false) {
        logger.warn("syncFiles: Unexpected missing file: " + src.getAbsolutePath());
        return;
      } else {
        logger.debug("syncFiles: Incorrect caching of exists()=false status for file: " + src.getAbsolutePath());
      }
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


    // Make sure we have CachedFile type objects to work with
    // This can speed up some of the tests if it removes the
    // need to do any actual disk I/O
    CachedFile cf_src = CachedFileManager.INSTANCE.inCache(src);
    CachedFile cf_dst = CachedFileManager.INSTANCE.inCache(dst);
    if (cf_src == null) {
      cf_src = new CachedFile(src.getPath());
      if (syncFilesDetail && logger.isTraceEnabled())
        logger.trace("syncFiles: Source not in cache: " + src.getPath());
    }
    if (cf_dst == null) {
      cf_dst = CachedFileManager.INSTANCE.addCachedFile(dst);
      if (syncFilesDetail && logger.isTraceEnabled())
        logger.trace("syncFiles: Target not in cache: " + src.getPath());
      cf_dst.setTarget(true);
    }

    //-----------------------------------------------------------------------------
    // Directory Handling
    //-----------------------------------------------------------------------------

    if (cf_src.isDirectory()) {
      if (logger.isTraceEnabled())
        logger.trace("Directory " + cf_src.getName() + " Processing Started");

      callback.showMessage(src.getParentFile().getName() + File.separator + cf_src.getName());

      // Create any missing target directories
      if (!cf_dst.exists()) {
        if (logger.isTraceEnabled())
          logger.trace("Directory " + cf_dst.getName() + " Create missing target");
        if (syncLog)
          syncLogFile.printf("CREATED: %s\n", cf_dst.getName());
        dst.mkdirs();
      }

      //  Sanity check - target should be a directory
      if (!cf_dst.isDirectory()) {
        logger.warn("Directory " + cf_src.getName() + " Unexpected file with name expected for directory");
        return;
      }

      // Create current list of files that are in source locations
      File sourceFiles[] = src.listFiles();
      // Create current list of files that are in target location
      List<File> targetNotInSourceFiles = new LinkedList<File>(Arrays.asList(dst.listFiles()));

      // Now we want to:
      // - Remove any that are in the source list as they will not need to be deleted.
      // - Copy across files from source list as we go
      for (int i = 0; i < sourceFiles.length; i++) {
        File sourceFile = sourceFiles[i];
        String fileName = sourceFile.getName();
        File destFile = new File(dst, fileName);

        // ITIMPI:  Need to decide if the exists() check is redundant
        //          as it may cause an unneeded file access
        if (destFile.exists()) {
          if ((cf_src.getName().endsWith(".xml")) && (currentProfile.getGenerateOpds() == true)) {
            // XML files never needed if not generating OPDS catalog
            if (logger.isTraceEnabled())
              logger.trace("No OPDS catalog so delete " + src.getName());
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
            logger.trace("Directory " + src.getName() + " Unexpected missing target");
          CachedFileManager.INSTANCE.removeCachedFile(destFile);
        }
        // copy across the file
        syncFiles(sourceFile, destFile);
      }
      // Now actually remove the files that are still in the list of removal candidates
      for (File file : targetNotInSourceFiles) {
        Helper.delete(file);
        if (syncLog)
          syncLogFile.printf("DELETED: %s\n", file.getAbsolutePath());
        if (CachedFileManager.INSTANCE.inCache(file) != null) {
          CachedFileManager.INSTANCE.removeCachedFile(file);
        }
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
      if (!currentProfile.getGenerateOpds()) {
        if (cf_src.getName().endsWith(".xml")) {
          if (cf_dst.exists()) {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + cf_dst.getName() + ": Deleted as XML file and no OPDS catalog required");
          } else {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + cf_src.getName() + ": Ignored as XML file and no OPDS catalog required");
          }
          CachedFileManager.INSTANCE.removeCachedFile(cf_src);
          CachedFileManager.INSTANCE.removeCachedFile(cf_dst);
          return;
        }
      }

      if (syncFilesDetail && logger.isTraceEnabled())
        logger.trace("File " + cf_src.getName() + ": Checking to see if should be copied");

      // Files that do not exist on target always need copying
      // ... so we only need to check other cases
      if (!cf_dst.exists()) {
        if (syncFilesDetail && logger.isTraceEnabled())
          logger.trace("File " + cf_src.getName() + ": Copy as target is missing");
        copyExistHits++;
        copyflag = true;
        if (syncLog)
          syncLogFile.printf("COPIED (New file): %s\n", cf_dst.getName());
      } else {

        if (syncFilesDetail && logger.isTraceEnabled())
          logger.trace("File " + cf_src.getName() + ": .. exists on target");
        // Target present, so check lengths
        if (cf_src.length() != cf_dst.length()) {
          if (logger.isTraceEnabled())
            logger.trace("File " + cf_src.getName() + ": Copy as size changed");
          copyLengthHits++;
          copyflag = true;
          if (syncLog)
            syncLogFile.printf("COPIED (length changed): %s\n", cf_src.getName());
        } else {
          if (syncFilesDetail && logger.isTraceEnabled())
            logger.trace("File " + cf_src.getName() + ": .. size same on source and target");

          // Size unchanged, so check dates
          // TODO  There could be some issues if the date/time on the target
          //       is different to the machine running calibre2opds.  It might
          //       be worth adding some code to calculate the difference and
          //       use the results in the date comparisons. However for the
          //       time being we are assuming this is not an issue.
          if (cf_src.lastModified() <= cf_dst.lastModified()) {
            // Target newer than source
            if (logger.isTraceEnabled())
              logger.trace("File " + cf_src.getName() + ": Skip Copy as source is not newer");
            copyDateMisses++;
            copyflag = false;
            if (syncLog)
              syncLogFile.printf("NOT COPIED (Source not newer): %s\n", cf_dst.getName());
          } else {
            if (syncFilesDetail && logger.isTraceEnabled())
              logger.trace("File " + cf_src.getName() + ": .. source is newer");
            // Source newer, but same size so see if CRC check to be done
            if (!checkCRC) {
              if (logger.isTraceEnabled())
                logger.trace("File " + cf_src.getName() + ": Copy as CRC check not active");
              if (cf_dst.isCrc())
                if (logger.isTraceEnabled())
                  logger.trace("File " + cf_src.getName() + "CRC entry invalidated");
              cf_dst.clearCrc();
              copyCrcUnchecked++;
              copyflag = true;
              if (syncLog)
                syncLogFile.printf("COPIED (CRC check not active): %s\n", cf_src.getName());
            } else {
              try {
                if (cf_src.getCrc() != cf_dst.getCrc()) {
                  if (logger.isTraceEnabled())
                    logger.trace("File " + cf_src.getName() + ": Copy as CRC's different");
                  copyCrcHits++;
                  copyflag = true;
                  if (syncLog)
                    syncLogFile.printf("COPIED (CRC changed): %s\n", cf_src.getName());
                } else {
                  if (logger.isTraceEnabled())
                    logger.trace("File " + cf_src.getName() + ": Skip copy as CRC's match");
                  copyCrcMisses++;
                  copyflag = false;
                  if (syncLog)
                    syncLogFile.printf("NOT COPIED (CRC same): %s\n", cf_src.getName());
                }
              } catch (Exception e) {
                // Failed to get CRC for some reason
                if (logger.isDebugEnabled())
                  logger.debug("File " + cf_src.getName() + ": Copy as CRC calculation failed");
                copyCrcMisses++;
                copyflag = false;
              } // End of CRC catch block
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
          logger.debug("Copying file " + cf_src.getName());
        Helper.copy(cf_src, cf_dst);
      }
    }  // End of File Handling section
  }

  /**
   * @return
   */
  private Element getAboutEntry() {
    String title = Localization.Main.getText("about.title", Constants.PROGTITLE);
    String urn = "urn:calibre2opds:about";
    String url = "http://wiki.mobileread.com/wiki/Calibre2opds";
    String summary = Localization.Main.getText("about.summary");
    // #751211: Use external icons option
    String icon = currentProfile.getExternalIcons() ? StanzaConstants.ICONFILE_ABOUT : StanzaConstants.ICON_ABOUT;
    return FeedHelper.INSTANCE.getAboutEntry(title, urn, url, summary, icon);
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
        List<Element> htmlElements = JDOM.INSTANCE.convertBookCommentToXhtml(text);
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
   * -----------------------------------------------
   * Control the overall catalog generation process
   * -----------------------------------------------
   *
   * @throws IOException
   */
  public void createMainCatalog() throws IOException {
    long countMetadata;     // Count of files for which ePub metadata is updated
    long countThumbnails;   // Count of thumbnail files that are generated/updated
    long countCovers;       // Count of image files that are generated/updated

    // reinitialize caches (in case of multiple calls in the same session)
    CachedFileManager.INSTANCE.initialize();
    CatalogContext.INSTANCE.initialize();
    CatalogContext.INSTANCE.setCallback(callback);

    // Do some sanity checks on the settings before starting the generation
    if (logger.isTraceEnabled())
      logger.trace("Start sanity checks against user errors that might cause data loss");

    // Make sure that at least one of OPDS and HTML catalog types is activated
    if ((!currentProfile.getGenerateOpds()) && (!currentProfile.getGenerateHtml())) {
      logger.warn(Localization.Main.getText("error.nogeneratetype"));
      JOptionPane.showMessageDialog(null, Localization.Main.getText("error.nogeneratetype"), "error", JOptionPane.ERROR_MESSAGE);
      return;
    }


    // Check that the catalog folder is actually set to something and not an empty string
    String catalogFolderName = currentProfile.getCatalogFolderName();
    if (catalogFolderName.length() == 0) {
      logger.warn(Localization.Main.getText("error.nocatalog"));
      JOptionPane.showMessageDialog(null, Localization.Main.getText("error.nocatalog"), null, JOptionPane.ERROR_MESSAGE);
      return;
    }
    // Check that folder specified as library folder actually contains a calibre database
    File calibreLibraryFolder = currentProfile.getDatabaseFolder();
    if (!DatabaseManager.INSTANCE.databaseExists()) {
      logger.warn(Localization.Main.getText("error.nodatabase", calibreLibraryFolder));
      callback.errorOccured(Localization.Main.getText("error.nodatabase", calibreLibraryFolder), null);
      return;
    }
    // Additional checks if destination folder also set.
    File calibreTargetFolder = currentProfile.getTargetFolder();
    if ((calibreTargetFolder != null)) {
      // Check that target folder (if set) is not set to be the same as the library folder
      if (calibreLibraryFolder.getAbsolutePath().equals(calibreTargetFolder.getAbsolutePath())) {
        logger.warn(Localization.Main.getText("error.targetsame"));
        JOptionPane.showMessageDialog(null, Localization.Main.getText("error.targetsame"), "error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      // Check that target folder (if set) is not set to be a higher level than the library folder
      // (which would have unfortunate consequences when deleting during sync operation)
      if (calibreLibraryFolder.getAbsolutePath().startsWith(calibreTargetFolder.getAbsolutePath())) {
        logger.warn(Localization.Main.getText("error.targetparent"));
        JOptionPane.showMessageDialog(null, Localization.Main.getText("error.targetparent"), "error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      // If not already a catalog at target, give overwrite warning
      if (!checkCatalogExistence(calibreTargetFolder, false)) {
        logger.warn(Localization.Main.getText("gui.confirm.clear", calibreTargetFolder));
        if (JOptionPane.NO_OPTION == JOptionPane
            .showConfirmDialog(null, Localization.Main.getText("gui.confirm.clear", calibreTargetFolder), "WARNING", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)) {
          if (logger.isTraceEnabled())
            logger.trace("User declined to overwrite folder " + calibreTargetFolder);
          return;
        }

      }
    }
    logger.trace("calibreTargetFolder set to " + calibreTargetFolder);
    // If catalog folder exists, then see if it looks like it already contains a catalog
    // and if not warn the user that existing contents will be lost and get confirmation OK
    File catalogParentFolder = calibreTargetFolder;
    // N.B.  Whether calibreTargetFolder was set to be different to the Library folder is mode dependent
    if (catalogParentFolder == null || catalogParentFolder.getName().length() == 0) {
      if (!checkCatalogExistence(calibreLibraryFolder, true)) {
        logger.warn(Localization.Main.getText("gui.confirm.clear", calibreLibraryFolder));
        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
            Localization.Main.getText("gui.confirm.clear", calibreLibraryFolder + File.separator + currentProfile.getCatalogFolderName()), "WARNING",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
          if (logger.isTraceEnabled())
            logger.trace("User declined to overwrite folder " + calibreLibraryFolder);
          return;
        }
      }
      catalogParentFolder = calibreLibraryFolder;
    }
    logger.trace("catalogParentFolder set to " + catalogParentFolder);
    File catalogFolder = new File(catalogParentFolder, currentProfile.getCatalogFolderName());
    if (logger.isTraceEnabled())
      logger.trace("New catalog to be generated at " + catalogFolder.getPath());

    // If copying catalog back to database folder check it is safe to overwrite
    if (true == currentProfile.getCopyToDatabaseFolder()) {
      logger.debug("STARTING: Copy Catalog Folder to Database Folder");
      File targetFolder = currentProfile.getDatabaseFolder();
      if ((DeviceMode.Dropbox != currentProfile.getDeviceMode()) && (!checkCatalogExistence(targetFolder, true))) {
        logger.warn(Localization.Main.getText("gui.confirm.clear", targetFolder));
        if (JOptionPane.NO_OPTION == JOptionPane
            .showConfirmDialog(null, Localization.Main.getText("gui.confirm.clear", targetFolder + File.separator + currentProfile.getCatalogFolderName()),
                "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
          if (logger.isTraceEnabled())
            logger.trace("User declined to overwrite folder " + targetFolder);
          return;
        }
      }
      catalogParentFolder = calibreLibraryFolder;
    }
    logger.trace("Passed sanity checks, so proceed with generation");

    // Sanity checks OK - get on with generation

    callback.dumpOptions();

    callback.startCreateMainCatalog();

    long now = System.currentTimeMillis();
    CachedFileManager.INSTANCE.setCacheFolder(catalogFolder);
    if (checkCRC) {
      if (logger.isTraceEnabled())
        logger.trace("Loading Cache");
      callback.showMessage(Localization.Main.getText("info.step.loadingcache"));
      CachedFileManager.INSTANCE.loadCache();
    } else {
      if (logger.isTraceEnabled())
        logger.trace("Deleting Cache");
      CachedFileManager.INSTANCE.deleteCache();
    }
    logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));

    //  Initialise temporary area for generating a catalog files
    File temp = File.createTempFile("calibre2opds", "");
    String tempPath = temp.getAbsolutePath();
    temp.delete();
    destinationFolder = new File(tempPath);

    if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
      destinationFolder = new File(destinationFolder, currentProfile.getCatalogFolderName() + Constants.TROOK_FOLDER_EXTENSION);
      if (logger.isTraceEnabled())
        logger.trace("Nook mode - destinationFolder set to " + destinationFolder);
    }
    destinationFolder.mkdirs();
    CatalogContext.INSTANCE.getCatalogManager().setDestinationFolder(destinationFolder);

    callback.startReadDatabase();
    now = System.currentTimeMillis();

    DataModel.INSTANCE.reset();
    RemoveFilteredOutBooks.INSTANCE.runOnDataModel();

    List<Book> books = DataModel.INSTANCE.getListOfBooks();
    if (Helper.isNullOrEmpty(books)) {
      JOptionPane.showMessageDialog(null, Localization.Main.getText("error.nobooks"), "error", JOptionPane.ERROR_MESSAGE);
      logger.info(Localization.Main.getText("error.nobooks"));
      return;
    }
    DataModel.INSTANCE.preloadDataModel();

    callback.endReadDatabase(System.currentTimeMillis() - now);

    String filename = SecureFileManager.INSTANCE.encode("index.xml");


    if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
      // prepare the Trook specific search database
      TrookSpecificSearchDatabaseManager.INSTANCE.setDatabaseFile(new File(destinationFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME));
      TrookSpecificSearchDatabaseManager.INSTANCE.getConnection();
    }


    String title = Localization.Main.getText("home.title");
    String urn = "calibre:catalog";

    String urlExt = "../" + filename;
    Breadcrumbs breadcrumbs = Breadcrumbs.newBreadcrumbs(title, urlExt);

    Element main = FeedHelper.INSTANCE.getFeedRootElement(null, title, urn, urlExt);

    Element entry;

    /* About entry */
    if (currentProfile.getIncludeAboutLink()) {
      entry = getAboutEntry();
      if (entry != null)
        main.addContent(entry);
    }

    /* Authors */
    logger.debug("STARTING: Generating Authors catalog");
    callback.startCreateAuthors(DataModel.INSTANCE.getListOfAuthors().size());
    now = System.currentTimeMillis();
    entry = new AuthorsSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
    if (entry != null)
      main.addContent(entry);
    callback.endCreateAuthors(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Authors catalog");

    /* Tags */
    logger.debug("STARTING: Generating Tags catalog");
    callback.startCreateTags(DataModel.INSTANCE.getListOfTags().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateTags()) {
      entry = TagSubCatalog.getTagSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
      if (entry != null)
        main.addContent(entry);
    }
    callback.endCreateTags(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Tags catalog");

    /* Series */
    logger.debug("STARTING: Generating Series catalog");
    callback.startCreateSeries(DataModel.INSTANCE.getListOfSeries().size());
    now = System.currentTimeMillis();
    entry = new SeriesSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
    if (entry != null)
      main.addContent(entry);
    callback.endCreateSeries(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Series catalog");

    /* Recent books */
    logger.debug("STARTING: Generating Recent catalog");
    int nbRecentBooks = Math.min(currentProfile.getBooksInRecentAdditions(), DataModel.INSTANCE.getListOfBooks().size());
    callback.startCreateRecent(nbRecentBooks);
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateRecent()) {
      Composite<Element, String> recent = new RecentBooksSubCatalog(books).getSubCatalogEntry(breadcrumbs);
      if (recent != null) {
        main.addContent(recent.getFirstElement());
      }
    }
    callback.endCreateRecent(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Recent catalog");

    /* Rated books */
    logger.debug("STARTING: Generating Ratings catalog");
    callback.startCreateRated(DataModel.INSTANCE.getListOfBooks().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateRatings()) {
      entry = new RatingsSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
      if (entry != null)
        main.addContent(entry);
    }
    callback.endCreateRated(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Ratings catalog");

    /* All books */
    logger.debug("STARTED: Generating All Books catalog");
    callback.startCreateAllbooks(DataModel.INSTANCE.getListOfBooks().size());
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateAllbooks()) {
      entry = new AllBooksSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
      if (entry != null)
        main.addContent(entry);
    }
    callback.endCreateAllbooks(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating All Books catalog");

    /* Featured catalog */
    String savedSearchName = ConfigurationManager.INSTANCE.getCurrentProfile().getCustomCatalogSavedSearchName();
    if (Helper.isNotNullOrEmpty(savedSearchName)) {
      logger.debug("STARTED: Generating Featured books catalog");
      String savedSearch = DataModel.INSTANCE.getMapOfSavedSearches().get(savedSearchName);
      if (Helper.isNullOrEmpty(savedSearch))
        savedSearch = DataModel.INSTANCE.getMapOfSavedSearches().get(savedSearchName.toUpperCase());
      if (Helper.isNotNullOrEmpty(savedSearch)) {
        CalibreQueryInterpreter interpreter = new CalibreQueryInterpreter(savedSearch);
        try {
          BookFilter filter = interpreter.interpret();
          List<Book> featuredBooks = FilterHelper.filter(filter, books);
          callback.startCreateFeaturedBooks(featuredBooks.size());
          now = System.currentTimeMillis();
          Composite<Element, String> featuredCatalog = new FeaturedBooksSubCatalog(featuredBooks).getSubCatalogEntry(breadcrumbs);
          if (featuredCatalog != null) {
            //main.addContent(featuredCatalog.getFirstElement());
            // add a "featured" link - 6 places the link right wher we want it...
            main.addContent(6, FeedHelper.INSTANCE
                .getFeaturedLink(featuredCatalog.getSecondElement(), ConfigurationManager.INSTANCE.getCurrentProfile().getCustomCatalogTitle()));
            // add the actual catalog
            main.addContent(featuredCatalog.getFirstElement());
          }
          callback.endCreateFeaturedBooks(System.currentTimeMillis() - now);
          logger.debug("COMPLETED: Generating Featured books catalog");
        } catch (CalibreQueryInterpreter.InterpretException e) {
          callback.errorOccured("Error occured while interpreting the featured books saved search", e);
        }
      }
    }

    File outputFile = new File(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), filename);
    Document document = new Document();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);

      // write the element to the file
      document.addContent(main);
      JDOM.INSTANCE.getOutputter().output(document, fos);
    } finally {
      if (fos != null)
        fos.close();
    }

    /* Thumbnails */
    logger.debug("STARTING: Generating Thumbnails");
    int nbThumbnails = CatalogContext.INSTANCE.getThumbnailManager().getNbImagesToGenerate();
    callback.startCreateThumbnails(nbThumbnails);
    now = System.currentTimeMillis();
    countThumbnails = CatalogContext.INSTANCE.getThumbnailManager().generateImages();
    callback.endCreateThumbnails(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Thumbnails");


    /* Reduced Covers */
    logger.debug("STARTING: Generating Reduced Covers");
    int nbCovers = CatalogContext.INSTANCE.getThumbnailManager().getNbImagesToGenerate();
    callback.startCreateCovers(nbCovers);
    now = System.currentTimeMillis();
    countCovers = CatalogContext.INSTANCE.getCoverManager().generateImages();
    callback.endCreateCovers(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Reduced Covers");

    /* Javascript database */
    logger.debug("STARTING: Generating Javascript database");
    long nbKeywords = IndexManager.INSTANCE.size();
    callback.startCreateJavascriptDatabase(nbKeywords);
    now = System.currentTimeMillis();
    if (currentProfile.getGenerateIndex())
      IndexManager.INSTANCE.exportToJavascriptArrays();
    callback.endCreateJavascriptDatabase(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Generating Javascript database");

    /* Epub metadata reprocessing */
    logger.debug("STARTING: Processing ePub Metadata");
    callback.startReprocessingEpubMetadata(DataModel.INSTANCE.getListOfBooks().size());
    now = System.currentTimeMillis();
    countMetadata = 0;
    if (currentProfile.getReprocessEpubMetadata()) {
      for (Book book : DataModel.INSTANCE.getListOfBooks()) {
        callback.incStepProgressIndicatorPosition();
        if (shouldReprocessEpubMetadata(book)) {
          // callback.showMessage(book.getTitle());
          callback.showMessage(book.getAuthors() + ": " + book.getTitle());
          new OpfOutput(book).processEPubFile();
          countMetadata++;
        }
      }
    }
    callback.endReprocessingEpubMetadata(System.currentTimeMillis() - now);
    logger.debug("COMPLETED: Processing ePub Metadata");

    // create the same file as html
    logger.debug("STARTED: Generating HTML Files");
    CatalogContext.INSTANCE.getHtmlManager().generateHtmlFromXml(document, outputFile, HtmlManager.FeedType.MainCatalog);
    logger.debug("COMPLETED: Generating HTML Files");

    // copy the resource files to the catalog folder
    logger.debug("STARTED: Copying Resource files");
    for (String resource : Constants.FILE_RESOURCES) {
      File resourceFile = new File(destinationFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName() + "/" + resource);
      InputStream resourceStream = JDOM.class.getResourceAsStream(resource);
      Helper.copy(resourceStream, resourceFile);
    }
    logger.debug("COMPLETED: Copying Resource files");

    if (syncLog)
      syncLogFile = new PrintWriter(ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants
          .SYNCFILE_NAME);

    /* copy the catalogs (and books, if the target folder is set) to the destination folder */

    // reset stats fields for this run
    copyExistHits = copyLengthHits = copyCrcUnchecked = copyCrcHits = copyCrcMisses = copyDateMisses = copyCrcUnchecked = 0;
    // if the target folder is set, copy/syncFiles the library there
    int nbFilesToCopyToTarget = CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy().size();
    callback.startCopyLibToTarget(nbFilesToCopyToTarget);
    if (currentProfile.getTargetFolder() != null) {
      File targetFolder = currentProfile.getTargetFolder();

      if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
        targetFolder = new File(targetFolder, currentProfile.getCatalogFolderName() + Constants.TROOK_FOLDER_EXTENSION);
        if (logger.isTraceEnabled())
          logger.trace("Nook mode - targetFolder set to" + targetFolder);
      }
      // syncFiles the eBook files
      logger.debug("STARTING: syncFiles eBook files to target");
      now = System.currentTimeMillis();
      for (String pathToCopy : CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy()) {
        CachedFile sourceFile = CachedFileManager.INSTANCE.addCachedFile(currentProfile.getDatabaseFolder(), pathToCopy);
        File targetFile = CachedFileManager.INSTANCE.addCachedFile(targetFolder, pathToCopy);
        syncFiles(sourceFile, targetFile);
      }
      logger.debug("COMPLETED: syncFiles eBook files to target");

      callback.showMessage(Localization.Main.getText("info.step.tidyingtarget"));

      // delete the target folders that were not in the source list (minus the catalog folder, of course)
      logger.debug("STARTING: Build list of files to delete from target");
      Set<File> usefulTargetFiles = new TreeSet<File>();
      List<String> sourceFiles = new LinkedList<String>(CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy());
      for (String sourceFile : sourceFiles) {
        File targetFile = new File(targetFolder, sourceFile);
        while (targetFile != null) {
          usefulTargetFiles.add(targetFile);
          targetFile = targetFile.getParentFile();
        }
      }
      logger.debug("COMPLETED: Build list of files to delete from target");

      logger.debug("STARTED: Creating list of files on target");
      List<File> existingTargetFiles = Helper.listFilesIn(targetFolder);
      logger.debug("COMPLETED: Creating list of files on target");
      String targetCatalogFolderPath = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName()).getAbsolutePath();
      String calibreFolderPath = currentProfile.getDatabaseFolder().getAbsolutePath();

      logger.debug("STARTING: Delete superfluous files from target");
      for (File existingTargetFile : existingTargetFiles) {
        if (!usefulTargetFiles.contains(existingTargetFile)) {
          if (!existingTargetFile.getAbsolutePath().startsWith(targetCatalogFolderPath)) // don't delete the catalog files
          {
            if (!existingTargetFile.getAbsolutePath().startsWith(calibreFolderPath)) // as an additional security, don't delete anything in the Calibre library
            {
              if (logger.isTraceEnabled())
                logger.trace("deleting " + existingTargetFile.getPath());
              Helper.delete(existingTargetFile);
              if (syncLog)
                syncLogFile.printf("DELETED: %s\n", existingTargetFile);
              // Ensure no longer in cache
              CachedFileManager.INSTANCE.removeCachedFile(existingTargetFile);
            }
          }
        }
      }
      logger.debug("COMPLETED: Delete superfluous files from target");
    }
    callback.endCopyLibToTarget(System.currentTimeMillis() - now);

    long nbCatalogFilesToCopyToTarget = Helper.count(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder());
    callback.startCopyCatToTarget(nbCatalogFilesToCopyToTarget);
    now = System.currentTimeMillis();
    // syncFiles the temporary catalog folder to the destination (either the target folder, or the library folder when the option is set,
    // or both)
    if (currentProfile.getTargetFolder() != null) {
      logger.debug("STARTING: syncFiles Catalog Folder");
      File targetFolder = currentProfile.getTargetFolder();
      if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
        logger.debug("...in NOOK mode");
        targetFolder = new File(targetFolder, currentProfile.getCatalogFolderName() + Constants.TROOK_FOLDER_EXTENSION);
        if (logger.isTraceEnabled())
          logger.trace("targetFolder=" + targetFolder);
        if (currentProfile.getZipTrookCatalog()) {
          // when publishing to the Nook, archive the catalog into a big zip file (easier to transfer, and Trook knows how to read it!)
          File targetCatalogZipFile = new File(targetFolder, Constants.TROOK_CATALOG_FILENAME);
          Helper.recursivelyZipFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), true, targetCatalogZipFile);
        } else {
          File targetCatalogFolder = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
          syncFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), targetCatalogFolder);
        }
        // when publishing to the Nook, don't forget to copy the search database
        File destinationFile = new File(targetFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME);
        Helper.copy(TrookSpecificSearchDatabaseManager.INSTANCE.getDatabaseFile(), destinationFile);
        // Also need to make sure catalog.xml exists for Trook use
        // Use index.xml already generated
        // logger.trace("dewstinationFolder=" + destinationFolder);
        // logger.trace("targetFolder" + targetFolder);
        // logger.trace("getCatalogFolderName=" + currentProfile.getCatalogFolderName());
        File indexFile = new File(destinationFolder, Constants.NOOK_CATALOG_FOLDERNAME + "/index.xml");
        // replicate it to catalog.xml in final target
        File catalogFile = new File(targetFolder, Constants.NOOK_CATALOG_FOLDERNAME + "/catalog.xml");
        if (logger.isTraceEnabled())
          logger.trace("copy '" + indexFile + "' to '" + catalogFile + "'");
        Helper.copy(indexFile, catalogFile);
      } else {
        logger.debug("...NOT in NOOK mode");
        File targetCatalogFolder = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
        syncFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), targetCatalogFolder);
      }
      logger.debug("COMPLETED: syncFiles Catalog Folder");
    }

    if (currentProfile.getCopyToDatabaseFolder()) {
      logger.debug("STARTING: Copy Catalog Folder to Database Folder");
      File targetFolder = currentProfile.getDatabaseFolder();
      File targetCatalogFolder = new File(targetFolder, currentProfile.getCatalogFolderName());
      if (logger.isTraceEnabled())
        logger.trace("syncfiles (" + CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder() + ", " + targetCatalogFolder);
      syncFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), targetCatalogFolder);
      logger.debug("COMPLETED: Copy Catalog Folder to Database Folder");
    }

    callback.endCopyCatToTarget(System.currentTimeMillis() - now);

    if (syncLog) {
      logger.info("Sync Log: " + ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
    }
    // Save the CRC cache to the catalog folder
    // We always do this even if CRC Checking not enabled
    now = System.currentTimeMillis();
    logger.info(Localization.Main.getText("info.step.savingcache"));
    callback.showMessage(Localization.Main.getText("info.step.savingcache"));
    CachedFileManager.INSTANCE.saveCache();
    logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));

    // delete the temporary folder
    now = System.currentTimeMillis();
    logger.info(Localization.Main.getText("info.step.deleteingfiles"));
    callback.showMessage(Localization.Main.getText("info.step.deletingfiles"));
    Helper.delete(destinationFolder);
    logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));

    // save the SecureFileManager data
    callback.showMessage("Saving Cache data");
    SecureFileManager.INSTANCE.save();

    if (syncLog) {
      syncLogFile.println();
      syncLogFile.println(Localization.Main.getText("stats.copy.header"));
      syncLogFile.println(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
      syncLogFile.println(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
      syncLogFile.println(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
      syncLogFile.println(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
      syncLogFile.println(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
      syncLogFile.println(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
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
    logger.info(String.format("%8d  ", countThumbnails) + Localization.Main.getText("stats.run.thumbnails"));
    logger.info(String.format("%8d  ", countCovers) + Localization.Main.getText("stats.run.covers"));
    logger.info("");
    logger.info(Localization.Main.getText("stats.copy.header"));
    logger.info(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
    logger.info(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
    logger.info(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
    logger.info(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
    logger.info(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
    logger.info(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
    logger.info("");
    if (copyToSelf != 0)
      logger.warn(String.format("%8d  ", copyToSelf) + Localization.Main.getText("stats.copy.toself"));

    // Now work put where to tell uer result has been placed

    String where = null;
    if (logger.isTraceEnabled())
      logger.trace("try to determine where the results have been put");
    if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
      if (logger.isTraceEnabled())
        logger.trace("Nook mode: set to " + Localization.Main.getText("info.step.done.nook"));
      where = Localization.Main.getText("info.step.done.nook");
    } else if (currentProfile.getTargetFolder() != null) {
      if (logger.isTraceEnabled())
        logger.trace("TargetFolder: " + currentProfile.getTargetFolder().getAbsolutePath());
      where = currentProfile.getTargetFolder().getAbsolutePath();
    }

    if (currentProfile.getCopyToDatabaseFolder()) {
      if (logger.isTraceEnabled())
        logger.trace("CopyToDatabawseFolder set");
      if (where != null) {
        if (logger.isTraceEnabled())
          logger.trace(where + " " + Localization.Main.getText("info.step.done.andYourDb"));
        where = where + " " + Localization.Main.getText("info.step.done.andYourDb");
      } else {
        if (logger.isTraceEnabled())
          logger.trace("DatabaseFolder=" + currentProfile.getDatabaseFolder().getAbsolutePath());
        where = currentProfile.getDatabaseFolder().getAbsolutePath();
      }
    }

    if (where == null) {
      if (logger.isTraceEnabled())
        logger.trace("outputfile.getParent=" + outputFile.getParent());
      where = outputFile.getParent();
    }

    callback.endCreateMainCatalog(where, CatalogContext.INSTANCE.getHtmlManager().getTimeInHtml());

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
    File catalogFolder = new File(catalogParentFolder, currentProfile.getCatalogFolderName());

    // We treat catalog folder as not existing as being equivalent to
    // catalog existing as there is no problem with over-writing.
    if ((false == catalogFolder.exists()) && (true == checkCatalogFolderOnly)) {
      if (logger.isTraceEnabled())
        logger.trace("checkCatalogExistence: true (catalog folder does not exist");
      return true;
    }

    if (logger.isTraceEnabled())
      logger.trace("checkCatalogExistence: Check for catalog at " + catalogFolder.getPath());
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
    if (logger.isTraceEnabled())
      logger.trace("checkCatalogExistence: true");
    return true;
  }
}
