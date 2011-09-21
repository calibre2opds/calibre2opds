package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.datamodel.Author;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestAuthor {

  @Test
  public void test() {
    Author author = new Author("0", "David Pierron", "");
    assertTrue("David Pierron".equals(author.getName()));
    author = new Author("0", "David Pierron|Ed.", "");
    assertTrue("David Pierron,Ed.".equals(author.getName()));
    author = new Author("0", "F. Paul Wilson", "");
    assertTrue("Wilson, F. Paul".equals(author.getSort()));
    author = new Author("0", "Platon", "");
    assertTrue("Platon".equals(author.getSort()));
    assertTrue("Platon".equals(author.getSort()));
  }

}

