package com.gmail.dpierron.calibre.datamodel;

/**
 * Class for handling Noisewords at the beginning of a
 * title or series name.   This is highly language
 * dependent so this class may be a bit simplistic.
 *
 * For languages where there are no list of noisewords
 * available this class effectively does nothing.
 */
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;

import java.util.*;

public class NoiseWord {
  private Locale locale;                      // Local for this class instance
  private List<String> noiseWords;            // List of Noisewords for this class instance

  // CONSTRUCTORS

  /**
   * Construct a locale specific NoiseWord object
   *
   * @param locale      // The locale for this instance
   * @param noiseWords  // The list of noise words for this locale
   *                    // If not known provide either null ir an empty list
   */
  public NoiseWord(Locale locale, List<String> noiseWords) {
    this.locale = locale;
    this.noiseWords = noiseWords;
  }


  // METHODS and PROPERTIES

  /**
   * Remove leading noisewords from the start of the provided string
   * and instead append them at the end after a comma.  The primary
   * purpose of this method is to allow for sorting titles and series
   * via the 'library ordeer' which ignores leading nosie words.
   *
   * @param s   // The String to be examined
   * @return    // String amended (if necessary) to place noise words at the end
   */
  public String removeLeadingNoiseWords(String s) {
    if (noiseWords == null || noiseWords.size() == 0) {
      return s;
    }
    String result = s;
    boolean found = true;
    while ((result.length() > 0) && found) {
      found = false;
      for (String noiseWord : noiseWords) {
        if (result.toUpperCase().startsWith(noiseWord)) {
          result = result.substring(noiseWord.length()) + ", " + result.substring(0,noiseWord.length());;
          found = true;
        }
      }
    }
    return result;
  }
}
