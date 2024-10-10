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

import com.boschsemanticstack.rql.model.v1.RqlModelVisitor;
import com.boschsemanticstack.rql.model.v1.RqlSlice;

public record RqlSliceImpl( long offset, long limit ) implements RqlSlice {

   @Override
   public <T> T accept( final RqlModelVisitor<? extends T> visitor ) {
      return visitor.visitSlice( this );
   }

   @Override
   public String toString() {
      return "Offset=" + offset + ", limit=" + limit;
   }
 
   @Override
   public boolean equals( final Object o ) {
      if ( this == o ) {
         return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
         return false;
      }

      final RqlSliceImpl slice = (RqlSliceImpl) o;

      if ( offset() != slice.offset() ) {
         return false;
      }
      return limit() == slice.limit();
   }

   @Override
   public int hashCode() {
      int result = (int) ( offset() ^ ( offset() >>> 32 ) );
      result = 31 * result + (int) ( limit() ^ ( limit() >>> 32 ) );
      return result;
   }
}
