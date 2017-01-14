package com.gmail.dpierron.calibre.opds;
/**
 *  Abstract class that contains methods that are common to all
 *  types of tag subcatalog.
 */
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.datamodel.filter.RemoveSelectedTagsFilter;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

public abstract class TagsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = LogManager.getLogger(TagsSubCatalog.class);
  private final static Collator collator = Collator.getInstance(ConfigurationManager.getLocale());

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
        Book newBook = originalBook.copy();
        result.add(newBook);
      }
      result = FilterHelper.filter(new RemoveSelectedTagsFilter(tagsToRemove), result);
    }
    return result;
  }

  /**
   * Get the list of tags we want to use
   * @return
   */
  List<Tag> getTags() {
    if (tags == null) {
      tags = new LinkedList<Tag>();
      for (Book book : getBooks()) {
        for (Tag tag : book.getTags()) {
          if (! CatalogManager.getTagsToIgnore().contains(tag)
          &&  ! tags.contains(tag)) {
            tags.add(tag);
          }
        }
      }
    }


      // sort the tags alphabetically
      Collections.sort(tags, new Comparator<Tag>() {

        public int compare(Tag o1, Tag o2) {
          return Helper.checkedCollatorCompareIgnoreCase(o1 == null ? "" : o1.getName(), o2.getName());
        }
      });
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

  /**
   *
   * @param tag
   * @param books
   * @return
   */
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

  static void sortBooksByAuthorAndTitle(List<Book> books) {
    Collections.sort(books, new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        String s1 = o1.getAuthorSort();
        String s2 = o2.getAuthorSort();
        if (! s1.equals(s2)) {
          return Helper.checkedCollatorCompareIgnoreCase(s1, s2, collator);
        }
        // If authors equal compare on title.
        return Helper.checkedCollatorCompareIgnoreCase(o1.getTitleToSplitByLetter(), o2.getTitleToSplitByLetter(), collator);
      }

    });
  }

  /**
   * Get the base filename that is used to store a given author
   *
   * Since we always hold a full list of authors at the top level the
   * name can be derived purely knowing the author involved.

   * @param tag
   * @return
   */
  public static String getTagFolderFilenameNoLevel(Tag tag) {
    return getCatalogBaseFolderFileNameIdNoLevelSplit(Constants.TAG_TYPE,tag.getId(), 100);
  }
  /**
   *    Get the base filename that is used to store a given author
   *    This version works within the given level
   * @param tag
   * @return
   */
  public String getTagFolderFilenameWithLevel (Tag tag) {
    if (currentProfile.getDontSplitTagsOn()) {
      return getCatalogBaseFolderFileNameIdSplit(Constants.TAG_TYPE, tag.getId(), 100);
    } else {
      return getCatalogBaseFolderFileNameIdSplit(Constants.TAG_TYPE, tag.getId(), 100);
    }
  }

  /**
   * Get the details for a specific tag
   *
   * @param pBreadcrumbs
   * @param tagObject
   * @param opts   baseurn
   *               titleWhenCategorized
   * @return
   * @throws IOException
   */
  // public Element getTagEntry(Breadcrumbs pBreadcrumbs, Tag tag, String baseurn, String titleWhenCategorized) throws IOException {
  public Element getDetailedEntry(Breadcrumbs pBreadcrumbs,
                                  Object tagObject,
                                  Object... opts) throws IOException {

    assert pBreadcrumbs != null;
    assert tagObject != null && tagObject.getClass().equals(Tag.class);
    Tag tag = (Tag)tagObject;
    assert opts[0] != null && opts[0].getClass().equals(String.class);
    String baseurn = (String)opts[0];
    if (opts[1] != null) assert opts[1].getClass().equals(String.class);
    String titleWhenCategorized = (String)opts[1];

    if (logger.isDebugEnabled()) logger.debug("getObjectEntry: Entry (" + pBreadcrumbs + "/" + tag + ")");

    tag.setDone();

    CatalogManager.callback.showMessage(pBreadcrumbs.toString());
    if (!isInDeepLevel()) {
      CatalogManager.callback.incStepProgressIndicatorPosition();
    }
    List<Book> books = getMapOfBooksByTag().get(tag);
    if (Helper.isNullOrEmpty(books)) {
      if (logger.isDebugEnabled()) logger.debug("getObjectEntry: Exit (no books fond for tag");
      return null;
    }
    // Tags are held at each level (i.e. not the top level)
    String filename = getTagFolderFilenameWithLevel(tag);
    String title = (titleWhenCategorized != null ? titleWhenCategorized : tag.getName());
    String urn = baseurn + Constants.URN_SEPARATOR + tag.getId();

    // Sort books according to user requirements
    if (logger.isDebugEnabled()) logger.debug("sorting " + books.size() + " books" + (currentProfile.getSortTagsByAuthor() ? " (withon author)" : ""));
    if (currentProfile.getSortTagsByAuthor()) {
      // #c2o-212  Sort tag books by author and then title
      sortBooksByAuthorAndTitle(books);
    } else {
      // sort books by title only
      sortBooksByTitle(books);
    }

    SplitOption splitOption = maxSplitLevels > 0 ? SplitOption.SplitByLetter : SplitOption.Paginate;
    // check if we need to make this tag deep
    if (makeTagDeep(tag, books)) {
      // specify that this is a deep level
      String summary = Localization.Main.getText("deeplevel.summary", Summarizer.getBookWord(books.size()));
      if (logger.isDebugEnabled()) {
        logger.debug("getObjectEntry: Making a deep level for tag " + tag);
        logger.trace("getObjectEntry:  Breadcrumbs=" + pBreadcrumbs.toString());
      }
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
      String summary = Summarizer.summarizeBooks(books);
      if (logger.isDebugEnabled()) {
        logger.debug("getObjectEntry: making a simple book list for tag " + tag);
        if (logger.isTraceEnabled()) logger.trace("getObjectEntry:  Breadcrumbs=" + pBreadcrumbs.toString());
      }

      if (currentProfile.getTagBooksNoSplit() && splitOption == SplitOption.SplitByLetter) {
        splitOption = SplitOption.Paginate;
      }

      Element element = getListOfBooks(pBreadcrumbs,
                            books,
                            true,               // Always in sub-dir for tag
                            0,
                            title,
                            summary,
                            urn,
                            filename,
                            splitOption, useExternalIcons ? getIconPrefix(true) + Icons.ICONFILE_TAGS : Icons.ICON_TAGS,
                            null);
      if (logger.isDebugEnabled()) logger.debug("getObjectEntry: Exit");
      return element;
    }
  }

//  abstract Composite<Element, String> getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException;
  abstract Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException;

}
