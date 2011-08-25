package com.gmail.dpierron.calibre.gui;

/**
 * Handle the main GUI within Calibre2opds
 *
 * Note that the GUI form and this associated java class is constructed
 * and maintained using the Netbeans IDE tool for form design.
 */
import com.gmail.dpierron.calibre.configuration.CompatibilityTrick;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.opds.Catalog;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.OS;
import com.l2fprod.common.swing.JDirectoryChooser;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mainframe extends javax.swing.JFrame
{
  Logger logger = Logger.getLogger(Mainframe.class);
  GenerateCatalogDialog catalogDialog;
  String language;

  /** Creates new form Mainframe */
  public Mainframe() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // do nothing
    }
    if (ConfigurationManager.INSTANCE.getCurrentProfile().isObsolete()) {
      ConfigurationManager.INSTANCE.getCurrentProfile().reset();
      String msg = Localization.Main.getText("gui.reset.warning");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
    }
    initComponents();
    loadValues();
    translateTexts();
  }

  private void processEpubMetadataOfAllBooks() {
    // question
    String message = Localization.Main.getText("gui.confirm.tools.processEpubMetadataOfAllBooks");
    String yes = Localization.Main.getText("boolean.yes");
    String cancel = Localization.Main.getText("boolean.no");
    boolean removeCss = false;
    File defaultCss = null;
    String onlyForTag = null;
    int result;
    if (ConfigurationManager.INSTANCE.isHacksEnabled()) {
      String yesAndRemoveCss = Localization.Main.getText("gui.confirm.tools.removeCss");
      result = JOptionPane.showOptionDialog(this, message, "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{yes, yesAndRemoveCss, cancel}, cancel);
      if (result == JOptionPane.CANCEL_OPTION) return;
      removeCss = (result == JOptionPane.NO_OPTION);
      if (removeCss) {
        onlyForTag = JOptionPane.showInputDialog("Only for tag (empty for all)");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
          @Override
          public boolean accept(File f) {
            return (f != null && (!f.isFile() || f.getName().toUpperCase().endsWith(".CSS")));
          }

          @Override
          public String getDescription() {
            return "CSS stylesheets";
          }
        });
        result = chooser.showOpenDialog(this);
        if (result != JFileChooser.CANCEL_OPTION)
          defaultCss = chooser.getSelectedFile();
      }
    } else {
      result = JOptionPane.showOptionDialog(this, message, "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{yes, cancel}, cancel);
      if (result == JOptionPane.NO_OPTION) return;
    }

    // confirmation
    message = Localization.Main.getText("gui.confirm.tools.processEpubMetadataOfAllBooks2");
    result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION) return;
    new ReprocessEpubMetadataDialog(this, true, removeCss, defaultCss, onlyForTag).start();
  }

  
  private void computeBrowseByCoverWithoutSplitVisibility() {
    boolean visible = chkBrowseByCover.isSelected();
    chkBrowseByCoverWithoutSplit.setVisible(visible);
    lblBrowseByCoverWithoutSplit.setVisible(visible);
  }
  
  private void actOnDontsplittagsActionPerformed() {
    actOnDontsplittagsActionPerformed(chkDontsplittags.isSelected());
  }

  private void actOnDontsplittagsActionPerformed(boolean dontsplit) {
    if (dontsplit) txtSplittagson.setText(null);
    txtSplittagson.setEnabled(!dontsplit);
    chkDontsplittags.setSelected(dontsplit);
  }

  private void adaptInterfaceToDeviceSpecificMode(DeviceMode mode) {
    Border RED_BORDER = new LineBorder(Color.red);
    switch (mode) {
      case Nook:
        // put a border on the selected mode icon
        lblDeviceDropbox.setBorder(null);
        lblDeviceNAS.setBorder(null);
        lblDeviceNook.setBorder(RED_BORDER);
        lblDeviceMode1.setText(Localization.Main.getText("config.DeviceMode.nook.description1"));
        lblDeviceMode2.setText(Localization.Main.getText("config.DeviceMode.nook.description2"));
        lblZipTrookCatalog.setVisible(true);
        chkZipTrookCatalog.setVisible(true);
        break;
      case Nas:
        // put a border on the selected mode icon
        lblDeviceDropbox.setBorder(null);
        lblDeviceNook.setBorder(null);
        lblDeviceNAS.setBorder(RED_BORDER);
        lblDeviceMode1.setText(Localization.Main.getText("config.DeviceMode.nas.description1"));
        lblDeviceMode2.setText(Localization.Main.getText("config.DeviceMode.nas.description2"));
        lblZipTrookCatalog.setVisible(false);
        chkZipTrookCatalog.setVisible(false);
        break;
      default:
        // put a border on the selected mode icon
        lblDeviceNAS.setBorder(null);
        lblDeviceNook.setBorder(null);
        lblDeviceDropbox.setBorder(RED_BORDER);
        lblDeviceMode1.setText(Localization.Main.getText("config.DeviceMode.dropbox.description1"));
        lblDeviceMode2.setText(Localization.Main.getText("config.DeviceMode.dropbox.description2")+" ");
        lblZipTrookCatalog.setVisible(false);
        chkZipTrookCatalog.setVisible(false);
        break;
    }
    // show all the buttons and commands
    cmdCancel.setEnabled(true);
    mnuFileExit.setEnabled(true);
    cmdGenerate.setEnabled(true);
    mnuFileGenerateCatalogs.setEnabled(true);
    cmdReset.setEnabled(true);
    cmdSave.setEnabled(true);
    mnuFileSave.setEnabled(true);
  }

  private void setDeviceSpecificMode(DeviceMode mode) {
    ConfigurationManager.INSTANCE.getCurrentProfile().setDeviceMode(mode);
    adaptInterfaceToDeviceSpecificMode(mode);
    loadValues();
  }

  private void changeLanguage() {
    String newLanguage = (String) cboLang.getSelectedItem();
    if (Helper.checkedCompare(language, newLanguage) != 0) {
      ConfigurationManager.INSTANCE.getCurrentProfile().setLanguage(newLanguage);
      language = newLanguage;
      Localization.Main.reloadLocalizations();
      Localization.Enum.reloadLocalizations();
      translateTexts();
      pack();
    }
  }

  private void debugShowFile(File file) {
    try {
      OS.factory().openFile(file);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
  private void debugShowFolder(File file) {
    Frame parent = new Frame();            // Dummy ever shown
    FileDialog fd = new FileDialog (parent, file.getPath(), FileDialog.LOAD);
    fd.setDirectory(file.getPath());
    fd.setVisible(true);
    // Do nothing with result
    parent.dispose();
  }

  private void debugShowLogFile() {
    File f = new File (ConfigurationManager.INSTANCE.getConfigurationDirectory(), Constants.LOGFILE_FOLDER + "/" + Constants.LOGFILE_NAME);
    logger.info (Localization.Main.getText("gui.menu.help.logFile") + ": " + f.getPath());
    debugShowFile(f);
  }

  private void debugShowLogFolder() {
      File f = new File (ConfigurationManager.INSTANCE.getConfigurationDirectory(), Constants.LOGFILE_FOLDER);
      logger.info (Localization.Main.getText("gui.menu.help.logFolder") + ": " + f.getPath());
      debugShowFile(f);
  }

  private void debugShowSupportFolder() {
    File f = ConfigurationManager.INSTANCE.getConfigurationDirectory();
    logger.info (Localization.Main.getText("gui.menu.help.supportFolder") + ": " + f.getPath());
    debugShowFolder(f);
  }

  private void about() {
    logger.info ("Displaying About dialog");
    String intro_goal = Localization.Main.getText("intro.goal");
    String intro_wiki_title = Localization.Main.getText("intro.wiki.title");
    String intro_wiki_url = Localization.Main.getText("intro.wiki.url");
    String intro_team_title = Localization.Main.getText("intro.team.title");
    String intro_team_list1 = Localization.Main.getText("intro.team.list1");
    String intro_team_list2 = Localization.Main.getText("intro.team.list2");
    String intro_team_list3 = Localization.Main.getText("intro.team.list3");
    String intro_team_list4 = Localization.Main.getText("intro.team.list4");
    String intro_thanks_1 = Localization.Main.getText("intro.thanks.1");
    String intro_thanks_2 = Localization.Main.getText("intro.thanks.2");
    String message = "<html>" + intro_goal + "<br>" + intro_wiki_title + intro_wiki_url + "<br>" + intro_team_title + "<br><ul>" + "<li>"
                     + intro_team_list1 + "<li>" + intro_team_list2 + "<li>" + intro_team_list3 + "<li>" + intro_team_list4 + "</ul><br>"
                     + intro_thanks_1 + "<br>" + intro_thanks_2 + "</html>";
    JOptionPane.showMessageDialog(this, message, Localization.Main.getText("gui.menu.help.about"), JOptionPane.INFORMATION_MESSAGE);
  }

  private void donate() {
    String message = Localization.Main.getText("gui.confirm.donate");
    int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION) return;
    logger.info (Localization.Main.getText("gui.menu.help.donate") + ": " + Constants.PAYPAL_DONATION);
    BareBonesBrowserLaunch.openURL(Constants.PAYPAL_DONATION);
  }

  private void help() {
    logger.info (Localization.Main.getText("gui.menu.help") + ": " + Constants.HELP_URL);
    BareBonesBrowserLaunch.openURL(Constants.HELP_URL);
  }

  private void generateCatalog() {

    String message = Localization.Main.getText("gui.confirm.generate");
    int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION) return;

    storeValues();

    catalogDialog = new GenerateCatalogDialog(this, true);
    final Catalog catalog = new Catalog(catalogDialog);
    Runnable runnable = new Runnable()
    {
      public void run() {
        try {
          catalog.createMainCatalog();
          catalogEnded(null);
        } catch (Exception e) {
          catalogEnded(e);
        }
      }
    };
    new Thread(runnable).start();
    try
    {
      catalogDialog.setVisible(true);
      catalogDialog.pack();
    } catch (Exception e) {
        // ITIMPI:  This is a brute force fix to #772538
        //          which has only been reproduced on OS/X so far
        //          Maybe a tidier fix could be implemented?
        // Do nothing
    }
  }

  synchronized void catalogEnded(final Exception e) {
    if (catalogDialog == null) return;

    catalogDialog.setVisible(false);
    catalogDialog = null;
    if (e != null) {
      Runnable runnable = new Runnable()
      {
        public void run() {
          String s = Helper.getStackTrace(e);
          s = e.getMessage() + "\n" + s;
          System.out.println(s);
          logger.error(s, e);
          JOptionPane.showMessageDialog(Mainframe.this, s, e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        }
      };
      new Thread(runnable).start();
    } 
  }

  private void setProfile(String profileName) {
    ConfigurationManager.INSTANCE.changeProfile(profileName);
    if (ConfigurationManager.INSTANCE.getCurrentProfile().isObsolete()) {
      ConfigurationManager.INSTANCE.getCurrentProfile().reset();
      String msg = Localization.Main.getText("gui.reset.warning");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
    }
    loadValues();
  }
  
  private void saveNewProfile() {
    String newProfileName = JOptionPane.showInputDialog(Localization.Main.getText("gui.profile.new"));
    if ("default".equalsIgnoreCase(newProfileName))
      return;
    ConfigurationManager.INSTANCE.copyCurrentProfile(newProfileName);
    loadValues();
  }
  
  private void loadProfiles() {
    // empty the profiles menu
    mnuProfiles.removeAll();
    JMenuItem mnuProfileNew = new JMenuItem();
    mnuProfileNew.setText(Localization.Main.getText("gui.menu.profiles.new"));
    mnuProfileNew.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        saveNewProfile();
      }
    });
    mnuProfiles.add(mnuProfileNew);
    JMenuItem mnuProfileManage = new JMenuItem();
    mnuProfileManage.setText(Localization.Main.getText("gui.menu.profiles.manage"));
    mnuProfileManage.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        ProfileManagerDialog dialog = new ProfileManagerDialog(Mainframe.this, true);
        dialog.setLocationRelativeTo(Mainframe.this);
        dialog.setVisible(true);
        loadProfiles();
      }
    });
    mnuProfiles.add(mnuProfileManage);
    mnuProfiles.add(new JSeparator());
    List<String> profiles = ConfigurationManager.INSTANCE.getExistingConfigurations();
    for (String profileName : profiles) {
      JCheckBoxMenuItem mnuProfileItem = new JCheckBoxMenuItem();
      mnuProfileItem.setText(profileName); // NOI18N
      ActionListener listener = new ActionListener() {
        String profile;
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            Mainframe.this.setProfile(profile);
        }
        public ActionListener setProfile(String profile) {
          this.profile = profile;
          return this;
        }
      }.setProfile(profileName);
      mnuProfileItem.addActionListener(listener);
      // TODO check if it works
      mnuProfileItem.setSelected(ConfigurationManager.INSTANCE.getCurrentProfileName().equalsIgnoreCase(profileName));
      mnuProfiles.add(mnuProfileItem);
    }
  }

  /**
    * Load values for configuration into GUI
    * Also enable/disable any fields according to current values if required
    */
  private void loadValues() {
    InputVerifier iv = new InputVerifier()
    {

      @Override
      public boolean verify(JComponent input) {
        if (!(input instanceof JTextField)) return false;
        try {
          Integer.parseInt(((JTextField) input).getText());
          return true;
        } catch (NumberFormatException e) {
          return false;
        }
      }
    };

    cboLang.setModel(new DefaultComboBoxModel(LocalizationHelper.INSTANCE.getAvailableLocalizations()));
    cboLang.setSelectedItem(ConfigurationManager.INSTANCE.getCurrentProfile().getLanguage());
    File f = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder();
    if (f == null || !f.exists()) f = new File(".");
    txtDatabaseFolder.setText(f.getAbsolutePath());
    cmdSetDatabaseFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isDatabaseFolderReadOnly());
    txtDatabaseFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isDatabaseFolderReadOnly());
    lblDatabaseFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isDatabaseFolderReadOnly());

    f = ConfigurationManager.INSTANCE.getCurrentProfile().getTargetFolder();
    if (f == null || !f.exists()) txtTargetFolder.setText("");
    else txtTargetFolder.setText(f.getAbsolutePath());
    cmdSetTargetFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTargetFolderReadOnly());
    txtTargetFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTargetFolderReadOnly());
    lblTargetFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTargetFolderReadOnly());

    chkCopyToDatabaseFolder.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getCopyToDatabaseFolder());
    chkCopyToDatabaseFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCopyToDatabaseFolderReadOnly());
    lblCopyToDatabaseFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCopyToDatabaseFolderReadOnly());
    chkReprocessEpubMetadata.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getReprocessEpubMetadata());
    chkReprocessEpubMetadata.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isReprocessEpubMetadataReadOnly());
    lblReprocessEpubMetadata.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isReprocessEpubMetadataReadOnly());
    txtWikilang.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getWikipediaLanguage());
    txtWikilang.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isWikipediaLanguageReadOnly());
    lblWikilang.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isWikipediaLanguageReadOnly());
    txtCatalogFolder.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogFolderName());
    txtCatalogFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCatalogFolderNameReadOnly());
    lblCatalogFolder.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCatalogFolderNameReadOnly());
    txtCatalogTitle.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogTitle());
    txtCatalogTitle.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCatalogTitleReadOnly());
    lblCatalogTitle.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCatalogTitleReadOnly());
    chkNoThumbnailGenerate.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getThumbnailGenerate());
    chkNoThumbnailGenerate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isThumbnailGenerateReadOnly());
    lblNoThumbnailGenerate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isThumbnailGenerateReadOnly());
    txtThumbnailheight.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getThumbnailHeight());
    txtThumbnailheight.setInputVerifier(iv);
    txtThumbnailheight.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isThumbnailHeightReadOnly());
    lblThumbnailheight.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isThumbnailHeightReadOnly());
    chkNoCoverResize.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getCoverResize());
    chkNoCoverResize.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCoverResizeReadOnly());
    lblNoCoverResize.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCoverResizeReadOnly());
    txtCoverHeight.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getCoverHeight());
    txtCoverHeight.setInputVerifier(iv);
    txtCoverHeight.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCoverHeightReadOnly());
    lblCoverHeight.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCoverHeightReadOnly());
    txtIncludeformat.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludedFormatsList());
    txtIncludeformat.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludedFormatsListReadOnly());
    lblIncludeformat.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludedFormatsListReadOnly());
    txtMaxbeforepaginate.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforePaginate());
    txtMaxbeforepaginate.setInputVerifier(iv);
    txtMaxbeforepaginate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxBeforePaginateReadOnly());
    lblMaxbeforepaginate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxBeforePaginateReadOnly());
    txtMaxbeforesplit.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit());
    txtMaxbeforesplit.setInputVerifier(iv);
    txtMaxbeforesplit.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxBeforeSplitReadOnly());
    lblMaxbeforesplit.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxBeforeSplitReadOnly());
    txtBooksinrecent.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getBooksInRecentAdditions());
    txtBooksinrecent.setInputVerifier(iv);
    txtBooksinrecent.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBooksInRecentAdditionsReadOnly());
    lblBooksinrecent.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBooksInRecentAdditionsReadOnly());
    txtMaxsummarylength.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getMaxSummaryLength());
    txtMaxsummarylength.setInputVerifier(iv);
    txtMaxsummarylength.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxSummaryLengthReadOnly());
    lblMaxsummarylength.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxSummaryLengthReadOnly());
    txtSplittagson.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getSplitTagsOn());
    txtSplittagson.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitTagsOnReadOnly());
    lblSplittagson.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitTagsOnReadOnly());
    chkDontsplittags.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitTagsOnReadOnly());
    actOnDontsplittagsActionPerformed(Helper.isNullOrEmpty(ConfigurationManager.INSTANCE.getCurrentProfile().getSplitTagsOn()));
    chkIncludeemptybooks.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeBooksWithNoFile());
    chkIncludeemptybooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeBooksWithNoFileReadOnly());
    lblIncludeemptybooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeBooksWithNoFileReadOnly());
    chkIncludeOnlyOneFile.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeOnlyOneFile());
    chkIncludeOnlyOneFile.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeOnlyOneFileReadOnly());
    lblIncludeOnlyOneFile.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeOnlyOneFileReadOnly());
    chkNobandwidthoptimize.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getSaveBandwidth());
    chkNobandwidthoptimize.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSaveBandwidthReadOnly());
    lblNobandwidthoptimize.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSaveBandwidthReadOnly());
    chkNogenerateopds.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateOpds());
    chkNogenerateopds.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateOpdsReadOnly());
    lblNogenerateopds.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateOpdsReadOnly());
    chkNogeneratehtml.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtml());
    chkNogeneratehtml.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateHtmlReadOnly());
    lblNogeneratehtml.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateHtmlReadOnly());
    chkMinimizeChangedFiles.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getMinimizeChangedFiles());
    chkMinimizeChangedFiles.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMinimizeChangedFilesReadOnly());
    lblMinimizeChangedFiles.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMinimizeChangedFilesReadOnly());
    chkExternalIcons.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons());
    chkExternalIcons.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isExternalIconsReadOnly());
    lblExternalIcons.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isExternalIconsReadOnly());
    lblNogeneratehtmlfiles.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateHtmlDownloadsReadOnly());
    chkSupressRatings.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getSuppressRatingsInTitles());
    chkSupressRatings.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSupressRatingsInTitlesReadyOnly());
    lblSupressRatings.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSupressRatingsInTitlesReadyOnly());
    chkBrowseByCover.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getBrowseByCover());
    chkBrowseByCover.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBrowseByCoverReadOnly());
    lblBrowseByCover.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBrowseByCoverReadOnly());
    chkBrowseByCoverWithoutSplit.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getBrowseByCoverWithoutSplit());
    chkBrowseByCoverWithoutSplit.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBrowseByCoverWithoutSplitReadOnly());
    lblBrowseByCoverWithoutSplit.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBrowseByCoverWithoutSplitReadOnly());
    chkIncludeAboutLink.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getIncludeAboutLink());
    chkIncludeAboutLink.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeAboutLinkReadOnly());
    lblIncludeAboutLink.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIncludeAboutLinkReadOnly());
    chkZipTrookCatalog.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getZipTrookCatalog());
    chkZipTrookCatalog.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isZipTrookCatalogReadOnly());
    lblZipTrookCatalog.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isZipTrookCatalogReadOnly());

    chkNogenerateopdsfiles.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateDownloads());
    chkNogenerateopdsfiles.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateDownloadsReadOnly());
    lblNogenerateopdsfiles.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateDownloadsReadOnly());
    chkCryptFilenames.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getCryptFilenames());
    chkCryptFilenames.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCryptFilenamesReadOnly());
    lblCryptFilenames.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isCryptFilenamesReadOnly());
    chkNoShowSeries.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getShowSeriesInAuthorCatalog());
    chkNoShowSeries.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isShowSeriesInAuthorCatalogReadOnly());
    lblNoShowSeries.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isShowSeriesInAuthorCatalogReadOnly());
    chkOrderAllBooksBySeries.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getOrderAllBooksBySeries());
    chkOrderAllBooksBySeries.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isOrderAllBooksBySeriesReadOnly());
    lblOrderAllBooksBySeries.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isOrderAllBooksBySeriesReadOnly());
    chkSplitByAuthorInitialGoToBooks.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getSplitByAuthorInitialGoToBooks());
    chkSplitByAuthorInitialGoToBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitByAuthorInitialGoToBooksReadOnly());
    chkSplitByAuthorInitialGoToBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitByAuthorInitialGoToBooksReadOnly());
    txtBookLanguageTag.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getBookLanguageTag());
    txtBookLanguageTag.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBookLanguageTagReadOnly());
    lblBookLanguageTag.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isBookLanguageTagReadOnly());
    txtTagstogenerate.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToGenerate());
    txtTagstogenerate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToGenerateReadOnly());
    lblTagstogenerate.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToGenerateReadOnly());
    txtTagstoexclude.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToExclude());
    txtTagstoexclude.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToExcludeReadOnly());
    lblTagstoexclude.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToExcludeReadOnly());
    chkPublishedDateAsYear.setSelected(ConfigurationManager.INSTANCE.getCurrentProfile().getPublishedDateAsYear());
    chkPublishedDateAsYear.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isPublishedDateAsYearReadOnly());
    lblPublishedDateAsYear.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isPublishedDateAsYearReadOnly());
    chkNogeneratecrosslinks.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateCrossLinks());
    chkNogeneratecrosslinks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateCrossLinksReadOnly());
    lblNogeneratecrosslinks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateCrossLinksReadOnly());
    chkNogenerateexternallinks.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateExternalLinks());
    chkNogenerateexternallinks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateExternalLinksReadOnly());
    lblNogenerateexternallinks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateExternalLinksReadOnly());
    chkNoGenerateTags.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateTags());
    chkNoGenerateTags.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateTagsReadOnly());
    lblNoGenerateTags.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateTagsReadOnly());
    chkNogenerateratings.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateRatings());
    chkNogenerateratings.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateRatingsReadOnly());
    lblNogenerateratings.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateRatingsReadOnly());
    chkNogenerateallbooks.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateAllbooks());
    chkNogenerateallbooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateAllbooksReadOnly());
    lblNogenerateallbooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGenerateAllbooksReadOnly());
    txtTagsToMakeDeep.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getTagsToMakeDeep());
    txtTagsToMakeDeep.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToMakeDeepReadOnly());
    lblTagsToMakeDeep.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isTagsToMakeDeepReadOnly());
    txtMinBooksToMakeDeepLevel.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getMinBooksToMakeDeepLevel());
    txtMinBooksToMakeDeepLevel.setInputVerifier(iv);
    txtMinBooksToMakeDeepLevel.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMinBooksToMakeDeepLevelReadOnly());
    lblMinBooksToMakeDeepLevel.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMinBooksToMakeDeepLevelReadOnly());
    txtMaxMobileResolution.setText("" + ConfigurationManager.INSTANCE.getCurrentProfile().getMaxMobileResolution());
    txtMaxMobileResolution.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxMobileResolutionReadOnly());
    lblMaxMobileResolution.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isMaxMobileResolutionReadOnly());
    txtMaxMobileResolution.setVisible(false);   // Not currently being used
    lblMaxMobileResolution.setVisible(false);   // Not currently being used
    cboCompatibilityTrick.setModel(new DefaultComboBoxModel(CompatibilityTrick.values()));
    cboCompatibilityTrick.setSelectedItem(ConfigurationManager.INSTANCE.getCurrentProfile().getCompatibilityTrick());
    chkNoSplitInAuthorBooks.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getSplitInAuthorBooks());
    chkNoSplitInAuthorBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitInAuthorBooksReadOnly());
    lblNoSplitInAuthorBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitInAuthorBooksReadOnly());
    chkNoSplitInSeriesBooks.setSelected(!ConfigurationManager.INSTANCE.getCurrentProfile().getSplitInSeriesBooks());
    chkNoSplitInSeriesBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitInSeriesBooksReadOnly());
    lblNoSplitInSeriesBooks.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isSplitInSeriesBooksReadOnly());

    /* external links */
    txtWikipediaUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getWikipediaUrl());
    txtWikipediaUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isWikipediaUrlReadOnly());
    lblWikipediaUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isWikipediaUrlReadOnly());
    txtAmazonAuthorUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getAmazonAuthorUrl());
    txtAmazonAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonAuthorUrlReadOnly());
    lblAmazonAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonAuthorUrlReadOnly());
    txtAmazonIsbnUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getAmazonIsbnUrl());
    txtAmazonIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonIsbnUrlReadOnly());
    lblAmazonIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonIsbnUrlReadOnly());
    txtAmazonTitleUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getAmazonTitleUrl());
    txtAmazonTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonTitleUrlReadOnly());
    lblAmazonTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isAmazonTitleUrlReadOnly());
    txtGoodreadAuthorUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getGoodreadAuthorUrl());
    txtGoodreadAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadAuthorUrlReadOnly());
    lblGoodreadAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadAuthorUrlReadOnly());
    txtGoodreadIsbnUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getGoodreadIsbnUrl());
    txtGoodreadIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadIsbnUrlReadOnly());
    lblGoodreadIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadIsbnUrlReadOnly());
    txtGoodreadTitleUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getGoodreadTitleUrl());
    txtGoodreadTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadTitleUrlReadOnly());
    lblGoodreadTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadTitleUrlReadOnly());
    txtGoodreadReviewIsbnUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getGoodreadReviewIsbnUrl());
    txtGoodreadReviewIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadReviewIsbnUrlReadOnly());
    lblGoodreadReviewIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isGoodreadReviewIsbnUrlReadOnly());
    txtIsfdbAuthorUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getIsfdbAuthorUrl());
    txtIsfdbAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIsfdbAuthorUrlReadOnly());
    lblIsfdbAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isIsfdbAuthorUrlReadOnly());
    txtLibrarythingAuthorUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getLibrarythingAuthorUrl());
    txtLibrarythingAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingAuthorUrlReadOnly());
    lblLibrarythingAuthorUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingAuthorUrlReadOnly());
    txtLibrarythingIsbnUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getLibrarythingIsbnUrl());
    txtLibrarythingIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingIsbnUrlReadOnly());
    lblLibrarythingIsbnUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingIsbnUrlReadOnly());
    txtLibrarythingTitleUrl.setText(ConfigurationManager.INSTANCE.getCurrentProfile().getLibrarythingTitleUrl());
    txtLibrarythingTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingTitleUrlReadOnly());
    lblLibrarythingTitleUrl.setEnabled(!ConfigurationManager.INSTANCE.getCurrentProfile().isLibrarythingTitleUrlReadOnly());

    adaptInterfaceToDeviceSpecificMode(ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode());
    computeBrowseByCoverWithoutSplitVisibility();
    
    changeLanguage();
    loadProfiles();
    
    pack();
  }

  private int getValue(JTextField field) {
    try {
      int i = Integer.parseInt(field.getText());
      return i;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private void resetValues() {
    String lang = ConfigurationManager.INSTANCE.getCurrentProfile().getLanguage();
    ConfigurationManager.INSTANCE.getCurrentProfile().reset();
    loadValues();
    ConfigurationManager.INSTANCE.getCurrentProfile().setLanguage(lang);
    changeLanguage();
  }

  private void storeValues() {
    ConfigurationManager.INSTANCE.getCurrentProfile().setLanguage("" + cboLang.getSelectedItem());
    ConfigurationManager.INSTANCE.getCurrentProfile().setCompatibilityTrick(CompatibilityTrick.valueOf(""+cboCompatibilityTrick.getSelectedItem()));
    File f = new File(txtDatabaseFolder.getText());
    if (f.exists()) ConfigurationManager.INSTANCE.getCurrentProfile().setDatabaseFolder(f);
    String s = txtTargetFolder.getText();
    if (Helper.isNotNullOrEmpty(s)) {
      f = new File(s);
      if (f.exists()) ConfigurationManager.INSTANCE.getCurrentProfile().setTargetFolder(f);
    } else ConfigurationManager.INSTANCE.getCurrentProfile().setTargetFolder(null);
    ConfigurationManager.INSTANCE.getCurrentProfile().setCopyToDatabaseFolder(chkCopyToDatabaseFolder.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setReprocessEpubMetadata(chkReprocessEpubMetadata.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setWikipediaLanguage(txtWikilang.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setCatalogFolderName(txtCatalogFolder.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setCatalogTitle(txtCatalogTitle.getText());
    int i;
    i = getValue(txtThumbnailheight);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setThumbnailHeight(i);
    i = getValue(txtCoverHeight);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setCoverHeight(i);
    ConfigurationManager.INSTANCE.getCurrentProfile().setIncludedFormatsList(txtIncludeformat.getText());
    i = getValue(txtMaxbeforepaginate);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setMaxBeforePaginate(i);
    i = getValue(txtMaxbeforesplit);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setMaxBeforeSplit(i);
    i = getValue(txtBooksinrecent);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setBooksInRecentAdditions(i);
    i = getValue(txtMaxsummarylength);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setMaxSummaryLength(i);
    ConfigurationManager.INSTANCE.getCurrentProfile().setSplitTagsOn(txtSplittagson.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setIncludeBooksWithNoFile(chkIncludeemptybooks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setIncludeOnlyOneFile(chkIncludeOnlyOneFile.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setZipTrookCatalog(chkZipTrookCatalog.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setMinimizeChangedFiles(chkMinimizeChangedFiles.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setExternalIcons(chkExternalIcons.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSaveBandwidth(!chkNobandwidthoptimize.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateOpds(!chkNogenerateopds.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateHtml(!chkNogeneratehtml.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateHtmlDownloads(!chkNogeneratehtmlfiles.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateOpdsDownloads(!chkNogenerateopdsfiles.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSuppressRatingsInTitles(chkSupressRatings.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setBrowseByCover(chkBrowseByCover.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setPublishedDateAsYear(chkPublishedDateAsYear.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setBrowseByCoverWithoutSplit(chkBrowseByCoverWithoutSplit.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setIncludeAboutLink(chkIncludeAboutLink.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateDownloads(!chkNogenerateopdsfiles.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setBookLanguageTag(txtBookLanguageTag.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setCryptFilenames(chkCryptFilenames.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setShowSeriesInAuthorCatalog(!chkNoShowSeries.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setOrderAllBooksBySeries(chkOrderAllBooksBySeries.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSplitByAuthorInitialGoToBooks(chkSplitByAuthorInitialGoToBooks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateCrossLinks(!chkNogeneratecrosslinks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateExternalLinks(!chkNogenerateexternallinks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setTagsToGenerate(txtTagstogenerate.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setTagsToExclude(txtTagstoexclude.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateTags(!chkNoGenerateTags.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateRatings(!chkNogenerateratings.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGenerateAllbooks(!chkNogenerateallbooks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setTagsToMakeDeep(txtTagsToMakeDeep.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSplitInAuthorBooks(!chkNoSplitInAuthorBooks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSplitInSeriesBooks(!chkNoSplitInSeriesBooks.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setCoverResize(!chkNoCoverResize.isSelected());
    ConfigurationManager.INSTANCE.getCurrentProfile().setThumbnailGenerate(!chkNoThumbnailGenerate.isSelected());
    i = getValue(txtMinBooksToMakeDeepLevel);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setMinBooksToMakeDeepLevel(i);
    i = getValue(txtMaxMobileResolution);
    if (i > -1) ConfigurationManager.INSTANCE.getCurrentProfile().setMaxMobileResolution(i);
    ConfigurationManager.INSTANCE.getCurrentProfile().setWikipediaUrl(txtWikipediaUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setAmazonAuthorUrl(txtAmazonAuthorUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setAmazonIsbnUrl(txtAmazonIsbnUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setAmazonTitleUrl(txtAmazonTitleUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGoodreadAuthorUrl(txtGoodreadAuthorUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGoodreadIsbnUrl(txtGoodreadIsbnUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGoodreadTitleUrl(txtGoodreadTitleUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setGoodreadReviewIsbnUrl(txtGoodreadReviewIsbnUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setIsfdbAuthorUrl(txtIsfdbAuthorUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setLibrarythingAuthorUrl(txtLibrarythingAuthorUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setLibrarythingIsbnUrl(txtLibrarythingIsbnUrl.getText());
    ConfigurationManager.INSTANCE.getCurrentProfile().setLibrarythingTitleUrl(txtLibrarythingTitleUrl.getText());
  }

  /**
   * Apply localization strings to all UI elements
   * Same tootip is applied to a label and its associated input field
   */
  private void translateTexts() {
    // main window
    String title = Localization.Main.getText("gui.title", Constants.PROGTITLE + Constants.BZR_VERSION);
    setTitle(title); // NOI18N
    lblBottom0.setText(Localization.Main.getText("gui.label.clickToDescribe")); // NOI18N
    lblBottom0.setFont(lblBottom0.getFont().deriveFont(Font.BOLD));
    cmdCancel.setText(Localization.Main.getText("gui.close")); // NOI18N
    cmdSave.setText(Localization.Main.getText("gui.save")); // NOI18N
    cmdGenerate.setText(Localization.Main.getText("gui.generate")); // NOI18N
    cmdReset.setText(Localization.Main.getText("gui.reset"));
    tabOptionsTabs.setTitleAt(0, Localization.Main.getText("gui.tab1"));
    tabOptionsTabs.setTitleAt(1, Localization.Main.getText("gui.tab2"));
    tabOptionsTabs.setTitleAt(2, Localization.Main.getText("gui.tab3"));
    tabOptionsTabs.setTitleAt(3, Localization.Main.getText("gui.tab4"));
    lblDeviceDropbox.setToolTipText(Localization.Main.getText("config.DeviceMode.dropbox.description1") + " " + Localization.Main.getText("config.DeviceMode.dropbox.description2"));
    lblDeviceNAS.setToolTipText(Localization.Main.getText("config.DeviceMode.nas.description1") + " " + Localization.Main.getText("config.DeviceMode.nas.description2"));
    lblDeviceNook.setToolTipText(Localization.Main.getText("config.DeviceMode.nook.description1") + " " + Localization.Main.getText("config.DeviceMode.nook.description2"));
    adaptInterfaceToDeviceSpecificMode(ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode());

    // main options
    lblLang.setText(Localization.Main.getText("config.Language.label")); // NOI18N
    lblLang.setToolTipText(Localization.Main.getText("config.Language.description")); // NOI18N
    lblCompatibilityTrick.setText(Localization.Main.getText("config.CompatibilityTrick.label")); // NOI18N
    lblCompatibilityTrick.setToolTipText(Localization.Main.getText("config.CompatibilityTrick.description")); // NOI18N
    lblDatabaseFolder.setText(Localization.Main.getText("config.DatabaseFolder.label")); // NOI18N
    lblDatabaseFolder.setToolTipText(Localization.Main.getText("config.DatabaseFolder.description")); // NOI18N
    lblTargetFolder.setText(Localization.Main.getText("config.TargetFolder.label")); // NOI18N
    lblTargetFolder.setToolTipText(Localization.Main.getText("config.TargetFolder.description")); // NOI18N
    lblCopyToDatabaseFolder.setText(Localization.Main.getText("config.CopyToDatabaseFolder.label")); // NOI18N
    lblCopyToDatabaseFolder.setToolTipText(Localization.Main.getText("config.CopyToDatabaseFolder.description")); // NOI18N
    lblReprocessEpubMetadata.setText(Localization.Main.getText("config.ReprocessEpubMetadata.label")); // NOI18N
    lblReprocessEpubMetadata.setToolTipText(Localization.Main.getText("config.ReprocessEpubMetadata.description")); // NOI18N
    lblCatalogFolder.setText(Localization.Main.getText("config.CatalogFolderName.label")); // NOI18N
    lblCatalogFolder.setToolTipText(Localization.Main.getText("config.CatalogFolderName.description")); // NOI18N
    lblCatalogTitle.setText(Localization.Main.getText("config.CatalogTitle.label")); // NOI18N
    lblCatalogTitle.setToolTipText(Localization.Main.getText("config.CatalogTitle.description")); // NOI18N
    lblSplittagson.setText(Localization.Main.getText("config.SplitTagsOn.label")); // NOI18N
    lblSplittagson.setToolTipText(Localization.Main.getText("config.SplitTagsOn.description")); // NOI18N
    chkDontsplittags.setText(Localization.Main.getText("config.SplitTagsOn.splitbyletter")); // NOI18N
    lblBookLanguageTag.setText(Localization.Main.getText("config.BookLanguageTag.label")); // NOI18N
    lblBookLanguageTag.setToolTipText(Localization.Main.getText("config.BookLanguageTag.description")); // NOI18N
    lblTagstogenerate.setText(Localization.Main.getText("config.TagsToGenerate.label")); // NOI18N
    lblTagstogenerate.setToolTipText(Localization.Main.getText("config.TagsToGenerate.description")); // NOI18N
    lblTagstoexclude.setText(Localization.Main.getText("config.TagsToExclude.label")); // NOI18N
    lblTagstoexclude.setToolTipText(Localization.Main.getText("config.TagsToExclude.description")); // NOI18N
    lblWikilang.setText(Localization.Main.getText("config.WikipediaLanguage.label")); // NOI18N
    lblWikilang.setToolTipText(Localization.Main.getText("config.WikipediaLanguage.description")); // NOI18N

    // catalog generation options
    lblNogenerateopds.setText(Localization.Main.getText("config.GenerateOpds.label")); // NOI18N
    lblNogenerateopds.setToolTipText(Localization.Main.getText("config.GenerateOpds.description")); // NOI18N
    chkNogenerateopds.setToolTipText(Localization.Main.getText("config.GenerateOpds.description")); // NOI18N
    lblNogeneratehtml.setText(Localization.Main.getText("config.GenerateHtml.label")); // NOI18N
    lblNogeneratehtml.setToolTipText(Localization.Main.getText("config.GenerateHtml.description")); // NOI18N
    chkNogeneratehtml.setToolTipText(Localization.Main.getText("config.GenerateHtml.description")); // NOI18N
    lblNogeneratehtmlfiles.setText(Localization.Main.getText("config.GenerateHtmlDownloads.label")); // NOI18N
    lblNogeneratehtmlfiles.setToolTipText(Localization.Main.getText("config.GenerateHtmlDownloads.description")); // NOI18N
    chkNogeneratehtmlfiles.setToolTipText(Localization.Main.getText("config.GenerateHtmlDownloads.description")); // NOI18N
    lblBrowseByCover.setText(Localization.Main.getText("config.BrowseByCover.label")); // NOI18N
    lblBrowseByCover.setToolTipText(Localization.Main.getText("config.BrowseByCover.description")); // NOI18N
    chkBrowseByCover.setToolTipText(Localization.Main.getText("config.BrowseByCover.description")); // NOI18N
    lblBrowseByCoverWithoutSplit.setText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.label")); // NOI18N
    lblBrowseByCoverWithoutSplit.setToolTipText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.description")); // NOI18N
    chkBrowseByCoverWithoutSplit.setToolTipText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.description")); // NOI18N
    lblIncludeAboutLink.setText(Localization.Main.getText("config.IncludeAboutLink.label")); // NOI18N
    lblIncludeAboutLink.setToolTipText(Localization.Main.getText("config.IncludeAboutLink.description")); // NOI18N
    chkIncludeAboutLink.setToolTipText(Localization.Main.getText("config.IncludeAboutLink.description")); // NOI18N
    lblNogenerateopdsfiles.setText(Localization.Main.getText("config.GenerateOpdsDownloads.label")); // NOI18N
    lblNogenerateopdsfiles.setToolTipText(Localization.Main.getText("config.GenerateOpdsDownloads.description")); // NOI18N
    chkNogenerateopdsfiles.setToolTipText(Localization.Main.getText("config.GenerateOpdsDownloads.description")); // NOI18N
    lblNogenerateexternallinks.setText(Localization.Main.getText("config.GenerateExternalLinks.label")); // NOI18N
    lblNogenerateexternallinks.setToolTipText(Localization.Main.getText("config.GenerateExternalLinks.description")); // NOI18N
    chkNogenerateexternallinks.setToolTipText(Localization.Main.getText("config.GenerateExternalLinks.description")); // NOI18N
    lblNogeneratecrosslinks.setText(Localization.Main.getText("config.GenerateCrossLinks.label")); // NOI18N
    lblNogeneratecrosslinks.setToolTipText(Localization.Main.getText("config.GenerateCrossLinks.description")); // NOI18N
    chkNogeneratecrosslinks.setToolTipText(Localization.Main.getText("config.GenerateCrossLinks.description")); // NOI18N
    lblNobandwidthoptimize.setText(Localization.Main.getText("config.SaveBandwidth.label")); // NOI18N
    lblNobandwidthoptimize.setToolTipText(Localization.Main.getText("config.SaveBandwidth.description")); // NOI18N
    chkNobandwidthoptimize.setToolTipText(Localization.Main.getText("config.SaveBandwidth.description")); // NOI18N
    lblPublishedDateAsYear.setText(Localization.Main.getText("config.PublishedDateAsYear.label")); // NOI18N
    lblPublishedDateAsYear.setToolTipText(Localization.Main.getText("config.PublishedDateAsYear.description")); // NOI18N
    chkPublishedDateAsYear.setToolTipText(Localization.Main.getText("config.PublishedDateAsYear.description")); // NOI18N
    lblExternalIcons.setText(Localization.Main.getText("config.ExternalIcons.label")); // NOI18N
    lblExternalIcons.setToolTipText(Localization.Main.getText("config.ExternalIcons.description")); // NOI18N
    chkExternalIcons.setToolTipText(Localization.Main.getText("config.ExternalIcons.description")); // NOI18N
    lblNoGenerateTags.setText(Localization.Main.getText("config.GenerateTags.label")); // NOI18N
    lblNoGenerateTags.setToolTipText(Localization.Main.getText("config.GenerateTags.description")); // NOI18N
    chkNoGenerateTags.setToolTipText(Localization.Main.getText("config.GenerateTags.description")); // NOI18N
    lblNogenerateratings.setText(Localization.Main.getText("config.GenerateRatings.label")); // NOI18N
    lblNogenerateratings.setToolTipText(Localization.Main.getText("config.GenerateRatings.description")); // NOI18N
    chkNogenerateratings.setToolTipText(Localization.Main.getText("config.GenerateRatings.description")); // NOI18N
    lblSupressRatings.setText(Localization.Main.getText("config.SuppressRatingsInTitles.label")); // NOI18N
    lblSupressRatings.setToolTipText(Localization.Main.getText("config.SuppressRatingsInTitles.description")); // NOI18N
    chkSupressRatings.setToolTipText(Localization.Main.getText("config.SuppressRatingsInTitles.description")); // NOI18N
    lblNogenerateallbooks.setText(Localization.Main.getText("config.GenerateAllbooks.label")); // NOI18N
    lblNogenerateallbooks.setToolTipText(Localization.Main.getText("config.GenerateAllbooks.description")); // NOI18N
    chkNogenerateallbooks.setToolTipText(Localization.Main.getText("config.GenerateAllbooks.description")); // NOI18N
    lblMinimizeChangedFiles.setText(Localization.Main.getText("config.MinimizeChangedFiles.label")); // NOI18N
    lblMinimizeChangedFiles.setToolTipText(Localization.Main.getText("config.MinimizeChangedFiles.description")); // NOI18N
    chkMinimizeChangedFiles.setToolTipText(Localization.Main.getText("config.MinimizeChangedFiles.description")); // NOI18N
    lblCryptFilenames.setText(Localization.Main.getText("config.CryptFilenames.label")); // NOI18N
    lblCryptFilenames.setToolTipText(Localization.Main.getText("config.CryptFilenames.description")); // NOI18N
    chkCryptFilenames.setToolTipText(Localization.Main.getText("config.CryptFilenames.description")); // NOI18N

    // advanced customization options
    lblIncludeformat.setText(Localization.Main.getText("config.IncludedFormatsList.label")); // NOI18N
    lblIncludeformat.setToolTipText(Localization.Main.getText("config.IncludedFormatsList.description")); // NOI18N
    lblMaxbeforepaginate.setText(Localization.Main.getText("config.MaxBeforePaginate.label")); // NOI18N
    lblMaxbeforepaginate.setToolTipText(Localization.Main.getText("config.MaxBeforePaginate.description")); // NOI18N
    lblMaxbeforesplit.setText(Localization.Main.getText("config.MaxBeforeSplit.label")); // NOI18N
    lblMaxbeforesplit.setToolTipText(Localization.Main.getText("config.MaxBeforeSplit.description")); // NOI18N
    lblBooksinrecent.setText(Localization.Main.getText("config.BooksInRecentAdditions.label")); // NOI18N
    lblBooksinrecent.setToolTipText(Localization.Main.getText("config.BooksInRecentAdditions.description")); // NOI18N
    lblMaxsummarylength.setText(Localization.Main.getText("config.MaxSummaryLength.label")); // NOI18N
    lblMaxsummarylength.setToolTipText(Localization.Main.getText("config.MaxSummaryLength.description")); // NOI18N
    lblIncludeemptybooks.setText(Localization.Main.getText("config.IncludeBooksWithNoFile.label")); // NOI18N
    lblIncludeemptybooks.setToolTipText(Localization.Main.getText("config.IncludeBooksWithNoFile.description")); // NOI18N
    lblIncludeOnlyOneFile.setText(Localization.Main.getText("config.IncludeOnlyOneFile.label")); // NOI18N
    lblIncludeOnlyOneFile.setToolTipText(Localization.Main.getText("config.IncludeOnlyOneFile.description")); // NOI18N
    lblZipTrookCatalog.setText(Localization.Main.getText("config.ZipTrookCatalog.label")); // NOI18N
    lblZipTrookCatalog.setToolTipText(Localization.Main.getText("config.ZipTrookCatalog.description")); // NOI18N
    lblNoShowSeries.setText(Localization.Main.getText("config.ShowSeriesInAuthorCatalog.label")); // NOI18N
    lblNoShowSeries.setToolTipText(Localization.Main.getText("config.ShowSeriesInAuthorCatalog.description")); // NOI18N
    lblOrderAllBooksBySeries.setText(Localization.Main.getText("config.OrderAllBooksBySeries.label")); // NOI18N
    lblOrderAllBooksBySeries.setToolTipText(Localization.Main.getText("config.OrderAllBooksBySeries.description")); // NOI18N
    lblSplitByAuthorInitialGoToBooks.setText(Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.label")); // NOI18N
    lblSplitByAuthorInitialGoToBooks.setToolTipText(Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.description")); // NOI18N
    lblNoThumbnailGenerate.setText(Localization.Main.getText("config.ThumbnailGenerate.label")); // NOI18N
    lblNoThumbnailGenerate.setToolTipText(Localization.Main.getText("config.ThumbnailGenerate.description")); // NOI18N
    lblThumbnailheight.setText(Localization.Main.getText("config.ThumbnailHeight.label")); // NOI18N
    lblThumbnailheight.setToolTipText(Localization.Main.getText("config.ThumbnailHeight.description")); // NOI18N
    lblNoCoverResize.setText(Localization.Main.getText("config.CoverResize.label")); // NOI18N
    lblNoCoverResize.setToolTipText(Localization.Main.getText("config.CoverResize.description")); // NOI18N
    lblCoverHeight.setText(Localization.Main.getText("config.CoverHeight.label")); // NOI18N
    lblCoverHeight.setToolTipText(Localization.Main.getText("config.CoverHeight.description")); // NOI18N
    lblTagsToMakeDeep.setText(Localization.Main.getText("config.TagsToMakeDeep.label")); // NOI18N
    lblTagsToMakeDeep.setToolTipText(Localization.Main.getText("config.TagsToMakeDeep.description")); // NOI18N
    lblMinBooksToMakeDeepLevel.setText(Localization.Main.getText("config.MinBooksToMakeDeepLevel.label")); // NOI18N
    lblMinBooksToMakeDeepLevel.setToolTipText(Localization.Main.getText("config.MinBooksToMakeDeepLevel.description")); // NOI18N
    lblMaxMobileResolution.setText(Localization.Main.getText("config.MaxMobileResolution.label")); // NOI18N
    lblMaxMobileResolution.setToolTipText(Localization.Main.getText("config.MaxMobileResolution.description")); // NOI18N
    lblNoSplitInAuthorBooks.setText(Localization.Main.getText("config.SplitInAuthorBooks.label")); // NOI18N
    lblNoSplitInAuthorBooks.setToolTipText(Localization.Main.getText("config.SplitInAuthorBooks.description")); // NOI18N
    lblNoSplitInSeriesBooks.setText(Localization.Main.getText("config.SplitInSeriesBooks.label")); // NOI18N
    lblNoSplitInSeriesBooks.setToolTipText(Localization.Main.getText("config.SplitInSeriesBooks.description")); // NOI18N

    // external links
    lblWikipediaUrl.setText(Localization.Main.getText("config.WikipediaUrl.label")); // NOI18N
    lblWikipediaUrl.setToolTipText(Localization.Main.getText("config.WikipediaUrl.description")); // NOI18N
    cmdWikipediaUrlReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdWikipediaUrlReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonAuthorUrl.setText(Localization.Main.getText("config.AmazonAuthorUrl.label")); // NOI18N
    lblAmazonAuthorUrl.setToolTipText(Localization.Main.getText("config.AmazonAuthorUrl.description")); // NOI18N
    cmdAmazonUrlReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonUrlReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonIsbnUrl.setText(Localization.Main.getText("config.AmazonIsbnUrl.label")); // NOI18N
    lblAmazonIsbnUrl.setToolTipText(Localization.Main.getText("config.AmazonIsbnUrl.description")); // NOI18N
    cmdAmazonIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonTitleUrl.setText(Localization.Main.getText("config.AmazonTitleUrl.label")); // NOI18N
    lblAmazonTitleUrl.setToolTipText(Localization.Main.getText("config.AmazonTitleUrl.description")); // NOI18N
    cmdAmazonTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadAuthorUrl.setText(Localization.Main.getText("config.GoodreadAuthorUrl.label")); // NOI18N
    lblGoodreadAuthorUrl.setToolTipText(Localization.Main.getText("config.GoodreadAuthorUrl.description")); // NOI18N
    cmdGoodreadAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadIsbnUrl.setText(Localization.Main.getText("config.GoodreadIsbnUrl.label")); // NOI18N
    lblGoodreadIsbnUrl.setToolTipText(Localization.Main.getText("config.GoodreadIsbnUrl.description")); // NOI18N
    cmdGoodreadIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadTitleUrl.setText(Localization.Main.getText("config.GoodreadTitleUrl.label")); // NOI18N
    lblGoodreadTitleUrl.setToolTipText(Localization.Main.getText("config.GoodreadTitleUrl.description")); // NOI18N
    cmdGoodreadTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadReviewIsbnUrl.setText(Localization.Main.getText("config.GoodreadReviewIsbnUrl.label")); // NOI18N
    lblGoodreadReviewIsbnUrl.setToolTipText(Localization.Main.getText("config.GoodreadReviewIsbnUrl.description")); // NOI18N
    cmdGoodreadReviewReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadReviewReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblIsfdbAuthorUrl.setText(Localization.Main.getText("config.IsfdbAuthorUrl.label")); // NOI18N
    lblIsfdbAuthorUrl.setToolTipText(Localization.Main.getText("config.IsfdbAuthorUrl.description")); // NOI18N
    cmdIsfdbAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdIsfdbAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingAuthorUrl.setText(Localization.Main.getText("config.LibrarythingAuthorUrl.label")); // NOI18N
    lblLibrarythingAuthorUrl.setToolTipText(Localization.Main.getText("config.LibrarythingAuthorUrl.description")); // NOI18N
    cmdLibrarythingAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingIsbnUrl.setText(Localization.Main.getText("config.LibrarythingIsbnUrl.label")); // NOI18N
    lblLibrarythingIsbnUrl.setToolTipText(Localization.Main.getText("config.LibrarythingIsbnUrl.description")); // NOI18N
    cmdLibrarythingIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingTitleUrl.setText(Localization.Main.getText("config.LibrarythingTitleUrl.label")); // NOI18N
    lblLibrarythingTitleUrl.setToolTipText(Localization.Main.getText("config.LibrarythingTitleUrl.description")); // NOI18N
    cmdLibrarythingTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N

    // menus
    mnuFile.setText(Localization.Main.getText("gui.menu.file")); // NOI18N
    mnuFileSave.setText(Localization.Main.getText("gui.save")); // NOI18N
    mnuFileGenerateCatalogs.setText(Localization.Main.getText("gui.generate")); // NOI18N
    mnuFileExit.setText(Localization.Main.getText("gui.close")); // NOI18N
    mnuProfiles.setText(Localization.Main.getText("gui.menu.profiles")); // NOI18N
    mnuTools.setText(Localization.Main.getText("gui.menu.tools")); // NOI18N
    mnuToolsprocessEpubMetadataOfAllBooks.setText(Localization.Main.getText("gui.menu.tools.processEpubMetadataOfAllBooks")); // NOI18N
    mnuHelp.setText(Localization.Main.getText("gui.menu.help")); // NOI18N
    mnuHelpDonate.setText(Localization.Main.getText("gui.menu.help.donate")); // NOI18N
    mnuHelpAbout.setText(Localization.Main.getText("gui.menu.help.about")); // NOI18N
    mnuHelpWiki.setText(Localization.Main.getText("gui.menu.help.wiki")); // NOI18N
    mnuHelpOpenLog.setText(Localization.Main.getText("gui.menu.help.logFile")); // NOI18N
    mnuHelpOpenSupport.setText(Localization.Main.getText("gui.menu.help.supportFolder")); // NOI18N
  }

  /**
   * Display in a popup the tooltip assoicated with a label that the user has clicked on
   * This is for convenience in environments where the tootip is not conveniently displayed.
   * @param label
   */
  private void popupExplanation(JLabel label) {
    String popup = null;
    // main options
    if (label == lblLang) popup = Localization.Main.getText("config.Language.description");
    else if (label == lblCompatibilityTrick) popup = Localization.Main.getText("config.CompatibilityTrick.description");
    else if (label == lblDatabaseFolder) popup = Localization.Main.getText("config.DatabaseFolder.description");
    else if (label == lblTargetFolder) popup = Localization.Main.getText("config.TargetFolder.description");
    else if (label == lblCopyToDatabaseFolder) popup = Localization.Main.getText("config.CopyToDatabaseFolder.description");
    else if (label == lblReprocessEpubMetadata) popup = Localization.Main.getText("config.ReprocessEpubMetadata.description");
    else if (label == lblCatalogFolder) popup = Localization.Main.getText("config.CatalogFolderName.description");
    else if (label == lblCatalogTitle) popup = Localization.Main.getText("config.CatalogTitle.description");
    else if (label == lblSplittagson) popup = Localization.Main.getText("config.SplitTagsOn.description");
    else if (label == lblBookLanguageTag) popup = Localization.Main.getText("config.BookLanguageTag.description");
    else if (label == lblTagstogenerate) popup = Localization.Main.getText("config.TagsToGenerate.description");
    else if (label == lblTagstoexclude) popup = Localization.Main.getText("config.TagsToExclude.description");
    else if (label == lblWikilang) popup = Localization.Main.getText("config.WikipediaLanguage.description");

    // catalog generation options
    else if (label == lblNogenerateopds) popup = Localization.Main.getText("config.GenerateOpds.description");
    else if (label == lblNogeneratehtml) popup = Localization.Main.getText("config.GenerateHtml.description");
    else if (label == lblIncludeAboutLink) popup = Localization.Main.getText("config.IncludeAboutLink.description");
    else if (label == lblNoGenerateTags) popup = Localization.Main.getText("config.NoGenerateTags.description");
    else if (label == lblNogenerateratings) popup = Localization.Main.getText("config.Nogenerateratings.description");
    else if (label == lblNogenerateallbooks) popup = Localization.Main.getText("config.Nogenerateallbooks.description");
    else if (label == lblNogenerateexternallinks) popup = Localization.Main.getText("config.GenerateExternalLinks.description");
    else if (label == lblNogeneratecrosslinks) popup = Localization.Main.getText("config.GenerateCrossLinks.description");
    else if (label == lblNogenerateopdsfiles) popup = Localization.Main.getText("config.GenerateOpdsDownloads.description");
    else if (label == lblNogeneratehtmlfiles) popup = Localization.Main.getText("config.GenerateHtmlDownloads.description");
    else if (label == lblBrowseByCover) popup = Localization.Main.getText("config.BrowseByCover.description");
    else if (label == lblPublishedDateAsYear) popup = Localization.Main.getText("config.PublishedDateAsYear.description");
    else if (label == lblBrowseByCoverWithoutSplit) popup = Localization.Main.getText("config.BrowseByCoverWithoutSplit.description");
    else if (label == lblExternalIcons) popup = Localization.Main.getText("config.ExternalIcons.description");
    else if (label == lblNobandwidthoptimize) popup = Localization.Main.getText("config.SaveBandwidth.description");
    else if (label == lblMinimizeChangedFiles) popup = Localization.Main.getText("config.MinimizeChangedFiles.description");
    else if (label == lblCryptFilenames) popup = Localization.Main.getText("config.CryptFilenames.description");

    // advanced customization options
    else if (label == lblIncludeformat) popup = Localization.Main.getText("config.IncludedFormatsList.description");
    else if (label == lblMaxbeforepaginate) popup = Localization.Main.getText("config.MaxBeforePaginate.description");
    else if (label == lblMaxbeforesplit) popup = Localization.Main.getText("config.MaxBeforeSplit.description");
    else if (label == lblBooksinrecent) popup = Localization.Main.getText("config.BooksInRecentAdditions.description");
    else if (label == lblMaxsummarylength) popup = Localization.Main.getText("config.MaxSummaryLength.description");
    else if (label == lblIncludeemptybooks) popup = Localization.Main.getText("config.IncludeBooksWithNoFile.description");
    else if (label == lblIncludeOnlyOneFile) popup = Localization.Main.getText("config.IncludeOnlyOneFile.description");
    else if (label == lblZipTrookCatalog) popup = Localization.Main.getText("config.ZipTrookCatalog.description");
    else if (label == lblNoShowSeries) popup = Localization.Main.getText("config.ShowSeriesInAuthorCatalog.description");
    else if (label == lblSplitByAuthorInitialGoToBooks) popup = Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.description");
    else if (label == lblNoThumbnailGenerate) popup = Localization.Main.getText("config.ThumbnailGenerate.description");
    else if (label == lblThumbnailheight) popup = Localization.Main.getText("config.ThumbnailHeight.description");
    else if (label == lblNoCoverResize) popup = Localization.Main.getText("config.CoverResize.description");
    else if (label == lblCoverHeight) popup = Localization.Main.getText("config.CoverHeight.description");
    else if (label == lblTagsToMakeDeep) popup = Localization.Main.getText("config.TagsToMakeDeep.description");
    else if (label == lblMinBooksToMakeDeepLevel) popup = Localization.Main.getText("config.MinBooksToMakeDeepLevel.description");
    else if (label == lblMaxMobileResolution) popup = Localization.Main.getText("config.MaxMobileResolution.description");
    else if (label == lblNoSplitInAuthorBooks) popup = Localization.Main.getText("config.SplitInAuthorBooks.description");
    else if (label == lblNoSplitInSeriesBooks) popup = Localization.Main.getText("config.SplitInSeriesBooks.description");

    // external links
    else if (label == lblWikipediaUrl) popup = Localization.Main.getText("config.WikipediaUrl.description");
    else if (label == lblAmazonAuthorUrl) popup = Localization.Main.getText("config.AmazonAuthorUrl.description");
    else if (label == lblAmazonIsbnUrl) popup = Localization.Main.getText("config.AmazonIsbnUrl.description");
    else if (label == lblAmazonTitleUrl) popup = Localization.Main.getText("config.AmazonTitleUrl.description");
    else if (label == lblGoodreadAuthorUrl) popup = Localization.Main.getText("config.GoodreadAuthorUrl.description");
    else if (label == lblGoodreadIsbnUrl) popup = Localization.Main.getText("config.GoodreadIsbnUrl.description");
    else if (label == lblGoodreadTitleUrl) popup = Localization.Main.getText("config.GoodreadTitleUrl.description");
    else if (label == lblGoodreadReviewIsbnUrl) popup = Localization.Main.getText("config.GoodreadReviewIsbnUrl.description");
    else if (label == lblIsfdbAuthorUrl) popup = Localization.Main.getText("config.IsfdbAuthorUrl.description");
    else if (label == lblLibrarythingAuthorUrl) popup = Localization.Main.getText("config.LibrarythingAuthorUrl.description");
    else if (label == lblLibrarythingIsbnUrl) popup = Localization.Main.getText("config.LibrarythingIsbnUrl.description");
    else if (label == lblLibrarythingTitleUrl) popup = Localization.Main.getText("config.LibrarythingTitleUrl.description");

    if (Helper.isNotNullOrEmpty(popup)) JOptionPane.showMessageDialog(this, popup, Localization.Main.getText("gui.description"),
                                                                      JOptionPane.INFORMATION_MESSAGE);
  }

  private void showSetDatabaseFolderDialog() {
    JDirectoryChooser chooser = new JDirectoryChooser();
    chooser.setShowingCreateDirectory(false);
    File f = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder();
    if (f != null && f.exists()) chooser.setCurrentDirectory(f);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.CANCEL_OPTION) return;
    f = chooser.getSelectedFile();
    if (setDatabaseFolder(f.getAbsolutePath()))
      txtDatabaseFolder.setText(f.getAbsolutePath());
  }

  private boolean setDatabaseFolder(String targetFolder) {
    File newFolder = new File(targetFolder);
    if (newFolder.exists()) {
      File oldFolder = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder();
      ConfigurationManager.INSTANCE.getCurrentProfile().setDatabaseFolder(newFolder);
      if (DatabaseManager.INSTANCE.databaseExists()) { 
        JOptionPane.showMessageDialog(this, Localization.Main.getText("info.databasefolderset", targetFolder), null, JOptionPane.INFORMATION_MESSAGE);
        return true;
      } else
        ConfigurationManager.INSTANCE.getCurrentProfile().setDatabaseFolder(oldFolder);
    }
    JOptionPane.showMessageDialog(this, Localization.Main.getText("error.nodatabase", targetFolder), null, JOptionPane.ERROR_MESSAGE);
    return false;
  }

  private void showSetTargetFolderDialog() {
    JDirectoryChooser chooser = new JDirectoryChooser();
    chooser.setShowingCreateDirectory(true);
    File f = ConfigurationManager.INSTANCE.getCurrentProfile().getTargetFolder();
    if (f != null && f.exists()) chooser.setCurrentDirectory(f);
    else {
      f = ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder();
      if (f != null && f.exists()) chooser.setCurrentDirectory(f);
    }
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.CANCEL_OPTION) return;
    f = chooser.getSelectedFile();
    if (setTargetFolder(f.getAbsolutePath()))
      txtTargetFolder.setText(f.getAbsolutePath());
  }
  
  private boolean setTargetFolder(String targetFolder) {
    File newFolder = new File(targetFolder);
    if (!newFolder.exists()) {
      String message = Localization.Main.getText("error.targetdoesnotexist", targetFolder);
      int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
      if (result != JOptionPane.YES_OPTION) return false;
      newFolder.mkdirs();
    }
    JOptionPane.showMessageDialog(this, Localization.Main.getText("info.targetfolderset", targetFolder), null, JOptionPane.INFORMATION_MESSAGE);
    return true;
  }

  private void openLogFolder() {
      // Do nothing yet
  }
  private void saveConfiguration() {
    storeValues();
    String message = Localization.Main.getText("gui.info.saved");
    int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION) return;
    System.exit(0);
  }

  private void cancelDialog() {
    System.exit(0);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
   * method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlMain = new javax.swing.JPanel();
        lblDeviceDropbox = new javax.swing.JLabel();
        lblDeviceNAS = new javax.swing.JLabel();
        lblDeviceNook = new javax.swing.JLabel();
        lblDeviceMode1 = new javax.swing.JLabel();
        lblDeviceMode2 = new javax.swing.JLabel();
        lblDonate = new javax.swing.JLabel();
        tabOptionsTabs = new javax.swing.JTabbedPane();
        pnlMainOptions = new javax.swing.JPanel();
        lblLang = new javax.swing.JLabel();
        cboLang = new javax.swing.JComboBox();
        lblDatabaseFolder = new javax.swing.JLabel();
        txtDatabaseFolder = new javax.swing.JTextField();
        cmdSetDatabaseFolder = new javax.swing.JButton();
        lblTargetFolder = new javax.swing.JLabel();
        txtTargetFolder = new javax.swing.JTextField();
        cmdSetTargetFolder = new javax.swing.JButton();
        lblCatalogFolder = new javax.swing.JLabel();
        txtCatalogFolder = new javax.swing.JTextField();
        lblCatalogTitle = new javax.swing.JLabel();
        txtCatalogTitle = new javax.swing.JTextField();
        lblSplittagson = new javax.swing.JLabel();
        pnlSplitTagsOn = new javax.swing.JPanel();
        txtSplittagson = new javax.swing.JTextField();
        chkDontsplittags = new javax.swing.JCheckBox();
        lblBookLanguageTag = new javax.swing.JLabel();
        txtBookLanguageTag = new javax.swing.JTextField();
        lblTagstogenerate = new javax.swing.JLabel();
        txtTagstogenerate = new javax.swing.JTextField();
        lblTagstoexclude = new javax.swing.JLabel();
        txtTagstoexclude = new javax.swing.JTextField();
        lblWikilang = new javax.swing.JLabel();
        txtWikilang = new javax.swing.JTextField();
        chkCopyToDatabaseFolder = new javax.swing.JCheckBox();
        lblCopyToDatabaseFolder = new javax.swing.JLabel();
        lblCompatibilityTrick = new javax.swing.JLabel();
        cboCompatibilityTrick = new javax.swing.JComboBox();
        chkReprocessEpubMetadata = new javax.swing.JCheckBox();
        lblReprocessEpubMetadata = new javax.swing.JLabel();
        pnlGenerationOptions = new javax.swing.JPanel();
        lblCryptFilenames = new javax.swing.JLabel();
        chkCryptFilenames = new javax.swing.JCheckBox();
        lblNogeneratehtml = new javax.swing.JLabel();
        chkNogeneratehtml = new javax.swing.JCheckBox();
        lblNogeneratehtmlfiles = new javax.swing.JLabel();
        chkMinimizeChangedFiles = new javax.swing.JCheckBox();
        lblBrowseByCover = new javax.swing.JLabel();
        chkBrowseByCover = new javax.swing.JCheckBox();
        lblBrowseByCoverWithoutSplit = new javax.swing.JLabel();
        chkBrowseByCoverWithoutSplit = new javax.swing.JCheckBox();
        lblIncludeAboutLink = new javax.swing.JLabel();
        chkIncludeAboutLink = new javax.swing.JCheckBox();
        lblNogenerateopdsfiles = new javax.swing.JLabel();
        chkNogenerateopdsfiles = new javax.swing.JCheckBox();
        lblNogenerateexternallinks = new javax.swing.JLabel();
        lblNogeneratecrosslinks = new javax.swing.JLabel();
        chkNogenerateexternallinks = new javax.swing.JCheckBox();
        chkNogeneratecrosslinks = new javax.swing.JCheckBox();
        lblNobandwidthoptimize = new javax.swing.JLabel();
        chkNobandwidthoptimize = new javax.swing.JCheckBox();
        lblNogenerateratings = new javax.swing.JLabel();
        chkNogenerateratings = new javax.swing.JCheckBox();
        lblNogenerateallbooks = new javax.swing.JLabel();
        chkNogenerateallbooks = new javax.swing.JCheckBox();
        lblSupressRatings = new javax.swing.JLabel();
        chkSupressRatings = new javax.swing.JCheckBox();
        lblMinimizeChangedFiles = new javax.swing.JLabel();
        chkNogeneratehtmlfiles = new javax.swing.JCheckBox();
        chkNogenerateopds = new javax.swing.JCheckBox();
        lblNogenerateopds = new javax.swing.JLabel();
        lblExternalIcons = new javax.swing.JLabel();
        chkExternalIcons = new javax.swing.JCheckBox();
        chkPublishedDateAsYear = new javax.swing.JCheckBox();
        lblPublishedDateAsYear = new javax.swing.JLabel();
        chkNoGenerateTags = new javax.swing.JCheckBox();
        lblNoGenerateTags = new javax.swing.JLabel();
        lblNoSplitInSeriesBooks = new javax.swing.JLabel();
        chkNoSplitInSeriesBooks = new javax.swing.JCheckBox();
        lblNoSplitInAuthorBooks = new javax.swing.JLabel();
        chkNoSplitInAuthorBooks = new javax.swing.JCheckBox();
        pnlAdvancedOptions = new javax.swing.JPanel();
        lblIncludeformat = new javax.swing.JLabel();
        txtIncludeformat = new javax.swing.JTextField();
        lblMaxbeforepaginate = new javax.swing.JLabel();
        txtMaxbeforepaginate = new javax.swing.JTextField();
        lblMaxbeforesplit = new javax.swing.JLabel();
        txtMaxbeforesplit = new javax.swing.JTextField();
        lblBooksinrecent = new javax.swing.JLabel();
        txtBooksinrecent = new javax.swing.JTextField();
        lblMaxsummarylength = new javax.swing.JLabel();
        txtMaxsummarylength = new javax.swing.JTextField();
        lblIncludeemptybooks = new javax.swing.JLabel();
        chkIncludeemptybooks = new javax.swing.JCheckBox();
        lblNoShowSeries = new javax.swing.JLabel();
        chkNoShowSeries = new javax.swing.JCheckBox();
        lblThumbnailheight = new javax.swing.JLabel();
        txtThumbnailheight = new javax.swing.JTextField();
        lblSplitByAuthorInitialGoToBooks = new javax.swing.JLabel();
        chkSplitByAuthorInitialGoToBooks = new javax.swing.JCheckBox();
        lblTagsToMakeDeep = new javax.swing.JLabel();
        txtTagsToMakeDeep = new javax.swing.JTextField();
        lblMinBooksToMakeDeepLevel = new javax.swing.JLabel();
        txtMinBooksToMakeDeepLevel = new javax.swing.JTextField();
        txtCoverHeight = new javax.swing.JTextField();
        lblCoverHeight = new javax.swing.JLabel();
        lblIncludeOnlyOneFile = new javax.swing.JLabel();
        chkIncludeOnlyOneFile = new javax.swing.JCheckBox();
        lblZipTrookCatalog = new javax.swing.JLabel();
        chkZipTrookCatalog = new javax.swing.JCheckBox();
        chkOrderAllBooksBySeries = new javax.swing.JCheckBox();
        lblOrderAllBooksBySeries = new javax.swing.JLabel();
        txtMaxMobileResolution = new javax.swing.JTextField();
        lblMaxMobileResolution = new javax.swing.JLabel();
        lblNoCoverResize = new javax.swing.JLabel();
        lblNoThumbnailGenerate = new javax.swing.JLabel();
        chkNoCoverResize = new javax.swing.JCheckBox();
        chkNoThumbnailGenerate = new javax.swing.JCheckBox();
        pnlExternalUrlsOptions = new javax.swing.JPanel();
        lblWikipediaUrl = new javax.swing.JLabel();
        txtWikipediaUrl = new javax.swing.JTextField();
        lblAmazonAuthorUrl = new javax.swing.JLabel();
        txtAmazonAuthorUrl = new javax.swing.JTextField();
        lblAmazonIsbnUrl = new javax.swing.JLabel();
        txtAmazonIsbnUrl = new javax.swing.JTextField();
        lblAmazonTitleUrl = new javax.swing.JLabel();
        txtAmazonTitleUrl = new javax.swing.JTextField();
        lblGoodreadAuthorUrl = new javax.swing.JLabel();
        txtGoodreadAuthorUrl = new javax.swing.JTextField();
        lblGoodreadIsbnUrl = new javax.swing.JLabel();
        txtGoodreadIsbnUrl = new javax.swing.JTextField();
        lblGoodreadTitleUrl = new javax.swing.JLabel();
        txtGoodreadTitleUrl = new javax.swing.JTextField();
        lblGoodreadReviewIsbnUrl = new javax.swing.JLabel();
        txtGoodreadReviewIsbnUrl = new javax.swing.JTextField();
        lblIsfdbAuthorUrl = new javax.swing.JLabel();
        txtIsfdbAuthorUrl = new javax.swing.JTextField();
        lblLibrarythingAuthorUrl = new javax.swing.JLabel();
        txtLibrarythingAuthorUrl = new javax.swing.JTextField();
        lblLibrarythingIsbnUrl = new javax.swing.JLabel();
        txtLibrarythingIsbnUrl = new javax.swing.JTextField();
        lblLibrarythingTitleUrl = new javax.swing.JLabel();
        txtLibrarythingTitleUrl = new javax.swing.JTextField();
        cmdWikipediaUrlReset = new javax.swing.JButton();
        cmdAmazonUrlReset = new javax.swing.JButton();
        cmdAmazonTitleReset = new javax.swing.JButton();
        cmdAmazonIsbnReset = new javax.swing.JButton();
        cmdGoodreadAuthorReset = new javax.swing.JButton();
        cmdGoodreadIsbnReset = new javax.swing.JButton();
        cmdGoodreadReviewReset = new javax.swing.JButton();
        cmdGoodreadTitleReset = new javax.swing.JButton();
        cmdIsfdbAuthorReset = new javax.swing.JButton();
        cmdLibrarythingAuthorReset = new javax.swing.JButton();
        cmdLibrarythingIsbnReset = new javax.swing.JButton();
        cmdLibrarythingTitleReset = new javax.swing.JButton();
        pnlBottom = new javax.swing.JPanel();
        lblBottom0 = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        cmdCancel = new javax.swing.JButton();
        cmdReset = new javax.swing.JButton();
        cmdSave = new javax.swing.JButton();
        cmdGenerate = new javax.swing.JButton();
        pnlTitle = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuFileSave = new javax.swing.JMenuItem();
        mnuFileGenerateCatalogs = new javax.swing.JMenuItem();
        mnuFileExit = new javax.swing.JMenuItem();
        mnuProfiles = new javax.swing.JMenu();
        mnuTools = new javax.swing.JMenu();
        mnuToolsprocessEpubMetadataOfAllBooks = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuHelpDonate = new javax.swing.JMenuItem();
        mnuHelpWiki = new javax.swing.JMenuItem();
        mnuHelpOpenLog = new javax.swing.JMenuItem();
        mnuHelpOpenSupport = new javax.swing.JMenuItem();
        mnuHelpAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Localization.Main.getText("gui.title")); // NOI18N

        pnlMain.setLayout(new java.awt.GridBagLayout());

        lblDeviceDropbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/calibre-icon.gif"))); // NOI18N
        lblDeviceDropbox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDeviceDropboxMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(lblDeviceDropbox, gridBagConstraints);

        lblDeviceNAS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nas.png"))); // NOI18N
        lblDeviceNAS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDeviceNASMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(lblDeviceNAS, gridBagConstraints);

        lblDeviceNook.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nook.png"))); // NOI18N
        lblDeviceNook.setMinimumSize(null);
        lblDeviceNook.setPreferredSize(null);
        lblDeviceNook.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDeviceNookMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(lblDeviceNook, gridBagConstraints);

        lblDeviceMode1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDeviceMode1.setText(Localization.Main.getText("gui.label.clickToDescribe")); // NOI18N
        lblDeviceMode1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        pnlMain.add(lblDeviceMode1, gridBagConstraints);

        lblDeviceMode2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDeviceMode2.setText(Localization.Main.getText("gui.label.clickToDescribe")); // NOI18N
        lblDeviceMode2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 5);
        pnlMain.add(lblDeviceMode2, gridBagConstraints);

        lblDonate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donate.gif"))); // NOI18N
        lblDonate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDonateMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(lblDonate, gridBagConstraints);

        pnlMainOptions.setLayout(new java.awt.GridBagLayout());

        lblLang.setText(Localization.Main.getText("config.Language.label")); // NOI18N
        lblLang.setToolTipText(Localization.Main.getText("config.Language.description")); // NOI18N
        lblLang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblLang, gridBagConstraints);

        cboLang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboLang.setPreferredSize(new java.awt.Dimension(100, 20));
        cboLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboLangActionPerformed(evt);
            }
        });
        cboLang.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                cboLangVetoableChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cboLang, gridBagConstraints);

        lblDatabaseFolder.setText(Localization.Main.getText("config.DatabaseFolder.label")); // NOI18N
        lblDatabaseFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblDatabaseFolder, gridBagConstraints);

        txtDatabaseFolder.setText("txtDatabaseFolder");
        txtDatabaseFolder.setPreferredSize(new java.awt.Dimension(400, 20));
        txtDatabaseFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDatabaseFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtDatabaseFolder, gridBagConstraints);

        cmdSetDatabaseFolder.setText("...");
        cmdSetDatabaseFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSetDatabaseFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cmdSetDatabaseFolder, gridBagConstraints);

        lblTargetFolder.setText(Localization.Main.getText("config.TargetFolder.label")); // NOI18N
        lblTargetFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblTargetFolder, gridBagConstraints);

        txtTargetFolder.setText("txtTargetFolder");
        txtTargetFolder.setPreferredSize(new java.awt.Dimension(400, 20));
        txtTargetFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTargetFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtTargetFolder, gridBagConstraints);

        cmdSetTargetFolder.setText("...");
        cmdSetTargetFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSetTargetFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cmdSetTargetFolder, gridBagConstraints);

        lblCatalogFolder.setText(Localization.Main.getText("config.CatalogFolderName.label")); // NOI18N
        lblCatalogFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogFolder, gridBagConstraints);

        txtCatalogFolder.setText("txtCatalogFolder");
        txtCatalogFolder.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtCatalogFolder, gridBagConstraints);

        lblCatalogTitle.setText(Localization.Main.getText("config.CatalogTitle.label")); // NOI18N
        lblCatalogTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogTitle, gridBagConstraints);

        txtCatalogTitle.setText("txtCatalogTitle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtCatalogTitle, gridBagConstraints);

        lblSplittagson.setText(Localization.Main.getText("config.SplitTagsOn.label")); // NOI18N
        lblSplittagson.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblSplittagson, gridBagConstraints);

        pnlSplitTagsOn.setLayout(new java.awt.GridBagLayout());

        txtSplittagson.setText("txtSplittagson");
        txtSplittagson.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlSplitTagsOn.add(txtSplittagson, gridBagConstraints);

        chkDontsplittags.setText(Localization.Main.getText("config.SplitTagsOn.splitbyletter")); // NOI18N
        chkDontsplittags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDontsplittagsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pnlSplitTagsOn.add(chkDontsplittags, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMainOptions.add(pnlSplitTagsOn, gridBagConstraints);

        lblBookLanguageTag.setText(Localization.Main.getText("config.BookLanguageTag.label")); // NOI18N
        lblBookLanguageTag.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblBookLanguageTag, gridBagConstraints);

        txtBookLanguageTag.setText("txtBookLanguageTag");
        txtBookLanguageTag.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtBookLanguageTag, gridBagConstraints);

        lblTagstogenerate.setText(Localization.Main.getText("config.TagsToGenerate.label")); // NOI18N
        lblTagstogenerate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblTagstogenerate, gridBagConstraints);

        txtTagstogenerate.setText("txtTagstogenerate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtTagstogenerate, gridBagConstraints);

        lblTagstoexclude.setText(Localization.Main.getText("config.TagsToExclude.label")); // NOI18N
        lblTagstoexclude.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblTagstoexclude, gridBagConstraints);

        txtTagstoexclude.setText("txtTagstoexclude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtTagstoexclude, gridBagConstraints);

        lblWikilang.setText(Localization.Main.getText("config.WikipediaLanguage.label")); // NOI18N
        lblWikilang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblWikilang, gridBagConstraints);

        txtWikilang.setText("txtWikilang");
        txtWikilang.setPreferredSize(new java.awt.Dimension(60, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtWikilang, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkCopyToDatabaseFolder, gridBagConstraints);

        lblCopyToDatabaseFolder.setText(Localization.Main.getText("config.CopyToDatabaseFolder.label")); // NOI18N
        lblCopyToDatabaseFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblCopyToDatabaseFolder, gridBagConstraints);

        lblCompatibilityTrick.setText(Localization.Main.getText("config.CompatibilityTrick.label")); // NOI18N
        lblCompatibilityTrick.setToolTipText(Localization.Main.getText("config.CompatibilityTrick.description")); // NOI18N
        lblCompatibilityTrick.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCompatibilityTrick, gridBagConstraints);

        cboCompatibilityTrick.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboCompatibilityTrick.setPreferredSize(new java.awt.Dimension(100, 20));
        cboCompatibilityTrick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboCompatibilityTrickActionPerformed(evt);
            }
        });
        cboCompatibilityTrick.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                cboCompatibilityTrickVetoableChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cboCompatibilityTrick, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkReprocessEpubMetadata, gridBagConstraints);

        lblReprocessEpubMetadata.setText(Localization.Main.getText("config.ReprocessEpubMetadata.label")); // NOI18N
        lblReprocessEpubMetadata.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblReprocessEpubMetadata, gridBagConstraints);

        tabOptionsTabs.addTab(Localization.Main.getText("gui.tab1"), pnlMainOptions); // NOI18N

        pnlGenerationOptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        pnlGenerationOptions.setLayout(new java.awt.GridBagLayout());

        lblCryptFilenames.setText(Localization.Main.getText("config.CryptFilenames.label")); // NOI18N
        lblCryptFilenames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblCryptFilenames, gridBagConstraints);
        lblCryptFilenames.getAccessibleContext().setAccessibleName("Encrypt the filenames ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkCryptFilenames, gridBagConstraints);

        lblNogeneratehtml.setText(Localization.Main.getText("config.GenerateHtml.label")); // NOI18N
        lblNogeneratehtml.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogeneratehtml, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogeneratehtml, gridBagConstraints);

        lblNogeneratehtmlfiles.setText(Localization.Main.getText("config.GenerateHtmlDownloads.label")); // NOI18N
        lblNogeneratehtmlfiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogeneratehtmlfiles, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkMinimizeChangedFiles, gridBagConstraints);

        lblBrowseByCover.setText(Localization.Main.getText("config.BrowseByCover.label")); // NOI18N
        lblBrowseByCover.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblBrowseByCover, gridBagConstraints);

        chkBrowseByCover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBrowseByCoverActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkBrowseByCover, gridBagConstraints);

        lblBrowseByCoverWithoutSplit.setText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.label")); // NOI18N
        lblBrowseByCoverWithoutSplit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblBrowseByCoverWithoutSplit, gridBagConstraints);
        lblBrowseByCoverWithoutSplit.getAccessibleContext().setAccessibleName("Do not split by letter in \"Browse by Cover\" mode");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkBrowseByCoverWithoutSplit, gridBagConstraints);

        lblIncludeAboutLink.setText(Localization.Main.getText("config.IncludeAboutLink.label")); // NOI18N
        lblIncludeAboutLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblIncludeAboutLink, gridBagConstraints);
        lblIncludeAboutLink.getAccessibleContext().setAccessibleName("Include the \"About calibre2opds\" link");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkIncludeAboutLink, gridBagConstraints);

        lblNogenerateopdsfiles.setText(Localization.Main.getText("config.GenerateDownloads.label")); // NOI18N
        lblNogenerateopdsfiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        lblNogenerateopdsfiles.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                lblNoenerateOpdsfile(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogenerateopdsfiles, gridBagConstraints);
        lblNogenerateopdsfiles.getAccessibleContext().setAccessibleName("Do not generate OPDS downloads");

        chkNogenerateopdsfiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNogenerateopdsfilesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogenerateopdsfiles, gridBagConstraints);

        lblNogenerateexternallinks.setText(Localization.Main.getText("config.GenerateExternalLinks.label")); // NOI18N
        lblNogenerateexternallinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogenerateexternallinks, gridBagConstraints);
        lblNogenerateexternallinks.getAccessibleContext().setAccessibleName("Do not generate external links ");

        lblNogeneratecrosslinks.setText(Localization.Main.getText("config.GenerateCrossLinks.label")); // NOI18N
        lblNogeneratecrosslinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogeneratecrosslinks, gridBagConstraints);
        lblNogeneratecrosslinks.getAccessibleContext().setAccessibleName("Do not generate cross-reference links ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogenerateexternallinks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogeneratecrosslinks, gridBagConstraints);

        lblNobandwidthoptimize.setText(Localization.Main.getText("config.SaveBandwith.label")); // NOI18N
        lblNobandwidthoptimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNobandwidthoptimize, gridBagConstraints);
        lblNobandwidthoptimize.getAccessibleContext().setAccessibleName("Regenerate all thumbnail images ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNobandwidthoptimize, gridBagConstraints);

        lblNogenerateratings.setText(Localization.Main.getText("config.GenerateRatings.label")); // NOI18N
        lblNogenerateratings.setRequestFocusEnabled(false);
        lblNogenerateratings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogenerateratings, gridBagConstraints);
        lblNogenerateratings.getAccessibleContext().setAccessibleName("Do not generate the \"Ratings\" catalog ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogenerateratings, gridBagConstraints);

        lblNogenerateallbooks.setText(Localization.Main.getText("config.GenerateAllbooks.label")); // NOI18N
        lblNogenerateallbooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogenerateallbooks, gridBagConstraints);
        lblNogenerateallbooks.getAccessibleContext().setAccessibleName("Do not generate the \"All books\" catalog ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogenerateallbooks, gridBagConstraints);

        lblSupressRatings.setText(Localization.Main.getText("config.SuppressRatingsInTitles.label")); // NOI18N
        lblSupressRatings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblSupressRatings, gridBagConstraints);
        lblSupressRatings.getAccessibleContext().setAccessibleName("Suppress ratings in the books titles ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkSupressRatings, gridBagConstraints);

        lblMinimizeChangedFiles.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMinimizeChangedFiles.setText("lblMinimizeChangedFiles");
        lblMinimizeChangedFiles.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblMinimizeChangedFiles.setMaximumSize(new java.awt.Dimension(162, 14));
        lblMinimizeChangedFiles.setMinimumSize(new java.awt.Dimension(162, 14));
        lblMinimizeChangedFiles.setPreferredSize(new java.awt.Dimension(162, 14));
        lblMinimizeChangedFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlGenerationOptions.add(lblMinimizeChangedFiles, gridBagConstraints);
        lblMinimizeChangedFiles.getAccessibleContext().setAccessibleName("Minimze number of changed files");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogeneratehtmlfiles, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNogenerateopds, gridBagConstraints);

        lblNogenerateopds.setText("lblNogenerateopds");
        lblNogenerateopds.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNogenerateopds, gridBagConstraints);
        lblNogenerateopds.getAccessibleContext().setAccessibleName("Di Not generate OPDS catalogs");

        lblExternalIcons.setText("lblExternalIcons");
        lblExternalIcons.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblExternalIcons, gridBagConstraints);
        lblExternalIcons.getAccessibleContext().setAccessibleName("Use External Files for Icons");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkExternalIcons, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkPublishedDateAsYear, gridBagConstraints);

        lblPublishedDateAsYear.setText("lblPublishedDateAsYear");
        lblPublishedDateAsYear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblPublishedDateAsYear, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNoGenerateTags, gridBagConstraints);

        lblNoGenerateTags.setText("Do not generate the \"Tags\" catalog");
        lblNoGenerateTags.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNoGenerateTags, gridBagConstraints);

        lblNoSplitInSeriesBooks.setText("lblNoSplitInSeriesBooks");
        lblNoSplitInSeriesBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlGenerationOptions.add(lblNoSplitInSeriesBooks, gridBagConstraints);
        lblNoSplitInSeriesBooks.getAccessibleContext().setAccessibleName("Do not split books in Series");
        lblNoSplitInSeriesBooks.getAccessibleContext().setAccessibleParent(pnlGenerationOptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNoSplitInSeriesBooks, gridBagConstraints);
        chkNoSplitInSeriesBooks.getAccessibleContext().setAccessibleParent(pnlGenerationOptions);

        lblNoSplitInAuthorBooks.setText("lblNoSplitInAuthorBooks");
        lblNoSplitInAuthorBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(lblNoSplitInAuthorBooks, gridBagConstraints);
        lblNoSplitInAuthorBooks.getAccessibleContext().setAccessibleName("Do not split books in Author");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlGenerationOptions.add(chkNoSplitInAuthorBooks, gridBagConstraints);

        tabOptionsTabs.addTab(Localization.Main.getText("gui.tab2"), pnlGenerationOptions); // NOI18N

        pnlAdvancedOptions.setLayout(new java.awt.GridBagLayout());

        lblIncludeformat.setText(Localization.Main.getText("config.IncludedFormatsList.label")); // NOI18N
        lblIncludeformat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIncludeformat, gridBagConstraints);

        txtIncludeformat.setText("txtIncludeformat");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtIncludeformat, gridBagConstraints);

        lblMaxbeforepaginate.setText(Localization.Main.getText("config.MaxBeforePaginate.label")); // NOI18N
        lblMaxbeforepaginate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxbeforepaginate, gridBagConstraints);

        txtMaxbeforepaginate.setText("txtMaxbeforepaginate");
        txtMaxbeforepaginate.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxbeforepaginate, gridBagConstraints);

        lblMaxbeforesplit.setText(Localization.Main.getText("config.MaxBeforeSplit.label")); // NOI18N
        lblMaxbeforesplit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxbeforesplit, gridBagConstraints);

        txtMaxbeforesplit.setText("txtMaxbeforesplit");
        txtMaxbeforesplit.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxbeforesplit, gridBagConstraints);

        lblBooksinrecent.setText(Localization.Main.getText("config.BooksInRecentAdditions.label")); // NOI18N
        lblBooksinrecent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblBooksinrecent, gridBagConstraints);

        txtBooksinrecent.setText("txtBooksinrecent");
        txtBooksinrecent.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtBooksinrecent, gridBagConstraints);

        lblMaxsummarylength.setText(Localization.Main.getText("config.MaxSummaryLength.label")); // NOI18N
        lblMaxsummarylength.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxsummarylength, gridBagConstraints);

        txtMaxsummarylength.setText("txtMaxsummarylength");
        txtMaxsummarylength.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxsummarylength, gridBagConstraints);

        lblIncludeemptybooks.setText(Localization.Main.getText("config.IncludeBooksWithNoFile.label")); // NOI18N
        lblIncludeemptybooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIncludeemptybooks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkIncludeemptybooks, gridBagConstraints);

        lblNoShowSeries.setText(Localization.Main.getText("config.ShowSeriesInAuthorCatalog.label")); // NOI18N
        lblNoShowSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblNoShowSeries, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkNoShowSeries, gridBagConstraints);

        lblThumbnailheight.setText(Localization.Main.getText("config.ThumbnailHeight.label")); // NOI18N
        lblThumbnailheight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblThumbnailheight, gridBagConstraints);

        txtThumbnailheight.setText("txtThumbnailheight");
        txtThumbnailheight.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtThumbnailheight, gridBagConstraints);

        lblSplitByAuthorInitialGoToBooks.setText(Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.label")); // NOI18N
        lblSplitByAuthorInitialGoToBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblSplitByAuthorInitialGoToBooks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkSplitByAuthorInitialGoToBooks, gridBagConstraints);

        lblTagsToMakeDeep.setText(Localization.Main.getText("config.TagsToMakeDeep.label")); // NOI18N
        lblTagsToMakeDeep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblTagsToMakeDeep, gridBagConstraints);

        txtTagsToMakeDeep.setText("txtTagsToMakeDeep");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtTagsToMakeDeep, gridBagConstraints);

        lblMinBooksToMakeDeepLevel.setText(Localization.Main.getText("config.MinBooksToMakeDeepLevel.label")); // NOI18N
        lblMinBooksToMakeDeepLevel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMinBooksToMakeDeepLevel, gridBagConstraints);

        txtMinBooksToMakeDeepLevel.setText("txtMaxsummarylength");
        txtMinBooksToMakeDeepLevel.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMinBooksToMakeDeepLevel, gridBagConstraints);

        txtCoverHeight.setText("txtCoverHeight");
        txtCoverHeight.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtCoverHeight, gridBagConstraints);

        lblCoverHeight.setText(Localization.Main.getText("config.CoverHeight.label")); // NOI18N
        lblCoverHeight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblCoverHeight, gridBagConstraints);

        lblIncludeOnlyOneFile.setText(Localization.Main.getText("config.IncludeOnlyOneFile.label")); // NOI18N
        lblIncludeOnlyOneFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIncludeOnlyOneFile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkIncludeOnlyOneFile, gridBagConstraints);

        lblZipTrookCatalog.setText(Localization.Main.getText("config.ZipTrookCatalog.label")); // NOI18N
        lblZipTrookCatalog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblZipTrookCatalog, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkZipTrookCatalog, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkOrderAllBooksBySeries, gridBagConstraints);

        lblOrderAllBooksBySeries.setText(Localization.Main.getText("config.OrderAllBooksBySeries.label")); // NOI18N
        lblOrderAllBooksBySeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblOrderAllBooksBySeries, gridBagConstraints);

        txtMaxMobileResolution.setText("txtMaxMobileResolution");
        txtMaxMobileResolution.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtMaxMobileResolution(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxMobileResolution, gridBagConstraints);

        lblMaxMobileResolution.setText("lblMaxMobileResolution");
        lblMaxMobileResolution.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxMobileResolution, gridBagConstraints);

        lblNoCoverResize.setText("Do not resize covers");
        lblNoCoverResize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblNoCoverResize, gridBagConstraints);

        lblNoThumbnailGenerate.setText("Do not generate thumbnails");
        lblNoThumbnailGenerate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblNoThumbnailGenerate, gridBagConstraints);

        chkNoCoverResize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoCoverResizeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkNoCoverResize, gridBagConstraints);

        chkNoThumbnailGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoThumbnailGenerateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkNoThumbnailGenerate, gridBagConstraints);

        tabOptionsTabs.addTab(Localization.Main.getText("gui.tab3"), pnlAdvancedOptions); // NOI18N

        pnlExternalUrlsOptions.setLayout(new java.awt.GridBagLayout());

        lblWikipediaUrl.setText(Localization.Main.getText("config.WikipediaUrl.label")); // NOI18N
        lblWikipediaUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblWikipediaUrl, gridBagConstraints);

        txtWikipediaUrl.setText("txtWikipediaUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtWikipediaUrl, gridBagConstraints);

        lblAmazonAuthorUrl.setText(Localization.Main.getText("config.AmazonAuthorUrl.label")); // NOI18N
        lblAmazonAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonAuthorUrl, gridBagConstraints);

        txtAmazonAuthorUrl.setText("txtAmazonAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonAuthorUrl, gridBagConstraints);

        lblAmazonIsbnUrl.setText(Localization.Main.getText("config.AmazonIsbnUrl.label")); // NOI18N
        lblAmazonIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonIsbnUrl, gridBagConstraints);

        txtAmazonIsbnUrl.setText("txtAmazonIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonIsbnUrl, gridBagConstraints);

        lblAmazonTitleUrl.setText(Localization.Main.getText("config.AmazonTitleUrl.label")); // NOI18N
        lblAmazonTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonTitleUrl, gridBagConstraints);

        txtAmazonTitleUrl.setText("txtAmazonTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonTitleUrl, gridBagConstraints);

        lblGoodreadAuthorUrl.setText(Localization.Main.getText("config.GoodreadAuthorUrl.label")); // NOI18N
        lblGoodreadAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadAuthorUrl, gridBagConstraints);

        txtGoodreadAuthorUrl.setText("txtGoodreadAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadAuthorUrl, gridBagConstraints);

        lblGoodreadIsbnUrl.setText(Localization.Main.getText("config.GoodreadIsbnUrl.label")); // NOI18N
        lblGoodreadIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadIsbnUrl, gridBagConstraints);

        txtGoodreadIsbnUrl.setText("txtGoodreadIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadIsbnUrl, gridBagConstraints);

        lblGoodreadTitleUrl.setText(Localization.Main.getText("config.GoodreadTitleUrl.label")); // NOI18N
        lblGoodreadTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadTitleUrl, gridBagConstraints);

        txtGoodreadTitleUrl.setText("txtGoodreadTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadTitleUrl, gridBagConstraints);

        lblGoodreadReviewIsbnUrl.setText(Localization.Main.getText("config.GoodreadReviewIsbnUrl.label")); // NOI18N
        lblGoodreadReviewIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadReviewIsbnUrl, gridBagConstraints);

        txtGoodreadReviewIsbnUrl.setText("txtGoodreadReviewIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadReviewIsbnUrl, gridBagConstraints);

        lblIsfdbAuthorUrl.setText(Localization.Main.getText("config.IsfdbAuthorUrl.label")); // NOI18N
        lblIsfdbAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblIsfdbAuthorUrl, gridBagConstraints);

        txtIsfdbAuthorUrl.setText("txtIsfdbAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtIsfdbAuthorUrl, gridBagConstraints);

        lblLibrarythingAuthorUrl.setText(Localization.Main.getText("config.LibrarythingAuthorUrl.label")); // NOI18N
        lblLibrarythingAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingAuthorUrl, gridBagConstraints);

        txtLibrarythingAuthorUrl.setText("txtLibrarythingAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingAuthorUrl, gridBagConstraints);

        lblLibrarythingIsbnUrl.setText(Localization.Main.getText("config.LibrarythingIsbnUrl.label")); // NOI18N
        lblLibrarythingIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingIsbnUrl, gridBagConstraints);

        txtLibrarythingIsbnUrl.setText("txtLibrarythingIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingIsbnUrl, gridBagConstraints);

        lblLibrarythingTitleUrl.setText(Localization.Main.getText("config.LibrarythingTitleUrl.label")); // NOI18N
        lblLibrarythingTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingTitleUrl, gridBagConstraints);

        txtLibrarythingTitleUrl.setText("txtLibrarythingTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingTitleUrl, gridBagConstraints);

        cmdWikipediaUrlReset.setText("Reset!");
        cmdWikipediaUrlReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdWikipediaUrlResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdWikipediaUrlReset, gridBagConstraints);

        cmdAmazonUrlReset.setText("Reset!");
        cmdAmazonUrlReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAmazonUrlResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdAmazonUrlReset, gridBagConstraints);

        cmdAmazonTitleReset.setText("Reset!");
        cmdAmazonTitleReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAmazonTitleResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdAmazonTitleReset, gridBagConstraints);

        cmdAmazonIsbnReset.setText("Reset!");
        cmdAmazonIsbnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAmazonIsbnResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdAmazonIsbnReset, gridBagConstraints);

        cmdGoodreadAuthorReset.setText("Reset!");
        cmdGoodreadAuthorReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGoodreadAuthorResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdGoodreadAuthorReset, gridBagConstraints);

        cmdGoodreadIsbnReset.setText("Reset!");
        cmdGoodreadIsbnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGoodreadIsbnResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdGoodreadIsbnReset, gridBagConstraints);

        cmdGoodreadReviewReset.setText("Reset!");
        cmdGoodreadReviewReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGoodreadReviewResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdGoodreadReviewReset, gridBagConstraints);

        cmdGoodreadTitleReset.setText("Reset!");
        cmdGoodreadTitleReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGoodreadTitleResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdGoodreadTitleReset, gridBagConstraints);

        cmdIsfdbAuthorReset.setText("Reset!");
        cmdIsfdbAuthorReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdIsfdbAuthorResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdIsfdbAuthorReset, gridBagConstraints);

        cmdLibrarythingAuthorReset.setText("Reset!");
        cmdLibrarythingAuthorReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingAuthorResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingAuthorReset, gridBagConstraints);

        cmdLibrarythingIsbnReset.setText("Reset!");
        cmdLibrarythingIsbnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingIsbnResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingIsbnReset, gridBagConstraints);

        cmdLibrarythingTitleReset.setText("Reset!");
        cmdLibrarythingTitleReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingTitleResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingTitleReset, gridBagConstraints);

        tabOptionsTabs.addTab("Main options", pnlExternalUrlsOptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(tabOptionsTabs, gridBagConstraints);
        tabOptionsTabs.getAccessibleContext().setAccessibleName("External Links");

        pnlBottom.setLayout(new java.awt.GridBagLayout());

        lblBottom0.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblBottom0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBottom0.setText(Localization.Main.getText("gui.label.clickToDescribe")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBottom.add(lblBottom0, gridBagConstraints);

        cmdCancel.setText(Localization.Main.getText("gui.close")); // NOI18N
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdCancel);

        cmdReset.setText(Localization.Main.getText("gui.reset")); // NOI18N
        cmdReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdResetActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdReset);

        cmdSave.setText(Localization.Main.getText("gui.save")); // NOI18N
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdSave);

        cmdGenerate.setText(Localization.Main.getText("gui.generate")); // NOI18N
        cmdGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGenerateActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdGenerate);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBottom.add(pnlButtons, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlMain.add(pnlBottom, gridBagConstraints);

        pnlTitle.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        pnlMain.add(pnlTitle, gridBagConstraints);

        getContentPane().add(pnlMain, java.awt.BorderLayout.CENTER);

        mnuFile.setText(Localization.Main.getText("gui.menu.file")); // NOI18N

        mnuFileSave.setText(Localization.Main.getText("gui.save")); // NOI18N
        mnuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileSaveActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileSave);

        mnuFileGenerateCatalogs.setText(Localization.Main.getText("gui.generate")); // NOI18N
        mnuFileGenerateCatalogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileGenerateCatalogsActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileGenerateCatalogs);

        mnuFileExit.setText(Localization.Main.getText("gui.close")); // NOI18N
        mnuFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileExitActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileExit);

        jMenuBar1.add(mnuFile);

        mnuProfiles.setText(Localization.Main.getText("gui.menu.profiles")); // NOI18N
        jMenuBar1.add(mnuProfiles);

        mnuTools.setText(Localization.Main.getText("gui.menu.tools")); // NOI18N

        mnuToolsprocessEpubMetadataOfAllBooks.setText(Localization.Main.getText("gui.menu.tools.processEpubMetadataOfAllBooks")); // NOI18N
        mnuToolsprocessEpubMetadataOfAllBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsprocessEpubMetadataOfAllBooksActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsprocessEpubMetadataOfAllBooks);

        jMenuBar1.add(mnuTools);

        mnuHelp.setText(Localization.Main.getText("gui.menu.help")); // NOI18N

        mnuHelpDonate.setText(Localization.Main.getText("gui.menu.help.donate")); // NOI18N
        mnuHelpDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpDonateActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpDonate);

        mnuHelpWiki.setText(Localization.Main.getText("gui.menu.help.wiki")); // NOI18N
        mnuHelpWiki.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpWikiActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpWiki);

        mnuHelpOpenLog.setText(Localization.Main.getText("gui.menu.help.logFile")); // NOI18N
        mnuHelpOpenLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenLogActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenLog);

        mnuHelpOpenSupport.setText(Localization.Main.getText("gui.menu.help.supportFolder")); // NOI18N
        mnuHelpOpenSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenSupportActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenSupport);

        mnuHelpAbout.setText(Localization.Main.getText("gui.menu.help.about")); // NOI18N
        mnuHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpAbout);

        jMenuBar1.add(mnuHelp);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void lblDeviceNASMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDeviceNASMouseClicked
    setDeviceSpecificMode(DeviceMode.Nas);
  }//GEN-LAST:event_lblDeviceNASMouseClicked

  private void chkBrowseByCoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBrowseByCoverActionPerformed
    computeBrowseByCoverWithoutSplitVisibility();
  }//GEN-LAST:event_chkBrowseByCoverActionPerformed

  private void txtDatabaseFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDatabaseFolderActionPerformed
      setDatabaseFolder(txtDatabaseFolder.getText());
  }//GEN-LAST:event_txtDatabaseFolderActionPerformed

  private void txtTargetFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTargetFolderActionPerformed
      setTargetFolder(txtTargetFolder.getText());
  }//GEN-LAST:event_txtTargetFolderActionPerformed

  private void cboCompatibilityTrickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCompatibilityTrickActionPerformed
      // TODO add your handling code here:
  }//GEN-LAST:event_cboCompatibilityTrickActionPerformed

  private void cboCompatibilityTrickVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_cboCompatibilityTrickVetoableChange
      // TODO add your handling code here:
  }//GEN-LAST:event_cboCompatibilityTrickVetoableChange

  private void mnuHelpOpenSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenSupportActionPerformed
      debugShowSupportFolder();
  }//GEN-LAST:event_mnuHelpOpenSupportActionPerformed

  private void mnuHelpOpenLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenLogActionPerformed
      debugShowLogFile();
  }//GEN-LAST:event_mnuHelpOpenLogActionPerformed

  private void mnuToolsprocessEpubMetadataOfAllBooksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsprocessEpubMetadataOfAllBooksActionPerformed
      processEpubMetadataOfAllBooks();
  }//GEN-LAST:event_mnuToolsprocessEpubMetadataOfAllBooksActionPerformed

  private void txtMaxMobileResolution(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtMaxMobileResolution
      JOptionPane.showMessageDialog(null, Localization.Main.getText("error.notYetReady"), "Warning",
                                      JOptionPane.WARNING_MESSAGE);
  }//GEN-LAST:event_txtMaxMobileResolution

  private void chkNoCoverResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNoCoverResizeActionPerformed
    // lblCoverHeight.setVisible(!chkNoCoverResize.isSelected());
    lblCoverHeight.setVisible(true);
    // txtCoverHeight.setVisible(!chkNoCoverResize.isSelected());
    txtCoverHeight.setVisible(true);
  }//GEN-LAST:event_chkNoCoverResizeActionPerformed

  private void chkNoThumbnailGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNoThumbnailGenerateActionPerformed
    // lblThumbnailheight.setVisible(!chkNoThumbnailGenerate.isSelected());
    lblThumbnailheight.setVisible(true);
    // txtThumbnailheight.setVisible(!chkNoThumbnailGenerate.isSelected());
    txtThumbnailheight.setVisible(true);
  }//GEN-LAST:event_chkNoThumbnailGenerateActionPerformed

  private void cmdWikipediaUrlResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWikipediaUrlResetActionPerformed
    txtWikipediaUrl.setText(StanzaConstants.WIKIPEDIA_URL);
  }//GEN-LAST:event_cmdWikipediaUrlResetActionPerformed

  private void cmdAmazonUrlResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonUrlResetActionPerformed
      txtAmazonAuthorUrl.setText(StanzaConstants.AMAZON_AUTHOR_URL);
  }//GEN-LAST:event_cmdAmazonUrlResetActionPerformed

  private void cmdAmazonIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonIsbnResetActionPerformed
      txtAmazonIsbnUrl.setText(StanzaConstants.AMAZON_ISBN_URL);
  }//GEN-LAST:event_cmdAmazonIsbnResetActionPerformed

  private void cmdAmazonTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonTitleResetActionPerformed
      txtAmazonTitleUrl.setText(StanzaConstants.AMAZON_TITLE_URL);
  }//GEN-LAST:event_cmdAmazonTitleResetActionPerformed

  private void cmdGoodreadAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadAuthorResetActionPerformed
      txtGoodreadAuthorUrl.setText(StanzaConstants.GOODREAD_AUTHOR_URL);
  }//GEN-LAST:event_cmdGoodreadAuthorResetActionPerformed

  private void cmdGoodreadIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadIsbnResetActionPerformed
      txtGoodreadIsbnUrl.setText(StanzaConstants.GOODREAD_ISBN_URL);
  }//GEN-LAST:event_cmdGoodreadIsbnResetActionPerformed

  private void cmdGoodreadTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadTitleResetActionPerformed
      txtGoodreadTitleUrl.setText(StanzaConstants.GOODREAD_TITLE_URL);
  }//GEN-LAST:event_cmdGoodreadTitleResetActionPerformed

  private void cmdGoodreadReviewResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadReviewResetActionPerformed
      txtGoodreadReviewIsbnUrl.setText(StanzaConstants.GOODREAD_REVIEW_ISBN_URL);
  }//GEN-LAST:event_cmdGoodreadReviewResetActionPerformed

  private void cmdIsfdbAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdIsfdbAuthorResetActionPerformed
      txtIsfdbAuthorUrl.setText(StanzaConstants.ISFDB_AUTHOR_URL);
  }//GEN-LAST:event_cmdIsfdbAuthorResetActionPerformed

  private void cmdLibrarythingAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingAuthorResetActionPerformed
      txtLibrarythingAuthorUrl.setText(StanzaConstants.LIBRARYTHING_AUTHOR_URL);
  }//GEN-LAST:event_cmdLibrarythingAuthorResetActionPerformed

  private void cmdLibrarythingIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingIsbnResetActionPerformed
      txtLibrarythingIsbnUrl.setText(StanzaConstants.LIBRARYTHING_ISBN_URL);
  }//GEN-LAST:event_cmdLibrarythingIsbnResetActionPerformed

  private void cmdLibrarythingTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingTitleResetActionPerformed
      txtLibrarythingTitleUrl.setText(StanzaConstants.LIBRARYTHING_TITLE_URL);
  }//GEN-LAST:event_cmdLibrarythingTitleResetActionPerformed

  private void lblNoenerateOpdsfile(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_lblNoenerateOpdsfile
      // TODO add your handling code here:
  }//GEN-LAST:event_lblNoenerateOpdsfile

  private void chkNogenerateopdsfilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNogenerateopdsfilesActionPerformed
    // We do not allow HTML Downloads if OPDS Downloads are suppressed
    if (chkNogenerateopdsfiles.isSelected() == true)
    {
        chkNogeneratehtmlfiles.setSelected(true);
        chkNogeneratehtmlfiles.setEnabled(false);
    }
    else
    {
      if (chkNogeneratehtmlfiles.isEnabled() == false)
      {
        chkNogeneratehtmlfiles.setEnabled(true);
        chkNogeneratehtmlfiles.setSelected(false);
      }
    }
  }//GEN-LAST:event_chkNogenerateopdsfilesActionPerformed

  private void cmdSetTargetFolderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSetTargetFolderActionPerformed
    showSetTargetFolderDialog();
  }// GEN-LAST:event_cmdSetTargetFolderActionPerformed

  private void cboLangVetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {// GEN-FIRST:event_cboLangVetoableChange
    changeLanguage();
  }// GEN-LAST:event_cboLangVetoableChange

  private void cboLangActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cboLangActionPerformed
    changeLanguage();
  }// GEN-LAST:event_cboLangActionPerformed

  private void chkDontsplittagsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chkDontsplittagsActionPerformed
    actOnDontsplittagsActionPerformed();
  }// GEN-LAST:event_chkDontsplittagsActionPerformed

  private void cmdResetActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdResetActionPerformed
    resetValues();
  }// GEN-LAST:event_cmdResetActionPerformed

  private void mnuHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuHelpAboutActionPerformed
    about();
  }// GEN-LAST:event_mnuHelpAboutActionPerformed

  private void mnuHelpWikiActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuHelpWikiActionPerformed
    help();
  }// GEN-LAST:event_mnuHelpWikiActionPerformed

  private void mnuHelpDonateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuHelpDonateActionPerformed
    donate();
  }// GEN-LAST:event_mnuHelpDonateActionPerformed

  private void mnuFileSaveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuFileSaveActionPerformed
    saveConfiguration();
  }// GEN-LAST:event_mnuFileSaveActionPerformed

  private void mnuFileExitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuFileExitActionPerformed
    cancelDialog();
  }// GEN-LAST:event_mnuFileExitActionPerformed

  private void mnuFileGenerateCatalogsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuFileGenerateCatalogsActionPerformed
    generateCatalog();
  }// GEN-LAST:event_mnuFileGenerateCatalogsActionPerformed

  private void lblDeviceNookMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_lblDeviceNookMouseClicked
    setDeviceSpecificMode(DeviceMode.Nook);
  }// GEN-LAST:event_lblDeviceNookMouseClicked

  private void lblDeviceDropboxMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_lblDeviceIphoneMouseClicked
    setDeviceSpecificMode(DeviceMode.Dropbox);
  }// GEN-LAST:event_lblDeviceIphoneMouseClicked

  private void lblDonateMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jLabel19MouseClicked
    donate();
  }// GEN-LAST:event_lblDonateMouseClicked

  private void handleMouseClickOnLabel(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_handleMouseClickOnLabel
    popupExplanation((JLabel) evt.getSource());
  }// GEN-LAST:event_handleMouseClickOnLabel

  private void cmdGenerateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdGenerateActionPerformed
    generateCatalog();
  }// GEN-LAST:event_cmdGenerateActionPerformed

  private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSaveActionPerformed
    saveConfiguration();
  }// GEN-LAST:event_cmdSaveActionPerformed

  private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdCancelActionPerformed
    cancelDialog();
  }// GEN-LAST:event_cmdCancelActionPerformed

  private void cmdSetDatabaseFolderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSetDatabaseFolderActionPerformed
    showSetDatabaseFolderDialog();
  }// GEN-LAST:event_cmdSetDatabaseFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboCompatibilityTrick;
    private javax.swing.JComboBox cboLang;
    private javax.swing.JCheckBox chkBrowseByCover;
    private javax.swing.JCheckBox chkBrowseByCoverWithoutSplit;
    private javax.swing.JCheckBox chkCopyToDatabaseFolder;
    private javax.swing.JCheckBox chkCryptFilenames;
    private javax.swing.JCheckBox chkDontsplittags;
    private javax.swing.JCheckBox chkExternalIcons;
    private javax.swing.JCheckBox chkIncludeAboutLink;
    private javax.swing.JCheckBox chkIncludeOnlyOneFile;
    private javax.swing.JCheckBox chkIncludeemptybooks;
    private javax.swing.JCheckBox chkMinimizeChangedFiles;
    private javax.swing.JCheckBox chkNoCoverResize;
    private javax.swing.JCheckBox chkNoGenerateTags;
    private javax.swing.JCheckBox chkNoShowSeries;
    private javax.swing.JCheckBox chkNoSplitInAuthorBooks;
    private javax.swing.JCheckBox chkNoSplitInSeriesBooks;
    private javax.swing.JCheckBox chkNoThumbnailGenerate;
    private javax.swing.JCheckBox chkNobandwidthoptimize;
    private javax.swing.JCheckBox chkNogenerateallbooks;
    private javax.swing.JCheckBox chkNogeneratecrosslinks;
    private javax.swing.JCheckBox chkNogenerateexternallinks;
    private javax.swing.JCheckBox chkNogeneratehtml;
    private javax.swing.JCheckBox chkNogeneratehtmlfiles;
    private javax.swing.JCheckBox chkNogenerateopds;
    private javax.swing.JCheckBox chkNogenerateopdsfiles;
    private javax.swing.JCheckBox chkNogenerateratings;
    private javax.swing.JCheckBox chkOrderAllBooksBySeries;
    private javax.swing.JCheckBox chkPublishedDateAsYear;
    private javax.swing.JCheckBox chkReprocessEpubMetadata;
    private javax.swing.JCheckBox chkSplitByAuthorInitialGoToBooks;
    private javax.swing.JCheckBox chkSupressRatings;
    private javax.swing.JCheckBox chkZipTrookCatalog;
    private javax.swing.JButton cmdAmazonIsbnReset;
    private javax.swing.JButton cmdAmazonTitleReset;
    private javax.swing.JButton cmdAmazonUrlReset;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdGenerate;
    private javax.swing.JButton cmdGoodreadAuthorReset;
    private javax.swing.JButton cmdGoodreadIsbnReset;
    private javax.swing.JButton cmdGoodreadReviewReset;
    private javax.swing.JButton cmdGoodreadTitleReset;
    private javax.swing.JButton cmdIsfdbAuthorReset;
    private javax.swing.JButton cmdLibrarythingAuthorReset;
    private javax.swing.JButton cmdLibrarythingIsbnReset;
    private javax.swing.JButton cmdLibrarythingTitleReset;
    private javax.swing.JButton cmdReset;
    private javax.swing.JButton cmdSave;
    private javax.swing.JButton cmdSetDatabaseFolder;
    private javax.swing.JButton cmdSetTargetFolder;
    private javax.swing.JButton cmdWikipediaUrlReset;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JLabel lblAmazonAuthorUrl;
    private javax.swing.JLabel lblAmazonIsbnUrl;
    private javax.swing.JLabel lblAmazonTitleUrl;
    private javax.swing.JLabel lblBookLanguageTag;
    private javax.swing.JLabel lblBooksinrecent;
    private javax.swing.JLabel lblBottom0;
    private javax.swing.JLabel lblBrowseByCover;
    private javax.swing.JLabel lblBrowseByCoverWithoutSplit;
    private javax.swing.JLabel lblCatalogFolder;
    private javax.swing.JLabel lblCatalogTitle;
    private javax.swing.JLabel lblCompatibilityTrick;
    private javax.swing.JLabel lblCopyToDatabaseFolder;
    private javax.swing.JLabel lblCoverHeight;
    private javax.swing.JLabel lblCryptFilenames;
    private javax.swing.JLabel lblDatabaseFolder;
    private javax.swing.JLabel lblDeviceDropbox;
    private javax.swing.JLabel lblDeviceMode1;
    private javax.swing.JLabel lblDeviceMode2;
    private javax.swing.JLabel lblDeviceNAS;
    private javax.swing.JLabel lblDeviceNook;
    private javax.swing.JLabel lblDonate;
    private javax.swing.JLabel lblExternalIcons;
    private javax.swing.JLabel lblGoodreadAuthorUrl;
    private javax.swing.JLabel lblGoodreadIsbnUrl;
    private javax.swing.JLabel lblGoodreadReviewIsbnUrl;
    private javax.swing.JLabel lblGoodreadTitleUrl;
    private javax.swing.JLabel lblIncludeAboutLink;
    private javax.swing.JLabel lblIncludeOnlyOneFile;
    private javax.swing.JLabel lblIncludeemptybooks;
    private javax.swing.JLabel lblIncludeformat;
    private javax.swing.JLabel lblIsfdbAuthorUrl;
    private javax.swing.JLabel lblLang;
    private javax.swing.JLabel lblLibrarythingAuthorUrl;
    private javax.swing.JLabel lblLibrarythingIsbnUrl;
    private javax.swing.JLabel lblLibrarythingTitleUrl;
    private javax.swing.JLabel lblMaxMobileResolution;
    private javax.swing.JLabel lblMaxbeforepaginate;
    private javax.swing.JLabel lblMaxbeforesplit;
    private javax.swing.JLabel lblMaxsummarylength;
    private javax.swing.JLabel lblMinBooksToMakeDeepLevel;
    private javax.swing.JLabel lblMinimizeChangedFiles;
    private javax.swing.JLabel lblNoCoverResize;
    private javax.swing.JLabel lblNoGenerateTags;
    private javax.swing.JLabel lblNoShowSeries;
    private javax.swing.JLabel lblNoSplitInAuthorBooks;
    private javax.swing.JLabel lblNoSplitInSeriesBooks;
    private javax.swing.JLabel lblNoThumbnailGenerate;
    private javax.swing.JLabel lblNobandwidthoptimize;
    private javax.swing.JLabel lblNogenerateallbooks;
    private javax.swing.JLabel lblNogeneratecrosslinks;
    private javax.swing.JLabel lblNogenerateexternallinks;
    private javax.swing.JLabel lblNogeneratehtml;
    private javax.swing.JLabel lblNogeneratehtmlfiles;
    private javax.swing.JLabel lblNogenerateopds;
    private javax.swing.JLabel lblNogenerateopdsfiles;
    private javax.swing.JLabel lblNogenerateratings;
    private javax.swing.JLabel lblOrderAllBooksBySeries;
    private javax.swing.JLabel lblPublishedDateAsYear;
    private javax.swing.JLabel lblReprocessEpubMetadata;
    private javax.swing.JLabel lblSplitByAuthorInitialGoToBooks;
    private javax.swing.JLabel lblSplittagson;
    private javax.swing.JLabel lblSupressRatings;
    private javax.swing.JLabel lblTagsToMakeDeep;
    private javax.swing.JLabel lblTagstoexclude;
    private javax.swing.JLabel lblTagstogenerate;
    private javax.swing.JLabel lblTargetFolder;
    private javax.swing.JLabel lblThumbnailheight;
    private javax.swing.JLabel lblWikilang;
    private javax.swing.JLabel lblWikipediaUrl;
    private javax.swing.JLabel lblZipTrookCatalog;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenuItem mnuFileExit;
    private javax.swing.JMenuItem mnuFileGenerateCatalogs;
    private javax.swing.JMenuItem mnuFileSave;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuHelpAbout;
    private javax.swing.JMenuItem mnuHelpDonate;
    private javax.swing.JMenuItem mnuHelpOpenLog;
    private javax.swing.JMenuItem mnuHelpOpenSupport;
    private javax.swing.JMenuItem mnuHelpWiki;
    private javax.swing.JMenu mnuProfiles;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JMenuItem mnuToolsprocessEpubMetadataOfAllBooks;
    private javax.swing.JPanel pnlAdvancedOptions;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlExternalUrlsOptions;
    private javax.swing.JPanel pnlGenerationOptions;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlMainOptions;
    private javax.swing.JPanel pnlSplitTagsOn;
    private javax.swing.JPanel pnlTitle;
    private javax.swing.JTabbedPane tabOptionsTabs;
    private javax.swing.JTextField txtAmazonAuthorUrl;
    private javax.swing.JTextField txtAmazonIsbnUrl;
    private javax.swing.JTextField txtAmazonTitleUrl;
    private javax.swing.JTextField txtBookLanguageTag;
    private javax.swing.JTextField txtBooksinrecent;
    private javax.swing.JTextField txtCatalogFolder;
    private javax.swing.JTextField txtCatalogTitle;
    private javax.swing.JTextField txtCoverHeight;
    private javax.swing.JTextField txtDatabaseFolder;
    private javax.swing.JTextField txtGoodreadAuthorUrl;
    private javax.swing.JTextField txtGoodreadIsbnUrl;
    private javax.swing.JTextField txtGoodreadReviewIsbnUrl;
    private javax.swing.JTextField txtGoodreadTitleUrl;
    private javax.swing.JTextField txtIncludeformat;
    private javax.swing.JTextField txtIsfdbAuthorUrl;
    private javax.swing.JTextField txtLibrarythingAuthorUrl;
    private javax.swing.JTextField txtLibrarythingIsbnUrl;
    private javax.swing.JTextField txtLibrarythingTitleUrl;
    private javax.swing.JTextField txtMaxMobileResolution;
    private javax.swing.JTextField txtMaxbeforepaginate;
    private javax.swing.JTextField txtMaxbeforesplit;
    private javax.swing.JTextField txtMaxsummarylength;
    private javax.swing.JTextField txtMinBooksToMakeDeepLevel;
    private javax.swing.JTextField txtSplittagson;
    private javax.swing.JTextField txtTagsToMakeDeep;
    private javax.swing.JTextField txtTagstoexclude;
    private javax.swing.JTextField txtTagstogenerate;
    private javax.swing.JTextField txtTargetFolder;
    private javax.swing.JTextField txtThumbnailheight;
    private javax.swing.JTextField txtWikilang;
    private javax.swing.JTextField txtWikipediaUrl;
    // End of variables declaration//GEN-END:variables

}
