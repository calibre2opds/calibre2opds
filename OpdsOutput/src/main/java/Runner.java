import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.test.TestDataModel;
import com.gmail.dpierron.calibre.gui.Mainframe;
import com.gmail.dpierron.calibre.opds.Catalog;
import com.gmail.dpierron.calibre.opds.CatalogManager;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.Log4jCatalogCallback;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.i18n.Localization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.status.StatusLogger;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class Runner {

  private static Logger logger = null;

  private static boolean introDone = false;
  private static boolean testMode = false;     // Set this to true to generate a test datamodel
  private static boolean guiMode;


  /**
   * Constructor
   * @param startGui
   * @throws IOException
   */
  static void run(boolean startGui) throws IOException {
    intro();
    if (testMode) {
      new TestDataModel().testDataModel();
    } else if (startGui) {
      java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
          new Mainframe().setVisible(true);
        }
      });
    } else {
      Log4jCatalogCallback log4jCatalogCallback =  new Log4jCatalogCallback();
      log4jCatalogCallback.setStartGui(false);
      new Catalog(log4jCatalogCallback).createMainCatalog();
    }
  }

  /**
   * Constructor
   * Start of run initialisation
   * @param args
   * @param startGui
   */
  public static void run(String[] args, boolean startGui) {
    if (logger == null) initLog4J();
    assert configurationFolder != null;
    ConfigurationManager.setConfigurationDirectory(configurationFolder);
    ConfigurationManager.initialiseListOfSupportedEbookFormats();

    Locale lc = Locale.getDefault();
    Localization.Main.reloadLocalizations(Locale.ENGLISH);      // Initalize Localization object to English
    Vector<Locale> avail = Localization.Main.getAvailableLocalizationsAsLocales();
    Localization.Enum.reloadLocalizations(avail.contains(lc) ? lc : Locale.ENGLISH);
    Localization.Main.reloadLocalizations(avail.contains(lc) ? lc : Locale.ENGLISH);

    guiMode = startGui;
    logger.info("");
    logger.info("--------------------------------------------");
    logger.info(Constants.PROGTITLE + Constants.BZR_VERSION);
    logger.info("**** " + (startGui?"GUI":"BATCH") + " MODE ****");
    logger.info("");
    logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty(
        "os.arch") + ")");
    logger.info("LANG: " + lc.getLanguage() + " (" + lc.getDisplayLanguage() + ")");
    logger.info("JAVA: " + System.getProperty("java.specification.version") + " (" + System.getProperty("java.version") + ")");
    logger.info("");

    String levelText = "";
    if (logger.isTraceEnabled()) {
      levelText += "TRACE + ";
    }
    if (logger.isDebugEnabled()) {
      levelText += "DEBUG + ";
    }
    if (logger.isInfoEnabled()) {
      levelText += "INFO + ";
    }
    levelText += "WARN + ERROR + FATAL";
    logger.info("");
    logger.info ("LOG LEVELS: " + levelText);
    logger.info("");
    try {
      String currentProfileName = ConfigurationManager.getCurrentProfileName();
      logger.info(Localization.Main.getText("startup.profiledefault", currentProfileName));
      switch (args.length) {
        case 0:
            // This is the normal default where we use the last profile used in the GUI.
            break;
        default:
            // We seem to have more parameters than expected.
            // Log the details and then assume that the first may be a profile
            String argstring = "";
            for (String s : args) argstring += s + " ";
            logger.warn(Localization.Main.getText("warn.extraParameters", "  " + argstring ));  Helper.statsWarnings++;
            // FALLTHRU
        case 1:
            // It appears that a profile has been supplied
            String profileName = args[0];
            if (ConfigurationManager.isExistingConfiguration(profileName) == null) {
              logger.error(Localization.Main.getText("startup.profilemissing", profileName)); Helper.statsErrors++;
              if (!startGui)
                System.exit(-3);
            } else {
              logger.info(Localization.Main.getText("Switching to profile:  {0}", ConfigurationManager.isExistingConfiguration(profileName)));
              ConfigurationManager.changeProfile(ConfigurationManager.isExistingConfiguration(profileName), startGui);
            }
            break;
      }


      // runner.run(startGui);
      run(startGui);
    } catch (IOException e) {
      logger.info(Localization.Main.getText("error.generic", Constants.AUTHOREMAIL));
      e.printStackTrace();
    }
  }

  /**
   * Display project team information
   * (protected so only displayed once if called recursively)
   */
  private static void intro() {
    if (!introDone) {
      logger.info("");
      logger.info(Localization.Main.getText("intro.goal"));
      logger.info(Localization.Main.getText("intro.wiki.title")
                  + Localization.Main.getText("intro.wiki.url"));
      logger.info("");
      // TODO:   ITIMPI: List of members revised to reflect active developers!
      logger.info(Localization.Main.getText("intro.team.title"));
      logger.info("  * " + Localization.Main.getText("intro.team.list1"));
      logger.info("");
      logger.info(Localization.Main.getText("intro.team.title2"));
      logger.info("  * " + Localization.Main.getText("intro.team.list2"));
      logger.info("  * " + Localization.Main.getText("intro.team.list3"));
      logger.info("  * " + Localization.Main.getText("intro.team.list4"));
      logger.info("  * " + Localization.Main.getText("intro.team.list5"));
      logger.info("");
      logger.info(Localization.Main.getText("intro.thanks.1"));
      logger.info(Localization.Main.getText("intro.thanks.2"));
      logger.info("");
      logger.info(Localization.Main.getText("usage.intro", ConfigurationManager.getCurrentProfile().getPropertiesFile().getPath()));
      logger.info("");
      introDone = true;
    }
  }

  /**
   * log4j initialisation
   * *
   * NOTE:  As we are going to specify the log configuration file
   *        programatically we must not use any classes that try
   *        and instantiate a logger berfore this has happened or
   *        we will find there is an issue with getting logger
   *        started as expected.
   */
  private static void initLog4J() {
    System.setProperty("org.apache.logging.log4j.level", "INFO");   // Set default logging level for no configuration file
    System.setProperty("org.apache.logging.simplelogj.level", "INFO");
    System.setProperty("Log4jDefaultStatusLevel", "INFO");

    String[] levels = new String[]{".info", ".debug", ".trace", ".trace.FileCachingSystem", "STANDARD"};
    String defaultLevel = ".info";
    configurationFolder = getDefaultConfigurationDirectory();
    if (configurationFolder == null) {
      addStartupLogMessage("ERROR: Failed to initialize logging - could not find suitable log folder");
      System.exit(-5);
    }

    // Create the standard default list of log4j onfiguration files if they do not already exist

    System.setProperty("calibre2opds.home", configurationFolder.getAbsolutePath());
    addStartupLogMessage("calibre2opds.home=" + configurationFolder.getAbsolutePath());
    String defaultOutFileName = "log/log4j2";
    File log4jConfig = null;
    for (String level : levels) {
      String outFileName;
      String inFileName = "log4j2";
      if (level.equals("STANDARD")) {
        outFileName = defaultOutFileName;
        inFileName += defaultLevel;
      } else {
        outFileName = defaultOutFileName + level;
        inFileName += level;
      }
      inFileName += Constants.XML_EXTENSION;
      outFileName += Constants.XML_EXTENSION;
      log4jConfig = new File(configurationFolder, outFileName);
      // If the target does not already exist thenwe copy the
      // default file of this type from from the resources
      if (!log4jConfig.exists()) { // do not overwrite
        try {
          new File(log4jConfig.getParent()).mkdirs();
          InputStream is = null;
          FileOutputStream os = null;
          try {
            is = Runner.class.getResourceAsStream(inFileName);
            if (is == null) {
              addStartupLogMessage("Cannot find " + inFileName + " in the resources");
            }
            os = new FileOutputStream(log4jConfig);
            byte buffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
              os.write(buffer, 0, bytesRead);
            }
          } finally {
            if (os != null) {
              os.flush();
              os.close();
            }
            if (is != null) {
              is.close();
            }
          }
        } catch (Exception e) {
          e.printStackTrace(System.out);
        }
      }
    }
    addStartupLogMessage("Log4j2 configuration file is " + log4jConfig.getAbsolutePath());

    // Configure and watch
    System.setProperty("log4j.configurationFile", log4jConfig.getAbsolutePath());

    try {
      ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jConfig));
      Configurator.initialize(null, source);
      logger = LogManager.getLogger(Runner.class.getName());
      addStartupLogMessage("Using log4j2 configuration file " + log4jConfig);
    } catch (java.io.FileNotFoundException e) {
      // Ignore?
    } catch (java.io.IOException f) {
      // Ignore?
    }
    // Now get saved messages into the log
    for ( String s : getStartupLogMessages()) {
      logger.debug(s);
    }
    clearStartupLogMessages();
  }

  private static List<String> startupLogMessages;

  /**
   * Add a log message to the array of those to be kept for replaying to
   * the log after log4j has been initialised.  The message is also
   * output to the console so that we can get some basic diagnsotics
   * even if the program fails to start properly.
   *
   * @param message
   */
  public static void addStartupLogMessage (String message) {
    System.out.println(message);
    if (startupLogMessages == null) {
      startupLogMessages = new ArrayList<String>();
    }
    startupLogMessages.add(message);
  }

  /**
   * Get the list of startup messages that have been built up
   * @return
   */
  public static List<String> getStartupLogMessages() {
    return startupLogMessages;
  }

  /**
   * Clear down the list of startup messages.
   * (Just saves a little RAM if there were a lot?)
   */
  public static void clearStartupLogMessages() {
    startupLogMessages = null;
  }
  /**
   * Get the startup messsages
   *
   * @return
   */
  private static String startupMessagesForDisplay() {
    StringBuffer s = new StringBuffer("\n\nLOG:");
    for (String m : startupLogMessages) {
      s.append(m + "\n");
    }
    return s.toString();
  }

  private static File configurationFolder = null;
  private final static String CONFIGURATION_FOLDER = ".calibre2opds";

  /**
   * Check for redirection (if any)
   *
   * @param redirectToNewHome  Folder to check
   * @return                   Final result (same a redirectToNewHome if redirect not active.
   */
  private static File configurationRedirect(File redirectToNewHome) {
    assert redirectToNewHome != null;
    assert redirectToNewHome.exists() == true;

    File redirectConfigurationFile = new File(redirectToNewHome, ".redirect");
    if (! redirectConfigurationFile.exists()) {
      addStartupLogMessage("Using configuration folder: " + redirectToNewHome);
      return redirectToNewHome;
    }
    // Attempt to follow a redirect
    String message = ".redirect file found in " + redirectToNewHome.getPath();
    addStartupLogMessage(message);
    try {
      BufferedReader fr = null;
      try {
        fr = new BufferedReader(new FileReader(redirectConfigurationFile));
        String newHomeFileName = fr.readLine();
        File newHome = new File(newHomeFileName);
        if (newHome.exists()) {
          addStartupLogMessage("redirecting home folder to " + newHome.getAbsolutePath());
          // Allow for recursion
          return configurationRedirect(newHome);
        } else {
          String message2 = "... unable to find redirect folder " + newHome.getPath();
          String message3 = "... so redirect abandoned";
          if (guiMode) {
            JOptionPane.showMessageDialog(null, message + "\n" + message2 + "\n" + message3 + startupMessagesForDisplay(), Constants.PROGNAME,
                JOptionPane.ERROR_MESSAGE);
          }
          addStartupLogMessage(Helper.getTextFromPseudoHtmlText(message3));
          addStartupLogMessage(Helper.getTextFromPseudoHtmlText(message2));
          System.exit(-1);
        }
      } finally {
        if (fr != null)
          fr.close();
      }
    } catch (IOException e) {
      String message2 = "... failure reading .redirect file";
      String message3 = "... so redirect abandoned";
      if (guiMode) {
        JOptionPane.showMessageDialog(null, message + "\n" + message2 + "\n" + message3 + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
      }
      addStartupLogMessage(Helper.getTextFromPseudoHtmlText(message2));
      addStartupLogMessage(Helper.getTextFromPseudoHtmlText(message3));
      addStartupLogMessage("Exit(-2)");
      System.exit(-2);
    }
    // Do not think we can actually get here!
    // However if we do assume no redirect found
    addStartupLogMessage("Using configuration folder: " + redirectToNewHome);
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
      addStartupLogMessage("Using configuration folder: " + configurationFolder);
      return configurationFolder;
    }
    assert configurationFolder == null;

    //  Now all the standard locations if we do not already have an answer

    // try the CALIBRE2OPDS_CONFIG environment variable
    String configDirectory = System.getenv("CALIBRE2OPDS_CONFIG");
    if (configDirectory != null) {
      File envConfigurationFolder = new File(configDirectory);
      addStartupLogMessage("CALIBRE2OPDS_CONFIG=" + envConfigurationFolder);
      if (envConfigurationFolder.exists()) {
        // Allow for redirect
        return configurationRedirect(envConfigurationFolder);
      } else {
        addStartupLogMessage("... but specified folder does not exist");
        configurationFolder = null;
      }
    }
    assert configurationFolder == null;
    File configurationFolderParent = null;    // Set to the first potential parent for configuration folder.

    // try with user.home  (normal default)

    String userHomePath = System.getProperty("user.home");
    if (userHomePath != null) {
      File homeParent = new File(userHomePath);
      addStartupLogMessage("Try configuration folder in user home folder: " + homeParent);
      if (homeParent.exists()) {
        File homeConfigurationFolder = new File(homeParent, CONFIGURATION_FOLDER);
        if (homeConfigurationFolder.exists()) {
          // Allow for redirection
          addStartupLogMessage("Try configuration folder in user home folder: " + homeConfigurationFolder);
          return configurationRedirect(homeConfigurationFolder);
        }
        configurationFolderParent = homeParent;       // Set as potential home for configuration folder
      } else {
        addStartupLogMessage("... but specified folder does not exist");
      }
    }
    assert configurationFolder == null;

    // try with tilde (fallback default on linux/mac)

    File tildeParent = new File("~");
    addStartupLogMessage("Try configuration folder from tilde folder: " + configurationFolderParent);
    if (tildeParent.exists()) {
      File tildeConfigurationFolder = new File(tildeParent, CONFIGURATION_FOLDER);
      if (tildeConfigurationFolder.exists()) {
        return configurationRedirect(tildeConfigurationFolder);
      } else {
        if (configurationFolderParent == null) configurationFolderParent = tildeConfigurationFolder;
      }
    } else {
      addStartupLogMessage("... but specified folder does not exist");
    }
    assert configurationFolder == null;

    // Last ditch effort - try the install folder

    File  installConfigurationFolderParent = Helper.getInstallDirectory();
    addStartupLogMessage("Try configuration folder from .jar location: " + installConfigurationFolderParent);
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
      addStartupLogMessage("... but specified folder does not exist");
    }
    assert configurationFolder == null;

    // No suitable location found (is this actually possible!
    if (configurationFolderParent == null) {
      String message = "No suitable configuration folder found";
      if (guiMode) {
        JOptionPane.showMessageDialog(null, message + startupMessagesForDisplay(), Constants.PROGNAME, JOptionPane.ERROR_MESSAGE);
      }
      addStartupLogMessage(message);
      addStartupLogMessage("Exit(-1)");
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
      addStartupLogMessage(message);
      addStartupLogMessage("Exit(-1)");
      System.exit(-4);
    }
    configurationFolder = newConfigurationFolder;
    addStartupLogMessage("Using configuration folder: " + configurationFolder);
    return configurationFolder;
  }

}
