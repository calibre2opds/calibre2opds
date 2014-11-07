package com.gmail.dpierron.calibre.gui;

/**
 * Handle the main GUI within Calibre2opds
 *
 * Note that the GUI form and this associated java class is constructed
 * and maintained using the Netbeans IDE tool for form design.
 * Althought he java class can be edited from within the IntelliJ IDEA editor
 * you should not make any changes that invalidate its use by the Netbeans one.
 */

import com.gmail.dpierron.calibre.configuration.*;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.datamodel.EBookFormat;
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
import sun.security.krb5.Config;

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
  private guiField[] guiFields;
  private File  SyncLogFile = new File(ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);


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
    initGuiFields();
    tabHelpUrl = Constants.HELP_URL_MAIN_OPTIONS;
    loadValues();
    translateTexts();

  }
  /**
   * Table that defines the GUI fields
   * Allows standardised handling of GUI fields to be applied with minimal developer effort
   *
   * There are a couple of different constructors supported to keep table definition clean.
   * The fields indicated as Optional are only included for the specific field type they relate to.
   *
   * Meaning/Use of the fields is:
   *
   * Field 1  Mandatory   Field to which the label localisation should be applied
   *                      Can be set to null if no label localization required
   * Field 2  Mandatory   Field in which value stored )if relevant).  Can be same as field 1.
   *                      Also has tooltip localisation applied if .tooltip version of Field 3 found
   *                      so set field 1 to null if only tooltip to be set up.
   *                      Can be null for fields that do not held stored configuration values
   * Field 3  Mandatory   Key for finding localization string.  Any .label/.tooltip suffix is omitted
   *                      Can optionally have the .label added to the key in localization file
   *                      If key with .tooltip found in localization file this is assumed to be a tooltip
   *
   * Field 4  Optional    Base name of the methods for loading/storing the the values in Field 2
   *                      If the field only needs localisation, but not storing in the configuration
   *                      file then then there will be no method defined so only fields 1 to 3 defined.
   *
   * Field 5  Optional    For checkboxes only.  Indicate is displayed field is negated from config value
   *
   * Field 5  Optional    Numeric fields.  Indicates minimum value allowed
   * Field 6  Optional    Numeric fields:  Indicates maximum value allowed
   *
   * NOTE:  If any new types are introduced for field 1 or field 2 then guiField class will
   *         neeed to be updated to handle this new type in the standard way desired.
   */
    private void initGuiFields() {
      guiFields = new guiField[] {

      // Yab definitions
      // (last character of localizationKey is assumed to be the index)

      new guiField(tabOptionsTabs, null, "gui.tab1"),
      new guiField(tabOptionsTabs, null, "gui.tab2"),
      new guiField(tabOptionsTabs, null, "gui.tab3"),
      new guiField(tabOptionsTabs, null, "gui.tab4"),
      new guiField(tabOptionsTabs, null, "gui.tab5"),
      new guiField(tabOptionsTabs, null, "gui.tab6"),
      new guiField(tabOptionsTabs, null, "gui.tab7"),

    // Main Windows

      new guiField(cmdCancel, null, "gui.close"),
      new guiField(cmdSave, null, "gui.save"),
      new guiField(cmdGenerate, null, "gui.generate"),
      new guiField(cmdReset, null, "gui.reset"),
      new guiField(cmdHelp, null, "gui.help"),
      new guiField(null, lblProfile, "config.profile"),

      // main options

      new guiField(lblDatabaseFolder, txtDatabaseFolder, "config.DatabaseFolder", "DatabaseFolder"),
      new guiField(lblTargetFolder, txtTargetFolder, "config.TargetFolder", "TargetFolder"),
      new guiField(lblCopyToDatabaseFolder, chkCopyToDatabaseFolder, "config.CopyToDatabaseFolder", "CopyToDatabaseFolder"),
      new guiField(lblOnlyCatalogAtTarget, chkOnlyCatalogAtTarget, "config.OnlyCatalogAtTarget", "OnlyCatalogAtTarget"),
      new guiField(lblReprocessEpubMetadata, chkReprocessEpubMetadata, "config.ReprocessEpubMetadata", "ReprocessEpubMetadata"),
      new guiField(lblCatalogFolder, txtCatalogFolder, "config.CatalogFolderName", "CatalogFolderName"),
      new guiField(lblUrlBooks, txtUrlBooks, "config.UrlBooks", "UrlBooks"),
      new guiField(lblCatalogTitle, txtCatalogTitle, "config.CatalogTitle", "CatalogTitle"),
      new guiField(lblSplittagson, txtSplittagson, "config.SplitTagsOn", "SplitTagsOn", true),
      new guiField(chkDontsplittags, chkDontsplittags, "config.DontSplitTagsOn", "DontSplitTagsOn"),
      new guiField(lblCatalogFilter, txtCatalogFilter, "config.CatalogFilter", "CatalogFilter"),
      new guiField(lblFavicon, txtFavicon, "config.Favicon", "Favicon"),
      new guiField(lblWikilang, txtWikilang, "config.WikipediaLanguage", "WikipediaLanguage"),
      new guiField(lblCryptFilenames, chkCryptFilenames, "config.CryptFilenames", "CryptFilenames"),

      // catalog structure options

      new guiField(lblNogenerateopds, chkNogenerateopds, "config.GenerateOpds", "GenerateOpds", true),
      new guiField(lblNogeneratehtml, chkNogeneratehtml, "config.GenerateHtml", "GenerateHtml", true),
      new guiField(lblNogenerateopdsfiles, chkNogenerateopdsfiles, "config.GenerateOpdsDownloads", "", true),
      new guiField(lblNogeneratehtmlfiles, chkNogeneratehtmlfiles, "config.GenerateHtmlDownloads", "GenerateHtmlDownloads", true),
      new guiField(lblBrowseByCover, chkBrowseByCover, "config.BrowseByCover", "BrowseByCover"),
      new guiField(lblBrowseByCoverWithoutSplit, chkBrowseByCoverWithoutSplit, "config.BrowseByCoverWithoutSplit", "BrowseByCoverWithoutSplit"),
      new guiField(lblLanguageAsTag, chkLanguageAsTag, "config.LanguageAsTag", "LanguageAsTag"),
      new guiField(lblNoIncludeAboutLink, chkNoIncludeAboutLink, "config.IncludeAboutLink", "IncludeAboutLink", true),
      new guiField(lblExternalIcons, chkExternalIcons, "config.ExternalIcons", "ExternalIcons"),
      new guiField(lblexternalImages, chkExternalImages, "config.ExternalImages", "ExternalImages"),
      new guiField(lblNoGenerateAuthors, chkNoGenerateAuthors, "config.GenerateAuthors", "GenerateAuthors", true),
      new guiField(lblNoGenerateTags, chkNoGenerateTags, "config.GenerateTags", "GenerateTags", true),
      new guiField(lblTagsToIgnore, txtTagsToIgnore, "config.TagsToIgnore", "TagsToIgnore"),
      new guiField(lblCatalogCustomColumns, txtCatalogCustomColumns, "config.CatalogCustomColumns", "CatalogCustomColumns"),
      new guiField(lblNoGenerateSeries, chkNoGenerateSeries, "config.GenerateSeries", "GenerateSeries", true),
      new guiField(lblNogeneraterecent, chkNogeneraterecent, "config.GenerateRecent", "GenerateRecent", true),
      new guiField(lblNogenerateratings, chkNogenerateratings, "config.GenerateRatings", "GenerateRatings", true),
      new guiField(lblSupressRatings, chkSupressRatings, "config.SuppressRatingsInTitles", "SuppressRatingsInTitles"),
      new guiField(lblNogenerateallbooks, chkNogenerateallbooks, "config.GenerateAllbooks", "GenerateAllbooks", true),
      new guiField(lblSortTagsByAuthor, chkSortTagsByAuthor, "config.SortTagsByAuthor", "SortTagsByAuthor"),
      new guiField(lblTagBooksNoSplit, chkTagBookNoSplit, "config.TagBooksNoSplit", "TagBooksNoSplit"),
      new guiField(lblSortUsingAuthor, chkSortUsingAuthorSort, "config.SortUsingAuthor", "SortUsingAuthor"),
      new guiField(lblSortUsingTitle, chkSortUsingTitleSort, "config.SortUsingTitle", "SortUsingTitle"),
      new guiField(lblSortUsingSeries, chkSortUsingSeriesSort, "config.SortUsingSeries", "SortUsingSeries"),

      // Book Details Options

      new guiField(lblIncludeSeriesInBookDetails, chkIncludeSeriesInBookDetails, "config.IncludeSeriesInBookDetails", "IncludeSeriesInBookDetails"),
      new guiField(lblIncludeRatingInBookDetails, chkIncludeRatingInBookDetails, "config.IncludeRatingInBookDetails", "IncludeRatingInBookDetails"),
      new guiField(lblIncludeTagsInBookDetails, chkIncludeTagsInBookDetails, "config.IncludeTagsInBookDetails", "IncludeTagsInBookDetails"),
      new guiField(lblIncludePublisherInBookDetails, chkIncludePublisherInBookDetails, "config.IncludePublisherInBookDetails", "IncludePublisherInBookDetails"),
      new guiField(lblIncludePublishedInBookDetails, chkIncludePublishedInBookDetails, "config.IncludePublishedInBookDetails", "IncludePublishedInBookDetails"),
      new guiField(lblPublishedDateAsYear, chkPublishedDateAsYear, "config.PublishedDateAsYear", "PublishedDateAsYear"),
      new guiField(lblIncludeAddedInBookDetails, chkIncludeAddedInBookDetails, "config.IncludeAddedInBookDetails", "IncludeAddedInBookDetails"),
      new guiField(lblIncludeModifiedInBookDetails1, chkIncludeModifiedInBookDetails, "config.IncludeModifiedInBookDetails", "IncludeModifiedInBookDetails"),
      new guiField(lblDisplayAuthorSort, chkDisplayAuthorSort, "config.DisplayAuthorSort", "DisplayAuthorSort"),
      new guiField(lblDisplayTitleSort, chkDisplayTitleSort, "config.DisplayTitleSort", "DisplayTitleSort"),
      new guiField(lblDisplaySeriesSort, chkDisplaySeriesSort, "config.DisplaySeriesSort", "DisplaySeriesSort"),
      new guiField(lblBookDetailsCustomFields, txtBookDetailsCustomFields, "config.BookDetailsCustomFields", "BookDetailsCustomFields"),
      new guiField(null, chkBookDetailsCustomFieldsAlways, "config.BookDetailsCustomFieldsAlways", "BookDetailsCustomFieldsAlways"),
      new guiField(lblNogeneratecrosslinks, chkNogeneratecrosslinks, "config.GenerateCrossLinks", "GenerateCrossLinks", true),
      new guiField(lblSingleBookCrossReferences, chkSingleBookCrossReferences, "config.SingleBookCrossReferences", "SingleBookCrossReferences"),
      new guiField(lblIncludeAuthorCrossReferences, chkIncludeAuthorCrossReferences, "config.IncludeAuthorCrossReferences", "IncludeAuthorCrossReferences"),
      new guiField(lblIncludeSerieCrossReferences, chkIncludeSerieCrossReferences, "config.IncludeSerieCrossReferences", "IncludeSerieCrossReferences"),
      new guiField(lblIncludeTagCrossReferences, chkIncludeTagCrossReferences, "config.IncludeTagCrossReferences", "IncludeTagCrossReferences"),
      new guiField(lblIncludeRatingCrossReferences, chkIncludeRatingCrossReferences, "config.IncludeRatingCrossReferences", "IncludeRatingCrossReferences"),

              // advanced customization options

      new guiField(lblIncludeformat, txtIncludeformat, "config.IncludedFormatsList", "IncludedFormatsList"),
      new guiField(lblMaxbeforepaginate, txtMaxbeforepaginate, "config.MaxBeforePaginate", "MaxBeforePaginate", 0, 99999),
      new guiField(lblMaxbeforesplit, txtMaxbeforesplit, "config.MaxBeforeSplit", "MaxBeforeSplit",0, 99999),
      new guiField(lblMaxSplitLevels, txtMaxSplitLevels, "config.MaxSplitLevels", "MaxSplitLevels", 0,8),
      new guiField(lblBooksinrecent, txtBooksinrecent, "config.BooksInRecentAdditions", "BooksInRecentAdditions", 0, 500),
      new guiField(lblMaxsummarylength, txtMaxsummarylength, "config.MaxSummaryLength", "MaxSummaryLength", 0, 99999),
      new guiField(lblMaxBookSummaryLength, txtMaxBookSummaryLength, "config.MaxBookSummaryLength", "MaxBookSummaryLength", 0, 99999),
      new guiField(lblIncludeemptybooks, chkIncludeemptybooks, "config.IncludeBooksWithNoFile", "IncludeBooksWithNoFile"),
      new guiField(lblIncludeOnlyOneFile, chkIncludeOnlyOneFile, "config.IncludeOnlyOneFile", "IncludeOnlyOneFile"),
      new guiField(lblZipTrookCatalog, chkZipTrookCatalog, "config.ZipTrookCatalog", "ZipTrookCatalog"),
      new guiField(lblNoShowSeries, chkNoShowSeries, "config.ShowSeriesInAuthorCatalog", "ShowSeriesInAuthorCatalog", true),
      new guiField(lblOrderAllBooksBySeries, chkOrderAllBooksBySeries, "config.OrderAllBooksBySeries", "OrderAllBooksBySeries"),
      new guiField(lblSplitByAuthorInitialGoToBooks, chkSplitByAuthorInitialGoToBooks, "config.SplitByAuthorInitialGoToBooks", "SplitByAuthorInitialGoToBooks"),
      new guiField(lblNoThumbnailGenerate, chkNoThumbnailGenerate, "config.ThumbnailGenerate", "ThumbnailGenerate", true),
      new guiField(lblThumbnailheight, txtThumbnailheight, "config.ThumbnailHeight", "ThumbnailHeight", 0, 1000),
      new guiField(lblNoCoverResize, chkNoCoverResize, "config.CoverResize", "CoverResize", true),
      new guiField(lblIncludeCoversInCatalog, chkIncludeCoversInCatalog, "config.IncludeCoversInCatalog", "IncludeCoversInCatalog"),
      new guiField(lblUseThumbnailAsCover, chkUseThumbnailAsCover, "config.UseThumbnailsAsCovers", "UseThumbnailsAsCovers"),
      new guiField(lblZipCatalog, chkZipCatalog, "config.ZipCatalog", "ZipCatalog"),
      new guiField(lblZipOmitXml, chkZipOmitXml, "config.ZipOmitXml", "ZipOmitXml"),
      new guiField(lblCoverHeight, txtCoverHeight, "config.CoverHeight", "CoverHeight", 0, 999),
      new guiField(lblTagsToMakeDeep, txtTagsToMakeDeep, "config.TagsToMakeDeep", "TagsToMakeDeep"),
      new guiField(lblMinBooksToMakeDeepLevel, txtMinBooksToMakeDeepLevel, "config.MinBooksToMakeDeepLevel", "MinBooksToMakeDeepLevel", 0, 99999),
      // new guiField(lblMaxMobileResolution, txtMaxMobileResolution, "config.MaxMobileResolution", "MaxMobileResolution", 0, 2000),
      new guiField(lblMinimizeChangedFiles, chkMinimizeChangedFiles, "config.MinimizeChangedFiles", "MinimizeChangedFiles"),
      new guiField(lblGenerateIndex, chkGenerateIndex, "config.GenerateIndex", "GenerateIndex"),
      new guiField(lblMaxKeywords, txtMaxKeywords, "config.MaxKeywords", "MaxKeywords"),
      new guiField(lblIndexComments, chkIndexComments, "config.IndexComments", "IndexComments"),
      new guiField(lblIndexFilterAlgorithm, cboIndexFilterAlgorithm, "config.IndexFilterAlgorithm"),

      // external links

      new guiField(lblNogenerateexternallinks, chkNogenerateexternallinks, "config.GenerateExternalLinks", "GenerateExternalLinks", true),
      new guiField(lblWikipediaUrl, txtWikipediaUrl, "config.WikipediaUrl", "WikipediaUrl"),
      new guiField(cmdWikipediaUrlReset, null, "config.Reset"),
      new guiField(lblAmazonAuthorUrl, txtAmazonAuthorUrl, "config.AmazonAuthorUrl", "AmazonAuthorUrl"),
      new guiField(cmdAmazonUrlReset, null, "config.Reset"),
      new guiField(lblAmazonIsbnUrl, txtAmazonIsbnUrl, "config.AmazonIsbnUrl", "AmazonIsbnUrl"),
      new guiField(cmdAmazonIsbnReset, null, "config.Reset"),
      new guiField(lblAmazonTitleUrl, txtAmazonTitleUrl, "config.AmazonTitleUrl", "AmazonTitleUrl"),
      new guiField(cmdAmazonTitleReset, null, "config.Reset"),
      new guiField(lblGoodreadAuthorUrl, txtGoodreadAuthorUrl, "config.GoodreadAuthorUrl", "GoodreadAuthorUrl"),
      new guiField(cmdGoodreadAuthorReset, null, "config.Reset"),
      new guiField(lblGoodreadIsbnUrl, txtGoodreadIsbnUrl, "config.GoodreadIsbnUrl", "GoodreadIsbnUrl"),
      new guiField(cmdGoodreadIsbnReset, null, "config.Reset.label"),
      new guiField(lblGoodreadTitleUrl, txtGoodreadTitleUrl, "config.GoodreadTitleUrl", "GoodreadTitleUrl"),
      new guiField(cmdGoodreadTitleReset, null, "config.Reset"),
      new guiField(lblGoodreadReviewIsbnUrl, txtGoodreadReviewIsbnUrl, "config.GoodreadReviewIsbnUrl", "GoodreadReviewIsbnUrl"),
      new guiField(cmdGoodreadReviewReset, null, "config.Reset"),
      new guiField(lblIsfdbAuthorUrl, txtIsfdbAuthorUrl, "config.IsfdbAuthorUrl", "IsfdbAuthorUrl"),
      new guiField(cmdIsfdbAuthorReset, null, "config.Reset.label"),
      new guiField(lblLibrarythingAuthorUrl, txtLibrarythingAuthorUrl, "config.LibrarythingAuthorUrl", "LibrarythingAuthorUrl"),
      new guiField(cmdLibrarythingAuthorReset, null, "config.Reset"),
      new guiField(lblLibrarythingIsbnUrl, txtLibrarythingIsbnUrl, "config.LibrarythingIsbnUrl", "LibrarythingIsbnUrl"),
      new guiField(cmdLibrarythingIsbnReset, null, "config.Reset"),
      new guiField(lblLibrarythingTitleUrl, txtLibrarythingTitleUrl, "config.LibrarythingTitleUrl", "LibrarythingTitleUrl"),
      new guiField(cmdLibrarythingTitleReset, null, "config.Reset"),

      // Custom catalogs

      new guiField(cmdAdd, null, "config.CustomCatalogAdd"),
      new guiField(lblFeaturedCatalogTitle, txtFeaturedCatalogTitle, "config.FeaturedCatalogTitle", "FeaturedCatalogTitle"),
      new guiField(lblFeaturedCatalogSavedSearchName, txtFeaturedCatalogSavedSearchName, "config.FeaturedCatalogSavedSearchName", "FeaturedCatalogSavedSearchName"),

      // Menuse

      new guiField(mnuFile, null, "gui.menu.file"),
      new guiField(mnuFileSave, null, "gui.save"),
      new guiField(mnuFileGenerateCatalogs, null, "gui.generate"),
      new guiField(mnuFileExit, null, "gui.close"),
      new guiField(mnuProfiles, null, "gui.menu.profiles"),
      new guiField(mnuTools, null, "gui.menu.tools"),
      new guiField(mnuToolsprocessEpubMetadataOfAllBooks, null, "gui.menu.tools.processEpubMetadataOfAllBooks"),
      new guiField(mnuHelp, null, "gui.menu.help"),
      new guiField(mnuHelpDonate, null, "gui.menu.help.donate"),
      new guiField(mnuHelpAbout, null, "gui.menu.help.about"),
      new guiField(mnuHelpHome, null, "gui.menu.help.home"),
      new guiField(mnuHelpUserGuide, null, "gui.menu.help.userGuide"),
      new guiField(mnuHelpDevelopersGuide, null, "gui.menu.help.developerGuide"),
      new guiField(mnuHelpOpenIssues, null, "gui.menu.help.issueRegister"),
      new guiField(mnuHelpOpenForum, null, "gui.menu.help.supportForum"),
      new guiField(mnuHelpOpenLocalize, null, "gui.menu.help.localize"),
      new guiField(mnuHelpOpenCustomize, null, "gui.menu.help.customize"),
      new guiField(mnuToolsResetSecurityCache, null, "gui.menu.tools.resetEncrypted"),
      new guiField(mnuToolsConfigLog,null,"gui.menu.tools.logConfig"),
      new guiField(mnuToolsOpenLog,null,  "gui.menu.tools.logFile"),
      new guiField(mnuToolsOpenSyncLog,null,  "gui.menu.tools.syncFile"),
      new guiField(mnuToolsClearLog, null, "gui.menu.tools.logClear"),
      new guiField(mnuToolsOpenConfig, null, "gui.menu.tools.configFolder"),
      new guiField(lblSearchDeprecated,null,"gui.searchDeprecated.label"),
      // Should always be last entry in case it triggers re-localisation to new language
      new guiField(lblLang, cboLang, "config.Language", "Language")
    };
  }
  /**
   *  add a button to the custom catalogs table
   */
  private void addDeleteButtonToCustomCatalogsTable() {
    // add a button to the custom catalogs table
    Action delete = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int modelRow = Integer.valueOf(e.getActionCommand());
        customCatalogTableModel.deleteCustomCatalog(modelRow);
      }
    };

    /*
    Action check = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int modelRow = Integer.valueOf(e.getActionCommand());
        customCatalogTableModel.checkCustomCatalog(modelRow);
      }
    };
    */

    // CheckboxColumn checkboxColumn = new CheckboxColumn(tblCustomCatalogs, null, 2);
    // checkboxColumn.setMnemonic(KeyEvent.VK_C);
    ButtonColumn buttonColumn = new ButtonColumn(tblCustomCatalogs, delete, 3);
    buttonColumn.setMnemonic(KeyEvent.VK_D);

    Dimension d = tabOptionsTabs.getPreferredSize();
    int width = (int) d.getWidth() - 24;
    tblCustomCatalogs.getColumnModel().getColumn(0).setPreferredWidth((int)(width * .2));
    tblCustomCatalogs.getColumnModel().getColumn(1).setPreferredWidth((int)(width * .6));
    tblCustomCatalogs.getColumnModel().getColumn(2).setMinWidth(40);    // Checkbox
    tblCustomCatalogs.getColumnModel().getColumn(2).setMaxWidth(40);    // Checkbox
    tblCustomCatalogs.getColumnModel().getColumn(3).setPreferredWidth((int)(width * .10));    // Delete button
    tblCustomCatalogs.getColumnModel().getColumn(3).setMaxWidth(80);    // Delete
  }

  /**
   * Check a field that is meant to contain a search term
   *
   * @param title
   * @param searchText
   * @return
   */
  private boolean validateSearch (String title, String searchText) {
    if (searchText.toUpperCase().startsWith(Constants.CUSTOMCATALOG_SEARCH_SAVED.toUpperCase())) {
      return true;
    }
    for (String val : Constants.CUSTOMCATALOG_SEARCH_FIELD_NAMES) {
      if (searchText.toUpperCase().startsWith(val.toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   *  check a field that defines a Custom Catalog
   */
  private boolean validateCustomCatalog(String title, String value) {
    return false;
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

  /**
   * Do some validation on the options to split tags.
   *
   * If the option is disabled, but the txt field is empty then set it
   * to the default value (this will auto-correct legacy cases)
   *
   * Ptherwise give error mesages if the field is empty or eet to comma
   */
  private boolean checkSplitTagsOn(boolean warn) {
    if (chkDontsplittags.isSelected()) {
      if (txtSplittagson.getText().equals("")) {
        txtSplittagson.setText(".");
      }
    } else {
      if (txtSplittagson.getText().equals("")) {
        String message = Localization.Main.getText("config.SplitTagsOnEmpty.error");
        JOptionPane.showMessageDialog(this, message,"", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (warn && txtSplittagson.getText().equals(",")) {
        String message = Localization.Main.getText("config.SplitTagsOnComma.warn");
        JOptionPane.showMessageDialog(this, message,"", JOptionPane.WARNING_MESSAGE);
        return false;
      }
    }
    txtSplittagson.setEnabled(! chkDontsplittags.isSelected());
    return true;
  }
  /**
   *
   */
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
      chkZipOmitXml.setSelected(true);
      chkZipOmitXml.setEnabled(false);
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
      chkZipOmitXml.setSelected(false);
      chkZipOmitXml.setEnabled(false);
    } else {
      // If we are generating HTHL catalogs then HTML
      // downloads are only allowed if OPDS ones are also active
      // or we are not generating OPDS catalogs
      chkNogeneratehtmlfiles.setEnabled(chkNogenerateopds.isSelected()==true || chkNogenerateopdsfiles.isSelected()==false);
    }
    lblZipOmitXml.setEnabled(lblZipOmitXml.isEnabled());
  }

  /**
   * Check if the user is allowed to set the option to only have a
   * catalog at the target location?
   */
  private void checkOnlyCatalogAllowed() {
    if (lblTargetFolder.isEnabled()
    &&  Helper.isNotNullOrEmpty(txtUrlBooks.getText())) {
      lblOnlyCatalogAtTarget.setEnabled(true);
    } else {
      lblOnlyCatalogAtTarget.setEnabled(false);
    }
    chkOnlyCatalogAtTarget.setEnabled(lblOnlyCatalogAtTarget.isEnabled());
    checkCatalogFolderNeeded();
  }

  /**
   * Decide if the catalog folder setting is relevant
   */
  private void checkCatalogFolderNeeded() {
    if (Helper.isNotNullOrEmpty(txtUrlBooks.getText())
    &&  chkOnlyCatalogAtTarget.isSelected()
    &&  lblOnlyCatalogAtTarget.isEnabled()
    && !chkCopyToDatabaseFolder.isSelected()) {
      lblCatalogFolder.setEnabled(false);
    } else {
      lblCatalogFolder.setEnabled(true);
    }
    txtCatalogFolder.setEnabled(lblCatalogFolder.isEnabled());
  }

  /**
   * Enable/disable the sub-selections for cross-references depending on master setting
   */
  private void checkCrossReferencesEnabled() {
    boolean state = !chkNogeneratecrosslinks.isSelected();
    lblSingleBookCrossReferences.setEnabled(state);
    chkSingleBookCrossReferences.setEnabled(state);
    lblIncludeAuthorCrossReferences.setEnabled(state);
    chkIncludeAuthorCrossReferences.setEnabled(state);
    lblIncludeSerieCrossReferences.setEnabled(state);
    chkIncludeSerieCrossReferences.setEnabled(state);
    lblIncludeTagCrossReferences.setEnabled(state);
    chkIncludeTagCrossReferences.setEnabled(state);
    lblIncludeRatingCrossReferences.setEnabled(state);
    chkIncludeRatingCrossReferences.setEnabled(state);
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

  /**
   * Enable/disables GUI fiels that are mode specific
   * @param mode
   */
  private void adaptInterfaceToDeviceSpecificMode(DeviceMode mode) {
    Border RED_BORDER = new LineBorder(Color.red,2);
    switch (mode) {
      case Nook:
        // put a border on the selected mode icon
        lblDeviceDropbox.setBorder(null);
        lblDeviceNAS.setBorder(null);
        lblDeviceNook.setBorder(RED_BORDER);
        lblDeviceMode.setText(Localization.Main.getText("config.DeviceMode.nook.tooltip"));
        lblZipTrookCatalog.setVisible(true);
        chkZipTrookCatalog.setVisible(true);
        break;
      case Nas:
        // put a border on the selected mode icon
        lblDeviceDropbox.setBorder(null);
        lblDeviceNook.setBorder(null);
        lblDeviceNAS.setBorder(RED_BORDER);
        lblDeviceMode.setText(Localization.Main.getText("config.DeviceMode.nas.tooltip"));
        lblZipTrookCatalog.setVisible(false);
        chkZipTrookCatalog.setVisible(false);
        break;
      default:
        // put a border on the selected mode icon
        lblDeviceNAS.setBorder(null);
        lblDeviceNook.setBorder(null);
        lblDeviceDropbox.setBorder(RED_BORDER);
        lblDeviceMode.setText(Localization.Main.getText("config.DeviceMode.dropbox.tooltip"));
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

  /**
   *
   * @param mode
   */
  private void setDeviceSpecificMode(DeviceMode mode) {
    DeviceMode currentMode = currentProfile.getDeviceMode();
    // If switching modes, then some values may need their values forced to be mode specific
    if (currentMode != mode) {
      currentProfile.setDeviceMode(mode);
      adaptInterfaceToDeviceSpecificMode(mode);
      DeviceMode.fromName(currentProfile.getDeviceMode().toString()).setModeSpecificOptions(currentProfile);
    }
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

  /**
   * Start up the dialog for configuring the log settings
   */
  private void configLogFile() {
    ConfigureLoggingDialog dialog = new ConfigureLoggingDialog(Mainframe.this, true);
    dialog.setLocationRelativeTo(Mainframe.this);
    dialog.setVisible(true);
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
    String intro_team_title2 = Localization.Main.getText("intro.team.title2");
    String intro_team_list2 = Localization.Main.getText("intro.team.list2");
    String intro_team_list3 = Localization.Main.getText("intro.team.list3");
    String intro_team_list4 = Localization.Main.getText("intro.team.list4");
    String intro_team_list5 = Localization.Main.getText("intro.team.list5");
    String intro_thanks_1 = Localization.Main.getText("intro.thanks.1");
    String intro_thanks_2 = Localization.Main.getText("intro.thanks.2");
    String message =
        "<html>" + prog_version + "<br><br>" + intro_goal + "<br><br>" + intro_wiki_title + intro_wiki_url
            + "<br><br>" + intro_team_title
            + "<br><ul>"
            + "<li>" + intro_team_list1
            + "</ul>"
            + "<br>" + intro_team_title2
            + "<br><ul>"
            + "<li>" + intro_team_list2
            + "<li>" + intro_team_list3
            + "<li>" + intro_team_list4
            + "<li>" + intro_team_list5
            + "</ul><br>"
            + intro_thanks_1
            + "<br>" + intro_thanks_2 + "<br><br></html>";
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

    if (! checkSplitTagsOn(false)) {
      return;
    }
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

  /**
   *
   */
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
        // We only get to this next statement when the dialog hides itself.
        loadProfiles(); // Reload the list as it has probably changed.
        loadValues();   // Reload the current profile as it may have been renamed
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
    lblWikipediaUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdWikipediaUrlReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonAuthorUrlReadOnly());
    txtAmazonAuthorUrl.setEnabled(derivedState);
    lblAmazonAuthorUrl.setEnabled(derivedState);
    lblAmazonAuthorUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdAmazonUrlReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonIsbnUrlReadOnly());
    txtAmazonIsbnUrl.setEnabled(derivedState);
    lblAmazonIsbnUrl.setEnabled(derivedState);
    lblAmazonIsbnUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdAmazonIsbnReset.setEnabled(derivedState);
    derivedState = ! (enabledNoExternalLinks || currentProfile.isAmazonTitleUrlReadOnly());
    txtAmazonTitleUrl.setEnabled(derivedState);
    lblAmazonTitleUrl.setEnabled(derivedState);
    lblAmazonTitleUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdAmazonTitleReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadAuthorUrlReadOnly());
    txtGoodreadAuthorUrl.setEnabled(derivedState);
    lblGoodreadAuthorUrl.setEnabled(derivedState);
    lblGoodreadAuthorUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdGoodreadAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadIsbnUrlReadOnly());
    txtGoodreadIsbnUrl.setEnabled(derivedState);
    lblGoodreadIsbnUrl.setEnabled(derivedState);
    lblGoodreadIsbnUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdGoodreadIsbnReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadTitleUrlReadOnly());
    txtGoodreadTitleUrl.setEnabled(derivedState);
    lblGoodreadTitleUrl.setEnabled(derivedState);
    lblGoodreadTitleUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdGoodreadTitleReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isGoodreadReviewIsbnUrlReadOnly());
    txtGoodreadReviewIsbnUrl.setEnabled(derivedState);
    lblGoodreadReviewIsbnUrl.setEnabled(derivedState);
    lblGoodreadReviewIsbnUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdGoodreadReviewReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isIsfdbAuthorUrlReadOnly());
    txtIsfdbAuthorUrl.setEnabled(derivedState);
    lblIsfdbAuthorUrl.setEnabled(derivedState);
    lblIsfdbAuthorUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdIsfdbAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingAuthorUrlReadOnly());
    txtLibrarythingAuthorUrl.setEnabled(derivedState);
    lblLibrarythingAuthorUrl.setEnabled(derivedState);
    lblLibrarythingAuthorUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdLibrarythingAuthorReset.setEnabled((derivedState));
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingIsbnUrlReadOnly());
    txtLibrarythingIsbnUrl.setEnabled(derivedState);
    lblLibrarythingIsbnUrl.setEnabled(derivedState);
    cmdLibrarythingIsbnReset.setEnabled((derivedState));
    lblLibrarythingIsbnUrl.setEnabled(false);  //TODO remove when support for changing title ready
    derivedState = ! (enabledNoExternalLinks || currentProfile.isLibrarythingTitleUrlReadOnly());
    txtLibrarythingTitleUrl.setEnabled(derivedState);
    lblLibrarythingTitleUrl.setEnabled(derivedState);
    lblLibrarythingTitleUrl.setEnabled(false);  //TODO remove when support for changing title ready
    cmdLibrarythingTitleReset.setEnabled((derivedState));
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

    // Localizations that need completing before calling default processing

    cboLang.setModel(new DefaultComboBoxModel(LocalizationHelper.INSTANCE.getAvailableLocalizations()));

    // Types not handled (yet) by guiField class

    cboIndexFilterAlgorithm.setModel(new DefaultComboBoxModel(Index.FilterHintType.values()));
    cboIndexFilterAlgorithm.setSelectedItem(currentProfile.getIndexFilterAlgorithm());
    cboIndexFilterAlgorithm.setEnabled(!currentProfile.isIndexFilterAlgorithmReadOnly());
    lblIndexFilterAlgorithm.setEnabled(!currentProfile.isIndexFilterAlgorithmReadOnly());

    // Invoke standard field processing

    for (guiField g : guiFields) g.loadValue();

    // Now additional settings not handled by default processing

    cmdSetDatabaseFolder.setEnabled(lblDatabaseFolder.isEnabled());
    // TODO Check whether check against Default mode is needed
    if (currentProfile.getDeviceMode() == DeviceMode.Default) {
      txtTargetFolder.setEnabled(false);
      lblTargetFolder.setEnabled(false);
    }
    cmdSetTargetFolder.setEnabled(lblTargetFolder.isEnabled());

    lblIndexFilterAlgorithm.setEnabled(!currentProfile.isIndexFilterAlgorithmReadOnly());
    actOnGenerateIndexActionPerformed();

    checkDownloads();
    checkCrossReferencesEnabled();
    checkSplitTagsOn(false);

    lblFavicon.setVisible(false);     // TODO remove to activate option
    txtFavicon.setVisible(false);     // TODO remove to activate option
    cmdSetFavicon.setVisible(false);  // TODO remove to activate option
    lblCatalogCustomColumns.setVisible(false);  // TODO remove to activate option
    txtCatalogCustomColumns.setVisible(false);  // TODO remove to activate option
    lblSortUsingSeries.setVisible(false);       // TODO remove to activate option
    chkSortUsingSeriesSort.setVisible(false);   // TODO remove to activate option
    lblDisplaySeriesSort.setVisible(false);     // TODO remove to activate option
    chkDisplaySeriesSort.setVisible(false);     // TODO remove to activate option
    // txtMaxMobileResolution.setVisible(false);   // TODO Not currently being used
    // lblMaxMobileResolution.setVisible(false);   // TODO Not currently being used
    setExternalLinksEnabledState();
    customCatalogTableModel.setCustomCatalogs(currentProfile.getCustomCatalogs());
    tblCustomCatalogs.setModel(customCatalogTableModel);
    tblCustomCatalogs.setEnabled(!currentProfile.isCustomCatalogsReadOnly());
    pnlCustomCatalogsTableButtons.setEnabled(!currentProfile.isCustomCatalogsReadOnly());
    tblCustomCatalogs.revalidate();         // Force a redraw of table contents

    DeviceMode mode = currentProfile.getDeviceMode();
    // Ensuer we have a Device Mode actually set
    if (Helper.isNullOrEmpty(mode)) {
      if (logger.isTraceEnabled())
        logger.trace("Device mode was not set - setting to " + DeviceMode.Default);
      currentProfile.setDeviceMode(DeviceMode.Default);
    }
    // Set interface to match the mode
    adaptInterfaceToDeviceSpecificMode(mode);
    computeBrowseByCoverWithoutSplitVisibility();

    changeLanguage();
    loadProfiles();
    checkDownloads();
    checkOnlyCatalogAllowed();

    String profile=Localization.Main.getText("config.profile.label", ConfigurationManager.INSTANCE.getCurrentProfileName());
    String title = Localization.Main.getText("gui.title", Constants.PROGTITLE + Constants.BZR_VERSION) + " - " + profile;
    setTitle(title);
    lblProfile.setText(profile);
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

  /**
   * Reset GUI vlues to match the current profile
   */
  private void resetValues() {
    String lang = currentProfile.getLanguage();
    currentProfile.reset();
    loadValues();
    currentProfile.setLanguage(lang);
    changeLanguage();
  }

  /**
   *
   * @return
   */
  private String getFolderNameToStore()  {
    assert false: "Not yet ready for use";
    return "";
  }
  /**
   * Save the setting values from the GUI to the configuration file
   */
  private void storeValues() {
    checkSplitTagsOn(false);
    setCursor(hourglassCursor);

    // setMinimumSize(new java.awt.Dimension(1000, 750));
    Dimension size = getSize();
    currentProfile.setWindowHeight((size.height));
    currentProfile.setWindowWidth(size.width);

    for (guiField g : guiFields) g.storeValue();

    // Field types not (yet) handled by guiField

    currentProfile.setCustomCatalogs(customCatalogTableModel.getCustomCatalogs());
    currentProfile.setIndexFilterAlgorithm(Index.FilterHintType.valueOf("" + cboIndexFilterAlgorithm.getSelectedItem()));

    setCursor(normalCursor);
  }

  /**
   * Apply localization strings to all UI elements and set up Tooltips
   * Same tootip is applied to a label and its associated input field
   */
  private void translateTexts() {
    // main window
    setSize(new java.awt.Dimension(currentProfile.getWindowWidth(), currentProfile.getWindowHeight()));
    lblBottom0.setText(Localization.Main.getText("gui.label.clickToDescribe")); // NOI18N
    lblBottom0.setFont(lblBottom0.getFont().deriveFont(Font.BOLD));

    lblDeviceDropbox.setToolTipText(
        Localization.Main.getText("config.DeviceMode.dropbox.tooltip1") + " " + Localization.Main.getText("config.DeviceMode.dropbox.tooltip2"));
    lblDeviceNAS.setToolTipText(Localization.Main.getText("config.DeviceMode.nas.tooltip1") + " " + Localization.Main.getText("config.DeviceMode.nas" +
        ".tooltip2"));
    lblDeviceNook.setToolTipText(
        Localization.Main.getText("config.DeviceMode.nook.tooltip1") + " " + Localization.Main.getText("config.DeviceMode.nook.tooltip2"));

    // Do translations that are handled by guiFields table

    for (guiField f : guiFields) f.translateTexts();

    // `Additional translations  (if any)
    adaptInterfaceToDeviceSpecificMode(currentProfile.getDeviceMode());
    mnuToolsOpenSyncLog.setVisible(SyncLogFile.exists());
  }

  /**
   * Display in a popup the tooltip assoicated with a label that the user has clicked on
   * This is for convenience in environments where the tootip is not conveniently displayed.
   *
   * @param label
   */
  private void popupExplanation(JLabel label) {
    if (Helper.isNotNullOrEmpty(label.getToolTipText()))
      JOptionPane.showMessageDialog(this, label.getToolTipText(), Localization.Main.getText("gui.tooltip"), JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   *
   */
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

  /**
   *
   * @param targetFolder
   * @return
   */
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

  /**
   *
   */
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

  /**
   *
   * @param targetFolder
   * @return
   */
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

  /**
   *
   */
   private void openLogFolder() {
     // Do nothing yet
   }

  /**
   *
   */
   private void saveConfiguration() {
     storeValues();
     String message = Localization.Main.getText("gui.info.saved");
     JOptionPane.showMessageDialog(this, message, "", JOptionPane.OK_OPTION);
   }


  /**
   *
   */
   private void exitProgram() {
     System.exit(0);
   }

  /**
   *
   * @return
   */
   private TableModel getTblCustomCatalogsModel() {
     return customCatalogTableModel;
   }

  /**
   * Add a new entry to the custom Catalog table
   */
   private void addCustomCatalog() {
     assert customCatalogTableModel.equals(tblCustomCatalogs.getModel());
     customCatalogTableModel.addCustomCatalog();
     tblCustomCatalogs.revalidate();
     tblCustomCatalogs.repaint();
   }

   /**
    * This method is called from within the constructor to reset the form. WARNING: Do NOT modify this code. The content of this
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
        lblDeviceMode = new javax.swing.JLabel();
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
        lblCryptFilenames = new javax.swing.JLabel();
        chkCryptFilenames = new javax.swing.JCheckBox();
        lblFavicon = new javax.swing.JLabel();
        txtFavicon = new javax.swing.JTextField();
        cmdSetFavicon = new javax.swing.JButton();
        pnlCatalogStructure = new javax.swing.JPanel();
        lblNogeneratehtml = new javax.swing.JLabel();
        chkNogeneratehtml = new javax.swing.JCheckBox();
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
        lblSortUsingAuthor = new javax.swing.JLabel();
        lblSortUsingTitle = new javax.swing.JLabel();
        chkSortUsingAuthorSort = new javax.swing.JCheckBox();
        chkSortUsingTitleSort = new javax.swing.JCheckBox();
        lblTagsToIgnore = new javax.swing.JLabel();
        txtTagsToIgnore = new javax.swing.JTextField();
        lblCatalogCustomColumns = new javax.swing.JLabel();
        txtCatalogCustomColumns = new javax.swing.JTextField();
        lblSortTagsByAuthor = new javax.swing.JLabel();
        chkSortTagsByAuthor = new javax.swing.JCheckBox();
        lblTagBooksNoSplit = new javax.swing.JLabel();
        chkTagBookNoSplit = new javax.swing.JCheckBox();
        lblNogeneratehtmlfiles = new javax.swing.JLabel();
        chkNogeneratehtmlfiles = new javax.swing.JCheckBox();
        lblSortUsingSeries = new javax.swing.JLabel();
        lblBrowseByCover = new javax.swing.JLabel();
        chkBrowseByCover = new javax.swing.JCheckBox();
        lblBrowseByCoverWithoutSplit = new javax.swing.JLabel();
        chkBrowseByCoverWithoutSplit = new javax.swing.JCheckBox();
        chkSortUsingSeriesSort = new javax.swing.JCheckBox();
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
        lblDisplayAuthorSort = new javax.swing.JLabel();
        lblDisplayTitleSort = new javax.swing.JLabel();
        chkDisplayAuthorSort = new javax.swing.JCheckBox();
        chkDisplayTitleSort = new javax.swing.JCheckBox();
        lblNogeneratecrosslinks = new javax.swing.JLabel();
        chkNogeneratecrosslinks = new javax.swing.JCheckBox();
        chkPublishedDateAsYear = new javax.swing.JCheckBox();
        lblPublishedDateAsYear = new javax.swing.JLabel();
        lblIncludeAddedInBookDetails = new javax.swing.JLabel();
        chkIncludeAddedInBookDetails = new javax.swing.JCheckBox();
        lblIncludeRatingInBookDetails = new javax.swing.JLabel();
        chkIncludeRatingInBookDetails = new javax.swing.JCheckBox();
        txtBookDetailsCustomFields = new javax.swing.JTextField();
        lblBookDetailsCustomFields = new javax.swing.JLabel();
        chkBookDetailsCustomFieldsAlways = new javax.swing.JCheckBox();
        lblSingleBookCrossReferences = new javax.swing.JLabel();
        chkSingleBookCrossReferences = new javax.swing.JCheckBox();
        chkIncludeAuthorCrossReferences = new javax.swing.JCheckBox();
        lblIncludeAuthorCrossReferences = new javax.swing.JLabel();
        chkIncludeTagCrossReferences = new javax.swing.JCheckBox();
        lblIncludeTagCrossReferences = new javax.swing.JLabel();
        lblIncludeRatingCrossReferences = new javax.swing.JLabel();
        lblIncludeSerieCrossReferences = new javax.swing.JLabel();
        chkIncludeSerieCrossReferences = new javax.swing.JCheckBox();
        chkIncludeRatingCrossReferences = new javax.swing.JCheckBox();
        lblDisplaySeriesSort = new javax.swing.JLabel();
        chkDisplaySeriesSort = new javax.swing.JCheckBox();
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
        lblNoCoverResize = new javax.swing.JLabel();
        lblNoThumbnailGenerate = new javax.swing.JLabel();
        chkNoCoverResize = new javax.swing.JCheckBox();
        chkNoThumbnailGenerate = new javax.swing.JCheckBox();
        lblMaxBookSummaryLength = new javax.swing.JLabel();
        txtMaxBookSummaryLength = new javax.swing.JTextField();
        lblMinimizeChangedFiles = new javax.swing.JLabel();
        chkMinimizeChangedFiles = new javax.swing.JCheckBox();
        lblExternalIcons = new javax.swing.JLabel();
        chkExternalIcons = new javax.swing.JCheckBox();
        lblMaxSplitLevels = new javax.swing.JLabel();
        txtMaxSplitLevels = new javax.swing.JTextField();
        txtTagsToMakeDeep = new javax.swing.JTextField();
        lblTagsToMakeDeep = new javax.swing.JLabel();
        lblIncludeCoversInCatalog = new javax.swing.JLabel();
        chkIncludeCoversInCatalog = new javax.swing.JCheckBox();
        lblZipCatalog = new javax.swing.JLabel();
        lblZipOmitXml = new javax.swing.JLabel();
        chkZipOmitXml = new javax.swing.JCheckBox();
        chkZipCatalog = new javax.swing.JCheckBox();
        lblUseThumbnailAsCover = new javax.swing.JLabel();
        chkUseThumbnailAsCover = new javax.swing.JCheckBox();
        lblexternalImages = new javax.swing.JLabel();
        chkExternalImages = new javax.swing.JCheckBox();
        lblLanguageAsTag = new javax.swing.JLabel();
        chkLanguageAsTag = new javax.swing.JCheckBox();
        pnlExternalUrlsOptions = new javax.swing.JPanel();
        txtWikipediaUrl = new javax.swing.JTextField();
        txtAmazonAuthorUrl = new javax.swing.JTextField();
        txtAmazonIsbnUrl = new javax.swing.JTextField();
        txtAmazonTitleUrl = new javax.swing.JTextField();
        txtGoodreadAuthorUrl = new javax.swing.JTextField();
        txtGoodreadIsbnUrl = new javax.swing.JTextField();
        txtGoodreadTitleUrl = new javax.swing.JTextField();
        txtGoodreadReviewIsbnUrl = new javax.swing.JTextField();
        txtIsfdbAuthorUrl = new javax.swing.JTextField();
        txtLibrarythingAuthorUrl = new javax.swing.JTextField();
        txtLibrarythingIsbnUrl = new javax.swing.JTextField();
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
        lblWikipediaUrl = new javax.swing.JTextField();
        lblAmazonAuthorUrl = new javax.swing.JTextField();
        lblAmazonIsbnUrl = new javax.swing.JTextField();
        lblAmazonTitleUrl = new javax.swing.JTextField();
        lblGoodreadAuthorUrl = new javax.swing.JTextField();
        lblGoodreadIsbnUrl = new javax.swing.JTextField();
        lblGoodreadTitleUrl = new javax.swing.JTextField();
        lblIsfdbAuthorUrl = new javax.swing.JTextField();
        lblLibrarythingAuthorUrl = new javax.swing.JTextField();
        lblLibrarythingIsbnUrl = new javax.swing.JTextField();
        lblLibrarythingTitleUrl = new javax.swing.JTextField();
        lblNogenerateexternallinks = new javax.swing.JLabel();
        chkNogenerateexternallinks = new javax.swing.JCheckBox();
        lblGoodreadReviewIsbnUrl = new javax.swing.JTextField();
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
        pnlSearchOptions = new javax.swing.JPanel();
        txtMaxKeywords = new javax.swing.JTextField();
        lblMaxKeywords = new javax.swing.JLabel();
        lblIndexComments = new javax.swing.JLabel();
        chkIndexComments = new javax.swing.JCheckBox();
        lblIndexFilterAlgorithm = new javax.swing.JLabel();
        cboIndexFilterAlgorithm = new javax.swing.JComboBox();
        lblGenerateIndex = new javax.swing.JLabel();
        chkGenerateIndex = new javax.swing.JCheckBox();
        lblSearchDeprecated = new javax.swing.JLabel();
        pnlBottom = new javax.swing.JPanel();
        lblBottom0 = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        cmdCancel = new javax.swing.JButton();
        cmdReset = new javax.swing.JButton();
        cmdSave = new javax.swing.JButton();
        cmdGenerate = new javax.swing.JButton();
        cmdHelp = new javax.swing.JButton();
        pnlTitle = new javax.swing.JPanel();
        lblProfile = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuFileSave = new javax.swing.JMenuItem();
        mnuFileGenerateCatalogs = new javax.swing.JMenuItem();
        mnuFileExit = new javax.swing.JMenuItem();
        mnuProfiles = new javax.swing.JMenu();
        mnuTools = new javax.swing.JMenu();
        mnuToolsprocessEpubMetadataOfAllBooks = new javax.swing.JMenuItem();
        mnuToolsResetSecurityCache = new javax.swing.JMenuItem();
        mnuToolsConfigLog = new javax.swing.JMenuItem();
        mnuToolsClearLog = new javax.swing.JMenuItem();
        mnuToolsOpenLog = new javax.swing.JMenuItem();
        mnuToolsOpenSyncLog = new javax.swing.JMenuItem();
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
        setMinimumSize(new java.awt.Dimension(700, 550));
        setPreferredSize(new java.awt.Dimension(1050, 760));

        pnlMain.setMinimumSize(new java.awt.Dimension(910, 750));
        pnlMain.setName(""); // NOI18N
        pnlMain.setPreferredSize(new java.awt.Dimension(900, 760));
        pnlMain.setLayout(new java.awt.GridBagLayout());

        lblDeviceDropbox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/calibre-icon.gif"))); // NOI18N
        lblDeviceDropbox.setMinimumSize(new java.awt.Dimension(80, 61));
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
        lblDeviceNAS.setMinimumSize(new java.awt.Dimension(61, 61));
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
        lblDeviceNook.setMinimumSize(new java.awt.Dimension(50, 61));
        lblDeviceNook.setName(""); // NOI18N
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

        lblDeviceMode.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDeviceMode.setText("lblDeviceMode1");
        lblDeviceMode.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblDeviceMode.setRequestFocusEnabled(false);
        lblDeviceMode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        pnlMain.add(lblDeviceMode, gridBagConstraints);

        lblDonate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/donate.gif"))); // NOI18N
        lblDonate.setMinimumSize(new java.awt.Dimension(80, 28));
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

        tabOptionsTabs.setMinimumSize(new java.awt.Dimension(850, 470));
        tabOptionsTabs.setPreferredSize(new java.awt.Dimension(90, 480));

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
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 2;
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
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 3;
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
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblUrlBooks, gridBagConstraints);

        txtUrlBooks.setText("txtUrlBooks");
        txtUrlBooks.setPreferredSize(new java.awt.Dimension(400, 20));
        txtUrlBooks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                txtUrlBooksMouseExited(evt);
            }
        });
        txtUrlBooks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckOnlyCatalogAllowed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogFolder, gridBagConstraints);

        txtCatalogFolder.setText("txtCatalogFolder");
        txtCatalogFolder.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogTitle, gridBagConstraints);

        txtCatalogTitle.setText("txtCatalogTitle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
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
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblSplittagson, gridBagConstraints);

        pnlSplitTagsOn.setLayout(new java.awt.GridBagLayout());

        txtSplittagson.setText("txtSplittagson");
        txtSplittagson.setPreferredSize(new java.awt.Dimension(40, 20));
        txtSplittagson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSplittagsonActionPerformed(evt);
            }
        });
        txtSplittagson.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSplittagsonFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pnlSplitTagsOn.add(chkDontsplittags, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblCatalogFilter, gridBagConstraints);

        txtCatalogFilter.setText("txtCatalogFilter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblWikilang, gridBagConstraints);

        txtWikilang.setText("txtWikilang");
        txtWikilang.setPreferredSize(new java.awt.Dimension(60, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtWikilang, gridBagConstraints);

        chkCopyToDatabaseFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCatalogFolderNeeded(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblCopyToDatabaseFolder, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblZipTrookCatalog, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblOnlyCatalogAtTarget, gridBagConstraints);

        chkOnlyCatalogAtTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCatalogFolderNeeded(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkOnlyCatalogAtTarget, gridBagConstraints);

        lblCryptFilenames.setText("lblCryptFilenames");
        lblCryptFilenames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(lblCryptFilenames, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlMainOptions.add(chkCryptFilenames, gridBagConstraints);

        lblFavicon.setText("lblFavicon");
        lblFavicon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlMainOptions.add(lblFavicon, gridBagConstraints);

        txtFavicon.setText("txtFavicon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(txtFavicon, gridBagConstraints);

        cmdSetFavicon.setText("...");
        cmdSetFavicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSetFaviconActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlMainOptions.add(cmdSetFavicon, gridBagConstraints);

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

        lblSortUsingAuthor.setText("lblSortUsingAuthor");
        lblSortUsingAuthor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
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
        gridBagConstraints.gridy = 11;
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
        pnlCatalogStructure.add(chkSortUsingAuthorSort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSortUsingTitleSort, gridBagConstraints);

        lblTagsToIgnore.setText("lblTagsToIgnore");
        lblTagsToIgnore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblTagsToIgnore, gridBagConstraints);

        txtTagsToIgnore.setText("txtTagsToIgnore");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(txtTagsToIgnore, gridBagConstraints);

        lblCatalogCustomColumns.setText("lblCatalogCustomColumns");
        lblCatalogCustomColumns.setToolTipText("");
        lblCatalogCustomColumns.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblCatalogCustomColumns, gridBagConstraints);

        txtCatalogCustomColumns.setText("txtCatalogCustomColumns");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(txtCatalogCustomColumns, gridBagConstraints);

        lblSortTagsByAuthor.setText("lblSortTagsByAuthor");
        lblSortTagsByAuthor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSortTagsByAuthor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSortTagsByAuthor, gridBagConstraints);

        lblTagBooksNoSplit.setText("lblTagBooksNoSplit");
        lblTagBooksNoSplit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblTagBooksNoSplit, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkTagBookNoSplit, gridBagConstraints);

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

        lblSortUsingSeries.setText("lblSortUsingSeries");
        lblSortUsingSeries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSortUsingSerieshandleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(lblSortUsingSeries, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCatalogStructure.add(chkSortUsingSeriesSort, gridBagConstraints);

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

        lblDisplayAuthorSort.setText("lblDisplayAuthorSort");
        lblDisplayAuthorSort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblDisplayAuthorSort, gridBagConstraints);
        lblDisplayAuthorSort.getAccessibleContext().setAccessibleName("");

        lblDisplayTitleSort.setText("lblDisplayTitleSort");
        lblDisplayTitleSort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblDisplayTitleSort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkDisplayAuthorSort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkDisplayTitleSort, gridBagConstraints);

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

        chkNogeneratecrosslinks.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkNogeneratecrosslinksStateChanged(evt);
            }
        });
        chkNogeneratecrosslinks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNogeneratecrosslinksActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkNogeneratecrosslinks, gridBagConstraints);
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

        lblIncludeAddedInBookDetails.setText("lblIncludeAddedInBookDetails");
        lblIncludeAddedInBookDetails.setOpaque(true);
        lblIncludeAddedInBookDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeAddedInBookDetails, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeAddedInBookDetails, gridBagConstraints);

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
        pnlBookDetails.add(chkIncludeRatingInBookDetails, gridBagConstraints);

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
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkBookDetailsCustomFieldsAlways, gridBagConstraints);

        lblSingleBookCrossReferences.setText("lblSingleBookCrossReferences");
        lblSingleBookCrossReferences.setToolTipText("");
        lblSingleBookCrossReferences.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblSingleBookCrossReferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkSingleBookCrossReferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeAuthorCrossReferences, gridBagConstraints);

        lblIncludeAuthorCrossReferences.setText("lblIncludeAuthorCrossReferences");
        lblIncludeAuthorCrossReferences.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeAuthorCrossReferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
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
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeTagCrossReferences, gridBagConstraints);

        lblIncludeRatingCrossReferences.setText("lblIncludeRatingCrossReferences");
        lblIncludeRatingCrossReferences.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeRatingCrossReferences, gridBagConstraints);

        lblIncludeSerieCrossReferences.setText("lblIncludeSerieCrossReferences");
        lblIncludeSerieCrossReferences.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblIncludeSerieCrossReferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeSerieCrossReferences, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkIncludeRatingCrossReferences, gridBagConstraints);

        lblDisplaySeriesSort.setText("lblDisplaySeriesSort");
        lblDisplaySeriesSort.setToolTipText("");
        lblDisplaySeriesSort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(lblDisplaySeriesSort, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlBookDetails.add(chkDisplaySeriesSort, gridBagConstraints);

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
        txtMaxbeforepaginate.setMaximumSize(new java.awt.Dimension(100, 100));
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
        txtMaxbeforesplit.setMaximumSize(new java.awt.Dimension(100, 100));
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
        txtBooksinrecent.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBooksinrecentFocusLost(evt);
            }
        });
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
        txtThumbnailheight.setMaximumSize(new java.awt.Dimension(100, 100));
        txtThumbnailheight.setPreferredSize(new java.awt.Dimension(40, 20));
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblMinBooksToMakeDeepLevel, gridBagConstraints);

        txtMinBooksToMakeDeepLevel.setText("txtMaxsummarylength");
        txtMinBooksToMakeDeepLevel.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMinBooksToMakeDeepLevel, gridBagConstraints);

        txtCoverHeight.setText("txtCoverHeight");
        txtCoverHeight.setMaximumSize(new java.awt.Dimension(100, 100));
        txtCoverHeight.setPreferredSize(new java.awt.Dimension(40, 20));
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlAdvancedOptions.add(lblMinimizeChangedFiles, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblExternalIcons, gridBagConstraints);

        chkExternalIcons.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
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
        txtMaxSplitLevels.setMaximumSize(new java.awt.Dimension(100, 100));
        txtMaxSplitLevels.setPreferredSize(new java.awt.Dimension(40, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(txtMaxSplitLevels, gridBagConstraints);

        txtTagsToMakeDeep.setText("txtTagsToMakeDeep");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
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
        gridBagConstraints.gridy = 11;
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblIncludeCoversInCatalog, gridBagConstraints);

        chkIncludeCoversInCatalog.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkIncludeCoversInCatalog, gridBagConstraints);

        lblZipCatalog.setText("lblZipCatalog");
        lblZipCatalog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblZipCatalog, gridBagConstraints);

        lblZipOmitXml.setText("lblZipOmitXml");
        lblZipOmitXml.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblZipOmitXml, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkZipOmitXml, gridBagConstraints);

        chkZipCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkZipCatalogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkZipCatalog, gridBagConstraints);

        lblUseThumbnailAsCover.setText("lblUseThumbnailAsCover");
        lblUseThumbnailAsCover.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblUseThumbnailAsCover, gridBagConstraints);

        chkUseThumbnailAsCover.setRequestFocusEnabled(false);
        chkUseThumbnailAsCover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkUseThumbnailAsCoverActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkUseThumbnailAsCover, gridBagConstraints);

        lblexternalImages.setText("lblexternalImages");
        lblexternalImages.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblexternalImages, gridBagConstraints);

        chkExternalImages.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkExternalImages, gridBagConstraints);

        lblLanguageAsTag.setText("lblLanguageAsTag");
        lblLanguageAsTag.setToolTipText("");
        lblLanguageAsTag.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(lblLanguageAsTag, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlAdvancedOptions.add(chkLanguageAsTag, gridBagConstraints);

        tabOptionsTabs.addTab("pnlAdvancedOptions", pnlAdvancedOptions);

        pnlExternalUrlsOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlExternalUrlsOptionsComponentShown(evt);
            }
        });
        pnlExternalUrlsOptions.setLayout(new java.awt.GridBagLayout());

        txtWikipediaUrl.setText("txtWikipediaUrl");
        txtWikipediaUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtWikipediaUrl, gridBagConstraints);

        txtAmazonAuthorUrl.setText("txtAmazonAuthorUrl");
        txtAmazonAuthorUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonAuthorUrl, gridBagConstraints);

        txtAmazonIsbnUrl.setText("txtAmazonIsbnUrl");
        txtAmazonIsbnUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonIsbnUrl, gridBagConstraints);

        txtAmazonTitleUrl.setText("txtAmazonTitleUrl");
        txtAmazonTitleUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtAmazonTitleUrl, gridBagConstraints);

        txtGoodreadAuthorUrl.setText("txtGoodreadAuthorUrl");
        txtGoodreadAuthorUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadAuthorUrl, gridBagConstraints);

        txtGoodreadIsbnUrl.setText("txtGoodreadIsbnUrl");
        txtGoodreadIsbnUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadIsbnUrl, gridBagConstraints);

        txtGoodreadTitleUrl.setText("txtGoodreadTitleUrl");
        txtGoodreadTitleUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadTitleUrl, gridBagConstraints);

        txtGoodreadReviewIsbnUrl.setText("txtGoodreadReviewIsbnUrl");
        txtGoodreadReviewIsbnUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtGoodreadReviewIsbnUrl, gridBagConstraints);

        txtIsfdbAuthorUrl.setText("txtIsfdbAuthorUrl");
        txtIsfdbAuthorUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtIsfdbAuthorUrl, gridBagConstraints);

        txtLibrarythingAuthorUrl.setText("txtLibrarythingAuthorUrl");
        txtLibrarythingAuthorUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingAuthorUrl, gridBagConstraints);

        txtLibrarythingIsbnUrl.setText("txtLibrarythingIsbnUrl");
        txtLibrarythingIsbnUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(txtLibrarythingIsbnUrl, gridBagConstraints);

        txtLibrarythingTitleUrl.setText("txtLibrarythingTitleUrl");
        txtLibrarythingTitleUrl.setPreferredSize(new java.awt.Dimension(600, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdIsfdbAuthorReset, gridBagConstraints);

        cmdLibrarythingAuthorReset.setText("Reset!");
        cmdLibrarythingAuthorReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingAuthorResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingAuthorReset, gridBagConstraints);

        cmdLibrarythingIsbnReset.setText("Reset!");
        cmdLibrarythingIsbnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingIsbnResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingIsbnReset, gridBagConstraints);

        cmdLibrarythingTitleReset.setText("Reset!");
        cmdLibrarythingTitleReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdLibrarythingTitleResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(cmdLibrarythingTitleReset, gridBagConstraints);

        lblWikipediaUrl.setText("lblWikipediaUrl");
        lblWikipediaUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblWikipediaUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblWikipediaUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblWikipediaUrl, gridBagConstraints);

        lblAmazonAuthorUrl.setText("lblAmazonAuthorUrl");
        lblAmazonAuthorUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblAmazonAuthorUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblAmazonAuthorUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        lblAmazonAuthorUrl.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonAuthorUrl, gridBagConstraints);

        lblAmazonIsbnUrl.setText("lblAmazonIsbnUrl");
        lblAmazonIsbnUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblAmazonIsbnUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblAmazonIsbnUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        lblAmazonIsbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblAmazonIsbnUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonIsbnUrl, gridBagConstraints);

        lblAmazonTitleUrl.setText("lblAmazonTitleUrl");
        lblAmazonTitleUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblAmazonTitleUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblAmazonTitleUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        lblAmazonTitleUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblAmazonTitleUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblAmazonTitleUrl, gridBagConstraints);

        lblGoodreadAuthorUrl.setText("lblGoodreadAuthorUrl");
        lblGoodreadAuthorUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblGoodreadAuthorUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblGoodreadAuthorUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadAuthorUrl, gridBagConstraints);

        lblGoodreadIsbnUrl.setText("lblGoodreadIsbnUrl");
        lblGoodreadIsbnUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblGoodreadIsbnUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblGoodreadIsbnUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadIsbnUrl, gridBagConstraints);

        lblGoodreadTitleUrl.setText("lblGoodreadTitleUrl");
        lblGoodreadTitleUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblGoodreadTitleUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblGoodreadTitleUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadTitleUrl, gridBagConstraints);

        lblIsfdbAuthorUrl.setText("tlblsfdbAuthorUrl");
        lblIsfdbAuthorUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblIsfdbAuthorUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblIsfdbAuthorUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblIsfdbAuthorUrl, gridBagConstraints);

        lblLibrarythingAuthorUrl.setText("lblLibrarythingAuthorUrl");
        lblLibrarythingAuthorUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblLibrarythingAuthorUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblLibrarythingAuthorUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingAuthorUrl, gridBagConstraints);

        lblLibrarythingIsbnUrl.setText("lblLibrarythingIsbnUrl");
        lblLibrarythingIsbnUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblLibrarythingIsbnUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblLibrarythingIsbnUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        lblLibrarythingIsbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblLibrarythingIsbnUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingIsbnUrl, gridBagConstraints);

        lblLibrarythingTitleUrl.setText("lblLibrarythingTitleUrl");
        lblLibrarythingTitleUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblLibrarythingTitleUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblLibrarythingTitleUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        lblLibrarythingTitleUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblLibrarythingTitleUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblLibrarythingTitleUrl, gridBagConstraints);

        lblNogenerateexternallinks.setText("lblNogenerateexternallinks");
        lblNogenerateexternallinks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(lblNogenerateexternallinks, gridBagConstraints);

        chkNogenerateexternallinks.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkNogenerateexternallinksStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlExternalUrlsOptions.add(chkNogenerateexternallinks, gridBagConstraints);

        lblGoodreadReviewIsbnUrl.setText("lblGoodreadReviewIsbnUrl");
        lblGoodreadReviewIsbnUrl.setMaximumSize(new java.awt.Dimension(250, 2147483647));
        lblGoodreadReviewIsbnUrl.setMinimumSize(new java.awt.Dimension(250, 22));
        lblGoodreadReviewIsbnUrl.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlExternalUrlsOptions.add(lblGoodreadReviewIsbnUrl, gridBagConstraints);

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
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pnlCustomCatalogs.add(scrCustomCatalogs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        pnlCustomCatalogs.add(pnlCustomCatalogsTableButtons, gridBagConstraints);

        tabOptionsTabs.addTab("pnlCustomCatalogs", pnlCustomCatalogs);

        pnlSearchOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlSearchComponentShown(evt);
            }
        });
        pnlSearchOptions.setLayout(new java.awt.GridBagLayout());

        txtMaxKeywords.setText("txtMaxKeywords");
        txtMaxKeywords.setMinimumSize(new java.awt.Dimension(150, 26));
        txtMaxKeywords.setName(""); // NOI18N
        txtMaxKeywords.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSearchOptions.add(txtMaxKeywords, gridBagConstraints);

        lblMaxKeywords.setText("lblMaxKeywords");
        lblMaxKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlSearchOptions.add(lblMaxKeywords, gridBagConstraints);

        lblIndexComments.setText("lblIndexComments");
        lblIndexComments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlSearchOptions.add(lblIndexComments, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlSearchOptions.add(chkIndexComments, gridBagConstraints);

        lblIndexFilterAlgorithm.setText("lblIndexFilterAlgorithm");
        lblIndexFilterAlgorithm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        pnlSearchOptions.add(lblIndexFilterAlgorithm, gridBagConstraints);

        cboIndexFilterAlgorithm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboIndexFilterAlgorithm.setMinimumSize(new java.awt.Dimension(200, 26));
        cboIndexFilterAlgorithm.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        pnlSearchOptions.add(cboIndexFilterAlgorithm, gridBagConstraints);

        lblGenerateIndex.setText("lblGenerateIndex");
        lblGenerateIndex.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        pnlSearchOptions.add(lblGenerateIndex, gridBagConstraints);

        chkGenerateIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGenerateIndexActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlSearchOptions.add(chkGenerateIndex, gridBagConstraints);

        lblSearchDeprecated.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSearchDeprecated.setText("lblSearchDeprecated");
        lblSearchDeprecated.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSearchDeprecatedhandleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        pnlSearchOptions.add(lblSearchDeprecated, gridBagConstraints);

        tabOptionsTabs.addTab("pnlSearchOptions", pnlSearchOptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
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

        lblProfile.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lblProfile.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblProfile.setText("lblProfile");
        lblProfile.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblProfile.setRequestFocusEnabled(false);
        lblProfile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClickOnLabel(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        pnlMain.add(lblProfile, gridBagConstraints);

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

        mnuToolsConfigLog.setText("mnuToolsConfigLog");
        mnuToolsConfigLog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mnuToolsConfigLogMouseClicked(evt);
            }
        });
        mnuToolsConfigLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsConfigLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsConfigLog);

        mnuToolsClearLog.setText("mnuToolsClearLog");
        mnuToolsClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsClearLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsClearLog);

        mnuToolsOpenLog.setText("mnuToolsOpenLog");
        mnuToolsOpenLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsOpenLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsOpenLog);

        mnuToolsOpenSyncLog.setText("mnuToolsOpenSyncLog");
        mnuToolsOpenSyncLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuToolsOpenSyncLogActionPerformed(evt);
            }
        });
        mnuTools.add(mnuToolsOpenSyncLog);

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
    txtAmazonAuthorUrl.setText((new DefaultConfigurationSettings()).getAmazonAuthorUrl());
  }//GEN-LAST:event_cmdAmazonUrlResetActionPerformed

  private void cmdAmazonIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonIsbnResetActionPerformed
    txtAmazonIsbnUrl.setText((new DefaultConfigurationSettings()).getAmazonIsbnUrl());
  }//GEN-LAST:event_cmdAmazonIsbnResetActionPerformed

  private void cmdAmazonTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAmazonTitleResetActionPerformed
    txtAmazonTitleUrl.setText((new DefaultConfigurationSettings()).getAmazonTitleUrl());
  }//GEN-LAST:event_cmdAmazonTitleResetActionPerformed

  private void cmdGoodreadAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadAuthorResetActionPerformed
    txtGoodreadAuthorUrl.setText((new DefaultConfigurationSettings()).getGoodreadAuthorUrl());
  }//GEN-LAST:event_cmdGoodreadAuthorResetActionPerformed

  private void cmdGoodreadIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadIsbnResetActionPerformed
    txtGoodreadIsbnUrl.setText((new DefaultConfigurationSettings()).getGoodreadIsbnUrl());
  }//GEN-LAST:event_cmdGoodreadIsbnResetActionPerformed

  private void cmdGoodreadTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadTitleResetActionPerformed
    txtGoodreadTitleUrl.setText((new DefaultConfigurationSettings()).getGoodreadTitleUrl());
  }//GEN-LAST:event_cmdGoodreadTitleResetActionPerformed

  private void cmdGoodreadReviewResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdGoodreadReviewResetActionPerformed
    txtGoodreadReviewIsbnUrl.setText((new DefaultConfigurationSettings()).getGoodreadReviewIsbnUrl());
  }//GEN-LAST:event_cmdGoodreadReviewResetActionPerformed

  private void cmdIsfdbAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdIsfdbAuthorResetActionPerformed
    txtIsfdbAuthorUrl.setText((new DefaultConfigurationSettings()).getIsfdbAuthorUrl());
  }//GEN-LAST:event_cmdIsfdbAuthorResetActionPerformed

  private void cmdLibrarythingAuthorResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingAuthorResetActionPerformed
    txtLibrarythingAuthorUrl.setText((new DefaultConfigurationSettings()).getLibrarythingAuthorUrl());
  }//GEN-LAST:event_cmdLibrarythingAuthorResetActionPerformed

  private void cmdLibrarythingIsbnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingIsbnResetActionPerformed
    txtLibrarythingIsbnUrl.setText((new DefaultConfigurationSettings()).getLibrarythingIsbnUrl());
  }//GEN-LAST:event_cmdLibrarythingIsbnResetActionPerformed

  private void cmdLibrarythingTitleResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLibrarythingTitleResetActionPerformed
    txtLibrarythingTitleUrl.setText((new DefaultConfigurationSettings()).getLibrarythingTitleUrl());
  }//GEN-LAST:event_cmdLibrarythingTitleResetActionPerformed

  private void cmdWikipediaUrlResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWikipediaUrlResetActionPerformed
    txtWikipediaUrl.setText((new DefaultConfigurationSettings()).getWikipediaUrl());
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

  private void mnuToolsConfigLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuToolsConfigLogActionPerformed
   configLogFile();
  }//GEN-LAST:event_mnuToolsConfigLogActionPerformed

  private void mnuToolsOpenLogActionPerformed(java.awt.event.ActionEvent evt) {                                                  
    debugShowLogFile();
  }

  private void mnuToolsOpenSyncLogActionPerformed(java.awt.event.ActionEvent evt) {
    File f = new File(ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
    if (f.exists()) {
      logger.info(Localization.Main.getText("gui.menu.tools.logFile") + ": " + f.getPath());
      debugShowFile(f);
    }
  }

  private void mnuToolsOpenConfigActionPerformed(java.awt.event.ActionEvent evt) {                                                
  debugShowSupportFolder();
  }                                                  

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

  private void checkDownloads(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkDownloads
      checkDownloads();
  }//GEN-LAST:event_checkDownloads

  private void CheckOnlyCatalogAllowed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckOnlyCatalogAllowed
     checkOnlyCatalogAllowed();
  }//GEN-LAST:event_CheckOnlyCatalogAllowed

  private void checkCatalogFolderNeeded(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCatalogFolderNeeded
    checkCatalogFolderNeeded();
  }//GEN-LAST:event_checkCatalogFolderNeeded

  private void txtUrlBooksMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtUrlBooksMouseExited
    checkOnlyCatalogAllowed();
  }//GEN-LAST:event_txtUrlBooksMouseExited

  private void lblLibrarythingIsbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblLibrarythingIsbnUrlActionPerformed
      // TODO add your handling code here:
  }//GEN-LAST:event_lblLibrarythingIsbnUrlActionPerformed

  private void chkUseThumbnailAsCoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUseThumbnailAsCoverActionPerformed
      // TODO add your handling code here:
  }//GEN-LAST:event_chkUseThumbnailAsCoverActionPerformed

  private void chkZipCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkZipCatalogActionPerformed
    boolean genOptions = !chkNogenerateopds.isSelected() && !chkNogeneratehtml.isSelected();
    lblZipOmitXml.setEnabled(chkZipCatalog.isSelected() && genOptions);
    chkZipOmitXml.setEnabled(lblZipOmitXml.isEnabled() && genOptions);
  }//GEN-LAST:event_chkZipCatalogActionPerformed

  private void txtBooksinrecentFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBooksinrecentFocusLost
    DefaultConfigurationSettings defaults = new DefaultConfigurationSettings();
    if (getValue(txtBooksinrecent) > defaults.getBooksInRecentAdditions()) {
      String message = Localization.Main.getText("error.recentTooLarge", defaults.getBooksInRecentAdditions());
      JOptionPane.showMessageDialog(this, message, "", JOptionPane.ERROR_MESSAGE);
      txtBooksinrecent.setText("" + defaults.getBooksInRecentAdditions());
    }
  }//GEN-LAST:event_txtBooksinrecentFocusLost

    private void lblAmazonTitleUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblAmazonTitleUrlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lblAmazonTitleUrlActionPerformed

    private void chkNogeneratecrosslinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNogeneratecrosslinksActionPerformed
      checkCrossReferencesEnabled();
    }//GEN-LAST:event_chkNogeneratecrosslinksActionPerformed

    private void chkNogeneratecrosslinksStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkNogeneratecrosslinksStateChanged
      checkCrossReferencesEnabled();
    }//GEN-LAST:event_chkNogeneratecrosslinksStateChanged

    private void lblAmazonIsbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblAmazonIsbnUrlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lblAmazonIsbnUrlActionPerformed

    private void lblLibrarythingTitleUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblLibrarythingTitleUrlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lblLibrarythingTitleUrlActionPerformed

    private void mnuToolsConfigLogMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mnuToolsConfigLogMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_mnuToolsConfigLogMouseClicked

    private void txtSplittagsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSplittagsonActionPerformed
      checkSplitTagsOn(true);
    }//GEN-LAST:event_txtSplittagsonActionPerformed

    private void txtSplittagsonFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSplittagsonFocusLost
      checkSplitTagsOn(true);
    }//GEN-LAST:event_txtSplittagsonFocusLost

    private void pnlSearchComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlSearchComponentShown
      tabHelpUrl = Constants.HELP_URL_SEARCH;
    }//GEN-LAST:event_pnlSearchComponentShown

    private void lblSortUsingSerieshandleMouseClickOnLabel(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSortUsingSerieshandleMouseClickOnLabel
        // TODO add your handling code here:
    }//GEN-LAST:event_lblSortUsingSerieshandleMouseClickOnLabel

    private void cmdSetFaviconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSetFaviconActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmdSetFaviconActionPerformed

    private void lblSearchDeprecatedhandleMouseClickOnLabel(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSearchDeprecatedhandleMouseClickOnLabel
        // TODO add your handling code here:
    }//GEN-LAST:event_lblSearchDeprecatedhandleMouseClickOnLabel

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
    checkSplitTagsOn(true);
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
    setDeviceSpecificMode(DeviceMode.Default);
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
    private javax.swing.JCheckBox chkBookDetailsCustomFieldsAlways;
    private javax.swing.JCheckBox chkBrowseByCover;
    private javax.swing.JCheckBox chkBrowseByCoverWithoutSplit;
    private javax.swing.JCheckBox chkCopyToDatabaseFolder;
    private javax.swing.JCheckBox chkCryptFilenames;
    private javax.swing.JCheckBox chkDisplayAuthorSort;
    private javax.swing.JCheckBox chkDisplaySeriesSort;
    private javax.swing.JCheckBox chkDisplayTitleSort;
    private javax.swing.JCheckBox chkDontsplittags;
    private javax.swing.JCheckBox chkExternalIcons;
    private javax.swing.JCheckBox chkExternalImages;
    private javax.swing.JCheckBox chkGenerateIndex;
    private javax.swing.JCheckBox chkIncludeAddedInBookDetails;
    private javax.swing.JCheckBox chkIncludeAuthorCrossReferences;
    private javax.swing.JCheckBox chkIncludeCoversInCatalog;
    private javax.swing.JCheckBox chkIncludeModifiedInBookDetails;
    private javax.swing.JCheckBox chkIncludeOnlyOneFile;
    private javax.swing.JCheckBox chkIncludePublishedInBookDetails;
    private javax.swing.JCheckBox chkIncludePublisherInBookDetails;
    private javax.swing.JCheckBox chkIncludeRatingCrossReferences;
    private javax.swing.JCheckBox chkIncludeRatingInBookDetails;
    private javax.swing.JCheckBox chkIncludeSerieCrossReferences;
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
    private javax.swing.JCheckBox chkSingleBookCrossReferences;
    private javax.swing.JCheckBox chkSortTagsByAuthor;
    private javax.swing.JCheckBox chkSortUsingAuthorSort;
    private javax.swing.JCheckBox chkSortUsingSeriesSort;
    private javax.swing.JCheckBox chkSortUsingTitleSort;
    private javax.swing.JCheckBox chkSplitByAuthorInitialGoToBooks;
    private javax.swing.JCheckBox chkSupressRatings;
    private javax.swing.JCheckBox chkTagBookNoSplit;
    private javax.swing.JCheckBox chkUseThumbnailAsCover;
    private javax.swing.JCheckBox chkZipCatalog;
    private javax.swing.JCheckBox chkZipOmitXml;
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
    private javax.swing.JButton cmdSetFavicon;
    private javax.swing.JButton cmdSetTargetFolder;
    private javax.swing.JButton cmdWikipediaUrlReset;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTextField lblAmazonAuthorUrl;
    private javax.swing.JTextField lblAmazonIsbnUrl;
    private javax.swing.JTextField lblAmazonTitleUrl;
    private javax.swing.JLabel lblBookDetailsCustomFields;
    private javax.swing.JLabel lblBooksinrecent;
    private javax.swing.JLabel lblBottom0;
    private javax.swing.JLabel lblBrowseByCover;
    private javax.swing.JLabel lblBrowseByCoverWithoutSplit;
    private javax.swing.JLabel lblCatalogCustomColumns;
    private javax.swing.JLabel lblCatalogFilter;
    private javax.swing.JLabel lblCatalogFolder;
    private javax.swing.JLabel lblCatalogTitle;
    private javax.swing.JLabel lblCopyToDatabaseFolder;
    private javax.swing.JLabel lblCoverHeight;
    private javax.swing.JLabel lblCryptFilenames;
    private javax.swing.JLabel lblCustomDummy1;
    private javax.swing.JLabel lblCustomDummy2;
    private javax.swing.JLabel lblDatabaseFolder;
    private javax.swing.JLabel lblDeviceDropbox;
    private javax.swing.JLabel lblDeviceMode;
    private javax.swing.JLabel lblDeviceNAS;
    private javax.swing.JLabel lblDeviceNook;
    private javax.swing.JLabel lblDisplayAuthorSort;
    private javax.swing.JLabel lblDisplaySeriesSort;
    private javax.swing.JLabel lblDisplayTitleSort;
    private javax.swing.JLabel lblDonate;
    private javax.swing.JLabel lblExternalIcons;
    private javax.swing.JLabel lblFavicon;
    private javax.swing.JLabel lblFeaturedCatalogSavedSearchName;
    private javax.swing.JLabel lblFeaturedCatalogTitle;
    private javax.swing.JLabel lblGenerateIndex;
    private javax.swing.JTextField lblGoodreadAuthorUrl;
    private javax.swing.JTextField lblGoodreadIsbnUrl;
    private javax.swing.JTextField lblGoodreadReviewIsbnUrl;
    private javax.swing.JTextField lblGoodreadTitleUrl;
    private javax.swing.JLabel lblIncludeAddedInBookDetails;
    private javax.swing.JLabel lblIncludeAuthorCrossReferences;
    private javax.swing.JLabel lblIncludeCoversInCatalog;
    private javax.swing.JLabel lblIncludeModifiedInBookDetails1;
    private javax.swing.JLabel lblIncludeOnlyOneFile;
    private javax.swing.JLabel lblIncludePublishedInBookDetails;
    private javax.swing.JLabel lblIncludePublisherInBookDetails;
    private javax.swing.JLabel lblIncludeRatingCrossReferences;
    private javax.swing.JLabel lblIncludeRatingInBookDetails;
    private javax.swing.JLabel lblIncludeSerieCrossReferences;
    private javax.swing.JLabel lblIncludeSeriesInBookDetails;
    private javax.swing.JLabel lblIncludeTagCrossReferences;
    private javax.swing.JLabel lblIncludeTagsInBookDetails;
    private javax.swing.JLabel lblIncludeemptybooks;
    private javax.swing.JLabel lblIncludeformat;
    private javax.swing.JLabel lblIndexComments;
    private javax.swing.JLabel lblIndexFilterAlgorithm;
    private javax.swing.JTextField lblIsfdbAuthorUrl;
    private javax.swing.JLabel lblLang;
    private javax.swing.JLabel lblLanguageAsTag;
    private javax.swing.JTextField lblLibrarythingAuthorUrl;
    private javax.swing.JTextField lblLibrarythingIsbnUrl;
    private javax.swing.JTextField lblLibrarythingTitleUrl;
    private javax.swing.JLabel lblMaxBookSummaryLength;
    private javax.swing.JLabel lblMaxKeywords;
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
    private javax.swing.JLabel lblProfile;
    private javax.swing.JLabel lblPublishedDateAsYear;
    private javax.swing.JLabel lblReprocessEpubMetadata;
    private javax.swing.JLabel lblSearchDeprecated;
    private javax.swing.JLabel lblSingleBookCrossReferences;
    private javax.swing.JLabel lblSortTagsByAuthor;
    private javax.swing.JLabel lblSortUsingAuthor;
    private javax.swing.JLabel lblSortUsingSeries;
    private javax.swing.JLabel lblSortUsingTitle;
    private javax.swing.JLabel lblSplitByAuthorInitialGoToBooks;
    private javax.swing.JLabel lblSplittagson;
    private javax.swing.JLabel lblSupressRatings;
    private javax.swing.JLabel lblTagBooksNoSplit;
    private javax.swing.JLabel lblTagsToIgnore;
    private javax.swing.JLabel lblTagsToMakeDeep;
    private javax.swing.JLabel lblTargetFolder;
    private javax.swing.JLabel lblThumbnailheight;
    private javax.swing.JLabel lblUrlBooks;
    private javax.swing.JLabel lblUseThumbnailAsCover;
    private javax.swing.JLabel lblWikilang;
    private javax.swing.JTextField lblWikipediaUrl;
    private javax.swing.JLabel lblZipCatalog;
    private javax.swing.JLabel lblZipOmitXml;
    private javax.swing.JLabel lblZipTrookCatalog;
    private javax.swing.JLabel lblexternalImages;
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
    private javax.swing.JMenuItem mnuToolsConfigLog;
    private javax.swing.JMenuItem mnuToolsOpenConfig;
    private javax.swing.JMenuItem mnuToolsOpenLog;
    private javax.swing.JMenuItem mnuToolsOpenSyncLog;
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
    private javax.swing.JPanel pnlSearchOptions;
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
    private javax.swing.JTextField txtCatalogCustomColumns;
    private javax.swing.JTextField txtCatalogFilter;
    private javax.swing.JTextField txtCatalogFolder;
    private javax.swing.JTextField txtCatalogTitle;
    private javax.swing.JTextField txtCoverHeight;
    private javax.swing.JTextField txtDatabaseFolder;
    private javax.swing.JTextField txtFavicon;
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
