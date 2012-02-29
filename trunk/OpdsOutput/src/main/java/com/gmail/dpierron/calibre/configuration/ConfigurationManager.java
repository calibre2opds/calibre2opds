package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.opds.JDOM;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public enum ConfigurationManager {
  INSTANCE;

  public static final String PROFILES_SUFFIX = ".profile.xml";
  private final static String PROFILE_FILENAME = "profile.xml";
  private final static String CONFIGURATION_FOLDER = ".calibre2opds";
  private final static String DEFAULT_PROFILE = "default";
  private final static String PROPERTY_NAME_CURRENTCONFIGURATION = "CurrentConfiguration";

  private final static Logger logger = Logger.getLogger(ConfigurationManager.class);

  private static File installDirectory;
  private static File configurationDirectory;
  private static File configurationFolder = null;
  private ConfigurationHolder currentProfile;
  private PropertiesBasedConfiguration defaultConfiguration;

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
      System.out.println("Default Configuration folder home redirected to " + redirectToNewHome.getPath());
    }

    // now check for redirect
    if (configurationFolder != null && configurationFolder.exists()) {
      File redirect = new File(configurationFolder, ".redirect");
      if (redirect.exists()) {
        System.out.println(".redirect file found in " + configurationFolder.getPath());
        try {
          BufferedReader fr = null;
          try {
            fr = new BufferedReader(new FileReader(redirect));
            String newHomeFileName = fr.readLine();
            File newHome = new File(newHomeFileName);
            if (! newHome.exists()) {
              System.out.println("... unable to find redirect folder " + newHome.getPath());
              System.out.println("... so redirect abandoned");
            } else {
              // log4j is not yet initialized
              System.out.println("redirecting home folder to " + newHome.getAbsolutePath());
              configurationFolder = getDefaultConfigurationDirectory(newHome);
            }
          } finally {
            if (fr != null)
              fr.close();
          }
        } catch (IOException e) {
          System.out.println("... failure reading .redirect file ");
          System.out.println("... so redirect abandoned");
        }
      }
      return configurationFolder;
    }

    //  NOw all the standard locations if we do not already have an answer

    // try the CALIBRE2OPDS_CONFIG environment variable
    String configDirectory = System.getenv("CALIBRE2OPDS_CONFIG");
    if (Helper.isNotNullOrEmpty(configDirectory)) {
      configurationFolder = new File(configDirectory);
      System.out.println("CALIBRE2OPDS_CONFIG=" + configurationFolder);
      if (! configurationFolder.exists()) {
        System.out.println("... but specified folder dows not exist");
        configurationFolder = null;  
      }
    }

    // try with user.home
    if (configurationFolder == null || !configurationFolder.exists()) {
      String userHomePath = System.getProperty("user.home");
      if (Helper.isNotNullOrEmpty(userHomePath)) {
        configurationFolder = new File(userHomePath);
        System.out.println("Try Configuration folder set to user home: " + configurationFolder);
        if (! configurationFolder.exists()) {
          System.out.println("... but specified folder does not exist");
          configurationFolder = null;
        }
      }
    }

    if (configurationFolder == null || !configurationFolder.exists()) {
      // try with tilde
      configurationFolder = new File("~");
      System.out.println("Try Configuration folder set to tilde: " + configurationFolder);
      if (! configurationFolder.exists()) {
        System.out.println("... but specified folder does not exist");
        configurationFolder = null;
      }
    }

    if (configurationFolder == null || !configurationFolder.exists()) {
      // hopeless, try and find out where the JAR was stored
      configurationFolder = getInstallDirectory();
      System.out.println("Try Configuration folder set to  .jar location: " + configurationFolder);
      if (! configurationFolder.exists()) {
        System.out.println("... but specified folder dowes not exist");
        configurationFolder = null;
      }
    }

    if (configurationFolder == null) {
      System.out.println("ERROR: failed to find a suitable configuration folder");
      System.out.println("Exit(-1)");
      System.exit(-1);
    } else {
      configurationFolder = new File(configurationFolder, CONFIGURATION_FOLDER);
      if (!configurationFolder.exists()) {
        configurationFolder.mkdirs();
        System.out.println("Default Configuration folder created: " + configurationFolder.getPath());
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
  public File getConfigurationFile() {
    File configurationFolder = getConfigurationDirectory();

    if (configurationFolder != null && configurationFolder.exists()) {
      // found the user home, let's check for the configuration file
      String filename = PROFILE_FILENAME;
      return new File(configurationFolder, filename);
    } else
      return null;

  }

  public boolean isHacksEnabled() {
    return Helper.isNotNullOrEmpty(System.getenv("CALIBRE2OPDS_HACKSENABLED"));
  }
}
