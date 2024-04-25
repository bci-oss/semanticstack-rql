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

package com.boschsemanticstack.rql.examples.querydsljpa.controller;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.boschsemanticstack.rql.model.v1.RqlSlice;
import com.boschsemanticstack.rql.querydsl.QueryModelToQueryDSL;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BeanPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public class QueryDslRepositoryFilter<T, I, R extends QuerydslPredicateExecutor<T> & PagingAndSortingRepository<T, I>> {

   public static final int DEFAULT_OFFSET = 0;
   public static final int DEFAULT_PAGELIMIT = 20;

   private final R repository;

   private QueryDslRepositoryFilter( final R repository ) {
      this.repository = repository;
   }

   public static <T, I, R extends QuerydslPredicateExecutor<T> & PagingAndSortingRepository<T, I>> QueryDslRepositoryFilter<T, I, R> inRepository(
         final R repository ) {
      return new QueryDslRepositoryFilter<>( repository );
   }

   public Page<T> findWithQuery( final QueryModelToQueryDSL queryDsl ) {
      if ( queryDsl == null ) {
         throw new IllegalArgumentException( "Query must not be null" );
      }
      return find( queryDsl );
   }

   private Page<T> find( final QueryModelToQueryDSL queryDsl ) {
      final PageRequest pageRequest = createPageRequest( queryDsl );
      final Optional<Predicate> predicate = queryDsl.getPredicate();
      return predicate.map( p -> repository.findAll( p, pageRequest ) ) //
            .orElse( repository.findAll( pageRequest ) );
   }

   private PageRequest createPageRequest( final QueryModelToQueryDSL queryDsl ) {
      // Somewhat inconsistent in the API: ISLice is from query.dsl, OrderSpecifier from spring data
      final Optional<RqlSlice> pagination = queryDsl.getPagination();
      final List<OrderSpecifier<?>> ordering = queryDsl.getOrdering();

      final List<Sort.Order> sortOrder = ordering.stream()
            .map( QueryDslRepositoryFilter::convert )
            .filter( Objects::nonNull )
            .collect( Collectors.toList() );

      return pagination.map( p -> PageRequest.of( (int) p.offset(), (int) p.limit(), Sort.by( sortOrder ) ) )
            .orElse( PageRequest.of( DEFAULT_OFFSET, DEFAULT_PAGELIMIT, Sort.by( sortOrder ) ) );
   }

   private static Sort.Order convert( final OrderSpecifier<?> spec ) {
      final Expression<?> target = spec.getTarget();
      if ( target instanceof BeanPath ) {
         final AnnotatedElement annotatedElement = ((BeanPath<?>) target).getAnnotatedElement();
         if ( annotatedElement instanceof Field ) {
            final String name = ((Field) annotatedElement).getName();
            return Order.ASC == spec.getOrder() ? Sort.Order.asc( name ) : Sort.Order.desc( name );
         }
      }
      return null;
   }

   /**
    * Get the paged limit (The maximum amount of elements on the page).
    *
    * @return the page limit (not the real amount of elements on the page).
    */
   public static int getPageLimit( final QueryModelToQueryDSL queryDsl ) {
      return queryDsl.getPagination().map( p -> (int) p.limit() ).orElse( DEFAULT_PAGELIMIT );
   }
}
