package com.gmail.dpierron.calibre.datamodel;


public class EBookFormat implements Comparable<EBookFormat> {
  public final static EBookFormat EPUB = new EBookFormat("EPUB", "application/epub+zip");
  public final static EBookFormat TXT = new EBookFormat("TXT", "text/plain");
  public final static EBookFormat ZIP = new EBookFormat("ZIP", "application/zip");
  public final static EBookFormat PRC = new EBookFormat("PRC");
  public final static EBookFormat PDB = new EBookFormat("PDB", "application/pdb");
  public final static EBookFormat MOBI = new EBookFormat("MOBI", "application/x-mobipocket-ebook");
  public final static EBookFormat LRF = new EBookFormat("LRF", "application/x-sony-bbeb");
  public final static EBookFormat LRX = new EBookFormat("LRX", "application/x-bbeb-book");
  public final static EBookFormat FB2 = new EBookFormat("FB2", "text/xml");
  public final static EBookFormat RAR = new EBookFormat("RAR", "application/rar");
  public final static EBookFormat PDF = new EBookFormat("PDF", "application/pdf");
  public final static EBookFormat RTF = new EBookFormat("RTF", "text/rtf");
  public final static EBookFormat LIT = new EBookFormat("LIT", "application/x-ms-reader");
  public final static EBookFormat DOC = new EBookFormat("DOC", "application/msword");
  public final static EBookFormat CBR = new EBookFormat("CBR", "application/x-cbr");
  public final static EBookFormat CBZ = new EBookFormat("CBZ", "application/x-cbz");
  public final static EBookFormat CHM = new EBookFormat("CHM", "application/x-chm");
  public final static EBookFormat AZW = new EBookFormat("AZW", "application/octet-stream");

  private final static EBookFormat[] values = {EPUB, TXT, ZIP, PRC, PDB, AZW, MOBI, LRF, LRX, FB2, RAR, PDF, RTF, LIT, DOC, CBR, CBZ, CHM};

  private final static String DEFAULTMIME = "application/other";

  private String mime;
  private String name;

  private EBookFormat(String name, String mime) {
    this(name);
    this.mime = mime;
  }

  private EBookFormat(String name) {
    this.name = name;
  }

  private EBookFormat() {
    this(DEFAULTMIME);
  }

  int priority = -1;

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

}
