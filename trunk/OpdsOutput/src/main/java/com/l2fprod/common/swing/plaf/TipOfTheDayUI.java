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

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.plaf.ComponentUI;

/**
 * Pluggable UI for <code>JTipOfTheDay</code>.
 * 
 */
public abstract class TipOfTheDayUI extends ComponentUI {

  /**
   * Creates a new JDialog suitable to display a TipOfTheDay panel. If
   * <code>choice</code> is not null then the window will offer a way for the
   * end-user to not show the tip of the day dialog.
   * 
   * @param parentComponent
   * @param choice
   */
  public abstract JDialog createDialog(Component parentComponent,
    JTipOfTheDay.ShowOnStartupChoice choice);

}
