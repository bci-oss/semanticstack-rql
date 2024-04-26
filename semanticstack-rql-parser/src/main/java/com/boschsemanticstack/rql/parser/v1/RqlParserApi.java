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

import java.util.Optional;
import java.util.stream.Collectors;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

class RqlParserApi extends BaseRqlParserApi {

   public RqlQueryModel parseFullQuery( final String rqlQuery ) {
      if ( rqlQuery == null ) {
         throw new ParseException( "Input was null!" );
      }
      final ParseResult parseResult = createParseTree( rqlQuery );
      return createModelFromParseTree( parseResult );
   }

   private RqlQueryModel createModelFromParseTree( final ParseResult parseResult ) {
      if ( parseResult.getErrors().isEmpty() && parseResult.getParseTree() != null ) {
         return (RqlQueryModel) new RqlParseTreeVisitor().visit( parseResult.getParseTree() );
      }
      final Optional<ParseException> firstAvailableErrorWithLocation = parseResult.getErrors().stream()
            .filter( exception -> exception.getSourceLocation().isPresent() )
            .findFirst();

      throw firstAvailableErrorWithLocation.orElse( createFallbackException( parseResult ) );
   }

   private ParseException createFallbackException( final ParseResult parseResult ) {
      final ParseException argumentException = new ParseException( "Not a valid rqlQuery: "
            + parseResult.getErrors().stream()
            .map( Exception::getMessage )
            .collect( Collectors.joining( "\n" ) ) );
      parseResult.getErrors().forEach( argumentException::addSuppressed );
      return argumentException;
   }
}
