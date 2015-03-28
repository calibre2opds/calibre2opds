package com.gmail.dpierron.calibre.datamodel;

/**
 * Class that supports the data model that is used within Calibre2opds
 *
 * The data model is largely determined by the Calibre database structure.
 *
 * NOTE:   There should only ever be one instance of this object, so all
 *         global variables and methods are declared static
 */
import com.gmail.dpierron.calibre.database.Database;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import com.gmail.dpierron.tools.i18n.Localization;
import org.apache.log4j.Logger;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class DataModel {

  private final static Logger logger = Logger.getLogger(DataModel.class);

  protected static final String IMPLICIT_LANGUAGE_TAG_PREFIX = "Lang:";

  private static Map<String, List<EBookFile>> mapOfEBookFilesByBookId;
  private static Map<String, List<Publisher>> mapOfPublishersByBookId;
  private static Map<String, List<Author>> mapOfAuthorsByBookId;
  private static Map<String, List<Tag>> mapOfTagsByBookId;
  private static Map<String, List<Series>> mapOfSeriesByBookId;
  private static Map<String, List<String>> mapOfCommentsByBookId;
  // private static Map<String, List<Tag>> mapOfCustomTagsByBookId;
  // private static Map<String, List<Series>> mapOfCustomSeriesByBookId;

  private static List<Book> listOfBooks;
  private static Map<String, Book> mapOfBooks;

  private static List<Tag> listOfTags;
  private static Map<String, Tag> mapOfTags;
  private static Map<Tag, List<Book>> mapOfBooksByTag;
  // private static List<Tag> listOfCustomTags;

  private static List<Author> listOfAuthors;
  private static Map<String, Author> mapOfAuthors;
  private static Map<Author, List<Book>> mapOfBooksByAuthor;

  private static List<Series> listOfSeries;
  private static Map<String, Series> mapOfSeries;
  private static Map<Series, List<Book>> mapOfBooksBySeries;
  // private static List<Series> listOfCustomSeries;

  private static List<BookRating> listOfRatings;
  private static Map<String, BookRating> mapOfRatings;
  private static Map<BookRating, List<Book>> mapOfBooksByRating;

  private static List<Publisher> listOfPublishers;
  private static Map<String, Publisher> mapOfPublishers;
  private static Map<Publisher, List<Book>> mapOfBooksByPublisher;

  private static Map<String, Language> mapOfLanguagesById;
  private static Map<String, Language> mapOfLanguagesByIsoCode;
  private static Map<String, String> mapOfSavedSearches;

  private static List<CustomColumnType> listOfCustomColumnTypes;
  // private static List<CustomColumnType> listOfCustomColumnTypesReferenced;
  private static Map<String, List <CustomColumnValue>> mapOfCustomColumnValuesByBookId;

  private static Map<Locale, NoiseWord> mapOfNoisewords;

  private static boolean useLanguagesAsTags = true;
  private static boolean librarySortAuthor = true;
  private static boolean librarySortTitle = true;
  private static boolean librarySortSeries = true;

  public static void reset() {
    mapOfEBookFilesByBookId = null;
    mapOfPublishersByBookId = null;
    mapOfAuthorsByBookId = null;
    mapOfTagsByBookId = null;
    mapOfSeriesByBookId = null;
    mapOfCommentsByBookId = null;
    listOfBooks = null;
    mapOfBooks = null;
    listOfTags = null;
    mapOfTags = null;
    mapOfBooksByTag = null;
    listOfAuthors = null;
    mapOfAuthors = null;
    mapOfBooksByAuthor = null;
    listOfSeries = null;
    mapOfSeries = null;
    mapOfBooksBySeries = null;
    listOfRatings = null;
    mapOfRatings = null;
    mapOfBooksByRating = null;
    listOfPublishers = null;
    mapOfPublishers = null;
    mapOfBooksByPublisher = null;
    mapOfLanguagesById = null;
    mapOfLanguagesByIsoCode = null;
    mapOfSavedSearches = null;
    listOfCustomColumnTypes = null;
    // listOfCustomColumnTypesReferenced = null;
    mapOfCustomColumnValuesByBookId = null;
    // listOfCustomTags = null;
    // listOfCustomSeries = null;

    // reset the database
    Database.reset();
  }

  /**
   * The following loads up data in a number of different views
   * to help with more efficient access to it later in the run.
   *
   * Some values that are optional are only loaded on demand when
   * an attempt is amde to access their data set.
   */
  public static void preloadDataModel() {
    // Load reference data from database
    getMapOfLanguagesById();
    getListOfCustomColumnTypes();
    // listOfCustomColumnTypesReferenced = listOfCustomColumnTypes;  // Initialise to full list

    // Build up cross-reference lookups by bookid
    getMapOfEBookFilesByBookId();
    getMapOfAuthorsByBookId();
    getMapOfTagsByBookId();
    getMapOfSeriesByBookId();
    getMapOfCommentsByBookId();

    getListOfTags();
    getListOfAuthors();
    getListOfSeries();
    getListOfBooks();
    // Build simple lookups for main data objects by id
    // (these will always be needed so get it donw ASAP)
    getMapOfBooks();
    getMapOfTags();
    getMapOfAuthors();
    getMapOfSeries();

    // Build up cross-reference list by other types
    generateImplicitLanguageTags();   // NOTE:  Must be done befoe generate BooksByTag listing
    getMapOfBooksByTag();
    getMapOfBooksByAuthor();
    getMapOfBooksBySeries();
    getMapOfBooksByRating();
  }

  /**
   * This list should not be very large so we do not mind loading all of it every time
   * @return
   */
  public static List<CustomColumnType> getListOfCustomColumnTypes () {
    if (listOfCustomColumnTypes == null) {
      listOfCustomColumnTypes = Database.getlistOfCustoColumnTypes();
    }
    return listOfCustomColumnTypes;
  }

  /**
   * Get list of Custom Column Values
   *
   * Since it highly likely that not all custom columns will be referenced,
   * the loading should be delayed until we know which ones are required for
   * this parituclar run to both improve performance and reduce RAM usage.
   *
   * @return
   */
  public static Map<String, List<CustomColumnValue>> getMapOfCustomColumnValuesByBookId() {
    if (mapOfCustomColumnValuesByBookId == null) {
       mapOfCustomColumnValuesByBookId = Database.getMapofCustomColumnValuesbyBookId(getListOfCustomColumnTypes());
    }
    return mapOfCustomColumnValuesByBookId;
  }

  public static Map<String, String> getMapOfSavedSearches() {
    if (mapOfSavedSearches == null) {
      mapOfSavedSearches = Database.getMapOfSavedSearches();
    }
    return mapOfSavedSearches;
  }


  public static Map<String, Language> getMapOfLanguagesById() {
    if (mapOfLanguagesById == null) {
      Composite<Map<String, Language>, Map<String, Language>> result = Database.getMapsOfLanguages();
      mapOfLanguagesById = result.getFirstElement();
      mapOfLanguagesByIsoCode = result.getSecondElement();
    }
    return mapOfLanguagesById;
  }

  public static Map<String, Language> getMapOfLanguagesByIsoCode() {
    if (mapOfLanguagesByIsoCode == null) {
      getMapOfLanguagesById();
    }
    return mapOfLanguagesById;
  }

  /**
   * Get the list of formats that should exist for eah book
   *
   * @return
   */
  public static Map<String, List<EBookFile>> getMapOfEBookFilesByBookId() {
    if (mapOfEBookFilesByBookId == null) {
      mapOfEBookFilesByBookId = Database.getMapOfEBookFilesByBookId();
    }
    return mapOfEBookFilesByBookId;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Author>> getMapOfAuthorsByBookId() {
    if (mapOfAuthorsByBookId == null) {
      mapOfAuthorsByBookId = Database.getMapOfAuthorsByBookId();
    }
    return mapOfAuthorsByBookId;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Publisher>> getMapOfPublishersByBookId() {
    if (mapOfPublishersByBookId == null) {
      mapOfPublishersByBookId = Database.listPublishersByBookId();
    }
    return mapOfPublishersByBookId;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Tag>> getMapOfTagsByBookId() {
    if (mapOfTagsByBookId == null) {
      mapOfTagsByBookId = Database.getMapOfTagsByBookId();
    }
    return mapOfTagsByBookId;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<Series>> getMapOfSeriesByBookId() {
    if (mapOfSeriesByBookId == null) {
      mapOfSeriesByBookId = Database.getMapOfSeriesByBookId();
    }
    return mapOfSeriesByBookId;
  }

  /**
   *
   * @return
   */
  public static Map<String, List<String>> getMapOfCommentsByBookId() {
    if (mapOfCommentsByBookId == null) {
      mapOfCommentsByBookId = Database.getMapOfCommentsByBookId();
    }
    return mapOfCommentsByBookId;
  }

  /**
   *
   * @return
   */
  public static List<Book> getListOfBooks() {
    if (listOfBooks == null) {
      listOfBooks = Database.listBooks();
    }
    return listOfBooks;
  }

  public static Map<String, Book> getMapOfBooks() {
    if (mapOfBooks == null) {
      mapOfBooks = new HashMap<String, Book>();
      for (Book book : getListOfBooks()) {
        mapOfBooks.put(book.getId(), book);
      }
    }
    return mapOfBooks;
  }

  public static List<Tag> getListOfTags() {
    if (listOfTags == null) {
      listOfTags = Database.listTags();
    }
    return listOfTags;
  }

  /**
   * Build up a map of tags by their tag id
   *
   * @return
   */
  public static Map<String, Tag> getMapOfTags() {
    if (mapOfTags == null) {
      mapOfTags = new HashMap<String, Tag>();
      for (Tag tag : getListOfTags()) {
        mapOfTags.put(tag.getId(), tag);
      }
    }
    return mapOfTags;
  }

  /**
   * Add a tag to the list of tags
   *
   * @param tag
   */
  public static void addTag(Tag tag) {
    if (getListOfTags().contains(tag)) {
      return;
    }
    getListOfTags().add(tag);
    getMapOfTags().put(tag.getId(), tag);
  }

  /**
   * Get the mapping of tags to Book Id.
   * @return
   */
  public static Map<Tag, List<Book>> getMapOfBooksByTag() {
    if (mapOfBooksByTag == null) {
      mapOfBooksByTag = new HashMap<Tag, List<Book>>();
      for (Book book : getListOfBooks()) {
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

  public static List<Author> getListOfAuthors() {
    if (listOfAuthors == null) {
      listOfAuthors = Database.listAuthors();
    }
    return listOfAuthors;
  }

  public static Map<String, Author> getMapOfAuthors() {
    if (mapOfAuthors == null) {
      mapOfAuthors = new HashMap<String, Author>();
      for (Author author : getListOfAuthors()) {
        mapOfAuthors.put(author.getId(), author);
      }
    }
    return mapOfAuthors;
  }

  public static Map<Author, List<Book>> getMapOfBooksByAuthor() {
    if (mapOfBooksByAuthor == null) {
      mapOfBooksByAuthor = new HashMap<Author, List<Book>>();
      for (Book book : getListOfBooks()) {
        for (Author author : book.getAuthors()) {
          List<Book> books = mapOfBooksByAuthor.get(author);
          if (books == null) {
            books = new LinkedList<Book>();
            mapOfBooksByAuthor.put(author, books);
          }
          books.add(book);
        }
      }
    }

    return mapOfBooksByAuthor;
  }

  public static List<Series> getListOfSeries() {
    if (listOfSeries == null) {
      listOfSeries = Database.listSeries();
    }
    return listOfSeries;
  }

  public static Map<String, Series> getMapOfSeries() {
    if (mapOfSeries == null) {
      mapOfSeries = new HashMap<String, Series>();
      for (Series serie : getListOfSeries()) {
        mapOfSeries.put(serie.getId(), serie);
      }
    }
    return mapOfSeries;
  }

  public static Map<Series, List<Book>> getMapOfBooksBySeries() {
    if (mapOfBooksBySeries == null) {
      mapOfBooksBySeries = new HashMap<Series, List<Book>>();
      for (Book book : getListOfBooks()) {
        List<Book> books = mapOfBooksBySeries.get(book.getSeries());
        if (books == null) {
          books = new LinkedList<Book>();
          Series series = book.getSeries();
          if (series != null)
            mapOfBooksBySeries.put(series, books);
        }
        books.add(book);
      }
    }
    return mapOfBooksBySeries;
  }

  public static Map<BookRating, List<Book>> getMapOfBooksByRating() {
    if (mapOfBooksByRating == null) {
      mapOfBooksByRating = new HashMap<BookRating, List<Book>>();
      for (Book book : getListOfBooks()) {
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

  public static List<Publisher> getListOfPublishers() {
    if (listOfPublishers == null) {
      listOfPublishers = Database.listPublishers();
    }
    return listOfPublishers;
  }

  public static Map<String, Publisher> getMapOfPublishers() {
    if (mapOfPublishers == null) {
      mapOfPublishers = new HashMap<String, Publisher>();
      for (Publisher publisher : getListOfPublishers()) {
        mapOfPublishers.put(publisher.getId(), publisher);
      }
    }
    return mapOfPublishers;
  }

  public static Map<Publisher, List<Book>> getMapOfBooksByPublisher() {
    if (mapOfBooksByPublisher == null) {
      mapOfBooksByPublisher = new HashMap<Publisher, List<Book>>();
      for (Book book : getListOfBooks()) {
        Publisher publisher = book.getPublisher();
        List<Book> books = mapOfBooksByPublisher.get(publisher);
        if (books == null) {
          books = new LinkedList<Book>();
          mapOfBooksByPublisher.put(publisher, books);
        }
        books.add(book);
      }
    }
    return mapOfBooksByPublisher;
  }


  public static Map<String, List<Book>> splitBooksByLetter(List<Book> books) {
    Comparator<Book> comparator = new Comparator<Book>() {

      public int compare(Book o1, Book o2) {
        String title1 = (o1 == null ? "" : o1.getTitleToSplitByLetter());
        String title2 = (o2 == null ? "" : o2.getTitleToSplitByLetter());
        return title1.compareTo(title2);
      }
    };

    return splitByLetter(books, comparator);
  }

  public static Map<String, List<Author>> splitAuthorsByLetter(List<Author> authors) {
    Comparator<Author> comparator = new Comparator<Author>() {

      public int compare(Author o1, Author o2) {
        String author1 = (o1 == null ? "" : o1.getTitleToSplitByLetter());
        String author2 = (o2 == null ? "" : o2.getTitleToSplitByLetter());
        return author1.compareTo(author2);
      }
    };

    return splitByLetter(authors, comparator);
  }

  public static Map<String, List<Series>> splitSeriesByLetter(List<Series> series) {
    Comparator<Series> comparator = new Comparator<Series>() {

      public int compare(Series o1, Series o2) {
        String series1 = (o1 == null ? "" : o1.getTitleToSplitByLetter());
        String series2 = (o2 == null ? "" : o2.getTitleToSplitByLetter());
        return series1.compareTo(series2);
      }
    };

    return splitByLetter(series, comparator);
  }

  public static Map<String, List<Tag>> splitTagsByLetter(List<Tag> tags) {
    Comparator<Tag> comparator = new Comparator<Tag>() {

      public int compare(Tag o1, Tag o2) {
        String tag1 = (o1 == null ? "" : o1.getName());
        String tag2 = (o2 == null ? "" : o2.getName());
        return tag1.compareTo(tag2);
      }
    };

    return splitByLetter(tags, comparator);
  }

  public static Map<DateRange, List<Book>> splitBooksByDate(List<Book> books) {

    Map<DateRange, List<Book>> splitByDate = new HashMap<DateRange, List<Book>>();
    for (Book book : books) {
      DateRange range = DateRange.valueOf(book.getTimestamp());
      List<Book> list = splitByDate.get(range);
      if (list == null) {
        list = new LinkedList<Book>();
        splitByDate.put(range, list);
      }
      list.add(book);
    }
    return splitByDate;
  }

  /**
   * Split a list of items by the first letter that is different in some (e.g. Mortal, More and Morris will be splitted on t, e and r)
   * @param objects
   * @param comparator
   * @return
   */
  public static <T extends SplitableByLetter> Map<String, List<T>> splitByLetter(List<T> objects, Comparator<T> comparator) {
    Map<String, List<T>> splitMap = new HashMap<String, List<T>>();

    // construct a list of all strings to split
    Vector<String> stringsToSplit = new Vector<String>(objects.size());
    String commonPart = null;
    for (T object : objects) {
      if (object == null)
        continue;
      String string = object.getTitleToSplitByLetter();

      // remove any diacritic mark
      String temp = Normalizer.normalize(string, Normalizer.Form.NFD);
      Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
      String norm = pattern.matcher(temp).replaceAll("");
      string = norm;
      stringsToSplit.add(string);

      // find the common part
      if (commonPart == null) {
        commonPart = string.toUpperCase();
      } else if (commonPart.length() > 0) {
        String tempCommonPart = commonPart;
        while (tempCommonPart.length() > 0 && !string.toUpperCase().startsWith(tempCommonPart)) {
          tempCommonPart = tempCommonPart.substring(0, tempCommonPart.length()-1);
        }
        commonPart = tempCommonPart.toUpperCase();
      }
    }

    // note the position of the first different char
    int firstDifferentCharPosition = commonPart.length();

    // browse all objects and split them up
    for (int i=0; i<objects.size(); i++) {
      T object = objects.get(i);
      if (object == null)
        continue;

      String string = stringsToSplit.get(i);
      String discriminantPart = "_";
      if (Helper.isNotNullOrEmpty(string)) {
        if (firstDifferentCharPosition + 1 >= string.length())
          discriminantPart = string.toUpperCase();
        else
          discriminantPart = string.substring(0, firstDifferentCharPosition+1).toUpperCase();

        // find the already existing list (of items split by this discriminant part)
        List<T> list = splitMap.get(discriminantPart);
        if (list == null) {
          // no list yet, create one
          list = new LinkedList<T>();
          splitMap.put(discriminantPart, list);
        }

        // add the current item to the list
        list.add(object);
      }
    }

    // sort each list
    for (String letter : splitMap.keySet()) {
      List<T> objectsInThisLetter = splitMap.get(letter);
      Collections.sort(objectsInThisLetter, comparator);
    }
    return splitMap;
  }

  /**
   * This method generates implicit Lang:XX tags based on the books languages (as specified in the database).
   * It checks for collisions with existing tags (i.e. it will not add a book twice to an already set tag).
   * Must be called AFTER the datamodel list of books and tags has been loaded, but before the map of books by
   * tags is loaded (or else it'll be reloaded)
   *
   * There is a configuration option to disable the treatement of language as implict tags.
   */
  public static void generateImplicitLanguageTags() {
    logger.debug("generateImplicitLanguageTags:  Enter");
    if (useLanguagesAsTags == false) {
      logger.debug("generateImplicitLanguageTags:  Exit - not wanted");
      return;
    }
    // compute the latest id
    int lastId = -1;
    for (Tag tag : getListOfTags()) {
      int id = Integer.parseInt(tag.getId());
      if (lastId < id)
        lastId = id;
    }
    Map<String, Tag> mapOfLanguageTags = new HashMap<String, Tag>();
    for (Book book : getListOfBooks()) {
      if (Helper.isNotNullOrEmpty(book.getBookLanguages())) {
        for (Language language : book.getBookLanguages()) {
          if (language != null) {
            String languageTagName = IMPLICIT_LANGUAGE_TAG_PREFIX + language.getIso2();
            Tag languageTag = mapOfLanguageTags.get(languageTagName.toUpperCase(Locale.ENGLISH));
            if (languageTag == null) {
              // check in the existing tags
              for (Tag tag : getListOfTags()) {
                if (tag.getName().equalsIgnoreCase(languageTagName)) {
                  languageTag = tag;
                  break;
                }
              }
              if (languageTag == null) {
                languageTag = new Tag("" + (++lastId), languageTagName);
                addTag(languageTag);
              }
              mapOfLanguageTags.put(languageTagName.toUpperCase(Locale.ENGLISH), languageTag);
            }
            if (!book.getTags().contains(languageTag))
              book.getTags().add(languageTag);
          } else {
            logger.error("generateImplicitLanguageTags: found a null language for book " + book);
            for (Language language1 : book.getBookLanguages()) {
              logger.error(language1);
            }
          }
        }
      }
    }
    mapOfBooksByTag = null;
    logger.debug("generateImplicitLanguageTags:  Exit");
  }
  /**
   * Get the book languages in the form of tags.
   * Only available if the apprpriate option set
   * @return
   */
  private static List<Tag> getBookLanguagesAsTags(int bookId) {
    if (!useLanguagesAsTags) {
      return null;
    }
    Book book = getListOfBooks().get(bookId);
    assert book != null;
    List<Language> bookLanguages = book.getBookLanguages();
    List<Tag>tags = null;
    if (bookLanguages != null) {
      tags = new LinkedList<Tag>();
      for (Language language : bookLanguages) {
        String tagName = IMPLICIT_LANGUAGE_TAG_PREFIX + language.getIso2();
        Tag tag = getMapOfTags().get(tagName);
        // Do not think null return is possible, but lets play safe
        if (tag != null) {
          tags.add(tag);
        }
      }
    }
    return tags;
  }

  public static void setUseLanguagesAsTags(boolean b) {
    useLanguagesAsTags = b;
  }

  public static boolean getUseLanguagesAsTags() { return useLanguagesAsTags; }

  public static void setLibrarySortAuthor(boolean b) { librarySortAuthor = b; }

  public static boolean getLibrarySortAuthor() {return librarySortAuthor; }

  public static void setLibrarySortTitle(boolean b) { librarySortTitle = b; }

  public static boolean getLibrarySortTitle() {return librarySortTitle; }

  public static void setLibrarySortSeries(boolean b) { librarySortSeries = b; }

  public static boolean getLibrarySortSeries() {return librarySortSeries; }

  /**
   * Get a Noiseword object given the language string
   */
  public static NoiseWord getNoiseword(String language) {
    Locale locale;
    switch (language.length()) {
      case 2: locale = Localization.Main.getLocaleFromiso2(language);
              break;
      case 3: locale = Localization.Main.getLocaleFromIso3(language);
              break;
      default:
              // Bit sure this should even be ossible!
              assert false : "Unexpected value for language parameter (" + language +")";
              return null;
    }
    return getNoiseword(locale);
  }
  /**
   * Get a Noiseword correspnding to a Language object
   *
   * @param lang
   * @return
   */
  public static NoiseWord getNoiseword(Language lang) {
    if (lang == null) {
      // TODO Is this option even possible?
      logger.debug("Unexpected null Language parameter");
      return new NoiseWord(null, new LinkedList<String>());
    }
    return getNoiseword(lang.getLocale());
  }

  /**
   * Get a Noiseword object corresponding to a locale
   *
   * @param locale
   * @return
   */
  public static NoiseWord getNoiseword (Locale locale) {
    if (mapOfNoisewords == null) {
      mapOfNoisewords = new HashMap<Locale, NoiseWord>();
    }
    if (mapOfNoisewords.containsKey(locale)) {
      return mapOfNoisewords.get(locale);
    }

    // This is a 'tweak' to ensure we have the correct internal locale
    Locale ll = Localization.Main.getLocaleFromIso3(locale.getISO3Language());
    if (ll == null) {
      if (logger.isTraceEnabled()) logger.trace("Attempt to create Noiseword for unsupported locale " + locale.getDisplayLanguage());
      return new NoiseWord(locale, null);
    }

    // Convert the string returned from localization to the vector format used internally

    String langNoiseWords = Localization.Main.getText(ll,"i18n.noiseWords");
    List<String> noiseWordList = new LinkedList<String>();
    StringTokenizer st = new StringTokenizer(langNoiseWords, ",");
    NoiseWord nw = null;
    if (langNoiseWords != null) {
      while (st.hasMoreTokens()) {
        String nt = st.nextToken().toUpperCase(locale);
        if (nt.length() > 1)
          noiseWordList.add(nt);
      }
      nw = new NoiseWord(locale, noiseWordList);
      mapOfNoisewords.put(locale, nw);
    }
    return nw;
  }

  /**
   * Apply the specified filter to the current data model
   *
   * @param filter
   */
  public static void filterDataModel(BookFilter filter) {
    List<Book> booksCopy = new LinkedList<Book>(DataModel.getListOfBooks());
    for (Book book : booksCopy) {

      if (!filter.didBookPassThroughFilter(book)) {

        // remove the book from the map of books by tags
        for (Tag tag : book.getTags()) {
          List<Book> books = DataModel.getMapOfBooksByTag().get(tag);
          if (Helper.isNotNullOrEmpty(books))
            books.remove(book);
          if (Helper.isNullOrEmpty(books)) {
            DataModel.getMapOfBooksByTag().remove(tag);
            DataModel.getListOfTags().remove(tag);
          }
        }
        DataModel.getMapOfTagsByBookId().remove(book.getId());

        // remove the book from the map of books by series
        Series serie = book.getSeries();
        List<Book> booksInSerie = DataModel.getMapOfBooksBySeries().get(serie);
        if (Helper.isNotNullOrEmpty(booksInSerie))
          booksInSerie.remove(book);
        if (Helper.isNullOrEmpty(booksInSerie)) {
          DataModel.getMapOfBooksBySeries().remove(serie);
          DataModel.getListOfSeries().remove(serie);
        }
        DataModel.getMapOfSeriesByBookId().remove(book.getId());

        // remove the book from the map of books by author
        for (Author author : book.getAuthors()) {
          List<Book> booksByAuthor = DataModel.getMapOfBooksByAuthor().get(author);
          if (Helper.isNotNullOrEmpty(booksByAuthor))
            booksByAuthor.remove(book);
          if (Helper.isNullOrEmpty(booksByAuthor)) {
            DataModel.getMapOfBooksByAuthor().remove(author);
            DataModel.getListOfAuthors().remove(author);
          }
        }
        DataModel.getMapOfAuthorsByBookId().remove(book.getId());

        // remove the book from the map of books by rating
        BookRating rating = book.getRating();
        List<Book> booksInRating = DataModel.getMapOfBooksByRating().get(rating);
        if (Helper.isNotNullOrEmpty(booksInRating))
          booksInRating.remove(book);
        if (Helper.isNullOrEmpty(booksInRating)) {
          DataModel.getMapOfBooksByRating().remove(rating);
        }

        // remove the book from the map of books by publisher
        Publisher publisher = book.getPublisher();
        List<Book> booksByPublisher = DataModel.getMapOfBooksByPublisher().get(publisher);
        if (Helper.isNotNullOrEmpty(booksByPublisher))
          booksByPublisher.remove(book);
        if (Helper.isNullOrEmpty(booksByPublisher)) {
          DataModel.getMapOfBooksByPublisher().remove(publisher);
          DataModel.getListOfPublishers().remove(publisher);
        }

        // remove the book from the list of books
        DataModel.getListOfBooks().remove(book);

        // remove the book from the map of books
        DataModel.getMapOfBooks().remove(book.getId());

        // remove the book from the maps of XXX by bookId
        DataModel.getMapOfCommentsByBookId().remove(book.getId());
        DataModel.getMapOfEBookFilesByBookId().remove(book.getId());

      }
    }

    /* check that no empty list exist */

    // check that no books by tag list is empty
    LinkedList<Tag> tagList = new LinkedList<Tag>(DataModel.getListOfTags());
    for (Tag tag : tagList) {
      List<Book> books = DataModel.getMapOfBooksByTag().get(tag);
      if (Helper.isNullOrEmpty(books)) {
        DataModel.getMapOfBooksByTag().remove(tag);
        DataModel.getListOfTags().remove(tag);
      }
    }

    // check that no books by series list is empty
    LinkedList<Series> seriesList = new LinkedList<Series>(DataModel.getListOfSeries());
    for (Series serie : seriesList) {
      List<Book> booksInSerie = DataModel.getMapOfBooksBySeries().get(serie);
      if (Helper.isNullOrEmpty(booksInSerie)) {
        DataModel.getMapOfBooksBySeries().remove(serie);
        DataModel.getListOfSeries().remove(serie);
      }
    }

    // check that no books by author list is empty
    LinkedList<Author> authorList = new LinkedList<Author>(DataModel.getListOfAuthors());
    for (Author author : authorList) {
      List<Book> booksByAuthor = DataModel.getMapOfBooksByAuthor().get(author);
      if (Helper.isNullOrEmpty(booksByAuthor)) {
        DataModel.getMapOfBooksByAuthor().remove(author);
        DataModel.getListOfAuthors().remove(author);
      }
    }

  }
}
