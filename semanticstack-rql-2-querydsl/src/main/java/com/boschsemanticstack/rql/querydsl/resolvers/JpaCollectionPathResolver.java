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

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
  
/**
 * JPA-specific version of the {@link CollectionPathResolver} that takes care of properly created queries
 * in order to keep join-semantic for multiple conditions on collection entries.
 * This means all predicats will be applied to the same collection entry.
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
public class JpaCollectionPathResolver<T> extends RecursiveResolver<CollectionPathBase<?, ?, ?>> {
   private final SimpleValueResolver simpleValueResolver;

   private final Map<CollectionExpression, EntityPath> subCollections = new LinkedHashMap<>();
   private final EntityPathBase<T> rootResource;

   public JpaCollectionPathResolver( final AbstractPathPredicateResolver parentResolver, final EntityPathBase<T> rootResource,
         final SimpleValueResolver simpleValueResolver ) {
      super( parentResolver );
      this.rootResource = rootResource;
      this.simpleValueResolver = simpleValueResolver;
   }

   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      return resolveCollection( (CollectionPathBase) field.get( root ), pathElements, filter );
   }

   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      return resolveCollection( (CollectionPathBase) method.invoke( root ), pathElements, filter );
   }

   public Predicate resolveCollection( final CollectionPathBase collectionPath, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {

      final SimpleExpression anyCollectionEntryPath = collectionPath.any();
      if ( pathElements.isEmpty() ) {
         return simpleValueResolver.resolve( anyCollectionEntryPath, null, pathElements, filter );
      } else {
         final Class<? extends SimpleExpression> typeOfCollectionEntries = anyCollectionEntryPath.getClass();
         final Field subentityQInstanceField = Arrays.stream( typeOfCollectionEntries.getDeclaredFields() )
               .filter( entityField -> entityField.getType()
                     .equals(
                           typeOfCollectionEntries ) )
               .findFirst().orElseThrow(
                     () -> new RuntimeException( "Invalid collection entry type." ) );
         final EntityPath<?> collectionEntryPath = (EntityPath) subentityQInstanceField.get( null );

         subCollections.put( collectionPath, collectionEntryPath );
         return super.resolve( (SimpleExpression) collectionEntryPath, null, pathElements, filter );
      }
   }

   @Override
   public Predicate postProcess( final Predicate predicate ) {
      if ( !subCollections.isEmpty() ) {
         final JPQLSubQuery<T> subQuery = JPAExpressions.selectFrom( rootResource );
         subCollections.forEach(
               ( collectionPath, collectionEntryPath ) -> addJoin( subQuery, collectionPath, collectionEntryPath ) );
         subQuery.where( predicate );
         return rootResource.in( subQuery );
      }
      return predicate;
   }

   private <P> void addJoin( final JPQLSubQuery<T> subQuery, final CollectionExpression<?, P> collectionPath,
         final EntityPath<P> collectionEntryPath ) {
      subQuery.leftJoin( collectionPath, collectionEntryPath );
   }
}
