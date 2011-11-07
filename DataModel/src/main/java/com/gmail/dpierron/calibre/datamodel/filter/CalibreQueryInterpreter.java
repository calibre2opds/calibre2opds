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
  private static final String WORD_LANGUAGE = "languages:";
  private static final String WORD_RATING = "rating:";

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
      logger.trace("template="+template);
    if (template.toLowerCase().startsWith(WORD_TAG)) {
      boolean isEqualsPresent = template.substring(WORD_TAG.length() + 1, WORD_TAG.length() + 2).equals("=");
      String tag = template.substring(WORD_TAG.length() + (isEqualsPresent ? 2 : 1), template.length() - 1); // skip the "= and the "
      if (logger.isDebugEnabled())
        logger.debug("found tag filter: " + tag);
      return new TagFilter(tag, !isEqualsPresent);
    } else if (template.toLowerCase().startsWith(WORD_LANGUAGE)) {
      String lang = template.substring(WORD_LANGUAGE.length() + 2, template.length() - 1); // skip the "= and the "
      if (logger.isDebugEnabled())
        logger.debug("found language filter: " + lang);
      return new LanguageFilter(lang);
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
      if (logger.isDebugEnabled())
        logger.debug("found rating filter: " + comparator + " " + rating);
      return new RatingFilter(comparator, rating);
    } else if (template.equalsIgnoreCase(WORD_NOT)) {
      // descend into the parsing with a negation filter
      if (logger.isDebugEnabled())
        logger.debug("found not filter!");
      return new NotFilter(getFilterForNode(node.getChild(0)));
    } else if (template.equalsIgnoreCase(WORD_OR)) {
      // descend into the parsing with a boolean OR filter
      if (logger.isDebugEnabled())
        logger.debug("found OR filter!");
      return new BooleanOrFilter(getFilterForNode(node.getChild(0)), getFilterForNode(node.getChild(1)));
    } else if (template.equalsIgnoreCase(WORD_AND)) {
      // descend into the parsing with a boolean AND filter
      if (logger.isDebugEnabled())
        logger.debug("found AND filter!");
      return new BooleanAndFilter(getFilterForNode(node.getChild(0)), getFilterForNode(node.getChild(1)));
    } else if (logger.isDebugEnabled())
      logger.debug("found unsupported filter! " + template);
    return new PassthroughFilter();
  }

  private String optionallyRemoveQuotes(String template, String word) {
    char quote = template.charAt(word.length());
    if (quote == '"') {
      return template.substring(word.length()+1, template.length()-1);
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

  public static BookFilter interpret(String calibreSearchQueryOrName) throws CalibreSavedSearchInterpretException, CalibreSavedSearchNotFoundException {
    if (logger.isDebugEnabled())
      logger.debug("CalibreQueryInterpreter.interpret:"+calibreSearchQueryOrName);
    BookFilter filter = null;
    String calibreQuery = calibreSearchQueryOrName;
    if (Helper.isNotNullOrEmpty(calibreSearchQueryOrName)) {
      String calibreSavedSearchName = null;
      if (calibreSearchQueryOrName.toUpperCase(Locale.ENGLISH).startsWith("SAVED:")) {
        calibreSavedSearchName = calibreSearchQueryOrName.substring(6);
        if (logger.isDebugEnabled())
          logger.debug("searching for saved search "+calibreSavedSearchName);
        calibreQuery = DataModel.INSTANCE.getMapOfSavedSearches().get(calibreSavedSearchName);
        if (Helper.isNullOrEmpty(calibreQuery))
          calibreQuery = DataModel.INSTANCE.getMapOfSavedSearches().get(calibreSavedSearchName.toUpperCase());
      }
      if (Helper.isNullOrEmpty(calibreQuery))
        throw new CalibreSavedSearchNotFoundException(calibreSavedSearchName);
      else {
        if (logger.isDebugEnabled())
          logger.debug("interpreting "+calibreQuery);

        CalibreQueryInterpreter interpreter = new CalibreQueryInterpreter(calibreQuery);
        filter = interpreter.interpret();
      }
    }
    return filter;
  }

}
