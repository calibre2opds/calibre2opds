package com.gmail.dpierron.calibre.datamodel;

/**
 * Class for maintaining details about a Calibre series
 */
import com.gmail.dpierron.tools.Helper;

import java.util.Locale;


public class Series extends GenericDataObject implements SplitableByLetter {
  private String id;            // Calibre database ID for this series
  private String displayName;          // Display displayName
  private String sortName;          // Sort displayName set in Calibre
  private Locale locale;        // Locale for this series.  English if not known
  // private boolean done = false;       // sse true when this series has been generated
  // private boolean referenced = false; // Set true if referenced (which means entry needs generating
  // Flags
  // NOTE: Using byte plus bit settings is more memory efficient than using boolean types
  private final static byte FLAG_ALL_CLEAR = 0;
  private final static byte FLAG_DONE = 0x01;
  private final static byte FLAG_REFERENCED = 0x02;
  private byte flags = FLAG_ALL_CLEAR;

  // CONSTRUCTORS

  public Series(String id, String name, String sort) {
    super();
    assert (Helper.isNotNullOrEmpty(name));
    this.id = id;
    this.displayName = name;
    // Calibre has a title_sort field but at the moment does
    // appears to be always setting it to be the same as the
    // series displayName field even if it starts with noise words!
    // if (Helper.isNullOrEmpty(sortName)) {
      // TDDO  How we determine the Locale for a Series if not english?
    if (name.toUpperCase().startsWith("THE ")) {
      int dummy = 1;
    }
      this.sortName = DataModel.getNoiseword(Locale.ENGLISH).removeLeadingNoiseWords(name);
    // } else {
    //  this.sortName = sortName;
    // }

    // We try and optimise storage by pointing sortName at the
    // displayName field if they areidentical
    // TODO check if unecessary if Java does this automatically?

    if (this.displayName.equalsIgnoreCase(this.sortName)) {
      this.sortName = this.displayName;
    }
    locale = Locale.ENGLISH;

  }

  // METHODS and PROPERTIES implementing Abstract ones from Base class)

  public String getColumnName () {
    return "Series";
  }
  public ColumType getColumnType() {
    return ColumType.COLUMN_SERIES;
  }
  public String getDisplayName() {
    return displayName;
  }
  public String getSortName() {
      return sortName;
  }
  public String getTextToDisplay() {
    return DataModel.displaySeriesSort ? getDisplayName() : getSortName();
  }
  public String getTextToSort() {
    return DataModel.librarySortSeries ? getSortName(): getDisplayName();
  }

  //                            METHODS and PROPERTIES

  public String getId() {
    return id;
  }


  public String toString() {
    return getId() + " - " + getDisplayName();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Series) {
      return (Helper.checkedCompare(((Series) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
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

  /**
   * Set the locale for the series.
   *
   * NOTE:  Calibre does not have the concpt of locale for a series.
   *        This method allows for the case where calibre2opds manages
   *        to determine empirically that another locale should be used
   *        by the fact that the books in the series belong to a
   *        different locale.
   * @param locale
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Get the current locale set for the series
   * If we do know better then English will have been assumed.
   *
   * @return
   */
  public Locale getLocale() {
    return locale;
  }
}
