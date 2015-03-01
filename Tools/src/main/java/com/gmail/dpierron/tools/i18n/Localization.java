package com.gmail.dpierron.tools.i18n;

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

  private boolean localization_initialized = false;

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

  /*
   * all localizations.
   * Loaded once to avoid reloads later.
   */
  private Vector<ResourceBundle> allLocalizations;

  /**
   * The name of the last loaded locale
   */
  private Locale lastLocalLanguage = null;
  private Locale profileLanguage = null;
  public void setProfileLanguage(Locale lang) {
    profileLanguage = lang;
  }
  private Locale getProfileLanguage() {
    return profileLanguage == null ? Locale.ENGLISH : profileLanguage;
  }

  /**
   * Get the loaded bundle for the last localization locale used
   * @return
   */
  public ResourceBundle getBundle() {
    if (localizations == null)
      reloadLocalizations();
    return localizations;
  }

  /**
   * Get the bundle for the English locale
   * (we always default to English if a dfferent locale fails)
   * @return
   */
  public ResourceBundle getEnglishBundle() {
    if (englishLocalizations == null)
      reloadLocalizations();
    return englishLocalizations;
  }

  /**
   * Get the bundle for a specific locale
   * NOTE Default loaded locale is not changed
   * @param language
   * @return
   */
  public ResourceBundle getBundle(Locale language) {
     ResourceBundle localizations = null;
    // We always want the English localizations loaded
    if (englishLocalizations == null)    {
      englishLocalizations = getResourceBundle(localizationBundleName, Locale.ENGLISH, true);
    }
    // No need to load english localizations twice!
    assert englishLocalizations != null : "Program Error: English localizations should always have loaded OK";
    if (Helper.isNullOrEmpty(language) || language.equals(Locale.ENGLISH)) {
      localizations = englishLocalizations;
    } else {
      localizations = getResourceBundle(localizationBundleName, language, true);
    }
    return localizations;
  }

  /**
   * Constructs a new {@link Localization}
   * We initally load the English localization as we always want that
   *
   * @param localizationBundleName the properties files to load
   */
  private Localization(String localizationBundleName) {
    this.localizationBundleName = localizationBundleName;
    reloadLocalizations(Locale.ENGLISH);
  }

  // Save results to improve efficency on subsequent calls
  private static Vector<Locale> localAvailableLocalizations = null;
  private static Vector<String> stringAvailableLicalizations = null;

  /**
   * Get the kist of localizations that we have available
   * in the properties file for Calibre2opds.
   *
   * @return
   */
  public Vector<Locale> getAvailableLocalizationsAsLocales() {
    if (Helper.isNullOrEmpty(localAvailableLocalizations)) {
      localAvailableLocalizations = new Vector<Locale>();
      stringAvailableLicalizations = new Vector<String>();
      for (Locale locale : Locale.getAvailableLocales()) {
        ResourceBundle bundle = getResourceBundle(localizationBundleName, locale, false);
        if (bundle != null) {
          if (bundle.getLocale().getLanguage().equals(locale))
            localAvailableLocalizations.add(locale);
            stringAvailableLicalizations.add(locale.getLanguage());
        }
      }
    }
    return localAvailableLocalizations;
  }
  public Vector<String> getAvailableLocalizationsAsStrings() {
    if (stringAvailableLicalizations == null ) getAvailableLocalizationsAsLocales();
    return stringAvailableLicalizations;
  }

  /**
   * Get a given resource bundle.
   *
   * @param name
   * @param locale          Set to the locale we want.
   * @param englishIfNull   Set to true if we should fall back to English if
   *                        the requested bundle cannot be found.
   * @return
   */
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

  /**
   * Reloads the localizations according to the language set in Configuration manager                        Helper
   */
  public void reloadLocalizations() {
    reloadLocalizations(getProfileLanguage());
  }

  /**
   * forces the resource bundle to reload from the properties file
   * (use when the locale changes)
   *
   * @throws IOException
   */
  public void reloadLocalizations(Locale language) {
    // We always want the English localizations loaded
    if (englishLocalizations == null)    {
      englishLocalizations = getResourceBundle(localizationBundleName, Locale.ENGLISH, true);
      lastLocalLanguage = Locale.ENGLISH;
      if (localizations == null)  localizations = englishLocalizations;
    }
    // No need to load english localizations twice!
    assert englishLocalizations != null : "Program Error: English localizations should always have loaded OK";
    if (! language.equals(Locale.ENGLISH)) {
      if (Helper.isNullOrEmpty(language)) {
        localizations = getResourceBundle(localizationBundleName, Locale.ENGLISH, true);
      } else {
        localizations = getResourceBundle(localizationBundleName, language, true);
        lastLocalLanguage = language;
      }
    }
    localization_initialized = true;
  }

  /**
   * Get the text for the current localization that corresponds to the given key.
   * If it cannot be found in the current localization we fall back to English.
   * If it does not exist in English either we simply return the key name.
   *
   * @param key     Key to be used
   * @return        The localized text
   */
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
   * Get the text for the specified locale that corresponds to the given key.
   * If it cannot be found in the specified localewe fall back to English.
   * If it does not exist in English either we simply return the key name.
   * NOTE:  Does not change the default for the currently loaded locale
   *
   * @param locale
   * @param key
   * @return
   */
  private String lookupText(Locale locale, String key) {
    try {
      return getBundle(locale).getString(key);
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
   * fetches a localized message.
   * Optionally parameters can be added to embed in the
   * message.
   *
   * @param key        the key in the resource bundle
   * @param parameters the parameters used to construct the message
   * @return the message
   */
  public String getText(String key, Object... parameters) {
    String message = lookupText(key);
    if (message == null)
      return null;
    if (parameters.length != 0) {
      message = MessageFormat.format(message, parameters);
    }
    return message;
  }

  /**
   * fetches a locale specific message with (optional) parameters
   *
   * @param locale
   * @param key        the key in the resource bundle
   * @param parameters the parameters used to construct the message
   * @return
   */
  public String getText(Locale locale, String key, Object... parameters) {
    String message = lookupText(locale, key);
    if (message == null)
      return null;
    if (parameters.length != 0) {
      message = MessageFormat.format(message, parameters);
    }
    return message;
  }

  /**
   * Check if the localization bundle has been loaded
   * @return
   */
  public boolean isLocalization_initialized() {
    return localization_initialized;
  }
}
