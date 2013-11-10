package com.gmail.dpierron.calibre.cache;
/**
 * This class is intended to allow the amount of physical I/O to
 * be reduced to a minimum during a run by caching as much
 * information as is practical.   This means that the main
 * application logic does not need to worry too much about
 * making redundant calls on file status type information
 * as doing so will not cause real calls to be made on the
 * underlying file system unless it is really needed.
 *
 * It is also intended that this type of object can be cached
 * between runs, so special support is provided for this.
 */

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class CachedFile extends File {
  private final static Logger logger = Logger.getLogger(CachedFile.class);

  private boolean exists;
  private boolean existsChecked = false;
  private long lastModified;
  private boolean lastModifiedChecked;
  private long length;
  private boolean lengthChecked;
  private long crc;                     // A -ve value indicates invalid CRC;
  private boolean crcCalced;
  private boolean targetFile;           // Set to true if file/directory is only on target
  private boolean cachedFile;           // Set to true if data is from cache and not filing system
  private boolean isChanged;            // If set to false the can assume file is unchanged so target cioy is OK

  // Constructors mirror those for the File class

  public CachedFile(String pathname) {
    super(pathname);
    if (logger.isTraceEnabled())
      logger.trace("new CachedFile: " + getAbsolutePath());
    clearCached();
  }


  /**
   * Clear all cached information.
   */
  private void clearCached() {
    //        if (logCachedFile && logger.isTraceEnabled())
    //            logger.trace("reset to defaults");
    cachedFile = false;
    targetFile = false;
    setAsNew();
  }

  /**
   * If this instance has not been used since
   * it was loaded from cache, check which
   * stored values are still relevant.
   */
  private void checkCachedValues() {
    // If it is not a cached entry then simply return
    if (!cachedFile) {
      return;
    }

    if (logger.isTraceEnabled())
      logger.trace("Check Cached data for " + getPath());

    existsChecked = true;
    if (!super.exists()) {
      // Cached entry for non-existent file needs resetting
      clearCached();
      if (logger.isTraceEnabled())
        logger.trace("File does not exist - reset to defaults");
      return;
    }
    exists = true;

    // We assume that if date and length match then cached CRC values are valid
    long d = super.lastModified();
    long l = super.length();
    if ((lastModifiedChecked && (d == lastModified)) || (lengthChecked && (l != length))) {
      if (logger.isTraceEnabled())
        logger.trace("date/length matched - CRC assumed valid");
      crcCalced = true;
    } else {
      if (logger.isTraceEnabled())
        logger.trace("date/length not matched - CRC needs recalculating");
      crc = -1;
      crcCalced = false;
    }
    lastModified = d;
    lastModifiedChecked = true;
    length = l;
    lengthChecked = true;
  }

  /**
   * @return exists status, using cached value if already known
   */
  @Override
  public boolean exists() {
    checkCachedValues();
    if (!existsChecked) {
      if (logger.isTraceEnabled())
        logger.trace("checking exists for " + getAbsolutePath());
      exists = super.exists();
      existsChecked = true;
    } else {
      if (logger.isTraceEnabled())
        logger.trace("skipping check exists for " + getAbsolutePath());
    }
    if (logger.isTraceEnabled())
      logger.trace("exists=" + exists + ": " + getAbsolutePath());
    return exists;
  }

  /**
   * Use this when we have just created a new file.
   * This is pribarliy when new image files are generated,
   * so we want to force any cached files to be re-read.
   */
  public void setAsNew() {
    crcCalced = false;
    lastModifiedChecked = false;
    existsChecked = false;
    lengthChecked = false;
    exists = false;
    lastModified = 0;
    crc = -1;
    exists = false;
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

    if (!lastModifiedChecked) {
      if (logger.isTraceEnabled())
        logger.trace("checking lastModified for " + getAbsolutePath());
      lastModified = super.lastModified();
      lastModifiedChecked = true;
    } else {
      if (logger.isTraceEnabled())
        logger.trace("skipping check lastModified for " + getAbsolutePath());
    }
    if (logger.isTraceEnabled())
      logger.trace("lastMoodified=" + lastModified + ": " + getAbsolutePath());
    return lastModified;
  }

  /**
   * Get the length of the file
   * Use cached value if known, otherwise get real value and cache it
   *
   * @return  File length
   */
  @Override
  public long length() {
    checkCachedValues();
    if (!lengthChecked) {
      if (logger.isTraceEnabled())
        logger.trace("checking length for " + getAbsolutePath());
      length = super.length();
      lengthChecked = true;
    } else {
      if (logger.isTraceEnabled())
        logger.trace("skipping check length for " + getAbsolutePath());
    }
    if (logger.isTraceEnabled())
      logger.trace("length=" + length + ": " + getAbsolutePath());
    return length;
  }

  /**
   * Get the CRC for the given file.
   * If already known the cached value is returned,
   * and if not then it is calculated (and then cached)
   *
   * @return The CRC value
   */
  public long getCrc() {
    checkCachedValues();
    if (!crcCalced)
      calcCrc();
    if (logger.isTraceEnabled())
      logger.trace("getCrc=" + crc + ": " + getAbsolutePath());
    return crc;
  }


  /**
   * Set the cached CRC value
   *
   * @param value Value to set as CRC
   */
  public void setCrc(long value) {
    crc = value;
    crcCalced = true;
    if (logger.isTraceEnabled())
      logger.trace("setCrc(" + crc + "): " + getAbsolutePath());
  }

  /**
   * Find out if crc value known(and not simply cached).
   * Can be used to find out if the crc can be retrieved
   * via getCrc() without forcing it be calculated
   *
   * @return True if yes, false if not.
   */
  public boolean isCrc() {
    return !cachedFile && crcCalced;
  }


  private void calcCrc() {
    CheckedInputStream cis;
    try {
      // Note that Adler32 is used as it is significantly faster than CRC32
      // Apparently there is a slight risk of a false match. but for this
      // purpose we can live with that, and the chance is anyway very small.
      cis = new CheckedInputStream(new FileInputStream(super.getPath()), new Adler32());
    } catch (IOException e) {
      crcCalced = false;
      crc = -1;
      return;
    }
    byte[] buf = new byte[128];
    try {
      while (cis.read(buf) >= 0) {
      }
      setCrc(cis.getChecksum().getValue());
    } catch (IOException e) {
      crcCalced = false;
      crc = -1;
      return;
    }
    // Close file ignoring any errors
    try {
      cis.close();
    } catch (IOException e) {
      // Do nothing
    }
    if (logger.isTraceEnabled())
      logger.trace("calcCrc=" + crcCalced + " (crc=" + crc + "): " + getAbsolutePath());
  }

  /**
   * Clear any cached CRC value to invalidate it
   */
  public void clearCrc() {
    crc = -1;
    crcCalced = false;
  }

  /**
   * Set to status for whether file is on target rather than source
   *
   * @param b True if it is, false otherwise
   */
  public void setTarget(boolean b) {
    targetFile = b;
    if (logger.isTraceEnabled())
      logger.trace("setTarget(" + targetFile + "): " + getAbsolutePath());
  }


  /**
   * indicates that data is from a cached object
   * NOTE: Method only intended for use by CachedFileManager
   */
  public void setCached() {
    cachedFile = true;
    if (logger.isTraceEnabled())
      logger.trace("setCached(" + cachedFile + "): " + getAbsolutePath());
  }


  /**
   * Indicate whether entry is cached data or file system data
   *
   * @return true if cached data
   */
  public boolean isCached() {
    if (logger.isTraceEnabled())
      logger.trace("isCached=" + cachedFile + ": " + getAbsolutePath());
    return cachedFile;
  }

  public boolean isChanged() {
    return isChanged;
  }

  /**
   * Set to status to say whether file has changed since last run
   * of calibre2opds.  For backwards compatibility default is true.
   *
   * @param b True if it is, false otherwise
   */
  public void setChanged (boolean b) {
    isChanged = b;
    if (logger.isTraceEnabled())
      logger.trace("setChanged(" + isChanged + "): " + getAbsolutePath());
  }

}
