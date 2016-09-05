/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProfileManagerDialog.java
 *
 * Created on 10 juin 2010, 14:26:23
 */

package com.gmail.dpierron.calibre.gui;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;


/**
 * @author David Pierron
 */
public class ProfileManagerDialog extends javax.swing.JDialog {
  private final Logger logger = LogManager.getLogger(ProfileManagerDialog.class);
  private guiField[] guiFields;

  /**
   * Creates new form ProfileManagerDialog
   */
  public ProfileManagerDialog(java.awt.Frame parent, boolean modal) {
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

        new guiField(cmdNew, null, "gui.profile.new"),
        new guiField(cmdRename, null, "gui.profile.rename"),
        new guiField(cmdDelete, null, "gui.profile.delete"),
        new guiField(cmdClose, null, "gui.profile.close")
    };
  }

  /**
   * Apply localization to this dialog
   */
  private void translateTexts() {
    // Do translations that are handled by guiFields table
    for (guiField f : guiFields){
      f.translateTexts();
    }
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

  private void loadProfiles() {
    DefaultListModel listOfProfiles = new DefaultListModel();
    for (String profile : ConfigurationManager.getExistingConfigurations()) {
      if ("default".equalsIgnoreCase(profile))
        continue;
      listOfProfiles.addElement(profile);
    }
    lstProfiles.setModel(listOfProfiles);
  }

  private void renameProfile(int index) {
    if (index < 0 || index >= lstProfiles.getModel().getSize())
      return;

    String profile = (String) lstProfiles.getModel().getElementAt(index);
    String newProfile = JOptionPane.showInputDialog(Localization.Main.getText("gui.profile.rename.msg", profile), profile);
    if ("default".equalsIgnoreCase(newProfile))
      return;
    if (Helper.isNotNullOrEmpty(newProfile) && !newProfile.equals(profile)) {
      File profileFile =
          new File(ConfigurationManager.getConfigurationDirectory(), profile + ConfigurationManager.PROFILES_SUFFIX);
      if (profileFile.exists()) {
        profileFile.renameTo(new File(ConfigurationManager.getConfigurationDirectory(), newProfile + ConfigurationManager.PROFILES_SUFFIX));
        // #c2o-10:  If current profile is the one renamed then need to refresh main screen
        if (ConfigurationManager.getCurrentProfileName().equals(profile)) {
          ConfigurationManager.setCurrentProfileName(newProfile);
        }
        loadProfiles();
      }
    }
  }

  private void deleteProfiles(int[] indices) {
    if (indices == null || indices.length == 0)
      return;

    for (int index : indices) {
      String profile = (String) lstProfiles.getModel().getElementAt(index);
      int result = JOptionPane
          .showConfirmDialog(this, Localization.Main.getText("gui.profile.delete.msg", profile), null, JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE);
      if (result != JOptionPane.CANCEL_OPTION) {
        File profileFile =
            new File(ConfigurationManager.getConfigurationDirectory(), profile + ConfigurationManager.PROFILES_SUFFIX);
        if (profileFile.exists()) {
          profileFile.delete();
        }
      }
    }
    loadProfiles();
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

        scrProfiles = new javax.swing.JScrollPane();
        lstProfiles = new javax.swing.JList();
        loadProfiles();
        pnlButtons = new javax.swing.JPanel();
        cmdNew = new javax.swing.JButton();
        cmdRename = new javax.swing.JButton();
        cmdDelete = new javax.swing.JButton();
        pnlButtons1 = new javax.swing.JPanel();
        cmdClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Localization.Main.getText("gui.menu.profiles.manage")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        scrProfiles.setViewportView(lstProfiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(scrProfiles, gridBagConstraints);

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
        cmdDelete.getAccessibleContext().setAccessibleName(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(pnlButtons, gridBagConstraints);

        cmdClose.setText("cmdClose");
        cmdClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCloseActionPerformed(evt);
            }
        });
        pnlButtons1.add(cmdClose);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(pnlButtons1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void cmdRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRenameActionPerformed
    int index = lstProfiles.getSelectedIndex();
    if (index < 0) {
      String msg = Localization.Main.getText("gui.profile.rename.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    renameProfile(index);
  }//GEN-LAST:event_cmdRenameActionPerformed

  private void cmdDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteActionPerformed
    int indices[] = lstProfiles.getSelectedIndices();
    if (indices.length < 1) {
      String msg = Localization.Main.getText("gui.profile.delete.select");
      JOptionPane.showMessageDialog(this, msg, "", JOptionPane.WARNING_MESSAGE);
      return;
    }
    deleteProfiles(indices);
  }//GEN-LAST:event_cmdDeleteActionPerformed

  private void cmdNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewActionPerformed
    String newProfileName = JOptionPane.showInputDialog(Localization.Main.getText("gui.profile.new.msg"));
    if ("default".equalsIgnoreCase(newProfileName))
      return;
    ConfigurationManager.copyCurrentProfile(newProfileName);
    loadProfiles();
  }//GEN-LAST:event_cmdNewActionPerformed

    private void cmdCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCloseActionPerformed
      this.setVisible(false);
    }//GEN-LAST:event_cmdCloseActionPerformed

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
    private javax.swing.JButton cmdNew;
    private javax.swing.JButton cmdRename;
    private javax.swing.JList lstProfiles;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlButtons1;
    private javax.swing.JScrollPane scrProfiles;
    // End of variables declaration//GEN-END:variables

}
