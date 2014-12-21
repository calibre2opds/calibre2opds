package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.i18n.Localization;

import java.util.*;

public enum NoiseWord {
  EN("eng", "the ", "a ", "an "),
  FR("fra", "le ", "la ", "les ", "l'", "un ", "une ", "du ", "de ", "la ", "des "),
  DE("deu", "der ", "die ", "das ", "ein ", "eine ");

  private final List<String> noiseWords;
  private final String lang;
  private static Map<String, NoiseWord> map;
  private static final NoiseWord DEFAULT = EN;

  private NoiseWord(String lang, String... words) {
    String langnoiseWords = Localization.Main.getText("i18n.noiseWords");
    List<String> temp = Arrays.asList(words);
    this.noiseWords = new LinkedList<String>();
    for (String s : temp) {
      noiseWords.add(s.toUpperCase(Locale.ENGLISH));
    }
    this.lang = lang;
    addToMap();
  }

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
