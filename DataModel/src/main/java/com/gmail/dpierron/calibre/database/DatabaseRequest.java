package com.gmail.dpierron.calibre.database;
/**
 * Abstract the SQL underlying standard requests for calibre2opds
 */

import com.gmail.dpierron.calibre.configuration.Configuration;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum DatabaseRequest {
  TEST("SELECT COUNT(*) FROM books"),
  ALL_TAGS("SELECT DISTINCT  id, name " + " FROM tags " + " ORDER BY name "),

  ALL_BOOKS("SELECT  DISTINCT   " + "b.id AS book_id,   " + "b.title AS book_title,   "  + "b.title_sort as book_title_sort,  "
                            + "b.series_index AS series_index,   " + "b.path AS book_path,   "
                            + "b.timestamp AS book_timestamp, " + "b.pubdate AS book_pubdate, "
                            + "b.isbn AS isbn, " + "b.uuid AS uuid, " + "b.author_sort AS author_sort, "
                            + "r.rating AS rating "
                            + "FROM books b " + "LEFT OUTER JOIN books_ratings_link brl ON brl.book=b.id "
                                              + "LEFT OUTER JOIN ratings r ON brl.rating=r.id "),

  ALL_AUTHORS("select " + "a.id, " + "a.name, " + "a.sort " + "from authors a " + "order by a.id"),

  ALL_PUBLISHERS("select " + "p.id, " + "p.name, " + "p.sort " + "from publishers p " + "order by p.id"),

  ALL_SERIES("select " + "s.id, " + "s.name, " + "s.sort as serie_sort " + "from series s " + "order by s.sort"),

  ALL_LANGUAGES("select l.id, l.lang_code from languages l"),

  BOOKS_SERIES("select book, series from books_series_link"),
  BOOKS_TAGS("select book, tag from books_tags_link"),
  BOOKS_AUTHORS("select book, author from books_authors_link"),
  BOOKS_PUBLISHERS("select book, publisher from books_publishers_link"),
  BOOKS_DATA("select book, format, name from data"),
  BOOKS_COMMENTS("select book, text from comments"),
  BOOKS_LANGUAGES("select book, lang_code from books_languages_link where book = :bookId"),
  SAVED_SEARCHES("select val from preferences where key='saved_searches'");

  private static final Logger logger = Logger.getLogger(DatabaseRequest.class);
  private final String sql;
  private PreparedStatement preparedStatement;

  private DatabaseRequest(String sql) {
    this.sql = sql;
  }

  public void resetStatement() {
    preparedStatement = null;
  }

  public PreparedStatement getStatement() throws RuntimeException {
    if (preparedStatement == null) {
      try {
        Connection connection = DatabaseManager.INSTANCE.getConnection();
        if (connection == null) {
          String e = "Cannot establish a database connection to " + new File(Configuration.instance().getDatabaseFolder(), "metadata.db");
          logger.error(e);
          throw new RuntimeException(e);
        }
        preparedStatement = connection.prepareStatement(sql);
      } catch (SQLException e) {
        logger.error(e);
        throw new RuntimeException(e);
      }
    }
    return preparedStatement;
  }

  public static void reset() {
    for (DatabaseRequest databaseRequest : values()) {
      databaseRequest.resetStatement();
    }
  }
}
