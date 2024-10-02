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

package com.boschsemanticstack.rql.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import com.boschsemanticstack.rql.parser.v1.RqlParser;
import com.boschsemanticstack.rql.querydsl.entities.QEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class QueryDSLCollectionJPATest {

   public static Stream<Arguments> generateParameters() {
      return new QueryDSLCollectionTestCases().getCases().stream()
            .map( c -> arguments( c.getDescription(), c.getRql(), c.getJpa() ) );
   }

   @ParameterizedTest( name = "Test {index}: {0}" )
   @MethodSource( { "generateParameters" } )
   void testJpa( final String name, final String rql, final String expectedQuery ) {
      final QueryModelToQueryDSL query = getJpaQuery( rql );
      final Predicate predicate = query.getPredicate().orElseThrow( () -> new IllegalStateException( "Predicate Expected" ) );
      final String generatedQuery = asJpaQuery( predicate );
      assertThat( generatedQuery ).isEqualTo( expectedQuery );
   }
 
   private QueryModelToQueryDSL getJpaQuery( final String queryParams ) {
      return QueryModelToQueryDSL.forJpa( QEntity.entity, RqlParser.from( queryParams ) );
   }

   private String asJpaQuery( final Predicate predicate ) {
      final JPQLSerializer serializer = new JPQLSerializer( JPQLTemplates.DEFAULT );
      final JPAQuery jpaQuery = (JPAQuery) new JPAQuery().where( predicate );
      serializer.serialize( jpaQuery.getMetadata(), false, null );
      return serializer.toString();
   }
}
