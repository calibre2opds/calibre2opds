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

import com.l2fprod.common.swing.JTaskPaneGroup;
import com.l2fprod.common.swing.plaf.basic.BasicTaskPaneGroupUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Windows implementation of the WindowsTaskPaneUI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class WindowsTaskPaneGroupUI extends BasicTaskPaneGroupUI {

  public static ComponentUI createUI(JComponent c) {
    return new WindowsTaskPaneGroupUI();
  }

  protected Border createPaneBorder() {
    return new XPPaneBorder();
  }

  /**
   * Overriden to paint the background of the component but keeping the rounded
   * corners.
   */
  public void update(Graphics g, JComponent c) {
    if (c.isOpaque()) {
      g.setColor(c.getParent().getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
      g.setColor(c.getBackground());
      g.fillRect(0, ROUND_HEIGHT, c.getWidth(), c.getHeight() - ROUND_HEIGHT);
    }
    paint(g, c);
  }

  /**
   * The border of the taskpane group paints the "text", the "icon", the
   * "expanded" status and the "special" type.
   */
  class XPPaneBorder extends PaneBorder {

    protected void paintTitleBackground(JTaskPaneGroup group, Graphics g) {
      if (group.isSpecial()) {
        g.setColor(specialTitleBackground);
        g.fillRoundRect(0, 0, group.getWidth(), ROUND_HEIGHT * 2, ROUND_HEIGHT, ROUND_HEIGHT);
        g.fillRect(0, ROUND_HEIGHT, group.getWidth(), TITLE_HEIGHT - ROUND_HEIGHT);
      } else {
        Paint oldPaint = ((Graphics2D) g).getPaint();
        GradientPaint gradient = new GradientPaint(0f, group.getWidth() / 2,
            group.getComponentOrientation().isLeftToRight() ? titleBackgroundGradientStart : titleBackgroundGradientEnd, group.getWidth(),
            TITLE_HEIGHT, group.getComponentOrientation().isLeftToRight() ? titleBackgroundGradientEnd : titleBackgroundGradientStart);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ((Graphics2D) g).setPaint(gradient);
        g.fillRoundRect(0, 0, group.getWidth(), ROUND_HEIGHT * 2, ROUND_HEIGHT, ROUND_HEIGHT);
        g.fillRect(0, ROUND_HEIGHT, group.getWidth(), TITLE_HEIGHT - ROUND_HEIGHT);
        ((Graphics2D) g).setPaint(oldPaint);
      }
    }

    protected void paintExpandedControls(JTaskPaneGroup group, Graphics g, int x, int y, int width, int height) {
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      paintOvalAroundControls(group, g, x, y, width, height);
      g.setColor(getPaintColor(group));
      paintChevronControls(group, g, x, y, width, height);

      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected boolean isMouseOverBorder() {
      return true;
    }

  }

}
