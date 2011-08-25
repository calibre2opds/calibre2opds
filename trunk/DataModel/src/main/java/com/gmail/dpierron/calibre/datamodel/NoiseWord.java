package com.gmail.dpierron.calibre.datamodel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.gmail.dpierron.tools.Helper;

public enum NoiseWord {
  ENGLISH("EN", "the ", "a ", "an "),
  FRENCH("FR", "le ", "la ", "les ", "l'", "un ", "une ", "du ", "de ", "la ", "des "),
  GERMAN("DE", "der ", "die ", "das ", "ein ", "eine ");
  
  private List<String> noiseWords;
  private String lang;
  private static Map<String, NoiseWord> map;
  public static NoiseWord DEFAULT = ENGLISH;
  
  private NoiseWord(String lang, String ... words) {
    List<String> temp = Arrays.asList(words);
    this.noiseWords = new Vector<String>(Helper.transformList(temp, "toUpperCase"));
    this.lang = lang;
    addToMap();
  }
  
  public String getLang() {
    return lang;
  }
  
  private void addToMap() {
    if (map == null)
      map = new HashMap<String, NoiseWord>();
    map.put(lang.toUpperCase(), this);
  }
  
  public static NoiseWord fromLanguage(String lang) {
    if (lang == null)
      return DEFAULT;
    NoiseWord result = map.get(lang.toUpperCase());
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
