// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g 2011-10-01 22:08:36


package com.gmail.dpierron.calibre.datamodel.calibrequerylanguage;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

public class CalibreQueryParser extends AbstractTParser {
  public static final String[] tokenNames =
      new String[]{"<invalid>", "<EOR>", "<DOWN>", "<UP>", "LEFT_PAREN", "RIGHT_PAREN", "AND", "OR", "NOT", "TAG", "LANG", "RATING", "WHITESPACE"};
  public static final int RIGHT_PAREN = 5;
  public static final int OR = 7;
  public static final int LEFT_PAREN = 4;
  public static final int WHITESPACE = 12;
  public static final int NOT = 8;
  public static final int AND = 6;
  public static final int EOF = -1;
  public static final int RATING = 11;
  public static final int TAG = 9;
  public static final int LANG = 10;

  // delegates
  // delegators


  public CalibreQueryParser(TokenStream input) {
    this(input, new RecognizerSharedState());
  }

  public CalibreQueryParser(TokenStream input, RecognizerSharedState state) {
    super(input, state);

  }

  protected TreeAdaptor adaptor = new CommonTreeAdaptor();

  public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
  }

  public TreeAdaptor getTreeAdaptor() {
    return adaptor;
  }

  public String[] getTokenNames() { return CalibreQueryParser.tokenNames; }

  public String getGrammarFileName() { return "com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g"; }


  public static class expr_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "expr"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:51:1: expr : orexpression ;
  public final CalibreQueryParser.expr_return expr() throws RecognitionException {
    CalibreQueryParser.expr_return retval = new CalibreQueryParser.expr_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    CalibreQueryParser.orexpression_return orexpression1 = null;


    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:51:6: ( orexpression )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:51:8: orexpression
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_orexpression_in_expr192);
        orexpression1 = orexpression();

        state._fsp--;

        adaptor.addChild(root_0, orexpression1.getTree());

      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "expr"

  public static class orexpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "orexpression"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:53:1: orexpression : andexpression ( OR andexpression )* ;
  public final CalibreQueryParser.orexpression_return orexpression() throws RecognitionException {
    CalibreQueryParser.orexpression_return retval = new CalibreQueryParser.orexpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token OR3 = null;
    CalibreQueryParser.andexpression_return andexpression2 = null;

    CalibreQueryParser.andexpression_return andexpression4 = null;


    Object OR3_tree = null;

    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:54:5: ( andexpression ( OR andexpression )* )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:54:9: andexpression ( OR andexpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_andexpression_in_orexpression206);
        andexpression2 = andexpression();

        state._fsp--;

        adaptor.addChild(root_0, andexpression2.getTree());
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:54:23: ( OR andexpression )*
        loop1:
        do {
          int alt1 = 2;
          int LA1_0 = input.LA(1);

          if ((LA1_0 == OR)) {
            alt1 = 1;
          }


          switch (alt1) {
            case 1:
              // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:54:24: OR andexpression
            {
              OR3 = (Token) match(input, OR, FOLLOW_OR_in_orexpression209);
              OR3_tree = (Object) adaptor.create(OR3);
              root_0 = (Object) adaptor.becomeRoot(OR3_tree, root_0);

              pushFollow(FOLLOW_andexpression_in_orexpression212);
              andexpression4 = andexpression();

              state._fsp--;

              adaptor.addChild(root_0, andexpression4.getTree());

            }
            break;

            default:
              break loop1;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "orexpression"

  public static class andexpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "andexpression"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:57:1: andexpression : notexpression ( AND notexpression )* ;
  public final CalibreQueryParser.andexpression_return andexpression() throws RecognitionException {
    CalibreQueryParser.andexpression_return retval = new CalibreQueryParser.andexpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token AND6 = null;
    CalibreQueryParser.notexpression_return notexpression5 = null;

    CalibreQueryParser.notexpression_return notexpression7 = null;


    Object AND6_tree = null;

    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:58:5: ( notexpression ( AND notexpression )* )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:58:7: notexpression ( AND notexpression )*
      {
        root_0 = (Object) adaptor.nil();

        pushFollow(FOLLOW_notexpression_in_andexpression235);
        notexpression5 = notexpression();

        state._fsp--;

        adaptor.addChild(root_0, notexpression5.getTree());
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:58:21: ( AND notexpression )*
        loop2:
        do {
          int alt2 = 2;
          int LA2_0 = input.LA(1);

          if ((LA2_0 == AND)) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
              // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:58:22: AND notexpression
            {
              AND6 = (Token) match(input, AND, FOLLOW_AND_in_andexpression238);
              AND6_tree = (Object) adaptor.create(AND6);
              root_0 = (Object) adaptor.becomeRoot(AND6_tree, root_0);

              pushFollow(FOLLOW_notexpression_in_andexpression241);
              notexpression7 = notexpression();

              state._fsp--;

              adaptor.addChild(root_0, notexpression7.getTree());

            }
            break;

            default:
              break loop2;
          }
        } while (true);


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "andexpression"

  public static class notexpression_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "notexpression"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:61:1: notexpression : ( NOT )? atom ;
  public final CalibreQueryParser.notexpression_return notexpression() throws RecognitionException {
    CalibreQueryParser.notexpression_return retval = new CalibreQueryParser.notexpression_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token NOT8 = null;
    CalibreQueryParser.atom_return atom9 = null;


    Object NOT8_tree = null;

    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:62:5: ( ( NOT )? atom )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:62:7: ( NOT )? atom
      {
        root_0 = (Object) adaptor.nil();

        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:62:7: ( NOT )?
        int alt3 = 2;
        int LA3_0 = input.LA(1);

        if ((LA3_0 == NOT)) {
          alt3 = 1;
        }
        switch (alt3) {
          case 1:
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:62:8: NOT
          {
            NOT8 = (Token) match(input, NOT, FOLLOW_NOT_in_notexpression261);
            NOT8_tree = (Object) adaptor.create(NOT8);
            root_0 = (Object) adaptor.becomeRoot(NOT8_tree, root_0);


          }
          break;

        }

        pushFollow(FOLLOW_atom_in_notexpression266);
        atom9 = atom();

        state._fsp--;

        adaptor.addChild(root_0, atom9.getTree());

      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "notexpression"

  public static class atom_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "atom"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:65:1: atom : ( condition | LEFT_PAREN orexpression RIGHT_PAREN );
  public final CalibreQueryParser.atom_return atom() throws RecognitionException {
    CalibreQueryParser.atom_return retval = new CalibreQueryParser.atom_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token LEFT_PAREN11 = null;
    Token RIGHT_PAREN13 = null;
    CalibreQueryParser.condition_return condition10 = null;

    CalibreQueryParser.orexpression_return orexpression12 = null;


    Object LEFT_PAREN11_tree = null;
    Object RIGHT_PAREN13_tree = null;

    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:66:5: ( condition | LEFT_PAREN orexpression RIGHT_PAREN )
      int alt4 = 2;
      int LA4_0 = input.LA(1);

      if (((LA4_0 >= TAG && LA4_0 <= RATING))) {
        alt4 = 1;
      } else if ((LA4_0 == LEFT_PAREN)) {
        alt4 = 2;
      } else {
        NoViableAltException nvae = new NoViableAltException("", 4, 0, input);

        throw nvae;
      }
      switch (alt4) {
        case 1:
          // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:66:7: condition
        {
          root_0 = (Object) adaptor.nil();

          pushFollow(FOLLOW_condition_in_atom283);
          condition10 = condition();

          state._fsp--;

          adaptor.addChild(root_0, condition10.getTree());

        }
        break;
        case 2:
          // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:67:7: LEFT_PAREN orexpression RIGHT_PAREN
        {
          root_0 = (Object) adaptor.nil();

          LEFT_PAREN11 = (Token) match(input, LEFT_PAREN, FOLLOW_LEFT_PAREN_in_atom291);
          pushFollow(FOLLOW_orexpression_in_atom294);
          orexpression12 = orexpression();

          state._fsp--;

          adaptor.addChild(root_0, orexpression12.getTree());
          RIGHT_PAREN13 = (Token) match(input, RIGHT_PAREN, FOLLOW_RIGHT_PAREN_in_atom296);

        }
        break;

      }
      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "atom"

  public static class condition_return extends ParserRuleReturnScope {
    Object tree;

    public Object getTree() { return tree; }
  }

  ;

  // $ANTLR start "condition"
  // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:70:1: condition : ( TAG | LANG | RATING );
  public final CalibreQueryParser.condition_return condition() throws RecognitionException {
    CalibreQueryParser.condition_return retval = new CalibreQueryParser.condition_return();
    retval.start = input.LT(1);

    Object root_0 = null;

    Token set14 = null;

    Object set14_tree = null;

    try {
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:71:5: ( TAG | LANG | RATING )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryParser.g:
      {
        root_0 = (Object) adaptor.nil();

        set14 = (Token) input.LT(1);
        if ((input.LA(1) >= TAG && input.LA(1) <= RATING)) {
          input.consume();
          adaptor.addChild(root_0, (Object) adaptor.create(set14));
          state.errorRecovery = false;
        } else {
          MismatchedSetException mse = new MismatchedSetException(null, input);
          throw mse;
        }


      }

      retval.stop = input.LT(-1);

      retval.tree = (Object) adaptor.rulePostProcessing(root_0);
      adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

    } catch (RecognitionException re) {
      reportError(re);
      recover(input, re);
      retval.tree = (Object) adaptor.errorNode(input, retval.start, input.LT(-1), re);

    } finally {
    }
    return retval;
  }
  // $ANTLR end "condition"

  // Delegated rules


  public static final BitSet FOLLOW_orexpression_in_expr192 = new BitSet(new long[]{0x0000000000000002L});
  public static final BitSet FOLLOW_andexpression_in_orexpression206 = new BitSet(new long[]{0x0000000000000082L});
  public static final BitSet FOLLOW_OR_in_orexpression209 = new BitSet(new long[]{0x0000000000000F10L});
  public static final BitSet FOLLOW_andexpression_in_orexpression212 = new BitSet(new long[]{0x0000000000000082L});
  public static final BitSet FOLLOW_notexpression_in_andexpression235 = new BitSet(new long[]{0x0000000000000042L});
  public static final BitSet FOLLOW_AND_in_andexpression238 = new BitSet(new long[]{0x0000000000000F10L});
  public static final BitSet FOLLOW_notexpression_in_andexpression241 = new BitSet(new long[]{0x0000000000000042L});
  public static final BitSet FOLLOW_NOT_in_notexpression261 = new BitSet(new long[]{0x0000000000000F10L});
  public static final BitSet FOLLOW_atom_in_notexpression266 = new BitSet(new long[]{0x0000000000000002L});
  public static final BitSet FOLLOW_condition_in_atom283 = new BitSet(new long[]{0x0000000000000002L});
  public static final BitSet FOLLOW_LEFT_PAREN_in_atom291 = new BitSet(new long[]{0x0000000000000F10L});
  public static final BitSet FOLLOW_orexpression_in_atom294 = new BitSet(new long[]{0x0000000000000020L});
  public static final BitSet FOLLOW_RIGHT_PAREN_in_atom296 = new BitSet(new long[]{0x0000000000000002L});
  public static final BitSet FOLLOW_set_in_condition0 = new BitSet(new long[]{0x0000000000000002L});

}