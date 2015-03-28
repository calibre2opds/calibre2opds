package com.gmail.dpierron.calibre.opds.indexer;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.CatalogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * all the algorithms needed to index the model objects are stored here
 */
public class IndexManager {
  private final static Logger logger = Logger.getLogger(IndexManager.class);
  private static Index index = new Index();

  private IndexManager() {
  }

  public void reset () {
    index = new Index();
  }

  public static void indexBook(Book book, String url, String thumbnailUrl) {
    index.indexBook(book, url, thumbnailUrl);
  }

  public static long size() {
    return index.size();
  }

  private static void exportToJavascript(Index pIndex, String name) throws IOException {
  }
 /*
  public static void exportToJavascript() throws IOException {
    Index index2 = index.filterIndex(ConfigurationManager.getCurrentProfile().getMaxKeywords(),
        ConfigurationManager.getCurrentProfile().getIndexFilterAlgorithm());
    File searchFolder = new File(CatalogManager.getGenerateFolder(), "_search");
    searchFolder.mkdirs();
    index2.exportToJavascript(searchFolder);
  }

  public static void exportToJSON() throws IOException {
    Index index2 = index.filterIndex(ConfigurationManager.getCurrentProfile().getMaxKeywords(),
        ConfigurationManager.getCurrentProfile().getIndexFilterAlgorithm());
    File searchFolder = new File(CatalogManager.getGenerateFolder(), "_search");
    searchFolder.mkdirs();
    index2.exportToJSON(searchFolder);
  }
*/
  /**
   * Export search information to Javascript arrays in files
   * @throws IOException
   */
  public static void exportToJavascriptArrays() throws IOException {
    Index index2 = index.filterIndex(ConfigurationManager.getCurrentProfile().getMaxKeywords(),
        ConfigurationManager.getCurrentProfile().getIndexFilterAlgorithm());
    File searchFolder = new File(CatalogManager.getGenerateFolder(), "_search");
    searchFolder = new File(searchFolder, "database");
    searchFolder.mkdirs();
    index2.exportToJavascriptArrays(searchFolder);
  }
}
