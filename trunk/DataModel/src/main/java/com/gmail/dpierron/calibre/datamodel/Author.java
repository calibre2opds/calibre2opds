package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.List;

public class Author implements SplitableByLetter, Comparable<Author> {
  private String id;
  private String name;
  private String sort;
  private String guessedLastName;
  private String nameForSort;

  public Author(String id, String name, String sort) {
    super();
    this.id = id;
    setName(name);
    /* history of change for the author.sort column :
     * - at the beginning, C2O was looking in this column for sort info ; if it was empty, it would be computed
     * - then a bug was found (577526) and I removed the load code for this column 
     * - and then, after bug 655081 I realized that Calibre had evolved into using the column again ; so back to step one
     */
    this.sort = sort;
  }

  public void setName(String name) {
    this.name = name.replace('|', ',');
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getLastName() {
    if (guessedLastName == null) {
      String sortedName = getSort();
      // sometimes getSort computes the last name for us... optimize !
      if (Helper.isNotNullOrEmpty(guessedLastName))
        return guessedLastName;
      guessedLastName = sortedName;
      if (Helper.isNotNullOrEmpty(getSort())) {
        int posOfSpace = getSort().indexOf(',');
        if (posOfSpace >= 0)
          guessedLastName = getSort().substring(0, posOfSpace);
      } else
        guessedLastName = name;
    }
    return guessedLastName;
  }

  public String getNameForSort() {
    if (Helper.isNullOrEmpty(nameForSort)) {
      nameForSort = getLastName().replaceAll(" ", "").toUpperCase();
    }
    return nameForSort;
  }

  public String getSort() {
    if (Helper.isNullOrEmpty(sort))
      sort = computeSort();
    return sort;
  }

  private String computeSort() {
    // first, let's try and find a book by this author (alone) which has a author_sort field
    List<Book> books = DataModel.INSTANCE.getMapOfBooksByAuthor().get(this);
    if (books != null)
      for (Book book : books) {
        if (book.hasSingleAuthor()) {
          String authorSort = book.getAuthorSort();
          if (Helper.isNotNullOrEmpty(authorSort) && authorSort.contains(","))
            return authorSort;
        }
      }

    // Check if there is a comma
    int posOfSpace = name.indexOf(',');
    if (posOfSpace >= 0) {
      guessedLastName = name.substring(0, posOfSpace);
      return name;
    }

    // then, reverse the name
    List<String> words = Helper.tokenize(name, " ");
    if (words.size() == 1)
      return words.get(0);
    guessedLastName = words.get(words.size() - 1);
    words.remove(words.size() - 1);
    String result = guessedLastName + ", " + Helper.concatenateList(" ", words);
    return result;
  }

  public String toString() {
    return getId() + " - " + getName();
  }

  public String getTitleToSplitByLetter() {
    return getLastName();
  }

  /* Comparable interface, used to sort an authors list */

  public int compareTo(Author o) {
    if (o == null)
      return 1;
    else {
      int i = Helper.trueStringCompare(getSort(), o.getSort());
      return i;
    }
  }


}
