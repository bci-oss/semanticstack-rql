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

package com.boschsemanticstack.rql.querydsl.resolvers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.boschsemanticstack.rql.exceptions.IllegalValueTypeQueryException;
import com.boschsemanticstack.rql.exceptions.NonComparableFieldQueryException;
import com.boschsemanticstack.rql.model.v1.RqlFilter;
import com.boschsemanticstack.rql.querydsl.resolvers.converters.TypeConverters;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * Handles the {@link Predicate} resolution process for simple values that can either be single- or multi-valued.
 * Typical operations on those are {@code =}, {@code >}, {@code <} etc.
 */
@SuppressWarnings( { "java:S3740", "java:S1192" } )
// java:S3740 parameterized types - thy are not known for the expressions handled here so they cannot be given explicitely
// java:S1192 duplicated String literals ... 3 single-word occurrences while creating error messages => ignore
public class SimpleValueResolver extends AbstractPathPredicateResolver<SimpleExpression<?>, Predicate> {

   private final TypeConverters typeConverters;

   public SimpleValueResolver( final TypeConverters typeConverters ) {
      super();
      this.typeConverters = typeConverters;
   }

   @Override
   public Predicate resolve( final SimpleExpression root, final Field field, final Queue<String> pathElements, final RqlFilter filter )
         throws IllegalAccessException {
      final SimpleExpression property = field != null ? (SimpleExpression) field.get( root ) : root;
      return resolveExpression( property, filter );
   }

   @Override
   public Predicate resolveMethod( final SimpleExpression root, final Method method, final Queue<String> pathElements,
         final RqlFilter filter )
         throws ReflectiveOperationException {
      final SimpleExpression property = method != null ? (SimpleExpression) method.invoke( root ) : root;
      return resolveExpression( property, filter );
   }

   public Predicate resolveExpression( final SimpleExpression expression, final RqlFilter filter ) {
      return getPredicate( filter, filter.getValues(), expression );
   }

   private Predicate getPredicate( final RqlFilter filter, final List<Object> values, final SimpleExpression property ) {
      if ( values.size() == 1 ) {
         return getSingleValuePredicate( filter, property );
      }
      return getMultiValuePredicate( filter, values, property );
   }

   private Predicate getMultiValuePredicate( final RqlFilter filter, final List<Object> values, final SimpleExpression property ) {
      final List<Object> typeConvertedValues = values.stream()
            .map( value -> typeConverters
                  .convertTo( property.getType(), value ) )
            .collect( Collectors.toList() );

      if ( RqlFilter.Operator.IN == filter.getOperator() ) {
         return ExpressionUtils.inAny( property,
               IntStream.iterate( 0, i -> i < typeConvertedValues.size(), i -> i + 1000 ).boxed()
                     .map( i -> typeConvertedValues.subList( i, Math.min( i + 1000, typeConvertedValues.size() ) ) )
                     .collect( Collectors.toList() ) );
      } else {
         throw new IllegalValueTypeQueryException(
               "Operator " + filter.getOperator() + " not supported for multiple values for property " + property );
      }
   }

   private Predicate getSingleValuePredicate( final RqlFilter filter, final SimpleExpression property ) {
      final Object value = filter.getValue();
      if ( null == value ) {
         return getNullPredicate( filter.getOperator(), property );
      } else {
         return getValuePredicate( filter.getOperator(), property, value );
      }
   }

   private Predicate getNullPredicate( final RqlFilter.Operator operator, final SimpleExpression property ) {
      return switch ( operator ) {
         case EQ -> property.isNull();
         case NE -> property.isNotNull();
         default -> throw new IllegalValueTypeQueryException(
               "Operator " + operator + " not supported with null values for property " + property );
      };
   }

   @SuppressWarnings( { "unchecked", "squid:MethodCyclomaticComplexity" } )
   private BooleanExpression getValuePredicate( final RqlFilter.Operator operator, final SimpleExpression property,
         final Object value ) {
      final Object convertedValue = typeConverters.convertTo( property.getType(), value );

      if ( !property.getType().isAssignableFrom( convertedValue.getClass() ) ) {
         throw new IllegalValueTypeQueryException(
               "Invalid value type " + convertedValue.getClass().getSimpleName() + " for property " + property );
      }

      return switch ( operator ) {
         case EQ, IN -> property.eq( convertedValue );
         case NE -> property.ne( convertedValue );
         case GT -> compare( property, convertedValue, ComparableExpression::gt, NumberExpression::gt );
         case GE -> compare( property, convertedValue, ComparableExpression::goe, NumberExpression::goe );
         case LT -> compare( property, convertedValue, ComparableExpression::lt, NumberExpression::lt );
         case LE -> compare( property, convertedValue, ComparableExpression::loe, NumberExpression::loe );
         case LIKE -> stringCompare( property, convertedValue, ( p, v ) -> p.like( convertLikeWildcards( v ) ) );
         case LIKE_IGNORE_CASE -> stringCompare( property, convertedValue, ( p, v ) -> p.likeIgnoreCase( convertLikeWildcards( v ) ) );
         default -> throw new IllegalArgumentException(
               "Operator " + operator + " not supported for single value for property " + property );
      };
   }

   private BooleanExpression compare( final SimpleExpression property, final Object value,
         final ComparableComparisonProvider comparableComparator,
         final NumberComparisonProvider numberComparator ) {
      if ( property instanceof final ComparableExpression comparableExpression ) {
         if ( value instanceof final Comparable comparable ) {
            return comparableComparator.get( comparableExpression, comparable );
         } else {
            throw new IllegalValueTypeQueryException(
                  "Illegal value type for property " + property + ", expected (Comparable) but found " + value
                        .getClass().getSimpleName() );
         }
      } else if ( property instanceof final NumberExpression numberExpression ) {
         if ( value instanceof Number && value instanceof final Comparable comparable ) {
            return numberComparator.get( numberExpression, (Number) comparable );
         } else {
            throw new IllegalValueTypeQueryException(
                  "Illegal value type for property " + property + ", expected (Number & Comparable), found " + value
                        .getClass().getSimpleName() );
         }
      }
      throw new NonComparableFieldQueryException( property.toString() );
   }

   private String convertLikeWildcards( final String v ) {
      return v.replace( "*", "%" ).replace( "?", "_" );
   }

   private BooleanExpression stringCompare( final SimpleExpression property, final Object value,
         final StringComparisonProvider comparator ) {
      if ( property instanceof final StringExpression stringExpression ) {
         return comparator.get( stringExpression, (String) value );
      }
      throw new NonComparableFieldQueryException( property.toString() );
   }

   private interface ComparisonProvider<P, V, R> {
      R get( P property, V value );
   }

   private interface StringComparisonProvider extends
         ComparisonProvider<StringExpression, String, BooleanExpression> {
   }

   private interface ComparableComparisonProvider extends
         ComparisonProvider<ComparableExpression, Comparable, BooleanExpression> {
   }

   private interface NumberComparisonProvider<T extends Number & Comparable<?>> {
      BooleanExpression get( NumberExpression property, T value );
   }
}
