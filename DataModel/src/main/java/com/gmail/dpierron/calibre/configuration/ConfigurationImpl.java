package com.gmail.dpierron.calibre.configuration;

import java.io.File;

public class ConfigurationImpl implements ReadOnlyConfigurationInterface {

  private File databaseFolder;

  public ConfigurationImpl() {
    ReadOnlyConfigurationInterface defaults = new DefaultConfiguration();
    setDatabaseFolder(defaults.getDatabaseFolder());
  }

  public File getDatabaseFolder() {
    return databaseFolder;
  }

  public void setDatabaseFolder(File databaseFolder) {
    this.databaseFolder = databaseFolder;
  }

}
