package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.datamodel.NoiseWord;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestNoiseWords {

  @Test
  public void testRemoveLeadingNoiseWords() {
    NoiseWord noiseWord = NoiseWord.fromLanguage("fra");
    String withoutNoise = noiseWord.removeLeadingNoiseWords("le chien d'anglais qui aimait les frites");
    assertTrue("chien d'anglais qui aimait les frites" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("la le un anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("l'anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites" .equals(withoutNoise));

    noiseWord = NoiseWord.fromLanguage("eng");
    withoutNoise = noiseWord.removeLeadingNoiseWords("the cat who liked girls");
    assertTrue("cat who liked girls" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the a an cat who liked girls");
    assertTrue("cat who liked girls" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the un cat who liked girls");
    assertTrue("un cat who liked girls" .equals(withoutNoise));

    noiseWord = NoiseWord.fromLanguage("deu");
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein voegel");
    assertTrue("voegel" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein der die das voegel");
    assertTrue("voegel" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein un voegel");
    assertTrue("un voegel" .equals(withoutNoise));

  }

}
