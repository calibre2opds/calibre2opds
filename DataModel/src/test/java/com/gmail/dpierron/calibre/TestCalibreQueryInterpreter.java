package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.configuration.ReadOnlyConfigurationInterface;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestCalibreQueryInterpreter {

  @Test
  public void testInterpret() throws Exception {
    ReadOnlyConfigurationInterface conf = new ReadOnlyConfigurationInterface() {
      public File getDatabaseFolder() {
        String fileName = TestCalibreQueryInterpreter.class.getResource("metadata.db").getFile();
        File file = new File(fileName).getParentFile();
        System.out.println("DataModelTest.testDataModel using database folder : " + file);
        return file;
      }
    };
    Configuration.setConfiguration(conf);
    {
      final String CALIBRE_QUERY = "tags:\"=Temp:AddToDemoCatalog\" and rating:\">2\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      final String CALIBRE_QUERY =
          "not tags:\"=Interest:1\" and not tags:\"=Interest:2\" and tags:\"=State:ToRead\"  and not tags:\"=Length:SHORT\" and languages:\"=French\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(170, books.size());
    }
  }
}
