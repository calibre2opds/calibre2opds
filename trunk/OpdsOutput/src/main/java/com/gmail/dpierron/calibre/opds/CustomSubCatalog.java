package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CustomSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(CustomSubCatalog.class);

  private String title;

  public CustomSubCatalog(List<Book> books, String title) {
    super(books);
    this.title = title;
    setStuffToFilterOut(new Vector<Object>() {{add("dummy");}}); // needed to make SubCatalog.isInDeepLevel() know that we're a deep level
  }

  @Override
  public Composite<Element, String> getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getBooks()))
      return null;

    String id = Integer.toHexString(title.hashCode());
    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_custom_" + id + ".xml");
    String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
    Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
    String urn = "calibre:custom:" + id;

    if (logger.isTraceEnabled())
      logger.trace("getSubCatalogEntry  Breadcrumbs=" + pBreadcrumbs.toString());

    // specify that this is a deep level
    String summary = Localization.Main.getText("deeplevel.summary", Summarizer.INSTANCE.getBookWord(getBooks().size()));
    if (logger.isDebugEnabled())
      logger.debug("making a deep level");
    boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
    Element entry = getSubCatalogLevel(pBreadcrumbs, getBooks(), getStuffToFilterOut(), title, summary, urn, filename, null,
        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
            getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + Icons.ICONFILE_CUSTOM :
            Icons.ICON_CUSTOM);
    String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, pBreadcrumbs.size() > 1);
    return new Composite<Element, String>(entry, urlInItsSubfolder);
  }
}
