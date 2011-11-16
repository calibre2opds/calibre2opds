package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.database.Database;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public enum DataModel {
  INSTANCE;

  private final static Logger logger = Logger.getLogger(DataModel.class);

  private static final String IMPLICIT_LANGUAGE_TAG_PREFIX = "Lang:";

  private Map<String, List<EBookFile>> mapOfFilesByBookId;
  private Map<String, List<Publisher>> mapOfPublishersByBookId;
  private Map<String, List<Author>> mapOfAuthorsByBookId;
  private Map<String, List<Tag>> mapOfTagsByBookId;
  private Map<String, List<Series>> mapOfSeriesByBookId;
  private Map<String, List<String>> mapOfCommentsByBookId;

  private List<Book> listOfBooks;
  private Map<String, Book> mapOfBooks;

  private List<Tag> listOfTags;
  private Map<String, Tag> mapOfTags;
  private Map<Tag, List<Book>> mapOfBooksByTag;

  private List<Author> listOfAuthors;
  private Map<String, Author> mapOfAuthors;
  private Map<Author, List<Book>> mapOfBooksByAuthor;

  private List<Series> listOfSeries;
  private Map<String, Series> mapOfSeries;
  private Map<Series, List<Book>> mapOfBooksBySeries;

  private Map<BookRating, List<Book>> mapOfBooksByRating;

  private List<Publisher> listOfPublishers;
  private Map<String, Publisher> mapOfPublishers;
  private Map<Publisher, List<Book>> mapOfBooksByPublisher;

  private Map<String, Language> mapOfLanguagesById;
  private Map<String, Language> mapOfLanguagesByIsoCode;
  private Map<String, String> mapOfSavedSearches;

  public void reset() {
    mapOfFilesByBookId = null;
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
    mapOfBooksByRating = null;
    listOfPublishers = null;
    mapOfPublishers = null;
    mapOfBooksByPublisher = null;
    mapOfLanguagesById = null;
    mapOfLanguagesByIsoCode = null;
    mapOfSavedSearches = null;

    // reset the database
    DatabaseManager.INSTANCE.reset();
  }

  public void preloadDataModel() {
    getMapOfLanguagesById();
    // getMapOfLanguagesByIsoCode(); not useful, loaded by getMapOfLanguagesById();
    getListOfBooks();
    getMapOfFilesByBookId();
    getMapOfAuthorsByBookId();
    getMapOfTagsByBookId();
    getMapOfSeriesByBookId();
    getMapOfCommentsByBookId();
    getMapOfBooks();
    getListOfTags();
    getMapOfTags();
    generateImplicitLanguageTags();
    getMapOfBooksByTag();
    getListOfAuthors();
    getMapOfAuthors();
    getMapOfBooksByAuthor();
    getListOfSeries();
    getMapOfSeries();
    getMapOfBooksBySeries();
    getMapOfBooksByRating();
  }

  public Map<String, String> getMapOfSavedSearches() {
    if (mapOfSavedSearches == null) {
      mapOfSavedSearches = Database.INSTANCE.getMapOfSavedSearches();
    }
    return mapOfSavedSearches;
  }


  public Map<String, Language> getMapOfLanguagesById() {
    if (mapOfLanguagesById == null) {
      Composite<Map<String, Language>, Map<String, Language>> result = Database.INSTANCE.getMapsOfLanguages();
      mapOfLanguagesById = result.getFirstElement();
      mapOfLanguagesByIsoCode = result.getSecondElement();
    }
    return mapOfLanguagesById;
  }

  public Map<String, Language> getMapOfLanguagesByIsoCode() {
    if (mapOfLanguagesByIsoCode == null) {
      getMapOfLanguagesById();
    }
    return mapOfLanguagesById;
  }

  public Map<String, List<EBookFile>> getMapOfFilesByBookId() {
    if (mapOfFilesByBookId == null) {
      mapOfFilesByBookId = Database.INSTANCE.listFilesByBookId();
    }
    return mapOfFilesByBookId;
  }

  public Map<String, List<Author>> getMapOfAuthorsByBookId() {
    if (mapOfAuthorsByBookId == null) {
      mapOfAuthorsByBookId = Database.INSTANCE.listAuthorsByBookId();
    }
    return mapOfAuthorsByBookId;
  }

  public Map<String, List<Publisher>> getMapOfPublishersByBookId() {
    if (mapOfPublishersByBookId == null) {
      mapOfPublishersByBookId = Database.INSTANCE.listPublishersByBookId();
    }
    return mapOfPublishersByBookId;
  }

  public Map<String, List<Tag>> getMapOfTagsByBookId() {
    if (mapOfTagsByBookId == null) {
      mapOfTagsByBookId = Database.INSTANCE.listTagsByBookId();
    }
    return mapOfTagsByBookId;
  }

  public Map<String, List<Series>> getMapOfSeriesByBookId() {
    if (mapOfSeriesByBookId == null) {
      mapOfSeriesByBookId = Database.INSTANCE.listSeriesByBookId();
    }
    return mapOfSeriesByBookId;
  }

  public Map<String, List<String>> getMapOfCommentsByBookId() {
    if (mapOfCommentsByBookId == null) {
      mapOfCommentsByBookId = Database.INSTANCE.listCommentsByBookId();
    }
    return mapOfCommentsByBookId;
  }

  public List<Book> getListOfBooks() {
    if (listOfBooks == null) {
      listOfBooks = Database.INSTANCE.listBooks();
    }
    return listOfBooks;
  }

  public Map<String, Book> getMapOfBooks() {
    if (mapOfBooks == null) {
      mapOfBooks = new HashMap<String, Book>();
      for (Book book : getListOfBooks()) {
        mapOfBooks.put(book.getId(), book);
      }
    }
    return mapOfBooks;
  }

  public List<Tag> getListOfTags() {
    if (listOfTags == null) {
      listOfTags = Database.INSTANCE.listTags();
    }
    return listOfTags;
  }

  public Map<String, Tag> getMapOfTags() {
    if (mapOfTags == null) {
      mapOfTags = new HashMap<String, Tag>();
      for (Tag tag : getListOfTags()) {
        mapOfTags.put(tag.getId(), tag);
      }
    }
    return mapOfTags;
  }

  void addTag(Tag tag) {
    if (getListOfTags().contains(tag))
      return;
    getListOfTags().add(tag);
    getMapOfTags().put(tag.getId(), tag);
  }

  public Map<Tag, List<Book>> getMapOfBooksByTag() {
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

  public List<Author> getListOfAuthors() {
    if (listOfAuthors == null) {
      listOfAuthors = Database.INSTANCE.listAuthors();
    }
    return listOfAuthors;
  }

  public Map<String, Author> getMapOfAuthors() {
    if (mapOfAuthors == null) {
      mapOfAuthors = new HashMap<String, Author>();
      for (Author author : getListOfAuthors()) {
        mapOfAuthors.put(author.getId(), author);
      }
    }
    return mapOfAuthors;
  }

  public Map<Author, List<Book>> getMapOfBooksByAuthor() {
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

  public List<Series> getListOfSeries() {
    if (listOfSeries == null) {
      listOfSeries = Database.INSTANCE.listSeries();
    }
    return listOfSeries;
  }

  public Map<String, Series> getMapOfSeries() {
    if (mapOfSeries == null) {
      mapOfSeries = new HashMap<String, Series>();
      for (Series serie : getListOfSeries()) {
        mapOfSeries.put(serie.getId(), serie);
      }
    }
    return mapOfSeries;
  }

  public Map<Series, List<Book>> getMapOfBooksBySeries() {
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

  public Map<BookRating, List<Book>> getMapOfBooksByRating() {
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

  public List<Publisher> getListOfPublishers() {
    if (listOfPublishers == null) {
      listOfPublishers = Database.INSTANCE.listPublishers();
    }
    return listOfPublishers;
  }

  public Map<String, Publisher> getMapOfPublishers() {
    if (mapOfPublishers == null) {
      mapOfPublishers = new HashMap<String, Publisher>();
      for (Publisher publisher : getListOfPublishers()) {
        mapOfPublishers.put(publisher.getId(), publisher);
      }
    }
    return mapOfPublishers;
  }

  public Map<Publisher, List<Book>> getMapOfBooksByPublisher() {
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
        String title1 = (o1 == null ? "" : o1.getTitleForSort());
        String title2 = (o2 == null ? "" : o2.getTitleForSort());
        return title1.compareTo(title2);
      }
    };

    return splitByLetter(books, comparator);
  }

  public static Map<String, List<Author>> splitAuthorsByLetter(List<Author> authors) {
    Comparator<Author> comparator = new Comparator<Author>() {

      public int compare(Author o1, Author o2) {
        String author1 = (o1 == null ? "" : o1.getNameForSort());
        String author2 = (o2 == null ? "" : o2.getNameForSort());
        return author1.compareTo(author2);
      }
    };

    return splitByLetter(authors, comparator);
  }

  public static Map<String, List<Series>> splitSeriesByLetter(List<Series> series) {
    Comparator<Series> comparator = new Comparator<Series>() {

      public int compare(Series o1, Series o2) {
        String series1 = (o1 == null ? "" : o1.getName());
        String series2 = (o2 == null ? "" : o2.getName());
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
  static <T extends SplitableByLetter> Map<String, List<T>> splitByLetter(List<T> objects, Comparator<T> comparator) {
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
   */
  void generateImplicitLanguageTags() {
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
            logger.error("found a null language for book " + book);
            for (Language language1 : book.getBookLanguages()) {
              logger.error(language1);
            }
          }
        }
      }
    }
    mapOfBooksByTag = null;
  }
}
