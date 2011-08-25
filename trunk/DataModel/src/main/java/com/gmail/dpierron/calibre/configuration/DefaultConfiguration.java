package com.gmail.dpierron.calibre.configuration;

import java.io.File;

public class DefaultConfiguration implements ReadOnlyConfigurationInterface {
  private final static String DEFAULT_DATABASE = ".";
  
  public File getDatabaseFolder() {
    return new File(DEFAULT_DATABASE);
  }

}
