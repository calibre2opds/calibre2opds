package com.gmail.dpierron.calibre.configuration;

/**
 * Class that handles Configuration Management
 *
 * NOTE:  As there should only ever be a single instance of this
 *        class all vriables and methods are declared static
 */
import com.gmail.dpierron.calibre.datamodel.EBookFormat;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.JDOMManager;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.runner.Runner;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class ConfigurationManager {

  public static final String PROFILES_SUFFIX = ".profile.xml";
  private final static String PROFILE_FILENAME = "profile.xml";
  private final static String DEFAULT_PROFILE = "default";
  private final static String PROPERTY_NAME_CURRENTCONFIGURATION = "CurrentConfiguration";

  private final static Logger logger = LogManager.getLogger(ConfigurationManager.class);

  private static File configurationDirectory;
  private static File configurationFolder = null;
  private static ConfigurationHolder currentProfile;
  private static PropertiesBasedConfiguration defaultConfiguration;
  private static Locale configLocale = null;
  // Listof formats that are used in the current profile
  private static List<EBookFormat> profileFormats = null;

  /**
   *
   * @return
   */
  public static PropertiesBasedConfiguration getDefaultConfiguration() {
    if (defaultConfiguration == null) {
      logger.trace("defaultConfiguration is not set");
      File file = new File(getConfigurationDirectory(), PROFILE_FILENAME);
      logger.trace("file=" + file);
      defaultConfiguration = new PropertiesBasedConfiguration(file);
      defaultConfiguration.setPropertiesFile(file);
      if (file.exists()) {
        try {
          defaultConfiguration.load();
        } catch (IOException e) {
          logger.warn(Localization.Main.getText("error.loadingProperties") + ": " + file.getName());
        }
      } else {
        // Create the standard default file
        defaultConfiguration.setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, "Default");
        defaultConfiguration.save();
      }
    }
    return defaultConfiguration;
  }

  /**
   *
   * @return
   */
  public static ConfigurationHolder getCurrentProfile() {
    if (currentProfile == null) {
      logger.trace("getCurrentProfile - currentProfile not set");
      currentProfile = new ConfigurationHolder(new File(getConfigurationDirectory(), getCurrentProfileName() + PROFILES_SUFFIX));
      Configuration.setConfiguration(currentProfile);
    }
    return currentProfile;
  }

  /**
   *
   * @return
   */
  public static String getCurrentProfileName() {
    String s = getDefaultConfiguration().getProperty(PROPERTY_NAME_CURRENTCONFIGURATION);
    if (Helper.isNotNullOrEmpty(s))
      return s;
    else
      return DEFAULT_PROFILE;
  }

  /**
   * Set a new name for the current profile after it has beeb renamed.
   * As a consistency check the old name is provided.
   *
   * @param newName
   */
  public static void setCurrentProfileName(String newName) {
    getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, newName);
  }

  /**
   * Change the current loaded GUI.
   *
   * There is an option as to whether it should become the default
   *
   * @param profileName
   * @param setDefault
   */
  public static void changeProfile(String profileName, boolean setDefault) {
    logger.trace("changeProfile to " + profileName);
    String currentProfileName = getCurrentProfileName();
    getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, profileName);
    currentProfile = null;
    getCurrentProfile();
    if (setDefault) getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, profileName);
  }

  /**
   * Copy the current profile to a new one with a given name
   *
   * @param newProfileName
   */
  public static void copyCurrentProfile(String newProfileName) {
    getCurrentProfile().setPropertiesFile(new File(getConfigurationDirectory(), newProfileName + PROFILES_SUFFIX));
    getCurrentProfile().save();
    getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, newProfileName);
    currentProfile = null;
    getCurrentProfile();
  }

  /**
   * See if the configuration already exists
   *
   * We return the name if matched so that the
   * name case is maintained.
   *
   * @param filename
   * @return    Null if not found
   *            Existing name if found
   */
  public static String isExistingConfiguration(String filename) {
    for (String existingConfigName : getExistingConfigurations()) {
      if (existingConfigName.equalsIgnoreCase(filename))
        return existingConfigName;
    }
    return null;
  }

  /**
   *
   * @return
   */
  public static List<String> getExistingConfigurations() {
    File configurationFolder = getConfigurationDirectory();
    String[] files = configurationFolder.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        return name.toUpperCase().endsWith(PROFILES_SUFFIX.toUpperCase());
      }
    });
    List<String> result = new LinkedList<String>();
    for (String file : files) {
      result.add(file.substring(0, file.toUpperCase().indexOf(PROFILES_SUFFIX.toUpperCase())));
    }
    return result;
  }

  /**
   * Get the Configuration folder
   *
   * @return
   */
  public static File getConfigurationDirectory() {
    assert configurationDirectory != null;
    return configurationDirectory;
  }

  /**
   * Set the configuration folder
   * @param f
   */
  public static void setConfigurationDirectory (File f) {
    configurationDirectory = f;
  }


  /**
   * Special variant of this that checks several locations for the file before
   * resorting to using the built-in resource file.  The purpose is to allow
   * the user to over-ride the built-in resource files if so required.
   * @param filename
   * @return
   */
  public static InputStream getResourceAsStream(String filename) {
    InputStream ins = null;
    try {
        // Try user configuration folder
        ins = new FileInputStream(getConfigurationDirectory() + "/" + filename);
        logger.info("Resource '" + filename + "' loaded from Configuration folder");
    } catch (FileNotFoundException e) {
      try {
          // If that fails, try install folder
          ins = new FileInputStream (Helper.getInstallDirectory() + "/" + filename);
          logger.info("Resource '" + filename + "' loaded from Install folder");
      } catch (FileNotFoundException f) {
          // If still not found then use built-in resource
        ins = JDOMManager.class.getResourceAsStream(filename);
      }
    }
    return ins;
  }

  public static boolean isHacksEnabled() {
    return Helper.isNotNullOrEmpty(System.getenv("CALIBRE2OPDS_HACKSENABLED"));
  }

  /**
   * Set the lcoal that we are using for this generation
   * If the one requested is not one we support we set it to English
   * @param lc
   */
  public static void setLocale (Locale lc){
    if (lc == null) {
      lc = Locale.getDefault();
      logger.debug("setLocale: lc==null.  Trying to set to Default Locale: " + lc.getISO3Language());
    }
    Vector<Locale> avail = Localization.Main.getAvailableLocalizationsAsLocales();
    if (avail.contains(lc)) {
      configLocale = lc;
    } else {
      configLocale = Locale.ENGLISH;
      logger.trace("setLocale: Requested locale " + lc.getISO3Language() + " is not supported");
      logger.trace("setLocale: set to fallback of English (EN)");
    }
    logger.trace("setLocale: Locale set to " + configLocale.getLanguage() + "(" + configLocale.getDisplayLanguage() + ")");
  }

  /**
   * Get the locale that is to be used for this configuration
   * @return
   */
  public static Locale getLocale () {
    if (configLocale == null) {
      logger.trace("getLocale: Not set, so try to set to default");
      setLocale(Locale.getDefault());
    }
    return configLocale;
  }

  /**
   * get the list of supported ebook formats.
   *
   * We use the function that can read from a user
   * configuration file (if present), and if that
   * is not present the default resource file
   *
   * @return
   */
  public static void initialiseListOfSupportedEbookFormats () {

    if (EBookFormat.getSupportedFormats() != null) {
      return;
    }
    List<EBookFormat> supportedFormats = new LinkedList<EBookFormat>();
    InputStream is = ConfigurationManager.getResourceAsStream(Constants.MIMETYPES_FILENAME);
    assert is != null;
    Scanner scanner = new Scanner(is);
    String line;
    try {
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        // Ignore blank lines and those starting with #
        if (line.length() == 0 || line.charAt(0) == '#') {
          continue;
        }
        // Split any line into format identifier and mime type
        Scanner lineScanner = new Scanner (line);
        String formatType = null;
        if (lineScanner.hasNext()) formatType = lineScanner.next();
        String mimeType = null;
        if (lineScanner.hasNext()) mimeType = lineScanner.next();
        if (Helper.isNullOrEmpty(formatType) || Helper.isNullOrEmpty(mimeType)) {
          logger.error("Invalid line in Mimetypes file '" + line + "'");
          continue;
        }
        supportedFormats.add(new EBookFormat(formatType,mimeType));
      }
      scanner.close();
      is.close();
    } catch (Exception e) {
      // Error reading the file
    }
    EBookFormat.setSupportedFormats(supportedFormats);
  }
}
