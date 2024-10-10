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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;

import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.SimpleExpression;

/**
 * Transforms an {@link RqlQueryModel} into predicates, paging and sorting information for a given QueryDSL query type.
 * <br/>
 * To use this class create an instance fitting your underlying store and then apply it to your respective query.<br/>
 * Example for JPA:
 * <pre>
 *    QueryModelToQueryDSL jpaQuery = QueryModelToQueryDSL.forJpa(yourRootEntity).applyTo(query);
 *    ...
 *    jpaQuery.getPredicate();
 * </pre>
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitly
public class QueryModelToQueryDSL extends AbstractQueryModelToQueryDSL<Predicate> {
 
   /**
    * Transform {@link RqlQueryModel} into predicates, paging and sorting information
    * for a given Querydsl query type.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    */
   QueryModelToQueryDSL( final EntityPathBase rootResource ) {
      super( rootResource );
   }

   /**
    * Creates an instance for a JPA specific transformation and applies it to the given query.
    * <p>
    * This is a shortcut convenience method. If you need more control over how the transformation is configured you
    * can use the {@link RqlToQueryDslConverterBuilder}.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    * @return the newly created instance which then can be applied to a query
    * @see #applyTo(RqlQueryModel)
    * @see #forGenericStore(EntityPathBase, RqlQueryModel)
    */
   public static QueryModelToQueryDSL forJpa( final EntityPathBase rootResource, final RqlQueryModel query ) {
      return RqlToQueryDslConverterBuilder.forJpa( rootResource ).build().applyTo( query );
   }

   // Used via Reflection so the override is necessary.
   @SuppressWarnings( "java:S1185" )
   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter ) {
      return super.resolve( root, field, pathElements, filter );
   }

   // Used via Reflection so the override is necessary.
   @SuppressWarnings( "java:S1185" )
   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {
      return super.resolveMethod( root, method, pathElements, filter );
   }

   /**
    * Creates an instance for a generic (non-JPA) transformation and applies it to the given query.
    * <p>
    * This is a shortcut convenience method. If you need more control over how the transformation is configured you
    * can use the {@link RqlToQueryDslConverterBuilder}.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    * @return the newly created instance which then can be applied to a query
    * @see #applyTo(RqlQueryModel)
    * @see #forJpa(EntityPathBase, RqlQueryModel)
    */
   public static QueryModelToQueryDSL forGenericStore( final EntityPathBase rootResource, final RqlQueryModel query ) {
      return RqlToQueryDslConverterBuilder.forGenericStore( rootResource ).build().applyTo( query );
   }

   @Override
   protected Predicate digest( final RqlFilter filter ) {
      switch ( filter.getFilterType() ) {
         case AND:
            final BooleanBuilder booleanBuilder = new BooleanBuilder();
            filter.getChildren().forEach( operand -> booleanBuilder.and( digest( operand ) ) );
            return booleanBuilder.getValue();
         case OR:
            final BooleanBuilder booleanBuilderOr = new BooleanBuilder();
            filter.getChildren().forEach( operand -> booleanBuilderOr.or( digest( operand ) ) );
            return booleanBuilderOr.getValue();
         case NOT:
            return digest( filter.getChildren() ).not();
         case VALUE:
            return getValuePredicate( filter );
         default:
            throw new IllegalArgumentException( "Unknown filter type " + filter.getFilterType() + " for " + filter );
      }
   }

   private Predicate digest( final List<RqlFilter> children ) {
      if ( children.size() == 1 ) {
         return digest( children.get( 0 ) );
      } else {
         final BooleanBuilder booleanBuilder = new BooleanBuilder();
         children.forEach( operand -> booleanBuilder.and( digest( operand ) ) );
         return booleanBuilder.getValue();
      }
   }

   public QueryModelToQueryDSL applyTo( final RqlQueryModel query ) {
      applyModel( query );
      return this;
   }
}
