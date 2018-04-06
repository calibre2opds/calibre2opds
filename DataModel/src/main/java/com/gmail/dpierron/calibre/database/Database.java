package com.gmail.dpierron.calibre.database;

/**
 * Abstract the basic database operations used by calibre2opds
 * Handles transfering data between database field and calibre2opds variables
 *
 * NOTE:  There should only ever be one instance of this object, so all
 *        global variables and methods are declared static.
 */

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Database {

  private static final Logger logger = LogManager.getLogger(Database.class);
  private static final DateFormat SQLITE_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static int sqlException = 0;    // Set to bit dependent value to allow for multiple different exception points

  /**
   *
   * @return
   */
  public static List<Tag> listTags() {
    List<Tag> result = new LinkedList<Tag>();
    PreparedStatement statement = DatabaseRequest.ALL_TAGS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        result.add(new Tag(set.getString("id"), set.getString("name")));
      }
    } catch (SQLException e) {
      logger.error("ListTag: " + e); Helper.statsErrors++;
      sqlException += (2^0);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static boolean test() {
    PreparedStatement statement = DatabaseRequest.TEST.getStatement();
    try {
      statement.executeQuery();
      return true;
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("test: " + e);
      sqlException += (2^1);
      return false;
    }
  }

  /**
   *
   * @return
   */
  public static Composite<Map<String, Language>, Map<String, Language>> getMapsOfLanguages() {
    Map<String, Language> mapOfLanguagesById = new HashMap<String, Language>();
    Map<String, Language> mapOfLanguagesByIsoCode = new HashMap<String, Language>();
    PreparedStatement statement = DatabaseRequest.ALL_LANGUAGES.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("id");
        String isoCode = set.getString("lang_code");
        Language language = new Language(id, isoCode);
        mapOfLanguagesById.put(id, language);
        mapOfLanguagesByIsoCode.put(isoCode, language);
        if (logger.isDebugEnabled()) {
          logger.debug("language id:"+id+", lang_code:"+isoCode);
          logger.debug("language = "+language);
        }
      }
    } catch (SQLException e) {
      logger.error("getMapsOfLanguages: " + e); Helper.statsErrors++;
      sqlException += (2^2);
    }
    return new Composite<Map<String, Language>, Map<String, Language>>(mapOfLanguagesById, mapOfLanguagesByIsoCode);
  }

  /**
   *
   * @return
   */
  public static List<Book> listBooks() {
    List<Book> result = new LinkedList<Book>();
    PreparedStatement statement = DatabaseRequest.ALL_BOOKS.getStatement();
    PreparedStatement stmtBooksLanguagesLink = DatabaseRequest.BOOKS_LANGUAGES.getStatement();
    String bookId = null;
    int step = 0;     // Brute force way used to help diagnose whichs tement fails (if any) without lots of try/catch statements
    try {
      ResultSet set = null;
      try {
        set = statement.executeQuery();
      } catch (SQLException e) {
        logger.error("listBooks: statement=" + statement + "\n" + e); Helper.statsErrors++;
        sqlException += (2^3);
        return result;
      }
      // if (logger.isTraceEnabled())
      //     logger.trace("Processing Query results");
      while (set.next()) {
        step=2 ; bookId = set.getString("book_id");
        // if (logger.isTraceEnabled())
        //     logger.trace("Processing bookId " + bookId);
        step = 3; String uuid = set.getString("uuid");
        Date timestamp = null;
        try {
          step= 4; timestamp = SQLITE_TIMESTAMP_FORMAT.parse(set.getString("book_timestamp"));
        } catch (ParseException e) {
          if (logger.isDebugEnabled()) logger.debug("listBooks (timestamp): " + e);
          // we don't care
        }
        Date modified = null;
        try {
          step= 5; modified = SQLITE_TIMESTAMP_FORMAT.parse(set.getString("book_modified"));
        } catch (ParseException e) {
          if (logger.isDebugEnabled()) logger.debug("listBooks (modified): " + e);
          // we don't care
        }
        Date publicationDate = null;
        try {
          step=6; publicationDate = SQLITE_TIMESTAMP_FORMAT.parse(set.getString("book_pubdate"));
        } catch (ParseException e) {
          if (logger.isDebugEnabled()) logger.debug("listBooks (publicationDate): " + e);
          // we don't care
        }
        // add a new book
        step=10 ; String title = set.getString("book_title");
        step=11 ; String title_sort = set.getString(("book_title_sort"));
        step=12 ; String path = set.getString("book_path");
        step=13 ; float index = set.getFloat("series_index");   // Bug 716914 Get series index correctly
        step=14 ; String isbn = set.getString("isbn");
        step=15 ; String authorSort = set.getString("author_sort");
        step=16 ; int iRating = set.getInt("rating");
        iRating += (iRating % 2);    // bug #c2o-128  Ensure values even (round up if necessary)
        BookRating rating = BookRating.fromValue(iRating);
        Book book = new Book(bookId, uuid, title, title_sort, path, index, timestamp, modified, publicationDate, isbn, authorSort, rating);

        // fetch its languages
        step=20 ; stmtBooksLanguagesLink.setString(1, book.getId());
        ResultSet setLanguages = null;
        try {
          setLanguages = stmtBooksLanguagesLink.executeQuery();
        } catch (SQLException e) {
          logger.error("listBooks: bookId=" + bookId + "\nstmtBooksLanguageLink=" + stmtBooksLanguagesLink + "\n" + e); Helper.statsErrors++;
          sqlException += (2^4);
          return result;
        }
        step=21;
        while (setLanguages.next()) {
          step=14 ; String languageId = setLanguages.getString("lang_code");
          book.addBookLanguage(DataModel.getMapOfLanguagesById().get(languageId));
          step=22;
        }

        // fetch its author
        List<Author> authors = DataModel.getMapOfAuthorsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(authors)) {
          for (Author author : authors) {
            book.addAuthor(author);
          }
        } else {
          if (logger.isTraceEnabled()) logger.trace("Appear to be no authors for bookId" + bookId);
        }

        // fetch its publisher
        List<Publisher> publishers = DataModel.getMapOfPublishersByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(publishers)) {
          book.setPublisher(publishers.get(0));
        }

        // fetch its series
        List<Series> series = DataModel.getMapOfSeriesByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(series))
          book.setSeries(series.get(0));

        // fetch its comment
        List<String> comments = DataModel.getMapOfCommentsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(comments))
          book.setComment(comments.get(0));

        // fetch its categories
        List<Tag> tags = DataModel.getMapOfTagsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(tags))
          book.getTags().addAll(tags);

        // fetch its files
        List<EBookFile> files = DataModel.getMapOfEBookFilesByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(files)) {
          for (EBookFile file : files) {
            file.setBook(book);
            book.addFile(file);
          }
        }
        result.add(book);
        step = 1;
      }
    } catch (SQLException e) {
      logger.error("listBooks: step=" + step + "\n" + e);  Helper.statsErrors++;
      sqlException += (2^5);
    }
    return result;
  }

  /**
   * Get the list of possible authors
   * If it does not already exist, then create it.
   *
   * @return
   */
  public static List<Author> listAuthors() {
    List<Author> result = new LinkedList<Author>();
    List<String> ids = new LinkedList<String>();
    PreparedStatement statement = DatabaseRequest.ALL_AUTHORS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("id");
        if (!ids.contains(id)) {
          ids.add(id);
          result.add(new Author(id, set.getString("name"), set.getString("sort")));
        }
      }
    } catch (SQLException e) {
      logger.error("listAuthors: " + e); Helper.statsErrors++;
      sqlException += (2^6);
    }
    return result;
  }

  /**
   * Get the list of possible publishers.
   * If it does not already exist thenc reate it.
   *
   * @return
   */
  public static List<Publisher> listPublishers() {
    List<Publisher> result = new LinkedList<Publisher>();
    List<String> ids = new LinkedList<String>();
    PreparedStatement statement = DatabaseRequest.ALL_PUBLISHERS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("id");
        if (!ids.contains(id)) {
          ids.add(id);
          result.add(new Publisher(id, set.getString("name"), set.getString("sort")));
        }
      }
    } catch (SQLException e) {
      logger.error("listPublishers: " + e); Helper.statsErrors++;
      sqlException += (2^7);
    }
    return result;
  }

  /**
   * Get the list of possible Series
   * If it does not exist then create it
   * by loading it from the Calibre database.
   *
   * @return
   */
  public static List<Series> listSeries() {
    List<Series> result = new LinkedList<Series>();
    List<String> ids = new LinkedList<String>();
    PreparedStatement statement = DatabaseRequest.ALL_SERIES.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("id");
        if (!ids.contains(id)) {
          ids.add(id);
          result.add(new Series(id, set.getString("name"), set.getString("serie_sort")));
        }
      }
    } catch (SQLException e) {
      logger.error("listSeries: " + e); Helper.statsErrors++;
      sqlException += (2^8);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<EBookFile>> getMapOfEBookFilesByBookId() {
    Map<String, List<EBookFile>> result = new HashMap<String, List<EBookFile>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_DATA.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("book");
        String format = set.getString("format");
        String name = set.getString("name");
        List<EBookFile> files = result.get(id);
        if (files == null) {
          files = new LinkedList<EBookFile>();
          result.put(id, files);
        }
        files.add(new EBookFile(format, name));
      }
    } catch (SQLException e) {
      logger.error("listFilesByBook: " + e); Helper.statsErrors++;
      sqlException += (2^9);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Author>> getMapOfAuthorsByBookId() {
    Map<String, List<Author>> result = new HashMap<String, List<Author>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_AUTHORS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String bookId = set.getString("book");
        String authorId = set.getString("author");
        List<Author> authors = result.get(bookId);
        if (authors == null) {
          authors = new LinkedList<Author>();
          result.put(bookId, authors);
        }
        Author author = DataModel.getMapOfAuthors().get(authorId);
        if (author != null) {
          authors.add(author);
        } else {
          logger.warn("cannot find author #" + authorId);
          Helper.statsWarnings++;
        }
      }
    } catch (SQLException e) {
      logger.error("listAuthorsByBook: " + e); Helper.statsErrors++;
      sqlException += (2^10);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Publisher>> listPublishersByBookId() {
    Map<String, List<Publisher>> result = new HashMap<String, List<Publisher>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_PUBLISHERS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String bookId = set.getString("book");
        String publisherId = set.getString("publisher");
        List<Publisher> publishers = result.get(bookId);
        if (publishers == null) {
          publishers = new LinkedList<Publisher>();
          result.put(bookId, publishers);
        }
        Publisher publisher = DataModel.getMapOfPublishers().get(publisherId);
        if (publisher != null) {
          publishers.add(publisher);
        } else {
          logger.warn("cannot find publisher #" + publisherId);
          Helper.statsWarnings++;
        }
      }
    } catch (SQLException e) {
      logger.error("listPublishersByBook: " + e); Helper.statsErrors++;
      sqlException += (2^11);
    }
    return result;
  }

  /**
   * Build up a list of the tags by the bookid using them
   *
   * @return
   */
  public static Map<String, List<Tag>> getMapOfTagsByBookId() {
    Map<String, List<Tag>> result = new HashMap<String, List<Tag>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_TAGS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
       while (set.next()) {
        String bookId = set.getString("book");
        String tagId = set.getString("tag");
        List<Tag> tags = result.get(bookId);
        if (tags == null) {
          tags = new LinkedList<Tag>();
          result.put(bookId, tags);
        }
        Tag tag = DataModel.getMapOfTags().get(tagId);
        if (tag != null) {
          tags.add(tag);
        } else {
          logger.warn("cannot find tag #" + tagId);  Helper.statsWarnings++;
        }
      }
    } catch (SQLException e) {
      logger.error("getMapOfTagsByBookId: " + e); Helper.statsErrors++;
      sqlException += (2^12);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Series>> getMapOfSeriesByBookId() {
    Map<String, List<Series>> result = new HashMap<String, List<Series>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_SERIES.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String bookId = set.getString("book");
        String serieId = set.getString("series");
        List<Series> series = result.get(bookId);
        if (series == null) {
          series = new LinkedList<Series>();
          result.put(bookId, series);
        }
        Series serie = DataModel.getMapOfSeries().get(serieId);
        if (serie != null) {
          series.add(serie);
        } else {
          logger.warn("cannot find serie #" + serieId);
          Helper.statsWarnings++;
        }
      }
    } catch (SQLException e) {
      logger.error("getMapOfSeriesByBookId: " + e); Helper.statsErrors++;
      sqlException += (2^13);
    }
    return result;
  }

  /**
   * TODO Experiment with cost of reading comments on demand for each book
   * TODO Might be worth it to reduce runtime RAM usage?
   * TODO Write out somw statistics to work out possible gain.
   * @return
   */

  public static Map<String, List<String>> getMapOfCommentsByBookId() {
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    PreparedStatement statement = DatabaseRequest.BOOKS_COMMENTS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String id = set.getString("book");
        String text = set.getString("text");
        List<String> comments = result.get(id);
        if (comments == null) {
          comments = new LinkedList<String>();
          result.put(id, comments);
        }
        comments.add(text);
      }
    } catch (SQLException e) {
      logger.error("getMapOfCommentsByBookId: " + e); Helper.statsErrors++;
      sqlException += (2^14);
    }
    if (logger.isDebugEnabled()) logger.debug("Number of comments=" + result.size() + ", Total Size="+ result.toString().length());
    return result;
  }

  /**
   *
   * @return
   */
  public static Map<String, String> getMapOfSavedSearches() {
    final String MIDDLE_DELIMITER = "\": \"";
    Map<String, String> result = new HashMap<String, String>();
    PreparedStatement statement = DatabaseRequest.SAVED_SEARCHES.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        String val = set.getString("val");
        /* interpret the python dictionary */
        String dictionary = val.substring(1, val.length() - 1); // skip the opening and closing brackets
        List<String> lines = Helper.tokenize(dictionary, ", \n");
        for (String line : lines) {
          int posStart = line.indexOf("\"");
          if (posStart > -1) {
            int posMiddle = line.indexOf(MIDDLE_DELIMITER);
            if (posMiddle > -1) {
              String name = line.substring(posStart + 1, posMiddle);
              int posEnd = line.lastIndexOf("\"");
              if (posEnd > -1) {
                String search = line.substring(posMiddle + MIDDLE_DELIMITER.length(), posEnd);
                // unescape double quotes
                search = search.replace("\\\"", "\"");
                result.put(name, search);
                result.put(name.toUpperCase(Locale.ENGLISH), search);
              }
            }
          }
        }
      }
    } catch (SQLException e) {
      logger.error("getMapOfSavedSearches: " + e); Helper.statsErrors++;
      sqlException += (2^15);
    }
    return result;
  }

  /**
   * Determine if an SQL Exception occurred trying to load the database.
   *
   * @return  0 = no error
   *          other = value with bit field deterining exception points encountered
   */
  public static int wasSqlEsception() {
    return sqlException;
  }

  /**
   * Get the list of Custom Column type definitions
   *
   * @return
   */
  public static List<CustomColumnType> getlistOfCustoColumnTypes() {
    List<CustomColumnType> result = new LinkedList<CustomColumnType>();
    PreparedStatement statement = DatabaseRequest.CUSTOM_COLUMN_DEFINITION.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        Long id = set.getLong("id");
        String label = set.getString("label");
        String name = set.getString("name");
        String datatype = set.getString("datatype");
        boolean is_multiple = set.getBoolean("is_multiple");
        boolean normalized = set.getBoolean("normalized");
        CustomColumnType customColType = new CustomColumnType(id, label, name, datatype, is_multiple, normalized );
        result.add(customColType);
      }
    } catch (SQLException e) {
      logger.error("getlistOfCustoColumnTypes: " + e); Helper.statsErrors++;
      sqlException += (2^16);
    }

    return result;
  }

  /**
   * Get the list of custom column values by Book Id.
   *
   * @return
   */
  public static Map<String, List<CustomColumnValue>> getMapofCustomColumnValuesbyBookId (List<CustomColumnType> listTypes) {
    Map<String, List<CustomColumnValue>> result = new HashMap<String, List<CustomColumnValue>>();
    for (CustomColumnType listType : listTypes)  {
      PreparedStatement statement;
      if (listType.isNormalized()) {
        if (listType.getLabel().equals("series")) {
          DatabaseRequest.CUSTOM_COLUMN_NORMALIZED_DATA.resetStatement();
          statement = DatabaseRequest.CUSTOM_COLUMN_NORMALIZED_DATA_EXTRA.getStatementId(Long.toString(listType.getId()));
        } else {
          DatabaseRequest.CUSTOM_COLUMN_NORMALIZED_DATA.resetStatement();
          statement = DatabaseRequest.CUSTOM_COLUMN_NORMALIZED_DATA.getStatementId(Long.toString(listType.getId()));
        }
      } else {
        DatabaseRequest.CUSTOM_COLUMN_DATA.resetStatement();
        statement = DatabaseRequest.CUSTOM_COLUMN_DATA.getStatementId(Long.toString(listType.getId()));
      }
      try {
        ResultSet set = statement.executeQuery();
          while (set.next()) {
            String bookId;
            String value;
            String extra;
            bookId = set.getString("book");
            value = set.getString("value");
            if (listType.getDatatype().equals("series")) {
              extra = set.getString("extra");
            } else {
              extra = null;
            }
            List<CustomColumnValue> customColumnValues = result.get(bookId);
            if (customColumnValues == null) {
              customColumnValues = new LinkedList<CustomColumnValue>();
              result.put(bookId, customColumnValues);
            }
            CustomColumnValue customColumnValue = new CustomColumnValue(listType, value, extra);
            customColumnValues.add (customColumnValue);
          }
      } catch (SQLException e) {
        logger.error("getMapofCustomColumnValuesbyBookId: " + e); Helper.statsErrors++;
        sqlException += (2^17);
      }
    }

    return result;
  }



  private static Connection connection;

  public static Connection getConnection() {
    if (connection == null) {
      initConnection();
    }
    return connection;
  }

  public static boolean databaseExists() {
    Boolean reply;
    File database = new File(Configuration.instance().getDatabaseFolder(), "metadata.db");
    reply = database.exists();
    logger.debug("Database existence check: " + reply);
    if (reply) {
      // check for BOOKS table
      reply=Database.test();
      logger.debug("Database access check: " + reply);
    }
    return reply;
  }


  private static void initConnection() {
    initConnection(Configuration.instance().getDatabaseFolder());
  }

  private static void closeConnection() {
    if (connection != null)
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        logger.warn("Unexpected error on database closeConnection: " + e); Helper.statsWarnings++;
      }
  }

  public static void initConnection(File calibreLibrary) {
    try {
      Class.forName("org.sqlite.JDBC");
      File database = new File(calibreLibrary, "metadata.db");
      String url = database.toURI().getPath();
      connection = DriverManager.getConnection("jdbc:sqlite:" + url);
    } catch (ClassNotFoundException e) {
      logger.error(e); Helper.statsErrors++;
    } catch (SQLException e) {
      logger.error("initConnection: " + e); Helper.statsErrors++;
    }
  }

  public static void reset() {
    // reset the database prepared statements
    DatabaseRequest.reset();
    closeConnection();
  }

}
