package com.gmail.dpierron.calibre.gui;

/**
 * Handle the main GUI within Calibre2opds
 *
 * Note that the GUI form and this associated java class is constructed
 * and maintained using the Netbeans IDE tool for form design.
 * Althought he java class can be edited from within the IntelliJ IDEA editor
 * you should not make any changes that invalidate its use by the Netbeans one.
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.gui.table.ButtonColumn;
import com.gmail.dpierron.calibre.gui.table.CustomCatalogTableModel;
import com.gmail.dpierron.calibre.opds.Catalog;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.OS;
import com.l2fprod.common.swing.JDirectoryChooser;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Mainframe extends javax.swing.JFrame {
  Logger logger = Logger.getLogger(Mainframe.class);
  GenerateCatalogDialog catalogDialog;
  String language;
  CustomCatalogTableModel customCatalogTableModel = new CustomCatalogTableModel();
  private final Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
  private final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
  private String tabHelpUrl = Constants.HELP_URL_MAIN_OPTIONS;
  // Store this as we use it a lot and it should improve effeciency
  // IMPORTANT:  We need to update this cached copy if the profile ever gets changed!
  private ConfigurationHolder currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();


  /**
   * Creates new form Mainframe
   */
  public Mainframe() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException e) {
      // do nothing
    } catch (InstantiationException e) {
      // do nothing
    } catch (IllegalAccessException e) {
      // do nothing
    } catch (UnsupportedLookAndFeelException e) {
      // do nothing
    }
    if (currentProfile.isObsolete()) {
      currentProfile.reset();
      String msg = Localization.Main.getText("gui.reset.warning");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
    }
    initComponents();
    tabHelpUrl = Constants.HELP_URL_MAIN_OPTIONS;
    loadValues();
    translateTexts();
  }

  /**
   *
   */
  private void addDeleteButtonToCustomCatalogsTable() {
    // add a button to the custom catalogs table
    Action delete = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int modelRow = Integer.valueOf(e.getActionCommand());
        customCatalogTableModel.deleteCustomCatalog(modelRow);
      }
    };

    ButtonColumn buttonColumn = new ButtonColumn(tblCustomCatalogs, delete, 2);
    buttonColumn.setMnemonic(KeyEvent.VK_D);

    int width = 550; //tblCustomCatalogs.getWidth();
    tblCustomCatalogs.getColumnModel().getColumn(0).setPreferredWidth((int)(width * .25));
    tblCustomCatalogs.getColumnModel().getColumn(1).setPreferredWidth((int)(width * .65));
    tblCustomCatalogs.getColumnModel().getColumn(2).setPreferredWidth((int)(width * .075));    // Delete button
  }

  private void processEpubMetadataOfAllBooks() {
    // question
    String message = Localization.Main.getText("gui.confirm.tools.processEpubMetadataOfAllBooks");
    String yes = Localization.Main.getText("boolean.yes");
    String cancel = Localization.Main.getText("boolean.no");
    boolean removeCss = false;
    boolean restoreCss = false;
    File defaultCss = null;
    String onlyForTag = null;
    int result;
    if (ConfigurationManager.INSTANCE.isHacksEnabled()) {
      String yesAndRemoveCss = Localization.Main.getText("gui.confirm.tools.removeCss");
      String yesAndRestoreCss = Localization.Main.getText("gui.confirm.tools.restoreCss");
      result = JOptionPane
          .showOptionDialog(this, message, "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{yes, yesAndRemoveCss, yesAndRestoreCss,
              cancel},
              cancel);
      if (result == 3)
        return;
      removeCss = (result == 1);
      restoreCss = (result == 2);
      if (removeCss || restoreCss) {
        onlyForTag = JOptionPane.showInputDialog(this, "Only for tag (empty for all)");
        if (removeCss) {
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
      }
    } else {
      result =
          JOptionPane.showOptionDialog(this, message, "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{yes, cancel}, cancel);
      if (result == JOptionPane.NO_OPTION)
        return;
    }

    // confirmation
    message = Localization.Main.getText("gui.confirm.tools.processEpubMetadataOfAllBooks2");
    result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION)
      return;
    new ReprocessEpubMetadataDialog(this, true, removeCss, restoreCss, defaultCss, onlyForTag).start();
  }


  private void computeBrowseByCoverWithoutSplitVisibility() {
    boolean visible = chkBrowseByCover.isSelected();
    chkBrowseByCoverWithoutSplit.setVisible(visible);
    lblBrowseByCoverWithoutSplit.setVisible(visible);
  }

  /**
   * Control the valid combinations of gneration types and downloads allowed
   */
  private void checkDownloads() {
    // If we are not generating OPDS files, then the option to
    // set OPDS downloads will follow the HTML setting.  Also the
    // user can no longer toggle the OPDS download option directly.
    if (chkNogenerateopds.isSelected()) {
       chkNogeneratehtmlfiles.setEnabled(chkNogeneratehtml.isEnabled());
       chkNogenerateopdsfiles.setSelected(chkNogeneratehtmlfiles.isSelected());
       chkNogenerateopdsfiles.setEnabled(false);
    } else {
      // If  we are generating OPDS catalogs, then start by assuming that
      // both types of downloads are allowed.
      chkNogenerateopdsfiles.setEnabled(true);
      // If we are not generating OPDS downloads then the HTML downloads
      // must be suppressed.
      if (chkNogenerateopdsfiles.isSelected()) {
        chkNogeneratehtmlfiles.setSelected(true);
        chkNogeneratehtmlfiles.setEnabled(false);
      } else {
        chkNogeneratehtmlfiles.setEnabled(true);
      }
    }
    // If we are not generating HTML catalogs then we cannot
    // set HTML downloads
    if (chkNogeneratehtml.isSelected()) {
      chkNogeneratehtmlfiles.setSelected(true);
      chkNogeneratehtmlfiles.setEnabled(false);
    } else {
      // If we are generating HTHL catalogs then HTML
      // downloads are only allowed if OPDS ones are also active
      // or we are not generating OPDS catalogs
      chkNogeneratehtmlfiles.setEnabled(chkNogenerateopds.isSelected()==true || chkNogenerateopdsfiles.isSelected()==false);
    }
  }

  /**
   * Check if the user is allowed to set the option to only have a
   * catalog at the target location?
   */
  private void checkOnlyCatalogAllowed() {
    // Do nothing at the moment
  }

  private void actOnDontsplittagsActionPerformed() {
    actOnDontsplittagsActionPerformed(chkDontsplittags.isSelected());
  }

  private void actOnDontsplittagsActionPerformed(boolean dontsplit) {
    if (dontsplit)
      txtSplittagson.setText(null);
    txtSplittagson.setEnabled(!dontsplit);
    chkDontsplittags.setSelected(dontsplit);
  }

  private void actOnGenerateIndexActionPerformed() {
    boolean generateIndex = chkGenerateIndex.isSelected();
    lblIndexComments.setVisible(generateIndex);
    chkIndexComments.setVisible(generateIndex);
    lblMaxKeywords.setVisible(generateIndex);
    txtMaxKeywords.setVisible(generateIndex);
    lblIndexFilterAlgorithm.setVisible(generateIndex);
    cboIndexFilterAlgorithm.setVisible(generateIndex);
  }

  private void adaptInterfaceToDeviceSpecificMode(DeviceMode mode) {
    Border RED_BORDER = new LineBorder(Color.red,2);
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
        lblDeviceMode2.setText(Localization.Main.getText("config.DeviceMode.dropbox.description2") + " ");
        lblZipTrookCatalog.setVisible(false);
        chkZipTrookCatalog.setVisible(false);
        break;
    }
    // show all the buttons and commands
    // ITIMPI:  Not quite sure why this needs to be done here?
    cmdCancel.setEnabled(true);
    mnuFileExit.setEnabled(true);
    cmdGenerate.setEnabled(true);
    mnuFileGenerateCatalogs.setEnabled(true);
    cmdReset.setEnabled(true);
    cmdSave.setEnabled(true);
    mnuFileSave.setEnabled(true);
  }

  private void setDeviceSpecificMode(DeviceMode mode) {
    currentProfile.setDeviceMode(mode);
    adaptInterfaceToDeviceSpecificMode(mode);
    loadValues();
  }

  /**
   *  Change the localization to match the language set in the GUI option
   */
  private void changeLanguage() {
    String newLanguage = (String) cboLang.getSelectedItem();
    if (Helper.checkedCompare(language, newLanguage) != 0) {
      currentProfile.setLanguage(newLanguage);
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
    FileDialog fd = new FileDialog(parent, file.getPath());
    fd.setDirectory(file.getPath() +"\\");
    fd.setMode(FileDialog.LOAD);
    fd.setVisible(true);
    // Do nothing with result
    parent.dispose();
  }

  private void debugShowLogFile() {
    File f = new File(ConfigurationManager.INSTANCE.getConfigurationDirectory(), Constants.LOGFILE_FOLDER + "/" + Constants.LOGFILE_NAME);
    logger.info(Localization.Main.getText("gui.menu.tools.logFile") + ": " + f.getPath());
    debugShowFile(f);
  }

  /**
   * Remove any existing log files.
   * This will include any of the following that are present:
   * calibre2opds.log
   * calibre2opds.log.*
   * synclog.log
   * TODO:  There appears to be an issue deleting the current active log file - not sure how to resolve this!
   */
  private void debugClearLogFile() {
    File logFolder = new File(ConfigurationManager.INSTANCE.getConfigurationDirectory(), Constants.LOGFILE_FOLDER);
    File fileList[] = logFolder.listFiles();
    for (File f : fileList) {
      if (f.getName().contains(".log")) {
        f.delete();
        logger.trace("Deleted: " + f);
      }
    }
    String msg = Localization.Main.getText("gui.menu.tools.logCleared");
    logger.info(msg);
    JOptionPane.showMessageDialog(this, msg, "", JOptionPane.INFORMATION_MESSAGE);
  }

  private void debugShowLogFolder() {
    File f = new File(ConfigurationManager.INSTANCE.getConfigurationDirectory(), Constants.LOGFILE_FOLDER);
    logger.info(Localization.Main.getText("gui.menu.tools.logFolder") + ": " + f.getPath());
    debugShowFile(f);
  }

  private void debugShowSupportFolder() {
    File f = ConfigurationManager.INSTANCE.getConfigurationDirectory();
    logger.info(Localization.Main.getText("gui.menu.tools.configFolder") + ": " + f.getPath());
    debugShowFolder(f);
  }

  private void about() {
    logger.info("Displaying About dialog");
    String prog_version = Localization.Main.getText("gui.title", Constants.PROGTITLE + Constants.BZR_VERSION);
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
    String message =
        "<html>" + prog_version + "<br><br>" + intro_goal + "<br><br>" + intro_wiki_title + intro_wiki_url + "<br><br>" + intro_team_title + "<br><ul>" + "<li>" + intro_team_list1 + "<li>" +
            intro_team_list2 + "<li>" + intro_team_list3 + "<li>" + intro_team_list4 + "</ul><br>" + intro_thanks_1 + "<br>" + intro_thanks_2 + "<br><br></html>";
    JOptionPane.showMessageDialog(this, message, Localization.Main.getText("gui.menu.help.about"), JOptionPane.INFORMATION_MESSAGE);
  }

  private void donate() {
    String message = Localization.Main.getText("gui.confirm.donate");
    int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
    if (result != JOptionPane.YES_OPTION)
      return;
    logger.info(Localization.Main.getText("gui.menu.help.donate") + ": " + Constants.PAYPAL_DONATION);
    BareBonesBrowserLaunch.openURL(Constants.PAYPAL_DONATION);
  }

  /**
   * Control the generation of the catalog
   * displaying the progress as it goes
   */
  private void generateCatalog() {

    storeValues();

    catalogDialog = new GenerateCatalogDialog(this, true);
    final Catalog catalog = new Catalog(catalogDialog);
    Runnable runnable = new Runnable() {
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
    try {
      catalogDialog.setVisible(true);
      catalogDialog.pack();
    } catch (Exception e) {
      // ITIMPI:  This is a brute force fix to #772538
      //          which has only been reproduced on OS/X so far
      //          Maybe a tidier fix could be implemented?
      // Do nothing
    }
  }

  /**
   * Catalog generation completed so close down generation dialog
   * If necessary display the reason
   * @param e
   */
  synchronized void catalogEnded(final Exception e) {
    if (catalogDialog == null)
      return;

    catalogDialog.setVisible(false);
    catalogDialog = null;
    if (e != null) {
      Runnable runnable = new Runnable() {
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

  /**
   * Set a new profile
   * @param profileName
   */
  private void setProfile(String profileName) {
    ConfigurationManager.INSTANCE.changeProfile(profileName);
    // Changed profile - so need to update local cached copy as well!
    currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
    if (currentProfile.isObsolete()) {
      currentProfile.reset();
      String msg = Localization.Main.getText("gui.reset.warning");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
    }
    logger.info(Localization.Main.getText("info.loadProfile", profileName));
    customCatalogTableModel.reset();
    loadValues();
  }

  public void saveNewProfile() {
    String newProfileName = JOptionPane.showInputDialog(Localization.Main.getText("gui.profile.new.msg"));
    if ("default".equalsIgnoreCase(newProfileName))
      return;
    ConfigurationManager.INSTANCE.copyCurrentProfile(newProfileName);
    // Changed profile - so need to update local cached copy pointer as well!
    currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();
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
   *  We want the 'enabled's tate of the fields for specifying the URL's for
   *  external links to be anbled/disabled according to whether we are
   *  allowing the generation of this sort of link
   */
  private void setExternalLinksEnabledState() {
    boolean enabledNoExternalLinks = chkNogenerateexternallinks.isSelected();
    boolean derivedState;

    derivedState = ! (enabledNoExternalLinks || currentProfile.isWikipediaUrlReadOnly());
    txtWikipediaUrl.setEnabled(derivedState);
    lblWikipediaUrl.setEnabled(derivedState);
    cmdWikipediaUrlReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonAuthorUrlReadOnly());
    txtAmazonAuthorUrl.setEnabled(derivedState);
    lblAmazonAuthorUrl.setEnabled(derivedState);
    cmdAmazonUrlReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonIsbnUrlReadOnly());
    txtAmazonIsbnUrl.setEnabled(derivedState);
    lblAmazonIsbnUrl.setEnabled(derivedState);
    cmdAmazonIsbnReset.setEnabled(derivedState);
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonTitleUrlReadOnly());
    txtAmazonTitleUrl.setEnabled(derivedState);
    lblAmazonTitleUrl.setEnabled(derivedState);
    cmdAmazonTitleReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadAuthorUrlReadOnly());
    txtGoodreadAuthorUrl.setEnabled(derivedState);
    lblGoodreadAuthorUrl.setEnabled(derivedState);
    cmdGoodreadAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadIsbnUrlReadOnly());
    txtGoodreadIsbnUrl.setEnabled(derivedState);
    lblGoodreadIsbnUrl.setEnabled(derivedState);
    cmdGoodreadIsbnReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadTitleUrlReadOnly());
    txtGoodreadTitleUrl.setEnabled(derivedState);
    lblGoodreadTitleUrl.setEnabled(derivedState);
    cmdGoodreadTitleReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadReviewIsbnUrlReadOnly());
    txtGoodreadReviewIsbnUrl.setEnabled(derivedState);
    lblGoodreadReviewIsbnUrl.setEnabled(derivedState);
    cmdGoodreadReviewReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isIsfdbAuthorUrlReadOnly());
    txtIsfdbAuthorUrl.setEnabled(derivedState);
    lblIsfdbAuthorUrl.setEnabled(derivedState);
    cmdIsfdbAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingAuthorUrlReadOnly());
    txtLibrarythingAuthorUrl.setEnabled(derivedState);
    lblLibrarythingAuthorUrl.setEnabled(derivedState);
    cmdLibrarythingAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingIsbnUrlReadOnly());
    txtLibrarythingIsbnUrl.setEnabled(derivedState);
    lblLibrarythingIsbnUrl.setEnabled(derivedState);
    cmdLibrarythingIsbnReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingTitleUrlReadOnly());
    txtLibrarythingTitleUrl.setEnabled(derivedState);
    lblLibrarythingTitleUrl.setEnabled(derivedState);
    cmdLibrarythingTitleReset.setEnabled((derivedState));
  }

  /**
   * Helper function to set up a checkbox and associated label given its base name
   * @param classNameBase
   */
  private void setCheckBox(String classNameBase) {
    String chkName = "chk" + classNameBase;
    String lblName = "lbl" + classNameBase;
    String getName = "get" + classNameBase;
    String roName = "is" + classNameBase + "ReadOnly";
    chkCopyToDatabaseFolder.setSelected(currentProfile.getCopyToDatabaseFolder());
    chkCopyToDatabaseFolder.setEnabled(!currentProfile.isCopyToDatabaseFolderReadOnly());
    lblCopyToDatabaseFolder.setEnabled(chkCopyToDatabaseFolder.isEnabled());

  }
  /**
   * Load values for configuration into GUI
   * Also enable/disable any fields according to current values if required
   */
  private void loadValues() {
    InputVerifier iv = new InputVerifier() {

      @Override
      public boolean verify(JComponent input) {
        if (!(input instanceof JTextField))
          return false;
        try {
          Integer.parseInt(((JTextField) input).getText());
          return true;
        } catch (NumberFormatException e) {
          return false;
        }
      }
    };

    cboLang.setModel(new DefaultComboBoxModel(LocalizationHelper.INSTANCE.getAvailableLocalizations()));
    cboLang.setSelectedItem(currentProfile.getLanguage());
    lblCurrentProfile.setText(Localization.Main.getText("config.profile.label", ConfigurationManager.INSTANCE.getCurrentProfileName() + "       "));
    lblCurrentProfile.setFont(lblCurrentProfile.getFont().deriveFont(Font.BOLD));

    lblCurrentProfile.setToolTipText(Localization.Main.getText("config.profile.description"));
    File f = currentProfile.getDatabaseFolder();
    if (f == null || !f.exists())
      f = new File(".");
    txtDatabaseFolder.setText(f.getAbsolutePath());
    cmdSetDatabaseFolder.setEnabled(!currentProfile.isDatabaseFolderReadOnly());
    txtDatabaseFolder.setEnabled(!currentProfile.isDatabaseFolderReadOnly());
    lblDatabaseFolder.setEnabled(!currentProfile.isDatabaseFolderReadOnly());

    f = currentProfile.getTargetFolder();
    if (f == null || !f.exists())
      txtTargetFolder.setText("");
    else
      txtTargetFolder.setText(f.getAbsolutePath());
    cmdSetTargetFolder.setEnabled(!currentProfile.isTargetFolderReadOnly());
    txtTargetFolder.setEnabled(!currentProfile.isTargetFolderReadOnly());
    lblTargetFolder.setEnabled(!currentProfile.isTargetFolderReadOnly());
    // c2o-77 Ensure that Target Folder cannot be entered in default mode
    txtTargetFolder.setEnabled(currentProfile.getDeviceMode() != DeviceMode.Dropbox);
    cmdSetTargetFolder.setEnabled(txtTargetFolder.isEnabled());
    chkCopyToDatabaseFolder.setSelected(currentProfile.getCopyToDatabaseFolder());
    chkCopyToDatabaseFolder.setEnabled(!currentProfile.isCopyToDatabaseFolderReadOnly());
    lblCopyToDatabaseFolder.setEnabled(chkCopyToDatabaseFolder.isEnabled());
    chkOnlyCatalogAtTarget.setSelected(currentProfile.getOnlyCatalogAtTarget());
    chkOnlyCatalogAtTarget.setEnabled((!currentProfile.isOnlyCatalogAtTargetReadOnly()));
    lblOnlyCatalogAtTarget.setEnabled(chkOnlyCatalogAtTarget.isEnabled());
    chkReprocessEpubMetadata.setSelected(currentProfile.getReprocessEpubMetadata());
    chkReprocessEpubMetadata.setEnabled(!currentProfile.isReprocessEpubMetadataReadOnly());
    lblReprocessEpubMetadata.setEnabled(chkReprocessEpubMetadata.isEnabled());
    txtWikilang.setText(currentProfile.getWikipediaLanguage());
    txtWikilang.setEnabled(!currentProfile.isWikipediaLanguageReadOnly());
    lblWikilang.setEnabled(txtWikilang.isEnabled());
    txtCatalogFolder.setText(currentProfile.getCatalogFolderName());
    txtCatalogFolder.setEnabled(!currentProfile.isCatalogFolderNameReadOnly());
    lblCatalogFolder.setEnabled(txtCatalogFolder.isEnabled());
    txtUrlBooks.setText(currentProfile.getUrlBooks());
    txtUrlBooks.setEnabled(!currentProfile.isUrlBooksReadOnly());
    lblUrlBooks.setEnabled(txtUrlBooks.isEnabled());
    txtCatalogTitle.setText(currentProfile.getCatalogTitle());
    txtCatalogTitle.setEnabled(!currentProfile.isCatalogTitleReadOnly());
    lblCatalogTitle.setEnabled(txtCatalogTitle.isEnabled());
    chkLanguageAsTag.setSelected(currentProfile.getLanguageAsTag());
    chkLanguageAsTag.setEnabled(!currentProfile.isLanguageAsTagReadOnly());
    lblLanguageAsTag.setEnabled(chkLanguageAsTag.isEnabled());
    chkNoThumbnailGenerate.setSelected(!currentProfile.getThumbnailGenerate());
    chkNoThumbnailGenerate.setEnabled(!currentProfile.isThumbnailGenerateReadOnly());
    lblNoThumbnailGenerate.setEnabled(chkNoThumbnailGenerate.isEnabled());
    txtThumbnailheight.setText("" + currentProfile.getThumbnailHeight());
    txtThumbnailheight.setInputVerifier(iv);
    txtThumbnailheight.setEnabled(!currentProfile.isThumbnailHeightReadOnly());
    lblThumbnailheight.setEnabled(txtThumbnailheight.isEnabled());
    chkNoCoverResize.setSelected(!currentProfile.getCoverResize());
    chkNoCoverResize.setEnabled(!currentProfile.isCoverResizeReadOnly());
    lblNoCoverResize.setEnabled(chkNoCoverResize.isEnabled());
    txtCoverHeight.setText("" + currentProfile.getCoverHeight());
    txtCoverHeight.setInputVerifier(iv);
    txtCoverHeight.setEnabled(!currentProfile.isCoverHeightReadOnly());
    lblCoverHeight.setEnabled(!currentProfile.isCoverHeightReadOnly());
    txtIncludeformat.setText(currentProfile.getIncludedFormatsList());
    txtIncludeformat.setEnabled(!currentProfile.isIncludedFormatsListReadOnly());
    lblIncludeformat.setEnabled(!currentProfile.isIncludedFormatsListReadOnly());
    txtMaxbeforepaginate.setText("" + currentProfile.getMaxBeforePaginate());
    txtMaxbeforepaginate.setInputVerifier(iv);
    txtMaxbeforepaginate.setEnabled(!currentProfile.isMaxBeforePaginateReadOnly());
    lblMaxbeforepaginate.setEnabled(!currentProfile.isMaxBeforePaginateReadOnly());
    txtMaxbeforesplit.setText("" + currentProfile.getMaxBeforeSplit());
    txtMaxbeforesplit.setInputVerifier(iv);
    txtMaxbeforesplit.setEnabled(!currentProfile.isMaxBeforeSplitReadOnly());
    lblMaxbeforesplit.setEnabled(!currentProfile.isMaxBeforeSplitReadOnly());
    txtMaxSplitLevels.setText("" + currentProfile.getMaxSplitLevels());
    txtMaxSplitLevels.setInputVerifier(iv);
    txtMaxSplitLevels.setEnabled(!currentProfile.isMaxSplitLevelsReadOnly());
    lblMaxSplitLevels.setEnabled(!currentProfile.isMaxSplitLevelsReadOnly());
    txtBooksinrecent.setText("" + currentProfile.getBooksInRecentAdditions());
    txtBooksinrecent.setInputVerifier(iv);
    txtBooksinrecent.setEnabled(!currentProfile.isBooksInRecentAdditionsReadOnly());
    lblBooksinrecent.setEnabled(!currentProfile.isBooksInRecentAdditionsReadOnly());
    txtMaxsummarylength.setText("" + currentProfile.getMaxSummaryLength());
    txtMaxsummarylength.setInputVerifier(iv);
    txtMaxsummarylength.setEnabled(!currentProfile.isMaxSummaryLengthReadOnly());
    lblMaxBookSummaryLength.setEnabled(!currentProfile.isMaxBookSummaryLengthReadOnly());
    txtMaxBookSummaryLength.setText("" + currentProfile.getMaxBookSummaryLength());
    txtMaxBookSummaryLength.setInputVerifier(iv);
    txtMaxBookSummaryLength.setEnabled(!currentProfile.isMaxBookSummaryLengthReadOnly());
    lblMaxBookSummaryLength.setEnabled(!currentProfile.isMaxBookSummaryLengthReadOnly());
    txtSplittagson.setText(currentProfile.getSplitTagsOn());
    txtSplittagson.setEnabled(!currentProfile.isSplitTagsOnReadOnly());
    lblSplittagson.setEnabled(!currentProfile.isSplitTagsOnReadOnly());
    chkDontsplittags.setEnabled(!currentProfile.isSplitTagsOnReadOnly());
    actOnDontsplittagsActionPerformed(Helper.isNullOrEmpty(currentProfile.getSplitTagsOn()));
    chkIncludeemptybooks.setSelected(currentProfile.getIncludeBooksWithNoFile());
    chkIncludeemptybooks.setEnabled(!currentProfile.isIncludeBooksWithNoFileReadOnly());
    lblIncludeemptybooks.setEnabled(!currentProfile.isIncludeBooksWithNoFileReadOnly());
    chkIncludeOnlyOneFile.setSelected(currentProfile.getIncludeOnlyOneFile());
    chkIncludeOnlyOneFile.setEnabled(!currentProfile.isIncludeOnlyOneFileReadOnly());
    lblIncludeOnlyOneFile.setEnabled(!currentProfile.isIncludeOnlyOneFileReadOnly());
    chkNogenerateopds.setSelected(!currentProfile.getGenerateOpds());
    chkNogenerateopds.setEnabled(!currentProfile.isGenerateOpdsReadOnly());
    lblNogenerateopds.setEnabled(!currentProfile.isGenerateOpdsReadOnly());
    chkNogeneratehtml.setSelected(!currentProfile.getGenerateHtml());
    chkNogeneratehtml.setEnabled(!currentProfile.isGenerateHtmlReadOnly());
    lblNogeneratehtml.setEnabled(!currentProfile.isGenerateHtmlReadOnly());
    chkMinimizeChangedFiles.setSelected(currentProfile.getMinimizeChangedFiles());
    chkMinimizeChangedFiles.setEnabled(!currentProfile.isMinimizeChangedFilesReadOnly());
    lblMinimizeChangedFiles.setEnabled(!currentProfile.isMinimizeChangedFilesReadOnly());
    chkExternalIcons.setSelected(currentProfile.getExternalIcons());
    chkExternalIcons.setEnabled(!currentProfile.isExternalIconsReadOnly());
    lblExternalIcons.setEnabled(!currentProfile.isExternalIconsReadOnly());
    lblNogeneratehtmlfiles.setEnabled(!currentProfile.isGenerateHtmlDownloadsReadOnly());
    chkSupressRatings.setSelected(currentProfile.getSuppressRatingsInTitles());
    chkSupressRatings.setEnabled(!currentProfile.isSupressRatingsInTitlesReadyOnly());
    lblSupressRatings.setEnabled(!currentProfile.isSupressRatingsInTitlesReadyOnly());
    chkBrowseByCover.setSelected(currentProfile.getBrowseByCover());
    chkBrowseByCover.setEnabled(!currentProfile.isBrowseByCoverReadOnly());
    lblBrowseByCover.setEnabled(!currentProfile.isBrowseByCoverReadOnly());
    chkBrowseByCoverWithoutSplit.setSelected(currentProfile.getBrowseByCoverWithoutSplit());
    chkBrowseByCoverWithoutSplit.setEnabled(!currentProfile.isBrowseByCoverWithoutSplitReadOnly());
    lblBrowseByCoverWithoutSplit.setEnabled(!currentProfile.isBrowseByCoverWithoutSplitReadOnly());
    chkNoIncludeAboutLink.setSelected(!currentProfile.getIncludeAboutLink());
    chkNoIncludeAboutLink.setEnabled(!currentProfile.isIncludeAboutLinkReadOnly());
    lblNoIncludeAboutLink.setEnabled(!currentProfile.isIncludeAboutLinkReadOnly());
    chkZipTrookCatalog.setSelected(currentProfile.getZipTrookCatalog());
    chkZipTrookCatalog.setEnabled(!currentProfile.isZipTrookCatalogReadOnly());
    lblZipTrookCatalog.setEnabled(!currentProfile.isZipTrookCatalogReadOnly());

    chkIndexComments.setSelected(currentProfile.getIndexComments());
    chkIndexComments.setEnabled(!currentProfile.isIndexCommentsReadOnly());
    lblIndexComments.setEnabled(!currentProfile.isIndexCommentsReadOnly());
    txtMaxKeywords.setText("" + currentProfile.getMaxKeywords());
    txtMaxKeywords.setEnabled(!currentProfile.isMaxKeywordsReadOnly());
    lblMaxKeywords.setEnabled(!currentProfile.isMaxKeywordsReadOnly());
    cboIndexFilterAlgorithm.setModel(new DefaultComboBoxModel(Index.FilterHintType.values()));
    cboIndexFilterAlgorithm.setSelectedItem(currentProfile.getIndexFilterAlgorithm());
    cboIndexFilterAlgorithm.setEnabled(!currentProfile.isIndexFilterAlgorithmReadOnly());
    lblIndexFilterAlgorithm.setEnabled(!currentProfile.isIndexFilterAlgorithmReadOnly());
    chkGenerateIndex.setSelected(currentProfile.getGenerateIndex());
    chkGenerateIndex.setEnabled(!currentProfile.isGenerateIndexReadOnly());
    lblGenerateIndex.setEnabled(!currentProfile.isGenerateIndexReadOnly());
    actOnGenerateIndexActionPerformed();

    chkNogenerateopdsfiles.setSelected(!currentProfile.getGenerateDownloads());
    chkNogenerateopdsfiles.setEnabled(!currentProfile.isGenerateDownloadsReadOnly());
    lblNogenerateopdsfiles.setEnabled(chkNogenerateopdsfiles.isEnabled());
    chkCryptFilenames.setSelected(currentProfile.getCryptFilenames());
    chkCryptFilenames.setEnabled(!currentProfile.isCryptFilenamesReadOnly());
    lblCryptFilenames.setEnabled(chkCryptFilenames.isEnabled());
    chkNoShowSeries.setSelected(!currentProfile.getShowSeriesInAuthorCatalog());
    chkNoShowSeries.setEnabled(!currentProfile.isShowSeriesInAuthorCatalogReadOnly());
    lblNoShowSeries.setEnabled(chkNoShowSeries.isEnabled());
    chkOrderAllBooksBySeries.setSelected(currentProfile.getOrderAllBooksBySeries());
    chkOrderAllBooksBySeries.setEnabled(!currentProfile.isOrderAllBooksBySeriesReadOnly());
    lblOrderAllBooksBySeries.setEnabled(chkOrderAllBooksBySeries.isEnabled());
    chkSplitByAuthorInitialGoToBooks.setSelected(currentProfile.getSplitByAuthorInitialGoToBooks());
    chkSplitByAuthorInitialGoToBooks.setEnabled(!currentProfile.isSplitByAuthorInitialGoToBooksReadOnly());
    lblSplitByAuthorInitialGoToBooks.setEnabled(chkSplitByAuthorInitialGoToBooks.isEnabled());
    txtCatalogFilter.setText("" + currentProfile.getCatalogFilter());
    txtCatalogFilter.setEnabled(!currentProfile.isCatalogFilterReadOnly());
    lblCatalogFilter.setEnabled(txtCatalogFilter.isEnabled());
    chkNoGenerateAuthors.setSelected(!currentProfile.getGenerateAuthors());
    chkNoGenerateAuthors.setEnabled(!currentProfile.isGenerateAuthorsReadOnly());
    lblNoGenerateAuthors.setEnabled(chkNoGenerateAuthors.isEnabled());
    chkNoGenerateTags.setSelected(!currentProfile.getGenerateTags());
    chkNoGenerateTags.setEnabled(!currentProfile.isGenerateTagsReadOnly());
    lblNoGenerateTags.setEnabled(chkNoGenerateTags.isEnabled());
    txtTagsToIgnore.setText("" + currentProfile.getTagsToIgnore());
    txtTagsToIgnore.setEnabled(!currentProfile.isTagsToIgnoreReadOnly());
    lblTagsToIgnore.setEnabled(txtTagsToIgnore.isEnabled());
    txtTagsToMakeDeep.setText("" + currentProfile.getTagsToMakeDeep());
    txtTagsToMakeDeep.setEnabled(!currentProfile.isTagsToMakeDeepReadOnly());
    lblTagsToMakeDeep.setEnabled(txtTagsToMakeDeep.isEnabled());
    txtTagsToIgnore.setEnabled(false);  // TODO enable when code ready
    lblTagsToIgnore.setEnabled(false);  // TODO enable when code ready
    chkNoGenerateSeries.setSelected(!currentProfile.getGenerateSeries());
    chkNoGenerateSeries.setEnabled(!currentProfile.isGenerateSeriesReadOnly());
    lblNoGenerateSeries.setEnabled(chkNoGenerateSeries.isEnabled());
    chkNogeneraterecent.setSelected(!currentProfile.getGenerateRecent());
    chkNogeneraterecent.setEnabled(!currentProfile.isGenerateRecentReadOnly());
    lblNogeneraterecent.setEnabled(chkNogeneraterecent.isEnabled());
    chkNogenerateratings.setSelected(!currentProfile.getGenerateRatings());
    chkNogenerateratings.setEnabled(!currentProfile.isGenerateRatingsReadOnly());
    lblNogenerateratings.setEnabled(chkNogenerateratings.isEnabled());
    chkNogenerateallbooks.setSelected(!currentProfile.getGenerateAllbooks());
    chkNogenerateallbooks.setEnabled(!currentProfile.isGenerateAllbooksReadOnly());
    lblNogenerateallbooks.setEnabled(chkNogenerateallbooks.isEnabled());
    chkDisplayAuthorSortInAuthorLists.setSelected(currentProfile.getDisplayAuthorSortInAuthorLists());
    chkDisplayAuthorSortInAuthorLists.setEnabled(!currentProfile.isDisplayAuthorSortInAuthorListsReadOnly());
    lblDisplayAuthorSortInAuthorLists.setEnabled(chkDisplayAuthorSortInAuthorLists.isEnabled());
    chkDisplayTitleSortInBookLists.setSelected(currentProfile.getDisplayTitleSortInBookLists());
    chkDisplayTitleSortInBookLists.setEnabled(!currentProfile.isDisplayTitleSortInBookListsReadOnly());
    lblDisplayTitleSortInBookLists.setEnabled(chkDisplayTitleSortInBookLists.isEnabled());
    chkSortUsingAuthorSort.setSelected(currentProfile.getSortUsingAuthor());
    chkSortUsingAuthorSort.setEnabled(!currentProfile.isSortUsingAuthorReadOnly());
    lblSortUsingAuthor.setEnabled(chkSortUsingAuthorSort.isEnabled());
    chkSortUsingTitleSort.setSelected(currentProfile.getSortUsingTitle());
    chkSortUsingTitleSort.setEnabled(!currentProfile.isSortUsingTitleReadOnly());
    lblSortUsingTitle.setEnabled(chkSortUsingTitleSort.isEnabled());

    // Book Details

    chkIncludeSeriesInBookDetails.setSelected(currentProfile.getIncludeSeriesInBookDetails());
    chkIncludeSeriesInBookDetails.setEnabled(!currentProfile.isIncludeSeriesInBookDetailsReadOnly());
    lblIncludeSeriesInBookDetails.setEnabled(!currentProfile.isIncludeSeriesInBookDetailsReadOnly());
    chkIncludeRatingInBookDetails1.setSelected(currentProfile.getIncludeRatingInBookDetails());
    chkIncludeRatingInBookDetails1.setEnabled(!currentProfile.isIncludeRatingInBookDetailsReadOnly());
    lblIncludeRatingInBookDetails.setEnabled(!currentProfile.isIncludeRatingInBookDetailsReadOnly());
    chkIncludeTagsInBookDetails.setSelected(currentProfile.getIncludeTagsInBookDetails());
    chkIncludeTagsInBookDetails.setEnabled(!currentProfile.isIncludeTagsInBookDetailsReadOnly());
    lblIncludeTagsInBookDetails.setEnabled(!currentProfile.isIncludeTagsInBookDetailsReadOnly());
    chkIncludePublisherInBookDetails.setSelected(currentProfile.getIncludePublisherInBookDetails());
    chkIncludePublisherInBookDetails.setEnabled(!currentProfile.isIncludePublisherInBookDetailsReadOnly());
    lblIncludePublisherInBookDetails.setEnabled(!currentProfile.isIncludePublisherInBookDetailsReadOnly());
    chkIncludePublishedInBookDetails.setSelected(currentProfile.getIncludePublishedInBookDetails());
    chkIncludePublishedInBookDetails.setEnabled(!currentProfile.isIncludePublishedInBookDetailsReadOnly());
    lblIncludePublishedInBookDetails.setEnabled(!currentProfile.isIncludePublishedInBookDetailsReadOnly());
    chkPublishedDateAsYear.setSelected(currentProfile.getPublishedDateAsYear());
    chkPublishedDateAsYear.setEnabled(!currentProfile.isPublishedDateAsYearReadOnly());
    lblPublishedDateAsYear.setEnabled(!currentProfile.isPublishedDateAsYearReadOnly());
    chkIncludeAddednBookDetails1.setSelected(currentProfile.getIncludeAddedInBookDetails());
    chkIncludeAddednBookDetails1.setEnabled(!currentProfile.isIncludeAddedInBookDetailsReadOnly());
    lblIncludeAddedInBookDetails2.setEnabled(!currentProfile.isIncludeAddedInBookDetailsReadOnly());
    chkIncludeModifiedInBookDetails.setSelected(currentProfile.getIncludeModifiedInBookDetails());
    chkIncludeModifiedInBookDetails.setEnabled(!currentProfile.isIncludeModifiedInBookDetailsReadOnly());
    lblIncludeModifiedInBookDetails1.setEnabled(!currentProfile.isIncludeModifiedInBookDetailsReadOnly());
    chkDisplayAuthorSortInBookDetails.setSelected(currentProfile.getDisplayAuthorSortInBookDetails());
    chkDisplayAuthorSortInBookDetails.setEnabled(!currentProfile.isDisplayAuthorSortInBookDetailsReadOnly());
    lblDisplayAuthorSortInBookDetails.setEnabled(!currentProfile.isDisplayAuthorSortInBookDetailsReadOnly());
    chkDisplayTitleSortInBookDetails.setSelected(currentProfile.getDisplayTitleSortInBookDetails());
    chkDisplayTitleSortInBookDetails.setEnabled(!currentProfile.isDisplayTitleSortInBookDetailsReadOnly());
    lblDisplayTitleSortInBookDetails.setEnabled(!currentProfile.isDisplayTitleSortInBookDetailsReadOnly());
    txtBookDetailsCustomFields.setText(currentProfile.getBookDetailsCustomFields());
    txtBookDetailsCustomFields.setEnabled(!currentProfile.isBookDetailsCustomFieldsReadOnly());
    lblBookDetailsCustomFields.setEnabled(txtBookDetailsCustomFields.isEnabled());
    chkNogeneratecrosslinks.setSelected(!currentProfile.getGenerateCrossLinks());
    chkNogeneratecrosslinks.setEnabled(!currentProfile.isGenerateCrossLinksReadOnly());
    lblNogeneratecrosslinks.setEnabled(chkNogeneratecrosslinks.isEnabled());
    chkIncludeTagCrossReferences.setSelected(currentProfile.getIncludeTagCrossReferences());
    chkIncludeTagCrossReferences.setEnabled(!currentProfile.isIncludeTagCrossReferencesReadOnly());
    lblIncludeTagCrossReferences.setEnabled(chkIncludeTagCrossReferences.isEnabled());
    chkNogenerateexternallinks.setSelected(!currentProfile.getGenerateExternalLinks());
    chkNogenerateexternallinks.setEnabled(!currentProfile.isGenerateExternalLinksReadOnly());
    lblNogenerateexternallinks.setEnabled(chkNogenerateexternallinks.isEnabled());

    // Advanced

    txtMinBooksToMakeDeepLevel.setText("" + currentProfile.getMinBooksToMakeDeepLevel());
    txtMinBooksToMakeDeepLevel.setInputVerifier(iv);
    txtMinBooksToMakeDeepLevel.setEnabled(!currentProfile.isMinBooksToMakeDeepLevelReadOnly());
    lblMinBooksToMakeDeepLevel.setEnabled(txtMinBooksToMakeDeepLevel.isEnabled());
    txtMaxMobileResolution.setText("" + currentProfile.getMaxMobileResolution());
    txtMaxMobileResolution.setEnabled(!currentProfile.isMaxMobileResolutionReadOnly());
    lblMaxMobileResolution.setEnabled(txtMaxMobileResolution.isEnabled());
    txtMaxMobileResolution.setVisible(false);   // TODO Not currently being used
    lblMaxMobileResolution.setVisible(false);   // TODO Not currently being used
    chkIncludeCoversInCatalog.setSelected(currentProfile.getIncludeCoversInCatalog());
    chkIncludeCoversInCatalog.setEnabled(!currentProfile.isIncludeCoversInCatalogReadOnly());
    lblIncludeCoversInCatalog.setEnabled(chkIncludeCoversInCatalog.isEnabled());
    chkIncludeCoversInCatalog.setEnabled(false);  // TODO enable when support code ready
    lblIncludeCoversInCatalog.setEnabled(false);  // TODO enable when support code ready

    /* external links */
    txtWikipediaUrl.setText(currentProfile.getWikipediaUrl());
    txtAmazonAuthorUrl.setText(currentProfile.getAmazonAuthorUrl());
    txtAmazonIsbnUrl.setText(currentProfile.getAmazonIsbnUrl());
    txtAmazonTitleUrl.setText(currentProfile.getAmazonTitleUrl());
    txtGoodreadAuthorUrl.setText(currentProfile.getGoodreadAuthorUrl());
    txtGoodreadIsbnUrl.setText(currentProfile.getGoodreadIsbnUrl());
    txtGoodreadTitleUrl.setText(currentProfile.getGoodreadTitleUrl());
    txtGoodreadReviewIsbnUrl.setText(currentProfile.getGoodreadReviewIsbnUrl());
    txtIsfdbAuthorUrl.setText(currentProfile.getIsfdbAuthorUrl());
    txtLibrarythingAuthorUrl.setText(currentProfile.getLibrarythingAuthorUrl());
    txtLibrarythingIsbnUrl.setText(currentProfile.getLibrarythingIsbnUrl());
    txtLibrarythingTitleUrl.setText(currentProfile.getLibrarythingTitleUrl());
    setExternalLinksEnabledState();

    // custom catalogs

    txtFeaturedCatalogTitle.setText("" + currentProfile.getFeaturedCatalogTitle());
    txtFeaturedCatalogTitle.setEnabled(!currentProfile.isFeaturedCatalogTitleReadOnly());
    lblFeaturedCatalogTitle.setEnabled(!currentProfile.isFeaturedCatalogTitleReadOnly());
    txtFeaturedCatalogSavedSearchName.setText("" + currentProfile.getFeaturedCatalogSavedSearchName());
    txtFeaturedCatalogSavedSearchName.setEnabled(!currentProfile.isFeaturedCatalogSavedSearchNameReadOnly());
    lblFeaturedCatalogSavedSearchName.setEnabled(!currentProfile.isFeaturedCatalogSavedSearchNameReadOnly());
    customCatalogTableModel.setCustomCatalogs(currentProfile.getCustomCatalogs());
    tblCustomCatalogs.setEnabled(!currentProfile.isCustomCatalogsReadOnly());
    pnlCustomCatalogsTableButtons.setEnabled(!currentProfile.isCustomCatalogsReadOnly());
    tblCustomCatalogs.revalidate();         // Force a redraw of table contents

    DeviceMode mode = currentProfile.getDeviceMode();
    // Ensuer we have a Device Mode actually set
    if (Helper.isNullOrEmpty(mode)) {
      if (logger.isTraceEnabled())
        logger.trace("Device mode was not set - setting to " + DeviceMode.Dropbox);
      currentProfile.setDeviceMode(DeviceMode.Dropbox);
    }
    // Set interface to match the mode
    adaptInterfaceToDeviceSpecificMode(mode);
    computeBrowseByCoverWithoutSplitVisibility();

    changeLanguage();
    loadProfiles();
    checkDownloads();
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
    String lang = currentProfile.getLanguage();
    currentProfile.reset();
    loadValues();
    currentProfile.setLanguage(lang);
    changeLanguage();
  }

  private void storeValues() {
    setCursor(hourglassCursor);
    currentProfile.setLanguage("" + cboLang.getSelectedItem());
    File f = new File(txtDatabaseFolder.getText());
    if (f.exists())
      currentProfile.setDatabaseFolder(f);
    String s = txtTargetFolder.getText();
    if (Helper.isNotNullOrEmpty(s)) {
      f = new File(s);
      if (f.exists())
        currentProfile.setTargetFolder(f);
    } else
      currentProfile.setTargetFolder(null);
    currentProfile.setCopyToDatabaseFolder(chkCopyToDatabaseFolder.isSelected());
    currentProfile.setOnlyCatalogAtTarget((chkOnlyCatalogAtTarget.isSelected()));
    currentProfile.setReprocessEpubMetadata(chkReprocessEpubMetadata.isSelected());
    currentProfile.setWikipediaLanguage(txtWikilang.getText());
    currentProfile.setCatalogFolderName(txtCatalogFolder.getText());
    currentProfile.setUrlBooks(txtUrlBooks.getText());
    currentProfile.setCatalogTitle(txtCatalogTitle.getText());
    int i;
    i = getValue(txtThumbnailheight);
    if (i > -1)
      currentProfile.setThumbnailHeight(i);
    i = getValue(txtCoverHeight);
    if (i > -1)
      currentProfile.setCoverHeight(i);
    currentProfile.setIncludedFormatsList(txtIncludeformat.getText());
    i = getValue(txtMaxbeforepaginate);
    if (i > -1)
      currentProfile.setMaxBeforePaginate(i);
    i = getValue(txtMaxbeforesplit);
    if (i > -1)
      currentProfile.setMaxBeforeSplit(i);
    i = getValue(txtMaxSplitLevels);
    if (i > -1)
      currentProfile.setMaxSplitLevels(i);
    i = getValue(txtBooksinrecent);
    if (i > -1)
      currentProfile.setBooksInRecentAdditions(i);
    i = getValue(txtMaxsummarylength);
    if (i > -1)
      currentProfile.setMaxSummaryLength(i);
    i = getValue(txtMaxBookSummaryLength);
    if (i >= -1)
      currentProfile.setMaxBookSummaryLength(i);
    currentProfile.setIncludeAboutLink(!chkNoIncludeAboutLink.isSelected());
    currentProfile.setSplitTagsOn(txtSplittagson.getText());
    currentProfile.setIncludeBooksWithNoFile(chkIncludeemptybooks.isSelected());
    currentProfile.setIncludeOnlyOneFile(chkIncludeOnlyOneFile.isSelected());
    currentProfile.setZipTrookCatalog(chkZipTrookCatalog.isSelected());
    currentProfile.setMinimizeChangedFiles(chkMinimizeChangedFiles.isSelected());
    currentProfile.setExternalIcons(chkExternalIcons.isSelected());
    currentProfile.setSuppressRatingsInTitles(chkSupressRatings.isSelected());
    currentProfile.setBrowseByCover(chkBrowseByCover.isSelected());
    currentProfile.setPublishedDateAsYear(chkPublishedDateAsYear.isSelected());
    currentProfile.setBrowseByCoverWithoutSplit(chkBrowseByCoverWithoutSplit.isSelected());
    currentProfile.setGenerateDownloads(!chkNogenerateopdsfiles.isSelected());
    currentProfile.setCryptFilenames(chkCryptFilenames.isSelected());
    currentProfile.setShowSeriesInAuthorCatalog(!chkNoShowSeries.isSelected());
    currentProfile.setOrderAllBooksBySeries(chkOrderAllBooksBySeries.isSelected());
    currentProfile.setSplitByAuthorInitialGoToBooks(chkSplitByAuthorInitialGoToBooks.isSelected());
    currentProfile.setCatalogFilter(txtCatalogFilter.getText());

    // Catalog Structure

    currentProfile.setGenerateOpds(!chkNogenerateopds.isSelected());
    currentProfile.setGenerateHtml(!chkNogeneratehtml.isSelected());
    currentProfile.setGenerateHtmlDownloads(!chkNogeneratehtmlfiles.isSelected());
    currentProfile.setGenerateOpdsDownloads(!chkNogenerateopdsfiles.isSelected());
    currentProfile.setGenerateAuthors(!chkNoGenerateAuthors.isSelected());
    currentProfile.setGenerateTags(!chkNoGenerateTags.isSelected());
    currentProfile.setGenerateSeries(!chkNoGenerateSeries.isSelected());
    currentProfile.setGenerateRecent(!chkNogeneraterecent.isSelected());
    currentProfile.setGenerateRatings(!chkNogenerateratings.isSelected());
    currentProfile.setGenerateAllbooks(!chkNogenerateallbooks.isSelected());
    currentProfile.setLanguageAsTag(chkLanguageAsTag.isSelected());
    currentProfile.setTagsToIgnore(txtTagsToIgnore.getText());
    currentProfile.setDisplayAuthorSortInAuthorLists(chkDisplayAuthorSortInAuthorLists.isSelected());
    currentProfile.setDisplayTitleSortInBookListss(chkDisplayTitleSortInBookLists.isSelected());
    currentProfile.setSortUsingAuthor(chkSortUsingAuthorSort.isSelected());
    currentProfile.setSortUsingTitle(chkSortUsingTitleSort.isSelected());

    // Book Details

    currentProfile.setIncludeSeriesInBookDetails(chkIncludeSeriesInBookDetails.isSelected());
    currentProfile.setIncludeRatingInBookDetails(chkIncludeRatingInBookDetails1.isSelected());
    currentProfile.setIncludeTagsInBookDetails(chkIncludeTagsInBookDetails.isSelected());
    currentProfile.setIncludePublisherInBookDetails(chkIncludePublisherInBookDetails.isSelected());
    currentProfile.setIncludePublishedInBookDetails(chkIncludePublishedInBookDetails.isSelected());
    currentProfile.setPublishedDateAsYear(chkPublishedDateAsYear.isSelected());
    currentProfile.setIncludeAddedInBookDetails(chkIncludeAddednBookDetails1.isSelected());
    currentProfile.setIncludeModifiedInBookDetails(chkIncludeModifiedInBookDetails.isSelected());
    currentProfile.setDisplayAuthorSortInBookDetails(chkDisplayAuthorSortInBookDetails.isSelected());
    currentProfile.setDisplayTitleSortInBookDetails(chkDisplayTitleSortInBookDetails.isSelected());
    currentProfile.setBookDetailsCustomFields(txtBookDetailsCustomFields.getText());
    currentProfile.setGenerateCrossLinks(!chkNogeneratecrosslinks.isSelected());
    currentProfile.setGenerateExternalLinks(!chkNogenerateexternallinks.isSelected());
    currentProfile.setIncludeTagCrossReferences(chkIncludeTagCrossReferences.isSelected());

    // Advanced

    currentProfile.setTagsToMakeDeep(txtTagsToMakeDeep.getText());
    currentProfile.setCoverResize(!chkNoCoverResize.isSelected());
    currentProfile.setThumbnailGenerate(!chkNoThumbnailGenerate.isSelected());
    i = getValue(txtMinBooksToMakeDeepLevel);
    if (i >= -1)
      currentProfile.setMinBooksToMakeDeepLevel(i);
    i = getValue(txtMaxMobileResolution);
    if (i > -1)
      currentProfile.setMaxMobileResolution(i);
    currentProfile.setGenerateIndex(chkGenerateIndex.isSelected());
    currentProfile.setIndexComments(chkIndexComments.isSelected());
    i = getValue(txtMaxKeywords);
    currentProfile.setMaxKeywords(i);
    currentProfile.setIndexFilterAlgorithm(Index.FilterHintType.valueOf("" + cboIndexFilterAlgorithm.getSelectedItem()));
    currentProfile.setIncludeCoversInCatalog(chkIncludeCoversInCatalog.isSelected());

    // External Links

    currentProfile.setWikipediaUrl(txtWikipediaUrl.getText());
    currentProfile.setAmazonAuthorUrl(txtAmazonAuthorUrl.getText());
    currentProfile.setAmazonIsbnUrl(txtAmazonIsbnUrl.getText());
    currentProfile.setAmazonTitleUrl(txtAmazonTitleUrl.getText());
    currentProfile.setGoodreadAuthorUrl(txtGoodreadAuthorUrl.getText());
    currentProfile.setGoodreadIsbnUrl(txtGoodreadIsbnUrl.getText());
    currentProfile.setGoodreadTitleUrl(txtGoodreadTitleUrl.getText());
    currentProfile.setGoodreadReviewIsbnUrl(txtGoodreadReviewIsbnUrl.getText());
    currentProfile.setIsfdbAuthorUrl(txtIsfdbAuthorUrl.getText());
    currentProfile.setLibrarythingAuthorUrl(txtLibrarythingAuthorUrl.getText());
    currentProfile.setLibrarythingIsbnUrl(txtLibrarythingIsbnUrl.getText());
    currentProfile.setLibrarythingTitleUrl(txtLibrarythingTitleUrl.getText());

    // custom catalogs

    currentProfile.setFeaturedCatalogTitle(txtFeaturedCatalogTitle.getText());
    currentProfile.setFeaturedCatalogSavedSearchName(txtFeaturedCatalogSavedSearchName.getText());
    currentProfile.setCustomCatalogs(customCatalogTableModel.getCustomCatalogs());
    setCursor(normalCursor);
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
    cmdCancel.setToolTipText(Localization.Main.getText("gui.close.description")); // NOI18N
    cmdSave.setText(Localization.Main.getText("gui.save")); // NOI18N
    cmdSave.setToolTipText(Localization.Main.getText("gui.save.description")); // NOI18N
    cmdGenerate.setText(Localization.Main.getText("gui.generate")); // NOI18N
    cmdGenerate.setToolTipText(Localization.Main.getText("gui.generate.description")); // NOI18N
    cmdReset.setText(Localization.Main.getText("gui.reset"));
    cmdReset.setToolTipText(Localization.Main.getText("gui.reset.description"));
    cmdHelp.setText(Localization.Main.getText("gui.help"));
    cmdHelp.setToolTipText(Localization.Main.getText("gui.help.description"));
    tabOptionsTabs.setTitleAt(0, Localization.Main.getText("gui.tab1"));
    tabOptionsTabs.setToolTipTextAt(0, Localization.Main.getText("gui.tab1.description"));
    tabOptionsTabs.setTitleAt(1, Localization.Main.getText("gui.tab2"));
    tabOptionsTabs.setToolTipTextAt(1, Localization.Main.getText("gui.tab2.description"));
    tabOptionsTabs.setTitleAt(2, Localization.Main.getText("gui.tab3"));
    tabOptionsTabs.setToolTipTextAt(2, Localization.Main.getText("gui.tab3.description"));
    tabOptionsTabs.setTitleAt(3, Localization.Main.getText("gui.tab4"));
    tabOptionsTabs.setToolTipTextAt(3, Localization.Main.getText("gui.tab4.description"));
    tabOptionsTabs.setTitleAt(4, Localization.Main.getText("gui.tab5"));
    tabOptionsTabs.setToolTipTextAt(4, Localization.Main.getText("gui.tab5.description"));
    tabOptionsTabs.setTitleAt(5, Localization.Main.getText("gui.tab6"));
    tabOptionsTabs.setToolTipTextAt(5, Localization.Main.getText("gui.tab6.description"));
    lblDeviceDropbox.setToolTipText(
        Localization.Main.getText("config.DeviceMode.dropbox.description1") + " " + Localization.Main.getText("config.DeviceMode.dropbox.description2"));
    lblDeviceNAS.setToolTipText(
        Localization.Main.getText("config.DeviceMode.nas.description1") + " " + Localization.Main.getText("config.DeviceMode.nas.description2"));
    lblDeviceNook.setToolTipText(
        Localization.Main.getText("config.DeviceMode.nook.description1") + " " + Localization.Main.getText("config.DeviceMode.nook.description2"));
    adaptInterfaceToDeviceSpecificMode(currentProfile.getDeviceMode());

    // main options

    lblLang.setText(Localization.Main.getText("config.Language.label")); // NOI18N
    lblLang.setToolTipText(Localization.Main.getText("config.Language.description")); // NOI18N
    cboLang.setToolTipText(lblLang.getToolTipText()); // NOI18N
    lblDatabaseFolder.setText(Localization.Main.getText("config.DatabaseFolder.label")); // NOI18N
    lblDatabaseFolder.setToolTipText(Localization.Main.getText("config.DatabaseFolder.description")); // NOI18N
    txtDatabaseFolder.setToolTipText(lblDatabaseFolder.getToolTipText()); // NOI18N
    lblTargetFolder.setText(Localization.Main.getText("config.TargetFolder.label")); // NOI18N
    lblTargetFolder.setToolTipText(Localization.Main.getText("config.TargetFolder.description")); // NOI18N
    txtTargetFolder.setToolTipText(lblTargetFolder.getToolTipText()); // NOI18N
    lblCopyToDatabaseFolder.setText(Localization.Main.getText("config.CopyToDatabaseFolder.label")); // NOI18N
    lblCopyToDatabaseFolder.setToolTipText(Localization.Main.getText("config.CopyToDatabaseFolder.description")); // NOI18N
    chkCopyToDatabaseFolder.setToolTipText(lblCopyToDatabaseFolder.getToolTipText()); // NOI18N
    lblOnlyCatalogAtTarget.setText(Localization.Main.getText("config.OnlyCatalogAtTarget.label")); // NOI18N
    lblOnlyCatalogAtTarget.setToolTipText(Localization.Main.getText("config.OnlyCatalogAtTarget.description")); // NOI18N
    chkOnlyCatalogAtTarget.setToolTipText(lblOnlyCatalogAtTarget.getToolTipText()); // NOI18N
    // lblOnlyCatalogAtTarget.setEnabled(false);    // TODO enable when support implemented
    // chkOnlyCatalogAtTarget.setEnabled(false);    // TODO enable when support implemented
    lblReprocessEpubMetadata.setText(Localization.Main.getText("config.ReprocessEpubMetadata.label")); // NOI18N
    lblReprocessEpubMetadata.setToolTipText(Localization.Main.getText("config.ReprocessEpubMetadata.description")); // NOI18N
    chkReprocessEpubMetadata.setToolTipText(lblReprocessEpubMetadata.getToolTipText()); // NOI18N
    lblCatalogFolder.setText(Localization.Main.getText("config.CatalogFolderName.label")); // NOI18N
    lblCatalogFolder.setToolTipText(Localization.Main.getText("config.CatalogFolderName.description")); // NOI18N
    txtCatalogFolder.setToolTipText(lblCatalogFolder.getToolTipText()); // NOI18N
    lblUrlBooks.setText(Localization.Main.getText("config.UrlBooks.label")); // NOI18N
    lblUrlBooks.setToolTipText(Localization.Main.getText("config.UrlBooks.description")); // NOI18N
    txtUrlBooks.setToolTipText(lblUrlBooks.getToolTipText()); // NOI18N
    lblCatalogTitle.setText(Localization.Main.getText("config.CatalogTitle.label")); // NOI18N
    lblCatalogTitle.setToolTipText(Localization.Main.getText("config.CatalogTitle.description")); // NOI18N
    txtCatalogTitle.setToolTipText(lblCatalogTitle.getToolTipText()); // NOI18N
    lblSplittagson.setText(Localization.Main.getText("config.SplitTagsOn.label")); // NOI18N
    lblSplittagson.setToolTipText(Localization.Main.getText("config.SplitTagsOn.description")); // NOI18N
    chkDontsplittags.setText(Localization.Main.getText("config.SplitTagsOn.splitbyletter")); // NOI18N
    lblCatalogFilter.setText(Localization.Main.getText("config.CatalogFilter.label")); // NOI18N
    lblCatalogFilter.setToolTipText(Localization.Main.getText("config.CatalogFilter.description")); // NOI18N
    txtCatalogFilter.setToolTipText(lblCatalogFilter.getToolTipText()); // NOI18N
    lblWikilang.setText(Localization.Main.getText("config.WikipediaLanguage.label")); // NOI18N
    lblWikilang.setToolTipText(Localization.Main.getText("config.WikipediaLanguage.description")); // NOI18N
    txtWikilang.setToolTipText(lblWikilang.getToolTipText()); // NOI18N

    // catalog structure options

    lblNogenerateopds.setText(Localization.Main.getText("config.GenerateOpds.label")); // NOI18N
    lblNogenerateopds.setToolTipText(Localization.Main.getText("config.GenerateOpds.description")); // NOI18N
    chkNogenerateopds.setToolTipText(lblNogenerateopds.getToolTipText()); // NOI18N
    lblNogeneratehtml.setText(Localization.Main.getText("config.GenerateHtml.label")); // NOI18N
    lblNogeneratehtml.setToolTipText(Localization.Main.getText("config.GenerateHtml.description")); // NOI18N
    chkNogeneratehtml.setToolTipText(lblNogeneratehtml.getToolTipText()); // NOI18N
    lblNogeneratehtmlfiles.setText(Localization.Main.getText("config.GenerateHtmlDownloads.label")); // NOI18N
    lblNogeneratehtmlfiles.setToolTipText(Localization.Main.getText("config.GenerateHtmlDownloads.description")); // NOI18N
    chkNogeneratehtmlfiles.setToolTipText(lblNogeneratehtmlfiles.getToolTipText()); // NOI18N
    lblBrowseByCover.setText(Localization.Main.getText("config.BrowseByCover.label")); // NOI18N
    lblBrowseByCover.setToolTipText(Localization.Main.getText("config.BrowseByCover.description")); // NOI18N
    chkBrowseByCover.setToolTipText(lblBrowseByCover.getToolTipText()); // NOI18N
    lblBrowseByCoverWithoutSplit.setText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.label")); // NOI18N
    lblBrowseByCoverWithoutSplit.setToolTipText(Localization.Main.getText("config.BrowseByCoverWithoutSplit.description")); // NOI18N
    chkBrowseByCoverWithoutSplit.setToolTipText(lblBrowseByCoverWithoutSplit.getToolTipText()); // NOI18N
    lblLanguageAsTag.setText(Localization.Main.getText("config.LanguageAsTag.label")); // NOI18N
    lblLanguageAsTag.setToolTipText(Localization.Main.getText("config.LanguageAsTag.description")); // NOI18N
    chkLanguageAsTag.setToolTipText(lblLanguageAsTag.getToolTipText()); // NOI18N
    lblNoIncludeAboutLink.setText(Localization.Main.getText("config.IncludeAboutLink.label")); // NOI18N
    lblNoIncludeAboutLink.setToolTipText(Localization.Main.getText("config.IncludeAboutLink.description")); // NOI18N
    chkNoIncludeAboutLink.setToolTipText(lblNoIncludeAboutLink.getToolTipText()); // NOI18N
    lblNogenerateopdsfiles.setText(Localization.Main.getText("config.GenerateOpdsDownloads.label")); // NOI18N
    lblNogenerateopdsfiles.setToolTipText(Localization.Main.getText("config.GenerateOpdsDownloads.description")); // NOI18N
    chkNogenerateopdsfiles.setToolTipText(lblNogenerateopdsfiles.getToolTipText()); // NOI18N
    lblExternalIcons.setText(Localization.Main.getText("config.ExternalIcons.label")); // NOI18N
    lblExternalIcons.setToolTipText(Localization.Main.getText("config.ExternalIcons.description")); // NOI18N
    chkExternalIcons.setToolTipText(lblExternalIcons.getToolTipText()); // NOI18N
    lblNoGenerateAuthors.setText(Localization.Main.getText("config.GenerateAuthors.label")); // NOI18N
    lblNoGenerateAuthors.setToolTipText(Localization.Main.getText("config.GenerateAuthors.description")); // NOI18N
    chkNoGenerateAuthors.setToolTipText(lblNoGenerateAuthors.getToolTipText()); // NOI18N
    lblNoGenerateTags.setText(Localization.Main.getText("config.GenerateTags.label")); // NOI18N
    lblNoGenerateTags.setToolTipText(Localization.Main.getText("config.GenerateTags.description")); // NOI18N
    chkNoGenerateTags.setToolTipText(lblNoGenerateTags.getToolTipText()); // NOI18N
    lblTagsToIgnore.setText(Localization.Main.getText("config.TagsToIgnore.label")); // NOI18N
    lblTagsToIgnore.setToolTipText(Localization.Main.getText("config.TagsToIgnore.description")); // NOI18N
    txtTagsToIgnore.setToolTipText(lblTagsToIgnore.getToolTipText()); // NOI18N
    lblTagsToIgnore.setEnabled(false);    // TODO enable when support code ready
    txtTagsToIgnore.setEnabled(false);    // TODO enable when support code ready
    lblTagsToMakeDeep.setText(Localization.Main.getText("config.TagsToMakeDeep.label")); // NOI18N
    lblTagsToMakeDeep.setToolTipText(Localization.Main.getText("config.TagsToMakeDeep.description")); // NOI18N
    txtTagsToMakeDeep.setToolTipText(lblTagsToMakeDeep.getToolTipText()); // NOI18N
    lblNoGenerateSeries.setText(Localization.Main.getText("config.GenerateSeries.label")); // NOI18N
    lblNoGenerateSeries.setToolTipText(Localization.Main.getText("config.GenerateSeries.description")); // NOI18N
    chkNoGenerateSeries.setToolTipText(lblNoGenerateSeries.getToolTipText()); // NOI18N
    lblNogeneraterecent.setText(Localization.Main.getText("config.GenerateRecent.label")); // NOI18N
    lblNogeneraterecent.setToolTipText(Localization.Main.getText("config.GenerateRecent.description")); // NOI18N
    chkNogeneraterecent.setToolTipText(lblNogeneraterecent.getToolTipText()); // NOI18N
    lblNogenerateratings.setText(Localization.Main.getText("config.GenerateRatings.label")); // NOI18N
    lblNogenerateratings.setToolTipText(Localization.Main.getText("config.GenerateRatings.description")); // NOI18N
    chkNogenerateratings.setToolTipText(lblNogenerateratings.getToolTipText()); // NOI18N
    lblSupressRatings.setText(Localization.Main.getText("config.SuppressRatingsInTitles.label")); // NOI18N
    lblSupressRatings.setToolTipText(Localization.Main.getText("config.SuppressRatingsInTitles.description")); // NOI18N
    chkSupressRatings.setToolTipText(lblSupressRatings.getToolTipText()); // NOI18N
    lblNogenerateallbooks.setText(Localization.Main.getText("config.GenerateAllbooks.label")); // NOI18N
    lblNogenerateallbooks.setToolTipText(Localization.Main.getText("config.GenerateAllbooks.description")); // NOI18N
    chkNogenerateallbooks.setToolTipText(lblNogenerateallbooks.getToolTipText()); // NOI18N
    lblDisplayAuthorSortInAuthorLists.setText(Localization.Main.getText("config.DisplayAuthorSortInAuthorLists.label")); // NOI18N
    lblDisplayAuthorSortInAuthorLists.setToolTipText(Localization.Main.getText("config.DisplayAuthorSortInAuthorLists.description")); // NOI18N
    chkDisplayAuthorSortInAuthorLists.setToolTipText(lblDisplayAuthorSortInAuthorLists.getToolTipText()); // NOI18N
    lblDisplayTitleSortInBookLists.setText(Localization.Main.getText("config.DisplayTitleSortInBookLists.label")); // NOI18N
    lblDisplayTitleSortInBookLists.setToolTipText(Localization.Main.getText("config.DisplayTitleSortInBookLists.description")); // NOI18N
    chkDisplayTitleSortInBookLists.setToolTipText(lblDisplayTitleSortInBookLists.getToolTipText()); // NOI18N
    lblSortUsingAuthor.setText(Localization.Main.getText("config.SortUsingAuthor.label")); // NOI18N
    lblSortUsingAuthor.setToolTipText(Localization.Main.getText("config.SortUsingAuthor.description")); // NOI18N
    chkSortUsingAuthorSort.setToolTipText(lblSortUsingAuthor.getToolTipText()); // NOI18N
    lblSortUsingTitle.setText(Localization.Main.getText("config.SortUsingTitle.label")); // NOI18N
    lblSortUsingTitle.setToolTipText(Localization.Main.getText("config.SortUsingTitle.description")); // NOI18N
    chkSortUsingTitleSort.setToolTipText(lblSortUsingTitle.getToolTipText()); // NOI18N

    // Book Details Options

    lblIncludeSeriesInBookDetails.setText(Localization.Main.getText("config.IncludeSeriesInBookDetails.label")); // NOI18N
    lblIncludeSeriesInBookDetails.setToolTipText(Localization.Main.getText("config.IncludeSeriesInBookDetails.description")); // NOI18N
    chkIncludeSeriesInBookDetails.setToolTipText(lblIncludeSeriesInBookDetails.getToolTipText()); // NOI18N
    lblIncludeRatingInBookDetails.setText(Localization.Main.getText("config.IncludeRatingInBookDetails.label")); // NOI18N
    lblIncludeRatingInBookDetails.setToolTipText(Localization.Main.getText("config.IncludeRatingInBookDetails.description")); // NOI18N
    chkIncludeRatingInBookDetails1.setToolTipText(lblIncludeRatingInBookDetails.getToolTipText()); // NOI18N
    lblIncludeTagsInBookDetails.setText(Localization.Main.getText("config.IncludeTagsInBookDetails.label")); // NOI18N
    lblIncludeTagsInBookDetails.setToolTipText(Localization.Main.getText("config.IncludeTagsInBookDetails.description")); // NOI18N
    chkIncludeTagsInBookDetails.setToolTipText(lblIncludeTagsInBookDetails.getToolTipText()); // NOI18N
    lblIncludePublisherInBookDetails.setText(Localization.Main.getText("config.IncludePublisherInBookDetails.label")); // NOI18N
    lblIncludePublisherInBookDetails.setToolTipText(Localization.Main.getText("config.IncludePublisherInBookDetails.description")); // NOI18N
    chkIncludePublisherInBookDetails.setToolTipText(lblIncludePublishedInBookDetails.getToolTipText()); // NOI18N
    lblIncludePublishedInBookDetails.setText(Localization.Main.getText("config.IncludePublishedInBookDetails.label")); // NOI18N
    lblIncludePublishedInBookDetails.setToolTipText(Localization.Main.getText("config.IncludePublishedInBookDetails.description")); // NOI18N
    chkIncludePublishedInBookDetails.setToolTipText(lblIncludePublishedInBookDetails.getToolTipText()); // NOI18N
    lblPublishedDateAsYear.setText(Localization.Main.getText("config.PublishedDateAsYear.label")); // NOI18N
    lblPublishedDateAsYear.setToolTipText(Localization.Main.getText("config.PublishedDateAsYear.description")); // NOI18N
    chkPublishedDateAsYear.setToolTipText(lblPublishedDateAsYear.getToolTipText()); // NOI18N
    lblIncludeAddedInBookDetails2.setText(Localization.Main.getText("config.IncludeAddedInBookDetails.label")); // NOI18N
    lblIncludeAddedInBookDetails2.setToolTipText(Localization.Main.getText("config.IncludeAddedInBookDetails.description")); // NOI18N
    chkIncludeAddednBookDetails1.setToolTipText(lblIncludeAddedInBookDetails2.getToolTipText()); // NOI18N
    lblIncludeModifiedInBookDetails1.setText(Localization.Main.getText("config.IncludeModifiedInBookDetails.label")); // NOI18N
    lblIncludeModifiedInBookDetails1.setToolTipText(Localization.Main.getText("config.IncludeModifiedInBookDetails.description")); // NOI18N
    chkIncludeModifiedInBookDetails.setToolTipText(lblIncludeModifiedInBookDetails1.getToolTipText()); // NOI18N
    lblDisplayAuthorSortInBookDetails.setText(Localization.Main.getText("config.DisplayAuthorSortInBookDetails.label")); // NOI18N
    lblDisplayAuthorSortInBookDetails.setToolTipText(Localization.Main.getText("config.DisplayAuthorSortInBookDetails.description")); // NOI18N
    chkDisplayAuthorSortInBookDetails.setToolTipText(lblDisplayAuthorSortInBookDetails.getToolTipText()); // NOI18N
    lblDisplayTitleSortInBookDetails.setText(Localization.Main.getText("config.DisplayTitleSortInBookDetails.label")); // NOI18N
    lblDisplayTitleSortInBookDetails.setToolTipText(Localization.Main.getText("config.DisplayTitleSortInBookDetails.description")); // NOI18N
    chkDisplayTitleSortInBookDetails.setToolTipText(lblDisplayTitleSortInBookDetails.getToolTipText()); // NOI18N
    lblBookDetailsCustomFields.setText(Localization.Main.getText("config.BookDetailsCustomFields.label")); // NOI18N
    lblBookDetailsCustomFields.setToolTipText(Localization.Main.getText("config.BookDetailsCustomFields.description")); // NOI18N
    txtBookDetailsCustomFields.setToolTipText(lblBookDetailsCustomFields.getToolTipText());
    lblNogenerateexternallinks.setText(Localization.Main.getText("config.GenerateExternalLinks.label")); // NOI18N
    lblNogenerateexternallinks.setToolTipText(Localization.Main.getText("config.GenerateExternalLinks.description")); // NOI18N
    chkNogenerateexternallinks.setToolTipText(lblNogeneratehtmlfiles.getToolTipText()); // NOI18N
    lblNogeneratecrosslinks.setText(Localization.Main.getText("config.GenerateCrossLinks.label")); // NOI18N
    lblNogeneratecrosslinks.setToolTipText(Localization.Main.getText("config.GenerateCrossLinks.description")); // NOI18N
    chkNogeneratecrosslinks.setToolTipText(lblNogeneratecrosslinks.getToolTipText()); // NOI18N
    lblIncludeTagCrossReferences.setText(Localization.Main.getText("config.IncludeTagCrossReferences.label")); // NOI18N
    lblIncludeTagCrossReferences.setToolTipText(Localization.Main.getText("config.IncludeTagCrossReferences.description")); // NOI18N
    chkIncludeTagCrossReferences.setToolTipText(lblIncludeTagCrossReferences.getToolTipText()); // NOI18N

    // advanced customization options

    lblIncludeformat.setText(Localization.Main.getText("config.IncludedFormatsList.label")); // NOI18N
    lblIncludeformat.setToolTipText(Localization.Main.getText("config.IncludedFormatsList.description")); // NOI18N
    txtIncludeformat.setToolTipText(lblIncludeformat.getToolTipText()); // NOI18N
    lblMaxbeforepaginate.setText(Localization.Main.getText("config.MaxBeforePaginate.label")); // NOI18N
    lblMaxbeforepaginate.setToolTipText(Localization.Main.getText("config.MaxBeforePaginate.description")); // NOI18N
    txtMaxbeforepaginate.setToolTipText(lblMaxbeforepaginate.getToolTipText()); // NOI18N
    lblMaxbeforesplit.setText(Localization.Main.getText("config.MaxBeforeSplit.label")); // NOI18N
    lblMaxbeforesplit.setToolTipText(Localization.Main.getText("config.MaxBeforeSplit.description")); // NOI18N
    txtMaxbeforesplit.setToolTipText(lblMaxbeforesplit.getToolTipText()); // NOI18N
    lblMaxSplitLevels.setText(Localization.Main.getText("config.MaxSplitLevels.label")); // NOI18N
    lblMaxSplitLevels.setToolTipText(Localization.Main.getText("config.MaxSplitLevels.description")); // NOI18N
    txtMaxSplitLevels.setToolTipText(lblMaxSplitLevels.getToolTipText()); // NOI18N
    lblBooksinrecent.setText(Localization.Main.getText("config.BooksInRecentAdditions.label")); // NOI18N
    lblBooksinrecent.setToolTipText(Localization.Main.getText("config.BooksInRecentAdditions.description")); // NOI18N
    txtBooksinrecent.setToolTipText(lblBooksinrecent.getToolTipText()); // NOI18N
    lblMaxsummarylength.setText(Localization.Main.getText("config.MaxSummaryLength.label")); // NOI18N
    lblMaxsummarylength.setToolTipText(Localization.Main.getText("config.MaxSummaryLength.description")); // NOI18N
    txtMaxsummarylength.setToolTipText(lblMaxsummarylength.getToolTipText()); // NOI18N
    lblMaxBookSummaryLength.setText(Localization.Main.getText("config.MaxBookSummaryLength.label")); // NOI18N
    lblMaxBookSummaryLength.setToolTipText(Localization.Main.getText("config.MaxBookSummaryLength.description")); // NOI18N
    txtMaxBookSummaryLength.setToolTipText(lblMaxBookSummaryLength.getToolTipText()); // NOI18N
    lblIncludeemptybooks.setText(Localization.Main.getText("config.IncludeBooksWithNoFile.label")); // NOI18N
    lblIncludeemptybooks.setToolTipText(Localization.Main.getText("config.IncludeBooksWithNoFile.description")); // NOI18N
    chkIncludeemptybooks.setToolTipText(lblIncludeemptybooks.getToolTipText()); // NOI18N
    lblIncludeOnlyOneFile.setText(Localization.Main.getText("config.IncludeOnlyOneFile.label")); // NOI18N
    lblIncludeOnlyOneFile.setToolTipText(Localization.Main.getText("config.IncludeOnlyOneFile.description")); // NOI18N
    chkIncludeOnlyOneFile.setToolTipText(lblIncludeOnlyOneFile.getToolTipText()); // NOI18N
    lblZipTrookCatalog.setText(Localization.Main.getText("config.ZipTrookCatalog.label")); // NOI18N
    lblZipTrookCatalog.setToolTipText(Localization.Main.getText("config.ZipTrookCatalog.description")); // NOI18N
    chkZipTrookCatalog.setToolTipText(lblZipTrookCatalog.getToolTipText()); // NOI18N
    lblNoShowSeries.setText(Localization.Main.getText("config.ShowSeriesInAuthorCatalog.label")); // NOI18N
    lblNoShowSeries.setToolTipText(Localization.Main.getText("config.ShowSeriesInAuthorCatalog.description")); // NOI18N
    chkNoShowSeries.setToolTipText(lblNoShowSeries.getToolTipText()); // NOI18N
    lblOrderAllBooksBySeries.setText(Localization.Main.getText("config.OrderAllBooksBySeries.label")); // NOI18N
    lblOrderAllBooksBySeries.setToolTipText(Localization.Main.getText("config.OrderAllBooksBySeries.description")); // NOI18N
    chkOrderAllBooksBySeries.setToolTipText(lblOrderAllBooksBySeries.getToolTipText()); // NOI18N
    lblSplitByAuthorInitialGoToBooks.setText(Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.label")); // NOI18N
    lblSplitByAuthorInitialGoToBooks.setToolTipText(Localization.Main.getText("config.SplitByAuthorInitialGoToBooks.description")); // NOI18N
    chkSplitByAuthorInitialGoToBooks.setToolTipText(lblSplitByAuthorInitialGoToBooks.getToolTipText()); // NOI18N
    lblNoThumbnailGenerate.setText(Localization.Main.getText("config.ThumbnailGenerate.label")); // NOI18N
    lblNoThumbnailGenerate.setToolTipText(Localization.Main.getText("config.ThumbnailGenerate.description")); // NOI18N
    chkNoThumbnailGenerate.setToolTipText(lblNoThumbnailGenerate.getToolTipText()); // NOI18N
    lblThumbnailheight.setText(Localization.Main.getText("config.ThumbnailHeight.label")); // NOI18N
    lblThumbnailheight.setToolTipText(Localization.Main.getText("config.ThumbnailHeight.description")); // NOI18N
    txtThumbnailheight.setToolTipText(lblThumbnailheight.getToolTipText()); // NOI18N
    lblNoCoverResize.setText(Localization.Main.getText("config.CoverResize.label")); // NOI18N
    lblNoCoverResize.setToolTipText(Localization.Main.getText("config.CoverResize.description")); // NOI18N
    chkNoCoverResize.setToolTipText(lblNoCoverResize.getToolTipText()); // NOI18N
    lblIncludeCoversInCatalog.setText(Localization.Main.getText("config.IncludeCoversInCatalog.label")); // NOI18N
    lblIncludeCoversInCatalog.setToolTipText(Localization.Main.getText("config.IncludeCoversInCatalog.description")); // NOI18N
    chkIncludeCoversInCatalog.setToolTipText(lblIncludeCoversInCatalog.getToolTipText()); // NOI18N
    lblIncludeCoversInCatalog.setEnabled(false);    // TODO enable when support cody ready
    chkIncludeCoversInCatalog.setEnabled(false);    // TODO enable when support code ready
    lblCoverHeight.setText(Localization.Main.getText("config.CoverHeight.label")); // NOI18N
    lblCoverHeight.setToolTipText(Localization.Main.getText("config.CoverHeight.description")); // NOI18N
    txtCoverHeight.setToolTipText(lblCoverHeight.getToolTipText()); // NOI18N
    lblMinBooksToMakeDeepLevel.setText(Localization.Main.getText("config.MinBooksToMakeDeepLevel.label")); // NOI18N
    lblMinBooksToMakeDeepLevel.setToolTipText(Localization.Main.getText("config.MinBooksToMakeDeepLevel.description")); // NOI18N
    txtMinBooksToMakeDeepLevel.setToolTipText(lblMinBooksToMakeDeepLevel.getToolTipText()); // NOI18N
    lblMaxMobileResolution.setText(Localization.Main.getText("config.MaxMobileResolution.label")); // NOI18N
    lblMaxMobileResolution.setToolTipText(Localization.Main.getText("config.MaxMobileResolution.description")); // NOI18N
    txtMaxMobileResolution.setToolTipText(lblMaxMobileResolution.getToolTipText()); // NOI18N
    lblMinimizeChangedFiles.setText(Localization.Main.getText("config.MinimizeChangedFiles.label")); // NOI18N
    lblMinimizeChangedFiles.setToolTipText(Localization.Main.getText("config.MinimizeChangedFiles.description")); // NOI18N
    chkMinimizeChangedFiles.setToolTipText(lblMinimizeChangedFiles.getToolTipText()); // NOI18N
    lblCryptFilenames.setText(Localization.Main.getText("config.CryptFilenames.label")); // NOI18N
    lblCryptFilenames.setToolTipText(Localization.Main.getText("config.CryptFilenames.description")); // NOI18N
    chkCryptFilenames.setToolTipText(lblCryptFilenames.getToolTipText()); // NOI18N
    lblGenerateIndex.setText(Localization.Main.getText("config.GenerateIndex.label")); // NOI18N
    lblGenerateIndex.setToolTipText(Localization.Main.getText("config.GenerateIndex.description")); // NOI18N
    chkGenerateIndex.setToolTipText(lblGenerateIndex.getToolTipText()); // NOI18N

    // external links

    lblWikipediaUrl.setText(Localization.Main.getText("config.WikipediaUrl.label")); // NOI18N
    lblWikipediaUrl.setToolTipText(Localization.Main.getText("config.WikipediaUrl.description")); // NOI18N
    txtWikipediaUrl.setToolTipText(lblWikipediaUrl.getToolTipText()); // NOI18N
    cmdWikipediaUrlReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdWikipediaUrlReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonAuthorUrl.setText(Localization.Main.getText("config.AmazonAuthorUrl.label")); // NOI18N
    lblAmazonAuthorUrl.setToolTipText(Localization.Main.getText("config.AmazonAuthorUrl.description")); // NOI18N
    txtAmazonAuthorUrl.setToolTipText(lblAmazonAuthorUrl.getToolTipText()); // NOI18N
    cmdAmazonUrlReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonUrlReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonIsbnUrl.setText(Localization.Main.getText("config.AmazonIsbnUrl.label")); // NOI18N
    lblAmazonIsbnUrl.setToolTipText(Localization.Main.getText("config.AmazonIsbnUrl.description")); // NOI18N
    txtAmazonIsbnUrl.setToolTipText(lblAmazonIsbnUrl.getToolTipText()); // NOI18N
    cmdAmazonIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblAmazonTitleUrl.setText(Localization.Main.getText("config.AmazonTitleUrl.label")); // NOI18N
    lblAmazonTitleUrl.setToolTipText(Localization.Main.getText("config.AmazonTitleUrl.description")); // NOI18N
    txtAmazonTitleUrl.setToolTipText(lblAmazonTitleUrl.getToolTipText()); // NOI18N
    cmdAmazonTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdAmazonTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadAuthorUrl.setText(Localization.Main.getText("config.GoodreadAuthorUrl.label")); // NOI18N
    lblGoodreadAuthorUrl.setToolTipText(Localization.Main.getText("config.GoodreadAuthorUrl.description")); // NOI18N
    txtGoodreadAuthorUrl.setToolTipText(lblGoodreadAuthorUrl.getToolTipText()); // NOI18N
    cmdGoodreadAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadIsbnUrl.setText(Localization.Main.getText("config.GoodreadIsbnUrl.label")); // NOI18N
    lblGoodreadIsbnUrl.setToolTipText(Localization.Main.getText("config.GoodreadIsbnUrl.description")); // NOI18N
    txtGoodreadIsbnUrl.setToolTipText(lblGoodreadIsbnUrl.getToolTipText()); // NOI18N
    cmdGoodreadIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadTitleUrl.setText(Localization.Main.getText("config.GoodreadTitleUrl.label")); // NOI18N
    lblGoodreadTitleUrl.setToolTipText(Localization.Main.getText("config.GoodreadTitleUrl.description")); // NOI18N
    txtGoodreadTitleUrl.setToolTipText(lblGoodreadTitleUrl.getToolTipText()); // NOI18N
    cmdGoodreadTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblGoodreadReviewIsbnUrl.setText(Localization.Main.getText("config.GoodreadReviewIsbnUrl.label")); // NOI18N
    lblGoodreadReviewIsbnUrl.setToolTipText(Localization.Main.getText("config.GoodreadReviewIsbnUrl.description")); // NOI18N
    txtGoodreadReviewIsbnUrl.setToolTipText(lblGoodreadReviewIsbnUrl.getToolTipText()); // NOI18N
    cmdGoodreadReviewReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdGoodreadReviewReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblIsfdbAuthorUrl.setText(Localization.Main.getText("config.IsfdbAuthorUrl.label")); // NOI18N
    lblIsfdbAuthorUrl.setToolTipText(Localization.Main.getText("config.IsfdbAuthorUrl.description")); // NOI18N
    txtIsfdbAuthorUrl.setToolTipText(lblIsfdbAuthorUrl.getToolTipText()); // NOI18N
    cmdIsfdbAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdIsfdbAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingAuthorUrl.setText(Localization.Main.getText("config.LibrarythingAuthorUrl.label")); // NOI18N
    lblLibrarythingAuthorUrl.setToolTipText(Localization.Main.getText("config.LibrarythingAuthorUrl.description")); // NOI18N
    txtLibrarythingAuthorUrl.setToolTipText(lblLibrarythingAuthorUrl.getToolTipText()); // NOI18N
    cmdLibrarythingAuthorReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingAuthorReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingIsbnUrl.setText(Localization.Main.getText("config.LibrarythingIsbnUrl.label")); // NOI18N
    lblLibrarythingIsbnUrl.setToolTipText(Localization.Main.getText("config.LibrarythingIsbnUrl.description")); // NOI18N
    txtLibrarythingIsbnUrl.setToolTipText(lblLibrarythingIsbnUrl.getToolTipText()); // NOI18N
    cmdLibrarythingIsbnReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingIsbnReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N
    lblLibrarythingTitleUrl.setText(Localization.Main.getText("config.LibrarythingTitleUrl.label")); // NOI18N
    lblLibrarythingTitleUrl.setToolTipText(Localization.Main.getText("config.LibrarythingTitleUrl.description")); // NOI18N
    txtLibrarythingTitleUrl.setToolTipText(lblLibrarythingTitleUrl.getToolTipText()); // NOI18N
    lblMaxKeywords.setText(Localization.Main.getText("config.MaxKeywords.label")); // NOI18N
    lblMaxKeywords.setToolTipText(Localization.Main.getText("config.MaxKeywords.description")); // NOI18N
    lblIndexComments.setText(Localization.Main.getText("config.IndexComments.label")); // NOI18N
    lblIndexComments.setToolTipText(Localization.Main.getText("config.IndexComments.description")); // NOI18N
    chkIndexComments.setToolTipText(lblIndexComments.getToolTipText()); // NOI18N
    lblIndexFilterAlgorithm.setText(Localization.Main.getText("config.IndexFilterAlgorithm.label")); // NOI18N
    lblIndexFilterAlgorithm.setToolTipText(Localization.Main.getText("config.IndexFilterAlgorithm.description")); // NOI18N
    cboIndexFilterAlgorithm.setToolTipText(lblIndexFilterAlgorithm.getToolTipText()); // NOI18N
    cmdLibrarythingTitleReset.setText(Localization.Main.getText("config.Reset.label")); // NOI18N
    cmdLibrarythingTitleReset.setToolTipText(Localization.Main.getText("config.Reset.description")); // NOI18N

    // Custom catalogs

    cmdAdd.setText(Localization.Main.getText("gui.add")); // NOI18N
    lblFeaturedCatalogTitle.setText(Localization.Main.getText("config.FeaturedCatalogTitle.label")); // NOI18N
    lblFeaturedCatalogTitle.setToolTipText(Localization.Main.getText("config.FeaturedCatalogTitle.description")); // NOI18N
    txtFeaturedCatalogTitle.setToolTipText(lblFeaturedCatalogTitle.getToolTipText()); // NOI18N
    lblFeaturedCatalogSavedSearchName.setText(Localization.Main.getText("config.FeaturedCatalogSavedSearchName.label")); // NOI18N
    lblFeaturedCatalogSavedSearchName.setToolTipText(Localization.Main.getText("config.FeaturedCatalogSavedSearchName.description")); // NOI18N
    txtFeaturedCatalogSavedSearchName.setToolTipText(lblFeaturedCatalogSavedSearchName.getToolTipText()); // NOI18N

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
    mnuHelpHome.setText(Localization.Main.getText("gui.menu.help.home")); // NOI18N
    mnuHelpUserGuide.setText(Localization.Main.getText("gui.menu.help.userGuide")); // NOI18N
    mnuHelpDevelopersGuide.setText(Localization.Main.getText("gui.menu.help.developerGuide")); // NOI18N
    mnuHelpOpenIssues.setText(Localization.Main.getText("gui.menu.help.issueRegister")); // NOI18N
    mnuHelpOpenForum.setText(Localization.Main.getText("gui.menu.help.supportForum")); // NOI18N
    mnuHelpOpenLocalize.setText(Localization.Main.getText("gui.menu.help.localize")); // NOI18N
    mnuHelpOpenCustomize.setText(Localization.Main.getText("gui.menu.help.customize")); // NOI18N
    mnuToolsResetSecurityCache.setText(Localization.Main.getText("gui.menu.tools.resetEncrypted")); // NOI18N
    mnuToolsOpenLog.setText(Localization.Main.getText("gui.menu.tools.logFile")); // NOI18N
    mnuToolsClearLog.setText(Localization.Main.getText("gui.menu.tools.logClear")); // NOI18N
    mnuToolsOpenConfig.setText(Localization.Main.getText("gui.menu.tools.configFolder")); // NOI18N
  }

  /**
   * Display in a popup the tooltip assoicated with a label that the user has clicked on
   * This is for convenience in environments where the tootip is not conveniently displayed.
   *
   * @param label
   */
  private void popupExplanation(JLabel label) {
    if (Helper.isNotNullOrEmpty(label.getToolTipText()))
      JOptionPane.showMessageDialog(this, label.getToolTipText(), Localization.Main.getText("gui.description"), JOptionPane.INFORMATION_MESSAGE);
  }

   private void showSetDatabaseFolderDialog() {
     JDirectoryChooser chooser = new JDirectoryChooser();
     chooser.setShowingCreateDirectory(false);
     File f = currentProfile.getDatabaseFolder();
     if (f != null && f.exists())
       chooser.setCurrentDirectory(f);
     int result = chooser.showOpenDialog(this);
     if (result == JFileChooser.CANCEL_OPTION)
       return;
     f = chooser.getSelectedFile();
     if (setDatabaseFolder(f.getAbsolutePath()))
       txtDatabaseFolder.setText(f.getAbsolutePath());
   }

   private boolean setDatabaseFolder(String targetFolder) {
     File newFolder = new File(targetFolder);
     if (newFolder.exists()) {
       File oldFolder = currentProfile.getDatabaseFolder();
       currentProfile.setDatabaseFolder(newFolder);
       if (DatabaseManager.INSTANCE.databaseExists()) {
         JOptionPane.showMessageDialog(this, Localization.Main.getText("info.databasefolderset", targetFolder), null, JOptionPane.INFORMATION_MESSAGE);
         return true;
       } else
         currentProfile.setDatabaseFolder(oldFolder);
     }
     JOptionPane.showMessageDialog(this, Localization.Main.getText("error.nodatabase", targetFolder), null, JOptionPane.ERROR_MESSAGE);
     return false;
   }

   private void showSetTargetFolderDialog() {
     JDirectoryChooser chooser = new JDirectoryChooser();
     chooser.setShowingCreateDirectory(true);
     File f = currentProfile.getTargetFolder();
     if (f != null && f.exists())
       chooser.setCurrentDirectory(f);
     else {
       f = currentProfile.getDatabaseFolder();
       if (f != null && f.exists())
         chooser.setCurrentDirectory(f);
     }
     int result = chooser.showOpenDialog(this);
     if (result == JFileChooser.CANCEL_OPTION)
       return;
     f = chooser.getSelectedFile();
     if (setTargetFolder(f.getAbsolutePath()))
       txtTargetFolder.setText(f.getAbsolutePath());
   }

   private boolean setTargetFolder(String targetFolder) {
     File newFolder = new File(targetFolder);
     if (!newFolder.exists()) {
       String message = Localization.Main.getText("error.targetdoesnotexist", targetFolder);
       int result = JOptionPane.showConfirmDialog(this, message, "", JOptionPane.YES_NO_OPTION);
       if (result != JOptionPane.YES_OPTION)
         return false;
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
     JOptionPane.showMessageDialog(this, message, "", JOptionPane.OK_OPTION);
   }

   private void exitProgram() {
     System.exit(0);
   }

   private TableModel getTblCustomCatalogsModel() {
     return customCatalogTableModel;
   }

   private void addCustomCatalog() {
     customCatalogTableModel.addCustomCatalog();
     tblCustomCatalogs.revalidate();
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
        lblUrlBooks = new javax.swing.JLabel();
        txtUrlBooks = new javax.swing.JTextField();
        lblCatalogFolder = new javax.swing.JLabel();
        txtCatalogFolder = new javax.swing.JTextField();
        lblCatalogTitle = new javax.swing.JLabel();
        txtCatalogTitle = new javax.swing.JTextField();
        lblSplittagson = new javax.swing.JLabel();
        pnlSplitTagsOn = new javax.swing.JPanel();
        txtSplittagson = new javax.swing.JTextField();
        chkDontsplittags = new javax.swing.JCheckBox();
        lblCatalogFilter = new javax.swing.JLabel();
        txtCatalogFilter = new javax.swing.JTextField();
        lblWikilang = new javax.swing.JLabel();
        txtWikilang = new javax.swing.JTextField();
        chkCopyToDatabaseFolder = new javax.swing.JCheckBox();
        lblCopyToDatabaseFolder = new javax.swing.JLabel();
        chkReprocessEpubMetadata = new javax.swing.JCheckBox();
        lblReprocessEpubMetadata = new javax.swing.JLabel();
        lblZipTrookCatalog = new javax.swing.JLabel();
        chkZipTrookCatalog = new javax.swing.JCheckBox();
        lblOnlyCatalogAtTarget = new javax.swing.JLabel();
        chkOnlyCatalogAtTarget = new javax.swing.JCheckBox();
        pnlCatalogStructure = new javax.swing.JPanel();
        lblNogeneratehtml = new javax.swing.JLabel();
        chkNogeneratehtml = new javax.swing.JCheckBox();
        lblNogeneratehtmlfiles = new javax.swing.JLabel();
        lblBrowseByCover = new javax.swing.JLabel();
        chkBrowseByCover = new javax.swing.JCheckBox();
        lblBrowseByCoverWithoutSplit = new javax.swing.JLabel();
        chkBrowseByCoverWithoutSplit = new javax.swing.JCheckBox();
        lblNoIncludeAboutLink = new javax.swing.JLabel();
        chkNoIncludeAboutLink = new javax.swing.JCheckBox();
        lblNogenerateopdsfiles = new javax.swing.JLabel();
        chkNogenerateopdsfiles = new javax.swing.JCheckBox();
        lblNogenerateratings = new javax.swing.JLabel();
        chkNogenerateratings = new javax.swing.JCheckBox();
        lblNogenerateallbooks = new javax.swing.JLabel();
        chkNogenerateallbooks = new javax.swing.JCheckBox();
        lblSupressRatings = new javax.swing.JLabel();
        chkSupressRatings = new javax.swing.JCheckBox();
        chkNogeneratehtmlfiles = new javax.swing.JCheckBox();
        chkNogenerateopds = new javax.swing.JCheckBox();
        lblNogenerateopds = new javax.swing.JLabel();
        chkNoGenerateAuthors = new javax.swing.JCheckBox();
        lblNoGenerateSeries = new javax.swing.JLabel();
        lblNogeneraterecent = new javax.swing.JLabel();
        chkNogeneraterecent = new javax.swing.JCheckBox();
        lblNoGenerateTags = new javax.swing.JLabel();
        lblNoGenerateAuthors = new javax.swing.JLabel();
        chkNoGenerateTags = new javax.swing.JCheckBox();
        chkNoGenerateSeries = new javax.swing.JCheckBox();
        lblNoShowSeries = new javax.swing.JLabel();
        chkNoShowSeries = new javax.swing.JCheckBox();
        lblOrderAllBooksBySeries = new javax.swing.JLabel();
        chkOrderAllBooksBySeries = new javax.swing.JCheckBox();
        lblSplitByAuthorInitialGoToBooks = new javax.swing.JLabel();
        chkSplitByAuthorInitialGoToBooks = new javax.swing.JCheckBox();
        lblDisplayAuthorSortInAuthorLists = new javax.swing.JLabel();
        lblDisplayTitleSortInBookLists = new javax.swing.JLabel();
        lblSortUsingAuthor = new javax.swing.JLabel();
        lblSortUsingTitle = new javax.swing.JLabel();
        chkDisplayAuthorSortInAuthorLists = new javax.swing.JCheckBox();
        chkDisplayTitleSortInBookLists = new javax.swing.JCheckBox();
        chkSortUsingAuthorSort = new javax.swing.JCheckBox();
        chkSortUsingTitleSort = new javax.swing.JCheckBox();
        lblLanguageAsTag = new javax.swing.JLabel();
        chkLanguageAsTag = new javax.swing.JCheckBox();
        lblTagsToIgnore = new javax.swing.JLabel();
        txtTagsToIgnore = new javax.swing.JTextField();
        javax.swing.JPanel pnlBookDetails = new javax.swing.JPanel();
        chkIncludeTagsInBookDetails = new javax.swing.JCheckBox();
        lblIncludeTagsInBookDetails = new javax.swing.JLabel();
        chkIncludeSeriesInBookDetails = new javax.swing.JCheckBox();
        lblIncludePublisherInBookDetails = new javax.swing.JLabel();
        lblIncludeSeriesInBookDetails = new javax.swing.JLabel();
        chkIncludePublisherInBookDetails = new javax.swing.JCheckBox();
        lblIncludePublishedInBookDetails = new javax.swing.JLabel();
        lblIncludeModifiedInBookDetails1 = new javax.swing.JLabel();
        chkIncludeModifiedInBookDetails = new javax.swing.JCheckBox();
        chkIncludePublishedInBookDetails = new javax.swing.JCheckBox();
        lblDisplayAuthorSortInBookDetails = new javax.swing.JLabel();
        lblDisplayTitleSortInBookDetails = new javax.swing.JLabel();
        chkDisplayAuthorSortInBookDetails = new javax.swing.JCheckBox();
        chkDisplayTitleSortInBookDetails = new javax.swing.JCheckBox();
        lblNogeneratecrosslinks = new javax.swing.JLabel();
        chkNogeneratecrosslinks = new javax.swing.JCheckBox();
        lblNogenerateexternallinks = new javax.swing.JLabel();
        chkNogenerateexternallinks = new javax.swing.JCheckBox();
        chkPublishedDateAsYear = new javax.swing.JCheckBox();
        lblPublishedDateAsYear = new javax.swing.JLabel();
        lblIncludeAddedInBookDetails2 = new javax.swing.JLabel();
        chkIncludeAddednBookDetails1 = new javax.swing.JCheckBox();
        lblIncludeRatingInBookDetails = new javax.swing.JLabel();
        chkIncludeRatingInBookDetails1 = new javax.swing.JCheckBox();
        txtBookDetailsCustomFields = new javax.swing.JTextField();
        lblBookDetailsCustomFields = new javax.swing.JLabel();
        chkIncludeTagCrossReferences = new javax.swing.JCheckBox();
        lblIncludeTagCrossReferences = new javax.swing.JLabel();
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
        lblThumbnailheight = new javax.swing.JLabel();
        txtThumbnailheight = new javax.swing.JTextField();
        lblMinBooksToMakeDeepLevel = new javax.swing.JLabel();
        txtMinBooksToMakeDeepLevel = new javax.swing.JTextField();
        txtCoverHeight = new javax.swing.JTextField();
        lblCoverHeight = new javax.swing.JLabel();
        lblIncludeOnlyOneFile = new javax.swing.JLabel();
        chkIncludeOnlyOneFile = new javax.swing.JCheckBox();
        txtMaxMobileResolution = new javax.swing.JTextField();
        lblMaxMobileResolution = new javax.swing.JLabel();
        lblNoCoverResize = new javax.swing.JLabel();
        lblNoThumbnailGenerate = new javax.swing.JLabel();
        chkNoCoverResize = new javax.swing.JCheckBox();
        chkNoThumbnailGenerate = new javax.swing.JCheckBox();
        txtMaxKeywords = new javax.swing.JTextField();
        lblMaxKeywords = new javax.swing.JLabel();
        lblIndexComments = new javax.swing.JLabel();
        chkIndexComments = new javax.swing.JCheckBox();
        lblIndexFilterAlgorithm = new javax.swing.JLabel();
        cboIndexFilterAlgorithm = new javax.swing.JComboBox();
        lblGenerateIndex = new javax.swing.JLabel();
        chkGenerateIndex = new javax.swing.JCheckBox();
        lblMaxBookSummaryLength = new javax.swing.JLabel();
        txtMaxBookSummaryLength = new javax.swing.JTextField();
        lblMinimizeChangedFiles = new javax.swing.JLabel();
        chkMinimizeChangedFiles = new javax.swing.JCheckBox();
        lblExternalIcons = new javax.swing.JLabel();
        chkExternalIcons = new javax.swing.JCheckBox();
        lblMaxSplitLevels = new javax.swing.JLabel();
        txtMaxSplitLevels = new javax.swing.JTextField();
        lblCryptFilenames = new javax.swing.JLabel();
        chkCryptFilenames = new javax.swing.JCheckBox();
        txtTagsToMakeDeep = new javax.swing.JTextField();
        lblTagsToMakeDeep = new javax.swing.JLabel();
        lblIncludeCoversInCatalog = new javax.swing.JLabel();
        chkIncludeCoversInCatalog = new javax.swing.JCheckBox();
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
        pnlCustomCatalogs = new javax.swing.JPanel();
        lblCustomDummy1 = new javax.swing.JLabel();
        lblFeaturedCatalogSavedSearchName = new javax.swing.JLabel();
        txtFeaturedCatalogSavedSearchName = new javax.swing.JTextField();
        lblFeaturedCatalogTitle = new javax.swing.JLabel();
        txtFeaturedCatalogTitle = new javax.swing.JTextField();
        lblCustomDummy2 = new javax.swing.JLabel();
        cmdAdd = new javax.swing.JButton();
        scrCustomCatalogs = new javax.swing.JScrollPane();
        tblCustomCatalogs = new javax.swing.JTable();
        pnlCustomCatalogsTableButtons = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        lblBottom0 = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        cmdCancel = new javax.swing.JButton();
        cmdReset = new javax.swing.JButton();
        cmdSave = new javax.swing.JButton();
        cmdGenerate = new javax.swing.JButton();
        cmdHelp = new javax.swing.JButton();
        pnlTitle = new javax.swing.JPanel();
        lblCurrentProfile = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuFileSave = new javax.swing.JMenuItem();
        mnuFileGenerateCatalogs = new javax.swing.JMenuItem();
        mnuFileExit = new javax.swing.JMenuItem();
        mnuProfiles = new javax.swing.JMenu();
        mnuTools = new javax.swing.JMenu();
        mnuToolsprocessEpubMetadataOfAllBooks = new javax.swing.JMenuItem();
        mnuToolsResetSecurityCache = new javax.swing.JMenuItem();
        mnuToolsOpenLog = new javax.swing.JMenuItem();
        mnuToolsClearLog = new javax.swing.JMenuItem();
        mnuToolsOpenConfig = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuHelpDonate = new javax.swing.JMenuItem();
        mnuHelpHome = new javax.swing.JMenuItem();
        mnuHelpOpenForum = new javax.swing.JMenuItem();
        mnuHelpOpenIssues = new javax.swing.JMenuItem();
        mnuHelpUserGuide = new javax.swing.JMenuItem();
        mnuHelpDevelopersGuide = new javax.swing.JMenuItem();
        mnuHelpOpenLocalize = new javax.swing.JMenuItem();
        mnuHelpOpenCustomize = new javax.swing.JMenuItem();
        mnuHelpAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1000, 600));

        pnlMain.setMinimumSize(new java.awt.Dimension(910, 600));
        pnlMain.setPreferredSize(new java.awt.Dimension(900, 800));
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
        lblDeviceMode1.setText("lblDeviceMode1");
        lblDeviceMode1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblDeviceMode1.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        pnlMain.add(lblDeviceMode1, gridBagConstraints);

        lblDeviceMode2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDeviceMode2.setText("lblDeviceMode2");
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(lblDonate, gridBagConstraints);

        tabOptionsTabs.setMinimumSize(new java.awt.Dimension(900, 450));
        tabOptionsTabs.setPreferredSize(new java.awt.Dimension(824, 660));

        pnlMainOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlMainOptionsComponentShown(evt);
            }
        });
        pnlMainOptions.setLayout(new java.awt.GridBagLayout());

        lblLang.setText("lblLang");
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

        lblDatabaseFolder.setText("lblDatabaseFolder");
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
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cmdSetDatabaseFolder, gridBagConstraints);

        lblTargetFolder.setText("lblDatabaseFolder");
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
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cmdSetTargetFolder, gridBagConstraints);

        lblUrlBooks.setText("lblUrlBooks");
        lblUrlBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblUrlBooks, gridBagConstraints);

        txtUrlBooks.setText("txtUrlBooks");
        txtUrlBooks.setPreferredSize(new java.awt.Dimension(400, 20));
        txtUrlBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckOnlyCatalogAllowed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtUrlBooks, gridBagConstraints);

        lblCatalogFolder.setText("lblCatalogFolder");
        lblCatalogFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogFolder, gridBagConstraints);

        txtCatalogFolder.setText("txtCatalogFolder");
        txtCatalogFolder.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtCatalogFolder, gridBagConstraints);

        lblCatalogTitle.setText("lblCatalogTitle");
        lblCatalogTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogTitle, gridBagConstraints);

        txtCatalogTitle.setText("txtCatalogTitle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtCatalogTitle, gridBagConstraints);

        lblSplittagson.setText("lblSplittagson");
        lblSplittagson.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
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

        chkDontsplittags.setText("chkDontsplittags");
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlMainOptions.add(pnlSplitTagsOn, gridBagConstraints);

        lblCatalogFilter.setText("lblCatalogFilter");
        lblCatalogFilter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogFilter, gridBagConstraints);

        txtCatalogFilter.setText("txtCatalogFilter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtCatalogFilter, gridBagConstraints);

        lblWikilang.setText("lblWikilang");
        lblWikilang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblWikilang, gridBagConstraints);

        txtWikilang.setText("txtWikilang");
        txtWikilang.setPreferredSize(new java.awt.Dimension(60, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtWikilang, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkCopyToDatabaseFolder, gridBagConstraints);

        lblCopyToDatabaseFolder.setText("lblCopyToDatabaseFolder");
        lblCopyToDatabaseFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblCopyToDatabaseFolder, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkReprocessEpubMetadata, gridBagConstraints);

        lblReprocessEpubMetadata.setText("lblReprocessEpubMetadata");
        lblReprocessEpubMetadata.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblReprocessEpubMetadata, gridBagConstraints);

        lblZipTrookCatalog.setText("lblZipTrookCatalog");
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
        pnlMainOptions.add(lblZipTrookCatalog, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkZipTrookCatalog, gridBagConstraints);

        lblOnlyCatalogAtTarget.setText("lblOnlyCatalogAtTarget");
        lblOnlyCatalogAtTarget.setToolTipText("");
        lblOnlyCatalogAtTarget.setAutoscrolls(true);
        lblOnlyCatalogAtTarget.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblOnlyCatalogAtTarget, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkOnlyCatalogAtTarget, gridBagConstraints);

        tabOptionsTabs.addTab("pnlMainOptions", pnlMainOptions);
        pnlMainOptions.getAccessibleContext().setAccessibleName("Main Option");

        pnlCatalogStructure.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlCatalogStructureComponentShown(evt);
            }
        });
        pnlCatalogStructure.setLayout(new java.awt.GridBagLayout());

        lblNogeneratehtml.setText("lblNogeneratehtml");
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
        pnlCatalogStructure.add(lblNogeneratehtml, gridBagConstraints);

        chkNogeneratehtml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkDownloads(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogeneratehtml, gridBagConstraints);

        lblNogeneratehtmlfiles.setText("lblNogeneratehtmlfiles");
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
        pnlCatalogStructure.add(lblNogeneratehtmlfiles, gridBagConstraints);

        lblBrowseByCover.setText("lblBrowseByCover");
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
        pnlCatalogStructure.add(lblBrowseByCover, gridBagConstraints);

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
        pnlCatalogStructure.add(chkBrowseByCover, gridBagConstraints);

        lblBrowseByCoverWithoutSplit.setText("lblBrowseByCoverWithoutSplit");
        lblBrowseByCoverWithoutSplit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        pnlCatalogStructure.add(lblBrowseByCoverWithoutSplit, gridBagConstraints);
        lblBrowseByCoverWithoutSplit.getAccessibleContext().setAccessibleName("Do not split by letter in \"Browse by Cover\" mode");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkBrowseByCoverWithoutSplit, gridBagConstraints);

        lblNoIncludeAboutLink.setText("lblNoIncludeAboutLink");
        lblNoIncludeAboutLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNoIncludeAboutLink, gridBagConstraints);
        lblNoIncludeAboutLink.getAccessibleContext().setAccessibleName("Include the \"About calibre2opds\" link");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNoIncludeAboutLink, gridBagConstraints);

        lblNogenerateopdsfiles.setText("lblNogenerateopdsfiles");
        lblNogenerateopdsfiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNogenerateopdsfiles, gridBagConstraints);
        lblNogenerateopdsfiles.getAccessibleContext().setAccessibleName("Do not generate OPDS downloads");

        chkNogenerateopdsfiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkDownloads(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogenerateopdsfiles, gridBagConstraints);

        lblNogenerateratings.setText("lblNogenerateratings");
        lblNogenerateratings.setRequestFocusEnabled(false);
        lblNogenerateratings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNogenerateratings, gridBagConstraints);
        lblNogenerateratings.getAccessibleContext().setAccessibleName("Do not generate the \"Ratings\" catalog ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogenerateratings, gridBagConstraints);

        lblNogenerateallbooks.setText("lblNogenerateallbooks");
        lblNogenerateallbooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNogenerateallbooks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogenerateallbooks, gridBagConstraints);

        lblSupressRatings.setText("lblSupressRatings");
        lblSupressRatings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSupressRatings, gridBagConstraints);
        lblSupressRatings.getAccessibleContext().setAccessibleName("Suppress ratings in the books titles ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSupressRatings, gridBagConstraints);

        chkNogeneratehtmlfiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkDownloads(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogeneratehtmlfiles, gridBagConstraints);

        chkNogenerateopds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkDownloads(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogenerateopds, gridBagConstraints);

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
        pnlCatalogStructure.add(lblNogenerateopds, gridBagConstraints);
        lblNogenerateopds.getAccessibleContext().setAccessibleName("Di Not generate OPDS catalogs");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNoGenerateAuthors, gridBagConstraints);

        lblNoGenerateSeries.setText("lblNoGenerateSeries");
        lblNoGenerateSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNoGenerateSeries, gridBagConstraints);

        lblNogeneraterecent.setText("lblNogeneraterecent");
        lblNogeneraterecent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNogeneraterecent, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNogeneraterecent, gridBagConstraints);

        lblNoGenerateTags.setText("lblNoGenerateTags");
        lblNoGenerateTags.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNoGenerateTags, gridBagConstraints);

        lblNoGenerateAuthors.setText("lblNoGenerateAuthors");
        lblNoGenerateAuthors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNoGenerateAuthors, gridBagConstraints);

        chkNoGenerateTags.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNoGenerateTagsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNoGenerateTags, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNoGenerateSeries, gridBagConstraints);

        lblNoShowSeries.setText("lblNoShowSeries");
        lblNoShowSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblNoShowSeries, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkNoShowSeries, gridBagConstraints);

        lblOrderAllBooksBySeries.setText("lblOrderAllBooksBySeries");
        lblOrderAllBooksBySeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblOrderAllBooksBySeries, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkOrderAllBooksBySeries, gridBagConstraints);

        lblSplitByAuthorInitialGoToBooks.setText("lblSplitByAuthorInitialGoToBooks");
        lblSplitByAuthorInitialGoToBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSplitByAuthorInitialGoToBooks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSplitByAuthorInitialGoToBooks, gridBagConstraints);

        lblDisplayAuthorSortInAuthorLists.setText("lblDisplayAuthorSortInAuthorLists");
        lblDisplayAuthorSortInAuthorLists.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblDisplayAuthorSortInAuthorLists, gridBagConstraints);

        lblDisplayTitleSortInBookLists.setText("lblDisplayTitleSortInBookLists");
        lblDisplayTitleSortInBookLists.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblDisplayTitleSortInBookLists, gridBagConstraints);

        lblSortUsingAuthor.setText("lblSortUsingAuthor");
        lblSortUsingAuthor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSortUsingAuthor, gridBagConstraints);

        lblSortUsingTitle.setText("lblSortUsingTitle");
        lblSortUsingTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSortUsingTitle, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkDisplayAuthorSortInAuthorLists, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkDisplayTitleSortInBookLists, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSortUsingAuthorSort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSortUsingTitleSort, gridBagConstraints);

        lblLanguageAsTag.setText("lblLanguageAsTag");
        lblLanguageAsTag.setToolTipText("");
        lblLanguageAsTag.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblLanguageAsTag, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkLanguageAsTag, gridBagConstraints);

        lblTagsToIgnore.setText("lblTagsToIgnore");
        lblTagsToIgnore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblTagsToIgnore, gridBagConstraints);

        txtTagsToIgnore.setText("txtTagsToIgnore");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(txtTagsToIgnore, gridBagConstraints);

        tabOptionsTabs.addTab("pnlCatalogStructure", pnlCatalogStructure);

        pnlBookDetails.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlBookDetailsComponentShown(evt);
            }
        });
        pnlBookDetails.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeTagsInBookDetails, gridBagConstraints);

        lblIncludeTagsInBookDetails.setText("lblIncludeTagsInBookDetails");
        lblIncludeTagsInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeTagsInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeSeriesInBookDetails, gridBagConstraints);

        lblIncludePublisherInBookDetails.setText("lblIncludePublisherInBookDetails");
        lblIncludePublisherInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludePublisherInBookDetails, gridBagConstraints);

        lblIncludeSeriesInBookDetails.setText("lblIncludeSeriesInBookDetails");
        lblIncludeSeriesInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeSeriesInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludePublisherInBookDetails, gridBagConstraints);

        lblIncludePublishedInBookDetails.setText("lblIncludePublishedInBookDetails");
        lblIncludePublishedInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludePublishedInBookDetails, gridBagConstraints);

        lblIncludeModifiedInBookDetails1.setText("lblIncludeModifiedInBookDetails");
        lblIncludeModifiedInBookDetails1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeModifiedInBookDetails1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeModifiedInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludePublishedInBookDetails, gridBagConstraints);

        lblDisplayAuthorSortInBookDetails.setText("lblDisplayAuthorSortInBookDetails");
        lblDisplayAuthorSortInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblDisplayAuthorSortInBookDetails, gridBagConstraints);
        lblDisplayAuthorSortInBookDetails.getAccessibleContext().setAccessibleName("");

        lblDisplayTitleSortInBookDetails.setText("lblDisplayTitleSortInBookDetails");
        lblDisplayTitleSortInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblDisplayTitleSortInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkDisplayAuthorSortInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkDisplayTitleSortInBookDetails, gridBagConstraints);

        lblNogeneratecrosslinks.setText("lblNogeneratecrosslinks");
        lblNogeneratecrosslinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblNogeneratecrosslinks, gridBagConstraints);
        lblNogeneratecrosslinks.getAccessibleContext().setAccessibleName("Do not generate cross-reference links ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkNogeneratecrosslinks, gridBagConstraints);

        lblNogenerateexternallinks.setText("lblNogenerateexternallinks");
        lblNogenerateexternallinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblNogenerateexternallinks, gridBagConstraints);

        chkNogenerateexternallinks.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkNogenerateexternallinksStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkNogenerateexternallinks, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkPublishedDateAsYear, gridBagConstraints);

        lblPublishedDateAsYear.setText("lblPublishedDateAsYear");
        lblPublishedDateAsYear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblPublishedDateAsYear, gridBagConstraints);

        lblIncludeAddedInBookDetails2.setText("lblIncludeAddedInBookDetails");
        lblIncludeAddedInBookDetails2.setOpaque(true);
        lblIncludeAddedInBookDetails2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeAddedInBookDetails2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeAddednBookDetails1, gridBagConstraints);

        lblIncludeRatingInBookDetails.setText("lblIncludeRatingInBookDetails");
        lblIncludeRatingInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeRatingInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeRatingInBookDetails1, gridBagConstraints);

        txtBookDetailsCustomFields.setText("txtBookDetailsCustomFields");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(txtBookDetailsCustomFields, gridBagConstraints);

        lblBookDetailsCustomFields.setText("lblBookDetailsCustomFields");
        lblBookDetailsCustomFields.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblBookDetailsCustomFields, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeTagCrossReferences, gridBagConstraints);

        lblIncludeTagCrossReferences.setText("lblIncludeTagCrossReferences");
        lblIncludeTagCrossReferences.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeTagCrossReferences, gridBagConstraints);

        tabOptionsTabs.addTab("pnlBookDetails", pnlBookDetails);

        pnlAdvancedOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlAdvancedOptionsComponentShown(evt);
            }
        });
        pnlAdvancedOptions.setLayout(new java.awt.GridBagLayout());

        lblIncludeformat.setText("lblIncludeformat");
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

        lblMaxbeforepaginate.setText("lblMaxbeforepaginate");
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
        txtMaxbeforepaginate.setMaximumSize(new java.awt.Dimension(50, 22));
        txtMaxbeforepaginate.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxbeforepaginate, gridBagConstraints);

        lblMaxbeforesplit.setText("lblMaxbeforesplit");
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
        txtMaxbeforesplit.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxbeforesplit, gridBagConstraints);

        lblBooksinrecent.setText("lblBooksinrecent");
        lblBooksinrecent.setMaximumSize(new java.awt.Dimension(115, 250));
        lblBooksinrecent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblBooksinrecent, gridBagConstraints);

        txtBooksinrecent.setText("txtBooksinrecent");
        txtBooksinrecent.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtBooksinrecent, gridBagConstraints);

        lblMaxsummarylength.setText("lblMaxsummarylength");
        lblMaxsummarylength.setMaximumSize(new java.awt.Dimension(250, 20));
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

        lblIncludeemptybooks.setText("lblIncludeemptybooks");
        lblIncludeemptybooks.setInheritsPopupMenu(false);
        lblIncludeemptybooks.setMaximumSize(new java.awt.Dimension(250, 20));
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

        lblThumbnailheight.setText("lblThumbnailheight");
        lblThumbnailheight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblThumbnailheight, gridBagConstraints);

        txtThumbnailheight.setText("txtThumbnailheight");
        txtThumbnailheight.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtThumbnailheight, gridBagConstraints);

        lblMinBooksToMakeDeepLevel.setText("lblMinBooksToMakeDeepLevel");
        lblMinBooksToMakeDeepLevel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMinBooksToMakeDeepLevel, gridBagConstraints);

        txtMinBooksToMakeDeepLevel.setText("txtMaxsummarylength");
        txtMinBooksToMakeDeepLevel.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMinBooksToMakeDeepLevel, gridBagConstraints);

        txtCoverHeight.setText("txtCoverHeight");
        txtCoverHeight.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtCoverHeight, gridBagConstraints);

        lblCoverHeight.setText("lblCoverHeight");
        lblCoverHeight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblCoverHeight, gridBagConstraints);

        lblIncludeOnlyOneFile.setText("lblIncludeOnlyOneFile");
        lblIncludeOnlyOneFile.setMaximumSize(new java.awt.Dimension(250, 20));
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

        txtMaxMobileResolution.setText("txtMaxMobileResolution");
        txtMaxMobileResolution.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtMaxMobileResolution(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxMobileResolution, gridBagConstraints);

        lblNoCoverResize.setText("lblNoCoverResize");
        lblNoCoverResize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblNoCoverResize, gridBagConstraints);

        lblNoThumbnailGenerate.setText("lblNoThumbnailGenerate");
        lblNoThumbnailGenerate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkNoThumbnailGenerate, gridBagConstraints);

        txtMaxKeywords.setText("txtMaxKeywords");
        txtMaxKeywords.setPreferredSize(new java.awt.Dimension(187, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlAdvancedOptions.add(txtMaxKeywords, gridBagConstraints);

        lblMaxKeywords.setText("lblMaxKeywords");
        lblMaxKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxKeywords, gridBagConstraints);

        lblIndexComments.setText("lblIndexComments");
        lblIndexComments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIndexComments, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkIndexComments, gridBagConstraints);

        lblIndexFilterAlgorithm.setText("lblIndexFilterAlgorithm");
        lblIndexFilterAlgorithm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlAdvancedOptions.add(lblIndexFilterAlgorithm, gridBagConstraints);

        cboIndexFilterAlgorithm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboIndexFilterAlgorithm.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlAdvancedOptions.add(cboIndexFilterAlgorithm, gridBagConstraints);

        lblGenerateIndex.setText("lblGenerateIndex");
        lblGenerateIndex.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlAdvancedOptions.add(lblGenerateIndex, gridBagConstraints);

        chkGenerateIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGenerateIndexActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkGenerateIndex, gridBagConstraints);

        lblMaxBookSummaryLength.setText("lblMaxBookSummaryLength");
        lblMaxBookSummaryLength.setMaximumSize(new java.awt.Dimension(250, 20));
        lblMaxBookSummaryLength.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxBookSummaryLength, gridBagConstraints);

        txtMaxBookSummaryLength.setText("txtMaxBookSummaryLength");
        txtMaxBookSummaryLength.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxBookSummaryLength, gridBagConstraints);

        lblMinimizeChangedFiles.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMinimizeChangedFiles.setText("lblMinimizeChangedFiles");
        lblMinimizeChangedFiles.setToolTipText("");
        lblMinimizeChangedFiles.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lblMinimizeChangedFiles.setInheritsPopupMenu(false);
        lblMinimizeChangedFiles.setMaximumSize(new java.awt.Dimension(250, 16));
        lblMinimizeChangedFiles.setMinimumSize(new java.awt.Dimension(155, 10));
        lblMinimizeChangedFiles.setPreferredSize(new java.awt.Dimension(250, 16));
        lblMinimizeChangedFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlAdvancedOptions.add(lblMinimizeChangedFiles, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkMinimizeChangedFiles, gridBagConstraints);

        lblExternalIcons.setText("lblExternalIcons");
        lblExternalIcons.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblExternalIcons, gridBagConstraints);

        chkExternalIcons.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkExternalIcons, gridBagConstraints);

        lblMaxSplitLevels.setText("lblMaxSplitlevels");
        lblMaxSplitLevels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMaxSplitLevels, gridBagConstraints);

        txtMaxSplitLevels.setText("txtMaxSplitLevels");
        txtMaxSplitLevels.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxSplitLevels, gridBagConstraints);

        lblCryptFilenames.setText("lblCryptFilenames");
        lblCryptFilenames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblCryptFilenames, gridBagConstraints);
        lblCryptFilenames.getAccessibleContext().setAccessibleName("Encrypt the filenames ");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkCryptFilenames, gridBagConstraints);

        txtTagsToMakeDeep.setText("txtTagsToMakeDeep");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtTagsToMakeDeep, gridBagConstraints);

        lblTagsToMakeDeep.setText("lblTagsToMakeDeep");
        lblTagsToMakeDeep.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblTagsToMakeDeep, gridBagConstraints);

        lblIncludeCoversInCatalog.setText("lblIncludeCoversInCatalog");
        lblIncludeCoversInCatalog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIncludeCoversInCatalog, gridBagConstraints);

        chkIncludeCoversInCatalog.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkIncludeCoversInCatalog, gridBagConstraints);

        tabOptionsTabs.addTab("pnlAdvancedOptions", pnlAdvancedOptions);

        pnlExternalUrlsOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlExternalUrlsOptionsComponentShown(evt);
            }
        });
        pnlExternalUrlsOptions.setLayout(new java.awt.GridBagLayout());

        lblWikipediaUrl.setText("lblWikipediaUrl");
        lblWikipediaUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblWikipediaUrl, gridBagConstraints);

        txtWikipediaUrl.setText("txtWikipediaUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtWikipediaUrl, gridBagConstraints);

        lblAmazonAuthorUrl.setText("lblAmazonAuthorUrl");
        lblAmazonAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonAuthorUrl, gridBagConstraints);

        txtAmazonAuthorUrl.setText("txtAmazonAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonAuthorUrl, gridBagConstraints);

        lblAmazonIsbnUrl.setText("lblAmazonIsbnUrl");
        lblAmazonIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonIsbnUrl, gridBagConstraints);

        txtAmazonIsbnUrl.setText("txtAmazonIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonIsbnUrl, gridBagConstraints);

        lblAmazonTitleUrl.setText("lblAmazonTitleUrl");
        lblAmazonTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonTitleUrl, gridBagConstraints);

        txtAmazonTitleUrl.setText("txtAmazonTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonTitleUrl, gridBagConstraints);

        lblGoodreadAuthorUrl.setText("lblGoodreadAuthorUrl");
        lblGoodreadAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadAuthorUrl, gridBagConstraints);

        txtGoodreadAuthorUrl.setText("txtGoodreadAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadAuthorUrl, gridBagConstraints);

        lblGoodreadIsbnUrl.setText("lblGoodreadIsbnUrl");
        lblGoodreadIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadIsbnUrl, gridBagConstraints);

        txtGoodreadIsbnUrl.setText("txtGoodreadIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadIsbnUrl, gridBagConstraints);

        lblGoodreadTitleUrl.setText("lblGoodreadTitleUrl");
        lblGoodreadTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadTitleUrl, gridBagConstraints);

        txtGoodreadTitleUrl.setText("txtGoodreadTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadTitleUrl, gridBagConstraints);

        lblGoodreadReviewIsbnUrl.setText("lblGoodreadReviewIsbnUrl");
        lblGoodreadReviewIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadReviewIsbnUrl, gridBagConstraints);

        txtGoodreadReviewIsbnUrl.setText("txtGoodreadReviewIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadReviewIsbnUrl, gridBagConstraints);

        lblIsfdbAuthorUrl.setText("lblIsfdbAuthorUrl");
        lblIsfdbAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblIsfdbAuthorUrl, gridBagConstraints);

        txtIsfdbAuthorUrl.setText("txtIsfdbAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtIsfdbAuthorUrl, gridBagConstraints);

        lblLibrarythingAuthorUrl.setText("lblLibrarythingAuthorUrl");
        lblLibrarythingAuthorUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingAuthorUrl, gridBagConstraints);

        txtLibrarythingAuthorUrl.setText("txtLibrarythingAuthorUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingAuthorUrl, gridBagConstraints);

        lblLibrarythingIsbnUrl.setText("lblLibrarythingIsbnUrl");
        lblLibrarythingIsbnUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingIsbnUrl, gridBagConstraints);

        txtLibrarythingIsbnUrl.setText("txtLibrarythingIsbnUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingIsbnUrl, gridBagConstraints);

        lblLibrarythingTitleUrl.setText("lblLibrarythingTitleUrl");
        lblLibrarythingTitleUrl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingTitleUrl, gridBagConstraints);

        txtLibrarythingTitleUrl.setText("txtLibrarythingTitleUrl");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
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
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 4;
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
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 5;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 8;
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
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 9;
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
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 11;
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
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingTitleReset, gridBagConstraints);

        tabOptionsTabs.addTab("pnlExternalUrlsOptions", pnlExternalUrlsOptions);

        pnlCustomCatalogs.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlCustomCatalogsComponentShown(evt);
            }
        });
        pnlCustomCatalogs.setLayout(new java.awt.GridBagLayout());

        lblCustomDummy1.setText("     ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(lblCustomDummy1, gridBagConstraints);

        lblFeaturedCatalogSavedSearchName.setText("lblFeaturedCatalogSavedSearchName");
        lblFeaturedCatalogSavedSearchName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(lblFeaturedCatalogSavedSearchName, gridBagConstraints);

        txtFeaturedCatalogSavedSearchName.setText("txtFeaturedCatalogSavedSearchName");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(txtFeaturedCatalogSavedSearchName, gridBagConstraints);

        lblFeaturedCatalogTitle.setText("lblFeaturedCatalogTitle");
        lblFeaturedCatalogTitle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(lblFeaturedCatalogTitle, gridBagConstraints);

        txtFeaturedCatalogTitle.setText("txtFeaturedCatalogTitle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(txtFeaturedCatalogTitle, gridBagConstraints);

        lblCustomDummy2.setText("     ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCustomCatalogs.add(lblCustomDummy2, gridBagConstraints);

        cmdAdd.setText("cmdAdd");
        cmdAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddCustomCatalogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlCustomCatalogs.add(cmdAdd, gridBagConstraints);

        tblCustomCatalogs.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        tblCustomCatalogs.setModel(getTblCustomCatalogsModel());
        tblCustomCatalogs.setColumnSelectionAllowed(true);
        tblCustomCatalogs.setRowHeight(30);
        tblCustomCatalogs.setRowMargin(3);
        addDeleteButtonToCustomCatalogsTable();
        scrCustomCatalogs.setViewportView(tblCustomCatalogs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pnlCustomCatalogs.add(scrCustomCatalogs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlCustomCatalogs.add(pnlCustomCatalogsTableButtons, gridBagConstraints);

        tabOptionsTabs.addTab("pnlCustomCatalogs", pnlCustomCatalogs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        pnlMain.add(tabOptionsTabs, gridBagConstraints);
        tabOptionsTabs.getAccessibleContext().setAccessibleName("Catalog Structure");

        pnlBottom.setLayout(new java.awt.GridBagLayout());

        lblBottom0.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblBottom0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBottom0.setText("lblBottom0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBottom.add(lblBottom0, gridBagConstraints);

        cmdCancel.setText("cmdCancel");
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdCancel);

        cmdReset.setText("cmdReset");
        cmdReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdResetActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdReset);

        cmdSave.setText("cmdSave");
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdSave);

        cmdGenerate.setText("cmdGenerate");
        cmdGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdGenerateActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdGenerate);

        cmdHelp.setText("cmdHelp");
        cmdHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdHelpActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdHelp);

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

        lblCurrentProfile.setText("lblCurrentProfile");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlMain.add(lblCurrentProfile, gridBagConstraints);

        getContentPane().add(pnlMain, java.awt.BorderLayout.CENTER);

        mnuFile.setText("mnuFile");

        mnuFileSave.setText("mnuFileSave");
        mnuFileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileSaveActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileSave);

        mnuFileGenerateCatalogs.setText("mnuFileGenerateCatalogs");
        mnuFileGenerateCatalogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileGenerateCatalogsActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileGenerateCatalogs);

        mnuFileExit.setText("mnuFileExit");
        mnuFileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuFileExitActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileExit);

        jMenuBar1.add(mnuFile);

        mnuProfiles.setText("mnuProfiles");
        jMenuBar1.add(mnuProfiles);

        mnuTools.setText("mnuTools");

        mnuToolsprocessEpubMetadataOfAllBooks.setText("mnuToolsprocessEpubMetadataOfAllBooks");
        mnuToolsprocessEpubMetadataOfAllBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsprocessEpubMetadataOfAllBooksActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsprocessEpubMetadataOfAllBooks);

        mnuToolsResetSecurityCache.setText("mnuToolsResetSecurityCache");
        mnuToolsResetSecurityCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsResetSecurityCacheActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsResetSecurityCache);

        mnuToolsOpenLog.setText("mnuToolsOpenLog");
        mnuToolsOpenLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsOpenLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsOpenLog);

        mnuToolsClearLog.setText("mnuToolsClearLog");
        mnuToolsClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsClearLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsClearLog);

        mnuToolsOpenConfig.setText("mnuToolsOpenConfig");
        mnuToolsOpenConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsOpenConfigActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsOpenConfig);

        jMenuBar1.add(mnuTools);

        mnuHelp.setText("mnuHelp");

        mnuHelpDonate.setText("mnuHelpDonate");
        mnuHelpDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpDonateActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpDonate);

        mnuHelpHome.setText("mnuHelpHome");
        mnuHelpHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpHomeActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpHome);

        mnuHelpOpenForum.setText("mnuHelpOpenForum");
        mnuHelpOpenForum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenForumActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenForum);

        mnuHelpOpenIssues.setText("mnuHelpOpenIssues");
        mnuHelpOpenIssues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenIssuesActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenIssues);

        mnuHelpUserGuide.setText("mnuHelpUserGuide");
        mnuHelpUserGuide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpUserGuideActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpUserGuide);

        mnuHelpDevelopersGuide.setText("mnuHelpDevelopersGuide");
        mnuHelpDevelopersGuide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpDevelopersGuideActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpDevelopersGuide);

        mnuHelpOpenLocalize.setText("mnuHelpOpenLocalize");
        mnuHelpOpenLocalize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenLocalizeActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenLocalize);

        mnuHelpOpenCustomize.setText("mnuHelpOpenCustomize");
        mnuHelpOpenCustomize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuHelpOpenCustomizeActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuHelpOpenCustomize);

        mnuHelpAbout.setText("mnuHelpAbout");
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

  private void mnuToolsprocessEpubMetadataOfAllBooksActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event_mnuToolsprocessEpubMetadataOfAllBooksActionPerformed
    processEpubMetadataOfAllBooks();
  }//GEN-LAST:event_mnuToolsprocessEpubMetadataOfAllBooksActionPerformed

  private void txtMaxMobileResolution(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtMaxMobileResolution
    JOptionPane.showMessageDialog(null, Localization.Main.getText("error.notYetReady"), "Warning", JOptionPane.WARNING_MESSAGE);
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

  private void cmdAmazonUrlResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonUrlResetActionPerformed
    txtAmazonAuthorUrl.setText(Localization.Main.getText(StanzaConstants.AMAZON_AUTHORS_URL_DEFAULT));
  }//GEN-LAST:event_cmdAmazonUrlResetActionPerformed

  private void cmdAmazonIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonIsbnResetActionPerformed
    txtAmazonIsbnUrl.setText(Localization.Main.getText(StanzaConstants.AMAZON_ISBN_URL_DEFAULT));
  }//GEN-LAST:event_cmdAmazonIsbnResetActionPerformed

  private void cmdAmazonTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonTitleResetActionPerformed
    txtAmazonTitleUrl.setText(Localization.Main.getText(StanzaConstants.AMAZON_TITLE_URL_DEFAULT));
  }//GEN-LAST:event_cmdAmazonTitleResetActionPerformed

  private void cmdGoodreadAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadAuthorResetActionPerformed
    txtGoodreadAuthorUrl.setText(Localization.Main.getText(StanzaConstants.GOODREADS_AUTHOR_URL_DEFAULT));
  }//GEN-LAST:event_cmdGoodreadAuthorResetActionPerformed

  private void cmdGoodreadIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadIsbnResetActionPerformed
    txtGoodreadIsbnUrl.setText(Localization.Main.getText(StanzaConstants.GOODREADS_ISBN_URL_DEFAULT));
  }//GEN-LAST:event_cmdGoodreadIsbnResetActionPerformed

  private void cmdGoodreadTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadTitleResetActionPerformed
    txtGoodreadTitleUrl.setText(Localization.Main.getText(StanzaConstants.GOODREADS_TITLE_URL_DEFAULT));
  }//GEN-LAST:event_cmdGoodreadTitleResetActionPerformed

  private void cmdGoodreadReviewResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadReviewResetActionPerformed
    txtGoodreadReviewIsbnUrl.setText(Localization.Main.getText(StanzaConstants.GOODREADS_REVIEW_URL_DEFAULT));
  }//GEN-LAST:event_cmdGoodreadReviewResetActionPerformed

  private void cmdIsfdbAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdIsfdbAuthorResetActionPerformed
    txtIsfdbAuthorUrl.setText(Localization.Main.getText(StanzaConstants.ISFDB_AUTHOR_URL_DEFAULT));
  }//GEN-LAST:event_cmdIsfdbAuthorResetActionPerformed

  private void cmdLibrarythingAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingAuthorResetActionPerformed
    txtLibrarythingAuthorUrl.setText(Localization.Main.getText(StanzaConstants.LIBRARYTHING_AUTHOR_URL_DEFAULT));
  }//GEN-LAST:event_cmdLibrarythingAuthorResetActionPerformed

  private void cmdLibrarythingIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingIsbnResetActionPerformed
    txtLibrarythingIsbnUrl.setText(Localization.Main.getText(StanzaConstants.LIBRARYTHING_ISBN_URL_DEFAULT));
  }//GEN-LAST:event_cmdLibrarythingIsbnResetActionPerformed

  private void cmdLibrarythingTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingTitleResetActionPerformed
    txtLibrarythingTitleUrl.setText(Localization.Main.getText(StanzaConstants.LIBRARYTHING_TITLE_URL_DEFAULT));
  }//GEN-LAST:event_cmdLibrarythingTitleResetActionPerformed

  private void cmdWikipediaUrlResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWikipediaUrlResetActionPerformed
    txtWikipediaUrl.setText(Localization.Main.getText(StanzaConstants.WIKIPEDIA_URL_DEFAULT));
  }//GEN-LAST:event_cmdWikipediaUrlResetActionPerformed

  private void chkGenerateIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGenerateIndexActionPerformed
    actOnGenerateIndexActionPerformed();
  }//GEN-LAST:event_chkGenerateIndexActionPerformed

  private void cmdAddCustomCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddCustomCatalogActionPerformed
    addCustomCatalog();
  }//GEN-LAST:event_cmdAddCustomCatalogActionPerformed

  private void chkNogenerateexternallinksStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkNogenerateexternallinksStateChanged
    setExternalLinksEnabledState();
  }//GEN-LAST:event_chkNogenerateexternallinksStateChanged

  /**
   * Reset the encrypted files cache as long as the user confirms that this is
   * really what thery intended (to protect against clicking wrong menu optiob)
   * @param evt
   */
  private void mnuToolsResetSecurityCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsResetSecurityCacheActionPerformed
    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, Localization.Main.getText("gui.confirm.tools.resetEncrypted"), "", JOptionPane.YES_NO_OPTION))
      return;
    logger.info(Localization.Main.getText("gui.menu.tools.resetEncrypted"));
    Random generator = new Random(System.currentTimeMillis());
    String securityCode = Integer.toHexString(generator.nextInt());
    ConfigurationManager.INSTANCE.getCurrentProfile().setSecurityCode(securityCode);
  }//GEN-LAST:event_mnuToolsResetSecurityCacheActionPerformed

  private void mnuToolsClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsClearLogActionPerformed
      debugClearLogFile();
    }//GEN-LAST:event_mnuToolsClearLogActionPerformed

    private void mnuToolsOpenLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsOpenLogActionPerformed
     debugShowLogFile();
    }//GEN-LAST:event_mnuToolsOpenLogActionPerformed

    private void mnuToolsOpenConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsOpenConfigActionPerformed
    debugShowSupportFolder();
    }//GEN-LAST:event_mnuToolsOpenConfigActionPerformed

    private void mnuHelpOpenForumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenForumActionPerformed
        logger.info(Localization.Main.getText("gui.menu.supportForum") + ": " + Constants.FORUM_URL);
        BareBonesBrowserLaunch.openURL(Constants.FORUM_URL);
    }//GEN-LAST:event_mnuHelpOpenForumActionPerformed

    private void mnuHelpOpenIssuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenIssuesActionPerformed
        logger.info(Localization.Main.getText("gui.menu.issueRegister") + ": " + Constants.ISSUES_URL);
        BareBonesBrowserLaunch.openURL(Constants.ISSUES_URL);
    }//GEN-LAST:event_mnuHelpOpenIssuesActionPerformed

    private void mnuHelpDevelopersGuideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpDevelopersGuideActionPerformed
        logger.info(Localization.Main.getText("gui.menu.developerGuide") + ": " + Constants.DEVELOPERGUIDE_URL);
        BareBonesBrowserLaunch.openURL(Constants.DEVELOPERGUIDE_URL);
    }//GEN-LAST:event_mnuHelpDevelopersGuideActionPerformed

    private void mnuHelpUserGuideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpUserGuideActionPerformed
        logger.info(Localization.Main.getText("gui.menu.userGuide") + ": " + Constants.USERGUIDE_URL);
        BareBonesBrowserLaunch.openURL(Constants.USERGUIDE_URL);
    }//GEN-LAST:event_mnuHelpUserGuideActionPerformed

    private void mnuHelpHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpHomeActionPerformed
        logger.info(Localization.Main.getText("gui.menu.help") + ": " + Constants.HOME_URL);
        BareBonesBrowserLaunch.openURL(Constants.HOME_URL);
    }//GEN-LAST:event_mnuHelpHomeActionPerformed

    private void cmdHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdHelpActionPerforme
      logger.info(Localization.Main.getText("gui.menu.help") + ": " + tabHelpUrl);
      BareBonesBrowserLaunch.openURL(tabHelpUrl);
    }//GEN-LAST:event_cmdHelpActionPerforme

    private void pnlMainOptionsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlMainOptionsComponentShown
      tabHelpUrl = Constants.HELP_URL_MAIN_OPTIONS;
    }//GEN-LAST:event_pnlMainOptionsComponentShown

    private void pnlCatalogStructureComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlCatalogStructureComponentShown
      tabHelpUrl = Constants.HELP_URL_CATALOGSTRUCTURE;
    }//GEN-LAST:event_pnlCatalogStructureComponentShown

    private void pnlBookDetailsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlBookDetailsComponentShown
      tabHelpUrl = Constants.HELP_URL_BOOKDETAILS;
    }//GEN-LAST:event_pnlBookDetailsComponentShown

    private void pnlAdvancedOptionsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlAdvancedOptionsComponentShown
      tabHelpUrl = Constants.HELP_URL_ADVANCED;
    }//GEN-LAST:event_pnlAdvancedOptionsComponentShown

    private void pnlExternalUrlsOptionsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlExternalUrlsOptionsComponentShown
      tabHelpUrl = Constants.HELP_URL_EXTERNALLINKS;
    }//GEN-LAST:event_pnlExternalUrlsOptionsComponentShown

    private void pnlCustomCatalogsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlCustomCatalogsComponentShown
      tabHelpUrl = Constants.HELP_URL_CUSTOMCATALOGS;
    }//GEN-LAST:event_pnlCustomCatalogsComponentShown

    private void mnuHelpOpenLocalizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenLocalizeActionPerformed
      logger.info(Localization.Main.getText("gui.menu.help.localize") + ": " + Constants.LOCALIZE_URL);
      BareBonesBrowserLaunch.openURL(Constants.LOCALIZE_URL);
    }//GEN-LAST:event_mnuHelpOpenLocalizeActionPerformed

    private void mnuHelpOpenCustomizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuHelpOpenCustomizeActionPerformed
      logger.info(Localization.Main.getText("gui.menu.help.customize") + ": " + Constants.CUSTOMIZE_URL);
      BareBonesBrowserLaunch.openURL(Constants.CUSTOMIZE_URL);
    }//GEN-LAST:event_mnuHelpOpenCustomizeActionPerformed

    private void chkNoGenerateTagsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNoGenerateTagsActionPerformed
      chkIncludeTagCrossReferences.setEnabled(true);
      if (chkNoGenerateTags.isSelected()) chkIncludeTagCrossReferences.setSelected(false);
      lblIncludeTagCrossReferences.setEnabled(! chkNoGenerateTags.isSelected());
      chkIncludeTagCrossReferences.setEnabled(! chkNoGenerateTags.isSelected());
    }//GEN-LAST:event_chkNoGenerateTagsActionPerformed

    private void checkDownloads(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkDownloads
        checkDownloads();
    }//GEN-LAST:event_checkDownloads

    private void CheckOnlyCatalogAllowed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckOnlyCatalogAllowed
       checkOnlyCatalogAllowed();
    }//GEN-LAST:event_CheckOnlyCatalogAllowed

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

  private void mnuHelpDonateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuHelpDonateActionPerformed
    donate();
  }// GEN-LAST:event_mnuHelpDonateActionPerformed

  private void mnuFileSaveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuFileSaveActionPerformed
    saveConfiguration();
  }// GEN-LAST:event_mnuFileSaveActionPerformed

  private void mnuFileExitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuFileExitActionPerformed
    exitProgram();
  }// GEN-LAST:event_mnuFileExitActionPerformed

  private void mnuFileGenerateCatalogsActionPerformed(java.awt.event.ActionEvent evt) {//
    // GEN-FIRST:event_mnuFileGenerateCatalogsActionPerformed
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
    exitProgram();
  }// GEN-LAST:event_cmdCancelActionPerformed

  private void cmdSetDatabaseFolderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSetDatabaseFolderActionPerformed
    showSetDatabaseFolderDialog();
  }// GEN-LAST:event_cmdSetDatabaseFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboIndexFilterAlgorithm;
    private javax.swing.JComboBox cboLang;
    private javax.swing.JCheckBox chkBrowseByCover;
    private javax.swing.JCheckBox chkBrowseByCoverWithoutSplit;
    private javax.swing.JCheckBox chkCopyToDatabaseFolder;
    private javax.swing.JCheckBox chkCryptFilenames;
    private javax.swing.JCheckBox chkDisplayAuthorSortInAuthorLists;
    private javax.swing.JCheckBox chkDisplayAuthorSortInBookDetails;
    private javax.swing.JCheckBox chkDisplayTitleSortInBookDetails;
    private javax.swing.JCheckBox chkDisplayTitleSortInBookLists;
    private javax.swing.JCheckBox chkDontsplittags;
    private javax.swing.JCheckBox chkExternalIcons;
    private javax.swing.JCheckBox chkGenerateIndex;
    private javax.swing.JCheckBox chkIncludeAddednBookDetails1;
    private javax.swing.JCheckBox chkIncludeCoversInCatalog;
    private javax.swing.JCheckBox chkIncludeModifiedInBookDetails;
    private javax.swing.JCheckBox chkIncludeOnlyOneFile;
    private javax.swing.JCheckBox chkIncludePublishedInBookDetails;
    private javax.swing.JCheckBox chkIncludePublisherInBookDetails;
    private javax.swing.JCheckBox chkIncludeRatingInBookDetails1;
    private javax.swing.JCheckBox chkIncludeSeriesInBookDetails;
    private javax.swing.JCheckBox chkIncludeTagCrossReferences;
    private javax.swing.JCheckBox chkIncludeTagsInBookDetails;
    private javax.swing.JCheckBox chkIncludeemptybooks;
    private javax.swing.JCheckBox chkIndexComments;
    private javax.swing.JCheckBox chkLanguageAsTag;
    private javax.swing.JCheckBox chkMinimizeChangedFiles;
    private javax.swing.JCheckBox chkNoCoverResize;
    private javax.swing.JCheckBox chkNoGenerateAuthors;
    private javax.swing.JCheckBox chkNoGenerateSeries;
    private javax.swing.JCheckBox chkNoGenerateTags;
    private javax.swing.JCheckBox chkNoIncludeAboutLink;
    private javax.swing.JCheckBox chkNoShowSeries;
    private javax.swing.JCheckBox chkNoThumbnailGenerate;
    private javax.swing.JCheckBox chkNogenerateallbooks;
    private javax.swing.JCheckBox chkNogeneratecrosslinks;
    private javax.swing.JCheckBox chkNogenerateexternallinks;
    private javax.swing.JCheckBox chkNogeneratehtml;
    private javax.swing.JCheckBox chkNogeneratehtmlfiles;
    private javax.swing.JCheckBox chkNogenerateopds;
    private javax.swing.JCheckBox chkNogenerateopdsfiles;
    private javax.swing.JCheckBox chkNogenerateratings;
    private javax.swing.JCheckBox chkNogeneraterecent;
    private javax.swing.JCheckBox chkOnlyCatalogAtTarget;
    private javax.swing.JCheckBox chkOrderAllBooksBySeries;
    private javax.swing.JCheckBox chkPublishedDateAsYear;
    private javax.swing.JCheckBox chkReprocessEpubMetadata;
    private javax.swing.JCheckBox chkSortUsingAuthorSort;
    private javax.swing.JCheckBox chkSortUsingTitleSort;
    private javax.swing.JCheckBox chkSplitByAuthorInitialGoToBooks;
    private javax.swing.JCheckBox chkSupressRatings;
    private javax.swing.JCheckBox chkZipTrookCatalog;
    private javax.swing.JButton cmdAdd;
    private javax.swing.JButton cmdAmazonIsbnReset;
    private javax.swing.JButton cmdAmazonTitleReset;
    private javax.swing.JButton cmdAmazonUrlReset;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdGenerate;
    private javax.swing.JButton cmdGoodreadAuthorReset;
    private javax.swing.JButton cmdGoodreadIsbnReset;
    private javax.swing.JButton cmdGoodreadReviewReset;
    private javax.swing.JButton cmdGoodreadTitleReset;
    private javax.swing.JButton cmdHelp;
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
    private javax.swing.JLabel lblBookDetailsCustomFields;
    private javax.swing.JLabel lblBooksinrecent;
    private javax.swing.JLabel lblBottom0;
    private javax.swing.JLabel lblBrowseByCover;
    private javax.swing.JLabel lblBrowseByCoverWithoutSplit;
    private javax.swing.JLabel lblCatalogFilter;
    private javax.swing.JLabel lblCatalogFolder;
    private javax.swing.JLabel lblCatalogTitle;
    private javax.swing.JLabel lblCopyToDatabaseFolder;
    private javax.swing.JLabel lblCoverHeight;
    private javax.swing.JLabel lblCryptFilenames;
    private javax.swing.JLabel lblCurrentProfile;
    private javax.swing.JLabel lblCustomDummy1;
    private javax.swing.JLabel lblCustomDummy2;
    private javax.swing.JLabel lblDatabaseFolder;
    private javax.swing.JLabel lblDeviceDropbox;
    private javax.swing.JLabel lblDeviceMode1;
    private javax.swing.JLabel lblDeviceMode2;
    private javax.swing.JLabel lblDeviceNAS;
    private javax.swing.JLabel lblDeviceNook;
    private javax.swing.JLabel lblDisplayAuthorSortInAuthorLists;
    private javax.swing.JLabel lblDisplayAuthorSortInBookDetails;
    private javax.swing.JLabel lblDisplayTitleSortInBookDetails;
    private javax.swing.JLabel lblDisplayTitleSortInBookLists;
    private javax.swing.JLabel lblDonate;
    private javax.swing.JLabel lblExternalIcons;
    private javax.swing.JLabel lblFeaturedCatalogSavedSearchName;
    private javax.swing.JLabel lblFeaturedCatalogTitle;
    private javax.swing.JLabel lblGenerateIndex;
    private javax.swing.JLabel lblGoodreadAuthorUrl;
    private javax.swing.JLabel lblGoodreadIsbnUrl;
    private javax.swing.JLabel lblGoodreadReviewIsbnUrl;
    private javax.swing.JLabel lblGoodreadTitleUrl;
    private javax.swing.JLabel lblIncludeAddedInBookDetails2;
    private javax.swing.JLabel lblIncludeCoversInCatalog;
    private javax.swing.JLabel lblIncludeModifiedInBookDetails1;
    private javax.swing.JLabel lblIncludeOnlyOneFile;
    private javax.swing.JLabel lblIncludePublishedInBookDetails;
    private javax.swing.JLabel lblIncludePublisherInBookDetails;
    private javax.swing.JLabel lblIncludeRatingInBookDetails;
    private javax.swing.JLabel lblIncludeSeriesInBookDetails;
    private javax.swing.JLabel lblIncludeTagCrossReferences;
    private javax.swing.JLabel lblIncludeTagsInBookDetails;
    private javax.swing.JLabel lblIncludeemptybooks;
    private javax.swing.JLabel lblIncludeformat;
    private javax.swing.JLabel lblIndexComments;
    private javax.swing.JLabel lblIndexFilterAlgorithm;
    private javax.swing.JLabel lblIsfdbAuthorUrl;
    private javax.swing.JLabel lblLang;
    private javax.swing.JLabel lblLanguageAsTag;
    private javax.swing.JLabel lblLibrarythingAuthorUrl;
    private javax.swing.JLabel lblLibrarythingIsbnUrl;
    private javax.swing.JLabel lblLibrarythingTitleUrl;
    private javax.swing.JLabel lblMaxBookSummaryLength;
    private javax.swing.JLabel lblMaxKeywords;
    private javax.swing.JLabel lblMaxMobileResolution;
    private javax.swing.JLabel lblMaxSplitLevels;
    private javax.swing.JLabel lblMaxbeforepaginate;
    private javax.swing.JLabel lblMaxbeforesplit;
    private javax.swing.JLabel lblMaxsummarylength;
    private javax.swing.JLabel lblMinBooksToMakeDeepLevel;
    private javax.swing.JLabel lblMinimizeChangedFiles;
    private javax.swing.JLabel lblNoCoverResize;
    private javax.swing.JLabel lblNoGenerateAuthors;
    private javax.swing.JLabel lblNoGenerateSeries;
    private javax.swing.JLabel lblNoGenerateTags;
    private javax.swing.JLabel lblNoIncludeAboutLink;
    private javax.swing.JLabel lblNoShowSeries;
    private javax.swing.JLabel lblNoThumbnailGenerate;
    private javax.swing.JLabel lblNogenerateallbooks;
    private javax.swing.JLabel lblNogeneratecrosslinks;
    private javax.swing.JLabel lblNogenerateexternallinks;
    private javax.swing.JLabel lblNogeneratehtml;
    private javax.swing.JLabel lblNogeneratehtmlfiles;
    private javax.swing.JLabel lblNogenerateopds;
    private javax.swing.JLabel lblNogenerateopdsfiles;
    private javax.swing.JLabel lblNogenerateratings;
    private javax.swing.JLabel lblNogeneraterecent;
    private javax.swing.JLabel lblOnlyCatalogAtTarget;
    private javax.swing.JLabel lblOrderAllBooksBySeries;
    private javax.swing.JLabel lblPublishedDateAsYear;
    private javax.swing.JLabel lblReprocessEpubMetadata;
    private javax.swing.JLabel lblSortUsingAuthor;
    private javax.swing.JLabel lblSortUsingTitle;
    private javax.swing.JLabel lblSplitByAuthorInitialGoToBooks;
    private javax.swing.JLabel lblSplittagson;
    private javax.swing.JLabel lblSupressRatings;
    private javax.swing.JLabel lblTagsToIgnore;
    private javax.swing.JLabel lblTagsToMakeDeep;
    private javax.swing.JLabel lblTargetFolder;
    private javax.swing.JLabel lblThumbnailheight;
    private javax.swing.JLabel lblUrlBooks;
    private javax.swing.JLabel lblWikilang;
    private javax.swing.JLabel lblWikipediaUrl;
    private javax.swing.JLabel lblZipTrookCatalog;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenuItem mnuFileExit;
    private javax.swing.JMenuItem mnuFileGenerateCatalogs;
    private javax.swing.JMenuItem mnuFileSave;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuHelpAbout;
    private javax.swing.JMenuItem mnuHelpDevelopersGuide;
    private javax.swing.JMenuItem mnuHelpDonate;
    private javax.swing.JMenuItem mnuHelpHome;
    private javax.swing.JMenuItem mnuHelpOpenCustomize;
    private javax.swing.JMenuItem mnuHelpOpenForum;
    private javax.swing.JMenuItem mnuHelpOpenIssues;
    private javax.swing.JMenuItem mnuHelpOpenLocalize;
    private javax.swing.JMenuItem mnuHelpUserGuide;
    private javax.swing.JMenu mnuProfiles;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JMenuItem mnuToolsClearLog;
    private javax.swing.JMenuItem mnuToolsOpenConfig;
    private javax.swing.JMenuItem mnuToolsOpenLog;
    private javax.swing.JMenuItem mnuToolsResetSecurityCache;
    private javax.swing.JMenuItem mnuToolsprocessEpubMetadataOfAllBooks;
    private javax.swing.JPanel pnlAdvancedOptions;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlCatalogStructure;
    private javax.swing.JPanel pnlCustomCatalogs;
    private javax.swing.JPanel pnlCustomCatalogsTableButtons;
    private javax.swing.JPanel pnlExternalUrlsOptions;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlMainOptions;
    private javax.swing.JPanel pnlSplitTagsOn;
    private javax.swing.JPanel pnlTitle;
    private javax.swing.JScrollPane scrCustomCatalogs;
    private javax.swing.JTabbedPane tabOptionsTabs;
    private javax.swing.JTable tblCustomCatalogs;
    private javax.swing.JTextField txtAmazonAuthorUrl;
    private javax.swing.JTextField txtAmazonIsbnUrl;
    private javax.swing.JTextField txtAmazonTitleUrl;
    private javax.swing.JTextField txtBookDetailsCustomFields;
    private javax.swing.JTextField txtBooksinrecent;
    private javax.swing.JTextField txtCatalogFilter;
    private javax.swing.JTextField txtCatalogFolder;
    private javax.swing.JTextField txtCatalogTitle;
    private javax.swing.JTextField txtCoverHeight;
    private javax.swing.JTextField txtDatabaseFolder;
    private javax.swing.JTextField txtFeaturedCatalogSavedSearchName;
    private javax.swing.JTextField txtFeaturedCatalogTitle;
    private javax.swing.JTextField txtGoodreadAuthorUrl;
    private javax.swing.JTextField txtGoodreadIsbnUrl;
    private javax.swing.JTextField txtGoodreadReviewIsbnUrl;
    private javax.swing.JTextField txtGoodreadTitleUrl;
    private javax.swing.JTextField txtIncludeformat;
    private javax.swing.JTextField txtIsfdbAuthorUrl;
    private javax.swing.JTextField txtLibrarythingAuthorUrl;
    private javax.swing.JTextField txtLibrarythingIsbnUrl;
    private javax.swing.JTextField txtLibrarythingTitleUrl;
    private javax.swing.JTextField txtMaxBookSummaryLength;
    private javax.swing.JTextField txtMaxKeywords;
    private javax.swing.JTextField txtMaxMobileResolution;
    private javax.swing.JTextField txtMaxSplitLevels;
    private javax.swing.JTextField txtMaxbeforepaginate;
    private javax.swing.JTextField txtMaxbeforesplit;
    private javax.swing.JTextField txtMaxsummarylength;
    private javax.swing.JTextField txtMinBooksToMakeDeepLevel;
    private javax.swing.JTextField txtSplittagson;
    private javax.swing.JTextField txtTagsToIgnore;
    private javax.swing.JTextField txtTagsToMakeDeep;
    private javax.swing.JTextField txtTargetFolder;
    private javax.swing.JTextField txtThumbnailheight;
    private javax.swing.JTextField txtUrlBooks;
    private javax.swing.JTextField txtWikilang;
    private javax.swing.JTextField txtWikipediaUrl;
    // End of variables declaration//GEN-END:variables

}
