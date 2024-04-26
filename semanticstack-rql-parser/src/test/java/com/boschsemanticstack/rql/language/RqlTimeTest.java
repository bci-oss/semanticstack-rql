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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

class RqlTimeTest {

   @Test
   void shouldNotParseIso8601ZuluWithSecondsMissing() {
      final String expression = "filter=eq(created,2007-12-03T10:15Z)";

      final Throwable thrown = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( thrown ).as( "This is actually valid ISO 8601 but not supported yet" )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input" );
   }

   @Test
   void shouldNotParseIso8601ZuluWithMinutesAndSecondsMissing() {
      final String expression = "filter=eq(created,2007-12-03T10:15+4:27)";

      final Throwable thrown = catchThrowable( () -> RqlParser.from( expression ) );

      assertThat( thrown ).as( "This is actually valid ISO 8601 but not supported yet" )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input" );
   }

   @Test
   void shouldParseIso8601ZuluWithFractionalSeconds() {
      final String expression = "filter=eq(created,2007-12-03T10:15:30.0123Z)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "created" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo(
            OffsetDateTime.of( 2007, 12, 3, 10, 15, 30, 123 * 1000 * 100, ZoneOffset.UTC ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseIso8601ZuluWithFractionalSecondsInCommaNotation() {
      final String expression = "filter=eq(created,2007-12-03T10:15:30,0123Z)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "created" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo(
            OffsetDateTime.of( 2007, 12, 3, 10, 15, 30, 123 * 1000 * 100, ZoneOffset.UTC ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseIso8601ZuluWithoutFractionalSeconds() {
      final String expression = "filter=eq(created,2007-12-03T10:15:30Z)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "created" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( OffsetDateTime.of( 2007, 12, 3, 10, 15, 30, 0, ZoneOffset.UTC ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseIso8601ZuluWithNonCapitalizedTandZ() {
      final String expression = "filter=eq(created,2007-12-03t10:15:30z)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "created" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo( OffsetDateTime.of( 2007, 12, 3, 10, 15, 30, 0, ZoneOffset.UTC ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldParseIso8601WithOffsetWithoutFractionalSeconds() {
      final String expression = "filter=eq(created,2007-12-03T10:15:30+04:37)";

      final RqlQueryModel model = RqlParser.from( expression );

      assertThat( model.getFilter().get().getOperator() ).isEqualTo( RqlFilter.Operator.EQ );
      assertThat( model.getFilter().get().getAttribute() ).isEqualTo( "created" );
      assertThat( model.getFilter().get().getValue() ).isEqualTo(
            OffsetDateTime.of( 2007, 12, 3, 10, 15, 30, 0, ZoneOffset.ofHoursMinutes( 4, 37 ) ) );
      assertThat( model.getFilter().get().getChildren() ).isEmpty();
   }

   @Test
   void shouldThrowOnIso8601WithSingleDigitOffset() {
      final String expression = "filter=eq(created,2007-12-03T10:15:30+4:27)";

      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input" );
   }

   @Test
   void shouldThrowParsingIso8601ZuluWithIllegalDate() {
      final String expression = "filter=eq(created,2007-17-03T10:15:30Z)";

      assertThatThrownBy( () -> RqlParser.from( expression ) )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "Invalid value for MonthOfYear" );
   }
}
