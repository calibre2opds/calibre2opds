package com.gmail.dpierron.calibre.opds.indexer;

import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Author;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.Tag;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * The index of all the catalog items (books, authors, series, etc.) composing the catalog, with the keywords to search them full-text
 */
public class Index {
  private static int MIN_KEYWORD_SIZE = 3;
  private final static Logger logger = Logger.getLogger(Index.class);
//  private static Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  Map<String, Keyword> mapOfKeywords;

  public Index() {
    super();
    mapOfKeywords = new TreeMap<String, Keyword>();
  }

  private Index(Index toCopy) {
    this();
    mapOfKeywords = new TreeMap<String, Keyword>();
    if (toCopy.mapOfKeywords != null)
      mapOfKeywords.putAll(toCopy.mapOfKeywords);
  }

  public long size() {
    if (mapOfKeywords == null)
      return 0;
    return mapOfKeywords.size();
  }

  public static String prepareKeywordForIndexing(String keyword) {
    if (keyword == null)
      return null;

    String result = keyword;

    // all the keywords are in lower case
    result = result.toLowerCase(Locale.ENGLISH);
//    String temp = Normalizer.normalize(result, Normalizer.Form.NFD);
//    result = pattern.matcher(temp).replaceAll("");

    // trim the keywords
    result = result.trim();

    // no space in the keyword
    result = result.replace(" ", "");

    return result;
  }

  private List<String> splitStringIntoKeywords(String text, boolean pTags) {
    List<String> result = new ArrayList<String>();
    String splitTagsOn = ConfigurationManager.INSTANCE.getCurrentProfile().getSplitTagsOn();
    boolean processingTags = pTags && Helper.isNotNullOrEmpty(splitTagsOn);
    char tagChar = ' ';
    if (processingTags)
      tagChar = splitTagsOn.charAt(0);
    if (Helper.isNullOrEmpty(text)) {
      return result;
    }

    StringBuffer currentKeyword = new StringBuffer();
    for (char c : text.toCharArray()) {
      if (Character.isLetter(c) || (processingTags && (c == tagChar)))
        currentKeyword.append(c);
      else {
        if (currentKeyword.length() >= MIN_KEYWORD_SIZE)
          result.add(currentKeyword.toString());
        currentKeyword = new StringBuffer();
      }
    }
    result.add(currentKeyword.toString());
    return result;
  }

  public void addItem(String pKeyword, ItemType type, BookEntry bookEntry) {
    Keyword keyword;
    String word = prepareKeywordForIndexing(pKeyword);
    if (Helper.isNullOrEmpty(word))
      return;
    if (mapOfKeywords == null) {
      mapOfKeywords = new TreeMap<String, Keyword>();
      keyword = new Keyword(1, word);
      keyword.addCatalogItem(type, bookEntry);
    } else {
      keyword = mapOfKeywords.get(word);
      if (keyword == null) {
        keyword = new Keyword(mapOfKeywords.size(), word);
        keyword.addCatalogItem(type, bookEntry);
        mapOfKeywords.put(word, keyword);
      } else {
        keyword.addCatalogItem(type, bookEntry);
      }
    }
  }

  private void indexMultipleKeywords(String text, ItemType type, BookEntry bookEntry, boolean tags) {
    List<String> keywords = splitStringIntoKeywords(text, tags);
    for (String keyword : keywords) {
      addItem(keyword, type, bookEntry);
    }
  }

  public void indexBook(Book book, String url, String coverUrl) {
    if (logger.isTraceEnabled())
        logger.trace("indexBook: book=" + book + ", url=" + url + ", coverUrl=" + coverUrl);
    if (book == null)
      return;

    BookEntry bookEntry = new BookEntry(book, url, coverUrl);

    // parse the book title
    indexMultipleKeywords(book.getTitle(), ItemType.BookTitle, bookEntry, false);

    // parse the book comments
    if (ConfigurationManager.INSTANCE.getCurrentProfile().getIndexComments())
      indexMultipleKeywords(book.getComment(), ItemType.BookComment, bookEntry, false);

    // parse the book series
    if (book.getSeries() != null)
      indexMultipleKeywords(book.getSeries().getName(), ItemType.Series, bookEntry, false);

    // parse the book authors
    for (Author author : book.getAuthors()) {
      indexMultipleKeywords(author.getName(), ItemType.Author, bookEntry, false);
    }

    // parse the book tags
    for (Tag tag : book.getTags()) {
      indexMultipleKeywords(tag.getName(), ItemType.Tag, bookEntry, true);
    }
  }

