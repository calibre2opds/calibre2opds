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

  private long privateLastModified;
  private long privateLength;
  private long privateCrc;                     // A -ve value indicates invalid CRC;
  private final static long CRC_NOT_SET = -1;

  // Flags indicating state of entry
  // Noee:  Using bits in more memory efficient than using boolean types.
  //        This can mount up with the number of these objects that are created.
  final static short FLAG_ALL_CLEAR = 0x0000;
  final static short FLAG_EXISTS = 0x0001;
  final static short FLAG_EXISTS_CHECKED = 0x0002;
  final static short FLAG_MODIFIED_CHECKED = 0x0004;
  final static short FLAG_LENGTH_CHECKED = 0x0008;
  final static short FLAG_IS_DIRECTORY = 0x0010;
  final static short FLAG_IS_DIRECTORY_CHECKED = 0x0020;
  final static short FLAG_CRC_CALCED = 0x0040;
  final static short FLAG_TARGET_FILE = 0x0080;
  final static short FLAG_CACHED_VALUES_CHECKED = 0x0100;
  final static short FLAG_IS_CHANGED = 0x02000;             // If set to clear then can assume file is unchanged so target cioy is OK
  private short flags = FLAG_ALL_CLEAR;

  // Constructors mirror those for the File class

  public CachedFile(String pathname) {
    super(pathname);
    if (logger.isTraceEnabled()) logger.trace("new CachedFile: " + getAbsolutePath());
    resetCached();
  }


  /**
   *   Helper routine to set flags bits state
   */
  private void setFlags (boolean b, int f) {
    if (b == true)
      flags |= f;           // Set flag bits
    else
      flags &= ~f;           // Clear flag bits;
  }

  /**
   * Helper routine to check if specified kags set
   * @param f
   * @return
   */
  private boolean isFlags( int f) {
    return ((flags & f) == f);
  }

  /**
   * Reset all cached information to defaults.
   */
  private void resetCached() {
    setFlags(false, FLAG_CACHED_VALUES_CHECKED + FLAG_TARGET_FILE);
    clearCachedInformation();
  }
  /**
   * Use this when we have just created a new file.
   * This is pribarliy when new image files are generated,
   * so we want to force any cached files to be re-read.
   */
  public void clearCachedInformation() {
    privateLastModified = 0;
    privateCrc = CRC_NOT_SET;
    setFlags(false,
        FLAG_CRC_CALCED + FLAG_MODIFIED_CHECKED + FLAG_EXISTS_CHECKED + FLAG_LENGTH_CHECKED + FLAG_EXISTS
        + FLAG_IS_DIRECTORY + FLAG_IS_DIRECTORY_CHECKED + FLAG_CACHED_VALUES_CHECKED);     // Reset to say cache only entry
    setFlags(true, FLAG_IS_CHANGED);          // We always assume a file is changed until explicitly told otherwise
  }


  /**
   * If this instance has not been used since
   * it was loaded from cache, check which
   * stored values are still relevant.
   */
  private void checkCachedValues() {
    // If it is not a cached entry then simply return
    if (isFlags(FLAG_CACHED_VALUES_CHECKED)) {
      return;
    }

    if (logger.isTraceEnabled()) logger.trace("Check Cached data for " + getPath());

    if (! isFlags(FLAG_EXISTS_CHECKED)) {
      setFlags(super.exists(), FLAG_EXISTS);
      setFlags(true, FLAG_EXISTS_CHECKED);
      if (! isFlags (FLAG_EXISTS)) {
        // Cached entry for non-existent file needs resetting
        if (logger.isTraceEnabled())
          logger.trace("File does not exist - reset to defaults");
        resetCached();
        return;
      }
    }

    if (! isFlags(FLAG_LENGTH_CHECKED)) {
      long l = super.length();
      if (privateLength != l) {
        if (logger.isTraceEnabled()) logger.trace("privateLength not matched - CRC needs recalculating");
        clearCachedCrc();
      }
      privateLength = l;
    }

    if (! isFlags(FLAG_MODIFIED_CHECKED)) {
      long d = super.lastModified();
      if (privateLastModified != d) {
        if (logger.isTraceEnabled()) logger.trace("date not matched - CRC needs recalculating");
        clearCachedCrc();
      }
      privateLastModified = d;
    }
    if (!isFlags(FLAG_CRC_CALCED) && privateCrc != CRC_NOT_SET) {
      if (logger.isTraceEnabled()) logger.trace("CRC assumed valid");
      setFlags(true, FLAG_CRC_CALCED);
    }

    if ( ! isFlags(FLAG_IS_DIRECTORY_CHECKED)) {
      setFlags(super.isDirectory(), FLAG_IS_DIRECTORY);
    }

    setFlags(true, FLAG_MODIFIED_CHECKED + FLAG_LENGTH_CHECKED + FLAG_IS_DIRECTORY_CHECKED + FLAG_CACHED_VALUES_CHECKED);
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
    setFlags(exists, FLAG_EXISTS);
    privateLastModified = lastModified;
    privateLength = length;
    privateCrc = crc;
    setFlags((crc != -1), FLAG_CRC_CALCED);
    setFlags(isDirectory, FLAG_IS_DIRECTORY);
    setFlags(true, FLAG_EXISTS_CHECKED + FLAG_LENGTH_CHECKED + FLAG_MODIFIED_CHECKED + FLAG_IS_DIRECTORY_CHECKED + FLAG_CACHED_VALUES_CHECKED);
  }

  /**
   * @return privateExists status, using cached value if already known
   */
  @Override
  public boolean exists() {
    checkCachedValues();
    return isFlags(FLAG_EXISTS);
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
    return isFlags(FLAG_IS_DIRECTORY);
  }

  @Override
  public boolean delete() {
    resetCached();
    return super.delete();
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
    if (! isFlags(FLAG_CRC_CALCED)) {
      // See i conditions for used cached value are met
      if ((privateCrc != CRC_NOT_SET) && isFlags(FLAG_LENGTH_CHECKED + FLAG_MODIFIED_CHECKED)) {
        setFlags(true, FLAG_CRC_CALCED);
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
          setFlags((privateCrc != CRC_NOT_SET), FLAG_CRC_CALCED);
        }
        // Close file ignoring any errors
        try {
          if (cis != null)
            cis.close();
        } catch (IOException e) {
          // Do nothing
        }
        if (logger.isTraceEnabled()) logger.trace("calcCrc=" + isFlags(FLAG_CRC_CALCED) + " (privateCrc=" + privateCrc + "): " + getAbsolutePath());
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
    return isFlags(FLAG_CACHED_VALUES_CHECKED & FLAG_CRC_CALCED);
  }


  /**
   * Clear any cached CRC value to invalidate it
   */
  private void clearCachedCrc() {
    privateCrc = CRC_NOT_SET;
    setFlags(false, FLAG_CRC_CALCED);
  }

  /**
   * Set to status for whether file is on target rather than source
   *
   * @param b True if it is, false otherwise
   */
  public void setTarget(boolean b) {
    setFlags(b, FLAG_TARGET_FILE);
    if (logger.isTraceEnabled()) logger.trace("setTarget(" + isFlags(FLAG_TARGET_FILE) + "): " + getAbsolutePath());
  }

  /**
   * Indicate whether entry is cached data or file system data
   *
   * @return true if cached data
   */
  public boolean isCachedValidated() {
    if (logger.isTraceEnabled()) logger.trace("isCachedValidated=" + isFlags(FLAG_CACHED_VALUES_CHECKED)
                                          + ": " + getAbsolutePath());
    return isFlags (FLAG_CACHED_VALUES_CHECKED);
  }

  public void clearCacheValidated() {
    setFlags(false, FLAG_EXISTS_CHECKED + FLAG_LENGTH_CHECKED + FLAG_MODIFIED_CHECKED + FLAG_CACHED_VALUES_CHECKED);
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
    return isFlags(FLAG_IS_CHANGED);
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
    setFlags(b, FLAG_IS_CHANGED);
    if (logger.isTraceEnabled()) logger.trace("setChanged(" + isFlags(FLAG_IS_CHANGED) + "): " + getAbsolutePath());
  }
}
