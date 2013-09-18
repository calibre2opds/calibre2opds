package com.gmail.dpierron.calibre.configuration;

/**
 * Calss that hold the methods for storing and retrieving the
 * configuration settings for a calibre2opds profile.
 */
import com.gmail.dpierron.calibre.opds.Constants;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class PropertiesBasedConfiguration {
  private final static Logger logger = Logger.getLogger(PropertiesBasedConfiguration.class);

  Properties properties;
  File propertiesFile;
  boolean readOnly = false;

  public PropertiesBasedConfiguration(File file) {
    super();
    logger.trace("new PropertiesBasedConfiguration: " + file);
    properties = new Properties();
    propertiesFile = file;
    if (propertiesFile == null) {
      logger.warn("PropertiesBasedConfigurationFile: null parameter");
    } else {
      if (!propertiesFile.exists()) {
        logger.trace("propertiesFile does not exist!");
        save();
      }
    }
  }

  protected void setPropertiesFile(File propertiesFile) {
    logger.trace("setPropertiesFile: " + propertiesFile);
    this.propertiesFile = propertiesFile;
  }

  public File getPropertiesFile() {
    return propertiesFile;
  }

  public void load() throws IOException {
    logger.trace("loadPropertiesFile: " + propertiesFile);
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(getPropertiesFile()));
      properties.loadFromXML(bis);
    } finally {
      if (bis != null)
        bis.close();
    }
  }

  public void save() {
    if (readOnly) {
      return;
    }
    BufferedOutputStream bos = null;
    try {
      try {
        bos = new BufferedOutputStream(new FileOutputStream(getPropertiesFile()));
        properties.storeToXML(bos, Constants.PROGTITLE);
      } finally {
        if (bos != null)
          bos.close();
      }
    } catch (IOException e) {
      logger.error("error while storing properties in " + getPropertiesFile().getAbsolutePath(), e);
    }
  }

  public void setProperty(String name, Object value) {
    if (value == null)
      properties.remove(name);
    else
      properties.setProperty(name, value.toString());
    save();
  }

  public void setProperty(String name, boolean value) {
    properties.setProperty(name, Boolean.toString(value));
    save();
  }

  public void setProperty(String name, int value) {
    properties.setProperty(name, Integer.toString(value));
    save();
  }

  public boolean isPropertyReadOnly(String name) {
    String readOnlyName = name + "_ReadOnly";
    return Helper.trueBoolean(getBoolean(readOnlyName));
  }

  public void setPropertyReadOnly(String name, boolean readOnly) {
    if (Helper.isNullOrEmpty(name))
      return;
    String readOnlyName = name + "_ReadOnly";
    setProperty(readOnlyName, readOnly);
  }

  public String getProperty(String name) {
    return properties.getProperty(name);
  }

  public Boolean getBoolean(String name) {
    String s = getProperty(name);
    if (s == null)
      return null;
    else
      return new Boolean(s);
  }

  public Integer getInteger(String name) {
    return Helper.parseInteger(getProperty(name));
  }

}