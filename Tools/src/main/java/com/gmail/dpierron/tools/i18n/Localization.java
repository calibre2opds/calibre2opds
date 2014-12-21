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

  /*
   * all localizations.
   * Loaded once to avoid reloads later.
   */
  private Vector<ResourceBundle> allLocalizations;

  /**
   * The name of the last loaded locale
   */
  private String lastLocalLanguage = null;
  private String profileLanguage = null;
  public void setProgileLanguage (String lang) {
    profileLanguage = lang;
  }
  private String getProfileLanguage() {
    return profileLanguage == null ? "en" : profileLanguage;
  }
  
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
      reloadLocalizations(language);
    return localizations;
  }

  /**
   * See if the localization for the given language code is available
   *
   * @param languageCodeIso2    The ISO character code
   * @return                    True if Calibre2Opds has localization for this language, false if not
   */
  public boolean isLocalization (String languageCodeIso2) {
      return getAvailableLocalizations().contains(languageCodeIso2);
  }
  /**
   * Constructs a new {@link Localization}
   * We initally load the English localization as we always want that
   *
   * @param localizationBundleName the properties files to load
   */
  private Localization(String localizationBundleName) {
    this.localizationBundleName = localizationBundleName;
    reloadLocalizations("en");
  }

  // Save results to improve efficency on subsequent calls
  private static Vector<String> availableLocalizations = null;

  public Vector<String> getAvailableLocalizations() {
    if (Helper.isNullOrEmpty(availableLocalizations)) {
      availableLocalizations = new Vector<String>();
      for (String lang : Locale.getISOLanguages()) {
        Locale locale = new Locale(lang);
        ResourceBundle bundle = getResourceBundle(localizationBundleName, locale, false);
        if (bundle != null) {
          if (bundle.getLocale().getLanguage().equalsIgnoreCase(lang))
            availableLocalizations.add(locale.getLanguage());
        }
      }
    }
    return availableLocalizations;
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

  /**
   * Reloads the localizations according to the language set in Configuration manager
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
  public void reloadLocalizations(String language) {
    // We always want the English localizations loaded
    if (englishLocalizations == null)    {
      englishLocalizations = getResourceBundle(localizationBundleName, Locale.ENGLISH);
      lastLocalLanguage = "en";
    }
    // No need to load english localizations twice!
    if (language.equals("en") && englishLocalizations != null)
      localizations = englishLocalizations;
    else {
      if (Helper.isNullOrEmpty(language)) {
        localizations = getResourceBundle(localizationBundleName);
      } else {
        localizations = getResourceBundle(localizationBundleName, new Locale(language));
        lastLocalLanguage = language;
      }
    }

    initialized = true;
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

  /**
   * Check if the localization bundle has been loaded
   * @return
   */
  public boolean isInitialized() {
    return initialized;
  }

}
