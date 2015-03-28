package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.NoiseWord;
import java.util.Locale;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestNoiseWords {

  @Test
  public void testRemoveLeadingNoiseWords() {

    NoiseWord noiseWord;
    String withoutNoise;

    // Test ENGLSH
    noiseWord = DataModel.getNoiseword("en");        // Using 2-character ISO code
    withoutNoise = noiseWord.removeLeadingNoiseWords("the cat who liked girls");
    assertTrue("cat who liked girls".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the a an cat who liked girls");
    assertTrue("cat who liked girls" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the un cat who liked girls");
    assertTrue("un cat who liked girls" .equals(withoutNoise));
    noiseWord.setRemovewords(false);       // Test now with  moving to end
    withoutNoise = noiseWord.removeLeadingNoiseWords("the un cat who liked girls");
    assertTrue("un cat who liked girls, the".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("the a an cat who liked girls");
    assertTrue("cat who liked girls, the a an" .equals(withoutNoise));

    // Test FRENCH
    noiseWord = DataModel.getNoiseword(Locale.FRENCH);         // Using specific Locale
    withoutNoise = noiseWord.removeLeadingNoiseWords("le chien d'anglais qui aimait les frites");
    assertTrue("chien d'anglais qui aimait les frites".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("la le un anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites".equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("l'anglais qui aimait les frites");
    assertTrue("anglais qui aimait les frites" .equals(withoutNoise));

    // Test GERMAN
    noiseWord = DataModel.getNoiseword("deu");       // Using 3-character ISO code
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein voegel");
    assertTrue("voegel" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein der die das voegel");
    assertTrue("voegel" .equals(withoutNoise));
    withoutNoise = noiseWord.removeLeadingNoiseWords("ein un voegel");
    assertTrue("un voegel" .equals(withoutNoise));

    // TODO:  Add other languages when approprate localizstions provided
  }

}