  private String parseForApostrophes(String text) {
    return text.replace("'", "\\'");
  }

  private String parseForFrenchQuotes(String text) {
    return text.replace("\"", "\\\"");
  }

  public enum FilterHintType {
    RemoveRare,
    RemoveCommon,
    RemoveMedian;
  }

  /**
   * filter the current index, by making a smaller copy of it (reducing the number of keywords)
   *
   * @param maxKeywords the maximum number of keywords in the filtered index
   * @param filterHint  refines the selection algorithm
   * @return a filtered copy of the current index
   */
  public Index filterIndex(long maxKeywords, FilterHintType filterHint) {
    Index result = new Index(this);

    // we may already be small enough !
    if (size() <= maxKeywords || maxKeywords == -1)
      return result;

    // don't filter less than 10 items
    if (size() <= 10)
      return result;

    // sort the keywords in a list, by number of uses
    List<Keyword> keywords = new ArrayList<Keyword>(mapOfKeywords.size());
    keywords.addAll(mapOfKeywords.values());
    Collections.sort(keywords, new Comparator<Keyword>() {
      public int compare(Keyword o1, Keyword o2) {
        int o1Size = o1.catalogItems.size();
        int o2Size = o2.catalogItems.size();
        return o1Size - o2Size;
      }
    });

    // compute how much keywords we must remove
    long nbKeywordsToRemove = size() - maxKeywords;

    // parse the keywords, removing the less desired
    int startingPosition = 0;
    int position = startingPosition;
    if (filterHint == FilterHintType.RemoveCommon) {
      position = startingPosition = keywords.size() - 1;
    } else if (filterHint == FilterHintType.RemoveMedian) {
      startingPosition = keywords.size() / 2;
      position = startingPosition - 1;
    }

    while ((nbKeywordsToRemove > 0) && (position >= 0) && (position < keywords.size())) {
      // remove the current keyword
      Keyword keyword = keywords.get(position);
      result.mapOfKeywords.remove(keyword.word);
      // for the "remove median" case, remove the one to the same distance, on the opposite side of the middle
      if (filterHint == FilterHintType.RemoveMedian) {
        int oppositePosition = startingPosition + (startingPosition - position);
        keyword = keywords.get(oppositePosition);
        result.mapOfKeywords.remove(keyword.word);
        nbKeywordsToRemove = nbKeywordsToRemove - 2;
        position--;
      } else {
        nbKeywordsToRemove--;
        if (filterHint == FilterHintType.RemoveRare)
          position++;
        else
          position--;
      }
    }

    return result;
  }

  public void exportToJavascript(File exportFolder) throws FileNotFoundException {
    List<String> sqlKeywords = new ArrayList<String>(mapOfKeywords.size());
    List<String> sqlBooks = new ArrayList<String>(mapOfKeywords.size());
    List<String> sqlCatalogItems = new ArrayList<String>(mapOfKeywords.size());
    // clear the flag of all books
    for (Book book : DataModel.INSTANCE.getListOfBooks()) {
      book.clearFlag();
    }
    for (Keyword keyword : mapOfKeywords.values()) {
      String kwId = Long.toString(keyword.id);
      {
        // add a line in the KEYWORDS table
        String kwWord = keyword.word; // no need to search for apostrophes, the keywords are already cleaned-up and uppercase
        String kwWeight = "" + keyword.catalogItems.size();
        String sql = "tx.executeSql('INSERT INTO KEYWORDS (KW_ID, KW_WORD, KW_WEIGHT) VALUES (?, ?, ?)', ['" + kwId + "', '" + kwWord + "', '" + kwWeight + "']);";
        sqlKeywords.add(sql);
      }
      for (Map.Entry<ItemType, CatalogItem> catalogItemEntry : keyword.catalogItems.entrySet()) {
        for (BookEntry bookEntry : catalogItemEntry.getValue().bookEntries) {
          String bkId = bookEntry.book.getId();
          int bookId = Integer.parseInt(bkId);
          if (!bookEntry.book.isFlagged()) {
            bookEntry.book.setFlag();
            {
              // add a line in the BOOKS table
              String bkTitle = parseForApostrophes(bookEntry.book.getTitle());
              String bkUrl = bookEntry.url;
              String bkCoverUrl = bookEntry.coverUrl;
              String sql = "tx.executeSql('INSERT INTO BOOKS (BK_ID, BK_TITLE, BK_URL, BK_COVER_URL) VALUES (?, ?, ?, ?)', ['" + bkId + "', '" + bkTitle + "', '" + bkUrl + "','" + bkCoverUrl + "']);";
              sqlBooks.add(sql);
            }
          }
          // add a line in the CATALOG_ITEMS table
          {
            String catType = catalogItemEntry.getKey().getCode();
            String sql = "tx.executeSql('INSERT INTO CATALOG_ITEMS (KW_ID, BK_ID, CAT_TYPE) VALUES (?, ?, ?)', ['" + kwId + "', '" + bkId + "', '" + catType + "']);";
            sqlCatalogItems.add(sql);
          }
        }
      }
    }

    // output the SQL
    File outputFile = new File(exportFolder, "database.js");
    FileOutputStream fos = null;
    PrintWriter pw = null;
    try {
      fos = new FileOutputStream(outputFile);
      pw = new PrintWriter(fos);
      pw.println("// inserting Books");
      pw.println("console.log('inserting Books ');");
      Collections.sort(sqlBooks);
      for (String sql : sqlBooks) {
        pw.println(sql);
      }
      pw.println("// end of Books");
      pw.println("console.log('finished inserting Books ');");

      pw.println("// inserting Keywords");
      pw.println("console.log('inserting Keywords ');");
      Collections.sort(sqlKeywords);
      for (String sql : sqlKeywords) {
        pw.println(sql);
      }
      pw.println("// end of Keywords");
      pw.println("console.log('finished inserting Keywords ');");

      pw.println("// inserting CatalogItems");
      pw.println("console.log('inserting CatalogItems ');");
      Collections.sort(sqlCatalogItems);
      for (String sql : sqlCatalogItems) {
        pw.println(sql);
      }
      pw.println("// end of CatalogItems");
      pw.println("console.log('finished inserting CatalogItems ');");
    } finally {
      if (pw != null)
        pw.close();
    }
  }


