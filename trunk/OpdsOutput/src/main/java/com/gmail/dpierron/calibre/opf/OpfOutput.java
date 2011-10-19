package com.gmail.dpierron.calibre.opf;

import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.calibre.opds.JDOM;
import com.gmail.dpierron.calibre.opds.JDOM.Namespace;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class OpfOutput {
  private final static Logger logger = Logger.getLogger(OpfOutput.class);
  private static final DateFormat CALIBRE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final DateFormat CALIBRE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

  private Book book;
  private boolean removeCss = false;
  private File defaultCss = null;

  public OpfOutput(Book book, boolean removeCss, File defaultCss) {
    this.book = book;
    if (defaultCss != null && defaultCss.exists())
      this.defaultCss = defaultCss;
    this.removeCss = (this.defaultCss != null) || removeCss; // copyDefaultCss forces removeCss
  }

  public OpfOutput(Book book) {
    this(book, false, null);
  }

  public boolean isRemoveCss() {
    return removeCss;
  }

  public File getDefaultCss() {
    return defaultCss;
  }

  private String convertRatingToCalibreRating(BookRating rating) {
    if (rating == null)
      return "0.00";
    switch (rating) {
      case NOTRATED:
        return "0.00";
      case ONE:
        return "1.00";
      case TWO:
        return "2.00";
      case THREE:
        return "3.00";
      case FOUR:
        return "4.00";
      case FIVE:
        return "5.00";
      default:
        return "0.00";
    }
  }

  private void removeMetaElement(Element source, String name) {
    Content childToRemove = null;
    for (Object childO : source.getChildren()) {
      if (childO instanceof Element) {
        Element child = (Element) childO;
        if (child.getName().equalsIgnoreCase("meta")) {
          if (child.getAttributeValue("name").equalsIgnoreCase(name)) {
            childToRemove = child;
            break;
          }
        }
      }
    }
    if (childToRemove != null)
      source.removeContent(childToRemove);
  }

  private void removeDcElements(Element source, String name) {
    source.removeChildren(name, Namespace.Dc.getJdomNamespace());
  }

  private void addMetaElement(Element source, String name, String content) {
    Element meta = JDOM.INSTANCE.element("meta", Namespace.Opf);
    meta.setAttribute("name", name);
    meta.setAttribute("content", content);
    source.addContent(meta);
  }

  private void addDublinCoreElement(Element source, String name, String content) {
    source.addContent(getDublinCoreElement(source, name, content));
  }

  private Element getDublinCoreElement(Element source, String name, String content) {
    Element dc = JDOM.INSTANCE.element(name, Namespace.Dc);
    dc.setText(content);
    return dc;
  }

  private String convertDate(Date date) {
    if (date == null)
      return "";
    String sDate = CALIBRE_DATE_FORMAT.format(date);
    String sTime = CALIBRE_TIME_FORMAT.format(date);
    return sDate + "T" + sTime;
  }

  private void processMetadataElement(Element source) {
    /* Calibre stuff */
    removeMetaElement(source, "calibre:rating");
    addMetaElement(source, "calibre:rating", convertRatingToCalibreRating(book.getRating()));

    removeMetaElement(source, "calibre:series");
    removeMetaElement(source, "calibre:series_index");
    if (book.getSeries() != null) {
      addMetaElement(source, "calibre:series", book.getSeries().getName());
      addMetaElement(source, "calibre:series_index", "" + book.getSerieIndex());
    }
    removeMetaElement(source, "calibre:timestamp");
    if (book.getTimestamp() != null) {
      addMetaElement(source, "calibre:timestamp", convertDate(book.getTimestamp()));
    }

    /* Dublin Core stuff */
    if (Helper.isNotNullOrEmpty(book.getUuid())) {
      removeDcElements(source, "identifier");
      Element dc = getDublinCoreElement(source, "identifier", book.getUuid());
      dc.setAttribute("id", "calibre-uuid");
      source.addContent(dc);
    }
    List<Language> bookLanguages = book.getBookLanguages();
    if (Helper.isNotNullOrEmpty(bookLanguages)) {
      removeDcElements(source, "language");
      for (Language language : bookLanguages) {
        addDublinCoreElement(source, "language", language.getIso3());
      }
    }
    if (Helper.isNotNullOrEmpty(book.getAuthors())) {
      removeDcElements(source, "creator");
      for (Author author : book.getAuthors()) {
        Element dc = getDublinCoreElement(source, "creator", author.getName());
        Attribute att = new Attribute("file-as", author.getSort(), Namespace.Opf.getJdomNamespace());
        dc.setAttribute(att);
        att = new Attribute("role", "aut", Namespace.Opf.getJdomNamespace());
        dc.setAttribute(att);
        source.addContent(dc);
      }
    }
    if (Helper.isNotNullOrEmpty(book.getTitle())) {
      removeDcElements(source, "title");
      addDublinCoreElement(source, "title", book.getTitle());
    }
    if (book.getTimestamp() != null) {
      removeDcElements(source, "date");
      addDublinCoreElement(source, "date", convertDate(book.getTimestamp()));
    }
    boolean subjectsRemoved = false;
    if (Helper.isNotNullOrEmpty(book.getTags())) {
      if (!subjectsRemoved) {
        removeDcElements(source, "subject");
        subjectsRemoved = true;
      }
      for (Tag tag : book.getTags()) {
        addDublinCoreElement(source, "subject", tag.getName());
      }
    }
    if (book.getSeries() != null) {
      if (!subjectsRemoved) {
        removeDcElements(source, "subject");
        subjectsRemoved = true;
      }
      addDublinCoreElement(source, "subject", book.getSeries().getName());
    }
  }

  public void processEPubFile() throws IOException {
    if (book == null || book.getEpubFile() == null)
      return;
    File outputFile = File.createTempFile("calibre-epub-opfoutput", ".epub");
    try {
      processEPubFile(outputFile);
      Helper.copy(outputFile, book.getEpubFile().getFile());
    } catch (IOException e) {
      logger.error(e);
      logger.error("... for book: " + book.getTitle() + " (file " + book.getEpubFile().getFile() + ")");
    } finally {
      outputFile.delete();
    }
  }

  public void processEPubFile(File outputFile) throws IOException {
    if (book.getEpubFile() == null)
      return;
    File inputFile = book.getEpubFile().getFile();

    ZipFile zipInputFile = null;
    ZipOutputStream zos = null;
    try {
      try {
        zipInputFile = new ZipFile(inputFile);
        outputFile.getParentFile().mkdirs();
        zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        Enumeration entries = zipInputFile.entries();
        while (entries.hasMoreElements()) {
          Object o = entries.nextElement();
          if (o instanceof ZipEntry) {
            ZipEntry zipEntry = (ZipEntry) o;
            InputStream inputStream = zipInputFile.getInputStream(zipEntry);
            if (zipEntry.getName().toUpperCase().endsWith("CONTENT.OPF")) {
              // process the XML in the file
              try {
                Document doc = JDOM.INSTANCE.getSaxBuilder().build(inputStream);
                doc.getRootElement().addNamespaceDeclaration(Namespace.Opf.getJdomNamespace());
                doc.getRootElement().addNamespaceDeclaration(Namespace.Dc.getJdomNamespace());
                doc.getRootElement().addNamespaceDeclaration(Namespace.DcTerms.getJdomNamespace());
                doc.getRootElement().addNamespaceDeclaration(Namespace.Calibre.getJdomNamespace());
                Element metadata = doc.getRootElement().getChild("metadata", Namespace.Opf.getJdomNamespace());
                if (metadata != null)
                  processMetadataElement(metadata);
                try {
                  ZipEntry newEntry = new ZipEntry(zipEntry.getName());
                  zos.putNextEntry(newEntry);
                  JDOM.INSTANCE.getOutputter().output(doc, zos);
                } finally {
                  zos.closeEntry();
                }
              } catch (IOException e) {
                logger.error(e);
                logger.error("... for book: " + book.getTitle() + " (file " + inputFile + ")");
              }
            } else {
              // copy the entry to the output file
              BufferedInputStream in = null;
              try {
                // THIS COMMENTED-OUT CODE RENAMES THE .CSS_BAK FILES BACK TO .CSS
                //                try {
                //                  String filename = zipEntry.getName();
                //                  if (isRemoveCss() && filename.toUpperCase().endsWith(".CSS_BAK")) {
                //                    filename = filename.substring(0, filename.length() - 4);
                //                  }
                //                  ZipEntry newEntry = new ZipEntry(filename);
                //                  newEntry.setMethod(zipEntry.getMethod());
                //                  if (newEntry.getMethod() == ZipEntry.STORED) {
                //                    newEntry.setSize(zipEntry.getSize());
                //                    newEntry.setCrc(zipEntry.getCrc());
                //                  }
                //                  zos.putNextEntry(newEntry);
                //                  byte[] data = new byte[1024];
                //                  in = new BufferedInputStream(inputStream, 1024);
                //                  int count;
                //                  while ((count = in.read(data, 0, data.length)) != -1) {
                //                    zos.write(data, 0, count);
                //                  }
                //                } finally {
                //                  zos.closeEntry();
                //                  if (in != null)
                //                    in.close();
                //                }

                // check if we must remove the CSS files (rename them to .css_bak)
                String filename = zipEntry.getName();
                if (isRemoveCss() && filename.toUpperCase().endsWith(".CSS")) {
                  // copy the default stylesheet if needed
                  if (getDefaultCss() != null) {
                    try {
                      BufferedInputStream in2 = null;
                      try {
                        ZipEntry newEntry = new ZipEntry(filename);
                        newEntry.setMethod(ZipEntry.DEFLATED);
                        zos.putNextEntry(newEntry);
                        byte[] data = new byte[1024];
                        in2 = new BufferedInputStream(new FileInputStream(getDefaultCss()), 1024);
                        int count;
                        while ((count = in2.read(data, 0, data.length)) != -1) {
                          zos.write(data, 0, count);
                        }
                      } finally {
                        zos.closeEntry();
                        if (in2 != null)
                          in2.close();
                      }
                    } catch (IOException e) {
                      logger.error(e);
                      logger.error("... for book: " + book.getTitle() + " (cannot copy the default stylesheet)");
                    }
                  }
                  filename += "_BAK";
                  // don't duplicate entries
                  if (zipInputFile.getEntry(filename) != null)
                    filename = null;
                }
                if (filename != null) {
                  try {
                    ZipEntry newEntry = new ZipEntry(filename);
                    newEntry.setMethod(zipEntry.getMethod());
                    if (newEntry.getMethod() == ZipEntry.STORED) {
                      newEntry.setSize(zipEntry.getSize());
                      newEntry.setCrc(zipEntry.getCrc());
                    }
                    zos.putNextEntry(newEntry);
                    byte[] data = new byte[1024];
                    in = new BufferedInputStream(inputStream, 1024);
                    int count;
                    while ((count = in.read(data, 0, data.length)) != -1) {
                      zos.write(data, 0, count);
                    }
                  } finally {
                    zos.closeEntry();
                    if (in != null)
                      in.close();
                  }
                }
              } catch (IOException e) {
                logger.error(e);
                logger.error("... for book: " + book.getTitle() + " (file " + inputFile + ")");
              }
            }
          }
        }
      } finally {
        if (zos != null) {
          zos.close();
        }
        if (zipInputFile != null)
          zipInputFile.close();

      }
    } catch (JDOMException e) {
      logger.error("", e);
      throw new IOException(e);
    }
  }
}
