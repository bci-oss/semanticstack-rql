/*
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH
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

import com.boschsemanticstack.rql.assertj.RqlFilterAssert;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

import org.junit.jupiter.api.Test;

class RqlParserPreProcessingTest {

   @Test
   void shouldThrowExceptionWhenModelIsNull() {
      assertThatThrownBy( () -> RqlParser.preProcessFilter( null ) )
            .isInstanceOf( IllegalArgumentException.class )
            .hasMessage( "Model must not be null for pre-processing." );
   }

   @Test
   void shouldNotChangeWhenNoFilterFound() {
      final String expression = "select=id,name";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .hasNoFilter();
   }

   @Test
   void shouldNotChangeNotMatchingFilterExpression() {

      final String expression = "filter=eq(parentId,null)";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .filter()
            .valueIsNull();
   }

   @Test
   void shouldSimplifyFilterWithAndNeAsNotInWhenTheAttributesAreTheSame() {
      final String expression = "filter=and(ne(xyz,\"a\"), ne(xyz,\"b\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.NOT )
            .hasChildCount( 1 )
            .getFirstChild()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.IN )
            .valuesContainExactly( "a", "b" );
   }

   @Test
   void shouldSimplifyFilterWithOrEqAsInWhenTheAttributesAreTheSame() {
      final String expression = "filter=or(eq(xyz,\"a\"), eq(xyz,\"b\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.IN )
            .valuesContainExactly( "a", "b" );
   }

   @Test
   void shouldSimplifyFilterWithNotNeAsEq() {
      final String expression = "filter=not(ne(xyz,\"a\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.EQ )
            .valueIsEqualTo( "a" );
   }

   @Test
   void shouldSimplifyFilterWithComplexExpression() {
      final String expression = "filter=or(not(ne(xyz,\"a\")), eq(xyz,\"b\"), eq(xyz,\"c\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.IN )
            .valuesContainExactly( "a", "b", "c" );
   }

   @Test
   void shouldNotSimplifyFilterWithOrEqExpressionWhenAttributesAreNotTheSame() {
      final String expression = "filter=or(eq(xyz,\"a\"), eq(abc,\"b\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      final RqlFilterAssert filterAssert = assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.OR )
            .hasChildCount( 2 );
      filterAssert.getFirstChild()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.EQ )
            .valuesContainExactly( "a" );
      filterAssert.getChild( 1 )
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "abc" )
            .hasOperator( RqlFilter.Operator.EQ )
            .valuesContainExactly( "b" );
   }

   @Test
   void shouldNotSimplifyFilterWithAndNeExpressionWhenAttributesAreNotTheSame() {
      final String expression = "filter=and(ne(xyz,\"a\"), ne(abc,\"b\"))";

      final RqlQueryModel parsedModel = RqlParser.from( expression );
      final RqlQueryModel model = RqlParser.preProcessFilter( parsedModel );

      final RqlFilterAssert filterAssert = assertThat( model )
            .filter()
            .hasFilterType( RqlFilter.FilterType.AND )
            .hasChildCount( 2 );
      filterAssert.getFirstChild()
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "xyz" )
            .hasOperator( RqlFilter.Operator.NE )
            .valuesContainExactly( "a" );
      filterAssert.getChild( 1 )
            .hasFilterType( RqlFilter.FilterType.VALUE )
            .hasAttribute( "abc" )
            .hasOperator( RqlFilter.Operator.NE )
            .valuesContainExactly( "b" );
   }
}
