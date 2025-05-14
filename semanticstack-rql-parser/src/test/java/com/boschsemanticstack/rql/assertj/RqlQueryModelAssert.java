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

import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;

public class RqlQueryModelAssert extends AbstractAssert<RqlQueryModelAssert, RqlQueryModel> {

   protected RqlQueryModelAssert( RqlQueryModel actual ) {
      super( actual, RqlQueryModelAssert.class );
   }

   public static RqlQueryModelAssert assertThat( RqlQueryModel actual ) {
      return new RqlQueryModelAssert( actual );
   }

   public RqlQueryModelAssert isEmpty() {
      isNotNull();
      if ( !actual.isEmpty() ) {
         failWithMessage( "Expected query model to be empty but was <%s>", actual );
      }
      return this;
   }

   public RqlQueryModelAssert isNotEmpty() {
      isNotNull();
      if ( actual.isEmpty() ) {
         failWithMessage( "Expected query model to not be empty but was" );
      }
      return this;
   }

   public RqlQueryModelAssert hasNoFilter() {
      isNotNull();
      if ( actual.getFilter().isPresent() ) {
         failWithMessage( "Expected no filter but was <%s>", actual.getFilter().get() );
      }
      return this;
   }

   public RqlFilterAssert filter() {
      isNotNull();
      if ( actual.getFilter().isPresent() ) {
         return RqlFilterAssert.assertThat( actual.getFilter().get() );
      } else {
         failWithMessage( "Expected filter to be present but was not" );
         return null; // This line will never be reached due to the exception thrown above
      }
   }

   public RqlOptionsAssert options() {
      isNotNull();
      return RqlOptionsAssert.assertThat( actual.getOptions() );
   }

   public RqlQueryModelAssert hasNoOptions() {
      isNotNull();
      if ( !actual.getOptions().isEmpty() ) {
         failWithMessage( "Expected no options but was <%s>", actual.getOptions() );
      }
      return this;
   }

   public RqlSelectAssert select() {
      isNotNull();
      if ( !actual.getSelect().isEmpty() ) {
         return RqlSelectAssert.assertThat( actual.getSelect() );
      } else {
         failWithMessage( "Expected select to be present but was not" );
         return null; // This line will never be reached due to the exception thrown above
      }
   }

   public RqlQueryModelAssert hasNoSelect() {
      isNotNull();
      if ( !actual.getSelect().isEmpty() ) {
         failWithMessage( "Expected no select but was <%s>", actual.getSelect() );
      }
      return this;
   }

   public StringAssert queryString() {
      isNotNull();
      return new StringAssert( RqlParser.toString( actual ) );
   }
}
