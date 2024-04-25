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

package com.boschsemanticstack.rql.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.boschsemanticstack.rql.querydsl.entities.Entity;
import com.boschsemanticstack.rql.querydsl.entities.QEntity;
import com.boschsemanticstack.rql.querydsl.entities.QSubEntity;
import com.boschsemanticstack.rql.querydsl.entities.QSubEntity2;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.Test;

class QueryDSLJPATest {

   @Test
   void rawQueryDSLComparisonWithConstraintOnCollectionEntryAndItsSupplementsShouldReturnNothing() {
      final JPQLQuery<Entity> waySubQuery = JPAExpressions.selectFrom( QEntity.entity )
            .leftJoin( QEntity.entity.subEntities, QSubEntity.subEntity )
            .leftJoin( QEntity.entity.subEntities2 );
      final Predicate innerPredicate = new BooleanBuilder()
            .and( QSubEntity.subEntity.name.eq( "a" ) )
            .and( QSubEntity.subEntity.metadata.contains( "mkey", "mvalue" ) )
            .and( QSubEntity2.subEntity2.name.eq( "b" ) )
            .getValue();
      waySubQuery.where( innerPredicate );
      final Predicate predicate = QEntity.entity.in( waySubQuery );
      final String jpaQuery = asJpaQuery( predicate );
      assertThat( jpaQuery ).isEqualTo( "\n"
            + "where entity in (select entity\n"
            + "from Entity entity\n"
            + "  left join entity.subEntities as subEntity\n"
            + "  left join entity.subEntities2\n"
            + "  left join subEntity.metadata as subEntity_metadata_0 on key(subEntity_metadata_0) = ?1\n"
            + "where subEntity.name = ?2 and subEntity_metadata_0 = ?3 and subEntity2.name = ?4)" );
   }
   
   private String asJpaQuery( final Predicate predicate ) {
      final JPQLSerializer serializer = new JPQLSerializer( JPQLTemplates.DEFAULT );
      final JPAQuery jpaQuery = (JPAQuery) new JPAQuery().where( predicate );
      serializer.serialize( jpaQuery.getMetadata(), false, null );
      return serializer.toString();
   }
}
