package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.configuration.ReadOnlyConfigurationInterface;
import com.gmail.dpierron.calibre.datamodel.test.TestDataModel;
import org.junit.Test;

import java.io.File;


public class DataModelTest {

  @Test
  public void testDataModel() {
    ReadOnlyConfigurationInterface conf = new ReadOnlyConfigurationInterface() {
      public File getDatabaseFolder() {
        String fileName = DataModelTest.class.getResource("metadata.db").getFile();
        File file = new File(fileName).getParentFile();
        System.out.println("DataModelTest.testDataModel using database folder : " + file);
        return file;
      }
    };
    Configuration.setConfiguration(conf);
    new TestDataModel().testDataModel(false);
  }

}
