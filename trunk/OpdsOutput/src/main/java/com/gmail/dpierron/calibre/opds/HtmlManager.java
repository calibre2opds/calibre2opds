package com.gmail.dpierron.calibre.opds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.i18n.Localization;

public class HtmlManager {
  private final static Logger logger = Logger.getLogger(HtmlManager.class);

  public enum FeedType {
    MainCatalog,
    Catalog,
    BookFullEntry
  }
  
  private long timeInHtml = 0;

  public HtmlManager() {
    timeInHtml = 0;
  }
  
  public long getTimeInHtml() {
    return timeInHtml;
  }
  
  public void generateHtmlFromXml(Document document, File outputFile) throws IOException {
    generateHtmlFromXml(document, outputFile, FeedType.Catalog);
  }

  public void generateHtmlFromXml(Document document, File outputFile, FeedType feedType) throws IOException {
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtml()) {
      FileOutputStream fos = null;
      try {
        // create the same file as html
        long now = System.currentTimeMillis();
        JDOMSource source = new JDOMSource(document);
        String xmlFilename = outputFile.getAbsolutePath();
        File htmlFile = new File(getHtmlFilenameFromXmlFilename(xmlFilename));
        fos = new FileOutputStream(htmlFile);
        StreamResult streamResult = new StreamResult(fos);
        try {
          Transformer transformer = null;
          switch (feedType) {
            case MainCatalog:
              generateHeaderHtml(document,outputFile);
              transformer = JDOM.INSTANCE.getMainCatalogTransformer();
              break;
            case Catalog:
              transformer = JDOM.INSTANCE.getCatalogTransformer();
              break;
            case BookFullEntry:
              transformer = JDOM.INSTANCE.getBookFullEntryTransformer();
              break;
          }
          transformer.transform(source, streamResult);
        } catch (TransformerException e) {
          logger.error(Localization.Main.getText("error.cannotTransform",outputFile.getAbsolutePath()),e);
        }
        timeInHtml += (System.currentTimeMillis() - now);
      } finally {
        if (fos != null)
          fos.close();
      }
    }
  }

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

    public String getHtmlFilenameFromXmlFilename(String xmlFilename) {
    String htmlFilename = xmlFilename.substring(0, xmlFilename.length() - 4) + ".html";
    return htmlFilename;
  }
  


}
