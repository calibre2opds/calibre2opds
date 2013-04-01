package com.gmail.dpierron.calibre.opds.secure;
/**
 * Class that is used to obfusticate the filenames that are used
 * when generating a calbire2opds catalog.   This is used to help
 * maintain secutiry when making a calibre2opds catalog accessible
 * via the internet as it means that the filenames used withing
 * the catalog itself become hard to guess.
 *
 * one feature is that the mapping of 'clear' to 'obfusticated' names
 * is maintained so that it is consistent between runs
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

public enum SecureFileManager {
  INSTANCE;

  private final static boolean autosave = false;
  private final static char DELIM1 = '_';
  private final static String NAKED_KEYWORD = "naked.";
  private final static String CODED_KEYWORD = "coded.";
  private final static String PROPERTY_FILENAME = ".calibre2opds.secureFileManager.xml";
  private final static Logger logger = Logger.getLogger(SecureFileManager.class);
  private final static String COMMENT = Constants.PROGTITLE;
  private static Properties properties;
  private File propertiesFile;

  private boolean isSecurityOn() {
    return ConfigurationManager.INSTANCE.getCurrentProfile().getCryptFilenames();
  }

  public void reset() {
    reset(true);
  }

  /**
   * Clear down any existing cached information and set up a new
   * empty set of properties for security information.
   * @param deleteFile
   */
  private void reset(boolean deleteFile) {
    if (deleteFile)
      getPropertiesFile().delete();

    properties = new Properties();

    // let's try and load the properties;
    if (getPropertiesFile().exists())
      load();
  }

  private SecureFileManager() {
    reset(false);
  }

  /**
   * Get the name of the file that is being used to store the secure
   * filenames between runs
   *
   * ITIMPI:  At the moment this seems to be a global setting for a given
   *          user.  Maybe it should be separate for each profile?
   *
   * @return
   */
  private File getPropertiesFile() {
    File configurationFolder = ConfigurationManager.INSTANCE.getConfigurationDirectory();

    if (configurationFolder != null && configurationFolder.exists()) {
      // found the user home, let's check for the configuration file
      propertiesFile = new File(configurationFolder, PROPERTY_FILENAME);
    }

    return propertiesFile;
  }

  /**
   * Load the current mapping to file
   */
  private void load() {
    BufferedInputStream bis = null;
    try {
      try {
        bis = new BufferedInputStream(new FileInputStream(getPropertiesFile()));
        properties.loadFromXML(bis);
      } finally {
        if (bis != null)
          bis.close();
      }
    } catch (IOException e) {
      logger.error("error while loading properties from " + getPropertiesFile().getAbsolutePath(), e);
    }

  }

  /**
   * Save the current mappings to file
   */
  public void save() {
    BufferedOutputStream bos = null;
    try {
      try {
        bos = new BufferedOutputStream(new FileOutputStream(getPropertiesFile()));
        properties.storeToXML(bos, COMMENT);
      } finally {
        if (bos != null)
          bos.close();
      }
    } catch (IOException e) {
      logger.error("error while storing properties in " + getPropertiesFile().getAbsolutePath(), e);
    }
  }

  /**
   * Store a new mapping of name to value
   *
   * @param name
   * @param value
   */
  private void setProperty(String name, String value) {
    properties.setProperty(name, value);
    if (autosave)
      save();
  }

  /**
   * Retrieve the value associated with a given name
   * @param name
   * @return
   */
  private String getProperty(String name) {
    return properties.getProperty(name);
  }

  /**
   * Generate a new random file name
   *
   * ITIMPI:  THe original algorithm worked by effectively encoding
   *           the pathname as the first part of the filename.   The
   *           big downside to this approach is that the encoded name
   *           can get progressively longer for each recursion level,
   *           and it has been found that this can easily end up breaking
   *           the system limits on maximum path length.
   *
   * @param naked
   * @return
   */
  public String generateNewRandomFile(String naked) {
    if (Helper.isNullOrEmpty(naked))
      return naked;

    // separate name from extension
    // Extensions need to be kept in the clear as they
    // tend to be significant at the OS/System level

    String base = naked;
    String ext = null;
    int pos = base.lastIndexOf('.');
    if (pos > -1) {
      ext = base.substring(pos);
      base = base.substring(0, pos);
    }

    StringBuffer sb = new StringBuffer();
    // ITIMPI:  Are these next two lines really necessary?
    //          Removing them would stop the name getting progressively longer
    //          (however it may need name algorithms need reworking)
    sb.append(base);
    sb.append(DELIM1);
    sb.append(getNewRandomCypher());
    if (Helper.isNotNullOrEmpty(ext))
      sb.append(ext);
    return sb.toString();
  }

  /**
   * Remove an entry from the secure file manage cache
   *
   * TODO:  Need to implement this and start using to stop cache from always growing
   *
   * @param filename
   */
  public void remove (String filename) {

  }
  /**
   * Convert a file name to its encoded form
   * We do not encode the file extension, so this
   * always needs to be accounted for.
   *
   * @param naked Filename that needs to be encoded
   * @return Encoded value.
   *         If security is off, this is the same
   *         as the original value passed in.
   */
  public String encode(String naked) {
    if (!isSecurityOn())
      return naked;

    String fileExt;
    String fileBase;
    if (naked == null)
      assert naked != null;
    int pos = naked.indexOf(Constants.PAGE_DELIM);
    if (pos != -1 ) {
      fileExt = naked.substring(pos);
      fileBase = naked.substring(0,pos);
    } else {
      if (naked.endsWith(".xml")) {
        fileExt = ".xml";
        fileBase = naked.substring(0, naked.length()-4);
      } else {
        fileExt = "";
        fileBase = naked;
      }
    }

    // We now need to see if we already have this encoded
    // This stops us extended the name unneccesarily
    String result = getProperty(NAKED_KEYWORD + fileBase);
    if (Helper.isNotNullOrEmpty(result)) {
      return result + fileExt;
    }
    // Check we have not been passed a name we have already encoded!
    if (Helper.isNotNullOrEmpty(getProperty((CODED_KEYWORD + fileBase)))) {
      return fileBase + fileExt;
    }
    result = generateNewRandomFile(fileBase);
    setProperty(NAKED_KEYWORD + fileBase, result);
    setProperty(CODED_KEYWORD + result, fileBase);
    return result + fileExt;
  }

  /**
   * Take a (potentially) endoded name and convert it
   * to the decoded version for internal program use
   *
   * We always store code names without the file
   * extension or the 'page' part of the filename so
   * this needs to be accounted for.
   *
   * @param coded Encoded filename
   * @return Decoded filename
   *         (if security off then same as encoded)
   */
  public String decode(String coded) {
    if (!isSecurityOn())
      return coded;

    String fileExt;
    String fileBase;
    int pos = coded.indexOf(Constants.PAGE_DELIM);
    if (pos != -1 ) {
      fileExt = coded.substring(pos);
      fileBase = coded.substring(0,pos);
    } else {
      if (coded.endsWith(".xml")) {
        fileExt = ".xml";
        fileBase = coded.substring(0, coded.length()-4);
      } else {
        fileExt = "";
        fileBase = coded;
      }
    }
  String result = getProperty(CODED_KEYWORD + fileBase);
    return result == null ? null : result + fileExt;
  }

  /**
   * Generate a new random cypher string.
   * The chance of collision with an value that
   * has been previsouly generated is extermely low,
   * but a check for this special case should be made.
   *
   * @return
   */
  private String getNewRandomCypher() {
    int CYPHERLEN = 12;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < CYPHERLEN; i++) {
      double random = Math.random() * 36 + 1;
      int r = (int) random;
      if (r <= 10)
        r = r + 47;
      else
        r = r + 54;
      char c = (char) r;
      sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Used to derive a new filename and get it stored in the secure filename store
   *
   * @param baseFilename  Filename to use as base.  It may be encoded on entry.
   * @param splitText     The new split text
   * @return
   */
  public String getSplitFilename (String baseFilename, String splitText) {
    String baseFilenameNoExt;
    String baseFilenameCleanedUp;
    String fileExt = "";
    int pos = baseFilename.indexOf(".xml");   // Check for presence of extension .xml
    // Get name without file extension
    if (pos != -1) {                          // ... did we find it?
      fileExt = baseFilename.substring(pos);  // save extension for later
      baseFilenameNoExt = baseFilename.substring(0,pos);
    } else {
      baseFilenameNoExt = baseFilename;
    }
    // See if we are merely trying to add a page number?
    if (splitText.startsWith(Constants.PAGE_DELIM))  {
      pos = baseFilename.indexOf(Constants.PAGE_DELIM);
      if (pos != -1) {
        baseFilenameCleanedUp = baseFilenameNoExt.substring(0, pos);
      } else {
        baseFilenameCleanedUp = baseFilenameNoExt;
      }
      if (baseFilenameCleanedUp == null)
        assert baseFilenameCleanedUp == null : "baseFileNameCleanedUp=null while adding a page number";
      // Now to see if we have been passed a encoded base?
      if (isSecurityOn()) {
        String result = getProperty(CODED_KEYWORD + baseFilenameNoExt);
        if (Helper.isNotNullOrEmpty(result))
          baseFilenameCleanedUp = result;
      }
      return encode(baseFilenameCleanedUp) + splitText + fileExt;
    }
    // We are not merely adding a page number!
    baseFilenameCleanedUp = decode(baseFilenameNoExt);  // Remove any encoded part
    if (baseFilenameCleanedUp == null)
      assert baseFilenameCleanedUp == null : "baseFileNameCleanedUp=null while adding a page number";
    return encode(Helper.getSplitString(baseFilenameCleanedUp, splitText, "_")) + fileExt;
  }
}
