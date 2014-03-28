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
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;

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
  private final static Logger logger = Logger.getLogger(HtmlManager.class);
  private long timeInHtml = 0;

  public HtmlManager() {
    timeInHtml = 0;
  }

  public long getTimeInHtml() {
    return timeInHtml;
  }

  public void generateHtmlFromDOM(Document document, File outputFile, FeedType feedType) throws IOException {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtml()) {
      FileOutputStream fos = null;
      try {
        // create the same file as html
        long now = System.currentTimeMillis();
        File htmlFile = new File(getHtmlFilename(outputFile.toString()));
        if (htmlFile.exists()) {
          return;
        }
        JDOMSource source = new JDOMSource(document);
        fos = new FileOutputStream(htmlFile);
        StreamResult streamResult = new StreamResult(fos);
        try {
          Transformer transformer = null;
          switch (feedType) {
            case MainCatalog:
              generateHeaderHtml(document, outputFile);
              transformer = JDOM.INSTANCE.getMainCatalogTransformer();
              break;

            case Catalog:
              transformer = JDOM.INSTANCE.getCatalogTransformer();
              break;

            case BookFullEntry:
              transformer = JDOM.INSTANCE.getBookFullEntryTransformer();
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
          logger.error(Localization.Main.getText("error.cannotTransform", outputFile.getAbsolutePath()), e);
        }
        timeInHtml += (System.currentTimeMillis() - now);
      } finally {
        if (fos != null)
          fos.close();
      }
    }
  }

  /**
   *   TODO:  Decide if this function is needed at all!
   */
  private void generateHeaderHtml(Document document, File outputFile) throws IOException {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtml()) {
      FileOutputStream fos = null;
      try {
        JDOMSource source = new JDOMSource(document);
        String xmlFilename = outputFile.getParentFile().getAbsolutePath();
        File htmlFile = new File(xmlFilename + File.separator + "header.html");
        fos = new FileOutputStream(htmlFile);
        StreamResult streamResult = new StreamResult(fos);
        try {
          Transformer transformer;
          transformer = JDOM.INSTANCE.getHeaderTransformer();
          transformer.transform(source, streamResult);
        } catch (TransformerException e) {
          logger.error(Localization.Main.getText("error.cannotTransform", outputFile.getAbsolutePath()), e);
        }
      } finally {
        if (fos != null)
          fos.close();
      }
    }
  }

  /**
   * create the HTML filename.
   * Handle the case of both no file extension, and existing XML one.
   *
   * @param filename
   * @return
   */
  public String getHtmlFilename(String filename) {
    assert Helper.isNotNullOrEmpty(filename) : "Program error: Attempt to create HTML filename for empty/null filename";
    assert ! filename.startsWith(ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogFolderName()):
              "Program Error:  filename should not include catalog folder" ;
    String htmlfilename = filename.replaceAll(Constants.XML_EXTENSION, "") + Constants.HTML_EXTENSION;
    if (! htmlfilename.endsWith(Constants.HTML_EXTENSION)) {
      htmlfilename += Constants.HTML_EXTENSION;
    }
    return htmlfilename;
  }
}
