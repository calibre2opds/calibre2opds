package com.gmail.dpierron.calibre.opds;
/**
 *  Abstract class that contains methos that are common to all
 *  types of tag subcatalog.
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.datamodel.filter.RemoveSelectedTagsFilter;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

public abstract class TagsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(TagsSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.INSTANCE.getLocale());

  private List<Tag> tags;
  private Map<Tag, List<Book>> mapOfBooksByTag;

  public TagsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }

  public TagsSubCatalog(List<Book> books) {
    super(books);
  }

  /**
   *
   * @param originalBooks   The list of books to check
   * @return                The list of books passing the criteria (may be an empty list if none found)
   */
  @Override
  List<Book> filterOutStuff(List<Book> originalBooks) {
    List<Book> result = originalBooks;

    Set<Tag> tagsToRemove = new TreeSet<Tag>();
    for (Object objectToFilterOut : getStuffToFilterOut()) {
      if (objectToFilterOut instanceof Tag)
        tagsToRemove.add((Tag) objectToFilterOut);
    }

    if (Helper.isNotNullOrEmpty(tagsToRemove)) {
      // make a copy of the books because RemoveSelectedTagsFilter actually removes tags from the Book objects
      result = new LinkedList<Book>();
      for (Book originalBook : originalBooks) {
        result.add(originalBook.copy());
      }
      result = FilterHelper.filter(new RemoveSelectedTagsFilter(tagsToRemove), result);
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
          String title1 = (o1 == null ? "" : o1.getName().toUpperCase());
          String title2 = (o2 == null ? "" : o2.getName().toUpperCase());
          return collator.compare(title1, title2);
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
    if (books.size() < currentProfile.getMinBooksToMakeDeepLevel())
      return false;

    String name = tag.getName().toUpperCase(Locale.ENGLISH);
    for (String tagToMakeDeep : currentProfile.getTokenizedTagsToMakeDeep()) {
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

  Element getTag(Breadcrumbs pBreadcrumbs,
                 Tag tag,
                 String baseurn,
                 String titleWhenCategorized) throws IOException {
    if (logger.isDebugEnabled())
      logger.debug(pBreadcrumbs + "/" + tag);

    CatalogContext.INSTANCE.callback.showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel())
      CatalogContext.INSTANCE.callback.incStepProgressIndicatorPosition();

    List<Book> books = getMapOfBooksByTag().get(tag);
    if (Helper.isNullOrEmpty(books))
      return null;

    // Tags are held at the level (i.e. not the top level)
    String filename = getCatalogBaseFolderFileNameId(Constants.TAG_TYPE, tag.getId());
    String title = (titleWhenCategorized != null ? titleWhenCategorized : tag.getName());
    String urn = baseurn + Constants.URN_SEPARATOR + tag.getId();

    // sort books by title
    if (logger.isDebugEnabled())
      logger.debug("sorting " + books.size() + " books");
    sortBooksByTitle(books);

    SplitOption splitOption = maxSplitLevels > 0 ? SplitOption.SplitByLetter : SplitOption.Paginate;
    // check if we need to make this tag deep
    if (makeTagDeep(tag, books)) {
      // specify that this is a deep level
      String summary = Localization.Main.getText("deeplevel.summary", Summarizer.INSTANCE.getBookWord(books.size()));
      if (logger.isDebugEnabled())
        logger.debug("getTag: Making a deep level for tag " + tag);
      if (logger.isDebugEnabled())
        logger.trace("getTag:  Breadcrumbs=" + pBreadcrumbs.toString());
      // String urlExt = optimizeCatalogURL(catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, true));
      // Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, tag.getName(), null);
      LevelSubCatalog level = new LevelSubCatalog(books, title);
      level.setCatalogLevel(Breadcrumbs.addBreadcrumb(pBreadcrumbs, tag.getName(), null));  // Create a brand new level using current breadcrumbs!
      level.setCatalogType("");
      level.setCatalogFolder("");
      level.setCatalogBaseFilename(filename);
      return level.getCatalog(pBreadcrumbs,
                              getStuffToFilterOutAnd(tag),   // Used to determine what's left!
                              true,                          // Always in sub-dir for a tag !
                              summary,
                              urn,
                              splitOption, useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS);
    } else {
      // try and list the items to make the summary
      String summary = Summarizer.INSTANCE.summarizeBooks(books);
      if (logger.isDebugEnabled())
        logger.debug("getTag: making a simple book list for tag " + tag);
      logger.trace("getTag:  Breadcrumbs=" + pBreadcrumbs.toString());
      return getListOfBooks(pBreadcrumbs,
                            books,
                            true,               // Always in sub-dir for tag
                            0,
                            title,
                            summary,
                            urn,
                            filename,
                            splitOption, useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS,
                            null);
    }
  }

//  abstract Composite<Element, String> getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException;
  abstract Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException;

}
