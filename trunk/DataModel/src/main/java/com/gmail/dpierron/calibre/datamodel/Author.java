package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;

import java.util.List;

public class Author implements SplitableByLetter, Comparable<Author> {
  private final String id;
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

  void setName(String name) {
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

  /**
   * Derive the value we are going to use for sorting by Author
   * @return
   */
  private String computeSort() {
    // first, let's try and find a book by this author (alone) which has a author_sort field
    // If so we use that value as the most likely one
    List<Book> books = DataModel.INSTANCE.getMapOfBooksByAuthor().get(this);
    if (books != null)
      for (Book book : books) {
        if (book.hasSingleAuthor()) {
          String authorSort = book.getAuthorSort();
          // ITIMPI:   Perhaps we should also consider a value with no comma and no space as a valid author sort?
          if (Helper.isNotNullOrEmpty(authorSort) && authorSort.contains(","))
            return authorSort;
        }
      }

    // We could not dind an acceptable author sort value so we need to do something further

    // Check if there is a comma   in the name field
    // If so assume what follows is the author sort surname

    int posOfSpace = name.indexOf(',');
    if (posOfSpace >= 0) {
      guessedLastName = name.substring(0, posOfSpace);
      return name;
    }

    // then split the author on space seaparator
    List<String> words = Helper.tokenize(name, " ");
    switch (words.size()) {
      case 1:
        // If there is only a single word then it must be the author sort
        guessedLastName = name;
        return name;
      case 0:
          guessedLastName = "[CALIBRE2OPDS] BAD_AUTHOR_SORT (" + name + ")";
          return guessedLastName;
      default:
        // Grab the last word as the surname
        guessedLastName = words.get(words.size() - 1);
        // Remove from the array the word we have assumed is the surname
        words.remove(words.size() - 1);
        // Now construct the sort nsme we are going to use
        return guessedLastName + ", " + Helper.concatenateList(" ", words);
    }
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
      return Helper.trueStringCompare(getSort(), o.getSort());
    }
  }


}
