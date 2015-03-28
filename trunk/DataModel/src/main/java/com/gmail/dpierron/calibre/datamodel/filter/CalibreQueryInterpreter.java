package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.calibrequerylanguage.CalibreQueryLexer;
import com.gmail.dpierron.calibre.datamodel.calibrequerylanguage.CalibreQueryParser;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchNotFoundException;
import com.gmail.dpierron.tools.Helper;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

import java.util.Locale;

public class CalibreQueryInterpreter {
  private final static Logger logger = Logger.getLogger(CalibreQueryInterpreter.class);

  // recognized lexical elements
  private static final String WORD_TAG = "tags:";
  private static final String WORD_TAG_TRUE = "tags:true";
  private static final String WORD_TAG_FALSE = "tags:false";

  private static final String WORD_LANGUAGE = "languages:";
  private static final String WORD_LANGUAGE_TRUE = "languages:true";
  private static final String WORD_LANGUAGE_FALSE = "languages:false";

  private static final String WORD_RATING = "rating:";
  private static final String WORD_RATING_TRUE = "rating:true";
  private static final String WORD_RATING_FALSE = "rating:false";

  private static final String WORD_AUTHOR = "authors:";
  private static final String WORD_AUTHOR_TRUE = "authors:true";
  private static final String WORD_AUTHOR_FALSE = "authors:false";

  private static final String WORD_SERIES = "series:";
  private static final String WORD_SERIES_TRUE = "series:true";
  private static final String WORD_SERIES_FALSE = "series:false";

  private static final String WORD_FORMAT = "formats:";
  private static final String WORD_FORMAT_TRUE = "formats:true";
  private static final String WORD_FORMAT_FALSE = "formats:false";

  private static final String WORD_PUBLISHER = "publisher:"; // there is no 's', it's correct
  private static final String WORD_PUBLISHER_TRUE = "publisher:true";
  private static final String WORD_PUBLISHER_FALSE = "publisher:false";

  // unary negation operator
  private static final String WORD_NOT = "not";

  // binary boolean operators
  private static final String WORD_AND = "and";
  private static final String WORD_OR = "or";

  private final String calibreQuery;

  public CalibreQueryInterpreter(String calibreQuery) {
    this.calibreQuery = calibreQuery;
  }

