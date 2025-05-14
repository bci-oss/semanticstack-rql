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

import com.boschsemanticstack.rql.model.v1.RqlSelect;

import org.assertj.core.api.AbstractAssert;

public class RqlSelectAssert extends AbstractAssert<RqlSelectAssert, RqlSelect> {

   protected RqlSelectAssert( RqlSelect rqlSelect ) {
      super( rqlSelect, RqlSelectAssert.class );
   }

   public static RqlSelectAssert assertThat( RqlSelect actual ) {
      return new RqlSelectAssert( actual );
   }

   public RqlSelectAssert attributesContainExactly( String... attributes ) {
      isNotNull();
      if ( actual.attributes().size() != attributes.length ) {
         failWithMessage( "Expected <%s> attributes but was <%s>", attributes.length, actual.attributes().size() );
      }
      for ( int i = 0; i < attributes.length; i++ ) {
         if ( !Objects.equals( actual.attributes().get( i ), attributes[i] ) ) {
            failWithMessage( "Expected filter to contain attribute <%s> at index <%d> but was <%s>", attributes[i], i, actual.attributes().get( i ) );
         }
      }
      return this;
   }
}
