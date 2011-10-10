package com.gmail.dpierron.calibre.datamodel.filter;

import com.gmail.dpierron.calibre.datamodel.calibrequerylanguage.CalibreQueryLexer;
import com.gmail.dpierron.calibre.datamodel.calibrequerylanguage.CalibreQueryParser;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

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

  private BookFilter getFilterForNode(Tree node) throws InterpretException {
    if (node instanceof CommonErrorNode) {
      // an error occured during parsing
      throw new InterpretException(node.toString(), ((CommonErrorNode) node).trappedException);
    }
    String template = node.getText();
    if (template.toLowerCase().startsWith(WORD_TAG)) {
      String tag = template.substring(WORD_TAG.length() + 2, template.length() - 1); // skip the "= and the "
      if (logger.isDebugEnabled())
        logger.debug("found tag filter: "+tag);
      return new TagFilter(tag);
    } else if (template.toLowerCase().startsWith(WORD_LANGUAGE)) {
      String lang = template.substring(WORD_LANGUAGE.length() + 2, template.length() - 1); // skip the "= and the "
      if (logger.isDebugEnabled())
        logger.debug("found language filter: "+lang);
      return new LanguageFilter(lang);
    } else if (template.toLowerCase().startsWith(WORD_RATING)) {
      char comparator = template.charAt(WORD_RATING.length() + 1);
      char rating = template.charAt(WORD_RATING.length() + 2);
      if (logger.isDebugEnabled())
        logger.debug("found rating filter: "+comparator+" "+rating);
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
    } else
    if (logger.isDebugEnabled())
      logger.debug("found unsupported filter! "+template);
      return new PassthroughFilter();
  }

  /**
   * @return a filter, possibly composite, built as a parse tree of the calibreQuery string
   * @throws com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter.InterpretException when something failed
   */
  public BookFilter interpret() throws InterpretException {
    CharStream cs = new ANTLRStringStream(calibreQuery);
    CalibreQueryLexer lexer = new CalibreQueryLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    CalibreQueryParser parser = new CalibreQueryParser(tokens);
    CalibreQueryParser.expr_return result;
    try {
      result = parser.expr();
    } catch (RecognitionException e) {
      throw new InterpretException(e);
    }
    return getFilterForNode((Tree) result.getTree());
  }

  public class InterpretException extends Exception {
    InterpretException(String message, Throwable cause) {
      super(message, cause);
    }

    InterpretException(Throwable cause) {
      super(cause);
    }
  }
}
