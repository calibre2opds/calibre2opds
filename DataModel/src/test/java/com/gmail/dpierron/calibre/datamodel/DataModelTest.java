package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.configuration.ReadOnlyConfigurationInterface;
import com.gmail.dpierron.calibre.datamodel.test.TestDataModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DataModelTest {

  @Test
  public void testDataModel() {
    ReadOnlyConfigurationInterface conf = new ReadOnlyConfigurationInterface() {
      public File getDatabaseFolder() {
        String fileName = DataModelTest.class.getResource("../metadata.db").getFile();
        File file = new File(fileName).getParentFile();
        System.out.println("DataModelTest.testDataModel using database folder : " + file);
        return file;
      }
    };
    Configuration.setConfiguration(conf);
    new TestDataModel().testDataModel(false);
  }

  class FakeBook extends GenericDataObject implements SplitableByLetter {
    String title;

    FakeBook(String title) {
      this.title = title;
    }
    public String getColumnName () {
      return "book";
    }
    public ColumType getColumnType() {
      return ColumType.COLUMN_BOOK;
    }
    public String getSortName() {
      return title;
    }
    public String getDisplayName() {
      return title;
    }
    public String getTextToSort() {
      return title;
    }
    public String getTextToDisplay() {
      return title;
    }
  }
/**
  Comparator<FakeBook> comparator = new Comparator<FakeBook>() {

    public int compare(FakeBook o1, FakeBook o2) {
      String s1 = (o1 == null ? "" : o1.getTitleToSplitByLetter());
      String s2 = (o2 == null ? "" : o2.getTitleToSplitByLetter());
      return s1.compareTo(s2);
    }
  };
*/
  @Test
  public void testSplitByLetter() throws Exception {
    int expected[] = new int[]{2, 18, 3, 12, 23};

    BufferedReader r = null;
    try {
      r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("russian.txt"), "utf-8"));
      List<FakeBook> books = new LinkedList<FakeBook>();
      List<String> lines = new LinkedList<String>();
      String line;
      while ((line = r.readLine()) != null) {
        lines.add(line);
        books.add(new FakeBook(line));
      }
//      Map<String, List<FakeBook>> result = DataModel.splitObjectsByLetter(books);
      Map<String, List<FakeBook>> result = GenericDataObject.splitByLetter(books);
      for (int i = 0; i < 5; i++) {
        String letter = lines.get(i).substring(0, 1).toUpperCase();
        List<FakeBook> listOfBooksByLetter = result.get(letter);
        Assert.assertEquals(expected[i], listOfBooksByLetter.size());
        for (FakeBook book : listOfBooksByLetter) {
          Assert.assertEquals(book.getTextToSort().substring(0, 1).toUpperCase(), letter);
        }
      }
    } finally {
      if (r != null)
        r.close();
    }
  }

  @Test
  public void testSplit() throws Exception {
    String[] names = new String[] {"Morrisson", "More", "Morris", "Maman", "Papa", "pépé"};
    List<FakeBook> books = new LinkedList<FakeBook>();
    for (String line : names) {
      books.add(new FakeBook(line));
    }
//    Map<String, List<FakeBook>> result = DataModel.splitObjectsByLetter(books);
    Map<String, List<FakeBook>> result = GenericDataObject.splitByLetter(books);
    Assert.assertNotNull(result);
    //for (int i = 0; i < 5; i++) {
    //  String letter = lines.get(i).substring(0, 1).toUpperCase();
    //  List<FakeBook> listOfBooksByLetter = result.get(letter);
    //  Assert.assertEquals(expected[i], listOfBooksByLetter.size());
    //  for (FakeBook book : listOfBooksByLetter) {
    //    Assert.assertEquals(book.getTitleToSplitByLetter().substring(0, 1).toUpperCase(), letter);
    //  }
    //}
  }
}
