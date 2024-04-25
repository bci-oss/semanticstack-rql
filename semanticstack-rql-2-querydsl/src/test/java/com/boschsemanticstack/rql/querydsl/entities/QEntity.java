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

package com.boschsemanticstack.rql.querydsl.entities;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.UUID;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;

public class QEntity extends EntityPathBase<Entity> {

   public static final QEntity entity = new QEntity( "entity" );

   public final StringPath id = createString( "id" );

   public final ComparablePath<UUID> specialId = createComparable( "specialId", java.util.UUID.class );

   public final StringPath name = createString( "name" );

   public final StringPath type = createString( "type" );

   public final BooleanPath special = createBoolean( "special" );

   public final QSubEntity subEntity;

   public final ListPath<SubEntity, QSubEntity> subEntities = createList( "subEntities", SubEntity.class,
         QSubEntity.class,
         PathInits.DIRECT2 );

   public final ListPath<SubEntity2, QSubEntity2> subEntities2 = createList( "subEntities2", SubEntity2.class,
         QSubEntity2.class,
         PathInits.DIRECT2 );

   public final ListPath<String, StringPath> stringList = createList( "stringList", String.class, StringPath.class,
         PathInits.DIRECT2 );

   public QEntity( final String variable ) {
      this( Entity.class, forVariable( variable ), PathInits.DIRECT2 );
   }

   public QEntity( final Path<? extends Entity> path ) {
      this( path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? PathInits.DIRECT2 : PathInits.DEFAULT );
   }

   public QEntity( final PathMetadata metadata ) {
      this( metadata, metadata.isRoot() ? PathInits.DIRECT2 : PathInits.DEFAULT );
   }

   public QEntity( final PathMetadata metadata, final PathInits inits ) {
      this( Entity.class, metadata, inits );
   }

   public QEntity( final Class<? extends Entity> type, final PathMetadata metadata, final PathInits inits ) {
      super( type, metadata, inits );
      subEntity = inits.isInitialized( "subEntity" ) ? new QSubEntity( forProperty( "subEntity" ) ) : null;
   }

   public ListPath<SubEntity, QSubEntity> subs() {
      return subEntities;
   }

   public ListPath<SubEntity, QSubEntity> entity3() {
      return subEntities;
   }
}
