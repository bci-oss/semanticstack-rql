/*
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH
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

package com.boschsemanticstack.rql.assertj;

import java.util.Objects;

import com.boschsemanticstack.rql.model.v1.RqlOptions;
import com.boschsemanticstack.rql.model.v1.impl.RqlCursorImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;

import org.assertj.core.api.AbstractAssert;

public class RqlOptionsAssert extends AbstractAssert<RqlOptionsAssert, RqlOptions> {

   protected RqlOptionsAssert( RqlOptions rqlOptions ) {
      super( rqlOptions, RqlOptionsAssert.class );
   }

   public static RqlOptionsAssert assertThat( RqlOptions actual ) {
      return new RqlOptionsAssert( actual );
   }

   public RqlOptionsAssert hasNoSlice() {
      isNotNull();
      if ( actual.getSlice().isPresent() ) {
         failWithMessage( "Expected no slice but was <%s>", actual.getSlice().get() );
      }
      return this;
   }

   public RqlOptionsAssert hasLimit( int limit ) {
      isNotNull();
      if ( actual.getSlice().isPresent() && actual.getSlice().get().limit() != limit ) {
         failWithMessage( "Expected limit <%s> but was <%s>", limit, actual.getSlice().get().limit() );
      }
      return this;
   }

   public RqlOptionsAssert hasOffset( int offset ) {
      isNotNull();
      if ( actual.getSlice().isPresent() && actual.getSlice().get().offset() != offset ) {
         failWithMessage( "Expected offset <%s> but was <%s>", offset, actual.getSlice().get().offset() );
      }
      return this;
   }

   public RqlOptionsAssert hasNoOrder() {
      isNotNull();
      if ( !actual.getOrder().isEmpty() ) {
         failWithMessage( "Expected no order but was <%s>", actual.getOrder() );
      }
      return this;
   }

   public RqlOptionsAssert isNotEmpty() {
      isNotNull();
      if ( actual.isEmpty() ) {
         failWithMessage( "Expected options to not be empty but was" );
      }
      return this;
   }

   public RqlOptionsAssert containsCursor( RqlCursorImpl rqlCursor ) {
      isNotEmpty();
      if ( actual.getCursor().isEmpty() ) {
         failWithMessage( "Expected options to contain a cursor but was empty" );
      }
      if ( !actual.getCursor().get().equals( rqlCursor ) ) {
         failWithMessage( "Expected options to contain cursor <%s> but was <%s>", rqlCursor, actual.getCursor().get() );
      }
      return this;
   }

   public RqlOptionsAssert orderContainsExactly( RqlFieldDirectionImpl... fieldDirection ) {
      isNotEmpty();
      if ( actual.getOrder().isEmpty() ) {
         failWithMessage( "Expected options to contain order but was empty" );
      }
      if ( actual.getOrder().fieldDirections().size() != fieldDirection.length ) {
         failWithMessage( "Expected options to contain order of size <%d> but was <%d>", fieldDirection.length, actual.getOrder().fieldDirections().size() );
      }
      for ( int i = 0; i < fieldDirection.length; i++ ) {
         if ( !Objects.equals( actual.getOrder().fieldDirections().get( i ), fieldDirection[i] ) ) {
            failWithMessage( "Expected options to contain order <%s> at index <%d> but was <%s>", fieldDirection[i], i,
                  actual.getOrder().fieldDirections().get( i ) );
         }
      }
      return this;
   }
}
