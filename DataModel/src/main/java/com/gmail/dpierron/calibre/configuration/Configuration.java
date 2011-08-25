package com.gmail.dpierron.calibre.configuration;

public class Configuration {
  private static ReadOnlyConfigurationInterface instance;
  
  public static ReadOnlyConfigurationInterface instance() {
    if (instance == null)
      setConfiguration(new ConfigurationImpl());
    return instance;
  }

  public static void setConfiguration(ReadOnlyConfigurationInterface configuration) {
    instance = configuration;
  }
  
}
