package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;

import java.util.*;

public class NoiseWord {
//  EN("eng", "the ", "a ", "an "),
//  FR("fra", "le ", "la ", "les ", "l'", "un ", "une ", "du ", "de ", "la ", "des "),
//  DE("deu", "der ", "die ", "das ", "ein ", "eine ");
//    EN("eng"),
//    FR("fra"),
//    DE("deu");

  private List<String> noiseWords;            // List of Noisewords for this class instance
  private String lang;                        // Language code for this class instance
  private Locale locale;                      // Local for this class instance
  private static Map<String, NoiseWord> map;  // Map of all Noiseword instances identified by Language code
  private static NoiseWord DEFAULT;           // The default Noisword class instance
  private Vector<List<String>> allNoiseWords; //
  // private Vector<Map<String, word>> allNoiseWordMaps;

  // CONSTRUCTORS

  /**
   * Constructor that initialise static variables if not already done so
   * This optimises later creating language specific instances of classes,
   * and avoids creating two instances for the same language.
   */
  private NoiseWord() {
    Generic_Initilisation();
  }

  /**
   * Constructor for a language specific instance
   *
   * @param lang
   */
  private NoiseWord(String lang) {
    Locale locale = Helper.getLocaleFromLanguageString(lang);
    Locale_Initialization(locale);
    // Localization.Main.reloadLocalizations(Helper.getLocaleFromLanguageString(lang));
  }

  /**
   * Constructor for a locale specific instance
   *
   * @param l
   */
  private NoiseWord(Locale l) {
    Locale_Initialization(l);
  }

  /**
   * Initialisation that is done for all constructor types
   */
  private void Generic_Initilisation() {
    if (allNoiseWords != null) {
      return;
    }
    DEFAULT = new NoiseWord(Locale.ENGLISH.getLanguage());
  }

  /**
   * Initialisation that is done for a specific language/locale instance
   *
   * @param l
   */
  private void Locale_Initialization(Locale l) {
    Generic_Initilisation();
    String langNoiseWords = Localization.Main.getText(Helper.getLocaleFromLanguageString(lang),"i18n.noiseWords");
    StringTokenizer st = new StringTokenizer(langNoiseWords,",");
    this.noiseWords = new LinkedList<String>();
    while (st.hasMoreTokens()) {
      noiseWords.add(st.nextToken().toUpperCase(Locale.ENGLISH));
    }
    this.lang = lang;
    addToMap();
    Localization.Main.reloadLocalizations();
  }

  // METHODS and PROPERTIES

  private void addToMap() {
    if (map == null)
      map = new HashMap<String, NoiseWord>();
    map.put(lang.toLowerCase(), this);
  }

  public static NoiseWord fromLanguage(Language lang) {
    if (lang == null)
      return DEFAULT;
    else
      return fromLanguage(lang.getIso3());
  }

  public static NoiseWord fromLanguage(String lang) {
    if (lang == null)
      return DEFAULT;
    NoiseWord result = map.get(lang.toLowerCase());
    if (result == null)
      result = DEFAULT;
    return result;
  }

  public String removeLeadingNoiseWords(String s) {
    String result = s;
    boolean found = true;
    while ((result.length() > 0) && found) {
      found = false;
      for (String noiseWord : noiseWords) {
        if (result.toUpperCase().startsWith(noiseWord)) {
          result = result.substring(noiseWord.length());
          found = true;
        }
      }
    }
    return result;
  }

}
