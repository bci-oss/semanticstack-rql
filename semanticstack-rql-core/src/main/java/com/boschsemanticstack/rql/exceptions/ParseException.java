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

package com.boschsemanticstack.rql.exceptions;

import java.util.Optional;

public class ParseException extends RuntimeException {

   private final SourceLocation sourceLocation;

   public ParseException( final String message ) {
      this( message, (SourceLocation) null );
   }

   public ParseException( final String message, final SourceLocation sourceLocation ) {
      super( createErrorMessage( message, sourceLocation ) );
      this.sourceLocation = sourceLocation;
   }

   public ParseException( final String message, final Throwable cause ) {
      this( message, (SourceLocation) null, cause );
   }

   public ParseException( final String message, final SourceLocation sourceLocation, final Throwable cause ) {
      super( createErrorMessage( message, sourceLocation ), cause );
      this.sourceLocation = sourceLocation;
   }

   public ParseException( final String message, final Optional<SourceLocation> sourceLocation, final Throwable cause ) {
      super( createErrorMessage( message, sourceLocation.orElse( null ) ), cause );
      this.sourceLocation = sourceLocation.orElse( null );
   }

   /**
    * @return The appropriate position in the source corresponding to the problem (if applicable)
    */
   public Optional<SourceLocation> getSourceLocation() {
      return Optional.ofNullable( sourceLocation );
   }

   private static String createErrorMessage( final String message, final SourceLocation sourceLocation ) {
      if ( sourceLocation == null ) {
         return message;
      }
      return message + " @" + sourceLocation;
   }

   @Override
   public String toString() {
      final String message = getLocalizedMessage() + getSourceLocation()
            .map( SourceLocation::toString )
            .map( sp -> " @" + sp )
            .orElse( " @[source position unknown]" );
      return getClass().getName() + ": " + message;
   }
}
