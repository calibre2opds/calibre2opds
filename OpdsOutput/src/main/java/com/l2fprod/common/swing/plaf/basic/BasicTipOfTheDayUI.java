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

import com.l2fprod.common.swing.ButtonAreaLayout;
import com.l2fprod.common.swing.JTipOfTheDay;
import com.l2fprod.common.swing.LookAndFeelTweaks;
import com.l2fprod.common.swing.JTipOfTheDay.ShowOnStartupChoice;
import com.l2fprod.common.swing.TipModel.Tip;
import com.l2fprod.common.swing.plaf.TipOfTheDayUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;

public class BasicTipOfTheDayUI extends TipOfTheDayUI {

  public static ComponentUI createUI(JComponent c) {
    return new BasicTipOfTheDayUI((JTipOfTheDay)c);
  }

  protected JTipOfTheDay tipPane;
  protected JPanel tipArea;
  protected Component currentTipComponent;

  protected Font tipFont;
  protected PropertyChangeListener changeListener;

  public BasicTipOfTheDayUI(JTipOfTheDay tipPane) {
    this.tipPane = tipPane;
  }

  public JDialog createDialog(Component parentComponent,
    final ShowOnStartupChoice choice) {
    return createDialog(parentComponent, choice, true);
  }

  protected JDialog createDialog(Component parentComponent,
    final ShowOnStartupChoice choice, boolean showPreviousButton) {
    String title = UIManager.getString("TipOfTheDay.dialogTitle");

    final JDialog dialog;

    Window window;
    if (parentComponent == null) {
      window = JOptionPane.getRootFrame();
    } else {
      window = (parentComponent instanceof Window)?(Window)parentComponent
        :SwingUtilities.getWindowAncestor(parentComponent);
    }

    if (window instanceof Frame) {
      dialog = new JDialog((Frame)window, title, true);
    } else {
      dialog = new JDialog((Dialog)window, title, true);
    }

    dialog.getContentPane().setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().add("Center", tipPane);
    ((JComponent)dialog.getContentPane()).setBorder(BorderFactory
      .createEmptyBorder(10, 10, 10, 10));

    final JCheckBox showOnStartupBox;

    // tip controls
    JPanel controls = new JPanel(new BorderLayout());
    dialog.getContentPane().add("South", controls);

    if (choice != null) {
      showOnStartupBox = new JCheckBox(UIManager
        .getString("TipOfTheDay.showOnStartupText"), choice
        .isShowingOnStartup());
      controls.add("Center", showOnStartupBox);
    } else {
      showOnStartupBox = null;
    }

    JPanel buttons = new JPanel(new ButtonAreaLayout(9));
    controls.add("East", buttons);

    if (showPreviousButton) {
      JButton previousTipButton = new JButton(UIManager
        .getString("TipOfTheDay.previousTipText"));
      buttons.add(previousTipButton);
      previousTipButton.addActionListener(getActionMap().get("previousTip"));
    }

    JButton nextTipButton = new JButton(UIManager
      .getString("TipOfTheDay.nextTipText"));
    buttons.add(nextTipButton);
    nextTipButton.addActionListener(getActionMap().get("nextTip"));

    JButton closeButton = new JButton(UIManager
      .getString("TipOfTheDay.closeText"));
    buttons.add(closeButton);

    final ActionListener saveChoice = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (choice != null) {
          choice.setShowingOnStartup(showOnStartupBox.isSelected());
        }
        dialog.setVisible(false);
      }
    };

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        saveChoice.actionPerformed(null);
      }
    });
    dialog.getRootPane().setDefaultButton(closeButton);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        saveChoice.actionPerformed(null);
      }
    });

    ((JComponent)dialog.getContentPane()).registerKeyboardAction(saveChoice,
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);

    dialog.pack();
    dialog.setLocationRelativeTo(parentComponent);

    return dialog;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    installDefaults();
    installKeyboardActions();
    installComponents();
    installListeners();

    showCurrentTip();
  }

  protected void installKeyboardActions() {
    ActionMap map = getActionMap();
    if (map != null) {
      SwingUtilities.replaceUIActionMap(tipPane, map);
    }
  }

  ActionMap getActionMap() {
    ActionMap map = new ActionMapUIResource();
    map.put("previousTip", new PreviousTipAction());
    map.put("nextTip", new NextTipAction());
    return map;
  }

  protected void installListeners() {
    changeListener = createChangeListener();
    tipPane.addPropertyChangeListener(changeListener);
  }

  protected PropertyChangeListener createChangeListener() {
    return new ChangeListener();
  }

  protected void installDefaults() {
    LookAndFeel.installColorsAndFont(tipPane, "TipOfTheDay.background",
      "TipOfTheDay.foreground", "TipOfTheDay.font");
    LookAndFeel.installBorder(tipPane, "TipOfTheDay.border");
    tipFont = UIManager.getFont("TipOfTheDay.tipFont");
    tipPane.setOpaque(true);
  }

  protected void installComponents() {
    tipPane.setLayout(new BorderLayout());

    // tip icon
    JLabel tipIcon = new JLabel(UIManager
      .getString("TipOfTheDay.didYouKnowText"));
    tipIcon.setIcon(UIManager.getIcon("TipOfTheDay.icon"));
    tipIcon.setBorder(BorderFactory.createEmptyBorder(22, 15, 22, 15));
    tipPane.add("North", tipIcon);

    // tip area
    tipArea = new JPanel(new BorderLayout(2, 2));
    tipArea.setOpaque(false);
    tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tipPane.add("Center", tipArea);
  }

  public Dimension getPreferredSize(JComponent c) {
    return new Dimension(420, 175);
  }

  protected void showCurrentTip() {
    if (currentTipComponent != null) {
      tipArea.remove(currentTipComponent);
    }

    int currentTip = tipPane.getCurrentTip();
    if (currentTip == -1) {
      JLabel label = new JLabel();
      label.setOpaque(true);
      label.setBackground(UIManager.getColor("TextArea.background"));
      currentTipComponent = label;
      tipArea.add("Center", currentTipComponent);
      return;
    }

    // tip does not fall in current tip range
    if (tipPane.getModel().getTipCount() == 0
      || (currentTip < 0 && currentTip >= tipPane.getModel().getTipCount())) {
      currentTipComponent = new JLabel();
    } else {    
      Tip tip = tipPane.getModel().getTipAt(currentTip);

      Object tipObject = tip.getTip();
      if (tipObject instanceof Component) {
        currentTipComponent = (Component)tipObject;
      } else if (tipObject instanceof Icon) {
        currentTipComponent = new JLabel((Icon)tipObject);
      } else {
        JScrollPane tipScroll = new JScrollPane();
        tipScroll.setBorder(null);
        tipScroll.setOpaque(false);
        tipScroll.getViewport().setOpaque(false);
        tipScroll.setBorder(null);

        String text = tipObject == null?"":tipObject.toString();

        if (text.toLowerCase().startsWith("<html>")) {
          JEditorPane editor = new JEditorPane("text/html", text);
          LookAndFeelTweaks.htmlize(editor, tipPane.getFont());
          editor.setEditable(false);
          editor.setBorder(null);
          editor.setMargin(null);
          editor.setOpaque(false);
          tipScroll.getViewport().setView(editor);
        } else {
          JTextArea area = new JTextArea(text);
          area.setFont(tipPane.getFont());
          area.setEditable(false);
          area.setLineWrap(true);
          area.setWrapStyleWord(true);
          area.setBorder(null);
          area.setMargin(null);
          area.setOpaque(false);
          tipScroll.getViewport().setView(area);
        }

        currentTipComponent = tipScroll;
      }
    }
    
    tipArea.add("Center", currentTipComponent);
    tipArea.revalidate();
    tipArea.repaint();
  }

  public void uninstallUI(JComponent c) {
    uninstallListeners();
    uninstallComponents();
    uninstallDefaults();
    super.uninstallUI(c);
  }

  protected void uninstallListeners() {
    tipPane.removePropertyChangeListener(changeListener);
  }

  protected void uninstallComponents() {}

  protected void uninstallDefaults() {}

  class ChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      if (JTipOfTheDay.CURRENT_TIP_CHANGED_KEY.equals(evt.getPropertyName())) {
        showCurrentTip();
      }
    }
  }

  class PreviousTipAction extends AbstractAction {
    public PreviousTipAction() {
      super("previousTip");
    }
    public void actionPerformed(ActionEvent e) {
      tipPane.previousTip();
    }
    public boolean isEnabled() {
      return tipPane.isEnabled();
    }
  }

  class NextTipAction extends AbstractAction {
    public NextTipAction() {
      super("nextTip");
    }
    public void actionPerformed(ActionEvent e) {
      tipPane.nextTip();
    }
    public boolean isEnabled() {
      return tipPane.isEnabled();
    }
  }

}
