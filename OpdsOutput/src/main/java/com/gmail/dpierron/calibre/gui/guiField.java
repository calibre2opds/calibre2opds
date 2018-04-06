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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import javax.swing.*;
import java.lang.reflect.*;
import java.util.Locale;

public class guiField {
  private final static Logger logger = LogManager.getLogger(guiField.class);

  private JComponent guiLabelField;      // Label associated with this field
  private JComponent guiValueField;      // Value field associated with this field.  NULL if no value involved
  private String localizationKey;  // LocalisationText (without .lable/.tooltip)
  private String methodBase;        // The base of the methodNames associated with loading/storing values
                                    // Set it to null if this field does not have a value loaded/stored
  boolean negate;                   // For check boxes, set to true if checkbox value is negated when displayed
  int minimum, maximum;             // Set for numeric fields to indicate range.  If both zero then field is not nu

  //  The following values are cached to reduce the overhead of deriving them each time

  private static ConfigurationHolder currentConfiguration = null;
  private Class  paramTypes[] = new Class[1];
  private Object profileType = null;
  private Object guiType = null;


  //
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
    this.guiLabelField = label;
    this.guiValueField = value;
    this.localizationKey = localizationKey;
    this.methodBase = methodBase;
    this.negate = negate;
    this.minimum = minimum;
    this.maximum = maximum;
  }

  public void setCurrentConfiguration(ConfigurationHolder c) {
    currentConfiguration = c;
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
    if (guiLabelField != null) {
      if (guiLabelField instanceof JButton) {
        ((JButton) guiLabelField).setText(labelText);
      } else if (guiLabelField instanceof JLabel) {
        ((JLabel) guiLabelField).setText(labelText);
      } else if (guiLabelField instanceof JTextField) {
        ((JTextField) guiLabelField).setText(labelText);
      } else if (guiLabelField instanceof JCheckBox) {
        ((JCheckBox) guiLabelField).setText(labelText);
      } else if (guiLabelField instanceof JTabbedPane) {
        int tabIndex = -1 + (localizationKey.charAt(localizationKey.length() - 1)) - '0';
        ((JTabbedPane) guiLabelField).setTitleAt(tabIndex, labelText);
      } else if (guiLabelField instanceof JMenuItem) {
        ((JMenuItem) guiLabelField).setText(labelText);
      } else {
        logger.error("setTranslateTexta:  Cannot handle the type for LocalizationKey=" + localizationKey); Helper.statsErrors++;
        return;
      }
    }
    // If tooltip provided apply tooltip localisation
    if (Helper.isNotNullOrEmpty(tooltipText)) {
      if (guiLabelField != null) guiLabelField.setToolTipText(tooltipText);
      if (guiValueField != null) guiValueField.setToolTipText(tooltipText);
    }
  }

  /**
   * Get the label field associated with this instance
   *
   * @return
   */
  public JComponent getGuiLabel() {
    return guiLabelField;
  }

  public int getMinimum() {
    return minimum;
  }
  public int getMaximum () {
    return maximum;
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
   * Method that gets the value from the GUI and
   * returns it as an object of the correct type to
   * compare against a profile object.
   *
   * @return
   */
  public Object getGuiValue() {
    if (methodBase == null) {
      return null;
    }
    Object guiValue = null;
    // Text fields
    if (guiValueField instanceof JTextField) {
      String s = ((JTextField) guiValueField).getText();
      assert s != null : "getGuiValue:  Unexpected null return reading value of " + guiValueField.getName();
      if (minimum == 0 && maximum == 0) {
        if (String.class.equals(profileType)) {
          guiValue = s;
        } else if (File.class.equals(profileType)) {
          if (s.length() > 0)   guiValue = new File(s);
        } else if (Integer.class.equals (profileType)) {
          guiValue = getValue(s);
        } else if (Locale.class.equals(profileType)) {
          guiValue = s.length() == 0 ? "en" : s;
        } else {
          logger.error("getGuiValue: Unexpected profileType for 'get" + methodBase + "', type=" + profileType.getClass().getName()); Helper.statsErrors++;
          guiValue = null;
        }
      } else {
        assert (Integer.class.equals(profileType));
        guiValue = getValue(s);
      }
      // Checkboxes
    } else if (guiValueField instanceof JCheckBox) {
      Boolean b = ((JCheckBox) guiValueField).isSelected();
      guiValue = negate ? !b : b;
      // Combo boxes
    } else if (guiValueField instanceof JComboBox) {
      guiValue = ((JComboBox) guiValueField).getSelectedItem();

    } else {
      logger.error("getGuiValue: object type (" + guiValueField.getClass().getName() + ") not recognised for " + guiValueField.getName()); Helper.statsErrors++;
      guiValue = null;
    }

    // For language fields we need a locale object
    if (guiValue != null && Locale.class.equals(profileType)) {
      assert String.class.equals(guiValue.getClass()) : "Unexpected class " + guiValue.getClass().getName() +" (" + guiValue + ")";
      guiValue = Helper.getLocaleFromLanguageString((String)guiValue);
    }
    return guiValue;
  }

  /**
   * Transfer the value to the correct GUI field
   *
   * @param oValue
   */
  private void putGuiValue (Object oValue) {
    if (oValue == null) {
      if (profileType instanceof File) {
        oValue="";
      }
    }
    // Text fields
    if (guiValueField instanceof JTextField) {
      String s;
      if (oValue instanceof String) {
        s = (String)oValue;
      } else if (oValue instanceof Integer) {
        s = ((Integer)oValue).toString();
      } else if (oValue instanceof File) {
        s = ((File)oValue).getAbsolutePath();
      } else if (oValue instanceof  Locale) {
        s = ((Locale)oValue).getLanguage();
      } else {
        // We assume an empty string for null being returned
        s = "";
      }
      // If parameter exceeds maximum, then coerce to the maximum value
      if (minimum != 0 || maximum != 0) {
        if (Integer.parseInt(s) > maximum) {
          s = Integer.toString(maximum);
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
        ((JTextField) guiValueField).setInputVerifier(iv);
      }
      ((JTextField) guiValueField).setText(s);
      // Check boxes
    } else if (guiValueField instanceof JCheckBox) {
      assert oValue instanceof  Boolean;
      ((JCheckBox)guiValueField).setSelected(negate ? !(Boolean)oValue : (Boolean)oValue);
    } else if (guiValueField instanceof JComboBox) {
      if (oValue instanceof Locale) {
        ((JComboBox) guiValueField).setSelectedItem(((Locale) oValue).getLanguage());
      } else {
        ((JComboBox) guiValueField).setSelectedItem((String) oValue);
      }
    } else {
      logger.error("loadValue: putGuiValue oBject type not recognised for " + guiLabelField); Helper.statsErrors++;
      return;
    }

  }

  /**
   * Method that gets the correct profile value
   *
   * @return
   */
  public Object getProfileValue() {
    Method profileGetMethod = getProfileGetMethod();
    if (profileGetMethod == null) {
      return null;
    }
    Object profileValue;
    try {
      // Invoke the get method
      profileValue =  profileGetMethod.invoke(ConfigurationManager.getCurrentProfile());
    } catch (Exception e) {
      logger.error("loadValue:  Invoke 'get" + methodBase + "' Exception " + e); Helper.statsErrors++;
      profileValue = null;
    }
    return profileValue;
  }

  /**
   * Store a value in the profile
   *
   * @param setValue
   */
  private void putProfileValue(Object setValue) {
    Method profileSetMethod = getProfileSetMethod();
    if (profileSetMethod != null) {
      try {
        profileSetMethod.invoke(ConfigurationManager.getCurrentProfile(), setValue);
        // None of the following should happen except in development - but lets play safe!
      } catch (final Exception e) {
        logger.error("storeValue: invoke 'set" + methodBase + "Exception " + e); Helper.statsErrors++;
      }
    }
  }

  /**
   * Transfer a value from the GUI to the profile
   * Do nothing if field has no associated value field or method
   */
  public void storeValue() {
    if (Helper.isNullOrEmpty(methodBase)) {
      // logger.trace("storeValue: Method baee name to store value not set");
      return;
    }
    if (Helper.isNullOrEmpty(guiValueField)) {
      // logger.trace("storeValue: Attempt to store a null value");
      return;
    }
    // Now we can set up the parameter for the setmethod
    Object setValue = getGuiValue();
    if (setValue == null && ! (profileType instanceof File)) {
      return;
    }
    putProfileValue(setValue);
  }

  /**
   * Transfer a value from the profile to the GUI
   * Do nothing if field has no associated value field or method
   */
  public void loadValue() {
    // Check if there is a GUI field to store any value
    if (Helper.isNullOrEmpty(guiValueField)) {
      return;
    }
    // Check if we have a profile method base specified
    if (Helper.isNullOrEmpty(methodBase)) {
      return;
    }
    // Check we can actually find the method
    Method method = getProfileGetMethod();;
    if (method == null) {
      return;
    }
    // Get the value from the profile
    // Only time null is valid for File objects
    Object loadValue = getProfileValue();
    putGuiValue(loadValue);

    // Now use ReadOnly method to see if field should be currently dsiabled
    // If no such method eists, then assume is always enabled
    Boolean disable;
    try {
      String methodName = "is" + methodBase + "ReadOnly";
      method = currentConfiguration.getClass().getDeclaredMethod(methodName);
      disable = (Boolean) method.invoke(currentConfiguration);
    } catch (final Exception e) {
      // We assume ReadOnly method does not exist if we get to this point.
      disable = false;
    }
    if (guiLabelField != null) guiLabelField.setEnabled(!disable);
    if (guiValueField != null) guiValueField.setEnabled(!disable);
  }


  /**
   * Get the method for getting a value from the profile
   *
   * If successful will also set up the global variables
   * paramTypes[]
   * profileType
   * This means you MUST have found getProfileGetMethod() before using these
   *
   * @return  method if OK, and null otherwise
   */
  private Method getProfileGetMethod() {
    Method profileGetMethod = null;
    String getMethodName = "get" + methodBase;
    try {
      // Use get method to work out parameter type for set method
      profileGetMethod = currentConfiguration.getClass().getDeclaredMethod(getMethodName);
      paramTypes[0] = profileGetMethod.getReturnType();
      profileType = profileGetMethod.getReturnType();
    } catch (NoSuchMethodException e1) {
      logger.error("getProfileGetMethod:  unable to find method '" + getMethodName + "'"); Helper.statsErrors++;
      profileGetMethod = null;
      paramTypes = null;
      profileType = null;
    }
    return profileGetMethod;
  }


  /**
   * Get the method for storing values in the profile.
   *
   * @return  method if successful, null otherwise
   */
  private Method getProfileSetMethod() {
    String setMethodName = "set" + methodBase;
    Method profileSetMethod = null;
    try {
      profileSetMethod = currentConfiguration.getClass().getDeclaredMethod(setMethodName, paramTypes);
    } catch (NoSuchMethodException e1) {
      logger.error("getProfileSetMethod:  unable to find method '" + setMethodName + "'"); Helper.statsErrors++;
      profileSetMethod = null;
    }
    return profileSetMethod;
  }
}
