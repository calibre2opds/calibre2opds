package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by WalkerDJ on 16/04/2015.
 *
 * This object is meant to act as a way of providing any methods and
 * function that need to be supplied by all data objects that can
 * generate their own catalog sub-section so that the logic for
 * such generation can be made as generic as possible.
 */
public abstract class GenericDataObject implements SplitableByLetter {
  private final static Logger logger = LogManager.getLogger(Author.class);

  //      METHODS and PROPERTIES  that must be implemented in derived classes)

  public abstract String getColumnName();       // Calibre lookup name
  public abstract ColumType getColumnType();    // Calibre column type
  public abstract String getDisplayName();      // Name that is used for Display purposes
  public abstract String getSortName();         // Sort name (if different to Display Name)
  public abstract String getTextToSort();       // Text to sort by (that takes into account whether DisplayName or SortName is to be used)
  public abstract String getTextToDisplay();    // Text for display that takes into account whether DisplaName or SortName is to be used.

  //                            METHODS and PROPERTIES

  static void  sortObjectsByTitle(List<GenericDataObject> objs) {
    Collections.sort(objs, new Comparator<GenericDataObject>() {
      public int compare(GenericDataObject o1, GenericDataObject o2) {
        return Helper.checkedCollatorCompareIgnoreCase(o1.getTextToSort(), o2.getTextToSort());
      }
    });
  }

static Comparator<GenericDataObject> comparator = new Comparator<GenericDataObject>() {

  public int compare(GenericDataObject o1, GenericDataObject o2) {
    String s1 = (o1 == null ? "" : o1.getTextToSort());
    String s2 = (o2 == null ? "" : o2.getTextToSort());
    return s1.compareTo(s2);
  }
};

  public static <T extends SplitableByLetter> Map<String, List<T>> splitByLetter(List<T> objects) {
    return splitByLetter(objects, comparator);
  }
 /**
   * Split a list of items by the first letter that is different in some (e.g. Mortal, More and Morris will be splitted on t, e and r)
   * @param objects
   * @return
   */
  public static <T extends SplitableByLetter> Map<String, List<T>> splitByLetter(List<T> objects, Comparator comparator) {
    Map<String, List<T>> splitMap = new HashMap<String, List<T>>();

    // construct a list of all strings to split
    Vector<String> stringsToSplit = new Vector<String>(objects.size());
    String commonPart = null;
    for (T object : objects) {
      if (object == null)
        continue;
      String string = object.getTextToSort();

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

}
