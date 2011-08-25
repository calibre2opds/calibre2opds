package com.gmail.dpierron.calibre.datamodel.test;

import com.gmail.dpierron.calibre.datamodel.*;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.List;
import java.util.Vector;


public class TestDataModel {
  private static final Logger logger = Logger.getLogger(TestDataModel.class);

  private void skipLine() {
    logger.info("\r\n");
  }

  @Test
  public void testDataModel() {
    testDataModel(true);
  }
  
  public void testDataModel(boolean checkFiles) {
    skipLine();
    logger.info("loading books");
    List<Book> books = DataModel.INSTANCE.getListOfBooks();
    logger.info("Found "+books.size()+" book(s)");
    skipLine();
  
    if (checkFiles) {
      skipLine();
      logger.info("checking books ePub files");
      List<Book> withEpub = new Vector<Book>();
      List<Book> withoutEpub = new Vector<Book>();
      for (Book book : books) {
        if (book.doesEpubFileExist()) 
          withEpub.add(book);
        else
          withoutEpub.add(book);
      }
      skipLine();
  
      skipLine();
      logger.info("list of the "+withEpub.size()+" book(s) with a valid ePub file : ");
      for (Book book : withEpub) {
        logger.info("book:"+book.toDetailedString());
        logger.info("epub:"+book.getEpubFilename());
      }
      skipLine();
  
      skipLine();
      logger.info("list of the "+withoutEpub.size()+" book(s) without a valid ePub file : ");
      List<Book> withWrongEpub = new Vector<Book>();
      for (Book book : withoutEpub) {
        logger.info("book:"+book.toDetailedString());
        logger.info("epub:"+book.getEpubFilename());
        if (book.getBookFolder().exists()) {
          String[] files = book.getBookFolder().list();
          for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            logger.info("file:"+filename);
            if (filename.toUpperCase().endsWith(".EPUB"))
              withWrongEpub.add(book);
          }
          
        } else {
          logger.info("file: Book folder does not even exist! "+book.getBookFolder().getAbsolutePath());
        }
      }
      skipLine();
      
      skipLine();
      logger.info("list of the "+withWrongEpub.size()+" book(s) with an invalid ePub file : ");
      for (Book book : withWrongEpub) {
        logger.info("book:"+book.toDetailedString());
        logger.info("epub:"+book.getEpubFilename());
        if (book.getBookFolder().exists()) {
          String[] files = book.getBookFolder().list();
          for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            if (filename.toUpperCase().endsWith(".EPUB"))
              logger.info("file:"+filename);
          }
          
        } else {
          logger.info("file: Book folder does not even exist! "+book.getBookFolder().getAbsolutePath());
        }
      }
      skipLine();
    }
    
    skipLine();
    logger.info("loading tags");
    List<Tag> list = DataModel.INSTANCE.getListOfTags(null); 
    logger.info("there are "+list.size()+" tag(s)");
    skipLine();

    skipLine();
    logger.info("loading books by tag");
    for (Tag key : DataModel.INSTANCE.getMapOfBooksByTag().keySet()) {
      books = DataModel.INSTANCE.getMapOfBooksByTag().get(key);
      logger.info(key + " has "+books.size()+" book(s)");
    }
    skipLine();
    
    skipLine();
    logger.info("loading books by author");
    for (Author key : DataModel.INSTANCE.getMapOfBooksByAuthor().keySet()) {
      books = DataModel.INSTANCE.getMapOfBooksByAuthor().get(key);
      logger.info(key + " has "+books.size()+" book(s)");
    }
    skipLine();

    skipLine();
    logger.info("loading books by serie");
    for (Series key : DataModel.INSTANCE.getMapOfBooksBySeries().keySet()) {
      books = DataModel.INSTANCE.getMapOfBooksBySeries().get(key);
      logger.info(key + " has "+books.size()+" book(s)");
    }
    skipLine();
    
  }

}
