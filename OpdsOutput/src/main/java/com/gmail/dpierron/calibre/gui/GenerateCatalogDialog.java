/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GenerateCatalogDialog.java
 *
 * Created on 17 f�vr. 2010, 14:01:28
 */

package com.gmail.dpierron.calibre.gui;

import com.gmail.dpierron.calibre.opds.*;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * @author David
 */
public class GenerateCatalogDialog extends javax.swing.JDialog implements CatalogCallbackInterface {
  private final static Logger logger = Logger.getLogger(Catalog.class);
  private Log4jCatalogCallback delegate = new Log4jCatalogCallback();
  protected boolean continueGenerating = true;

  // step progress indicator
  ProgressIndicator progressStep = new ProgressIndicator() {
    @Override
    public void reset() {
      super.reset();
      if (jProgressStep != null)
        jProgressStep.setValue(0);
    }

    @Override
    public void actOnPositionChange(int newPos) {
      if (jProgressStep != null)
        jProgressStep.setValue(newPos);
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {

      }
    }

    @Override
    public void actOnMessage(String message) {
      super.actOnMessage(message);
      if (lblStepMessage != null) {
        lblStepMessage.setText(message);
      }

    }
    @Override
    public ProgressIndicator setMaxVisible(long maxVisible) {
      super.setMaxVisible(maxVisible);
      if (jProgressStep != null)
        jProgressStep.setMaximum((int) maxVisible);
      return this;
    }
  };


  /**
   * Initialise the progress indicator
   *
   * @param max
   */
  public void setProgressMax(long max) {
    progressStep.setMaxScale(max);
  }

  public void incStepProgressIndicatorPosition() {
    checkIfContinueGenerating();
    progressStep.incPosition();
  }

