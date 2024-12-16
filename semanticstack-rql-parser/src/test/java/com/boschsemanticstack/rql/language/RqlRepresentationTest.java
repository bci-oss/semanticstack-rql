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
import static org.assertj.core.api.Assertions.tuple;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RqlRepresentationTest {

   @Test
   void queryWithSlashAttributeShouldBeNotParseable() {
      final String expression = " option=sort(+att1,-att2)"
            + "&filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))"
            + "&select=att1,att2,att3/subAtt4"
            + "&option=limit(5,500)";

      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }

   @Test
   void queryWithRandomizedDuplicateOptionShouldNotBeParsable() {
      final String expression = " option=sort(+att1,-att2)"
            + "&filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))"
            + "&select=att1,att2,att3.subAtt4"
            + "&option=limit(5,500)";

      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one options statement allowed" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),limit(5,500)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=limit(5,500),sort(+att1,-att2)"
   } )
   void queryWithRandomizedOrderShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      assertThat( query.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            );
      assertThat( query.getOptions().getSlice().get().limit() ).isEqualTo( 500 );
      assertThat( query.getOptions().getSlice().get().offset() ).isEqualTo( 5 );
      assertThat( query.getSelect().attributes() )
            .containsExactly( "att1", "att2", "att3.subAtt4" );

      assertThat( query.getFilter().get().getChildren() ).extracting( RqlFilter::getOperator,
                  RqlFilter::getAttribute, RqlFilter::getValue )
            .containsExactly(
                  tuple( RqlFilter.Operator.EQ, "att2", "theSame" ), tuple( RqlFilter.Operator.LT, "att1", 5 ),
                  tuple( RqlFilter.Operator.GT, "att1", 42 ) );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),cursor(500)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(500),sort(+att1,-att2)"
   } )
   void queryWithRandomizedOrderAndCursorShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      assertThat( query.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            );
      assertThat( query.getOptions().getCursor().get().limit() ).isEqualTo( 500 );
      assertThat( query.getSelect().attributes() )
            .containsExactly( "att1", "att2", "att3.subAtt4" );

      assertThat( query.getFilter().get().getChildren() ).extracting( RqlFilter::getOperator,
                  RqlFilter::getAttribute, RqlFilter::getValue )
            .containsExactly(
                  tuple( RqlFilter.Operator.EQ, "att2", "theSame" ),
                  tuple( RqlFilter.Operator.LT, "att1", 5 ),
                  tuple( RqlFilter.Operator.GT, "att1", 42 ) );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),cursor(\"a\",500)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(\"a\",500),sort(+att1,-att2)"
   } )
   void queryWithRandomizedOrderAndCursorStartShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      assertThat( query.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            );
      assertThat( query.getOptions().getCursor().get().limit() ).isEqualTo( 500 );
      assertThat( query.getOptions().getCursor().get().cursor() ).contains( "a" );
      assertThat( query.getSelect().attributes() )
            .containsExactly( "att1", "att2", "att3.subAtt4" );

      assertThat( query.getFilter().get().getChildren() ).extracting( RqlFilter::getOperator,
                  RqlFilter::getAttribute, RqlFilter::getValue )
            .containsExactly(
                  tuple( RqlFilter.Operator.EQ, "att2", "theSame" ),
                  tuple( RqlFilter.Operator.LT, "att1", 5 ),
                  tuple( RqlFilter.Operator.GT, "att1", 42 ) );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),cursor(\"a\","
               + "500),limit(100,0)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),limit(100,0),"
               + "cursor(\"a\",500)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),cursor(\"a\","
               + "500),limit(100,0)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=sort(+att1,-att2),limit(100,0),"
               + "cursor(500)"
   } )
   void queryWithRandomizedOrderThrowInvalidExpressionExcpetion( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "extraneous input ',' expecting {<EOF>, '&'}" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(\"a\",500),sort(+att1,"
               + "-att2),limit(100,0)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(0),limit(10,100),"
               + "sort(+att1,-att2)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(\"a\",500),limit(10,100),"
               + "sort(+att1,-att2)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=limit(100,0),cursor(500),sort"
               + "(+att1,-att2)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=limit(100,0),cursor(\"a\",500),"
               + "sort(+att1,-att2)"
   } )
   void queryWithRandomizedOrderThrowCursorInvalidExpression( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "Cursor and Limit cannot be used together" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(\"a\",500),cursor(\"a\",0)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=cursor(0),cursor(500),sort"
               + "(+att1,-att2)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=limit(10,500),limit(10,10)",
         "filter=and(eq(att2,\"theSame\"),lt(att1,5),gt(att1,42))&select=att1,att2,att3.subAtt4&option=limit(10,500),limit(10,10),sort"
               + "(+att1,-att2)"
   } )
   void queryWithRandomizedOrderThrowExtraneousInputExcpetion( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "no viable alternative at input" );
   }

   @Test
   void shouldParseNewSyntaxWithSelectEqualsWithoutParens() {
      // GIVEN
      final String expression = "select=id,name&filter=eq(id,\"4711\")&option=sort(+name,-description),limit(5,10)";

      // WHEN
      final RqlQueryModel model = RqlParser.from( expression );

      // THEN
      assertThat( model.getSelect().attributes() ).containsExactly( "id", "name" );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getValues() ).containsExactly( "4711" );

      assertThat( model.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );
      assertThat( model.getOptions().getSlice() ).contains( new RqlSliceImpl( 5, 10 ) );
   }
}
