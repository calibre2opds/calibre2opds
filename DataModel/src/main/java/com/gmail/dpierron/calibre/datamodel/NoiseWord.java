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
  private boolean removeWords;                // true = remove words completely, false = move words to end

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
    removeWords = true;
  }


  // METHODS and PROPERTIES

  /**
   *
   * @param b
   */
  public void setRemovewords(boolean b) {
    removeWords = b;
  }
  /**
   * Remove leading noisewords from the start of the provided string.
   * The primary purpose of this method is to allow for sorting titles
   * and series via the 'library ordeer' which ignores leading nosie
   * words.
   *
   * Whether the words are removed entirely or moved to the end receeded
   * by a comma is controlled via the removeWords prperty (defaults to move)
   *
   * @param s   // The String to be examined
   * @return    // Amended string.
   */
  public String removeLeadingNoiseWords(String s) {
    if (noiseWords == null || noiseWords.size() == 0) {
      return s;
    }
    String result = s.toUpperCase(locale);
    boolean found = true;
    int startPos = 0;
    while ( (startPos < s.length()) && found) {
      found = false;
      for (String noiseWord : noiseWords) {
        if (result.substring(startPos).startsWith(noiseWord)) {
          startPos += noiseWord.length();
          found = true;
        }
      }
    }
    result = s.substring(startPos);
    if (! removeWords && startPos > 0) {
      // We want to ignore any trailing space on the noiseword(s)
      if (s.charAt(startPos-1) == ' ')  startPos--;
      // Move noisewords to end
      result += ", " + s.substring(0, startPos);
    }
    return result;
  }

  @Override
  public String toString() {
    return (locale == null ? "Unknown" : locale.getDisplayLanguage())
            + ": "
            + (Helper.isNullOrEmpty(noiseWords) ? "[]" : noiseWords.toString());
  }
}
