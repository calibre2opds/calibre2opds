package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.io.FileInputStream;

public class EBookFormat implements Comparable<EBookFormat> {
  public final static EBookFormat EPUB = new EBookFormat("EPUB", "application/epub+zip");
  private final static EBookFormat TXT = new EBookFormat("TXT", "text/plain");
  private final static EBookFormat ZIP = new EBookFormat("ZIP", "application/zip");
//  private final static EBookFormat PRC = new EBookFormat("PRC");
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

  private final static EBookFormat[] values = {EPUB, TXT, ZIP, PRC, PDB, AZW, MOBI, LRF, LRX, FB2, RAR, PDF, RTF, LIT, DOC, DOCX, CBR, CBZ, CHM, AZW3, KF8, };

  private String mime;
  private final String name;

  private EBookFormat(String name, String mime) {
    this(name);
    this.mime = mime;
    setupFormats();
  }

  private EBookFormat(String name) {
    this.name = name;
    setupFormats();
  }

  /**
   * Initialise format list and associated mime types from resource
   */
  private void setupFormats() {
    if (values== null) {
      FileInputStream is;
    }
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

  public static EBookFormat[] values() {
    return values;
  }

  public static EBookFormat fromFormat(String sFormat) {
    for (EBookFormat format : values()) {
      if (format.name.equalsIgnoreCase(sFormat))
        return format;
    }
    return null;
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
}
