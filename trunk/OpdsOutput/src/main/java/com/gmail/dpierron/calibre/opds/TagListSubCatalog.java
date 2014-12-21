package com.gmail.dpierron.calibre.opds;
/**
 *  Class for defining methods that define a tag sub catalog
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;

public class TagListSubCatalog extends TagsSubCatalog {
  private final static Logger logger = Logger.getLogger(TagListSubCatalog.class);


  public TagListSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.TAGLIST_TYPE);
  }

  public TagListSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.TAGLIST_TYPE);
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
      List<Tag> listTags,
      boolean inSubDir,
      int from,
      String title,
      String summary,
      String urn,
      String pFilename,
      SplitOption splitOption) throws IOException {

    if (from != 0) inSubDir = true;
    if (pBreadcrumbs.size() > 1) inSubDir = true;
    int pageNumber = Summarizer.INSTANCE.getPageNumber(from + 1);
    int catalogSize;
    String filename = pFilename + Constants.PAGE_DELIM + pageNumber ;
    logger.debug("getListOfTags: generating " + filename);
    Map<String, List<Tag>> mapOfTagsByLetter = null;
    String urlExt = optimizeCatalogURL(CatalogManager.INSTANCE.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir));
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /*inSubDir*/);

    if (splitOption == null) {
      splitOption = ((maxSplitLevels > 0) && (from == 0)) ? SplitOption.SplitByLetter : SplitOption.Paginate;
      if (logger.isTraceEnabled())
        logger.trace("getListOfTags: splitOption was null - set to " + splitOption);
    }
    // andle special case where split-by-letter depth exceeds tag name length
    if (splitOption == SplitOption.SplitByLetter) {
      while (listTags.size() > 0
           && pFilename.toUpperCase().endsWith(Constants.TYPE_SEPARATOR + listTags.get(0).getName().toUpperCase())) {
        Tag tag = listTags.get(0);
        listTags.remove(0);
        Element element;
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
        element = getTagEntry(breadcrumbs, tag, urn, "");
        assert element != null;
        if (element != null) {
          feed.addContent(element);
        }
      }
    }
    boolean willSplitByLetter = checkSplitByLetter(splitOption, listTags.size());
    if (willSplitByLetter) {
      mapOfTagsByLetter = DataModel.splitTagsByLetter(listTags);
      catalogSize = 0;
    } else
      catalogSize = listTags.size();
    int maxPages = Summarizer.INSTANCE.getPageNumber(catalogSize);

    // list the entries (or split them)
    List<Element> result;
    if (willSplitByLetter && listTags.size() > 1) {
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
      logger.debug("calling getListOfTagsSplitByLetter");
      result = getListOfTagsSplitByLetter(breadcrumbs,
                                          mapOfTagsByLetter,
                                          inSubDir,
                                          title,
                                          urn,
                                          pFilename);
    } else {
      logger.debug("no split by letter");
      result = new LinkedList<Element>();
      for (int i = from; i < listTags.size(); i++) {
        if ((splitOption != SplitOption.DontSplitNorPaginate) && ((i - from) >= maxBeforePaginate)) {
          Element nextLink = getListOfTags(pBreadcrumbs,
                                           listTags,
                                           inSubDir,
                                           i,
                                           title,
                                           summary,
                                           urn,
                                           pFilename,
                                           splitOption != SplitOption.DontSplitNorPaginate ? SplitOption.Paginate : splitOption)/*.getFirstElement()*/;
          result.add(0, nextLink);
          break;
        } else {
          Tag tag = listTags.get(i);
          Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
          logger.debug("getTagEntry:" + tag);
          Element entry = getTagEntry(breadcrumbs, tag, urn, null);
          if (entry != null) {
            logger.debug("adding tag to the TROOK database:" + tag);
            result.add(entry);
            TrookSpecificSearchDatabaseManager.INSTANCE.addTag(tag, entry);
          }
        }
      } // End of tags for loop
    }

    feed.addContent(result);

    Element entry;
      entry = createPaginateLinks(feed, filename, pageNumber, maxPages);
      createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);
      if (from == 0) {
      if (logger.isDebugEnabled())
        logger.trace("getListOfTags" + pBreadcrumbs.toString());
      if (title.equals("Science Fiction")) {
        int x = 1;
      }
      entry = FeedHelper.getCatalogEntry(title,
                                         urn,
                                         urlExt,
                                         summary,
                                         useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS);
    }
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

    if (pBreadcrumbs.size() > 1) inSubDir = true;
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
                                summary,
                                letterUrn,
                                letterFilename,
                                checkSplitByLetter(letter))/*.getFirstElement()*/;
      }
      if (element != null)
        result.add(element);
    }
    return result;
  }
}
