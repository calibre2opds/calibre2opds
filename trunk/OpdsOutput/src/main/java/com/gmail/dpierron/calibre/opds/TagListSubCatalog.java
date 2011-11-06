package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class TagListSubCatalog extends TagSubCatalog {
  private final static Logger logger = Logger.getLogger(TagListSubCatalog.class);

  public TagListSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  public TagListSubCatalog(List<Book> books) {
    super(books);
  }

  @Override
  Composite<Element, String> _getEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename() + "_tags.xml");
    String title = Localization.Main.getText("tags.title");
    String urn = "calibre:tags";

    String summary = "";
    if (getTags().size() > 1)
      summary = Localization.Main.getText("tags.alphabetical", getTags().size());
    else if (getTags().size() == 1)
      summary = Localization.Main.getText("authors.alphabetical.single");

    return getListOfTags(pBreadcrumbs, getTags(), 0, null, title, summary, urn, filename, null);
  }

  private Composite<Element, String> getListOfTags(Breadcrumbs pBreadcrumbs,
      List<Tag> tags,
      int from,
      String guid,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {
    int catalogSize;
    Map<String, List<Tag>> mapOfTagsByLetter = null;
    boolean willSplit = splitOption != SplitOption.Paginate && (tags.size() > ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforeSplit());
    if (willSplit) {
      mapOfTagsByLetter = DataModel.splitTagsByLetter(tags);
      catalogSize = 0;
    } else
      catalogSize = tags.size();

    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);

    String filename = SecureFileManager.INSTANCE.decode(pFilename);
    if (Helper.isNotNullOrEmpty(guid))
      filename = filename + "_" + guid;
    if (from > 0) {
      int pos = filename.lastIndexOf(".xml");
      if (pos >= 0)
        filename = filename.substring(0, pos);
      filename = filename + "_" + pageNumber;
    }
    if (!filename.endsWith(".xml"))
      filename = filename + ".xml";

    logger.debug("getListOfTags: generating " + filename);
    filename = SecureFileManager.INSTANCE.encode(filename);

    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    String urlExt = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename);
    try {
      fos = new FileOutputStream(outputFile);

      Element feed = FeedHelper.INSTANCE.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

      // list the entries (or split them)
      List<Element> result;
      if (willSplit) {
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        logger.debug("calling getListOfTagsSplitByLetter");
        result = getListOfTagsSplitByLetter(breadcrumbs, mapOfTagsByLetter, guid, title, urn, pFilename);
      } else {
        logger.debug("no split by letter");
        result = new LinkedList<Element>();
        for (int i = from; i < tags.size(); i++) {
          if ((i - from) >= ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforePaginate()) {
            Element nextLink = getListOfTags(pBreadcrumbs, tags, i, guid, title, summary, urn, pFilename, splitOption).getFirstElement();
            result.add(0, nextLink);
            break;
          } else {
            Tag tag = tags.get(i);
            Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
            logger.debug("getTag:" + tag);
            Element entry = getTag(breadcrumbs, tag, urn, null);
            if (entry != null) {
              logger.debug("adding tag to the TROOK database:" + tag);
              result.add(entry);
              TrookSpecificSearchDatabaseManager.INSTANCE.addTag(tag, entry);
            }
          }
        }
      }

      // add the entries to the feed
      feed.addContent(result);

      // write the element to the file
      document.addContent(feed);
      JDOM.INSTANCE.getOutputter().output(document, fos);
    } finally {
      if (fos != null)
        fos.close();
    }

    // create the same file as html
    getHtmlManager().generateHtmlFromXml(document, outputFile);

    Element entry;
    boolean weAreAlsoInSubFolder = (pBreadcrumbs.size() > 1);
    String urlInItsSubfolder = getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, weAreAlsoInSubFolder);
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages)
        titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);
      else
        titleNext = Localization.Main.getText("title.lastpage");

      entry = FeedHelper.INSTANCE.getNextLink(urlExt, titleNext);
    } else {
      if (logger.isDebugEnabled())
        logger.trace("getListOfTags" + pBreadcrumbs.toString());

      entry = FeedHelper.INSTANCE.getCatalogEntry(title, urn, urlInItsSubfolder, summary, ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons
          () ?
          getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + Icons.ICONFILE_TAGS :
          Icons.ICON_TAGS);
    }
    return new Composite<Element, String>(entry, urlInItsSubfolder);
  }

  private List<Element> getListOfTagsSplitByLetter(Breadcrumbs pBreadcrumbs,
      Map<String, List<Tag>> mapOfTagsByLetter,
      String guid,
      String baseTitle,
      String baseUrn,
      String baseFilename) throws IOException {
    if (Helper.isNullOrEmpty(mapOfTagsByLetter))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle = sTitle + ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfTagsByLetter.keySet());
    for (String letter : letters) {
      // generate the letter file
      String baseFilenameCleanedUp = SecureFileManager.INSTANCE.decode(baseFilename);
      int pos = baseFilenameCleanedUp.indexOf(".xml");
      if (pos > -1)
        baseFilenameCleanedUp = baseFilenameCleanedUp.substring(0, pos);
      String letterFilename = baseFilenameCleanedUp + "_" + Helper.convertToHex(letter) + ".xml";
      letterFilename = SecureFileManager.INSTANCE.encode(letterFilename);

      String letterUrn = baseUrn + ":" + letter;

      List<Tag> tagsInThisLetter = mapOfTagsByLetter.get(letter);
      String letterTitle;
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.tag.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("tagword.title"),
                                                    letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);

      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeTags(tagsInThisLetter);

      Element element = null;
      if (tagsInThisLetter.size() > 0) {
        logger.debug("calling getListOfTags for the letter " + letter);
        element =
            getListOfTags(pBreadcrumbs, tagsInThisLetter, 0, guid, letterTitle, summary, letterUrn, letterFilename, SplitOption.SplitByLetter).getFirstElement();
      }

      if (element != null)
        result.add(element);
    }
    return result;
  }
}
