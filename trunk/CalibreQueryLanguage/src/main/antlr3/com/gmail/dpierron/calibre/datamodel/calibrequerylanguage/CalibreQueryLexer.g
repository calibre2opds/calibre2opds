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
AND : 'and';
OR : 'or';
NOT : 'not';

// calibre specific constraints
TAG : 'tags:"='(options {greedy=false;}:.)*'"';
LANG : 'languages:"='(options{greedy=false;}:.)*'"';
RATING : 'rating:"'('='|'>'|'<')('0'..'5')'"';

// skip all whitespace
WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ { $channel = HIDDEN; };
