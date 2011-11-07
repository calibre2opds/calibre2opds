/*
interpret Calibre Query Language 
*/
lexer grammar CalibreQueryLexer;

options {
   language=Java;  // Default

   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and 
   // variables will be placed.
   //
   superClass = AbstractTLexer;
}

// What package should the generated source exist in?
//
@header {

    package com.gmail.dpierron.calibre.datamodel.calibrequerylanguage;
}

// standard boolean arithmetics
LEFT_PAREN : '(';
RIGHT_PAREN : ')';
AND : A N D;
OR : O R;
NOT : N O T;

// tag filters
TAG : T A G S ':"' (options {greedy=false;}:.)*'"';
TAG_TRUE: T A G S ':' T R U E;
TAG_FALSE: T A G S ':' F A L S E;

// language filters
LANG : L A N G U A G E S ':"' (options{greedy=false;}:.)*'"';
LANG_TRUE: L A N G U A G E S ':' T R U E;
LANG_FALSE: L A N G U A G E S ':' F A L S E;

// author filters
AUTHOR : A U T H O R S ':"' (options{greedy=false;}:.)*'"';
AUTHOR_TRUE: A U T H O R S ':' T R U E;
AUTHOR_FALSE: A U T H O R S ':' F A L S E;

// series filters
SERIES : S E R I E S ':"' (options{greedy=false;}:.)*'"';
SERIES_TRUE: S E R I E S ':' T R U E;
SERIES_FALSE: S E R I E S ':' F A L S E;

// format filters
FORMAT : F O R M A T S ':"' (options{greedy=false;}:.)*'"';
FORMAT_TRUE: F O R M A T S ':' T R U E;
FORMAT_FALSE: F O R M A T S ':' F A L S E;

// publisher filters
PUBLISHER : P U B L I S H E R ':"' (options{greedy=false;}:.)*'"';
PUBLISHER_TRUE: P U B L I S H E R ':' T R U E;
PUBLISHER_FALSE: P U B L I S H E R ':' F A L S E;

// rating filters
RATING : R A T I N G ':' ('"')? ('<'|'>'|'=')? ('0'..'5') ('"')?;
RATING_TRUE: R A T I N G ':' T R U E;
RATING_FALSE: R A T I N G ':' F A L S E;

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

// skip all whitespace
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
