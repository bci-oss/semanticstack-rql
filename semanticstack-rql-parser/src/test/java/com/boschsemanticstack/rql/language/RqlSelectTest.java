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
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;

class RqlSelectTest {

   @Test
   void select_withRegularSyntax_shouldBeNotParsable() {
      final String queryString = RqlParser.toString(
            RqlParser.builder().select( "abc", "def/ghi", "jk.lmnop" ).build() );
      assertThatThrownBy( () -> RqlParser.from( queryString ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }

   @Test
   void select_withRegularSyntax_shouldBeParsable() {
      final String queryString = RqlParser.toString(
            RqlParser.builder().select( "abc", "def.ghi", "jk.lmnop" ).build() );
      final RqlQueryModel parsedQueryModel = RqlParser.from( queryString );
      assertThat( parsedQueryModel )
            .select()
            .attributesContainExactly( "abc", "def.ghi", "jk.lmnop" );
   }

   @Test
   void select_withMultipleSelects_shouldNotBeParsable() {
      final String queryString = "select=abc&select=def.ghi,jk.lmnop";
      assertThatThrownBy( () -> RqlParser.from( queryString ) )
            .isInstanceOf( ParseException.class )
            .hasMessageStartingWith( "No more than one select statement allowed" );
   }

   @Test
   void select_withParameterMultiValue_shouldNotBeParsable() {
      final String queryString = "select=abc,def/ghi,jk.lmnop";
      assertThatThrownBy( () -> RqlParser.from( queryString ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }

   @Test
   void select_withParameterMultiValue_shouldBeParsable() {
      final String queryString = "select=abc,def.ghi,jk.lmnop";
      final RqlQueryModel parsedQueryModel = RqlParser.from( queryString );
      assertThat( parsedQueryModel )
            .select()
            .attributesContainExactly( "abc", "def.ghi", "jk.lmnop" );
   }

   @Test
   void select_Syntax_shouldBeNotParsable() {
      final String queryString = RqlParser.toString(
            RqlParser.builder().select( "abc", "def.ghi", "jk/lmnop" ).build() );
      assertThatThrownBy( () -> RqlParser.from( queryString ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }
}
