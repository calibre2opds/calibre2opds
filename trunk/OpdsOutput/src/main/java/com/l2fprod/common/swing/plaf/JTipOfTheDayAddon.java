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

import com.l2fprod.common.swing.JTipOfTheDay;
import com.l2fprod.common.swing.plaf.basic.BasicTipOfTheDayUI;
import com.l2fprod.common.swing.plaf.windows.WindowsTipOfTheDayUI;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.List;

/**
 * Addon for <code>JTipOfTheDay</code>.<br>
 */
public class JTipOfTheDayAddon extends AbstractComponentAddon {

  public JTipOfTheDayAddon() {
    super("JTipOfTheDay");
  }

  protected void addBasicDefaults(LookAndFeelAddons addon, List defaults) {
    defaults.add(JTipOfTheDay.uiClassID);
    defaults.add(BasicTipOfTheDayUI.class.getName());

    defaults.add("TipOfTheDay.font");
    defaults.add(UIManager.getFont("TextPane.font"));

    defaults.add("TipOfTheDay.tipFont");
    defaults.add(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));

    defaults.add("TipOfTheDay.background");
    defaults.add(new ColorUIResource(Color.white));

    defaults.add("TipOfTheDay.icon");
    defaults.add(LookAndFeel.makeIcon(BasicTipOfTheDayUI.class, "TipOfTheDay24.gif"));

    defaults.add("TipOfTheDay.border");
    defaults.add(new BorderUIResource(BorderFactory.createLineBorder(new Color(117, 117, 117))));

    addResource(defaults, "com.l2fprod.common.swing.plaf.basic.resources.TipOfTheDay");
  }

  protected void addWindowsDefaults(LookAndFeelAddons addon, List defaults) {
    super.addWindowsDefaults(addon, defaults);

    defaults.add(JTipOfTheDay.uiClassID);
    defaults.add(WindowsTipOfTheDayUI.class.getName());

    defaults.add("TipOfTheDay.background");
    defaults.add(new ColorUIResource(128, 128, 128));

    defaults.add("TipOfTheDay.font");
    defaults.add(UIManager.getFont("Label.font").deriveFont(13f));

    defaults.add("TipOfTheDay.icon");
    defaults.add(LookAndFeel.makeIcon(WindowsTipOfTheDayUI.class, "tipoftheday.png"));

    defaults.add("TipOfTheDay.border");
    defaults.add(new BorderUIResource(new WindowsTipOfTheDayUI.TipAreaBorder()));

    addResource(defaults, "com.l2fprod.common.swing.plaf.windows.resources.TipOfTheDay");
  }

}
