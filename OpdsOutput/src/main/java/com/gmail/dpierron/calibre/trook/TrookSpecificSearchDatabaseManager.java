package com.gmail.dpierron.calibre.trook;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.datamodel.Author;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Series;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.JDOM;

public enum TrookSpecificSearchDatabaseManager
{
  INSTANCE;

  private final int MIN_KEYWORD_LEN = 3;
  
  private File databaseFile = null;
  private Connection connection = null;
  private Map<String, Long> keywords = new HashMap<String, Long>();
  private List<Book> storedBooks= new Vector<Book>();
  private List<Author> storedAuthors = new Vector<Author>();
  private List<Series> storedSeries= new Vector<Series>();
  private List<Tag> storedTags= new Vector<Tag>();
  private long keywordCounter = 0;
  private long resultCounter = 0;

  private static final Logger logger = Logger.getLogger(TrookSpecificSearchDatabaseManager.class);

  public void setDatabaseFile(File databaseFile) {
    this.databaseFile = databaseFile;
  }

  public File getDatabaseFile() {
    return databaseFile;
  }
  
  public Connection getConnection() {
    if (connection == null) {
      initConnection();
    }
    return connection;
  }
  
  public boolean databaseExists() {
    if (databaseFile == null)
      return false;
    if (!databaseFile.exists())
      return false;
    
    return true;
  }
  
  private void initConnection() {
    try {
      Class.forName("org.sqlite.JDBC");
      if (databaseFile == null) 
        return;
      if (databaseExists())
        databaseFile.delete();
      String url = databaseFile.toURI().getPath();
      connection = DriverManager.getConnection("jdbc:sqlite:"+url);
      Statement stat = connection.createStatement();
      // drop the indexes
      stat.executeUpdate("DROP INDEX IF EXISTS KEYWORDS_RESULTS_RELATION_INDEX_ON_RESULTS;");
      stat.executeUpdate("DROP INDEX IF EXISTS KEYWORDS_RESULTS_RELATION_INDEX_ON_KEYWORD;");
      stat.executeUpdate("DROP INDEX IF EXISTS RESULTS_INDEX;");
      stat.executeUpdate("DROP INDEX IF EXISTS KEYWORDS_INDEX;");
      // drop the tables
      stat.executeUpdate("DROP TABLE IF EXISTS keywords_results_relation");
      stat.executeUpdate("DROP TABLE IF EXISTS results");
      stat.executeUpdate("DROP TABLE IF EXISTS keywords");
      // create the tables
      stat.executeUpdate("CREATE TABLE keywords (keyword_id INTEGER PRIMARY KEY, keyword_value TEXT);");
      stat.executeUpdate("CREATE TABLE results (result_id INTEGER PRIMARY KEY, result_type TEXT, result_entry TEXT);");
      stat.executeUpdate("CREATE TABLE keywords_results_relation (keyword_id INTEGER, result_id INTEGER);");
      // create the indexes
      stat.executeUpdate("CREATE UNIQUE INDEX KEYWORDS_INDEX ON keywords(keyword_id ASC);");
      stat.executeUpdate("CREATE UNIQUE INDEX RESULTS_INDEX ON results(result_id ASC);");
      stat.executeUpdate("CREATE INDEX KEYWORDS_RESULTS_RELATION_INDEX_ON_KEYWORD ON keywords_results_relation(keyword_id ASC);");
      stat.executeUpdate("CREATE INDEX KEYWORDS_RESULTS_RELATION_INDEX_ON_RESULTS ON keywords_results_relation(result_id ASC);");
    } catch (ClassNotFoundException e) {
      logger.error(e);
    } catch (SQLException e) {
      logger.error(e);
    }
  }
  
  private long addResult(String opdsEntry, ResultType type) throws SQLException {
    PreparedStatement ps = getConnection().prepareStatement("INSERT INTO results(result_id, result_type, result_entry) values (?, ?, ?);");
    ps.setLong(1, ++resultCounter);
    ps.setString(2, type.name().toUpperCase(Locale.ENGLISH));
    ps.setString(3, opdsEntry);
    ps.execute();
    return resultCounter;
  }
  
  private long addKeyword(String keyword) throws SQLException {
    // check if keyword already exist (we test in memory to be quicker)
    if (keywords.containsKey(keyword))
      return keywords.get(keyword);
    else {
      PreparedStatement ps = getConnection().prepareStatement("INSERT INTO keywords(keyword_id, keyword_value) values (?, ?);");
      ps.setLong(1, ++keywordCounter);
      ps.setString(2, keyword);
      ps.execute();
      keywords.put(keyword, keywordCounter);
    }
    return keywordCounter;
  }
  
