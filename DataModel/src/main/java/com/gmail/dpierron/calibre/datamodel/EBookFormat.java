package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class EBookFormat implements Comparable<EBookFormat> {
  public final static EBookFormat EPUB = new EBookFormat("EPUB", "application/epub+zip");
  /*
  private final static EBookFormat TXT = new EBookFormat("TXT", "text/plain");
  private final static EBookFormat ZIP = new EBookFormat("ZIP", "application/zip");
  private final static EBookFormat HTMLZ = new EBookFormat("HTMLZ", "application/zip");
  private final static EBookFormat PRC = new EBookFormat("PRC", "application/x-mobipocket");
  private final static EBookFormat PDB = new EBookFormat("PDB", "application/pdb");
  private final static EBookFormat AZW = new EBookFormat("AZW", "application/octet-stream");
  private final static EBookFormat MOBI = new EBookFormat("MOBI", "application/x-mobipocket-ebook");
  private final static EBookFormat LRF = new EBookFormat("LRF", "application/x-sony-bbeb");
  private final static EBookFormat LRX = new EBookFormat("LRX", "application/x-bbeb-book");
  private final static EBookFormat FB2 = new EBookFormat("FB2", "text/fb2");
  private final static EBookFormat RAR = new EBookFormat("RAR", "application/rar");
  private final static EBookFormat PDF = new EBookFormat("PDF", "application/pdf");
  private final static EBookFormat RTF = new EBookFormat("RTF", "text/rtf");
  private final static EBookFormat LIT = new EBookFormat("LIT", "application/x-ms-reader");
  private final static EBookFormat DOC = new EBookFormat("DOC", "application/msword");
  private final static EBookFormat DOCX = new EBookFormat("DOCX", "application/msword");
  private final static EBookFormat CBR = new EBookFormat("CBR", "application/x-cbr");
  private final static EBookFormat CBZ = new EBookFormat("CBZ", "application/x-cbz");
  private final static EBookFormat CHM = new EBookFormat("CHM", "application/x-chm");
  private final static EBookFormat AZW3 = new EBookFormat("AZW3", "application/octet-stream");
  private final static EBookFormat KF8 = new EBookFormat("KF8", "application/octet-stream");
  private final static EBookFormat DJVU = new EBookFormat("DJVU", "image/vnd.djvu");

  private final static EBookFormat[] values = {EPUB, TXT, ZIP, HTMLZ, PRC, PDB, AZW, MOBI, LRF, LRX, FB2, RAR, PDF, RTF, LIT, DOC, DOCX, CBR, CBZ, CHM, AZW3, KF8, DJVU };
  */

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
