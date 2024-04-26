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

package com.boschsemanticstack.rql.querydsl.entities;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;

public class QWildcardEntity extends EntityPathBase<WildcardEntity> {

   public static final QWildcardEntity entity = new QWildcardEntity( "entity" );

   public final StringPath id = createString( "id" );

   public final StringPath type = createString( "type" );

   public final StringPath count1 = createString( "count1" );

   public final StringPath count2 = createString( "count2" );

   public final StringPath regex1 = createString( "regex1" );

   public final StringPath regex2 = createString( "regex2" );

   public final StringPath error1 = createString( "error1" );

   public final StringPath error2 = createString( "error2" );

   public final QSubEntity subEntity;

   public final ListPath<SubEntity, QSubEntity> subEntities = createList( "subEntities", SubEntity.class,
         QSubEntity.class,
         PathInits.DIRECT2 );

   public QWildcardEntity( final String variable ) {
      this( WildcardEntity.class, forVariable( variable ), PathInits.DIRECT2 );
   }

   public QWildcardEntity( final Path<? extends WildcardEntity> path ) {
      this( path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? PathInits.DIRECT2 : PathInits.DEFAULT );
   }

   public QWildcardEntity( final PathMetadata metadata ) {
      this( metadata, metadata.isRoot() ? PathInits.DIRECT2 : PathInits.DEFAULT );
   }

   public QWildcardEntity( final PathMetadata metadata, final PathInits inits ) {
      this( WildcardEntity.class, metadata, inits );
   }

   public QWildcardEntity( final Class<? extends WildcardEntity> type, final PathMetadata metadata, final PathInits inits ) {
      super( type, metadata, inits );
      subEntity = inits.isInitialized( "subEntity" ) ? new QSubEntity( forProperty( "subEntity" ) ) : null;
   }

   public ListPath<SubEntity, QSubEntity> subEntities() {
      return subEntities;
   }

}
