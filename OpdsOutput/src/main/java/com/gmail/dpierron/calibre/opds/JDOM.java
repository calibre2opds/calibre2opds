package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public enum JDOM {
  INSTANCE;
  private final static Logger logger = Logger.getLogger(JDOM.class);

  private JDOMFactory factory;
  private XMLOutputter outputter;
  private XMLOutputter serializer;
  private TransformerFactory transformerFactory;
  private Transformer catalogTransformer;
  private Transformer headerTransformer;
  private Transformer mainTransformer;
  private SAXBuilder sb;

    public Transformer getHeaderTransformer() {
        if (headerTransformer == null) {
            try {
                headerTransformer = getTransformerFactory().newTransformer(new StreamSource(getClass().getResourceAsStream("header.xsl")));
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
    catalogTransformer.setParameter("i18n.backToMain", Localization.Main.getText("i18n.backToMain"));
    catalogTransformer.setParameter("i18n.downloads", Localization.Main.getText("i18n.downloads"));
    catalogTransformer.setParameter("i18n.links", Localization.Main.getText("i18n.links"));
    catalogTransformer.setParameter("i18n.downloadfile", Localization.Main.getText("i18n.downloadfile"));
    catalogTransformer.setParameter("i18n.coversection", Localization.Main.getText("i18n.coversection"));
    catalogTransformer.setParameter("i18n.summarysection", Localization.Main.getText("i18n.summarysection"));
    catalogTransformer.setParameter("i18n.downloadsection", Localization.Main.getText("i18n.downloadsection"));
    catalogTransformer.setParameter("i18n.relatedsection", Localization.Main.getText("i18n.relatedsection"));
    catalogTransformer.setParameter("i18n.linksection", Localization.Main.getText("i18n.linksection"));
    String dateGenerated = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale(ConfigurationManager.INSTANCE.getCurrentProfile().getLanguage())).format(new Date());
    catalogTransformer.setParameter("i18n.dateGenerated", Localization.Main.getText("i18n.dateGenerated", dateGenerated));
    catalogTransformer.setParameter("browseByCover", Boolean.toString(ConfigurationManager.INSTANCE.getCurrentProfile().getBrowseByCover()).toLowerCase());
  }

  public Transformer getCatalogTransformer() {
    if (catalogTransformer == null) {
      try {
        catalogTransformer = getTransformerFactory().newTransformer(new StreamSource(getClass().getResourceAsStream(getXsltFilename())));
        setParametersOnCatalog(catalogTransformer);
      } catch (TransformerConfigurationException e) {
        logger.error("getCatalogTransformer(): Error while configuring catalog transformer", e);
      }
    }
    return catalogTransformer;
  }

  private String getXsltFilename() {
    switch (ConfigurationManager.INSTANCE.getCurrentProfile().getCompatibilityTrick())
    {
    case TROOK:
            return "catalog-TROOK.xsl";
    default:
            return "catalog.xsl";
    }
  }

  public Transformer getMainCatalogTransformer() {
    if (mainTransformer == null) {
      try {
        mainTransformer = getTransformerFactory().newTransformer(new StreamSource(getClass().getResourceAsStream(getXsltFilename())));
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

  public List<Element> convertBookCommentToXhtml(String text) {
    List<Element> result = null;

    // if (logger.isTraceEnabled())
    //   logger.trace("Comment to convert: " + Database.INSTANCE.stringToHex(text));
    if (Helper.isNotNullOrEmpty(text)) {
      if (!text.startsWith("<")) {
        // plain text
        if (logger.isTraceEnabled())
          logger.trace("plain text");
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

      try {
        EntityResolver er = new EntityResolver() {
          public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (Helper.isNullOrEmpty(systemId))
              return null;
            // find out the filename
            String filename = systemId;
            int lastSlash = systemId.lastIndexOf('/');
            if (lastSlash > -1)
              filename = systemId.substring(lastSlash);
            InputStream is = getClass().getResourceAsStream("/dtd" + filename);
            if (is != null)
              return new InputSource(is);
            else {
              logger.warn("resolveEntity cannot find " + systemId);
              return null;
            }
          }
        };
        SAXBuilder sb = new SAXBuilder(false);
        sb.setEntityResolver(er);
        if (logger.isTraceEnabled())
          logger.trace("building doc");
        Document doc = sb.build(new StringReader(text));
        result = new ArrayList<Element>();
        result.add(doc.getRootElement());
      } catch (Exception ee) {
        if (logger.isTraceEnabled())
          logger.trace("caught exception in the xhtml conversion process", ee);
      }

      if (result != null) {
        if (logger.isTraceEnabled())
          logger.trace("returning XHTML");
      } else {
        // TODO  It would be nice to identify the book in the message
        // ITIMPI:  It appears that this can happen with empty text
        //          (added quotes around text to check)
        logger.warn("convertBookCommentToXhtml: Cannot convert text : '" + text + "'");
      }
    }
    return result;
  }

}
