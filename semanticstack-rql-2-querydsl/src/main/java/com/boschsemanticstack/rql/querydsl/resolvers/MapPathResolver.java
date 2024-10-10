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

package com.boschsemanticstack.rql.querydsl.resolvers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.stream.Collectors;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * A {@link Predicate} resolver that handles property paths that run into or through a {@link java.util.Map} property.
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public class MapPathResolver extends AbstractPathPredicateResolver<MapPath<?, ?, ?>, Predicate> {
   private final SimpleValueResolver simpleValueResolver;

   public MapPathResolver( final SimpleValueResolver simpleValueResolver ) {
      super();
      this.simpleValueResolver = simpleValueResolver;
   }

   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws IllegalAccessException {
      final MapPath mapPath = (MapPath) field.get( root );
      return resolveExpression( mapPath, pathElements, filter );
   }

   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {
      final MapPath mapPath = (MapPath) method.invoke( root );
      return resolveExpression( mapPath, pathElements, filter );
   }

   public Predicate resolveExpression( final MapPath mapPath, final Queue<String> pathElements, final RqlFilter filter )
         throws IllegalAccessException {

      // the remaining property path is the map's key
      final String mapKey = pathElements.stream().collect( Collectors.joining( "." ) );

      if ( !mapPath.getKeyType().isAssignableFrom( mapKey.getClass() ) ) {
         throw new IllegalArgumentException(
               "Illegal key type for property " + mapPath + ", expected " + mapPath.getKeyType() + ", found " + mapKey
                     .getClass() );
      }
      // build predicate for map value field
      return simpleValueResolver.resolve( mapPath.get( mapKey ), null, pathElements, filter );
   }
}
