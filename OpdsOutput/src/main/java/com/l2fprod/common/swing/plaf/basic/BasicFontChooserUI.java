/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.l2fprod.common.swing.plaf.basic;

import com.l2fprod.common.swing.JFontChooser;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.swing.PercentLayout;
import com.l2fprod.common.swing.plaf.FontChooserUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * BasicFontChooserUI. <br>
 */
public class BasicFontChooserUI extends FontChooserUI {

  public static ComponentUI createUI(JComponent component) {
    return new BasicFontChooserUI();
  }

  private JFontChooser chooser;

  private JPanel fontPanel;
  private JTextField fontField;
  private JList fontList;

  private JPanel fontSizePanel;
  private JTextField fontSizeField;
  private JList fontSizeList;

  private JCheckBox boldCheck;
  private JCheckBox italicCheck;

  private JTextArea previewPanel;

  private PropertyChangeListener propertyListener;

  public void installUI(JComponent c) {
    super.installUI(c);

    chooser = (JFontChooser) c;

    installComponents();
    installListeners();
  }

  protected void installComponents() {
    JLabel label;

    ResourceBundle bundle = ResourceBundle.getBundle(FontChooserUI.class.getName() + "RB");

    fontPanel = new JPanel(new PercentLayout(PercentLayout.VERTICAL, 2));
    fontPanel.add(label = new JLabel(bundle.getString("FontChooserUI.fontLabel")));
    fontPanel.add(fontField = new JTextField(25));
    fontField.setEditable(false);
    fontPanel.add(new JScrollPane(fontList = new JList()), "*");
    label.setLabelFor(fontList);
    label.setDisplayedMnemonic(bundle.getString("FontChooserUI.fontLabel.mnemonic").charAt(0));
    fontList.setVisibleRowCount(7);
    fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    String[] fontFamilies = chooser.getModel().getFontFamilies(null);
    fontList.setListData(fontFamilies);

    fontSizePanel = new JPanel(new PercentLayout(PercentLayout.VERTICAL, 2));

    fontSizePanel.add(label = new JLabel(bundle.getString("FontChooserUI.styleLabel")));
    fontSizePanel.add(boldCheck = new JCheckBox(bundle.getString("FontChooserUI.style.bold")));
    fontSizePanel.add(italicCheck = new JCheckBox(bundle.getString("FontChooserUI.style.italic")));
    boldCheck.setMnemonic(bundle.getString("FontChooserUI.style.bold.mnemonic").charAt(0));
    italicCheck.setMnemonic(bundle.getString("FontChooserUI.style.italic.mnemonic").charAt(0));

    fontSizePanel.add(label = new JLabel(bundle.getString("FontChooserUI.sizeLabel")));

    label.setDisplayedMnemonic(bundle.getString("FontChooserUI.sizeLabel.mnemonic").charAt(0));

    fontSizePanel.add(fontSizeField = new JTextField());
    label.setLabelFor(fontSizeField);
    fontSizePanel.add(new JScrollPane(fontSizeList = new JList()), "*");

    int[] defaultFontSizes = chooser.getModel().getDefaultSizes();
    String[] sizes = new String[defaultFontSizes.length];
    for (int i = 0, c = sizes.length; i < c; i++) {
      sizes[i] = String.valueOf(defaultFontSizes[i]);
    }
    fontSizeList.setListData(sizes);
    fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontSizeList.setVisibleRowCount(2);

    chooser.setLayout(LookAndFeelTweaks.createBorderLayout());
    JPanel panel = new JPanel();
    panel.setLayout(LookAndFeelTweaks.createHorizontalPercentLayout());
    panel.add(fontPanel, "*");
    panel.add(fontSizePanel);
    chooser.add("Center", panel);

    previewPanel = new JTextArea();
    previewPanel.setPreferredSize(new Dimension(100, 40));
    previewPanel.setText(chooser.getModel().getPreviewMessage(null));
    JScrollPane scroll = new JScrollPane(previewPanel);
    chooser.add("South", scroll);
  }

  protected void installListeners() {
    SelectedFontUpdater listener = new SelectedFontUpdater();
    fontList.addListSelectionListener(listener);
    fontSizeList.addListSelectionListener(listener);
    fontSizeField.getDocument().addDocumentListener(listener);
    boldCheck.addActionListener(listener);
    italicCheck.addActionListener(listener);

    propertyListener = createPropertyChangeListener();
    chooser.addPropertyChangeListener(JFontChooser.SELECTED_FONT_CHANGED_KEY, propertyListener);
  }

  public void uninstallUI(JComponent c) {
    chooser.remove(fontPanel);
    chooser.remove(fontSizePanel);

    super.uninstallUI(c);
  }

  public void uninstallListeners() {
    chooser.removePropertyChangeListener(propertyListener);
  }

  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateDisplay();
      }
    };
  }

  private void updateDisplay() {
    Font selected = chooser.getSelectedFont();
    if (selected != null) {
      previewPanel.setFont(selected);
      fontList.setSelectedValue(selected.getName(), true);
      fontSizeField.setText(String.valueOf(selected.getSize()));
      fontSizeList.setSelectedValue(String.valueOf(selected.getSize()), true);
      boldCheck.setSelected(selected.isBold());
      italicCheck.setSelected(selected.isItalic());
    }
  }

  private void updateSelectedFont() {
    Font currentFont = chooser.getSelectedFont();
    String fontFamily = currentFont == null ? "SansSerif" : currentFont.getName();
    int fontSize = currentFont == null ? 11 : currentFont.getSize();

    if (fontList.getSelectedIndex() >= 0) {
      fontFamily = (String) fontList.getSelectedValue();
    }

    if (fontSizeField.getText().trim().length() > 0) {
      try {
        fontSize = Integer.parseInt(fontSizeField.getText().trim());
      } catch (Exception e) {
        // ignore the NumberFormatException
      }
    }

    Map attributes = new HashMap();
    attributes.put(TextAttribute.SIZE, new Float(fontSize));
    attributes.put(TextAttribute.FAMILY, fontFamily);
    if (boldCheck.isSelected()) {
      attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    }
    if (italicCheck.isSelected()) {
      attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    }

    Font font = Font.getFont(attributes);
    if (!font.equals(currentFont)) {
      chooser.setSelectedFont(font);
      previewPanel.setFont(font);
    }
  }

  private class SelectedFontUpdater implements ListSelectionListener, DocumentListener, ActionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (fontList == e.getSource() && fontList.getSelectedValue() != null) {
        fontField.setText((String) fontList.getSelectedValue());
      }
      if (fontSizeList == e.getSource() && fontSizeList.getSelectedValue() != null) {
        fontSizeField.setText((String) fontSizeList.getSelectedValue());
      }
      updateSelectedFont();
    }

    public void changedUpdate(DocumentEvent e) {
      updateLater();
    }

    public void insertUpdate(DocumentEvent e) {
      updateLater();
    }

    public void removeUpdate(DocumentEvent e) {
      updateLater();
    }

    public void actionPerformed(ActionEvent e) {
      updateLater();
    }

    void updateLater() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          updateSelectedFont();
        }
      });
    }
  }

}
