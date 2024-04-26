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

import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;

public record RqlFieldDirectionImpl(String attribute, Direction direction) implements RqlFieldDirection {

   public RqlFieldDirectionImpl {
      if ( attribute == null || direction == null ) {
         throw new IllegalArgumentException( "Neither attribute nor direction may be null!" );
      }
   }

   /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return attribute + (direction == Direction.ASCENDING ? " ASC" : " DESC");
   }

   @Override
   public boolean equals( final Object o ) {
      if ( this == o ) {
         return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
         return false;
      }

      final RqlFieldDirectionImpl that = (RqlFieldDirectionImpl) o;

      if ( !attribute().equals( that.attribute() ) ) {
         return false;
      }
      return direction() == that.direction();
   }

   @Override
   public int hashCode() {
      int result = attribute().hashCode();
      result = 31 * result + direction().hashCode();
      return result;
   }
}
