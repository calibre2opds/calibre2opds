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
package com.l2fprod.common.swing.plaf;

import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons;
import com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons;
import com.l2fprod.common.util.OS;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Addon for <code>JTaskPane</code>. <br>
 */
public class JTaskPaneAddon extends AbstractComponentAddon {

  public JTaskPaneAddon() {
    super("JTaskPane");
  }

  protected void addBasicDefaults(LookAndFeelAddons addon, List defaults) {
    super.addBasicDefaults(addon, defaults);
    defaults.addAll(Arrays.asList(new Object[]{JTaskPane.UI_CLASS_ID,
        "com.l2fprod.common.swing.plaf.basic.BasicTaskPaneUI",
        "TaskPane.useGradient",
        Boolean.FALSE,
        "TaskPane.background",
        UIManager.getColor("Desktop.background")}));
  }

  protected void addMetalDefaults(LookAndFeelAddons addon, List defaults) {
    super.addMetalDefaults(addon, defaults);
    defaults.addAll(Arrays.asList(new Object[]{"TaskPane.background", MetalLookAndFeel.getDesktopColor()}));
  }

  protected void addWindowsDefaults(LookAndFeelAddons addon, List defaults) {
    super.addWindowsDefaults(addon, defaults);
    if (addon instanceof WindowsClassicLookAndFeelAddons) {
      defaults.addAll(Arrays.asList(new Object[]{"TaskPane.background", UIManager.getColor("List.background")}));
    } else if (addon instanceof WindowsLookAndFeelAddons) {
      String xpStyle = OS.getWindowsVisualStyle();
      ColorUIResource background;
      ColorUIResource backgroundGradientStart;
      ColorUIResource backgroundGradientEnd;

      if (WindowsLookAndFeelAddons.HOMESTEAD_VISUAL_STYLE.equalsIgnoreCase(xpStyle)) {
        background = new ColorUIResource(201, 215, 170);
        backgroundGradientStart = new ColorUIResource(204, 217, 173);
        backgroundGradientEnd = new ColorUIResource(165, 189, 132);
      } else if (WindowsLookAndFeelAddons.SILVER_VISUAL_STYLE.equalsIgnoreCase(xpStyle)) {
        background = new ColorUIResource(192, 195, 209);
        backgroundGradientStart = new ColorUIResource(196, 200, 212);
        backgroundGradientEnd = new ColorUIResource(177, 179, 200);
      } else {
        if (OS.isWindowsVista()) {
          final Toolkit toolkit = Toolkit.getDefaultToolkit();
          background = new ColorUIResource((Color) toolkit.getDesktopProperty("win.3d.backgroundColor"));
          backgroundGradientStart = new ColorUIResource((Color) toolkit.getDesktopProperty("win.frame.activeCaptionColor"));
          backgroundGradientEnd = new ColorUIResource((Color) toolkit.getDesktopProperty("win.frame.inactiveCaptionColor"));
        } else {
          background = new ColorUIResource(117, 150, 227);
          backgroundGradientStart = new ColorUIResource(123, 162, 231);
          backgroundGradientEnd = new ColorUIResource(99, 117, 214);
        }
      }
      defaults.addAll(Arrays.asList(new Object[]{"TaskPane.useGradient",
          Boolean.TRUE,
          "TaskPane.background",
          background,
          "TaskPane.backgroundGradientStart",
          backgroundGradientStart,
          "TaskPane.backgroundGradientEnd",
          backgroundGradientEnd,}));
    }
  }

  protected void addMacDefaults(LookAndFeelAddons addon, List defaults) {
    super.addMacDefaults(addon, defaults);
    defaults.addAll(Arrays.asList(new Object[]{"TaskPane.background", new ColorUIResource(238, 238, 238),}));
  }

}
