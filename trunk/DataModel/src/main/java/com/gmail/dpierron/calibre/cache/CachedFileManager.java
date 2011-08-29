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
 */
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.awt.image.ImageFilter;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public enum CachedFileManager
{
    INSTANCE;
    private final static Logger logger = Logger.getLogger(CachedFileManager.class);
    private Map<String, CachedFile> cachedFilesMap = new HashMap<String, CachedFile>();
    private File cacheFolder = null;
    private File cacheFile = null;

    //-----------------------------------------------------------------------------------------------------------
    private final boolean logCacheManager = true;   // Set to false to get verbose code for logging optimised out.
    //-----------------------------------------------------------------------------------------------------------

    public void initialize()
    {
        cachedFilesMap = null;    // Force release any currently assigned map
        cachedFilesMap = new HashMap<String, CachedFile>();
        loadCache();
    }

    /**
     * Determine if an entry is already in the cache
     * @param cf     CachedFile object to check
     * @return       null if not present, object otherwise
     */
    public CachedFile inCache(CachedFile cf)
    {
        CachedFile cf_result = cachedFilesMap.get(cf.getPath());
        if (logCacheManager && logger.isTraceEnabled())
            logger.trace("inCache=" + (cf_result!= null) + ": " + cf.getPath());
        return cf_result;
    }

    /**
     * Determine if an entry is already in the cache
     * @param  f     File object to check
     * @return       null if not present, object otherwise
     */
    public CachedFile inCache(File f)
    {
        CachedFile cf_result = cachedFilesMap.get(f.getPath());
        if (logCacheManager && logger.isTraceEnabled())
            logger.trace("inCache=" + (cf_result!= null) + ": " + f.getPath());
        return cf_result;
    }


    /**
     * Check the entry against the cache and if needed
     * create a new entry.
     *
     * @param cf CachedFile object representing file
     * @return A CachedFile object for the given path
     */
    public CachedFile addCachedFile(CachedFile cf)
    {
        String path = cf.getPath();
        CachedFile cf2 = inCache(cf);
        if (cf2 == null)
        {
            cf2 = new CachedFile(path);
            cachedFilesMap.put(path, cf2);
            if (logCacheManager && logger.isTraceEnabled())
                logger.trace("Added CachedFile: " + path);
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
    public CachedFile addCachedFile(File f)
    {
        String path = f.getPath();
        CachedFile cf = inCache(f);
        if (cf == null)
        {
            cf = new CachedFile(path);
            cachedFilesMap.put(path, cf);
            if (logCacheManager && logger.isTraceEnabled())
                logger.trace("Added file to cache: " + path);
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
    public CachedFile addCachedFile(File parent, String child)
    {
        return addCachedFile(new File(parent, child));
    }


    /**
     * Remove the entry from the cache (if it is present).
     *
     * @param f File object representing file
     */
    public void removeCachedFile(File f)
    {
        String path = f.getPath();
        if (cachedFilesMap.containsKey(path))
        {
            cachedFilesMap.remove(path);
            if (logCacheManager && logger.isTraceEnabled())
                logger.trace("Remove CachedFile: " + path);
        }
        else
        {
            if (logCacheManager && logger.isTraceEnabled())
                logger.trace("Remove CachedFile (not found): " + path);
        }
        return;
    }


    /**
     * Set the location for any existing cache file
     *
     * @param cf Specify the folder to hold the cache
     *           This is normally the catalog sub-folder of the target folder
     */
    public void setCacheFolder(File cf)
    {
        cacheFolder = cf;
        cacheFile = new File(cacheFolder.getAbsolutePath(), "calibre2opds.cache");
        logger.debug("CRC Cache file set to " + cacheFile.getPath());
    }

    /**
     * Save the current cache for potential later re-use
     * <p/>
     * N.B. the setCacheFolder() call must have been used
     */
    public void saveCache()
    {

        // Check Cache folder has been set
        if (cacheFolder == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Aborting saveCache() as cache folder not set");
            return;
        }

        long savedCount = 0;
        long ignoredCount = 0;
        // Open cache file
        ObjectOutputStream os = null;
        FileOutputStream fs = null;
        try
        {
            logger.debug("STARTED Saving CRC cache to file " + cacheFile.getPath());
            fs = new FileOutputStream(cacheFile);
            os = new ObjectOutputStream(fs);

            // Write out the cache entries
            for (Map.Entry<String, CachedFile> m : cachedFilesMap.entrySet())
            {
                CachedFile cf = m.getValue();
                String key = m.getKey();

                // We are only interested in caching entries for which the CRC is known
                // as this is the expensive operation we do not want to do unnecessarily
                if (!cf.isCrc())
                {
                    if (logCacheManager && logger.isTraceEnabled())
                        logger.trace("CRC not known.  Not saving CachedFile " + key);
                    ignoredCount++;
                }
                else
                {
                    // We only want to cache items that have actually been used this time
                    // around, so ignore entries that indicate cached values not used.
                    if (cf.isCached())
                    {
                        if (logCacheManager && logger.isTraceEnabled())
                            logger.trace("Not used.  Not saving CachedFile " + key);
                        ignoredCount++;
                    }
                    else
                    {
                        // No point in caching entries for non-existent files
                        if (!cf.exists())
                        {
                            if (logCacheManager && logger.isTraceEnabled())
                                logger.trace("Not exists.  Not saving CachedFile " + key);
                            ignoredCount++;
                        }
                        else
                        {
                            os.writeObject(cf);
                            if (logCacheManager && logger.isTraceEnabled())
                                logger.trace("Saved " + key);
                            savedCount++;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Exception trying to write cache: " + e);
        }

        // Close cache file
        try
        {
            os.close();
            fs.close();
        }
        catch (Exception e)
        {
            // Do nothing
        }

        logger.debug("Cache Entries Saved:   " + savedCount);
        logger.debug("Cache Entries Ignored: " + ignoredCount);
        logger.debug("COMPLETED Saving CRC cache to file " + cacheFile.getPath());
        return;
    }

    /**
     * Initialize the cache if there is a saved one present
     * <p/>
     * N.B. the setCacheFolder() call must have been used
     */
    public void loadCache()
    {

        // Check cache folder has been specified
        if (cacheFolder == null)
        {
            if (logger.isTraceEnabled())
                logger.trace("Aborting loadCache() as cache folder not set");
            return;
        }

        if (!cacheFile.exists())
        {
            if (logger.isDebugEnabled())
                logger.debug("Exiting loadCache() as cache file not present");
            return;
        }
        // Open Cache file
        ObjectInputStream os = null;
        FileInputStream fs;
        long loadedCount = 0;
        try
        {
            logger.info("STARTED Loading CRC cache from file " + cacheFile.getPath());
            fs = new FileInputStream(cacheFile);
            os = new ObjectInputStream(fs);
        }
        catch (Exception e)
        {
            logger.warn("Aborting loadCache() as cache file failed to open");
            // Abort any cache loading
            return;
        }

        // Read in entries from cache
        try
        {
            for (; ;)
            {
                CachedFile cf;
                cf = (CachedFile) os.readObject();

                String path = cf.getPath();
                if (logCacheManager && logger.isTraceEnabled())
                    logger.trace("Loaded cached object " + path);
                loadedCount++;
                CachedFile cf2 = inCache(cf);
                if (cf2 == null)
                {
                    // Not in cache, so simply add it and
                    // set indicator that values not yet checked
                    cf.setCached(true);
                    addCachedFile(cf);
                    if  (logCacheManager && logger.isTraceEnabled())
                        logger.trace ("added entry to cache");
                }
                else
                {
                    // Already in cache (can this happen?), so we
                    // need to determine what values (if any) can
                    // be set in the entry already there.
                    logger.debug ("Entry already in cache - ignore cached entry for now");
                }
            }
        }
        catch (java.io.EOFException io)
        {
            logger.trace ("End of Cache file encountered");
            // Do nothing else - this is expected
        }
        catch (java.io.InvalidClassException ic)
        {
            logger.debug ("Cache ignored as CachedFile class changed since it was created");
            // Should just mean that CachedFile class was changed so old cache invalid
        }
        catch (Exception e)
        {
            // This is to catch any currently unexpected error cnditions
            logger.warn("Exception trying to read cache: " + e);
        }

        // Close cache file
        try
        {
            os.close();
            fs.close();
        }
        catch (Exception e)
        {
            // Do nothing
        }

        logger.info("Cache Entries Loaded: " + loadedCount);
        logger.info("COMPLETED Loading CRC cache from file " + cacheFile.getPath());
        return;
    }

    /**
     * Delete any existing cache file
     */
    public void deleteCache()
    {
        if (cacheFolder == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Aborting deleteCache() as cache folder not set");
            return;
        }
        Helper.delete(cacheFile);
        if (logger.isDebugEnabled())
            logger.debug("Deleted CRC cache file " + cacheFile.getPath());
        return;
    }

}
