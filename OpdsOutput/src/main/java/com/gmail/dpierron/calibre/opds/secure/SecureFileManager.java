package com.gmail.dpierron.calibre.opds.secure;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

public enum SecureFileManager {
  INSTANCE;

  private final static boolean autosave = false;
  private final static char DELIM1 = '_';
  private final static String PROPERTY_FILENAME = ".calibre2opds.secureFileManager.xml";
  private final static Logger logger = Logger.getLogger(SecureFileManager.class);
  private final static String COMMENT = Constants.PROGTITLE;
  private Properties properties;
  private File propertiesFile;

  private boolean isSecurityOn() {
    return ConfigurationManager.INSTANCE.getCurrentProfile().getCryptFilenames();
  }

  public void reset() {
    reset(true);
  }

  private void reset(boolean deleteFile) {
    if (deleteFile)
      getPropertiesFile().delete();

    properties = new Properties();

    // let's try and load the properties
    tryAndLoadProperties();
  }

  private SecureFileManager() {
    reset(false);
  }

  private File getPropertiesFile() {
    File configurationFolder = ConfigurationManager.INSTANCE.getConfigurationDirectory();

    if (configurationFolder != null && configurationFolder.exists()) {
      // found the user home, let's check for the configuration file
      propertiesFile = new File(configurationFolder, PROPERTY_FILENAME);
    }

    return propertiesFile;
  }

  private void tryAndLoadProperties() {
    if (getPropertiesFile().exists())
      load();
  }

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

  private void setProperty(String name, String value) {
    properties.setProperty(name, value);
    if (autosave)
      save();
  }

  private String getProperty(String name) {
    return properties.getProperty(name);
  }

  public String generateNewRandomFile(String naked) {
    if (Helper.isNullOrEmpty(naked))
      return naked;


    // separate name from extension
    String base = naked;
    String ext = null;
    int pos = base.lastIndexOf('.');
    if (pos > -1) {
      ext = base.substring(pos);
      base = base.substring(0, pos);
    }

    StringBuffer sb = new StringBuffer();
    sb.append(base);
    sb.append(DELIM1);
    sb.append(getNewRandomCypher());
    if (Helper.isNotNullOrEmpty(ext))
      sb.append(ext);
    return sb.toString();
  }

  /**
   * Convert a file name to its encoded form
   *
   * @param naked Filename that needs to be encoded
   * @return Encoded value.
   *         If security is off, this is the same
   *         as the original value passed in.
   */
  public String encode(String naked) {
    if (!isSecurityOn())
      return naked;
    String result = getProperty("naked." + naked);
    if (Helper.isNullOrEmpty(result)) {
      result = generateNewRandomFile(naked);
      setProperty("naked." + naked, result);
      setProperty("coded." + result, naked);
    }
    return result;
  }

  /**
   * Take a (potentially) endoded name and convert it
   * to the decoded version for internal program use
   *
   * @param coded Encoded filename
   * @return Decoded filename
   *         (if security off then same as encoded)
   */
  public String decode(String coded) {
    if (!isSecurityOn())
      return coded;
    String result = getProperty("coded." + coded);
    return result;
  }


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
}
