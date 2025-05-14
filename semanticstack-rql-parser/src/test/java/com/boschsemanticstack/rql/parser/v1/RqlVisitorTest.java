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

package com.boschsemanticstack.rql.parser.v1;

import static com.boschsemanticstack.rql.assertj.RqlQueryModelAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

import org.junit.jupiter.api.Test;

class RqlVisitorTest {

   @Test
   void first() {
      final String expression = "select=id,name,nameWithSome.Extra1_9Chars.";
      final RqlQueryModel model = new RqlParserApi().parseFullQuery( expression );

      assertThat( model )
            .select()
            .attributesContainExactly( "id", "name", "nameWithSome.Extra1_9Chars." );
   }

   @Test
   void firstNotParseable() {
      final String expression = "select=id,name,nameWithSome/Extra1_9Chars.";
      final RqlParserApi rqlParserApi = new RqlParserApi();

      assertThatThrownBy( () -> rqlParserApi.parseFullQuery( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParsePlus() {
      final String illegalPrefix = "+";

      validateParseFails( illegalPrefix, "extraneous input '+' expecting FieldIdentifier" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParseMinus() {
      final String illegalPrefix = "-";

      validateParseFails( illegalPrefix, "extraneous input '-' expecting FieldIdentifier" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParseBackslash() {
      final String illegalPrefix = "\\";

      validateParseFails( illegalPrefix, "token recognition error at: '\\'" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParseSlash() {
      final String illegalPrefix = "/";

      validateParseFails( illegalPrefix, "token recognition error at: '/'" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParseDot() {
      final String illegalPrefix = ".";

      validateParseFails( illegalPrefix, "token recognition error at: '.'" );
   }

   @Test
   void selectWithInvalidStartingCharsTestShouldNotParseNonAscii() {
      final String illegalPrefix = "ó";

      validateParseFails( illegalPrefix, "token recognition error at: 'ó'" );
   }

   private void validateParseFails( final String illegalPrefix, final String messageContaining ) {
      final String expression = "select=id," + illegalPrefix + "name";
      final RqlParserApi rqlParserApi = new RqlParserApi();

      assertThatThrownBy( () -> rqlParserApi.parseFullQuery( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( messageContaining );
   }
}
