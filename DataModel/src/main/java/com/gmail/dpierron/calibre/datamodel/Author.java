package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Author  extends GenericDataObject implements SplitableByLetter, Comparable<Author>  {
  private final static Logger logger = LogManager.getLogger(Author.class);
  private final String id;
  private String displayName;
  private String sortName;
  private String guessedLastName;
  private String nameForSort;
  // Flags
  // NOTE: Using byte plus bit settings is more memory efficient than using boolean types
  private final static byte FLAG_ALL_CLEAR = 0;
  private final static byte FLAG_DONE = 0x01;
  private final static byte FLAG_REFERENCED = 0x02;
  private byte flags = FLAG_ALL_CLEAR;

  // CONSTRUCTORS

  public Author(String id, String DisplayName, String sort) {
    super();
    this.id = id;
    this.displayName = DisplayName.replace('|', ',');
    /* history of change for the author.sortName column :
     * - at the beginning, C2O was looking in this column for sortName info ; if it was empty, it would be computed
     * - then a bug was found (577526) and I removed the load code for this column
     * - and then, after bug 655081 I realized that Calibre had evolved into using the column again ; so back to step one
     */
    this.sortName = sort;
  }

  // METHODS and PROPERTIES implementing Abstract ones from Base class)

  public ColumType getColumnType() {
    return ColumType.COLUMN_AUTHOR;
  }
   public String getColumnName() {
     return "authors";
   }
  public String getDisplayName() {
    return displayName;
  }
  /**
   * Get the stored sortName string
   * If necessary derive the value we are going to use for sorting
   *
   * @return
   */
  public String getSortName() {
    if (Helper.isNullOrEmpty(sortName)) {
      // first, let's try and find a book by this author (alone) which has a author_sort field
      // If so we use that value as the most likely one
      List<Book> books = DataModel.getMapOfBooksByAuthor().get(this);
      if (books != null) {
        for (Book book : books) {
          if (book.hasSingleAuthor()) {
            String authorSort = book.getAuthorSort();
            // ITIMPI:   Perhaps we should also consider a value with no comma and no space as a valid author sortName?
            if (Helper.isNotNullOrEmpty(authorSort) && authorSort.contains(","))
              return authorSort;
          }
        }
      }

      // We could not find an acceptable author sortName value so we need to do something further

      // Check if there is a comma   in the displayName field
      // If so assume what follows is the author sortName surname

      int posOfSpace = displayName.indexOf(',');
      if (posOfSpace >= 0) {
        guessedLastName = displayName.substring(0, posOfSpace);
        return displayName;
      }

      // then split the author on space seaparator
      List<String> words = Helper.tokenize(displayName, " ");
      switch (words.size()) {
        case 1:
          // If there is only a single word then it must be the author sortName
          guessedLastName = displayName;
          return displayName;
        case 0:
          logger.warn("Problem computing Author sortName for author: " + displayName); Helper.statsWarnings++;
          guessedLastName = "[CALIBRE2OPDS] BAD_AUTHOR_SORT (" + displayName + ")";
          return guessedLastName;
        default:
          // Grab the last word as the surname
          guessedLastName = words.get(words.size() - 1);
          // Remove from the array the word we have assumed is the surname
          words.remove(words.size() - 1);
          // Now construct the sortName nsme we are going to use
          return guessedLastName + ", " + Helper.concatenateList(" ", words);
      }
    }
    return sortName;
  }

  public String getTextToDisplay() {
    return DataModel.displayAuthorSort ? getSortName() : getDisplayName() ;
  }
  public String getTextToSort() {
    return DataModel.librarySortAuthor ? getDisplayName() :  getSortName();
  }

  //                            METHODS and PROPERTIES


  public String getId() {
    return id;
  }


  /**
   * Try and extract the last displayName for sorting purposes
   * @return
   */
  public String getLastName() {
    if (guessedLastName == null) {
      String sortedName = getSortName();
      // sometimes getSortName computes the last displayName for us... optimize !
      if (Helper.isNotNullOrEmpty(guessedLastName))
        return guessedLastName;
      guessedLastName = sortedName;
      if (Helper.isNotNullOrEmpty(getSortName())) {
        int posOfSpace = getSortName().indexOf(',');
        if (posOfSpace >= 0)
          guessedLastName = getSortName().substring(0, posOfSpace);
      } else
        guessedLastName = displayName;
    }
    return guessedLastName;
  }

  public String toString() {
    return getId() + " - " + getDisplayName();
  }
  /* Comparable interface, used to sortName an authors list */

  public int compareTo(Author o) {
    if (o == null)
      return 1;
    else {
      return Helper.trueStringCompare(getSortName(), o.getSortName());
    }
  }

  public void setDone() {
    flags |= FLAG_DONE;
  }
  public boolean isDone () {
    return ((flags & FLAG_DONE) != 0);
  }

  public void setReferenced() {
    flags |= FLAG_REFERENCED;
  }
  public boolean isReferenced () {
    return ((flags & FLAG_REFERENCED) != 0);
  }
}
