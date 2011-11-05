package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Book implements SplitableByLetter {
  private final static DateFormat TIMESTAMP_INTITLE_FORMAT = new SimpleDateFormat("dd/MM");
  private final static Logger logger = Logger.getLogger(Book.class);

  private File bookFolder;
  private final String id;
  private final String uuid;
  private String title;
  private String titleForSort;
  private String titleWithSerieNumber;
  private String titleWithTimestamp;
  private String titleWithRating;
  private final String path;
  private String comment;
  private String summary;
  private Integer summaryMaxLength;
  private final Float serieIndex;
  private final Date timestamp;
  private final Date publicationDate;
  private final String isbn;
  private List<Author> authors;
  private String listOfAuthors;
  private String authorSort;
  private Publisher publisher;
  private Series series;
  private List<Tag> tags;
  private List<EBookFile> files;
  private boolean filesSorted = false;
  private EBookFile preferredFile;
  private EBookFile epubFile;
  private boolean epubFileComputed = false;
  private boolean preferredFileComputed = false;
  private String epubFileName;
  private long latestFileModifiedDate = -1;
  private final BookRating rating;
  private List<Language> bookLanguages = new LinkedList<Language>();
  private boolean flag;

  private static Date ZERO;
  static {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(0);
    ZERO = c.getTime();
  }

  public Book(String id,
      String uuid,
      String title,
      String path,
      Float serieIndex,
      Date timestamp,
      Date publicationDate,
      String isbn,
      String authorSort,
      BookRating rating) {
    super();
    this.id = id;
    this.uuid = uuid;
    setTitle(title);
    this.path = path;
    this.serieIndex = serieIndex;
    this.timestamp = timestamp;
    this.publicationDate = publicationDate;
    this.tags = new LinkedList<Tag>();
    this.files = new LinkedList<EBookFile>();
    this.authors = new LinkedList<Author>();
    this.isbn = isbn;
    this.authorSort = authorSort;
    this.rating = rating;
  }

  public String getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  private void setTitle(String value) {
    titleForSort = null;
    titleWithRating = null;
    titleWithSerieNumber = null;
    titleWithTimestamp = null;
    title = value;
    // clean up
    if (Helper.isNotNullOrEmpty(title)) {
      title = title.trim();
      title = title.substring(0, 1).toUpperCase() + title.substring(1);
    }
  }

  public String getTitle() {
    return title;
  }

  public String getTitleForSort() {
    if (titleForSort == null)
      titleForSort = NoiseWord.fromLanguage(getBookLanguage()).removeLeadingNoiseWords(getTitle());
    return titleForSort;
  }

  public Language getBookLanguage() {
    List<Language> languages = getBookLanguages();
    if ((languages == null) || (languages.size() == 0))
      return null;
    else
      return languages.get(0);
  }

  public List<Language> getBookLanguages() {
    return bookLanguages;
  }

  public void addBookLanguage(Language bookLanguage) {
    if (bookLanguage == null)
      return;
    if (bookLanguages == null)
      bookLanguages = new LinkedList<Language>();
    if (!bookLanguages.contains(bookLanguage))
      bookLanguages.add(bookLanguage);
  }

  public String getIsbn() {
    return (isbn == null ? "" : isbn);
  }

  public String getTitleWithSerieNumber() {
    if (titleWithSerieNumber == null) {
      DecimalFormat df = new DecimalFormat("####.##");
      titleWithSerieNumber = df.format(getSerieIndex()) + " - " + title;
    }
    return titleWithSerieNumber;
  }

  public String getTitleWithTimestamp() {
    if (titleWithTimestamp == null) {
      if (getTimestamp() != null)
        titleWithTimestamp = TIMESTAMP_INTITLE_FORMAT.format(getTimestamp()) + " - " + title;
      else
        titleWithTimestamp = title;
    }
    return titleWithTimestamp;
  }

  public String getTitleWithRating(String message, String ratingText) {
    if (titleWithRating == null) {
      if (getRating() != BookRating.NOTRATED)
        titleWithRating = MessageFormat.format(message, getTitle(), ratingText);
      else
        titleWithRating = title;
    }
    return titleWithRating;
  }


  public String getPath() {
    return path;
  }

  public String getComment() {
    return comment;
  }

  /**
   * TODO  THis is still being worked on - not yet being used
   * Remove leading text from the given XHTNL string.  This is
   *        used to remove words such as SUMMARY that we add
   *        ourselves in the catalog.   It is also used to remove
   *        other common expressions from the Summary to try and
   *        leave text that is more useful as a summary.
   *
   *        Special processing requirements:
   *        - Any leading HTML tags or spaces are ignored
   *        - Any trailing ':' or spaces are removed
   *        - If after removing the desired text there are
   *          HTML tags that were purely surrounding the text
   *          that has been removed, they are also removed.
   * @param text            Text that is being worked on
   * @param LeadingText     String to be checked for (case ignored)
   * @return                Result of removing text, or null if input was null
   */
  private String removeLeadingText (String text, String leadingText) {
    // Check for fast exit conditions
    if (text == null || leadingText == null || text.charAt(0) != '<')
      return text;

    int textLength = text.length();
    int leadingLength = leadingText.length();
    int startText = 0;
    // skip over leading tags
    while (text.charAt(startText) == '<' ) {
        int tagEnd = text.indexOf('>');
        if (tagEnd == -1)
            return text;
        else
            startText = tagEnd + 1;
    }
    if (text.subSequence(startText,startText+leadingLength -1) != leadingText)
      return text;
    text.trim();
    // Now try and get past any tags

    // If there is no match on leading text, then give up and return result same as input

    // After removing leading text, now remove any tags that are now empty of content

    return text;
  }

  /**
   * Remove the word 'SUMMARY' from the start of the given string if it present
   * TODO  Enhance this by looking beyond starting HTML tags?
   * @param text
   * @return Input if null or does not start with SUMMARY
   */
  private String removeSummaryText (String text) {
    if (text != null) {
      // Special Processing - remove SUMMARY from start of comment field (if present)
      if (text.toUpperCase(Locale.ENGLISH).startsWith("SUMMARY:"))
        text = text.substring(8);
      else if (text.toUpperCase(Locale.ENGLISH).startsWith("SUMMARY"))
        text = text.substring(6);
    }
    return text;
  }
  /**
   * Sets the comment value
   * If it starts with 'SUMMARY' then this is removed as superfluous
   * NOTE.  Comments are allowed to contain (X)HTML) tags
   * @param value the new comment
   */
  public void setComment(String value) {
    summary = null;
    summaryMaxLength = -1;
    comment = removeSummaryText(value);
    // The following log entry can be useful if trying to debug character encoding issues
    // logger.info("Book " + id + ", setComment (Hex): " + Database.INSTANCE.stringToHex(comment));
  }

  /**
   * Sets the book summary
   * This starts with any series information, and then as much of the book comment as
   * will fit in the space allowed.  The word 'SUMMARY' is removed as superfluous at the
   * start of the text part of the summary.
   * NOTE.  Summary must be pure text (no (X)HTML tags) for OPDS compatibility
   * @param maxLength   Maximum length of text allowed in summary
   * @return
   */
  public String getSummary(int maxLength) {
    if (summary == null  || maxLength != summaryMaxLength) {
      summary = "";
      summaryMaxLength = maxLength;
      // Check for series info to include
      if (Helper.isNotNullOrEmpty(getSeries())) {
        float seriesIndexFloat = getSerieIndex();
        int seriesIndexInt = (int)seriesIndexFloat;
        String seriesIndexText;
        // For the commonest case of integers we want to truncate off the fractional part
        if (seriesIndexFloat == (float)seriesIndexInt)
           seriesIndexText = String.format("%d", seriesIndexInt);
        else {
          // For fractions we want only 2 decimal places to match calibre
          seriesIndexText = String.format("%.2f",seriesIndexFloat);
        }
        summary += Helper.shorten(getSeries().getName() + " [" + seriesIndexText + "]: ", maxLength);
      }
      // See if still space for comment info
      if (maxLength > (summary.length() + 3)) {
        String noHtml = removeSummaryText(Helper.removeHtmlElements(getComment()));
        if (noHtml != null) {
          // Special Processing - remove PRODUCT DESCRIPTION from start of comment field (if present)
          if (noHtml.toUpperCase(Locale.ENGLISH).startsWith("PRODUCT DESCRIPTION"))
            noHtml = noHtml.substring(19);
          // Is there actually any comment info?
          if (Helper.isNotNullOrEmpty(noHtml))
            summary += Helper.shorten(noHtml, maxLength - summary.length());
        }
      }
    }
    return summary;
  }

  public Float getSerieIndex() {
    return serieIndex;
  }

  public Date getTimestamp() {
    // ITIMPI:  Return 'now' if timestamp not set - would 0 be better?
    if (timestamp == null) {
      logger.warn("Date Added not set for book '" + title + "'");
      return new Date();
    }
    return timestamp;
  }

  public Date getPublicationDate() {
    if (publicationDate == null) {
      logger.warn("Publication Date not set for book '" + title + "'");
      return ZERO;
    }
    return (publicationDate);
  }

  public boolean hasAuthor() {
    return Helper.isNotNullOrEmpty(getAuthors());
  }

  public boolean hasSingleAuthor() {
    return (Helper.isNotNullOrEmpty(getAuthors()) && getAuthors().size() == 1);
  }

  public List<Author> getAuthors() {
    return authors;
  }

  public String getListOfAuthors() {
    if (listOfAuthors == null)
      listOfAuthors = Helper.concatenateList(" & ", getAuthors(), "getName");
    return listOfAuthors;
  }

  public Author getMainAuthor() {
    if (getAuthors() == null || getAuthors().size() == 0)
      return null;
    return getAuthors().get(0);
  }

  public String getAuthorSort() {
    return authorSort;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }

  public Series getSeries() {
    return series;
  }

  public void setSeries(Series value) {
    this.series = value;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public List<EBookFile> getFiles() {
    if (!filesSorted) {
      if (files != null && files.size() > 1) {
        Collections.sort(files, new Comparator<EBookFile>() {
          public int compare(EBookFile o1, EBookFile o2) {
            if (o1 == null)
              if (o2 == null)
                return 0;
              else
                return 1;
            if (o2 == null)
              if (o1 == null)
                return 0;
              else
                return -1;
            return Helper.checkedCompare(o1.getFormat(), o2.getFormat());
          }
        });
      }
      filesSorted = true;
    }
    return files;
  }

  public void removeFile(EBookFile file) {
    files.remove(file);
    epubFileComputed = false;
    preferredFileComputed = false;
    epubFile = null;
    preferredFile = null;
    latestFileModifiedDate = -1;
    filesSorted = false;
  }

  public void addFile(EBookFile file) {
    files.add(file);
    epubFileComputed = false;
    preferredFileComputed = false;
    epubFile = null;
    preferredFile = null;
    latestFileModifiedDate = -1;
    filesSorted = false;
  }

  public EBookFile getPreferredFile() {
    if (!preferredFileComputed) {
      for (EBookFile file : getFiles()) {
        if (preferredFile == null || file.getFormat().getPriority() > preferredFile.getFormat().getPriority())
          preferredFile = file;
      }
      preferredFileComputed = true;
    }
    return preferredFile;
  }

  public void addAuthor(Author author) {
    listOfAuthors = null;
    if (authors == null)
      authors = new LinkedList<Author>();
    if (!authors.contains(author))
      authors.add(author);
  }

  public String toString() {
    return getId() + " - " + getTitle();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Book) {
      return (Helper.checkedCompare(((Book) obj).getId(), getId()) == 0);
    } else
      return super.equals(obj);
  }

  public String toDetailedString() {
    return getId() + " - " + getMainAuthor().getName() + " - " + getTitle() + " - " + Helper.concatenateList(getTags()) + " - " + getPath();
  }

  public File getBookFolder() {
    if (bookFolder == null) {
      File calibreLibraryFolder = Configuration.instance().getDatabaseFolder();
      bookFolder = new File(calibreLibraryFolder, getPath());
    }
    return bookFolder;
  }

  public String getEpubFilename() {
    if (!epubFileComputed) {
      getEpubFile();
    }
    return epubFileName;
  }

  public EBookFile getEpubFile() {
    if (!epubFileComputed) {
      epubFile = null;
      epubFileName = null;
      for (EBookFile file : getFiles()) {
        if (file.getFormat() == EBookFormat.EPUB) {
          epubFile = file;
          epubFileName = epubFile.getName() + epubFile.getExtension();
        }
      }
      epubFileComputed = true;
    }
    return epubFile;
  }

  public boolean doesEpubFileExist() {
    EBookFile file = getEpubFile();
    if (file == null)
      return false;
    File f = file.getFile();
    return (f != null && f.exists());
  }

  public long getLatestFileModifiedDate() {
    if (latestFileModifiedDate == -1) {
      latestFileModifiedDate = 0;
      for (EBookFile file : getFiles()) {
        File f = file.getFile();
        if (f.exists()) {
          long m = f.lastModified();
          if (m > latestFileModifiedDate)
            latestFileModifiedDate = m;
        }
      }
    }
    return latestFileModifiedDate;
  }

  public BookRating getRating() {
    return rating;
  }

  public String getTitleToSplitByLetter() {
    return getTitleForSort();
  }

  public Book copy() {
    Book result = new Book(id, uuid, title, path, serieIndex, timestamp, publicationDate, isbn, authorSort, rating);
    result.setComment(this.getComment());
    result.setSeries(this.getSeries());
    result.setPublisher(this.getPublisher());

    result.files = new LinkedList<EBookFile>(this.getFiles());
    result.tags = new LinkedList<Tag>(this.getTags());
    result.authors = new LinkedList<Author>(this.getAuthors());

    return result;
  }

  public boolean isFlagged() {
    return flag;
  }

  public void setFlag() {
    flag = true;
  }

  public void clearFlag() {
    flag = false;
  }

}
