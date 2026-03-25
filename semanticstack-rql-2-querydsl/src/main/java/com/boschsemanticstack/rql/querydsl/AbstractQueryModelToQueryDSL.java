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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Pattern;

import com.boschsemanticstack.rql.annotation.RqlPattern;
import com.boschsemanticstack.rql.annotation.WildcardCount;
import com.boschsemanticstack.rql.exceptions.IllegalValueTypeQueryException;
import com.boschsemanticstack.rql.exceptions.NoSuchFieldQueryException;
import com.boschsemanticstack.rql.exceptions.NonComparableFieldQueryException;
import com.boschsemanticstack.rql.exceptions.UnsupportedFieldTypeQueryException;
import com.boschsemanticstack.rql.model.v1.RqlFieldDirection;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.model.v1.RqlQueryModel;
import com.boschsemanticstack.rql.model.v1.RqlSlice;
import com.boschsemanticstack.rql.querydsl.resolvers.AbstractPathPredicateResolver;
import com.boschsemanticstack.rql.querydsl.resolvers.PathPredicateResolver;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.util.StringUtils;

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
public abstract class AbstractQueryModelToQueryDSL<R> extends AbstractPathPredicateResolver<SimpleExpression<?>, R> {

   private static final Map<String, Pattern> PATTERN_CACHE = Collections.synchronizedMap( new LinkedHashMap<>() {
      @Override
      protected boolean removeEldestEntry( final Map.Entry<String, Pattern> eldest ) {
         return size() > 50;
      }
   } );

   private R predicate;
   private RqlSlice pagination;
   private final List<OrderSpecifier<?>> ordering = new ArrayList<>();
   private final EntityPathBase rootResource;
   private List<PathPredicateResolver> predicateResolvers;

   /**
    * Transform {@link RqlQueryModel} into predicates, paging and sorting information
    * for a given Querydsl query type.
    *
    * @param rootResource the QueryDSL metamodel instance to apply the query to
    */
   protected AbstractQueryModelToQueryDSL( final EntityPathBase rootResource ) {
      super();
      this.rootResource = rootResource;
   }

   public void setPredicateResolvers( final List<PathPredicateResolver> predicateResolvers ) {
      this.predicateResolvers = predicateResolvers;
   }

   /**
    * Performs the actual transformation of the given query.
    *
    * @param query the query to transform
    * @return {@code this} for method chaining. To retrieve the generated {@link Predicate}, use {@link #getPredicate()}.
    * @throws IllegalValueTypeQueryException for wrong value types
    * @throws NonComparableFieldQueryException if a field is not comparable (e.g. like with an integer field or lt
    * with a text field)
    * @throws NoSuchFieldQueryException if a field does not exist
    * @throws UnsupportedFieldTypeQueryException if a field is not supported by the transformation
    */
   protected void applyModel( final RqlQueryModel query ) {
      ordering.addAll( extractOrdering( query.getOptions().getOrder().fieldDirections() ) );
      pagination = query.getOptions().getSlice().orElse( null );
      predicate = query.getFilter()
            .map( this::digest )
            .map( this::postProcess )
            .orElse( null );
   }

   @SuppressWarnings( "java:S2293" )
   // specifying the generic on BeanPath is not optional as java cannot work out the types otherwise
   private Collection<? extends OrderSpecifier<?>> extractOrdering( final List<RqlFieldDirection> orders ) {
      return orders.stream()
            .map( this::existsSortAttribute )
            .map( order -> new OrderSpecifier<>(
                  order.direction().equals( RqlFieldDirection.Direction.ASCENDING ) ? Order.ASC : Order.DESC,
                  new BeanPath<Comparable<?>>( rootResource.getType(), rootResource, order.attribute() ) ) )
            .toList();
   }

   private RqlFieldDirection existsSortAttribute( final RqlFieldDirection order ) {
      final LinkedList<String> attributes = splitAttribute( order.attribute() );

      //check if Field exists
      SimpleExpression expression = rootResource;
      for ( final String attribute : attributes ) {
         try {
            expression = existsSortAttribute( expression, attribute );
         } catch ( final IllegalAccessException e ) {
            // cannot happen
         }
      }

      return order;
   }

