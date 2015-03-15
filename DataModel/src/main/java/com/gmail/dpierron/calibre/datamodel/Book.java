package com.gmail.dpierron.calibre.datamodel;

import com.gmail.dpierron.calibre.configuration.Configuration;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class Book implements SplitableByLetter {
  private final static Logger logger = Logger.getLogger(Book.class);

  private File bookFolder;
  private final String id;
  private final String uuid;
  private String title;
  private String titleSort;
  private final String path;
  private String comment;
  private String summary;
  private Integer summaryMaxLength;
  private final Float serieIndex;
  private final Date timestamp;
  private final Date modified;
  private final Date publicationDate;
  private final String isbn;
  private List<Author> authors;
  private String listOfAuthors;
  private String authorSort;
  private Publisher publisher;
  private Series series;
  private List<Tag> tags;
  private List<EBookFile> files;
  private EBookFile preferredFile;
  private EBookFile epubFile;
  private String epubFileName;
  private long latestFileModifiedDate = -1;
  private final BookRating rating;
  private List<Language> bookLanguages = new LinkedList<Language>();
  private List<CustomColumnValue> customColumnValues;
  private static Date ZERO;
  private static final Pattern tag_br = Pattern.compile("\\<br\\>", Pattern.CASE_INSENSITIVE);
  protected Book copyOfBook;          // If a book is copied then this points to the original

  // Flags
  // NOTE: Using byte plus bit settings is more memory efficient than using boolean types
  private final static byte FLAG_ALL_CLEAR = 0;
  private final static byte FLAG_DONE = 0x01;                   // Set when the Book full details have been generated
  private final static byte FLAG_REFERENCED = 0x02;             // Set if book full details must be generated as referenced
  private final static byte FLAG_FILES_SORTED = 0x04;
  private final static byte FLAG_EPUBFILE_COMPUTED = 0x08;
  private final static byte FLAG_PREFERREDFILECOMPUTED = 0x10;
  private final static byte FLAG_CHANGED = 0x20;
  private final static byte FLAG_FLAGGED = 0x40;
  private byte flags = FLAG_ALL_CLEAR;

  static {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(0);
    ZERO = c.getTime();
  }

  // CONSTRUCTORS

  public Book(String id,
      String uuid,
      String title,
      String title_sort,
      String path,
      Float serieIndex,
      Date timestamp,
      Date modified,
      Date publicationDate,
      String isbn,
      String authorSort,
      BookRating rating) {
    super();
    assert Helper.isNotNullOrEmpty(id);
    this.id = id;
    assert Helper.isNotNullOrEmpty(uuid);
    this.uuid = uuid;

    assert Helper.isNotNullOrEmpty(title) : "Unexpected null/empty title for book ID " + id;
    // Do some (possibly unnecessary) tidyong of title
    title = title.trim();
    this.title = title.substring(0, 1).toUpperCase() + title.substring(1);
    // title_sort is normally set by Calibre autoamtically, but it can be cleared
    // by users and may not be set by older versions of Calibre.  In these
    // cases we fall back to using the (mandatory) title field and issue a warning
    if (Helper.isNullOrEmpty(title_sort)) {
      logger.warn("Title_Sort not set - using Title for book '" + this.title + "'");
      this.titleSort = DataModel.INSTANCE.getNoiseword(getBookLanguage()).removeLeadingNoiseWords(this.title);
    } else {
      this.titleSort = title_sort;
    }
      // Small memory optimisation to re-use title object if possible
      // TODO check if unecessary if Java does this automatically?
    if (this.title.equalsIgnoreCase(this.titleSort)) {
      this.titleSort = this.title;
    }

    this.path = path;
    this.serieIndex = serieIndex;
    this.timestamp = timestamp;
    this.modified = modified;
    this.publicationDate = publicationDate;
    this.tags = new LinkedList<Tag>();
    this.files = new LinkedList<EBookFile>();
    this.authors = new LinkedList<Author>();
    this.isbn = isbn;
    this.authorSort = authorSort;
    this.rating = rating;
    copyOfBook = null;
  }

  // METHODS and PROPERTIES
  /**
   *   Helper routine to set flags bits state
   */
  private void setFlags (boolean b, int f) {
    if (b == true)
      flags |= f;           // Set flag bits
    else
      flags &= ~f;           // Clear flag bits;
  }

  /**
   * Helper routine to check if specified kags set
   * @param f
   * @return
   */
  private boolean isFlags( int f) {
    return ((flags & f) == f);
  }


  public String getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  public String getTitle() {
    return title;
  }

  public String getTitle_Sort() {
    return titleSort;
  }

  /**
   * Return the first listed language for a book
   * (this is on the assumption it is the most important one)
   * @return
   */
  public Language getBookLanguage() {
    if (copyOfBook != null) return copyOfBook.getBookLanguage();
    List<Language> languages = getBookLanguages();
    if ((languages == null) || (languages.size() == 0))
      return null;
    else
      return languages.get(0);
  }

  /**
   * Get all the languages for a book.
   * Most of the time there will only be one, but Ca;ibre
   * allows for multpiple languages on the same book.
   * @return
   */
  public List<Language> getBookLanguages() {
    if (copyOfBook != null) return copyOfBook.getBookLanguages();
    return bookLanguages;
  }

  /**
   * Add a language to the book.
   *
   * @param bookLanguage
   */
  public void addBookLanguage(Language bookLanguage) {
    assert copyOfBook == null;        // Never expect this to be used on a copy of the book!
    if (getBookLanguages() == null) {
      bookLanguages = new LinkedList<Language>();
    }
    if (!bookLanguages.contains(bookLanguage)) {
      bookLanguages.add(bookLanguage);
    }
  }

  public String getIsbn() {
    return (isbn == null ? "" : isbn);
  }

  public String getPath() {
    return path;
  }

  public String getComment() {
    if (copyOfBook != null) return copyOfBook.getComment();
    return comment;
  }

  /**
   * Remove leading text from the given XHTNL string.  This is
   * used to remove words such as SUMMARY that we add  ourselves
   * in the catalog.   It is also used to remove other common
   * expressions from the Summary to try and leave text that is more
   * useful as a summary.
   *
   *        Special processing requirements:
   *        - Any leading HTML tags or spaces are ignored
   *        - Any trailing ':' or spaces are removed
   *        - If after removing the desired text there are
   *          HTML tags that were purely surrounding the text
   *          that has been removed, they are also removed.
   * @param text            Text that is being worked on
   * @param leadingText     String to be checked for (case ignored)
   * @return                Result of removing text, or null if input was null
   */
  private String removeLeadingText (String text, String leadingText) {
    // Check for fast exit conditions
    if (Helper.isNullOrEmpty(text) || Helper.isNullOrEmpty(leadingText) )
      return text;
    int textLength = text.length();
    int leadingLength = leadingText.length();

    int cutStart = 0;          // Start scanning from beginning of string
    int cutStartMax = textLength - leadingLength - 1 ;

    // skip over leading tags and spaces
    boolean scanning = true;
    while (scanning) {
      // Give up no room left to match text
      if (cutStart > cutStartMax)
        return text;
      // Look for special characters
      switch (text.charAt(cutStart)) {
        case ' ':
                cutStart++;
                break;
        case '<':
                // Look for end of tag
                int tagEnd = text.indexOf('>',cutStart);
                // If not found then give up But should this really occur)
                if (tagEnd == -1)
                  return text;
                else
                  cutStart = tagEnd + 1;
                break;
        default:
                scanning = false;
                break;
      }
    }

   // Exit if text does not match
    if (! text.substring(cutStart).toUpperCase(Locale.ENGLISH).startsWith(leadingText.toUpperCase(Locale.ENGLISH)))
      return text;
    // Set end of text to remove
    int cutEnd = cutStart + leadingLength;

    // After removing leading text, now remove any tags that are now empty of content
    // TODO - complete the logic here.  Currently does not remove such empty tags
    scanning=true;
    while (scanning) {
      if (cutEnd >= textLength)  {
        scanning = false;
      }
      else {
        switch (text.charAt(cutEnd)) {
            case ' ':
            case ':':
                cutEnd++;
                break;
            case '<':
                if (text.charAt(cutEnd+1) != '/'){
                  // Handle case of BR tag following removed text
                  if (text.substring(cutEnd).toUpperCase().startsWith("<BR")) {
                      int tagEnd = text.indexOf('>', cutEnd+1);
                      if (tagEnd != -1)  {
                        cutEnd = tagEnd + 1;
                        break;
                      }
                  }
                  scanning = false;
                  break;
                }
                else {
                  int tagEnd = text.indexOf('>');
                  if (tagEnd == -1)  {
                    scanning = false;
                    break;
                  }
                  else {
                      cutEnd = tagEnd + 1;
                  }
                }
                break;
            default:
                scanning = false;
                break;
        } // End of switch
      }
    }   // End of while

    if (cutStart > 0)
      return (text.substring(0, cutStart) + text.substring(cutEnd)).trim();
    else
      return text.substring(cutEnd).trim();
  }

  /**
   * Sets the comment value
   * If it starts with 'SUMMARY' then this is removed as superfluous
   * NOTE.  Comments are allowed to contain (X)HTML) tags
   * @param value the new comment
   */
  public void setComment(String value) {
    assert copyOfBook == null;    // Never expect this to be used on a copy
    summary = null;
    summaryMaxLength = -1;
    if (Helper.isNotNullOrEmpty(value)) {
      comment = removeLeadingText(value, "SUMMARY");
      comment = removeLeadingText(comment, "PRODUCT DESCRIPTION");
      // The following log entry can be useful if trying to debug character encoding issues
      // logger.info("Book " + id + ", setComment (Hex): " + Database.INSTANCE.stringToHex(comment));
    
      if (comment != null && comment.matches("(?i)\\<br\\>")) {
        logger.warn("<br> tag in comment changed to <br /> for Book: Id=" + id + " Title=" + title);
        comment.replaceAll("(?i)\\<br\\>", "<br />");
      }
    }
  }

  /**
   * Get the book summary
   * This starts with any series information, and then as much of the book comment as
   * will fit in the space allowed.
   *
   * Special processing Requirements
   * - The word 'SUMMARY' is removed as superfluous at the start of the comment text
   * - The words 'PRODUCT DESCRIPTION' are removed as superfluous at the start of the comment text
   * - The calculated value is stored for later re-use (which is very likely to happen).
   * NOTE.  Summary must be pure text (no (X)HTML tags) for OPDS compatibility
   * @param maxLength   Maximum length of text allowed in summary
   * @return The value of the calculated summary field
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
        summary += getSeries().getName() + " [" + seriesIndexText + "]: ";
        if (maxLength != -1) {
          summary = Helper.shorten(summary, maxLength);
        }
      }
      // See if still space for comment info
      // allow for special case of -1 which means no limit.
      if (maxLength == -1 || (maxLength > (summary.length() + 3))) {
        String noHtml = Helper.removeHtmlElements(getComment());
        if (noHtml != null) {
          noHtml = removeLeadingText(noHtml, "SUMMARY");
          noHtml = removeLeadingText(noHtml, "PRODUCT DESCRIPTION");
          if (maxLength == -1 ) {
            summary += noHtml;
          } else {
            summary += Helper.shorten(noHtml, maxLength - summary.length());
          }
        }
      }
    }
    return summary;
  }

  /**&
   * Get the series index.
   * It is thought that Calibre always sets this but it is better to play safe!
   * @return
   */
  public Float getSerieIndex() {
    if (Helper.isNotNullOrEmpty(serieIndex)) {
      return  serieIndex;
    }
    // We never expect to get here!
    logger.warn("Unexpected null/empty Series Index for book " + getTitle() + "(" + getId() + ")");
    return (float)1.0;
  }

  public Date getTimestamp() {
    // ITIMPI:  Return 'now' if timestamp not set - would 0 be better?
    if (timestamp == null) {
      logger.warn("Date/Time Added not set for book '" + title + "'");
      return new Date();
    }
    return timestamp;
  }

  public Date getModified() {
    // ITIMPI:  Return 'now' if modified not set - would 0 be better?
    if (modified == null) {
      logger.warn("Date/Time Modified not set for book '" + title + "'");
      return new Date();
    }
    return modified;
  }

  public Date getPublicationDate() {
    if (publicationDate == null) {
      logger.warn("Publication Date not set for book '" + title + "'");
      return ZERO;
    }
    return (publicationDate);
  }

  public boolean hasAuthor() {
    return  Helper.isNotNullOrEmpty(getAuthors());
  }

  public boolean hasSingleAuthor() {
    return (Helper.isNotNullOrEmpty(authors) && authors.size() == 1);
  }

  public List<Author> getAuthors() {
    return authors;
  }

  /**
   * Create a comma separated list of authors
   * @return
   */
  public String getListOfAuthors() {
    if (listOfAuthors == null)
      listOfAuthors = Helper.concatenateList(" & ", getAuthors(), "getName");
    return listOfAuthors;
  }

  public Author getMainAuthor() {
    if (copyOfBook != null) return copyOfBook.getMainAuthor();
    if (getAuthors() == null || getAuthors().size() == 0)
      return null;
    return  getAuthors().get(0);
  }

  public String getAuthorSort() {
    return authorSort;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    assert copyOfBook == null;    // Do not expect this on a copy
    this.publisher = publisher;
  }

  public Series getSeries() {
    return series;
  }

  public void setSeries(Series value) {
    assert copyOfBook == null;    // Do not expect this on a copy
    this.series = value;
  }

  /**
   * Note that the list of tags for a books can be different
   * in the master and in any copies of the book object
   *
   * @return
   */
  public List<Tag> getTags() {
    return tags;
  }

  /**
   * Get the list of eBook files associated with this book.
   * @return
   */
  public List<EBookFile> getFiles() {
    if (copyOfBook != null) return copyOfBook.getFiles();
    if (! isFlags(FLAG_FILES_SORTED)) {
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
       setFlags(true, FLAG_FILES_SORTED);;
    }
    return files;
  }

  public void removeFile(EBookFile file) {
    assert copyOfBook == null;    // Do not expect this on a copy
    files.remove(file);
    epubFile = null;
    preferredFile = null;
    latestFileModifiedDate = -1;
    setFlags(false,FLAG_EPUBFILE_COMPUTED + FLAG_PREFERREDFILECOMPUTED + FLAG_FILES_SORTED);
  }

  /**
   *
   * @param file
   */
  public void addFile(EBookFile file) {
    assert copyOfBook == null;      // Fo not expect this on a copy
    files.add(file);
    epubFile = null;
    preferredFile = null;
    latestFileModifiedDate = -1;
    setFlags(false,FLAG_EPUBFILE_COMPUTED + FLAG_PREFERREDFILECOMPUTED + FLAG_FILES_SORTED);
  }

  public EBookFile getPreferredFile() {
    if (copyOfBook != null) return copyOfBook.getPreferredFile();
    if (! isFlags(FLAG_PREFERREDFILECOMPUTED)) {
      for (EBookFile file : getFiles()) {
        if (preferredFile == null || file.getFormat().getPriority() > preferredFile.getFormat().getPriority())
          preferredFile = file;
      }
      setFlags(true, FLAG_PREFERREDFILECOMPUTED);
    }
    return preferredFile;
  }

  /**
   * @param author
   */
  public void addAuthor(Author author) {
    assert copyOfBook == null;    // Do not expect this on a copy
    listOfAuthors = null;     // Force display list to be recalculated
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
    if (copyOfBook != null) return copyOfBook.getBookFolder();
    if (bookFolder == null) {
      File calibreLibraryFolder = Configuration.instance().getDatabaseFolder();
      bookFolder = new File(calibreLibraryFolder, getPath());
    }
    return bookFolder;
  }

  public String getEpubFilename() {
    if (! isFlags(FLAG_EPUBFILE_COMPUTED)) {
      getEpubFile();
    }
    return epubFileName;
  }

  public EBookFile getEpubFile() {
    if (!isFlags(FLAG_EPUBFILE_COMPUTED)) {
      epubFile = null;
      epubFileName = null;
      for (EBookFile file : getFiles()) {
        if (file.getFormat() == EBookFormat.EPUB) {
          epubFile = file;
          epubFileName = epubFile.getName() + epubFile.getExtension();
        }
      }
      setFlags(true, FLAG_EPUBFILE_COMPUTED);
    }
    return epubFile;
  }

  public boolean doesEpubFileExist() {
    if (copyOfBook != null) return copyOfBook.doesEpubFileExist();
    EBookFile file = getEpubFile();
    if (file == null)
      return false;
    File f = file.getFile();
    return (f != null && f.exists());
  }

  public long getLatestFileModifiedDate() {
    if (copyOfBook != null) return copyOfBook.getLatestFileModifiedDate();
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
    if (copyOfBook != null)  return copyOfBook.getRating();
    return rating;
  }

  public String getTitleToSplitByLetter() {
    return DataModel.INSTANCE.getLibrarySortTitle() ? getTitle() : getTitle_Sort();
  }

  /**
   * Make a copy of the book object.
   *
   * The copy has some special behavior in that most properties are read
   * from the original, but the tags one is still private.
   *
   * NOTE:  Can pass as null values that are alwars read from parent!
   *        Not sure if this reduces RAM usage or not!
   *
   * @return  Book object that is the copy
   */
  public Book copy() {
    Book result = new Book(id,uuid,title,titleSort,path,serieIndex,timestamp,modified,publicationDate,isbn,authorSort,rating);

    // Set some private variables that should be invariant in a copy
    authors = this.authors;
    series = this.series;
    publisher = this.publisher;
    listOfAuthors = this.listOfAuthors;
    authorSort = this.authorSort;
    latestFileModifiedDate = this.getLatestFileModifiedDate();
    epubFile = this.getEpubFile();
    epubFileName = this.getEpubFilename();
    customColumnValues = this.customColumnValues;
    comment = this.comment;
    summary = this.summary;
    summaryMaxLength = this.summaryMaxLength;

    // The tags aassciated with this entry may be changed, so we make
    // a copy of the ones currently associated
    result.tags = new LinkedList<Tag>(this.getTags());

    // Indicate this is a copy by setting a reference to the parent
    // This is used to read/set variables that must be in parent.
    result.copyOfBook = (this.copyOfBook == null) ? this : this.copyOfBook;
    return result;
  }

  public boolean isFlagged() {
    if (copyOfBook != null) return copyOfBook.isFlagged();
    return isFlags(FLAG_FLAGGED);
  }

  public void setFlagged() {
    if (copyOfBook != null) {
      copyOfBook.setFlagged();
      return;
    }
    setFlags(true, FLAG_FLAGGED);
  }

  public void clearFlagged() {
    if (copyOfBook != null) {
      copyOfBook.clearFlagged();
      return;
    }
    setFlags(false, FLAG_FLAGGED);
  }

  /**
   * Return whether we believe book has been changed since last run
   * @return
   */
  public boolean isChanged() {
    if (copyOfBook != null) return copyOfBook.isChanged();
    return isFlags(FLAG_CHANGED);
  }

  /**
   * Set the changed status to be true
   * (default is false, -so should only need to set to true.
   * If neccesary could change to pass in required state).
   */
  public void setChanged()  {
    if (copyOfBook != null) {
      copyOfBook.setChanged();
      return;
    }
    setFlags(true, FLAG_CHANGED);
  }

  public List<CustomColumnValue> getCustomColumnValues() {
    return customColumnValues;
  }

  public void setCustomColumnValues (List <CustomColumnValue> values) {
    customColumnValues = values;
  }

  public CustomColumnValue getCustomColumnValue (String name) {
    assert false : "getCustomColumnValue() not yet ready for use";
    return null;
  }

  public void setCustomColumnValue (String name, String value) {
    assert false : "setCustomColumnValue() not yet ready for use";
  }

  public void setDone() {
    if (copyOfBook != null) {
      copyOfBook.setDone();
      return;
    }
    setFlags(true, FLAG_DONE);
  }

  public boolean isDone() {
    if (copyOfBook != null) return copyOfBook.isDone();
    return isFlags(FLAG_DONE);
  }

  /**
   * Set referenced flag.
   */
  public void setReferenced() {
    if (copyOfBook != null) {
      copyOfBook.setReferenced();
      return;
    }
    setFlags(true, FLAG_REFERENCED);
  }

  /**
   * Get referenced flag
   *
   * @return
   */
  public boolean isReferenced() {
    if (copyOfBook != null) return copyOfBook.isReferenced();
    return isFlags(FLAG_REFERENCED);
  }
}
