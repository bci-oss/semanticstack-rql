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
import static org.assertj.core.api.Assertions.catchThrowable;

import com.boschsemanticstack.rql.exceptions.ParseException;
import com.boschsemanticstack.rql.exceptions.SourceLocation;
import com.boschsemanticstack.rql.parser.v1.RqlParser;

import org.junit.jupiter.api.Test;
 
class RqlInvalidInputTest {

   @Test
   void shouldThrowOnInvalidRqlString() {
      final String invalidRql = "select = att1,\"att2\",att3/subAtt4&filter = and(eq(att2,\"theSame\"),or(lt(att1,23),not(gt(att1,13))))"
            + "&option=limit(0,500),sort(+att1,-att2)";

      final Throwable thrown = catchThrowable( () -> RqlParser.from( invalidRql ) );
      assertThat( thrown )
            .describedAs( "Attribute names should not be quoted." )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "mismatched input '\"att2\"'" );
   }

   @Test
   void shouldThrowOnTrailingPercentageAfterAttributeName() {
      final String invalidRql = "select = att1%%%&filter = eq(att2%%%,\"SomeThing\")";

      final Throwable thrown = catchThrowable( () -> RqlParser.from( invalidRql ) );
      assertThat( thrown )
            .describedAs( "No trailing illegal chars allowed" )
            .isInstanceOf( ParseException.class )
            .hasMessageContaining( "token recognition error at: '%'" );

      assertThat( ( (ParseException) thrown ).getSourceLocation() )
            .contains( new SourceLocation( 1, 14 ) );
   }
}
