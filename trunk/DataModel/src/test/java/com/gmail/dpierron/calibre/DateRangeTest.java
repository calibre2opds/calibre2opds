package com.gmail.dpierron.calibre;

import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.gmail.dpierron.calibre.datamodel.DateRange;


public class DateRangeTest {

  @Test
  public void testValueOf() {
    GregorianCalendar d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -0);
    
    if (DateRange.ONEDAY != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on ONEDAY");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -6);
    
    if (DateRange.ONEWEEK != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on ONEWEEK");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -13);
    
    if (DateRange.FORTNIGHT != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on FORTNIGHT");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -30);
    
    if (DateRange.MONTH != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on MONTH");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -60);
    
    if (DateRange.TWOMONTHS != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on TWOMONTHS");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -90);
    
    if (DateRange.THREEMONTHS != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on THREEMONTHS");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -180);
    
    if (DateRange.SIXMONTHS != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on SIXMONTHS");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -360);
    
    if (DateRange.YEAR != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on YEAR");

    d = new GregorianCalendar();
    d.add(Calendar.DAY_OF_MONTH, -3000);
    
    if (DateRange.MORE != DateRange.valueOf(d.getTime()))
      fail("DateRange failed on MORE");

  }

}
