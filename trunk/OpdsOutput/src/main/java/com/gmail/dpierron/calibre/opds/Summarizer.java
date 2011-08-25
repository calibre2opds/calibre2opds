package com.gmail.dpierron.calibre.opds;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Author;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.Series;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.TreeNode;

public enum Summarizer {
  INSTANCE;

  public int getPageNumber(int itemNumber) {
    double pageSize = ConfigurationManager.INSTANCE.getCurrentProfile().getMaxBeforePaginate();
    double dItemNumber = itemNumber;
    double result = dItemNumber / pageSize;
    return (int) Math.ceil(result);
  }
  
  public String summarizeBooks(List elements) {
    InternalSummarizer summarizer = new InternalSummarizer() {
      
      @Override
      String getWord(int itemsCount) {
        return getBookWord(itemsCount);
      }
      
      @Override
      String getTitle(Object o) {
        return ((Book)o).getTitle();
      }
    };
    
    return summarizer.summarize(elements);
  }
  
  public String summarizeTags(List elements) {
    InternalSummarizer summarizer = new InternalSummarizer() {
      
      @Override
      String getWord(int itemsCount) {
        return getTagWord(itemsCount);
      }
      
      @Override
      String getTitle(Object o) {
        return ((Tag)o).getName();
      }
    };
    
    return summarizer.summarize(elements);
  }
  
  public String summarizeTagLevels(List elements) {
    InternalSummarizer summarizer = new InternalSummarizer() {
      
      @Override
      String getWord(int itemsCount) {
        return getTagLevelWord(itemsCount);
      }
      
      @Override
      String getTitle(Object o) {
        return ((TreeNode)o).getId();
      }
    };
    
    return summarizer.summarize(elements);
  }
  
  public String summarizeAuthors(List elements) {
    InternalSummarizer summarizer = new InternalSummarizer() {
      
      @Override
      String getWord(int itemsCount) {
        return getAuthorWord(itemsCount);
      }
      
      @Override
      String getTitle(Object o) {
        return ((Author)o).getLastName();
      }
    };
    
    return summarizer.summarize(elements);
  }
  
  public String summarizeSeries(List elements) {
    InternalSummarizer summarizer = new InternalSummarizer() {
      
      @Override
      String getWord(int itemsCount) {
        return getSeriesWord(itemsCount);
      }
      
      @Override
      String getTitle(Object o) {
        return ((Series)o).getName();
      }
    };
    
    return summarizer.summarize(elements);
  }
  
  public String getBookWord(int nb) {
    if (nb == 0)
      return Localization.Main.getText("bookword.none");
    else if (nb == 1)
      return Localization.Main.getText("bookword.one");
    else 
      return Localization.Main.getText("bookword.many", nb);
  }
  
  public  String getAuthorWord(int nb) {
    if (nb == 0)
      return Localization.Main.getText("authorword.none");
    else if (nb == 1)
      return Localization.Main.getText("authorword.one");
    else 
      return Localization.Main.getText("authorword.many", nb);
  }
  
  public  String getTagLevelWord(int nb) {
    if (nb == 0)
      return Localization.Main.getText("taglevelword.none");
    else if (nb == 1)
      return Localization.Main.getText("taglevelword.one");
    else 
      return Localization.Main.getText("taglevelword.many", nb);
  }
  
  public  String getSeriesWord(int nb) {
    if (nb == 0)
      return Localization.Main.getText("seriesword.none");
    else if (nb == 1)
      return Localization.Main.getText("seriesword.one");
    else 
      return Localization.Main.getText("seriesword.many", nb);
  }
  
  public  String getTagWord(int nb) {
    if (nb == 0)
      return Localization.Main.getText("tagword.none");
    else if (nb == 1)
      return Localization.Main.getText("tagword.one");
    else 
      return Localization.Main.getText("tagword.many", nb);
  }
  
  private abstract class InternalSummarizer {
    abstract String getWord(int itemsCount);
    abstract String getTitle(Object o);
    
    public Composite<Integer, String> concatenateListWhileShortening(String separator, Collection toConcatenate, int maxSize) {
      Composite<Integer, String> badResult = new Composite<Integer, String>(0, null);
      
      try {
        if (toConcatenate == null || toConcatenate.size() == 0)
          return badResult;
        StringBuffer lines = new StringBuffer();
        String theSeparator = (separator == null ? ", " : separator);
        Iterator iter = toConcatenate.iterator();
        int howMuch = 0;
        while (iter.hasNext() && lines.length() < maxSize) {
          Object o = iter.next();
          String result = null;
          result = getTitle(o);
          if (result == null)
            result = o.toString();
          if (lines.length() < (maxSize - result.length())) {
            lines.append(result);
            howMuch++;
            if (iter.hasNext() && lines.length() < (maxSize - theSeparator.length()))
              lines.append(theSeparator);
          } else {
            lines.append("...");
            break;
          }
        }
        return new Composite<Integer, String>(howMuch, lines.toString());
      } catch (Exception e) {
        return badResult;
      }
    }
    
    public String summarize(List elements) {
      return summarize(elements, false);
    }
    
    public String summarize(List elements, boolean includePagesCount) {
      if (elements == null)
        return "";
      
      int itemsCount = elements.size();
  
      // try and list the items to make the summary
      Composite<Integer, String> shortList = concatenateListWhileShortening(", ", elements, ConfigurationManager.INSTANCE.getCurrentProfile().getMaxSummaryLength()); 
      int shortListItemCount = shortList.getFirstElement();
      String summary = shortList.getSecondElement();
      if (shortListItemCount < itemsCount) {
        // too many items in this list, we cannot simply list them
        String itemsCountSummary = getWord(itemsCount);
        summary = itemsCountSummary;
        if (includePagesCount) {
          int maxPages = getPageNumber(itemsCount);
          if (maxPages > 1)
            summary = Localization.Main.getText("title.numberOfPages", itemsCountSummary, maxPages);
        }
      }
      
      return summary;
    }
  }  
  
}
