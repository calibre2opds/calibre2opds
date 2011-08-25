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

import com.l2fprod.common.swing.plaf.JTaskPaneGroupAddon;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import com.l2fprod.common.swing.plaf.TaskPaneGroupUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * <code>JTaskPaneGroup</code> is a container for tasks and other
 * arbitrary components.
 * 
 * <p>
 * Several <code>JTaskPaneGroup</code>s are usually grouped together within a
 * {@link JTaskPane}. However it is not mandatory
 * to use a JTaskPane as the parent for JTaskPaneGroup. The JTaskPaneGroup can
 * be added to any other container. See
 * {@link JTaskPane} to understand the benefits of
 * using it as the parent container.
 * 
 * <p>
 * <code>JTaskPaneGroup</code> provides control to expand and
 * collapse the content area in order to show or hide the task list. It can have an
 * <code>icon</code>, a <code>title</code> and can be marked as
 * <code>special</code>. Marking a <code>JTaskPaneGroup</code> as
 * <code>special</code> is only a hint for the pluggable UI which
 * will usually paint it differently (by example by using another
 * color for the border of the pane).
 * 
 * <p> 
 * When the JTaskPaneGroup is expanded or collapsed, it will be
 * animated with a fade effect. The animated can be disabled on a per
 * component basis through {@link #setAnimated(boolean)}.
 * 
 * To disable the animation for all newly created <code>JTaskPaneGroup</code>,
 * use the UIManager property:
 * <code>UIManager.put("TaskPaneGroup.animate", Boolean.FALSE);</code>.
 * 
 * <p>
 * Example:
 * <pre>
 * <code>
 * JXFrame frame = new JXFrame();
 * 
 * // a container to put all JTaskPaneGroup together
 * JTaskPane taskPaneContainer = new JTaskPane();
 * 
 * // create a first taskPane with common actions
 * JTaskPaneGroup actionPane = new JTaskPaneGroup();
 * actionPane.setTitle("Files and Folders");
 * actionPane.setSpecial(true);
 * 
 * // actions can be added, an hyperlink will be created
 * Action renameSelectedFile = createRenameFileAction();
 * actionPane.add(renameSelectedFile);
 * actionPane.add(createDeleteFileAction());
 * 
 * // add this taskPane to the taskPaneContainer
 * taskPaneContainer.add(actionPane);
 * 
 * // create another taskPane, it will show details of the selected file
 * JTaskPaneGroup details = new JTaskPaneGroup();
 * details.setTitle("Details");
 *  
 * // add standard components to the details taskPane
 * JLabel searchLabel = new JLabel("Search:");
 * JTextField searchField = new JTextField("");
 * details.add(searchLabel);
 * details.add(searchField);
 * 
 * taskPaneContainer.add(details);
 * 
 * // put the action list on the left 
 * frame.add(taskPaneContainer, BorderLayout.EAST);
 * 
 * // and a file browser in the middle
 * frame.add(fileBrowser, BorderLayout.CENTER);
 * 
 * frame.pack().
 * frame.setVisible(true);
 * </code>
 * </pre>
 * 
 * @see JTaskPane
 * @see JCollapsiblePane
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * 
 * @javabean.attribute
 *          name="isContainer"
 *          value="Boolean.TRUE"
 *          rtexpr="true"
 *          
 * @javabean.attribute
 *          name="containerDelegate"
 *          value="getContentPane"
 *          
 * @javabean.class
 *          name="JTaskPaneGroup"
 *          shortDescription="JTaskPaneGroup is a container for tasks and other arbitrary components."
 *          stopClass="java.awt.Component"
 * 
 * @javabean.icons
 *          mono16="JTaskPaneGroup16-mono.gif"
 *          color16="JTaskPaneGroup16.gif"
 *          mono32="JTaskPaneGroup32-mono.gif"
 *          color32="JTaskPaneGroup32.gif"
 */
public class JTaskPaneGroup extends JPanel implements
  JCollapsiblePane.JCollapsiblePaneContainer {

  public final static String UI_CLASS_ID = "TaskPaneGroupUI";
  
  // ensure at least the default ui is registered
  static {
    LookAndFeelAddons.contribute(new JTaskPaneGroupAddon());
  }

  /**
   * Used when generating PropertyChangeEvents for the "expanded" property
   */
  public static final String EXPANDED_CHANGED_KEY = "expanded";

  /**
   * Used when generating PropertyChangeEvents for the "collapsable" property
   */
  public static final String COLLAPSABLE_CHANGED_KEY = "collapsable";

  /**
   * Used when generating PropertyChangeEvents for the "scrollOnExpand" property
   */
  public static final String SCROLL_ON_EXPAND_CHANGED_KEY = "scrollOnExpand";

  /**
   * Used when generating PropertyChangeEvents for the "title" property
   */
  public static final String TITLE_CHANGED_KEY = "title";

  /**
   * Used when generating PropertyChangeEvents for the "icon" property
   */
  public static final String ICON_CHANGED_KEY = "icon";

  /**
   * Used when generating PropertyChangeEvents for the "special" property
   */
  public static final String SPECIAL_CHANGED_KEY = "special";

  /**
   * Used when generating PropertyChangeEvents for the "animated" property
   */
  public static final String ANIMATED_CHANGED_KEY = "animated";

  private String title;
  private Icon icon;
  private boolean special;
  private boolean expanded = true;
  private boolean scrollOnExpand;
  private boolean collapsable = true;
  
  private JCollapsiblePane collapsePane;
  
  /**
   * Creates a new empty <code>JTaskPaneGroup</code>.
   */
  public JTaskPaneGroup() {
    collapsePane = new JCollapsiblePane();
    super.setLayout(new BorderLayout(0, 0));
    super.addImpl(collapsePane, BorderLayout.CENTER, -1);
    
    updateUI();
    setFocusable(true);
    setOpaque(false);

    // disable animation if specified in UIManager
    setAnimated(!Boolean.FALSE.equals(UIManager.get("TaskPaneGroup.animate")));
    
    // listen for animation events and forward them to registered listeners
    collapsePane.addPropertyChangeListener(
      JCollapsiblePane.ANIMATION_STATE_KEY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          JTaskPaneGroup.this.firePropertyChange(evt.getPropertyName(), evt
            .getOldValue(), evt.getNewValue());
        }
      });
  }

  /**
   * Returns the contentPane object for this JTaskPaneGroup.
   * @return the contentPane property
   */
  public Container getContentPane() {
    return collapsePane.getContentPane();
  }
  
  /**
   * Notification from the <code>UIManager</code> that the L&F has changed.
   * Replaces the current UI object with the latest version from the <code>UIManager</code>.
   * 
   * @see javax.swing.JComponent#updateUI
   */
  public void updateUI() {
    // collapsePane is null when updateUI() is called by the "super()"
    // constructor
    if (collapsePane == null) {
      return;
    }
    setUI((TaskPaneGroupUI)LookAndFeelAddons.getUI(this, TaskPaneGroupUI.class));
  }
  
  /**
   * Sets the L&F object that renders this component.
   * 
   * @param ui the <code>TaskPaneGroupUI</code> L&F object
   * @see javax.swing.UIDefaults#getUI
   * 
   * @beaninfo bound: true hidden: true description: The UI object that
   * implements the taskpane group's LookAndFeel.
   */
  public void setUI(TaskPaneGroupUI ui) {
    super.setUI(ui);
  }

  /**
   * Returns the name of the L&F class that renders this component.
   * 
   * @return the string {@link #UI_CLASS_ID}
   * @see javax.swing.JComponent#getUIClassID
   * @see javax.swing.UIDefaults#getUI
   */
  public String getUIClassID() {
    return UI_CLASS_ID;
  }

  /**
   * Returns the title currently displayed in the border of this pane.
   * 
   * @since 0.2
   * @return the title currently displayed in the border of this pane
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title to be displayed in the border of this pane.
   * 
   * @since 0.2
   * @param title the title to be displayed in the border of this pane
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setTitle(String title) {
    String old = title;
    this.title = title;
    firePropertyChange(TITLE_CHANGED_KEY, old, title);
  }

  /**
   * @param text
   * @deprecated
   * @see #setTitle(String)
   */
  public void setText(String text) {
    setTitle(text);
  }
  
  /**
   * @deprecated
   * @see #getTitle()
   */
  public String getText() {
    return getTitle();
  }
  
  /**
   * Returns the icon currently displayed in the border of this pane.
   * 
   * @return the icon currently displayed in the border of this pane
   */
  public Icon getIcon() {
    return icon;
  }

  /**
   * Sets the icon to be displayed in the border of this pane. Some pluggable
   * UIs may impose size constraints for the icon. A size of 16x16 pixels is
   * the recommended icon size.
   * 
   * @param icon the icon to be displayed in the border of this pane
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setIcon(Icon icon) {
    Icon old = icon;
    this.icon = icon;
    firePropertyChange(ICON_CHANGED_KEY, old, icon);
  }

  /**
   * Returns true if this pane is "special".
   * 
   * @return true if this pane is "special"
   */
  public boolean isSpecial() {
    return special;
  }

  /**
   * Sets this pane to be "special" or not. Marking a <code>JTaskPaneGroup</code>
   * as <code>special</code> is only a hint for the pluggable UI which will
   * usually paint it differently (by example by using another color for the
   * border of the pane).
   * 
   * <p>
   * Usually the first JTaskPaneGroup in a JTaskPane is marked as special
   * because it contains the default set of actions which can be executed given
   * the current context.
   * 
   * @param special true if this pane is "special", false otherwise
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setSpecial(boolean special) {
    if (this.special != special) {
      this.special = special;
      firePropertyChange(SPECIAL_CHANGED_KEY, !special, special);
    }
  }

  /**
   * Should this group be scrolled to be visible on expand.
   * 
   * 
   * @param scrollOnExpand true to scroll this group to be
   * visible if this group is expanded.
   * 
   * @see #setExpanded(boolean)
   * 
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setScrollOnExpand(boolean scrollOnExpand) {
    if (this.scrollOnExpand != scrollOnExpand) {
      this.scrollOnExpand = scrollOnExpand;
      firePropertyChange(SCROLL_ON_EXPAND_CHANGED_KEY,
        !scrollOnExpand, scrollOnExpand);
    }
  }
  
  /**
   * Should this group scroll to be visible after
   * this group was expanded.
   * 
   * @return true if we should scroll false if nothing
   * should be done.
   */
  public boolean isScrollOnExpand() {
    return scrollOnExpand;
  }
  
  /**
   * Expands or collapses this group.
   * 
   * @param expanded true to expand the group, false to collapse it
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      collapsePane.setCollapsed(!expanded);
      firePropertyChange(EXPANDED_CHANGED_KEY, !expanded, expanded);
    }
  }

  /**
   * Returns true if this taskpane is expanded, false if it is collapsed.
   * 
   * @return true if this taskpane is expanded, false if it is collapsed.
   */
  public boolean isExpanded() {
    return expanded;
  }

  /**
   * Sets whether or not this group can be collapsed by the user
   * 
   * @param collapsable false to prevent the group to be manually collapsed
   * @javabean.property bound="true" preferred="true"
   */
  public void setCollapsable(boolean collapsable) {
    if (this.collapsable != collapsable) {
      this.collapsable = collapsable;
      firePropertyChange(COLLAPSABLE_CHANGED_KEY, !collapsable, collapsable);
    }
  }

  /**
   * @return true if this taskpane can be collapsed by the user.
   */
  public boolean isCollapsable() {
    return collapsable;
  }
  
  /**
   * Enables or disables animation during expand/collapse transition.
   * 
   * @param animated
   * @javabean.property
   *          bound="true"
   *          preferred="true"
   */
  public void setAnimated(boolean animated) {
    if (isAnimated() != animated) {
      collapsePane.setAnimated(animated);
      firePropertyChange(ANIMATED_CHANGED_KEY, !isAnimated(), isAnimated());
    }
  }
  
  /**
   * Returns true if this taskpane is animated during expand/collapse
   * transition.
   * 
   * @return true if this taskpane is animated during expand/collapse
   *         transition.
   */
  public boolean isAnimated() {
    return collapsePane.isAnimated();
  }
  
  /**
   * Adds an action to this <code>JTaskPaneGroup</code>. Returns a
   * component built from the action. The returned component has been
   * added to the <code>JTaskPaneGroup</code>.
   * 
   * @param action
   * @return a component built from the action
   */
  public Component add(Action action) {
    Component c = ((TaskPaneGroupUI)ui).createAction(action);
    add(c);
    return c;
  }

  public Container getValidatingContainer() {
    return getParent();
  }
  
  /**
   * Overriden to redirect call to the content pane.
   */
  protected void addImpl(Component comp, Object constraints, int index) {
    getContentPane().add(comp, constraints, index);
  }

  /**
   * Overriden to redirect call to the content pane.
   */
  public void setLayout(LayoutManager mgr) {
    if (collapsePane != null) {
      getContentPane().setLayout(mgr);
    }
  }
  
  /**
   * Overriden to redirect call to the content pane
   */
  public void remove(Component comp) {
    getContentPane().remove(comp);
  }

  /**
   * Overriden to redirect call to the content pane.
   */
  public void remove(int index) {
    getContentPane().remove(index);
  }
  
  /**
   * Overriden to redirect call to the content pane.
   */
  public void removeAll() {
    getContentPane().removeAll();
  }

  /**
   * Overriden to prevent focus to group header when group is not collapsable
   */
  public boolean isFocusable() {
    return super.isFocusable() && isCollapsable();
  }
  
  /**
   * @see JComponent#paramString()
   */
  protected String paramString() {
    return super.paramString()
      + ",title="
      + getTitle()
      + ",icon="
      + getIcon()
      + ",expanded="
      + String.valueOf(isExpanded())
      + ",special="
      + String.valueOf(isSpecial())
      + ",scrollOnExpand=" 
      + String.valueOf(isScrollOnExpand())
      + ",ui=" + getUI();
  }

}
