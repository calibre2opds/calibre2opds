// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g 2011-10-01 22:08:36


package com.gmail.dpierron.calibre.datamodel.calibrequerylanguage;


import org.antlr.runtime.*;

public class CalibreQueryLexer extends AbstractTLexer {
  public static final int RIGHT_PAREN = 5;
  public static final int OR = 7;
  public static final int LEFT_PAREN = 4;
  public static final int WHITESPACE = 12;
  public static final int NOT = 8;
  public static final int AND = 6;
  public static final int EOF = -1;
  public static final int RATING = 11;
  public static final int LANG = 10;
  public static final int TAG = 9;

  // delegates
  // delegators

  public CalibreQueryLexer() {;}

  public CalibreQueryLexer(CharStream input) {
    this(input, new RecognizerSharedState());
  }

  public CalibreQueryLexer(CharStream input, RecognizerSharedState state) {
    super(input, state);

  }

  public String getGrammarFileName() { return "com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g"; }

  // $ANTLR start "LEFT_PAREN"
  public final void mLEFT_PAREN() throws RecognitionException {
    try {
      int _type = LEFT_PAREN;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:24:12: ( '(' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:24:14: '('
      {
        match('(');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "LEFT_PAREN"

  // $ANTLR start "RIGHT_PAREN"
  public final void mRIGHT_PAREN() throws RecognitionException {
    try {
      int _type = RIGHT_PAREN;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:25:13: ( ')' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:25:15: ')'
      {
        match(')');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "RIGHT_PAREN"

  // $ANTLR start "AND"
  public final void mAND() throws RecognitionException {
    try {
      int _type = AND;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:26:5: ( 'and' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:26:7: 'and'
      {
        match("and");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "AND"

  // $ANTLR start "OR"
  public final void mOR() throws RecognitionException {
    try {
      int _type = OR;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:27:4: ( 'or' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:27:6: 'or'
      {
        match("or");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "OR"

  // $ANTLR start "NOT"
  public final void mNOT() throws RecognitionException {
    try {
      int _type = NOT;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:28:5: ( 'not' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:28:7: 'not'
      {
        match("not");


      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "NOT"

  // $ANTLR start "TAG"
  public final void mTAG() throws RecognitionException {
    try {
      int _type = TAG;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:5: ( 'tags:\"=' ( options {greedy=false; } : . )* '\"' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:7: 'tags:\"=' ( options {greedy=false; } : . )* '\"'
      {
        match("tags:\"=");

        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:16: ( options {greedy=false; } : . )*
        loop1:
        do {
          int alt1 = 2;
          int LA1_0 = input.LA(1);

          if ((LA1_0 == '\"')) {
            alt1 = 2;
          } else if (((LA1_0 >= '\u0000' && LA1_0 <= '!') || (LA1_0 >= '#' && LA1_0 <= '\uFFFF'))) {
            alt1 = 1;
          }


          switch (alt1) {
            case 1:
              // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:41: .
            {
              matchAny();

            }
            break;

            default:
              break loop1;
          }
        } while (true);

        match('\"');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "TAG"

  // $ANTLR start "LANG"
  public final void mLANG() throws RecognitionException {
    try {
      int _type = LANG;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:6: ( 'languages:\"=' ( options {greedy=false; } : . )* '\"' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:8: 'languages:\"=' ( options {greedy=false; } : . )* '\"'
      {
        match("languages:\"=");

        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:22: ( options {greedy=false; } : . )*
        loop2:
        do {
          int alt2 = 2;
          int LA2_0 = input.LA(1);

          if ((LA2_0 == '\"')) {
            alt2 = 2;
          } else if (((LA2_0 >= '\u0000' && LA2_0 <= '!') || (LA2_0 >= '#' && LA2_0 <= '\uFFFF'))) {
            alt2 = 1;
          }


          switch (alt2) {
            case 1:
              // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:46: .
            {
              matchAny();

            }
            break;

            default:
              break loop2;
          }
        } while (true);

        match('\"');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "LANG"

  // $ANTLR start "RATING"
  public final void mRATING() throws RecognitionException {
    try {
      int _type = RATING;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:8: ( 'rating:\"' ( '=' | '>' | '<' ) ( '0' .. '5' ) '\"' )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:10: 'rating:\"' ( '=' | '>' | '<' ) ( '0' .. '5' ) '\"'
      {
        match("rating:\"");

        if ((input.LA(1) >= '<' && input.LA(1) <= '>')) {
          input.consume();

        } else {
          MismatchedSetException mse = new MismatchedSetException(null, input);
          recover(mse);
          throw mse;
        }

        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:33: ( '0' .. '5' )
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:34: '0' .. '5'
        {
          matchRange('0', '5');

        }

        match('\"');

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "RATING"

  // $ANTLR start "WHITESPACE"
  public final void mWHITESPACE() throws RecognitionException {
    try {
      int _type = WHITESPACE;
      int _channel = DEFAULT_TOKEN_CHANNEL;
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:12: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+ )
      // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
      {
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
        int cnt3 = 0;
        loop3:
        do {
          int alt3 = 2;
          int LA3_0 = input.LA(1);

          if (((LA3_0 >= '\t' && LA3_0 <= '\n') || (LA3_0 >= '\f' && LA3_0 <= '\r') || LA3_0 == ' ')) {
            alt3 = 1;
          }


          switch (alt3) {
            case 1:
              // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:
            {
              if ((input.LA(1) >= '\t' && input.LA(1) <= '\n') || (input.LA(1) >= '\f' && input.LA(1) <= '\r') || input.LA(1) == ' ') {
                input.consume();

              } else {
                MismatchedSetException mse = new MismatchedSetException(null, input);
                recover(mse);
                throw mse;
              }


            }
            break;

            default:
              if (cnt3 >= 1)
                break loop3;
              EarlyExitException eee = new EarlyExitException(3, input);
              throw eee;
          }
          cnt3++;
        } while (true);

        _channel = HIDDEN;

      }

      state.type = _type;
      state.channel = _channel;
    } finally {
    }
  }
  // $ANTLR end "WHITESPACE"

  public void mTokens() throws RecognitionException {
    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:8: ( LEFT_PAREN | RIGHT_PAREN | AND | OR | NOT | TAG | LANG |
    // RATING | WHITESPACE )
    int alt4 = 9;
    switch (input.LA(1)) {
      case '(': {
        alt4 = 1;
      }
      break;
      case ')': {
        alt4 = 2;
      }
      break;
      case 'a': {
        alt4 = 3;
      }
      break;
      case 'o': {
        alt4 = 4;
      }
      break;
      case 'n': {
        alt4 = 5;
      }
      break;
      case 't': {
        alt4 = 6;
      }
      break;
      case 'l': {
        alt4 = 7;
      }
      break;
      case 'r': {
        alt4 = 8;
      }
      break;
      case '\t':
      case '\n':
      case '\f':
      case '\r':
      case ' ': {
        alt4 = 9;
      }
      break;
      default:
        NoViableAltException nvae = new NoViableAltException("", 4, 0, input);

        throw nvae;
    }

    switch (alt4) {
      case 1:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:10: LEFT_PAREN
      {
        mLEFT_PAREN();

      }
      break;
      case 2:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:21: RIGHT_PAREN
      {
        mRIGHT_PAREN();

      }
      break;
      case 3:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:33: AND
      {
        mAND();

      }
      break;
      case 4:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:37: OR
      {
        mOR();

      }
      break;
      case 5:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:40: NOT
      {
        mNOT();

      }
      break;
      case 6:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:44: TAG
      {
        mTAG();

      }
      break;
      case 7:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:48: LANG
      {
        mLANG();

      }
      break;
      case 8:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:53: RATING
      {
        mRATING();

      }
      break;
      case 9:
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:60: WHITESPACE
      {
        mWHITESPACE();

      }
      break;

    }

  }


}