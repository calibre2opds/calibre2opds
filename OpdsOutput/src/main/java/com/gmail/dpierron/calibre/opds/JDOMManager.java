package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.tidy.Tidy;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class JDOMManager {
  private final static Logger logger = Logger.getLogger(JDOMManager.class);
  private static final String CATALOG_XSL = "catalog.xsl";
  private static final String HEADER_XSL = "header.xsl";
  private static final String FULLENTRY_XSL = "fullentry.xsl";

  private static JDOMFactory factory;
  private static XMLOutputter outputter;
  private static XMLOutputter serializer;
  private static TransformerFactory transformerFactory;
  private static Transformer bookFullEntryTransformer;
  private static Transformer catalogTransformer;
  private static Transformer headerTransformer;
  private static Transformer mainTransformer;
  private static SAXBuilder sb;

  public static void reset() {
    factory = null;
    outputter = null;
    serializer = null;
    transformerFactory = null;
    bookFullEntryTransformer = null;
    catalogTransformer = null;
    headerTransformer = null;
    mainTransformer = null;
    sb = null;
  }

  /**
   * Set the paremeters for creating the header.html file
   *
   * @return
   */
  public static Transformer getHeaderTransformer() {
    if (headerTransformer == null) {
      try {
        headerTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.getResourceAsStream(HEADER_XSL)));
        setParametersOnCatalog(headerTransformer);
        setIntroParameters(headerTransformer);
        // Add book count if not generating ALl Books (which gives count if present)
        // headerTransformer.setParameter("programName", Constants.PROGNAME);
        // headerTransformer.setParameter("programVersion", Constants.PROGVERSION + Constants.BZR_VERSION);
 //        headerTransformer.setParameter("bookCount", Localization.Main.getText("bookword.many", DataModel.getListOfBooks().size()));
         String dateGenerated =
             DateFormat.getDateInstance(DateFormat.DEFAULT, ConfigurationManager.getCurrentProfile().getLanguage()).format(new Date());
        headerTransformer.setParameter("i18n.dateGenerated",
             Constants.PROGNAME + " " + Constants.PROGVERSION + " " + Constants.BZR_VERSION + ": "
             + Localization.Main.getText("i18n.dateGenerated",dateGenerated)
             + "  ("+ Localization.Main.getText("bookword.many", DataModel.getListOfBooks().size()) +")");
      } catch (TransformerConfigurationException e) {
        logger.error("getHeaderTransformer(): Error while configuring header transformer", e);
        headerTransformer = null;
      }
    }
    return headerTransformer;
  }

  public enum Namespace {
    Atom("", "http://www.w3.org/2005/Atom"),
    Opds("opds", "http://opds-spec.org/2010/catalog"),
    Opf("opf", "http://www.idpf.org/2007/opf"),
    Dc("dc", "http://purl.org/dc/elements/1.1/"),
    DcTerms("dcterms", "http://purl.org/dc/terms"),
    Calibre("calibre", "http://calibre.kovidgoyal.net/2009/metadata"),
    Xhtml("xhtml", "http://www.w3.org/1999/xhtml");

    private org.jdom.Namespace jdomNamespace;

    private Namespace(String prefix, String uri) {
      jdomNamespace = org.jdom.Namespace.getNamespace(prefix, uri);
    }

    public org.jdom.Namespace getJdomNamespace() {
      return this.jdomNamespace;
    }
  }

  /**
   * Set parameters that are common to many the transformers we use.
   * @param catalogTransformer
   */
  private static void setParametersOnCatalog(Transformer catalogTransformer) {
    double dh = ConfigurationManager.getCurrentProfile().getCoverHeight();
    double dw = 2f / 3f * dh;
    long lh = (long) Math.floor(dh);
    long lw = (long) Math.floor(dw);
    catalogTransformer.setParameter("coverWidth", lw);
    catalogTransformer.setParameter("coverHeight", lh);
    dh = ConfigurationManager.getCurrentProfile().getThumbnailHeight();
    dw = 2f / 3f * dh;
    lh = (long) Math.floor(dh);
    lw = (long) Math.floor(dw);
    catalogTransformer.setParameter("thumbWidth", lw);
    catalogTransformer.setParameter("thumbHeight", lh);
    catalogTransformer.setParameter("generateDownloads", Boolean.toString(ConfigurationManager.getCurrentProfile().getGenerateHtmlDownloads()).toLowerCase());
    catalogTransformer.setParameter("libraryTitle", ConfigurationManager.getCurrentProfile().getCatalogTitle());
    catalogTransformer.setParameter("i18n.and", Localization.Main.getText("i18n.and"));
    catalogTransformer.setParameter("i18n.backToMain", Localization.Main.getText("i18n.backToMain"));
    catalogTransformer.setParameter("i18n.downloads", Localization.Main.getText("i18n.downloads"));
    catalogTransformer.setParameter("i18n.links", Localization.Main.getText("i18n.links"));
    catalogTransformer.setParameter("i18n.downloadfile", Localization.Main.getText("i18n.downloadfile"));
    catalogTransformer.setParameter("i18n.coversection", Localization.Main.getText("i18n.coversection"));
    catalogTransformer.setParameter("i18n.summarysection", Localization.Main.getText("i18n.summarysection"));
    catalogTransformer.setParameter("i18n.downloadsection", Localization.Main.getText("i18n.downloadsection"));
    catalogTransformer.setParameter("i18n.relatedsection", Localization.Main.getText("i18n.relatedsection"));
    catalogTransformer.setParameter("i18n.linksection", Localization.Main.getText("i18n.linksection"));
    catalogTransformer.setParameter("browseByCover", Boolean.toString(ConfigurationManager.getCurrentProfile().getBrowseByCover()).toLowerCase());
    catalogTransformer.setParameter("generateIndex", Boolean.toString(ConfigurationManager.getCurrentProfile().getGenerateIndex()).toLowerCase());
    // We only want to add the Date Generated to the bottom of each catalog page if
    // we have not elected to try and minimise the number of files changed each run
    // (it will still be added to the top page)
    // TODO:  decide if we never want this on each page?
    if (ConfigurationManager.getCurrentProfile().getMinimizeChangedFiles()) {
      catalogTransformer.setParameter("i18n.dateGenerated","");
    } else {
      String dateGenerated =
          DateFormat.getDateInstance(DateFormat.DEFAULT, ConfigurationManager.getCurrentProfile().getLanguage()).format(new Date());
      catalogTransformer.setParameter("i18n.dateGenerated", Localization.Main.getText("i18n.dateGenerated", dateGenerated));
    }
  }

  public static Transformer getCatalogTransformer() {
    if (catalogTransformer == null) {
      try {
        catalogTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.getResourceAsStream(CATALOG_XSL)));
        setParametersOnCatalog(catalogTransformer);
        catalogTransformer.setParameter("programName", "");  // Set to empty for all pages except top level
      } catch (TransformerConfigurationException e) {
        logger.error("getCatalogTransformer(): Error while configuring catalog transformer", e);
        catalogTransformer = null;
     }
    }
    return catalogTransformer;
  }

  /**
   * Transformer used on a Book fulle entry.
   *
   * @return
   */
  public static Transformer getBookFullEntryTransformer() {
    if (bookFullEntryTransformer == null) {
      try {
        bookFullEntryTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.getResourceAsStream(FULLENTRY_XSL)));
        setParametersOnCatalog(bookFullEntryTransformer);
      } catch (TransformerConfigurationException e) {
        logger.error("getCatalogTransformer(): Error while configuring book full entry transformer", e);
        bookFullEntryTransformer = null;
      }
    }
    return bookFullEntryTransformer;
  }

  /**
   * set the transformer that is used for the top level page.
   *
   * @return
   */
  public static Transformer getMainCatalogTransformer() {
    if (mainTransformer == null) {
      try {
        mainTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.getResourceAsStream(CATALOG_XSL)));
        setParametersOnCatalog(mainTransformer);
        setIntroParameters(mainTransformer);
      } catch (TransformerConfigurationException e) {
        logger.error("getMainCatalogTransformer(): Error while configuring catalog transformer", e);
        mainTransformer = null;
      }
    }
    return mainTransformer;
  }

  /**
   * Set parameters that reate to the 'about' information for calibre2opds
   *
   * @param transformer
   * @return
   */
  private static Transformer setIntroParameters (Transformer transformer) {
    if (transformer != null) {
      String dateGenerated =
          DateFormat.getDateInstance(DateFormat.DEFAULT, ConfigurationManager.getCurrentProfile().getLanguage()).format(new Date());
      transformer.setParameter("i18n.dateGenerated",
          Constants.PROGNAME + " " + Constants.PROGVERSION + " " + Constants.BZR_VERSION + ": "
              + Localization.Main.getText("i18n.dateGenerated",dateGenerated)
              + "  ("+ Localization.Main.getText("bookword.many", DataModel.getListOfBooks().size()) +")");
      boolean includeAbout =ConfigurationManager.getCurrentProfile().getIncludeAboutLink();
      transformer.setParameter("programName", Constants.PROGNAME);
      transformer.setParameter("programVersion", includeAbout ? Constants.PROGVERSION + Constants.BZR_VERSION : "");
      // transformer.setParameter("i18n.intro.line1", Localization.Main.getText("intro.line1"));
      transformer.setParameter("intro.goal", includeAbout ? Localization.Main.getText("intro.goal") : "");
      transformer.setParameter("intro.wiki.title", includeAbout ? Localization.Main.getText("intro.wiki.title") : "");
      transformer.setParameter("intro.wiki.url", includeAbout ? Localization.Main.getText("intro.wiki.url") : "");
      transformer.setParameter("intro.userguide", includeAbout ? Localization.Main.getText("gui.menu.help.userGuide") : "");
      transformer.setParameter("intro.userguide.url", includeAbout ? Constants.USERGUIDE_URL : "");
      transformer.setParameter("intro.developerguide", includeAbout ? Localization.Main.getText("gui.menu.help.developerGuide") : "");
      transformer.setParameter("intro.developerguide.url", includeAbout ? Constants.DEVELOPERGUIDE_URL : "");
      transformer.setParameter("intro.team.title", includeAbout ? Localization.Main.getText("intro.team.title") : "");
      transformer.setParameter("intro.team.list1", includeAbout ? Localization.Main.getText("intro.team.list1") : "");
      // transformer.setParameter("intro.team.list2", Localization.Main.getText("intro.team.list2"));
      transformer.setParameter("intro.team.list2", "");
      // transformer.setParameter("intro.team.list3", Localization.Main.getText("intro.team.list3"));
      transformer.setParameter("intro.team.list3", "");
      // transformer.setParameter("intro.team.list4", Localization.Main.getText("intro.team.list4"));
      transformer.setParameter("intro.team.list4", "");
      //  transformer.setParameter("intro.thanks.1", Localization.Main.getText("intro.thanks.1"));
      transformer.setParameter("intro.team.thanks.1", "");
      // transformer.setParameter("intro.thanks.2", Localization.Main.getText("intro.thanks.2"));
      transformer.setParameter("intro.team.thanks.2", "");
    }
    return transformer;
  }

  public static TransformerFactory getTransformerFactory() {
    if (transformerFactory == null) {
      transformerFactory = TransformerFactory.newInstance();
    }
    return transformerFactory;
  }

  public static void setTransformerFactory(TransformerFactory transformerFactory) {
    transformerFactory = transformerFactory;
  }

  public static JDOMFactory getFactory() {
    if (factory == null) {
      factory = new DefaultJDOMFactory();
    }
    return factory;
  }

  public static SAXBuilder getSaxBuilder() {
    if (sb == null) {
      sb = new SAXBuilder();
    }
    return sb;
  }

  public static XMLOutputter getOutputter() {
    if (outputter == null)
      outputter = new XMLOutputter(Format.getPrettyFormat());
    return outputter;
  }

  public static XMLOutputter getSerializer() {
    if (serializer == null)
      serializer = new XMLOutputter(Format.getCompactFormat());
    return serializer;
  }

  public static Element rootElement(String name, Namespace namespace, Namespace... declaredNamespaces) {
    Element result = element(name, namespace);
    for (Namespace declaredNamespace : declaredNamespaces) {
      result.addNamespaceDeclaration(declaredNamespace.getJdomNamespace());
    }
    return result;
  }

  public static Element element(String name, Namespace namespace) {
    Element result = getFactory().element(name);
    if (namespace != null)
      result.setNamespace(namespace.getJdomNamespace());
    return result;
  }

  public static Element element(String name) {
    return element(name, Namespace.Atom);
  }

  public static Element newParagraph() {
    return element("p", Namespace.Atom);
  }

  private static Tidy tidyForTidyInputStream = null;

  /**
   * Routine to tidy up the HTML in the Summary field
   *
   * @param in
   * @return
   * @throws JDOMException
   * @throws IOException
   */
  public static List<Element> tidyInputStream(InputStream in) throws JDOMException, IOException {
    List<Element> result = null;
    // initializing tidy
    if (logger.isTraceEnabled())
      logger.trace("tidyInputStream: initializing tidy");
    if (tidyForTidyInputStream == null) {
      tidyForTidyInputStream = new Tidy();
      tidyForTidyInputStream.setShowWarnings(false);
      tidyForTidyInputStream.setXmlOut(true);
      tidyForTidyInputStream.setInputEncoding("utf-8");
      tidyForTidyInputStream.setQuiet(true);
      tidyForTidyInputStream.setDropEmptyParas(false);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (logger.isTraceEnabled())
      logger.trace("tidyInputStream: parsing with Tidy");
    try {
      tidyForTidyInputStream.parseDOM(in, out);
    } finally {
      out.close();
    }
    String text2 = new String(out.toByteArray());
    SAXBuilder sb = new SAXBuilder(false);
    if (logger.isTraceEnabled())
      logger.trace("tidyInputStream: building doc");
    Document doc = sb.build(new StringReader(text2));
    Element html = doc.getRootElement();
    if (! html.getName().equalsIgnoreCase("html")) {
      if (logger.isTraceEnabled())
        logger.trace("tidyInputStream: no html tag found");
    } else {
      if (logger.isTraceEnabled())
        logger.trace("tidyInputStream: found html tag");
      for (Object o : html.getChildren()) {
        if (o instanceof Element) {
          Element child = (Element) o;
          if (! child.getName().equalsIgnoreCase("body")) {
            if (logger.isTraceEnabled())
              logger.trace("tidyInputStream: no body tag found");
          } else {
            if (logger.isTraceEnabled())
              logger.trace("tidyInputStream: found body tag");
            (result = new ArrayList<Element>()).addAll(child.getChildren());
          }
        }
      }
    }
    if (logger.isTraceEnabled())
      logger.trace("tidyInputStream: completed tidy");
    return result;
  }

  /**
   *
   * @param text      text to convert to HTML
   * @return
   */
  public static List<Element> convertHtmlTextToXhtml(String text) {
    List<Element> result = null;

    // if (logger.isTraceEnabled())
    //   logger.trace("Comment to convert: " + Database.stringToHex(text));
    if (Helper.isNotNullOrEmpty(text)) {
      if (!text.startsWith("<")) {
        // plain text
        if (logger.isTraceEnabled())logger.trace("convertHtmlTextToXhtml: plain text");
        StringBuffer sb = new StringBuffer();
        if (Helper.isNotNullOrEmpty(text)) {
          List<String> strings = Helper.tokenize(text, "\n", true);
          for (String string : strings) {
            if (Helper.isNullOrEmpty(string)) {
              sb.append("<p />");
            } else {
              sb.append("<p>");
              sb.append(string);
              sb.append("</p>\n");
            }
          }
        }
        text = sb.toString();
      }

      // tidy the text
      if (logger.isTraceEnabled())
        logger.trace("convertHtmlTextToXhtml: tidy the text");
      try {
        result = tidyInputStream(new ByteArrayInputStream(text.getBytes("utf-8")));
      } catch (JDOMParseException j) {
        if (logger.isDebugEnabled()) logger.trace("convertHtmlTextToXhtml: caught JDOMParseException in the tidy process");
        if (logger.isTraceEnabled()) logger.trace( "" + j);
        tidyForTidyInputStream = null;    // Force a new clean object to be gebnerated for next time around
      } catch (Exception ee) {
        if (logger.isDebugEnabled()) logger.debug("convertHtmlTextToXhtml: caught exception in the tidy process", ee);
        tidyForTidyInputStream = null;    // Force a new clean object to be gebnerated for next time around
      } catch (Throwable t) {
        logger.error("convertHtmlTextToXhtml: caught throwable in the tidy process", t);
        tidyForTidyInputStream = null;    // Force a new clean object to be gebnerated for next time around
      }

      if (result != null) {
        if (logger.isTraceEnabled())
          logger.trace("convertHtmlTextToXhtml: returning XHTML");
      } else {
        if (Helper.isNotNullOrEmpty(text))
          logger.debug("convertHtmlTextToXhtml: Cannot convert comment text\n" + text);
      }
    }
    return result;
  }
}
