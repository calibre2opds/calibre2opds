package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.StanzaConstants;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.BookRating;
import com.gmail.dpierron.calibre.datamodel.Option;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RatingsSubCatalog extends BooksSubCatalog {
  private List<BookRating> ratings;
  Map<BookRating, List<Book>> mapOfBooksByRating;

  public RatingsSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
  }
  
  public RatingsSubCatalog(List<Book> books) {
    super(books);
  }

  List<BookRating> getRatings() {
    if (ratings == null) {
      ratings = new Vector<BookRating>();
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
          books = new Vector<Book>();
          BookRating rating = book.getRating();
          if (rating!= null)
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

    if (books == null)
      books = new Vector<Book>();

    String basename = "rated_";
    String filename = getFilenamePrefix(pBreadcrumbs)+basename+rating.getId()+".xml";
    filename = SecureFileManager.INSTANCE.encode(filename);

    String title = LocalizationHelper.INSTANCE.getEnumConstantHumanName(rating);
    String urn = baseurn + ":" + rating.getId();

    // sort books by title
    sortBooksByTitle(books);

    // try and list the items to make the summary
    String summary = Summarizer.INSTANCE.summarizeBooks(books);
    // logger.trace("getAuthor  Breadcrumbs=" + pBreadcrumbs.toString());
    Element result = getListOfBooks(pBreadcrumbs,
                                    books,
                                    0,
                                    title,
                                    summary,
                                    urn,
                                    filename,
                                    null,
                                    // #751211: Use external icons option
                                    ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons()
                                            ? getCatalogManager().getPathToCatalogRoot(filename) + StanzaConstants.ICONFILE_RATING
                                            : StanzaConstants.ICON_RATING,
                                    Option.DONOTINCLUDE_RATING);
    return result;
  }

  public Element getSubCatalogEntry(Breadcrumbs pBreadcrumbs) throws IOException {
    if (Helper.isNullOrEmpty(getRatings()))
      return null;

    String filename = SecureFileManager.INSTANCE.encode(pBreadcrumbs.getFilename()+"_rating.xml");
    String title = Localization.Main.getText("rating.title");
    String urn = "calibre:rating";
    String summary = Localization.Main.getText("rating.summary", Summarizer.INSTANCE.getBookWord(getBooks().size()));

    File outputFile = getCatalogManager().storeCatalogFileInSubfolder(filename);
    FileOutputStream fos = null;
    Document document = new Document();
    try {
      fos = new FileOutputStream(outputFile);
      Element feed = FeedHelper.INSTANCE.getFeed(pBreadcrumbs, title, urn, summary);

      // list the entries (or split them)
      List<Element> result;
      result = new Vector<Element>();
      for (int i = 0; i < BookRating.sortedRatings().length; i++) {
        BookRating rating = BookRating.sortedRatings()[i];
        Breadcrumbs breadcrumbs = Breadcrumbs.addBreadcrumb(pBreadcrumbs, title, getCatalogManager().getCatalogFileUrlInItsSubfolder(filename));
        Element entry = getRatedBooks(breadcrumbs, rating, urn);
        if (entry != null)
          result.add(entry);
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

    boolean weAreAlsoInSubFolder = pBreadcrumbs.size()>1;
    return FeedHelper.INSTANCE.getEntry(title,
                                        urn,
                                        getCatalogManager().getCatalogFileUrlInItsSubfolder(filename, weAreAlsoInSubFolder),
                                        summary,
                                        // #751211: Use external icons option
                                        ConfigurationManager.INSTANCE.getCurrentProfile().getExternalIcons()
                                                ? getCatalogManager().getPathToCatalogRoot(filename,weAreAlsoInSubFolder) + StanzaConstants.ICONFILE_RATING
                                                : StanzaConstants.ICON_RATING);
  }

}
