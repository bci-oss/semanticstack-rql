/*
 * Copyright (c) 2024 Robert Bosch Manufacturing Solutions GmbH
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package com.boschsemanticstack.rql.parser.v1;

import java.util.function.Function;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.exceptions.SourceLocation;
import com.boschsemanticstack.rql.parser.v1.internal.InternalRqlLexer;
import com.boschsemanticstack.rql.parser.v1.internal.InternalRqlParser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

class BaseRqlParserApi {
   public ParseResult createParseTree( final String rqlQuery ) {
      return createParseTree( rqlQuery, InternalRqlParser::query );
   }

   public ParseResult createParseTree( final String rqlQuery, final Function<InternalRqlParser, ParserRuleContext> startingRule ) {
      final ParseResult result = new ParseResult();
      final CollectingErrorListener errorListener = new CollectingErrorListener( result );
      final InternalRqlLexer lexer = new InternalRqlLexer( CharStreams.fromString( rqlQuery ) );
      final InternalRqlParser parser = new InternalRqlParser( new CommonTokenStream( lexer ) );
      lexer.removeErrorListeners(); // default error listener prints to System.out
      lexer.addErrorListener( errorListener );

      parser.setErrorHandler( new DefaultErrorStrategy() );
      parser.removeErrorListeners(); // default error listener prints to System.out
      parser.addErrorListener( errorListener );
      result.setParser( parser );

      try {
         result.setParseTree( startingRule.apply( parser ) );
      } catch ( final RecognitionException e ) {
         new DefaultErrorStrategy().reportError( parser, e ); // should not happen - loops back to collecting Error listener above
      }
      return result;
   }

   private static class CollectingErrorListener extends BaseErrorListener {
      private final ParseResult result;

      CollectingErrorListener( final ParseResult result ) {
         this.result = result;
      }

      @Override
      public void syntaxError(
            final Recognizer<?, ?> recognizer,
            final Object offendingSymbol,
            final int line,
            final int charPositionInLine,
            final String msg,
            final RecognitionException e ) {

         if ( line < 0 || charPositionInLine < 0 ) { // ANTLRs way of telling us it has no information on location
            result.getErrors().add( new ParseException( msg ) );
            return;
         }

         result.getErrors().add(
               new ParseException(
                     msg,
                     new SourceLocation( line, charPositionInLine + 1 )
               ) );
      }
   }
}
