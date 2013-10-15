package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
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


public enum JDOM {
  INSTANCE;
  private final static Logger logger = Logger.getLogger(JDOM.class);
  private static final String CATALOG_XSL = "catalog.xsl";
  private static final String HEADER_XSL = "header.xsl";
  private static final String FULLENTRY_XSL = "fullentry.xsl";

  private JDOMFactory factory;
  private XMLOutputter outputter;
  private XMLOutputter serializer;
  private TransformerFactory transformerFactory;
  private Transformer bookFullEntryTransformer;
  private Transformer catalogTransformer;
  private Transformer headerTransformer;
  private Transformer mainTransformer;
  private SAXBuilder sb;

  public Transformer getHeaderTransformer() {
    if (headerTransformer == null) {
      try {
        headerTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.INSTANCE.getResourceAsStream(HEADER_XSL)));
        setParametersOnCatalog(headerTransformer);
        headerTransformer.setParameter("programName", Constants.PROGNAME);
        headerTransformer.setParameter("programVersion", Constants.PROGVERSION + Constants.BZR_VERSION);
        headerTransformer.setParameter("i18n.intro.line1", Localization.Main.getText("intro.line1"));
        headerTransformer.setParameter("intro.goal", Localization.Main.getText("intro.goal"));
        headerTransformer.setParameter("intro.wiki.title", Localization.Main.getText("intro.wiki.title"));
        headerTransformer.setParameter("intro.wiki.url", Localization.Main.getText("intro.wiki.url"));
        headerTransformer.setParameter("intro.team.title", Localization.Main.getText("intro.team.title"));
        headerTransformer.setParameter("intro.team.list1", Localization.Main.getText("intro.team.list1"));
        headerTransformer.setParameter("intro.team.list2", Localization.Main.getText("intro.team.list2"));
        headerTransformer.setParameter("intro.team.list3", Localization.Main.getText("intro.team.list3"));
        headerTransformer.setParameter("intro.team.list4", Localization.Main.getText("intro.team.list4"));
        headerTransformer.setParameter("intro.thanks.1", Localization.Main.getText("intro.thanks.1"));
        headerTransformer.setParameter("intro.thanks.2", Localization.Main.getText("intro.thanks.2"));
      } catch (TransformerConfigurationException e) {
        logger.error("getHeaderTransformer(): Error while configuring header transformer", e);
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
      this.jdomNamespace = org.jdom.Namespace.getNamespace(prefix, uri);
    }

