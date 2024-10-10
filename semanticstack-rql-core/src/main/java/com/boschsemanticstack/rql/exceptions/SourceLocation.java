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

import java.io.Serializable;

public class SourceLocation implements Serializable {
 
   private final int line;
   private final int column;

   public SourceLocation( final int lineOneBased, final int columnOneBased ) {
      line = lineOneBased;
      column = columnOneBased;
   }

   /**
    * @return 1-bases index of the input line
    */
   public int getLine() {
      return line;
   }

   /**
    * @return 1-bases index of the input column
    */
   public int getColumn() {
      return column;
   }

   @Override
   public String toString() {
      return "[line:" + line +
            ", column:" + column + "]";
   }

   @Override
   public boolean equals( final Object o ) {
      if ( this == o ) {
         return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
         return false;
      }

      final SourceLocation that = (SourceLocation) o;

      return line == that.line && column == that.column;
   }

   @Override
   public int hashCode() {
      int result = line;
      result = 31 * result + column;
      return result;
   }
}
