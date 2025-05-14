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

import java.math.BigDecimal;
import java.util.stream.Stream;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RqlFloatingPointTest {

   @ParameterizedTest
   @MethodSource( "expressions" )
   void shouldParseSimpleFloatingPointSyntaxIntoBigDecimal( final String expression ) {
      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model )
            .filter()
            .hasOperator( RqlFilter.Operator.EQ )
            .hasAttribute( "id" )
            .valueIsEqualTo( new BigDecimal( "12345.5432109876" ) )
            .hasNoChildren();
   }

   private static Stream<Arguments> expressions() {
      return Stream.of( Arguments.of( "filter=eq(id,12345.5432109876)" ),
            Arguments.of( "filter=eq(id,123455432109876e-10)" ),
            Arguments.of( "filter=eq(id,123455432109876e-0010)" ),
            Arguments.of( "filter=eq(id,1234.55432109876e1)" ),
            Arguments.of( "filter=eq(id,123.455432109876e+2)" ),
            Arguments.of( "filter=eq(id,123455432109876E-10)" )
      );
   }

   @Test
   void shouldParseSimpleFloatingPointSyntaxZeroIntoBigDecimal() {
      final String expression = "filter=eq(id,0.0)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model )
            .filter()
            .hasOperator( RqlFilter.Operator.EQ )
            .hasAttribute( "id" )
            .valueIsEqualTo( new BigDecimal( "0.0" ) )
            .hasNoChildren();
   }
}