  private void writeJsonArray(String name, List<String> strings, PrintWriter pw) {
    pw.print("[");
    Iterator<String> iterator = strings.iterator();
    while (iterator.hasNext()) {
      String string = iterator.next();
      pw.print("'" + string + "'");
      if (iterator.hasNext())
        pw.print(",");
    }
    pw.print("]");
  }

  private void writeJson(File exportFolder, String name, List<String> jsonData, List<String> jsonKeys) throws IOException {
    File outputFile = new File(exportFolder, name + ".json");
    FileOutputStream fos = null;
    PrintWriter pw = null;
    try {
      fos = new FileOutputStream(outputFile);
      pw = new PrintWriter(fos);
      pw.println("{");
      writeJsonArray(name, jsonData, pw);
      pw.println(",");
      writeJsonArray("keys", jsonKeys, pw);
      pw.println("}");
    } finally {
      if (pw != null)
        pw.close();
    }

  }

  public void exportToJSON(File exportFolder) throws IOException {
    List<String> jsonKeywords = new ArrayList<String>(mapOfKeywords.size());
    List<String> jsonBooks = new ArrayList<String>(mapOfKeywords.size());
    List<String> jsonCatalogItems = new ArrayList<String>(mapOfKeywords.size());
    // clear the flag of all books
    for (Book book : DataModel.INSTANCE.getListOfBooks()) {
      book.clearFlag();
    }
    for (Keyword keyword : mapOfKeywords.values()) {
      String kwId = Long.toString(keyword.id);
      {
        // add a line in the KEYWORDS table
        String kwWord = keyword.word; // no need to search for apostrophes, the keywords are already cleaned-up and uppercase
        String kwWeight = "" + keyword.catalogItems.size();
        jsonKeywords.add(kwId);
        jsonKeywords.add(kwWord);
        jsonKeywords.add(kwWeight);
      }
      for (Map.Entry<ItemType, CatalogItem> catalogItemEntry : keyword.catalogItems.entrySet()) {
        for (BookEntry bookEntry : catalogItemEntry.getValue().bookEntries) {
          String bkId = bookEntry.book.getId();
          int bookId = Integer.parseInt(bkId);
          if (!bookEntry.book.isFlagged()) {
            bookEntry.book.setFlag();
            {
              // add a line in the BOOKS table
              String bkTitle = parseForApostrophes(bookEntry.book.getTitle());
              String bkUrl = bookEntry.url;
              String bkCoverUrl = bookEntry.coverUrl;
              jsonBooks.add(bkId);
              jsonBooks.add(bkTitle);
              jsonBooks.add(bkUrl);
              jsonBooks.add(bkCoverUrl);
            }
          }
          // add a line in the CATALOG_ITEMS table
          {
            String catType = catalogItemEntry.getKey().getCode();
            jsonCatalogItems.add(kwId);
            jsonCatalogItems.add(bkId);
            jsonCatalogItems.add(catType);
          }
        }
      }
    }

    writeJson(exportFolder, "books", jsonBooks, new ArrayList<String>() {{
      add("bkId");
      add("bkTitle");
      add("bkUrl");
      add("bkCoverUrl");
    }});
    writeJson(exportFolder, "keywords", jsonKeywords, new ArrayList<String>() {{
      add("kwId");
      add("kwWord");
      add("kwWeight");
    }});
    writeJson(exportFolder, "catalogitems", jsonCatalogItems, new ArrayList<String>() {{
      add("kwId");
      add("bkId");
      add("catType");
    }});
  }

