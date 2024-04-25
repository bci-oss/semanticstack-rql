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

package com.boschsemanticstack.rql.querydsl;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import com.boschsemanticstack.rql.querydsl.resolvers.BeanPathResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.CollectionPathResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.JpaCollectionPathResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.MapPathResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.SimpleValueResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.converters.TypeConverters;
import com.querydsl.core.types.dsl.EntityPathBase;

/**
 * Builder to create {@link QueryModelToQueryDSL} instances.
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitly
public class RqlToQueryDslConverterBuilder {
   private final EntityPathBase rootResource;
   private Consumer<QueryModelToQueryDSL> predicateResolverConfigurer;
   private final TypeConverters typeConverters = new TypeConverters();

   private RqlToQueryDslConverterBuilder( final EntityPathBase rootResource ) {
      super();
      this.rootResource = rootResource;
   }

   /**
    * Creates an instance for a JPA specific transformation.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    * @return the builder instance
    * @see #forGenericStore(EntityPathBase)
    */
   public static RqlToQueryDslConverterBuilder forJpa( final EntityPathBase rootResource ) {
      return new RqlToQueryDslConverterBuilder( rootResource ).withJpaPredicateResolvers();
   }

   /**
    * Creates an instance for a generic (non-JPA) transformation.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    * @return the builder instance
    * @see #forJpa(EntityPathBase)
    */
   public static RqlToQueryDslConverterBuilder forGenericStore( final EntityPathBase rootResource ) {
      return new RqlToQueryDslConverterBuilder( rootResource ).withGenericPredicateResolvers();
   }

   /**
    * Adds a type converter to be used when resolving values.
    * <p>
    * Type converters help, when the domain model uses types that aren't directly simple/primitive and need some form
    * of conversion (e.g. a UUID).
    *
    * @param targetClass the target class values will be converted to (i.e. the type used in the domain model)
    * @param converter the converter function
    * @return {@code this} for method chaining
    */
   public <F, T> RqlToQueryDslConverterBuilder withTypeConverter( final Class<T> targetClass,
         final Function<F, T> converter ) {
      typeConverters.register( targetClass, converter );
      return this;
   }

   private RqlToQueryDslConverterBuilder withJpaPredicateResolvers() {
      predicateResolverConfigurer = queryModelToQueryDsl -> {
         final SimpleValueResolver simpleValueResolver = new SimpleValueResolver( typeConverters );
         queryModelToQueryDsl.setPredicateResolvers( Arrays.asList(
               new JpaCollectionPathResolver( queryModelToQueryDsl, rootResource, simpleValueResolver ),
               new MapPathResolver( simpleValueResolver ),
               new BeanPathResolver( queryModelToQueryDsl ),
               simpleValueResolver ) );
      };
      return this;
   }

   private RqlToQueryDslConverterBuilder withGenericPredicateResolvers() {
      predicateResolverConfigurer = queryModelToQueryDsl -> {
         final SimpleValueResolver simpleValueResolver = new SimpleValueResolver( typeConverters );
         queryModelToQueryDsl.setPredicateResolvers(
               Arrays.asList(
                     new CollectionPathResolver( queryModelToQueryDsl, simpleValueResolver ),
                     new MapPathResolver( simpleValueResolver ),
                     new BeanPathResolver( queryModelToQueryDsl ),
                     simpleValueResolver ) );
      };
      return this;
   }

   /**
    * Builds the configured {@link QueryModelToQueryDSL} instance.
    *
    * @return the configured {@link QueryModelToQueryDSL} instance.
    */
   public QueryModelToQueryDSL build() {
      final QueryModelToQueryDSL queryModelToQueryDsl = new QueryModelToQueryDSL( rootResource );
      predicateResolverConfigurer.accept( queryModelToQueryDsl );
      return queryModelToQueryDsl;
   }
}
