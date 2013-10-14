package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.JDOM;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.junit.runner.Runner;

import java.io.*;
import java.net.URL;
import java.util.*;

public enum ConfigurationManager {
  INSTANCE;

  public static final String PROFILES_SUFFIX = ".profile.xml";
  private final static String PROFILE_FILENAME = "profile.xml";
  private final static String CONFIGURATION_FOLDER = ".calibre2opds";
  private final static String DEFAULT_PROFILE = "default";
  private final static String PROPERTY_NAME_CURRENTCONFIGURATION = "CurrentConfiguration";

  private final static Logger logger = Logger.getLogger(ConfigurationManager.class);
  private static List<String> startupLogMessages;

  private static File installDirectory;
  private static File configurationDirectory;
  private static File configurationFolder = null;
  private static ConfigurationHolder currentProfile;
  private static PropertiesBasedConfiguration defaultConfiguration;
  private static Locale configLocale = null;

  PropertiesBasedConfiguration getDefaultConfiguration() {
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

  public ConfigurationHolder getCurrentProfile() {
    if (currentProfile == null) {
      logger.trace("getCurrentProfile - currentProfile not set");
      currentProfile = new ConfigurationHolder(new File(getConfigurationDirectory(), getCurrentProfileName() + PROFILES_SUFFIX));
      Configuration.setConfiguration(currentProfile);
    }
    return currentProfile;
  }

  public String getCurrentProfileName() {
    String s = getDefaultConfiguration().getProperty(PROPERTY_NAME_CURRENTCONFIGURATION);
    if (Helper.isNotNullOrEmpty(s))
      return s;
    else
      return DEFAULT_PROFILE;
  }

  public void changeProfile(String profileName) {
    logger.trace("changeProfile to " + profileName);
    getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, profileName);
    currentProfile = null;
    getCurrentProfile();
  }

  public void copyCurrentProfile(String newProfileName) {
    getCurrentProfile().setPropertiesFile(new File(getConfigurationDirectory(), newProfileName + PROFILES_SUFFIX));
    getCurrentProfile().save();
    getDefaultConfiguration().setProperty(PROPERTY_NAME_CURRENTCONFIGURATION, newProfileName);
    currentProfile = null;
    getCurrentProfile();
  }

  public boolean isExistingConfiguration(String filename) {
    for (String existingConfigName : getExistingConfigurations()) {
      if (existingConfigName.equalsIgnoreCase(filename))
        return true;
    }
    return false;
  }


