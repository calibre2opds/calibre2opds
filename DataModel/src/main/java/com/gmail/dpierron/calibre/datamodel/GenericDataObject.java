package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.tools.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by WalkerDJ on 16/04/2015.
 *
 * This object is meant to act as a way of provinding any methods and
 * function that need to be supplied by all data objects that can
 * generate their own catalog sub-section so that the logic for
 * such generation can be made as generic as possible.
 */
public abstract class GenericDataObject implements SplitableByLetter {
  private final static Logger logger = LogManager.getLogger(Author.class);

  public abstract String getTitleToSplitByLetter();

  static void  sortObjectsByTitle(List<GenericDataObject> objs) {
    Collections.sort(objs, new Comparator<GenericDataObject>() {
      public int compare(GenericDataObject o1, GenericDataObject o2) {
        return Helper.checkedCollatorCompareIgnoreCase(o1.getTitleToSplitByLetter(), o2.getTitleToSplitByLetter());
      }
    });
  }

}
