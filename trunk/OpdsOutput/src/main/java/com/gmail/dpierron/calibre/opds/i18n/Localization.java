package com.gmail.dpierron.calibre.opds.i18n;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.Utf8ResourceBundle;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Manages the localization of the messages in an application
 */
public enum Localization {
  Enum("Enumerations"), Main("Localization");

  Logger logger = Logger.getLogger(Localization.class);

  private boolean initialized = false;

  /**
   * the name of the resource bundle properties file used by this localization
   * resource
   */
  private String localizationBundleName;

  /**
   * the resource bundle used by this localization resource
   */
  private ResourceBundle localizations;

  /**
   * english localizations
   */
  private ResourceBundle englishLocalizations;

  public ResourceBundle getBundle() {
    if (localizations == null)
      reloadLocalizations();
    return localizations;
  }

  public ResourceBundle getEnglishBundle() {
    if (englishLocalizations == null)
      reloadLocalizations();
    return englishLocalizations;
  }

  public ResourceBundle getBundle(String language) {
    if (localizations == null)
      reloadLocalizations();
    return localizations;
  }

  /**
   * Constructs a new {@link Localization}
   *
   * @param localizationBundleName the properties files to load
   */
  private Localization(String localizationBundleName) {
    this.localizationBundleName = localizationBundleName;
    reloadLocalizations();
  }

  public Vector<String> getAvailableLocalizations() {
    Vector<String> result = new Vector<String>();
    for (String lang : Locale.getISOLanguages()) {
      Locale locale = new Locale(lang);
      ResourceBundle bundle = getResourceBundle(localizationBundleName, locale, false);
      if (bundle != null) {
        if (bundle.getLocale().getLanguage().equalsIgnoreCase(lang))
          result.add(locale.getLanguage());
      }
    }
    return result;
  }

  private ResourceBundle getResourceBundle(String name) {
    return getResourceBundle(name, null);
  }

  private ResourceBundle getResourceBundle(String name, Locale locale, boolean englishIfNull) {
    ResourceBundle result = null;
    try {
      if (locale != null)
        result = Utf8ResourceBundle.getBundle(name, locale);
      else
        result = Utf8ResourceBundle.getBundle(name);
    } catch (Exception e) {
      // do nothing
    }
    if (result == null && englishIfNull)
      return getResourceBundle(name, Locale.ENGLISH, false); // English is always there
    return result;
  }

  private ResourceBundle getResourceBundle(String name, Locale locale) {
    return getResourceBundle(name, locale, true);
  }

  public void reloadLocalizations() {
    reloadLocalizations(ConfigurationManager.INSTANCE.getCurrentProfile().getLanguage());
  }

  /**
   * forces the resource bundle to reload from the properties file (use when the
   * locale changes)
   *
   * @throws IOException
   */
  public void reloadLocalizations(String language) {
    if (Helper.isNullOrEmpty(language)) {
      localizations = getResourceBundle(localizationBundleName);
    } else {
      localizations = getResourceBundle(localizationBundleName, new Locale(language));
    }
    englishLocalizations = getResourceBundle(localizationBundleName, Locale.ENGLISH);
    initialized = true;
  }

  private String lookupText(String key) {
    try {
      return getBundle().getString(key);
    } catch (MissingResourceException e) {
      // try english
      try {
      return getEnglishBundle().getString(key);
      } catch (MissingResourceException ee) {
        return key;
      }
    }
  }

  /**
   * fetches a localized, zero-parameter message
   *
   * @param key the key in the resource bundle
   * @return the message, or the key if the message does not exist
   */
  public String getText(String key) {
    String message = lookupText(key);
    return message;
  }

  /**
   * fetches a localized message with parameters
   *
   * @param key        the key in the resource bundle
   * @param parameters the parameters used to construct the message
   * @return the message
   */
  public String getText(String key, Object... parameters) {
    String message = lookupText(key);
    if (message == null)
      return null;
    String formattedMessage = MessageFormat.format(message, parameters);
    return formattedMessage;
  }

  public boolean isInitialized() {
    return initialized;
  }

}
