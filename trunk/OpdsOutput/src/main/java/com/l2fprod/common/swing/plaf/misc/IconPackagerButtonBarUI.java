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
package com.l2fprod.common.swing.plaf.misc;

import com.l2fprod.common.swing.plaf.ButtonBarButtonUI;
import com.l2fprod.common.swing.plaf.basic.BasicButtonBarUI;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;

/**
 * An implementation for the JButtonBar UI which looks like the one found in
 * <a href="http://www.stardock.com/products/iconpackager/">IconPackager 2.5
 * </a>.
 */
public class IconPackagerButtonBarUI extends BasicButtonBarUI {

  public static ComponentUI createUI(JComponent c) {
    return new IconPackagerButtonBarUI();
  }

  protected void installDefaults() {
    Border b = bar.getBorder();
    if (b == null || b instanceof UIResource) {
      bar.setBorder(
        new BorderUIResource(
          new CompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(2, 2, 2, 2))));
    }

    if (bar.getBackground() == null
      || bar.getBackground() instanceof UIResource) {
      bar.setBackground(new ColorUIResource(128, 128, 128));
      bar.setOpaque(true);
    }
  }

  public void installButtonBarUI(AbstractButton button) {
    button.setUI(new ButtonUI());
    button.setHorizontalTextPosition(JButton.CENTER);
    button.setVerticalTextPosition(JButton.BOTTOM);
  }

  static class ButtonUI extends BasicButtonUI implements ButtonBarButtonUI {
    private static Color selectedBackground = Color.white;
    private static Color selectedBorder = Color.black;

    private static Color selectedForeground = Color.black;
    private static Color unselectedForeground = Color.white;

    public void installUI(JComponent c) {
      super.installUI(c);

      AbstractButton button = (AbstractButton)c;
      button.setOpaque(false);
      button.setRolloverEnabled(true);
      button.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    }

    public void paint(Graphics g, JComponent c) {
      AbstractButton button = (AbstractButton)c;

      if (button.getModel().isSelected()) {
        Color oldColor = g.getColor();
        g.setColor(selectedBackground);
        g.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 5, 5);

        g.setColor(selectedBorder);
        g.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 5, 5);

        g.setColor(oldColor);
      }

      // this is a tweak to get the View with the color we expect it to be. We
      // change directly the color of the button
      if (c.getClientProperty(BasicHTML.propertyKey) != null) {
        ButtonModel model = button.getModel();
        if (model.isEnabled()) {
          if (model.isSelected()) {
            button.setForeground(selectedForeground);
          } else {
            button.setForeground(unselectedForeground);
          }
        } else {
          button.setForeground(unselectedForeground.darker());
        }
      }

      super.paint(g, c);
    }

    protected void paintText(
      Graphics g,
      AbstractButton b,
      Rectangle textRect,
      String text) {
      ButtonModel model = b.getModel();
      FontMetrics fm = g.getFontMetrics();
      int mnemonicIndex = b.getDisplayedMnemonicIndex();

      Color oldColor = g.getColor();

      /* Draw the Text */
      if (model.isEnabled()) {
        /** * paint the text normally */
        if (model.isSelected()) {
          g.setColor(selectedForeground);
        } else {
          g.setColor(unselectedForeground);
        }
      } else {
        g.setColor(unselectedForeground.darker());
      }

      //            
      BasicGraphicsUtils.drawStringUnderlineCharAt(
        g,
        text,
        mnemonicIndex,
        textRect.x + getTextShiftOffset(),
        textRect.y + fm.getAscent() + getTextShiftOffset());
      //
      //      } else {
      //        g.setColor(b.getParent().getBackground().brighter());
      //        BasicGraphicsUtils.drawStringUnderlineCharAt(
      //          g,
      //          text,
      //          mnemonicIndex,
      //          textRect.x,
      //          textRect.y + fm.getAscent());
      //        g.setColor(b.getParent().getBackground().darker());
      //        BasicGraphicsUtils.drawStringUnderlineCharAt(
      //          g,
      //          text,
      //          mnemonicIndex,
      //          textRect.x - 1,
      //          textRect.y + fm.getAscent() - 1);
      //      }
      g.setColor(oldColor);
    }
  }

}
