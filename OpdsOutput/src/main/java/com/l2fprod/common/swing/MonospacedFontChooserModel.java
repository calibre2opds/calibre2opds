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
package com.l2fprod.common.swing;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An extension of the {@link com.l2fprod.common.swing.DefaultFontChooserModel}
 * showing only Monospaced fonts.
 */
public class MonospacedFontChooserModel extends DefaultFontChooserModel {

  public MonospacedFontChooserModel() {
    super();

    List monospaces = new ArrayList();
    String[] fontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    Arrays.sort(fontFamilies);
    for (int i = 0, c = fontFamilies.length; i < c; i++) {
      if (isMonospaced(fontFamilies[i])) {
        monospaces.add(fontFamilies[i]);
      }
    }

    setFontFamilies((String[]) monospaces.toArray(new String[monospaces.size()]));
  }

  /**
   * @param fontFamily
   * @return true if <code>fontFamily</code> can be considered as Monospaced,
   *         i.e if it contains the strings Fixed, Monospaced, ProFont, Console
   *         or Typewriter
   */
  protected boolean isMonospaced(String fontFamily) {
    String lower = fontFamily.toLowerCase();
    return lower.indexOf("fixed") >= 0 || lower.indexOf("monospaced") >= 0 || lower.indexOf("profont") >= 0 ||
        lower.indexOf("console") >= 0 || lower.indexOf("typewriter") >= 0;
  }

}
