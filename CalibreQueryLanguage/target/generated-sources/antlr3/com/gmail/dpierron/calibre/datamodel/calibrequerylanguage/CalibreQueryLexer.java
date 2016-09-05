// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g 2016-09-05 14:01:08


    package com.gmail.dpierron.calibre.datamodel.calibrequerylanguage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CalibreQueryLexer extends AbstractTLexer {
    public static final int AUTHOR=28;
    public static final int FORMAT_FALSE=38;
    public static final int RATING_FALSE=46;
    public static final int PUBLISHER=41;
    public static final int SERIES_FALSE=34;
    public static final int TAG_FALSE=23;
    public static final int NOT=14;
    public static final int AND=9;
    public static final int RATING_TRUE=45;
    public static final int EOF=-1;
    public static final int SERIES_TRUE=33;
    public static final int RIGHT_PAREN=5;
    public static final int FORMAT_TRUE=37;
    public static final int LANG_TRUE=25;
    public static final int TAG_TRUE=20;
    public static final int RATING=44;
    public static final int PUBLISHER_FALSE=43;
    public static final int D=8;
    public static final int E=19;
    public static final int F=21;
    public static final int G=15;
    public static final int A=6;
    public static final int B=40;
    public static final int C=47;
    public static final int LANG_FALSE=26;
    public static final int SERIES=32;
    public static final int L=22;
    public static final int M=35;
    public static final int N=7;
    public static final int O=10;
    public static final int H=27;
    public static final int I=31;
    public static final int J=48;
    public static final int K=49;
    public static final int U=18;
    public static final int T=13;
    public static final int WHITESPACE=56;
    public static final int W=52;
    public static final int V=51;
    public static final int Q=50;
    public static final int P=39;
    public static final int S=16;
    public static final int AUTHOR_TRUE=29;
    public static final int R=11;
    public static final int Y=54;
    public static final int X=53;
    public static final int TAG=17;
    public static final int Z=55;
    public static final int OR=12;
    public static final int PUBLISHER_TRUE=42;
    public static final int LEFT_PAREN=4;
    public static final int AUTHOR_FALSE=30;
    public static final int FORMAT=36;
    public static final int LANG=24;

    // delegates
    // delegators

    public CalibreQueryLexer() {;} 
    public CalibreQueryLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CalibreQueryLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

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
        }
        finally {
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
        }
        finally {
        }
    }
    // $ANTLR end "RIGHT_PAREN"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:26:5: ( A N D )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:26:7: A N D
            {
            mA(); 
            mN(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:27:4: ( O R )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:27:6: O R
            {
            mO(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:28:5: ( N O T )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:28:7: N O T
            {
            mN(); 
            mO(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "TAG"
    public final void mTAG() throws RecognitionException {
        try {
            int _type = TAG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:5: ( T A G S ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:7: T A G S ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mT(); 
            mA(); 
            mG(); 
            mS(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:20: ( options {greedy=false; } : . )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\"') ) {
                    alt1=2;
                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='!')||(LA1_0>='#' && LA1_0<='\uFFFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:31:45: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TAG"

    // $ANTLR start "TAG_TRUE"
    public final void mTAG_TRUE() throws RecognitionException {
        try {
            int _type = TAG_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:9: ( T A G S ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:32:11: T A G S ':' T R U E
            {
            mT(); 
            mA(); 
            mG(); 
            mS(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TAG_TRUE"

    // $ANTLR start "TAG_FALSE"
    public final void mTAG_FALSE() throws RecognitionException {
        try {
            int _type = TAG_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:10: ( T A G S ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:33:12: T A G S ':' F A L S E
            {
            mT(); 
            mA(); 
            mG(); 
            mS(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TAG_FALSE"

    // $ANTLR start "LANG"
    public final void mLANG() throws RecognitionException {
        try {
            int _type = LANG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:6: ( L A N G U A G E S ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:8: L A N G U A G E S ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mL(); 
            mA(); 
            mN(); 
            mG(); 
            mU(); 
            mA(); 
            mG(); 
            mE(); 
            mS(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:31: ( options {greedy=false; } : . )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\"') ) {
                    alt2=2;
                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='!')||(LA2_0>='#' && LA2_0<='\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:36:55: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LANG"

    // $ANTLR start "LANG_TRUE"
    public final void mLANG_TRUE() throws RecognitionException {
        try {
            int _type = LANG_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:37:10: ( L A N G U A G E S ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:37:12: L A N G U A G E S ':' T R U E
            {
            mL(); 
            mA(); 
            mN(); 
            mG(); 
            mU(); 
            mA(); 
            mG(); 
            mE(); 
            mS(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LANG_TRUE"

    // $ANTLR start "LANG_FALSE"
    public final void mLANG_FALSE() throws RecognitionException {
        try {
            int _type = LANG_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:38:11: ( L A N G U A G E S ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:38:13: L A N G U A G E S ':' F A L S E
            {
            mL(); 
            mA(); 
            mN(); 
            mG(); 
            mU(); 
            mA(); 
            mG(); 
            mE(); 
            mS(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LANG_FALSE"

    // $ANTLR start "AUTHOR"
    public final void mAUTHOR() throws RecognitionException {
        try {
            int _type = AUTHOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:41:8: ( A U T H O R S ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:41:10: A U T H O R S ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mA(); 
            mU(); 
            mT(); 
            mH(); 
            mO(); 
            mR(); 
            mS(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:41:29: ( options {greedy=false; } : . )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\"') ) {
                    alt3=2;
                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:41:53: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AUTHOR"

    // $ANTLR start "AUTHOR_TRUE"
    public final void mAUTHOR_TRUE() throws RecognitionException {
        try {
            int _type = AUTHOR_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:42:12: ( A U T H O R S ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:42:14: A U T H O R S ':' T R U E
            {
            mA(); 
            mU(); 
            mT(); 
            mH(); 
            mO(); 
            mR(); 
            mS(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AUTHOR_TRUE"

    // $ANTLR start "AUTHOR_FALSE"
    public final void mAUTHOR_FALSE() throws RecognitionException {
        try {
            int _type = AUTHOR_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:43:13: ( A U T H O R S ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:43:15: A U T H O R S ':' F A L S E
            {
            mA(); 
            mU(); 
            mT(); 
            mH(); 
            mO(); 
            mR(); 
            mS(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AUTHOR_FALSE"

    // $ANTLR start "SERIES"
    public final void mSERIES() throws RecognitionException {
        try {
            int _type = SERIES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:46:8: ( S E R I E S ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:46:10: S E R I E S ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mS(); 
            mE(); 
            mR(); 
            mI(); 
            mE(); 
            mS(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:46:27: ( options {greedy=false; } : . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\"') ) {
                    alt4=2;
                }
                else if ( ((LA4_0>='\u0000' && LA4_0<='!')||(LA4_0>='#' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:46:51: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SERIES"

    // $ANTLR start "SERIES_TRUE"
    public final void mSERIES_TRUE() throws RecognitionException {
        try {
            int _type = SERIES_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:47:12: ( S E R I E S ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:47:14: S E R I E S ':' T R U E
            {
            mS(); 
            mE(); 
            mR(); 
            mI(); 
            mE(); 
            mS(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SERIES_TRUE"

    // $ANTLR start "SERIES_FALSE"
    public final void mSERIES_FALSE() throws RecognitionException {
        try {
            int _type = SERIES_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:48:13: ( S E R I E S ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:48:15: S E R I E S ':' F A L S E
            {
            mS(); 
            mE(); 
            mR(); 
            mI(); 
            mE(); 
            mS(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SERIES_FALSE"

    // $ANTLR start "FORMAT"
    public final void mFORMAT() throws RecognitionException {
        try {
            int _type = FORMAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:51:8: ( F O R M A T S ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:51:10: F O R M A T S ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mF(); 
            mO(); 
            mR(); 
            mM(); 
            mA(); 
            mT(); 
            mS(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:51:29: ( options {greedy=false; } : . )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\"') ) {
                    alt5=2;
                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:51:53: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FORMAT"

    // $ANTLR start "FORMAT_TRUE"
    public final void mFORMAT_TRUE() throws RecognitionException {
        try {
            int _type = FORMAT_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:52:12: ( F O R M A T S ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:52:14: F O R M A T S ':' T R U E
            {
            mF(); 
            mO(); 
            mR(); 
            mM(); 
            mA(); 
            mT(); 
            mS(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FORMAT_TRUE"

    // $ANTLR start "FORMAT_FALSE"
    public final void mFORMAT_FALSE() throws RecognitionException {
        try {
            int _type = FORMAT_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:53:13: ( F O R M A T S ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:53:15: F O R M A T S ':' F A L S E
            {
            mF(); 
            mO(); 
            mR(); 
            mM(); 
            mA(); 
            mT(); 
            mS(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FORMAT_FALSE"

    // $ANTLR start "PUBLISHER"
    public final void mPUBLISHER() throws RecognitionException {
        try {
            int _type = PUBLISHER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:56:11: ( P U B L I S H E R ':\"' ( options {greedy=false; } : . )* '\"' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:56:13: P U B L I S H E R ':\"' ( options {greedy=false; } : . )* '\"'
            {
            mP(); 
            mU(); 
            mB(); 
            mL(); 
            mI(); 
            mS(); 
            mH(); 
            mE(); 
            mR(); 
            match(":\""); 

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:56:36: ( options {greedy=false; } : . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\"') ) {
                    alt6=2;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='!')||(LA6_0>='#' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:56:60: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PUBLISHER"

    // $ANTLR start "PUBLISHER_TRUE"
    public final void mPUBLISHER_TRUE() throws RecognitionException {
        try {
            int _type = PUBLISHER_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:57:15: ( P U B L I S H E R ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:57:17: P U B L I S H E R ':' T R U E
            {
            mP(); 
            mU(); 
            mB(); 
            mL(); 
            mI(); 
            mS(); 
            mH(); 
            mE(); 
            mR(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PUBLISHER_TRUE"

    // $ANTLR start "PUBLISHER_FALSE"
    public final void mPUBLISHER_FALSE() throws RecognitionException {
        try {
            int _type = PUBLISHER_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:58:16: ( P U B L I S H E R ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:58:18: P U B L I S H E R ':' F A L S E
            {
            mP(); 
            mU(); 
            mB(); 
            mL(); 
            mI(); 
            mS(); 
            mH(); 
            mE(); 
            mR(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PUBLISHER_FALSE"

    // $ANTLR start "RATING"
    public final void mRATING() throws RecognitionException {
        try {
            int _type = RATING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:8: ( R A T I N G ':' ( '\"' )? ( '<' | '>' | '=' )? ( '0' .. '5' ) ( '\"' )? )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:10: R A T I N G ':' ( '\"' )? ( '<' | '>' | '=' )? ( '0' .. '5' ) ( '\"' )?
            {
            mR(); 
            mA(); 
            mT(); 
            mI(); 
            mN(); 
            mG(); 
            match(':'); 
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:26: ( '\"' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\"') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:27: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:33: ( '<' | '>' | '=' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( ((LA8_0>='<' && LA8_0<='>')) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:
                    {
                    if ( (input.LA(1)>='<' && input.LA(1)<='>') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:48: ( '0' .. '5' )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:49: '0' .. '5'
            {
            matchRange('0','5'); 

            }

            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:59: ( '\"' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\"') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:61:60: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RATING"

    // $ANTLR start "RATING_TRUE"
    public final void mRATING_TRUE() throws RecognitionException {
        try {
            int _type = RATING_TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:62:12: ( R A T I N G ':' T R U E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:62:14: R A T I N G ':' T R U E
            {
            mR(); 
            mA(); 
            mT(); 
            mI(); 
            mN(); 
            mG(); 
            match(':'); 
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RATING_TRUE"

    // $ANTLR start "RATING_FALSE"
    public final void mRATING_FALSE() throws RecognitionException {
        try {
            int _type = RATING_FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:63:13: ( R A T I N G ':' F A L S E )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:63:15: R A T I N G ':' F A L S E
            {
            mR(); 
            mA(); 
            mT(); 
            mI(); 
            mN(); 
            mG(); 
            match(':'); 
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RATING_FALSE"

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:70:11: ( ( 'a' | 'A' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:70:12: ( 'a' | 'A' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:71:11: ( ( 'b' | 'B' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:71:12: ( 'b' | 'B' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:72:11: ( ( 'c' | 'C' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:72:12: ( 'c' | 'C' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:73:11: ( ( 'd' | 'D' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:73:12: ( 'd' | 'D' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:74:11: ( ( 'e' | 'E' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:74:12: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:75:11: ( ( 'f' | 'F' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:75:12: ( 'f' | 'F' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:76:11: ( ( 'g' | 'G' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:76:12: ( 'g' | 'G' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:77:11: ( ( 'h' | 'H' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:77:12: ( 'h' | 'H' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:78:11: ( ( 'i' | 'I' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:78:12: ( 'i' | 'I' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:79:11: ( ( 'j' | 'J' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:79:12: ( 'j' | 'J' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:80:11: ( ( 'k' | 'K' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:80:12: ( 'k' | 'K' )
            {
            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:81:11: ( ( 'l' | 'L' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:81:12: ( 'l' | 'L' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:82:11: ( ( 'm' | 'M' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:82:12: ( 'm' | 'M' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:83:11: ( ( 'n' | 'N' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:83:12: ( 'n' | 'N' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:84:11: ( ( 'o' | 'O' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:84:12: ( 'o' | 'O' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:85:11: ( ( 'p' | 'P' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:85:12: ( 'p' | 'P' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:86:11: ( ( 'q' | 'Q' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:86:12: ( 'q' | 'Q' )
            {
            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:87:11: ( ( 'r' | 'R' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:87:12: ( 'r' | 'R' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:88:11: ( ( 's' | 'S' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:88:12: ( 's' | 'S' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:89:11: ( ( 't' | 'T' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:89:12: ( 't' | 'T' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:90:11: ( ( 'u' | 'U' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:90:12: ( 'u' | 'U' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:91:11: ( ( 'v' | 'V' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:91:12: ( 'v' | 'V' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:92:11: ( ( 'w' | 'W' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:92:12: ( 'w' | 'W' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:93:11: ( ( 'x' | 'X' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:93:12: ( 'x' | 'X' )
            {
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:94:11: ( ( 'y' | 'Y' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:94:12: ( 'y' | 'Y' )
            {
            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:95:11: ( ( 'z' | 'Z' ) )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:95:12: ( 'z' | 'Z' )
            {
            if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:98:12: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+ )
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:98:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            {
            // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:98:14: ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='\t' && LA10_0<='\n')||(LA10_0>='\f' && LA10_0<='\r')||LA10_0==' ') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHITESPACE"

    public void mTokens() throws RecognitionException {
        // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:8: ( LEFT_PAREN | RIGHT_PAREN | AND | OR | NOT | TAG | TAG_TRUE | TAG_FALSE | LANG | LANG_TRUE | LANG_FALSE | AUTHOR | AUTHOR_TRUE | AUTHOR_FALSE | SERIES | SERIES_TRUE | SERIES_FALSE | FORMAT | FORMAT_TRUE | FORMAT_FALSE | PUBLISHER | PUBLISHER_TRUE | PUBLISHER_FALSE | RATING | RATING_TRUE | RATING_FALSE | WHITESPACE )
        int alt11=27;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:10: LEFT_PAREN
                {
                mLEFT_PAREN(); 

                }
                break;
            case 2 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:21: RIGHT_PAREN
                {
                mRIGHT_PAREN(); 

                }
                break;
            case 3 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:33: AND
                {
                mAND(); 

                }
                break;
            case 4 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:37: OR
                {
                mOR(); 

                }
                break;
            case 5 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:40: NOT
                {
                mNOT(); 

                }
                break;
            case 6 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:44: TAG
                {
                mTAG(); 

                }
                break;
            case 7 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:48: TAG_TRUE
                {
                mTAG_TRUE(); 

                }
                break;
            case 8 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:57: TAG_FALSE
                {
                mTAG_FALSE(); 

                }
                break;
            case 9 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:67: LANG
                {
                mLANG(); 

                }
                break;
            case 10 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:72: LANG_TRUE
                {
                mLANG_TRUE(); 

                }
                break;
            case 11 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:82: LANG_FALSE
                {
                mLANG_FALSE(); 

                }
                break;
            case 12 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:93: AUTHOR
                {
                mAUTHOR(); 

                }
                break;
            case 13 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:100: AUTHOR_TRUE
                {
                mAUTHOR_TRUE(); 

                }
                break;
            case 14 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:112: AUTHOR_FALSE
                {
                mAUTHOR_FALSE(); 

                }
                break;
            case 15 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:125: SERIES
                {
                mSERIES(); 

                }
                break;
            case 16 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:132: SERIES_TRUE
                {
                mSERIES_TRUE(); 

                }
                break;
            case 17 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:144: SERIES_FALSE
                {
                mSERIES_FALSE(); 

                }
                break;
            case 18 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:157: FORMAT
                {
                mFORMAT(); 

                }
                break;
            case 19 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:164: FORMAT_TRUE
                {
                mFORMAT_TRUE(); 

                }
                break;
            case 20 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:176: FORMAT_FALSE
                {
                mFORMAT_FALSE(); 

                }
                break;
            case 21 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:189: PUBLISHER
                {
                mPUBLISHER(); 

                }
                break;
            case 22 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:199: PUBLISHER_TRUE
                {
                mPUBLISHER_TRUE(); 

                }
                break;
            case 23 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:214: PUBLISHER_FALSE
                {
                mPUBLISHER_FALSE(); 

                }
                break;
            case 24 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:230: RATING
                {
                mRATING(); 

                }
                break;
            case 25 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:237: RATING_TRUE
                {
                mRATING_TRUE(); 

                }
                break;
            case 26 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:249: RATING_FALSE
                {
                mRATING_FALSE(); 

                }
                break;
            case 27 :
                // com\\gmail\\dpierron\\calibre\\datamodel\\calibrequerylanguage\\CalibreQueryLexer.g:1:262: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\123\uffff";
    static final String DFA11_eofS =
        "\123\uffff";
    static final String DFA11_minS =
        "\1\11\2\uffff\1\116\2\uffff\2\101\1\105\1\117\1\125\1\101\2\uffff"+
        "\1\124\1\107\1\116\2\122\1\102\1\124\1\110\1\123\1\107\1\111\1\115"+
        "\1\114\1\111\1\117\1\72\1\125\1\105\1\101\1\111\1\116\1\122\1\42"+
        "\1\101\1\123\1\124\1\123\1\107\1\123\3\uffff\1\107\1\72\1\123\1"+
        "\110\2\72\1\105\1\42\1\72\1\105\2\42\1\123\3\uffff\1\42\1\122\6"+
        "\uffff\1\72\3\uffff\1\72\2\42\6\uffff";
    static final String DFA11_maxS =
        "\1\164\2\uffff\1\165\2\uffff\2\141\1\145\1\157\1\165\1\141\2\uffff"+
        "\1\164\1\147\1\156\2\162\1\142\1\164\1\150\1\163\1\147\1\151\1\155"+
        "\1\154\1\151\1\157\1\72\1\165\1\145\1\141\1\151\1\156\1\162\1\164"+
        "\1\141\1\163\1\164\1\163\1\147\1\163\3\uffff\1\147\1\72\1\163\1"+
        "\150\2\72\1\145\1\164\1\72\1\145\2\164\1\163\3\uffff\1\164\1\162"+
        "\6\uffff\1\72\3\uffff\1\72\2\164\6\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\6\uffff\1\33\1\3\35\uffff\1\6"+
        "\1\10\1\7\15\uffff\1\17\1\21\1\20\2\uffff\1\31\1\30\1\32\1\14\1"+
        "\16\1\15\1\uffff\1\22\1\24\1\23\3\uffff\1\11\1\13\1\12\1\25\1\27"+
        "\1\26";
    static final String DFA11_specialS =
        "\123\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\14\1\uffff\2\14\22\uffff\1\14\7\uffff\1\1\1\2\27\uffff\1"+
            "\3\4\uffff\1\11\5\uffff\1\7\1\uffff\1\5\1\4\1\12\1\uffff\1\13"+
            "\1\10\1\6\14\uffff\1\3\4\uffff\1\11\5\uffff\1\7\1\uffff\1\5"+
            "\1\4\1\12\1\uffff\1\13\1\10\1\6",
            "",
            "",
            "\1\15\6\uffff\1\16\30\uffff\1\15\6\uffff\1\16",
            "",
            "",
            "\1\17\37\uffff\1\17",
            "\1\20\37\uffff\1\20",
            "\1\21\37\uffff\1\21",
            "\1\22\37\uffff\1\22",
            "\1\23\37\uffff\1\23",
            "\1\24\37\uffff\1\24",
            "",
            "",
            "\1\25\37\uffff\1\25",
            "\1\26\37\uffff\1\26",
            "\1\27\37\uffff\1\27",
            "\1\30\37\uffff\1\30",
            "\1\31\37\uffff\1\31",
            "\1\32\37\uffff\1\32",
            "\1\33\37\uffff\1\33",
            "\1\34\37\uffff\1\34",
            "\1\35\37\uffff\1\35",
            "\1\36\37\uffff\1\36",
            "\1\37\37\uffff\1\37",
            "\1\40\37\uffff\1\40",
            "\1\41\37\uffff\1\41",
            "\1\42\37\uffff\1\42",
            "\1\43\37\uffff\1\43",
            "\1\44",
            "\1\45\37\uffff\1\45",
            "\1\46\37\uffff\1\46",
            "\1\47\37\uffff\1\47",
            "\1\50\37\uffff\1\50",
            "\1\51\37\uffff\1\51",
            "\1\52\37\uffff\1\52",
            "\1\53\43\uffff\1\54\15\uffff\1\55\21\uffff\1\54\15\uffff\1"+
            "\55",
            "\1\56\37\uffff\1\56",
            "\1\57\37\uffff\1\57",
            "\1\60\37\uffff\1\60",
            "\1\61\37\uffff\1\61",
            "\1\62\37\uffff\1\62",
            "\1\63\37\uffff\1\63",
            "",
            "",
            "",
            "\1\64\37\uffff\1\64",
            "\1\65",
            "\1\66\37\uffff\1\66",
            "\1\67\37\uffff\1\67",
            "\1\70",
            "\1\71",
            "\1\72\37\uffff\1\72",
            "\1\73\43\uffff\1\74\15\uffff\1\75\21\uffff\1\74\15\uffff\1"+
            "\75",
            "\1\76",
            "\1\77\37\uffff\1\77",
            "\1\101\15\uffff\6\101\6\uffff\3\101\7\uffff\1\102\15\uffff"+
            "\1\100\21\uffff\1\102\15\uffff\1\100",
            "\1\103\43\uffff\1\104\15\uffff\1\105\21\uffff\1\104\15\uffff"+
            "\1\105",
            "\1\106\37\uffff\1\106",
            "",
            "",
            "",
            "\1\107\43\uffff\1\110\15\uffff\1\111\21\uffff\1\110\15\uffff"+
            "\1\111",
            "\1\112\37\uffff\1\112",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\113",
            "",
            "",
            "",
            "\1\114",
            "\1\115\43\uffff\1\116\15\uffff\1\117\21\uffff\1\116\15\uffff"+
            "\1\117",
            "\1\120\43\uffff\1\121\15\uffff\1\122\21\uffff\1\121\15\uffff"+
            "\1\122",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( LEFT_PAREN | RIGHT_PAREN | AND | OR | NOT | TAG | TAG_TRUE | TAG_FALSE | LANG | LANG_TRUE | LANG_FALSE | AUTHOR | AUTHOR_TRUE | AUTHOR_FALSE | SERIES | SERIES_TRUE | SERIES_FALSE | FORMAT | FORMAT_TRUE | FORMAT_FALSE | PUBLISHER | PUBLISHER_TRUE | PUBLISHER_FALSE | RATING | RATING_TRUE | RATING_FALSE | WHITESPACE );";
        }
    }
 

}