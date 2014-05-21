package com.gmail.dpierron.calibre.configuration;

import com.gmail.dpierron.calibre.datamodel.EBookFormat;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.JDOM;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import javax.swing.*;
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
  private static boolean guiMode = false;
  // Listof formats that are used in the current profile
  private static List<EBookFormat> profileFormats = null;

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

  private static String startupMessagesForDisplay() {
    StringBuffer s = new StringBuffer("\n\nLOG:");
    for (String m : startupLogMessages) {
      s.append( m + "\n");
    }
    return s.toString();
  }
  /**
   * Check for redirection (if any
   *
   * @param redirectToNewHome  Folder to check
   * @return                   Final result (same a redirectToNewHome if redirect not active.
   */
  private static File configurationRedirect(File redirectToNewHome) {
    assert redirectToNewHome != null;
    assert redirectToNewHome.exists() == true;

    File redirectConfigurationFile = new File(redirectToNewHome, ".redirect");
    if (! redirectConfigurationFile.exists()) {
      addStartupLogMessage(Localization.Main.getText("startup.configusing", redirectToNewHome));
      return redirectToNewHome;
    }
    // Attempt to follow a redirect
    String message = Localization.Main.getText("startup.redirectfound", redirectToNewHome.getPath());
    addStartupLogMessage(message);
    try {
      BufferedReader fr = null;
      try {
        fr = new BufferedReader(new FileReader(redirectConfigurationFile));
        String newHomeFileName = fr.readLine();
        File newHome = new File(newHomeFileName);
        if (newHome.exists()) {
          addStartupLogMessage(Localization.Main.getText("startup.redirecting", newHome.getAbsolutePath()));
          // Allow for recursion
          return configurationRedirect(newHome);
        } else {
          String message2 = Localization.Main.getText("startup.redirectnotfound", newHome.getPath());
          String message3 = Localization.Main.getText("startup.redirectabandoned");
          if (guiMode) {
            JOptionPane.showMessageDialog(null, message + "\n" + message2 + "\n" + message3 + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
          }
          addStartupLogMessage(message3);
          addStartupLogMessage(message2);
          System.exit(-1);
        }
      } finally {
        if (fr != null)
          fr.close();
      }
    } catch (IOException e) {
      String message2 = Localization.Main.getText("startup.redirectreadfail");
      String message3 = Localization.Main.getText("startup.redirectabandoned");
      if (guiMode) {
        JOptionPane.showMessageDialog(null, message + "\n" + message2 + "\n" + message3 + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
      }
      addStartupLogMessage(message2);
      addStartupLogMessage(message3);
      ConfigurationManager.addStartupLogMessage("Exit(-2)");
      System.exit(-2);
    }
    // Do not think we can actually get here!
    // However if we do assume no redirect found
    addStartupLogMessage(Localization.Main.getText("startup.configusing", redirectToNewHome));
    return redirectToNewHome;
  }
  /**
   * Work out where the configuration folder is located.
   * Note that at this poin t log4j will not have been initiaised
   * so send any messages to system.out.

   * @return  Folder to be used for configuration purposes
   */
  public static File getDefaultConfigurationDirectory() {

    // Check for redirect
    // Note that redirect's can be chained.

      // If we got here then configuration folder exists, and no redirect is active
    if (configurationFolder != null) {
      addStartupLogMessage(Localization.Main.getText("startup.configusing", configurationFolder));
      return configurationFolder;
    }
    assert configurationFolder == null;

    //  Now all the standard locations if we do not already have an answer

    // try the CALIBRE2OPDS_CONFIG environment variable
    String configDirectory = System.getenv("CALIBRE2OPDS_CONFIG");
    if (Helper.isNotNullOrEmpty(configDirectory)) {
      File envConfigurationFolder = new File(configDirectory);
      ConfigurationManager.addStartupLogMessage("CALIBRE2OPDS_CONFIG=" + envConfigurationFolder);
      if (envConfigurationFolder.exists()) {
        // Allow for redirect
        return configurationRedirect(envConfigurationFolder);
      } else {
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
        configurationFolder = null;  
      }
    }
    assert configurationFolder == null;
    File configurationFolderParent = null;    // Set to the first potential parent for configuration folder.

    // try with user.home  (normal default)

    String userHomePath = System.getProperty("user.home");
    if (Helper.isNotNullOrEmpty(userHomePath)) {
      File homeParent = new File(userHomePath);
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.folderuserhome", homeParent));
      if (homeParent.exists()) {
        File homeConfigurationFolder = new File(homeParent, CONFIGURATION_FOLDER);
        if (homeConfigurationFolder.exists()) {
          // Allow for redirection
          ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.folderuserhome", homeConfigurationFolder));
          return configurationRedirect(homeConfigurationFolder);
        }
        configurationFolderParent = homeParent;       // Set as potential home for configuration folder
      } else {
        ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
      }
    }
    assert configurationFolder == null;

    // try with tilde (fallback default on linux/mac)

    File tildeParent = new File("~");
    ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldertilde", configurationFolderParent));
    if (tildeParent.exists()) {
      File tildeConfigurationFolder = new File(tildeParent, CONFIGURATION_FOLDER);
      if (tildeConfigurationFolder.exists()) {
        return configurationRedirect(tildeConfigurationFolder);
      } else {
        if (configurationFolderParent == null) configurationFolderParent = tildeConfigurationFolder;
      }
    } else {
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
    }
    assert configurationFolder == null;

    // Last ditch effort - try the install folder

    File  installConfigurationFolderParent = getInstallDirectory();
    ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.folderjar", installConfigurationFolderParent));
    if (installConfigurationFolderParent.exists()) {
      File installConfigurationFolder = new File(installConfigurationFolderParent, CONFIGURATION_FOLDER);
      if (installConfigurationFolder.exists()) {
        // Allow for redirect
        return configurationRedirect(installConfigurationFolder);
      } else {
        if (configurationFolderParent == null) configurationFolderParent = installConfigurationFolderParent;
      }
    } else {
      // ITIMPI:   Is this condition really possible as surely the InstallDirectory exists!!
      ConfigurationManager.addStartupLogMessage(Localization.Main.getText("startup.foldernotexist"));
    }
    assert configurationFolder == null;

    // No suitable location found (is this actually possible!
    if (configurationFolderParent == null) {
      String message = Localization.Main.getText("startup.foldernotfound");
      if (guiMode) {
        JOptionPane.showMessageDialog(null, message + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
      }
      ConfigurationManager.addStartupLogMessage(message);
      ConfigurationManager.addStartupLogMessage("Exit(-1)");
      System.exit(-3);
    }
    assert configurationFolderParent != null && configurationFolder == null && configurationFolderParent.exists();

    // OK - configuration folder does not exist so we need to create it

    File newConfigurationFolder = new File (configurationFolderParent,CONFIGURATION_FOLDER);
    assert !newConfigurationFolder.exists();
    if (! newConfigurationFolder.mkdirs()) {
      String message = Localization.Main.getText("startup.foldernotcreatefailed", newConfigurationFolder);
      if (guiMode) {
        JOptionPane.showMessageDialog(null, message + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
      }
      ConfigurationManager.addStartupLogMessage(message);
      ConfigurationManager.addStartupLogMessage("Exit(-1)");
      System.exit(-4);
    }
    addStartupLogMessage(Localization.Main.getText("startup.configusing", newConfigurationFolder));
    return configurationFolder;
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
   * Clear down the list of startup messages.
   * (Just saves a little RAM if there were a lot?)
   */
  public void clearStartupLogMessages() {
    startupLogMessages = null;
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

  /**
   * set the whether we are running in GUI mode or not.
   *
   * This setting is used under certain dtartup error conditions to
   * determine whether we pp up an error dialog or simply log the error
   * @param b
   */
  public static void setGuiMode(boolean b) {
    guiMode = b;
  }

  /**
   * get the list of supported ebook formats.
   *
   * We use the funcyion that can read from a iser
   * configuration file (if present), and if that
   * is not present the default resource file
   *
   * @return
   */
  public void initialiseListOfSupportedEbookFormats () {

    if (EBookFormat.getSupportedFormats() != null) {
      return;
    }
    List<EBookFormat> supportedFormats = new LinkedList<EBookFormat>();
    InputStream is = getResourceAsStream(Constants.MIMETYPES_FILENAME);
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