    public org.jdom.Namespace getJdomNamespace() {
      return jdomNamespace;
    }
  }

  /**
   * Set parameters that are common to many the transformers we use.
   * @param catalogTransformer
   */
  private void setParametersOnCatalog(Transformer catalogTransformer) {
    double dh = ConfigurationManager.INSTANCE.getCurrentProfile().getCoverHeight();
    double dw = 2f / 3f * dh;
    long lh = (long) Math.floor(dh);
    long lw = (long) Math.floor(dw);
    catalogTransformer.setParameter("coverWidth", lw);
    catalogTransformer.setParameter("coverHeight", lh);
    dh = ConfigurationManager.INSTANCE.getCurrentProfile().getThumbnailHeight();
    dw = 2f / 3f * dh;
    lh = (long) Math.floor(dh);
    lw = (long) Math.floor(dw);
    catalogTransformer.setParameter("thumbWidth", lw);
    catalogTransformer.setParameter("thumbHeight", lh);
    catalogTransformer.setParameter("generateDownloads", Boolean.toString(ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateHtmlDownloads()).toLowerCase());
    catalogTransformer.setParameter("libraryTitle", ConfigurationManager.INSTANCE.getCurrentProfile().getCatalogTitle());
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
    String dateGenerated =
        DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale(ConfigurationManager.INSTANCE.getCurrentProfile().getLanguage())).format(new Date());
    catalogTransformer.setParameter("i18n.dateGenerated", Localization.Main.getText("i18n.dateGenerated", dateGenerated));
    catalogTransformer.setParameter("NTOWSE_BY_COVER", Boolean.toString(ConfigurationManager.INSTANCE.getCurrentProfile().getBrowseByCover()).toLowerCase());
    catalogTransformer.setParameter("generateIndex", Boolean.toString(ConfigurationManager.INSTANCE.getCurrentProfile().getGenerateIndex()).toLowerCase());
  }

  public Transformer getCatalogTransformer() {
    if (catalogTransformer == null) {
      try {
        catalogTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.INSTANCE.getResourceAsStream(CATALOG_XSL)));
        setParametersOnCatalog(catalogTransformer);
        catalogTransformer.setParameter("programName", "");  // Set to empty for all pages except top level
      } catch (TransformerConfigurationException e) {
        logger.error("getCatalogTransformer(): Error while configuring catalog transformer", e);

      }
    }
    return catalogTransformer;
  }

  public Transformer getBookFullEntryTransformer() {
    if (bookFullEntryTransformer == null) {
      try {
        bookFullEntryTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.INSTANCE.getResourceAsStream(FULLENTRY_XSL)));
        setParametersOnCatalog(bookFullEntryTransformer);
      } catch (TransformerConfigurationException e) {
        logger.error("getCatalogTransformer(): Error while configuring book full entry transformer", e);
        bookFullEntryTransformer = null;
      }
    }
    return bookFullEntryTransformer;
  }

  public Transformer getMainCatalogTransformer() {
    if (mainTransformer == null) {
      try {
        mainTransformer = getTransformerFactory().newTransformer(new StreamSource(ConfigurationManager.INSTANCE.getResourceAsStream(CATALOG_XSL)));
        setParametersOnCatalog(mainTransformer);
        mainTransformer.setParameter("programName", Constants.PROGNAME);
        mainTransformer.setParameter("programVersion", Constants.PROGVERSION + Constants.BZR_VERSION);
        mainTransformer.setParameter("i18n.intro.line1", Localization.Main.getText("intro.line1"));
        mainTransformer.setParameter("intro.goal", Localization.Main.getText("intro.goal"));
        mainTransformer.setParameter("intro.wiki.title", Localization.Main.getText("intro.wiki.title"));
        mainTransformer.setParameter("intro.wiki.url", Localization.Main.getText("intro.wiki.url"));
        mainTransformer.setParameter("intro.team.title", Localization.Main.getText("intro.team.title"));
        mainTransformer.setParameter("intro.team.list1", Localization.Main.getText("intro.team.list1"));
        mainTransformer.setParameter("intro.team.list2", Localization.Main.getText("intro.team.list2"));
        mainTransformer.setParameter("intro.team.list3", Localization.Main.getText("intro.team.list3"));
        mainTransformer.setParameter("intro.team.list4", Localization.Main.getText("intro.team.list4"));
        mainTransformer.setParameter("intro.thanks.1", Localization.Main.getText("intro.thanks.1"));
        mainTransformer.setParameter("intro.thanks.2", Localization.Main.getText("intro.thanks.2"));
      } catch (TransformerConfigurationException e) {
        logger.error("getMainCatalogTransformer(): Error while configuring catalog transformer", e);
        mainTransformer = null;
      }
    }
    return mainTransformer;
  }

  public TransformerFactory getTransformerFactory() {
    if (transformerFactory == null) {
      transformerFactory = TransformerFactory.newInstance();
    }
    return transformerFactory;
  }

  public void setTransformerFactory(TransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
  }

  public JDOMFactory getFactory() {
    if (factory == null) {
      factory = new DefaultJDOMFactory();
    }
    return factory;
  }

  public SAXBuilder getSaxBuilder() {
    if (sb == null) {
      sb = new SAXBuilder();
    }
    return sb;
  }

  public XMLOutputter getOutputter() {
    if (outputter == null)
      outputter = new XMLOutputter(Format.getPrettyFormat());
    return outputter;
  }

  public XMLOutputter getSerializer() {
    if (serializer == null)
      serializer = new XMLOutputter(Format.getCompactFormat());
    return serializer;
  }

  public Element rootElement(String name, Namespace namespace, Namespace... declaredNamespaces) {
    Element result = element(name, namespace);
    for (Namespace declaredNamespace : declaredNamespaces) {
      result.addNamespaceDeclaration(declaredNamespace.getJdomNamespace());
    }
    return result;
  }

  public Element element(String name, Namespace namespace) {
    Element result = getFactory().element(name);
    if (namespace != null)
      result.setNamespace(namespace.getJdomNamespace());
    return result;
  }

  public Element element(String name) {
    return element(name, Namespace.Atom);
  }

  public Element newParagraph() {
    return element("p", Namespace.Atom);
  }

  static Tidy
      tidyForTidyInputStream = null;

  /**
   * Routine to tidy up the HTML in the Summary field
   *
   * @param in
   * @return
   * @throws JDOMException
   * @throws IOException
   */
  public List<Element> tidyInputStream(InputStream in) throws JDOMException, IOException {
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

  public List<Element> convertBookCommentToXhtml(String text) {
    List<Element> result = null;

    // if (logger.isTraceEnabled())
    //   logger.trace("Comment to convert: " + Database.INSTANCE.stringToHex(text));
    if (Helper.isNotNullOrEmpty(text)) {
      if (!text.startsWith("<")) {
        // plain text
        if (logger.isTraceEnabled())
          logger.trace("convertBookCommentToXhtml: plain text");
        StringBuffer sb = new StringBuffer();
        if (Helper.isNotNullOrEmpty(text)) {
          List<String> strings = Helper.tokenize(text, "\n", true);
          for (String string : strings) {
            if (Helper.isNullOrEmpty(string))
              sb.append("<p />");
            else {
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
        logger.trace("convertBookCommentToXhtml: tidy the text");
      try {
        result = tidyInputStream(new ByteArrayInputStream(text.getBytes("utf-8")));
      } catch (JDOMParseException j) {
        if (logger.isDebugEnabled())
          logger.trace("convertBookCommentToXhtml: caught JDOMParseException in the tidy process");
        if (logger.isTraceEnabled())
          logger.trace( "" + j);
      } catch (Exception ee) {
        if (logger.isDebugEnabled())
          logger.debug("convertBookCommentToXhtml: caught exception in the tidy process", ee);
        tidyForTidyInputStream = null;    // Force a new clean object to be gebnerated for next time around
      } catch (Throwable t) {
        logger.error("convertBookCommentToXhtml: caught throwable in the tidy process", t);
        tidyForTidyInputStream = null;    // Force a new clean object to be gebnerated for next time around
      }

      if (result != null) {
        if (logger.isTraceEnabled())
          logger.trace("convertBookCommentToXhtml: returning XHTML");
      } else {
        if (Helper.isNotNullOrEmpty(text))
          logger.debug("convertBookCommentToXhtml: Cannot convert comment text\n" + text);
      }
    }
    return result;
  }

}
