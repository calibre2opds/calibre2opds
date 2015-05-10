package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.List;

public class EBookFormat implements Comparable<EBookFormat> {
  public final static EBookFormat EPUB = new EBookFormat("EPUB", "application/epub+zip");
  // List of formats that are recognised by Calibre2opds as valid formats
  // Initialised from the mimetypes file/resource
  private static List<EBookFormat> supportedFormats;
  private String mime;
  private final String name;

  public EBookFormat(String name, String mime) {
    this.name = name;
    this.mime = mime;
  }

  private int priority = -1;

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getMime() {
    return mime;
  }

  public int compareTo(EBookFormat o) {
    if (o == null)
      return -1;
    else {
      if (getPriority() == o.getPriority())
        return 0;
      else
        return (getPriority() > o.getPriority() ? -1 : 1);
    }
  }

  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }


  public static EBookFormat fromFormat(String sFormat) {
    // The following should only ever happen in test mode!
    // It is not cnvenient in test to call the routine to initialise
    // the list of formats so we simply assume it is valid
    // We therefore 'fudge' a return value to get tests to work
    if (supportedFormats == null) {
       return new EBookFormat(sFormat,"mimetestdummy");
    }
    for (EBookFormat format : supportedFormats) {
      if (format.getName().equalsIgnoreCase(sFormat))
        return format;
    }
    return null;
  }

  public static List<EBookFormat> getSupportedFormats () {
    return supportedFormats;
  }

  /**
   * Set the list of supported formats.
   * This list should be initialied by the ConfigurationManager
   *
   * @param formatList
   */
  public static void setSupportedFormats(List<EBookFormat> formatList) {
    supportedFormats = formatList;
  }
}
