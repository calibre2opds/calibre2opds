package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.BookRating;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.*;

public class RatingsSubCatalog extends BooksSubCatalog {
  private final static Logger logger = Logger.getLogger(RatingsSubCatalog.class);
  private List<BookRating> ratings;
  Map<BookRating, List<Book>> mapOfBooksByRating;

  public RatingsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    setCatalogType(Constants.RATED_TYPE);
  }

  public RatingsSubCatalog(List<Book> books) {
    super(books);
    setCatalogType(Constants.RATED_TYPE);
  }

  List<BookRating> getRatings() {
    if (ratings == null) {
      ratings = new LinkedList<BookRating>();
      for (Book book : getBooks()) {
        if (!ratings.contains(book.getRating()))
          ratings.add(book.getRating());
      }

      // sort the ratings
      Collections.sort(ratings, new Comparator<BookRating>() {

        public int compare(BookRating o1, BookRating o2) {
          int val1 = (o1 == null ? -1 : o1.getValue());
          int val2 = (o2 == null ? -1 : o2.getValue());
          return new Integer(val1).compareTo(new Integer(val2));
        }
      });

    }
    return ratings;
  }

  private Map<BookRating, List<Book>> getMapOfBooksByRating() {
    if (mapOfBooksByRating == null) {
      mapOfBooksByRating = new HashMap<BookRating, List<Book>>();
      for (Book book : getBooks()) {
        List<Book> books = mapOfBooksByRating.get(book.getRating());
        if (books == null) {
          books = new LinkedList<Book>();
          BookRating rating = book.getRating();
          if (rating != null)
            mapOfBooksByRating.put(rating, books);
        }
        books.add(book);
      }
    }
    return mapOfBooksByRating;
  }

  private Element getRatedBooks(Breadcrumbs pBreadcrumbs, BookRating rating, String baseurn) throws IOException {
    List<Book> books = getMapOfBooksByRating().get(rating);
    if (Helper.isNullOrEmpty(books))
      return null;

    boolean inSubDir = getCatalogLevel().length() > 0 || pBreadcrumbs.size() > 1;
    if (books == null)
      books = new LinkedList<Book>();

    String filename = getCatalogBaseFolderFileName() + Constants.TYPE_SEPARATOR + rating.getId();
    String title = LocalizationHelper.INSTANCE.getEnumConstantHumanName(rating);
    String urn = baseurn + Constants.URN_SEPARATOR + rating.getId();

    // sort books by title
    sortBooksByTitle(books);

    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(books);
    // logger.trace("getAuthor  Breadcrumbs=" + pBreadcrumbs.toString());
    SplitOption splitOption = maxSplitLevels > 0 ? SplitOption.SplitByLetter : SplitOption.Paginate;
    Element result = getListOfBooks(pBreadcrumbs,
                                    books,
                                    true,
                                    0,
                                    title,
                                    summary,
                                    urn,
                                    filename,
                                    splitOption,
                                    // #751211: Use external icons option
                                    useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_RATING : Icons.ICON_RATING, null, Option.DONOTINCLUDE_RATING);
    return result;
  }

  public Element getCatalog(Breadcrumbs pBreadcrumbs, boolean inSubDir) throws IOException {
    if (Helper.isNullOrEmpty(getRatings()))
      return null;

    String filename = getCatalogBaseFolderFileName();
    String title = Localization.Main.getText("rating.title");
    String urn = Constants.INITIAL_URN_PREFIX + getCatalogType();
    String summary = Localization.Main.getText("rating.summary", Summarizer.INSTANCE.getBookWord(getBooks().size()));

    String urlExt = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    Element feed = FeedHelper.getFeedRootElement(pBreadcrumbs, title, urn, urlExt, true /* inSubDir */);

    // list the entries (or split them)
    List<Element> result;
    result = new LinkedList<Element>();
    for (int i = 0; i < BookRating.sortedRatings().length; i++) {
      BookRating rating = BookRating.sortedRatings()[i];
      Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, urlExt);
      Element entry = getRatedBooks(breadcrumbs, rating, urn);
      if (entry != null)
        result.add(entry);
    }

    feed.addContent(result);
    createFilesFromElement(feed, filename, HtmlManager.FeedType.Catalog);

    String urlInItsSubfolder = catalogManager.getCatalogFileUrl(filename + Constants.XML_EXTENSION, inSubDir);
    Element result2 = FeedHelper.getCatalogEntry(title,
                                                 urn,
                                                 urlInItsSubfolder,
                                                 summary,
                                                 // #751211: Use external icons option
                                                 useExternalIcons ? getIconPrefix(inSubDir) + Icons.ICONFILE_RATING : Icons.ICON_RATING);
    return result2;
  }

}
