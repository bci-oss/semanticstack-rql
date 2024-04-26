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
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;

public class QSubEntity extends EntityPathBase<SubEntity> {

   private static final PathInits INITS = PathInits.DIRECT2;

   public static final QSubEntity subEntity = new QSubEntity( "subEntity" );

   public final StringPath id = createString( "id" );

   public final StringPath name = createString( "name" );

   public final StringPath type = createString( "type" );

   public final MapPath<String, String, StringPath> metadata = createMap( "metadata", String.class, String.class,
         StringPath.class );

   public QSubEntity3 entity;

   public final ListPath<String, StringPath> stringList = createList( "stringList", String.class, StringPath.class,
         PathInits.DIRECT2 );

   public QSubEntity( final String variable ) {
      this( SubEntity.class, forVariable( variable ), INITS );
   }

   public QSubEntity( final Path<? extends SubEntity> path ) {
      this( path.getType(), path.getMetadata(), PathInits.getFor( path.getMetadata(), INITS ) );
   }

   public QSubEntity( final PathMetadata metadata ) {
      this( metadata, PathInits.getFor( metadata, INITS ) );
   }

   public QSubEntity( final PathMetadata metadata, final PathInits inits ) {
      this( SubEntity.class, metadata, inits );
   }

   public QSubEntity( final Class<? extends SubEntity> type,
         final PathMetadata metadata,
         final PathInits inits ) {
      super( type, metadata, inits );
      entity = inits.isInitialized( "entity" ) ? new QSubEntity3( forProperty( "entity" ) ) : null;
   }

   public StringPath shortName() {
      return name;
   }

   public StringPath description() {
      return entity.description;
   }

}
