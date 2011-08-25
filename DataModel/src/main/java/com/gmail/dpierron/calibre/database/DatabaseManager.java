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
    File database = new File(Configuration.instance().getDatabaseFolder(), "metadata.db");
    if (!database.exists())
      return false;
    // check for BOOKS table
    return Database.INSTANCE.test();
  }
  
  
  private void initConnection() {
    initConnection(Configuration.instance().getDatabaseFolder());
  }

  public void initConnection(File calibreLibrary) {
    try {
      Class.forName("org.sqlite.JDBC");
      File database = new File(calibreLibrary, "metadata.db");
      String url = database.toURI().getPath();
      connection = DriverManager.getConnection("jdbc:sqlite:"+url);
    } catch (ClassNotFoundException e) {
      logger.error(e);
    } catch (SQLException e) {
      logger.error(e);
    }
  }
}
