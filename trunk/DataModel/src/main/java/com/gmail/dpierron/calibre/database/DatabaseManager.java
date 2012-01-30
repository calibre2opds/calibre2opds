package com.gmail.dpierron.calibre.database;

import com.gmail.dpierron.calibre.configuration.Configuration;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public enum DatabaseManager {
  INSTANCE;

  private static final Logger logger = Logger.getLogger(DatabaseManager.class);

  private Connection connection;

  public Connection getConnection() {
    if (connection == null) {
      initConnection();
    }
    return connection;
  }

  public boolean databaseExists() {
    Boolean reply;
    File database = new File(Configuration.instance().getDatabaseFolder(), "metadata.db");
    reply = database.exists();
    logger.debug("Database existence check: " + reply);
    if (reply) {
      // check for BOOKS table
      reply=Database.INSTANCE.test();
      logger.debug("Database access check: " + reply);
    }
    return reply;
  }


  private void initConnection() {
    initConnection(Configuration.instance().getDatabaseFolder());
  }

  private void closeConnection() {
    if (connection != null)
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        logger.warn("closeConnection: " + e);
      }
  }

  void initConnection(File calibreLibrary) {
    try {
      Class.forName("org.sqlite.JDBC");
      File database = new File(calibreLibrary, "metadata.db");
      String url = database.toURI().getPath();
      connection = DriverManager.getConnection("jdbc:sqlite:" + url);
    } catch (ClassNotFoundException e) {
      logger.error(e);
    } catch (SQLException e) {
      logger.error("initConnection: " + e);
    }
  }

  public void reset() {
    // reset the database prepared statements
    DatabaseRequest.reset();
    closeConnection();
  }
}
