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
import static org.assertj.core.api.Assertions.catchThrowable;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.impl.RqlFieldDirectionImpl;
import com.boschsemanticstack.rql.model.v1.impl.RqlSliceImpl;
import com.boschsemanticstack.rql.parser.v1.RqlParser;
import org.junit.jupiter.api.Test;

@SuppressWarnings( "java:S5976" ) // use parameterized tests => not for few instances which test content of an exception
class RqlOptionsTest {

   @Test
   void optionsWithBracketsAroundShouldLeadToSyntaxError() {
      final String expression = "   select=att1,att2,att3/subAtt4&filter=and(eq(att2,\"theSame\"),or(lt(att1,5),not(gt(att1,42))))&option="
            + "(limit(0,500),sort(+att1,-att2))";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "extraneous input '('" );
   }

   @Test
   void shouldThrowOnDoubleLimitClauseInExplodedFormSyntax() {
      final String expression = "select=id,name&filter=eq(id,\"47*\")&option=limit(1,2)&option=limit(5,7)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one limit statement allowed" );
   }

   @Test
   void shouldParseOptionsInAnyOrderInExplodedFormSortBeforeLimit() {
      final String sortLimitExpression = "select=id,name&option=sort(+name,-description)&option=limit(5,10)";

      final RqlQueryModel sortLimitParseTree = RqlParser.from( sortLimitExpression );

      assertThat( sortLimitParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( sortLimitParseTree.getOptions().getSlice() ).contains( new RqlSliceImpl( 5, 10 ) );
   }

   @Test
   void shouldParseOptionsInAnyOrderInExplodedFormLimitBeforeSort() {
      final String limitSortExpression = "select=id,name&option=limit(5,10)&option=sort(+name,-description)";

      final RqlQueryModel limitSortParseTree = RqlParser.from( limitSortExpression );

      assertThat( limitSortParseTree.getOptions().getOrder().fieldDirections() )
            .containsExactly(
                  new RqlFieldDirectionImpl( "name", RqlFieldDirection.Direction.ASCENDING ),
                  new RqlFieldDirectionImpl( "description", RqlFieldDirection.Direction.DESCENDING )
            );

      assertThat( limitSortParseTree.getOptions().getSlice() ).contains( new RqlSliceImpl( 5, 10 ) );
   }

   @Test
   void shouldThrowOnDoubleLimitClause() {
      final String expression = "select=id,name&filter=eq(id,\"47*\")&option=limit(1,2),limit(5,7)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one limit statement allowed" );
   }

   @Test
   void shouldThrowOnDoubleSortClause() {
      final String expression = "select=id,name&filter=eq(id,\"47*\")&option=sort(+id),sort(-name)";

      final Throwable throwable = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( throwable )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "No more than one sort statement allowed" );
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
}
