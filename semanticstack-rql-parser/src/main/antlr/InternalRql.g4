/*
 * Copyright (c)2024 Robert Bosch Manufacturing Solutions GmbH
 *
 *  See the AUTHORS file(s) distributed with this work for additional
 *  information regarding authorship.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *  SPDX-License-Identifier: MPL-2.0
 */

grammar InternalRql;

@header {
package com.boschsemanticstack.rql.parser.v1.internal;
}

query // entrypoint for complete query
    : queryParameter ( '&' queryParameter )* EOF
    | EOF
    ;

queryParameter
    : selectDeclaration
    | filterDeclaration
    | optionList
    ;

selectDeclaration
    : 'select' '=' fieldList
    ;

filterDeclaration
    : 'filter' '=' filterExpression
    ;

fieldList // entrypoint for fields if already partitioned
    : FieldIdentifier (',' FieldIdentifier)*
    |
    ;

filterList // entrypoint for filter if already partitioned
    : '(' filterExpression (',' filterExpression)* ')'
    ;

optionList // entrypoint for options if already partitioned
    : 'option' '=' optionExpression
    ;
// end toplevel expressions

filterExpression
    : logicalOperator
    | multiComparison
    | singleComparison
    ;


logicalOperator
    : 'not' '(' filterExpression ')'
    | 'and' filterList
    | 'or' filterList
    ;

multiComparison
    : 'in' '(' FieldIdentifier ',' literalList ')'
    ;

singleComparison
    : 'eq' '(' FieldIdentifier ',' literal ')' #equalityComparison //<1>
    | 'ne' '(' FieldIdentifier ',' literal ')' #equalityComparison //<1>
    | 'lt' '(' FieldIdentifier ',' linearilyOrderableLiteral ')' #orderRelation //<2>
    | 'le' '(' FieldIdentifier ',' linearilyOrderableLiteral ')' #orderRelation //<2>
    | 'gt' '(' FieldIdentifier ',' linearilyOrderableLiteral ')' #orderRelation //<2>
    | 'ge' '(' FieldIdentifier ',' linearilyOrderableLiteral ')' #orderRelation //<2>
    | 'like' '(' FieldIdentifier ',' StringLiteral ')' #stringMatch             //<3>
    | 'likeIgnoreCase' '(' FieldIdentifier ',' StringLiteral ')' #stringMatch   //<3>
    ;

literalList
    : stringLiteralList
    | floatLiteralList
    | intLiteralList
    ;

stringLiteralList
    : StringLiteral (',' StringLiteral)*
    ;

floatLiteralList
    : FloatLiteral (',' FloatLiteral)*
    ;

intLiteralList
    : IntLiteral (',' IntLiteral)*
    ;

optionExpression
    : sortExpression (',' limitOrCursorExpression)? | limitOrCursorExpression (',' sortExpression)?
    ;


limitOrCursorExpression
    : limitExpression | cursorExpression
    ;

sortExpression
    : 'sort' '(' sortFieldIdentifier (',' sortFieldIdentifier)* ')'
    ;

limitExpression
    : 'limit' '(' IntLiteral ',' IntLiteral ')'
    ;

cursorExpression
    : 'cursor' '(' (StringLiteral ',')? IntLiteral ')'
    ;

literal
    : linearilyOrderableLiteral
    | BooleanLiteral
    | NullLiteral
    ;

linearilyOrderableLiteral
    : FloatLiteral
    | IntLiteral
    | StringLiteral
    | TimeLiteral
    ;

NullLiteral
    : 'null'
    ;

BooleanLiteral
    : 'true'
    | 'false'
    ;

TimeLiteral
    : FullDate ('T'|'t') FullTime;

fragment DateFullyear : Digit Digit Digit Digit;
fragment DateMonth : Digit Digit;
fragment DateMday  : Digit Digit;
fragment TimeHour  : Digit Digit;
fragment TimeMinute : Digit Digit;
fragment TimeSecond: Digit Digit;
fragment TimeSecfrac : ('.'|',') Digit+;
fragment TimeNumOffset : Sign TimeHour ':' TimeMinute;
fragment TimeOffset: ('Z'|'z') | TimeNumOffset;
fragment PartialTime : TimeHour ':' TimeMinute ':' TimeSecond TimeSecfrac?;
fragment FullDate : DateFullyear '-' DateMonth '-' DateMday;
fragment FullTime : PartialTime TimeOffset;


FloatLiteral
    :  Sign? '0.' Digit+ ExponentPart?
    |  Sign? DigitWithoutZero Digit* '.' Digit+ ExponentPart?
    |  Sign? DigitWithoutZero Digit* ExponentPart
    ;

IntLiteral
    : Sign? DigitWithoutZero Digit*
    | '0'
    ;

sortFieldIdentifier
    : Sign FieldIdentifier
    ;

FieldIdentifier
    :   [a-zA-Z_][a-zA-Z0-9._]*
    ;

Sign
: [+-]
;


fragment
ExponentPart
	:	[eE] Sign? Digit+
	;


fragment DigitWithoutZero
    : '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';

fragment Digit
    : '0' | DigitWithoutZero;

StringLiteral
    : '"' StringCharacters? '"'
    ;

fragment StringCharacters
    : StringElement+
    ;

fragment StringElement
    : ~["\\]
    | CharEscapeSeq
    ;

fragment CharEscapeSeq : '\\' ('b' | 't' | 'n' | 'f' | 'r' | '"' | '\\');

WS
   : [ \t\n\r] + -> skip
   ;

