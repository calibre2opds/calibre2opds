package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.EBookFile;
import com.gmail.dpierron.calibre.datamodel.EBookFormat;
import com.gmail.dpierron.tools.Helper;

import java.util.*;

public class SelectedEbookFormatsFilter implements BookFilter {

  List<EBookFormat> includedFormats;
  boolean includeBooksWithNoFile;

  public SelectedEbookFormatsFilter(String includedFormatsList, boolean includeBooksWithNoFile) {
    includedFormats = new LinkedList<EBookFormat>();
    if (Helper.isNullOrEmpty(includedFormatsList) || "ALL".equalsIgnoreCase(includedFormatsList))
      includedFormats = Arrays.asList(EBookFormat.values());
    else {
      List<String> list = Helper.tokenize(includedFormatsList, ",", true);
      int priority = list.size();
      for (String string : list) {
        EBookFormat format = EBookFormat.fromFormat(string.trim());
        if (format != null) {
          format.setPriority(priority--);
          includedFormats.add(format);
        }
      }
    }
    this.includeBooksWithNoFile = includeBooksWithNoFile;
  }

  private List<EBookFormat> getIncludedFormats() {
    return includedFormats;
  }

  public boolean didBookPassThroughFilter(Book book) {
    if (book == null)
      return false;

    List<EBookFile> files = new LinkedList<EBookFile>(book.getFiles());
    for (EBookFile eBookFile : files) {
      if (!getIncludedFormats().contains(eBookFile.getFormat()))
        book.removeFile(eBookFile);
    }
    if (book.getFiles().size() == 0)
      return includeBooksWithNoFile;
    else {
      Collections.sort(book.getFiles(), new Comparator<EBookFile>() {

        public int compare(EBookFile o1, EBookFile o2) {
          if (o1 == null && o2 == null)
            return 0;
          if (o1 == null && o2 != null)
            return 1;
          if (o1 != null && o2 == null)
            return -1;
          return new Integer(o2.getFormat().getPriority()).compareTo(new Integer(o1.getFormat().getPriority()));
        }

      });
      return true;
    }
  }

}
