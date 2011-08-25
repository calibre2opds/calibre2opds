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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class is used to create a multiple-exclusion scope for a set of
 * JTaskPaneGroups. Creating a set of JTaskPaneGroups with the same
 * GroupOfTaskPaneGroup object means that expanding one of those JTaskPaneGroups
 * will collapse all other JTaskPaneGroups in the group.
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class GroupOfTaskPaneGroup implements PropertyChangeListener {

  private JTaskPaneGroup selection;

  /**
   * Adds a <code>JTaskPaneGroup</code> to this group.
   * 
   * @param taskpaneGroup
   */
  public void add(JTaskPaneGroup taskpaneGroup) {
    register(taskpaneGroup);
    // if we have no selection
    if (selection == null) {
      // and if the taskpane is expanded
      if (taskpaneGroup.isExpanded()) {
        // then the selection becomes the taskpane
        selection = taskpaneGroup;
      }
    } else {
      // we have a selection, so this taskpane must be collapsed
      taskpaneGroup.setExpanded(false);
    }
     
    maybeUpdateSelection(taskpaneGroup);
  }

  /**
   * Removes a <code>JTaskPaneGroup</code> from this group.
   * 
   * @param taskpaneGroup
   */
  public void remove(JTaskPaneGroup taskpaneGroup) {
    unregister(taskpaneGroup);
    // if we are removing the currently selection taskpane, reset the selection
    // to "null" otherwise the taskpane may get modified by us later.
    if (selection == taskpaneGroup) {
      selection = null;
    }
  }

  public void propertyChange(PropertyChangeEvent event) {
    // if a taskpane gets expanded, collapse the previously expanded one if any
    JTaskPaneGroup taskpaneGroup = (JTaskPaneGroup) event.getSource();
    maybeUpdateSelection(taskpaneGroup);
  }

  private void maybeUpdateSelection(JTaskPaneGroup taskpaneGroup) {
    if (selection != taskpaneGroup && taskpaneGroup.isExpanded()) {
      if (selection != null) {
        selection.setExpanded(false);
      }
      selection = taskpaneGroup;
    }
  }

  private void register(JTaskPaneGroup taskpaneGroup) {
    taskpaneGroup.addPropertyChangeListener(
        JTaskPaneGroup.EXPANDED_CHANGED_KEY, this);
  }

  private void unregister(JTaskPaneGroup taskpaneGroup) {
    taskpaneGroup.removePropertyChangeListener(
        JTaskPaneGroup.EXPANDED_CHANGED_KEY, this);
  }

}
