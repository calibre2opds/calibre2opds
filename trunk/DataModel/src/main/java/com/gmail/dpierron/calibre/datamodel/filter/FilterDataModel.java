package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.*;
import com.gmail.dpierron.tools.Helper;

import java.util.LinkedList;
import java.util.List;

public enum FilterDataModel {
  INSTANCE;

  public void runOnDataModel(BookFilter filter) {
    List<Book> booksCopy = new LinkedList<Book>(DataModel.INSTANCE.getListOfBooks());
    for (Book book : booksCopy) {

      if (!filter.didBookPassThroughFilter(book)) {

        // remove the book from the map of books by tags
        for (Tag tag : book.getTags()) {
          List<Book> books = DataModel.INSTANCE.getMapOfBooksByTag().get(tag);
          if (Helper.isNotNullOrEmpty(books))
            books.remove(book);
          if (Helper.isNullOrEmpty(books)) {
            DataModel.INSTANCE.getMapOfBooksByTag().remove(tag);
            DataModel.INSTANCE.getListOfTags(null).remove(tag);
          }
        }
        DataModel.INSTANCE.getMapOfTagsByBookId().remove(book.getId());

        // remove the book from the map of books by series
        Series serie = book.getSeries();
        List<Book> booksInSerie = DataModel.INSTANCE.getMapOfBooksBySeries().get(serie);
        if (Helper.isNotNullOrEmpty(booksInSerie))
          booksInSerie.remove(book);
        if (Helper.isNullOrEmpty(booksInSerie)) {
          DataModel.INSTANCE.getMapOfBooksBySeries().remove(serie);
          DataModel.INSTANCE.getListOfSeries().remove(serie);
        }
        DataModel.INSTANCE.getMapOfSeriesByBookId().remove(book.getId());

        // remove the book from the map of books by author
        for (Author author : book.getAuthors()) {
          List<Book> booksByAuthor = DataModel.INSTANCE.getMapOfBooksByAuthor().get(author);
          if (Helper.isNotNullOrEmpty(booksByAuthor))
            booksByAuthor.remove(book);
          if (Helper.isNullOrEmpty(booksByAuthor)) {
            DataModel.INSTANCE.getMapOfBooksByAuthor().remove(author);
            DataModel.INSTANCE.getListOfAuthors().remove(author);
          }
        }
        DataModel.INSTANCE.getMapOfAuthorsByBookId().remove(book.getId());

        // remove the book from the map of books by rating
        BookRating rating = book.getRating();
        List<Book> booksInRating = DataModel.INSTANCE.getMapOfBooksByRating().get(rating);
        if (Helper.isNotNullOrEmpty(booksInRating))
          booksInRating.remove(book);
        if (Helper.isNullOrEmpty(booksInRating)) {
          DataModel.INSTANCE.getMapOfBooksByRating().remove(rating);
        }

        // remove the book from the map of books by publisher
        Publisher publisher = book.getPublisher();
        List<Book> booksByPublisher = DataModel.INSTANCE.getMapOfBooksByPublisher().get(publisher);
        if (Helper.isNotNullOrEmpty(booksByPublisher))
          booksByPublisher.remove(book);
        if (Helper.isNullOrEmpty(booksByPublisher)) {
          DataModel.INSTANCE.getMapOfBooksByPublisher().remove(publisher);
          DataModel.INSTANCE.getListOfPublishers().remove(publisher);
        }

        // remove the book from the list of books
        DataModel.INSTANCE.getListOfBooks().remove(book);

        // remove the book from the map of books
        DataModel.INSTANCE.getMapOfBooks().remove(book.getId());

        // remove the book from the maps of XXX by bookId
        DataModel.INSTANCE.getMapOfCommentsByBookId().remove(book.getId());
        DataModel.INSTANCE.getMapOfFilesByBookId().remove(book.getId());

      }
    }

    /* check that no empty list exist */

    // check that no books by tag list is empty
    LinkedList<Tag> tagList = new LinkedList<Tag>(DataModel.INSTANCE.getListOfTags(null));
    for (Tag tag : tagList) {
      List<Book> books = DataModel.INSTANCE.getMapOfBooksByTag().get(tag);
      if (Helper.isNullOrEmpty(books)) {
        DataModel.INSTANCE.getMapOfBooksByTag().remove(tag);
        DataModel.INSTANCE.getListOfTags(null).remove(tag);
      }
    }

    // check that no books by serie list is empty
    LinkedList<Series> seriesList = new LinkedList<Series>(DataModel.INSTANCE.getListOfSeries());
    for (Series serie : seriesList) {
      List<Book> booksInSerie = DataModel.INSTANCE.getMapOfBooksBySeries().get(serie);
      if (Helper.isNullOrEmpty(booksInSerie)) {
        DataModel.INSTANCE.getMapOfBooksBySeries().remove(serie);
        DataModel.INSTANCE.getListOfSeries().remove(serie);
      }
    }

    // check that no books by author list is empty
    LinkedList<Author> authorList = new LinkedList<Author>(DataModel.INSTANCE.getListOfAuthors());
    for (Author author : authorList) {
      List<Book> booksByAuthor = DataModel.INSTANCE.getMapOfBooksByAuthor().get(author);
      if (Helper.isNullOrEmpty(booksByAuthor)) {
        DataModel.INSTANCE.getMapOfBooksByAuthor().remove(author);
        DataModel.INSTANCE.getListOfAuthors().remove(author);
      }
    }

  }
}
