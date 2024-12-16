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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlCursorImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings( "java:S5976" ) // use parameterized tests => not for few instances which test content of an exception
class RqlOptionsTest {

   @Test
   void optionsWithBracketsAroundShouldLeadToSyntaxError() {
      final String expression = "   select=att1,att2,att3.subAtt4&filter=and(eq(att2,\"theSame\"),or(lt(att1,5),not(gt(att1,42))))&option="
            + "(limit(0,500),sort(+att1,-att2))";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "extraneous input '('" );
   }

   @Test
   void optionsWithoutCommaShouldLeadToSyntaxError() {
      final String expression = "select=id,name&filter=eq(id,\"47*\")&option=sort(+name,-description)limit(1,2)cursor(2)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "extraneous input 'limit'" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "select=id,name&filter=eq(id,\"47*\")&option=limit(1,2)&option=limit(5,7)",
         "select=id,name&option=cursor(\"abc\",10)&option=sort(+name,-description)",
         "select=id,name&option=sort(+name,-description)&option=limit(5,10)",
         "select=id,name&option=limit(5,10)&option=sort(+name,-description)",
         "select=id,name&option=sort(+name,-description)&option=limit(5,10)&option=cursor(\"abc\",10)"
   } )
   void shouldThrowDoubleOptionException( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one options statement allowed" );
   }

   @Test
   void shouldParseCursorWithSortOptions() {
      final String sortLimitExpression = "select=id,name&option=sort(+name,-description),cursor(\"abc\",10)";

      final RqlQueryModel sortLimitParseTree = RqlParser.from( sortLimitExpression );

      assertThat( sortLimitParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( sortLimitParseTree.getOptions().getCursor() ).contains( new RqlCursorImpl( "abc", 10 ) );
   }

   @Test
   void shouldParseCursorOptions() {
      final String sortLimitExpression = "select=id,name&option=cursor(\"abc\",10)";

      final RqlQueryModel sortLimitParseTree = RqlParser.from( sortLimitExpression );

      assertThat( sortLimitParseTree.getOptions().getCursor() ).contains( new RqlCursorImpl( "abc", 10 ) );
   }

   @Test
   void shouldParseCursorOptionsWithoutStart() {
      final String sortLimitExpression = "select=id,name&option=cursor(10)";

      final RqlQueryModel sortLimitParseTree = RqlParser.from( sortLimitExpression );

      assertThat( sortLimitParseTree.getOptions().getCursor() ).contains( new RqlCursorImpl( 10 ) );
   }

   @Test
   void shouldParseOptionsInAnyOrderCursorBeforeSort() {
      final String limitSortExpression = "select=id,name&option=cursor(\"abc\",10),sort(+name,-description)";

      final RqlQueryModel limitSortParseTree = RqlParser.from( limitSortExpression );

      assertThat( limitSortParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( limitSortParseTree.getOptions().getCursor() ).contains( new RqlCursorImpl( "abc", 10 ) );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "option=,cursor(\"abc\",10)",
         "option=,limit(100,10)",
         "option=,sort(+id)",
         "option=cursor(\"abc\",10)cursor(10)",
         "option=sort(+id)sort(+id)",
         "option=limit(100,10)limit(100,10)",
         "option=sort(+id)limit(100)",
         "option=limit(100,10)sort(+id)",
         "option=sort(+id)cursor(100)",
         "option=cursor(100)sort(+id)",
         "option=cursor(100),limit(10,100)sort(+id)"
   } )
   void shouldThrowMismatchInputWrongOptionSyntax( final String expression ) {
      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "option=cursor(100),limit(10,100)",
         "option=cursor(100),limit(10,100),sort(+id)",
   } )
   void shouldThrowInvalidExpressionSyntax( final String expression ) {
      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "Cursor and Limit cannot be used together" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "option=cursor(\"abc\",10),cursor(\"abc\",10)",
         "option=cursor(\"abc\",10),cursor(10)",
         "option=sort(+id),sort(+id)",
         "option=limit(100)sort(+id)",
         "option=limit(100,10),limit(100,10)",
         "option=limit(100,10),cursor(100,10)",
   } )
   void shouldThrowNoViableAlternativeOptionSyntax( final String expression ) {
      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "no viable alternative at input" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "option=sort(id)",
   } )
   void shouldNotThrowNoViableAlternativeOptionSyntax( final String expression ) {
      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );
      assertThat( throwable ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "missing Sign at 'id'" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "option=sort(+id),limit(10,10)",
         "option=limit(100, 10),sort(+id)",
         "option=sort(+id),cursor(100)",
         "option=cursor(100)",
         "option=sort(+id)",
         "option=limit(100, 10)",
         "option=cursor(\"abc\",10)",
         "option=cursor(100),sort(+id)",
         "option=sort(+id),cursor(100)",
   } )
   void shouldParseableSyntax( final String expression ) {
      final RqlQueryModel sortLimitParseTree = RqlParser.from( expression );

      assertThat( sortLimitParseTree.getOptions().isEmpty() ).isFalse();
   }

   @Test
   void shouldParseOptionsInAnyOrderSortBeforeLimit() {
      final String sortLimitExpression = "select=id,name&option=sort(+name,-description),limit(5,10)";

      final RqlQueryModel sortLimitParseTree = RqlParser.from( sortLimitExpression );

      assertThat( sortLimitParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( sortLimitParseTree.getOptions().getSlice() ).contains( new RqlSliceImpl( 5, 10 ) );
   }

   @Test
   void shouldParseOptionsInAnyOrderLimitBeforeSort() {
      final String limitSortExpression = "select=id,name&option=limit(5,10),sort(+name,-description)";

      final RqlQueryModel limitSortParseTree = RqlParser.from( limitSortExpression );

      assertThat( limitSortParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( limitSortParseTree.getOptions().getSlice() ).contains( new RqlSliceImpl( 5, 10 ) );
   }

   @Test
   void cursorNullValueShouldParseable() {
      final String limitSortExpression = "select=id,name&option=cursor(\"null\",10)";

      final RqlQueryModel limitSortParseTree = RqlParser.from( limitSortExpression );

      assertThat( limitSortParseTree.getOptions().getCursor() ).contains( new RqlCursorImpl( "null", 10 ) );
   }
}
