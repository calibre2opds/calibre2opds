package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.configuration.ReadOnlyConfigurationInterface;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import junit.framework.Assert;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TestCalibreQueryInterpreter {

  @BeforeClass
  public static void setUp() {
    Logger rootLogger = Logger.getRootLogger();
    if (!rootLogger.getAllAppenders().hasMoreElements()) {
      rootLogger.setLevel(Level.INFO);
      rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));

      // The TTCC_CONVERSION_PATTERN contains more info than
      // the pattern we used for the root logger
      Logger thisLogger = rootLogger.getLoggerRepository().getLogger(TestCalibreQueryInterpreter.class.getCanonicalName());
      thisLogger.setLevel(Level.DEBUG);
      thisLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
    }

  }

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
    {
      // test iso2 language query
      final String CALIBRE_QUERY = "languages:\"=Fr\" and rating:\">2\"";

      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(25, books.size());
    }
    {
      // test iso3 language query
      final String CALIBRE_QUERY = "languages:\"=Eng\" and rating:\">2\"";

      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(64, books.size());
    }
    {
      // test english name language query
      final String CALIBRE_QUERY = "languages:\"=French\" and rating:\">2\"";

      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(25, books.size());
    }
    {
      // test "tag contains" query
      final String CALIBRE_QUERY = "tags:\"Temp:\" and rating:\">2\"";

      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      // test mixed case keywords
      final String CALIBRE_QUERY = "Tags:\"=Temp:AddToDemoCatalog\" And RATING:\">2\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.INSTANCE.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
  }
}
