package com.gmail.dpierron.calibre;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gmail.dpierron.calibre.datamodel.NoiseWord;

public class TestNoiseWords {

  @Test
  public void testRemoveLeadingNoiseWords() {
    NoiseWord noiseWord = NoiseWord.FRENCH;
    String withoutNoise = noiseWord.removeLeadingNoiseWords("le chien d'anglais qui aimait les frites");
    assertTrue("chien d'anglais qui aimait les frites".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("la le un anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("l'anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites".equals(withoutNoise));

    noiseWord = NoiseWord.fromLanguage("en");
    withoutNoise = noiseWord.removeLeadingNoiseWords("the cat who liked girls");
    assertTrue("cat who liked girls".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the a an cat who liked girls");
    assertTrue("cat who liked girls".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the un cat who liked girls");
    assertTrue("un cat who liked girls".equals(withoutNoise));
    
    noiseWord = NoiseWord.fromLanguage("DE");
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein voegel");
    assertTrue("voegel".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein der die das voegel");
    assertTrue("voegel".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein un voegel");
    assertTrue("un voegel".equals(withoutNoise));
    
  }

}
