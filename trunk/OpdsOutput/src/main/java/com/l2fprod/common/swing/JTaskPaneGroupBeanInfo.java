/*
 * Generated file - Do not edit!
 */
package com.l2fprod.common.swing;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Vector;

/**
 * BeanInfo class for JTaskPaneGroup.
 */
public class JTaskPaneGroupBeanInfo extends SimpleBeanInfo {

  /** Description of the Field */
  protected BeanDescriptor bd = new BeanDescriptor(
    com.l2fprod.common.swing.JTaskPaneGroup.class);
  /** Description of the Field */
  protected Image iconMono16 = loadImage("JTaskPaneGroup16-mono.gif");
  /** Description of the Field */
  protected Image iconColor16 = loadImage("JTaskPaneGroup16.gif");
  /** Description of the Field */
  protected Image iconMono32 = loadImage("JTaskPaneGroup32-mono.gif");
  /** Description of the Field */
  protected Image iconColor32 = loadImage("JTaskPaneGroup32.gif");

  /** Constructor for the JTaskPaneGroupBeanInfo object */
  public JTaskPaneGroupBeanInfo() throws java.beans.IntrospectionException {
    // setup bean descriptor in constructor.
    bd.setName("JTaskPaneGroup");

    bd.setShortDescription("JTaskPaneGroup is a container"
      + " for tasks and other arbitrary components.");

    bd.setValue("isContainer", Boolean.TRUE);
    bd.setValue("containerDelegate", "getContentPane");

    BeanInfo info = Introspector.getBeanInfo(getBeanDescriptor().getBeanClass()
      .getSuperclass());
    String order = info.getBeanDescriptor().getValue("propertyorder") == null?""
      :(String)info.getBeanDescriptor().getValue("propertyorder");
    PropertyDescriptor[] pd = getPropertyDescriptors();
    for (int i = 0; i != pd.length; i++) {
      if (order.indexOf(pd[i].getName()) == -1) {
        order = order + (order.length() == 0?"":":") + pd[i].getName();
      }
    }
    getBeanDescriptor().setValue("propertyorder", order);
  }

  /**
   * Gets the additionalBeanInfo
   * 
   * @return The additionalBeanInfo value
   */
  public BeanInfo[] getAdditionalBeanInfo() {
    Vector bi = new Vector();
    BeanInfo[] biarr = null;
    try {
      for (Class cl = com.l2fprod.common.swing.JTaskPaneGroup.class
        .getSuperclass(); !cl.equals(java.awt.Component.class.getSuperclass()); cl = cl
        .getSuperclass()) {
        bi.addElement(Introspector.getBeanInfo(cl));
      }
      biarr = new BeanInfo[bi.size()];
      bi.copyInto(biarr);
    } catch (Exception e) {
      // Ignore it
    }
    return biarr;
  }

  /**
   * Gets the beanDescriptor
   * 
   * @return The beanDescriptor value
   */
  public BeanDescriptor getBeanDescriptor() {
    return bd;
  }

  /**
   * Gets the defaultPropertyIndex
   * 
   * @return The defaultPropertyIndex value
   */
  public int getDefaultPropertyIndex() {
    String defName = "";
    if (defName.equals("")) { return -1; }
    PropertyDescriptor[] pd = getPropertyDescriptors();
    for (int i = 0; i < pd.length; i++) {
      if (pd[i].getName().equals(defName)) { return i; }
    }
    return -1;
  }

  /**
   * Gets the icon
   * 
   * @param type
   *          Description of the Parameter
   * @return The icon value
   */
  public Image getIcon(int type) {
    if (type == BeanInfo.ICON_COLOR_16x16) { return iconColor16; }
    if (type == BeanInfo.ICON_MONO_16x16) { return iconMono16; }
    if (type == BeanInfo.ICON_COLOR_32x32) { return iconColor32; }
    if (type == BeanInfo.ICON_MONO_32x32) { return iconMono32; }
    return null;
  }

  /**
   * Gets the Property Descriptors
   * 
   * @return The propertyDescriptors value
   */
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      Vector descriptors = new Vector();
      PropertyDescriptor descriptor = null;

      try {
        descriptor = new PropertyDescriptor("title",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("title",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getTitle", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("icon",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("icon",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getIcon", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("special",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("special",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getSpecial", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("scrollOnExpand",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("scrollOnExpand",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getScrollOnExpand",
          null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("expanded",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("expanded",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getExpanded", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("collapsable",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("collapsable",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getCollapsable", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);
      try {
        descriptor = new PropertyDescriptor("animated",
          com.l2fprod.common.swing.JTaskPaneGroup.class);
      } catch (IntrospectionException e) {
        descriptor = new PropertyDescriptor("animated",
          com.l2fprod.common.swing.JTaskPaneGroup.class, "getAnimated", null);
      }

      descriptor.setPreferred(true);

      descriptor.setBound(true);

      descriptors.add(descriptor);

      return (PropertyDescriptor[])descriptors
        .toArray(new PropertyDescriptor[descriptors.size()]);
    } catch (Exception e) {
      // do not ignore, bomb politely so use has chance to discover what went
      // wrong...
      // I know that this is suboptimal solution, but swallowing silently is
      // even worse... Propose better solution!
      e.printStackTrace();
    }
    return null;
  }

}
