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
package com.l2fprod.common.swing.plaf.blue;

import com.l2fprod.common.swing.plaf.ButtonBarButtonUI;
import com.l2fprod.common.swing.plaf.basic.BasicButtonBarUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * BlueishButtonBarUI. <br>
 */
public class BlueishButtonBarUI extends BasicButtonBarUI {

  public static ComponentUI createUI(JComponent c) {
    return new BlueishButtonBarUI();
  }

  protected void installDefaults() {
    Border b = bar.getBorder();
    if (b == null || b instanceof UIResource) {
      bar.setBorder(new BorderUIResource(new CompoundBorder(BorderFactory.createLineBorder(UIManager.getColor("controlDkShadow")),
          BorderFactory.createEmptyBorder(1, 1, 1, 1))));
    }

    Color color = bar.getBackground();
    if (color == null || color instanceof ColorUIResource) {
      bar.setOpaque(true);
      bar.setBackground(new ColorUIResource(Color.white));
    }
  }

  public void installButtonBarUI(AbstractButton button) {
    button.setUI(new BlueishButtonBarButtonUI());
    button.setHorizontalTextPosition(JButton.CENTER);
    button.setVerticalTextPosition(JButton.BOTTOM);
    button.setOpaque(false);
  }

  static class BlueishButtonBarButtonUI extends BlueishButtonUI implements ButtonBarButtonUI {}

}
