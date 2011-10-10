package com.gmail.dpierron.calibre.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public enum DateRange {
  ONEDAY(1),
  ONEWEEK(7),
  FORTNIGHT(15),
  MONTH(30),
  TWOMONTHS(60),
  THREEMONTHS(90),
  SIXMONTHS(180),
  YEAR(360),
  MORE(-1);

  private final int nbDays;

  private DateRange(int nbDays) {
    this.nbDays = nbDays;
  }

  private int getNbDays() {
    return nbDays;
  }

  /**
   * Work out which date range a date falls into for Recent Books section
   *
   * @param d Date to be checked
   * @return Range into which date falls
   */
  public static DateRange valueOf(Date d) {
    if (d == null)
      return MORE;
    GregorianCalendar da1 = new GregorianCalendar();
    GregorianCalendar da2 = new GregorianCalendar();
    da1.setTime(d);                      // Initialise to time to check
    da1.set(Calendar.HOUR_OF_DAY, 23);    // Force time to just before midnight so that the
    da1.set(Calendar.MINUTE, 59);         // day divisions align with calendar day boundaries
    da1.set(Calendar.SECOND, 59);         // Fixes bugs #716558 & #716923
    da2.set(Calendar.HOUR_OF_DAY, 23);    // Force time to just before midnight so that the
    da2.set(Calendar.MINUTE, 59);         // day divisions align with calendar day boundaries
    da2.set(Calendar.SECOND, 0);         //
    long d1 = da1.getTime().getTime();
    long d2 = da2.getTime().getTime();
    long difMil = d2 - d1;
    long milPerDay = 1000 * 60 * 60 * 24;

    long days = difMil / milPerDay;

    for (DateRange range : values()) {
      if (range.getNbDays() > days)
        return range;
    }
    return MORE;
  }
}
