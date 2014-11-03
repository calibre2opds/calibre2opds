package com.gmail.dpierron.calibre.database;

/**
 * Abstract the basic database operations used by calibre2opds
 * Handles transfering data between database field and calibre2opds variables
 */

import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public enum Database {

  INSTANCE;

  private static final Logger logger = Logger.getLogger(Database.class);
  private static final DateFormat SQLITE_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static int sqlException = 0;    // Set to bit dependent value to allow for multiple different exception points

  /**
   *
   * @return
   */
  public List<Tag> listTags() {
    List<Tag> result = new LinkedList<Tag>();
    PreparedStatement statement = DatabaseRequest.ALL_TAGS.getStatement();
    try {
      ResultSet set = statement.executeQuery();
      while (set.next()) {
        result.add(new Tag(set.getString("id"), set.getString("name")));
      }
    } catch (SQLException e) {
      logger.error("ListTag: " + e);
      sqlException += (2^0);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public boolean test() {
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
  public Composite<Map<String, Language>, Map<String, Language>> getMapsOfLanguages() {
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
      logger.error("getMapsOfLanguages: " + e);
      sqlException += (2^2);
    }
    return new Composite<Map<String, Language>, Map<String, Language>>(mapOfLanguagesById, mapOfLanguagesByIsoCode);
  }

  /**
   *
   * @return
   */
  public List<Book> listBooks() {
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
        logger.error("listBooks: statement=" + statement + "\n" + e);
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
          logger.error("listBooks: bookId=" + bookId + "\nstmtBooksLanguageLink=" + stmtBooksLanguagesLink + "\n" + e);
          sqlException += (2^4);
          return result;
        }
        step=21;
        while (setLanguages.next()) {
          step=14 ; String languageId = setLanguages.getString("lang_code");
          book.addBookLanguage(DataModel.INSTANCE.getMapOfLanguagesById().get(languageId));
          step=22;
        }

        // fetch its author
        List<Author> authors = DataModel.INSTANCE.getMapOfAuthorsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(authors)) {
          for (Author author : authors) {
            book.addAuthor(author);
          }
        } else {
          if (logger.isTraceEnabled()) logger.trace("Appear to be no authors for bookId" + bookId);
        }

        // fetch its publisher
        List<Publisher> publishers = DataModel.INSTANCE.getMapOfPublishersByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(publishers)) {
          book.setPublisher(publishers.get(0));
        }

        // fetch its series
        List<Series> series = DataModel.INSTANCE.getMapOfSeriesByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(series))
          book.setSeries(series.get(0));

        // fetch its comment
        List<String> comments = DataModel.INSTANCE.getMapOfCommentsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(comments))
          book.setComment(comments.get(0));

        // fetch its categories
        List<Tag> tags = DataModel.INSTANCE.getMapOfTagsByBookId().get(bookId);
        if (Helper.isNotNullOrEmpty(tags))
          book.getTags().addAll(tags);

        // fetch its files
        List<EBookFile> files = DataModel.INSTANCE.getMapOfFilesByBookId().get(bookId);
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
      logger.error("listBooks: step=" + step + "\n" + e);
      sqlException += (2^5);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public List<Author> listAuthors() {
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
      logger.error("listAuthors: " + e);
      sqlException += (2^6);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public List<Publisher> listPublishers() {
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
      logger.error("listPublishers: " + e);
      sqlException += (2^7);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public List<Series> listSeries() {
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
      logger.error("listSeries: " + e);
      sqlException += (2^8);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, List<EBookFile>> listFilesByBookId() {
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
      logger.error("listFilesByBook: " + e);
      sqlException += (2^9);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, List<Author>> listAuthorsByBookId() {
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
        Author author = DataModel.INSTANCE.getMapOfAuthors().get(authorId);
        if (author != null)
          authors.add(author);
        else
          logger.warn("cannot find author #" + authorId);
      }
    } catch (SQLException e) {
      logger.error("listAuthorsByBook: " + e);
      sqlException += (2^10);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, List<Publisher>> listPublishersByBookId() {
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
        Publisher publisher = DataModel.INSTANCE.getMapOfPublishers().get(publisherId);
        if (publisher != null)
          publishers.add(publisher);
        else
          logger.warn("cannot find publisher #" + publisherId);
      }
    } catch (SQLException e) {
      logger.error("listPublishersByBook: " + e);
      sqlException += (2^11);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, List<Tag>> listTagsByBookId() {
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
        Tag tag = DataModel.INSTANCE.getMapOfTags().get(tagId);
        if (tag != null)
          tags.add(tag);
        else
          logger.warn("cannot find tag #" + tagId);
      }
    } catch (SQLException e) {
      logger.error("listTagsByBookId: " + e);
      sqlException += (2^12);
    }
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, List<Series>> listSeriesByBookId() {
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
        Series serie = DataModel.INSTANCE.getMapOfSeries().get(serieId);
        if (serie != null)
          series.add(serie);
        else
          logger.warn("cannot find serie #" + serieId);
      }
    } catch (SQLException e) {
      logger.error("listSeriesByBookId: " + e);
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

  public Map<String, List<String>> listCommentsByBookId() {
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
      logger.error("listCommentsByBookId: " + e);
      sqlException += (2^14);
    }
    if (logger.isDebugEnabled()) logger.debug("Number of comments=" + result.size() + ", Total Size="+ result.toString().length());
    return result;
  }

  /**
   *
   * @return
   */
  public Map<String, String> getMapOfSavedSearches() {
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
      logger.error("getMapOfSavedSearches: " + e);
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
  public int wasSqlEsception() {
    return sqlException;
  }

  /**
   * Get the list of Custom Column type definitions
   *
   * @return
   */
  public List<CustomColumnType> getlistOfCustoColumnTypes() {
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
      logger.error("getlistOfCustoColumnTypes: " + e);
      sqlException += (2^16);
    }

    return result;
  }

  /**
   * Get the list of custom column values by Book Id.
   *
   * @return
   */
  public Map<String, List<CustomColumnValue>> getMapofCustomColumnValuesbyBookId (List<CustomColumnType> listTypes) {
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
        logger.error("getMapofCustomColumnValuesbyBookId: " + e);
        sqlException += (2^17);
      }
    }

    return result;
  }
}
