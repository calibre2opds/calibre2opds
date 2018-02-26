package com.gmail.dpierron.calibre;

import com.gmail.dpierron.calibre.datamodel.Author;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestAuthor {

  @Test
  public void test() {
    Author author = new Author("0", "David Pierron", "");
    assertTrue("David Pierron".equals(author.getDisplayName()));
    author = new Author("0", "David Pierron|Ed.", "");
    assertTrue("David Pierron,Ed.".equals(author.getDisplayName()));
    author = new Author("0", "F. Paul Wilson", "");
    assertTrue("Wilson, F. Paul".equals(author.getSortName()));
    author = new Author("0", "Platon", "");
    assertTrue("Platon".equals(author.getSortName()));
    assertTrue("Platon".equals(author.getSortName()));
  }

}

