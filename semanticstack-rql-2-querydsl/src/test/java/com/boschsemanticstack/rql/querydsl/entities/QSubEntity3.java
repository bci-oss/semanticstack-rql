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

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;

public class QSubEntity3 extends EntityPathBase<SubEntity> {

   public static final QSubEntity3 subEntity = new QSubEntity3( "subEntity3" );

   public final StringPath description = createString( "description" );

   public QSubEntity3( final String variable ) {
      super( SubEntity.class, forVariable( variable ) );
   }

   public QSubEntity3( final Path<? extends SubEntity> path ) {
      super( path.getType(), path.getMetadata() );
   }

   public QSubEntity3( final PathMetadata metadata ) {
      super( SubEntity.class, metadata );
   }
}