  private void addKeywordResultRelation(long keywordId, long resultId) throws SQLException {
    PreparedStatement ps = getConnection().prepareStatement("INSERT INTO keywords_results_relation(keyword_id, result_id) values (?, ?);");
    ps.setLong(1, keywordId);
    ps.setLong(2, resultId);
    ps.execute();
  }

  private List<String> keywordize(String pKeywordString) {
    List<String> keywords = new Vector<String>();
    List<String> result = new Vector<String>();
    String keywordString = pKeywordString.toUpperCase(Locale.ENGLISH);
    StringBuffer currentKeyword = null;
    for (int pos = 0; pos < keywordString.length(); pos++) {
      char c = keywordString.charAt(pos);
      if (!Character.isLetterOrDigit(c)) {
        // skip and break keywords at non letter or digits
        if (currentKeyword != null) {
          // process current keyword
          keywords.add(currentKeyword.toString());
          currentKeyword = null;
        }
        continue;
      }
      if (currentKeyword == null)
        currentKeyword = new StringBuffer();
      currentKeyword.append(c);
    }

    // process the last keyword
    if (currentKeyword != null) {
      keywords.add(currentKeyword.toString());
      currentKeyword = null;
    }
    
    // make packages with keywords, in increasing size
    for(int size=1;size<=keywords.size();size++) {
      int pos = 0;
      while (pos + size <= keywords.size()) {
        StringBuffer keywordPackage = new StringBuffer();
        for (int i=pos; i<pos+size; i++) {
          keywordPackage.append(keywords.get(i));
          keywordPackage.append(' ');
        }
        String s = keywordPackage.toString();
        result.add(s.substring(0, s.length() - 1));
        pos++;
      }
    }
    
    // add the "whole string" keyword
    result.add(keywordString);
    
    return result;
  }
  
  public void addEntry(String opdsEntry, ResultType type, String... keywordStrings) throws SQLException {
    // add the result
    long resultId = addResult(opdsEntry, type);
    
    for (String keywordString : keywordStrings) {
      // process the keyword string
      List<String> keywords = keywordize(keywordString);
      for (String keyword : keywords) {
        if (keyword.length() >= MIN_KEYWORD_LEN) {
          long keywordId = addKeyword(keyword);
          addKeywordResultRelation(keywordId, resultId);
        }
      }
    }
  }

  private void addEntryToTrookSearchDatabase(Element entry, ResultType type, String... keywords) {
    try {
      StringWriter sw = new StringWriter();
      JDOM.INSTANCE.getSerializer().output(entry, sw);
      String opdsEntry = sw.getBuffer().toString();
      TrookSpecificSearchDatabaseManager.INSTANCE.addEntry(opdsEntry, type, keywords);
    } catch (IOException e) {
      logger.warn(e.getMessage());
    } catch (SQLException e) {
      logger.warn(e.getMessage());
    }
  }

  public void addAuthor(Author item, Element entry) {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode() == DeviceMode.Nook) {
      if (!storedAuthors.contains(item)) {
        logger.debug("adding result for "+item);
        String keywords = item.getName();
        addEntryToTrookSearchDatabase(entry, ResultType.AUTHOR, keywords);
        storedAuthors.add(item);
      }
    }
  }

  public void addSeries(Series item, Element entry) {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode() == DeviceMode.Nook) {
      if (!storedSeries.contains(item)) {
        logger.debug("adding result for "+item);
        String keywords = item.getName();
        // TODO do this after Doug has added support for noise words filtering in series 
        // String noNoise = item.getTitleForSort(ConfigurationManager.INSTANCE.getCurrentProfile().getBookLanguageTag());
        addEntryToTrookSearchDatabase(entry, ResultType.SERIES, keywords);
        storedSeries.add(item);
      }
    }
  }

  public void addTag(Tag item, Element entry) {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode() == DeviceMode.Nook) {
      if (!storedTags.contains(item)) {
        logger.debug("adding result for "+item);
        String keywords = item.getName();
        addEntryToTrookSearchDatabase(entry, ResultType.TAG, keywords);
        storedTags.add(item);
      }
    }
  }

  public void addBook(Book item, Element entry) {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getDeviceMode() == DeviceMode.Nook) {
      if (!storedBooks.contains(item)) {
        logger.debug("adding result for "+item);
        String keywords = item.getTitle();
        String noNoise = item.getTitleForSort(ConfigurationManager.INSTANCE.getCurrentProfile().getBookLanguageTag());
        addEntryToTrookSearchDatabase(entry, ResultType.BOOK, keywords, noNoise);
        storedBooks.add(item);
      }
    }
  }
}
