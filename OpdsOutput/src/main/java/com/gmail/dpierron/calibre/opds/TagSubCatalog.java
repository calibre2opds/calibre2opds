package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.RemoveSelectedTagsFilter;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;

public abstract class TagSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(TagSubCatalog.class);

  private List<Tag> tags;
  private Map<Tag, List<Book>> mapOfBooksByTag;

  public TagSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  public TagSubCatalog(List<Book> books) {
    super(books);
  }

  @Override
  List<Book> filterOutStuff(List<Book> originalBooks) {
    List<Book> result = new LinkedList<Book>();
    Set<Tag> tagsToRemove = new TreeSet<Tag>();
    for (Object objectToFilterOut : stuffToFilterOut) {
      if (objectToFilterOut instanceof Tag)
        tagsToRemove.add((Tag) objectToFilterOut);
    }

    if (Helper.isNotNullOrEmpty(tagsToRemove)) {
      // copy the books list, before filtering it (so we don't modify the original books in the DataModel)
      List<Book> originalBooks2 = new LinkedList<Book>();
      for (Book book : originalBooks) {
        originalBooks2.add(book.copy());
      }
      originalBooks = originalBooks2;

      // copy all to result (we browse one collection, and modify the other)
      result.addAll(originalBooks);

      // first, filter unwanted stuff
      BookFilter filter = new RemoveSelectedTagsFilter(tagsToRemove);
      for (Book book : originalBooks) {
        if (!filter.didBookPassThroughFilter(book)) {
          result.remove(book);
        }
      }
    }
    return result;
  }

  List<Tag> getTags() {
    if (tags == null) {
      tags = new LinkedList<Tag>();
      for (Book book : getBooks()) {
        for (Tag tag : book.getTags()) {
          if (!tags.contains(tag))
            tags.add(tag);
        }
      }

      // sort the tags alphabetically
      Collections.sort(tags, new Comparator<Tag>() {

        public int compare(Tag o1, Tag o2) {
          String title1 = (o1 == null ? "" : o1.getName());
          String title2 = (o2 == null ? "" : o2.getName());
          return title1.compareTo(title2);
        }
      });
    }
    return tags;
  }

  public Map<Tag, List<Book>> getMapOfBooksByTag() {
    if (mapOfBooksByTag == null) {
      mapOfBooksByTag = new HashMap<Tag, List<Book>>();
      for (Book book : getBooks()) {
        for (Tag tag : book.getTags()) {
          List<Book> books = mapOfBooksByTag.get(tag);
          if (books == null) {
            books = new LinkedList<Book>();
            mapOfBooksByTag.put(tag, books);
          }
          books.add(book);
        }

      }
    }
    return mapOfBooksByTag;
  }

  private boolean makeTagDeep(Tag tag, List<Book> books) {
    if (tag == null)
      return false;
    if (Helper.isNullOrEmpty(tag.getName()))
      return false;
    if (books.size() < ConfigurationManager.INSTANCE.getCurrentProfile().getMinBooksToMakeDeepLevel())
      return false;

    String name = tag.getName().toUpperCase(Locale.ENGLISH);
    for (String tagToMakeDeep : ConfigurationManager.INSTANCE.getCurrentProfile().getTokenizedTagsToMakeDeep()) {
      if (tagToMakeDeep.contains("*")) {
        tagToMakeDeep = tagToMakeDeep.substring(0, tagToMakeDeep.indexOf('*'));
        if (name.startsWith(tagToMakeDeep))
          return true;
      } else {
        if (name.equals(tagToMakeDeep.toUpperCase(Locale.ENGLISH)))
          return true;
      }
    }
    return false;
  }

  Element getTag(Breadcrumbs pBreadcrumbs, Tag tag, String baseurn, String titleWhenCategorized) throws IOException {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + tag);

    CatalogContext.INSTANCE.getCallback().showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogContext.INSTANCE.getCallback().incStepProgressIndicatorPosition();

    List<Book> books = getMapOfBooksByTag().get(tag);
    if (Helper.isNullOrEmpty(books))
      return null;


    String basename = "tag_";
    String filename = getFilenamePrefix(pBreadcrumbs) + basename + tag.getId() + ".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);

    String title = (titleWhenCategorized != null ? titleWhenCategorized : tag.getName());
    String urn = baseurn + ":" + tag.getId();

    // sort books by title
    if (logger.isDebugEnabled())
      logger.debug("sorting " + books.size() + " books");
    sortBooksByTitle(books);

    // check if we need to make this tag deep
    if (makeTagDeep(tag, books)) {
      // specify that this is a deep level
      String summary = Localization.Main.getText("deeplevel.summary", Summarizer.INSTANCE.getBookWord(books.size()));
      if (logger.isDebugEnabled())
        logger.debug("making a deep level");
      if (logger.isDebugEnabled())
        logger.trace("getTag:  Breadcrumbs=" + pBreadcrumbs.toString());
      boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
      return getSubCatalogLevel(pBreadcrumbs, books, getStuffToFilterOutAnd(tag), title, summary, urn, filename, null,
          ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
              getCatalogManager().getPathToCatalogRoot(filename, weAreAlsoInSubFolder) + StanzaConstants.ICONFILE_TAGS :
              StanzaConstants.ICON_TAGS);
    } else {
      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeBooks(books);
      if (logger.isDebugEnabled())
        logger.debug("making a simple book list");
      logger.trace("getTag:  Breadcrumbs=" + pBreadcrumbs.toString());
      boolean weAreAlsoInSubFolder = pBreadcrumbs.size() > 1;
      return getListOfBooks(pBreadcrumbs, books, 0, title, summary, urn, filename, null,
          ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons() ?
              getCatalogManager().getPathToCatalogRoot(filename, true) + StanzaConstants.ICONFILE_TAGS :
              StanzaConstants.ICON_TAGS);
    }
  }

  abstract Element _getEntry(Breadcrumbs pBreadcrumbs) throws IOException;

  public Element getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getTags()))
      return null;

    logger.debug("getSubCatalogEntry:" + pBreadcrumbs.toString());
    return _getEntry(pBreadcrumbs);
  }

  public static SubCatalog getTagSubCatalog(List<Book> books) {
    if (Helper.isNotNullOrEmpty(ConfigurationManager.INSTANCE.getCurrentProfile().getSplitTagsOn()))
      return new TagTreeSubCatalog(books);
    else
      return new TagListSubCatalog(books);
  }

  public static SubCatalog getTagSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    if (Helper.isNotNullOrEmpty(ConfigurationManager.INSTANCE.getCurrentProfile().getSplitTagsOn()))
      return new TagTreeSubCatalog(stuffToFilterOut, books);
    else
      return new TagListSubCatalog(stuffToFilterOut, books);
  }
}
