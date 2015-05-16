/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfigureLoggingDialog.java
 *
 * Created on 10 juin 2010, 14:26:23
 */

package com.gmail.dpierron.calibre.gui;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.tools.OS;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * @author David Pierron
 */
public class ConfigureLoggingDialog extends javax.swing.JDialog {
  private final Logger logger = LogManager.getLogger(ConfigureLoggingDialog.class);
  private guiField[] guiFields;
  private String activeName = "";

  /**
   * Creates new form ProfileManagerDialog
   */
  public ConfigureLoggingDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();
    initGuiFields();
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

        // Main Windows

        new guiField(cmdNew, null, "gui.logging.new"),
        new guiField(cmdRename, null, "gui.logging.rename"),
        new guiField(cmdDelete, null, "gui.logging.delete"),
        new guiField(cmdView, null, "gui.logging.view"),
        new guiField(cmdEdit, null, "gui.logging.edit"),
        new guiField(cmdUse, null, "gui.logging.use"),
        new guiField(cmdClose, null, "gui.logging.close"),
        new guiField(lblActive, null, "gui.logging.active")
    };
  }

  /**
   * Convert a filename to the config name
   *
   * @param filename
   * @return
   */
  private String getConfigFromFilename (String filename) {
    assert filename.startsWith(ConfigurationManager.LOGGING_PREFIX);
    assert filename.endsWith(ConfigurationManager.LOGGING_SUFFIX);
    filename = filename.substring(ConfigurationManager.LOGGING_PREFIX.length());                  // Remove prefix
    filename = filename.substring(0, filename.length()-ConfigurationManager.LOGGING_SUFFIX.length()); // Remove suffix
    return filename;
  }

  /**
   * Apply localization to this dialog
   */
  private void translateTexts() {
    // Do translations that are handled by guiFields table
    for (guiField f : guiFields){
      f.translateTexts();
    }
    cmdEdit.setVisible(false);        // **** Hide until ready to use ****
    txtActive.setText(activeName);
    checkActiveButtons();
  }

  /**
   * Display in a popup the tooltip associated with a label that the user has clicked on
   * This is for convenience in environments where the tootip is not conveniently displayed.
   *
   * @param label
   */
  private void popupExplanation(JLabel label) {
    if (Helper.isNotNullOrEmpty(label.getToolTipText()))
      JOptionPane.showMessageDialog(this, label.getToolTipText(), Localization.Main.getText("gui.tooltip"), JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Set buttons inactive until a configuration is selected.
   */
  private void checkActiveButtons() {
    int [] indices = lstLoggingConfigurations.getSelectedIndices();
    boolean selected = indices.length > 0;
    cmdNew.setEnabled(selected);
    cmdRename.setEnabled(selected);
    cmdDelete.setEnabled(selected);
    cmdView.setEnabled(selected);
    cmdEdit.setEnabled(selected);
    cmdUse.setEnabled(selected);
  }
  /**
   * Convert a config name to the filename
   *
   * @param config
   * @return
   */
  private String getFilenameFromConfig (String config) {
    return ConfigurationManager.LOGGING_PREFIX + config + ConfigurationManager.LOGGING_SUFFIX;
  }


  private void loadLoggingConfigurations() {
    DefaultListModel listOfLoggingConfigurations = new DefaultListModel();
    File loggingFolder = new File(ConfigurationManager.getConfigurationDirectory(),File.separator + Constants.LOGFILE_FOLDER);
    String[] files = loggingFolder.list(new FilenameFilter() {

      public boolean accept(File dir, String name) {
        return name.toUpperCase().endsWith(ConfigurationManager.LOGGING_SUFFIX.toUpperCase());
      }
    });

    String lastFile = files[files.length-1];
    // We assume last file is always the active one!
    assert lastFile.equalsIgnoreCase(ConfigurationManager.LOGGING_FILENAME);
    long activeSize = (new File(ConfigurationManager.getConfigurationDirectory() + File.separator
                                + Constants.LOGFILE_FOLDER + File.separator + lastFile)).length();
    for (String file : files) {
      // The default should always be the first one found
      if (ConfigurationManager.LOGGING_FILENAME.equals(file)) {
        continue;
      }
      String config = getConfigFromFilename(file);
      assert activeSize != 0;
      if (activeSize == (new File(ConfigurationManager.getConfigurationDirectory() + File.separator
          + Constants.LOGFILE_FOLDER + File.separator + file)).length()) {
        activeName = config;
      }
      listOfLoggingConfigurations.addElement(config);
    }
    lstLoggingConfigurations.setModel(listOfLoggingConfigurations);
    if (txtActive != null) txtActive.setText(activeName);
  }

  /**
   * Switch to using a different logging configuration
   *
   * @param indices
   */
  private void useLoggingConfiguration( int [] indices) {
    if (indices == null ||  indices.length < 1) {
      String msg = Localization.Main.getText("gui.logging.use.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      logger.warn(msg);
      return;
    }
    String newConfig = (String) lstLoggingConfigurations.getModel().getElementAt(indices[0]);

    File activeConfigFile = new File(ConfigurationManager.getConfigurationDirectory(),
                                    Constants.LOGFILE_FOLDER + File.separator + ConfigurationManager.LOGGING_FILENAME);
    // TODO Work out how to get configuration file closed so rename can work!
    File activeBackupFile = new File(activeConfigFile+ ".bak");
    if (activeConfigFile.renameTo(activeBackupFile) != true) {
      String msg = Localization.Main.getText("gui.logging.use.renameFail");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      logger.warn(msg);
      return;
    }else {
      File newConfigFile =
          new File(ConfigurationManager.getConfigurationDirectory(), Constants.LOGFILE_FOLDER + File.separator + getFilenameFromConfig(newConfig));
      try {
        Helper.copy(newConfigFile, activeConfigFile);
      } catch (IOException e) {
        activeConfigFile.delete();
        activeBackupFile.renameTo(activeConfigFile);
        String msg = Localization.Main.getText("gui.logging.use.copyFail");
        JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
        logger.warn(msg);
        return;
      }
    }
    activeName = newConfig;
    loadLoggingConfigurations();
    String msg = Localization.Main.getText("gui.logging.use.success", newConfig);
    JOptionPane.showMessageDialog(this, msg, "", JOptionPane.INFORMATION_MESSAGE);
    logger.info(msg);
  }

  /**
   * Rename a logging configration.
   *
   * Note.  The default ones will get recreated on next run if they are renamed
   *
   * @param index
   */
  private void renameLoggingConfiguration(int index) {
    if (index < 0 || index >= lstLoggingConfigurations.getModel().getSize())
      return;

    String logConfig = (String) lstLoggingConfigurations.getModel().getElementAt(index);
    String newLogConfig = JOptionPane.showInputDialog(Localization.Main.getText("gui.logging.rename.msg", logConfig), logConfig);
    if ("default".equalsIgnoreCase(newLogConfig))
      return;
    if (Helper.isNotNullOrEmpty(newLogConfig) && !newLogConfig.equals(logConfig)) {
      File configFile =
          new File(ConfigurationManager.getConfigurationDirectory(),
                    Constants.LOGFILE_FOLDER + File.separator +
                     ConfigurationManager.LOGGING_PREFIX + logConfig + ConfigurationManager.LOGGING_PREFIX);
      if (configFile.exists()) {
        configFile.renameTo(new File(ConfigurationManager.getConfigurationDirectory(),
                              Constants.LOGFILE_FOLDER + File.separator
                              + ConfigurationManager.LOGGING_PREFIX + newLogConfig + ConfigurationManager.LOGGING_PREFIX));
        loadLoggingConfigurations();
      }
    }
  }

  /**
   * Delete a logging configuration
   *
   * Note.  The default ones will get recreated on next run if deleted.
   *
   * @param indices
   */
  private void deleteLoggingConfiguration(int[] indices) {
    if (indices == null ||  indices.length < 1) {
      String msg = Localization.Main.getText("gui.logging.delete.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }

    for (int index : indices) {
      String loggingConfig = (String) lstLoggingConfigurations.getModel().getElementAt(index);
      int result = JOptionPane
          .showConfirmDialog(this, Localization.Main.getText("gui.logging.delete.msg", loggingConfig), null, JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE);
      if (result != JOptionPane.CANCEL_OPTION) {
        File profileFile =
            new File(ConfigurationManager.getConfigurationDirectory(), loggingConfig + ConfigurationManager.LOGGING_SUFFIX);
        if (profileFile.exists()) {
          profileFile.delete();
        }
      }
    }
    loadLoggingConfigurations();
  }

  /**
   * Create a new Logging Configuration as a copy of the one selected
   *
   * @param indices
   */
  private void newLoggingConfiguration(int [] indices) {
    if (indices == null ||  indices.length != 1) {
      String msg = Localization.Main.getText("gui.logging.new.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String loggingConfig = JOptionPane.showInputDialog(Localization.Main.getText("gui.logging.new.msg"));
    if ("info".equalsIgnoreCase(loggingConfig)) {
      return;
    }
    ConfigurationManager.copyCurrentProfile(loggingConfig);
    loadLoggingConfigurations();

  }


  /**
   * View a Logging Configruation
   *
   * @param indices
   */
  private void viewLoggingConfiguration(int [] indices) {
    if (indices == null ||  indices.length != 1) {
      String msg = Localization.Main.getText("gui.logging.view.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String loggingConfig = (String) lstLoggingConfigurations.getModel().getElementAt(indices[0]);
    try {
      OS.factory().openFile(new File(ConfigurationManager.getConfigurationDirectory(),
                              Constants.LOGFILE_FOLDER + File.separator + getFilenameFromConfig(loggingConfig)));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
  /**
   * Edit a Logging Configruation
   *
   * @param indices
   */
  private void editLoggingConfiguration(int [] indices) {
    if (indices == null ||  indices.length != 1) {
      String msg = Localization.Main.getText("gui.logging.edit.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    JOptionPane.showMessageDialog(this, "NOT YET READY FOR USE", "", JOptionPane.WARNING_MESSAGE);
    String loggingConfig = (String) lstLoggingConfigurations.getModel().getElementAt(indices[0]);
/*
    // TODO   Work out what is needed here for editing?
    try {
      OS.factory().openFile(new File(ConfigurationManager.getConfigurationDirectory(),
          Constants.LOGFILE_FOLDER + File.separator + getFilenameFromConfig(loggingConfig)));
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
*/
  }
  /**
   * This method is called from within the constructor to
   * reset the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scrLogging = new javax.swing.JScrollPane();
        lstLoggingConfigurations = new javax.swing.JList();
        loadLoggingConfigurations();
        pnlButtons = new javax.swing.JPanel();
        cmdNew = new javax.swing.JButton();
        cmdRename = new javax.swing.JButton();
        cmdDelete = new javax.swing.JButton();
        pnlButtons1 = new javax.swing.JPanel();
        cmdView = new javax.swing.JButton();
        cmdEdit = new javax.swing.JButton();
        cmdUse = new javax.swing.JButton();
        pnlButtons2 = new javax.swing.JPanel();
        cmdClose = new javax.swing.JButton();
        lblActive = new javax.swing.JLabel();
        txtActive = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Localization.Main.getText("gui.menu.logging.manage")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lstLoggingConfigurations.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstLoggingConfigurationsMouseClicked(evt);
            }
        });
        scrLogging.setViewportView(lstLoggingConfigurations);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(scrLogging, gridBagConstraints);

        cmdNew.setText("cmdNew");
        cmdNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdNewActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdNew);

        cmdRename.setText("cmdRename");
        cmdRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRenameActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdRename);

        cmdDelete.setText("cmdDelete");
        cmdDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDeleteActionPerformed(evt);
            }
        });
        pnlButtons.add(cmdDelete);
        cmdDelete.getAccessibleContext().setAccessibleName("null");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(pnlButtons, gridBagConstraints);

        cmdView.setText("cmdView");
        cmdView.setToolTipText("");
        cmdView.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdViewActionPerformed(evt);
            }
        });
        pnlButtons1.add(cmdView);
        cmdView.getAccessibleContext().setAccessibleParent(pnlButtons1);

        cmdEdit.setText("cmdEdit");
        cmdEdit.setToolTipText("");
        cmdEdit.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cmdEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdEditActionPerformed(evt);
            }
        });
        pnlButtons1.add(cmdEdit);

        cmdUse.setText("cmdUse");
        cmdUse.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cmdUse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdUseActionPerformed(evt);
            }
        });
        pnlButtons1.add(cmdUse);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(pnlButtons1, gridBagConstraints);

        cmdClose.setText("cmdClose");
        cmdClose.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cmdClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCloseActionPerformed(evt);
            }
        });
        pnlButtons2.add(cmdClose);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(pnlButtons2, gridBagConstraints);

        lblActive.setText("lblActive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        getContentPane().add(lblActive, gridBagConstraints);

        txtActive.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        txtActive.setText("txtActive");
        txtActive.setToolTipText("");
        txtActive.setBorder(null);
        txtActive.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        getContentPane().add(txtActive, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void cmdRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRenameActionPerformed
    int index = lstLoggingConfigurations.getSelectedIndex();
    if (index < 0) {
      String msg = Localization.Main.getText("gui.logging.rename.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    renameLoggingConfiguration(index);
  }//GEN-LAST:event_cmdRenameActionPerformed

  private void cmdDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteActionPerformed
    deleteLoggingConfiguration(lstLoggingConfigurations.getSelectedIndices());
  }//GEN-LAST:event_cmdDeleteActionPerformed

  private void cmdNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewActionPerformed
    newLoggingConfiguration(lstLoggingConfigurations.getSelectedIndices());
  }//GEN-LAST:event_cmdNewActionPerformed

    private void cmdCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCloseActionPerformed
      this.setVisible(false);
    }//GEN-LAST:event_cmdCloseActionPerformed

    private void cmdViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdViewActionPerformed
      viewLoggingConfiguration(lstLoggingConfigurations.getSelectedIndices());
    }//GEN-LAST:event_cmdViewActionPerformed

    private void cmdEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdEditActionPerformed
      editLoggingConfiguration(lstLoggingConfigurations.getSelectedIndices());
    }//GEN-LAST:event_cmdEditActionPerformed

    private void lstLoggingConfigurationsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstLoggingConfigurationsMouseClicked
      checkActiveButtons();   // TODO add your handling code here:
    }//GEN-LAST:event_lstLoggingConfigurationsMouseClicked

    private void cmdUseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUseActionPerformed
      useLoggingConfiguration(lstLoggingConfigurations.getSelectedIndices());
    }//GEN-LAST:event_cmdUseActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        ProfileManagerDialog dialog = new ProfileManagerDialog(new javax.swing.JFrame(), true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdClose;
    private javax.swing.JButton cmdDelete;
    private javax.swing.JButton cmdEdit;
    private javax.swing.JButton cmdNew;
    private javax.swing.JButton cmdRename;
    private javax.swing.JButton cmdUse;
    private javax.swing.JButton cmdView;
    private javax.swing.JLabel lblActive;
    private javax.swing.JList lstLoggingConfigurations;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlButtons1;
    private javax.swing.JPanel pnlButtons2;
    private javax.swing.JScrollPane scrLogging;
    private javax.swing.JTextField txtActive;
    // End of variables declaration//GEN-END:variables

}
