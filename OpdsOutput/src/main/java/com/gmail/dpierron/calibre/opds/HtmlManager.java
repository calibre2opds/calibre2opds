package com.gmail.dpierron.calibre.opds;
/**
 *  This class controls the generation of appropriate HTML files from
 *  the XML documents created during the catalog build process.
 *
 *  The process uses an XSLT transform on the DOM document.  A number
 *  of different transformation variants are possible according to the
 *  document type that is being handled
 */

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HtmlManager {
  public enum FeedType {
    Catalog,
    BookFullEntry,
    MainCatalog
  }
  private final static Logger logger = LogManager.getLogger(HtmlManager.class);
  private static long timeInHtml = 0;

  public HtmlManager() {
    timeInHtml = 0;
  }

  public long getTimeInHtml() {
    return timeInHtml;
  }

  public static void generateHtmlFromDOM(Document document, File outputFile, FeedType feedType) throws IOException {

    FileOutputStream fos = null;
    try {
      // create the same file as html
      long now = System.currentTimeMillis();

      JDOMSource source = new JDOMSource(document);
      fos = new FileOutputStream(outputFile);
      StreamResult streamResult = new StreamResult(fos);
      try {
        Transformer transformer = null;
        switch (feedType) {
          case MainCatalog:
            generateIncludedHtml(document, CatalogManager.getGenerateFolder(), Constants.HEADER_XSL);
            generateIncludedHtml(document, CatalogManager.getGenerateFolder(), Constants.GENERATED_XSL);
            transformer = JDOMManager.getMainCatalogTransformer();
            break;

          case Catalog:
            transformer = JDOMManager.getCatalogTransformer();
            break;

          case BookFullEntry:
            transformer = JDOMManager.getBookFullEntryTransformer();
            break;

          default:
            assert false : "generateHtmlFromXml: Unknown feed type " + feedType;
        }

        if (transformer == null) {
          logger.fatal("Failed to get transformer: Probably means XSL file invalid and failed to compile!" );
        } else {
          transformer.transform(source, streamResult);
        }
      } catch (TransformerException e) {
        logger.error(Localization.Main.getText("error.cannotTransform", outputFile.getAbsolutePath()), e); Helper.statsErrors++;
      }
      timeInHtml += (System.currentTimeMillis() - now);
    } finally {
      if (fos != null)
        fos.close();
    }
  }

  /**
   *   This routine is sued to generate files that have no .xml file, but only a .xsl file.
   *   It is used for files included by other html files to avoid repreating the contents;
   *     - header.html
   *     - generated.html
   */
  public static void generateIncludedHtml(Document document, File outputPath, String xslname) throws IOException {
    if (ConfigurationManager.getCurrentProfile().getGenerateHtml()) {
      FileOutputStream fos = null;
      try {
        JDOMSource source = new JDOMSource(document);
        File htmlFile = new File(outputPath,getHtmlFilename(xslname));
        fos = new FileOutputStream(htmlFile);
        StreamResult streamResult = new StreamResult(fos);
        try {
          Transformer transformer;
          transformer = JDOMManager.getIncludeTransformer(xslname);
          if (transformer == null ||  source == null  || streamResult == null) {
            int dummy = 1;
          }
          transformer.transform(source, streamResult);
        } catch (TransformerException e) {
          logger.error(Localization.Main.getText("error.cannotTransform", xslname), e); Helper.statsErrors++;
        }
      } finally {
        if (fos != null)
          fos.close();
      }
    }
  }

  /**
   * create the HTML filename.
   * Handle the case of both no file extension, and existing XML/XSL one.
   *
   * @param filename
   * @return
   */
  public static String getHtmlFilename(String filename) {
    assert Helper.isNotNullOrEmpty(filename) :
            "Program error: Attempt to create HTML filename for empty/null filename";
    assert ! filename.startsWith(ConfigurationManager.getCurrentProfile().getCatalogFolderName()):
            "Program Error: Filename should not include catalog folder" ;
    assert filename.endsWith(Constants.XML_EXTENSION) || filename.endsWith(Constants.XSL_EXTENSION) :
            "Program Error: Filename '" + filename + "' does not end with " + Constants.XML_EXTENSION + " or " +  Constants.XML_EXTENSION;
    int pos = filename.lastIndexOf(Constants.EXTENSION_SEPARATOR);
    // if (pos == -1) pos = filename.length();
    return filename.substring(0, pos) + Constants.HTML_EXTENSION;
  }
}
