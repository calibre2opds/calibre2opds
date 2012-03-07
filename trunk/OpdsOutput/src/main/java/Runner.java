import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.test.TestDataModel;
import com.gmail.dpierron.calibre.gui.Mainframe;
import com.gmail.dpierron.calibre.opds.Catalog;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.Log4jCatalogCallback;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class Runner {
  private static boolean introDone = false;
  private boolean testMode = false;     // Set this to true to generate a test datamodel
  private final static Logger logger = Logger.getLogger(Runner.class);


  public void run(boolean startGui) throws IOException {
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
      new Catalog(new Log4jCatalogCallback()).createMainCatalog();
    }
  }

  private static void intro() {
    if (!introDone) {
      logger.info("");
      logger.info(Localization.Main.getText("intro.goal"));
      logger.info(Localization.Main.getText("intro.wiki.title") + Localization.Main.getText("intro.wiki.url"));
      logger.info("");
      logger.info(Localization.Main.getText("intro.team.title"));
      logger.info("  * " + Localization.Main.getText("intro.team.list1"));
      logger.info("  * " + Localization.Main.getText("intro.team.list2"));
      logger.info("  * " + Localization.Main.getText("intro.team.list3"));
      logger.info("  * " + Localization.Main.getText("intro.team.list4"));
      logger.info("  * " + Localization.Main.getText("intro.team.list5"));
      logger.info("");
      logger.info(Localization.Main.getText("intro.thanks.1"));
      logger.info(Localization.Main.getText("intro.thanks.2"));
      logger.info("");
      logger
          .info(Localization.Main.getText("usage.intro", ConfigurationManager.INSTANCE.getCurrentProfile().getPropertiesFile().getPath()));
      logger.info("");
      introDone = true;
    }
  }

  private void initLog4J() {
    String[] levels = new String[]{".info", ".debug", ".trace", ".trace.noCachedFileTracing", "STANDARD"};
    String standardLevel = ".info";
    File home = ConfigurationManager.getDefaultConfigurationDirectory();
    System.setProperty("calibre2opds.home", home.getAbsolutePath());
    if (home == null)
      return;
    String defaultOutFileName = "log/log4j.properties";
    File log4jConfig = null;
    for (String level : levels) {
      String outFileName;
      String inFileName;
      if (level.equals("STANDARD")) {
        level = standardLevel;
        outFileName = defaultOutFileName;
        inFileName = "/config.log4j" + level;
      } else {
        outFileName = defaultOutFileName + level;
        inFileName = "/config.log4j" + level;
      }
      log4jConfig = new File(home, outFileName);
      // Copy the file from the resources
      if (!log4jConfig.exists()) { // do not overwrite
        try {
          new File(log4jConfig.getParent()).mkdirs();
          InputStream is = null;
          FileOutputStream os = null;
          try {
            is = this.getClass().getResourceAsStream(inFileName);
            if (is == null) {
              ConfigurationManager.addStartupLogMessage("Cannot find " + inFileName + " in the resources");
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
    ConfigurationManager.addStartupLogMessage("Log4J configuration file is " + log4jConfig.getAbsolutePath());

    // Configure and watch
    PropertyConfigurator.configureAndWatch(log4jConfig.getAbsolutePath(), 3000);
    List<String> startupLogMessages = ConfigurationManager.INSTANCE.getStartupLogMessages();
    if (startupLogMessages != null) {
      for ( String s : startupLogMessages) {
        logger.info(s);
      }
      startupLogMessages.clear();
      startupLogMessages = null;
    }
  }

  public static void run(String[] args, boolean startGui) {
    // Start by setting lcoal to be the same as the system local (or english if it is not one we support)
    Locale lc = Locale.getDefault();
    System.out.println("System language: " + lc.getISO3Language());
    Vector<String> avail = LocalizationHelper.INSTANCE.getAvailableLocalizations();
    Localization.Enum.reloadLocalizations(avail.contains(lc.getISO3Language()) ? lc.getISO3Language() : "en");
    Localization.Main.reloadLocalizations(avail.contains(lc.getISO3Language()) ? lc.getISO3Language() : "en");

    ConfigurationManager.addStartupLogMessage("");
    ConfigurationManager.addStartupLogMessage(Constants.PROGTITLE + Constants.BZR_VERSION);
    ConfigurationManager.addStartupLogMessage("");


    Runner runner = new Runner();
    runner.initLog4J();
    try {
      if (args.length == 1) {
        String profileName = args[0];
        if (ConfigurationManager.INSTANCE.isExistingConfiguration(profileName))
          ConfigurationManager.INSTANCE.changeProfile(profileName);
      }
      runner.run(startGui);
    } catch (IOException e) {
      logger.info(Localization.Main.getText("error.generic", Constants.AUTHOREMAIL));
      e.printStackTrace();
    }
  }
}