   private SimpleExpression existsSortAttribute( final SimpleExpression root, final String attribute ) throws IllegalAccessException {
      final Field field = getField( root, attribute );
      final Object path = field.get( root );
      if ( path instanceof CollectionPathBase ) {
         throw new NoSuchFieldQueryException( "Sorting by collection child entity is not supported." );
      }
      return (SimpleExpression) path;
   }

   @Override
   public R postProcess( final R predicate ) {
      R processedPredicate = predicate;
      for ( final PathPredicateResolver predicateResolver : predicateResolvers ) {
         processedPredicate = (R) predicateResolver.postProcess( processedPredicate );
      }
      return processedPredicate;
   }

   protected abstract R digest( RqlFilter filter );

   protected R getValuePredicate( final RqlFilter filter ) {
      final List<Object> values = filter.getValues();
      if ( values.isEmpty() ) {
         throw new IllegalValueTypeQueryException( "No values given for predicate " + filter );
      }
      return resolve( rootResource, null, splitAttribute( filter.getAttribute() ), filter );
   }

   private LinkedList<String> splitAttribute( final String attribute ) {
      return new LinkedList<>( List.of( attribute.split( "[./]" ) ) );
   }

   @Override
   public R resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter ) {
      final String path = pathElements.poll();
      try {
         final Field declaredField = getField( root, path );
         if ( pathElements.isEmpty() ) {
            getWildcardCount( root, path ).ifPresent( wildcardCount -> checkLikeCountExpression( path, filter, wildcardCount ) );
            getRqlPattern( root, path ).ifPresent( rqlPattern -> checkLikePatternExpression( path, filter, rqlPattern ) );
         }

         return (R) findPredicateResolverFor( declaredField ).resolve( root, declaredField, pathElements, filter );
      } catch ( final NoSuchFieldQueryException e ) {
         return getMethodPredicate( root, path, pathElements, filter, e );
      } catch ( final ReflectiveOperationException e ) {
         return getMethodPredicate( root, path, pathElements, filter, new NoSuchFieldQueryException( e.getMessage(), e ) );
      }
   }

   private Optional<RqlPattern> getRqlPattern( final SimpleExpression root, final String path ) {
      return getAnnotationOnField( root, path, RqlPattern.class );
   }

   private Optional<WildcardCount> getWildcardCount( final SimpleExpression root, final String path ) {
      return getAnnotationOnField( root, path, WildcardCount.class );
   }

   private <T extends Annotation> Optional<T> getAnnotationOnField( final SimpleExpression root, final String path,
         final Class<T> annotation ) {
      try {
         final Field entityField = root.getType().getDeclaredField( path );
         return Optional.ofNullable( entityField.getAnnotation( annotation ) );
      } catch ( final NoSuchFieldException e ) {
         return Optional.empty();
      }
   }

   @Override
   public R resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements, final RqlFilter filter )
         throws ReflectiveOperationException {
      final Optional<PathPredicateResolver> resolver = findPredicateResolverFor( method );
      if ( resolver.isEmpty() ) {
         return null;
      }

      return (R) resolver.get().resolveMethod( root, method, pathElements, filter );
   }

   @SuppressWarnings( "squid:S1166" )
   // squid:S1166 log or rethrow exception is not necessary
   private R getMethodPredicate( final SimpleExpression root, final String path,
         final Queue<String> pathElements, final RqlFilter filter, final NoSuchFieldQueryException e ) {
      final Optional<Method> declareMethod = getMethod( root, path );
      if ( declareMethod.isEmpty() ) {
         throw e;
      }

      try {

         if ( pathElements.isEmpty() ) {
            final WildcardCount wildcardCount = declareMethod.get().getAnnotation( WildcardCount.class );
            if ( wildcardCount != null ) {
               checkLikeCountExpression( path, filter, wildcardCount );
            }

            final RqlPattern rqlPattern = declareMethod.get().getAnnotation( RqlPattern.class );
            if ( rqlPattern != null ) {
               checkLikePatternExpression( path, filter, rqlPattern );
            }
         }

         final R methodPredicate = resolveMethod( root, declareMethod.get(), pathElements, filter );
         if ( methodPredicate == null ) {
            throw e;
         }
         return methodPredicate;
      } catch ( final NoSuchFieldQueryException exception ) {
         throw exception;
      } catch ( final Exception exception ) {
         throw e;
      }
   }

   private PathPredicateResolver findPredicateResolverFor( final Field field ) {
      final Class<?> declaredFieldType = field.getType();
      return predicateResolvers
            .stream()
            .filter( resolver -> resolver.getHandledPathType().isAssignableFrom( declaredFieldType ) )
            .findAny()
            .orElseThrow( () -> new UnsupportedFieldTypeQueryException(
                  "Type of field '" + field.getName() + "' not supported, found " + declaredFieldType ) );
   }

   private Optional<PathPredicateResolver> findPredicateResolverFor( final Method method ) {
      final Class<?> declaredFieldType = method.getReturnType();
      return predicateResolvers
            .stream()
            .filter( resolver -> resolver.getHandledPathType().isAssignableFrom( declaredFieldType ) )
            .findAny();
   }

   private void checkLikeCountExpression( final String path, final RqlFilter filter, final WildcardCount wildcardCount ) {
      if ( !RqlFilter.Operator.LIKE.equals( filter.getOperator() ) && !RqlFilter.Operator.LIKE_IGNORE_CASE.equals(
            filter.getOperator() ) ) {
         return;
      }

      final int allowedWildcards = wildcardCount.count();
      final Optional<Long> isWildcardAllowed = filter.getValues().stream().map( Object::toString )
            .map( value -> countWildcards( value, wildcardCount.wildcards() ) )
            .filter( countWildcard -> countWildcard.intValue() > allowedWildcards ).findAny();

      if ( isWildcardAllowed.isEmpty() ) {
         return;
      }

      throwWildCardException( "Too many wildcards for '" + path + "'.", wildcardCount.errorMessage() );
   }

   private void throwWildCardException( final String defaultError, final String errorMessage ) {
      if ( StringUtils.isNullOrEmpty( errorMessage ) ) {
         throw new NoSuchFieldQueryException( defaultError );
      }
      throw new NoSuchFieldQueryException( errorMessage );
   }

   private void checkLikePatternExpression( final String path, final RqlFilter filter, final RqlPattern rqlPattern ) {
      if ( !RqlFilter.Operator.LIKE.equals( filter.getOperator() ) && !RqlFilter.Operator.LIKE_IGNORE_CASE.equals(
            filter.getOperator() ) ) {
         return;
      }

      final String regex = rqlPattern.regex();
      if ( StringUtils.isNullOrEmpty( regex ) ) {
         return;
      }

      final Pattern allowedWildcards = getWildcardPattern( regex );
      final java.util.function.Predicate<String> patternPredicate = allowedWildcards.asPredicate();

      final long matches = filter.getValues().stream().map( Object::toString ).filter( patternPredicate ).count();

      if ( matches == filter.getValues().size() ) {
         return;
      }
      throwWildCardException( "Wildcard for '" + path + "' is not allowed on this position.", rqlPattern.errorMessage() );
   }

   private Pattern getWildcardPattern( final String regex ) {
      if ( PATTERN_CACHE.containsKey( regex ) ) {
         return PATTERN_CACHE.get( regex );
      }
      final Pattern allowedWildcards = Pattern.compile( regex );
      PATTERN_CACHE.put( regex, allowedWildcards );
      return allowedWildcards;
   }

   private long countWildcards( final String value, final char[] wildcards ) {
      return CharBuffer.wrap( wildcards ).chars().mapToLong( wildcard -> countWildcard( value, (char) wildcard ) ).sum();
   }

   private long countWildcard( final String value, final char wildcard ) {
      return value.chars().filter( ch -> ch == wildcard ).count();
   }

   public Optional<R> getPredicate() {
      return Optional.ofNullable( predicate );
   }

   public Optional<RqlSlice> getPagination() {
      return Optional.ofNullable( pagination );
   }

   @SuppressWarnings( "java:S1452" ) //don't return wildcard types - this seems impossible to side-step
   public List<OrderSpecifier<?>> getOrdering() {
      return ordering;
   }
}
