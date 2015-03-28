package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.configuration.ReadOnlyConfigurationInterface;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.datamodel.filter.PassthroughFilter;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException;

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
      Logger logger = rootLogger.getLoggerRepository().getLogger(TestCalibreQueryInterpreter.class.getCanonicalName());
      logger.setLevel(Level.TRACE);
      logger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));

      // The TTCC_CONVERSION_PATTERN contains more info than
      // the pattern we used for the root logger
      logger = rootLogger.getLoggerRepository().getLogger(CalibreQueryInterpreter.class.getCanonicalName());
      logger.setLevel(Level.TRACE);
      logger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
    }

    ReadOnlyConfigurationInterface conf = new ReadOnlyConfigurationInterface() {
      public File getDatabaseFolder() {
        String fileName = TestCalibreQueryInterpreter.class.getResource("metadata.db").getFile();
        File file = new File(fileName).getParentFile();
        System.out.println("DataModelTest.testDataModel using database folder : " + file);
        return file;
      }
    };
    Configuration.setConfiguration(conf);
  }

  @Test
  public void testInterpret() throws Exception {
    {
      // Base test
      BookFilter bf = new PassthroughFilter();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(945, books.size());
    }

    /* crash on unknown filter */
    {
      final String CALIBRE_QUERY = "tags:\"=State:ToRead\" and not (soussisson:\"=moncopain\")";
      try {
        BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
        Assert.fail("should have catched an exception here");
      } catch (CalibreSavedSearchInterpretException e) {
        // it's ok
      }
    }

    /* Multiple filters tests */

    {
      final String CALIBRE_QUERY = "tags:\"=State:ToRead\" and not (tags:\"=Length:SHORT\")";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(725, books.size());
    }
    {
      final String CALIBRE_QUERY = "tags:\"=Temp:AddToDemoCatalog\" and rating:\">2\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      final String CALIBRE_QUERY = "rating:\">2\" and tags:\"=Temp:AddToDemoCatalog\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      final String CALIBRE_QUERY = "rating:>2 and tags:\"=Temp:AddToDemoCatalog\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      final String CALIBRE_QUERY =
          "not tags:\"=Interest:1\" and not tags:\"=Interest:2\" and tags:\"=State:ToRead\"  and not tags:\"=Length:SHORT\" and languages:\"=French\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(170, books.size());
    }
  }

  @Test
  public void testPublisher() throws CalibreSavedSearchInterpretException {/* Publisher */
    {
      // test publisher:true
      final String CALIBRE_QUERY = "publisher:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(866, books.size());
    }
    {
      // test publisher:false
      final String CALIBRE_QUERY = "publisher:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(79, books.size());
    }
    {
      // "equals" query
      final String CALIBRE_QUERY = "publisher:\"=Editions Flammarion\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(2, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "puBLiSher:\"=Editions Flammarion\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(2, books.size());
    }
    {
      // "contains" query
      final String CALIBRE_QUERY = "publisher:\"Flammarion\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(3, books.size());
    }
  }

  @Test
  public void testFormats() throws CalibreSavedSearchInterpretException {/* Formats */
    {
      // test formats:true
      final String CALIBRE_QUERY = "formats:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(945, books.size());
    }
    {
      // test formats:false
      final String CALIBRE_QUERY = "formats:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(0, books.size());
    }
    {
      // "equals" query
      final String CALIBRE_QUERY = "formats:\"=TXT\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "foRMats:\"=EPUB\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(940, books.size());
    }
    {
      // "contains" query
      final String CALIBRE_QUERY = "formats:\"PUB\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(940, books.size());
    }
  }

  @Test
  public void testSeries() throws CalibreSavedSearchInterpretException {/* Series */
    {
      // test series:true
      final String CALIBRE_QUERY = "series:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(470, books.size());
    }
    {
      // test series:false
      final String CALIBRE_QUERY = "series:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(475, books.size());
    }
    {
      // "equals" query
      final String CALIBRE_QUERY = "series:\"=The Lost Fleet\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "sERiEs:\"=The Lost Fleet\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(5, books.size());
    }
    {
      // "contains" query
      final String CALIBRE_QUERY = "series:\"Fleet\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(6, books.size());
    }
  }

  @Test
  public void testAuthors() throws CalibreSavedSearchInterpretException {/* Authors */
    {
      // test authors:true
      final String CALIBRE_QUERY = "authors:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(945, books.size());
    }
    {
      // test authors:false
      final String CALIBRE_QUERY = "authors:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(0, books.size());
    }
    {
      // "equals" query
      final String CALIBRE_QUERY = "authors:\"=Isaac Asimov\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(29, books.size());
    }
    {
      // "equals" query with author sort
      final String CALIBRE_QUERY = "authors:\"=Asimov, Isaac\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(29, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "AUTHors:\"=Isaac Asimov\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(29, books.size());
    }
    {
      // "contains" query
      final String CALIBRE_QUERY = "authors:\"Isaac\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(29, books.size());
    }
    {
      // "contains" query with author sort
      final String CALIBRE_QUERY = "authors:\"Asimov\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(29, books.size());
    }
  }

  @Test
  public void testRatings() throws CalibreSavedSearchInterpretException {/* Ratings */
    {
      // test rating:true
      final String CALIBRE_QUERY = "rating:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(127, books.size());
    }
    {
      // test rating:false
      final String CALIBRE_QUERY = "rating:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(818, books.size());
    }
    {
      // test rating:4
      final String CALIBRE_QUERY = "rating:4";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(50, books.size());
    }
    {
      // test rating:=4
      final String CALIBRE_QUERY = "rating:=4";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(50, books.size());
    }
    {
      // test rating:"=4"
      final String CALIBRE_QUERY = "rating:\"=4\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(50, books.size());
    }
    {
      // test rating:>4
      final String CALIBRE_QUERY = "rating:>4";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(6, books.size());
    }
    {
      // test rating:">4"
      final String CALIBRE_QUERY = "rating:\">4\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(6, books.size());
    }
    {
      // test rating:"<4"
      final String CALIBRE_QUERY = "rating:\"<4\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(889, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "rATiNg:<4";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(889, books.size());
    }
  }

  @Test
  public void testLanguages() throws CalibreSavedSearchInterpretException {/* Languages */
    {
      // test languages:true
      final String CALIBRE_QUERY = "languages:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(937, books.size());
    }
    {
      // test languages:false
      final String CALIBRE_QUERY = "languages:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(8, books.size());
    }
    {
      // test iso2 language query
      final String CALIBRE_QUERY = "languages:\"=Fr\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(708, books.size());
    }
    {
      // test iso3 language query
      final String CALIBRE_QUERY = "languages:\"=Eng\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(229, books.size());
    }
    {
      // test english name language query
      final String CALIBRE_QUERY = "languages:\"=French\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(708, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "lAngUAGes:\"=French\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(708, books.size());
    }
    // no "contains" query for languages !
  }

  @Test
  public void testTags() throws CalibreSavedSearchInterpretException {/* Tags */
    {
      // test tags:true
      final String CALIBRE_QUERY = "tags:true";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(945, books.size());
    }
    {
      // test tags:false
      final String CALIBRE_QUERY = "tags:false";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(0, books.size());
    }
    {
      // "equals" query
      final String CALIBRE_QUERY = "tags:\"=State:ToRead\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(746, books.size());
    }
    {
      // mix case
      final String CALIBRE_QUERY = "tAGs:\"=State:ToRead\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(746, books.size());
    }
    {
      // "contains" query
      final String CALIBRE_QUERY = "tags:\"State:\"";
      BookFilter bf = new CalibreQueryInterpreter(CALIBRE_QUERY).interpret();
      List<Book> books = FilterHelper.filter(bf, DataModel.getListOfBooks());
      Assert.assertEquals(929, books.size());
    }
  }
}
