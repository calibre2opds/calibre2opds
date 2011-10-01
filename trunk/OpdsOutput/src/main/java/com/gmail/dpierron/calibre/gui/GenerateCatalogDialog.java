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

import com.gmail.dpierron.calibre.opds.Catalog;
import com.gmail.dpierron.calibre.opds.CatalogCallbackInterface;
import com.gmail.dpierron.calibre.opds.Log4jCatalogCallback;
import com.gmail.dpierron.calibre.opds.ProgressIndicator;
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
    lblStarted.setText(Localization.Main.getText("info.step.started")); // NOI18N
    lblDatabase.setText(Localization.Main.getText("info.step.database")); // NOI18N
    lblTags.setText(Localization.Main.getText("info.step.tags")); // NOI18N
    lblAuthors.setText(Localization.Main.getText("info.step.authors")); // NOI18N
    lblSeries.setText(Localization.Main.getText("info.step.series")); // NOI18N
    lblRecent.setText(Localization.Main.getText("info.step.recent")); // NOI18N
    lblAllbooks.setText(Localization.Main.getText("info.step.allbooks")); // NOI18N
    lblThmbnails.setText(Localization.Main.getText("info.step.thumbnails")); // NOI18N
    lblCovers.setText(Localization.Main.getText("info.step.covers")); // NOI18N
    lblCopyLibToTarget.setText(Localization.Main.getText("info.step.copylib")); // NOI18N
    lblCopyCatToTarget.setText(Localization.Main.getText("info.step.copycat")); // NOI18N
    lblFinished.setText(Localization.Main.getText("info.step.done.gui")); // NOI18N
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
    progressStep.reset();
    lblDatabaseTime.setText("");
    lblAuthorsTime.setText("");
    lblTagsTime.setText("");
    lblSeriesTime.setText("");
    lblRecentTime.setText("");
    lblRatingTime.setText("");
    lblAllbooksTime.setText("");
    lblThumbnailsTime.setText("");
    lblCoversTime.setText("");
    lblReprocessingEpubMetadataTime.setText("");
    lblIndexTime.setText("");
    lblCopyLibraryTime.setText("");
    lblCopyCatalogTime.setText("");
    lblFinishedTime.setText("");
  }

  public void startReadDatabase() {
    logger.info(Localization.Main.getText("info.step.database"));
    chkStarted.setSelected(true);
    setTimeNow(lblStartedTime);
    boldFont(lblDatabase, true);
  }

  public void endReadDatabase(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkDatabase.setSelected(true);
    boldFont(lblDatabase, false);
    setTimeNow(lblDatabaseTime);
  }

  public void startCreateTags(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.tags"));
    boldFont(lblTags, true);
  }

  public void endCreateTags(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkTags.setSelected(true);
    boldFont(lblTags, false);
    setTimeNow(lblTagsTime);
  }

  public void startCreateAuthors(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.authors"));
    boldFont(lblAuthors, true);
  }

  public void endCreateAuthors(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkAuthors.setSelected(true);
    boldFont(lblAuthors, false);
    setTimeNow(lblAuthorsTime);
  }

  public void startCreateSeries(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.series"));
    boldFont(lblSeries, true);
  }

  public void endCreateSeries(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkSeries.setSelected(true);
    boldFont(lblSeries, false);
    setTimeNow(lblSeriesTime);
  }

  public void startCreateRecent(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.recent"));
    boldFont(lblRecent, true);
  }

  public void endCreateRecent(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkRecent.setSelected(true);
    boldFont(lblRecent, false);
    setTimeNow(lblRecentTime);
  }

  public void startCreateRated(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.rated"));
    boldFont(lblRated, true);
  }

  public void endCreateRated(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkRated.setSelected(true);
    boldFont(lblRated, false);
    setTimeNow(lblRatingTime);
  }

  public void startCreateAllbooks(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.allbooks"));
    boldFont(lblAllbooks, true);
  }

  public void endCreateAllbooks(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkAllbooks.setSelected(true);
    boldFont(lblAllbooks, false);
    setTimeNow(lblAllbooksTime);
  }

  public void startCreateFeaturedBooks(long nb) {
    logger.info(Localization.Main.getText("info.step.featuredbooks"));
    progressStep.setMaxScale(nb);
  }

  public void endCreateFeaturedBooks(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
  }

  public void startCreateThumbnails(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.thumbnails"));
    boldFont(lblThmbnails, true);
  }

  public void endCreateThumbnails(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkThumbnails.setSelected(true);
    boldFont(lblThmbnails, false);
    setTimeNow(lblThumbnailsTime);
  }

  public void startCreateCovers(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.covers"));
    boldFont(lblCovers, true);
  }

  public void endCreateCovers(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkCovers.setSelected(true);
    boldFont(lblCovers, false);
    setTimeNow(lblCoversTime);
  }

  public void startReprocessingEpubMetadata(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.reprocessingEpubMetadata"));
    boldFont(lblReprocessingEpubMetadata, true);
    lblReprocessingEpubMetadataTime.setText(String.format("%tT", System.currentTimeMillis()));
  }

  public void endReprocessingEpubMetadata(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkReprocessingEpubMetadata.setSelected(true);
    boldFont(lblReprocessingEpubMetadata, false);
    setTimeNow(lblReprocessingEpubMetadataTime);
  }

  public void endCreateJavascriptDatabase(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkIndex.setSelected(true);
    boldFont(lblIndex, false);
    setTimeNow(lblIndexTime);
  }

  public void startCreateJavascriptDatabase(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.index"));
    boldFont(lblIndex, true);
    lblIndexTime.setText(String.format("%tT", System.currentTimeMillis()));
  }

  public void startCopyLibToTarget(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.copylib"));
    boldFont(lblCopyLibToTarget, true);
  }

  public void endCopyLibToTarget(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkCopyLibToTarget.setSelected(true);
    boldFont(lblCopyLibToTarget, false);
    setTimeNow(lblCopyLibraryTime);
  }

  public void startCopyCatToTarget(long nb) {
    progressStep.setMaxScale(nb);
    logger.info(Localization.Main.getText("info.step.copycat"));
    boldFont(lblCopyCatToTarget, true);
  }

  public void endCopyCatToTarget(long milliseconds) {
    logger.info(Localization.Main.getText("info.step.donein", milliseconds));
    chkCopyCatToTarget.setSelected(true);
    boldFont(lblCopyCatToTarget, false);
    setTimeNow(lblCopyCatalogTime);
  }

  public void endCreateMainCatalog(String where, long timeInHtml) {
    progressStep.reset();
    chkFinished.setSelected(true);
    setTimeNow(lblFinishedTime);
    String message = Localization.Main.getText("info.step.done", where);
    logger.info(message);
    int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION)
      System.exit(0);
  }

  public void incStepProgressIndicatorPosition() {
    progressStep.incPosition();
  }

  public void showMessage(String message) {
    progressStep.actOnMessage(message);
  }

  public void errorOccured(String message, Throwable error) {
    String msg;
    String title;
    if (error != null) {
      title = message;
      if (Helper.isNullOrEmpty(title))
        title = error.getClass().getName();
      msg = Helper.getStackTrace(error);
    } else {
      msg = message;
      title = "";
    }
    JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    logger.error(message, error);
  }

  /**
   * This method is called from within the constructor to initialize the form.
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
    chkThumbnails = new javax.swing.JCheckBox();
    lblThmbnails = new javax.swing.JLabel();
    lblThumbnailsTime = new javax.swing.JLabel();
    chkCovers = new javax.swing.JCheckBox();
    lblCovers = new javax.swing.JLabel();
    lblCoversTime = new javax.swing.JLabel();
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

    chkStarted.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkStarted, gridBagConstraints);

    lblStarted.setText(Localization.Main.getText("info.step.started")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblStarted, gridBagConstraints);

    lblStartedTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblStartedTime, gridBagConstraints);

    chkDatabase.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkDatabase, gridBagConstraints);

    lblDatabase.setText(Localization.Main.getText("info.step.database")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblDatabase, gridBagConstraints);

    lblDatabaseTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblDatabaseTime, gridBagConstraints);

    chkAuthors.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkAuthors, gridBagConstraints);

    lblAuthors.setText(Localization.Main.getText("info.step.authors")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblAuthors, gridBagConstraints);

    lblAuthorsTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblAuthorsTime, gridBagConstraints);

    chkTags.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkTags, gridBagConstraints);

    lblTags.setText(Localization.Main.getText("info.step.tags")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblTags, gridBagConstraints);

    lblTagsTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblTagsTime, gridBagConstraints);

    chkSeries.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkSeries, gridBagConstraints);

    lblSeries.setText(Localization.Main.getText("info.step.series")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblSeries, gridBagConstraints);

    lblSeriesTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblSeriesTime, gridBagConstraints);

    chkRecent.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkRecent, gridBagConstraints);

    lblRecent.setText(Localization.Main.getText("info.step.recent")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblRecent, gridBagConstraints);

    lblRecentTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblRecentTime, gridBagConstraints);

    chkRated.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkRated, gridBagConstraints);

    lblRated.setText(Localization.Main.getText("info.step.rated")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblRated, gridBagConstraints);

    lblRatingTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblRatingTime, gridBagConstraints);

    chkAllbooks.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkAllbooks, gridBagConstraints);

    lblAllbooks.setText(Localization.Main.getText("info.step.allbooks")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblAllbooks, gridBagConstraints);

    lblAllbooksTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblAllbooksTime, gridBagConstraints);

    chkThumbnails.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkThumbnails, gridBagConstraints);

    lblThmbnails.setText(Localization.Main.getText("info.step.thumbnails")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblThmbnails, gridBagConstraints);

    lblThumbnailsTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblThumbnailsTime, gridBagConstraints);

    chkCovers.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkCovers, gridBagConstraints);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Localization_en"); // NOI18N
    lblCovers.setText(bundle.getString("info.step.covers")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblCovers, gridBagConstraints);

    lblCoversTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblCoversTime, gridBagConstraints);

    chkReprocessingEpubMetadata.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkReprocessingEpubMetadata, gridBagConstraints);

    lblReprocessingEpubMetadata.setText(bundle.getString("info.step.reprocessingEpubMetadata")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblReprocessingEpubMetadata, gridBagConstraints);

    lblReprocessingEpubMetadataTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblReprocessingEpubMetadataTime, gridBagConstraints);

    chkIndex.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkIndex, gridBagConstraints);

    lblIndex.setText(bundle.getString("info.step.index")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblIndex, gridBagConstraints);

    lblIndexTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblIndexTime, gridBagConstraints);

    chkCopyLibToTarget.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkCopyLibToTarget, gridBagConstraints);

    lblCopyLibToTarget.setText(Localization.Main.getText("info.step.copylib")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblCopyLibToTarget, gridBagConstraints);

    lblCopyLibraryTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblCopyLibraryTime, gridBagConstraints);

    chkCopyCatToTarget.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkCopyCatToTarget, gridBagConstraints);

    lblCopyCatToTarget.setText(Localization.Main.getText("info.step.copycat")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblCopyCatToTarget, gridBagConstraints);

    lblCopyCatalogTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    getContentPane().add(lblCopyCatalogTime, gridBagConstraints);

    chkFinished.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(chkFinished, gridBagConstraints);

    lblFinished.setText(Localization.Main.getText("info.step.done.gui")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    getContentPane().add(lblFinished, gridBagConstraints);

    lblFinishedTime.setText("!");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 15;
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
    gridBagConstraints.gridy = 16;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(15, 5, 5, 5);
    getContentPane().add(lblStepMessage, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox chkAllbooks;
  private javax.swing.JCheckBox chkAuthors;
  private javax.swing.JCheckBox chkCopyCatToTarget;
  private javax.swing.JCheckBox chkCopyLibToTarget;
  private javax.swing.JCheckBox chkCovers;
  private javax.swing.JCheckBox chkDatabase;
  private javax.swing.JCheckBox chkFinished;
  private javax.swing.JCheckBox chkIndex;
  private javax.swing.JCheckBox chkRated;
  private javax.swing.JCheckBox chkRecent;
  private javax.swing.JCheckBox chkReprocessingEpubMetadata;
  private javax.swing.JCheckBox chkSeries;
  private javax.swing.JCheckBox chkStarted;
  private javax.swing.JCheckBox chkTags;
  private javax.swing.JCheckBox chkThumbnails;
  private javax.swing.JProgressBar jProgressStep;
  private javax.swing.JLabel lblAllbooks;
  private javax.swing.JLabel lblAllbooksTime;
  private javax.swing.JLabel lblAuthors;
  private javax.swing.JLabel lblAuthorsTime;
  private javax.swing.JLabel lblCopyCatToTarget;
  private javax.swing.JLabel lblCopyCatalogTime;
  private javax.swing.JLabel lblCopyLibToTarget;
  private javax.swing.JLabel lblCopyLibraryTime;
  private javax.swing.JLabel lblCovers;
  private javax.swing.JLabel lblCoversTime;
  private javax.swing.JLabel lblDatabase;
  private javax.swing.JLabel lblDatabaseTime;
  private javax.swing.JLabel lblFinished;
  private javax.swing.JLabel lblFinishedTime;
  private javax.swing.JLabel lblIndex;
  private javax.swing.JLabel lblIndexTime;
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
  private javax.swing.JLabel lblTags;
  private javax.swing.JLabel lblTagsTime;
  private javax.swing.JLabel lblThmbnails;
  private javax.swing.JLabel lblThumbnailsTime;
  // End of variables declaration//GEN-END:variables

}
