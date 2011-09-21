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
 * Windows Classic (NT/2000) implementation of the
 * <code>JTaskPaneGroup</code> UI.
 *
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class WindowsClassicTaskPaneGroupUI extends BasicTaskPaneGroupUI {

  public static ComponentUI createUI(JComponent c) {
    return new WindowsClassicTaskPaneGroupUI();
  }

  protected void installDefaults() {
    super.installDefaults();
    group.setOpaque(false);
  }

  protected Border createPaneBorder() {
    return new ClassicPaneBorder();
  }

  /**
   * The border of the taskpane group paints the "text", the "icon", the
   * "expanded" status and the "special" type.
   */
  class ClassicPaneBorder extends PaneBorder {

    protected void paintExpandedControls(JTaskPaneGroup group, Graphics g, int x, int y, int width, int height) {
      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      paintRectAroundControls(group, g, x, y, width, height, Color.white, Color.gray);
      g.setColor(getPaintColor(group));
      paintChevronControls(group, g, x, y, width, height);

      ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
  }

}
