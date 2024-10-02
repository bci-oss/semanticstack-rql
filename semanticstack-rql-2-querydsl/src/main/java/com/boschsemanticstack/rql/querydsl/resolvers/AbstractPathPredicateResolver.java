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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

import com.boschsemanticstack.rql.exceptions.NoSuchFieldQueryException;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.SimpleExpression;
import jakarta.validation.constraints.NotNull;

/**
 * Common base class for all {@link PathPredicateResolver}s.
 * <br/>
 * Captures the generic path type the actual implementation handles to allow for a lookup of a matching predicate
 * resolver.
 *
 * @param <T> the type of property path expression this class can handle
 * @param <P> the type of the Predicate
 */
@SuppressWarnings( { "java:S3740" } )
// java:S3740 parameterized types - they are not known for the expressions handled here so they cannot be given explicitly
public abstract class AbstractPathPredicateResolver<T extends Expression<?>, P> implements PathPredicateResolver<T, P> {
 
   private final Class<T> handledPathType = getRawClassOfFirstTypeParameter();

   /**
    * do this one time instead of on each resolution
    */
   @SuppressWarnings( "unchecked" )
   private Class<T> getRawClassOfFirstTypeParameter() {
      final ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass(); // we KNOW it's parameterized ;)
      final Type typeOfFirstArgument = superclass.getActualTypeArguments()[0];
      return typeOfFirstArgument instanceof ParameterizedType
            ? (Class<T>) ((ParameterizedType) typeOfFirstArgument).getRawType()
            : (Class<T>) typeOfFirstArgument;
   }

   @Override
   public Class<T> getHandledPathType() {
      return handledPathType;
   }

   @NotNull
   protected Field getField( final SimpleExpression path, final String fieldName ) {
      final Class<? extends SimpleExpression> typeOfElementAtPath = path.getClass(); // either NPE on path or not null
      try {
         return typeOfElementAtPath.getField( fieldName ); // not null as per contract
      } catch ( final NoSuchFieldException e ) {
         return Arrays.stream( typeOfElementAtPath.getFields() )
               .filter( field -> field.getName().equalsIgnoreCase( fieldName ) )
               .findAny()// not null or exception
               .orElseThrow(
                     () -> new NoSuchFieldQueryException( "Field '" + fieldName + "' does not exist.", e ) );
      }
   }

   protected Optional<Method> getMethod( final SimpleExpression path, final String methodName ) {
      final Class<? extends SimpleExpression> typeOfElementAtPath = path.getClass();
      try {
         return Optional.of( typeOfElementAtPath.getMethod( methodName ) );
      } catch ( final NoSuchMethodException e ) {
         return Optional.empty();
      }
   }

   protected Optional<Method> getMethod( final SimpleExpression path, final String methodName, final Class<?>... parameterTypes ) {
      final Class<? extends SimpleExpression> typeOfElementAtPath = path.getClass();
      try {
         return Optional.of( typeOfElementAtPath.getMethod( methodName, parameterTypes ) );
      } catch ( final NoSuchMethodException e ) {
         return Optional.empty();
      }
   }
}
