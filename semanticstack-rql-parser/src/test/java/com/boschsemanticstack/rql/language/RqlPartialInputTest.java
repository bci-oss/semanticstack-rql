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

import static com.boschsemanticstack.rql.assertj.RqlQueryModelAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlCursorImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;

class RqlPartialInputTest {

   @Test
   void nullInputShouldThrowParseException() {
      final String expression = null;

      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "Input was null" );
   }

   @Test
   void emptyInputShouldBeDetectable() {
      final String expression = "";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model )
            .describedAs( "Parsing empty string should lead to an empty model." )
            .isEmpty();
   }

   @Test
   void selectOnlyShouldBeParseable() {
      final String expression = "select=id,name";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model )
            .describedAs( "Parsing select only should lead to an empty filter." )
            .hasNoFilter()
            .describedAs( "Parsing select only string should lead to empty options." )
            .hasNoOptions()
            .select()
            .attributesContainExactly( "id", "name" );
   }

   @Test
   void optionsOnlyShouldBeParseable() {
      final String expression = "option=cursor(500)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model )
            .hasNoFilter()
            .hasNoSelect()
            .options()
            .containsCursor( new RqlCursorImpl( 500 ) );
   }
}
