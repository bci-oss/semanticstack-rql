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

import com.boschsemanticstack.rql.assertj.RqlFilterAssert;
import com.boschsemanticstack.rql.assertj.RqlQueryModelAssert;
import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlCursorImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RqlRepresentationTest {

   @Test
   void queryWithSlashAttributeShouldBeNotParseable() {
      final String expression = """
             option=sort(+att1,-att2)\
            &filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
            &select=att1,att2,att3/subAtt4\
            &option=limit(5,500)""";

      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '/'" );
   }

   @Test
   void queryWithRandomizedDuplicateOptionShouldNotBeParsable() {
      final String expression = """
             option=sort(+att1,-att2)\
            &filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
            &select=att1,att2,att3.subAtt4\
            &option=limit(5,500)""";

      assertThatThrownBy( () -> RqlParser.from( expression ) ).isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one options statement allowed" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),limit(5,500)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=limit(5,500),sort(+att1,-att2)"""
   } )
   void queryWithRandomizedOrderShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      final RqlQueryModelAssert modelAssert = assertThat( query );
      modelAssert.options()
            .orderContainsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            )
            .hasLimit( 500 )
            .hasOffset( 5 );
      modelAssert.select()
            .attributesContainExactly( "att1", "att2", "att3.subAtt4" );
      final RqlFilterAssert filterAssert = modelAssert.filter()
            .hasChildCount( 3 );
      filterAssert.getFirstChild()
            .hasOperator( RqlFilter.Operator.EQ )
            .hasAttribute( "att2" )
            .valueIsEqualTo( "theSame" );
      filterAssert.getChild( 1 )
            .hasOperator( RqlFilter.Operator.LT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 5 );
      filterAssert.getChild( 2 )
            .hasOperator( RqlFilter.Operator.GT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 42 );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),cursor(500)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor(500),sort(+att1,-att2)"""
   } )
   void queryWithRandomizedOrderAndCursorShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      final RqlQueryModelAssert modelAssert = assertThat( query );
      modelAssert.options()
            .orderContainsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            )
            .containsCursor( new RqlCursorImpl( 500 ) );
      modelAssert.select()
            .attributesContainExactly( "att1", "att2", "att3.subAtt4" );
      final RqlFilterAssert filterAssert = modelAssert.filter()
            .hasChildCount( 3 );
      filterAssert.getFirstChild()
            .hasOperator( RqlFilter.Operator.EQ )
            .hasAttribute( "att2" )
            .valueIsEqualTo( "theSame" );
      filterAssert.getChild( 1 )
            .hasOperator( RqlFilter.Operator.LT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 5 );
      filterAssert.getChild( 2 )
            .hasOperator( RqlFilter.Operator.GT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 42 );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),cursor("a",500)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor("a",500),sort(+att1,-att2)"""
   } )
   void queryWithRandomizedOrderAndCursorStartShouldBeParsable( final String expression ) {
      final RqlQueryModel query = RqlParser.from( expression );

      final RqlQueryModelAssert modelAssert = assertThat( query );
      modelAssert.options()
            .orderContainsExactly(
                  new RqlFieldDirectionImpl( "att1", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "att2", RqlFieldDirection.Direction.DESCENDING )
            )
            .containsCursor( new RqlCursorImpl( "a", 500 ) );
      modelAssert.select()
            .attributesContainExactly( "att1", "att2", "att3.subAtt4" );
      final RqlFilterAssert filterAssert = modelAssert.filter()
            .hasChildCount( 3 );
      filterAssert.getFirstChild()
            .hasOperator( RqlFilter.Operator.EQ )
            .hasAttribute( "att2" )
            .valueIsEqualTo( "theSame" );
      filterAssert.getChild( 1 )
            .hasOperator( RqlFilter.Operator.LT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 5 );
      filterAssert.getChild( 2 )
            .hasOperator( RqlFilter.Operator.GT )
            .hasAttribute( "att1" )
            .valueIsEqualTo( 42 );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor("a",500),sort(+att1,-att2),limit(100,0)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),cursor("a",500),limit(100,0)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),limit(100,0),cursor("a",500)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),cursor("a",500),limit(100,0)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=sort(+att1,-att2),limit(100,0),cursor(500)"""
   } )
   void shouldThrowExtraneousInputOptionSyntax( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "extraneous input ',' expecting {<EOF>, '&'}" )
            .hasMessageContaining( "@[line:1" )
            .hasMessageContaining( "column" );
   }

   @ParameterizedTest
   @ValueSource( strings = {
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor("a",500),cursor("a",0)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor(0),cursor(500),sort(+att1,-att2)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=limit(10,500),limit(10,10)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=limit(10,500),limit(10,10),sort(+att1,-att2)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor(0),limit(10,100),sort(+att1,-att2)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=cursor("a",500),limit(10,100),sort(+att1,-att2)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=limit(100,0),cursor(500),sort(+att1,-att2)""",
         """
                filter=and(eq(att2,"theSame"),lt(att1,5),gt(att1,42))\
               &select=att1,att2,att3.subAtt4\
               &option=limit(100,0),cursor("a",500),sort(+att1,-att2)"""
   } )
   void shouldThrowMismatchInputWrongOptionSyntax( final String expression ) {
      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input" )
            .hasMessageContaining( "@[line:1" )
            .hasMessageContaining( "column" );
   }

   @Test
   void shouldParseNewSyntaxWithSelectEqualsWithoutParens() {
      // GIVEN
      final String expression = """
             select=id,name\
            &filter=eq(id,"4711")\
            &option=sort(+name,-description),limit(5,10)""";

      // WHEN
      final RqlQueryModel model = RqlParser.from( expression );

      // THEN
      final RqlQueryModelAssert modelAssert = assertThat( model );
      modelAssert.select()
            .attributesContainExactly( "id", "name" );
      modelAssert.filter()
            .hasOperator( RqlFilter.Operator.EQ )
            .valueIsEqualTo( "4711" );
      modelAssert.options()
            .orderContainsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            )
            .hasOffset( 5 )
            .hasLimit( 10 );
   }
}
