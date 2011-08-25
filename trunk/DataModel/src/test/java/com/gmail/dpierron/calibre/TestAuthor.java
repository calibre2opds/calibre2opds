package com.gmail.dpierron.calibre;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gmail.dpierron.calibre.datamodel.Author;

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