  /**
   * Creates new form GenerateCatalogDialog
   */
  public GenerateCatalogDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    setLocationRelativeTo(parent);
    initComponents();
    progressStep.setMaxVisible(500);
    translateTexts();
  }

  @Override
  public void pack() {
    super.pack();
    Rectangle oldbounds = getBounds();
    oldbounds.width += 10;
    oldbounds.height += 10;
    setBounds(oldbounds);
  }

  private void translateTexts() {
    setTitle(Localization.Main.getText("gui.generateProgress"));
    lblRamUsage.setText("");
    lblStoppingGeneration.setVisible(false);
    lblStoppingGeneration.repaint();
    lblStoppingGeneration.setText(Localization.Main.getText("gui.stoppingGeneration")); // NOI18N
    lblStarted.setText(Localization.Main.getText("info.step.started")); // NOI18N
    lblDatabase.setText(Localization.Main.getText("info.step.database")); // NOI18N
    lblTags.setText(Localization.Main.getText("info.step.tags")); // NOI18N
    lblAuthors.setText(Localization.Main.getText("info.step.authors")); // NOI18N
    lblSeries.setText(Localization.Main.getText("info.step.series")); // NOI18N
    lblRecent.setText(Localization.Main.getText("info.step.recent")); // NOI18N
    lblAllbooks.setText(Localization.Main.getText("info.step.allbooks")); // NOI18N
    lblRated.setText(Localization.Main.getText("info.step.rated")); // NOI18N
    lblFeaturedBooks.setText(Localization.Main.getText("info.step.featuredbooks")); // NOI18N
    lblCustomCatalogs.setText(Localization.Main.getText("info.step.customcatalogs")); // NOI18N
    lblIndex.setText(Localization.Main.getText("info.step.index")); // NOI18N
    lblReprocessingEpubMetadata.setText(Localization.Main.getText("info.step.reprocessingEpubMetadata")); // NOI18N
    lblCopyLibToTarget.setText(Localization.Main.getText("info.step.copylib")); // NOI18N
    lblCopyCatToTarget.setText(Localization.Main.getText("info.step.copycat")); // NOI18N
    lblZipCatalog.setText(Localization.Main.getText("info.step.zipCatalog")); // NOI18N
    lblFinished.setText(Localization.Main.getText("info.step.done.gui")); // NOI18N
    cmdStopGenerating.setText(Localization.Main.getText("gui.stopGeneration")); // NOI18N
  }

  public void dumpOptions() {
    delegate.dumpOptions();
  }

  private void boldFont(JLabel label, boolean bold) {
    label.setFont(lblStarted.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN));
  }

  private void setTimeNow(JLabel label) {
    label.setText(String.format("%tT", System.currentTimeMillis()));
  }

  public void startCreateMainCatalog() {
    logger.info(Localization.Main.getText("info.step.started"));
    cmdStopGenerating.setVisible(true);
    cmdStopGenerating.repaint();
    lblStoppingGeneration.setVisible(false);
    lblStoppingGeneration.repaint();
    progressStep.reset();
    lblStartedTime.setText("");
    lblDatabaseTime.setText("");
    lblAuthorsTime.setText("");
    lblTagsTime.setText("");
    lblSeriesTime.setText("");
    lblRecentTime.setText("");
    lblRatingTime.setText("");
    lblAllbooksTime.setText("");
    lblFeaturedBooksTime.setText("");
    lblCustomCatalogsTime.setText("");
    lblReprocessingEpubMetadataTime.setText("");
    lblIndexTime.setText("");
    lblCopyLibraryTime.setText("");
    lblCopyCatalogTime.setText("");
    lblZipCatalogTime.setText("");
    lblFinishedTime.setText("");
    boldFont(lblStarted, true);
  }

  private void startStage(long nb, JLabel label, String localizationKey) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText(localizationKey, label));
    boldFont(label, true);
  }

  private void endStage(long milliseconds, JLabel label, JLabel time, JCheckBox ckkBox) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    ckkBox.setSelected(true);
    boldFont(label, false);
    setTimeNow(time);
  }

  public void startReadDatabase() {
    logger.info(Localization.Main.getText("info.step.database"));
    chkStarted.setSelected(true);
    setTimeNow(lblStartedTime);
    boldFont(lblStarted, false);
    boldFont(lblDatabase, true);
  }

  public void endReadDatabase(long milliseconds, String summary) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkDatabase.setSelected(true);
    boldFont(lblDatabase, false);
    lblDatabase.setText(lblDatabase.getText() + " (" + summary + ")");
    setTimeNow(lblDatabaseTime);
  }

  public void setAuthorCount(String summary){
    lblAuthors.setText(lblAuthors.getText() + " (" + summary + ")");
  }

  public void setSeriesCount(String summary) {
    lblSeries.setText(lblSeries.getText() + " (" + summary + ")");
  }

  public void setTagCount(String summary) {
    lblTags.setText(lblTags.getText() + " (" + summary + ")");
  }

  public void setFeaturedCount(String summary) {
    lblFeaturedBooks.setText(lblFeaturedBooks.getText() + " (" + summary + ")");
  }

  public void startCreateTags(long nb) {
    startStage(nb, lblTags, "info.step.tags");
  }

  public void endCreateTags(long milliseconds) {
    endStage(milliseconds, lblTags, lblTagsTime, chkTags);
  }

  public void disableCreateTags() {
    lblTags.setEnabled(false);
  }

  public void startCreateAuthors(long nb) {
    startStage(nb, lblAllbooks, "info.step.authors");
  }

  public void endCreateAuthors(long milliseconds) {
    endStage(milliseconds, lblAuthors, lblAuthorsTime,chkAuthors);
  }

  public void disableCreateAuthors() {
    lblAuthors.setEnabled(false);
  }

  public void startCreateSeries(long nb) {
    startStage(nb, lblSeries, "info.step.series");
  }

  public void endCreateSeries(long milliseconds) {
    endStage(milliseconds, lblSeries, lblSeriesTime, chkSeries);
  }

  public void disableCreateSeries() {
    lblSeries.setEnabled(false);
  }

  public void startCreateRecent(long nb) {
    startStage(nb, lblRecent,"info.step.recent");
  }

  public void endCreateRecent(long milliseconds) {
    endStage(milliseconds, lblRecent, lblRecentTime, chkRecent);
  }

  public void disableCreateRecent() {
    lblRecent.setEnabled(false);
  }

  public void startCreateRated(long nb) {
    startStage(nb, lblRated, "info.step.rated");
  }

  public void endCreateRated(long milliseconds) {
    endStage(milliseconds, lblRated, lblRatingTime, chkRated);
  }

  public void disableCreateRated() {
    lblRated.setEnabled(false);
  }

  public void startCreateAllbooks(long nb) {
    startStage(nb, lblAllbooks, "info.step.allbooks");
  }

  public void endCreateAllbooks(long milliseconds) {
    endStage(milliseconds, lblAllbooks, lblAllbooksTime, chkAllbooks);
  }

  public void disableCreateAllBooks() {
    lblAllbooks.setEnabled(false);
  }

  public void startCreateFeaturedBooks(long nb) {
    startStage(nb, lblFeaturedBooks, "info.step.featuredbooks");
  }

  public void endCreateFeaturedBooks(long milliseconds) {
    endStage(milliseconds, lblFeaturedBooks, lblFeaturedBooksTime, chkFeaturedBooks);
  }

  public void disableCreateFeaturedBooks() {
    lblFeaturedBooks.setEnabled(false);
  }

  public void startCreateCustomCatalogs(long nb) {
    startStage(nb, lblCustomCatalogs, "info.step.customcatalogs");
  }

  public void endCreateCustomCatalogs(long milliseconds) {
    endStage(milliseconds, lblCustomCatalogs, lblCustomCatalogsTime, chkCustomCatalogs);
  }

  public void disableCreateCustomCatalogs() {
    lblCustomCatalogs.setEnabled(false);
  }

  public void startReprocessingEpubMetadata(long nb) {
    startStage(nb, lblReprocessingEpubMetadata, "info.step.reprocessingEpubMetadata");
    lblReprocessingEpubMetadataTime.setText(String.format("%tT", System.currentTimeMillis()));
  }

  public void endReprocessingEpubMetadata(long milliseconds) {
    endStage(milliseconds, lblReprocessingEpubMetadata, lblReprocessingEpubMetadataTime, chkReprocessingEpubMetadata);
  }

  public void disableReprocessingEpubMetadata () {
    lblReprocessingEpubMetadata.setEnabled(false);
  }

  public void endCreateJavascriptDatabase(long milliseconds) {
    endStage(milliseconds, lblIndex, lblIndexTime, chkIndex);
  }

  public void startCreateJavascriptDatabase(long nb) {
    startStage(nb, lblIndex, "info.step.index");
    lblIndexTime.setText(String.format("%tT", System.currentTimeMillis()));
  }

  public void disableCreateJavascriptDatabase() {
    lblIndex.setEnabled(false);
  }

  public void setCopyLibCount(String summary){
    lblCopyLibToTarget.setText(lblCopyLibToTarget.getText() + " (" + summary + ")");
  }

  public void startCopyLibToTarget(long nb) {
    startStage(nb, lblCopyLibToTarget, "info.step.copylib");
  }

  public void endCopyLibToTarget(long milliseconds) {
    endStage(milliseconds, lblCopyLibToTarget, lblCopyLibraryTime, chkCopyLibToTarget);
  }

  public void disableCopyLibToTarget() {
    lblCopyLibToTarget.setEnabled(false);
  }

  public void setCopyCatCount(String summary){
    lblCopyCatToTarget.setText(lblCopyCatalogTime.getText() + " (" + summary + ")");
  }

  public void startCopyCatToTarget(long nb) {
    startStage(nb, lblCopyCatToTarget, "info.step.copycat");
  }

  public void endCopyCatToTarget(long milliseconds) {
    endStage(milliseconds, lblCopyCatToTarget, lblCopyCatalogTime, chkCopyCatToTarget);
  }

  public void startZipCatalog(long nb) {
    startStage(nb,lblZipCatalog,"info.step.zipCatalog");
  }

  public void endZipCatalog(long milliseconds) {
    endStage(milliseconds, lblZipCatalog, lblZipCatalogTime, chkZipCatalog);
  }

  public void disableZipCatalog() {
    lblZipCatalog.setEnabled(false);
  }

  public void endCreateMainCatalog(String where, long timeInHtml) {
    endStage(timeInHtml, lblFinished, lblFinishedTime, chkFinished);
    if (where != null) {
      String message = Localization.Main.getText("info.step.done", where);
      logger.info(message);
      JOptionPane.showMessageDialog(this, message);
    }
    if (getWarnCount() != 0) {
      String message = Localization.Main.getText("info.completedWithWarnings", getWarnCount()) ;
      logger.info(message);
      JOptionPane.showMessageDialog(this, message);
    }
  }

  public void showMessage(String message) {
    progressStep.actOnMessage(message);
  }

  public void showRamUsage(String message) {
    lblRamUsage.setText(message);
  }

  public void errorOccured(String message, Throwable error) {
    String msg;
    String title;
    Boolean b = cmdStopGenerating.isVisible();
    cmdStopGenerating.setVisible(false);
    cmdStopGenerating.repaint();
    if (error != null) {
      title = message;
      if (Helper.isNullOrEmpty(title))
        title = error.getClass().getName();
      msg = error.getMessage() + "\n" + error.getClass() + "\n" + Helper.getStackTrace(error);
    } else {
      msg = message;
      title = "";
    }
    lblStoppingGeneration.setVisible(false);
    JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    cmdStopGenerating.setVisible(b);
    cmdStopGenerating.repaint();
    logger.error(message, error);
  }

  public int askUser(String message, String... possibleAnswers) {
    Boolean b = cmdStopGenerating.isVisible();
    cmdStopGenerating.setVisible(false);
    cmdStopGenerating.repaint();
    int nAnswer = JOptionPane
        .showOptionDialog(this, message, message, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, possibleAnswers, possibleAnswers[0]);
    if (nAnswer > -1) {
      String logMessage = message + " (answered " + possibleAnswers[nAnswer] + ")";
      logger.info(logMessage);
    }
    cmdStopGenerating.setVisible(b);
    cmdStopGenerating.repaint();
    return nAnswer;
  }

  private void setCmdStopGeneratingVisbile(boolean state)  {
    cmdStopGenerating.setVisible(state);
    cmdStopGenerating.repaint();
  }
  public void setStopGenerating () {
    setCmdStopGeneratingVisbile(false);
    lblStoppingGeneration.repaint();
    continueGenerating = false;
  }
  private void actionStopGenerating() {
    setCmdStopGeneratingVisbile(false);
    int n = JOptionPane.showConfirmDialog(this, Localization.Main.getText("gui.stopGeneration.confirm"), "", JOptionPane.OK_CANCEL_OPTION);
    if (JOptionPane.OK_OPTION == n) {
      lblStoppingGeneration.setText(Localization.Main.getText("gui.prepareStopGeneration"));
      lblStoppingGeneration.setVisible(true);
      setStopGenerating();
    } else {
      setCmdStopGeneratingVisbile(true);
    }
  }

  public void checkIfContinueGenerating() throws GenerationStoppedException {
    if (!continueGenerating) {
      lblStoppingGeneration.setText(Localization.Main.getText("gui.stoppingGeneration"));
      throw new GenerationStoppedException();
    }
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
  /**
   * This method is called from within the constructor to reset the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jProgressStep = new javax.swing.JProgressBar();
        cmdStopGenerating = new javax.swing.JButton();
        chkStarted = new javax.swing.JCheckBox();
        lblStarted = new javax.swing.JLabel();
        lblStartedTime = new javax.swing.JLabel();
        chkDatabase = new javax.swing.JCheckBox();
        lblDatabase = new javax.swing.JLabel();
        lblDatabaseTime = new javax.swing.JLabel();
        chkAuthors = new javax.swing.JCheckBox();
        lblAuthors = new javax.swing.JLabel();
        lblAuthorsTime = new javax.swing.JLabel();
        chkTags = new javax.swing.JCheckBox();
        lblTags = new javax.swing.JLabel();
        lblTagsTime = new javax.swing.JLabel();
        chkSeries = new javax.swing.JCheckBox();
        lblSeries = new javax.swing.JLabel();
        lblSeriesTime = new javax.swing.JLabel();
        chkRecent = new javax.swing.JCheckBox();
        lblRecent = new javax.swing.JLabel();
        lblRecentTime = new javax.swing.JLabel();
        chkRated = new javax.swing.JCheckBox();
        lblRated = new javax.swing.JLabel();
        lblRatingTime = new javax.swing.JLabel();
        chkAllbooks = new javax.swing.JCheckBox();
        lblAllbooks = new javax.swing.JLabel();
        lblAllbooksTime = new javax.swing.JLabel();
        chkFeaturedBooks = new javax.swing.JCheckBox();
        lblFeaturedBooks = new javax.swing.JLabel();
        lblFeaturedBooksTime = new javax.swing.JLabel();
        chkCustomCatalogs = new javax.swing.JCheckBox();
        lblCustomCatalogs = new javax.swing.JLabel();
        lblCustomCatalogsTime = new javax.swing.JLabel();
        chkReprocessingEpubMetadata = new javax.swing.JCheckBox();
        lblReprocessingEpubMetadata = new javax.swing.JLabel();
        lblReprocessingEpubMetadataTime = new javax.swing.JLabel();
        chkIndex = new javax.swing.JCheckBox();
        lblIndex = new javax.swing.JLabel();
        lblIndexTime = new javax.swing.JLabel();
        chkCopyLibToTarget = new javax.swing.JCheckBox();
        lblCopyLibToTarget = new javax.swing.JLabel();
        lblCopyLibraryTime = new javax.swing.JLabel();
        chkCopyCatToTarget = new javax.swing.JCheckBox();
        lblCopyCatToTarget = new javax.swing.JLabel();
        lblCopyCatalogTime = new javax.swing.JLabel();
        chkFinished = new javax.swing.JCheckBox();
        lblFinished = new javax.swing.JLabel();
        lblFinishedTime = new javax.swing.JLabel();
        lblStepMessage = new javax.swing.JLabel();
        lblStoppingGeneration = new javax.swing.JLabel();
        lblZipCatalog = new javax.swing.JLabel();
        lblZipCatalogTime = new javax.swing.JLabel();
        chkZipCatalog = new javax.swing.JCheckBox();
        lblRamUsage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jProgressStep.setMaximum(62);
        jProgressStep.setPreferredSize(new java.awt.Dimension(300, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 15, 5);
        getContentPane().add(jProgressStep, gridBagConstraints);

        cmdStopGenerating.setText("cmdStopGenerating");
        cmdStopGenerating.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cmdStopGeneratingMouseClicked(evt);
            }
        });
        cmdStopGenerating.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdStopGeneratingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(cmdStopGenerating, gridBagConstraints);

        chkStarted.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkStarted, gridBagConstraints);

        lblStarted.setText("lblStarted");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblStarted, gridBagConstraints);

        lblStartedTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblStartedTime, gridBagConstraints);

        chkDatabase.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkDatabase, gridBagConstraints);

        lblDatabase.setText("lblDatabase");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblDatabase, gridBagConstraints);

        lblDatabaseTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblDatabaseTime, gridBagConstraints);

        chkAuthors.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkAuthors, gridBagConstraints);

        lblAuthors.setText("lblAuthors");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblAuthors, gridBagConstraints);

        lblAuthorsTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblAuthorsTime, gridBagConstraints);

        chkTags.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkTags, gridBagConstraints);

        lblTags.setText("lblTags");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblTags, gridBagConstraints);

        lblTagsTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblTagsTime, gridBagConstraints);

        chkSeries.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkSeries, gridBagConstraints);

        lblSeries.setText("lblSeries");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblSeries, gridBagConstraints);

        lblSeriesTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblSeriesTime, gridBagConstraints);

        chkRecent.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkRecent, gridBagConstraints);

        lblRecent.setText("lblRecent");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblRecent, gridBagConstraints);

        lblRecentTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblRecentTime, gridBagConstraints);

        chkRated.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkRated, gridBagConstraints);

        lblRated.setText("lblRated");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblRated, gridBagConstraints);

        lblRatingTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblRatingTime, gridBagConstraints);

        chkAllbooks.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkAllbooks, gridBagConstraints);

        lblAllbooks.setText("lblAllbooks");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblAllbooks, gridBagConstraints);

        lblAllbooksTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblAllbooksTime, gridBagConstraints);

        chkFeaturedBooks.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkFeaturedBooks, gridBagConstraints);

        lblFeaturedBooks.setText("lblFeaturedBooks");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblFeaturedBooks, gridBagConstraints);

        lblFeaturedBooksTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblFeaturedBooksTime, gridBagConstraints);

        chkCustomCatalogs.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkCustomCatalogs, gridBagConstraints);

        lblCustomCatalogs.setText("lblCustomCatalogs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblCustomCatalogs, gridBagConstraints);

        lblCustomCatalogsTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblCustomCatalogsTime, gridBagConstraints);

        chkReprocessingEpubMetadata.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkReprocessingEpubMetadata, gridBagConstraints);

        lblReprocessingEpubMetadata.setText("lblReprocessingEpubMetadata");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblReprocessingEpubMetadata, gridBagConstraints);

        lblReprocessingEpubMetadataTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblReprocessingEpubMetadataTime, gridBagConstraints);

        chkIndex.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkIndex, gridBagConstraints);

        lblIndex.setText("lblIndex");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblIndex, gridBagConstraints);

        lblIndexTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblIndexTime, gridBagConstraints);

        chkCopyLibToTarget.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkCopyLibToTarget, gridBagConstraints);

        lblCopyLibToTarget.setText("lblCopyLibToTarget");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblCopyLibToTarget, gridBagConstraints);

        lblCopyLibraryTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblCopyLibraryTime, gridBagConstraints);

        chkCopyCatToTarget.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkCopyCatToTarget, gridBagConstraints);

        lblCopyCatToTarget.setText("lblCopyCatToTarget");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblCopyCatToTarget, gridBagConstraints);

        lblCopyCatalogTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblCopyCatalogTime, gridBagConstraints);

        chkFinished.setEnabled(false);
        chkFinished.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFinishedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkFinished, gridBagConstraints);

        lblFinished.setText("lblFinished");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblFinished, gridBagConstraints);
        lblFinished.getAccessibleContext().setAccessibleName(null);

        lblFinishedTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblFinishedTime, gridBagConstraints);

        lblStepMessage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblStepMessage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblStepMessage.setMaximumSize(new java.awt.Dimension(600, 20));
        lblStepMessage.setMinimumSize(new java.awt.Dimension(600, 0));
        lblStepMessage.setPreferredSize(new java.awt.Dimension(600, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
        getContentPane().add(lblStepMessage, gridBagConstraints);

        lblStoppingGeneration.setText("lblStoppingGeneration");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblStoppingGeneration, gridBagConstraints);

        lblZipCatalog.setText("lblZipCatalog");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblZipCatalog, gridBagConstraints);

        lblZipCatalogTime.setText("!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(lblZipCatalogTime, gridBagConstraints);

        chkZipCatalog.setEnabled(false);
        chkZipCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkZipCatalogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(chkZipCatalog, gridBagConstraints);

        lblRamUsage.setText("lblRamUsage");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(lblRamUsage, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void cmdStopGeneratingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdStopGeneratingActionPerformed
    actionStopGenerating();
  }//GEN-LAST:event_cmdStopGeneratingActionPerformed

  private void cmdStopGeneratingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmdStopGeneratingMouseClicked
  }//GEN-LAST:event_cmdStopGeneratingMouseClicked

    private void chkFinishedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkFinishedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkFinishedActionPerformed

    private void chkZipCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkZipCatalogActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkZipCatalogActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkAllbooks;
    private javax.swing.JCheckBox chkAuthors;
    private javax.swing.JCheckBox chkCopyCatToTarget;
    private javax.swing.JCheckBox chkCopyLibToTarget;
    private javax.swing.JCheckBox chkCustomCatalogs;
    private javax.swing.JCheckBox chkDatabase;
    private javax.swing.JCheckBox chkFeaturedBooks;
    private javax.swing.JCheckBox chkFinished;
    private javax.swing.JCheckBox chkIndex;
    private javax.swing.JCheckBox chkRated;
    private javax.swing.JCheckBox chkRecent;
    private javax.swing.JCheckBox chkReprocessingEpubMetadata;
    private javax.swing.JCheckBox chkSeries;
    private javax.swing.JCheckBox chkStarted;
    private javax.swing.JCheckBox chkTags;
    private javax.swing.JCheckBox chkZipCatalog;
    private javax.swing.JButton cmdStopGenerating;
    private javax.swing.JProgressBar jProgressStep;
    private javax.swing.JLabel lblAllbooks;
    private javax.swing.JLabel lblAllbooksTime;
    private javax.swing.JLabel lblAuthors;
    private javax.swing.JLabel lblAuthorsTime;
    private javax.swing.JLabel lblCopyCatToTarget;
    private javax.swing.JLabel lblCopyCatalogTime;
    private javax.swing.JLabel lblCopyLibToTarget;
    private javax.swing.JLabel lblCopyLibraryTime;
    private javax.swing.JLabel lblCustomCatalogs;
    private javax.swing.JLabel lblCustomCatalogsTime;
    private javax.swing.JLabel lblDatabase;
    private javax.swing.JLabel lblDatabaseTime;
    private javax.swing.JLabel lblFeaturedBooks;
    private javax.swing.JLabel lblFeaturedBooksTime;
    private javax.swing.JLabel lblFinished;
    private javax.swing.JLabel lblFinishedTime;
    private javax.swing.JLabel lblIndex;
    private javax.swing.JLabel lblIndexTime;
    private javax.swing.JLabel lblRamUsage;
    private javax.swing.JLabel lblRated;
    private javax.swing.JLabel lblRatingTime;
    private javax.swing.JLabel lblRecent;
    private javax.swing.JLabel lblRecentTime;
    private javax.swing.JLabel lblReprocessingEpubMetadata;
    private javax.swing.JLabel lblReprocessingEpubMetadataTime;
    private javax.swing.JLabel lblSeries;
    private javax.swing.JLabel lblSeriesTime;
    private javax.swing.JLabel lblStarted;
    private javax.swing.JLabel lblStartedTime;
    private javax.swing.JLabel lblStepMessage;
    private javax.swing.JLabel lblStoppingGeneration;
    private javax.swing.JLabel lblTags;
    private javax.swing.JLabel lblTagsTime;
    private javax.swing.JLabel lblZipCatalog;
    private javax.swing.JLabel lblZipCatalogTime;
    // End of variables declaration//GEN-END:variables

}
