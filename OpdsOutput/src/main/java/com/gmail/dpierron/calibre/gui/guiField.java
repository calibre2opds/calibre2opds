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
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.Index;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import javax.swing.*;
import javax.swing.text.StringContent;
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
    if (labelText.endsWith(".tooltip"))  tooltipText = "";

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
      }
    }
    // If tooltip provided apply tooltip localisation
    if (Helper.isNotNullOrEmpty(tooltipText)) {
      if (guiLabel != null) guiLabel.setToolTipText(tooltipText);
      if (guiValue != null) guiValue.setToolTipText(tooltipText);
    }
  }

  private int getValue(String s) {
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

    String getMethodName = "get" + methodBase;
    String setMethodName = "set" + methodBase;
    Method getMethod;
    Method setMethod = null;
    Object paramType = null;
    Class[] paramTypes = new Class[1];
    try {
      getMethod = ConfigurationHolder.class.getDeclaredMethod(getMethodName);
      paramType = getMethod.getReturnType();
      paramTypes[0] = paramType.getClass();
      setMethod = ConfigurationHolder.class.getDeclaredMethod(setMethodName, paramTypes);
    } catch (NoSuchMethodException e1) {
      logger.error("storeValue:  unhandled parameter type for  '" + setMethodName + "'");
    } finally {
      if (setMethod == null) {
        logger.error("Unable to get method " + setMethodName);
      } else try {
          // Text fields
        if (guiValue instanceof JTextField) {
          String s = ((JTextField) guiValue).getText();
          assert s != null : "storeValue:  Unexpected null return reading value of " + guiValue.getName();
          if (minimum == 0 && maximum == 0) {
            if (paramType instanceof String) {
              setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), s);
            } else if (paramType instanceof File) {
              // This catches the case where the text field relates to a file/folder
              setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), Helper.isNullOrEmpty(s) ? null : new File(s));
            } else if (paramType instanceof Integer) {
              setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), Integer.getInteger(s));
            }
          } else {
            assert (paramType instanceof Integer);
            Integer i = getValue(s);
            setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), i);
          }

        } else if (guiValue instanceof JCheckBox) {
          Boolean b = ((JCheckBox) guiValue).isSelected();
          setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), negate ?  !b : b);

        } else if (guiValue instanceof JComboBox) {
          setMethod.invoke(ConfigurationManager.INSTANCE.getCurrentProfile(), ((JComboBox) guiValue).getSelectedItem());

        } else {
          logger.error("storeValue: gui value oBject type not recognised for " + guiLabel);
        }
      // None of the following should happen except in development - but lets play safe!
      } catch (final SecurityException e) {
        int dummy = 2;
      } catch (final IllegalArgumentException e) {
        int dummy = 4;
      } catch (final IllegalAccessException e) {
        int dummy = 5;
      } catch (final InvocationTargetException e) {
        int dummy = 6;
      } catch (Exception e) {
        int dummy = 1;
      }
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

    String getMethodName = "get" + methodBase;
    String isReadOnlyMethodName = "is" + methodBase + "ReadOnly";
    try {
      Method method = ConfigurationHolder.class.getDeclaredMethod(getMethodName);
      Object returnType = method.getReturnType();
      // Text fields
      if (guiValue instanceof JTextField) {
        String s;
        if (String.class.equals(returnType)) {
          s = (String) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
        } else if (Integer.class.equals(returnType)) {
          Integer i = (Integer) (method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile()));
          s = i.toString();
        } else if (File.class.equals(returnType)) {
          File f = (File) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
          s = (f==null ? "" : f.getAbsolutePath());
        } else {
          logger.error("loadValue:  Unhandled return type for " + methodBase);
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
        Boolean b = (Boolean) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
        ((JCheckBox)guiValue).setSelected(negate ? !b : b);
      } else if (guiValue instanceof JComboBox) {
        String s = (String) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
        ((JComboBox) guiValue).setSelectedItem(s);
      } else {
        logger.error("loadValue: guiValue oBject type not recognised for " + guiLabel);
      }

      // Now use ReadOnly method to see if field should be currently dsiabled
      // If no such method eists, then assume is always enabled
      Boolean disable;
      try {
        method = ConfigurationHolder.class.getDeclaredMethod(isReadOnlyMethodName);
        disable = (Boolean) method.invoke(ConfigurationManager.INSTANCE.getCurrentProfile());
      } catch (final NoSuchMethodException e) {
        disable = false;
      }
      if (guiLabel != null) guiLabel.setEnabled(!disable);
      if (guiValue != null) guiValue.setEnabled(!disable);

    } catch (final SecurityException e) {
      int dummy = 2;
    } catch (final NoSuchMethodException e) {
      logger.error("loadValue:  Method '" + getMethodName + "' not found");
    } catch (final IllegalArgumentException e) {
      int dummy = 4;
    } catch (final IllegalAccessException e) {
      int dummy = 5;
    } catch (final InvocationTargetException e) {
      int dummy = 6;
    } catch (Exception e) {
      int dummy = 1;
    }
  }
}
