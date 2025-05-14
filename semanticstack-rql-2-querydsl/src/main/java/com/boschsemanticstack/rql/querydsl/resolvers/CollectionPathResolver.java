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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Queue;

import com.boschsemanticstack.rql.exceptions.NoSuchFieldQueryException;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.DslExpression;
import com.querydsl.core.types.dsl.MapPath;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * Resolver for property paths that run into or through collection properties. Also supports special cases like maps
 * that are within the collection elements.
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public class CollectionPathResolver extends RecursiveResolver<CollectionPathBase<?, ?, ?>> {
   private final SimpleValueResolver simpleValueResolver;
 
   public CollectionPathResolver( final AbstractPathPredicateResolver parentResolver,
         final SimpleValueResolver simpleValueResolver ) {
      super( parentResolver );
      this.simpleValueResolver = simpleValueResolver;
   }

   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {

      final CollectionPathBase collectionPathBase = (CollectionPathBase) field.get( root );
      return resolveExpression( collectionPathBase, pathElements, filter );
   }

   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {

      final CollectionPathBase collectionPathBase = (CollectionPathBase) method.invoke( root );
      return resolveExpression( collectionPathBase, pathElements, filter );
   }

   public Predicate resolveExpression( final CollectionPathBase collectionPathBase, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {

      if ( pathElements.isEmpty() ) {
         final SimpleExpression anyCollectionEntryPath = collectionPathBase.any();
         return simpleValueResolver.resolve( anyCollectionEntryPath, null, pathElements, filter );
      }
      final CollectionPathStep collectionExpressionStep = getCollectionPathStep( collectionPathBase, pathElements );
      return super.resolve( collectionExpressionStep.getCollectionExpression(), null, pathElements, filter );
   }

   CollectionPathStep getCollectionPathStep( final CollectionPathBase collectionPathBase, final Queue<String> pathElements ) {

      // expression to query for any element in a collection that fulfills the predicate
      final SimpleExpression collectionExpression = collectionPathBase.any();

      // dirty hack for network.ways.supplements (map after list) causing invalid query for any-expression
      Class<?> type;
      final String pathElement = pathElements.peek();
      try {
         final Field nextField = getField( collectionExpression, pathElement );
         type = nextField.getType();
      } catch ( final NoSuchFieldQueryException e ) {
         final Optional<Method> method = getMethod( collectionExpression, pathElement );
         type = method.orElseThrow( () -> e ).getReturnType();
      }

      if ( MapPath.class.isAssignableFrom( type ) ) {
         return new CollectionPathStep( getCollectionSubQueryExpression( collectionPathBase, collectionExpression ),
               MapPath.class );
      }
      return new CollectionPathStep( collectionExpression, SimpleExpression.class );
   }

   SimpleExpression getCollectionSubQueryExpression( final CollectionPathBase collectionPathBase,
         final SimpleExpression collectionExpression ) {
      try {
         // we need to instantiate the collection's query type for a proper subquery or join
         final Constructor<? extends SimpleExpression> constructor = collectionExpression.getClass().getConstructor( Path.class );
         return constructor.newInstance( collectionPathBase );
      } catch ( final ReflectiveOperationException e ) {
         throw new RuntimeException( "Failed to instantiate subtype of " + collectionPathBase.toString(), e );
      }
   }

   /**
    * Stores the path type along with the actual QueryDSL expression so that resolution logic can use that
    * information when needed.
    */
   public class CollectionPathStep {
      private final SimpleExpression collectionExpression;

      private final Class<? extends DslExpression> expressionType;

      public CollectionPathStep( final SimpleExpression collectionExpression,
            final Class<? extends DslExpression> expressionType ) {
         this.collectionExpression = collectionExpression;
         this.expressionType = expressionType;
      }

      /**
       * @return {@code true} if the path is a Map path, {@code false} for standard collection paths (Lists etc.)
       */
      public boolean isMap() {
         return MapPath.class.isAssignableFrom( expressionType );
      }

      public SimpleExpression getCollectionExpression() {
         return collectionExpression;
      }
   }
}
