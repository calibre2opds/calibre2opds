package com.gmail.dpierron.calibre.gui;

/**
 * Created by WalkerDJ on 19/04/2014.
 *
 * This class describes a single field within the GUI.
 *
 * It purpose is to allo some generalisation of the code to do with tranlating
 * the user interface, and loading and storing configuration settings.
 *
 * It uses the Java reflection API to allow method names to be derived from strings.  Since
 * for all properties we have a get, set and isReadOnly method we can store just the base name.
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import javax.swing.*;
import java.lang.reflect.*;

public class guiField {
  private final static Logger logger = Logger.getLogger(guiField.class);

  private JComponent guiLabel;      // Label associated with this field
  private JComponent guiValue;      // Value field associated with this field.  NULL if no value involved
  private String localizationKey;  // LocalisationText (without .lable/.tooltip)
  private String methodBase;        // The base of the methodNames associated with loading/storing values
                                    // Set it to null if this field does not have a value loaded/stored
  boolean negate;                   // For check boxes, set to true if checkbox value is negated when displayed
  int minimum, maximum;             // Set for numeric fields to indicate range.  If both zero then field is not numeric
  // CONSTRUCTORS

  // Constructor for checkbox fields that have associated configuration information
  public guiField (JComponent label,  JComponent value, String localizationKey, String methodBase, int minimum, int maximum) {
    initialise(label,value, localizationKey, methodBase, false, minimum,maximum);
  }

  // Constructor for checkbox fields that have associated configuration information
  public guiField (JComponent label,  JComponent value, String localizationKey, String methodBase, boolean negate) {
    initialise(label,value, localizationKey, methodBase, negate, 0,0);
  }
  // Constructor for none-checkbox fields that have associated configuration information
  public guiField (JComponent label,  JComponent value, String localizationKey, String methodBase) {
    initialise(label,value, localizationKey, methodBase, false, 0,0);
  }

  // Constructor for fields that have no associated configuration information
  public guiField (JComponent label,  JComponent value, String localizationKey) {
    initialise(label,value, localizationKey, null, false, 0,0);
  }

  // METHODS

  private void initialise(JComponent label,  JComponent value, String localizationKey, String methodBase,boolean negate, int minimum, int maximum) {
    this.guiLabel = label;
    this.guiValue = value;
    this.localizationKey = localizationKey;
    this.methodBase = methodBase;
    this.negate = negate;
    this.minimum = minimum;
    this.maximum = maximum;
  }
  /**
   * Apply the localization for the given field(s)
   */
  public void translateTexts () {
    String labelText = Localization.Main.getText(localizationKey + ".label");
    // Allow for cases where keyndoes not end in .label
    if (labelText.endsWith("label"))  labelText = Localization.Main.getText(localizationKey);
    if (labelText.equals(localizationKey))  labelText = "";
    String tooltipText = Localization.Main.getText(localizationKey + ".tooltip");
    // Allow for tooltips not lways being supplied
    if (tooltipText.endsWith(".tooltip"))  tooltipText = "";

    // Apply localisation to the designated 'label' field
    if (guiLabel != null) {
      if (guiLabel instanceof JButton) {
        ((JButton) guiLabel).setText(labelText);
      } else if (guiLabel instanceof JLabel) {
        ((JLabel) guiLabel).setText(labelText);
      } else if (guiLabel instanceof JTextField) {
        ((JTextField) guiLabel).setText(labelText);
      } else if (guiLabel instanceof JCheckBox) {
        ((JCheckBox) guiLabel).setText(labelText);
      } else if (guiLabel instanceof JTabbedPane) {
        int tabIndex = -1 + (localizationKey.charAt(localizationKey.length() - 1)) - '0';
        ((JTabbedPane) guiLabel).setTitleAt(tabIndex, labelText);
      } else if (guiLabel instanceof JMenuItem) {
        ((JMenuItem) guiLabel).setText(labelText);
      } else {
        logger.error("setTranslateTexta:  Cannot handle the type for LocalizationKey=" + localizationKey);
        return;
      }
    }
    // If tooltip provided apply tooltip localisation
    if (Helper.isNotNullOrEmpty(tooltipText)) {
      if (guiLabel != null) guiLabel.setToolTipText(tooltipText);
      if (guiValue != null) guiValue.setToolTipText(tooltipText);
    }
  }

  private Integer getValue(String s) {
    try {
      int i = Integer.parseInt(s);
      return i;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Store the configuration information for the given field
   * Do nothing if field has no associted value field or method
   */
  public void storeValue() {

    if (Helper.isNullOrEmpty(guiValue)) {
      return;
    }
    if (Helper.isNullOrEmpty(methodBase)) {
      return;
    }
    Object paramType = null;
    Class[] paramTypes = new Class[1];
    String getMethodName = "get" + methodBase;
    String setMethodName = "set" + methodBase;
    try {
      // Use get method to work out parameter type for set method
      Method getMethod = ConfigurationHolder.class.getDeclaredMethod(getMethodName);
      paramTypes[0] = getMethod.getReturnType();
      paramType = getMethod.getReturnType();
    } catch (NoSuchMethodException e1) {
      logger.error("storeValue:  unable to find get method '" + getMethodName + "'");
      return;
    }
    // Now we can set up the parameter for the setmethod
    Object setValue = null;
    // Text fields
    if (guiValue instanceof JTextField) {
      String s = ((JTextField) guiValue).getText();
      assert s != null : "storeValue:  Unexpected null return reading value of " + guiValue.getName();
      if (minimum == 0 && maximum == 0) {
        if (String.class.equals(paramType)) {
          setValue = s;
        } else if (File.class.equals(paramType)) {
          if (s.length() > 0)   setValue = new File(s);
        } else if (Integer.class.equals (paramType)) {
          setValue = getValue(s);
        } else {
          logger.error("storeValue: Unexpected paramType for '" + setMethodName + "'");
          return;
        }
      } else {
        assert (Integer.class.equals(paramType));
        setValue = getValue(s);
      }
    // Checkboxes
    } else if (guiValue instanceof JCheckBox) {
      Boolean b = ((JCheckBox) guiValue).isSelected();
      setValue = negate ? !b : b;
    // Combo boxes
    } else if (guiValue instanceof JComboBox) {
      setValue = ((JComboBox) guiValue).getSelectedItem();

    } else {
      logger.error("storeValue: gui value oBject type not recognised for " + guiLabel);
      return;
    }
    // Now we can do the set method!
    Method setMethod;
    try {
      setMethod = ConfigurationHolder.class.getDeclaredMethod(setMethodName, paramTypes);
    } catch (NoSuchMethodException e1) {
      logger.error("storeValue:  unhandled parameter type for  '" + setMethodName + "'");
      return;
    }
    try {
      setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), setValue);

    // None of the following should happen except in development - but lets play safe!
    } catch (final Exception e) {
      logger.error("storeValue: invoke 'set" + methodBase + "Excepion " + e );
    }
  }

  /**
   * Load the configuration informtion for the field
   * Do nothing if field has no associted value field or method
   */
  public void loadValue() {

    if (Helper.isNullOrEmpty(guiValue)) {
      return;
    }
    if (Helper.isNullOrEmpty(methodBase)) {
      return;
    }

    InputVerifier iv = new InputVerifier() {

      @Override
      public boolean verify(JComponent input) {
        if (!(input instanceof JTextField))
          return false;
        try {
          Integer.parseInt(((JTextField) input).getText());
          return true;
        } catch (NumberFormatException e) {
          return false;
        }
      }
    };

    Method method;
    Object loadValue = null;
    // Find the get method
    try {
      method = ConfigurationHolder.class.getDeclaredMethod("get" + methodBase);
    } catch (final NoSuchMethodException e) {
      logger.error("loadValue:  Method 'get" + methodBase + "' not found");
      return;
    }
    // Invoke the get method
    try {
      loadValue =  method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
    } catch (Exception e) {
      logger.error("loadValue:  Invoke 'get" + methodBase + "' Exception " + e);
      return;
    }

    // Now use the return value to set the GUI field

    // Text fields
    if (guiValue instanceof JTextField) {
      String s;
      if (loadValue instanceof String) {
        s = (String)loadValue;
      } else if (loadValue instanceof Integer) {
        s = ((Integer)loadValue).toString();
      } else if (loadValue instanceof File) {
        s = (loadValue==null ? "" : ((File)loadValue).getAbsolutePath());
      } else {
        // We assume an empty string for null being returned
        s = "";
      }
      // If parameter exceeds maximum, then co-erceto the maximum value
      if (minimum != 0 || maximum != 0) {
        if (Integer.parseInt(s) > maximum) {
          s = Integer.toString(maximum);
        }
        ((JTextField) guiValue).setInputVerifier(iv);
      }
      ((JTextField) guiValue).setText(s);
    // Check boxes
    } else if (guiValue instanceof JCheckBox) {
      assert loadValue instanceof  Boolean;
      ((JCheckBox)guiValue).setSelected(negate ? !(Boolean)loadValue : (Boolean)loadValue);
    } else if (guiValue instanceof JComboBox) {
      assert loadValue instanceof  String;
      ((JComboBox) guiValue).setSelectedItem((String)loadValue);
    } else {
      logger.error("loadValue: guiValue oBject type not recognised for " + guiLabel);
      return;
    }

    // Now use ReadOnly method to see if field should be currently dsiabled
    // If no such method eists, then assume is always enabled
    Boolean disable;
    try {
      String methodName = "is" + methodBase + "ReadOnly";
      method = ConfigurationHolder.class.getDeclaredMethod(methodName);
      disable = (Boolean) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
    } catch (final Exception e) {
      // We assume ReadOnly method does not exist if we get to this point.
      disable = false;
    }
    if (guiLabel != null) guiLabel.setEnabled(!disable);
    if (guiValue != null) guiValue.setEnabled(!disable);
  }
}
