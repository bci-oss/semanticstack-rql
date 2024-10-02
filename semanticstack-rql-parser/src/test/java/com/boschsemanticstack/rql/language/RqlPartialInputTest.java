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

package com.boschsemanticstack.rql.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;

class RqlPartialInputTest {

   @Test
   void nullInputShouldThrowParseException() {
      final String expression = null;

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "Input was null" );
   }
 
   @Test
   void emptyInputShouldBeDetectable() {
      final String expression = "";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.isEmpty() )
            .describedAs( "Parsing empty string should lead to an empty model." )
            .isTrue();
   }

   @Test
   void selectOnlyShouldBeParseable() {
      final String expression = "select=id,name";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter() )
            .describedAs( "Parsing select only should lead to an empty filter." )
            .isEmpty();
      assertThat( model.getOptions().isEmpty() )
            .describedAs( "Parsing select only string should lead to empty options." )
            .isTrue();
      assertThat( model.getSelect().attributes() ).containsExactly( "id", "name" );
   }
}
