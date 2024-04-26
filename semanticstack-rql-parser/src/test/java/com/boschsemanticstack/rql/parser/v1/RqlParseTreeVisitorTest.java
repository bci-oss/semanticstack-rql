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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

class RqlParseTreeVisitorTest {

   @Test
   void selectOnlyShouldBeParseable() {
      final String expression = "select=id,name";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getSelect().attributes() ).containsExactly( "id", "name" );
   }

   @Test
   void shouldParseNullLiteral() {
      final String attribute = "parentId";
      final String value = null;

      final String expression = "filter=eq(" + attribute + "," + value + ")";

      checkParseResult( expression, null );
   }

   @Test
   void shouldParseNotIn() {
      final String expression = "filter=not(in(xyz,\"a\", \"b\"))";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getFilterType() ).isEqualTo( RqlFilter.FilterType.NOT );
      assertThat( model.getFilter().get().getChildren() ).hasSize( 1 );

      final RqlFilter inFilter = model.getFilter().get().getChildren().get( 0 );
      assertThat( inFilter.getFilterType() ).isEqualTo( RqlFilter.FilterType.VALUE );
      assertThat( inFilter.getOperator() ).isEqualTo( RqlFilter.Operator.IN );
      assertThat( inFilter.getValues() ).containsExactly( "a", "b" );
   }

   @Test
   void shouldParseEqualsNull() {
      final String expression = "filter=eq(xyz,null)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getFilterType() ).isEqualTo( RqlFilter.FilterType.VALUE );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getValues() ).isNotNull();
      assertThat( model.getFilter().get().getValues() ).describedAs( model.getFilter().toString() )
            .hasSize( 1 )
            .containsNull();
   }

   @Test
   void shouldParseNotEqualsNull() {
      final String expression = "filter=ne(xyz,null)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getFilterType() ).isEqualTo( RqlFilter.FilterType.VALUE );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.NE );
      assertThat( model.getFilter().get().getValues() ).isNotNull();
      assertThat( model.getFilter().get().getValues() ).describedAs( model.getFilter().toString() )
            .hasSize( 1 )
            .containsNull();
   }

   @Test
   void shouldParseFilterOnlyWithSpecialCharsSequence() {
      final String expression = "filter=eq(id,\"47!§$%&/()=?`´*+~'#°^.:,;-_ë11\")";

      checkParseResult( expression, "47!§$%&/()=?`´*+~'#°^.:,;-_ë11" );
   }

   @Test
   void shouldParseFilterOnlyWithUnicodeCharsSequence() {
      final String expression = "filter=eq(id,\"世界您好!\")";

      checkParseResult( expression, "世界您好!" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapeSequence() {
      final String expression = "filter=eq(id,\"47\\\"1\\\\1\")";

      checkParseResult( expression, "47\"1\\1" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedBackslash() {
      final String expression = "filter=eq(id,\"SingleBackslash\\\\\")";

      checkParseResult( expression, "SingleBackslash\\" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedTab() {
      final String expression = "filter=eq(id,\"Single\\tTab\")";

      checkParseResult( expression, "Single\tTab" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedNewline() {
      final String expression = "filter=eq(id,\"Single\\nNewLine\")";

      checkParseResult( expression, "Single\nNewLine" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedCarriageReturn() {
      final String expression = "filter=eq(id,\"Single\\rCarriageReturn\")";

      checkParseResult( expression, "Single\rCarriageReturn" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedDoubleQuote() {
      final String expression = "filter=eq(id,\"Single\\\"DoubleQuote\")";

      checkParseResult( expression, "Single\"DoubleQuote" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedBackspace() {
      final String expression = "filter=eq(id,\"Single\\bBackspace\")";

      checkParseResult( expression, "Single\bBackspace" );
   }

   @Test
   void shouldParseFilterOnlyWithEscapedFormFeed() {
      final String expression = "filter=eq(id,\"Single\\fFormFeed\")";

      checkParseResult( expression, "Single\fFormFeed" );
   }

   private void checkParseResult( final String expression, final String expected ) {
      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getValue() )
            .describedAs( "Input '%s' should be parsed as '%s'", expression, expected )
            .isEqualTo( expected );
   }

   @Test
   void shouldParseInFilterWithOnlyFloats() {
      final String expression = "filter=in(id,4.6,7.8,9.0)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getValues() )
            .containsExactlyInAnyOrder( 4.6, 7.8, 9.0 );
   }

   @Test
   void shouldParseInFilterWithOnlyInts() {
      final String expression = "filter=in(id,4,6,7,8,9,0)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getValues() )
            .containsExactlyInAnyOrder( 4, 6, 7, 8, 9, 0 );
   }

   @Test
   void shouldParseInFilterWithOnlyStrings() {
      final String expression = "filter=in(id,\"A\",\"B\",\"C\")";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getValues() )
            .containsExactlyInAnyOrder( "A", "B", "C" );
   }

   @Test
   void shouldFailParsingInFilterWithNullAnywhere() {
      final String expression = "filter=in(id,4.6,null,9.0)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input 'null' expecting FloatLiteral" );
   }

   @Test
   void shouldFailParsingInFilterWithMixedStringAndNumber() {
      final String expression = "filter=in(id,4.6,\"foo\",9.0)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input '\"foo\"' expecting FloatLiteral" );
   }
}
