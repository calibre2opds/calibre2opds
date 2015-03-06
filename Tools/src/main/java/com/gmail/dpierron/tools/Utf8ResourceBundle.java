package com.gmail.dpierron.tools;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public abstract class Utf8ResourceBundle {

  public static final ResourceBundle getBundle(String baseName) {
    ResourceBundle bundle = ResourceBundle.getBundle(baseName);
    return createUtf8PropertyResourceBundle(bundle);
  }

  public static final ResourceBundle getBundle(String baseName, Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
    // return createUtf8PropertyResourceBundle(bundle);
    return (bundle.getLocale().equals(locale)) ? createUtf8PropertyResourceBundle(bundle): null;
  }

  public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
    ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, loader);
    return createUtf8PropertyResourceBundle(bundle);
  }

  private static ResourceBundle createUtf8PropertyResourceBundle(ResourceBundle bundle) {
    if (!(bundle instanceof PropertyResourceBundle))
      return bundle;

    return new Utf8PropertyResourceBundle((PropertyResourceBundle) bundle);
  }


}