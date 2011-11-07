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

// calibre specific constraints
TAG : T A G S ':"' (options {greedy=false;}:.)*'"';
LANG : L A N G U A G E S ':"' (options{greedy=false;}:.)*'"';
RATING : R A T I N G ':' ('"')? ('<'|'>'|'=')? ('0'..'5') ('"')?;

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
