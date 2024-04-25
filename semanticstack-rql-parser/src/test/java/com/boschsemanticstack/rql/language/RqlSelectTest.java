/*
 * Copyright (c)2024 Robert Bosch Manufacturing Solutions GmbH
 *
 *  See the AUTHORS file(s) distributed with this work for additional
 *  information regarding authorship.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *  SPDX-License-Identifier: MPL-2.0
 */

package com.boschsemanticstack.rql.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import org.junit.jupiter.api.Test;

class RqlSelectTest {

   @Test
   void select_withRegularSyntax_shouldBeParsable() {
      final String queryString = RqlParser.toString( RqlParser.builder().select( "abc", "def/ghi", "jk.lmnop" ).build() );
      final RqlQueryModel parsedQueryModel = RqlParser.from( queryString );
      assertThat( parsedQueryModel.getSelect().attributes() ).containsExactly( "abc", "def/ghi", "jk.lmnop" );
   }

   @Test
   void select_withMultipleParameters_shouldBeParsable() {
      final String queryString = "select=abc&select=def/ghi,jk.lmnop";
      try {
         RqlParser.from( queryString );
         fail();
      } catch ( final ParseException e ) {
         assertThat( e ).hasMessageStartingWith( "No more than one select statement allowed" );
      }
   }

   @Test
   void select_withParameterMultiValue_shouldNotBeParsable() {
      final String queryString = "select=abc,def/ghi,jk.lmnop";
      final RqlQueryModel parsedQueryModel = RqlParser.from( queryString );
      assertThat( parsedQueryModel.getSelect().attributes() ).containsExactly( "abc", "def/ghi", "jk.lmnop" );
   }

   @Test
   void select_Syntax_shouldBeParsable() {
      final String queryString = RqlParser.toString( RqlParser.builder().select( "abc", "def/ghi", "jk.lmnop" ).build() );
      final RqlQueryModel parsedQueryModel = RqlParser.from( queryString );
      assertThat( parsedQueryModel.getSelect().attributes() ).containsExactly( "abc", "def/ghi", "jk.lmnop" );
   }
}