  private BookFilter getFilterForNode(Tree node) throws CalibreSavedSearchInterpretException {
    if (node instanceof CommonErrorNode) {
      // an error occured during parsing
      throw new CalibreSavedSearchInterpretException(node.toString(), ((CommonErrorNode) node).trappedException);
    }
    String template = node.getText();
    if (logger.isTraceEnabled())
      logger.trace("template=" + template);

    /* Tags */
    if (template.toLowerCase().equals(WORD_TAG_TRUE)) {
      if (logger.isTraceEnabled()) logger.trace("found tag present filter");
      return new TagPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_TAG_FALSE)) {
      if (logger.isTraceEnabled()) logger.trace("found tag not present filter");
      return new TagPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_TAG)) {
      boolean isEqualsPresent = template.substring(WORD_TAG.length() + 1, WORD_TAG.length() + 2).equals("=");
      String tag = template.substring(WORD_TAG.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled()) logger.trace("found tag filter: " + tag);
      return new TagFilter(tag, !isEqualsPresent);
    }

    /* Languages */
    else if (template.toLowerCase().equals(WORD_LANGUAGE_TRUE)) {
      if (logger.isTraceEnabled()) logger.trace("found language present filter");
      return new LanguagePresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_LANGUAGE_FALSE)) {
      if (logger.isTraceEnabled())  logger.trace("found language not present filter");
      return new LanguagePresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_LANGUAGE)) {
      String lang = template.substring(WORD_LANGUAGE.length() + 2, template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled()) logger.trace("found language filter: " + lang);
      return new LanguageFilter(lang);
    }

    /* Ratings */
    else if (template.toLowerCase().equals(WORD_RATING_TRUE)) {
      if (logger.isTraceEnabled())  logger.trace("found rating present filter");
      return new RatingPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_RATING_FALSE)) {
      if (logger.isTraceEnabled()) logger.trace("found rating not present filter");
      return new RatingPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_RATING)) {
      /* optionally remove the quotes */
      String parameter = optionallyRemoveQuotes(template, WORD_RATING);
      char comparator = parameter.charAt(0);
      char rating;
      if (comparator == '<' || comparator == '>' || comparator == '=')
        rating = parameter.charAt(1);
      else {
        rating = parameter.charAt(0);
        comparator = '=';
      }
      if (logger.isTraceEnabled())  logger.trace("found rating filter: " + comparator + " " + rating);
      return new RatingFilter(comparator, rating);
    }

    /* Authors */
    if (template.toLowerCase().equals(WORD_AUTHOR_TRUE)) {
      if (logger.isTraceEnabled()) logger.trace("found author present filter");
      return new AuthorPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_AUTHOR_FALSE)) {
      if (logger.isTraceEnabled()) logger.trace("found author not present filter");
      return new AuthorPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_AUTHOR)) {
      boolean isEqualsPresent = template.substring(WORD_AUTHOR.length() + 1, WORD_AUTHOR.length() + 2).equals("=");
      String tag = template.substring(WORD_AUTHOR.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled()) logger.trace("found author filter: " + tag);
      return new AuthorFilter(tag, !isEqualsPresent);
    }

    /* Series */
    if (template.toLowerCase().equals(WORD_SERIES_TRUE)) {
      if (logger.isTraceEnabled()) logger.trace("found series present filter");
      return new SeriesPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_SERIES_FALSE)) {
      if (logger.isTraceEnabled())  logger.trace("found series not present filter");
      return new SeriesPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_SERIES)) {
      boolean isEqualsPresent = template.substring(WORD_SERIES.length() + 1, WORD_SERIES.length() + 2).equals("=");
      String tag = template.substring(WORD_SERIES.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled())  logger.trace("found series filter: " + tag);
      return new SeriesFilter(tag, !isEqualsPresent);
    }

    /* Formats */
    if (template.toLowerCase().equals(WORD_FORMAT_TRUE)) {
      if (logger.isTraceEnabled())  logger.trace("found format present filter");
      return new FormatPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_FORMAT_FALSE)) {
      if (logger.isTraceEnabled()) logger.trace("found format not present filter");
      return new FormatPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_FORMAT)) {
      boolean isEqualsPresent = template.substring(WORD_FORMAT.length() + 1, WORD_FORMAT.length() + 2).equals("=");
      String tag = template.substring(WORD_FORMAT.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled()) logger.trace("found format filter: " + tag);
      return new FormatFilter(tag, !isEqualsPresent);
    }

    /* Publisher */
    if (template.toLowerCase().equals(WORD_PUBLISHER_TRUE)) {
      if (logger.isTraceEnabled()) logger.trace("found publisher present filter");
      return new PublisherPresenceFilter(true);
    } else if (template.toLowerCase().equals(WORD_PUBLISHER_FALSE)) {
      if (logger.isTraceEnabled()) logger.trace("found publisher not present filter");
      return new PublisherPresenceFilter(false);
    } else if (template.toLowerCase().startsWith(WORD_PUBLISHER)) {
      boolean isEqualsPresent = template.substring(WORD_PUBLISHER.length() + 1, WORD_PUBLISHER.length() + 2).equals("=");
      String tag = template.substring(WORD_PUBLISHER.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isTraceEnabled()) logger.trace("found publisher filter: " + tag);
      return new PublisherFilter(tag, !isEqualsPresent);
    }

    /* Boolean operators */
    else if (template.equalsIgnoreCase(WORD_NOT)) {
      // descend into the parsing with a negation filter
      if (logger.isTraceEnabled()) logger.trace("found not filter!");
      return new NotFilter(getFilterForNode(node.getChild(0)));
    } else if (template.equalsIgnoreCase(WORD_OR)) {
      // descend into the parsing with a boolean OR filter
      if (logger.isTraceEnabled()) logger.trace("found OR filter!");
      return new BooleanOrFilter(getFilterForNode(node.getChild(0)), getFilterForNode(node.getChild(1)));
    } else if (template.equalsIgnoreCase(WORD_AND)) {
      // descend into the parsing with a boolean AND filter
      if (logger.isTraceEnabled()) logger.trace("found AND filter!");
      return new BooleanAndFilter(getFilterForNode(node.getChild(0)), getFilterForNode(node.getChild(1)));
    }

    /* Error ! */
    else {
      logger.warn("found unsupported filter! " + template);
      return new PassthroughFilter();
    }
  }

  private String optionallyRemoveQuotes(String template, String word) {
    char quote = template.charAt(word.length());
    if (quote == '"') {
      return template.substring(word.length() + 1, template.length() - 1);
    } else {
      return template.substring(word.length());
    }
  }

  /**
   * @return a filter, possibly composite, built as a parse tree of the calibreQuery string
   * @throws com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException
   *          when something failed
   */
  public BookFilter interpret() throws CalibreSavedSearchInterpretException {
    CharStream cs = new ANTLRStringStream(calibreQuery);
    CalibreQueryLexer lexer = new CalibreQueryLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    CalibreQueryParser parser = new CalibreQueryParser(tokens);
    CalibreQueryParser.expr_return result;
    try {
      result = parser.expr();
      return getFilterForNode((Tree) result.getTree());
    } catch (RecognitionException e) {
      throw new CalibreSavedSearchInterpretException(calibreQuery, e);
    } catch (CalibreSavedSearchInterpretException e) {
      throw new CalibreSavedSearchInterpretException(calibreQuery, e);
    }
  }

  /**
   *
   * @param calibreSearchQueryOrName
   * @return
   * @throws CalibreSavedSearchInterpretException
   * @throws CalibreSavedSearchNotFoundException
   */
  public static BookFilter interpret(String calibreSearchQueryOrName) throws CalibreSavedSearchInterpretException, CalibreSavedSearchNotFoundException {
    if (logger.isTraceEnabled())
      logger.trace("CalibreQueryInterpreter.interpret:" + calibreSearchQueryOrName);
    BookFilter filter = null;
    String calibreQuery = calibreSearchQueryOrName;
    if (Helper.isNotNullOrEmpty(calibreSearchQueryOrName)) {
      String calibreSavedSearchName = null;
      if (calibreSearchQueryOrName.toUpperCase(Locale.ENGLISH).startsWith("SAVED:")) {
        calibreSavedSearchName = calibreSearchQueryOrName.substring(6);
        if (logger.isTraceEnabled())
          logger.trace("searching for saved search " + calibreSavedSearchName);
        calibreQuery = DataModel.getMapOfSavedSearches().get(calibreSavedSearchName);
        if (Helper.isNullOrEmpty(calibreQuery))
          calibreQuery = DataModel.getMapOfSavedSearches().get(calibreSavedSearchName.toUpperCase());
        if (Helper.isNullOrEmpty(calibreQuery))
          throw new CalibreSavedSearchNotFoundException(calibreSavedSearchName);
      } else {
        if (logger.isDebugEnabled())
          logger.trace("interpreting " + calibreQuery);

        CalibreQueryInterpreter interpreter = new CalibreQueryInterpreter(calibreQuery);
        filter = interpreter.interpret();
      }
    }
    return filter;
  }

}
