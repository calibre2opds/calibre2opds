package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.ReadOnlyStanzaConfigurationInterface;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Log4jCatalogCallback implements CatalogCallbackInterface {
  private final static Logger logger = Logger.getLogger(Catalog.class);
  private final static String doNot = Localization.Main.getText("config.negate");
  private final static String yes = Localization.Main.getText("boolean.yes");
  private final static String no = Localization.Main.getText("boolean.no");
  // step progress indicator
  ProgressIndicator progressStep = new ProgressIndicator().setIndicator('*');
  protected boolean continueGenerating = true;
  private boolean startGui = true;

  /**
   * Dump the value for the given option to the log file
   * <p/>
   * Makes the following assumptions about entries in the localization
   * properties files for each option to be dumped:
   * - The name in the localization file is of the form config.xxxx.label
   * - The text associated with the name is the text to be used for dumping
   * - If the xxxx part starts with "No" (or the localised equivalent)
   * then any boolean should be inverted
   *
   * @param option Name of the option
   * @param value  The value of the option
   */
  private void dumpOption(String option, Object value) {
    String label = Localization.Main.getText("config." + option + ".label");
    label = Helper.pad(label, ' ', 50) + " : ";
    // For yes/no boolean values negate displayed value if name starts
    // with 'Do not' (or its localized equivalent)
    if (label.startsWith(doNot)) {
      if (value.toString().equalsIgnoreCase(yes))
        value = no;
      else
        value = yes;
    }
    logger.info(label + value);
  }

  /**
   * Dump all the configuration options listed as get methods in the
   * configuration interface.
   */
  public void dumpOptions() {
    logger.info("");
    for (Method getter : ReadOnlyStanzaConfigurationInterface.class.getMethods()) {
      String getterName = getter.getName();
      try {
        Object result = getter.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
        if (result instanceof Boolean) {
          result = LocalizationHelper.INSTANCE.getYesOrNo((Boolean) result);
        }
        String optionName = getterName.substring(3);
        dumpOption(optionName, result);
      } catch (IllegalAccessException e) {
        logger.warn("", e);
      } catch (InvocationTargetException e) {
        logger.warn("", e);
      }
    }
    logger.info("");
  }

  public void startCreateMainCatalog() {
    // do nothing
  }

  public void endCreateMainCatalog(String where, long timeInHtml) {
    progressStep.reset();
    if (timeInHtml > 1000)
      logger.info(Localization.Main.getText("info.html.donein", timeInHtml / 1000));

    logger.info(Localization.Main.getText("info.step.done", where));
  }

  public void startReadDatabase() {
    logger.info(Localization.Main.getText("info.step.database"));
  }

  public void endReadDatabase(long milliseconds, String summary) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    logger.info(Localization.Main.getText(summary));
  }

  public void setAuthorCount(String s) {
    return;
  }
  public void setSeriesCount(String s) {
    return;
  }
  public void setTagCount(String s) {
    return;
  }
  public void setFeaturedCount(String s) {
    return;
  }

  public void setCopyLibCount(String s) {
    // do nothing
  }

  public void setCopyCatCount(String s) {
    // do nothing
  }

  public void startCreateTags(long nb) {
    logger.info(Localization.Main.getText("info.step.tags"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateTags(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateTags() {}

  public void startCreateAuthors(long nb) {
    logger.info(Localization.Main.getText("info.step.authors"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateAuthors(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateAuthors() {}

  public void startCreateSeries(long nb) {
    logger.info(Localization.Main.getText("info.step.series"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateSeries(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateSeries() {}

  public void startCreateRecent(long nb) {
    logger.info(Localization.Main.getText("info.step.recent"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateRecent(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateRecent() {}

  public void startCreateRated(long nb) {
    logger.info(Localization.Main.getText("info.step.rated"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateRated(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateRated() {}

  public void startCreateAllbooks(long nb) {
    logger.info(Localization.Main.getText("info.step.allbooks"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateAllbooks(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateAllBooks() {}

  public void startCreateFeaturedBooks(long nb) {
    logger.info(Localization.Main.getText("info.step.featuredbooks"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateFeaturedBooks(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateFeaturedBooks() {}

  public void startCreateCustomCatalogs(long nb) {
    logger.info(Localization.Main.getText("info.step.customcatalogs"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateCustomCatalogs(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateCustomCatalogs() {}

  public void startReprocessingEpubMetadata(long nb) {
    logger.info(Localization.Main.getText("info.step.reprocessingEpubMetadata"));
    progressStep.setMaxScale(nb);
  }

  public void endReprocessingEpubMetadata(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableReprocessingEpubMetadata() {}

  public void startCreateJavascriptDatabase(long nb) {
    logger.info(Localization.Main.getText("info.step.index"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateJavascriptDatabase(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableCreateJavascriptDatabase() {}

  public void startCopyLibToTarget(long nb) {
    logger.info(Localization.Main.getText("info.step.copylib"));
    progressStep.setMaxScale(nb);
  }

  public void startCopyCatToTarget(long nb) {
    logger.info(Localization.Main.getText("info.step.copycat"));
    progressStep.setMaxScale(nb);
  }

  public void disableCopyLibToTarget() {}

  public void startZipCatalog(long nb) {
    logger.info(Localization.Main.getText("info.step.zipCatalog"));
    progressStep.setMaxScale(nb);
  }

  public void endZipCatalog(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void disableZipCatalog() {}

  public void incStepProgressIndicatorPosition() {
    if (startGui) {
      checkIfContinueGenerating();
      progressStep.incPosition();
    }
  }

  public void endCopyLibToTarget(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void endCopyCatToTarget(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void errorOccured(String message, Throwable error) {
    logger.error(message, error);
  }

  public int askUser(String message, String... possibleAnswers) {
    System.out.println(message);
    int num = 1;
    for (String possibleAnswer : possibleAnswers) {
      String s = "" + num++ + ". " + possibleAnswer;
      System.out.println(s);
    }
    InputStreamReader converter = new InputStreamReader(System.in);
    BufferedReader in = new BufferedReader(converter);
    int nAnswer = 0;
    try {
      String answer = in.readLine();
      nAnswer = Integer.parseInt(answer);
    } catch (IOException e) {
      // do nothing
    }
    if (nAnswer > 0) {
      String logMessage = message + " (answered " + possibleAnswers[nAnswer - 1] + ")";
      logger.info(logMessage);
    }
    return nAnswer - 1;
  }

  public void showMessage(String message) {
    progressStep.actOnMessage(message);
  }

  public void showRamUsage (String message) {
    logger.debug("Ram Usage: " + message);
  }

  public void checkIfContinueGenerating() throws GenerationStoppedException {
    if (!continueGenerating)
      throw new GenerationStoppedException();
  }

  private int warnCount;
  public void resetWarnCount() {
    warnCount = 0;
  }
  public int getWarnCount() {
    return warnCount;
  }
  public void incrementWarnCount() {
    warnCount++;
    return;
  }

  public void setStopGenerating () {

  };

  public void setStartGui (boolean startGui) {
    this.startGui = startGui;
  }
}
