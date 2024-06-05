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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.boschsemanticstack.rql.model.v1.RqlBuilder;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;

class RqlParserTest {

   @Test
   void emptyModelShouldBeDetectable() {
      final RqlQueryModel model = RqlParser.builder()
            .select()
            .sort()
            .build();

      assertThat( model.isEmpty() )
            .describedAs( "A model only containing empty parts should regard itself as empty" )
            .isTrue();
   }

   @Test
   void shouldProvideEasyBuilder() {
      final RqlQueryModel model = RqlParser.builder()
            .select( "att1", "att2", "att3/subAtt4" )
            .filter( RqlBuilder.and(
                  RqlBuilder.eq( "att2", "theSame" ),
                  RqlBuilder.or(
                        RqlBuilder.lt( "att1", 5238907523475022349L ),
                        RqlBuilder.not(
                              RqlBuilder.gt( "att1", new BigInteger( "12345678901234567890123456789012345678901234567890" ) )
                        )
                  ) ) )
            .sort( RqlBuilder.asc( "att1" ), RqlBuilder.desc( "att2" ) )
            .limit( 0, 500 )
            .build();

      final String representation = RqlParser.toString( model );

      assertThat( representation ).isEqualTo(
            "select=att1,att2,att3/subAtt4&filter=and(eq(att2,\"theSame\"),or(lt(att1,5238907523475022349),not(gt(att1,"
                  + "12345678901234567890123456789012345678901234567890))))&option=limit(0,500),sort(+att1,-att2)" );
   }

   @Test
   void buildingInRestrictionWithCollectionShouldWork() {

      final List<?> attributeList = Arrays.asList( "foo", "bar", "fooBar" );

      final RqlQueryModel model = RqlParser.builder()
            .filter(
                  RqlBuilder.in( "attribute", attributeList )
            )
            .build();

      assertThat( RqlParser.toString( model ) )
            .isEqualTo( "filter=in(attribute,\"foo\",\"bar\",\"fooBar\")" );
   }

   @Test
   void buildingInRestrictionWithCollectionCastToObjectShouldWork() {
      final Object attributeList = Arrays.asList( "foo", "bar", "fooBar" );

      final RqlQueryModel model = RqlParser.builder()
            .filter(
                  RqlBuilder.in( "attribute", attributeList )
            )
            .build();

      assertThat( RqlParser.toString( model ) )
            .isEqualTo( "filter=in(attribute,\"foo\",\"bar\",\"fooBar\")" );
   }

   @Test
   void additionOfRestrictionShouldWorkIfTopLevelRestrictionIsNotAnd() {
      final RqlQueryModel model = RqlParser.builder()
            .filter(
                  RqlBuilder.eq( "deviceId", "5b49d292-7572-48be-b205-e19d9fecf679" )
            )
            .build();

      final RqlQueryModel modelWithRestriction = RqlParser.addRestriction(
            model,
            RqlBuilder.eq( "discriminator", "COUPLING" )
      );

      assertThat( RqlParser.toString( modelWithRestriction ) )
            .isEqualTo( "filter=and(eq(deviceId,\"5b49d292-7572-48be-b205-e19d9fecf679\"),eq(discriminator,\"COUPLING\"))" );
   }

   @Test
   void additionOfRestrictionShouldWorkIfNoFilterWasSet() {
      final RqlQueryModel model = RqlParser.builder()
            .select( "att1" )
            .build();

      final RqlQueryModel modelWithRestriction = RqlParser.addRestriction(
            model,
            RqlBuilder.eq( "discriminator", "A" ),
            RqlBuilder.ne( "att3", 42 ),
            RqlBuilder.in( "att1", "A", "B", "C" )
      );

      assertThat( RqlParser.toString( modelWithRestriction ) )
            .isEqualTo( "select=att1&filter=and(eq(discriminator,\"A\"),ne(att3,42),in(att1,\"A\",\"B\",\"C\"))" );
   }

   @Test
   void additionOfRestrictionShouldNotNestAndsAndRetainLimitAndSelect() {
      final RqlQueryModel model = RqlParser.builder()
            .select( "att1", "att2", "att3/subAtt4" )
            .filter( RqlBuilder.and(
                  RqlBuilder.eq( "att2", "theSame" ),
                  RqlBuilder.lt( "att1", 23 ) )
            )
            .sort( RqlBuilder.asc( "att1" ), RqlBuilder.desc( "att2" ) )
            .limit( 0, 500 )
            .build();

      final RqlQueryModel modelWithRestriction = RqlParser.addRestriction(
            model,
            RqlBuilder.eq( "att2", "fizzBuzz" ), // <1>
            RqlBuilder.or( RqlBuilder.ne( "att3", 42 ) ) // <2>
      );

      assertThat( RqlParser.toString( modelWithRestriction ) )
            .isEqualTo(
                  "select=att1,att2,att3/subAtt4&filter=and(eq(att2,\"theSame\"),lt(att1,23),eq(att2,\"fizzBuzz\"),or(ne(att3,42)))"
                        + "&option=limit(0,500),sort(+att1,-att2)" );
   }

   @Test
   void shouldAllowEasyManipulationOfPaging() {
      final RqlQueryModel modelWith12to97Limit = RqlParser.builder()
            .limit( 12, 97 )
            .build();

      assertThat( RqlParser.toString( modelWith12to97Limit ) ).isEqualTo( "option=limit(12,97)" );

      assertThat( RqlParser.getPagedQueryStream( modelWith12to97Limit, 50 )
            .map( RqlParser::toString )
      ).containsExactly(
            "option=limit(12,50)",
            "option=limit(62,47)"
      );
   }

   @Test
   void pagingShouldAlwaysWorkForUnboundedQuery() {
      final RqlQueryModel unboundedQuery = RqlParser.builder()
            .select( "foo" )
            .build();

      assertThat( RqlParser.toString( unboundedQuery ) ).isEqualTo( "select=foo" );

      assertThat( RqlParser.getPagedQueryStream( unboundedQuery, 50 )
            .map( RqlParser::toString )
            .limit( 4 )
      ).containsExactly(
            "select=foo&option=limit(0,50)",
            "select=foo&option=limit(50,50)",
            "select=foo&option=limit(100,50)",
            "select=foo&option=limit(150,50)"
      );
   }
}

