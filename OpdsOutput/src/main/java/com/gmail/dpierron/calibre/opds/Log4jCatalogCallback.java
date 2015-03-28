package com.gmail.dpierron.calibre.opds;

/*
 *  Calss that is used when in batch mode for logging information on progress
 */
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.CustomCatalogEntry;
import com.gmail.dpierron.calibre.configuration.GetConfigurationInterface;
import com.gmail.dpierron.calibre.gui.CatalogCallbackInterface;
import com.gmail.dpierron.calibre.gui.GenerationStoppedException;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Log4jCatalogCallback implements CatalogCallbackInterface {
  private final static Logger logger = Logger.getLogger(Catalog.class);
  private final static String doNot = Localization.Main.getText("config.negate");
  private final static String yes = Localization.Main.getText("boolean.yes");
  private final static String no = Localization.Main.getText("boolean.no");
  // step progress indicator
  ProgressIndicator progressStep = new ProgressIndicator().setIndicator('*');
  protected boolean continueGenerating = true;
  private boolean startGui = true;
  private long stageStart;

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
    for (Method getter : GetConfigurationInterface.class.getMethods()) {
      String getterName = getter.getName();
      try {
        Object result = getter.invoke(ConfigurationManager.getCurrentProfile());
        if (result instanceof Boolean) {
          result = LocalizationHelper.getYesOrNo((Boolean) result);
        }
        // Check for special case of Custom Catalogs!
        if (result instanceof List) {
           for (int i = 0 ; i < ((List) result).size() ; i++) {
             assert ((List)result).get(i) instanceof CustomCatalogEntry;
             CustomCatalogEntry c = ((List<CustomCatalogEntry>)result).get(i);
             String OptionName =  Helper.pad(Localization.Main.getText("gui.tab6.label") + " [" + (i+1) + "], " + c.getAtTop().toString(), ' ', 50) + " : ";
             logger.info(OptionName + c.getLabel() + " (" + c.getValue().toString() + "), " + c.getAtTop().toString());
           }
        } else {
            String optionName = getterName.substring(3);
            dumpOption(optionName, result);
        }
      } catch (IllegalAccessException e) {
        logger.warn("", e);
      } catch (InvocationTargetException e) {
        logger.warn("", e);
      }
    }
    logger.info("");
  }


  public void setStartGui (boolean startGui) {
    this.startGui = startGui;
  }

  // ---------------------------
  //  CatalogCallBackInterface
  // --------------------------
  long stageStartTime;

  private void startStage(long nb, String localizationKey) {
    stageStartTime = System.currentTimeMillis();
    logger.info(Localization.Main.getText(localizationKey));
    progressStep.setMaxScale(nb);
  }

  private void endStage(String localizationKey) {
    logger.info(Localization.Main.getText("info.step.donein",  System.currentTimeMillis() - stageStartTime));
    stageStartTime = System.currentTimeMillis();
    progressStep.reset();       // Not sure this is necessary!
  }

  private void setCount(String summary)  {
    logger.info(summary);
  }

  public void startInitializeMainCatalog() {
    startStage(0, "info.step.started");
  }

  public void endInitializeMainCatalog() {
    endStage("info.step.started");
  }

  public void startFinalizeMainCatalog() {
    startStage(0 ,"info.step.done");
  }

  public void endFinalizeMainCatalog(String where, long timeInHtml) {
    endStage("info.step.done");
    if (timeInHtml > 1000)
      logger.info(Localization.Main.getText("info.html.donein", timeInHtml / 1000));
    logger.info(Localization.Main.getText("info.step.done", where));
  }

  public void startReadDatabase() {
    startStage(0, "info.step.database");
  }

  public void endReadDatabase() {
    endStage( "info.step.database");
  }

  public void setDatabaseCount(String s) { setCount(s);}
  public void setAuthorCount(String s) {
    setCount(s);
  }
  public void setSeriesCount(String s) {
    setCount(s);
  }
  public void setTagCount(String s) {
    setCount(s);
  }
  public void setFeaturedCount(String s) {
    setCount(s);
  }
  public void setRecentCount(String s) { setCount(s); }
  public void setAllBooksCount(String s) { setCount(s); }

  public void setCopyLibCount(String s) {
    setCount(s);
  }

  public void setCopyCatCount(String s) {
    setCount(s);
  }

  public void startCreateTags(long nb) {
    startStage(nb, "info.step.tags");
  }

  public void endCreateTags() {
    endStage("info.step.tags");
  }

  public void disableCreateTags() {}

  public void startCreateAuthors(long nb) {
    startStage(nb, "info.step.authors");
  }

  public void endCreateAuthors() {
    endStage("info.step.authors");
  }

  public void disableCreateAuthors() {}

  public void startCreateSeries(long nb) {
    startStage(nb, "info.step.series");
  }

  public void endCreateSeries() {
    endStage("info.step.series");
  }

  public void disableCreateSeries() {}

  public void startCreateRecent(long nb) {
    startStage(nb, "info.step.recent");
  }

  public void endCreateRecent() {
    endStage("info.step.recent");
  }

  public void disableCreateRecent() {}

  public void startCreateRated(long nb) {
    startStage(nb, "info.step.rated");
  }

  public void endCreateRated() {
    endStage("info.step.rated");
  }

  public void disableCreateRated() {}

  public void startCreateAllbooks(long nb) {
    startStage(nb, "info.step.allbooks");
  }

  public void endCreateAllbooks() {
    endStage("info.step.allbooks");
  }

  public void disableCreateAllBooks() {}

  public void startCreateFeaturedBooks(long nb) {
    startStage(nb, "info.step.featuredbooks");
  }

  public void endCreateFeaturedBooks() {
    endStage("info.step.featuredbooks");
  }

  public void disableCreateFeaturedBooks() {}

  public void startCreateCustomCatalogs(long nb) {
    startStage(nb, "info.step.customcatalogs");
  }

  public void endCreateCustomCatalogs() {
    endStage("info.step.customcatalogs");
  }

  public void disableCreateCustomCatalogs() {}

  public void startReprocessingEpubMetadata(long nb) {
    startStage(nb, "info.step.reprocessingEpubMetadata");
  }

  public void endReprocessingEpubMetadata() {
    endStage("info.step.reprocessingEpubMetadata");
  }

  public void disableReprocessingEpubMetadata() {}

  public void startCreateJavascriptDatabase(long nb) {
    startStage(nb, "info.step.index");
  }

  public void endCreateJavascriptDatabase() {
    endStage("info.step.index");
  }

  public void disableCreateJavascriptDatabase() {}

  public void startCopyLibToTarget(long nb) {
    startStage(nb, "info.step.copylib");
  }

  public void endCopyLibToTarget() {
    endStage("info.step.copylib");
  }

  public void startCopyCatToTarget(long nb) {
    startStage(nb, "info.step.copycat");
  }

  public void endCopyCatToTarget() {
    endStage("info.step.copycat");
  }

  public void disableCopyLibToTarget() {}

  public void startZipCatalog(long nb) {
    startStage(nb, "info.step.zipCatalog");
  }

  public void endZipCatalog() {
    endStage("info.step.zipCatalog");
  }

  public void disableZipCatalog() {}

  public void setProgressMax (long maxSteps) {  }

  public void incStepProgressIndicatorPosition() {
    if (startGui) {
      checkIfContinueGenerating();
      progressStep.incPosition();
    }
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

  public void setStopGenerating() {};
  public void clearStopGenerating() {};
}
