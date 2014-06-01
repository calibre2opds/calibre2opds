package com.gmail.dpierron.tools;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Utf8PropertyResourceBundle extends ResourceBundle {
  PropertyResourceBundle bundle;

  Utf8PropertyResourceBundle(PropertyResourceBundle bundle) {
    this.bundle = bundle;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.ResourceBundle#getKeys()
   */
  public Enumeration<String> getKeys() {
    return bundle.getKeys();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
   */
  protected Object handleGetObject(String key) {
    String value = (String) bundle.getString(key);
    if (value == null)
      return null;
    try {
      return new String(value.getBytes("ISO-8859-1"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // Shouldn't fail - but should we still add logging message?
      return null;
    }
  }

  public Locale getLocale() {
    return bundle.getLocale();
  }


}
