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

import java.math.BigDecimal;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;

class RqlFloatingPointTest {

   @Test
   void shouldParseSimpleFloatingPointSyntaxIntoBigDecimal() {
      final String expression = "filter=eq(id,12345.5432109876)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }
 
   @Test
   void shouldParseSimpleFloatingPointSyntaxZeroIntoBigDecimal() {
      final String expression = "filter=eq(id,0.0)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "0.0" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseExponentialFloatingPointSyntaxIntoBigDecimal() {
      final String expression = "filter=eq(id,123455432109876e-10)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseExponentialFloatingPointSyntaxWithLeadingZeroInExponentIntoBigDecimal() {
      final String expression = "filter=eq(id,123455432109876e-0010)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseExponentialFloatingPointSyntaxWithImplicitPositiveExponentIntoBigDecimal() {
      final String expression = "filter=eq(id,1234.55432109876e1)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseExponentialFloatingPointSyntaxWithExplicitlyPositiveExponentIntoBigDecimal() {
      final String expression = "filter=eq(id,123.455432109876e+2)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseExponentialFloatingPointSyntaxWithUpperCaseEIntoBigDecimal() {
      final String expression = "filter=eq(id,123455432109876E-10)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "id" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( new BigDecimal( "12345.5432109876" ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }
}
