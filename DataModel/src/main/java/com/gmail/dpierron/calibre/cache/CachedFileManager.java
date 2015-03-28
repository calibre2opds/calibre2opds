package com.gmail.dpierron.calibre.cache;
/**
 * This class is responsible for managing a cache of CachedFile
 * objects.
 *
 * As well as providing the obvious support for adding/removing testing
 * for such objects it also provides for the cache to be written
 * to file at the end of a run and reloaded at the beginning of
 * the next run.   The main purpose of this is to avoid having
 * to recalculate the CRC (which is an expensive operation) between
 * runs if it can be avoided.
 *
 * NOTE:  There should only ever be one instance of this class, so all
 *        global variables and methods are declared static
 */

import com.gmail.dpierron.calibre.gui.CatalogCallbackInterface;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class CachedFileManager {

  private final static Logger logger = Logger.getLogger(CachedFileManager.class);
  private static Map<String, CachedFile> cachedFilesMap = new HashMap<String, CachedFile>();
  private static File cacheFile = null;
  private final static String CALIBRE2OPDS_LOG_FILENAME = "c2o_cache";
  private final static String CALIBRE2OPDS_LOG_FILENAME_OLD = "calibre2opds.cache";

  private static long savedCount = 0;
  private static long ignoredCount = 0;

  public static void reset() {
    cachedFilesMap = null;    // Force release any currently assigned map
    cachedFilesMap = new HashMap<String, CachedFile>();
  }

  /**
   * Determine if an entry is already in the cache
   *
   * @param cf CachedFile object to check
   * @return null if not present, object otherwise
   */
  public static CachedFile inCache(CachedFile cf) {
    CachedFile cf_result = cachedFilesMap.get(cf.getPath());
    if (logger.isTraceEnabled())  logger.trace("inCache=" + (cf_result != null) + ": " + cf.getPath());
    return cf_result;
  }

  /**
   * Determine if an entry is already in the cache
   *
   * @param f File object to check
   * @return null if not present, object otherwise
   */
  public static CachedFile inCache(File f) {
    CachedFile cf_result = cachedFilesMap.get(f.getPath());
    if (logger.isTraceEnabled())  logger.trace("inCache=" + (cf_result != null) + ": " + f.getPath());
    return cf_result;
  }


  /**
   * Check the entry against the cache and if needed
   * create a new entry.
   *
   * @param cf CachedFile object representing file
   * @return A CachedFile object for the given path
   */
  public static CachedFile addCachedFile(CachedFile cf) {
    String path = cf.getPath();
    CachedFile cf2 = inCache(cf);
    if (cf2 == null) {
      cf2 = new CachedFile(path);
      cachedFilesMap.put(path, cf2);
      if (logger.isTraceEnabled())  logger.trace("Added CachedFile: " + path);
    }
    return cf2;
  }


  /**
   * Check the entry against the cache and if needed
   * create a new entry.
   *
   * @param f File object representing file
   * @return A CachedFile object for the given path
   */
  public static CachedFile addCachedFile(File f) {
    String path = f.getPath();
    CachedFile cf = inCache(f);
    if (cf == null) {
      cf = new CachedFile(path);
      cachedFilesMap.put(path, cf);
      if (logger.isTraceEnabled())  logger.trace("Added file to cache: " + path);
    }
    return cf;
  }


  /**
   * Add a file to the file cache that is a source file
   *
   * @param parent Folder that will contain the file
   * @param child  Filename
   * @return CachedFile object corresponding to file
   */
  public static CachedFile addCachedFile(File parent, String child) {
    return addCachedFile(new File(parent, child));
  }

  /**
   * Remove the entry from the cache (if it is present).
   *
   * @param f File object representing file
   */
  public static void removeCachedFile(File f) {
    String path = f.getPath();
    if (cachedFilesMap.containsKey(path)) {
      cachedFilesMap.remove(path);
      if (logger.isTraceEnabled())  logger.trace("Remove CachedFile: " + path);
    } else {
      if (logger.isTraceEnabled())  logger.trace("Remove CachedFile (not found): " + path);
    }
  }

  /**
   * Remove the entry from the cache (if it is present).
   *
   * @param cf CachedFile object representing file
   */
  public static void removeCachedFile(CachedFile cf) {
    removeCachedFile((File)cf);
  }


  /**
   * Set the location for any existing cache file
   *
   * @param cf Specify the folder to hold the cache
   *           This is normally the catalog sub-folder of the target folder
   */
  public static void setCacheFolder(File cf) {
    assert cf != null;    // cf must not be null
    cacheFile = new File(cf, CALIBRE2OPDS_LOG_FILENAME);
    if (logger.isDebugEnabled()) logger.debug("CRC Cache file set to " + cacheFile.getPath());

    // Check for old name, and if necessary rename to new style
    File cacheFileOld = new File(cf, CALIBRE2OPDS_LOG_FILENAME_OLD);
    if (cacheFileOld.exists()) {
        if (logger.isDebugEnabled()) logger.debug("Cache file found with name " + CALIBRE2OPDS_LOG_FILENAME_OLD + ", rename to " + CALIBRE2OPDS_LOG_FILENAME);
        if (cacheFileOld.renameTo(cacheFile)) {
          if (logger.isDebugEnabled())logger.debug("Cache file renamed to " + CALIBRE2OPDS_LOG_FILENAME);
        } else {
          if (logger.isDebugEnabled()) logger.debug("ERROR: failed to rename cache file");
      }
    }
  }

  /**
   * Save the current cache for potential later re-use
   * You can specify a path that should beignored so that
   * one can avoid saving objects for the TEMP area
   *
   * N.B. the setCacheFolder() call must have been used
   */
  public static void saveCache(String pathToIgnore, CatalogCallbackInterface callback) {

    // Check Cache folder has been set
    if (logger.isDebugEnabled()) logger.debug("saveCache; pathToIgnore=" + pathToIgnore);
    if (cacheFile == null) {
      if (logger.isDebugEnabled()) logger.debug("Aborting saveCache() as cacheFile not set");
      return;
    }

    savedCount = 0;
    ignoredCount = 0;
    long isDirectory = 0;
    long pathMatch  = 0;
    long crcNotKnown = 0;
    long notExists = 0 ;
    long notUsed = 0;
    long countChecked = 0;
    ObjectOutputStream os = null;
    BufferedOutputStream bs = null;
    FileOutputStream fs = null;
    long countPercent = cachedFilesMap.entrySet().size()/100;       // Use to avoid too frequent GUI updates
    if (callback != null ) callback.setProgressMax(100);
    deleteCache();

    try {
      try {
        if (logger.isDebugEnabled()) logger.debug("STARTED Saving cacheFile entries to " + cacheFile.getPath());
        // Open cache file (objects)
        fs = new FileOutputStream(cacheFile);         // Open File
        assert fs != null: "saveCache: fs should never be null at this point";
        bs = new BufferedOutputStream(fs,512 * 1024); // Add buffering
        assert bs != null: "saveCache: bs should never be null at this point";
        os = new ObjectOutputStream(bs);              // Add object handling
        assert os != null: "saveCache: os should never be null at this point";

        // Write out the cache entries
        CachedFile cf;
        String key = null;          // Force initialise to avoid later compile time warnings
        for (Map.Entry<String, CachedFile> m : cachedFilesMap.entrySet()) {
          // Only update GUI at 1% intervals (reduces overhead)
          if ((countChecked % countPercent) == 0
          &&  callback != null) callback.incStepProgressIndicatorPosition();
          countChecked++;

          cf = m.getValue();
          if (logger.isTraceEnabled()) key = m.getKey();

          // We d not want to cache files that have unvalidated cache values.
          if (! cf.isCachedValidated()) {
            if (logger.isTraceEnabled()) logger.trace("saveCache: Not used.  Not saving CachedFile " + key);
            notUsed++;
            ignoredCount++;
            continue;
          }
          // No point in caching enrtries for fiels in the temporary area.
          if (pathToIgnore != null && cf.getPath().startsWith(pathToIgnore)) {
            if (logger.isTraceEnabled()) logger.trace("saveCache: PathtoIgnore matches  Not saving CachedFile " + key);
            pathMatch++;
            ignoredCount++;
            continue;
          }
          // We are only interested in caching entries for which the CRC is known
          // as this is the expensive operation we do not want to do unnecessarily
          if (!cf.isCrc()) {
            if (logger.isTraceEnabled()) logger.trace("saveCache: CRC not known.  Not saving CachedFile " + key);
            crcNotKnown++;
            ignoredCount++;
            continue;
          }
          // No point in caching entries for non-existent files
          if (!cf.exists()) {
            if (logger.isTraceEnabled()) logger.trace("saveCache: Not exists.  Not saving CachedFile " + key);
            notExists++;
            ignoredCount++;
            continue;
          }
          // We do not bother with entries pointing at directories.
          if (cf.isDirectory()) {
            if (logger.isTraceEnabled()) logger.trace("saveCache: isDirectory  Not saving CachedFile " + key);
            isDirectory++;
            ignoredCount++;
            continue;
          }
          os.writeObject(cf);
          if (logger.isTraceEnabled())  logger.trace("saveCache: Saved " + key);
          savedCount++;
        }
      } finally {
        try {
          if (os != null) os.close();
          if (bs != null) bs.close();
          if (fs != null) fs.close();
        } catch (Exception e) {
          // Do nothing - we ignore an error at this point
          // Having said that, an error here is a bit unexpected so lets log it when testing
          logger.warn("saveCache: Unexpected error\n" + e);
        }
      }
    } catch (IOException e) {
      logger.warn("saveCache: Exception trying to write cache:\n" + e);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("saveCache: Cache Entries Saved:   " + savedCount);
      logger.debug("saveCache: Cache Entries Ignored: " + ignoredCount);
      logger.debug("saveCache: isDirectory=" + isDirectory + ", notUsed=" + notUsed + ", notExists=" + notExists + ", crcNotKnown=" + crcNotKnown + ", pathMatch=" + pathMatch);
      logger.debug("saveCache: COMPLETED Saving CRC cache to file " + cacheFile.getPath());
    }
  }

  /**
   * Initialize the cache if there is a saved one present
   *
   * N.B. the setCacheFolder() call must have been used
   */
  public static void loadCache() {

    reset();               // Reset cache to be empty

    // Check cache folder has been specified
    if (cacheFile == null) {
      if (logger.isTraceEnabled())  logger.trace("Aborting loadCache() as cache folder not set");
      return;
    }

    if (!cacheFile.exists()) {
      if (logger.isDebugEnabled())  logger.debug("Exiting loadCache() as cache file not present");
      return;
    }
    ObjectInputStream os = null;
    FileInputStream fs = null;
    BufferedInputStream bs = null;
    long loadedCount = 0;
    try {
      if (logger.isDebugEnabled()) logger.debug("STARTED Loading CRC cache from file " + cacheFile.getPath());
      // Open Cache file
      fs = new FileInputStream(cacheFile);          // Open file
      assert fs != null : "loadCache: fs should never be null at this point";
      bs = new BufferedInputStream(fs, 512 * 1024); // Add buffering
      assert bs != null : "loadCache: bs should never be null at this point";
      os = new ObjectInputStream(bs);               // And now object handling
      assert os != null : "loadCache: os should never be null at this point";
    } catch (IOException e) {
      logger.warn("loadCache: Aborting as cache file failed to open");
      // Abort any cache loading
      return;
    }

    // Read in entries from cache
    CachedFile cf;
    try {
        for (; ; ) {
          cf = (CachedFile) os.readObject();
          String path = cf.getPath();
          if (logger.isTraceEnabled())  logger.trace("Loaded cached object " + path);
          loadedCount++;
          CachedFile cf2 = inCache(cf);
          if (cf2 == null) {
            // Not in cache, so simply add it and
            // set indicator that values not yet checked
            addCachedFile(cf);
            cf.clearCacheValidated();
            cf.setChanged(true);    // Assume changed unless we find otherwise
            if (logger.isTraceEnabled()) logger.trace("added entry to cache");
          } else {
            // Already in cache (can this happen?), so we
            // need to determine what values (if any) can
            // be set in the entry already there.
            if (logger.isDebugEnabled()) logger.debug("Entry already in cache - ignore cached entry for now");
          }
        }
      } catch (ClassNotFoundException cnfe) {
        logger.warn("Cache file not loaded +\n" + cnfe);
      } catch (java.io.InvalidClassException ic) {
        if (logger.isDebugEnabled()) logger.debug("Cache ignored as CachedFile class changed since it was created");
        // Should just mean that CachedFile class was changed so old cache invalid
      } catch (java.io.EOFException io) {
        if (logger.isTraceEnabled()) logger.trace("End of Cache file encountered");
        // Do nothing else - this is expected
      } catch (IOException e) {
        // This is to catch any currently unexpected error cnditions
        logger.warn("Exception trying to read cache: " + e);
      } catch (Exception e) {
        logger.warn("Cache file not loaded +\n" + e);
    } finally {
      // Close cache file
      try {
        if (os != null)
          os.close();
        if (bs != null)
          bs.close();
        if (fs != null)
          fs.close();
      } catch (Exception e) {
        // do nothing
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Cache Entries Loaded: " + loadedCount);
      logger.debug("COMPLETED Loading CRC cache from file " + cacheFile.getPath());
    }
  }

  /**
   * Delete any existing cache file
   */
  public static void deleteCache() {
    if (cacheFile == null) {
      if (logger.isDebugEnabled())  logger.debug("Aborting deleteCache() as cache folder not set");
      return;
    }
    Helper.delete(cacheFile, false);
    if (logger.isDebugEnabled())  logger.debug("Deleted CRC cache file " + cacheFile.getPath());
  }

  public static long getCacheSize() {
    return cachedFilesMap.size();
  }

  public static long getSaveCount() {
    return savedCount;
  }

  public static long getIgnoredCount() {
    return ignoredCount;
  }
}
