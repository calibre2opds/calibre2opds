package com.gmail.dpierron.calibre.opds;
/**
 * Class for implementing the AllBooks sub-catalog
 * Inherits from:
 *  -> BooksSubcatalog - methods for listing contained books.
 *     -> SubCatalog
 */

import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Series;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllBooksSubCatalog extends BooksSubCatalog {
  private final static org.apache.log4j.Logger logger = Logger.getLogger(AllBooksSubCatalog.class);

  //------------------ CONSTRUCTORS ------------------------
  public AllBooksSubCatalog(List<Book> books) {

    super(books);
    sortBooks();
    setCatalogType(Constants.ALLBOOKS_TYPE);
  }

  /**
   * Version of catalog constructor where we can force sort by Title if wanted
   * @param stuffToFilterOut
   * @param subbooks
   * @param sortByTitle
   */
  public AllBooksSubCatalog(List<Object> stuffToFilterOut,
                            List<Book> subbooks,
                            boolean sortByTitle) {
    super(stuffToFilterOut, subbooks);
    if (sortByTitle)
      sortBooksByTitle(getBooks());
    else
      sortBooks();
    setCatalogType(Constants.ALLBOOKS_TYPE);
 }

  /**
   * Version of catalog constructor when we may want items filetered out
   * @param stuffToFilterOut
   * @param books
   */
  public AllBooksSubCatalog(List<Object> stuffToFilterOut, List<Book> books) {
    super(stuffToFilterOut, books);
    sortBooks();
    setCatalogType(Constants.ALLBOOKS_TYPE);
  }

  //------------------ METHODS ------------------------

  public boolean isBookTheStepUnit() {
    return true;
  }

  private void sortBooks() {
    if (currentProfile.getOrderAllBooksBySeries()) {
      // sort the books by series
      sortBooksBySeries(getBooks());
    } else {
      // sort the books by title
      sortBooksByTitle(getBooks());
    }
  }

  /**
   * Sort the list of books sorted with the following key sequence:
   * - Series (alphabetically)
   * - Series-Index (numerically ascending)
   * - Book Title (Alphabetically for case where series-Index is the same)
   *
   * NOTE:  This could be in the more generic BooksSubCatalog class, but since
   *        it is only ever used from this class it has been place here.
   *
   * TODO:  Bug c2o131 - We seem to occasionally get an exception thrown inside the Java libraries
   *        Not sure at the moment why this should be the case as nothing obvious shows up
   *        It looks as it only occurs when series information is set and the series indexes are
   *        the same for the book objects that are being compared but this needs confirming and
   *        in fact may be due to some other currently unknown factor.
   *        THINK THIS MAY NOW BE FIXED - NEED FURTHER CHECKING TO ENSURE THIS IS THE CASE
   *
   * @param books
   */
  void sortBooksBySeries(List<Book> books) {
    // logger.trace("sortBooksBySeries: enter");
    try {
      Collections.sort(books, new Comparator<Book>() {
        public int compare(Book o1, Book o2) {
          // logger.trace("o1=" + o1 + ", o2=" + o2);
          assert (o1 != null) && (o2 != null);
          // logger.trace("comparing books " + o1.getTitle() + "(" + o1.getId() + ") " + o2.getTitle() + "(" + o2.getId() + ")");
          Series series1 = o1.getSeries();
          Series series2 = o2.getSeries();
          // logger.trace("series1=" + series1 + ", series2=" + series2);

          if ((series1 == null) && (series2 == null)) {
            // both series are null, we need to compare the book titles (as always...)
            String title1;
            String title2;
            if (currentProfile.getSortUsingTitle()) {
              title1 = o1.getTitle();
              title2 = o2.getTitle();
            } else {
              title1 = o1.getTitle_Sort();
              title2 = o2.getTitle_Sort();
            }
            // logger.trace("comparing title '" + title1.toString() + "' to '" + title2.toString() + "'");
            try {
              int result = Helper.checkedCollatorCompareIgnoreCase(title1, title2, collator);
              // logger.trace("return=" + result);
              return result;
            } catch (Exception e) {
              logger.error("Error sorting between titles '" + title1.toString() + "' and '" + title2.toString() + "'");
              return 0;
            }
          }
          if (series1 == null) {
            // only series2 set  so assume series2 sorts greater than series1
            // logger.trace("return 1");
            return 1;
          }
          if (series2 == null) {
            // only series1 set  so assume series2 sorts less than series2
            // logger.trace("return -1");
            return -1;
          }

          // Both series set if we get to here
          assert (series1 != null) && (series2 != null);
          if (!series1.getId().equals(series2.getId())) {
            // different series, we need to compare the series title
            try {
              // logger.trace("comparing series " + series1.getName() + "(" + series1.getId() + ") to " + series2.getName() + "(" + series2.getId() + ")");
              int result = Helper.checkedCollatorCompareIgnoreCase(series1.getName(), series2.getName(), collator);
              // logger.trace("return=" + result);
              return result;
            } catch (Exception e) {
              logger.error("Error sorting between series '" + series1.getName().toString() + "' and '" + series2.getName().toString() + "'");
              return 0;
            }
          }
          // same series, we need to compare the index
          // logger.trace("o1.index=" + o1.getSerieIndex() + ", o2.index=" + o2.getSerieIndex());
          if (o1.getSerieIndex() == o2.getSerieIndex()) {
            // series index the same, so we need to sort on the book title
            // both series are null, we need to compare the book titles (as always...)
            // logger.trace("Same series and series index");
            String title1;
            String title2;
            if (currentProfile.getSortUsingTitle()) {
              title1 = o1.getTitle();
              title2 = o2.getTitle();
            } else {
              title1 = o1.getTitle_Sort();
              title2 = o2.getTitle_Sort();
            }
            // logger.trace("comparing title '" + title1.toString() + "' to '" + title2.toString() + "'");
            try {
              int result = Helper.checkedCollatorCompareIgnoreCase(title1, title2, collator);
              // logger.trace("return=" + result);
              return result;
            } catch (Exception e) {
              logger.error("Error sorting between titles '" + title1.toString() + "' and '" + title2.toString() + "'");
              // logger.trace("return 0");
              return 0;
            }
          }
          if (o1.getSerieIndex() > o2.getSerieIndex()) {
            // logger.trace("return 1");
            return 1;
          } else {
            // logger.trace("return -1");
            return -1;
          }
        }  // end of compare()
      } // End of Comparator()
      ); // End of Collections.sort() statement
    } catch (Exception e) {
      logger.error("Unexpected exception reached trying to sort books by series: " + e);
    }
    // logger.trace("sortBooksBySeries: exit");
    return;
  }

  public String getSummary() {
    if (getBooks().size() > 1)
      return Localization.Main.getText("allbooks.alphabetical", getBooks().size());
    else if (getBooks().size() == 1)
      return Localization.Main.getText("allbooks.alphabetical.single");
    else return "";
  }

  public String getUrn() {
    return Constants.INITIAL_URN_PREFIX + getCatalogType();
  }
}
