package com.gmail.dpierron.calibre.opds;
/**
 *  Class for defining methods that define a tag sub catalog
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;

public class TagListSubCatalog extends TagsSubCatalog {
  private final static Logger logger = Logger.getLogger(TagListSubCatalog.class);


  public TagListSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.TAGS_TYPE);
  }

  public TagListSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.TAGS_TYPE);
  }


  @Override
  //Composite<Element, String> getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
  Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    return getListOfTags(pBreadcrumbs,
                         getTags(),
                         pBreadcrumbs.size() > 1,
                         0,
                         Localization.Main.getText("tags.title"),
                         getTags().size() > 1 ? Localization.Main.getText("tags.alphabetical", getTags().size())
                                              : (getTags().size() == 1 ? Localization.Main.getText("authors.alphabetical.single") : "") ,
                         Constants.INITIAL_URN_PREFIX + getCatalogType() + getCatalogLevel(),
                         getCatalogBaseFolderFileName(),
                         null);
  }

//  private Composite<Element, String> getListOfTags(
      private Element getListOfTags(
      Breadcrumbs pBreadcrumbs,
      List<Tag> listtags,
      boolean inSubDir,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {
    int catalogSize;
    Map<String, List<Tag>> mapOfTagsByLetter = null;
    if (splitOption == null) {
      splitOption = ((maxSplitLevels > 0) && (from == 0)) ? SplitOption.SplitByLetter : SplitOption.Paginate;
      if (logger.isTraceEnabled())
        logger.trace("getListOfTags: splitOption was null - set to " + splitOption);
    }
    boolean willSplitByLetter = checkSplitByLetter(splitOption, listtags.size());
    if (willSplitByLetter) {
      mapOfTagsByLetter = DataModel.splitTagsByLetter(listtags);
      catalogSize = 0;
    } else
      catalogSize = listtags.size();

    if (from != 0) inSubDir = true;
    if (pBreadcrumbs.size() > 1) inSubDir = true;
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);

    String filename = pFilename + Constants.PAGE_DELIM + pageNumber ;
    logger.debug("getListOfTags: generating " + filename);
    String urlExt = optimizeCatalogURL(catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt);

    // list the entries (or split them)
    List<Element> result;
    if (willSplitByLetter) {
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
      logger.debug("calling getListOfTagsSplitByLetter");
      result = getListOfTagsSplitByLetter(breadcrumbs,
                                          mapOfTagsByLetter,
                                          inSubDir, title, urn, pFilename);
    } else {
      logger.debug("no split by letter");
      result = new LinkedList<Element>();
      for (int i = from; i < listtags.size(); i++) {
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          Element nextLink = getListOfTags(pBreadcrumbs,
                                           listtags,
                                           inSubDir,
                                           i,
                                           title, summary, urn, pFilename,
                                           splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption)/*.getFirstElement()*/;
          result.add(0, nextLink);
          break;
        } else {
          Tag tag = listtags.get(i);
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
          logger.debug("getTag:" + tag);
          Element entry = getTag(breadcrumbs, tag, urn, null);
          if (entry != null) {
            logger.debug("adding tag to the TROOK database:" + tag);
            result.add(entry);
            TrookSpecificSearchDatabaseManager.INSTANCE.addTag(tag, entry);
          }
        }
      } // End of tags for loop
    }

    feed.addContent(result);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
    String urlNext = catalogManager.getCatalogFileUrl(filename + Constants.PAGE_DELIM + (pageNumber+1) + Constants.XML_EXTENSION, inSubDir);

    Element entry;
    if (from > 0) {
      String titleNext;
      if (pageNumber != maxPages)
        titleNext = Localization.Main.getText("title.nextpage", pageNumber, maxPages);
      else
        titleNext = Localization.Main.getText("title.lastpage");

      entry = FeedHelper.getNextLink(urlExt, titleNext);
    } else {
      if (logger.isDebugEnabled())
        logger.trace("getListOfTags" + pBreadcrumbs.toString());
      if (title.equals("Science Fiction")) {
        int x = 1;
      }
      entry = FeedHelper.getCatalogEntry(title, urn, urlExt, summary,
          useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS);
    }
//    return new Composite<Element, String>(entry, urlExt);
    return entry;
  }

  private List<Element> getListOfTagsSplitByLetter(Breadcrumbs pBreadcrumbs,
      Map<String, List<Tag>> mapOfTagsByLetter,
      boolean inSubDir,
      String baseTitle,
      String baseUrn,
      String baseFilename) throws IOException {
    if (Helper.isNullOrEmpty(mapOfTagsByLetter))
      return null;

    String sTitle = baseTitle;
    if (Helper.isNotNullOrEmpty(sTitle))
      sTitle += ", ";

    List<Element> result = new LinkedList<Element>();
    SortedSet<String> letters = new TreeSet<String>(mapOfTagsByLetter.keySet());
    // assert baseFilename.endsWith(Constants.XML_EXTENSION);
    for (String letter : letters) {
      String letterFilename = Helper.getSplitString(baseFilename,letter, Constants.TYPE_SEPARATOR);
      String letterUrn = Helper.getSplitString(baseUrn,letter,Constants.URN_SEPARATOR);
      String letterTitle;
      if (letter.equals("_"))
        letterTitle = Localization.Main.getText("splitByLetter.tag.other");
      else
        letterTitle = Localization.Main.getText("splitByLetter.letter", Localization.Main.getText("tagword.title"),
                                                    letter.length() > 1 ? letter.substring(0,1) + letter.substring(1).toLowerCase() : letter);
      List<Tag> tagsInThisLetter = mapOfTagsByLetter.get(letter);
      String summary = Summarizer.INSTANCE.summarizeTags(tagsInThisLetter);

      Element element = null;
      if (tagsInThisLetter.size() > 0) {
        logger.debug("calling getListOfTags for the letter " + letter);
        element = getListOfTags(pBreadcrumbs,
                                tagsInThisLetter,
                                inSubDir,
                                0,
                                letterTitle,
                                summary, letterUrn, letterFilename,
                                checkSplitByLetter(letter))/*.getFirstElement()*/;
      }
      if (element != null)
        result.add(element);
    }
    return result;
  }
}