  public List<String> getExistingConfigurations() {
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

  public static File getInstallDirectory() {
    if (installDirectory == null) {
      URL mySource = ConfigurationHolder.class.getProtectionDomain().getCodeSource().getLocation();
      File sourceFile = new File(mySource.getPath());
      installDirectory = sourceFile.getParentFile();
    }
    return installDirectory;
  }
  
  public File getConfigurationDirectory() {
    if (configurationDirectory == null) {
      //            logger.trace("getConfigurationDirectory - configurationDirectory not set");
      configurationDirectory = getDefaultConfigurationDirectory();
      //            logger.debug("getConfigurationDirectory=" + configurationDirectory.getPath());
    }
    return configurationDirectory;
  }

  public static File getDefaultConfigurationDirectory() {
    return getDefaultConfigurationDirectory(null);
  }

  /**
   * Work out where the configuration folder is located.
   * Note that at this poin t log4j will not have been initiaised
   * so send any messages to system.out.
   * @param redirectToNewHome
   * @return
   */
  private static File getDefaultConfigurationDirectory(File redirectToNewHome) {


    // redirect is set, try the new home folder
    if (Helper.isNotNullOrEmpty(redirectToNewHome)) {
      configurationFolder = redirectToNewHome;
      addStartupLogMessage(Localization.Main.getText("startup.newhome", redirectToNewHome.getPath()));
      // addStartupLogMessage(Localization.Main.getText("startup.newhome", redirectToNewHome.getPath()));
    }

        // now check for redirect
    if (configurationFolder != null && configurationFolder.exists()) {
      File redirect = new File(configurationFolder, ".redirect");
      if (redirect.exists()) {
        addStartupLogMessage(Localization.Main.getText("startup.redirectfound", configurationFolder.getPath()));
        try {
          BufferedReader fr = null;
          try {
            fr = new BufferedReader(new FileReader(redirect));
            String newHomeFileName = fr.readLine();
            File newHome = new File(newHomeFileName);
            if (! newHome.exists()) {
              addStartupLogMessage(Localization.Main.getText("startup.redirectnotfound", newHome.getPath()));
              addStartupLogMessage(Localization.Main.getText("startup.redirectabandoned"));
            } else {
              // log4j is not yet initialized
              addStartupLogMessage(Localization.Main.getText("startup.redirecting",newHome.getAbsolutePath()));
              configurationFolder = getDefaultConfigurationDirectory(newHome);
              return configurationFolder = getDefaultConfigurationDirectory();
            }
          } finally {
            if (fr != null)
              fr.close();
          }
        } catch (IOException e) {
          addStartupLogMessage(Localization.Main.getText("startup.redirectreadfail"));
          addStartupLogMessage(Localization.Main.getText("startup.redirectabandoned"));
        }
      }
      if (redirectToNewHome == null)
        addStartupLogMessage(Localization.Main.getText("startup.configusing", configurationFolder));
      return configurationFolder;
    }

    //  Now all the standard locations if we do not already have an answer

    // try the CALIBRE2OPDS_CONFIG environment variable
    String configDirectory = System.getenv("CALIBRE2OPDS_CONFIG");
    if (Helper.isNotNullOrEmpty(configDirectory)) {
      configurationFolder = new File(configDirectory);
      ConfigurationManager.addStartupLogMessage("CALIBRE2OPDS_CONFIG=" + configDirectory);
      if (! configurationFolder.exists()) {
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
        configurationFolder = null;  
      }
    }

    // try with user.home
    if (configurationFolder == null || !configurationFolder.exists()) {
      String userHomePath = System.getProperty("user.home");
      if (Helper.isNotNullOrEmpty(userHomePath)) {
        configurationFolder = new File(userHomePath);
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.folderuserhome", configurationFolder));
        if (configurationFolder.exists()) {
          configurationFolder = new File(configurationFolder, CONFIGURATION_FOLDER);
        } else {
          ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
          configurationFolder = null;
        }
      }
    }

    if (configurationFolder == null || !configurationFolder.exists()) {
      // try with tilde
      configurationFolder = new File("~");
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldertilde", configurationFolder));
      if (configurationFolder.exists()) {
        configurationFolder = new File(configurationFolder, CONFIGURATION_FOLDER);
      } else {
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
        configurationFolder = null;
      }
    }

    if (configurationFolder == null || !configurationFolder.exists()) {
      // hopeless, try and find out where the JAR was stored
      configurationFolder = getInstallDirectory();
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.folderjar", configurationFolder));
      if (configurationFolder.exists()) {
        configurationFolder = new File(configurationFolder, CONFIGURATION_FOLDER);
      } else {
        // ITIMPI:   Is this condition really possible as surely the InstallDirectory exists!!
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
        configurationFolder = null;
      }
    }

    if (configurationFolder == null) {
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotfound"));
      ConfigurationManager.addStartupLogMessage("Exit(-1)");
      System.exit(-1);
    } else {
      if (! configurationFolder.exists()) {
        configurationFolder.mkdirs();
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldercreated", configurationFolder.getPath()));
        return configurationFolder;
      }
    }
    return getDefaultConfigurationDirectory();    // Recurse to allow for re-direction
  }

  /**
   * Special variant of this that checks several locations for the file before
   * resorting to using the built-in resource file.  The purpose is to allow
   * the user to over-ride the built-in resource files if so required.
   * @param filename
   * @return
   */
  public InputStream getResourceAsStream(String filename) {
    InputStream ins = null;
    try {
        // Try user configuration folder
        ins = new FileInputStream(getConfigurationDirectory() + "/" + filename);
        logger.info("Resource '" + filename + "' loaded from Configuration folder");
    } catch (FileNotFoundException e) {
      try {
          // If that fails, try install folder
          ins = new FileInputStream (getInstallDirectory() + "/" + filename);
          logger.info("Resource '" + filename + "' loaded from Install folder");
      } catch (FileNotFoundException f) {
          // If still not found then use built-in resource
        ins = JDOM.class.getResourceAsStream(filename);
      }
    }
    return ins;
  }
  
  // ITIMPI:  Method does not appear to be used anywhere!
/*
  public File getConfigurationFile() {
    File configurationFolder = getConfigurationDirectory();

    if (configurationFolder != null && configurationFolder.exists()) {
      // found the user home, let's check for the configuration file
      String filename = PROFILE_FILENAME;
      return new File(configurationFolder, filename);
    } else
      return null;

  }
*/

  public boolean isHacksEnabled() {
    return Helper.isNotNullOrEmpty(System.getenv("CALIBRE2OPDS_HACKSENABLED"));
  }


  /**
   * Add a log message to the array of those to be kept for recoding to
   * the log after log4j has been initialised
   *
   * @param message
   */
  public static void addStartupLogMessage (String message) {
    System.out.println(message);
    if (startupLogMessages == null)
      startupLogMessages = new ArrayList<String>();
    startupLogMessages.add(message);
  }

  /**
   * Get the list of startup messages that have been built up
   * @return
   */
  public List<String> getStartupLogMessages() {
    return startupLogMessages;
  }

  /**
   * Set the lcoal that we are using for this generation
   * If the one requested is not one we support we set it to English
   * @param lc
   */
  public void setLocale (Locale lc){
    if (lc == null) {
      lc = Locale.getDefault();
      logger.debug("setLocale: lc==null.  Trying to set to Default Locale: " + lc.getISO3Language());
    }
    Vector<String> avail = LocalizationHelper.INSTANCE.getAvailableLocalizations();
    if (avail.contains(lc.getISO3Language())) {
      configLocale = lc;
    } else {
      configLocale = Locale.getDefault();
      logger.trace("setLocale: Requested locale " + lc.getISO3Language() + " is not supported");
      if (avail.contains(configLocale.getISO3Language())) {
        logger.trace("setLocale: Locale set");
      } else {
        logger.trace("setLocale: set to fallback of English (EN)");
        lc = new Locale("EN");
      }
    }
    logger.trace("setLocale: Locale set to " + lc.getISO3Language());
  }

  /**
   * Get the locale that is to be used for this configuration
   * @return
   */
  public Locale getLocale () {
    if (configLocale == null) {
      logger.trace("getLocale: Not set, so try to set to default");
      setLocale(Locale.getDefault());
    }
    return configLocale;
  }
}
