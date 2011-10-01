package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.database.Database;

import java.util.*;

public enum DataModel {
  INSTANCE;

  Map<String, List<EBookFile>> mapOfFilesByBookId;
  Map<String, List<Publisher>> mapOfPublishersByBookId;
  Map<String, List<Author>> mapOfAuthorsByBookId;
  Map<String, List<Tag>> mapOfTagsByBookId;
  Map<String, List<Series>> mapOfSeriesByBookId;
  Map<String, List<String>> mapOfCommentsByBookId;

  List<Book> listOfBooks;
  Map<String, Book> mapOfBooks;

  List<Tag> listOfTags;
  Map<String, Tag> mapOfTags;
  Map<Tag, List<Book>> mapOfBooksByTag;

  List<Author> listOfAuthors;
  Map<String, Author> mapOfAuthors;
  Map<Author, List<Book>> mapOfBooksByAuthor;
  Map<Author, List<Series>> mapOfSeriesByAuthor;
  Map<Author, List<Book>> mapOfBooksNotInSerieByAuthor;

  List<Series> listOfSeries;
  Map<String, Series> mapOfSeries;
  Map<Series, List<Book>> mapOfBooksBySeries;

  Map<BookRating, List<Book>> mapOfBooksByRating;

  List<Publisher> listOfPublishers;
  Map<String, Publisher> mapOfPublishers;
  Map<Publisher, List<Book>> mapOfBooksByPublisher;

  Map<String, Language> mapOfLanguagesById;
  Map<String, Language> mapOfLanguagesByIsoCode;
  Map<String, String> mapOfSavedSearches;

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
    mapOfSeriesByAuthor = null;
    mapOfBooksNotInSerieByAuthor = null;
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
    getMapOfBooksByTag();
    getListOfAuthors();
    getMapOfAuthors();
    getMapOfBooksByAuthor();
    getMapOfSeriesByAuthor();
    getMapOfBooksNotInSeriesByAuthor();
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
      mapOfLanguagesById = Database.INSTANCE.getMapOfLanguagesById();
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

  public List<Tag> getListOfTags(List<Book> books) {
    if (books == null)
      return getListOfTags();

    List<Tag> result = new LinkedList<Tag>();
    for (Book book : books) {
      for (Tag tag : book.getTags()) {
        if (!result.contains(tag))
          result.add(tag);
      }
    }
    return result;
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

  public List<Author> getListOfAuthors(List<Book> books) {
    if (books == null)
      return getListOfAuthors();

    List<Author> result = new LinkedList<Author>();
    for (Book book : books) {
      for (Author author : book.getAuthors()) {
        if (!result.contains(author))
          result.add(author);
      }
    }
    return result;
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

  public Map<Author, List<Series>> getMapOfSeriesByAuthor() {
    if (mapOfSeriesByAuthor == null)
      computeMapOfSeriesByAuthor();
    return mapOfSeriesByAuthor;
  }

  public Map<Author, List<Book>> getMapOfBooksNotInSeriesByAuthor() {
    if (mapOfBooksNotInSerieByAuthor == null)
      computeMapOfSeriesByAuthor();
    return mapOfBooksNotInSerieByAuthor;
  }

  private void computeMapOfSeriesByAuthor() {
    mapOfSeriesByAuthor = new HashMap<Author, List<Series>>();
    mapOfBooksNotInSerieByAuthor = new HashMap<Author, List<Book>>();
    for (Book book : getListOfBooks()) {
      for (Author author : book.getAuthors()) {
        Series serie = book.getSeries();
        if (serie != null) {
          List<Series> series = mapOfSeriesByAuthor.get(author);
          if (series == null) {
            series = new LinkedList<Series>();
            mapOfSeriesByAuthor.put(author, series);
          }
          if (!series.contains(serie))
            series.add(serie);
        } else {
          List<Book> books = mapOfBooksNotInSerieByAuthor.get(author);
          if (books == null) {
            books = new LinkedList<Book>();
            mapOfBooksNotInSerieByAuthor.put(author, books);
          }
          if (!books.contains(book))
            books.add(book);
        }
      }
    }
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
    Comparator comparator = new Comparator<Author>() {

      public int compare(Author o1, Author o2) {
        String author1 = (o1 == null ? "" : o1.getNameForSort());
        String author2 = (o2 == null ? "" : o2.getNameForSort());
        return author1.compareTo(author2);
      }
    };

    return splitByLetter(authors, comparator);
  }

  public static Map<String, List<Series>> splitSeriesByLetter(List<Series> series) {
    Comparator comparator = new Comparator<Series>() {

      public int compare(Series o1, Series o2) {
        String series1 = (o1 == null ? "" : o1.getName());
        String series2 = (o2 == null ? "" : o2.getName());
        return series1.compareTo(series2);
      }
    };

    return splitByLetter(series, comparator);
  }

  public static Map<String, List<Tag>> splitTagsByLetter(List<Tag> tags) {
    Comparator comparator = new Comparator<Tag>() {

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

  private static <T extends SplitableByLetter> Map<String, List<T>> splitByLetter(List<T> objects, Comparator<T> comparator) {
    final String LETTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // split by letter
    Map<String, List<T>> splitByLetter = new HashMap<String, List<T>>();

    for (T object : objects) {
      if (object == null)
        continue;
      String firstLetter = "";
      String string = object.getTitleToSplitByLetter();
      if (string == null)
        string = "";
      else
        firstLetter = string.substring(0, 1).toUpperCase(Locale.ENGLISH); // we don't want accented chars
      if (!LETTERS.contains(firstLetter))
        firstLetter = "_";
      List<T> list = splitByLetter.get(firstLetter);
      if (list == null) {
        list = new LinkedList<T>();
        splitByLetter.put(firstLetter, list);
      }
      list.add(object);
    }

    // sort each list
    for (String letter : splitByLetter.keySet()) {
      List<T> objectsInThisLetter = splitByLetter.get(letter);
      Collections.sort(objectsInThisLetter, comparator);
    }
    return splitByLetter;
  }
}
