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

import com.l2fprod.common.swing.JTipOfTheDay;
import com.l2fprod.common.swing.JTipOfTheDay.ShowOnStartupChoice;
import com.l2fprod.common.swing.plaf.basic.BasicTipOfTheDayUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class WindowsTipOfTheDayUI extends BasicTipOfTheDayUI {

  public static ComponentUI createUI(JComponent c) {
    return new WindowsTipOfTheDayUI((JTipOfTheDay) c);
  }

  public WindowsTipOfTheDayUI(JTipOfTheDay tipPane) {
    super(tipPane);
  }

  public JDialog createDialog(Component parentComponent, final ShowOnStartupChoice choice) {
    return createDialog(parentComponent, choice, false);
  }

  protected void installComponents() {
    tipPane.setLayout(new BorderLayout());

    // tip icon
    JLabel tipIcon = new JLabel();
    tipIcon.setPreferredSize(new Dimension(60, 100));
    tipIcon.setIcon(UIManager.getIcon("TipOfTheDay.icon"));
    tipIcon.setHorizontalAlignment(JLabel.CENTER);
    tipIcon.setVerticalAlignment(JLabel.TOP);
    tipIcon.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
    tipPane.add("West", tipIcon);

    // tip area
    JPanel rightPane = new JPanel(new BorderLayout());
    JLabel didYouKnow = new JLabel(UIManager.getString("TipOfTheDay.didYouKnowText"));
    didYouKnow.setPreferredSize(new Dimension(50, 32));
    didYouKnow.setOpaque(true);
    didYouKnow.setBackground(UIManager.getColor("TextArea.background"));
    didYouKnow.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, tipPane.getBackground()),
        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    didYouKnow.setFont(tipPane.getFont().deriveFont(Font.BOLD, 15));
    rightPane.add("North", didYouKnow);

    tipArea = new JPanel(new BorderLayout());
    tipArea.setOpaque(true);
    tipArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tipArea.setBackground(UIManager.getColor("TextArea.background"));
    rightPane.add("Center", tipArea);

    tipPane.add("Center", rightPane);
  }

  public static class TipAreaBorder implements Border {
    public Insets getBorderInsets(Component c) {
      return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
      return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      g.setColor(UIManager.getColor("TipOfTheDay.background"));
      g.drawLine(x, y, x + width - 1, y);
      g.drawLine(x, y, x, y + height - 1);

      g.setColor(Color.black);
      g.drawLine(x + 1, y + 1, x + width - 3, y + 1);
      g.drawLine(x + 1, y + 1, x + 1, y + height - 3);

      g.setColor(Color.white);
      g.drawLine(x, y + height - 1, x + width, y + height - 1);
      g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
    }
  }

}
