package com.gmail.dpierron.calibre.datamodel;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public enum DateRange {
  ONEDAY(1),
  ONEWEEK(7),
  FORTNIGHT(14),
  MONTH(30),
  TWOMONTHS(60),
  THREEMONTHS(90),
  SIXMONTHS(182),
  YEAR(365),
  MORE(-1);

  private final int nbDays;

  private static final long milPerDay = 1000 * 60 * 60 * 24;
  private static long midnight = 0;

  DateRange(int nbDays) {
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
    if (midnight == 0) {
      GregorianCalendar gc = new GregorianCalendar();
      // TODO  Check if Timezone is automatically handled?
      gc.set(Calendar.HOUR_OF_DAY, 23);    // Force time to just before midnight so that the
      gc.set(Calendar.MINUTE, 59);         // day divisions align with calendar day boundaries
      gc.set(Calendar.SECOND, 59);
      midnight = gc.getTime().getTime();
    }
    long days = (midnight - d.getTime()) / milPerDay;

    for (DateRange range : values()) {
      if (days < range.getNbDays())
        return range;
    }
    return MORE;
  }
}
