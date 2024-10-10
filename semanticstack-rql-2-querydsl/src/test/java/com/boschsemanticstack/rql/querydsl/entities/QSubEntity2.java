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
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.StringPath;

public class QSubEntity2 extends EntityPathBase<SubEntity2> {

   public static final QSubEntity2 subEntity2 = new QSubEntity2( "subEntity2" );

   public final StringPath id = createString( "id" );

   public final StringPath name = createString( "name" );

   public final StringPath type = createString( "type" );
 
   public final MapPath<String, String, StringPath> metadata = createMap( "metadata", String.class, String.class,
         StringPath.class );

   public QSubEntity2( final String variable ) {
      super( SubEntity2.class, forVariable( variable ) );
   }

   public QSubEntity2( final Path<? extends SubEntity2> path ) {
      super( path.getType(), path.getMetadata() );
   }

   public QSubEntity2( final PathMetadata metadata ) {
      super( SubEntity2.class, metadata );
   }
}
