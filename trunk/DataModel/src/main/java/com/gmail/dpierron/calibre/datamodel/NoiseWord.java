package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.*;

public enum NoiseWord {
  EN("eng", "the ", "a ", "an "),
  FR("fra", "le ", "la ", "les ", "l'", "un ", "une ", "du ", "de ", "la ", "des "),
  DE("deu", "der ", "die ", "das ", "ein ", "eine ");

  private List<String> noiseWords;
  private String lang;
  private static Map<String, NoiseWord> map;
  public static NoiseWord DEFAULT = EN;

  private NoiseWord(String lang, String... words) {
    List<String> temp = Arrays.asList(words);
    this.noiseWords = new LinkedList<String>(Helper.transformList(temp, "toUpperCase"));
    this.lang = lang;
    addToMap();
  }

  public String getLang() {
    return lang;
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
      return fromLanguage(lang.getCalibreCode());
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
