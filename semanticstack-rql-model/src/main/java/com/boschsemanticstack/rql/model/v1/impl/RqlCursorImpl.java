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

package com.boschsemanticstack.rql.model.v1.impl;

import java.util.Objects;
import java.util.Optional;

import com.boschsemanticstack.rql.model.v1.RqlCursor;

/**
 * RqlCursorImpl present the cursor pagination
 *
 * @param cursor the optional cursor when empty then start from begin
 * @param limit the limit of the result
 */
public record RqlCursorImpl( Optional<String> cursor, long limit ) implements RqlCursor {

   public RqlCursorImpl( final String cursor, final long limit ) {
      this( Optional.of( cursor ), limit );
   }

   public RqlCursorImpl( final long limit ) {
      this( Optional.empty(), limit );
   }

   @Override
   public String toString() {
      return "RqlCursorImpl{"
            + "cursor=" + cursor
            + ", limit=" + limit
            + '}';
   }

   @Override
   public boolean equals( final Object o ) {
      if ( this == o ) {
         return true;
      }
      if ( !( o instanceof final RqlCursorImpl rqlCursor ) ) {
         return false;
      }
      return limit == rqlCursor.limit && Objects.equals( cursor, rqlCursor.cursor );
   }

   @Override
   public int hashCode() {
      return Objects.hash( cursor, limit );
   }
}
