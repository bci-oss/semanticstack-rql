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

import com.boschsemanticstack.rql.model.v1.RqlFilter;

import org.assertj.core.api.AbstractObjectAssert;

public class RqlFilterAssert extends AbstractObjectAssert<RqlFilterAssert, RqlFilter> {

   protected RqlFilterAssert( RqlFilter rqlFilter ) {
      super( rqlFilter, RqlFilterAssert.class );
   }

   public static RqlFilterAssert assertThat( RqlFilter actual ) {
      return new RqlFilterAssert( actual );
   }

   public RqlFilterAssert hasFilterType( RqlFilter.FilterType filterType ) {
      isNotNull();
      if ( actual.getFilterType() != filterType ) {
         failWithMessage( "Expected filter type to be <%s> but was <%s>", filterType, actual.getFilterType() );
      }
      return this;
   }

   public RqlFilterAssert hasAttribute( String attribute ) {
      isNotNull();
      if ( !Objects.equals( actual.getAttribute(), attribute ) ) {
         failWithMessage( "Expected filter to contain attribute <%s> but was <%s>", attribute, actual.getAttribute() );
      }
      return this;
   }

   public RqlFilterAssert hasOperator( RqlFilter.Operator operator ) {
      isNotNull();
      if ( actual.getOperator() != operator ) {
         failWithMessage( "Expected filter operator to be <%s> but was <%s>", operator, actual.getOperator() );
      }
      return this;
   }

   public RqlFilterAssert hasNoChildren() {
      isNotNull();
      if ( actual.getChildCount() != 0 ) {
         failWithMessage( "Expected filter to have no children but was <%s>", actual.getChildCount() );
      }
      return this;
   }

   public RqlFilterAssert hasChildCount( int count ) {
      isNotNull();
      if ( actual.getChildCount() != count ) {
         failWithMessage( "Expected filter to have <%s> children but was <%s>", count, actual.getChildCount() );
      }
      return this;
   }

   public RqlFilterAssert getFirstChild() {
      return getChild( 0 );
   }

   public RqlFilterAssert getChild( int index ) {
      isNotNull();
      if ( index < 0 || index >= actual.getChildCount() ) {
         failWithMessage( "Expected filter to have child at index <%s> but was out of bounds", index );
      }
      return new RqlFilterAssert( actual.getChildren().get( index ) );
   }

   public RqlFilterAssert valueIsNull() {
      isNotNull();
      if ( actual.getValue() != null ) {
         failWithMessage( "Expected filter to contain null value but was <%s>", actual.getValue() );
      }
      return this;
   }

   public RqlFilterAssert valueIsEqualTo( Object value ) {
      isNotNull();
      if ( !Objects.equals( actual.getValue(), value ) ) {
         failWithMessage( "Expected filter to contain value <%s> but was <%s>", value, actual.getValue() );
      }
      return this;
   }

   public RqlFilterAssert valuesContainExactly( Object... values ) {
      isNotNull();
      if ( actual.getValues().size() != values.length ) {
         failWithMessage( "Expected filter to contain <%s> values but was <%s>", values.length, actual.getValues().size() );
      }
      for ( int i = 0; i < values.length; i++ ) {
         if ( !Objects.equals( actual.getValues().get( i ), values[i] ) ) {
            failWithMessage( "Expected filter to contain value <%s> at index <%d> but was <%s>", values[i], i, actual.getValues().get( i ) );
         }
      }
      return this;
   }
}
