package com.gmail.dpierron.calibre.cache;
/**
 * This class is intended to allow the amount of physical I/O
 * to be reduced to a minimum during a run by caching as much
 * information as is practical.   This means that the main
 * application logic does not need to worry too much about
 * making redundant calls on file status type information
 * as doing so will not cause real calls to be made on the
 * underlying file system unless it is really needed.
 *
 * It is also intended that this type of object can be cached
 * between runs, so special support is provided for this.
 *
 * NOTE:  It is important that the main program removes the
 *        cached file entry (or updates/invalidates the cached
 *        information) if it writes to the file or deletes the
 *        file.  If not then unexpected actions can occur.
 */

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class CachedFile extends File {
  private final static Logger logger = Logger.getLogger(CachedFile.class);

  private boolean privateExists;
  private boolean existsChecked = false;
  private long privateLastModified;
  private boolean lastModifiedChecked;
  private long privateLength;
  private boolean lengthChecked;
  private boolean privateIsDirectory;
  private boolean isDirectoryChecked;
  private long privateCrc;                     // A -ve value indicates invalid CRC;
  private boolean crcCalced;
  private boolean targetFile;           // Set to true if file/directory is only on target
  private boolean cachedFileValuesChecked;           // Set to true if data is from cache and not filing system
  private boolean isChanged;            // If set to false the can assume file is unchanged so target cioy is OK

  // Constructors mirror those for the File class

  public CachedFile(String pathname) {
    super(pathname);
    if (logger.isTraceEnabled()) logger.trace("new CachedFile: " + getAbsolutePath());
    clearCached();
  }

  /**
   * Clear all cached information.
   */
  private void clearCached() {
    cachedFileValuesChecked = false;
    targetFile = false;
    clearCachedInformation();
  }

  /**
   * Use this when we have just created a new file.
   * This is pribarliy when new image files are generated,
   * so we want to force any cached files to be re-read.
   */
  public void clearCachedInformation() {
    crcCalced = false;
    lastModifiedChecked = false;
    existsChecked = false;
    lengthChecked = false;
    privateExists = false;
    privateLastModified = 0;
    privateIsDirectory = false;
    isDirectoryChecked = false;
    cachedFileValuesChecked = false;        // Reset to say cache pnly entry
  }


  /**
   * If this instance has not been used since
   * it was loaded from cache, check which
   * stored values are still relevant.
   */
  private void checkCachedValues() {
    // If it is not a cached entry then simply return
    if (cachedFileValuesChecked) {
      return;
    }

    if (logger.isTraceEnabled()) logger.trace("Check Cached data for " + getPath());

    if (! existsChecked) {
      if (!super.exists()) {
        // Cached entry for non-existent file needs resetting
        privateExists = false;
        clearCached();
        if (logger.isTraceEnabled())
          logger.trace("File does not exist - reset to defaults");
        privateExists = false;
        existsChecked = true;
        return;
      } else {
        privateExists = true;
        existsChecked = true;
      }
    }

    if (! lengthChecked) {
      long l = super.length();
      lengthChecked = true;
      if (privateLength != l) {
        if (logger.isTraceEnabled()) logger.trace("privateLength not matched - CRC needs recalculating");
        clearCachedCrc();
      }
      privateLength = l;
    }

    if (! lastModifiedChecked) {
      long d = super.lastModified();
      lastModifiedChecked = true;
      if (privateLastModified != d) {
        if (logger.isTraceEnabled()) logger.trace("date not matched - CRC needs recalculating");
        clearCachedCrc();
      }
      privateLastModified = d;
    }
    if (! crcCalced && privateCrc != -1) {
      if (logger.isTraceEnabled()) logger.trace("CRC assumed valid");
      crcCalced = true;
    }

    if (! isDirectoryChecked) {
      privateIsDirectory = super.isDirectory();
      isDirectoryChecked = true;
    }
    cachedFileValuesChecked = true;     // Set to say entry been used
  }

  /**
   *    * Set the privateExists cached values.
   *
   * CAUTION:  Set with great care as this will stop an actual check
   *           againstthe underlying file system.   Should only be used
   *           after successfully copying a file
   * @param exists
   * @param lastModified
   * @param length
   * @param crc
   * @param isDirectory
   */
  public void setCachedValues(boolean exists, long lastModified, long length, long crc, boolean isDirectory) {
    privateExists = exists;
    existsChecked = true;
    privateLastModified = lastModified;
    lastModifiedChecked = true;
    privateLength = length;
    lengthChecked = true;
    privateCrc = crc;
    crcCalced = (privateCrc != -1);
    privateIsDirectory = isDirectory;
    cachedFileValuesChecked = true;
  }

  /**
   * @return privateExists status, using cached value if already known
   */
  @Override
  public boolean exists() {
    checkCachedValues();
    return privateExists;
  }
 /**
   * Check modified date of the cached file object
   * Use cached value if known, otherwise get real value
   *
   * @return  last modified sate
   */
  @Override
  public long lastModified() {
    checkCachedValues();
    return privateLastModified;
  }

  /**
   * Get the Length of the file
   * Use cached value if known, otherwise get real value and cache it
   *
   * @return  File privateLength
   */
  @Override
  public long length() {
    checkCachedValues();
    return privateLength;
  }

  @Override
  public boolean isDirectory() {
    checkCachedValues();
    return privateIsDirectory;
  }

  /**
   * Get the CRC for the given file.
   *
   * If already known the cached value is returned,
   * and if not then it is calculated (and then cached)
   *
   * Note that Adler32 is used as it is significantly faster than CRC32
   * Apparently there is a slight risk of a false match. but for this
   * purpose we can live with that, and the chance is anyway very small.
   *
   * @return The CRC value
   */
  public long getCrc() {
    checkCachedValues();
    if (! crcCalced) {
      // See i conditions for used cached value are met
      if (privateCrc != -1 && lengthChecked && lastModifiedChecked) {
        crcCalced = true;
      } else {
        // Calculate the CRC for this file.
        CheckedInputStream cis = null;
        try {
          cis = new CheckedInputStream(new FileInputStream(super.getPath()), new Adler32());
          byte[] buf = new byte[128];
          while (cis.read(buf) >= 0) {
          }
          privateCrc = cis.getChecksum().getValue();
        } catch (IOException e) {
          logger.error("ERROR: Failed trying to calculate CRC for " + super.getAbsolutePath() + "\n" + e.toString());
          clearCachedCrc();
        } finally {
          crcCalced = (privateCrc != -1);
        }
        // Close file ignoring any errors
        try {
          if (cis != null)
            cis.close();
        } catch (IOException e) {
          // Do nothing
        }
        if (logger.isTraceEnabled()) logger.trace("calcCrc=" + crcCalced + " (privateCrc=" + privateCrc + "): " + getAbsolutePath());
      }
    }
    if (logger.isTraceEnabled()) logger.trace("getCrc=" + privateCrc + ": " + getAbsolutePath());
    return privateCrc;
  }

  /**
   * Find out if privateCrc value known(and not simply cached).
   * Can be used to find out if the privateCrc can be retrieved
   * via getCrc() without forcing it be calculated
   *
   * @return True if yes, false if not.
   */
  public boolean isCrc() {
    return cachedFileValuesChecked && crcCalced;
  }


  /**
   * Clear any cached CRC value to invalidate it
   */
  private void clearCachedCrc() {
    privateCrc = -1;
    crcCalced = false;
  }

  /**
   * Set to status for whether file is on target rather than source
   *
   * @param b True if it is, false otherwise
   */
  public void setTarget(boolean b) {
    targetFile = b;
    if (logger.isTraceEnabled()) logger.trace("setTarget(" + targetFile + "): " + getAbsolutePath());
  }

  /**
   * Indicate whether entry is cached data or file system data
   *
   * @return true if cached data
   */
  public boolean isCachedValidated() {
    if (logger.isTraceEnabled()) logger.trace("isCachedValidated=" + cachedFileValuesChecked + ": " + getAbsolutePath());
    return cachedFileValuesChecked;
  }

  public void clearCacheValidated() {
    existsChecked = false;
    lengthChecked = false;
    crcCalced = false;
    lastModifiedChecked = false;
    cachedFileValuesChecked = false;
  }

  /**
   * Return status to say whether file has changed since last run
   * of calibre2opds.  For backwards compatibility default is true.
   *
   * This method is intended to support a future optimisation where
   * we try and calculate whether pages have chnged without actually
   * generating them.   *
   * @return
   */
  public boolean isChanged() {
    return isChanged;
  }

  /**
   * Set to status to say whether file has changed since last run
   * of calibre2opds.  For backwards compatibility default is true.
   *
   * This method is intended to support a future optimisation where
   * we try and calculate whether pages have chnged without actually
   * generating them.
   *
   * @param b True if it is, false otherwise
   */
  public void setChanged (boolean b) {
    isChanged = b;
    if (logger.isTraceEnabled()) logger.trace("setChanged(" + isChanged + "): " + getAbsolutePath());
  }
}
