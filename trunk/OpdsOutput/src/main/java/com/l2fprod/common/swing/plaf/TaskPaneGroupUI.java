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

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.plaf.PanelUI;

/**
 * Pluggable UI for <code>JTaskPaneGroup</code>.
 *  
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class TaskPaneGroupUI extends PanelUI {

  /**
   * Called by the component when an action is added to the component through
   * the {@link com.l2fprod.common.swing.JTaskPaneGroup#add(Action)} method.
   * 
   * @param action
   * @return a component built from the action.
   */
  public Component createAction(Action action) {
    return new JButton(action);
  }

}
