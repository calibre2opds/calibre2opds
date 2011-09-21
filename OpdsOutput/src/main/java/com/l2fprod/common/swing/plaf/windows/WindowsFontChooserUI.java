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
package com.l2fprod.common.swing.plaf.windows;

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
 * Windows implementation of the JFontChooser pluggable UI.
 */
public class WindowsFontChooserUI extends FontChooserUI {

  public static ComponentUI createUI(JComponent component) {
    return new WindowsFontChooserUI();
  }

  private JFontChooser chooser;

  private JPanel fontPanel;
  private JTextField fontField;
  private JList fontList;

  private JTextField fontEffectField;
  private JList fontEffectList;

  private JPanel fontSizePanel;
  private JTextField fontSizeField;
  private JList fontSizeList;

  private JTextArea previewPanel;
  private JComboBox charSetCombo;

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

    // FIRST PANEL with Font list
    fontPanel = new JPanel(new PercentLayout(PercentLayout.VERTICAL, 2));
    fontPanel.add(label = new JLabel(bundle.getString("FontChooserUI.fontLabel")));
    fontPanel.add(fontField = new JTextField(25));
    fontField.setEditable(false);
    fontPanel.add(new JScrollPane(fontList = new JList()), "*");
    label.setLabelFor(fontList);
    label.setDisplayedMnemonic(bundle.getString("FontChooserUI.fontLabel.mnemonic").charAt(0));
    fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    String[] fontFamilies = chooser.getModel().getFontFamilies(null);
    fontList.setListData(fontFamilies);

    // SECOND PANEL with Bold, Italic, Charset
    JPanel fontEffectPanel = new JPanel(new PercentLayout(PercentLayout.VERTICAL, 2));
    fontEffectPanel.add(label = new JLabel(bundle.getString("FontChooserUI.styleLabel")));
    fontEffectPanel.add(fontEffectField = new JTextField(10));
    fontEffectField.setEditable(false);
    fontEffectPanel.add(new JScrollPane(fontEffectList = new JList()), "*");
    label.setLabelFor(fontEffectList);
    label.setDisplayedMnemonic(bundle.getString("FontChooserUI.styleLabel.mnemonic").charAt(0));
    fontEffectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    FontStyle[] fontStyles = new FontStyle[]{new FontStyle(Font.PLAIN, bundle.getString("FontChooserUI.style.plain")),
        new FontStyle(Font.BOLD, bundle.getString("FontChooserUI.style.bold")),
        new FontStyle(Font.ITALIC, bundle.getString("FontChooserUI.style.italic")),
        new FontStyle(Font.BOLD | Font.ITALIC, bundle.getString("FontChooserUI.style.bolditalic")),};
    fontEffectList.setListData(fontStyles);

    // The SIZE PANEL
    fontSizePanel = new JPanel(new PercentLayout(PercentLayout.VERTICAL, 2));
    fontSizePanel.add(label = new JLabel(bundle.getString("FontChooserUI.sizeLabel")));

    label.setDisplayedMnemonic(bundle.getString("FontChooserUI.sizeLabel.mnemonic").charAt(0));

    fontSizePanel.add(fontSizeField = new JTextField(5));
    label.setLabelFor(fontSizeField);
    fontSizePanel.add(new JScrollPane(fontSizeList = new JList()), "*");

    int[] defaultFontSizes = chooser.getModel().getDefaultSizes();
    String[] sizes = new String[defaultFontSizes.length];
    for (int i = 0, c = sizes.length; i < c; i++) {
      sizes[i] = String.valueOf(defaultFontSizes[i]);
    }
    fontSizeList.setPrototypeCellValue("012345");
    fontSizeList.setListData(sizes);
    fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontSizeList.setVisibleRowCount(2);

    chooser.setLayout(LookAndFeelTweaks.createBorderLayout());
    JPanel panel = new JPanel();
    panel.setLayout(LookAndFeelTweaks.createHorizontalPercentLayout());
    panel.add(fontPanel, "*");
    panel.add(fontEffectPanel);
    panel.add(fontSizePanel);

    previewPanel = new JTextArea();
    previewPanel.setText(chooser.getModel().getPreviewMessage(null));
    JScrollPane scroll = new JScrollPane(previewPanel);

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setBorder(null);
    split.setTopComponent(panel);
    split.setBottomComponent(scroll);
    split.setDividerLocation(0.5);
    split.setOneTouchExpandable(true);
    chooser.add("Center", split);

    // allow the split pane to completely hide the top panel
    panel.setMinimumSize(new Dimension(0, 0));

    JPanel charSetPanel = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 2));
    label = new JLabel("CHARSET");
    label.setHorizontalAlignment(JLabel.RIGHT);
    charSetPanel.add(label, "*");

    charSetCombo = new JComboBox(chooser.getModel().getCharSets());
    charSetPanel.add(charSetCombo);
    // PENDING(fred) implement charset
    // chooser.add("South", charSetPanel);
  }

  protected void installListeners() {
    SelectedFontUpdater listener = new SelectedFontUpdater();
    fontList.addListSelectionListener(listener);
    fontEffectList.addListSelectionListener(listener);
    fontSizeList.addListSelectionListener(listener);
    fontSizeField.getDocument().addDocumentListener(listener);

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
      /** PENDING(fred) implement charset
       String charset = (String)charSetCombo.getSelectedItem();
       String text = chooser.getModel().getPreviewMessage(charset);
       boolean canDisplay = selected.canDisplayUpTo(text) == -1;
       if (canDisplay) {
       previewPanel.setText(text);
       } else {
       previewPanel.setText("Charset not supported");
       }
       **/
      previewPanel.setFont(selected);
      fontList.setSelectedValue(selected.getName(), true);
      fontSizeField.setText(String.valueOf(selected.getSize()));
      fontSizeList.setSelectedValue(String.valueOf(selected.getSize()), true);

      FontStyle style = new FontStyle(selected.getStyle(), null);
      fontEffectList.setSelectedValue(style, true);
      style = (FontStyle) fontEffectList.getSelectedValue();
      fontEffectField.setText(style.toString());
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

    FontStyle style = (FontStyle) fontEffectList.getSelectedValue();
    if (style != null) {
      if (style.isBold()) {
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
      }
      if (style.isItalic()) {
        attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
      }
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

  static private class FontStyle {

    String display;
    int value;

    public FontStyle(int value, String display) {
      this.value = value;
      this.display = display;
    }

    public String toString() {
      return display;
    }

    public boolean isBold() {
      return (value & Font.BOLD) != 0;
    }

    public boolean isItalic() {
      return (value & Font.ITALIC) != 0;
    }

    public int hashCode() {
      return value;
    }

    public boolean equals(Object obj) {
      return (obj instanceof FontStyle) && (((FontStyle) obj).value == value);
    }

  }

}