  private void writeJavascript(File exportFolder, String name, List<String[]> data, List<String> keys) throws IOException {
    File outputFile = new File(exportFolder, name + ".js");
    FileOutputStream fos = null;
    PrintWriter pw = null;
    try {
      fos = new FileOutputStream(outputFile);
      pw = new PrintWriter(fos);
      {
        pw.println("function get" + Helper.toTitleCase(name) + " () {");
        pw.print("  // ");
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
          String string = iterator.next();
          pw.print(string);
          if (iterator.hasNext())
            pw.print(", ");
        }
        pw.println();
        pw.println("  // " + data.size() + " elements");
        pw.print("  return [");
        Iterator<String[]> arrayIterator = data.iterator();
        while (arrayIterator.hasNext()) {
          String[] stringArray = arrayIterator.next();
          pw.print("[");
          for (int i = 0; i < stringArray.length; i++) {
            String string = stringArray[i];
            pw.print("'" + string + "'");
            if (i+1 < stringArray.length)
              pw.print(",");
          }
          pw.print("]");
          if (arrayIterator.hasNext())
            pw.print(",");
        }
        pw.println("  ];");
        pw.println("}");
      }
    } finally {
      if (pw != null)
        pw.close();
    }

  }

  public void exportToJavascriptArrays(File exportFolder) throws IOException {
    List<String[]> jsKeywords = new ArrayList<String[]>(mapOfKeywords.size());
    List<String[]> jsBooks = new ArrayList<String[]>(mapOfKeywords.size());
    List<String[]> jsCatalogItems = new ArrayList<String[]>(mapOfKeywords.size());
    // clear the flag of all books
    for (Book book : DataModel.INSTANCE.getListOfBooks()) {
      book.clearFlag();
    }
    for (Keyword keyword : mapOfKeywords.values()) {
      String kwId = Long.toString(keyword.id);
      {
        // add a line in the KEYWORDS table
        String kwWord = keyword.word; // no need to search for apostrophes, the keywords are already cleaned-up and uppercase
        String kwWeight = "" + keyword.size();
        jsKeywords.add(new String[] {kwId, kwWord, kwWeight});
      }
      for (Map.Entry<ItemType, CatalogItem> catalogItemEntry : keyword.catalogItems.entrySet()) {
        for (BookEntry bookEntry : catalogItemEntry.getValue().bookEntries) {
          String bkId = bookEntry.book.getId();
          int bookId = Integer.parseInt(bkId);
          if (!bookEntry.book.isFlagged()) {
            bookEntry.book.setFlag();
            {
              // add a line in the BOOKS table
              String bkTitle = parseForApostrophes(bookEntry.book.getTitle());
              String bkUrl = bookEntry.url;
              String bkCoverUrl = bookEntry.coverUrl;
              jsBooks.add(new String[] {bkId, bkTitle, bkUrl, bkCoverUrl});
            }
          }
          // add a line in the CATALOG_ITEMS table
          {
            String catType = catalogItemEntry.getKey().getCode();
            jsCatalogItems.add(new String[] {kwId, bkId, catType});
          }
        }
      }
    }

    writeJavascript(exportFolder, "books", jsBooks, new ArrayList<String>() {{
      add("bkId");
      add("bkTitle");
      add("bkUrl");
      add("bkCoverUrl");
    }});
    writeJavascript(exportFolder, "keywords", jsKeywords, new ArrayList<String>() {{
      add("kwId");
      add("kwWord");
      add("kwWeight");
    }});
    writeJavascript(exportFolder, "catalogitems", jsCatalogItems, new ArrayList<String>() {{
      add("kwId");
      add("bkId");
      add("catType");
    }});
  }
}
